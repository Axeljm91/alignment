package com.iba.ialign;

import com.iba.ialign.borders.TitledBorderNoEdge;
import com.iba.icompx.core.activity.ActivityStatus;
import com.iba.pts.bms.bds.common.api.SmpsControllerActivityId;
import com.iba.pts.bms.datatypes.api.BeamDeliveryPoint;
import com.iba.pts.bms.datatypes.api.TreatmentMode;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import java.awt.*;
import java.util.TreeSet;
import javax.swing.*;
import javax.swing.border.TitledBorder;

@SuppressWarnings("serial")
public class SelectBeamlinePanel extends JPanel
{

    public SelectBeamlinePanel(EsbtsPanel pEsbtsPanel)
    {
        mEsbtsPanel = pEsbtsPanel;

        Font bigFont = new Font(Font.DIALOG, Font.BOLD, 16);

        JPanel selectBeamlinePanel = new JPanel();
        selectBeamlinePanel.setBorder(new TitledBorder(BorderFactory.createLineBorder(Color.lightGray)));
        FormLayout selectBeamlineLayout = new FormLayout("pref:grow,3dlu,pref:grow,18dlu,pref:grow", "72dlu,p:g,9dlu,p:g,120dlu,p:g,3dlu,p:g,90dlu,p:g,18dlu,p:g,72dlu");
        final PanelBuilder selectBeamlineBuilder = new PanelBuilder(selectBeamlineLayout, selectBeamlinePanel);

        TitledBorderNoEdge title = new TitledBorderNoEdge("Scanning Magnets");
        title.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.lightGray));

        JPanel selectBeamlinePanelTop = new JPanel();
        selectBeamlinePanelTop.setBorder(title);
        FormLayout selectBeamlineLayoutTop = new FormLayout("pref:grow,3dlu,pref:grow,18dlu,pref:grow", "60dlu,p:g,9dlu,p:g,60dlu");
        final PanelBuilder selectBeamlineBuilderTop = new PanelBuilder(selectBeamlineLayoutTop, selectBeamlinePanelTop);

        JPanel selectBeamlinePanelMid = new JPanel();
        title = new TitledBorderNoEdge("Beam Current");
        selectBeamlinePanelMid.setBorder(title);
        FormLayout selectBeamlineLayoutMid = new FormLayout("pref:grow,3dlu,pref:grow,18dlu,pref:grow", "45dlu,p:g,3dlu,p:g,45dlu");
        final PanelBuilder selectBeamlineBuilderMid = new PanelBuilder(selectBeamlineLayoutMid, selectBeamlinePanelMid);

        JPanel selectBeamlinePanelBottom = new JPanel();
        TitledBorder title2 = new TitledBorder("Slits");
        selectBeamlinePanelBottom.setBorder(title2);
        FormLayout selectBeamlineLayoutBottom = new FormLayout("pref:grow,3dlu,pref:grow,18dlu,pref:grow", "45dlu,p:g,18dlu,p:g,72dlu");
        final PanelBuilder selectBeamlineBuilderBottom = new PanelBuilder(selectBeamlineLayoutBottom, selectBeamlinePanelBottom);

        JLabel blankLabel = new JLabel("");
        JLabel blankLabel2 = new JLabel("");

        JLabel treatmentModeLabel = new JLabel("Beam Current(nA)");
        treatmentModeLabel.setFont(bigFont);
        treatmentModeLabel.setHorizontalAlignment(SwingConstants.CENTER);
        mBeamCurrentTextField = new JTextField("10.0");
        mBeamCurrentTextField.setFont(bigFont);
        mBeamCurrentTextField.setHorizontalAlignment(SwingConstants.CENTER);
        mTreatmentModeComboBox = new JComboBox(mTreatmentModes);

        mContinuousBeamCB = new JCheckBox("Continuous Beam");
        mContinuousBeamCB.setToolTipText("Un-check prior to setting beam current to use pulsed beam.");
        mContinuousBeamCB.setFont(bigFont);
        mContinuousBeamCB.setSelected(true);
        mContinuousBeamCB.setEnabled(false);
        mContinuousBeamCB.setHorizontalAlignment(SwingConstants.CENTER);
        //mContinuousBeamCB.setHorizontalTextPosition(SwingConstants.LEFT);

        mStartButton = new JButton("Set Beam Current");
        mStartButton.setToolTipText("Sends prep request to BCREU. *Try to idle for a few seconds if preps are failing*");
        mStartButton.setFont(bigFont);
        mStartButton.addActionListener(pArg0 -> {

            mEsbtsPanel.mActionsPanel.setDeeVoltage(0.0d);

            try {
                if (Double.parseDouble(mBeamCurrentTextField.getText()) < 0.0d){
                    mBeamCurrentTextField.setText("0.0");
                }
                if (Double.parseDouble(mBeamCurrentTextField.getText()) > 300.0d){
                    mBeamCurrentTextField.setText("300.0");
                }

                if (mContinuousBeamCB.isSelected()) {
                    Controller.beam.bpsController.startPrepareActivity(3, Double.parseDouble(mBeamCurrentTextField.getText()));
                }else{
                    Controller.beam.bpsController.startPrepareActivity(-1, Double.parseDouble(mBeamCurrentTextField.getText()));
//                    while (Controller.bcreu.getRunningState() != "Regulating" && Controller.bcreu.getPulseSource() != "Internal: Single") {
//                        Thread.sleep(1000);
//                    }
//                    Controller.beam.bcreu.setContinuousPulse(true);
                }

//                while (Controller.bcreu.getRunningState() != "Regulating") {
//                    Thread.sleep(500);
//                }
//                Controller.beam.bcreu.setContinuousPulse(true);
            }catch (Exception e){
                e.printStackTrace();
            }


            //mEsbtsPanel.getCallbackUtility().registerCallback(new BssUICallback("Select Beamline"));
            //String id = mEsbtsPanel.getCurrentAllocatedBeamSupplyPointId();
            //TreatmentMode treatmentMode = (TreatmentMode) mTreatmentModes.getSelectedItem();
            //mEsbtsPanel.getEsBtsController().startSelectBeamlineActivity(id, treatmentMode);
        });

        mSMPSStartButton = new JButton("Start Scanning Magnets");
        mSMPSStartButton.setToolTipText("Turns the scanning mangets ON in Uniform scanning mode. Apply setpoints from the BMS service screens on the other PC.");
        mSMPSStartButton.setFont(bigFont);
        mSMPSStartButton.addActionListener(pArg0 -> {
            if (Controller.beam.smpsController.getCurrentActivityName() == SmpsControllerActivityId.IDLE) {
                Controller.beam.smpsController.startPrepareActivity(TreatmentMode.UNIFORM_SCANNING);
            }
        });

        mSMPSStandbyButton = new JButton("Standby Scanning Magnets");
        mSMPSStandbyButton.setToolTipText("Stops the scanning magnets and allows them to be switched to another TR/mode.");
        mSMPSStandbyButton.setFont(bigFont);
        mSMPSStandbyButton.addActionListener(pArg0 -> {
            if (Controller.beam.smpsController.getCurrentActivityName() == SmpsControllerActivityId.PREPARE) {
                Controller.beam.smpsController.startIdleActivity();
            }
        });

        JLabel mSlitsLabel = new JLabel("ESS Slits(mm)", SwingConstants.CENTER);
        mSLE1TextField = new JTextField("10.0");
        mSLE1TextField.setFont(bigFont);
        mSLE1TextField.setHorizontalAlignment(SwingConstants.CENTER);
        mSLE1Button = new JButton("Set X Slits");
        mSLE1Button.setText("Set Slits");
        mSLE1Button.setToolTipText("Sends a request to the ECUBTCU for the new SLE1, SLE2, and SLE3 positions.");
        mSLE1Button.setFont(bigFont);
        mSLE1Button.addActionListener(pArg0 -> {
            Controller.setSlits(Double.parseDouble(mSLE1TextField.getText()), Double.parseDouble(mSLE2TextField.getText()), Double.parseDouble(mSLE3TextField.getText()));
        });
        mSLE2TextField = new JTextField("10.0");
        mSLE2TextField.setFont(bigFont);
        mSLE2TextField.setHorizontalAlignment(SwingConstants.CENTER);
        mSLE2Button = new JButton("Set Y Slits");
        mSLE2Button.setToolTipText("Sends a request to the ECUBTCU for the new SLE2 position.");
        mSLE2Button.setFont(bigFont);
        mSLE2Button.addActionListener(pArg0 -> {

        });
        mSLE3TextField = new JTextField("30.0");
        mSLE3TextField.setFont(bigFont);
        mSLE3TextField.setHorizontalAlignment(SwingConstants.CENTER);
        mSLE3Button = new JButton("Set Momentum Slits");
        mSLE3Button.setToolTipText("Sends a request to the ECUBTCU for the new SLE3 position.");
        mSLE3Button.setFont(bigFont);
        mSLE3Button.addActionListener(pArg0 -> {

        });

        JLabel xslit = new JLabel("X Slits                  ");
        xslit.setFont(bigFont);
        xslit.setHorizontalAlignment(SwingConstants.CENTER);
        xslit.setHorizontalTextPosition(SwingConstants.LEFT);
        JLabel yslit = new JLabel("Y Slits                  ");
        yslit.setFont(bigFont);
        yslit.setHorizontalTextPosition(SwingConstants.LEFT);
        yslit.setHorizontalAlignment(SwingConstants.CENTER);
        JLabel mslit = new JLabel("Momentum Slits");
        mslit.setFont(bigFont);
        mslit.setHorizontalTextPosition(SwingConstants.LEFT);
        mslit.setHorizontalAlignment(SwingConstants.CENTER);
        JLabel scanlabel = new JLabel("Scanning Magnets");
        scanlabel.setFont(bigFont);
        scanlabel.setHorizontalTextPosition(SwingConstants.LEFT);
        scanlabel.setHorizontalAlignment(SwingConstants.CENTER);


        CellConstraints cc = new CellConstraints();

        selectBeamlineBuilder.add(blankLabel, cc.rchw(1,1,1,5));

        selectBeamlineBuilder.add(scanlabel, cc.rchw(2,1,1,1));
        selectBeamlineBuilder.add(mSMPSStartButton, cc.rchw(2, 3, 1, 3));
        selectBeamlineBuilder.add(mSMPSStandbyButton, cc.rchw(4, 3, 1, 3));

        selectBeamlineBuilderTop.add(blankLabel, cc.rchw(1,1,1,5));

        selectBeamlineBuilderTop.add(scanlabel, cc.rchw(2,1,1,1));
        selectBeamlineBuilderTop.add(mSMPSStartButton, cc.rchw(2, 3, 1, 3));
        selectBeamlineBuilderTop.add(mSMPSStandbyButton, cc.rchw(4, 3, 1, 3));

        //selectBeamlineBuilder.addSeparator("", cc.rchw(5,1,1,5));

        selectBeamlineBuilder.add(treatmentModeLabel, cc.rchw(6, 1, 1, 1));
        selectBeamlineBuilder.add(mBeamCurrentTextField, cc.rchw(6, 3, 1, 1));
        selectBeamlineBuilder.add(mContinuousBeamCB, cc.rchw(6,5,1,1));
        selectBeamlineBuilder.add(mStartButton, cc.rchw(8, 3, 1, 3));

        selectBeamlineBuilderMid.add(treatmentModeLabel, cc.rchw(2, 1, 1, 1));
        selectBeamlineBuilderMid.add(mBeamCurrentTextField, cc.rchw(2, 3, 1, 1));
        selectBeamlineBuilderMid.add(mContinuousBeamCB, cc.rchw(2,5,1,1));
        selectBeamlineBuilderMid.add(mStartButton, cc.rchw(4, 3, 1, 3));

        //selectBeamlineBuilder.addSeparator("", cc.rchw(9,1,1,5));

