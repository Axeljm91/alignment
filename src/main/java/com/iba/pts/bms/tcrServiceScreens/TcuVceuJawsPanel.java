//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.iba.pts.bms.tcrServiceScreens;

import com.iba.ialign.Controller;
import com.iba.icomp.devices.Device.State;
import com.iba.icompx.ui.binding.Bindings3;
import com.iba.icompx.ui.binding.DescribedValueModel;
import com.iba.icompx.ui.binding.EqualsConverter;
import com.iba.icompx.ui.service.UiUtils;
import com.iba.icompx.ui.service.ServicePanel.NonWidget;
import com.iba.icompx.ui.service.ServicePanel.ReadOnlyWidget;
import com.iba.icompx.ui.service.UiUtils.JColoredButton;
import com.iba.pts.bms.bds.devices.api.TcuVariableCollimator;
import com.iba.pts.bms.tcrServiceScreens.actions.VceuActionFactory;
import com.jgoodies.binding.adapter.Bindings;
import com.jgoodies.binding.beans.BeanAdapter;
import com.jgoodies.binding.value.ConverterFactory;
import com.jgoodies.binding.value.ValueModel;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import java.util.ArrayList;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;

public class TcuVceuJawsPanel extends BdsServicePanel<TcuVariableCollimator> {
    @NonWidget
    private final String mJawType;
    @ReadOnlyWidget
    private JFormattedTextField mTextfieldFeedbackVolt1;
    @ReadOnlyWidget
    private JFormattedTextField mTextfieldFeedbackVolt2;
    @ReadOnlyWidget
    private JFormattedTextField mTextfieldFeedbackCm1;
    @ReadOnlyWidget
    private JFormattedTextField mTextfieldFeedbackCm2;
    private JFormattedTextField mTextfieldSetpointVolt;
    @ReadOnlyWidget
    private JFormattedTextField mTextfieldSetpointVoltFeedback;
    private JColoredButton mSetpointVoltSubmit;
    private JFormattedTextField mTextfieldSetpointCm;
    @ReadOnlyWidget
    private JFormattedTextField mTextfieldSetpointCmFeedback;
    private JColoredButton mSetpointCmSubmit;

    public TcuVceuJawsPanel(String pJawType) {
        this.mJawType = pJawType;
    }

    protected void initializeElements(TcuVariableCollimator pVceu) {
        this.mTextfieldSetpointVolt = UiUtils.getDoubleTextField(true);
        this.mTextfieldSetpointVoltFeedback = UiUtils.getDoubleTextField(false);
        this.mSetpointVoltSubmit = UiUtils.getButton("Set");
        this.mSetpointVoltSubmit.setName("[BMS_TCR_SS_VC_VOLT_SUBMIT_BTN]");
        this.mTextfieldFeedbackVolt1 = UiUtils.getDoubleTextField(false);
        this.mTextfieldFeedbackVolt2 = UiUtils.getDoubleTextField(false);
        this.mTextfieldSetpointCm = UiUtils.getDoubleTextField(true);
        this.mTextfieldSetpointCmFeedback = UiUtils.getDoubleTextField(false);
        this.mSetpointCmSubmit = UiUtils.getButton("Set");
        this.mSetpointCmSubmit.setName("[BMS_TCR_SS_VC_CM_SUBMIT_BTN]");
        this.mTextfieldFeedbackCm1 = UiUtils.getDoubleTextField(false);
        this.mTextfieldFeedbackCm2 = UiUtils.getDoubleTextField(false);
    }

    protected void layoutElements(TcuVariableCollimator pVceu) {
        String pot1Name = this.mJawType.equals("X") ? "Pot 1" : "Pot 2";
        String pot2Name = this.mJawType.equals("X") ? "Pot 3" : "Pot 4";
        this.setBorder(UiUtils.getTitledBorder(this.mJawType + " Jaws"));
        DefaultFormBuilder builder = new DefaultFormBuilder(new FormLayout("pref,10dlu,pref,3dlu,pref"));
        builder.appendSeparator("Volts setpoint");
        builder.append("", this.mTextfieldSetpointVolt, this.mSetpointVoltSubmit);
        builder.append("", this.mTextfieldSetpointVoltFeedback, new JLabel("V"));
        builder.appendSeparator("Volts feedback");
        builder.append(new JLabel(pot1Name), this.mTextfieldFeedbackVolt1, new JLabel("V"));
        builder.append(new JLabel(pot2Name), this.mTextfieldFeedbackVolt2, new JLabel("V"));
        builder.appendSeparator("Opening setpoint");
        builder.append("", this.mTextfieldSetpointCm, this.mSetpointCmSubmit);
        builder.append("", this.mTextfieldSetpointCmFeedback, new JLabel("cm"));
        builder.appendSeparator("Opening feedback");
        builder.append(new JLabel(pot1Name), this.mTextfieldFeedbackCm1, new JLabel("cm"));
        builder.append(new JLabel(pot2Name), this.mTextfieldFeedbackCm2, new JLabel("cm"));
        this.add(builder.getPanel());
    }

    protected void bindElements(ValueModel pActionModel, TcuVariableCollimator pVceu) {
        BeanAdapter<TcuVariableCollimator> vceu = new BeanAdapter(pVceu, true);
        ValueModel notMoving = ConverterFactory.createBooleanNegator(new EqualsConverter(true, vceu.getValueModel("deviceState"), new Object[]{State.BUSY, State.UNKNOWN}));
        ArrayList<String> descriptions = new ArrayList();
        descriptions.add("Variable collimator not moving : ");
        DescribedValueModel anyActionAllowed = new DescribedBooleansToBooleanConverter(descriptions, false, new ValueModel[]{pActionModel, notMoving});
        this.mSetpointCmSubmit.addActionListener(VceuActionFactory.createSetpointCmSubmitAction(pActionModel, pVceu, this.mJawType, this.mTextfieldSetpointCm));
        Bindings.bind(this.mTextfieldSetpointCm, "editable", anyActionAllowed);
        this.mSetpointVoltSubmit.addActionListener(VceuActionFactory.createSetpointVoltSubmitAction(pActionModel, pVceu, this.mJawType, this.mTextfieldSetpointVolt));
        Bindings.bind(this.mTextfieldSetpointVolt, "editable", anyActionAllowed);
        Bindings3.bindColoredButton(this.mSetpointCmSubmit, "enabled", anyActionAllowed);
        Bindings3.bindColoredButton(this.mSetpointVoltSubmit, "enabled", anyActionAllowed);
        Bindings.bind(this.mTextfieldSetpointCmFeedback, "value", vceu.getValueModel(this.mJawType + "OpenSetpoint"));
        Bindings.bind(this.mTextfieldFeedbackCm1, "value", vceu.getValueModel(this.mJawType + "OpenFeedback"));
        Bindings.bind(this.mTextfieldFeedbackCm2, "value", vceu.getValueModel(this.mJawType + "OpenFeedbackRedundant"));
        Bindings.bind(this.mTextfieldSetpointVoltFeedback, "value", vceu.getValueModel(this.mJawType + "VoltSetpoint"));
        Bindings.bind(this.mTextfieldFeedbackVolt1, "value", vceu.getValueModel(this.mJawType + "VoltFeedback"));
        Bindings.bind(this.mTextfieldFeedbackVolt2, "value", vceu.getValueModel(this.mJawType + "VoltFeedbackRedundant"));
    }

}
