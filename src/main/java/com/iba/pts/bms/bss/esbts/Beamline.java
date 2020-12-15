// ///////////////////////////////////////////////////////////////////
// Copyright (c) by Ion Beam Applications S.A.
// All rights reserved
//
// Rue du cyclotron, 3
// B-1348 Louvain-la-Neuve
// Tel : +32.10.47.58.11
// ///////////////////////////////////////////////////////////////////

package com.iba.pts.bms.bss.esbts;

import com.iba.icomp.core.util.Assert;
import com.iba.icomp.core.util.Logger;
import com.iba.icomp.devices.Device;
import com.iba.pts.bms.bss.esbts.blpscu.api.Blpscu;
import com.iba.pts.bms.bss.esbts.solution.RangeConverter;
import com.iba.tcs.beam.bss.devices.api.BeamStop;
import com.iba.tcs.beam.bss.devices.api.Bpm;
import com.iba.tcs.beam.bss.devices.api.Degrader;
import com.iba.tcs.beam.bss.devices.api.Group3;
import com.iba.tcs.beam.bss.devices.api.Magnet;
import com.iba.tcs.beam.bss.devices.api.Magnet.Applicability;
import com.iba.tcs.beam.bss.devices.api.Slits;
import com.iba.tcs.beam.bss.devices.api.gateway.rpc.Ecubtcu2;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
/**
 * Class representing a beamline.
 * <p>
 * <b>Initialization:</b> (= should be called before using this class)
 * <ul>
 * <li>{@link #setBlpscu(Blpscu)},</li>
 * <li>{@link #setFastSwitching(boolean)} (optional)</li>
 * </ul>
 */
public class Beamline
{

    /** "Range as requested" property name. */
    public static final String RANGE_AS_REQUESTED_PROPERTY = "rangeAsRequested";
    /** "Range reached on magnets" property name. */
    public static final String RANGE_MAGNETS_AS_REQUESTED_PROPERTY = "rangeMagnetsAsRequested";
    /** "Beam enable as requested" property name. */
    public static final String BEAM_ENABLE_AS_REQUESTED_PROPERTY = "beamEnableAsRequested";
    /** "Beam disable as requested" property name. */
    public static final String BEAM_DISABLE_AS_REQUESTED_PROPERTY = "beamDisableAsRequested";
    /** "Magnets power supplies off as requested" property name. */
    public static final String MAGNETS_POWER_SUPPLIES_OFF_AS_REQUESTED_PROPERTY = "magnetsPowerSuppliesOffAsRequested";
    /** "Magnets power supplies on as requested" property name. */
    public static final String MAGNETS_POWER_SUPPLIES_ON_AS_REQUESTED_PROPERTY = "magnetsPowerSuppliesOnAsRequested";

    static final String SEPARATOR = ";";
    static final String LINE_END = "\n";
    static final int FIELD_COUNT = 4;
    static final int DEVICE_NAME_INDEX = 2;
    static final int DEVICE_SETTING_INDEX = 3;

    protected static final Logger LOGGER = Logger.getLogger(Beamline.class);

    /**
     * Constructs a Beamline.
     * @param pBeamSupplyPointId the beam supply point id
     * @param pTreatmentRoomId the treatment room id
     * @param pBeamlineId the beamline id
     * @param pRangeConverters the map of optical solutions id to range converters
     * @param pBeamlineSections the beamline sections, the first one must be the ESS
     * @param pOffMagnets Set of Magnet from OTHER BEAMLINES that have to be OFF when Enable Beam is called on this beamline.
     */
    public Beamline(String pBeamSupplyPointId, int pTreatmentRoomId, int pBeamlineId, Map<String, RangeConverter> pRangeConverters,
                    List<BeamlineSection> pBeamlineSections, Set<Magnet> pOffMagnets)
    {
        mBeamSupplyPointId = pBeamSupplyPointId;
        mTreatmentRoomId = pTreatmentRoomId;
        mBeamlineId = pBeamlineId;
        mRangeConverters = pRangeConverters;
        mSections = pBeamlineSections;
        mOffMagnets = pOffMagnets;

        // add devices listeners
        RangeControlDeviceListener rangeControlDeviceListener = new RangeControlDeviceListener();
        MagnetListener magnetListener = new MagnetListener();
        BeamEnableDeviceListener beamEnableDeviceListener = new BeamEnableDeviceListener();
        for (BeamlineSection section : mSections)
        {
            for (Device device : section.getRangeControlDevices())
            {
                device.addPropertyChangeListener(rangeControlDeviceListener);
                if (device instanceof Magnet)
                {
                    device.addPropertyChangeListener(magnetListener);
                }
            }

            for (Device device : section.getBeamEnableDevices())
            {
                device.addPropertyChangeListener(beamEnableDeviceListener);
            }
        }

        // get room entrance bending magnet if any
        for (BeamlineSection section : mSections)
        {
            for (Device device : section.getRangeControlDevices())
            {
                if (device instanceof Magnet)
                {
                    Magnet magnet = (Magnet) device;
                    if (magnet.isRoomEntranceMagnet())
                    {
                        Assert.isNull(mRoomEntranceBendingMagnet,
                                "Several beamline magnets have been defined as room entrance bending magnet which is not supported");
                        LOGGER.info("Beamline %s (%s) room entrance bending magnet is %s", pBeamlineId, pBeamSupplyPointId,
                                magnet.getDeviceName());
                        mRoomEntranceBendingMagnet = magnet;
                        mRoomEntranceBendingMagnet.addPropertyChangeListener(new RoomEntranceBendingMagnetListener());
                    }
                }
            }
        }
        if (mRoomEntranceBendingMagnet == null)
        {
            LOGGER.warn("Beamline " + pBeamlineId + " (" + pBeamSupplyPointId + ") has no room entrance bending magnet defined");
        }
    }

