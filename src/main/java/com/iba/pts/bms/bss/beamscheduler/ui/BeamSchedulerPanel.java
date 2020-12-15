// ///////////////////////////////////////////////////////////////////
// Copyright (c) by Ion Beam Applications S.A.
// All rights reserved
//
// Rue du cyclotron, 3
// B-1348 Louvain-la-Neuve
// Tel : +32.10.47.58.11
// ///////////////////////////////////////////////////////////////////

package com.iba.pts.bms.bss.beamscheduler.ui;

import com.iba.ialign.Controller;
import com.iba.ialign.Gui;
import com.iba.icomp.core.util.Logger;
import com.iba.icompx.ui.i18n.ResourceDictionary;
import com.iba.icompx.ui.util.ResourceManager;
import com.iba.pts.bms.bss.beamscheduler.api.BeamAllocation;
import com.iba.pts.bms.bss.beamscheduler.api.BeamPriority;
import com.iba.pts.bms.bss.beamscheduler.api.BeamScheduler;
import com.iba.pts.bms.bss.beamscheduler.api.BeamSchedulerControl;
import com.iba.pts.bms.bss.beamscheduler.api.PendingBeamRequest;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import org.python.google.common.collect.ImmutableSet;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

/**
 * The main control panel of Beam Scheduling.
 */
@SuppressWarnings("serial")
public class BeamSchedulerPanel extends JPanel implements PropertyChangeListener
{
    public static final String PSEUDO_BEAM_SUPPLY_POINT_FOR_MAINTENANCE = "System Maintenance";

    /**
     * Constructor.
     * @param pBeamScheduler Swing-safe and application queue interceptor proxy to beam scheduler
     * @param pBeamSchedulerControl Swing-safe and application queue interceptor proxy to beam scheduler control interface
     */
    public BeamSchedulerPanel(BeamScheduler pBeamScheduler, BeamSchedulerControl pBeamSchedulerControl)
    {
        mBeamScheduler = pBeamScheduler;
        mBeamScheduler.addPropertyChangeListener(this);
        mBeamSchedulerControl = pBeamSchedulerControl;
        buildPanel();
    }

    /**
     * Set manual mode (some GUI widgets are enabled if and only if we are in manual mode).
     * @param pManualMode true for manual mode, false otherwise
     */
    public void setManualMode(boolean pManualMode)
    {
        mManualMode = pManualMode;
        updateMaintenanceButtons();
        mAllowManualBeamRequestRelease.setEnabled(pManualMode);
        setBeamRequestEnabled(pManualMode && mAllowManualBeamRequestRelease.isSelected());
        updateAllocatedInfo();
    }

