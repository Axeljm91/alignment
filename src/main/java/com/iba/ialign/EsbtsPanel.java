package com.iba.ialign;

import com.iba.blak.device.api.Magnet;
import com.iba.blakOverwrite.BeamLine;
import com.iba.icomp.core.checks.CheckManager;
import com.iba.icomp.core.component.CallbackUtility;
import com.iba.icomp.core.util.Logger;
import com.iba.icompx.core.activity.ActivityController;
import com.iba.icompx.core.activity.ActivityStatus;
import com.iba.icompx.ui.util.ResourceManager;
import com.iba.pts.bms.bss.beamscheduler.api.BeamAllocation;
import com.iba.pts.bms.bss.beamscheduler.api.BeamScheduler;
import com.iba.pts.bms.bss.beamscheduler.ui.BeamSchedulerPanel;
import com.iba.pts.bms.bss.controller.api.BssActivityId;
import com.iba.pts.bms.bss.controller.api.BssController;
import com.iba.pts.bms.bss.controller.api.BssControllerListener;
import com.iba.pts.bms.bss.controller.api.BssControllerLocal;
import com.iba.pts.bms.bss.controller.ui.LogPanel;
import com.iba.pts.bms.bss.controller.ui.bps.IdlePanel;
import com.iba.ialign.DevicesPanel;
import com.iba.pts.bms.bss.controller.ui.esbts.*;
import com.iba.pts.bms.bss.datatypes.api.BssSettings;
import com.iba.pts.bms.bss.esbts.Beamline;
import com.iba.pts.bms.bss.esbts.BeamlineSection;
import com.iba.pts.bms.bss.esbts.BeamlinesInfrastructure;
import com.iba.pts.bms.bss.esbts.PowerSaver;
import com.iba.pts.bms.bss.esbts.config.BeamlineConfig;
import com.iba.pts.bms.bss.esbts.controller.api.EsBtsActivityId;
import com.iba.pts.bms.bss.esbts.controller.api.EsBtsController;
import com.iba.pts.bms.bss.esbts.solution.RangeConverter;
import com.iba.pts.bms.datatypes.api.BeamDeliveryPoint;
import com.iba.pts.bms.datatypes.api.BeamSupplyPoint;
import com.iba.pts.bms.datatypes.api.TherapyCentre;
import com.iba.pts.bms.datatypes.api.TreatmentMode;

import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.*;
import java.util.List;
import javax.accessibility.AccessibleContext;
import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

@SuppressWarnings("serial")
public class EsbtsPanel extends JPanel implements PropertyChangeListener
{

    public EsbtsPanel()
    {
//        mBeamlines = pBeamlines;
//        mCyclicCheckManager = pCyclicCheckManager;
        Map<String, RangeConverter> map;
        RangeConverter converter;
        List<BeamlineSection> mBeamlineSection;
//        mSetRangePanel.getStartButton().addActionListener(pArg0 -> {
//            String id = Controller.beam.beamScheduler.getCurrentBeamAllocation().getBeamSupplyPointId();
//            double range = ((Number) mSetRangePanel.getRangeTextField().getValue()).doubleValue();
//            //String opticalSolution = (String) mSetRangePanel.getOpticalSolutionComboBox().getSelectedItem();
//            double angle = ((Number) mSetRangePanel.getGantryAngleTextField().getValue()).doubleValue();
//            double offsetX = ((Number) mSetRangePanel.getOffsetXTextField().getValue()).doubleValue();
//            double offsetY = ((Number) mSetRangePanel.getOffsetYTextField().getValue()).doubleValue();
//            boolean cycling = mSetRangePanel.getCyclingCheckBox().isSelected();
//
//            //mEsbtsPanel.getEsBtsController().startSetRangeActivity(id, range, opticalSolution, angle, offsetX, offsetY, cycling);
//            Controller.beam.bssController.startSetRangeActivity(id, range, "4.0", angle, offsetX, offsetY, cycling);
//            mActionsPanel.mSetRangeStatusLabel.setIcon(ResourceManager.getInstance().getImageIcon("bms/bss/controller/ui/good"));
//            mActionsPanel.mDisableBeamStatusLabel.setIcon(ResourceManager.getInstance().getImageIcon("bms/bss/controller/ui/inactive"));
//            mActionsPanel.mEnableBeamStatusLabel.setIcon(ResourceManager.getInstance().getImageIcon("bms/bss/controller/ui/inactive"));
//            mActionsPanel.mIdleStatusLabel.setIcon(ResourceManager.getInstance().getImageIcon("bms/bss/controller/ui/inactive"));
//            mActionsPanel.mSelectBeamlineStatusLabel.setIcon(ResourceManager.getInstance().getImageIcon("bms/bss/controller/ui/inactive"));
//        });
        Set<Magnet> mOffMagnet;
        BeamLine beamline = new BeamLine();
        //beamline.loadFromFile();
        //mBeamlines = new Beamline("FBTR1", 1, 1, map, mBeamlineSection,mOffMagnet);
        buildPanel();
    }