    /**
     * Sets the reference to the BLPSCU proxy.
     * @param pBlpscu the BLPSCU proxy
     */
    public void setBlpscu(Blpscu pBlpscu)
    {
        mBlpscu = pBlpscu;
    }

    /**
     * Sets the reference to the ECUBTCU proxy.
     * @param pEcubtcu the ECUBTCU proxy
     */
    public void setEcubtcu(Ecubtcu2 pEcubtcu)
    {
        mEcubtcu = pEcubtcu;
    }

    /**
     * Sets if fast switching is supported by this beamline or not.
     * <p>
     * Fast switching means switching the power supplies without waiting that the current feedback is near zero. It requires
     * specific devices developed by JEMA to be installed on the beamline.
     * @param pFastSwitching true if fast switching is supported by this beamline, false otherwise. Default is false.
     */
    public void setFastSwitching(boolean pFastSwitching)
    {
        mFastSwitching = pFastSwitching;
    }

    /**
     * Checks if beamline fast switching is enabled.
     * @return true if fast switching is enabled for this beamline, false otherwise
     */
    public boolean isFastSwitchingEnabled()
    {
        return mFastSwitching;
    }

    /**
     * Gets the beam supply point id.
     * @return the beam supply point id
     */
    public String getBeamSupplyPointId()
    {
        return mBeamSupplyPointId;
    }

    /**
     * Gets the treatment room id.
     * @return the treatment room id
     */
    public int getTreatmentRoomId()
    {
        return mTreatmentRoomId;
    }

    /**
     * Gets the beamline id.
     * @return the beamline id
     */
    public int getBeamlineId()
    {
        return mBeamlineId;
    }

    /**
     * Gets the optical solutions id.
     * @return the set of optical solutions id
     */
    public Set<String> getOpticalSolutions()
    {
        return mRangeConverters.keySet();
    }

    /**
     * Gets the beamline sections.
     * @return the beamline sections
     */
    public List<BeamlineSection> getSections()
    {
        return mSections;
    }

    /**
     * @return Set of Magnet from OTHER BEAMLINES that have to be OFF when Enable Beam is called on this beamline.
     */
    public Set<Magnet> getOffMagnets()
    {
        return mOffMagnets;
    }

    /**
     * Adds a property change listener.
     * @param pListener the property change listener to add
     */
    public void addPropertyChangeListener(PropertyChangeListener pListener)
    {
        mSupport.addPropertyChangeListener(pListener);
    }

    /**
     * Removes a property change listener.
     * @param pListener the property change listener to remove
     */
    public void removePropertyChangeListener(PropertyChangeListener pListener)
    {
        mSupport.removePropertyChangeListener(pListener);
    }

    /**
     * Checks if the beamline can be configured for all the given ranges and the given gantry angle.
     * @param pRangesByOpticalSolutions the optical solutions id and the associated ranges [g/cm2], to check
     * @param pGantryAngle the gantry angle to check, in degrees, 90 in case of FBTR or FSTR
     * @return true if the beamline can be configured for all the given ranges and the given gantry angle, false otherwise
     */
    public boolean checkRangeConfig(Map<String, Set<Double>> pRangesByOpticalSolutions, double pGantryAngle)
    {
        try
        {
            for (Map.Entry<String, Set<Double>> entry : pRangesByOpticalSolutions.entrySet())
            {
                String opticalSolution = entry.getKey();
                Set<Double> ranges = entry.getValue();

                RangeConverter rangeConverter = mRangeConverters.get(opticalSolution);

                if (rangeConverter == null)
                {
                    LOGGER.error("Invalid optical solution (%s) for beam supply point %s", opticalSolution, mBeamSupplyPointId);
                    return false;
                }

                for (Double range : ranges)
                {
                    LOGGER.debug("Check range config for range= %s, optical solution=%s, gantry angle= %s", range, opticalSolution,
                            pGantryAngle);

                    for (BeamlineSection section : mSections)
                    {
                        for (Device device : section.getRangeControlDevices(opticalSolution))
                        {
                            String deviceName = device.getDeviceName();

                            if (device instanceof Magnet)
                            {
                                LOGGER.debug("Check convert of magnet %s", deviceName);
                                rangeConverter.convertRangeToMagnetCurrent(deviceName, range, pGantryAngle, 0, 0);
                            }
                            else if (device instanceof Slits)
                            {
                                LOGGER.debug("Check convert of slits %s", deviceName);
                                rangeConverter.convertRangeToSlitsWidth(deviceName, range, pGantryAngle);
                            }
                            else if (device instanceof Degrader)
                            {
                                LOGGER.debug("Check convert of degrader %s", deviceName);
                                rangeConverter.convertRangeToDegraderPosition(deviceName, range, pGantryAngle);
                            }
                            else if (device instanceof Group3)
                            {
                                LOGGER.debug("Check convert of group3 %s", deviceName);
                                rangeConverter.convertRangeToGroup3Field(deviceName, range, pGantryAngle);
                            }
                        }
                    }
                }
            }
            return true;
        }
        catch (Exception e)
        {
            LOGGER.info("Check range failure ", e);
            return false;
        }
    }

