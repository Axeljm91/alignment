package com.iba.pts.bms.datatypes.impl.pbs;

import com.iba.pts.bms.common.settings.impl.pbs.PbsMapElement;
import java.io.Serializable;

public abstract class PbsEquipmentElement implements PbsMapElement, Serializable {
    private static final long serialVersionUID = -3817669156860186521L;
    public float mCurrentSetpointX;
    public float mCurrentSetpointY;
    public float mCurrentFeedbackMinXPrimary;
    public float mCurrentFeedbackMaxXPrimary;
    public float mCurrentFeedbackMinYPrimary;
    public float mCurrentFeedbackMaxYPrimary;
    public float mCurrentFeedbackMinXRedundant;
    public float mCurrentFeedbackMaxXRedundant;
    public float mCurrentFeedbackMinYRedundant;
    public float mCurrentFeedbackMaxYRedundant;
    public float mVoltageFeedbackMinXPrimary;
    public float mVoltageFeedbackMaxXPrimary;
    public float mVoltageFeedbackMinYPrimary;
    public float mVoltageFeedbackMaxYPrimary;
    public float mVoltageFeedbackMinXRedundant;
    public float mVoltageFeedbackMaxXRedundant;
    public float mVoltageFeedbackMinYRedundant;
    public float mVoltageFeedbackMaxYRedundant;
    public float mICCycloMin;
    public float mICCycloMax;
    public float mChargeMinPrimary;
    public float mChargeMaxPrimary;
    public float mChargeMinSecondary;
    public float mChargeMaxSecondary;
    public float mChargeMinTernary;
    public float mChargeMaxTernary;
    public float mChargeRateMinPrimary;
    public float mChargeRateMaxPrimary;
    public float mChargeRateMinSecondary;
    public float mChargeRateMaxSecondary;
    public float mChargeRateMinTernary;
    public float mChargeRateMaxTernary;
    public float mIrradiationTime;
    public double mBxLx;
    public double mByLy;
    public double mIx;
    public double mIy;
    public double mUxMin;
    public double mUxMax;
    public double mUyMin;
    public double mUyMax;
    public int mUxDelay;
    public int mUyDelay;

    public PbsEquipmentElement() {
    }

    public float getMaxCycloBeamFeedback() {
        return this.mICCycloMax;
    }

    public float getMaxDuration() {
        return this.mIrradiationTime;
    }

    public float getMaxPrimaryChargeFeedback() {
        return this.mChargeMaxPrimary;
    }

    public float getMaxPrimaryDoseRate() {
        return this.mChargeRateMaxPrimary;
    }

    public float getMaxSecondaryChargeFeedback() {
        return this.mChargeMaxSecondary;
    }

    public float getMaxSecondaryDoseRate() {
        return this.mChargeRateMaxSecondary;
    }

    public float getMaxTernaryChargeFeedback() {
        return this.mChargeMaxTernary;
    }

    public float getMaxTernaryDoseRate() {
        return this.mChargeRateMaxTernary;
    }

    public float getMinCycloBeamFeedback() {
        return this.mICCycloMin;
    }

    public float getMinPrimaryChargeFeedback() {
        return this.mChargeMinPrimary;
    }

    public float getMinPrimaryDoseRate() {
        return this.mChargeRateMinPrimary;
    }

    public float getMinSecondaryChargeFeedback() {
        return this.mChargeMinSecondary;
    }

    public float getMinSecondaryDoseRate() {
        return this.mChargeRateMinSecondary;
    }

    public float getMinTernaryChargeFeedback() {
        return this.mChargeMinTernary;
    }

    public float getMinTernaryDoseRate() {
        return this.mChargeRateMinTernary;
    }

    public float getXMagnetCurrentSetpoint() {
        return this.mCurrentSetpointX;
    }

    public float getXMaxPrimaryCurrentFeedback() {
        return this.mCurrentFeedbackMaxXPrimary;
    }

    public float getXMaxPrimaryVoltageFeedback() {
        return this.mVoltageFeedbackMaxXPrimary;
    }

    public float getXMaxSecondaryCurrentFeedback() {
        return this.mCurrentFeedbackMaxXRedundant;
    }

    public float getXMaxSecondaryVoltageFeedback() {
        return this.mVoltageFeedbackMaxXRedundant;
    }

    public float getXMinPrimaryCurrentFeedback() {
        return this.mCurrentFeedbackMinXPrimary;
    }

    public float getXMinPrimaryVoltageFeedback() {
        return this.mVoltageFeedbackMinXPrimary;
    }

    public float getXMinSecondaryCurrentFeedback() {
        return this.mCurrentFeedbackMinXRedundant;
    }

    public float getXMinSecondaryVoltageFeedback() {
        return this.mVoltageFeedbackMinXRedundant;
    }

    public float getYMagnetCurrentSetpoint() {
        return this.mCurrentSetpointY;
    }

    public float getYMaxPrimaryCurrentFeedback() {
        return this.mCurrentFeedbackMaxYPrimary;
    }

    public float getYMaxPrimaryVoltageFeedback() {
        return this.mVoltageFeedbackMaxYPrimary;
    }

    public float getYMaxSecondaryCurrentFeedback() {
        return this.mCurrentFeedbackMaxYRedundant;
    }

    public float getYMaxSecondaryVoltageFeedback() {
        return this.mVoltageFeedbackMaxYRedundant;
    }

    public float getYMinPrimaryCurrentFeedback() {
        return this.mCurrentFeedbackMinYPrimary;
    }

    public float getYMinPrimaryVoltageFeedback() {
        return this.mVoltageFeedbackMinYPrimary;
    }

    public float getYMinSecondaryCurrentFeedback() {
        return this.mCurrentFeedbackMinYRedundant;
    }

    public float getYMinSecondaryVoltageFeedback() {
        return this.mVoltageFeedbackMinYRedundant;
    }

    public final float getXMinField() {
        return -11.0F;
    }

    public final float getXMaxField() {
        return 11.0F;
    }

    public final float getYMinField() {
        return -11.0F;
    }

    public final float getYMaxField() {
        return 11.0F;
    }
}