    public void setTherapyCentre(TherapyCentre pTherapyCentre)
    {
        mTherapyCentre = pTherapyCentre;
    }

    /**
     * Sets the Beam scheduler.
     * @param pBeamScheduler the Beam Scheduler
     */
    public void setBeamScheduler(BeamScheduler pBeamScheduler)
    {
        pBeamScheduler.addPropertyChangeListener(this);
    }

    /**
     * Power saver setter.
     * @param pPowerSaver
     */
    public void setPowerSaver(PowerSaver pPowerSaver)
    {
        mPowerSavingPanel.setPowerSaver(pPowerSaver);
        mPowerSavingPanel.setEnableManualPowerSave(mManualMode);
        mPowerSavingPanel.init();
    }

    /**
     * Sets a list of check nodes to expand all the time.
     * @param pNodes a collection of nodes
     */
    public void setAutoExpandedCheckNodes(Collection<String> pNodes)
    {
        mAutoExpandedCheckNodes = pNodes;
        if (mCheckManagerPanel != null)
        {
            mCheckManagerPanel.setAutoExpandedNodes(mAutoExpandedCheckNodes);
        }
    }

    /**
     * Set manual mode (GUI is enabled if and only if we are in manual mode).
     * @param pManualMode true for manual mode, false otherwise
     */
    public void setManualMode(boolean pManualMode)
    {
        mManualMode = pManualMode;
        updateSubPanelsStatus(pManualMode);
    }

    /**
     * Returns the list of check nodes to expand all the time.
     * @return a collections of nodes
     */
    public Collection<String> getAutoExpandedCheckNodes()
    {
        return Collections.unmodifiableCollection(mAutoExpandedCheckNodes);
    }