    /**
     * Sets the devices of this beamline to achieve a given range.
     * @param pRange the range to achieve, in g/cm²
     * @param pOpticalSolution the optical solution id
     * @param pGantryAngle the gantry angle, in degrees, 90 in case of FBTR or FSTR
     * @param pOffsetX the horizontal offset to achieve on IC1, in mm
     * @param pOffsetY the vertical offset to achieve on IC1, in mm
     * @param pCycling true to do magnet cycling before applying setpoints, false otherwise
     * @throws Exception if if the beamline cannot be configured for the given range and the given gantry angle
     */
    public void setRange(Double pRange, String pOpticalSolution, Double pGantryAngle, Double pOffsetX, Double pOffsetY,
                         Boolean pCycling) throws Exception
    {
        mRangeAsRequested = false;
        mMagnetsAsRequested = false;
        mSelectedOpticalSolution = pOpticalSolution;
        mHasSetRangeSucceeded = false;
        if (pCycling)
        {
            mIsRecyclingRequired = false;
        }
        RangeConverter rangeConverter = mRangeConverters.get(pOpticalSolution);
        if (rangeConverter == null)
        {
            String message = String.format("Invalid optical solution (%s) for beam supply point %s", pOpticalSolution,
                    mBeamSupplyPointId);
            throw new IllegalArgumentException(message);
        }

        for (BeamlineSection section : mSections)
        {
            for (Device device : section.getRangeControlDevices(pOpticalSolution))
            {
                if (device instanceof Magnet)
                {
                    Magnet magnet = (Magnet) device;
                    // turn magnet power supply on if necessary
                    if (!magnet.isPoweredOn())
                    {
                        magnet.turnOn();
                    }
                    double current = rangeConverter.convertRangeToMagnetCurrent(magnet.getDeviceName(), pRange, pGantryAngle, pOffsetX,
                            pOffsetY);
                    magnet.setCurrent(current, pCycling);
                }
                else if (device instanceof Slits)
                {
                    Slits slits = (Slits) device;
                    double width = rangeConverter.convertRangeToSlitsWidth(slits.getDeviceName(), pRange, pGantryAngle);
                    slits.setWidth(width);
                }
                else if (device instanceof Degrader)
                {
                    Degrader degrader = (Degrader) device;
                    int position = rangeConverter.convertRangeToDegraderPosition(degrader.getDeviceName(), pRange, pGantryAngle);
                    degrader.setPosition(position);
                }
                else if (device instanceof Group3)
                {
                    Group3 group3 = (Group3) device;
                    double field = rangeConverter.convertRangeToGroup3Field(group3.getDeviceName(), pRange, pGantryAngle);
                    group3.setExpectedField(field);
                }
            }
        }

        updateRangeAsRequested();
    }

    /**
     * Gets the selected optical solution, i.e. the optical solution used in the last set range.
     * @return the selected optical solution, i.e. the optical solution used in the last set range
     * @see #setRange(Double, String, Double, Double, Double, Boolean)
     */
    public String getSelectedOpticalSolution()
    {
        return mSelectedOpticalSolution;
    }

    /**
     * Enables the beam in this beamline, i.e. retracts all beamstops and bpms.
     * <p>
     * If the beam has been disabled with a safety problem detected, you first have to power on the room entrance bending magnet
     * and wait that range is reached before re-enabling the beam.
     */
    public void enableBeam()
    {
        mEnablingBeam = true;
        mBeamEnableAsRequested = true;

        for (BeamlineSection section : mSections)
        {
            for (Device device : section.getBeamEnableDevices())
            {
                if (device instanceof BeamStop)
                {
                    BeamStop beamStop = (BeamStop) device;
                    beamStop.retract();
                    mBeamEnableAsRequested = false;
                }
                else if (device instanceof Bpm)
                {
                    Bpm bpm = (Bpm) device;
                    bpm.retract();
                    mBeamEnableAsRequested = false;
                }
            }
        }

        // set beam disable as requested to false
        updateBeamDisableAsRequested();

        updateBeamEnableAsRequested();
    }

    /**
     * Disables the beam in this beamline, i.e. inserts all beamstops and if a safety problem has been detected, powers off room
     * entrance bending magnet if any.
     * @param pSafetyProblemDetected true if a safety problem has been detected, false otherwise
     */
    public void disableBeam(boolean pSafetyProblemDetected)
    {
        mEnablingBeam = false;
        mSafetyProblemDetected = pSafetyProblemDetected;
        mBeamDisableAsRequested = true;

        for (BeamlineSection section : mSections)
        {
            for (Device device : section.getBeamEnableDevices())
            {
                if (device instanceof BeamStop)
                {
                    BeamStop beamStop = (BeamStop) device;
                    beamStop.insert();
                    mBeamDisableAsRequested = false;
                }
            }
        }

        if (pSafetyProblemDetected && mRoomEntranceBendingMagnet != null)
        {
            mRoomEntranceBendingMagnet.turnOff();
            mBeamDisableAsRequested = false;

            mIsRecyclingRequired = true;
            LOGGER.debug("Marked recycling required for beam supply point %s because turned off room entrance bending magnet",
                    mBeamSupplyPointId);
        }

        // set beam enable as requested to false
        updateBeamEnableAsRequested();

        updateBeamDisableAsRequested();
    }

