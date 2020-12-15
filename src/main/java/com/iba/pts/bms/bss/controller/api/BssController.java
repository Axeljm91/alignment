// ///////////////////////////////////////////////////////////////////
// Copyright (c) by Ion Beam Applications S.A.
// All rights reserved
//
// Rue du cyclotron, 3
// B-1348 Louvain-la-Neuve
// Tel : +32.10.47.58.11
// ///////////////////////////////////////////////////////////////////

package com.iba.pts.bms.bss.controller.api;

import com.iba.icompx.core.activity.ActivityController;
import com.iba.icompx.ui.panel.EnumValueLabel;
import com.iba.pts.bms.bss.datatypes.api.BssSettings;
import com.iba.pts.bms.datatypes.api.TreatmentMode;

/**
 * This is the interface of the BSS Controller, offered to the treatment process. The BSS Controller is the controller of whole
 * BSS (cyclotron, beam line...).
 * <p>
 * It generates a {@link java.beans.PropertyChangeEvent} for the following properties:
 * <ul>
 * <li>{@link #CURRENT_ACTIVITY_NAME_PROPERTY}: name of current activity,</li>
 * <li>{@link #CURRENT_ACTIVITY_STATUS_PROPERTY}: status of current activity.</li>
 * </ul>
 */
public interface BssController extends ActivityController<BssActivityId>
{
    /**
     * Operating mode of the BssController.
     */
    enum OperatingMode
    {
        /**
         * In manual mode, the MCR operator is controlling BSS manually.
         */
        MANUAL(EnumValueLabel.State.ERROR),
        /**
         * Temporary mode when BssController is switching from AUTOMATIC to MANUAL.
         */
        SWITCHING_TO_MANUAL(EnumValueLabel.State.WARNING),
        /**
         * In automatic mode, the BssController is automatically performing operations.
         */
        AUTOMATIC(EnumValueLabel.State.OK);

        private final EnumValueLabel.State state;

        private OperatingMode(EnumValueLabel.State state) {
            this.state = state;
        }

        public EnumValueLabel.State toState() {
            return state;
        }
    }

    /**
     * {@link OperatingMode} set when BssController starts up.
     */
    OperatingMode DEFAULT_OPERATING_MODE = OperatingMode.MANUAL;

    /**
     * Name of property holding current {@link OperatingMode}.
     */
    String OPERATING_MODE_PROPERTY = "operatingMode";

    /**
     * Returns the resume conditions from BssController point of view.
     * <p>
     * The call is asynchronous, so the response should be handled using {@link com.iba.icomp.core.component.Callbacks}, by setting
     * a callback before doing the call. If the call succeeds (see {@link com.iba.icomp.core.component.Callback#callSucceeded}) the
     * response will contain {@link com.iba.pts.bms.bss.datatypes.api.EsBtsResumeCondition} and
     * {@link com.iba.pts.bms.bss.datatypes.api.BpsResumeCondition} arguments.
     * @param pBeamSupplyPointId the beam supply point id for which to get resume conditions.
     */
    void getResumeCondition(String pBeamSupplyPointId);

    /**
     * Returns whether the beam detection consistency check between BIREU and BAEU is true or false (BpsController). If one of them
     * is not installed, the consistence will always be true.
     * <p>
     * The call is asynchronous, so the response should be handled using {@link com.iba.icomp.core.component.Callbacks}, by setting
     * a callback before doing the call. If the call succeeds (see {@link com.iba.icomp.core.component.Callback#callSucceeded}) the
     * response will contain a boolean argument.
     */
    void isBeamDetectionConsistent();

    /**
     * Returns the current operating mode.
     * @return the current operating mode.
     */
    OperatingMode getOperatingMode();

    // //////////////////////////////
    // Methods to start activities //
    // //////////////////////////////