    private void buildPanel()
    {
        Border border = BorderFactory.createLineBorder(Color.lightGray);

        setLayout(new GridBagLayout());

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        // ---------------------------------------------------------- TR Room
        JPanel maintenancePanel = new JPanel();
        maintenancePanel.setBorder(new TitledBorder(border, "Maintenance"));

        FormLayout maintenanceLayout = new FormLayout("pref:grow,3dlu,pref:grow,3dlu,pref", "pref:grow");
        final PanelBuilder maintenanceBuilder = new PanelBuilder(maintenanceLayout, maintenancePanel);

        mStartMaintenanceButton = new JButton();
        mStopMaintenanceButton = new JButton();

        mStartMaintenanceButton.setAction(new AbstractAction("Start")
        {
            @Override
            public void actionPerformed(ActionEvent pEvent)
            {
                mBeamScheduler.requestBeam(PSEUDO_BEAM_SUPPLY_POINT_FOR_MAINTENANCE, BeamPriority.Service, 10000, null);
                updateMaintenanceButtons();
            }
        });

        mStopMaintenanceButton.setAction(new AbstractAction("Stop")
        {
            @Override
            public void actionPerformed(ActionEvent pEvent)
            {
                BeamAllocation ba = mBeamScheduler.getCurrentBeamAllocation();
                if (ba != null && PSEUDO_BEAM_SUPPLY_POINT_FOR_MAINTENANCE.equals(ba.getBeamSupplyPointId()))
                {
                    mBeamScheduler.releaseBeam(PSEUDO_BEAM_SUPPLY_POINT_FOR_MAINTENANCE, false);
                }
                else
                {
                    for (PendingBeamRequest pbr : mBeamScheduler.getPendingBeamRequests())
                    {
                        if (PSEUDO_BEAM_SUPPLY_POINT_FOR_MAINTENANCE.equals(pbr.getBeamSupplyPointId()))
                        {
                            mBeamScheduler.cancelBeamRequest(PSEUDO_BEAM_SUPPLY_POINT_FOR_MAINTENANCE);
                        }
                    }
                }
                updateMaintenanceButtons();
            }
        });

        updateMaintenanceButtons();

        mAllowManualBeamRequestRelease = new JCheckBox();

        mAllowManualBeamRequestRelease.setAction(new AbstractAction("Allow Manual Beam Request/Release")
        {
            @Override
            public void actionPerformed(ActionEvent pEvent)
            {
                setBeamRequestEnabled(mAllowManualBeamRequestRelease.isSelected());
                updateAllocatedInfo();
            }
        });
        mAllowManualBeamRequestRelease.setSelected(false);

        maintenanceBuilder.add(mStartMaintenanceButton);
        maintenanceBuilder.nextColumn(2);
        maintenanceBuilder.add(mStopMaintenanceButton);
        maintenanceBuilder.nextColumn(2);
        maintenanceBuilder.add(mAllowManualBeamRequestRelease);

        c.fill = GridBagConstraints.BOTH;
        c.anchor = GridBagConstraints.NORTH;
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 2;
        c.gridheight = 1;
        c.weightx = 0.1;
        c.weighty = 0.1;
        mainPanel.add(maintenancePanel, c);

        // ---------------------------------------------------------- Queue Management
        JPanel queuePanel = createQueuePanel();

        updatePendingList();

        c.fill = GridBagConstraints.BOTH;
        c.anchor = GridBagConstraints.NORTHWEST;
        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth = 1;
        c.gridheight = 3;
        c.weightx = 0.1;
        c.weighty = 0.1;
        mainPanel.add(queuePanel, c);

        // ---------------------------------------------------------- Beam Request
        JPanel beamRequestPanel = createBeamRequestPanel();

        c.fill = GridBagConstraints.BOTH;
        c.anchor = GridBagConstraints.NORTHWEST;
        c.gridx = 1;
        c.gridy = 1;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.5;
        c.weighty = 0.1;
        mainPanel.add(beamRequestPanel, c);

        // ---------------------------------------------------------- Allocated
        JPanel allocatedPanel = createAllocatedPanel();

        c.fill = GridBagConstraints.BOTH;
        c.anchor = GridBagConstraints.NORTHWEST;
        c.gridx = 1;
        c.gridy = 2;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.5;
        c.weighty = 0.1;
        mainPanel.add(allocatedPanel, c);

        // ---------------------------------------------------------- Beam Scheduler Mode
        JPanel beamSchedulerModePanel = createModePanel();

        c.fill = GridBagConstraints.BOTH;
        c.anchor = GridBagConstraints.NORTHWEST;
        c.gridx = 1;
        c.gridy = 3;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.5;
        c.weighty = 0.1;
        mainPanel.add(beamSchedulerModePanel, c);

        // ---------------------------------------------------------- Flush Operation
        JPanel flushOperationPanel = createFlushOperationPanel();

        c.fill = GridBagConstraints.BOTH;
        c.anchor = GridBagConstraints.NORTH;
        c.gridx = 0;
        c.gridy = 4;
        c.gridwidth = 2;
        c.gridheight = 1;
        c.weightx = 0.1;
        c.weighty = 0.1;
        mainPanel.add(flushOperationPanel, c);

        c.fill = GridBagConstraints.BOTH;
        c.anchor = GridBagConstraints.NORTH;
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.1;
        c.weighty = 0.1;
        add(mainPanel, c);

        updateBeamSchedulerMode();
    }

