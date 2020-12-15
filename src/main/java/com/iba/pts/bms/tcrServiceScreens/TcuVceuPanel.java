//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.iba.pts.bms.tcrServiceScreens;

import com.iba.ialign.Controller;
import com.iba.icomp.devices.Device.State;
import com.iba.icompx.ui.binding.*;
import com.iba.icompx.ui.panel.BooleanValueLabel;
import com.iba.icompx.ui.panel.CustomProgressBar;
import com.iba.icompx.ui.panel.EnumValueLabel;
import com.iba.icompx.ui.panel.CustomProgressBar.Orientation;
import com.iba.icompx.ui.service.UiUtils;
import com.iba.icompx.ui.service.UiUtils.JColoredButton;
import com.iba.pts.bms.bds.devices.api.TcuVariableCollimator;
import com.iba.pts.bms.bds.devices.api.TcuVariableCollimator.AutoMoveRequest;
import com.iba.pts.bms.tcrServiceScreens.actions.VceuActionFactory;
import com.jgoodies.binding.adapter.Bindings;
import com.jgoodies.binding.beans.BeanAdapter;
import com.jgoodies.binding.value.ConverterFactory;
import com.jgoodies.binding.value.ValueModel;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.FormLayout;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import javax.swing.*;

public class TcuVceuPanel extends BdsServicePanel<TcuVariableCollimator> implements ActionListener {
    private TcuVceuJawsPanel mPanelX = new TcuVceuJawsPanel("X");
    private TcuVceuJawsPanel mPanelY = new TcuVceuJawsPanel("Y");
    private BooleanValueLabel mLabelXOpen;
    private BooleanValueLabel mLabelXClosed;
    private BooleanValueLabel mLabelYOpen;
    private BooleanValueLabel mLabelYClosed;
    private BooleanValueLabel mLabelTssCondition;
    private BooleanValueLabel mLabelTssXInPlace;
    private BooleanValueLabel mLabelTssYInPlace;
    private EnumValueLabel<State> mLabelOperationStatus;
    private JComponent mBarYUp;
    private JComponent mBarYDown;
    private JComponent mBarXLeft;
    private JComponent mBarXRight;
    public DeviceActionModel model;

    private VceuActionFactory VceuActions;

    @WidgetEnableCondition(
            conditions = {BeanCondition.VceuXNotOpen}
    )
    private JColoredButton mButtonAutoOpen;
    @WidgetEnableCondition(
            conditions = {BeanCondition.VceuXNotClosed}
    )
    private JColoredButton mButtonAutoClose;
    private JColoredButton mButtonAutoCalib;
    @ReadOnlyWidget
    private JTextField[] mTextfieldA0CalibPot;
    @ReadOnlyWidget
    private JTextField[] mTextfieldA1CalibPot;

    public TcuVceuPanel() {

//
//        //System.out.println(VceuProxy.getXOpenFeedback());
//        initializeElements(VceuProxy);
//        layoutElements(VceuProxy);
//        BeanAdapter<TcuVariableCollimator> vceu = new BeanAdapter(VceuProxy, true);
//
//        ValueModel vm = new EqualsConverter(true, vceu.getValueModel(TcuVariableCollimatorsProxyImpl.DEVICE_STATE_PROPERTY), State.READY, State.FAULTY);
//
//
//        CallbackUtilityImpl callback = new CallbackUtilityImpl();
//        callback.get();
//
//
//
//        //model = new DeviceActionModel(VceuProxy);
//
//
//        //AbstractActivityInteractingWithTcu.DefaultTcuTreatmentControllerRpcCallCallback test = new AbstractActivityInteractingWithTcu.DefaultTcuTreatmentControllerRpcCallCallback();
//        CallbackUtilityImpl asfd = new CallbackUtilityImpl();
//        asfd.registerCallback(new Callback() {
//            @Override
//            public void callSucceeded(Object... pResult) {
//                System.out.println("Success");
//            }
//
//            @Override
//            public void callFailed(Object... pResult) {
//                System.out.println("Failure");
//            }
//
//            @Override
//            public void callTimedOut() {
//                System.out.println("Timeout");
//            }
//        });
//
//        CallbackValueModel cbmodel = new CallbackValueModel();
//        cbmodel.setCallbackUtility(asfd);
//
//
//        model = new DeviceActionModel(asfd, VceuProxy);
//
//        ApplicationContext context = new ClassPathXmlApplicationContext("xml/config/bms/bms/controller/impl/properties/ui.service.uniNozzle.xml");
//
//        model = (DeviceActionModel) context.getBean("nozzleCuActionModel") ;
//
//        //this.mLabelTssCondition.setBoolValue(VceuProxy.isSafetyConditionOk());
//
//        //ValueModel test = ConverterFactory.createBooleanNegator(
//               // new EqualsConverter(true, vceu.getValueModel(TcuVariableCollimatorsProxyImpl.DEVICE_STATE_PROPERTY), State.BUSY, State.UNKNOWN, State.FAULTY));
//
//
//                //new EqualsConverter(true, vceu.getValueModel(TcuVariableCollimatorsProxyImpl.DEVICE_STATE_PROPERTY), State.READY, State.FAULTY);
//
//                //vceu.getValueModel(TcuVariableCollimatorsProxyImpl.FULL_DEVICE_NAME);
//        bindElements(model, VceuProxy);
    }