    /**
     * Requests the BssController to enable the {@link BssActivityId#IDLE} activity.
     * <p/>
     * The goal of this activity is to set the BSS in a state in which it can remain a long time.
     * <p/>
     * During this activity, all BssController operations are allowed.
     * <p/>
     * This call is rejected if the beam supply point passed is not the one currently having beam.
     * @param pBeamSupplyPointId the beam supply point id requesting to enable the IDLE activity.
     * @throws IllegalStateException if {@link #mayProgress()} returns <code>false</code>
     */
    void startIdleActivity(String pBeamSupplyPointId);

    /**
     * Requests the BssController to enable and start the {@link BssActivityId#PREPARE} activity.
     * <p>
     * The goal of this activity is to prepare the BSS for an irradiation. This implies
     * <ul>
     * <li>preparing cyclotron to produce requested beam current
     * <li>selecting the beamline of the given beam supply point
     * <li>powering on the ESS and beamline magnets power supplies
     * <li>setting requested range, doing magnet cycling before applying magnet setpoints
     * </ul>
     * <p>
     * To leave this activity, there are 2 options:
     * <ul>
     * <li>if {@link #mayProgress()} returns <code>true</code>, one can request to start another activity</li>
     * <li>otherwise one must call {@link #cancel()} and wait for property {@link #CURRENT_ACTIVITY_STATUS_PROPERTY} to become
     * {@link com.iba.icompx.core.activity.ActivityStatus#CANCELED}.</li>
     * </ul>
     * @param pBeamSupplyPointId the beam supply point id for which to prepare the BSS
     * @param pSettings the BSS settings that will be used for the treatment.
     * @param pOffsetX offset on IC1 X axis, in mm.
     * @param pOffsetY offset on IC1 Y axis, in mm.
     * @param pCycling whether magnets must be cycled or not.
     * @throws IllegalStateException if {@link #mayProgress()} returns <code>false</code>
     */
    void startPrepareActivity(String pBeamSupplyPointId, BssSettings pSettings, double pOffsetX, double pOffsetY,
                              boolean pCycling);

    /**
     * Start bps prepare activity. The goal of this activity is to reset the BIREU check when the irradiation has paused. Request
     * the BssController to enable and start the BPS prepare activity.
     * @param pBeamSupplyPointId the beam supply point id
     * @param pTreatmentMode the treatment mode
     * @param pBeamCurrentAtCycloExit the beam current at cyclo exit
     * @param pBcmFile the bcm file: the BCM file is defined as an array of Objects. This has been done to make possible the
     *        serialization of the int array when the data is sent to the controller through the proxy. However, the Object array
     *        must be built/read using an int array. When buildding the object array each int is cast to an Object. When read, each
     *        Object is cast as an int.
     * @param pStartDigit Bcm start digit
     * @param pStopDigit Bcm stop digit
     * @param pBcmId the bcm file Id
     */
    void startBpsPrepareActivity(String pBeamSupplyPointId, TreatmentMode pTreatmentMode, double pBeamCurrentAtCycloExit,
                                 Object[] pBcmFile, int pStartDigit, int pStopDigit, String pBcmId);

    /**
     * Requests the BssController to enable and start the {@link BssActivityId#SET_RANGE} activity.
     * <p>
     * The goal of this activity is to set all the devices of the beamline to the given beam supply point to achieve the given
     * range for the given gantry angle and apply a correction on the steering magnets to achieve the given offsets on IC1.
     * <p>
     * To leave this activity, there are 2 options:
     * <ul>
     * <li>if {@link #mayProgress()} returns <code>true</code>, one can request to start another activity</li>
     * <li>otherwise one must call {@link #cancel()} and wait for property {@link #CURRENT_ACTIVITY_STATUS_PROPERTY} to become
     * {@link com.iba.icompx.core.activity.ActivityStatus#CANCELED}.</li>
     * </ul>
     * @param pBeamSupplyPointId the beam supply point id for which to set range
     * @param pRange the range to achieve, in g/cm²
     * @param pOpticalSolution the optical solution id
     * @param pGantryAngle the gantry angle, in degrees, 90 in case of FBTR or FSTR
     * @param pOffsetX the horizontal offset to achieve on IC1, in mm
     * @param pOffsetY the vertical offset to achieve on IC1, in mm
     * @param pCycling true to do magnet cycling before applying setpoints, false otherwise
     * @throws IllegalStateException if {@link #mayProgress()} returns <code>false</code>
     */
    // If you change the position of pOffsetX/pOffsetY, don't forget to update
    // BmsPrepareActivityImpl.Forwarder.receive() !
    void startSetRangeActivity(String pBeamSupplyPointId, double pRange, String pOpticalSolution, double pGantryAngle,
                               double pOffsetX, double pOffsetY, boolean pCycling);