    /**
     * Sets the current of the analog magnet power supplies of this beamline to 0 A.
     * <p>
     * This should be done on old generation analog power supplies before turning them off (see PR 58709).
     * @param pIncludingEss true to set zero current on the analog power supplies of the ESS magnets also, false to only set zero
     *        current on the analog power supplies of the magnets of this beamline not including the ESS
     */
    public void setZeroCurrentOnAnalogMagnetsPowerSupplies(boolean pIncludingEss)
    {
        // the first section is the ESS
        boolean isEssSection = true;
        for (BeamlineSection section : mSections)
        {
            // skip ESS section if requested so
            if (isEssSection && !pIncludingEss)
            {
                isEssSection = false;
                continue;
            }

            for (Device device : section.getRangeControlDevices())
            {
                if (device instanceof Magnet)
                {
                    Magnet magnet = (Magnet) device;
                    if (magnet.getType() == Magnet.MagnetType.ANALOG)
                    {
                        magnet.setCurrent(0.0, false);
                    }
                }
            }

            isEssSection = false;
        }
    }

    /**
     * Turns off the power supplies of the magnets of this beamline.
     * <p>
     * If fast switching is supported, {@link #MAGNETS_POWER_SUPPLIES_OFF_AS_REQUESTED_PROPERTY} will be true as soon as the power
     * supplies are powered off, otherwise it will be true when the power supplies are powered off and the current feedback is near
     * zero.
     * @param pIncludingEss true to turn off the power supplies of the ESS magnets also, false to only turn off the power supplies
     *        of the magnets of this beamline not including the ESS
     */
    public void turnOffMagnetsPowerSupplies(boolean pIncludingEss)
    {
        mTurnOffMagnetsPowerSuppliesIncludingEss = pIncludingEss;
        mTurningOn = false;
        mOnlyTurnOffPbsMagnetsPowerSupplies = false;
        mMagnetsPowerSuppliesOffAsRequested = false;

        if (pIncludingEss)
        {
            mBlpscu.turnOffEssMagnetsPowerSupplies();
            mEcubtcu.turnOffEssMagnetsPowerSupplies();
        }

        mBlpscu.turnOffBeamlineMagnetsPowerSupplies(mBeamlineId);
        mEcubtcu.turnOffBeamlineMagnetsPowerSupplies(mBeamlineId);

        updateMagnetsPowerSuppliesOffAsRequested();
    }

    /**
     * Turns off the power supplies of the PBS-only magnets of this beamline.
     * <p>
     * If fast switching is supported, {@link #MAGNETS_POWER_SUPPLIES_OFF_AS_REQUESTED_PROPERTY} will be true as soon as the power
     * supplies are powered off, otherwise it will be true when the power supplies are powered off and the current feedback is near
     * zero.
     * @throws IllegalArgumentException if there are no PBS magnets (see {@link #containsPbsMagnets()})
     */
    public void turnOffPbsMagnetsPowerSupplies()
    {
        Assert.isTrue(containsPbsMagnets(), "Can only turn off PBS magnets if there are some.");

        mTurnOffMagnetsPowerSuppliesIncludingEss = true;
        mTurningOn = false;
        mOnlyTurnOffPbsMagnetsPowerSupplies = true;
        mMagnetsPowerSuppliesOffAsRequested = false;

        for (BeamlineSection section : mSections)
        {
            for (Device device : section.getRangeControlDevices())
            {
                if (device instanceof Magnet)
                {
                    Magnet magnet = (Magnet) device;
                    if (magnet.getApplicability() == Applicability.PBS_ONLY)
                    {
                        magnet.turnOff();
                    }
                }
            }
        }

        updateMagnetsPowerSuppliesOffAsRequested();
    }

