// ///////////////////////////////////////////////////////////////////
// Copyright (c) by Ion Beam Applications S.A.
// All rights reserved
//
// Rue du cyclotron, 3
// B-1348 Louvain-la-Neuve
// Tel : +32.10.47.58.11
// ///////////////////////////////////////////////////////////////////

package com.iba.pts.bms.bss.beamscheduler.proxy;

import com.iba.blak.device.api.Insertable;
import com.iba.ialign.Controller;
import com.iba.ialign.Gui;
import com.iba.icomp.core.component.AbstractProxy;
import com.iba.icomp.core.component.ComponentDictionary;
import com.iba.icomp.core.component.ComponentProperty;
import com.iba.icomp.core.component.ParameterCompatible;
import com.iba.icomp.core.component.XStreamCompatible;
import com.iba.icomp.core.util.Logger;
import com.iba.pts.bms.bss.beamscheduler.api.*;
import com.iba.pts.bms.bss.datatypes.api.BssSettings;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Proxy of a {@link BeamScheduler}.
 * <p>
 * <b>Initialization:</b> (= should be called before using this class)
 * <ul>
 * <li>{@link #setName(String)},</li>
 * <li>{@link #setComponentName(String)},</li>
 * <li>{@link #setContainer(com.iba.icomp.core.container.Container)} (optional),</li>
 * <li>{@link #setEventBus(com.iba.icomp.core.event.EventBus)},</li>
 * <li>{@link #setEventFactory(com.iba.icomp.core.event.EventFactory)},</li>
 * <li>{@link #setPropertyDefinitionDictionary(com.iba.icomp.core.property.ListablePropertyDefinitionDictionary)}</li>
 * </ul>
 */
@ComponentDictionary(classpaths = { "classpath:config/bms/bss/controller/api/properties/beam-scheduler-properties.xml" })
public class BeamSchedulerProxy extends AbstractProxy implements BeamScheduler, BeamSchedulerControl
{

    protected static final Logger LOGGER = Logger.getLogger(BeamSchedulerProxy.class);

    // ////////////////////////////////////////////////
    // Implementation of the BeamScheduler interface //
    // ////////////////////////////////////////////////

    /**
     * {@inheritDoc}
     * <p>
     * Returns the last version received from the Beam Scheduler.
     */
    @Override
    public BeamAllocation getCurrentBeamAllocation()
    {
        return mCurrentBeamAllocation;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Returns the last version received from the Beam Scheduler.
     */
    @Override
    public List<PendingBeamRequest> getPendingBeamRequests()
    {
        return mPendingBeamRequests;
    }

    @Override
    public void requestBeam(String pBeamSupplyPointId, BeamPriority pPriority, int pRequestedTime, BssSettings pBssSettings)
    {
        LOGGER.debug("Requesting beam for beam supply point %s with priority %s and requested time of %d seconds",
                pBeamSupplyPointId, pPriority, pRequestedTime);
        proxyRequest("requestBeam", pBeamSupplyPointId, pPriority, pRequestedTime, pBssSettings);
    }

    @Override
    public void cancelBeamRequest(String pBeamSupplyPointId)
    {
        LOGGER.debug("Canceling beam request for beam supply point %s", pBeamSupplyPointId);
        proxyRequest("cancelBeamRequest", pBeamSupplyPointId);
    }

    @Override
    public void releaseBeam(String pBeamSupplyPointId, boolean pReallocate)
    {
        LOGGER.info("Preparing to release beam...");
        BeamAllocation allocation = Controller.beam.beamScheduler.getCurrentBeamAllocation();
        if (allocation != null) {
            if (allocation.getBeamSupplyPointId().contains("IBTR3")) {
                //Controller.beam.bssController.startIdleActivity(Controller.beam.beamScheduler.getCurrentBeamAllocation().getBeamSupplyPointId());
                //controller.prepareForTreatment();
                Controller.beam.bpsController.startIdleActivity();
                Controller.beam.smpsController.startIdleActivity();
                try {
                    Controller.beam.bssController.startDisableBeamActivity(Controller.beam.beamScheduler.getCurrentBeamAllocation().getBeamSupplyPointId());
                    Thread.sleep(1000);
                    if (Controller.S2E.getPosition() == Insertable.Position.MOVING || Controller.S2E.getPosition() == Insertable.Position.INSERTED) {
                        //do nothing
                    } else {
                        Controller.ecubtcu.bsInsert("S2E");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                LOGGER.debug("Releasing beam for beam supply point %s%s", pBeamSupplyPointId, pReallocate ? " with reallocation" : "");
                proxyRequest("releaseBeam", pBeamSupplyPointId, pReallocate);
                //Controller.beam.beamScheduler.releaseBeam(Controller.beam.beamScheduler.getCurrentBeamAllocation().getBeamSupplyPointId(), false);
            }
        }
    }

    @Override
    public boolean isSchedulingAutomatic()
    {
        return mAutomaticScheduling;
    }

    // //////////////////////////////////////////////////////////////////////////

    @ComponentProperty(definition = "BeamScheduler.CurrentBeamAllocation")
    @ParameterCompatible(XStreamCompatible.class)
    private BeamAllocation mCurrentBeamAllocation = null;

    @ComponentProperty(definition = "BeamScheduler.PendingBeamRequests")
    @ParameterCompatible(XStreamCompatible.class)
    private List<PendingBeamRequest> mPendingBeamRequests = new LinkedList<>();

    @ComponentProperty(definition = "BeamScheduler.AutomaticScheduling")
    private boolean mAutomaticScheduling;

    @Override
    public Set<String> getBeamSupplyPointsIds() {
        return null;
    }

    @Override
    public void setAutomaticScheduling(boolean pAutomaticScheduling) {

    }

    @Override
    public void cancelPendingBeamRequests() {

    }

    @Override
    public void cancelPendingBeamRequests(BeamPriority pPriority) {

    }

    @Override
    public void stepUpBeamRequest(String pBeamSupplyPointId) {

    }

    @Override
    public void stepDownBeamRequest(String pBeamSupplyPointId) {

    }

    @Override
    public void acceptBeamRequest(String pBeamSupplyPointId) {

    }

    @Override
    public void rejectBeamRequest(String pBeamSupplyPointId) {

    }
}