    public JPanel createQueuePanel() {
        JPanel queuePanel = new JPanel();
        queuePanel.setBorder(new LineBorder(Color.lightGray));

        FormLayout queueLayout = new FormLayout("pref:grow,3dlu,pref:grow,3dlu,pref:grow,3dlu,pref,3dlu,pref",
                "pref,3dlu,pref:grow,3dlu,pref:grow,3dlu,pref:grow,3dlu,pref:grow,3dlu,pref:grow,3dlu,pref:grow");
        final PanelBuilder queueBuilder = new PanelBuilder(queueLayout, queuePanel);

        JLabel pendingLabel = new JLabel(RES.getString("bms.beam_scheduler.pending"), SwingConstants.LEFT);
        pendingLabel.setOpaque(true);
        pendingLabel.setBackground(Color.black);
        pendingLabel.setForeground(Color.white);

        JLabel pendingRoomLabel = new JLabel(RES.getString("bms.beam_scheduler.room"), SwingConstants.CENTER);
        JLabel pendingEtaLabel = new JLabel(RES.getString("bms.beam_scheduler.eta"), SwingConstants.CENTER);
        JLabel pendingPriorityLabel = new JLabel(RES.getString(BMS_BEAM_SCHEDULER_PRIORITY), SwingConstants.CENTER);

        JLabel mPending1RoomLabel = new JLabel(" ", SwingConstants.CENTER);
        mPending1RoomLabel.setBorder(new LineBorder(Color.lightGray));
        mPending1RoomLabel.setPreferredSize(new Dimension(60, 40));
        JLabel mPending2RoomLabel = new JLabel(" ", SwingConstants.CENTER);
        mPending2RoomLabel.setBorder(new LineBorder(Color.lightGray));
        mPending2RoomLabel.setPreferredSize(new Dimension(60, 40));
        JLabel mPending3RoomLabel = new JLabel(" ", SwingConstants.CENTER);
        mPending3RoomLabel.setBorder(new LineBorder(Color.lightGray));
        mPending3RoomLabel.setPreferredSize(new Dimension(60, 40));
        JLabel mPending4RoomLabel = new JLabel(" ", SwingConstants.CENTER);
        mPending4RoomLabel.setBorder(new LineBorder(Color.lightGray));
        mPending4RoomLabel.setPreferredSize(new Dimension(60, 40));
        JLabel mPending5RoomLabel = new JLabel(" ", SwingConstants.CENTER);
        mPending5RoomLabel.setBorder(new LineBorder(Color.lightGray));
        mPending5RoomLabel.setPreferredSize(new Dimension(60, 40));

        JLabel mPending1ETALabel = new JLabel(" ", SwingConstants.CENTER);
        mPending1ETALabel.setBorder(new LineBorder(Color.lightGray));
        mPending1ETALabel.setPreferredSize(new Dimension(20, 40));
        JLabel mPending2ETALabel = new JLabel(" ", SwingConstants.CENTER);
        mPending2ETALabel.setBorder(new LineBorder(Color.lightGray));
        mPending2ETALabel.setPreferredSize(new Dimension(20, 40));
        JLabel mPending3ETALabel = new JLabel(" ", SwingConstants.CENTER);
        mPending3ETALabel.setBorder(new LineBorder(Color.lightGray));
        mPending3ETALabel.setPreferredSize(new Dimension(20, 40));
        JLabel mPending4ETALabel = new JLabel(" ", SwingConstants.CENTER);
        mPending4ETALabel.setBorder(new LineBorder(Color.lightGray));
        mPending4ETALabel.setPreferredSize(new Dimension(20, 40));
        JLabel mPending5ETALabel = new JLabel(" ", SwingConstants.CENTER);
        mPending5ETALabel.setBorder(new LineBorder(Color.lightGray));
        mPending5ETALabel.setPreferredSize(new Dimension(20, 40));

        JLabel mPending1PriorityLabel = new JLabel(" ", SwingConstants.CENTER);
        mPending1PriorityLabel.setBorder(new LineBorder(Color.lightGray));
        mPending1PriorityLabel.setPreferredSize(new Dimension(40, 40));
        JLabel mPending2PriorityLabel = new JLabel(" ", SwingConstants.CENTER);
        mPending2PriorityLabel.setBorder(new LineBorder(Color.lightGray));
        mPending2PriorityLabel.setPreferredSize(new Dimension(40, 40));
        JLabel mPending3PriorityLabel = new JLabel(" ", SwingConstants.CENTER);
        mPending3PriorityLabel.setBorder(new LineBorder(Color.lightGray));
        mPending3PriorityLabel.setPreferredSize(new Dimension(40, 40));
        JLabel mPending4PriorityLabel = new JLabel(" ", SwingConstants.CENTER);
        mPending4PriorityLabel.setBorder(new LineBorder(Color.lightGray));
        mPending4PriorityLabel.setPreferredSize(new Dimension(40, 40));
        JLabel mPending5PriorityLabel = new JLabel(" ", SwingConstants.CENTER);
        mPending5PriorityLabel.setBorder(new LineBorder(Color.lightGray));
        mPending5PriorityLabel.setPreferredSize(new Dimension(40, 40));

        mPendingRoomLabels = new ArrayList<>();
        mPendingRoomLabels.add(mPending1RoomLabel);
        mPendingRoomLabels.add(mPending2RoomLabel);
        mPendingRoomLabels.add(mPending3RoomLabel);
        mPendingRoomLabels.add(mPending4RoomLabel);
        mPendingRoomLabels.add(mPending5RoomLabel);

        mPendingETALabels = new ArrayList<>();
        mPendingETALabels.add(mPending1ETALabel);
        mPendingETALabels.add(mPending2ETALabel);
        mPendingETALabels.add(mPending3ETALabel);
        mPendingETALabels.add(mPending4ETALabel);
        mPendingETALabels.add(mPending5ETALabel);

        mPendingPriorityLabels = new ArrayList<>();
        mPendingPriorityLabels.add(mPending1PriorityLabel);
        mPendingPriorityLabels.add(mPending2PriorityLabel);
        mPendingPriorityLabels.add(mPending3PriorityLabel);
        mPendingPriorityLabels.add(mPending4PriorityLabel);
        mPendingPriorityLabels.add(mPending5PriorityLabel);

        mQueue1AcceptButton = new JButton(RES.getString("bms.beam_scheduler.accept"));

        mQueue1AcceptButton.addActionListener(pE -> {
            List<PendingBeamRequest> reqs = mBeamScheduler.getPendingBeamRequests();

            if (!reqs.isEmpty())
            {
                mBeamSchedulerControl.acceptBeamRequest(reqs.get(0).getBeamSupplyPointId());
            }
        });

        String rejectLabelString = RES.getString("bms.beam_scheduler.reject");
        mQueue1RejectButton = new JButton(rejectLabelString);

        mQueue1RejectButton.addActionListener(pE -> rejectPendingRequest(0));

        mQueue2RejectButton = new JButton(rejectLabelString);

        mQueue2RejectButton.addActionListener(pE -> rejectPendingRequest(1));

        mQueue3RejectButton = new JButton(rejectLabelString);

        mQueue3RejectButton.addActionListener(pE -> rejectPendingRequest(2));

        mQueue4RejectButton = new JButton(rejectLabelString);

        mQueue4RejectButton.addActionListener(pE -> rejectPendingRequest(3));

        mQueue5RejectButton = new JButton(rejectLabelString);

        mQueue5RejectButton.addActionListener(pE -> rejectPendingRequest(4));

        mQueue2PlusButton = new JButton("+");

        mQueue2PlusButton.addActionListener(pArg0 -> stepUpPendingRequest(1));

        mQueue3PlusButton = new JButton("+");

        mQueue3PlusButton.addActionListener(pArg0 -> stepUpPendingRequest(2));

        mQueue4PlusButton = new JButton("+");

        mQueue4PlusButton.addActionListener(pArg0 -> stepUpPendingRequest(3));

        mQueue5PlusButton = new JButton("+");

        mQueue5PlusButton.addActionListener(pArg0 -> stepUpPendingRequest(4));

        Insets zeroInsets = new Insets(0, 0, 0, 0);
        mQueue1AcceptButton.setMargin(zeroInsets);
        mQueue1RejectButton.setMargin(zeroInsets);
        mQueue2RejectButton.setMargin(zeroInsets);
        mQueue3RejectButton.setMargin(zeroInsets);
        mQueue4RejectButton.setMargin(zeroInsets);
        mQueue5RejectButton.setMargin(zeroInsets);
        mQueue2PlusButton.setMargin(zeroInsets);
        mQueue3PlusButton.setMargin(zeroInsets);
        mQueue4PlusButton.setMargin(zeroInsets);
        mQueue5PlusButton.setMargin(zeroInsets);

        mQueue1AcceptButton.setPreferredSize(new Dimension(48, 40));
        mQueue1RejectButton.setPreferredSize(new Dimension(48, 40));
        mQueue2RejectButton.setPreferredSize(new Dimension(48, 40));
        mQueue3RejectButton.setPreferredSize(new Dimension(48, 40));
        mQueue4RejectButton.setPreferredSize(new Dimension(48, 40));
        mQueue5RejectButton.setPreferredSize(new Dimension(48, 40));
        mQueue2PlusButton.setPreferredSize(new Dimension(48, 40));
        mQueue3PlusButton.setPreferredSize(new Dimension(48, 40));
        mQueue4PlusButton.setPreferredSize(new Dimension(48, 40));
        mQueue5PlusButton.setPreferredSize(new Dimension(48, 40));

        CellConstraints cc = new CellConstraints();
        queueBuilder.add(pendingLabel, cc.rchw(1, 1, 1, 9));
        queueBuilder.add(pendingRoomLabel, cc.rchw(3, 1, 1, 1));
        queueBuilder.add(pendingEtaLabel, cc.rchw(3, 3, 1, 1));
        queueBuilder.add(pendingPriorityLabel, cc.rchw(3, 5, 1, 1));

        queueBuilder.add(mPending1RoomLabel, cc.rchw(5, 1, 1, 1));
        queueBuilder.add(mPending1ETALabel, cc.rchw(5, 3, 1, 1));
        queueBuilder.add(mPending1PriorityLabel, cc.rchw(5, 5, 1, 1));
        //queueBuilder.add(mQueue1AcceptButton, cc.rchw(5, 7, 1, 1));
        //queueBuilder.add(mQueue1RejectButton, cc.rchw(5, 9, 1, 1));

        queueBuilder.add(mPending2RoomLabel, cc.rchw(7, 1, 1, 1));
        queueBuilder.add(mPending2ETALabel, cc.rchw(7, 3, 1, 1));
        queueBuilder.add(mPending2PriorityLabel, cc.rchw(7, 5, 1, 1));
        //queueBuilder.add(mQueue2PlusButton, cc.rchw(7, 7, 1, 1));
        //queueBuilder.add(mQueue2RejectButton, cc.rchw(7, 9, 1, 1));

        queueBuilder.add(mPending3RoomLabel, cc.rchw(9, 1, 1, 1));
        queueBuilder.add(mPending3ETALabel, cc.rchw(9, 3, 1, 1));
        queueBuilder.add(mPending3PriorityLabel, cc.rchw(9, 5, 1, 1));
        //queueBuilder.add(mQueue3PlusButton, cc.rchw(9, 7, 1, 1));
        //queueBuilder.add(mQueue3RejectButton, cc.rchw(9, 9, 1, 1));

        queueBuilder.add(mPending4RoomLabel, cc.rchw(11, 1, 1, 1));
        queueBuilder.add(mPending4ETALabel, cc.rchw(11, 3, 1, 1));
        queueBuilder.add(mPending4PriorityLabel, cc.rchw(11, 5, 1, 1));
        //queueBuilder.add(mQueue4PlusButton, cc.rchw(11, 7, 1, 1));
        //queueBuilder.add(mQueue4RejectButton, cc.rchw(11, 9, 1, 1));

        queueBuilder.add(mPending5RoomLabel, cc.rchw(13, 1, 1, 1));
        queueBuilder.add(mPending5ETALabel, cc.rchw(13, 3, 1, 1));
        queueBuilder.add(mPending5PriorityLabel, cc.rchw(13, 5, 1, 1));
        //queueBuilder.add(mQueue5PlusButton, cc.rchw(13, 7, 1, 1));
        //queueBuilder.add(mQueue5RejectButton, cc.rchw(13, 9, 1, 1));

        return queuePanel;
    }