    /**
     * Indicates whether the beamline contains PBS-only magnets or not.
     * @return true if beamline contains PBS-only magnets
     */
    public boolean containsPbsMagnets()
    {
        // the first section is the ESS
        for (BeamlineSection section : mSections)
        {
            for (Device device : section.getRangeControlDevices())
            {
                if (device instanceof Magnet)
                {
                    Magnet magnet = (Magnet) device;
                    if (magnet.getApplicability() == Applicability.PBS_ONLY)
                    {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Turns on the power supplies of the magnets of this beamline.
     * @param pIncludingEss true to turn on the power supplies of the ESS magnets also, false to only to turn on the power supplies
     *        of the magnets of this beamline not including the ESS
     * @param pIncludingPbsMagnets true to also turn on the power supplies of the PBS magnets, false to only turn on the power
     *        supplies of the magnets which are not pbs-only
     */
    public void turnOnMagnetsPowerSupplies(boolean pIncludingEss, boolean pIncludingPbsMagnets)
    {
        mTurnOnMagnetsPowerSuppliesIncludingEss = pIncludingEss;
        mTurnOnMagnetsPowerSuppliesIncludingPbsMagnets = pIncludingPbsMagnets;
        mTurningOn = true;
        mMagnetsPowerSuppliesOnAsRequested = false;

        if (pIncludingEss)
        {
            mBlpscu.turnOnEssMagnetsPowerSupplies();
            mEcubtcu.turnOnEssMagnetsPowerSupplies();
        }

        mBlpscu.turnOnBeamlineMagnetsPowerSupplies(mBeamlineId);
        mEcubtcu.turnOnBeamlineMagnetsPowerSupplies(mBeamlineId);

        if (pIncludingPbsMagnets)
        {
            // turn on PBS-only magnets individually as they are not turned on by the global command
            // the first section is the ESS
            boolean isEssSection = true;
            for (BeamlineSection section : mSections)
            {
                if (isEssSection && !pIncludingEss)
                {
                    // skip the ESS section if it has been requested to turn on magnets power supplies not
                    // including ESS
                    isEssSection = false;
                    continue;
                }

                for (Device device : section.getRangeControlDevices())
                {
                    if (device instanceof Magnet)
                    {
                        Magnet magnet = (Magnet) device;
                        if (magnet.getApplicability() == Applicability.PBS_ONLY)
                        {
                            magnet.turnOn();
                        }
                    }
                }

                isEssSection = false;
            }
        }

        updateMagnetsPowerSuppliesOnAsRequested();
    }

    /**
     * Gets the "range as requested" status.
     * @return true if range is as requested, false otherwise
     */
    public boolean isRangeAsRequested()
    {
        return mRangeAsRequested;
    }

    public boolean isMagnetsAsRequested()
    {
        return mMagnetsAsRequested;
    }

    /**
     * Gets the "beam enable as requested" status.
     * @return true if beam enable is as requested, false otherwise
     */
    public boolean isBeamEnableAsRequested()
    {
        return mBeamEnableAsRequested;
    }

    /**
     * Gets the "beam enable as requested" status.
     * @return true if beam enable is as requested, false otherwise
     */
    public boolean isBeamDisableAsRequested()
    {
        return mBeamDisableAsRequested;
    }

    /**
     * Gets the "magnets power supplies off as requested" status.
     * @return true if magnets power supplies are off, false otherwise
     */
    public boolean isMagnetsPowerSuppliesOffAsRequested()
    {
        return mMagnetsPowerSuppliesOffAsRequested;
    }

    /**
     * Gets the "magnets power supplies on as requested" status.
     * @return true if magnets power supplies are on, false otherwise
     */
    public boolean isMagnetsPowerSuppliesOnAsRequested()
    {
        return mMagnetsPowerSuppliesOnAsRequested;
    }

    /**
     * Gets the devices used to control the range in this beamline that did not reached requested range.
     * <p>
     * This method may only be called after a call to {@link #setRange}.
     * @return the devices used to control the range in this beamline that did not reached requested range
     */
    public Collection<Device> getRangeNotReachedDevices()
    {
        ArrayList<Device> rangeNotReachedDevices = new ArrayList<>();
        for (BeamlineSection section : mSections)
        {
            for (Device device : section.getRangeControlDevices(mSelectedOpticalSolution))
            {
                if (!device.isAsRequested())
                {
                    rangeNotReachedDevices.add(device);
                }
            }
        }
        return rangeNotReachedDevices;
    }

    /**
     * Gets the devices that couldn't be set to enable the beam.
     * <p>
     * This method may only be called after a call to {@link #enableBeam}.
     * @return the devices that couldn't be set to enable the beam
     */
    public Collection<Device> getBeamNotEnabledDevices()
    {
        ArrayList<Device> beamNotEnabledDevices = new ArrayList<>();
        for (BeamlineSection section : mSections)
        {
            for (Device device : section.getBeamEnableDevices())
            {
                if ((device instanceof Bpm || device instanceof BeamStop) && !device.isAsRequested())
                {
                    beamNotEnabledDevices.add(device);
                }
            }
        }
        return beamNotEnabledDevices;
    }

    /**
     * Gets the devices that couldn't be set to disable the beam.
     * <p>
     * This method may only be called after a call to {@link #disableBeam}.
     * @return the devices that couldn't be set to disable the beam
     */
    public Collection<Device> getBeamNotDisabledDevices()
    {
        ArrayList<Device> beamNotDisabledDevices = new ArrayList<>();
        for (BeamlineSection section : mSections)
        {
            for (Device device : section.getBeamEnableDevices())
            {
                if (device instanceof BeamStop && !device.isAsRequested())
                {
                    beamNotDisabledDevices.add(device);
                }
            }
        }

        if (mSafetyProblemDetected && mRoomEntranceBendingMagnet != null && mRoomEntranceBendingMagnet.isPoweredOn())
        {
            beamNotDisabledDevices.add(mRoomEntranceBendingMagnet);
        }

        return beamNotDisabledDevices;
    }

    /**
     * Gets the magnets power supplies in this beamline that are not turned on.
     * <p>
     * This method may only be called after a call to {@link #turnOnMagnetsPowerSupplies}.
     * @return the magnets power supplies in this beamline that are not turned on
     */
    public Collection<Magnet> getNotOnMagnetsPowerSupplies()
    {
        ArrayList<Magnet> notOnMagnetsPowerSupplies = new ArrayList<>();

        // the first section is the ESS
        boolean isEssSection = true;
        for (BeamlineSection section : mSections)
        {
            if (isEssSection && !mTurnOnMagnetsPowerSuppliesIncludingEss)
            {
                // skip the ESS section if it has been requested to turn on magnets power supplies not including
                // ESS
                isEssSection = false;
                continue;
            }

            for (Device device : section.getRangeControlDevices())
            {
                if (device instanceof Magnet)
                {
                    Magnet magnet = (Magnet) device;
                    if (!mTurnOnMagnetsPowerSuppliesIncludingPbsMagnets && magnet.getApplicability() == Applicability.PBS_ONLY)
                    {
                        continue;
                    }
                    if (!magnet.isPoweredOn())
                    {
                        notOnMagnetsPowerSupplies.add(magnet);
                    }
                }
            }

            isEssSection = false;
        }
        return notOnMagnetsPowerSupplies;
    }

    /**
     * Gets the magnets power supplies in this beamline that are not turned off.
     * <p>
     * This method may only be called after a call to {@link #turnOffMagnetsPowerSupplies} or
     * {@link #turnOffPbsMagnetsPowerSupplies()}.
     * @return the magnets power supplies in this beamline that are not turned off
     */
    public Collection<Magnet> getNotOffMagnetsPowerSupplies()
    {
        ArrayList<Magnet> notOffMagnetsPowerSupplies = new ArrayList<>();

        // the first section is the ESS
        boolean isEssSection = true;
        for (BeamlineSection section : mSections)
        {
            if (isEssSection && !mTurnOffMagnetsPowerSuppliesIncludingEss)
            {
                // skip the ESS section if it has been requested to turn off magnets power supplies not including
                // ESS
                isEssSection = false;
                continue;
            }

            for (Device device : section.getRangeControlDevices())
            {
                if (device instanceof Magnet)
                {
                    Magnet magnet = (Magnet) device;
                    if (mOnlyTurnOffPbsMagnetsPowerSupplies && magnet.getApplicability() != Applicability.PBS_ONLY)
                    {
                        continue;
                    }
                    if (mFastSwitching)
                    {
                        if (magnet.isPoweredOn())
                        {
                            notOffMagnetsPowerSupplies.add(magnet);
                        }
                    }
                    else
                    {
                        if (!magnet.isOff())
                        {
                            notOffMagnetsPowerSupplies.add(magnet);
                        }
                    }
                }
            }

            isEssSection = false;
        }
        return notOffMagnetsPowerSupplies;
    }

    /**
     * Indicates whether magnets recycling is required (i.e. when bending magnet at room entrance current has been below (setpoint
     * - tolerance) since last set range).
     * @return true if magnets recycling is required, false otherwise.
     */
    public boolean isRecyclingRequired()
    {
        return mIsRecyclingRequired;
    }

    /**
     * save the settings of Beamline in CSV format.
     * @see #saveDeviceSetting(Device)
     */
    public String saveBeamlineSettings()
    {
        StringBuilder result = new StringBuilder();

        for (BeamlineSection section : mSections)
        {
            for (Device device : section.getRangeControlDevices())
            {
                result.append(saveDeviceSetting(device));
            }
        }

        return result.toString();
    }

    /**
     * Restore settings from the specified CSV setting content. <br>
     * The setting should be produced by {@link #saveBeamlineSettings()}
     */
    public void restoreBeamlineSettings(String pCSVSetting, boolean pCycling) throws IOException
    {
        String[] deviceSettings = pCSVSetting.split(LINE_END);

        int lineNbr = 0;
        for (String setting : deviceSettings)
        {
            restoreDeviceSetting(setting, lineNbr, pCycling);
            ++lineNbr;
        }
    }

    /**
     * Save setting of device (Power supply setpoints, slits opening, degrader motor steps) in CSV format.<br>
     * There are four columns: beamlineId; deviceType;deviceName;deviceSetValue
     */
    public String saveDeviceSetting(Device pDevice)
    {
        String result = getBeamlineId() + SEPARATOR + pDevice.getDeviceType() + SEPARATOR + pDevice.getDeviceName() + SEPARATOR;

        if (pDevice instanceof Magnet)
        {
            Magnet magnet = (Magnet) pDevice;

            result += magnet.getCurrent();
        }
        else if (pDevice instanceof Slits)
        {
            Slits slits = (Slits) pDevice;
            result += slits.getWidth();
        }
        else if (pDevice instanceof Degrader)
        {
            Degrader degrader = (Degrader) pDevice;

            result += degrader.getPosition();
        }
        else
        {
            return "";
        }
        result += LINE_END;

        return result;
    }

    /**
     * Restore the device settings.<br>
     * Device setting is in CSV format and produced by {@link #saveDeviceSetting(Device)}
     */
    public void restoreDeviceSetting(String pSetting, int pLineNumber, boolean pCycling) throws IOException
    {

        String[] settings = pSetting.split(SEPARATOR);

        if (isEmptyOrComment(settings))
        {
            return;
        }

        if (settings.length != FIELD_COUNT)
        {
            throw new IOException("Invalid syntax of device setting at line " + pLineNumber);
        }
        try
        {
            int lineId = Integer.valueOf(settings[0]);

            if (lineId != getBeamlineId())
            {
                throw new IOException("The device setting belongs to beam line (" + lineId + ") not for this beam line"
                        + getBeamlineId() + "in device setting at line " + pLineNumber);
            }

            for (BeamlineSection section : getSections())
            {
                for (Device device : section.getRangeControlDevices())
                {
                    if (device.getDeviceName().equals(settings[DEVICE_NAME_INDEX]))
                    {
                        if (device instanceof Magnet)
                        {
                            Magnet magnet = (Magnet) device;

                            magnet.setCurrent(Double.parseDouble(settings[DEVICE_SETTING_INDEX]), pCycling);
                        }
                        else if (device instanceof Slits)
                        {
                            Slits slits = (Slits) device;
                            slits.setWidth(Double.parseDouble(settings[DEVICE_SETTING_INDEX]));
                        }
                        else if (device instanceof Degrader)
                        {
                            Degrader degrader = (Degrader) device;

                            degrader.setPosition(Integer.parseInt(settings[DEVICE_SETTING_INDEX]));
                        }
                        else
                        {
                            throw new IOException("Setting of device " + device.getDeviceName() + " type " + device.getDeviceType()
                                    + " at line " + pLineNumber + " cannot be restored!");
                        }
                    }
                }
            }

        }
        catch (NumberFormatException nfe)
        {
            throw new IOException("Number format syntax of device setting at line " + pLineNumber + " (" + nfe + ")");
        }

    }

    /**
     * Whether the array of String is empty or comment.
     */
    private static boolean isEmptyOrComment(String[] pLine)
    {
        return pLine.length == 0 || pLine[0] == null || "".equals(pLine[0].trim()) || pLine[0].startsWith("#");
    }

    // ///////////////////////////////////////////////////////////////////

    /**
     * Updates the "range as requested" property (AND composition of all active range control devices "as requested" statuses) and
     * the mSetRangeHasSucceeded and mRecyclingRequired.
     */
    private void updateRangeAsRequested()
    {
        boolean asRequested = true;
        boolean magnetsAsRequested = true;

        for (BeamlineSection section : mSections)
        {
            for (Device device : section.getRangeControlDevices(mSelectedOpticalSolution))
            {
                asRequested = asRequested && device.isAsRequested();
                if (device instanceof Magnet)
                {
                    magnetsAsRequested = magnetsAsRequested && device.isAsRequested();
                }
                if (device.isAsRequested()) {
                    LOGGER.warn(device.getDeviceName());
                }
            }
        }

        if (asRequested)
        {
            mHasSetRangeSucceeded = true;
            LOGGER.debug("Marked set range succeeded for beam supply point %s", mBeamSupplyPointId);
        }
        mSupport.firePropertyChange(RANGE_MAGNETS_AS_REQUESTED_PROPERTY, mMagnetsAsRequested,
                mMagnetsAsRequested = magnetsAsRequested); // NOSONAR
        mSupport.firePropertyChange(RANGE_AS_REQUESTED_PROPERTY, mRangeAsRequested, mRangeAsRequested = asRequested); // NOSONAR
    }

    /**
     * Updates the "beam enable as requested" property (AND composition of all beamstops and bpms "as requested" statuses).
     */
    private void updateBeamEnableAsRequested()
    {
        boolean asRequested = mEnablingBeam;
        for (BeamlineSection section : mSections)
        {
            for (Device device : section.getBeamEnableDevices())
            {
                if (device instanceof Bpm || device instanceof BeamStop)
                {
                    asRequested = asRequested && device.isAsRequested();
                }
            }
        }

        mSupport.firePropertyChange(BEAM_ENABLE_AS_REQUESTED_PROPERTY, mBeamEnableAsRequested,
                mBeamEnableAsRequested = asRequested); // NOSONAR
    }

    /**
     * Updates the "beam disable as requested" property (AND composition of all beamstops "as requested" statuses).
     */
    private void updateBeamDisableAsRequested()
    {
        boolean asRequested = !mEnablingBeam;

        for (BeamlineSection section : mSections)
        {
            for (Device device : section.getBeamEnableDevices())
            {
                if (device instanceof BeamStop)
                {
                    asRequested = asRequested && device.isAsRequested();
                }
            }
        }

        if (mSafetyProblemDetected && mRoomEntranceBendingMagnet != null)
        {
            asRequested = asRequested && !mRoomEntranceBendingMagnet.isPoweredOn();
        }

        mSupport.firePropertyChange(BEAM_DISABLE_AS_REQUESTED_PROPERTY, mBeamDisableAsRequested,
                mBeamDisableAsRequested = asRequested); // NOSONAR
        // change
    }

    /**
     * Updates the "magnets power supplies off as requested" property (AND composition of all magnets power supplies "off" statuses
     * (power status = false + current feedback = 0)).
     */
    private void updateMagnetsPowerSuppliesOffAsRequested()
    {
        boolean asRequested = true;

        // the first section is the ESS
        boolean isEssSection = true;
        for (BeamlineSection section : mSections)
        {
            if (isEssSection && !mTurnOffMagnetsPowerSuppliesIncludingEss)
            {
                // skip the ESS section if it has been requested to turn off magnets power supplies not including
                // ESS
                isEssSection = false;
                continue;
            }

            for (Device device : section.getRangeControlDevices())
            {
                if (device instanceof Magnet)
                {
                    Magnet magnet = (Magnet) device;
                    if (mOnlyTurnOffPbsMagnetsPowerSupplies && magnet.getApplicability() != Applicability.PBS_ONLY)
                    {
                        continue;
                    }
                    asRequested = asRequested && (mFastSwitching ? !magnet.isPoweredOn() : magnet.isOff());
                }
            }

            isEssSection = false;
        }

        mSupport.firePropertyChange(MAGNETS_POWER_SUPPLIES_OFF_AS_REQUESTED_PROPERTY, mMagnetsPowerSuppliesOffAsRequested,
                mMagnetsPowerSuppliesOffAsRequested = asRequested); // NOSONAR
    }

    /**
     * Updates the "magnets power supplies on as requested" property (AND composition of all magnets power supplies "on" statuses
     * (power status = true)).
     */
    private void updateMagnetsPowerSuppliesOnAsRequested()
    {
        boolean asRequested = true;

        // the first section is the ESS
        boolean isEssSection = true;
        for (BeamlineSection section : mSections)
        {
            if (isEssSection && !mTurnOnMagnetsPowerSuppliesIncludingEss)
            {
                // skip the ESS section if it has been requested to turn on magnets power supplies not including
                // ESS
                isEssSection = false;
                continue;
            }

            for (Device device : section.getRangeControlDevices())
            {
                if (device instanceof Magnet)
                {
                    Magnet magnet = (Magnet) device;
                    if (!mTurnOnMagnetsPowerSuppliesIncludingPbsMagnets && magnet.getApplicability() == Applicability.PBS_ONLY)
                    {
                        continue;
                    }
                    asRequested = asRequested && magnet.isPoweredOn();
                }
            }

            isEssSection = false;
        }

        mSupport.firePropertyChange(MAGNETS_POWER_SUPPLIES_ON_AS_REQUESTED_PROPERTY, mMagnetsPowerSuppliesOnAsRequested,
                mMagnetsPowerSuppliesOnAsRequested = asRequested); // NOSONAR
    }

    // /////////////////////////////////////////////////////////////////////////

    /**
     * Property change listener for range control devices.
     */
    private final class RangeControlDeviceListener implements PropertyChangeListener
    {

        @Override
        public void propertyChange(PropertyChangeEvent pEvt)
        {
            if (Device.AS_REQUESTED_PROPERTY.endsWith(pEvt.getPropertyName()))
            {
                updateRangeAsRequested();
            }
            if (Device.AS_REQUESTED_PROPERTY.equals(pEvt.getPropertyName())){
                Device device = (Device) pEvt.getSource();
                LOGGER.warn(device.getDeviceName() + device.isAsRequested());
            }
        }
    }

    /**
     * Property change listener for magnets.
     */
    private final class MagnetListener implements PropertyChangeListener
    {

        @Override
        public void propertyChange(PropertyChangeEvent pEvt)
        {
            if (mTurningOn)
            {
                if (Magnet.MAGNET_POWER_STATUS_PROPERTY.equals(pEvt.getPropertyName()))
                {
                    updateMagnetsPowerSuppliesOnAsRequested();
                }
            }
            else
            {
                if (mFastSwitching)
                {
                    if (Magnet.MAGNET_POWER_STATUS_PROPERTY.equals(pEvt.getPropertyName()))
                    {
                        updateMagnetsPowerSuppliesOffAsRequested();
                    }
                }
                else
                {
                    if (Magnet.MAGNET_OFF_PROPERTY.equals(pEvt.getPropertyName()))
                    {
                        updateMagnetsPowerSuppliesOffAsRequested();
                    }

                }
            }

            // set mIsRecyclingRequired to true when a magnet current has dropped under its requested value
            if (!mIsRecyclingRequired && mHasSetRangeSucceeded && Magnet.MAGNET_CURRENT_PROPERTY.equals(pEvt.getPropertyName()))
            {
                Magnet magnet = (Magnet) pEvt.getSource();
                if (magnet.getCurrent() < magnet.getRequestedCurrent() - magnet.getTolerance())
                {
                    mIsRecyclingRequired = true;
                    LOGGER.info(
                            "Marked recycling required for beam supply point %s because %s current has dropped under requested value",
                            mBeamSupplyPointId, magnet.getDeviceName());
                }
            }
        }
    }

    /**
     * Property change listener for room entrance bending magnet.
     */
    private final class RoomEntranceBendingMagnetListener implements PropertyChangeListener
    {

        @Override
        public void propertyChange(PropertyChangeEvent pEvt)
        {
            if (Magnet.MAGNET_POWER_STATUS_PROPERTY.equals(pEvt.getPropertyName()))
            {
                if (!mEnablingBeam && mSafetyProblemDetected)
                {
                    updateBeamDisableAsRequested();
                }
            }
        }
    }

    /**
     * Property change listener for beam enable devices.
     */
    private final class BeamEnableDeviceListener implements PropertyChangeListener
    {

        @Override
        public void propertyChange(PropertyChangeEvent pEvt)
        {
            if (Device.AS_REQUESTED_PROPERTY.endsWith(pEvt.getPropertyName()))
            {
                if (mEnablingBeam)
                {
                    updateBeamEnableAsRequested();
                }
                else
                {
                    updateBeamDisableAsRequested();
                }
            }
        }
    }

    // /////////////////////////////////////////////////////////////////////////

    private final List<BeamlineSection> mSections;
    private final Set<Magnet> mOffMagnets;
    private Magnet mRoomEntranceBendingMagnet;
    private final PropertyChangeSupport mSupport = new PropertyChangeSupport(this);
    private final String mBeamSupplyPointId;
    private final int mTreatmentRoomId;
    private final int mBeamlineId;
    private final Map<String, RangeConverter> mRangeConverters;
    private Blpscu mBlpscu;
    private Ecubtcu2 mEcubtcu;
    private boolean mFastSwitching = false;
    private boolean mEnablingBeam = false;
    private boolean mSafetyProblemDetected = false;
    private boolean mRangeAsRequested = false;
    private boolean mMagnetsAsRequested = false;
    private boolean mBeamEnableAsRequested = false;
    private boolean mBeamDisableAsRequested = false;
    private boolean mMagnetsPowerSuppliesOffAsRequested = false;
    private boolean mMagnetsPowerSuppliesOnAsRequested = false;
    private boolean mTurningOn;
    private boolean mTurnOffMagnetsPowerSuppliesIncludingEss;
    private boolean mTurnOnMagnetsPowerSuppliesIncludingEss;
    private boolean mTurnOnMagnetsPowerSuppliesIncludingPbsMagnets;
    private boolean mOnlyTurnOffPbsMagnetsPowerSupplies;
    private boolean mHasSetRangeSucceeded = false; // true when the set range has succeeded once, even if it's
    // not as requested anymore
    private boolean mIsRecyclingRequired = false; // recyling is required if bending at room entrance has been
    // below setpoint since set range succeeded
    private String mSelectedOpticalSolution;
}
