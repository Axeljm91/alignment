package com.iba.ialign;

import com.iba.blak.device.api.BeamProfileMonitor;
import com.iba.blak.device.api.Insertable;
import com.iba.icompx.ui.util.ResourceManager;
import com.iba.pts.bms.bss.controller.ui.BssUICallback;
import com.iba.ialign.EsbtsPanel;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import java.awt.*;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

@SuppressWarnings("serial")
public class ActionsPanel extends JPanel
{
    public ActionsPanel(JTabbedPane pControlledPanels, EsbtsPanel pEsbtsPanel)
    {
        mControlledPanels = pControlledPanels;
        mEsbtsPanel = pEsbtsPanel;

        Font bigFont = new Font(Font.DIALOG, Font.BOLD, 14);

        mIdleStatusLabel = new JLabel(ResourceManager.getInstance().getImageIcon("bms/bss/controller/ui/inactive"),
                SwingConstants.CENTER);
        mSelectBeamlineStatusLabel = new JLabel(ResourceManager.getInstance().getImageIcon("bms/bss/controller/ui/inactive"),
                SwingConstants.CENTER);
        mSetRangeStatusLabel = new JLabel(ResourceManager.getInstance().getImageIcon("bms/bss/controller/ui/inactive"),
                SwingConstants.CENTER);
        mEnableBeamStatusLabel = new JLabel(ResourceManager.getInstance().getImageIcon("bms/bss/controller/ui/inactive"),
                SwingConstants.CENTER);
        mDisableBeamStatusLabel = new JLabel(ResourceManager.getInstance().getImageIcon("bms/bss/controller/ui/inactive"),
                SwingConstants.CENTER);

        mIdleButton = new JButton("Idle");
        mIdleButton.setFont(bigFont);
        mIdleButton.addActionListener(pEvent -> {
            mControlledPanels.setSelectedIndex(EsbtsPanel.PANEL_INDEX_DEVICES);
            //mEsbtsPanel.getCallbackUtility().registerCallback(new BssUICallback("Idle"));
            //mEsbtsPanel.getEsBtsController().startIdleActivity();
            if (Controller.beam.beamScheduler.getCurrentBeamAllocation() != null) {
                Controller.beam.bssController.startIdleActivity(Controller.beam.beamScheduler.getCurrentBeamAllocation().getBeamSupplyPointId());
            }
            Controller.beam.bpsController.startIdleActivity();
            Controller.beam.smpsController.startIdleActivity();
//            mIdleStatusLabel.setIcon(ResourceManager.getInstance().getImageIcon("bms/bss/controller/ui/good"));
//            mSetRangeStatusLabel.setIcon(ResourceManager.getInstance().getImageIcon("bms/bss/controller/ui/good"));
//            mDisableBeamStatusLabel.setIcon(ResourceManager.getInstance().getImageIcon("bms/bss/controller/ui/inactive"));
//            mEnableBeamStatusLabel.setIcon(ResourceManager.getInstance().getImageIcon("bms/bss/controller/ui/inactive"));
//            mSelectBeamlineStatusLabel.setIcon(ResourceManager.getInstance().getImageIcon("bms/bss/controller/ui/inactive"));
        });

        mSelectBeamlineButton = new JButton("Prepare");
        mSelectBeamlineButton.setFont(bigFont);
        mSelectBeamlineButton.addActionListener(pEvent -> {
                    mControlledPanels.setSelectedIndex(EsbtsPanel.PANEL_INDEX_SET_RANGE);


                });

        mSetRangeButton = new JButton("Set Range");
        mSetRangeButton.setFont(bigFont);
        mSetRangeButton.addActionListener(pEvent -> mControlledPanels.setSelectedIndex(EsbtsPanel.PANEL_INDEX_SELECT_BEAMLINE));

        deeVoltage = 0.0;

        mEnableBeamButton = new JButton("Enable Beam");
        mEnableBeamButton.setFont(bigFont);
        mEnableBeamButton.addActionListener(pEvent -> {
            //mControlledPanels.setSelectedIndex(EsbtsPanel.PANEL_INDEX_DEVICES);
            //mEsbtsPanel.getCallbackUtility().registerCallback(new BssUICallback("Enable Beam"));
            //mEsbtsPanel.getEsBtsController().startEnableBeamActivity(mEsbtsPanel.getCurrentAllocatedBeamSupplyPointId());
            if (mEsbtsPanel.mSelectBeamlinePanel.mContinuousBeamCB.isSelected()) {
                Controller.beam.llrf.setMaxVoltage(56.00d);
                try {
                    if (deeVoltage==0) {
                        deeVoltage = Gui.getRestVariable("B0.R1.LLA01.VDee1feedbackrv").getAsDouble();
                        deeVoltage = Math.floor(deeVoltage);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                System.out.println(deeVoltage);
                Controller.beam.bcreu.pauseRegulation();
                Controller.beam.llrf.setDeeVoltage2(41.00d);
                try {
                    Thread.sleep(1000);
                    Controller.beam.bssController.startEnableBeamActivity(Controller.beam.beamScheduler.getCurrentBeamAllocation().getBeamSupplyPointId());
                    Thread.sleep(1000);
                    if (Controller.S2E.getPosition() == Insertable.Position.MOVING || Controller.S2E.getPosition() == Insertable.Position.RETRACTED) {
                        //do nothing
                    } else {
                        Controller.ecubtcu.bsRetract("S2E");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
//                if (deeVoltage > 41) {
//                    Controller.beam.llrf.setDeeVoltage2(deeVoltage);
//                }
            }else {
                try {
                    Thread.sleep(500);
                    Controller.beam.bssController.startEnableBeamActivity(Controller.beam.beamScheduler.getCurrentBeamAllocation().getBeamSupplyPointId());
                    Thread.sleep(1000);
                    if (Controller.S2E.getPosition() == Insertable.Position.MOVING || Controller.S2E.getPosition() == Insertable.Position.RETRACTED) {
                        //do nothing
                    } else {
                        Controller.ecubtcu.bsRetract("S2E");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });


        mDisableBeamButton = new JButton("Disable Beam");
        mDisableBeamButton.setFont(bigFont);
        mDisableBeamButton.addActionListener(pEvent -> {
            //mControlledPanels.setSelectedIndex(EsbtsPanel.PANEL_INDEX_DEVICES);
            //mEsbtsPanel.getCallbackUtility().registerCallback(new BssUICallback("Disable Beam"));
            //mEsbtsPanel.getEsBtsController().startDisableBeamActivity(mEsbtsPanel.getCurrentAllocatedBeamSupplyPointId());
            if (mEsbtsPanel.mSelectBeamlinePanel.mContinuousBeamCB.isSelected()){
                Controller.beam.bcreu.pauseRegulation();
            }else{
                Controller.beam.bcreu.setContinuousPulse(false);
            }
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
            //Controller.beam.bcreu.pauseRegulation();

            Gui.startBeamButton.setText("Start Beam");
        });

        mIdleButton.setPreferredSize(new Dimension(140, 30));
        mSelectBeamlineButton.setPreferredSize(new Dimension(140, 30));
        mSetRangeButton.setPreferredSize(new Dimension(140, 30));
        mEnableBeamButton.setPreferredSize(new Dimension(140, 30));
        mDisableBeamButton.setPreferredSize(new Dimension(140, 30));

        // tool tips
        mIdleButton.setToolTipText("Set the RF to a non-beam producing voltage and inserts the ESS beam stop. Enables source tuning.");
        mSetRangeButton.setToolTipText("Selects the Set Range tab.");
        mSelectBeamlineButton.setToolTipText("Selects the Prepare tab.");
        mEnableBeamButton.setToolTipText("Pauses beam output and retracts beamstops from the beamline.");
        mDisableBeamButton.setToolTipText("Inserts beamstops to the beamline.");

        Border border = BorderFactory.createLineBorder(Color.lightGray);
        CellConstraints cc = new CellConstraints();

        JPanel idlePanel = new JPanel();
        idlePanel.setBorder(new TitledBorder(border));
        FormLayout idleLayout = new FormLayout("pref:grow,3dlu,pref:grow", "pref:grow,3dlu,pref:grow");
        final PanelBuilder idleBuilder = new PanelBuilder(idleLayout, idlePanel);

        idleBuilder.add(mIdleStatusLabel, cc.rchw(1, 1, 3, 1));
        idleBuilder.add(mIdleButton, cc.rchw(1, 3, 3, 1));

        JPanel selectBeamlinePanel = new JPanel();
        selectBeamlinePanel.setBorder(new TitledBorder(border));
        FormLayout selectBeamlineLayout = new FormLayout("pref:grow,3dlu,pref:grow", "pref:grow,3dlu,pref:grow");
        final PanelBuilder selectBeamlineBuilder = new PanelBuilder(selectBeamlineLayout, selectBeamlinePanel);

        selectBeamlineBuilder.add(mSelectBeamlineStatusLabel, cc.rchw(1, 1, 3, 1));
        selectBeamlineBuilder.add(mSelectBeamlineButton, cc.rchw(1, 3, 3, 1));

        JPanel setRangePanel = new JPanel();
        setRangePanel.setBorder(new TitledBorder(border));
        FormLayout setRangeLayout = new FormLayout("pref:grow,3dlu,pref:grow", "pref:grow,3dlu,pref:grow");
        final PanelBuilder setRangeBuilder = new PanelBuilder(setRangeLayout, setRangePanel);

        setRangeBuilder.add(mSetRangeStatusLabel, cc.rchw(1, 1, 3, 1));
        setRangeBuilder.add(mSetRangeButton, cc.rchw(1, 3, 3, 1));

        JPanel enableBeamPanel = new JPanel();
        enableBeamPanel.setBorder(new TitledBorder(border));
        FormLayout enableBeamLayout = new FormLayout("pref:grow,3dlu,pref:grow", "pref:grow,3dlu,pref:grow");
        final PanelBuilder enableBeamBuilder = new PanelBuilder(enableBeamLayout, enableBeamPanel);

        enableBeamBuilder.add(mEnableBeamStatusLabel, cc.rchw(1, 1, 3, 1));
        enableBeamBuilder.add(mEnableBeamButton, cc.rchw(1, 3, 3, 1));

        JPanel disableBeamPanel = new JPanel();
        disableBeamPanel.setBorder(new TitledBorder(border));
        FormLayout disableBeamLayout = new FormLayout("pref:grow,3dlu,pref:grow", "pref:grow,3dlu,pref:grow");
        final PanelBuilder disableBeamBuilder = new PanelBuilder(disableBeamLayout, disableBeamPanel);

        disableBeamBuilder.add(mDisableBeamStatusLabel, cc.rchw(1, 1, 3, 1));
        disableBeamBuilder.add(mDisableBeamButton, cc.rchw(1, 3, 3, 1));

        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        c.anchor = GridBagConstraints.NORTH;
        c.fill = GridBagConstraints.BOTH;
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.1;
        c.weighty = 0.1;
        add(idlePanel, c);

        c.anchor = GridBagConstraints.NORTH;
        c.fill = GridBagConstraints.BOTH;
        c.gridx = 0;
        c.gridy = 2;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.1;
        c.weighty = 0.1;
        add(selectBeamlinePanel, c);

        c.anchor = GridBagConstraints.NORTH;
        c.fill = GridBagConstraints.BOTH;
        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.1;
        c.weighty = 0.1;
        add(setRangePanel, c);

        c.anchor = GridBagConstraints.NORTH;
        c.fill = GridBagConstraints.BOTH;
        c.gridx = 0;
        c.gridy = 3;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.1;
        c.weighty = 0.1;
        add(enableBeamPanel, c);

        c.anchor = GridBagConstraints.NORTH;
        c.fill = GridBagConstraints.BOTH;
        c.gridx = 0;
        c.gridy = 4;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.1;
        c.weighty = 0.1;
        add(disableBeamPanel, c);

    }

    public void setControlledPanels(JTabbedPane pControlledPanels)
    {
        mControlledPanels = pControlledPanels;
    }

    public JLabel getIdleStatusLabel()
    {
        return mIdleStatusLabel;
    }

    public JLabel getSelectBeamlineStatusLabel()
    {
        return mSelectBeamlineStatusLabel;
    }

    public JLabel getSetRangeStatusLabel()
    {
        return mSetRangeStatusLabel;
    }

    public JLabel getEnableBeamStatusLabel()
    {
        return mEnableBeamStatusLabel;
    }

    public JLabel getDisableBeamStatusLabel()
    {
        return mDisableBeamStatusLabel;
    }

    public JButton getIdleButton()
    {
        return mIdleButton;
    }

    public JButton getSelectBeamlineButton()
    {
        return mSelectBeamlineButton;
    }

    public JButton getSetRangeButton()
    {
        return mSetRangeButton;
    }

    public JButton getEnableBeamButton()
    {
        return mEnableBeamButton;
    }

    public JButton getDisableBeamButton()
    {
        return mDisableBeamButton;
    }

    public Double getDeeVoltage() { return deeVoltage;}

    public void setDeeVoltage(Double newVoltage) { this.deeVoltage = newVoltage;}

    // the controlled panels are in the tabbed pane
    private JTabbedPane mControlledPanels;
    private EsbtsPanel mEsbtsPanel;

    public JLabel mIdleStatusLabel;
    public JLabel mSelectBeamlineStatusLabel;
    public JLabel mSetRangeStatusLabel;
    public JLabel mEnableBeamStatusLabel;
    public JLabel mDisableBeamStatusLabel;

    public Double deeVoltage;

    private JButton mIdleButton;
    private JButton mSelectBeamlineButton;
    private JButton mSetRangeButton;
    private JButton mEnableBeamButton;
    private JButton mDisableBeamButton;
}
