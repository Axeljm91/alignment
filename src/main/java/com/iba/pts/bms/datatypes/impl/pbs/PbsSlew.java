//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.iba.pts.bms.datatypes.impl.pbs;

import com.iba.pts.bms.common.settings.impl.pbs.PbsElementType;

public final class PbsSlew extends PbsEquipmentElement {
    private static final long serialVersionUID = -8359515668986400127L;
    public PbsSlewConstants mConstants;

    public PbsSlew(PbsSlewConstants pConstants) {
        mConstants = pConstants;
    }

    public int getSpotId() {
        return 0;
    }

    public PbsElementType getType() {
        return PbsElementType.SLEW;
    }

    public float getTargetCharge() {
        return 0.0F;
    }

    public float getExpectedDoseRate() {
        return 0.0F;
    }

    public float getBeamCurrentSetpoint() {
        return 0.0F;
    }

    public float getXMinPositionFeedback() {
        return -250.0F;
    }

    public float getXMaxPositionFeedback() {
        return 250.0F;
    }

    public float getYMinPositionFeedback() {
        return -250.0F;
    }

    public float getYMaxPositionFeedback() {
        return 250.0F;
    }

    public float getXMinBeamSizeFeedback() {
        return 0.0F;
    }

    public float getXMaxBeamSizeFeedback() {
        return 100.0F;
    }

    public float getYMinBeamSizeFeedback() {
        return 0.0F;
    }

    public float getYMaxBeamSizeFeedback() {
        return 100.0F;
    }

    public float getXMinNozzleEntrancePosition() {
        return this.mConstants.mDisabledIc1PositionMin;
    }

    public float getXMaxNozzleEntrancePosition() {
        return this.mConstants.mDisabledIc1PositionMax;
    }

    public float getYMinNozzleEntrancePosition() {
        return this.mConstants.mDisabledIc1PositionMin;
    }

    public float getYMaxNozzleEntrancePosition() {
        return this.mConstants.mDisabledIc1PositionMax;
    }

    public float getXMinNozzleEntranceWidth() {
        return 0.0F;
    }

    public float getXMaxNozzleEntranceWidth() {
        return 50.0F;
    }

    public float getYMinNozzleEntranceWidth() {
        return 0.0F;
    }

    public float getYMaxNozzleEntranceWidth() {
        return 50.0F;
    }

    public float getMaxCycloBeamFeedback() {
        return this.mConstants.mICCycloMax;
    }

    public float getMaxDuration() {
        return this.mConstants.mIrradiationTime;
    }

    public float getMaxIc1ChargeFeedback() {
        return this.mConstants.mChargeMaxIc1;
    }

    public float getMaxPrimaryChargeFeedback() {
        return this.mConstants.mChargeMaxPrimary;
    }

    public float getMaxPrimaryDoseRate() {
        return this.mConstants.mChargeRateMaxPrimary;
    }

    public float getMaxSecondaryChargeFeedback() {
        return this.mConstants.mChargeMaxSecondary;
    }

    public float getMaxSecondaryDoseRate() {
        return this.mConstants.mChargeRateMaxSecondary;
    }

    public float getMaxTernaryChargeFeedback() {
        return this.mConstants.mChargeMaxTernary;
    }

    public float getMaxTernaryDoseRate() {
        return this.mConstants.mChargeRateMaxTernary;
    }

    public float getMinCycloBeamFeedback() {
        return this.mConstants.mICCycloMin;
    }

    public float getMinIc1ChargeFeedback() {
        return this.mConstants.mChargeMinIc1;
    }

    public float getMinPrimaryChargeFeedback() {
        return this.mConstants.mChargeMinPrimary;
    }

    public float getMinPrimaryDoseRate() {
        return this.mConstants.mChargeRateMinPrimary;
    }

    public float getMinSecondaryChargeFeedback() {
        return this.mConstants.mChargeMinSecondary;
    }

    public float getMinSecondaryDoseRate() {
        return this.mConstants.mChargeRateMinSecondary;
    }

    public float getMinTernaryChargeFeedback() {
        return this.mConstants.mChargeMinTernary;
    }

    public float getMinTernaryDoseRate() {
        return this.mConstants.mChargeRateMinTernary;
    }
}