    private void rejectPendingRequest(int pIndex)
    {
        List<PendingBeamRequest> reqs = mBeamScheduler.getPendingBeamRequests();

        if (pIndex < reqs.size())
        {
            mBeamSchedulerControl.rejectBeamRequest(reqs.get(pIndex).getBeamSupplyPointId());
        }
    }

    private void stepUpPendingRequest(int pIndex)
    {
        List<PendingBeamRequest> reqs = mBeamScheduler.getPendingBeamRequests();

        if (pIndex < reqs.size())
        {
            mBeamSchedulerControl.stepUpBeamRequest(reqs.get(pIndex).getBeamSupplyPointId());
        }
    }

    public JPanel createBeamRequestPanel()
    {
        JPanel beamRequestPanel = new JPanel();
        beamRequestPanel.setBorder(new LineBorder(Color.lightGray));

        FormLayout beamRequestLayout = new FormLayout("pref:grow,3dlu,pref:grow",
                "pref,3dlu,pref:grow,3dlu,pref:grow,3dlu,pref:grow");
        final PanelBuilder beamRequestBuilder = new PanelBuilder(beamRequestLayout, beamRequestPanel);

        mBeamRequestLabel = new JLabel("Beam Request", SwingConstants.LEFT);
        mBeamRequestLabel.setOpaque(true);
        mBeamRequestLabel.setBackground(Color.black);
        mBeamRequestLabel.setForeground(Color.white);
        mBeamRequestLabel.setEnabled(false);

        mBeamSupplyIdsComboBox = new JComboBox(this.getBeamSupplyPointsIds().toArray());
        mBeamSupplyIdsComboBox.setEnabled(false);

        mBeamPrioritiesComboBox = new JComboBox(BeamPriority.values());
        mBeamPrioritiesComboBox.setSelectedItem(BeamPriority.Normal);
        mBeamPrioritiesComboBox.setEnabled(false);

        mRequestedTimeLabel = new JLabel("Expected Requested Time (s)");
        mRequestedTimeLabel.setEnabled(false);

        mRequestedTimeTextField = new JFormattedTextField();
        final DecimalFormat requestedTimeFormat = (DecimalFormat) NumberFormat.getNumberInstance();
        requestedTimeFormat.applyPattern("0");
        mRequestedTimeTextField = new JFormattedTextField(requestedTimeFormat);
        mRequestedTimeTextField.setHorizontalAlignment(SwingConstants.CENTER);
        mRequestedTimeTextField.setValue(30);
        mRequestedTimeTextField.setEnabled(false);

        mRequestBeamButton = new JButton("Request Beam");
        mRequestBeamButton.setToolTipText("Places TR in the queue to be automatically accepted.");
        mRequestBeamButton.setPreferredSize(new Dimension(40, 30));
        mRequestBeamButton.setEnabled(false);

        mRequestBeamButton.addActionListener(pEvent -> {
            String bspId = (String) mBeamSupplyIdsComboBox.getSelectedItem();
            mLogger.info("Manual beam request for %s", bspId);
            mBeamScheduler.requestBeam(bspId, (BeamPriority) mBeamPrioritiesComboBox.getSelectedItem(),
                    ((Number) mRequestedTimeTextField.getValue()).intValue(), null);
        });

        CellConstraints cc = new CellConstraints();

        beamRequestBuilder.add(mBeamRequestLabel, cc.rchw(1, 1, 1, 3));
        beamRequestBuilder.add(mBeamSupplyIdsComboBox, cc.rchw(3, 1, 1, 1));
        beamRequestBuilder.add(mBeamPrioritiesComboBox, cc.rchw(3, 3, 1, 1));
        //beamRequestBuilder.add(mRequestedTimeLabel, cc.rchw(5, 1, 1, 1));
        //beamRequestBuilder.add(mRequestedTimeTextField, cc.rchw(5, 3, 1, 1));
        beamRequestBuilder.add(mRequestBeamButton, cc.rchw(5, 1, 1, 3));

        return beamRequestPanel;
    }

