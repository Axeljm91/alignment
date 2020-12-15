package com.iba.pts.bms.bss.esbts.controller.impl;

import com.iba.icomp.core.checks.Check;
import com.iba.icomp.core.checks.CheckManager;
import com.iba.icomp.core.checks.CheckResult;
import com.iba.icomp.core.component.ComponentMethod;
import com.iba.icomp.core.property.Property;
import com.iba.icomp.devices.Device;
import com.iba.icompx.core.activity.ActivityStatus;
import com.iba.pts.bms.bss.beamscheduler.api.BeamAllocation;
import com.iba.pts.bms.bss.beamscheduler.api.BeamScheduler;
import com.iba.pts.bms.bss.controller.impl.CheckedAbstractActivityController;
import com.iba.pts.bms.bss.datatypes.api.EsBtsResumeCondition;
import com.iba.pts.bms.bss.esbts.Beamline;
import com.iba.pts.bms.bss.esbts.BeamlineSection;
import com.iba.pts.bms.bss.esbts.BeamlinesInfrastructure;
import com.iba.pts.bms.bss.esbts.controller.api.EsBtsActivityId;
import com.iba.pts.bms.bss.esbts.controller.api.EsBtsController;
import com.iba.pts.bms.bss.esbts.controller.impl.activity.AbstractEsBtsActivity;
import com.iba.pts.bms.bss.esbts.controller.impl.activity.EsBtsActivity;
import com.iba.pts.bms.bss.esbts.controller.impl.activity.EsBtsDisableBeamActivity;
import com.iba.pts.bms.bss.esbts.controller.impl.activity.EsBtsIdleActivity;
import com.iba.pts.bms.bss.esbts.controller.impl.activity.EsBtsPrepareWithDeviceSettingsActivity;
import com.iba.pts.bms.bss.esbts.controller.impl.activity.EsBtsSetRangeActivity;
import com.iba.pts.bms.bss.esbts.controller.impl.activity.selectbeamline.EsBtsSelectBeamlineActivity;
import com.iba.pts.bms.datatypes.api.TreatmentMode;
import com.iba.tcs.beam.bss.devices.api.Degrader;
import com.iba.tcs.beam.bss.devices.api.Slits;
import com.iba.tcs.beam.bss.devices.api.Valve;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.util.Assert;

public class EsBtsControllerImpl extends CheckedAbstractActivityController<EsBtsActivityId> implements EsBtsController {
    public static final String ES_BTS_CONTROLLER_PROXY = "esBtsControllerProxy";
    private BeamScheduler mBeamScheduler;
    private BeamlinesInfrastructure mBeamlinesInfrastructure;
    private CheckManager mCheckManager;
    private Set<String> mChecksRequiringSetRange;
    private Map<String, Boolean> mIsSetRangeRequired = new HashMap();

    public EsBtsControllerImpl() {
        super(EsBtsActivityId.IDLE);
    }

    public void setActivities(List<EsBtsActivity> pActivities) {
        super.setControllerActivities(pActivities);
        Iterator var2 = pActivities.iterator();

        while(var2.hasNext()) {
            EsBtsActivity activity = (EsBtsActivity)var2.next();
            activity.setEsBtsController(this);
        }

    }

    public void setBeamScheduler(BeamScheduler pBeamScheduler) {
        this.mBeamScheduler = pBeamScheduler;
    }

    public void setBeamlinesInfrastructure(BeamlinesInfrastructure pBeamlinesInfrastructure) {
        this.mBeamlinesInfrastructure = pBeamlinesInfrastructure;
    }

    public BeamlinesInfrastructure getBeamlinesInfrastructure() {
        return this.mBeamlinesInfrastructure;
    }

    public void setCheckManager(CheckManager pCheckManager) {
        this.mCheckManager = pCheckManager;
    }

    public void setChecksRequiringSetRange(Set<String> pChecksRequiringSetRange) {
        this.mChecksRequiringSetRange = pChecksRequiringSetRange;
    }

    protected void componentInitialized() {
        this.processPublish();
        Iterator var1 = this.mBeamlinesInfrastructure.getBeamSupplyPointsIds().iterator();

        while(var1.hasNext()) {
            String beamSupplyPointId = (String)var1.next();
            Iterator var3 = this.mChecksRequiringSetRange.iterator();

            while(var3.hasNext()) {
                String checkSimpleName = (String)var3.next();
                String checkName = beamSupplyPointId + '.' + checkSimpleName;
                Check check = this.mCheckManager.getCheck(checkName);
                check.addPropertyChangeListener((pEvt) -> {
                    if ("result".equals(pEvt.getPropertyName()) && CheckResult.ERROR == pEvt.getNewValue()) {
                        this.mIsSetRangeRequired.put(beamSupplyPointId, Boolean.TRUE);
                    }

                });
            }
        }

        AbstractEsBtsActivity setRangeActivity = (AbstractEsBtsActivity)this.getActivity(EsBtsActivityId.SET_RANGE);
        setRangeActivity.addPropertyChangeListener((pEvt) -> {
            if ("status".equals(pEvt.getPropertyName()) && ActivityStatus.COMPLETED == pEvt.getNewValue()) {
                this.mIsSetRangeRequired.put(setRangeActivity.getBeamline().getBeamSupplyPointId(), Boolean.FALSE);
            }

        });
    }

