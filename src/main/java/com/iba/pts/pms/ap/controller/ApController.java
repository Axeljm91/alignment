// ///////////////////////////////////////////////////////////////////
// Copyright (c) by Ion Beam Applications S.A.
// All rights reserved
//
// Rue du cyclotron, 3
// B-1348 Louvain-la-Neuve
// Tel : +32.10.47.58.11
// ///////////////////////////////////////////////////////////////////

package com.iba.pts.pms.ap.controller;

import com.iba.icomp.core.checks.CheckManager;
import com.iba.icomp.core.component.AbstractBean;
import com.iba.icomp.core.timer.Scheduled;
import com.iba.icomp.core.timer.Timer;
import com.iba.icomp.core.timer.TimerFactory;
import com.iba.icomp.core.timer.TimerState;
import com.iba.icomp.core.util.Assert;
import com.iba.icomp.core.util.Logger;
import com.iba.icomp.devices.Device;
import com.iba.icompx.core.activity.ActivityController;
import com.iba.icompx.ui.i18n.ResourceDictionary;
import com.iba.icompx.ui.util.ResourceManager;
import com.iba.pts.pms.ap.controller.api.AccessPointController;
import com.iba.pts.pms.ap.controller.api.AccessPointProcessMonitor;
import com.iba.pts.pms.ap.controller.api.helpers.HelperCallback;
import com.iba.pts.pms.ap.controller.contactivation.ContinuousMove;
import com.iba.pts.pms.ap.controller.helpers.*;
import com.iba.pts.pms.ap.hp.api.*;
import com.iba.pts.pms.ap.hp.impl.effects.ShowMenuEffect;
import com.iba.pts.pms.ap.hp.impl.effects.ShowMessageEffect;
import com.iba.pts.pms.ap.hp.impl.effects.ShowTextEffect;
import com.iba.pts.pms.controller.api.PmsController;
import com.iba.pts.pms.controller.impl.cbct.Cbct;
import com.iba.pts.pms.controller.impl.models.ActivityModelImpl;
import com.iba.pts.pms.controller.impl.models.PPVSModelImpl;
import com.iba.pts.pms.controller.models.api.PPVSModel;
import com.iba.pts.pms.datatypes.api.PpdPosition;
import com.iba.pts.pms.datatypes.api.PpsPosition;
import com.iba.pts.pms.datatypes.api.UserPositionImpl;
import com.iba.pts.pms.offline.api.PpdPositionChecks;
import com.iba.pts.pms.pcuproxy.api.PcuR6;
import com.iba.pts.pms.pcuproxy.util.PmsUtils;
import com.iba.pts.pms.poss.api.*;
import com.iba.pts.pms.poss.api.Home.Reference;
import com.iba.pts.pms.poss.api.PpsElbow.ElbowOrientation;
import com.iba.pts.pms.poss.devices.api.*;
import com.iba.pts.pms.poss.devices.api.PmsDevice.Label;
import com.iba.pts.pms.poss.devices.api.headneck.HeadNeckChairSupport;
import com.iba.pts.pms.poss.devices.impl.imaging.GantryImagerPanelProxy;
import com.iba.pts.pms.poss.devices.impl.posvalidity.PositionValidityManager;
import com.iba.pts.pms.poss.devices.impl.pps.forte.RenovatedPpsForteProxy;
import com.iba.pts.pms.poss.devices.impl.snout.RenovatedFixedSnoutHolderProxy;
import com.iba.pts.pms.poss.devices.impl.snout.RenovatedSnoutHolderProxy;
import com.iba.pts.pms.poss.devices.impl.snout.dn.DNSnoutHolderProxy;
import com.iba.pts.pms.poss.devices.ui.imaging.GantryImagerPanel;
import com.iba.pts.pms.utils.IcompCompatible.result.StatusResult;

import java.beans.PropertyChangeEvent;
import java.util.*;

import static com.iba.pts.pms.poss.api.Home.Reference.*;
import static com.iba.pts.pms.poss.api.PaliRequest.Action.SCANNER_RECOVER;

/**
 * Access point (AP/HP) controller. AP controller communicates with hand pendant
 * and handles:
 * <ul>
 * <li>Displaying menu following the button pressed (e.g. goto, home,..)
 * <li>Selected menu item and execution of the corresponding action via other
 * components (PMS controller, devices, etc.)
 * <li>Listening to the HP keyboard to command jog/goto/speed toggle/etc.
 * </ul>
 *
 * CBCT Requirements:
 *   <ul>
 *   <li>PMSC-REQ-149 (v.1)
 *   <li>PMSC-REQ-391 (v.1)
 *   <li>PMSC-REQ-505 (v.1)
 *   </ul>
 */