    private Set<String> getBeamSupplyPointsIds() {
        return ImmutableSet.of("IBTR3-90");
    }

    public JPanel createAllocatedPanel()
    {
        JPanel allocatedPanel = new JPanel();
        allocatedPanel.setBorder(new LineBorder(Color.lightGray));

        FormLayout allocatedLayout = new FormLayout("pref:grow,3dlu,pref:grow,3dlu,pref:grow,3dlu,pref:grow,3dlu,pref:grow",
                "pref,3dlu,pref:grow,3dlu,pref:grow,3dlu,pref:grow");
        final PanelBuilder allocatedBuilder = new PanelBuilder(allocatedLayout, allocatedPanel);

        JLabel allocatedLabel = new JLabel(RES.getString("bms.beam_scheduler.allocated"), SwingConstants.LEFT);
        allocatedLabel.setOpaque(true);
        allocatedLabel.setBackground(Color.black);
        allocatedLabel.setForeground(Color.white);

        JLabel allocatedBeamSupplyPointLabel = new JLabel("Beam Supply Point", SwingConstants.LEFT);
        mAllocatedRoomLabel = new JLabel("", SwingConstants.CENTER);
        mAllocatedRoomLabel.setBorder(new LineBorder(Color.BLACK));
        mAllocatedRoomLabel.setPreferredSize(new Dimension(40, 30));
        mAllocatedRoomLabel.setOpaque(true);
        mAllocatedRoomLabel.setBackground(Color.green);
        mAllocatedRoomLabel.setForeground(Color.BLACK);

        JLabel allocatedPriorityLabel = new JLabel(RES.getString(BMS_BEAM_SCHEDULER_PRIORITY), SwingConstants.LEFT);
        mAllocatedPriorityLabel = new JLabel("", SwingConstants.CENTER);
        mAllocatedPriorityLabel.setBorder(new LineBorder(Color.BLACK));
        mAllocatedPriorityLabel.setPreferredSize(new Dimension(40, 30));
        mAllocatedPriorityLabel.setOpaque(true);
        mAllocatedPriorityLabel.setBackground(Color.green);
        mAllocatedPriorityLabel.setForeground(Color.BLACK);

        mReleaseBeamButton = new JButton("Release Beam");
        mReleaseBeamButton.setToolTipText("Manually release TR from allocation.");
        mReleaseBeamButton.setPreferredSize(new Dimension(40, 30));
        mReleaseBeamButton.setEnabled(false);
        mReleaseBeamButton.addActionListener(pEvent -> {
            mLogger.info("Manual beam release");
            mBeamScheduler.releaseBeam(mBeamScheduler.getCurrentBeamAllocation().getBeamSupplyPointId(), false);
        });

        CellConstraints cc = new CellConstraints();

        allocatedBuilder.add(allocatedLabel, cc.rchw(1, 1, 1, 9));
        allocatedBuilder.add(allocatedBeamSupplyPointLabel, cc.rchw(3, 1, 1, 1));
        allocatedBuilder.add(mAllocatedRoomLabel, cc.rchw(3, 3, 1, 7));
        allocatedBuilder.add(allocatedPriorityLabel, cc.rchw(5, 1, 1, 1));
        allocatedBuilder.add(mAllocatedPriorityLabel, cc.rchw(5, 3, 1, 7));
        allocatedBuilder.add(mReleaseBeamButton, cc.rchw(7, 1, 1, 9));

        updateAllocatedInfo();

        return allocatedPanel;
    }