    protected void containerStopping() {
        super.containerStopping();
        this.getLogger().info("Container is stopping: insert all beam stops", new Object[0]);
        Iterator var1 = this.mBeamlinesInfrastructure.getBeamlines().iterator();

        while(var1.hasNext()) {
            Beamline beamline = (Beamline)var1.next();
            this.getLogger().info("Inserting beam stops of beamline of beam supply point " + beamline.getBeamSupplyPointId(), new Object[0]);
            beamline.disableBeam(false);
        }

    }

    protected void enableActivity(EsBtsActivityId pId) {
        this.unlockProperties();
        super.enableActivity(pId);
    }

    protected void processValues(String pName, Set<Property<?>> pSet) {
        super.processValues(pName, pSet);
        Iterator var3 = pSet.iterator();

        while(var3.hasNext()) {
            Property<?> prop = (Property)var3.next();
            if (prop.getPropertyDefinition().getId().endsWith("CU.state")) {
                Integer status = (Integer)prop.getValue();
                EsBtsIdleActivity idle = (EsBtsIdleActivity)this.getActivity(EsBtsActivityId.IDLE);
                idle.setEcubtcuRebooted(status > 1);
            }
        }

    }

    public EsBtsResumeCondition getResumeCondition(String pBeamSupplyPointId) {
        Beamline beamline = this.mBeamlinesInfrastructure.getBeamline(pBeamSupplyPointId);
        Assert.notNull(beamline);
        if (!this.getFailedCriticalChecks().isEmpty()) {
            this.getLogger().warn("A critical check is not fulfilled!", new Object[0]);
            return EsBtsResumeCondition.ResumeNotAllowed;
        } else {
            Iterator var3 = beamline.getSections().iterator();

            BeamlineSection section;
            Iterator var5;
            Device device;
            while(var3.hasNext()) {
                section = (BeamlineSection)var3.next();
                var5 = section.getRangeControlDevices(beamline.getSelectedOpticalSolution()).iterator();

                while(var5.hasNext()) {
                    device = (Device)var5.next();
                    if (device instanceof Valve) {
                        Valve valve = (Valve)device;
                        if (!valve.isValveOpened() || valve.isValveClosed()) {
                            this.getLogger().warn("Valve [%s] is not opened!", new Object[]{valve.getDeviceName()});
                            return EsBtsResumeCondition.ResumeNotAllowed;
                        }
                    }
                }
            }

            if (beamline.isRecyclingRequired()) {
                this.getLogger().warn("Beamline RecyclingRequired!", new Object[0]);
                return EsBtsResumeCondition.ResumeAllowedWithSetRangeAndCycling;
            } else if ((Boolean)this.mIsSetRangeRequired.get(pBeamSupplyPointId)) {
                this.getLogger().warn("A check requiring set range has failed!", new Object[0]);
                return EsBtsResumeCondition.ResumeAllowedWithSetRangeAndNoCycling;
            } else {
                var3 = beamline.getSections().iterator();

                while(var3.hasNext()) {
                    section = (BeamlineSection)var3.next();
                    var5 = section.getRangeControlDevices(beamline.getSelectedOpticalSolution()).iterator();

                    while(var5.hasNext()) {
                        device = (Device)var5.next();
                        if (device instanceof Degrader) {
                            Degrader degrader = (Degrader)device;
                            if (degrader.getRequestedPosition() != degrader.getPosition() || !degrader.isOperational()) {
                                this.getLogger().warn("Degrader not at requested position or not operational!", new Object[0]);
                                return EsBtsResumeCondition.ResumeAllowedWithSetRangeAndNoCycling;
                            }
                        } else if (device instanceof Slits) {
                            Slits slits = (Slits)device;
                            if (!slits.checkTolerance()) {
                                this.getLogger().warn("Slits [%s] position not within tolerance!", new Object[]{slits.getDeviceName()});
                                return EsBtsResumeCondition.ResumeAllowedWithSetRangeAndNoCycling;
                            }
                        }
                    }
                }

                this.getLogger().info("ES/BTS getResumeConditions(%s) returning %s.", new Object[]{pBeamSupplyPointId, EsBtsResumeCondition.ResumeAllowedWithoutSetRange});
                return EsBtsResumeCondition.ResumeAllowedWithoutSetRange;
            }
        }
    }