    protected void initializeElements(TcuVariableCollimator pVceu) {
        pVceu = Controller.beam.VCEU3;
        this.mPanelX.initializeElements(pVceu);
        this.mPanelY.initializeElements(pVceu);
        this.mLabelXOpen = new BooleanValueLabel(com.iba.icompx.ui.panel.EnumValueLabel.State.INFO, com.iba.icompx.ui.panel.EnumValueLabel.State.OK, new String[]{"LS1"});
        this.mLabelXClosed = new BooleanValueLabel(com.iba.icompx.ui.panel.EnumValueLabel.State.INFO, com.iba.icompx.ui.panel.EnumValueLabel.State.OK, new String[]{"LS2"});
        this.mLabelYOpen = new BooleanValueLabel(com.iba.icompx.ui.panel.EnumValueLabel.State.INFO, com.iba.icompx.ui.panel.EnumValueLabel.State.OK, new String[]{"LS3"});
        this.mLabelYClosed = new BooleanValueLabel(com.iba.icompx.ui.panel.EnumValueLabel.State.INFO, com.iba.icompx.ui.panel.EnumValueLabel.State.OK, new String[]{"LS4"});
        this.mLabelTssCondition = new BooleanValueLabel(new String[]{"TSS Condition"});
        this.mLabelTssXInPlace = new BooleanValueLabel(new String[]{"X Jaws in place"});
        this.mLabelTssYInPlace = new BooleanValueLabel(new String[]{"Y Jaws in place"});
        this.mLabelOperationStatus = UiUtils.getDeviceStateLabel();
        this.mBarXLeft = new CustomProgressBar(Orientation.LEFTWARDS, false, true);
        this.mBarXRight = new CustomProgressBar(Orientation.RIGHTWARDS, false, true);
        this.mBarYUp = new CustomProgressBar(Orientation.UPWARDS, false, true);
        this.mBarYDown = new CustomProgressBar(Orientation.DOWNWARDS, false, true);
        int thick = 30;
        int length = 150;
        this.mBarXLeft.setPreferredSize(new Dimension(length, thick));
        this.mBarXRight.setPreferredSize(new Dimension(length, thick));
        this.mBarYUp.setPreferredSize(new Dimension(thick, length));
        this.mBarYDown.setPreferredSize(new Dimension(thick, length));
        this.mButtonAutoOpen = UiUtils.getButton("Auto open");
        this.mButtonAutoOpen.setName("[BMS_TCR_SS_VC_AUTO_OPEN_BTN]");
        this.mButtonAutoClose = UiUtils.getButton("Auto close");
        this.mButtonAutoClose.setName("[BMS_TCR_SS_VC_AUTO_CLOSE_BTN]");
        this.mButtonAutoCalib = UiUtils.getButton("Auto Calibrate");
        this.mButtonAutoCalib.setName("[BMS_TCR_SS_VC_AUTO_CALIBRATE_BTN]");
        this.mTextfieldA0CalibPot = new JTextField[4];
        this.mTextfieldA1CalibPot = new JTextField[4];

        for(int i = 0; i < 4; ++i) {
            this.mTextfieldA0CalibPot[i] = UiUtils.getDoubleTextField(false);
            this.mTextfieldA1CalibPot[i] = UiUtils.getDoubleTextField(false);
        }

    }