    private void updateBeamSchedulerMode()
    {
        if (mBeamScheduler.isSchedulingAutomatic())
        {
            mBeamSchedulerModeLabel.setText(RES.getString("bms.beam_scheduler.mode.auto"));
            setButtonStatus(false);
        }
        else
        {
            mBeamSchedulerModeLabel.setText(RES.getString("bms.beam_scheduler.mode.manual"));
            setButtonStatus(true);
        }
    }

    private void setButtonStatus(boolean pEnabled)
    {
        mQueue1AcceptButton.setEnabled(pEnabled);
        mQueue1RejectButton.setEnabled(pEnabled);

        mQueue2RejectButton.setEnabled(pEnabled);
        mQueue3RejectButton.setEnabled(pEnabled);
        mQueue4RejectButton.setEnabled(pEnabled);
        mQueue5RejectButton.setEnabled(pEnabled);

        mQueue2PlusButton.setEnabled(pEnabled);
        mQueue3PlusButton.setEnabled(pEnabled);
        mQueue4PlusButton.setEnabled(pEnabled);
        mQueue5PlusButton.setEnabled(pEnabled);

        mFlushAllButton.setEnabled(pEnabled);
        mFlushServiceButton.setEnabled(pEnabled);
        mFlushNormalButton.setEnabled(pEnabled);
        mFlushHighButton.setEnabled(pEnabled);
    }