    private void buildPanel()
    {
        setLayout(new GridBagLayout());

        Border border = BorderFactory.createLineBorder(Color.lightGray);
        GridBagConstraints c = new GridBagConstraints();
        Font bigFont = new Font(Font.DIALOG, Font.BOLD, 16);
        Font bigTitle = new Font(Font.DIALOG, Font.BOLD, 14);

        // ---------------------------------------------------------- 1 - Details
        JPanel detailsPanel = new JPanel();
        TitledBorder title = new TitledBorder("Details");
        title.setTitleFont(bigTitle);
        title.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.lightGray));
        detailsPanel.setBorder(title);
        detailsPanel.setLayout(new GridBagLayout());

        mSelectBeamlinePanel = new SelectBeamlinePanel(this);
        mSetRangePanel = new SetRangePanel(this);
        mDevicesPanel = new DevicesPanel();

        //mCheckManagerPanel = new CheckManagerPanel(mCyclicCheckManager);
        //mCheckManagerPanel.setAutoExpandedNodes(mAutoExpandedCheckNodes);

        mLogPanel = new LogPanel();
        mLogPanel.setLoggerName("com.iba.pts.bms.bss.esbts");
        mLogPanel.afterPropertiesSet();

        mInfoPanel = new InfoPanel();

        mPowerSavingPanel = new PowerSavingPanel();

        // update devices panel when a manual select beamline is done
        mSelectBeamlinePanel.getStartButton().addActionListener(pArg0 -> mDevicesPanel.updatePanel(mCurrentAllocatedBeamline,
                (TreatmentMode) mSelectBeamlinePanel.getTreatmentModeComboBox().getSelectedItem()));

        mDevicesPanel.cleanupPanel();
        // mCyclicChecksPanel.setBeamlines(mBeamlines);
        // mCyclicChecksPanel.setCyclicCheckManager(mCyclicCheckManager);

        mControlPanel = new JTabbedPane();
        mControlPanel.setFont(bigTitle);
        mControlPanel.add("Idle", mDevicesPanel);
        mControlPanel.add(PANEL_TITLE_SET_RANGE, mSetRangePanel);
        mControlPanel.add(PANEL_TITLE_SELECT_BEAMLINE, mSelectBeamlinePanel);
        mControlPanel.add("Tips", mInfoPanel);
        //mControlPanel.add(PANEL_TITLE_SET_RANGE, mSetRangePanel);
        //mControlPanel.add(PANEL_TITLE_DEVICES, mDevicesPanel);
        //mControlPanel.add(PANEL_TITLE_CHECKS, mCheckManagerPanel);
        //mControlPanel.add(PANEL_TITLE_LOG, mLogPanel);
        //mControlPanel.add(PANEL_TITLE_POWER_SAVING, mPowerSavingPanel);

        // show devices panel
        mControlPanel.setSelectedIndex(PANEL_INDEX_DEVICES);

        mControlPanel.getAccessibleContext().addPropertyChangeListener(new com.iba.ialign.EsbtsPanel.EsbtsTabPagesListener());

        c.anchor = GridBagConstraints.NORTH;
        c.fill = GridBagConstraints.BOTH;
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.1;
        c.weighty = 0.1;
        detailsPanel.add(mControlPanel, c);

        c.anchor = GridBagConstraints.NORTH;
        c.fill = GridBagConstraints.BOTH;
        c.gridx = 1;
        c.gridy = 0;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.8;
        c.weighty = 0.1;
        add(detailsPanel, c);

        // ---------------------------------------------------------- 2 - Actions
        mActionsPanel = new ActionsPanel(mControlPanel, this);
        title = new TitledBorder("Actions");
        title.setTitleFont(bigTitle);
        title.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.lightGray));
        mActionsPanel.setBorder(title);

        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.2;
        c.weighty = 0.1;
        add(mActionsPanel, c);
    }

    @Override
    public void propertyChange(PropertyChangeEvent pEvent)
    {
        if (pEvent.getPropertyName().equals(ActivityController.CURRENT_ACTIVITY_NAME_PROPERTY))
        {
            mCurrentActivityName = (EsBtsActivityId) pEvent.getNewValue();
            switchTabPage();
          //  updateSubPanelsStatus();
        }
        else if (pEvent.getPropertyName().equals(ActivityController.CURRENT_ACTIVITY_STATUS_PROPERTY))
        {
            mCurrentActivityStatus = (ActivityStatus) pEvent.getNewValue();
            if (mCurrentActivityName == EsBtsActivityId.SELECT_BEAMLINE && mCurrentActivityStatus == ActivityStatus.COMPLETED)
            {
                mIsBeamlineSelected = true;
            }
            updateActionLabelStatus();
           // updateSubPanelsStatus();
        }
        else if (pEvent.getPropertyName().equals(BeamScheduler.CURRENT_BEAM_ALLOCATION_PROPERTY))
        {
            BeamAllocation beamAllocation = (BeamAllocation) pEvent.getNewValue();
            String newCurrentAllocatedBeamSupplyPointId = beamAllocation != null ? beamAllocation.getBeamSupplyPointId() : null;

            // treat pseudo beam supply point for maintenance as beam not allocated
            if (BeamSchedulerPanel.PSEUDO_BEAM_SUPPLY_POINT_FOR_MAINTENANCE.equals(newCurrentAllocatedBeamSupplyPointId))
            {
                newCurrentAllocatedBeamSupplyPointId = null;
            }

            if (!Objects.equals(newCurrentAllocatedBeamSupplyPointId, getCurrentAllocatedBeamSupplyPointId()))
            {
                //mCurrentAllocatedBeamline = mBeamlines.getBeamline(newCurrentAllocatedBeamSupplyPointId);
                BeamDeliveryPoint bdp = Controller.feedbackClient.tc.getBeamSupplyPoint(Controller.beam.beamScheduler.getCurrentBeamAllocation().getBeamSupplyPointId()).getBeamDeliveryPoint();
                mIsBeamlineSelected = false;
                //updateSubPanelsStatus();
                mDevicesPanel.updatePanel(mCurrentAllocatedBeamline, null);
                mSelectBeamlinePanel.updatePanel(bdp);
                //mSetRangePanel.updatePanel(mCurrentAllocatedBeamline);
                mCheckManagerPanel.updatePanel(mCurrentAllocatedBeamline);
                System.out.println("New supply point " + mCurrentAllocatedBeamline);
            }
        }
    }

    private void updateActionLabelStatus()
    {
        JLabel currentActivityLabel = null;
        switch (mCurrentActivityName)
        {
            case IDLE:
                currentActivityLabel = mActionsPanel.getIdleStatusLabel();
                break;
            case SELECT_BEAMLINE:
                currentActivityLabel = mActionsPanel.getSelectBeamlineStatusLabel();
                break;
            case SET_RANGE:
                currentActivityLabel = mActionsPanel.getSetRangeStatusLabel();
                break;
            case ENABLE_BEAM:
                currentActivityLabel = mActionsPanel.getEnableBeamStatusLabel();
                break;
            case DISABLE_BEAM:
                currentActivityLabel = mActionsPanel.getDisableBeamStatusLabel();
                break;
            default:
        }

        if (currentActivityLabel != null)
        {
            switch (mCurrentActivityStatus)
            {
                case ONGOING:
                case CANCELING:
                    currentActivityLabel.setIcon(ResourceManager.getInstance().getImageIcon("bms/bss/controller/ui/running"));
                    break;

                case ERROR:
                case CANCELED:
                    currentActivityLabel.setIcon(ResourceManager.getInstance().getImageIcon("bms/bss/controller/ui/bad"));
                    break;
                case DISABLED:
                    currentActivityLabel.setIcon(ResourceManager.getInstance().getImageIcon("bms/bss/controller/ui/inactive"));
                    break;

                case COMPLETED:
                case IDLE:
                    currentActivityLabel.setIcon(ResourceManager.getInstance().getImageIcon("bms/bss/controller/ui/good"));
                    break;
            }
        }
    }

    public void updateSubPanelsStatus(boolean readyForServiceBeam)
    {
        boolean allocated = Controller.isBeamAllocated();
        mCurrentActivityStatus = Controller.beam.bssController.getCurrentActivityStatus();
        boolean inProgress = mCurrentActivityStatus == ActivityStatus.ONGOING || mCurrentActivityStatus == ActivityStatus.CANCELING;
        mManualMode = Controller.beam.bssController.getOperatingMode().equals(BssController.OperatingMode.MANUAL);
        boolean enabled = readyForServiceBeam && !inProgress;
        //enabled = true;
        //updateActionButtonStatus(enabled);
        //mIsBeamlineSelected = true;
        updateSetRangePanel(enabled);
        //mPowerSavingPanel.setEnableManualPowerSave(mManualMode && !inProgress);
       // updateSelectBeamlinePanel(enabled);
    }

    public void updateActionButtonStatus(boolean pEnabled)
    {
        mActionsPanel.getIdleButton().setEnabled(pEnabled);
        mActionsPanel.getEnableBeamButton().setEnabled(pEnabled);
        mActionsPanel.getDisableBeamButton().setEnabled(pEnabled);
        mActionsPanel.getSelectBeamlineButton().setEnabled(pEnabled && !mSelectBeamlinePanel.isShowing());
        //mActionsPanel.getSetRangeButton().setEnabled(pEnabled && !mSetRangePanel.isShowing() && mIsBeamlineSelected);
        mActionsPanel.getSetRangeButton().setEnabled(pEnabled && !mSetRangePanel.isShowing());
    }

    private void updateSelectBeamlinePanel(boolean pEnabled)
    {
        //JComboBox treatmentModeComboBox = mSelectBeamlinePanel.getTreatmentModeComboBox();
        //treatmentModeComboBox.setEnabled(pEnabled && treatmentModeComboBox.getItemCount() > 0);
        mSelectBeamlinePanel.getStartButton().setEnabled(pEnabled);
    }

    private void updateSetRangePanel(boolean pEnabled)
    {
        mSetRangePanel.getRangeTextField().setEnabled(pEnabled);
        mSetRangePanel.getOpticalSolutionComboBox().setEnabled(pEnabled);
        mSetRangePanel.getGantryAngleTextField().setEnabled(pEnabled);
        mSetRangePanel.getOffsetXTextField().setEnabled(pEnabled);
        mSetRangePanel.getOffsetYTextField().setEnabled(pEnabled);
        mSetRangePanel.getCyclingCheckBox().setEnabled(pEnabled);
        //mSetRangePanel.getStartButton().setEnabled(pEnabled);
    }

    private void switchTabPage()
    {
        switch (mCurrentActivityName)
        {
            case SELECT_BEAMLINE:
                mControlPanel.setSelectedIndex(PANEL_INDEX_SELECT_BEAMLINE);
                break;
            case SET_RANGE:
                mControlPanel.setSelectedIndex(PANEL_INDEX_SET_RANGE);
                break;
            default:
                // show devices panel
                mControlPanel.setSelectedIndex(PANEL_INDEX_DEVICES);
        }
    }

    public ActionsPanel getActionsPanel()
    {
        return mActionsPanel;
    }

    public SetRangePanel getSetRangePanel()
    {
        return mSetRangePanel;
    }

    public SelectBeamlinePanel getSelectBeamlinePanel()
    {
        return mSelectBeamlinePanel;
    }

    public DevicesPanel getMagnetsPanel()
    {
        return mDevicesPanel;
    }

    public CheckManagerPanel getCheckManagerPanel()
    {
        return mCheckManagerPanel;
    }

    public LogPanel getLogPanel()
    {
        return mLogPanel;
    }

    public String getCurrentAllocatedBeamSupplyPointId()
    {
        if (mCurrentAllocatedBeamline == null)
        {
            return null;
        }
        return mCurrentAllocatedBeamline.getBeamSupplyPointId();
    }

    public void setEsBtsController(EsBtsController pEsBtsController)
    {
        mEsBtsController = pEsBtsController;

        mCurrentActivityName = mEsBtsController.getCurrentActivityName();
        mCurrentActivityStatus = mEsBtsController.getCurrentActivityStatus();

        mEsBtsController.addPropertyChangeListener(this);
    }

    public EsBtsController getEsBtsController()
    {
        return mEsBtsController;
    }

    public void setBssControllerLocal(BssControllerLocal pBssController)
    {
        pBssController.addBssControllerListener(new com.iba.ialign.EsbtsPanel.MyBssControllerListener());
    }

    private class EsbtsTabPagesListener implements PropertyChangeListener
    {

        @Override
        public void propertyChange(PropertyChangeEvent pEvent)
        {
            if (pEvent.getPropertyName().equals(AccessibleContext.ACCESSIBLE_NAME_PROPERTY))
            {
                //updateSubPanelsStatus();
            }
        }
    }

    public CallbackUtility getCallbackUtility()
    {
        return mCallbackUtility;
    }

    public void setCallbackUtility(CallbackUtility pCallbackUtility)
    {
        mCallbackUtility = pCallbackUtility;
    }

    public BeamlinesInfrastructure getBeamlineInfra()
    {
        return mBeamlines;
    }

    public TherapyCentre getTherapyCentre()
    {
        return mTherapyCentre;
    }

    private class MyBssControllerListener implements BssControllerListener
    {

        @Override
        public void startDisableBeamActivityRequested(String pBeamSupplyPointId)
        {
            // nothing to do
        }

        @Override
        public void startEnableBeamActivityRequested(String pBeamSupplyPointId)
        {
            // nothing to do
        }

        @Override
        public void startIdleActivityRequested()
        {
            // nothing to do
        }

        @Override
        public void startPrepareActivityRequested(final String pBeamSupplyPointId, final BssSettings pSettings,
                                                  final double pOffsetX, final double pOffsetY, final boolean pCycling)
        {
            SwingUtilities.invokeLater(() -> {
                mSelectBeamlinePanel.updateTreatmentMode(pSettings.getTreatmentMode());
                mSetRangePanel.updateRangeInfo(pSettings.getRangeAtNozzle(), pSettings.getOpticalSolution(),
                        pSettings.getGantryAngle(), pOffsetX, pOffsetY, pCycling);
                mDevicesPanel.updatePanel(mCurrentAllocatedBeamline, pSettings.getTreatmentMode());
            });
        }

        @Override
        public void startBpsPrepareActivityRequested(String pBeamSupplyPointId, TreatmentMode pTreatmentMode,
                                                     double pBeamCurrentAtCycloExit, int[] pBcmFile, int pStartDigit, int pStopDigit, String pBcmId)
        {
            // Nothing to do
        }

        @Override
        public void startSetRangeActivityRequested(final String pBeamSupplyPointId, final double pRange,
                                                   final String pOpticalSolution, final double pGantryAngle, final double pOffsetX, final double pOffsetY,
                                                   final boolean pCycling)
        {
            SwingUtilities.invokeLater(
                    () -> mSetRangePanel.updateRangeInfo(pRange, pOpticalSolution, pGantryAngle, pOffsetX, pOffsetY, pCycling));
        }

        @Override
        public void emergencyDisableBeamRequested(String pBeamSupplyPointId)
        {
            // nothing to do
        }

        @Override
        public void errorOccurred(BssActivityId pActivityId, String pDescription)
        {
            // nothing to do
        }
    }

    protected static final Logger LOGGER = Logger.getLogger();

    public static final int PANEL_INDEX_SELECT_BEAMLINE = 1;
    public static final int PANEL_INDEX_SET_RANGE = 2;
    public static final int PANEL_INDEX_DEVICES = 0;
    public static final int PANEL_INDEX_CHECKS = 3;
    public static final int PANEL_INDEX_LOG = 4;

    private static final String PANEL_TITLE_SELECT_BEAMLINE = "Prepare";
    private static final String PANEL_TITLE_SET_RANGE = "Set Range";
    private static final String PANEL_TITLE_DEVICES = "Devices";
    private static final String PANEL_TITLE_CHECKS = "Checks";
    private static final String PANEL_TITLE_LOG = "Log";
    private static final String PANEL_TITLE_POWER_SAVING = "Power Saving";

    private TherapyCentre mTherapyCentre;
    private Beamline mCurrentAllocatedBeamline;
    public BeamDeliveryPoint mBeamDeliveryPoint;
    public boolean mIsBeamlineSelected;
    private EsBtsController mEsBtsController;
    private EsBtsActivityId mCurrentActivityName;
    private ActivityStatus mCurrentActivityStatus;
    private BeamlinesInfrastructure mBeamlines;
    private CheckManager mCyclicCheckManager;
    private boolean mManualMode;

    private JTabbedPane mControlPanel;
    public ActionsPanel mActionsPanel;
    public SelectBeamlinePanel mSelectBeamlinePanel;
    private SetRangePanel mSetRangePanel;
    public DevicesPanel mDevicesPanel;
    private CheckManagerPanel mCheckManagerPanel;
    private LogPanel mLogPanel;
    private PowerSavingPanel mPowerSavingPanel;
    private InfoPanel mInfoPanel;

    private CallbackUtility mCallbackUtility;

    private Collection<String> mAutoExpandedCheckNodes = Collections.emptySet();
}