    public void getEsBtsDeviceSettings() {
        if (this.getCurrentActivity().getStatus() != ActivityStatus.ONGOING && this.getActivity(EsBtsActivityId.SELECT_BEAMLINE).getStatus() == ActivityStatus.COMPLETED) {
            EsBtsSelectBeamlineActivity selectBeamLine = (EsBtsSelectBeamlineActivity)this.getActivity(EsBtsActivityId.SELECT_BEAMLINE);
            this.getCallback().callSucceeded(new Object[]{selectBeamLine.getBeamline().saveBeamlineSettings()});
        } else {
            String message = "Beamline settings cannot be generated: ";
            if (this.getCurrentActivity().getStatus() == ActivityStatus.ONGOING) {
                message = message + "status of current activity (";
                message = message + this.getCurrentActivity().toString();
                message = message + ") is " + this.getCurrentActivity().getStatus();
            } else {
                message = message + "no beamline is selected";
            }

            this.getCallback().callFailed(new Object[]{message});
        }

    }

    @ComponentMethod
    public void startIdleActivity() {
        this.getLogger().info("Start of ES/BTS Controller IDLE activity requested", new Object[0]);
        this.enableActivity(EsBtsActivityId.IDLE);
    }

    @ComponentMethod
    public void startSelectBeamlineActivity(String pBeamSupplyPointId, TreatmentMode pTreatmentMode) {
        this.getLogger().info("Start of ES/BTS Controller SELECT_BEAMLINE activity requested for beam supply point %s for %s treatment mode", new Object[]{pBeamSupplyPointId, pTreatmentMode});
        BeamAllocation beamAllocation = this.mBeamScheduler.getCurrentBeamAllocation();
        if (beamAllocation != null && pBeamSupplyPointId.equals(beamAllocation.getBeamSupplyPointId())) {
            Beamline beamline = this.mBeamlinesInfrastructure.getBeamline(pBeamSupplyPointId);
            if (beamline != null) {
                this.mIsSetRangeRequired.put(pBeamSupplyPointId, Boolean.TRUE);
                ((EsBtsActivity)this.getActivity(EsBtsActivityId.SELECT_BEAMLINE)).setBeamline(beamline);
                ((EsBtsSelectBeamlineActivity)this.getActivity(EsBtsActivityId.SELECT_BEAMLINE)).setTreatmentMode(pTreatmentMode);
                this.enableActivity(EsBtsActivityId.SELECT_BEAMLINE);
            } else {
                this.getCallback().callFailed(new Object[0]);
            }

        } else {
            this.getLogger().error("Cannot start ES/BTS Controller SELECT_BEAMLINE activity for beam supply point %s because beam is %s", new Object[]{pBeamSupplyPointId, beamAllocation == null ? "not allocated" : "allocated to beam supply point " + beamAllocation.getBeamSupplyPointId()});
            this.getCallback().callFailed(new Object[0]);
        }
    }

    @ComponentMethod
    public void startSetRangeActivity(String pBeamSupplyPointId, double pRange, String pOpticalSolution, double pGantryAngle, double pOffsetX, double pOffsetY, boolean pCycling) {
        this.getLogger().info("Start of ES/BTS Controller SET_RANGE activity requested for beam supply point %s: range=%s, optical solution=%s, gantry angle=%s, offsetX=%s, offsetY=%s, cycling=%b", new Object[]{pBeamSupplyPointId, pRange, pOpticalSolution, pGantryAngle, pOffsetX, pOffsetY, pCycling});
        BeamAllocation beamAllocation = this.mBeamScheduler.getCurrentBeamAllocation();
        if (beamAllocation != null && pBeamSupplyPointId.equals(beamAllocation.getBeamSupplyPointId())) {
            Beamline beamline = this.mBeamlinesInfrastructure.getBeamline(pBeamSupplyPointId);
            if (beamline != null) {
                this.mIsSetRangeRequired.put(pBeamSupplyPointId, Boolean.TRUE);
                ((EsBtsActivity)this.getActivity(EsBtsActivityId.SET_RANGE)).setBeamline(beamline);
                ((EsBtsSetRangeActivity)this.getActivity(EsBtsActivityId.SET_RANGE)).setRangeParameters(pRange, pOpticalSolution, pGantryAngle, pOffsetX, pOffsetY, pCycling);
                this.enableActivity(EsBtsActivityId.SET_RANGE);
            } else {
                this.getCallback().callFailed(new Object[0]);
            }

        } else {
            this.getLogger().error("Cannot start ES/BTS Controller SET_RANGE activity for beam supply point %s because beam is %s", new Object[]{pBeamSupplyPointId, beamAllocation == null ? "not allocated" : "allocated to beam supply point " + beamAllocation.getBeamSupplyPointId()});
            this.getCallback().callFailed(new Object[0]);
        }
    }

