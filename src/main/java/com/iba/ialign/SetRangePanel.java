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

@SuppressWarnings("serial")
public class SetRangePanel extends JPanel
{

    public SetRangePanel(EsbtsPanel pEsbtsPanel)
    {
        mEsbtsPanel = pEsbtsPanel;

        Font bigFont = new Font(Font.DIALOG, Font.BOLD, 16);

        Double zero = Double.valueOf(0.0);

        JLabel rangeLabel = new JLabel("Range (g/cm\u00B2)", SwingConstants.RIGHT);
        rangeLabel.setFont(bigFont);
        JLabel energyLabel = new JLabel("Energy (MeV)", SwingConstants.RIGHT);
        energyLabel.setFont(bigFont);
        final DecimalFormat rangeFormat = (DecimalFormat) NumberFormat.getNumberInstance();
        rangeFormat.applyPattern("0.00");
        mRangeTextField = new JFormattedTextField(rangeFormat);
        mRangeTextField.setFont(bigFont);
        mRangeTextField.setValue(7.0d);

        JLabel opticalSolutionLabel = new JLabel("Optical Solution", SwingConstants.RIGHT);
        mOpticalSolutionComboBox = new JComboBox();

        JLabel gantryAngleLabel = new JLabel("Gantry Angle (degrees)", SwingConstants.RIGHT);
        gantryAngleLabel.setFont(bigFont);
        final DecimalFormat gantryAngleFormat = (DecimalFormat) NumberFormat.getNumberInstance();
        gantryAngleFormat.applyPattern("0.0");
        mGantryAngleTextField = new JFormattedTextField(gantryAngleFormat);
        mGantryAngleTextField.setFont(bigFont);
        mGantryAngleTextField.setValue(90.0);
        mGantryAngleTextField.setEditable(false);

        JLabel offsetXLabel = new JLabel("Offset X (mm)", SwingConstants.RIGHT);
        offsetXLabel.setFont(bigFont);
        final DecimalFormat offsetXFormat = (DecimalFormat) NumberFormat.getNumberInstance();
        offsetXFormat.applyPattern("0.0");
        mOffsetXTextField = new JFormattedTextField(offsetXFormat);
        mOffsetXTextField.setFont(bigFont);
        mOffsetXTextField.setEditable(true);
        mOffsetXTextField.setValue(zero);

        JLabel offsetYLabel = new JLabel("Offset Y (mm)", SwingConstants.RIGHT);
        offsetYLabel.setFont(bigFont);
        final DecimalFormat offsetYFormat = (DecimalFormat) NumberFormat.getNumberInstance();
        offsetYFormat.applyPattern("0.0");
        mOffsetYTextField = new JFormattedTextField(offsetYFormat);
        mOffsetYTextField.setFont(bigFont);
        mOffsetYTextField.setEditable(true);
        mOffsetYTextField.setValue(zero);

        mCyclingCheckBox = new JCheckBox("Cycling");
        mCyclingCheckBox.setToolTipText("Prevent magnetic hysteresis between set-ranges.");
        mCyclingCheckBox.setFont(bigFont);
        mCyclingCheckBox.setHorizontalAlignment(SwingConstants.RIGHT);
        mCyclingCheckBox.setSelected(true);

        Dimension textFieldDimension = new Dimension(60, 30);
        mRangeTextField.setPreferredSize(textFieldDimension);
        mGantryAngleTextField.setPreferredSize(textFieldDimension);
        mOffsetXTextField.setPreferredSize(textFieldDimension);
        mOffsetYTextField.setPreferredSize(textFieldDimension);

        mRangeTextField.setHorizontalAlignment(SwingConstants.CENTER);
        mGantryAngleTextField.setHorizontalAlignment(SwingConstants.CENTER);
        mOffsetXTextField.setHorizontalAlignment(SwingConstants.CENTER);
        mOffsetYTextField.setHorizontalAlignment(SwingConstants.CENTER);

        mStartButton = new JButton("Start Set Range");
        mStartButton.setToolTipText("Starts set-range activity for all beamline devices.");
        mStartButton.setFont(bigFont);
        mStartButton.addActionListener(pEvent -> {
            //mEsbtsPanel.getCallbackUtility().registerCallback(new BssUICallback("Set Range"));

            String id = Controller.beam.beamScheduler.getCurrentBeamAllocation().getBeamSupplyPointId();
            //double range = energyToRange(Double.valueOf(energyCB.getSelectedItem().toString()));
            double range = Double.parseDouble(mRangeTextField.getText());
            //String opticalSolution = (String) mOpticalSolutionComboBox.getSelectedItem();
            double angle = ((Number) mGantryAngleTextField.getValue()).doubleValue();
            double offsetX = ((Number) mOffsetXTextField.getValue()).doubleValue();
            double offsetY = ((Number) mOffsetYTextField.getValue()).doubleValue();
            boolean cycling = mCyclingCheckBox.isSelected();

            //mEsbtsPanel.getEsBtsController().startSetRangeActivity(id, range, opticalSolution, angle, offsetX, offsetY, cycling);
            if (range > 32.00d || range < 7.00d){
                if (range > 32.00d) {
                    mRangeTextField.setText("32.00");
                    range = 32.00d;
                }else {
                    mRangeTextField.setText("7.00");
                    range = 7.00d;
                }
            }

            Controller.beam.bssController.startSetRangeActivity(id, range, "DS_US_Default", angle, offsetX, offsetY, cycling);

            //Gui.mBlankPanel.getEsBtsController().startSelectBeamlineActivity("IBTR3-90", TreatmentMode.UNIFORM_SCANNING);
            //Controller.beam.esBtsController.startSelectBeamlineActivity("IBTR3-90", TreatmentMode.UNIFORM_SCANNING);
//            Controller.beam.blpscuCmdChannelProxy.sendCommand(117);
//            Controller.beam.blpscuCmdChannelProxy.proxyPublish();

            //System.out.println(Controller.beam.blpscu.getHardwareChecksum());


        });


        String column[] = {"Energy(MeV)", "Range(g/cm\u00B2)"};
        String data[][] = {{"100", "8.05"},{"100.8", "8.16"}, {"102.6","8.41"}, {"104.4","8.66"}, {"106.2","8.91"}, {"108","9.17"},
                {"109.6","9.40"}, {"111.4","9.67"}, {"113.1","9.92"}, {"114.7","10.16"}, {"115","10.21"}, {"116.4","10.42"},
                {"118","10.67"}, {"119.6","10.92"}, {"120","10.98"}, {"121.2","11.17"}, {"122.8","11.42"}, {"124.4","11.68"},
                {"126","11.94"}, {"127.5","12.18"}, {"128","12.26"}, {"129","12.43"}, {"130.4","12.66"}, {"131.8","12.89"},
                {"133.3","13.15"}, {"134.8","13.40"}, {"136.2","13.64"}, {"137.7","13.90"}, {"139.2","14.16"}, {"140","14.30"},
                {"140.6","14.41"}, {"142","14.65"}, {"143.4","14.9"}, {"144.8","15.15"}, {"146.2","15.41"}, {"147.5","15.64"},
                {"148.9","15.9"}, {"150.3","16.16"}, {"151","16.29"}, {"151.7","16.42"}, {"153","16.66"}, {"154.3","16.9"},
                {"155.7","17.17"}, {"157","17.42"}, {"158.3","17.66"}, {"159.6","17.91"}, {"160","17.99"}, {"160.9","18.17"},
                {"162.2","18.42"}, {"163.4","18.65"}, {"164.7","18.91"}, {"180","22.02"}, {"195","25.25"}, {"200","26.4"}};

        String energies[] = {"100", "100.8", "102.6", "104.4", "106.2", "108",
                "109.6", "111.4", "113.1", "114.7", "115", "116.4",
                "118", "119.6", "120", "121.2", "122.8", "124.4",
                "126", "127.5", "128", "129", "130.4", "131.8",
                "133.3", "134.8", "136.2", "137.7", "139.2", "140",
                "140.6", "142", "143.4", "144.8", "146.2", "147.5",
                "148.9", "150.3", "151", "151.7", "153","154.3",
                "155.7", "157", "158.3", "159.6", "160", "160.9",
                "162.2", "163.4", "164.7", "180", "195", "200"};


        JTable rangeConversionTable = new JTable(data,column);
        rangeConversionTable.getTableHeader().setBorder(new LineBorder(Color.darkGray));
        rangeConversionTable.getTableHeader().setFont(new Font(Font.DIALOG, Font.BOLD, 16));
        rangeConversionTable.setEnabled(true);
        rangeConversionTable.setFont(new Font(Font.DIALOG, Font.BOLD, 16));

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        rangeConversionTable.setDefaultRenderer(String.class, centerRenderer);

//        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
//        rightRenderer.setHorizontalAlignment(SwingConstants.CENTER);

        TableModel tableModel = rangeConversionTable.getModel();

        for (int columnIndex = 0; columnIndex < tableModel.getColumnCount(); columnIndex++)
        {
            rangeConversionTable.getColumnModel().getColumn(columnIndex).setCellRenderer(centerRenderer);
        }

        JScrollPane sp = new JScrollPane(rangeConversionTable);


        energyCB = new JComboBox();
        energyCB.setFont(bigFont);

        for (int i=0; i<energies.length; i++) {
            energyCB.insertItemAt(energies[i], i);
        }



        CellConstraints cc = new CellConstraints();
        Border border = BorderFactory.createLineBorder(Color.lightGray);

        JPanel rangePanel = new JPanel();
        TitledBorder title = new TitledBorder("Set Range");
        title.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.lightGray));
        rangePanel.setBorder(title);
        FormLayout setRangeLayout = new FormLayout("pref:grow,3dlu,pref:grow,3dlu,pref:grow,3dlu,pref:grow",
                "6dlu,p:g,3dlu,pref,3dlu,pref,3dlu,pref,12dlu,pref");
        final PanelBuilder setRangeBuilder = new PanelBuilder(setRangeLayout, rangePanel);

        int row = 2;