    /**
     * Requests the BssController to enable and start the {@link BssActivityId#ENABLE_BEAM} activity.
     * <p>
     * The goal of this activity is to enable the beam for the given beam supply point, i.e. retract all beam stops and bpms of the
     * beamline to the given beam supply point and if the room entrance bending magnet is off, power it on and recycle it.
     * <p>
     * To leave this activity, there are 2 options:
     * <ul>
     * <li>if {@link #mayProgress()} returns <code>true</code>, one can request to start another activity</li>
     * <li>otherwise one must call {@link #cancel()} and wait for property {@link #CURRENT_ACTIVITY_STATUS_PROPERTY} to become
     * {@link com.iba.icompx.core.activity.ActivityStatus#CANCELED}.</li>
     * </ul>
     * @param pBeamSupplyPointId the beam supply point id for which to enable beam
     * @throws IllegalStateException if {@link #mayProgress()} returns <code>false</code>
     */
    void startEnableBeamActivity(String pBeamSupplyPointId);

    /**
     * Requests the BssController to enable and start the {@link BssActivityId#DISABLE_BEAM} activity.
     * <p>
     * The goal of this activity is to disable the beam for the given beam supply point, i.e. insert all beam stops of the beamline
     * to the given beam supply point or even use stronger actions if a safety problem is detected (typically powering off room
     * entrance bending magnet) if it can.
     * <p>
     * To leave this activity, there are 2 options:
     * <ul>
     * <li>if {@link #mayProgress()} returns <code>true</code>, one can request to start another activity</li>
     * <li>otherwise one must call {@link #cancel()} and wait for property {@link #CURRENT_ACTIVITY_STATUS_PROPERTY} to become
     * {@link com.iba.icompx.core.activity.ActivityStatus#CANCELED}.</li>
     * </ul>
     * @param pBeamSupplyPointId the beam supply point id for which to disable beam
     * @throws IllegalStateException if {@link #mayProgress()} returns <code>false</code>
     * @see #emergencyDisableBeam(String)
     */
    void startDisableBeamActivity(String pBeamSupplyPointId);

    /**
     * Requests the BssController to disable the beam immediately, because of an emergency situation. This doesn't start an
     * activity, so it may be called even when an activity is ongoing.
     * <p>
     * The goal of this method is to disable the beam for the given beam supply point, i.e. insert all beam stops of the beamline
     * to the given beam supply point and power off room entrance bending magnet, without waiting for the actions to be finished.
     * @param pBeamSupplyPointId the beam supply point id for which to disable beam
     * @see #startDisableBeamActivity(String)
     */
    void emergencyDisableBeam(String pBeamSupplyPointId);

    /**
     * {@inheritDoc}
     * <p>
     * It's meant to interrupt the current activity to allow starting a new one. The current activity will be immediately
     * interrupted without doing any new action and will immediately go to
     * {@link com.iba.icompx.core.activity.ActivityStatus#CANCELING} then
     * {@link com.iba.icompx.core.activity.ActivityStatus#CANCELED} status.
     * <p>
     * It may be called at any time, except when property {@link #CURRENT_ACTIVITY_STATUS_PROPERTY} is
     * {@link com.iba.icompx.core.activity.ActivityStatus#CANCELING} or
     * {@link com.iba.icompx.core.activity.ActivityStatus#CANCELED}.
     */
    @Override
    void cancel();
}
