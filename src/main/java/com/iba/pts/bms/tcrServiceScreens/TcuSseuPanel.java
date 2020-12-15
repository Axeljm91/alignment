//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.iba.pts.bms.tcrServiceScreens;

import com.iba.ialign.Controller;
import com.iba.icomp.devices.Device.State;
import com.iba.icompx.ui.binding.BooleanToStringConverter;
import com.iba.icompx.ui.binding.EqualsConverter;
import com.iba.icompx.ui.panel.BooleanValueLabel;
import com.iba.icompx.ui.panel.EnumValueLabel;
import com.iba.icompx.ui.service.UiUtils;
import com.iba.icompx.ui.service.UiUtils.JColoredButton;
import com.iba.pts.bms.bds.devices.api.TcuSecondScatterer;
import com.iba.pts.bms.bds.devices.api.SecondScatterer.Position;
import com.iba.pts.bms.bds.tcu.devices.TcuSecondScattererImpl;
import com.jgoodies.binding.adapter.Bindings;
import com.jgoodies.binding.beans.BeanAdapter;
import com.jgoodies.binding.value.ConverterFactory;
import com.jgoodies.binding.value.ValueModel;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.FormLayout;
import java.util.ArrayList;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class TcuSseuPanel extends BdsServicePanel<TcuSecondScatterer> {
    private EnumValueLabel<State> mLabelOperationStatus;
    private BooleanValueLabel mLabelDriverOkStatus;
    private BooleanValueLabel mLabelDriverOnStatus;
    @WidgetEnableCondition(
            conditions = {BeanCondition.SseuJack0InPosition}
    )
    private JColoredButton mButtonJackControl;
    private JColoredButton mButtonDriverControl;
    @WidgetEnableCondition(
            conditions = {BeanCondition.SseuJack0NotInPosition, BeanCondition.SseuDriverOn}
    )
    private JFormattedTextField mTextfieldLowLevelPosInput;
    @ReadOnlyWidget
    private JFormattedTextField mTextfieldLowLevelPosSetpoint;
    @ReadOnlyWidget
    private JFormattedTextField mTextfieldLowLevelPosFeedback;
    @WidgetEnableCondition(
            conditions = {BeanCondition.SseuJack0NotInPosition, BeanCondition.SseuDriverOn}
    )
    private JColoredButton mButtonLowLevelPosSubmit;
    private EnumValueLabel<Position> mLabelHighLevelPosSetpoint;
    private EnumValueLabel<Position> mLabelHighLevelPosFeedback;
    private JColoredButton[] mButtonsHighLevelPosSubmit;
    private BooleanValueLabel mLabelTssCondition;
    private BooleanValueLabel mLabelTssJackInserted;
    private BooleanValueLabel mLabelTssDrawerInserted;

    public TcuSseuPanel() {
        TcuSecondScattererImpl SseuProxy = Controller.beam.SSEU3;
        initializeElements(SseuProxy);
        layoutElements(SseuProxy);
        BeanAdapter<TcuSecondScatterer> sseu = new BeanAdapter(SseuProxy, true);
        ValueModel SseuModel = sseu.getValueModel("drawerInserted");
        bindElements(SseuModel, SseuProxy);
    }

    protected void initializeElements(TcuSecondScatterer pSseu) {
        this.mLabelOperationStatus = UiUtils.getDeviceStateLabel();
        this.mLabelDriverOkStatus = new BooleanValueLabel(com.iba.icompx.ui.panel.EnumValueLabel.State.OK, com.iba.icompx.ui.panel.EnumValueLabel.State.ERROR, new String[]{"OK", "ERROR"});
        this.mLabelDriverOnStatus = new BooleanValueLabel(com.iba.icompx.ui.panel.EnumValueLabel.State.INFO, new String[]{"Off", "On"});
        this.mButtonJackControl = UiUtils.getButton("");
        this.mButtonJackControl.setName("[BMS_TCR_SS_SS_JACK_BTN]");
        this.mButtonDriverControl = UiUtils.getButton("");
        this.mButtonDriverControl.setName("[BMS_TCR_SS_SS_DRIVER_BTN]");
        this.mTextfieldLowLevelPosInput = UiUtils.getDoubleTextField(true);
        this.mTextfieldLowLevelPosSetpoint = UiUtils.getDoubleTextField(false);
        this.mTextfieldLowLevelPosFeedback = UiUtils.getDoubleTextField(false);
        this.mButtonLowLevelPosSubmit = UiUtils.getButton("Set");
        this.mButtonLowLevelPosSubmit.setName("[BMS_TCR_SS_SS_LOW_SET_BTN]");
        this.mLabelHighLevelPosSetpoint = new EnumValueLabel(Position.class, new com.iba.icompx.ui.panel.EnumValueLabel.State[0]);
        this.mLabelHighLevelPosFeedback = new EnumValueLabel(Position.class, new com.iba.icompx.ui.panel.EnumValueLabel.State[0]);
        this.mButtonsHighLevelPosSubmit = new JColoredButton[4];

        for(int i = 0; i < this.mButtonsHighLevelPosSubmit.length; ++i) {
            this.mButtonsHighLevelPosSubmit[i] = UiUtils.getButton(Position.values()[i + 1] + " (LS" + (i + 3) + ")");
        }

        this.mLabelTssCondition = new BooleanValueLabel(new String[]{"TSS Condition"});
        this.mLabelTssJackInserted = new BooleanValueLabel(new String[]{"Jack inserted (LS1)"});
        this.mLabelTssDrawerInserted = new BooleanValueLabel(new String[]{"Drawer inserted (LS2)"});
    }

    protected void layoutElements(TcuSecondScatterer pSseu) {
        DefaultFormBuilder mainPanelBuilder = new DefaultFormBuilder(new FormLayout("pref,3dlu,pref"));
        JPanel mainPanel = mainPanelBuilder.getPanel();
        mainPanel.setBorder(Borders.DLU4_BORDER);
        this.add(mainPanel, "Center");
        DefaultFormBuilder lowLevelBuilder = new DefaultFormBuilder(new FormLayout("67dlu,3dlu,50dlu"));
        lowLevelBuilder.append("Jack control", this.mButtonJackControl);
        lowLevelBuilder.append("Driver control", this.mButtonDriverControl);
        lowLevelBuilder.appendSeparator("Volt setpoint");
        lowLevelBuilder.append(new JLabel("Requires jack retracted and driver on."), 3);
        lowLevelBuilder.append(this.mTextfieldLowLevelPosInput, this.mButtonLowLevelPosSubmit);
        lowLevelBuilder.append(this.mTextfieldLowLevelPosSetpoint, new JLabel("V"));
        lowLevelBuilder.appendSeparator("Volt feedback");
        lowLevelBuilder.append(this.mTextfieldLowLevelPosFeedback, new JLabel("V"));
        JPanel lowLevelPanel = lowLevelBuilder.getPanel();
        lowLevelPanel.setBorder(UiUtils.getTitledBorder("Low level commands"));
        mainPanelBuilder.append(lowLevelPanel);
        DefaultFormBuilder highLevelBuilder = new DefaultFormBuilder(new FormLayout("65dlu,3dlu,60dlu"));
        highLevelBuilder.append("Position setpoint", this.mLabelHighLevelPosSetpoint);
        highLevelBuilder.append("Position feedback", this.mLabelHighLevelPosFeedback);
        highLevelBuilder.appendSeparator("Position selection");

        for(int i = 0; i < 4; ++i) {
            highLevelBuilder.append(this.mButtonsHighLevelPosSubmit[i], 3);
        }

        JPanel highLevelPanel = highLevelBuilder.getPanel();
        highLevelPanel.setBorder(UiUtils.getTitledBorder("High level commands"));
        mainPanelBuilder.append(highLevelPanel);
        DefaultFormBuilder statusBuilder = new DefaultFormBuilder(new FormLayout("60dlu,10dlu,50dlu"));
        statusBuilder.append("Operation status", this.mLabelOperationStatus);
        statusBuilder.append("Driver On Status", this.mLabelDriverOnStatus);
        statusBuilder.append("Driver OK Status", this.mLabelDriverOkStatus);
        JPanel statusPanel = statusBuilder.getPanel();
        statusPanel.setBorder(UiUtils.getTitledBorder("Status"));
        mainPanelBuilder.append(statusPanel);
        DefaultFormBuilder tssBuilder = new DefaultFormBuilder(new FormLayout("pref,10dlu,pref"));
        tssBuilder.append(this.mLabelTssCondition, 3);
        tssBuilder.append(new JLabel(), this.mLabelTssJackInserted);
        tssBuilder.append(new JLabel(), this.mLabelTssDrawerInserted);
        JPanel tssPanel = tssBuilder.getPanel();
        tssPanel.setBorder(UiUtils.getTitledBorder("TSS interlocks (BMS calculated)"));
        mainPanelBuilder.append(tssPanel);
    }

    protected void bindElements(ValueModel pActionModel, TcuSecondScatterer pSseu) {
        BeanAdapter<TcuSecondScatterer> sseu = new BeanAdapter(pSseu, true);
        ValueModel deviceState = sseu.getValueModel("deviceState");
        ValueModel jackInserted = sseu.getValueModel("jackInserted");
        ValueModel driverOn = sseu.getValueModel("driverOn");
        ValueModel driverOk = sseu.getValueModel("driverStatusOk");
        ValueModel positionSetpoint = sseu.getValueModel("positionSetpoint");
        ValueModel position = sseu.getValueModel("position");
        ValueModel voltSetpoint = sseu.getValueModel("positionVoltSetpoint");
        ValueModel voltFeedback = sseu.getValueModel("positionVoltFeedback");
        ArrayList<String> descriptions = new ArrayList();
        descriptions.add("TCU second scatterer OK for request : ");
        //DescribedValueModel anyActionAllowed = new DescribedBooleansToBooleanConverter(descriptions, false, new ValueModel[]{pActionModel, new BooleanNegator(new EqualsConverter(true, deviceState, new Object[]{State.UNKNOWN, State.BUSY}))});
        descriptions.clear();
        descriptions.add("Jack is not inserted : ");
        descriptions.add("Driver is on : ");
        //DescribedValueModel allowedLowLevelSubmit = new DescribedBooleansToBooleanConverter(descriptions, false, new ValueModel[]{anyActionAllowed, new BooleanNegator(jackInserted), driverOn});
        ValueModel[] positionSetpoints = new ValueModel[]{ConverterFactory.createBooleanNegator(new EqualsConverter(positionSetpoint, Position.PASSTHROUGH)), ConverterFactory.createBooleanNegator(new EqualsConverter(positionSetpoint, Position.POSITION2)), ConverterFactory.createBooleanNegator(new EqualsConverter(positionSetpoint, Position.POSITION3)), ConverterFactory.createBooleanNegator(new EqualsConverter(positionSetpoint, Position.POSITION4))};
        Bindings.bind(this.mLabelOperationStatus, "value", deviceState);
        Bindings.bind(this.mLabelDriverOnStatus, "boolValue", driverOn);
        Bindings.bind(this.mLabelDriverOkStatus, "boolValue", driverOk);
        descriptions.clear();
        descriptions.add("Position is not unknown : ");
        //Bindings3.bindColoredButton(this.mButtonJackControl, "enabled", new DescribedBooleansToBooleanConverter(descriptions, false, new ValueModel[]{anyActionAllowed, new BooleanNegator(new EqualsConverter(position, Position.UNKNOWN))}));
        Bindings.bind(this.mButtonJackControl, "text", new BooleanToStringConverter(jackInserted, "Retract", "Retract", "Insert"));
        this.mButtonJackControl.addActionListener((pEvent) -> {
            boolean newValue = !pSseu.isJackInserted();
            if (newValue) {
                pActionModel.setValue("Insert second scatterer jack");
                pSseu.insertJack();
            } else {
                pActionModel.setValue("Retract second scatterer jack");
                pSseu.retractJack();
            }

        });
        //Bindings3.bindColoredButton(this.mButtonDriverControl, "enabled", anyActionAllowed);
        Bindings.bind(this.mButtonDriverControl, "text", new BooleanToStringConverter(driverOn, "Switch Off", "Switch Off", "Switch On"));
        this.mButtonDriverControl.addActionListener((pEvent) -> {
            boolean newValue = !pSseu.isDriverOn();
            pActionModel.setValue("Switch " + (newValue ? "on" : "off") + " second scatterer driver");
            pSseu.setDriverEnabled(newValue);
        });
        //Bindings3.bindColoredButton(this.mButtonLowLevelPosSubmit, "enabled", allowedLowLevelSubmit);
        this.mButtonLowLevelPosSubmit.addActionListener((pEvent) -> {
            Object value = this.mTextfieldLowLevelPosInput.getValue();
            if (value != null) {
                double newValue = ((Number)value).doubleValue();
                pActionModel.setValue("Set second scatterer volt position");
                pSseu.setPositionVoltSetpoint(newValue);
            }

        });
        //Bindings.bind(this.mTextfieldLowLevelPosInput, "editable", allowedLowLevelSubmit);
        Bindings.bind(this.mTextfieldLowLevelPosSetpoint, "value", voltSetpoint);
        Bindings.bind(this.mTextfieldLowLevelPosFeedback, "value", voltFeedback);
        Bindings.bind(this.mLabelHighLevelPosSetpoint, "value", sseu.getValueModel("positionSetpoint"));
        Bindings.bind(this.mLabelHighLevelPosFeedback, "value", sseu.getValueModel("position"));
        descriptions.clear();
        descriptions.add("Setpoint is not passthrough : ");
        descriptions.add("Setpoint is not position 2 : ");
        descriptions.add("Setpoint is not position 3 : ");
        descriptions.add("Setpoint is not position 4 : ");

        for(int i = 0; i < 4; ++i) {
            ArrayList<String> description = new ArrayList();
            description.add(descriptions.get(i));
            //Bindings3.bindColoredButton(this.mButtonsHighLevelPosSubmit[i], "enabled", new DescribedBooleansToBooleanConverter(description, false, new ValueModel[]{anyActionAllowed, positionSetpoints[i]}));
            int finalI = i;
            this.mButtonsHighLevelPosSubmit[i].addActionListener((pE) -> {
                Position position1 = Position.values()[finalI + 1];
                pActionModel.setValue("Set second scatterer " + position1);
                pSseu.setPosition(position1);
            });
        }

        Bindings.bind(this.mLabelTssCondition, "boolValue", sseu.getValueModel("safetyConditionOk"));
        Bindings.bind(this.mLabelTssJackInserted, "boolValue", jackInserted);
        Bindings.bind(this.mLabelTssDrawerInserted, "boolValue", sseu.getValueModel("drawerInserted"));
    }
}