    private JPanel createModePanel()
    {
        JPanel beamSchedulerModePanel = new JPanel();
        beamSchedulerModePanel.setBorder(new LineBorder(Color.lightGray));

        FormLayout beamSchedulerModeLayout = new FormLayout("pref:grow,3dlu,pref:grow", "pref,3dlu,pref:grow");
        final PanelBuilder beamSchedulerModeBuilder = new PanelBuilder(beamSchedulerModeLayout, beamSchedulerModePanel);

        JLabel beamSchedulerModeLabel = new JLabel(RES.getString("bms.beam_scheduler.mode"), SwingConstants.LEFT);
        beamSchedulerModeLabel.setOpaque(true);
        beamSchedulerModeLabel.setBackground(Color.black);
        beamSchedulerModeLabel.setForeground(Color.white);

        mBeamSchedulerModeLabel = new JLabel("Unknown", SwingConstants.CENTER);
        mBeamSchedulerModeLabel.setOpaque(true);
        mBeamSchedulerModeLabel.setBackground(Color.lightGray);
        mBeamSchedulerModeLabel.setForeground(Color.white);
        mBeamSchedulerModeLabel.setPreferredSize(new Dimension(40, 30));

        JButton switchModeButton = new JButton(RES.getString("bms.beam_scheduler.switch_mode"));
        switchModeButton.setPreferredSize(new Dimension(40, 30));

        switchModeButton
                .addActionListener(pE -> mBeamSchedulerControl.setAutomaticScheduling(!mBeamScheduler.isSchedulingAutomatic()));

        CellConstraints cc = new CellConstraints();

        beamSchedulerModeBuilder.add(beamSchedulerModeLabel, cc.rchw(1, 1, 1, 3));
        beamSchedulerModeBuilder.add(mBeamSchedulerModeLabel, cc.rchw(3, 1, 1, 1));
        beamSchedulerModeBuilder.add(switchModeButton, cc.rchw(3, 3, 1, 1));

        return beamSchedulerModePanel;
    }

    private JPanel createFlushOperationPanel()
    {
        JPanel flushOperationPanel = new JPanel();
        flushOperationPanel.setBorder(new LineBorder(Color.lightGray));

        FormLayout flushOperationLayout = new FormLayout(
                "pref:grow,3dlu,pref:grow,3dlu,pref:grow,3dlu,pref:grow,3dlu,pref:grow,3dlu,pref:grow", "3dlu,pref:grow,3dlu");
        final PanelBuilder flushOperationBuilder = new PanelBuilder(flushOperationLayout, flushOperationPanel);

        mFlushAllButton = new JButton(RES.getString("bms.beam_scheduler.flush_all"));

        mFlushAllButton.addActionListener(pE -> mBeamSchedulerControl.cancelPendingBeamRequests());

        mFlushServiceButton = new JButton(RES.getString("bms.beam_scheduler.flush_service"));

        mFlushServiceButton.addActionListener(pE -> mBeamSchedulerControl.cancelPendingBeamRequests(BeamPriority.Service));

        mFlushNormalButton = new JButton(RES.getString("bms.beam_scheduler.flush_normal"));

        mFlushNormalButton.addActionListener(pE -> mBeamSchedulerControl.cancelPendingBeamRequests(BeamPriority.Normal));

        mFlushHighButton = new JButton(RES.getString("bms.beam_scheduler.flush_high"));

        mFlushHighButton.addActionListener(pE -> mBeamSchedulerControl.cancelPendingBeamRequests(BeamPriority.High));

        flushOperationBuilder.nextLine();
        flushOperationBuilder.add(mFlushAllButton);
        flushOperationBuilder.nextColumn(2);
        flushOperationBuilder.add(mFlushServiceButton);
        flushOperationBuilder.nextColumn(2);
        flushOperationBuilder.add(mFlushNormalButton);
        flushOperationBuilder.nextColumn(2);
        flushOperationBuilder.add(mFlushHighButton);
        flushOperationBuilder.nextColumn(4);
        // flushOperationBuilder.add(mExitSessionButton);

        return flushOperationPanel;
    }

    public void updateAllocatedInfo()
    {
        BeamAllocation allocation = Controller.beam.beamScheduler.getCurrentBeamAllocation();
        if (allocation == null)
        {
            mAllocatedRoomLabel.setText("");
            mAllocatedPriorityLabel.setText("");
            mReleaseBeamButton.setEnabled(false);
        }
        else
        {
            mAllocatedRoomLabel.setText(allocation.getBeamSupplyPointId());
            mAllocatedPriorityLabel.setText(allocation.getPriority().toString());
            //mReleaseBeamButton.setEnabled(mAllowManualBeamRequestRelease.isSelected());
        }
        if (allocation != null){
           // System.out.println(allocation.getBeamSupplyPointId());
            if (allocation.getBeamSupplyPointId().contains("IBTR3")) {
            mReleaseBeamButton.setEnabled(Gui.isADLogout);
        }
    }
    }

    public void updatePendingList()
    {
        List<PendingBeamRequest> requests = mBeamScheduler.getPendingBeamRequests();

        int i = 0;

        while (i < requests.size() && i < mPendingRoomLabels.size())
        {
            PendingBeamRequest req = requests.get(i);
            mPendingRoomLabels.get(i).setText(req.getBeamSupplyPointId());
            mPendingETALabels.get(i).setText(String.valueOf(req.getEstimatedWaitingTime()));
            mPendingPriorityLabels.get(i).setText(req.getPriority().toString());

            ++i;
        }

        while (i < mPendingRoomLabels.size())
        {
            mPendingRoomLabels.get(i).setText("");
            mPendingETALabels.get(i).setText("");
            mPendingPriorityLabels.get(i).setText("");
            ++i;
        }
    }

