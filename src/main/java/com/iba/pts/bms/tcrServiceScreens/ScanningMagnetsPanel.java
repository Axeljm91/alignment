//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.iba.pts.bms.tcrServiceScreens;

import com.iba.ialign.Controller;
import com.iba.icompx.ui.binding.BooleanNegator;
import com.iba.icompx.ui.panel.BooleanValueLabel;
import com.iba.icompx.ui.panel.EnumValueLabel;
import com.iba.icompx.ui.panel.EnumValueLabel.State;
import com.iba.icompx.ui.service.UiUtils;
import com.iba.icompx.ui.service.UiUtils.JColoredButton;
import com.iba.pts.bms.bds.devices.api.ScanningMagnets;
import com.iba.pts.bms.bds.devices.api.ScanningMagnets.GeneratorMode;
import com.iba.pts.bms.bds.tcu.devices.ScanningMagnetsImpl;
import com.iba.pts.bms.tcrServiceScreens.actions.ScanningMagnetsActionFactory;
import com.jgoodies.binding.adapter.Bindings;
import com.jgoodies.binding.beans.BeanAdapter;
import com.jgoodies.binding.value.ValueModel;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.FormLayout;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class ScanningMagnetsPanel extends BdsServicePanel<ScanningMagnets> {
    private JColoredButton mButtonSetPsVoltage;
    private JColoredButton mButtonSetPhaseShift;
    private JFormattedTextField mTextfieldSetPhaseShift;
    private ScanValuesPanel mPanelScan30HzValues;
    private ScanValuesPanel mPanelScan3HzValues;
    private EnumValueLabel<GeneratorMode> mLabelGeneratorMode;
    @WidgetEnableCondition(
            conditions = {BeanCondition.SmGeneratorModeExternal}
    )
    private JColoredButton mButtonSetGeneratorModeInternal;
    @WidgetEnableCondition(
            conditions = {BeanCondition.SmGeneratorModeInternal}
    )
    private JColoredButton mButtonSetGeneratorModeExternal;
    private BooleanValueLabel mLabelTssConditionSmeu;
    private BooleanValueLabel mLabelSmpsRemote;
    @NonWidget
    private boolean mSmpsJema = false;

    public ScanningMagnetsPanel() {
        ScanningMagnetsImpl SmeuProxy = Controller.beam.SMEU3;
        setPanel3Hz(new ScanValuesPanel(SmeuProxy, ScanningMagnets.ScanFrequency.Scan3Hz));
        setPanel30Hz(new ScanValuesPanel(SmeuProxy, ScanningMagnets.ScanFrequency.Scan30Hz));
        initializeElements(SmeuProxy);
        layoutElements(SmeuProxy);
        BeanAdapter<ScanningMagnets> scanningMagnets = new BeanAdapter(SmeuProxy, true);
        ValueModel smeuModel = scanningMagnets.getValueModel("generatorMode");

        bindElements(smeuModel, SmeuProxy);
        //SmeuProxy.get30
    }

    public void setSmpsType(String pSmpsType) {
        this.mSmpsJema = pSmpsType.toUpperCase().contains("JEMA");
    }

    public void setPanel30Hz(ScanValuesPanel pPanelScan30HzValues) {
        this.mPanelScan30HzValues = pPanelScan30HzValues;
    }

    public void setPanel3Hz(ScanValuesPanel pPanelScan3HzValues) {
        this.mPanelScan3HzValues = pPanelScan3HzValues;
    }

    protected void initializeElements(ScanningMagnets pScanningMagnets) {
        this.mButtonSetPsVoltage = UiUtils.getButton("Set voltage");
        if (this.mSmpsJema) {
            this.mTextfieldSetPhaseShift = UiUtils.getDoubleTextField(true);
            this.mButtonSetPhaseShift = UiUtils.getButton("Set");
        }

        this.mLabelTssConditionSmeu = new BooleanValueLabel(new String[]{"SMEU comparators"});
        this.mLabelSmpsRemote = new BooleanValueLabel(new String[]{"SMPS in remote mode"});
        this.mLabelGeneratorMode = new EnumValueLabel(GeneratorMode.class, new State[]{State.INFO, State.INFO, State.WARNING});
        this.mButtonSetGeneratorModeInternal = UiUtils.getButton("Set Internal");
        this.mButtonSetGeneratorModeExternal = UiUtils.getButton("Set External");
    }

    protected void layoutElements(ScanningMagnets pScanningMagnets) {
        DefaultFormBuilder mainPanelBuilder = new DefaultFormBuilder(new FormLayout("pref,3dlu,pref"));
        JPanel mainPanel = mainPanelBuilder.getPanel();
        mainPanel.setBorder(Borders.DLU4_BORDER);
        this.add(mainPanel, "Center");
        this.mPanelScan30HzValues.setBorder(UiUtils.getTitledBorder("Scan 30Hz (Y)"));
        this.mPanelScan3HzValues.setBorder(UiUtils.getTitledBorder("Scan 3Hz (X)"));
        mainPanelBuilder.append(this.mButtonSetPsVoltage, 3);
        mainPanelBuilder.append(this.mPanelScan3HzValues, this.mPanelScan30HzValues);
        mainPanelBuilder.append(new JLabel("Comparator check depends on TCU treatment mode."), 3);
        mainPanelBuilder.append(new JLabel(" "), 3);
        DefaultFormBuilder psPanelBuilder;
        if (this.mSmpsJema) {
            psPanelBuilder = new DefaultFormBuilder(new FormLayout("pref,3dlu,pref,3dlu,pref,3dlu,pref"));
            psPanelBuilder.getPanel().setBorder(UiUtils.getTitledBorder("Phase Shift"));
            psPanelBuilder.append(new JLabel("Phase Shift :"));
            psPanelBuilder.append(this.mTextfieldSetPhaseShift);
            psPanelBuilder.append(this.mButtonSetPhaseShift);
            mainPanelBuilder.append(psPanelBuilder.getPanel(), 3);
            mainPanelBuilder.append(new JLabel(" "), 3);
        }

        psPanelBuilder = new DefaultFormBuilder(new FormLayout("pref,10dlu,70dlu"));
        JPanel psPanel = psPanelBuilder.getPanel();
        psPanel.setBorder(UiUtils.getTitledBorder("Power Supply Generator"));
        mainPanelBuilder.append(psPanel);
        psPanelBuilder.append(" Mode", this.mLabelGeneratorMode);
        psPanelBuilder.appendSeparator();
        psPanelBuilder.append(this.mButtonSetGeneratorModeInternal, 3);
        psPanelBuilder.append(this.mButtonSetGeneratorModeExternal, 3);
        DefaultFormBuilder tssPanelBuilder = new DefaultFormBuilder(new FormLayout("pref"));
        JPanel tssPanel = tssPanelBuilder.getPanel();
        tssPanel.setBorder(UiUtils.getTitledBorder("TSS Interlocks (BMS calculated)"));
        mainPanelBuilder.append(tssPanel);
        tssPanelBuilder.append(this.mLabelTssConditionSmeu);
        tssPanelBuilder.append(this.mLabelSmpsRemote);
    }

    protected void bindElements(ValueModel pActionModel, ScanningMagnets pScanningMagnets) {
        BeanAdapter<ScanningMagnets> scanningMagnets = new BeanAdapter(pScanningMagnets, true);
        ValueModel generatorMode = scanningMagnets.getValueModel("generatorMode");
        ActionListener setPsVoltageActionListener = ScanningMagnetsActionFactory.createSetPsVoltageAction(pActionModel, pScanningMagnets, this.mPanelScan3HzValues.getSetpointField(), this.mPanelScan30HzValues.getSetpointField());
        this.mButtonSetPsVoltage.addActionListener(setPsVoltageActionListener);
        //Bindings3.bindColoredButton(this.mButtonSetPsVoltage, "enabled", (DescribedValueModel)pActionModel);
        if (this.mSmpsJema) {
            ActionListener setPhaseShiftActionListener = ScanningMagnetsActionFactory.createSetPhaseShiftAction(pActionModel, pScanningMagnets, this.mTextfieldSetPhaseShift);
            this.mButtonSetPhaseShift.addActionListener(setPhaseShiftActionListener);
        }

        Bindings.bind(this.mLabelTssConditionSmeu, "boolValue", scanningMagnets.getValueModel("safetyConditionOk"));
        Bindings.bind(this.mLabelSmpsRemote, "boolValue", new BooleanNegator(scanningMagnets.getValueModel("localControlMode")));
        Bindings.bind(this.mLabelGeneratorMode, "value", generatorMode);
        this.mButtonSetGeneratorModeInternal.addActionListener(ScanningMagnetsActionFactory.createSelectGeneratorModeAction(pActionModel, pScanningMagnets, GeneratorMode.Internal));
        this.mButtonSetGeneratorModeExternal.addActionListener(ScanningMagnetsActionFactory.createSelectGeneratorModeAction(pActionModel, pScanningMagnets, GeneratorMode.External));
        ArrayList<String> descriptions = new ArrayList();
        descriptions.add("Generator mode is external : ");
        //Bindings3.bindColoredButton(this.mButtonSetGeneratorModeInternal, "enabled", new DescribedBooleansToBooleanConverter(descriptions, false, new ValueModel[]{pActionModel, new EqualsConverter(true, generatorMode, new Object[]{GeneratorMode.External})}));
        descriptions.clear();
        descriptions.add("Generator mode is internal : ");
        //Bindings3.bindColoredButton(this.mButtonSetGeneratorModeExternal, "enabled", new DescribedBooleansToBooleanConverter(descriptions, false, new ValueModel[]{pActionModel, new EqualsConverter(true, generatorMode, new Object[]{GeneratorMode.Internal})}));
    }
}