    protected void layoutElements(TcuVariableCollimator pVceu) {
        pVceu = Controller.beam.VCEU3;
        this.mPanelX.layoutElements(pVceu);
        this.mPanelY.layoutElements(pVceu);
        JPanel mainPanel = new JPanel(new BorderLayout());
        JPanel northPanel = new JPanel(new BorderLayout());
        northPanel.add(this.mPanelX, "West");
        northPanel.add(this.mPanelY, "East");
        DefaultFormBuilder graphBuilder = new DefaultFormBuilder(new FormLayout("pref,3dlu,pref,3dlu,pref"));
        graphBuilder.append(new JLabel(), this.mBarYUp, new JLabel());
        graphBuilder.append(this.mBarXLeft, new JLabel(), this.mBarXRight);
        graphBuilder.append(new JLabel(), this.mBarYDown, new JLabel());
        graphBuilder.getPanel().setBorder(Borders.DLU4_BORDER);
        JPanel graphPanelOuter = new JPanel(new FlowLayout());
        graphPanelOuter.add(graphBuilder.getPanel());
        northPanel.add(graphPanelOuter, "Center");
        mainPanel.add(northPanel, "North");
        DefaultFormBuilder centerWestBuilder = new DefaultFormBuilder(new FormLayout("pref,10dlu,pref,10dlu,pref"));
        JPanel centerWestPanel = centerWestBuilder.getPanel();
        centerWestPanel.setBorder(UiUtils.getTitledBorder("Status"));
        centerWestBuilder.append("", new JLabel("Opened LS"), new JLabel("Closed LS"));
        centerWestBuilder.append("X jaws:", this.mLabelXOpen, this.mLabelXClosed);
        centerWestBuilder.append("Y jaws:", this.mLabelYOpen, this.mLabelYClosed);
        centerWestBuilder.appendSeparator();
        centerWestBuilder.append(new JLabel("Operation status:"));
        centerWestBuilder.append(this.mLabelOperationStatus, 3);
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(centerWestPanel, "West");
        mainPanel.add(centerPanel, "Center");
        DefaultFormBuilder centerCenterBuilder = new DefaultFormBuilder(new FormLayout("pref,10dlu,pref"));
        centerCenterBuilder.append(this.mLabelTssCondition, 3);
        centerCenterBuilder.append(new JLabel(), this.mLabelTssXInPlace);
        centerCenterBuilder.append(new JLabel(), this.mLabelTssYInPlace);
        JPanel centerCenterPanel = centerCenterBuilder.getPanel();
        centerCenterPanel.setBorder(UiUtils.getTitledBorder("TSS interlocks (BMS calculated)"));
        centerPanel.add(centerCenterPanel, "Center");
        DefaultFormBuilder centerEastBuilder = new DefaultFormBuilder(new FormLayout("pref,3dlu,pref"));
        centerEastBuilder.append(this.mButtonAutoOpen, this.mButtonAutoClose);
        centerEastBuilder.append(this.mButtonAutoCalib, 3);
        JPanel centerEastPanel = centerEastBuilder.getPanel();
        centerEastPanel.setBorder(UiUtils.getTitledBorder("Controls"));
        centerPanel.add(centerEastPanel, "East");
        DefaultFormBuilder southBuilder = new DefaultFormBuilder(new FormLayout("pref,10dlu,pref,3dlu,pref,10dlu,pref,3dlu,pref"));
        southBuilder.append("", new JLabel("A0 (offset)"), 3);
        southBuilder.append(new JLabel("A1 (slope)"), 3);

        for(int i = 0; i < 4; ++i) {
            southBuilder.append(new JLabel("Pot " + (i + 1)), this.mTextfieldA0CalibPot[i], new JLabel("V"));
            southBuilder.append(this.mTextfieldA1CalibPot[i], new JLabel("V"));
        }

        southBuilder.getPanel().setBorder(UiUtils.getTitledBorder("Calibration factors"));
        this.add(southBuilder.getPanel(), "South");
        this.add(mainPanel, "Center");
    }

