package com.iba.ialign;

import com.iba.ialign.common.IbaColors;
import com.iba.pts.bms.bss.controller.ui.BssUICallback;
import com.iba.ialign.EsbtsPanel;
import com.iba.pts.bms.bss.esbts.Beamline;
import com.iba.pts.bms.datatypes.api.TreatmentMode;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.opcOpenInterface.Rest;
import javafx.scene.text.TextAlignment;

import java.awt.*;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;


public class InfoPanel extends JPanel
{

    public InfoPanel()
    {
        Font bigFont = new Font(Font.DIALOG, Font.BOLD, 16);

        CellConstraints cc = new CellConstraints();
        Border border = BorderFactory.createLineBorder(Color.lightGray);

        JPanel infoPanel = new JPanel();
        TitledBorder title = new TitledBorder("Actions");
        title.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.lightGray));
        infoPanel.setBorder(title);
        FormLayout actionsLayout = new FormLayout("3dlu,pref,3dlu", "3dlu,pref,3dlu,pref,3dlu,pref,3dlu");
        final PanelBuilder actionsBuilder = new PanelBuilder(actionsLayout, infoPanel);

        JLabel actionsTip1 = new JLabel("Idle will set the RF to a non-beam producing voltage and insert the ESS beam stop.");
        actionsTip1.setFont(bigFont);
        JLabel actionsTip2 = new JLabel("Enable beam will retract all beam stops.");
        actionsTip2.setFont(bigFont);
        JLabel actionsTip3 = new JLabel("Disable beam will insert all beam stops.");
        actionsTip3.setFont(bigFont);

        int row = 2;
        actionsBuilder.add(actionsTip1, cc.rchw(row, 2, 1, 1));
        row +=2;

        actionsBuilder.add(actionsTip2, cc.rchw(row, 2, 1, 1));
        row +=2;

        actionsBuilder.add(actionsTip3, cc.rchw(row, 2, 1, 1));

        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.CENTER;
        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 1;
        c.weighty = 1;
        add(infoPanel, c);


        JPanel workflowPanel = new JPanel();
        title = new TitledBorder("Workflow");
        title.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.lightGray));
        workflowPanel.setBorder(title);
        FormLayout workflowLayout = new FormLayout("3dlu,pref,3dlu", "3dlu,pref,3dlu,pref,3dlu,pref,3dlu,pref,3dlu,pref,3dlu");
        final PanelBuilder workflowBuilder = new PanelBuilder(workflowLayout, workflowPanel);

        JLabel workflowTip1 = new JLabel("Press the Start Service Beam button on the Idle tab once all pre-reqs are satisfied.");
        workflowTip1.setFont(bigFont);
        JLabel workflowTip2 = new JLabel("Press the Request Beam button, and once allocated, begin a Set Range activity. ");
        workflowTip2.setFont(bigFont);
        JLabel workflowTip3 = new JLabel("Once the range is set, prepare a beam current and the slits positions.");
        workflowTip3.setFont(bigFont);
        JLabel workflowTip4 = new JLabel("Once settings are confirmed, press Enable Beam to retract beamstops.");
        workflowTip4.setFont(bigFont);
        JLabel workflowTip5 = new JLabel("Confirm Beam Timer settings and press Start Beam to begin your run.");
        workflowTip5.setFont(bigFont);

        row = 2;
        workflowBuilder.add(workflowTip1, cc.rchw(row, 2, 1, 1));
        row +=2;

        workflowBuilder.add(workflowTip2, cc.rchw(row, 2, 1, 1));
        row +=2;

        workflowBuilder.add(workflowTip3, cc.rchw(row, 2, 1, 1));
        row +=2;

        workflowBuilder.add(workflowTip4, cc.rchw(row, 2, 1, 1));
        row +=2;

        workflowBuilder.add(workflowTip5, cc.rchw(row, 2, 1, 1));

        c = new GridBagConstraints();

        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.CENTER;
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 1;
        c.weighty = 1;
        add(workflowPanel, c);

        JPanel infoPanel1 = new JPanel();
        title = new TitledBorder("Set Range");
        title.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.lightGray));
        infoPanel1.setBorder(title);
        FormLayout setRangeLayout = new FormLayout("3dlu,pref,3dlu", "3dlu,pref,3dlu,pref,3dlu,pref,3dlu,pref,3dlu,pref,3dlu");
        final PanelBuilder setRangeBuilder = new PanelBuilder(setRangeLayout, infoPanel1);

        JLabel setRangeTip1 = new JLabel("Ranges can be set from 7.0 to 32.0(g/cm\u00B2).");
        setRangeTip1.setFont(bigFont);
        JLabel setRangeTip2 = new JLabel("Use the X and Y offset fields to steer the beam position on IC1.");
        setRangeTip2.setFont(bigFont);
        JLabel setRangeTip3 = new JLabel("View the real-time position feedback from the TCR BMS Service Screens.");
        setRangeTip3.setFont(bigFont);
        JLabel setRangeTip4 = new JLabel("The 'Cycling' checkbox sets magnets to their max current before each new range setting.");
        setRangeTip4.setFont(bigFont);
        JLabel setRangeTip5 = new JLabel("Each set range will move the slits positions, be sure to check these after every change.");
        setRangeTip5.setFont(bigFont);


        row = 2;
        setRangeBuilder.add(setRangeTip1, cc.rchw(row, 2, 1, 1));
        row +=2;

        setRangeBuilder.add(setRangeTip2, cc.rchw(row, 2, 1, 1));
        row +=2;

        setRangeBuilder.add(setRangeTip3, cc.rchw(row, 2, 1, 1));
        row +=2;

        setRangeBuilder.add(setRangeTip4, cc.rchw(row, 2, 1, 1));
        row +=2;

        setRangeBuilder.add(setRangeTip5, cc.rchw(row, 2, 1, 1));

        c = new GridBagConstraints();

        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.CENTER;
        c.gridx = 0;
        c.gridy = 2;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 1;
        c.weighty = 1;
        add(infoPanel1, c);

        JPanel infoPanel2 = new JPanel();
        title = new TitledBorder("Prepare");
        title.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.lightGray));
        infoPanel2.setBorder(title);
        FormLayout prepareLayout = new FormLayout("3dlu,pref,3dlu", "3dlu,pref,3dlu,pref,3dlu,pref,3dlu,pref,3dlu,pref,3dlu");
        final PanelBuilder prepareBuilder = new PanelBuilder(prepareLayout, infoPanel2);

        JLabel prepTip1 = new JLabel("Use the BMS TCR Service Screens to change Scanning Magnet setpoints.");
        prepTip1.setFont(bigFont);
        JLabel prepTip2 = new JLabel("Idle before changing beam current for best results.");
        prepTip2.setFont(bigFont);
        JLabel prepTip3 = new JLabel("Beam current can be set up to 300nA.");
        prepTip3.setFont(bigFont);
        JLabel prepTip4 = new JLabel("If the prepared beam current feedback seems frozen, restart the application.");
        prepTip4.setFont(bigFont);
        JLabel prepTip5 = new JLabel("Uncheck 'Continuous Beam' before a new beam prep to use pulsed beam.");
        prepTip5.setFont(bigFont);


        row = 2;
        prepareBuilder.add(prepTip1, cc.rchw(row, 2, 1, 1));
        row +=2;

        prepareBuilder.add(prepTip2, cc.rchw(row, 2, 1, 1));
        row +=2;

        prepareBuilder.add(prepTip3, cc.rchw(row, 2, 1, 1));
        row +=2;

        prepareBuilder.add(prepTip4, cc.rchw(row, 2, 1, 1));
        row +=2;

        prepareBuilder.add(prepTip5, cc.rchw(row, 2, 1, 1));

        c = new GridBagConstraints();

        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.CENTER;
        c.gridx = 0;
        c.gridy = 3;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 1;
        c.weighty = 1;
        add(infoPanel2, c);

        JPanel infoPanel3 = new JPanel();
        title = new TitledBorder("Beam Timer");
        title.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.lightGray));
        infoPanel3.setBorder(title);
        FormLayout timerLayout = new FormLayout("3dlu,pref,3dlu", "3dlu,pref,3dlu,pref,3dlu,pref,3dlu,pref,3dlu,pref,3dlu");
        final PanelBuilder timerBuilder = new PanelBuilder(timerLayout, infoPanel3);

        JLabel timerTip1 = new JLabel("Set the timer for desired length of run in seconds.");
        timerTip1.setFont(bigFont);
        JLabel timerTip2 = new JLabel("Un-check the 'Timed Run' box to manually start/stop the beam.");
        timerTip2.setFont(bigFont);
        JLabel timerTip3 = new JLabel("The RF option in the dropdown menu will set the RF to idle voltage(41kV) after each run.");
        timerTip3.setFont(bigFont);
        JLabel timerTip4 = new JLabel("The Beamstop option in the dropdown menu will insert the ESS beamstop after each run(short delay).");
        timerTip4.setFont(bigFont);
        JLabel timerTip5 = new JLabel("If the RF OK feedback goes false during a run, the beam and timer are automatically stopped.");
        timerTip5.setFont(bigFont);


        row = 2;
        timerBuilder.add(timerTip1, cc.rchw(row, 2, 1, 1));
        row +=2;

        timerBuilder.add(timerTip2, cc.rchw(row, 2, 1, 1));
        row +=2;

        timerBuilder.add(timerTip3, cc.rchw(row, 2, 1, 1));
        row +=2;

        timerBuilder.add(timerTip4, cc.rchw(row, 2, 1, 1));
        row +=2;

        timerBuilder.add(timerTip5, cc.rchw(row, 2, 1, 1));

        c = new GridBagConstraints();

        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.CENTER;
        c.gridx = 0;
        c.gridy = 4;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 1;
        c.weighty = 1;
        add(infoPanel3, c);
    }


    public JFormattedTextField getRangeTextField()
    {
        return mRangeTextField;
    }

    public JComboBox getOpticalSolutionComboBox()
    {
        return mOpticalSolutionComboBox;
    }

    public JFormattedTextField getGantryAngleTextField()
    {
        return mGantryAngleTextField;
    }

    public JFormattedTextField getOffsetXTextField()
    {
        return mOffsetXTextField;
    }

    public JFormattedTextField getOffsetYTextField()
    {
        return mOffsetYTextField;
    }

    public JCheckBox getCyclingCheckBox()
    {
        return mCyclingCheckBox;
    }

    public JButton getStartButton()
    {
        return mStartButton;
    }

    private EsbtsPanel mEsbtsPanel;

    private JFormattedTextField mRangeTextField;
    private JComboBox mOpticalSolutionComboBox;
    private JFormattedTextField mGantryAngleTextField;
    private JFormattedTextField mOffsetXTextField;
    private JFormattedTextField mOffsetYTextField;
    private JCheckBox mCyclingCheckBox;
    private JButton mStartButton;
    private JComboBox energyCB;
}