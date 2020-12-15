//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.iba.pts.bms.tcrServiceScreens;

import com.iba.icomp.devices.Device.State;
import com.iba.icompx.ui.service.ServicePanel;
import com.iba.pts.bms.bds.devices.api.RangeModulator;
import com.iba.pts.bms.bds.devices.api.ScanningMagnets;
import com.iba.pts.bms.bds.devices.api.Tcu;
import com.iba.pts.bms.bds.devices.api.TcuSecondScatterer;
import com.iba.pts.bms.bds.devices.api.TcuVariableCollimator;
import com.iba.pts.bms.bds.devices.api.ScanningMagnets.GeneratorMode;
import com.iba.pts.bms.bds.devices.api.SecondScatterer.Position;
import com.iba.pts.bms.datatypes.api.TreatmentMode;
import com.iba.pts.bms.devices.api.InitializableDevice;
import com.iba.tcs.beam.bds.devices.api.IonSourceChain;
import com.iba.tcs.beam.bds.devices.api.Ubti;
import com.iba.tcs.beam.bds.devices.api.IonSourceChainExt.ChainMode;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public abstract class BdsServicePanel<T> extends ServicePanel<T> {
    public BdsServicePanel() {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD})
    protected @interface WidgetEnableCondition {
        BdsServicePanel.BeanCondition[] conditions();
    }

    public static enum BeanCondition {
        HardwareIseuChainClosed(IonSourceChain.class, "chainClosed", "mChainClosed", true, false, -1),
        HardwareIseuChainOpen(IonSourceChain.class, "chainClosed", "mChainClosed", false, true, -1),
        IseuChainPbsMode(IonSourceChain.class, "chainMode", "mChainMode", ChainMode.PBS, ChainMode.DS_US, -1),
        IseuChainDsUsMode(IonSourceChain.class, "chainMode", "mChainMode", ChainMode.DS_US, ChainMode.PBS, -1),
        InitializableDeviceFaulty(InitializableDevice.class, "deviceState", "mDeviceState", State.FAULTY, State.READY, -1),
        UbtiMode2Selected(Ubti.class, "mode2Selected", "mMode2Selected", true, false, -1),
        Ubti3HzTimeoutError(Ubti.class, "3HzTimeoutErrorSignal", "m3HzTimeoutErrorSignal", true, false, -1),
        RmHasLargeWheelSetpoint(RangeModulator.class, "largeWheelPositionSetpoint", "mLargeWheelPositionSetpoint", 1, 0, -1),
        RmHasSmallWheelSpeedSetpoint(RangeModulator.class, "smallWheelSpeedInRpmSetpoint", "mSmallWheelSpeedInRpmSetpoint", 1, 0, -1),
        SseuJack0InPosition(TcuSecondScatterer.class, "position", "mPosition", Position.POSITION2, Position.UNKNOWN, 0),
        SseuJack0NotInPosition(TcuSecondScatterer.class, "position", "mPosition", Position.UNKNOWN, Position.POSITION2, 0),
        SseuDriverOn(TcuSecondScatterer.class, "driverOn", "mDriverOn", true, false, -1),
        VceuXNotOpen(TcuVariableCollimator.class, "XNotOpen", "mXNotOpen", true, false, -1),
        VceuXNotClosed(TcuVariableCollimator.class, "XNotClosed", "mXNotClosed", true, false, -1),
        SmGeneratorModeExternal(ScanningMagnets.class, "generatorMode", "mGeneratorMode", GeneratorMode.External, GeneratorMode.Internal, -1),
        SmGeneratorModeInternal(ScanningMagnets.class, "generatorMode", "mGeneratorMode", GeneratorMode.Internal, GeneratorMode.External, -1),
        TcuTreatmentModeDs(Tcu.class, "treatmentMode", "mTreatmentMode", TreatmentMode.DOUBLE_SCATTERING, TreatmentMode.UNIFORM_SCANNING, -1),
        TcuTreatmentModeUs(Tcu.class, "treatmentMode", "mTreatmentMode", TreatmentMode.UNIFORM_SCANNING, TreatmentMode.DOUBLE_SCATTERING, -1);

        private final Class<?> mBeanType;
        private final String mProperty;
        private final String mFieldName;
        private final Object mEnableValue;
        private final Object mDisableValue;
        private final int mIndex;

        private BeanCondition(Class<?> pBeanType, String pProperty, String pFieldName, Object pEnableValue, Object pDisableValue, int pIndex) {
            this.mBeanType = pBeanType;
            this.mProperty = pProperty;
            this.mFieldName = pFieldName;
            this.mEnableValue = pEnableValue;
            this.mDisableValue = pDisableValue;
            this.mIndex = pIndex;
        }

        public Class<?> getBeanType() {
            return this.mBeanType;
        }

        public String getProperty() {
            return this.mProperty;
        }

        public String getFieldName() {
            return this.mFieldName;
        }

        public Object getEnableValue(boolean pEnable) {
            return pEnable ? this.mEnableValue : this.mDisableValue;
        }

        public int getIndex() {
            return this.mIndex;
        }
    }
}