//        selectBeamlineBuilder.add(mSlitsLabel, cc.rchw(5, 1, 1, 3));
//        selectBeamlineBuilder.add(mSLE1TextField, cc.rchw(6, 1, 1, 2));
//        selectBeamlineBuilder.add(mSLE1Button, cc.rchw(6, 3, 1, 1));
//        selectBeamlineBuilder.add(mSLE2TextField, cc.rchw(7, 1, 1, 2));
//        selectBeamlineBuilder.add(mSLE2Button, cc.rchw(7, 3, 1, 1));
//        selectBeamlineBuilder.add(mSLE3TextField, cc.rchw(8, 1, 1, 2));
//        selectBeamlineBuilder.add(mSLE3Button, cc.rchw(8, 3, 1, 1));

        //selectBeamlineBuilder.add(mSlitsLabel, cc.rchw(5,1,1,3));

        selectBeamlineBuilder.add(mSLE1Button, cc.rchw(10, 5, 3, 1));
        selectBeamlineBuilder.add(xslit, cc.rchw(10,1,1,1));
        selectBeamlineBuilder.add(mSLE1TextField, cc.rchw(10, 3, 1, 1));
        selectBeamlineBuilder.add(yslit, cc.rchw(11,1,1,1));
        selectBeamlineBuilder.add(mSLE2TextField, cc.rchw(11, 3, 1, 1));
        selectBeamlineBuilder.add(mslit, cc.rchw(12,1,1,1));
        selectBeamlineBuilder.add(mSLE3TextField, cc.rchw(12, 3, 1, 1));


        selectBeamlineBuilderBottom.add(mSLE1Button, cc.rchw(2, 5, 3, 1));
        selectBeamlineBuilderBottom.add(xslit, cc.rchw(2,1,1,1));
        selectBeamlineBuilderBottom.add(mSLE1TextField, cc.rchw(2, 3, 1, 1));
        selectBeamlineBuilderBottom.add(yslit, cc.rchw(3,1,1,1));
        selectBeamlineBuilderBottom.add(mSLE2TextField, cc.rchw(3, 3, 1, 1));
        selectBeamlineBuilderBottom.add(mslit, cc.rchw(4,1,1,1));
        selectBeamlineBuilderBottom.add(mSLE3TextField, cc.rchw(4, 3, 1, 1));


        selectBeamlineBuilder.add(blankLabel, cc.rchw(13,1,1,5));


        setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.CENTER;
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.1;
        c.weighty = 0.1;