    private void updateMaintenanceButtons()
    {
        boolean inMaintenanceOrPending = false;

        BeamAllocation ba = mBeamScheduler.getCurrentBeamAllocation();
        if (ba != null && PSEUDO_BEAM_SUPPLY_POINT_FOR_MAINTENANCE.equals(ba.getBeamSupplyPointId()))
        {
            inMaintenanceOrPending = true;
        }
        else
        {
            for (PendingBeamRequest pbr : mBeamScheduler.getPendingBeamRequests())
            {
                if (PSEUDO_BEAM_SUPPLY_POINT_FOR_MAINTENANCE.equals(pbr.getBeamSupplyPointId()))
                {
                    inMaintenanceOrPending = true;
                    break;
                }
            }
        }

        mStartMaintenanceButton.setEnabled(mManualMode && !inMaintenanceOrPending);
        mStopMaintenanceButton.setEnabled(mManualMode && inMaintenanceOrPending);
    }

    public void setBeamRequestEnabled(boolean pEnabled)
    {
        mBeamRequestLabel.setEnabled(pEnabled);
        mBeamSupplyIdsComboBox.setEnabled(pEnabled);
        mBeamPrioritiesComboBox.setEnabled(pEnabled);
        mRequestedTimeLabel.setEnabled(pEnabled);
        mRequestedTimeTextField.setEnabled(pEnabled);
        mRequestBeamButton.setEnabled(pEnabled);
        //mReleaseBeamButton.setEnabled(pEnabled);
    }

    @Override
    public void propertyChange(PropertyChangeEvent pEvent)
    {
        final String propertyName = pEvent.getPropertyName();
        if (propertyName.endsWith(BeamScheduler.CURRENT_BEAM_ALLOCATION_PROPERTY))
        {
            updateAllocatedInfo();
            updateMaintenanceButtons();
        }
        else if (propertyName.endsWith(BeamScheduler.PENDING_BEAM_REQUESTS_PROPERTY))
        {
            updatePendingList();
            updateMaintenanceButtons();
        }
        else if (BeamScheduler.AUTOMATIC_SCHEDULING_PROPERTY.equals(propertyName))
        {
            updateBeamSchedulerMode();
        }
    }

    /*
     * public static void main(String[] pArgs) { JFrame f = new JFrame();
     *
     * BeamSchedulerImpl bs = new BeamSchedulerImpl();
     *
     * HashSet<String> supply = new HashSet<String>(); supply.add("Test-Room-1"); supply.add("Test-Room-2");
     * supply.add("Test-Room-3"); bs.setBeamSupplyPointsIds(supply);
     *
     * BeamSchedulerPanel p = new BeamSchedulerPanel(bs, bs);
     *
     * f.getContentPane().add(p);
     *
     * f.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE); f.setTitle("Beam Scheduler Test Frame"); f.pack();
     * f.setVisible(true); }
     */
    /** Resource to retrieve text strings to be displayed. */
    private static final ResourceDictionary RES = ResourceManager.getInstance()
            .getDictionary("bms/bss/beamscheduler/ui/BeamSchedulerPanel");

    protected final Logger mLogger = Logger.getLogger(getClass());

    private BeamScheduler mBeamScheduler;
    private BeamSchedulerControl mBeamSchedulerControl;

    private boolean mManualMode;

    private JButton mStartMaintenanceButton;
    private JButton mStopMaintenanceButton;

    private JCheckBox mAllowManualBeamRequestRelease;

    private JLabel mAllocatedRoomLabel;
    private JLabel mAllocatedPriorityLabel;
    private JButton mRequestBeamButton;
    public JButton mReleaseBeamButton;

    private List<JLabel> mPendingRoomLabels;
    private List<JLabel> mPendingETALabels;
    private List<JLabel> mPendingPriorityLabels;

    private JLabel mBeamRequestLabel;
    private JComboBox mBeamSupplyIdsComboBox;
    private JComboBox mBeamPrioritiesComboBox;
    private JLabel mRequestedTimeLabel;
    private JFormattedTextField mRequestedTimeTextField;

    private JLabel mBeamSchedulerModeLabel;

    private JButton mQueue1AcceptButton;
    private JButton mQueue1RejectButton;

    private JButton mQueue2RejectButton;
    private JButton mQueue3RejectButton;
    private JButton mQueue4RejectButton;
    private JButton mQueue5RejectButton;

    private JButton mQueue2PlusButton;
    private JButton mQueue3PlusButton;
    private JButton mQueue4PlusButton;
    private JButton mQueue5PlusButton;

    private JButton mFlushAllButton;
    private JButton mFlushServiceButton;
    private JButton mFlushNormalButton;
    private JButton mFlushHighButton;

    /** priority */
    private static final String BMS_BEAM_SCHEDULER_PRIORITY = "bms.beam_scheduler.priority";
}