//        setRangeBuilder.add(sp, cc.rchw(row, 1, 1, 7));
//        row+=2;

        setRangeBuilder.add(rangeLabel, cc.rchw(row, 1, 1, 1));
        setRangeBuilder.add(mRangeTextField, cc.rchw(row, 3, 1, 1));
        //setRangeBuilder.add(energyLabel, cc.rchw(row, 1, 1, 1));
        //setRangeBuilder.add(energyCB, cc.rchw(row, 3, 1, 1));
        //row += 2;

        //setRangeBuilder.add(opticalSolutionLabel, cc.rchw(row, 1, 1, 1));
        //setRangeBuilder.add(mOpticalSolutionComboBox, cc.rchw(row, 3, 1, 1));
        setRangeBuilder.add(gantryAngleLabel, cc.rchw(row, 5, 1, 1));
        setRangeBuilder.add(mGantryAngleTextField, cc.rchw(row, 7, 1, 1));
        row += 2;

        setRangeBuilder.add(offsetXLabel, cc.rchw(row, 1, 1, 1));
        setRangeBuilder.add(mOffsetXTextField, cc.rchw(row, 3, 1, 1));
        setRangeBuilder.add(offsetYLabel, cc.rchw(row, 5, 1, 1));
        setRangeBuilder.add(mOffsetYTextField, cc.rchw(row, 7, 1, 1));
        row += 2;

        setRangeBuilder.add(mCyclingCheckBox, cc.rchw(row, 1, 1, 1));
        row += 2;

        setRangeBuilder.add(mStartButton, cc.rchw(row, 1, 1, 7));

        row+=2;
        //setRangeBuilder.add(sp,cc.rchw(row,1,1,7));
        //setRangeBuilder.add(rangeConversionTable.getTableHeader(), cc.rchw(row,1,1,7));
        //row+=1;
        //setRangeBuilder.add(rangeConversionTable, cc.rchw(row, 1, 1, 7));

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
        add(rangePanel, c);
    }

    private double energyToRange(double energy) {
        //        String data[][] = {{"100", "8.05"},{"100.8", "8.16"}, {"102.6","8.41"}, {"104.4","8.66"}, {"106.2","8.91"}, {"108","9.17"},
        //                {"109.6","9.40"}, {"111.4","9.67"}, {"113.1","9.92"}, {"114.7","10.16"}, {"115","10.21"}, {"116.4","10.42"},
        //                {"118","10.67"}, {"119.6","10.92"}, {"120","10.98"}, {"121.2","11.17"}, {"122.8","11.42"}, {"124.4","11.68"},
        //                {"126","11.94"}, {"127.5","12.18"}, {"128","12.26"}, {"129","12.43"}, {"130.4","12.66"}, {"131.8","12.89"},
        //                {"133.3","13.15"}, {"134.8","13.40"}, {"136.2","13.64"}, {"137.7","13.90"}, {"139.2","14.16"}, {"140","14.30"},
        //                {"140.6","14.41"}, {"142","14.65"}, {"143.4","14.9"}, {"144.8","15.15"}, {"146.2","15.41"}, {"147.5","15.64"},
        //                {"148.9","15.9"}, {"150.3","16.16"}, {"151","16.29"}, {"151.7","16.42"}, {"153","16.66"}, {"154.3","16.9"},
        //                {"155.7","17.17"}, {"157","17.42"}, {"158.3","17.66"}, {"159.6","17.91"}, {"160","17.99"}, {"160.9","18.17"},
        //                {"162.2","18.42"}, {"163.4","18.65"}, {"164.7","18.91"}, {"180","22.02"}, {"195","25.25"}, {"200","26.4"}};

        switch (String.valueOf(energy)) {
            case "100":
                return 8.05;
            case "100.8":
                return 8.16;
            case "102.6":
                return 8.41;
            case "104.4":
                return 8.66;
            case "106.2":
                return 8.91;
            case "108":
                return 9.17;
            case "109.6":
                return 9.40;
            case "111.4":
                return 9.67;
            case "113.1":
                return 9.92;
            case "114.7":
                return 10.16;
            case "115":
                return 10.21;
            case "116.4":
                return 10.42;
            case "118":
                return 10.67;
            case "119.6":
                return 10.92;
            case "120":
                return 10.98;
            case "121.2":
                return 11.17;
            case "122.8":
                return 11.42;
            case "124.4":
                return 11.68;
            case "126":
                return 11.94;
            case "127.5":
                return 12.18;
            case "128":
                return 12.26;
            case "129":
                return 12.43;
            case "130.4":
                return 12.66;
            case "131.8":
                return 12.89;
            case "133.3":
                return 13.15;
            case "134.8":
                return 13.40;
        }



        Double.valueOf(energyCB.getSelectedItem().toString());

        return 0;
    }

    /**
     * Updates the panel to show only optical solutions defined for the beamline to which beam is currently allocated.
     * @param pBeamline the beamline to which beam is currently allocated or null if beam is not allocated
     */
    public void updatePanel(Beamline pBeamline)
    {
        mOpticalSolutionComboBox.setSelectedIndex(-1);
        mOpticalSolutionComboBox.removeAllItems();
        if (pBeamline != null)
        {
            for (String opticalSolution : pBeamline.getOpticalSolutions())
            {
                mOpticalSolutionComboBox.addItem(opticalSolution);
            }
            mOpticalSolutionComboBox.setSelectedIndex(0);
        }
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

    /**
     * Updates range parameters.
     * <p>
     * To be called from the Swing thread.
     * @param pRange the range to achieve, in g/cm²
     * @param pOpticalSolution the optical solution id
     * @param pGantryAngle the gantry angle, in degrees, 90 in case of FBTR or FSTR
     * @param pOffsetX the horizontal offset to achieve on IC1, in mm
     * @param pOffsetY the vertical offset to achieve on IC1, in mm
     * @param pCycling true to do magnet cycling before applying setpoints, false otherwise
     */
    public void updateRangeInfo(double pRange, String pOpticalSolution, double pGantryAngle, double pOffsetX, double pOffsetY,
                                boolean pCycling)
    {
        mRangeTextField.setValue(pRange);
        mOpticalSolutionComboBox.setSelectedItem(pOpticalSolution);
        mGantryAngleTextField.setValue(pGantryAngle);
        mOffsetXTextField.setValue(pOffsetX);
        mOffsetYTextField.setValue(pOffsetY);
        mCyclingCheckBox.setSelected(pCycling);
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