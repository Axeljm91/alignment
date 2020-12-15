//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.iba.pts.bms.tcrServiceScreens;

import com.iba.icompx.ui.binding.BooleanNegator;
import com.iba.icompx.ui.panel.BooleanValueLabel;
import com.iba.icompx.ui.service.UiUtils;
import com.iba.pts.bms.bds.devices.api.ScanningMagnets;
import com.iba.pts.bms.bds.devices.api.ScanningMagnets.ScanFrequency;
import com.jgoodies.binding.adapter.Bindings;
import com.jgoodies.binding.beans.BeanAdapter;
import com.jgoodies.binding.value.ValueModel;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

import javax.swing.JFormattedTextField;
import javax.swing.JLabel;

public class ScanValuesPanel extends BdsServicePanel<ScanningMagnets> {
    @NonWidget
    private ScanFrequency mScanFrequency;
    private ScanningMagnets mScanningMagnets;
    private JFormattedTextField mTextfieldSetpointPsVoltage;
    @ReadOnlyWidget
    private JFormattedTextField mTextfieldSetpointPsVoltageFeedback;
    @ReadOnlyWidget
    private JFormattedTextField mTextfieldFeedbackPsCurrent;
    @ReadOnlyWidget
    private JFormattedTextField mTextfieldFeedbackPsVoltage;
    @ReadOnlyWidget
    private JFormattedTextField mTextfieldFeedbackPickupVoltage;
    @ReadOnlyWidget
    private JFormattedTextField mTextfieldFeedbackPickupField;
    private BooleanValueLabel mLabelRegulation;
    private BooleanValueLabel mLabelThermalSwitch;
    private BooleanValueLabel mLabelComparatorSmeu;

    public ScanValuesPanel(ScanningMagnets pScanningMagnets,ScanFrequency pScanFrequency) {
        this.mScanningMagnets = pScanningMagnets;
        this.mScanFrequency = pScanFrequency;

        BeanAdapter<ScanningMagnets> scanningMagnets = new BeanAdapter(pScanningMagnets, true);
        ValueModel ScanModel = scanningMagnets.getValueModel(ScanningMagnets.GENERATOR_MODE_PROPERTY);

        initializeElements(mScanningMagnets);
        layoutElements(mScanningMagnets);
        bindElements(ScanModel, mScanningMagnets);
    }

    public JFormattedTextField getSetpointField() {
        return this.mTextfieldSetpointPsVoltage;
    }

    protected void initializeElements(ScanningMagnets pScanningMagnets) {
        this.mTextfieldSetpointPsVoltage = UiUtils.getDoubleTextField(true);
        this.mTextfieldSetpointPsVoltage.setValue(0);
        this.mTextfieldSetpointPsVoltageFeedback = UiUtils.getDoubleTextField(false);
        this.mTextfieldFeedbackPsCurrent = UiUtils.getDoubleTextField(false);
        this.mTextfieldFeedbackPsVoltage = UiUtils.getDoubleTextField(false);
        this.mTextfieldFeedbackPickupVoltage = UiUtils.getDoubleTextField(false);
        this.mTextfieldFeedbackPickupField = UiUtils.getDoubleTextField(false);
        this.mLabelRegulation = new BooleanValueLabel(new String[]{"Regulation"});
        this.mLabelThermalSwitch = new BooleanValueLabel(new String[]{"Thermal Switch"});
        this.mLabelComparatorSmeu = new BooleanValueLabel(new String[]{"Comparator"});
    }

    protected void layoutElements(ScanningMagnets pScanningMagnets) {
        DefaultFormBuilder builder = new DefaultFormBuilder(new FormLayout("pref,10dlu,pref,3dlu,20dlu"));
        builder.appendSeparator("Power Supply Setpoint");
        builder.append(new JLabel("Voltage"), this.mTextfieldSetpointPsVoltage, new JLabel("V"));
        builder.append(new JLabel(""), this.mTextfieldSetpointPsVoltageFeedback, new JLabel("V"));
        builder.appendSeparator("Power Supply Feedbacks");
        builder.append(new JLabel("Current"), this.mTextfieldFeedbackPsCurrent, new JLabel("A"));
        builder.append(new JLabel("Voltage"), this.mTextfieldFeedbackPsVoltage, new JLabel("V"));
        builder.appendSeparator("Pickup Coil Feedbacks");
        builder.append(new JLabel("Voltage"), this.mTextfieldFeedbackPickupVoltage, new JLabel("V"));
        builder.append(new JLabel("Field"), this.mTextfieldFeedbackPickupField, new JLabel("T"));
        builder.appendSeparator("Status");
        builder.append(this.mLabelRegulation, 5);
        builder.append(this.mLabelThermalSwitch, 5);
        builder.append(this.mLabelComparatorSmeu, 5);
        this.add(builder.getPanel());
    }

    protected void bindElements(ValueModel pActionModel, ScanningMagnets pScanningMagnets) {
        BeanAdapter<ScanningMagnets> scanningMagnets = new BeanAdapter(pScanningMagnets, true);
        Bindings.bind(this.mTextfieldSetpointPsVoltageFeedback, "value", scanningMagnets.getValueModel("voltageSetpoint" + this.mScanFrequency.getPropertySuffix()));
        Bindings.bind(this.mTextfieldFeedbackPsCurrent, "value", scanningMagnets.getValueModel("psCurrentFeedback" + this.mScanFrequency.getPropertySuffix()));
        Bindings.bind(this.mTextfieldFeedbackPsVoltage, "value", scanningMagnets.getValueModel("psVoltageFeedback" + this.mScanFrequency.getPropertySuffix()));
        Bindings.bind(this.mTextfieldFeedbackPickupVoltage, "value", scanningMagnets.getValueModel("pickupCoilVoltage" + this.mScanFrequency.getPropertySuffix()));
        Bindings.bind(this.mTextfieldFeedbackPickupField, "value", scanningMagnets.getValueModel("fieldFeedbackInTesla" + this.mScanFrequency.getPropertySuffix()));
        Bindings.bind(this.mLabelRegulation, "boolValue", scanningMagnets.getValueModel("regulationOk" + this.mScanFrequency.getPropertySuffix()));
        Bindings.bind(this.mLabelThermalSwitch, "boolValue", new BooleanNegator(scanningMagnets.getValueModel("thermalSwitchNok" + this.mScanFrequency.getPropertySuffix())));
        Bindings.bind(this.mLabelComparatorSmeu, "boolValue", scanningMagnets.getValueModel("comparatorOk" + this.mScanFrequency.getPropertySuffix()));
        //Bindings.bind(this.mTextfieldSetpointPsVoltage, "enabled", pActionModel);
    }
}
