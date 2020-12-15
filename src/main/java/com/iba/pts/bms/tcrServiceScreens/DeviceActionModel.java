//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.iba.pts.bms.tcrServiceScreens;

import com.iba.icomp.core.component.CallbackUtility;
import com.iba.icomp.core.util.Assert;
import com.iba.icomp.devices.Device;
import com.iba.icomp.devices.Device.State;
import com.iba.icompx.ui.binding.BooleanNegator;
import com.iba.icompx.ui.binding.CallbackValueModel;
import com.iba.icompx.ui.binding.ContainerValueModel;
import com.iba.icompx.ui.binding.DescribedValueModel;
import com.iba.icompx.ui.binding.EqualsConverter;
import com.iba.icompx.ui.service.UiUtils;
import com.iba.pts.treatmentroomsession.TreatmentRoomSession;
import com.iba.pts.treatmentroomsession.TreatmentSessionModeHolder;
import com.jgoodies.binding.beans.BeanAdapter;
import com.jgoodies.binding.value.AbstractValueModel;
import com.jgoodies.binding.value.ValueModel;
import java.util.ArrayList;
import java.util.List;

public class DeviceActionModel extends AbstractValueModel implements DescribedValueModel {
    private static final long serialVersionUID = -7500789713045422308L;
    private final DescribedBooleansToBooleanConverter mOperationsAllowedValueModel;
    private final CallbackValueModel mCallbackValueModel;
    private DescriptionValueModel mDescriptionValueModel;

    public DeviceActionModel(CallbackUtility pCallbackUtility, Device pDevice) {
        this(pCallbackUtility, (ContainerValueModel)null, (TreatmentSessionModeHolder)null, pDevice);
    }

    public DeviceActionModel(Device pDevice) {
        this((CallbackUtility)null, (ContainerValueModel)null, (TreatmentSessionModeHolder)null, pDevice);
    }

    public DeviceActionModel(CallbackUtility pCallbackUtility, ContainerValueModel pBappModel, TreatmentSessionModeHolder pTreatmentSession, Device pDevice) {
        this(pCallbackUtility, pBappModel, pTreatmentSession, pDevice, true);
    }

    public DeviceActionModel(CallbackUtility pCallbackUtility, ContainerValueModel pBappModel, TreatmentSessionModeHolder pTreatmentSession, Device pDevice, boolean pCheckReady) {
        this.mDescriptionValueModel = new DescriptionValueModel();
        String deviceName = pDevice == null ? "" : pDevice.getDeviceName().replace("urn:device:", "").replaceFirst(":IBTR.", "");
        List<ValueModel> models = new ArrayList();
        ArrayList<String> descriptions = new ArrayList();
        if (pCallbackUtility != null) {
            this.mCallbackValueModel = new CallbackValueModel();
            this.mCallbackValueModel.setCallbackUtility(pCallbackUtility);
            models.add(this.mCallbackValueModel);
            descriptions.add("No previous " + deviceName + " call active : ");
        } else {
            this.mCallbackValueModel = null;
        }

        if (pBappModel != null) {
            models.add(pBappModel);
            descriptions.add(pBappModel.getContainer().getInstanceName() + " started and alive");
        }

        if (pTreatmentSession != null) {
            BeanAdapter<TreatmentSessionModeHolder> treatmentSession = new BeanAdapter(pTreatmentSession, true);
            ValueModel treatmentSessionModel = treatmentSession.getValueModel("treatmentRoomSession");
            ValueModel noTreatmentSession = new BooleanNegator(new EqualsConverter(treatmentSessionModel, TreatmentRoomSession.Treatment));
            models.add(noTreatmentSession);
            descriptions.add("Session not in treatment mode");
        }

        if (pDevice != null) {
            descriptions.add("Device " + deviceName + " ready");
            ValueModel deviceReady = new EqualsConverter(true, (new BeanAdapter(pDevice, true)).getValueModel("deviceState"), (Object[])(pCheckReady ? UiUtils.getDeviceStatesOKForRequest() : State.values()));
            models.add(deviceReady);
        }

        this.mOperationsAllowedValueModel = new DescribedBooleansToBooleanConverter(descriptions, false, (ValueModel[])models.toArray(new ValueModel[models.size()]));
        this.mOperationsAllowedValueModel.addValueChangeListener((pEvt) -> {
            this.fireValueChange(pEvt.getOldValue(), pEvt.getNewValue());
        });
        this.mOperationsAllowedValueModel.getDescriptionValueModel().addValueChangeListener((pEvt) -> {
            this.checkDescription();
        });
        this.checkDescription();
    }

    public DeviceActionModel(ContainerValueModel pBappModel, TreatmentSessionModeHolder pTreatmentSession, Device pDevice) {
        this((CallbackUtility)null, pBappModel, pTreatmentSession, pDevice);
    }

    public Object getValue() {
        return this.mOperationsAllowedValueModel.getValue();
    }

    public void setValue(Object pNewValue) {
        Assert.notNull(this.mCallbackValueModel, "No callback value model", new Object[0]);
        this.mCallbackValueModel.setValue(pNewValue);
    }

    public ValueModel getDescriptionValueModel() {
        return this.mDescriptionValueModel;
    }

    private void checkDescription() {
        this.mDescriptionValueModel.setValue(this.mOperationsAllowedValueModel.getDescriptionValueModel().getValue());
    }
}