    protected void bindElements(ValueModel pActionModel, TcuVariableCollimator pVceu) {
        pVceu = Controller.beam.VCEU3;
        this.mPanelX.bindElements(pActionModel, pVceu);
        this.mPanelY.bindElements(pActionModel, pVceu);
        BeanAdapter<TcuVariableCollimator> vceu = new BeanAdapter(pVceu, true);
        ValueModel operationStatus = vceu.getValueModel("deviceState");
        ArrayList<String> descriptions = new ArrayList();
        descriptions.add("Variable collimator OK for request : ");
        DescribedValueModel anyActionAllowed = new DescribedBooleansToBooleanConverter(descriptions, false, new ValueModel[]{pActionModel, new EqualsConverter(true, operationStatus, (Object[])UiUtils.getDeviceStatesOKForRequest())});
        descriptions.clear();
        descriptions.add("X jaw not opened : ");
        descriptions.add("Y jaw not opened : ");
        DescribedValueModel notOpen = new DescribedBooleansToBooleanConverter(descriptions, true, new ValueModel[]{vceu.getValueModel("XNotOpen"), vceu.getValueModel("YNotOpen")});
        descriptions.clear();
        descriptions.add("X jaw not closed : ");
        descriptions.add("Y jaw not closed : ");
        DescribedValueModel notClosed = new DescribedBooleansToBooleanConverter(descriptions, true, new ValueModel[]{vceu.getValueModel("XNotClosed"), vceu.getValueModel("YNotClosed")});
        Bindings.bind(this.mBarXLeft, "doubleMaximum", new ReadOnlyConverter(vceu.getValueModel("XMaxOpenSetpoint")));
        Bindings.bind(this.mBarXLeft, "doubleValue", new ReadOnlyConverter(vceu.getValueModel("XOpenFeedback")));
        Bindings.bind(this.mBarXRight, "doubleMaximum", new ReadOnlyConverter(vceu.getValueModel("XMaxOpenSetpoint")));
        Bindings.bind(this.mBarXRight, "doubleValue", new ReadOnlyConverter(vceu.getValueModel("XOpenFeedback")));
        Bindings.bind(this.mBarYDown, "doubleMaximum", new ReadOnlyConverter(vceu.getValueModel("YMaxOpenSetpoint")));
        Bindings.bind(this.mBarYDown, "doubleValue", new ReadOnlyConverter(vceu.getValueModel("YOpenFeedback")));
        Bindings.bind(this.mBarYUp, "doubleMaximum", new ReadOnlyConverter(vceu.getValueModel("YMaxOpenSetpoint")));
        Bindings.bind(this.mBarYUp, "doubleValue", new ReadOnlyConverter(vceu.getValueModel("YOpenFeedback")));
        Bindings.bind(this.mLabelXClosed, "boolValue", ConverterFactory.createBooleanNegator(vceu.getValueModel("XNotClosed")));
        Bindings.bind(this.mLabelYClosed, "boolValue", ConverterFactory.createBooleanNegator(vceu.getValueModel("YNotClosed")));
        Bindings.bind(this.mLabelXOpen, "boolValue", ConverterFactory.createBooleanNegator(vceu.getValueModel("XNotOpen")));
        Bindings.bind(this.mLabelYOpen, "boolValue", ConverterFactory.createBooleanNegator(vceu.getValueModel("YNotOpen")));
        Bindings.bind(this.mLabelOperationStatus, "value", operationStatus);
        Bindings.bind(this.mLabelTssCondition, "boolValue", vceu.getValueModel("safetyConditionOk"));
        Bindings.bind(this.mLabelTssXInPlace, "boolValue", vceu.getValueModel("XInPlace"));
        Bindings.bind(this.mLabelTssYInPlace, "boolValue", vceu.getValueModel("YInPlace"));
        //this.mButtonAutoOpen.addActionListener(VceuActionFactory.createAutoMoveAction((TcuVariableCollimatorsProxyImpl)pVceu, AutoMoveRequest.AUTO_OPEN));
//        this.mButtonAutoOpen.setActionCommand("AutoOpen");
//        this.mButtonAutoOpen.addActionListener(this);
        this.mButtonAutoOpen.addActionListener(VceuActionFactory.createAutoMoveAction(pActionModel, pVceu, AutoMoveRequest.AUTO_OPEN));
        Bindings3.bindColoredButton(this.mButtonAutoOpen, "enabled", new DescribedBooleansToBooleanConverter(false, new DescribedValueModel[]{anyActionAllowed, notOpen}));
        this.mButtonAutoClose.addActionListener(VceuActionFactory.createAutoMoveAction( pActionModel, pVceu, AutoMoveRequest.AUTO_CLOSE));
        Bindings3.bindColoredButton(this.mButtonAutoClose, "enabled", new DescribedBooleansToBooleanConverter(false, new DescribedValueModel[]{anyActionAllowed, notClosed}));
        this.mButtonAutoCalib.addActionListener(VceuActionFactory.createAutoCalibrationAction(pActionModel, pVceu));
        Bindings3.bindColoredButton(this.mButtonAutoCalib, "enabled", anyActionAllowed);

        for(int i = 0; i < 4; ++i) {
            Bindings.bind(this.mTextfieldA0CalibPot[i], "value", new ArraySelector(vceu, "potentiometerCalibA0", i));
            Bindings.bind(this.mTextfieldA1CalibPot[i], "value", new ArraySelector(vceu, "potentiometerCalibA1", i));
        }

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        switch (e.getActionCommand()) {
            case "AutoOpen":

                Controller.beam.VCEU3.initialize();
                Controller.beam.VCEU3.proxyPublish();

                break;

        }
    }
}