    @ComponentMethod
    public void startEnableBeamActivity(String pBeamSupplyPointId) {
        this.getLogger().info("Start of ES/BTS Controller ENABLE_BEAM activity requested for beam supply point %s", new Object[]{pBeamSupplyPointId});
        BeamAllocation beamAllocation = this.mBeamScheduler.getCurrentBeamAllocation();
        if (beamAllocation != null && pBeamSupplyPointId.equals(beamAllocation.getBeamSupplyPointId())) {
            Beamline beamline = this.mBeamlinesInfrastructure.getBeamline(pBeamSupplyPointId);
            if (beamline != null) {
                ((EsBtsActivity)this.getActivity(EsBtsActivityId.ENABLE_BEAM)).setBeamline(beamline);
                this.enableActivity(EsBtsActivityId.ENABLE_BEAM);
            } else {
                this.getCallback().callFailed(new Object[0]);
            }

        } else {
            this.getLogger().error("Cannot start ES/BTS Controller ENABLE_BEAM activity for beam supply point %s because beam is %s", new Object[]{pBeamSupplyPointId, beamAllocation == null ? "not allocated" : "allocated to beam supply point " + beamAllocation.getBeamSupplyPointId()});
            this.getCallback().callFailed(new Object[0]);
        }
    }

    @ComponentMethod
    public void startDisableBeamActivity(String pBeamSupplyPointId) {
        this.getLogger().info("Start of ES/BTS Controller DISABLE_BEAM activity requested for beam supply point %s", new Object[]{pBeamSupplyPointId});
        BeamAllocation beamAllocation = this.mBeamScheduler.getCurrentBeamAllocation();
        if (beamAllocation != null && pBeamSupplyPointId.equals(beamAllocation.getBeamSupplyPointId())) {
            Beamline beamline = this.mBeamlinesInfrastructure.getBeamline(pBeamSupplyPointId);
            if (beamline != null) {
                EsBtsDisableBeamActivity activity = (EsBtsDisableBeamActivity)this.getActivity(EsBtsActivityId.DISABLE_BEAM);
                activity.setBeamline(beamline);
                this.enableActivity(EsBtsActivityId.DISABLE_BEAM);
            } else {
                this.getCallback().callFailed(new Object[0]);
            }

        } else {
            this.getLogger().error("Cannot start ES/BTS Controller DISABLE_BEAM activity for beam supply point %s because beam is %s", new Object[]{pBeamSupplyPointId, beamAllocation == null ? "not allocated" : "allocated to beam supply point " + beamAllocation.getBeamSupplyPointId()});
            this.getCallback().callFailed(new Object[0]);
        }
    }

    @ComponentMethod
    public void startPrepareWithDeviceSettingsActivity(String pDeviceSettings, boolean pCycling) {
        this.getLogger().info("Start of ES/BTS Controller PREPARE_WITH_DEVICE_SETTINGS activity requested", new Object[0]);
        if (this.getActivity(EsBtsActivityId.SELECT_BEAMLINE).getStatus() == ActivityStatus.COMPLETED) {
            Beamline beamline = ((EsBtsSelectBeamlineActivity)this.getActivity(EsBtsActivityId.SELECT_BEAMLINE)).getBeamline();
            if (beamline != null) {
                ((EsBtsActivity)this.getActivity(EsBtsActivityId.PREPARE_WITH_DEVICE_SETTINGS)).setBeamline(beamline);
                ((EsBtsPrepareWithDeviceSettingsActivity)this.getActivity(EsBtsActivityId.PREPARE_WITH_DEVICE_SETTINGS)).setBeamlineSettings(pDeviceSettings, pCycling);
                this.enableActivity(EsBtsActivityId.PREPARE_WITH_DEVICE_SETTINGS);
            } else {
                this.getCallback().callFailed(new Object[0]);
            }
        } else {
            this.getLogger().error("Status of activity SELECT_BEAMLINE is not completed!", new Object[0]);
            this.getCallback().callFailed(new Object[0]);
        }

    }

    @ComponentMethod
    public void emergencyDisableBeam(String pBeamSupplyPointId) {
        this.getLogger().info("Emergency disable beam requested for beam supply point %s", new Object[]{pBeamSupplyPointId});
        Beamline beamline = this.mBeamlinesInfrastructure.getBeamline(pBeamSupplyPointId);
        if (beamline != null) {
            beamline.disableBeam(true);
            this.getCallback().callSucceeded(new Object[0]);
        } else {
            this.getCallback().callFailed(new Object[0]);
        }

    }

    @ComponentMethod
    public void startSavePowerActivity() {
        this.getLogger().info("Start of ES/BTS Controller SAVE_POWER activity requested", new Object[0]);
        this.enableActivity(EsBtsActivityId.SAVE_POWER);
    }
}