//        add(selectBeamlinePanel, c);

        add(selectBeamlinePanelTop, c);
        c.gridy++;
        add(selectBeamlinePanelMid, c);
        c.gridy++;
        add(selectBeamlinePanelBottom, c);
    }

    /**
     * Updates the panel to show only treatment modes supported by the beam delivery point of the currently allocated beamline.
     * @param //pBeamline the beamline to which beam is currently allocated or null if beam is not allocated
     */
    public void updatePanel(BeamDeliveryPoint bdp)
    {
        mTreatmentModeComboBox.setSelectedIndex(-1);
        mTreatmentModeComboBox.removeAllItems();
        if (bdp != null)
        {
            //BeamDeliveryPoint bdp = mEsbtsPanel.getTherapyCentre().getBeamSupplyPoint(pBeamline.getBeamSupplyPointId())
             //       .getBeamDeliveryPoint();
            //BeamDeliveryPoint bdp = Controller.feedbackClient.tc.getBeamSupplyPoint(Controller.beam.beamScheduler.getCurrentBeamAllocation().getBeamSupplyPointId()).getBeamDeliveryPoint();

            for (TreatmentMode treatmentMode : new TreeSet<>(bdp.getSupportedTreatmentModes()))
            {
                mTreatmentModeComboBox.addItem(treatmentMode);
            }
        }
        if (mTreatmentModeComboBox.getItemCount() > 0)
        {
            mTreatmentModeComboBox.setSelectedIndex(0);
            mTreatmentModeComboBox.setEnabled(mStartButton.isEnabled());
        }
        else
        {
            mTreatmentModeComboBox.setEnabled(false);
        }
    }

    public JComboBox getTreatmentModeComboBox()
    {
        return mTreatmentModeComboBox;
    }

    public JButton getStartButton()
    {
        return mStartButton;
    }

    /**
     * Updates the treatment mode parameter.
     * <p>
     * To be called from the Swing thread.
     * @param pTreatmentMode the treatment mode
     */
    public void updateTreatmentMode(TreatmentMode pTreatmentMode)
    {
        mTreatmentModeComboBox.setSelectedItem(pTreatmentMode);
    }

    private EsbtsPanel mEsbtsPanel;
    private DefaultComboBoxModel mTreatmentModes = new DefaultComboBoxModel();
    private JComboBox mTreatmentModeComboBox;
    public JButton mStartButton;
    public JCheckBox mContinuousBeamCB;
    public JButton mSMPSStartButton;
    public JButton mSMPSStandbyButton;
    public JTextField mBeamCurrentTextField;

    public JButton mSLE1Button;
    public JButton mSLE2Button;
    public JButton mSLE3Button;
    public JTextField mSLE1TextField;
    public JTextField mSLE2TextField;
    public JTextField mSLE3TextField;
}