public class ApController extends AbstractBean implements HpKeyListener,
        MenuRequestListener, AccessPointController, Scheduled, PpsForteErrorCodeListener,
        AccessPointProcessMonitor, HelperCallback
{
    private enum NozzleImagerScrutineerRecoverySteps
    {
        WARNING_MESSAGE,
        RESET_ALL_CU,
        RECOVER_NOZZLE,
        RESET_SRCU,
        CANCEL,
        STARTED, FINISH, NONE
    }
    private NozzleImagerScrutineerRecoverySteps mNozzleImagerRecoverNextStep = NozzleImagerScrutineerRecoverySteps.NONE;
    protected static final Logger LOGGER = Logger.getLogger(ApController.class);
    protected ResourceDictionary bundle = ResourceManager.getInstance().getDictionary("pms.ap.Ap");
    /**
     * Time period (in milliseconds) between detection of a state change and
     * check of the position.
     */
    private static final long STATE_CHANGED_TIMER_PERIOD = 500;
    private static final long STATE_CHANGED_TIMER_PERIOD_FOR_DNSH_CNSH = 2000;
    private static final long STATE_CHANGED_TIMER_PERIOD_FOR_DNSH_CNSH_HOMING = 6000;

    private static final long SRCU_RESET_PERIOD = 60000;
    /** Time period (in milliseconds) of proximity detection beeps. */
    private static final long PROXIMITY_TIMER_PERIOD = 1000;
    /** 20 seconds of free move before enabling the collision check automatically.*/
    private static final long COLLISION_TIMER_PERIOD = 1000L * 20L;
    /** 500 milliseconds of free move only if the user does not accept the disabling of the collision system.*/
    private static final long FAST_COLLISION_TIMER_PERIOD = 500;

    /** HELPERS **/
    private List<AbstractHelper> mHelpersContainer = new ArrayList<>();
    private GotoHelperImpl mGotoHelper;
    private JogHelperImpl mJogHelper;
    private PaliHelperImpl mPaliHelper;
    private CTHelperImpl mCTHelper;
    private HomeHelperImpl mHomeHelper;
    private ImagingHelperImpl mImagingHelper;
    private ElbowFlipHelperImpl mElbowFlipHelper;
    private DockingHelperImpl mDockingHelper;

    private int mLightCommandTimeout = 10; //Default value 10 min
    private CollisionManager mCollisionManager;
    private boolean mAskForDisableCollisionConfirmation;
    private Timer mCollisionTimer;
    private Timer mDelayCollisionTimer;
    private Timer mDelaySnoutBrakeReleased;
    private Timer mWeightCheckTimer;
    private PPVSModelImpl mPpvsModel;
    private ActivityModelImpl mActivityModel;
    private boolean mCbctPresent;
    private boolean mWorkflowStartedMessage;
    private boolean mWorkflowMessageConfirmation;

    private ProximityManager mProximityManager;
    /** Container of devices. * */
    private DevicesContainer mDevices;
    /** Container of predefined positions. * */
    private PpdPositionsContainer mPositions;
    /** Reference to the TR hand pendant object. * */
    private HandPendant mTrHandPendant;
    /** Reference to the TCR hand pendant object. * */
    private HandPendant mTcrHandPendant;
    /** Reference to the TR2 hand pendant object. * */
    private HandPendant mTr2HandPendant;
    /** Save position request. */
    private SaveRequest mSaveRequest;
    /** Jog mode: incremental or continuous. * */
    private Jog.Type mJogType;
    /** Coordinate System for jogging. * */
    private Jog.Mode mJogMode;
    /** Motion speed level: high or low. * */
    private Speed.Level mSpeedLevel = Speed.Level.LOW;
    /** Devices activated *.*/

    private Set<Label> mDevicesActivated = Collections.synchronizedSet(new HashSet<Label>());
    /** Device selected via hand pendant. * */
    private PmsDevice mSelectedDevice;
    /** Indicates if we are currently performing a docking movement. */
    private boolean mIsPerformingDockingMovement;
    /** The currently selected position label. */
    private PpdPositionLabel mSelectedPositionLabel = null;
    /** Locally stored timer factory. */
    private TimerFactory mTimerFactory;
    /** Device state change delay timer. */
    private Timer mTimer;
    /** Timer to beep on proximity. */
    private Timer mProximityTimer;
    /** Timer to deactivate all devices after srcu reset.*/
    private Timer mDeactivateTimer;
    /** Timer to deactivate all lights after a parametrizable delay.*/
    private Timer mDeactivateLightsTimer;
    /** Cached event to be handled after a certain delay. */
    private PropertyChangeEvent mCachedEvent;
    /** Whether we are in kVkV sequential mode radA, radB or false */
    private String mKVkVSequential;
    /** Whether the HP access point is enabled. */
    private boolean mEnabled;
    /** Indicates if we have to display the target not reached message or not.*/

    private boolean mShowTargetNotReachedMessage;
    private boolean mShowCTNotAwayTableMessage;
    private PatientPositioningDevice mTargetNotReachedDevice;
    /** Whether the RPD feature is present. */
    private boolean mRpdPresent;
    /**
     * In this field we store a key that activated a motion.
     * For instance if the motion was started on P+ key (jog),
     * <code>HpKey.JOG_PPITCH</code> will be stored here.
     * This key is verified every time with 'refresh' frame.
     */
    private HpKey mActivationKey = null;
    private ContinuousMove mCurrentContinuousMove;
    private boolean mConnected = true;
    /** HP display zone for rendering jog mode (INC or CON) icon. * */
    private static final int JOG_MODE_DISPLAY_ZONE = 13;
    /** HP display zone for rendering RPD selection (SEL or blank) icon. * */
    private static final int SEL_DISPLAY_ZONE = 14;
    /** HP display zone for rendering speed level icon (H or L). * */
    private static final int SPEED_DISPLAY_ZONE = 15;
    /** HP display zone for rendering jog system (EQ, ISO, etc.) icon. * */
    private static final int JOG_SYSTEM_DISPLAY_ZONE = 0;
    /** True if we want to display the docking status panel and false otherwise.*/

    private boolean mCorrectionVectorReceived;
    private boolean mCorrectionVectorOutsideTolerance;
    private PositionValidityManager mPositionValidityManager;
    private GantryCorrection mGantryCorrection;
    private AccessPointMotion mAccessPointMotion;
    boolean mTrEnabled;
    boolean mTcrEnabled;
    boolean mTr2Enabled;
    private String mGantryType;
    private CheckManager mCheckManagerPps;
    private CheckManager mCheckManagerPositioning;
    private PpdPositionChecks mPositionChecks;
    private boolean mPPSLeftRightArmOption;
    private Set<Object> lockRequesterSet = new HashSet<>();

    /**
     * Create an Access Point Controller associated to a particular hand pendant.
     *
     * @param pDevices            the devices to control.
     * @param pPositions          the positions.
     * @param pTrHandPendant      Treatment room Hand pendant reference.
     * @param pTcrHandPendant     Treatment control room Hand pendant reference.
     * @param pPPSLeftRightArmOption Allow Left Right Elbow
     */
    public ApController(DevicesContainer pDevices,
                        PpdPositionsContainer pPositions,
                        PositionValidityManager pPositionValidityManager,
                        GantryCorrection pGantryCorrection,
                        HandPendant pTrHandPendant,
                        HandPendant pTcrHandPendant,
                        HandPendant pTr2HandPendant,
                        boolean pPPSLeftRightArmOption,
                        GcfApplier pGcfApplier)
    {
        mDevices = pDevices;
        mDevices.addPropertyChangeListener(this);
        mPositions = pPositions;
        mGantryCorrection = pGantryCorrection;
        mPositionValidityManager = pPositionValidityManager;
        mTrHandPendant = pTrHandPendant;
        mTcrHandPendant = pTcrHandPendant;
        mTr2HandPendant = pTr2HandPendant;
        mPPSLeftRightArmOption = pPPSLeftRightArmOption;
        mGcfApplier = pGcfApplier;
        mTrHandPendant.getKeyboard().addKeyListener(this);
        mTrHandPendant.getBackEnd().addPropertyChangeListener(this);
        mTcrHandPendant.getKeyboard().addKeyListener(this);
        mTcrHandPendant.getBackEnd().addPropertyChangeListener(this);
        mTr2HandPendant.getKeyboard().addKeyListener(this);
        mTr2HandPendant.getBackEnd().addPropertyChangeListener(this);
        mEnabled = true;
        // Look for PPS Forte and if found subscribe for its error codes.
        PmsDevice ppsDevice = mDevices.getDevice(Label.PPS);
        if (ppsDevice != null && ppsDevice instanceof PpsForte)
        {
            ((PpsForte) ppsDevice).addErrorCodeListener(this);
        }
        mAccessPointMotion = (AccessPointMotion) mDevices.getDevice(Label.ACCESS_POINT_MOTION);
        mAccessPointMotion.addPropertyChangeListener(this);
        mGotoHelper = new GotoHelperImpl(this, mPositions, mDevices, mPositionValidityManager, mGantryCorrection, mPPSLeftRightArmOption, mGcfApplier);
        mJogHelper = new JogHelperImpl(this, mDevices);
        mPaliHelper = new PaliHelperImpl(this, mDevices);
        mCTHelper = new CTHelperImpl(this, mDevices);
        mHomeHelper = new HomeHelperImpl(this, mDevices);
        mImagingHelper = new ImagingHelperImpl(this, mDevices);
        mElbowFlipHelper = new ElbowFlipHelperImpl(this, mDevices);
        mDockingHelper = new DockingHelperImpl(this, mPositions, mDevices);
        mHelpersContainer.add(mHomeHelper);
        mHelpersContainer.add(mGotoHelper);
        mHelpersContainer.add(mElbowFlipHelper);
        mHelpersContainer.add(mCTHelper);
        mHelpersContainer.add(mDockingHelper);
        mHelpersContainer.add(mImagingHelper);
        mHelpersContainer.add(mPaliHelper);
    }

    /**
     * setPpvsModel.
     * @param pPpvsModel
     */
    public void setPpvsModel(PPVSModelImpl pPpvsModel)
    {
        mPpvsModel = pPpvsModel;
        mPpvsModel.addPropertyChangeListener(this);
    }

    /**
     * setActivityModel.
     * @param pActivityModel
     */
    public void setActivityModel(ActivityModelImpl pActivityModel)
    {
        mActivityModel = pActivityModel;
    }

    /**
     * setCbctController.
     * @param pCbct
     */
    public void setCbctController(Cbct pCbct)
    {
        cbct = pCbct;
        mGotoHelper.setCbctController(pCbct);
    }

    /**
     * setCbctPresent.
     * @param pCbctPresent
     */
    public void setCbctPresent(boolean pCbctPresent)
    {
        mCbctPresent = pCbctPresent;
    }

    private void startCollisionManagementPhase(long pPhaseDuration)
    {
        if (mCollisionTimer == null)
        {
            mCollisionTimer = mTimerFactory.create(this);
        }
        mCollisionTimer.schedule(pPhaseDuration);
    }

    private boolean isCollisionManagementPhaseRunning()
    {
        return mCollisionTimer != null && mCollisionTimer.getState() == TimerState.PENDING;
    }

    private void startDelayBeforeCollisionDisabling()
    {
        if (mDelayCollisionTimer == null)
        {
            mDelayCollisionTimer = mTimerFactory.create(this);
        }
        mDelayCollisionTimer.schedule(200);
    }

    private boolean isDelayBeforeCollisionDisablingRunning()
    {
        return mDelayCollisionTimer != null && mDelayCollisionTimer.getState() == TimerState.PENDING;
    }

    private void startDelaySnoutBrakeReleased()
    {
        if (mDelaySnoutBrakeReleased == null)
        {
            mDelaySnoutBrakeReleased = mTimerFactory.create(this);
        }
        mDelaySnoutBrakeReleased.schedule(2000);
    }

    private boolean isDelaySnoutBrakeReleasedRunning()
    {
        return mDelaySnoutBrakeReleased != null && mDelaySnoutBrakeReleased.getState() == TimerState.PENDING;
    }

    /**
     * getKVkVSequential.
     * @return String
     */
    public String getKVkVSequential()
    {
        return mKVkVSequential;
    }

    /**
     * setKVkVSequential.
     * @param pKVkVSequential
     */
    public void setKVkVSequential(String pKVkVSequential)
    {
        mKVkVSequential = pKVkVSequential;
    }

    /**
     * getTrHandPendant.
     * @return HandPendant
     */
    public HandPendant getTrHandPendant()
    {
        return mTrHandPendant;
    }

    /**
     * getTcrHandPendant.
     * @return HandPendant
     */
    public HandPendant getTcrHandPendant()
    {
        return mTcrHandPendant;
    }

    /**
     * getTr2HandPendant.
     * @return HandPendant
     */
    public HandPendant getTr2HandPendant()
    {
        return mTr2HandPendant;
    }

    /**
     * isRpdPresent.
     * @return boolean
     */
    public boolean isRpdPresent()
    {
        return mRpdPresent;
    }

    /**
     * setRpdPresent.
     * @param pRpdPresent
     */
    public void setRpdPresent(boolean pRpdPresent)
    {
        mRpdPresent = pRpdPresent;
    }

    /**
     * Assign a timer factory.
     *
     * @param pTimerFactory Timer factory to be assigned.
     */
    public void setTimerFactory(TimerFactory pTimerFactory)
    {
        Assert.notNull(pTimerFactory, "The timer factory may not be null.");
        mTimerFactory = pTimerFactory;
    }

    /**
     * setProximityManager.
     * @param pProximityManager
     */
    public void setProximityManager(ProximityManager pProximityManager)
    {
        mProximityManager = pProximityManager;
        mProximityManager.addPropertyChangeListener(this);
    }

    /**
     * setCollisionManager.
     * @param pCollisionManager
     */
    public void setCollisionManager(CollisionManager pCollisionManager)
    {
        mCollisionManager = pCollisionManager;
        mCollisionManager.addPropertyChangeListener(this);
    }

    /**
     * isTcrEnabled.
     * @return boolean
     */
    public boolean isTcrEnabled()
    {
        return mTcrEnabled;
    }

    /**
     * setTcrEnabled.
     * @param pTcrEnabled
     */
    public void setTcrEnabled(boolean pTcrEnabled)
    {
        mTcrEnabled = pTcrEnabled;
    }

    /**
     * isTrEnabled.
     * @return boolean
     */
    public boolean isTrEnabled()
    {
        return mTrEnabled;
    }

    /**
     * setTrEnabled.
     * @param pTrEnabled
     */
    public void setTrEnabled(boolean pTrEnabled)
    {
        mTrEnabled = pTrEnabled;
    }

    /**
     * isTr2Enabled.
     * @return boolean
     */
    public boolean isTr2Enabled()
    {
        return mTr2Enabled;
    }

    /**
     * setTr2Enabled.
     * @param pTr2Enabled
     */
    public void setTr2Enabled(boolean pTr2Enabled)
    {
        mTr2Enabled = pTr2Enabled;
    }

    /** {@inheritDoc} */
    public boolean isEnabled()
    {
        return mEnabled;
    }

    private void enable()
    {
        if (!isEnabled())
        {
            initializeHandPendants();
            setTrEnabled(true);
            setTcrEnabled(true);
            setTr2Enabled(true);
            mEnabled = true;
            setSpeedLevel(getSpeedLevel());
            enableHandPendantUi();
        }
    }
    /** {@inheritDoc} */
    @Override
    public void enable(Object requester)
    {
        if (requester==null)
        {
            lockRequesterSet.clear();
            enable();
            return;
        }
        lockRequesterSet.remove(requester);
        if (lockRequesterSet.isEmpty())
        {
            enable();
        }
    }

    /** {@inheritDoc} */
    @Override
    public void disable(Object requester)
    {
        if (requester == null)
        {
            disable();
            return;
        }
        lockRequesterSet.add(requester);
        disable();
    }

    private void disable()
    {
        mDevices.deactivateAll();
        mEnabled = false;
        setTrEnabled(false);
        setTcrEnabled(false);
        setTr2Enabled(false);
        disableTr(PmsUtils.getLocalizedMessageByKey(bundle, "pms.ap.controller.locked"));
        disableTcr(PmsUtils.getLocalizedMessageByKey(bundle, "pms.ap.controller.locked"));
        disableTr2(PmsUtils.getLocalizedMessageByKey(bundle, "pms.ap.controller.locked"));
        hideHandPendantUi();
    }

    /** {@inheritDoc} */
    public void disableForCbct(Object requester)
    {
        disable(requester);
        disableTr(PmsUtils.getLocalizedMessageByKey(bundle, "pms.ap.controller.lockedForCbct"));
        disableTcr(PmsUtils.getLocalizedMessageByKey(bundle, "pms.ap.controller.lockedForCbct"));
        disableTr2(PmsUtils.getLocalizedMessageByKey(bundle, "pms.ap.controller.lockedForCbct"));
    }

    void prepareSwitchHp()
    {
        mDevices.deactivateAll();
        setSpeedLevel(Speed.Level.LOW);
        //setJogMode(Jog.Mode.EQUIPMENT);
        setJogType(Jog.Type.CONTINUOUS);

        if (mJogMode == Jog.Mode.ISOCENTRIC)
        {
            setJogMode(Jog.Mode.ISOCENTRIC);
        }
        else
        {
            setJogMode(Jog.Mode.EQUIPMENT);
        }
    }


    /**
     * disableTr.
     * @param message
     */
    public void disableTr(String message)
    {
        setTrEnabled(false);
        mTrHandPendant.clearAll();
        mTrHandPendant.clearLeds();
        mTrHandPendant.showInfo(message);
        mTrHandPendant.getDisplay().getDisplayZone(SEL_DISPLAY_ZONE).setText("");
    }

    /**
     * disableTcr.
     * @param message
     */
    public void disableTcr(String message)
    {
        setTcrEnabled(false);
        mTcrHandPendant.clearAll();
        mTcrHandPendant.clearLeds();
        mTcrHandPendant.showInfo(message);
        mTcrHandPendant.getDisplay().getDisplayZone(SEL_DISPLAY_ZONE).setText("");
    }

    /**
     * disableTr2.
     * @param message
     */
    public void disableTr2(String message)
    {
        setTr2Enabled(false);
        mTr2HandPendant.clearAll();
        mTr2HandPendant.clearLeds();
        mTr2HandPendant.showInfo(message);
        mTr2HandPendant.getDisplay().getDisplayZone(SEL_DISPLAY_ZONE).setText("");
    }

    /** Provoke a short beep of HP. */
    private void shortBeep()
    {

        switch (mAccessPointMotion.getSelected())
        {
            case AccessPointMotion.TR_ACCESS_POINT:
                mTrHandPendant.beep(1);
                break;
            case AccessPointMotion.TCR_RPD_ACCESS_POINT:
            case AccessPointMotion.TCR_SAFETY_RACK_ACCESS_POINT:
                mTcrHandPendant.beep(1);
                break;
            case AccessPointMotion.TR_HP2_ACCESS_POINT:
                mTr2HandPendant.beep(1);
                break;
        }
    }

    /**
     * Provoke a long beep of the HP indicated by the index.

     *
     * @param hpIndex 0 all connected hand pendants, >0 the corresponding hand pendant
     */

    void longBeep(int hpIndex)
    {
        switch (hpIndex)
        {
            case AccessPointMotion.TR_ACCESS_POINT:
                mTrHandPendant.beep(300);
                break;
            case AccessPointMotion.TCR_RPD_ACCESS_POINT:
            case AccessPointMotion.TCR_SAFETY_RACK_ACCESS_POINT:
                mTcrHandPendant.beep(300);
                break;
            case AccessPointMotion.TR_HP2_ACCESS_POINT:
                mTr2HandPendant.beep(300);
                break;
        }
    }

    /**
     * Assign an activation key.
     *
     * @param pKey Motion key.
     */
    private synchronized void setActivationKey(HpKey pKey)
    {
        mActivationKey = pKey;
    }

    /**
     * Return current activation key.
     *
     * @return Activation key or <code>null</code> if not set.
     */
    private synchronized HpKey getActivationKey()
    {
        return mActivationKey;
    }

    /**
     * setConnected.
     * @param pConnected
     */
    public void setConnected(boolean pConnected)
    {
        mConnected = pConnected;
    }

    /**
     * isConnected.
     * @return boolean
     */
    public boolean isConnected()
    {
        return mConnected;
    }

    /**
     * Sets the jog mode.
     *
     * @param pValue The new jog mode.
     */
    private void setJogMode(Jog.Mode pValue)
    {
        mJogMode = pValue;

        if (mJogMode == Jog.Mode.EQUIPMENT)
        {
            setDisplayZoneText(JOG_SYSTEM_DISPLAY_ZONE,
                    PmsUtils.getLocalizedMessageByKey(bundle, "pms.ap.controller.label.equipment"));
            LOGGER.info("##PR103341## : Jog mode set to EQUIPMENT");
        }
        else if (mJogMode == Jog.Mode.ISOCENTRIC)
        {
            setDisplayZoneText(JOG_SYSTEM_DISPLAY_ZONE,
                    PmsUtils.getLocalizedMessageByKey(bundle, "pms.ap.controller.label.isocentric"));
            LOGGER.info("##PR103341## : Jog mode set to ISOCENTRIC");
        }
    }

    /** Toggle jog mode (used to avoid CS menu generation). */
    private void toggleJogMode()
    {
        if (mJogMode == Jog.Mode.EQUIPMENT)
        {
            setJogMode(Jog.Mode.ISOCENTRIC);
        }
        else if (mJogMode == Jog.Mode.ISOCENTRIC)
        {
            setJogMode(Jog.Mode.EQUIPMENT);
        }
    }

    /**
     * Sets the Docking Request.
     *
     * @param pRequest the request, may be <code>null</code>.
     */
    private void setDockingRequest(DockingRequest pRequest)
    {
        StatusResult sr = mDockingHelper.setDockingByRequest(pRequest);
        showMessage(sr);
        if(sr.isResetRequest())
        {
            mDockingHelper.clearRequest();
        }
        setLedOn(HpLed.MOVE, mDockingHelper.isRequestPrepared());
    }

    /**
     * Sets the Go-To request.
     *
     * @param pRequest the request, may be <code>null</code>.
     */
    private void setGoToRequest(MotionRequest pRequest)
    {
        mGotoHelper.setGotoByRequest(pRequest);
        if(mGotoHelper.isRequestPrepared())
        {
            StatusResult sr = mGotoHelper.isDeviceReadyForGoto(mSelectedDevice);
            showMessage(sr);
            setLedOn(HpLed.MOVE, sr.isStatus());
            if(sr.isStatus())
            {
                showMessage(mGotoHelper.isDeviceAtPosition(mSelectedDevice.getLabel()));
            }
            else
            {
                mGotoHelper.setGotoByRequest(null);
            }
        }
        else
        {
            setLedOn(HpLed.MOVE, false);
        }
    }

    /**
     * Sets the position selection request.
     *
     * @param pRequest The request.
     */
    private void setPositionRequest(PositionRequest pRequest)
    {
        final PpdPositionLabel oldLabel = mSelectedPositionLabel;
        PpdPositionLabel newLabel = PpdPositionLabel.NONE;

        if (pRequest != null)
        {
            newLabel = pRequest.getPpdPositionLabel();
            switch (newLabel)
            {
                case SETUP:
                case SETUP_90:
                case TREATMENT:
                case TREATMENT_90:
                case TREATMENT_ROT_ONLY:
                    if(!areHomableDevicesHomed())
                    {
                        return;
                    }
                    break;
                default:
                    break;
            }
            mSelectedPositionLabel = newLabel;
        }
        if (newLabel == PpdPositionLabel.HOME)
        {
            mHomeHelper.clearRequest();
            setLedOn(HpLed.MOVE, false);
            newLabel = PpdPositionLabel.NONE;
        }
        if (newLabel != PpdPositionLabel.NONE && newLabel != PpdPositionLabel.NOZZLE_IMAGER_POSITION)
        {
            firePropertyChange(SELECTED_POSITION_LABEL_PROPERTY, oldLabel, newLabel.toLowerCaseString());
            Pps pps = (Pps) mDevices.getDevice(Label.PPS);
            Gantry gantry = (Gantry) mDevices.getDevice(Label.GANTRY);
            Snout snout = (Snout) mDevices.getDevice(Label.SNOUT);
            if(newLabel == PpdPositionLabel.CT && pps instanceof CtOnRailPositionable)
            {
                ((CtOnRailPositionable) pps).setIECCTTargetPosition(0.0);

                mGantryCorrection.applyCorrectionOnCurrentPosition(false); //we do not apply gcf on CT position

                if(gantry != null)
                {
                    gantry.setGantryTargetPosition(gantry.getGantryPosition());
                }
                if(snout != null)
                {
                    snout.setSnoutTargetPosition(snout.getSnoutPosition());
                }
            }
            else
            {
                PpdPosition ppdPosition = mPositions.getPpdPositionByLabel(newLabel);
                if (ppdPosition != null)
                {
                    GcfApplier.Result gcfRes = mGcfApplier.isApplyGcf(mPositions, newLabel, ppdPosition.getGantryPosition(), ppdPosition.getSnoutPosition());
                    if(gcfRes.getApplyGcf())
                    {
                        if (gcfRes.getTargetGantryPosition() != null)
                        {
                            mGantryCorrection.setGantryTarget(gcfRes.getTargetGantryPosition());
                        }

                        if (gcfRes.getTargetSnoutPosition() != null)
                        {
                            mGantryCorrection.setSnoutTarget(gcfRes.getTargetSnoutPosition());
                        }
                    }
                    mGantryCorrection.applyCorrectionOnCurrentPosition(gcfRes.getApplyGcf());

                    if (mPPSLeftRightArmOption)
                    {
                        if (pps instanceof ElbowFlippable)
                        {
                            firePropertyChange(SELECTED_ELBOW_POSITION_LABEL_PROPERTY, null,
                                    ((ElbowFlippable) pps).getElbowOrientation());
                        }
                    }

                    if (gantry != null)
                    {
                        gantry.setGantryTargetPosition(ppdPosition.getGantryPosition());
                    }
                    if (snout != null)
                    {
                        snout.setSnoutTargetPosition(ppdPosition.getSnoutPosition());
                    }
                    if (pps != null)
                    {
                        if (newLabel == PpdPositionLabel.MEMORY ||
                                newLabel == PpdPositionLabel.USER)
                        {
                            UserPositionImpl userPosition = (UserPositionImpl) ppdPosition;

                            if (!userPosition.useIec())
                            {
                                double[] pos = new double[6];
                                pos[0] = PmsUtils.mmToCm(userPosition.getPpsPosition().getX());
                                pos[1] = PmsUtils.mmToCm(userPosition.getPpsPosition().getY());
                                pos[2] = PmsUtils.mmToCm(userPosition.getPpsPosition().getZ());
                                pos[3] = userPosition.getPpsPosition().getRotation();
                                pos[4] = userPosition.getPpsPosition().getPitch();
                                pos[5] = userPosition.getPpsPosition().getRoll();
                                PpsPosition iec = pps.realEquipmentToIdealIEC(pos, null, null);
                                pps.setTargetPpsPosition(iec);
                            }
                            else
                            {
                                pps.setTargetPpsPosition(ppdPosition.getPpsPosition());
                            }

                        }
                        else
                        {
                            pps.setTargetPpsPosition(ppdPosition.getPpsPosition());
                        }
                        if (pps instanceof RenovatedPpsForteProxy)
                        {
                            ((RenovatedPpsForteProxy) pps).setHeadNeckChairTargetPosition(
                                    ppdPosition.getHeadNeckChairPosition());
                        }
                    }
                }
                else
                {
                    firePropertyChange(SELECTED_ELBOW_POSITION_LABEL_PROPERTY, null, ElbowOrientation.NONE);
                }
            }
        }
    }

    /**
     * Resets motion activation watchdog.

     * The watchdog is reset only if activation key is currently pressed (for TR hand pendant) and
     * only if activation key is currently pressed or motion button is pressed (for TCR hand pendant).
     * If not, the watchdog is reset during a grace period in order to assure smooth soft stop.
     *
     * @param pHandPendant the hand pendant source of the event triggering watchdog reset
     */
    private void resetActivationWatchdog(HandPendant pHandPendant)
    {
        if (pHandPendant.equals(mTrHandPendant))
        {
            if (mAccessPointMotion.getSelected() == AccessPointMotion.TR_ACCESS_POINT)
            {
                if (pHandPendant.getKeyboard().isKeyPressed(getActivationKey()))
                {
                    if (mCurrentContinuousMove != null)
                    {
                        mCurrentContinuousMove.perform();
                        if (mSelectedDevice instanceof RenovatedSnoutHolderProxy && !isDelaySnoutBrakeReleasedRunning())
                        {
                            startDelaySnoutBrakeReleased();
                        }
                    }
                }
                else
                {
                    mCurrentContinuousMove = null;
                    if (!isDelayBeforeCollisionDisablingRunning())
                    {
                        mCollisionManager.disableCollisionDetection(getClass().getSimpleName());
                    }
                }
            }
        }
        else if (pHandPendant.equals(mTr2HandPendant))
        {
            if (mAccessPointMotion.getSelected() == AccessPointMotion.TR_HP2_ACCESS_POINT)
            {
                if (pHandPendant.getKeyboard().isKeyPressed(getActivationKey()))
                {
                    if (mCurrentContinuousMove != null)
                    {
                        mCurrentContinuousMove.perform();
                        if (mSelectedDevice instanceof RenovatedSnoutHolderProxy && !isDelaySnoutBrakeReleasedRunning())
                        {
                            startDelaySnoutBrakeReleased();
                        }
                    }
                }
                else
                {
                    mCurrentContinuousMove = null;
                    if (!isDelayBeforeCollisionDisablingRunning())
                    {
                        mCollisionManager.disableCollisionDetection(getClass().getSimpleName());
                    }
                }
            }
        }
        else if (pHandPendant.equals(mTcrHandPendant))
        {
            /*TCR HP in RPD mode*/
            if (mAccessPointMotion.getSelected() == AccessPointMotion.TCR_RPD_ACCESS_POINT)
            {
                if (mAccessPointMotion.isMotionPressed())
                {
                    if (mCurrentContinuousMove != null)
                    {
                        mCurrentContinuousMove.perform();
                        if (mSelectedDevice instanceof RenovatedSnoutHolderProxy && !isDelaySnoutBrakeReleasedRunning())
                        {
                            startDelaySnoutBrakeReleased();
                        }
                    }
                }
                else
                {
                    mCurrentContinuousMove = null;
                    if (!isDelayBeforeCollisionDisablingRunning())
                    {
                        mCollisionManager.disableCollisionDetection(getClass().getSimpleName());
                    }
                }
            }

            /* TCR HP in service mode*/
            if (mAccessPointMotion.getSelected() == AccessPointMotion.TCR_SAFETY_RACK_ACCESS_POINT)
            {
                if (pHandPendant.getKeyboard().isKeyPressed(getActivationKey()))
                {
                    if (mCurrentContinuousMove != null)
                    {
                        mCurrentContinuousMove.perform();
                        if (mSelectedDevice instanceof RenovatedSnoutHolderProxy && !isDelaySnoutBrakeReleasedRunning())
                        {
                            startDelaySnoutBrakeReleased();
                        }
                    }
                }
                else
                {
                    mCurrentContinuousMove = null;
                    if (!isDelayBeforeCollisionDisablingRunning())
                    {
                        mCollisionManager.disableCollisionDetection(getClass().getSimpleName());
                    }
                }
            }
        }
    }

    private void initializeHandPendants()
    {
        switch (mAccessPointMotion.getSelected())
        {
            case AccessPointMotion.NO_ACCESS_POINT:
                prepareSwitchHp();
                disableTr(PmsUtils.getLocalizedMessageByKey(bundle, "pms.ap.controller.locked"));
                disableTcr(PmsUtils.getLocalizedMessageByKey(bundle, "pms.ap.controller.locked"));
                disableTr2(PmsUtils.getLocalizedMessageByKey(bundle, "pms.ap.controller.locked"));
                break;
            case AccessPointMotion.TR_ACCESS_POINT:
                clear();
                prepareSwitchHp();
                setTrEnabled(true);
                disableTcr(PmsUtils.getLocalizedMessageByKey(bundle, "pms.ap.controller.lockedByTr"));
                disableTr2(PmsUtils.getLocalizedMessageByKey(bundle, "pms.ap.controller.lockedByTr"));
                setDisplayZoneText(SEL_DISPLAY_ZONE,
                        PmsUtils.getLocalizedMessageByKey(bundle, "pms.ap.controller.label.selected"));
                break;
            case AccessPointMotion.TCR_RPD_ACCESS_POINT:
                /*send mode = RPD*/
                setMode(mAccessPointMotion.getSelected());
                if (isRpdPresent())
                {
                    clear();
                    prepareSwitchHp();
                    setTcrEnabled(true);
                    disableTr(PmsUtils.getLocalizedMessageByKey(bundle, "pms.ap.controller.lockedByTcr"));
                    disableTr2(PmsUtils.getLocalizedMessageByKey(bundle, "pms.ap.controller.lockedByTcr"));
                    setDisplayZoneText(SEL_DISPLAY_ZONE,
                            PmsUtils.getLocalizedMessageByKey(bundle, "pms.ap.controller.label.selected"));
                }
                else
                {
                    prepareSwitchHp();
                    disableTcr(PmsUtils.getLocalizedMessageByKey(bundle, "pms.ap.controller.locked"));
                }
                break;
            case AccessPointMotion.TCR_SAFETY_RACK_ACCESS_POINT:
                /*send mode = Service*/

                setMode(mAccessPointMotion.getSelected());
                clear();
                prepareSwitchHp();
                setTcrEnabled(true);
                disableTr(PmsUtils.getLocalizedMessageByKey(bundle, "pms.ap.controller.lockedByTcr"));
                disableTr2(PmsUtils.getLocalizedMessageByKey(bundle, "pms.ap.controller.lockedByTcr"));
                setDisplayZoneText(SEL_DISPLAY_ZONE,
                        PmsUtils.getLocalizedMessageByKey(bundle, "pms.ap.controller.label.selected"));
                break;
            case AccessPointMotion.TR_HP2_ACCESS_POINT:
                clear();
                prepareSwitchHp();
                setTr2Enabled(true);
                disableTr(PmsUtils.getLocalizedMessageByKey(bundle, "pms.ap.controller.lockedByTr2"));
                disableTcr(PmsUtils.getLocalizedMessageByKey(bundle, "pms.ap.controller.lockedByTr2"));
                setDisplayZoneText(SEL_DISPLAY_ZONE,
                        PmsUtils.getLocalizedMessageByKey(bundle, "pms.ap.controller.label.selected"));
                break;
        }
        if(mPPSLeftRightArmOption)
        {
            PmsDevice pps = mDevices.getDevice(Label.PPS);
            if(pps instanceof ElbowFlippable)
            {
                setDisplayZoneText(3, PmsUtils.getLocalizedPPSElbowOrientationByKey(((ElbowFlippable) pps).getElbowOrientation()));
            }
        }
    }

    /** {@inheritDoc} */
    public void propertyChange(PropertyChangeEvent pEvt)
    {
        if (pEvt.getSource() == mTrHandPendant.getBackEnd())
        {

            if (pEvt.getPropertyName().equals(HpBackEnd.CONNECTED_PROPERTY) && isEnabled())
            {
                if (Boolean.TRUE.equals(pEvt.getNewValue()))
                {
                    performConnected(mTrHandPendant);
                }
                else
                {
                    performDisconnected(mTrHandPendant);
                }
            }
            else if (pEvt.getPropertyName().equals(HpBackEnd.FRAME_COUNTER_PROPERTY))
            {
                // Reset continuous activation watchdog.
                if (isConnected())
                {
                    resetActivationWatchdog(mTrHandPendant);
                }
            }
        }
        else if (pEvt.getSource() == mTr2HandPendant.getBackEnd())
        {

            if (pEvt.getPropertyName().equals(HpBackEnd.CONNECTED_PROPERTY) && isEnabled())
            {
                if (Boolean.TRUE.equals(pEvt.getNewValue()))
                {
                    performConnected(mTr2HandPendant);
                }
                else
                {
                    performDisconnected(mTr2HandPendant);
                }
            }
            else if (pEvt.getPropertyName().equals(HpBackEnd.FRAME_COUNTER_PROPERTY))
            {
                // Reset continuous activation watchdog.
                if (isConnected())
                {
                    resetActivationWatchdog(mTr2HandPendant);
                }
            }
        }
        else if (pEvt.getSource() == mTcrHandPendant.getBackEnd())
        {
            if (pEvt.getPropertyName().equals(HpBackEnd.CONNECTED_PROPERTY) && isEnabled())
            {
                if (Boolean.TRUE.equals(pEvt.getNewValue()))
                {
                    performConnected(mTcrHandPendant);
                }
                else
                {
                    performDisconnected(mTcrHandPendant);
                }
            }
            else if (pEvt.getPropertyName().equals(HpBackEnd.FRAME_COUNTER_PROPERTY))
            {
                // Reset continuous activation watchdog.
                if (isConnected())
                {
                    resetActivationWatchdog(mTcrHandPendant);
                }
            }
        }

        if (pEvt.getPropertyName().equals("motionPressed"))
        {
            if (mAccessPointMotion.isMotionPressed())
            {
                handleMoveKey();
            }
            else
            {
                showWorkflowStartedMessage();
            }
        }

        if (pEvt.getPropertyName().equals("selected") && isEnabled())
        {
            initializeHandPendants();
        }

        if (pEvt.getPropertyName().equals("mebPressed") && isEnabled())
        {
            if (!mAccessPointMotion.isMebPressed())
            {
                // notify other Hp that it can take control
                if (mAccessPointMotion.getSelected() == AccessPointMotion.TR_ACCESS_POINT)
                {
                    mTcrHandPendant.clear();
                    mTcrHandPendant.showInfo(PmsUtils.getLocalizedMessageByKey(bundle, "pms.ap.controller.canTakeControl"));
                    mTr2HandPendant.clear();
                    mTr2HandPendant.showInfo(PmsUtils.getLocalizedMessageByKey(bundle, "pms.ap.controller.canTakeControl"));
                }
                else if (mAccessPointMotion.getSelected() == AccessPointMotion.TCR_RPD_ACCESS_POINT
                        || mAccessPointMotion.getSelected() == AccessPointMotion.TCR_SAFETY_RACK_ACCESS_POINT)
                {
                    mTrHandPendant.clear();
                    mTrHandPendant.showInfo(PmsUtils.getLocalizedMessageByKey(bundle, "pms.ap.controller.canTakeControl"));
                    mTr2HandPendant.clear();
                    mTr2HandPendant.showInfo(PmsUtils.getLocalizedMessageByKey(bundle, "pms.ap.controller.canTakeControl"));
                }
                else if (mAccessPointMotion.getSelected() == AccessPointMotion.TR_HP2_ACCESS_POINT)
                {
                    mTrHandPendant.clear();
                    mTrHandPendant.showInfo(PmsUtils.getLocalizedMessageByKey(bundle, "pms.ap.controller.canTakeControl"));
                    mTcrHandPendant.clear();
                    mTcrHandPendant.showInfo(PmsUtils.getLocalizedMessageByKey(bundle, "pms.ap.controller.canTakeControl"));
                }
            }
            else
            {
                // relock other Hp
                if (mAccessPointMotion.getSelected() == AccessPointMotion.TR_ACCESS_POINT)
                {
                    disableTcr(PmsUtils.getLocalizedMessageByKey(bundle, "pms.ap.controller.lockedByTr"));
                    disableTr2(PmsUtils.getLocalizedMessageByKey(bundle, "pms.ap.controller.lockedByTr"));
                }
                else if (mAccessPointMotion.getSelected() == AccessPointMotion.TCR_RPD_ACCESS_POINT
                        || mAccessPointMotion.getSelected() == AccessPointMotion.TCR_SAFETY_RACK_ACCESS_POINT)
                {
                    disableTr(PmsUtils.getLocalizedMessageByKey(bundle, "pms.ap.controller.lockedByTcr"));
                    disableTr2(PmsUtils.getLocalizedMessageByKey(bundle, "pms.ap.controller.lockedByTcr"));
                }
                else if (mAccessPointMotion.getSelected() == AccessPointMotion.TR_HP2_ACCESS_POINT)
                {
                    disableTr(PmsUtils.getLocalizedMessageByKey(bundle, "pms.ap.controller.lockedByTr2"));
                    disableTcr(PmsUtils.getLocalizedMessageByKey(bundle, "pms.ap.controller.lockedByTr2"));
                }
            }
        }

        if (pEvt.getPropertyName().equals("motionVeto") && !pEvt.getNewValue().equals(Movable.noMotionVeto)
                && !mCollisionManager.isInCollision()) // otherwise, this "if" will hide the collision wizard.
        {
            if ((mSelectedDevice instanceof Movable)
                    && !((Movable)mSelectedDevice).getMotionVeto().equals(Movable.noMotionVeto))
            {
                showError(PmsUtils.getLocalizedMessageByKey(bundle,
                        "pms.ap.controller.motionVetoPreventsMotion", pEvt.getNewValue().toString()));
                longBeep(mAccessPointMotion.getSelected());
            }
        }

        Object source = pEvt.getSource();
        if (source instanceof PmsDevice)
        {
            if (pEvt.getPropertyName().equals(Device.DEVICE_STATE_PROPERTY))
            {
                /* PPD has changed its state */
                if (pEvt.getNewValue().equals(Device.State.READY)
                        && source.equals(mSelectedDevice))
                {
                    /*
                     * It may happen that other properties has not been updated yet,
                     * so we cannot handle the device state change correctly. So, we
                     * start a timer that will handle the state change after a
                     * certain delay.
                     */
                    mCachedEvent = pEvt;
                    if (mTimer == null)
                    {
                        mTimer = mTimerFactory.create(this);
                    }

                    if(mSelectedDevice instanceof PlcSnout)
                    {
                        if(mHomeHelper.isRequestPrepared())
                        {
                            mTimer.schedule(STATE_CHANGED_TIMER_PERIOD_FOR_DNSH_CNSH_HOMING);
                        }
                        else
                        {
                            mTimer.schedule(STATE_CHANGED_TIMER_PERIOD_FOR_DNSH_CNSH);
                        }
                    }
                    else
                    {
                        mTimer.schedule(STATE_CHANGED_TIMER_PERIOD);
                    }
                }
            }
            else if (pEvt.getPropertyName().equals("enabled"))
            {
                if((Boolean) pEvt.getNewValue())
                {
                    synchronized (mDevicesActivated)
                    {
                        mDevicesActivated.add(((PmsDevice) source).getLabel());
                        mDevicesActivated = PmsDevice.Label.getGroupedSet(mDevicesActivated);
                    }
                    if(mSelectedDevice != null &&
                            mNozzleImagerRecoverNextStep == NozzleImagerScrutineerRecoverySteps.NONE &&
                            !mSelectedDevice.equals(source) &&
                            !(mSelectedDevice instanceof PlcCbct && source instanceof PlcCbct) &&
                            !(mSelectedDevice instanceof GantryImager && source instanceof GantryImager) &&
                            !(mSelectedDevice instanceof GantryImager && source instanceof GantryImagerPanelProxy) &&
                            !(mSelectedDevice instanceof ImagingSystem && source instanceof GantryImagerPanelProxy) &&
                            !(mSelectedDevice instanceof ImagingSystem &&
                                    (source instanceof DigitalImagingDevice ||
                                            source instanceof XRay)))
                    {
                        // https://www.youtube.com/watch?v=otCpCn0
                        // l4Wo
                        // We clear the HP when we activate a different device.
                        // Except when the selected device and the source are PlcCbct (NozzleImager: Panel, Collimator, Tube).
                        // => Because of the multiple devices activation system.
                        // Except when the selected device is an ImagingSystem and the moving devices are a Flat Panel or a Tube.
                        clear();
                    }
                }
                else
                {
                    synchronized (mDevicesActivated)
                    {
                        mDevicesActivated.remove(((PmsDevice) source).getLabel());
                        if (mSelectedDevice != null && mSelectedDevice.equals(source))
                        {
                            if(mNozzleImagerRecoverNextStep != NozzleImagerScrutineerRecoverySteps.NONE)
                            {
                                if(mNozzleImagerRecoverNextStep == NozzleImagerScrutineerRecoverySteps.CANCEL)
                                {
                                    mNozzleImagerRecoverNextStep = NozzleImagerScrutineerRecoverySteps.NONE;
                                }
                            }
                            if (mDevicesActivated.size() == 1)
                            {
                                mSelectedDevice = mDevices.getDevice((Label) mDevicesActivated.toArray()[0]);
                            }
                            else if (mDevicesActivated.size() > 1){
                                mSelectedDevice = null;
                                LOGGER.info("MORE THAN ONE DEVICE SELECTED => DESELECTED ALL IN THE HAND-PENDANT.");
                            } else if (mDevicesActivated.size() == 0) {
                                mSelectedDevice = null;
                                LOGGER.info("NO DEVICES ACTIVATED => DESELECTED ALL IN THE HAND-PENDANT.");
                            }
                        }
                    }
                }
                actualiseLeds();
            }
            else if (pEvt.getPropertyName().equals("safetyErrorStates") || pEvt.getPropertyName().equals("LaserCrossed"))
            {
                if (source instanceof PlcDevice &&
                        (
                                ((PlcDevice) source).isPanelScannerCrossed()
                                        || 		((PlcDevice) source).isTubeScannerCrossed()
                        )
                        )
                {
                    clear();
                    showError(
                            PmsUtils.getLocalizedMessageByKey(bundle, "pms.ap.controller.nozzleImagerSafetyErrorHP"));
                    mNozzleImagerRecoverNextStep = NozzleImagerScrutineerRecoverySteps.WARNING_MESSAGE;
                }
            }
            else if (pEvt.getPropertyName().equals("scannersBypassed") && !(Boolean)pEvt.getNewValue())
            {
                if(mNozzleImagerRecoverNextStep != NozzleImagerScrutineerRecoverySteps.NONE)
                {
                    clear();
                    mNozzleImagerRecoverNextStep = NozzleImagerScrutineerRecoverySteps.NONE;
                    mDevices.deactivateAll();
                }
                showInfo(PmsUtils.getLocalizedMessageByKey(bundle, "pms.ap.controller.nozzleLasersEnabled"));
                shortBeep();
            }
            else if(mPPSLeftRightArmOption && pEvt.getPropertyName().equals(ElbowFlippable.ElbowOrientationProperty))
            {
                setDisplayZoneText(3,PmsUtils.getLocalizedPPSElbowOrientationByKey((ElbowOrientation) pEvt.getNewValue()));
            }
        }
        else if (source instanceof DNSnoutHolderProxy)
        {
            if (pEvt.getPropertyName().equals("motionError1") && ((DNSnoutHolder) source).isEncoderErrorMismatch())
            {
                onMenuRequest(MenuRequest.createHomeMenuRequest(Label.SNOUT, Reference.HOME));
            }
        }
        else if (source instanceof ProximityManager)
        {
            if (pEvt.getPropertyName().equals(PcuR6.PROXY_DETECTED))
            {
                if (mProximityManager.isInProximity())
                {
                    if(getSpeedLevel() == Speed.Level.HIGH)
                    {
                        setSpeedLevel(Speed.Level.PROXIMITY);
                        shortBeep();
                    }

                    /* Starting beeping timer */
                    if (mProximityTimer == null)
                    {
                        mProximityTimer = mTimerFactory.create(this);
                    }
                    mProximityTimer.schedule(PROXIMITY_TIMER_PERIOD);
                }
            }
        }
        else if (source instanceof CollisionManager)
        {
            if (pEvt.getPropertyName().equals(PcuR6.COLLISION_DETECTED_PROPERTY))
            {
                if (mCollisionManager.isInCollision())
                {
                    if (mSelectedDevice != null && mSelectedDevice instanceof Movable)
                    {
                        ((Movable) mSelectedDevice).softStop();
                    }
                    mDevices.deactivateAll();
                    longBeep(mAccessPointMotion.getSelected());

                    showError(PmsUtils.getLocalizedMessageByKey(
                            bundle, "pms.ap.controller.collisionDetected", String.valueOf(COLLISION_TIMER_PERIOD / 1000)));

                    mAskForDisableCollisionConfirmation = true;
                }
            }
        }
        else if (source instanceof PmsController)
        {
            if (pEvt.getPropertyName().equals(ActivityController.CURRENT_ACTIVITY_NAME_PROPERTY))
            {
                String name = pEvt.getNewValue().toString();
                if (name.equals("PREPARE") || name.equals("LOCK"))
                {
                    areHomableDevicesHomed();
                }
            }
        }
        else if (source instanceof PPVSModel)
        {
            if (pEvt.getPropertyName().equals(PPVSModel.CORRECTION_VECTOR_PROPERTY))
            {
                showInfo(PmsUtils.getLocalizedMessageByKey(bundle, "pms.ap.controller.corrVectorReceived"));
                mCorrectionVectorReceived = true;
                mCorrectionVectorOutsideTolerance = false;
            }
            else if (pEvt.getPropertyName().equals(PPVSModel.CORRECTION_VECTOR_OUTSIDE_TOLERANCE_PROPERTY))
            {
                showError(PmsUtils.getLocalizedMessageByKey(bundle, "pms.ap.controller.correctionIsOutOfTolerance"));
                longBeep(mAccessPointMotion.getSelected());
                mCorrectionVectorReceived = true;
                mCorrectionVectorOutsideTolerance = true;
            }
        }
        else
        {
            super.propertyChange(pEvt);
        }

    }

    /** {@inheritDoc} */
    @Override
    public void onActivityStart()
    {
        if(mCurrentContinuousMove == null)
        {
            handleGoToKey();
            Object oldValue = mSelectedPositionLabel;
            mSelectedPositionLabel = PpdPositionLabel.NONE;
            firePropertyChange(SELECTED_POSITION_LABEL_PROPERTY, oldValue, mSelectedPositionLabel);
            if (mPPSLeftRightArmOption)
            {
                firePropertyChange(SELECTED_ELBOW_POSITION_LABEL_PROPERTY, null, ElbowOrientation.NONE);
            }
        }
        mWorkflowStartedMessage = mCurrentContinuousMove != null;
    }

    /** {@inheritDoc} */
    @Override
    public void onActivityStop()
    {
        if(mCurrentContinuousMove == null)
        {
            handleGoToKey();
            Object oldValue = mSelectedPositionLabel;
            mSelectedPositionLabel = PpdPositionLabel.NONE;
            firePropertyChange(SELECTED_POSITION_LABEL_PROPERTY, oldValue, mSelectedPositionLabel);
            if (mPPSLeftRightArmOption)
            {
                firePropertyChange(SELECTED_ELBOW_POSITION_LABEL_PROPERTY, null, ElbowOrientation.NONE);
            }
        }
        mWorkflowStartedMessage = false;
    }

    private boolean areHomableDevicesHomed()
    {
        if (!mDevices.allHomed())
        {
            showError(PmsUtils.getLocalizedMessageByKey(
                    bundle, "pms.ap.controller.mustBeHomedFirst",
                    mDevices.getUnhomedDevicesNames()));
            return false;
        }
        return true;
    }

    /**
     * This method will handle device state change after a certain delay.
     * It also handles periodic beeps in case of proximity detection.
     */
    @Override
    public void onTimer(Timer pTimer)
    {
        if (pTimer.equals(mTimer))
        {
            /* On device state change */
            deviceStateChanged(mCachedEvent);
        }
        else if (pTimer.equals(mProximityTimer))
        {
            if (mProximityManager.isInProximity())
            {
                if (mDevices.isMoving())
                {
                    shortBeep();
                }
                mProximityTimer.schedule(PROXIMITY_TIMER_PERIOD);
            }
        }
        else if (pTimer.equals(mCollisionTimer))
        {
            shortBeep();
            showInfo(PmsUtils.getLocalizedMessageByKey(bundle, "pms.ap.controller.collisionEnabled"));
            mCollisionManager.enableCollisionDetection(getClass().getSimpleName());

            startDelayBeforeCollisionDisabling();
        }
        else if (pTimer.equals(mDeactivateTimer))
        {
            mDevices.deactivateAll();
        }
        else if (pTimer.equals(mDeactivateLightsTimer))
        {
            LightField light = (LightField) mDevices.getDevice(Label.LFIELD);
            Lasers lasers = (Lasers) mDevices.getDevice(Label.LASERS);
            Lasers ahLasers = (Lasers) mDevices.getDevice(Label.AH_LASERS);
            if (light != null)
            {
                mLightFieldOn = false;
                light.switchOff();
            }
            if (lasers != null)
            {
                lasers.switchOff();
            }
            if (ahLasers != null)
            {
                ahLasers.switchOff();
            }
            setRoomLightDimming(false);
        }
        else if (pTimer.equals(mDelaySnoutBrakeReleased))
        {
            if (mSelectedDevice instanceof RenovatedSnoutHolderProxy)
            {
                RenovatedSnoutHolderProxy device = (RenovatedSnoutHolderProxy) mSelectedDevice;
                if (!device.isBrakeRelease() && mSelectedDevice.getDeviceState() == Device.State.BUSY)
                {
                    mCurrentContinuousMove = null;
                    mDevices.softStopAll();
                    mDevices.deactivateAll();
                    clear();
                    showError(PmsUtils.getLocalizedMessageByKey(bundle,
                            "pms.ap.controller.snoutBrakeReleasedErrorMessage"));
                }
            }
        }
        else if (pTimer.equals(mWeightCheckTimer))
        {
            shortBeep();
            Tarable ppsDevice = (Tarable)mDevices.getDevice(Label.PPS);
            if(ppsDevice == null || ppsDevice.isWeightChecked())
            {
                showInfo(PmsUtils.getLocalizedMessageByKey(bundle, "pms.ap.controller.weightCheckedOk"));
            }
            else
            {
                showInfo(PmsUtils.getLocalizedMessageByKey(bundle, "pms.ap.controller.weightCheckedFailed"));
            }
        }
    }

    /**
     * Handle device state changing. This method will be called with a bit delay
     * after all others properties changes.
     *
     * @param pEvt Device state property change event.
     */
    void deviceStateChanged(PropertyChangeEvent pEvt)
    {
        PmsDevice bmd = (PmsDevice) pEvt.getSource();
        for (AbstractHelper helper : mHelpersContainer)
        {
            if(helper.isRequestPrepared())
            {
                StatusResult sr = helper.isTargetReached(bmd, pEvt);
                showMessage(sr);
                if(sr.isResetRequest())
                {
                    helper.clearRequest();
                    setLedOn(HpLed.MOVE, helper.isRequestPrepared());
                }
                return;
            }
        }
    }

    /**
     * Clear hand pendant screen, and unselects current selected device. This will also clear
     * all motion requests setting them to <code>null</code>.
     */
    private void clear()
    {
        clearSelectedHp();
        clearRequests();
    }

    /** Clear all pending requests. */
    private void clearRequests()
    {
        for (AbstractHelper helper : mHelpersContainer)
        {
            helper.clearRequest();
        }
        mJogHelper.clearRequest();
        mSaveRequest = null;
        mCurrentContinuousMove = null;
        setLedOn(HpLed.MOVE, false);

        if (mTrHandPendant.getEffect() instanceof ShowMenuEffect)
        {
            /*
             * If there is a menu shown on the HP, we have to unselect currently
             * highlighted item and nofify on menu change.
             */
            ShowMenuEffect effect = (ShowMenuEffect) (mTrHandPendant.getEffect());
            effect.unselectMenu();
        }

        if (mTcrHandPendant.getEffect() instanceof ShowMenuEffect)
        {
            /*
             * If there is a menu shown on the HP, we have to unselect currently
             * highlighted item and nofify on menu change.
             */
            ShowMenuEffect effect = (ShowMenuEffect) (mTcrHandPendant.getEffect());
            effect.unselectMenu();
        }

        if (mTr2HandPendant.getEffect() instanceof ShowMenuEffect)
        {
            /*
             * If there is a menu shown on the HP, we have to unselect currently
             * highlighted item and nofify on menu change.
             */
            ShowMenuEffect effect = (ShowMenuEffect) (mTr2HandPendant.getEffect());
            effect.unselectMenu();
        }
    }

    /**
     * Show motion menu on HP and notify listeners about menu change.
     *
     * @param pMenu Menu to be shown.
     */
    private void showMotionMenu(MotionMenu pMenu)
    {
        showMenu(pMenu.getHpMenu());
    }

    private void showMotionMenu(MotionMenu pMenu, int pSelected)
    {
        showMenu(pMenu.getHpMenu(), pSelected);
    }

    private void showMotionMenuAndSelect(MotionMenu pMenu, int pSelected)
    {
        showMenuAndSelect(pMenu.getHpMenu(), pSelected);
    }


    void deselectDevicesActivated(Label pExceptThisDevice)
    {
        List<Label> exceptTheseDevices = new ArrayList<>();
        exceptTheseDevices.add(pExceptThisDevice);
        switch (pExceptThisDevice)
        {
            case IMAGING:
                exceptTheseDevices.add(Label.DIDA);
                exceptTheseDevices.add(Label.DIDB);
                exceptTheseDevices.add(Label.DIDC);
                exceptTheseDevices.add(Label.XRAY);
                break;
            case CBCT_DEVICES_MANAGER:
                exceptTheseDevices.add(Label.CBCT_TUBE);
                exceptTheseDevices.add(Label.CBCT_COLLIMATOR);
                exceptTheseDevices.add(Label.CBCT_PANEL);
                exceptTheseDevices.add(Label.CBCT_CONTROLLER);
                exceptTheseDevices.add(Label.CBCT_NOZZLE_IMAGER_FOV_MANAGER);
                break;
            case IMAGER_MANAGER:
                exceptTheseDevices.add(Label.IMAGER_PANEL);
                exceptTheseDevices.add(Label.IMAGER_COLLIMATOR);
                exceptTheseDevices.add(Label.IMAGER_COLLIMATOR_BLADE);
                break;
        }
        synchronized (mDevicesActivated)
        {
            Iterator<Label> i = mDevicesActivated.iterator();
            while (i.hasNext())
            {
                Label label = i.next();
                if(!exceptTheseDevices.contains(label))
                {
                    PmsDevice device = mDevices.getDevice(label);
                    if (device instanceof Movable)
                    {
                        ((Movable) device).setActive(false);
                    }
                }
            }
        }
    }

    void selectDevice(PmsDevice pDevice)
    {
        if(mDevicesActivated.size() != 0)
        {
            deselectDevicesActivated(pDevice.getLabel());
        }
        try
        {
            Thread.sleep(10);
        }
        catch (InterruptedException e)
        {
            logger.error("Error during thread sleep in select device function", e);
        }
        if (pDevice instanceof RenovatedPpsForteProxy && ((RenovatedPpsForteProxy) pDevice).isHeadNeckChairPresent() &&
                !((RenovatedPpsForteProxy) pDevice).isHeadNeckChairWellPositioned())
        {

            showError(PmsUtils.getLocalizedMessageByKey(bundle,
                    "pms.ap.controller.headNeckChairRotate"));
        }
        else
        if(pDevice instanceof Movable)
        {
            ((Movable) pDevice).setActive(true);
            mSelectedDevice = pDevice;
        }
    }

    private void actualiseLeds()
    {
        synchronized (mDevicesActivated)
        {
            setLedOn(HpLed.PPS, mDevicesActivated.contains(Label.PPS));
            setLedOn(HpLed.JOG_XY, mDevicesActivated.contains(Label.PPS));
            setLedOn(HpLed.JOG_PITCH_ROLL,
                    mDevicesActivated.contains(Label.PPS) || mDevicesActivated.contains(Label.SNOUT));
            setLedOn(HpLed.JOG_Z, mDevicesActivated.contains(Label.PPS));
            setLedOn(HpLed.JOG_CW,
                    mDevicesActivated.contains(Label.PPS) || mDevicesActivated.contains(Label.GANTRY));
            setLedOn(HpLed.GANTRY, mDevicesActivated.contains(Label.GANTRY));
            setLedOn(HpLed.SNOUT, mDevicesActivated.contains(Label.SNOUT));
            setLedOn(HpLed.INC, mJogType == Jog.Type.INCREMENTAL);
        }
    }

    private void handleGoToKey()
    {
        clear();
        GoToMenu menu = new GoToMenu(mDevices, mPositions, mKVkVSequential,mPPSLeftRightArmOption, mCbctPresent);
        menu.addListener(this);
        int correctedID = menu.getCorrectedId();
        if (correctedID != -1)
        {
            showMotionMenu(menu, correctedID);
        }
        else
        {
            showMotionMenu(menu);
        }
    }

    private void handlePaliKeyAndSelectCTImaging(boolean pSendRequest)
    {
        if(pSendRequest)
        {
            clear();
        }
        PaliMenu menu = new PaliMenu(mDevices, mPositions, mCbctPresent, cbct);
        menu.addListener(this);
        int cTImagingID = menu.getCTId();
        if(cTImagingID != -1)
        {
            showMotionMenuAndSelect(menu, cTImagingID);
            if(menu.getHpMenu().getItems().get(cTImagingID).getItems().size() == 0 && pSendRequest)
            {
                onMenuRequest((MenuRequest) menu.getHpMenu().getItems().get(cTImagingID).getReferenceObject());
            }
        }
        else
        {
            showMotionMenu(menu);
        }
    }

    private void handlePaliKeyAndSelectNozzleImagerRetract()
    {
        PaliMenu menu = new PaliMenu(mDevices, mPositions, mCbctPresent, cbct);
        menu.addListener(this);
        int nozzleImagerID = menu.getNozzleImagerId();
        if(nozzleImagerID != -1)
        {
            HpMenu nozzleMenu = menu.getHpMenu().getItems().get(nozzleImagerID);
            HpMenu retractMenu = nozzleMenu.getItems().get(0);
            if(retractMenu.getName().equals(PmsUtils.getLocalizedMessageByKey(bundle, "pms.ap.menu.pali.cbctRetract")))
            {
                showMenuAndSelect(nozzleMenu, 0);
                onMenuRequest((MenuRequest) retractMenu.getReferenceObject());
            }
        }
    }

    private void handlePaliKeyAndSelectCbct(boolean pSendRequest)
    {
        if(pSendRequest)
        {
            clear();
        }
        PaliMenu menu = new PaliMenu(mDevices, mPositions, mCbctPresent, cbct);
        menu.computeLaserPosition(false);
        menu.addListener(this);
        int cbctId = menu.getCbctId();
        if(cbctId != -1)
        {
            showMotionMenuAndSelect(menu, cbctId);
            if(menu.getHpMenu().getItems().get(cbctId).getItems().size() == 0 && pSendRequest)
            {
                onMenuRequest((MenuRequest) menu.getHpMenu().getItems().get(cbctId).getReferenceObject());
            }
        }
        else
        {
            showMotionMenu(menu);
        }
        menu.computeLaserPosition(true);
    }

    /** Clear hand pendant and display Home menu. */
    private void handleHomeKey()
    {
        clear();
        HomeMenu menu = new HomeMenu(mDevices, mPositions);
        menu.addListener(this);
        showMotionMenu(menu);
    }

    /**
     * Clear hand pendant and display docking menu. For PPS Schaer without
     * Oncolog stuff, there is no docking.
     */
    private void handleTeachKey()
    {
        clear();
        DockingMenu menu = new DockingMenu(mDevices, mPositions);
        menu.addListener(this);
        showMotionMenu(menu);
    }

    /** Clear hand pendant and display PALI menu. */
    private void handlePaliKey()
    {
        clear();
        PaliMenu menu = new PaliMenu(mDevices, mPositions, mCbctPresent, cbct);
        menu.addListener(this);
        showMotionMenu(menu);
    }

    /** Toggle coordinate system (jog mode). */
    private void handleCsKey()
    {
        toggleJogMode();
    }

    /** Clear hand pendant and display 'save' menu. */
    private void handleSaveKey()
    {
        clear();
        SaveMenu menu = new SaveMenu(mDevices, mPositions);
        menu.addListener(this);
        showMotionMenu(menu);
    }

    /** Handle 'SN' key. Here we select the snout for the following jog operation. */
    private void handleSnoutKey()
    {
        if (!(mDevices.getDevice(PmsDevice.Label.SNOUT) instanceof RenovatedFixedSnoutHolderProxy)
                && mCurrentContinuousMove == null)
        {
            clear();
            SnoutJogMenu menu = new SnoutJogMenu(mDevices,
                    mPositions);
            menu.addListener(this);
            showMotionMenu(menu);
            selectDevice(mDevices.getDevice(PmsDevice.Label.SNOUT));
        }

    }

    /** Handle 'PPS' key. Here we select the PPS for the following jog operation. */
    private void handlePpsKey()
    {

        if (mDevices.getDevice(PmsDevice.Label.PPS) != null && mCurrentContinuousMove == null)
        {
            clear();
            PpsJogMenu menu = new PpsJogMenu(mDevices, mPositions, mPPSLeftRightArmOption);
            menu.addListener(this);
            showMotionMenu(menu);
            selectDevice(mDevices.getDevice(PmsDevice.Label.PPS));
        }
    }

    /** Handle 'G' key. Here we select the gantry for the following jog operation. */
    private void handleGantryKey()
    {

        if (mDevices.getDevice(PmsDevice.Label.GANTRY) != null && mCurrentContinuousMove == null)
        {
            clear();
            GantryJogMenu menu = new GantryJogMenu(mDevices, mPositions);
            menu.addListener(this);
            showMotionMenu(menu);
            selectDevice(mDevices.getDevice(PmsDevice.Label.GANTRY));
        }
    }

    /**
     * Handle 'INC' key. This key toggles incremental/continuous jog. We check
     * however that selected device is actually jogable.
     */
    private void handleIncKey()
    {
        if (mJogType == Jog.Type.CONTINUOUS)
        {
            setJogType(Jog.Type.INCREMENTAL);
        }
        else
        {
            setJogType(Jog.Type.CONTINUOUS);
        }
    }

    private void setJogType(Jog.Type pJogType)
    {
        mJogType = pJogType;
        if (pJogType == Jog.Type.CONTINUOUS)
        {
            setLedOn(HpLed.INC, false);
            setDisplayZoneText(JOG_MODE_DISPLAY_ZONE,
                    PmsUtils.getLocalizedMessageByKey(bundle, "pms.ap.controller.label.continuous"));
        }
        else
        {
            setLedOn(HpLed.INC, true);
            setDisplayZoneText(JOG_MODE_DISPLAY_ZONE,
                    PmsUtils.getLocalizedMessageByKey(bundle, "pms.ap.controller.label.incremental"));
        }
    }

    /** Handle 'H/L' key. */
    private void handleSpeedKey()
    {
        if(getSpeedLevel() == Speed.Level.LOW)
        {
            setSpeedLevel(Speed.Level.PROXIMITY);
        }
        else
        if(getSpeedLevel() == Speed.Level.PROXIMITY)
        {
            if(mProximityManager.isInProximity())
            {
                setSpeedLevel(Speed.Level.LOW);
            }
            else
            {
                setSpeedLevel(Speed.Level.HIGH);
            }
        }
        else
        {
            setSpeedLevel(Speed.Level.LOW);
        }
    }

    private void handleBKey()
    {
        clear();
        UtilMenu menu = new UtilMenu(mDevices, mPositions);
        menu.addListener(this);
        showMotionMenu(menu);
    }

    private boolean isPerformingDockingMovement()
    {
        return mIsPerformingDockingMovement;
    }

    /**
     * setPerformingDockingMovement.
     * @param pPerformingDocking
     */
    public void setPerformingDockingMovement(boolean pPerformingDocking)
    {
        if (pPerformingDocking != mIsPerformingDockingMovement)
        {
            if (pPerformingDocking)
            {
                LOGGER.info("Start performing docking move");
            }
            else
            {
                LOGGER.info("Stop performing docking move");
            }
        }

        mIsPerformingDockingMovement = pPerformingDocking;
    }

    private Speed.Level getSpeedLevel()
    {
        return mSpeedLevel;
    }

    /**
     * Set current speed level (for all devices).
     *
     * @param pLevel the speed level to be set.
     */
    private void setSpeedLevel(Speed.Level pLevel)
    {
        if (pLevel == Speed.Level.LOW || mHomeHelper.isRequestPrepared())
        {
            mSpeedLevel = Speed.Level.LOW;
            setDisplayZoneText(SPEED_DISPLAY_ZONE,
                    PmsUtils.getLocalizedMessageByKey(bundle, "pms.ap.controller.label.low"));
        }
        else
        if (pLevel == Speed.Level.PROXIMITY || mProximityManager.isInProximity() || isPerformingDockingMovement())
        {
            mSpeedLevel = Speed.Level.PROXIMITY;
            setDisplayZoneText(SPEED_DISPLAY_ZONE,
                    PmsUtils.getLocalizedMessageByKey(bundle, "pms.ap.controller.label.proxi"));
        }
        else
        {
            mSpeedLevel = Speed.Level.HIGH;
            setDisplayZoneText(SPEED_DISPLAY_ZONE,
                    PmsUtils.getLocalizedMessageByKey(bundle, "pms.ap.controller.label.high"));
        }

        /* Set speed level for all devices */
        mDevices.setSpeedLevel(mSpeedLevel);
    }

    /** Handle 'LF' key. */

    private boolean mLightFieldOn = false;

    private void handleLightFieldKey()
    {
        /* Toggle light field */
        LightField light = (LightField) mDevices.getDevice(PmsDevice.Label.LFIELD);
        if (light != null)
        {
            if (mLightFieldOn)
            {
                light.switchOff();
                setRoomLightDimming(false);
            }
            else
            {
                light.switchOn();
                setRoomLightDimming(true);
                startLightsTimer();
            }
            mLightFieldOn = !mLightFieldOn;
        }
    }

    /** Handle 'TRL' key. */
    private void handleTreatmentRoomLightKey()
    {
        if (mGantryType.equals("compact"))
        {
            clear();
            TrlMenu menu = new TrlMenu(mDevices, mPositions);
            menu.addListener(this);
            showMotionMenu(menu);
        }
        else
        {
            /* Toggle treatment room light */
            TreatmentRoomLight light = (TreatmentRoomLight) mDevices.getDevice(PmsDevice.Label.TRLIGHTS);
            if (light != null)
            {
                light.toggle();
            }
        }
    }

    /**
     * Implementation of PPS Forte error code listener.
     *
     * @see PpsForteErrorCodeListener
     */
    public void onPpsForteErrorCode(PpsForteErrorCode pErrorCode)
    {
        String message = null;

        switch (pErrorCode)
        {
            case PPS_ERROR_LOAD_180:
                message = PmsUtils.getLocalizedMessageByKey(bundle, "pms.ap.controller.ppsLoad180");
                break;
            case PPS_ERROR_LOAD_250:
                message = PmsUtils.getLocalizedMessageByKey(bundle, "pms.ap.controller.ppsLoad250");
                break;
            case PPS_ERROR_INVALID_COUPLER_STATE:
                message = PmsUtils.getLocalizedMessageByKey(bundle, "pms.ap.controller.ppsInvalidCouplerState");
                break;
            case PPS_ERROR_INVALID_LOCK_STATE:
                message = PmsUtils.getLocalizedMessageByKey(bundle, "pms.ap.controller.ppsInvalidLockState");
                break;
            case PPS_ERROR_MOTION_LIMITS:
                message = PmsUtils.getLocalizedMessageByKey(bundle, "pms.ap.controller.ppsOutOfMotionLimits");
                break;
            case PPS_ERROR_REFERENCE_EXPIRED:
                message = PmsUtils.getLocalizedMessageByKey(bundle, "pms.ap.controller.ppsReferenceTestExpired");
                break;
            case PPS_ERROR_POGOPINS_NOT_MATCH:
                message = PmsUtils.getLocalizedMessageByKey(bundle, "pms.ap.controller.ppsPogoPinsNotMatch");
                break;
            case PPS_ERROR_JOINTS_EXCEED:
                message = PmsUtils.getLocalizedMessageByKey(bundle, "pms.ap.controller.ppsJointsLimitsExceed");
                break;
            // We do not handle the following messages
            case PPS_ERROR_COLLISION_DETECTED:
            case PPS_ERROR_UNHEALTHY:
            default:
                break;
        }
        if (message != null)
        {
            showWarning(message);
        }
    }

    /**
     * Handle 'M' key: command GoTo or Home motion. Some other activities are
     * commanded by 'M' button as well, such as docking, for example.
     */
    private void handleMoveKey()
    {
        if (mSelectedDevice == null)
        {
            return;
        }

        if (!isCollisionManagementPhaseRunning())
        {
            OncologDockable oncologDockable = (mSelectedDevice instanceof OncologDockable)
                    ? (OncologDockable) mSelectedDevice : null;

            /* Do not enable collision detection for oncolog docking moves
             * between undock & freeTrolley positions
             */
            if (!(mDockingHelper.isRequestPrepared() &&
                    oncologDockable != null &&
                    ((mDockingHelper.getAction() == DockingRequest.Action.UN_DOCK &&
                            oncologDockable.isAtTrolleyFreePosition()) ||
                            (mDockingHelper.getAction() == DockingRequest.Action.FREE_TRLY &&
                                    oncologDockable.isAtDockUndockPosition()))))
            {
                mCollisionManager.enableCollisionDetection(getClass().getSimpleName());
            }
        }
        if (!(mSelectedDevice instanceof GoToable))
        {
            clear();
            showError(PmsUtils.getLocalizedMessageByKey(bundle, "pms.ap.controller.deviceNotMovable"));
            return;

        }
        for (AbstractHelper helper : mHelpersContainer)
        {
            if((!(helper instanceof HomeHelperImpl) && helper.isRequestPrepared())
                    || (helper instanceof HomeHelperImpl && ((HomeHelperImpl) helper).isMovableReference()))
            {
                RequestResult rr = helper.createContinuousMove();
                if(!rr.isContinuousMoveSet())
                {
                    showMessage(rr.getStatusResult());
                    if(rr.isResetRequest())
                    {
                        helper.clearRequest();
                        setLedOn(HpLed.MOVE, false);
                    }
                    return;
                }
                setSpeedLevel(getSpeedLevel());
                setActivationKey(HpKey.MOVE);
                mCurrentContinuousMove = rr.getContinuousMove();
                return;
            }
        }
        if (mJogHelper.isRequestPrepared()
                && mAccessPointMotion.getSelected() == AccessPointMotion.TCR_RPD_ACCESS_POINT
                && isRpdPresent())
        {

            Jogable jogable = (Jogable) mSelectedDevice;
            StatusResult deviceInterlockStatus = mDevices.checkOtherDevicesInterlocks(mSelectedDevice);
            if (deviceInterlockStatus.isStatus() && jogable.canJog().isStatus())
            {
                mCurrentContinuousMove = mJogHelper.getJogRequest();
            }
            else
            {
                showError(deviceInterlockStatus.getMessage() + " " + jogable.canJog().getMessage());
            }

        }
    }

    /**
     * Construct a jog request according to the HP key and send it to the
     * selected device.
     *
     * @param pKey HP key.
     */
    private void handleJogKey(HpKey pKey)
    {
        Jog.Axis axe;
        Jog.Direction direction;
        switch (pKey)
        {
            case JOG_MX:
                axe = Jog.Axis.X;
                direction = Jog.Direction.NEGATIVE;
                break;
            case JOG_MY:
                axe = Jog.Axis.Y;
                direction = Jog.Direction.NEGATIVE;
                break;
            case JOG_MZ:
                axe = Jog.Axis.Z;
                direction = Jog.Direction.NEGATIVE;
                break;
            case JOG_PX:
                axe = Jog.Axis.X;
                direction = Jog.Direction.POSITIVE;
                break;
            case JOG_PY:
                axe = Jog.Axis.Y;
                direction = Jog.Direction.POSITIVE;
                break;
            case JOG_PZ:
                axe = Jog.Axis.Z;
                direction = Jog.Direction.POSITIVE;
                break;
            case JOG_CCW:
                axe = Jog.Axis.ROT;
                direction = Jog.Direction.NEGATIVE;
                break;
            case JOG_CW:
                axe = Jog.Axis.ROT;
                direction = Jog.Direction.POSITIVE;
                break;
            case JOG_45:
                axe = Jog.Axis.XY_NE;
                direction = Jog.Direction.POSITIVE;
                break;
            case JOG_315:
                axe = Jog.Axis.XY_NW;
                direction = Jog.Direction.POSITIVE;
                break;
            case JOG_135:
                axe = Jog.Axis.XY_NW;
                direction = Jog.Direction.NEGATIVE;
                break;
            case JOG_225:
                axe = Jog.Axis.XY_NE;
                direction = Jog.Direction.NEGATIVE;
                break;
            case JOG_MROLL:
                axe = Jog.Axis.ROLL;
                direction = Jog.Direction.NEGATIVE;
                break;
            case JOG_PROLL:
                axe = Jog.Axis.ROLL;
                direction = Jog.Direction.POSITIVE;
                break;
            case JOG_PPITCH:
                axe = Jog.Axis.PITCH;
                direction = Jog.Direction.POSITIVE;
                break;
            case JOG_MPITCH:
                axe = Jog.Axis.PITCH;
                direction = Jog.Direction.NEGATIVE;
                break;
            default:
                clear();
                showError(PmsUtils.getLocalizedMessageByKey(bundle, "pms.ap.controller.notJogKey"));
                return;
        }
        RequestResult rr = mJogHelper.prepareJogRequest(mSelectedDevice, axe, direction, mJogType, mJogMode);
        if(rr.isContinuousMoveSet())
        {
            if(!isCollisionManagementPhaseRunning())
            {
                mCollisionManager.enableCollisionDetection(getClass().getSimpleName());
            }
            mGotoHelper.setGotoByRequest(null);
            mHomeHelper.clearRequest();
            setSpeedLevel(getSpeedLevel());
            setActivationKey(pKey);
            if (mAccessPointMotion.getSelected() == AccessPointMotion.TCR_RPD_ACCESS_POINT && isRpdPresent())
            {
                if (mSelectedDevice instanceof Snout && axe == Jog.Axis.PITCH)
                {
                    showInfo(PmsUtils.getLocalizedMessageByKey(
                            bundle, "pms.ap.controller.snoutJog",
                            PmsUtils.getLocalizedDeviceLabelByKey(mSelectedDevice.getLabel()),
                            PmsUtils.getLocalizedDirectionByKey(direction)));
                }
                else
                {

                    showInfo(PmsUtils.getLocalizedMessageByKey(
                            bundle, "pms.ap.controller.performingJog",
                            PmsUtils.getLocalizedDeviceLabelByKey(mSelectedDevice.getLabel()),
                            PmsUtils.getLocalizedAxisByKey(axe),
                            PmsUtils.getLocalizedDirectionByKey(direction)));
                }
            }
            mCurrentContinuousMove = rr.getContinuousMove();
        }
        else
        {
            showMessage(rr.getStatusResult());
        }
    }

    /**
     * This method will be called by PcuAdapterActivityPrepare once it has
     * confirmation from the treatment managed on position save.
     *
     * @param pValue whether the saved position has been accepted.
     */
    public void savePositionAcknowledged(boolean pValue)
    {
        if (mSaveRequest == null)
        {
            /* If the save request has been cleared already, we'll
             * show a simple message that does not specify position
             * label.
             */
            if (pValue)
            {
                DigitalImagingDevice didB = (DigitalImagingDevice)mDevices.getDevice(Label.DIDB);
                if(didB != null && didB.getRetractableDeviceState() != RetractableDevice.State.RETRACTED)
                {
                    showWarning(PmsUtils.getLocalizedMessageByKey(
                            bundle, "pms.ap.controller.positionSavedXRadBInserted"));
                    shortBeep();
                    return;
                }
                showInfo(PmsUtils.getLocalizedMessageByKey(
                        bundle, "pms.ap.controller.positionSavedX"));
                shortBeep();
            }
            else
            {
                showError(PmsUtils.getLocalizedMessageByKey(
                        bundle, "pms.ap.controller.positionSaveRefusedX"));
                longBeep(mAccessPointMotion.getSelected());
            }
        }
        else // Save request position label is available.
        {
            /* Save position confirmation */
            PpdPositionLabel label = mSaveRequest.getPpdPositionLabel();

            if (pValue)
            {
                if (label == PpdPositionLabel.TREATMENT || label == PpdPositionLabel.TREATMENT_ROT_ONLY)
                {
                    if (mDevices.getDevice(PmsDevice.Label.DIDB) == null ||
                            ((DigitalImagingDevice) mDevices.getDevice(PmsDevice.Label.DIDB)).getRetractableDeviceState() != RetractableDevice.State.INSERTED) // true if retracted
                    {
                        /* Take menu labels instead of positions */
                        showInfo(PmsUtils.getLocalizedMessageByKey(
                                bundle, "pms.ap.controller.positionSaved", PmsUtils.getLocalizedMessageByKey(bundle, "pms.ap.menu.goto.treatment")));
                    }
                    else
                    {
                        showWarning(PmsUtils.getLocalizedMessageByKey(
                                bundle, "pms.ap.controller.positionSavedRadBInserted", PmsUtils.getLocalizedMessageByKey(bundle, "pms.ap.menu.goto.treatment")));

                    }
                }
                else
                {
                    showInfo(PmsUtils.getLocalizedMessageByKey(
                            bundle, "pms.ap.controller.positionSaved", PmsUtils.getLocalizedPositionLabelByKey(label)));
                }
                shortBeep();
            }
            else
            {
                if (label == PpdPositionLabel.TREATMENT)
                {
                    /* Take menu labels instead of positions */
                    showInfo(PmsUtils.getLocalizedMessageByKey(
                            bundle, "pms.ap.controller.positionSaveRefused", PmsUtils.getLocalizedMessageByKey(bundle, "pms.ap.menu.goto.treatment")));
                }
                else
                {
                    showError(PmsUtils.getLocalizedMessageByKey(
                            bundle, "pms.ap.controller.positionSaveRefused", PmsUtils.getLocalizedPositionLabelByKey(label)));
                }
                longBeep(mAccessPointMotion.getSelected());

                PpdPosition backupPosition = mPositions.getPpdPositionByLabel(PpdPositionLabel.ACK_SAVE_BACKUP);
                mPositions.setPpdPosition(label, backupPosition);
            }

            /* Have to clear the save request */
            mSaveRequest = null;
        }
    }

    int keyboardToAccessPoint(HpKeyboard pKeyBoard)
    {
        if(pKeyBoard.equals(mTrHandPendant.getKeyboard()))
        {
            return AccessPointMotion.TR_ACCESS_POINT;
        }
        else
        if(pKeyBoard.equals(mTcrHandPendant.getKeyboard()))
        {
            return AccessPointMotion.TCR_RPD_ACCESS_POINT;
        }
        else
        if(pKeyBoard.equals(mTr2HandPendant.getKeyboard()))
        {
            return AccessPointMotion.TR_HP2_ACCESS_POINT;
        }
        else
        {
            return AccessPointMotion.NO_ACCESS_POINT;
        }
    }

    /**
     * This method is called whenever user pressed any button on the hand pendant
     * keyboard. Here we handle only menu and motion buttons. Menu item selection
     * is handled by hand pendant effect.
     *
     * @param pKeyBoard the key-board.
     * @param pKey      the pressed key.
     */
    public void keyPressed(HpKeyboard pKeyBoard, HpKey pKey)
    {
        PmsDevice snout = mDevices.getDevice(Label.SNOUT);
        if(mNozzleImagerRecoverNextStep != NozzleImagerScrutineerRecoverySteps.NONE && snout instanceof DNSnoutHolderProxy)
        {
            if(mNozzleImagerRecoverNextStep == NozzleImagerScrutineerRecoverySteps.CANCEL)
            {
                mNozzleImagerRecoverNextStep = NozzleImagerScrutineerRecoverySteps.NONE;
            }
            else
            if(mNozzleImagerRecoverNextStep == NozzleImagerScrutineerRecoverySteps.FINISH)
            {
                mDevices.deactivateAll();
                clear();
                mNozzleImagerRecoverNextStep = NozzleImagerScrutineerRecoverySteps.NONE;
            }
            else {
                if (pKey.equals(HpKey.OK)) {
                    switch (mNozzleImagerRecoverNextStep) {
                        case STARTED:
                        case WARNING_MESSAGE:
                            mDevices.activateAll();
                            ((DNSnoutHolderProxy) snout).enableScannersBypass();
                            mNozzleImagerRecoverNextStep = NozzleImagerScrutineerRecoverySteps.RESET_ALL_CU;
                            showStep(1, 4, PmsUtils.getLocalizedMessageByKey(bundle,
                                    "pms.ap.controller.nozzleImagerSafetyError.recover.resetAllSRCU"));
                            break;
                        case RESET_ALL_CU:
                            mDevices.deactivateAll();
                            mNozzleImagerRecoverNextStep = NozzleImagerScrutineerRecoverySteps.RECOVER_NOZZLE;
                            showStep(2, 4,PmsUtils.getLocalizedMessageByKey(bundle,
                                    "pms.ap.controller.nozzleImagerSafetyError.recover.recoverNozzle"));
                            break;
                        case RECOVER_NOZZLE:
                            clear();
                            handlePaliKeyAndSelectNozzleImagerRetract();
                            mNozzleImagerRecoverNextStep = NozzleImagerScrutineerRecoverySteps.RESET_SRCU;
                            break;
                        case RESET_SRCU:
                            mDevices.activateAll();
                            showStep(4, 4, PmsUtils.getLocalizedMessageByKey(bundle,
                                    "pms.ap.controller.nozzleImagerSafetyError.recover.resetSRCU"));
                            mNozzleImagerRecoverNextStep = NozzleImagerScrutineerRecoverySteps.FINISH;
                            break;
                        default:
                            mNozzleImagerRecoverNextStep = NozzleImagerScrutineerRecoverySteps.NONE;
                            break;
                    }
                    return;
                }
                else
                if(!(pKey.equals(HpKey.MOVE) && mNozzleImagerRecoverNextStep == NozzleImagerScrutineerRecoverySteps.RESET_SRCU))
                {
                    mDevices.deactivateAll();
                    mNozzleImagerRecoverNextStep = NozzleImagerScrutineerRecoverySteps.CANCEL;
                    showInfo(PmsUtils.getLocalizedMessageByKey(bundle,
                            "pms.ap.controller.nozzleImagerSafetyError.recover.cancel"));
                    return;
                }
            }
        }
        /* Check whether the HP is not locked */
        if (!isEnabled() || mAccessPointMotion.getSelected() != AccessPointMotion.NO_ACCESS_POINT)
        {
            /* If event comes from unselected or disabled Hp */
            if (	(pKeyBoard.equals(mTrHandPendant.getKeyboard())
                    && (mAccessPointMotion.getSelected() != AccessPointMotion.TR_ACCESS_POINT
                    || !isTrEnabled())
            )
                    || (pKeyBoard.equals(mTcrHandPendant.getKeyboard())
                    && (mAccessPointMotion.getSelected() != AccessPointMotion.TCR_RPD_ACCESS_POINT
                    && mAccessPointMotion.getSelected() != AccessPointMotion.TCR_SAFETY_RACK_ACCESS_POINT
                    || !isTcrEnabled()))
                    || (pKeyBoard.equals(mTr2HandPendant.getKeyboard())
                    && (mAccessPointMotion.getSelected() != AccessPointMotion.TR_HP2_ACCESS_POINT
                    || !isTr2Enabled())
            )
                    )
            {
                /* beep unselected HP, supposing we only have 2 HP*/
                longBeep(keyboardToAccessPoint(pKeyBoard));
                return;
            }
        }
        if(mShowCTNotAwayTableMessage)
        {
            if(pKey.equals(HpKey.OK))
            {
                handlePaliKeyAndSelectCTImaging(false);
            }
            mShowCTNotAwayTableMessage = false;
        }
        boolean targetNotReachedDisplayed = false;
        if (mShowTargetNotReachedMessage)
        {
            if (!pKey.equals(HpKey.MOVE) && mTargetNotReachedDevice != null && !mTargetNotReachedDevice.isTargetReached())
            {
                showWarning(PmsUtils.getLocalizedMessageByKey(
                        bundle, "pms.ap.controller.targetNotReached",
                        PmsUtils.getLocalizedDeviceLabelByKey(mTargetNotReachedDevice.getLabel())));

                setGoToRequest(null);
                mCTHelper.clearRequest();
                mDockingHelper.clearRequest();
                mElbowFlipHelper.clearRequest();

                if(!mCorrectionVectorReceived)
                {
                    targetNotReachedDisplayed = true;
                }
            }

            mShowTargetNotReachedMessage = false;
            mTargetNotReachedDevice = null;
        }

        if (mAskForDisableCollisionConfirmation)
        {
            mCollisionManager.disableCollisionDetection(getClass().getSimpleName());

            if (pKey.equals(HpKey.OK))
            {
                startCollisionManagementPhase(COLLISION_TIMER_PERIOD);
            }
            else
            {
                startCollisionManagementPhase(FAST_COLLISION_TIMER_PERIOD);
            }

            mAskForDisableCollisionConfirmation = false;
        }

        if (!targetNotReachedDisplayed)
        {
            /*
             * User pressed OK key. Normally we clear the screen if we had any text
             * shown before. If we had a menu, we do nothing. If we have a
             * confirmation message (e.g. SAVE position), we do an appropriate action.
             */
            if (pKey.equals(HpKey.OK))
            {
                HpEffect effect = getCurrentEffect();
                if (effect instanceof ShowMessageEffect)
                {
                    ShowMessageEffect sme = (ShowMessageEffect) effect;
                    HpEffect previousEffect = getPreviousEffect();

                    if (sme.getType() == ShowMessageEffect.CONFIRM_MESSAGE)
                    {
                        /* User has just confirmed a message */
                        if (mSaveRequest != null)
                        {
                            //Before Saving, we check if all devices are homed
                            if (!areHomableDevicesHomed())
                            {
                                return;
                            }

                            /* Save position */
                            PpdPositionLabel label = mSaveRequest.getPpdPositionLabel();
                            PpdPosition positionToBackup = mPositions.getPpdPositionByLabel(label);

                            mPositions.setPpdPosition(PpdPositionLabel.ACK_SAVE_BACKUP,
                                    positionToBackup);

                            if (label == PpdPositionLabel.MEMORY)
                            {
                                mPositions.setPpdPosition(label,
                                        mDevices.getCurrentPpdPositionAsUserPosition());
                                getLogger().info("Save " + label + " position " + mDevices.getCurrentPpdPositionAsUserPosition());
                            }
                            else
                            {
                                mPositions.setPpdPosition(label, positionToBackup.copy());
                                getLogger().info("Confirm " + label + " position.");
                            }

                            /*
                             * We do not clear the save request here. This will be done in
                             * savePositionAcknowledged method called by PcuAdapter.
                             * Except for the MEMORY position, that does not require to be
                             * acknowledged.
                             */
                            if (label == PpdPositionLabel.MEMORY)
                            {
                                savePositionAcknowledged(true);
                            }
                        }
                        else if (mHomeHelper.isRequestPrepared())
                        {
                            if(mHomeHelper.getReference() == BIAS)
                            {
                                showMessage(mHomeHelper.bias());
                                mHomeHelper.clearRequest();
                            }
                            else
                            if (mHomeHelper.getReference() == TARE)
                            {
                                showMessage(mHomeHelper.tare());
                                mHomeHelper.clearRequest();
                            }
                            else
                            if (mHomeHelper.getReference() == CHECK_WEIGHT)
                            {
                                /*
                                 * Weight check is a special case, since CHECK_WEIGHT item is in home menu, but in
                                 * general it has nothing to do with homing.
                                 */
                                Tarable tarable = (Tarable) mSelectedDevice;
                                tarable.checkWeight();
                                if (mWeightCheckTimer == null)
                                {
                                    mWeightCheckTimer = mTimerFactory.create(this);
                                }
                                mWeightCheckTimer.schedule(500);
                                mHomeHelper.clearRequest();
                            }
                        }
                        else
                        {
                            /* Clear HP, since it was not a save request confirmation. */
                            clear();
                        }
                    }
                    else
                    {
                        /* Simply clear all other message effects if no device is selected*/
                        if (mSelectedDevice == null)
                        {
                            clear();
                        }
                        /* Return to the previous effect if it was menu */
                        if (previousEffect != null
                                && previousEffect instanceof ShowMenuEffect)
                        {
                            if(((ShowMenuEffect) previousEffect).getCurrentMenu() != null
                                    && ((ShowMenuEffect) previousEffect).getCurrentMenu().getName().equals(
                                    PmsUtils.getLocalizedMessageByKey(bundle, "pms.ap.menu.pali.ct")+">"))
                            {
                                handlePaliKeyAndSelectCTImaging(true);
                            }
                            else
                            if(((ShowMenuEffect) previousEffect).getCurrentMenu() != null
                                    && ((ShowMenuEffect) previousEffect).getCurrentMenu().getName().equals(
                                    PmsUtils.getLocalizedMessageByKey(bundle, "pms.ap.menu.goto.cbct")+">"))
                            {
                                handlePaliKeyAndSelectCbct(true);
                            }
                            else
                            if(((ShowMenuEffect) previousEffect).getRootMenu().getName().equals(DockingMenu.DOCKING_NAME+">"))
                            {
                                handleTeachKey();
                            }
                            else
                            {
                                setEffect(previousEffect);
                            }
                        }
                        else
                        {
                            clearSelectedHp();
                        }
                    }
                }
                else if (effect instanceof ShowTextEffect)
                {
                    /* Clear any text messages */
                    clear();
                }

                if(mWorkflowMessageConfirmation)
                {
                    clear();
                    handleGoToKey();
                    mWorkflowMessageConfirmation = false;
                }

                if (mCorrectionVectorReceived)
                {
                    if (mCorrectionVectorOutsideTolerance)
                    {
                        mPpvsModel.setCorrectionVectorConfirmed(false);
                    }
                    else
                    {
                        mPpvsModel.setCorrectionVectorConfirmed(true);
                        handleGoToKey();
                        onMenuRequest(MenuRequest.createGoToMenuRequest(Label.PPS, PpdPositionLabel.CORRECTED));
                    }

                    mCorrectionVectorReceived = false;
                }
            }
            /*
             * User pressed CANCEL key. Clear everything.
             */
            else if (pKey.equals(HpKey.CANCEL))
            {
                if (mSelectedDevice != null)
                {
                    deselectDevicesActivated(Label.NULL);
                }
                clear();
                mWorkflowMessageConfirmation = false;
            }
            else if (pKey.equals(HpKey.RESET))
            {
                if (mSelectedDevice != null)
                {
                    deselectDevicesActivated(Label.NULL);
                }
            }
            /*
             * User pressed INC button. Toggle incremental/continuous jog mode.
             */
            else if (pKey.equals(HpKey.INC_CONT))
            {
                handleIncKey();
            }
            /*
             * User pressed 'H/L' button. Toggle speed level.
             */
            else if (pKey.equals(HpKey.SPEED))
            {
                handleSpeedKey();
            }
            /*
             * User pressed 'LF' button. Toggle light field.
             */
            else if (pKey.equals(HpKey.LIGHT_FIELD))
            {
                if(!mGantryType.equals("compact"))
                {
                    handleLightFieldKey();
                }
            }
            /*
             * User pressed 'TRL' button. Toggle treatment room light.
             */
            else if (pKey.equals(HpKey.TR_LIGHT))
            {
                handleTreatmentRoomLightKey();
            }
            /*
             * User pressed GOTO key. Create GoTo menu and show it.
             */
            else if (pKey.equals(HpKey.GOTO))
            {
                handleGoToKey();
            }
            /*
             * User pressed HOME button. Show home menu.
             */
            else if (pKey.equals(HpKey.HOME))
            {
                handleHomeKey();
            }
            /*
             * User pressed TEACH button. Show docking menu.
             */
            else if (pKey.equals(HpKey.TEACH))
            {
                handleTeachKey();
            }
            /*
             * User pressed PALI button. Show pali menu.
             */
            else if (pKey.equals(HpKey.PALI))
            {
                handlePaliKey();
            }
            /*
             * User pressed CS button. Show CS (jog modes) menu.
             */
            else if (pKey.equals(HpKey.CS))
            {
                handleCsKey();
            }
            /*
             * User pressed S button. Show save menu.
             */
            else if (pKey.equals(HpKey.SAVE))
            {
                handleSaveKey();
            }
            /* User pressed SN button to jog the snout */
            else if (pKey.equals(HpKey.SNOUT))
            {
                handleSnoutKey();
            }
            /* User pressed PPS button to jog the PPS */
            else if (pKey.equals(HpKey.PPS))
            {
                handlePpsKey();
            }
            /* User pressed G button to jog the gantry */
            else if (pKey.equals(HpKey.GANTRY))
            {
                handleGantryKey();
            }
            /*
             * User pressed MOVE button. Check whether target position and device are
             * defined and command the device motion.
             */
            else if (pKey.equals(HpKey.MOVE))
            {
                handleMoveKey();
            }
            else if (pKey.equals(HpKey.B))
            {
                handleBKey();
            }
            /*
             * User pressed any of the jog buttons. Jog a selected device (or do
             * nothing if no device selected).
             */
            else if (pKey.isJogRelated())
            {
                handleJogKey(pKey);
            }
        }
        if (mCorrectionVectorReceived)
        {
            mPpvsModel.setCorrectionVectorConfirmed(false);
            mCorrectionVectorReceived = false;
        }
    }

    /**
     * This method is called when user releases a button. For certain keys we
     * have to perform soft stop of currently moving device. NOTE: When
     * performing a softStop (or any other controller stop), there is not need to
     * disable collision detection, since it is disabled automatically by the
     * PCU.
     */
    @Override
    public void keyReleased(HpKeyboard pKeyBoard, HpKey pKey)
    {
        if (isDelaySnoutBrakeReleasedRunning())
        {
            mDelaySnoutBrakeReleased.cancel();
        }
        if (pKey.equals(HpKey.MOVE))
        {
            if (mSelectedDevice != null && mSelectedDevice instanceof Movable)
            {
                ((Movable) mSelectedDevice).softStop();
            }
            /* Stop all devices just to be sure */
            mDevices.softStopAll();
        }
        else if (mSelectedDevice instanceof Snout)
        {
            if (pKey.equals(HpKey.JOG_PPITCH) || pKey.equals(HpKey.JOG_MPITCH) && mSelectedDevice instanceof Movable)
            {
                ((Movable) mSelectedDevice).softStop();
            }
        }
        else if (mSelectedDevice instanceof Pps)
        {
            if (pKey.isJogRelated() && mSelectedDevice instanceof Movable)
            {
                ((Movable) mSelectedDevice).softStop();
            }
        }
        else if (mSelectedDevice instanceof Gantry)
        {
            if (pKey.equals(HpKey.JOG_CCW) || pKey.equals(HpKey.JOG_CW) && mSelectedDevice instanceof Movable)
            {
                ((Movable) mSelectedDevice).softStop();
            }
        }

        if(pKey.equals(getActivationKey()))
        {
            showWorkflowStartedMessage();
        }

        /*
         * Check whether the activation key is still pressed
         *
         */
        if (!pKeyBoard.isKeyPressed(getActivationKey()))
        {
            setActivationKey(null);
        }

    }

    private void setRoomLightDimming(boolean pDimming)
    {
        TreatmentRoomLight light = (TreatmentRoomLight) mDevices.getDevice(PmsDevice.Label.TRLIGHTS);
        if (light != null)
        {
            if (pDimming)
            {
                light.switchOn();
            }
            else
            {
                light.switchOff();
            }
        }
    }

    /**
     * This method is called whenever user selects a menu item via the hand
     * pendant.
     *
     * @param pMenuRequest Menu request received from menu effect.
     */

    /**
     * onMenuRequest.
     * @param pMenuRequest
     */
    public void onMenuRequest(MenuRequest pMenuRequest)
    {
        Assert.notNull(pMenuRequest, "Menu request cannot be null.");
        if(pMenuRequest.getMotionRequest() instanceof PaliRequest
                && ((PaliRequest) pMenuRequest.getMotionRequest()).getAction() == SCANNER_RECOVER)
        {
            showInfo(PmsUtils.getLocalizedMessageByKey(
                    bundle, "pms.ap.controller.nozzleImagerSafetyError.recover.startRecover"));
            mNozzleImagerRecoverNextStep = NozzleImagerScrutineerRecoverySteps.STARTED;
            return;
        }
        /* We may have an invalid menu that points to not installed devices */
        PmsDevice toBeSelected = mDevices.getDevice(pMenuRequest.getDeviceLabel());
        if (toBeSelected == null
                && !(pMenuRequest.getMotionRequest() instanceof CsRequest)
                && !(pMenuRequest.getMotionRequest() instanceof SaveRequest)
                && !(pMenuRequest.getMotionRequest() instanceof UtilRequest)
                && !(pMenuRequest.getMotionRequest() instanceof PositionRequest)
                && !(pMenuRequest.getMotionRequest() instanceof TrlRequest))
        {
            showError(PmsUtils.getLocalizedMessageByKey(
                    bundle, "pms.ap.controller.deviceNotInstalled",
                    PmsUtils.getLocalizedDeviceLabelByKey(pMenuRequest.getDeviceLabel())));
            return;
        }


        if (pMenuRequest.getMotionRequest() instanceof UtilRequest)
        {
            UtilRequest request = (UtilRequest) pMenuRequest.getMotionRequest();
            if (request.getAction() == UtilRequest.Action.RESETSRCU)
            {
                showInfo(PmsUtils.getLocalizedMessageByKey(bundle, "pms.ap.controller.srcuresetmsg", String.valueOf(SRCU_RESET_PERIOD / 1000)));
                mDevices.activateAll();
                if (mDeactivateTimer == null)
                {
                    mDeactivateTimer = mTimerFactory.create(this);
                }
                mDeactivateTimer.schedule(SRCU_RESET_PERIOD);
            }
            else if (request.getAction() == UtilRequest.Action.RESETCHECK)
            {
                resetCheckMgr();
            }
            setPositionRequest(null);
        }
        /* Check for position selection requests */
        else if (pMenuRequest.getMotionRequest() instanceof PositionRequest)
        {
            /* Notify that user has selected some position */
            setPositionRequest((PositionRequest) (pMenuRequest.getMotionRequest()));
        }
        /* Check for Elbow flip request */
        else if(pMenuRequest.getMotionRequest() instanceof ElbowFlipRequest)
        {
            StatusResult sr = mElbowFlipHelper.isDeviceReady(toBeSelected);
            if(!sr.isStatus())
            {
                showMessage(sr);
                return;
            }
            selectDevice(toBeSelected);
            mElbowFlipHelper.setElbowFlipRequest((ElbowFlipRequest) pMenuRequest.getMotionRequest());
            setLedOn(HpLed.MOVE, mElbowFlipHelper.isRequestPrepared());
        }
        else if(pMenuRequest.getMotionRequest() instanceof CTRequest)
        {
            double zPos = ((CTRequest)pMenuRequest.getMotionRequest()).getZPosition();
            selectDevice(toBeSelected);
            showMessage(mCTHelper.prepareCTMotion(zPos));
            showMessage(mCTHelper.isInRoomCTReady());
            setLedOn(HpLed.MOVE, mCTHelper.isRequestPrepared());
        }
        /* Check for GoTo requests */
        else if (pMenuRequest.getMotionRequest() instanceof GoToRequest
                || pMenuRequest.getMotionRequest() instanceof GoToPathRequest)
        {
            selectDevice(toBeSelected);
            setGoToRequest(pMenuRequest.getMotionRequest());
        }
        /* Check for Home requests */
        else if (pMenuRequest.getMotionRequest() instanceof HomeRequest)
        {
            mHomeHelper.setHomeReferenceByRequest((HomeRequest) pMenuRequest.getMotionRequest());
            selectDevice(toBeSelected);
            showMessage(mHomeHelper.getConfirmMessage());
            setSpeedLevel(getSpeedLevel());
            setLedOn(HpLed.MOVE, mHomeHelper.isMovableReference());
            setPositionRequest(null);
        }
        else if(pMenuRequest.getMotionRequest() instanceof ImagingRequest)
        {
            selectDevice(toBeSelected);
            setPositionRequest(null);
            mImagingHelper.setImagingRequest((ImagingRequest) (pMenuRequest.getMotionRequest()));
            setLedOn(HpLed.MOVE, mImagingHelper.isRequestPrepared());
        }
        /* Check for PALI requests */
        else if (pMenuRequest.getMotionRequest() instanceof PaliRequest)
        {
            /* For lasers we do not need to activate the move button */
            if (pMenuRequest.getDeviceLabel().equals(PmsDevice.Label.LASERS))
            {
                PaliRequest.Action action = ((PaliRequest) pMenuRequest.getMotionRequest()).getAction();
                Lasers lasers = (Lasers) mDevices.getDevice(PmsDevice.Label.LASERS);
                Lasers ahLasers = (Lasers) mDevices.getDevice(PmsDevice.Label.AH_LASERS);
                switch (action)
                {

                    case TOGGLE:
                        lasers.toggle();
                        if (lasers.isSwitchedOn())
                        {
                            startLightsTimer();
                        }
                        if (ahLasers != null)
                        {
                            ahLasers.toggle();
                            if (ahLasers.isSwitchedOn())
                            {
                                startLightsTimer();
                            }
                        }
                        ((TreatmentRoomLight) mDevices.getDevice(PmsDevice.Label.TRLIGHTS)).toggle();
                        break;
                    default:
                        throw new AccessPointException("Action " + action
                                + " is invalid for " + lasers.getLabel()
                                + " device.");
                }
            }
            else
            // Other PALI devices except lasers
            {
                if (pMenuRequest.getDeviceLabel().equals(PmsDevice.Label.MLC))
                {
                    /* For MLC we do not need to press Move button to select it */
                    showInfo(PmsUtils.getLocalizedMessageByKey(bundle, "pms.ap.controller.mlcIsSelected"));
                }
                else
                {
                    mPaliHelper.setPaliRequest((PaliRequest) pMenuRequest.getMotionRequest());
                    setLedOn(HpLed.MOVE, mPaliHelper.isRequestPrepared());
                }
                /* Selecting PALI device */
                selectDevice(toBeSelected);
            }
            setPositionRequest(null);
        }
        /* Handle docking menu requests */
        else if (pMenuRequest.getMotionRequest() instanceof DockingRequest)
        {
            /* Selecting device to be moved */
            selectDevice(toBeSelected);
            setDockingRequest((DockingRequest) (pMenuRequest.getMotionRequest()));
        }
        /* Check for CS requests */
        else if (pMenuRequest.getMotionRequest() instanceof CsRequest)
        {
            setJogMode(((CsRequest) (pMenuRequest.getMotionRequest())).getCs());
            if (mJogMode == Jog.Mode.EQUIPMENT)
            {
                setDisplayZoneText(JOG_SYSTEM_DISPLAY_ZONE,
                        PmsUtils.getLocalizedMessageByKey(bundle, "pms.ap.controller.label.equipment"));
            }
            else if (mJogMode == Jog.Mode.ISOCENTRIC)
            {
                setDisplayZoneText(JOG_SYSTEM_DISPLAY_ZONE,
                        PmsUtils.getLocalizedMessageByKey(bundle, "pms.ap.controller.label.isocentric"));
            }
            setPositionRequest(null);

        }
        /* Check for Save requests */
        else if (pMenuRequest.getMotionRequest() instanceof SaveRequest)
        {
            PmsDevice pps = mDevices.getDevice(Label.PPS);
            PpdPositionLabel label = ((SaveRequest) pMenuRequest.getMotionRequest()).getPpdPositionLabel();
            Gantry gantry = (Gantry)mDevices.getDevice(Label.GANTRY);
            if(!gantry.isCurrentPositionSystemToleranceStatus()
                    && !getKVkVSequential().equals("false")
                    && (label == PpdPositionLabel.TREATMENT || label == PpdPositionLabel.TREATMENT_ROT_ONLY))
            {
                showError(PmsUtils.getLocalizedMessageByKey(bundle, "pms.ap.controller.gantryOutsidePrescription"));
                shortBeep();
            }
            else if (pps != null
                    && pps instanceof HeadNeckChairSupport
                    && ((HeadNeckChairSupport) pps).isHeadNeckChairPresent()
                    && !((HeadNeckChairSupport) pps).isHeadNeckChairWellPositioned())
            {
                showInfo(PmsUtils.getLocalizedMessageByKey(bundle, "pms.ap.controller.positionChairBeforeSaving"));
                shortBeep();
            }
            else
            {
                mSaveRequest = (SaveRequest) (pMenuRequest.getMotionRequest());
                if (!currentPositionIsValid(mDevices.getCurrentPpdPosition()))
                {
                    showConfirm(PmsUtils.getLocalizedMessageByKey(bundle,
                            "pms.ap.controller.pressOkToSaveInvalidPos",
                            PmsUtils.getLocalizedPositionLabelByKey(mSaveRequest.getPpdPositionLabel())));
                    shortBeep();
                }
                else
                {
                    PpdPosition tolerance = mPositions.getPpdPositionByLabel(PpdPositionLabel.SAVE_TOLERANCE);
                    PpdPosition position = mPositions.getPpdInitialPositionByLabel(mSaveRequest.getPpdPositionLabel());
                    PpdPosition correctedPosition = mPositions.getPpdPositionByLabel(PpdPositionLabel.CORRECTED);

                    boolean insideTolerances = true;
                    if(tolerance != null && position != null)
                    {
                        insideTolerances = PmsUtils.insideTolerances(mDevices.getCurrentPpdPosition(), position, tolerance);
                    }
                    boolean correctedApply = true;
                    if(correctedPosition != null)
                    {
                        /** By default we apply the Gantry Correction file because we are in setup or in treatment
                         * and we want to confirm the position.
                         */
                        correctedApply = ((Pps)mDevices.getDevice(Label.PPS)).isAtPosition(correctedPosition, true);
                    }

                    if(insideTolerances && correctedApply)
                    {
                        showConfirm(PmsUtils.getLocalizedMessageByKey(bundle,
                                "pms.ap.controller.pressOkToSave",
                                PmsUtils.getLocalizedPositionLabelByKey(mSaveRequest.getPpdPositionLabel())));
                    }
                    else
                    if(insideTolerances)
                    {
                        showConfirm(PmsUtils.getLocalizedMessageByKey(bundle,
                                "pms.ap.controller.pressOkToSaveOutOfCorrectedPosition",
                                PmsUtils.getLocalizedPositionLabelByKey(mSaveRequest.getPpdPositionLabel())));
                        shortBeep();
                    }
                    else
                    if(correctedApply)
                    {
                        showConfirm(PmsUtils.getLocalizedMessageByKey(bundle,
                                "pms.ap.controller.pressOkToSaveOutOfTolerance",
                                PmsUtils.getLocalizedPositionLabelByKey(mSaveRequest.getPpdPositionLabel())));
                        shortBeep();
                    }
                    else
                    {
                        showConfirm(PmsUtils.getLocalizedMessageByKey(bundle,
                                "pms.ap.controller.pressOkToSaveOutOfToleranceAndCorrectedPosition",
                                PmsUtils.getLocalizedPositionLabelByKey(mSaveRequest.getPpdPositionLabel())));
                        shortBeep();
                    }
                }
            }
        }
        else if (pMenuRequest.getMotionRequest() instanceof TrlRequest)
        {
            TrlRequest request = (TrlRequest) pMenuRequest.getMotionRequest();
            LightField lightField = ((LightField) mDevices.getDevice(Label.LFIELD));
            if (lightField != null)
            {
                if (request.getAction() == TrlRequest.Action.ON)
                {

                    setRoomLightDimming(false);
                    lightField.switchOn();
                    mLightFieldOn = true;
                    startLightsTimer();
                }
                else if (request.getAction() == TrlRequest.Action.DIM)
                {
                    setRoomLightDimming(true);
                    lightField.switchOn();
                    mLightFieldOn = true;
                    startLightsTimer();
                }
                else
                {
                    setRoomLightDimming(false);
                    lightField.switchOff();
                    mLightFieldOn = false;
                }
            }
            setPositionRequest(null);
        }
    }

    void showInfo(String pText)
    {
        switch (mAccessPointMotion.getSelected())
        {
            case AccessPointMotion.TR_ACCESS_POINT:
                mTrHandPendant.showInfo(pText);
                break;
            case AccessPointMotion.TCR_RPD_ACCESS_POINT:
            case AccessPointMotion.TCR_SAFETY_RACK_ACCESS_POINT:
                mTcrHandPendant.showInfo(pText);
                break;
            case AccessPointMotion.TR_HP2_ACCESS_POINT:
                mTr2HandPendant.showInfo(pText);
                break;
        }
    }

    void showStep(int pCurrentStep, int pTotalStel, String pText)
    {
        switch (mAccessPointMotion.getSelected())
        {
            case AccessPointMotion.TR_ACCESS_POINT:
                mTrHandPendant.showStep(pCurrentStep, pTotalStel, pText);
                break;
            case AccessPointMotion.TCR_RPD_ACCESS_POINT:
            case AccessPointMotion.TCR_SAFETY_RACK_ACCESS_POINT:
                mTcrHandPendant.showStep(pCurrentStep, pTotalStel, pText);
                break;
            case AccessPointMotion.TR_HP2_ACCESS_POINT:
                mTr2HandPendant.showStep(pCurrentStep, pTotalStel, pText);
                break;
        }
    }

    void showError(String pText)
    {
        switch (mAccessPointMotion.getSelected())
        {
            case AccessPointMotion.TR_ACCESS_POINT:
                mTrHandPendant.showError(pText);
                break;
            case AccessPointMotion.TCR_RPD_ACCESS_POINT:
            case AccessPointMotion.TCR_SAFETY_RACK_ACCESS_POINT:
                mTcrHandPendant.showError(pText);
                break;
            case AccessPointMotion.TR_HP2_ACCESS_POINT:
                mTr2HandPendant.showError(pText);
                break;
        }

    }

    void showWarning(String pText)
    {
        switch (mAccessPointMotion.getSelected())
        {
            case AccessPointMotion.TR_ACCESS_POINT:
                mTrHandPendant.showWarning(pText);
                break;
            case AccessPointMotion.TCR_RPD_ACCESS_POINT:
            case AccessPointMotion.TCR_SAFETY_RACK_ACCESS_POINT:
                mTcrHandPendant.showWarning(pText);
                break;
            case AccessPointMotion.TR_HP2_ACCESS_POINT:
                mTr2HandPendant.showWarning(pText);
                break;
        }
    }

    void showConfirm(String pText)
    {
        switch (mAccessPointMotion.getSelected())
        {
            case AccessPointMotion.TR_ACCESS_POINT:
                mTrHandPendant.showConfirm(pText);
                break;
            case AccessPointMotion.TCR_RPD_ACCESS_POINT:
            case AccessPointMotion.TCR_SAFETY_RACK_ACCESS_POINT:
                mTcrHandPendant.showConfirm(pText);
                break;
            case AccessPointMotion.TR_HP2_ACCESS_POINT:
                mTr2HandPendant.showConfirm(pText);
                break;
        }

    }

    void showMenu(HpMenu pHpMenu)
    {
        switch (mAccessPointMotion.getSelected())
        {
            case AccessPointMotion.TR_ACCESS_POINT:
                mTrHandPendant.showMenu(pHpMenu);
                break;
            case AccessPointMotion.TCR_RPD_ACCESS_POINT:
            case AccessPointMotion.TCR_SAFETY_RACK_ACCESS_POINT:
                mTcrHandPendant.showMenu(pHpMenu);
                break;
            case AccessPointMotion.TR_HP2_ACCESS_POINT:
                mTr2HandPendant.showMenu(pHpMenu);
                break;
        }
    }

    void showMenu(HpMenu pHpMenu, int pSelected)
    {
        switch (mAccessPointMotion.getSelected())
        {
            case AccessPointMotion.TR_ACCESS_POINT:
                mTrHandPendant.showMenu(pHpMenu, pSelected);
                break;
            case AccessPointMotion.TCR_RPD_ACCESS_POINT:
            case AccessPointMotion.TCR_SAFETY_RACK_ACCESS_POINT:
                mTcrHandPendant.showMenu(pHpMenu, pSelected);
                break;
            case AccessPointMotion.TR_HP2_ACCESS_POINT:
                mTr2HandPendant.showMenu(pHpMenu, pSelected);
                break;
        }
    }

    void showMenuAndSelect(HpMenu pHpMenu, int pSelected)
    {
        switch (mAccessPointMotion.getSelected())
        {
            case AccessPointMotion.TR_ACCESS_POINT:
                mTrHandPendant.showMenuAndSelect(pHpMenu, pSelected);
                break;
            case AccessPointMotion.TCR_RPD_ACCESS_POINT:
            case AccessPointMotion.TCR_SAFETY_RACK_ACCESS_POINT:
                mTcrHandPendant.showMenuAndSelect(pHpMenu, pSelected);
                break;
            case AccessPointMotion.TR_HP2_ACCESS_POINT:
                mTr2HandPendant.showMenuAndSelect(pHpMenu, pSelected);
                break;
        }
    }

    void setDisplayZoneText(int pDisplayZone, String pText)
    {
        Assert.notNull(mAccessPointMotion, "Access point motion device cannot be null");
        switch (mAccessPointMotion.getSelected())
        {
            case AccessPointMotion.TR_ACCESS_POINT:
                mTrHandPendant.getDisplay().getDisplayZone(pDisplayZone).setText(pText);
                break;
            case AccessPointMotion.TCR_RPD_ACCESS_POINT:
            case AccessPointMotion.TCR_SAFETY_RACK_ACCESS_POINT:
                mTcrHandPendant.getDisplay().getDisplayZone(pDisplayZone).setText(pText);
                break;
            case AccessPointMotion.TR_HP2_ACCESS_POINT:
                mTr2HandPendant.getDisplay().getDisplayZone(pDisplayZone).setText(pText);
                break;
        }
    }

    HpEffect getCurrentEffect()
    {
        switch (mAccessPointMotion.getSelected())
        {
            case AccessPointMotion.TR_ACCESS_POINT:
                return mTrHandPendant.getEffect();
            case AccessPointMotion.TCR_RPD_ACCESS_POINT:
            case AccessPointMotion.TCR_SAFETY_RACK_ACCESS_POINT:
                return mTcrHandPendant.getEffect();
            case AccessPointMotion.TR_HP2_ACCESS_POINT:
                return mTr2HandPendant.getEffect();
        }
        return null;
    }

    HpEffect getPreviousEffect()
    {
        switch (mAccessPointMotion.getSelected())
        {
            case AccessPointMotion.TR_ACCESS_POINT:
                return mTrHandPendant.getPreviousEffect();
            case AccessPointMotion.TCR_RPD_ACCESS_POINT:
            case AccessPointMotion.TCR_SAFETY_RACK_ACCESS_POINT:
                return mTcrHandPendant.getPreviousEffect();
            case AccessPointMotion.TR_HP2_ACCESS_POINT:
                return mTr2HandPendant.getPreviousEffect();
        }
        return null;
    }

    void setEffect(HpEffect pEffect)
    {
        switch (mAccessPointMotion.getSelected())
        {
            case AccessPointMotion.TR_ACCESS_POINT:
                mTrHandPendant.setEffect(pEffect);
                break;
            case AccessPointMotion.TCR_RPD_ACCESS_POINT:
            case AccessPointMotion.TCR_SAFETY_RACK_ACCESS_POINT:
                mTcrHandPendant.setEffect(pEffect);
                break;
            case AccessPointMotion.TR_HP2_ACCESS_POINT:
                mTr2HandPendant.setEffect(pEffect);
                break;
        }
    }

    boolean handPendantIsSelected(HandPendant pHandPendant)
    {
        if(pHandPendant.equals(mTrHandPendant))
        {
            return mAccessPointMotion.getSelected() == AccessPointMotion.TR_ACCESS_POINT;
        }
        else if(pHandPendant.equals(mTr2HandPendant))
        {
            return mAccessPointMotion.getSelected() == AccessPointMotion.TR_HP2_ACCESS_POINT;
        }
        else if(pHandPendant.equals(mTcrHandPendant))
        {
            return mAccessPointMotion.getSelected() == AccessPointMotion.TCR_RPD_ACCESS_POINT
                    || mAccessPointMotion.getSelected() == AccessPointMotion.TCR_SAFETY_RACK_ACCESS_POINT;
        }
        else
        {
            return false;
        }
    }

    void performDisconnected(HandPendant pHandPendant)
    {
        if (handPendantIsSelected(pHandPendant))
        {
            mDevices.deactivateAll();
        }
    }

    void performConnected(HandPendant pHandPendant)
    {
        pHandPendant.showWelcome(PmsUtils.getLocalizedMessageByKey(bundle, "pms.ap.controller.welcome"));
        setJogType(Jog.Type.CONTINUOUS);
        setJogMode(Jog.Mode.EQUIPMENT);
        setSpeedLevel(Speed.Level.LOW);
        if (pHandPendant.equals(mTrHandPendant))
        {
            setTrEnabled(true);
            switch (mAccessPointMotion.getSelected())
            {
                case AccessPointMotion.NO_ACCESS_POINT:
                    clear();
                    disableTr(PmsUtils.getLocalizedMessageByKey(bundle, "pms.ap.controller.locked"));
                    break;
                case AccessPointMotion.TR_ACCESS_POINT:
                    setDisplayZoneText(SEL_DISPLAY_ZONE,
                            PmsUtils.getLocalizedMessageByKey(bundle, "pms.ap.controller.label.selected"));
                    break;
                case AccessPointMotion.TCR_RPD_ACCESS_POINT:
                case AccessPointMotion.TCR_SAFETY_RACK_ACCESS_POINT:
                    disableTr(PmsUtils.getLocalizedMessageByKey(bundle, "pms.ap.controller.lockedByTcr"));
                    break;
                case AccessPointMotion.TR_HP2_ACCESS_POINT:
                    disableTr(PmsUtils.getLocalizedMessageByKey(bundle, "pms.ap.controller.lockedByTr2"));
                    break;
            }
        }
        else if (pHandPendant.equals(mTr2HandPendant))
        {
            setTr2Enabled(true);
            switch (mAccessPointMotion.getSelected())
            {
                case AccessPointMotion.NO_ACCESS_POINT:
                    clear();
                    disableTr2(PmsUtils.getLocalizedMessageByKey(bundle, "pms.ap.controller.locked"));
                    break;
                case AccessPointMotion.TR_ACCESS_POINT:
                    disableTr2(PmsUtils.getLocalizedMessageByKey(bundle, "pms.ap.controller.lockedByTr"));
                    break;
                case AccessPointMotion.TCR_RPD_ACCESS_POINT:
                case AccessPointMotion.TCR_SAFETY_RACK_ACCESS_POINT:
                    disableTr2(PmsUtils.getLocalizedMessageByKey(bundle, "pms.ap.controller.lockedByTcr"));
                    break;
                case AccessPointMotion.TR_HP2_ACCESS_POINT:
                    setDisplayZoneText(SEL_DISPLAY_ZONE,
                            PmsUtils.getLocalizedMessageByKey(bundle, "pms.ap.controller.label.selected"));
                    break;
            }
        }
        else if (pHandPendant.equals(mTcrHandPendant))
        {
            setTcrEnabled(true);
            switch (mAccessPointMotion.getSelected())
            {
                case AccessPointMotion.NO_ACCESS_POINT:
                    clear();
                    disableTcr(PmsUtils.getLocalizedMessageByKey(bundle, "pms.ap.controller.locked"));
                    break;
                case AccessPointMotion.TR_ACCESS_POINT:
                    disableTcr(PmsUtils.getLocalizedMessageByKey(bundle, "pms.ap.controller.lockedByTr"));
                    break;
                case AccessPointMotion.TCR_RPD_ACCESS_POINT:
                case AccessPointMotion.TCR_SAFETY_RACK_ACCESS_POINT:
                    setMode(mAccessPointMotion.getSelected());
                    setDisplayZoneText(SEL_DISPLAY_ZONE,
                            PmsUtils.getLocalizedMessageByKey(bundle, "pms.ap.controller.label.selected"));
                    break;
                case AccessPointMotion.TR_HP2_ACCESS_POINT:
                    disableTcr(PmsUtils.getLocalizedMessageByKey(bundle, "pms.ap.controller.lockedByTr2"));
                    break;
            }
        }
    }

    void setLedOn(HpLed pLed, boolean pOn)
    {
        switch (mAccessPointMotion.getSelected())
        {
            case AccessPointMotion.TR_ACCESS_POINT:
                mTrHandPendant.getDisplay().setLedOn(pLed, pOn);
                break;
            case AccessPointMotion.TCR_RPD_ACCESS_POINT:
            case AccessPointMotion.TCR_SAFETY_RACK_ACCESS_POINT:
                mTcrHandPendant.getDisplay().setLedOn(pLed, pOn);
                break;
            case AccessPointMotion.TR_HP2_ACCESS_POINT:
                mTr2HandPendant.getDisplay().setLedOn(pLed, pOn);
                break;
        }
    }

    void clearLeds()
    {
        switch (mAccessPointMotion.getSelected())
        {
            case AccessPointMotion.TR_ACCESS_POINT:
                mTrHandPendant.getDisplay().clearLeds();
                break;
            case AccessPointMotion.TCR_RPD_ACCESS_POINT:
            case AccessPointMotion.TCR_SAFETY_RACK_ACCESS_POINT:
                mTcrHandPendant.getDisplay().clearLeds();
                break;
            case AccessPointMotion.TR_HP2_ACCESS_POINT:
                mTr2HandPendant.getDisplay().clearLeds();
                break;
        }
    }

    void clearSelectedHp()
    {
        switch (mAccessPointMotion.getSelected())
        {
            case AccessPointMotion.TR_ACCESS_POINT:
                mTrHandPendant.clear();
                break;
            case AccessPointMotion.TCR_RPD_ACCESS_POINT:
            case AccessPointMotion.TCR_SAFETY_RACK_ACCESS_POINT:
                mTcrHandPendant.clear();
                break;
            case AccessPointMotion.TR_HP2_ACCESS_POINT:
                mTr2HandPendant.clear();
                break;
        }
    }

    void setMode(int pMode)
    {
        mTcrHandPendant.setMode(pMode);
    }

    /**
     * showHandPendantUi.
     */
    public void showHandPendantUi()
    {
        mTcrHandPendant.showHandPendantUi();
    }

    /**
     * enableHandPendantUi.
     */
    public void enableHandPendantUi()
    {
        mTcrHandPendant.enableHandPendantUi();
    }

    void hideHandPendantUi()
    {
        mTcrHandPendant.hideHandPendantUi();
    }

    private boolean currentPositionIsValid(PpdPosition pPosition)
    {
        return mPositionChecks.isInTreatmentVolume(pPosition.getPpsPosition());
    }

    /**
     * setPositionChecks.
     * @param pPositionChecks
     */
    public void setPositionChecks(PpdPositionChecks pPositionChecks)
    {
        mPositionChecks = pPositionChecks;
    }

    /** {@inheritDoc} */
    @Override
    protected void containerStopping()
    {
        getLogger().info("Process is shutting down - DISABLING ALL DEVICES.");
        mDevices.deactivateAll();
        super.containerStopping();
    }

    /** {@inheritDoc} */
    @Override
    public void processStarted(String pProcessName)
    {
        //Do nothing
    }

    /** {@inheritDoc} */
    @Override
    public void processStopped(String pProcessName)
    {
        showError(PmsUtils.getLocalizedMessageByKey(bundle,
                "pms.ap.controller.processStopped",
                pProcessName));
    }

    /**
     * setCheckManagerPps.
     * @param pCheckManagerPps
     */
    public void setCheckManagerPps(CheckManager pCheckManagerPps)
    {
        Assert.notNull(pCheckManagerPps, "The pps check manager must not be null.");
        mCheckManagerPps = pCheckManagerPps;
    }

    /**
     * setCheckManagerPositioning.
     * @param pCheckManagerPositioning
     */
    public void setCheckManagerPositioning(CheckManager pCheckManagerPositioning)
    {
        Assert.notNull(pCheckManagerPositioning, "The Positioning Proxy check manager must not be null");
        mCheckManagerPositioning = pCheckManagerPositioning;
    }

    private void resetCheckMgr()
    {
        mCheckManagerPps.reset();
        mCheckManagerPositioning.reset();
        showInfo(PmsUtils.getLocalizedMessageByKey(bundle, "pms.ap.controller.checkMgrReseted"));
    }

    /**
     * setGantryType.
     * @param pGantryType
     */
    public void setGantryType(String pGantryType)
    {
        mGantryType = pGantryType;
    }

    /**
     * setLightCommandTimeout.
     * @param pLightCommandTimeout
     */
    public void setLightCommandTimeout(int pLightCommandTimeout)
    {
        mLightCommandTimeout = pLightCommandTimeout;
    }

    private void startLightsTimer()
    {
        if (mDeactivateLightsTimer == null)
        {
            mDeactivateLightsTimer = mTimerFactory.create(this);
        }
        mDeactivateLightsTimer.schedule(60000L * mLightCommandTimeout);
    }

    /** Helpers Callbacks **/
    @Override
    public PmsDevice getSelectedDevice()
    {
        return mSelectedDevice;
    }

    /** {@inheritDoc} */
    @Override
    public void setTargetPosition(PpdPosition pTargetPositionPPD)
    {
        //Nothing here
    }

    /** {@inheritDoc} */
    @Override
    public void setTargetPositionName(String pTargetPositionName)
    {
        PpdPositionLabel oldVal = mSelectedPositionLabel;
        mSelectedPositionLabel = PpdPositionLabel.valueOf(pTargetPositionName.toUpperCase());
        firePropertyChange(SELECTED_POSITION_LABEL_PROPERTY, oldVal, pTargetPositionName);
    }

    /** {@inheritDoc} */
    @Override
    public void setTargetElbowOrientation(ElbowOrientation pTargetElbow)
    {
        firePropertyChange(SELECTED_ELBOW_POSITION_LABEL_PROPERTY, ElbowOrientation.UNKNOWN, pTargetElbow);
    }

    /** {@inheritDoc} */
    @Override
    public StatusResult selectDevice(Label pTargetDevice)
    {
        selectDevice(mDevices.getDevice(pTargetDevice));
        return new StatusResult();
    }

    /** {@inheritDoc} */
    @Override
    public void clearScreen()
    {
        clear();
    }

    @Override
    public void setShowTargetNotReached(boolean pShowTargetNotReachedMessage,
                                        PatientPositioningDevice pTargetNotReachedDevice)
    {
        mShowTargetNotReachedMessage = pShowTargetNotReachedMessage;
        mTargetNotReachedDevice = pTargetNotReachedDevice;
    }

    /** Message factory **/
    private void showMessage(StatusResult pStatusResult)
    {
        if(!pStatusResult.isStatus())
        {
            switch (pStatusResult.getMessageType())
            {
                case INFO:
                    showInfo(pStatusResult.getMessage());
                    break;
                case ERROR:
                    showError(pStatusResult.getMessage());
                    break;
                case WARNING:
                    showWarning(pStatusResult.getMessage());
                    break;
                case CONFIRM:
                    showConfirm(pStatusResult.getMessage());
                    break;
            }
            switch (pStatusResult.getBeepType())
            {
                case SHORT:
                    shortBeep();
                    break;
                case LONG:
                    longBeep(mAccessPointMotion.getSelected());
                    break;
                case TWICE:
                    shortBeep();
                    shortBeep();
                    break;
            }
        }
    }
    private final Logger logger = Logger.getLogger(ApController.class);

    private Cbct cbct;
    private GcfApplier mGcfApplier;

    private void showWorkflowStartedMessage()
    {
        if(mWorkflowStartedMessage)
        {
            clear();
            showConfirm(PmsUtils.getLocalizedMessageByKey(bundle, "pms.ap.controller.workflowPositionReceived",
                    PmsUtils.getLocalizedPositionLabelByKey(
                            mActivityModel.isForIrradiation()?PpdPositionLabel.TREATMENT:PpdPositionLabel.SETUP)));
            mWorkflowStartedMessage = false;
            mWorkflowMessageConfirmation = true;
            Object oldValue = mSelectedPositionLabel;
            mSelectedPositionLabel = PpdPositionLabel.NONE;
            firePropertyChange(SELECTED_POSITION_LABEL_PROPERTY, oldValue, mSelectedPositionLabel);
            if (mPPSLeftRightArmOption)
            {
                firePropertyChange(SELECTED_ELBOW_POSITION_LABEL_PROPERTY, null, ElbowOrientation.NONE);
            }
        }
    }
}
