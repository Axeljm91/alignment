//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.iba.pts.bms.common.settings.impl.pbs;

import com.iba.pts.bms.datatypes.impl.pbs.PbsEquipmentElement;

public final class PbsSpot extends PbsEquipmentElement {
    private static final long serialVersionUID = 2909612874076386892L;
    public int mSpotId;
    public float mTargetCharge;
    public float mExpectedDoseRate;
    public float mBeamCurrentSetpoint;
    public float mXMinPositionFeedback;
    public float mXMaxPositionFeedback;
    public float mYMinPositionFeedback;
    public float mYMaxPositionFeedback;
    public float mXMinBeamSizeFeedback;
    public float mXMaxBeamSizeFeedback;
    public float mYMinBeamSizeFeedback;
    public float mYMaxBeamSizeFeedback;
    public float mXMinNozzleEntrancePosition;
    public float mXMaxNozzleEntrancePosition;
    public float mYMinNozzleEntrancePosition;
    public float mYMaxNozzleEntrancePosition;
    public float mXMinNozzleEntranceWidth;
    public float mXMaxNozzleEntranceWidth;
    public float mYMinNozzleEntranceWidth;
    public float mYMaxNozzleEntranceWidth;
    public float mICCycloMin;
    public float mICCycloMax;
    public float mChargeMinIc1;
    public float mChargeMaxIc1;
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
    public double mClinicalX;
    public double mClinicalY;
    public double mClinicalMeterset;
    public double mIc2Gain;
    public double mIc3Gain;

    public PbsSpot() {
    }

    public int getSpotId() {
        return this.mSpotId;
    }

    public PbsElementType getType() {
        return PbsElementType.SPOT;
    }

    public float getTargetCharge() {
        return this.mTargetCharge;
    }

    public float getExpectedDoseRate() {
        return this.mExpectedDoseRate;
    }

    public float getBeamCurrentSetpoint() {
        return this.mBeamCurrentSetpoint;
    }

    public float getXMinPositionFeedback() {
        return this.mXMinPositionFeedback;
    }

    public float getXMaxPositionFeedback() {
        return this.mXMaxPositionFeedback;
    }

    public float getYMinPositionFeedback() {
        return this.mYMinPositionFeedback;
    }

    public float getYMaxPositionFeedback() {
        return this.mYMaxPositionFeedback;
    }

    public float getXMinBeamSizeFeedback() {
        return this.mXMinBeamSizeFeedback;
    }

    public float getXMaxBeamSizeFeedback() {
        return this.mXMaxBeamSizeFeedback;
    }

    public float getYMinBeamSizeFeedback() {
        return this.mYMinBeamSizeFeedback;
    }

    public float getYMaxBeamSizeFeedback() {
        return this.mYMaxBeamSizeFeedback;
    }

    public float getXMinNozzleEntrancePosition() {
        return this.mXMinNozzleEntrancePosition;
    }

    public float getXMaxNozzleEntrancePosition() {
        return this.mXMaxNozzleEntrancePosition;
    }

    public float getYMinNozzleEntrancePosition() {
        return this.mYMinNozzleEntrancePosition;
    }

    public float getYMaxNozzleEntrancePosition() {
        return this.mYMaxNozzleEntrancePosition;
    }

    public float getXMinNozzleEntranceWidth() {
        return this.mXMinNozzleEntranceWidth;
    }

    public float getXMaxNozzleEntranceWidth() {
        return this.mXMaxNozzleEntranceWidth;
    }

    public float getYMinNozzleEntranceWidth() {
        return this.mYMinNozzleEntranceWidth;
    }

    public float getYMaxNozzleEntranceWidth() {
        return this.mYMaxNozzleEntranceWidth;
    }

    public float getMaxCycloBeamFeedback() {
        return this.mICCycloMax;
    }

    public float getMaxDuration() {
        return this.mIrradiationTime;
    }

    public float getMaxIc1ChargeFeedback() {
        return this.mChargeMaxIc1;
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

    public float getMinIc1ChargeFeedback() {
        return this.mChargeMinIc1;
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
}
