//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.iba.pts.bms.datatypes.impl.pbs;

import com.iba.pts.bms.common.settings.impl.pbs.PbsElementType;
import com.iba.pts.bms.common.settings.impl.pbs.PbsMap;
import com.iba.pts.bms.common.settings.impl.pbs.PbsMapElement;
import com.iba.pts.bms.common.settings.impl.pbs.PbsSpot;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public final class PbsEquipmentMap implements PbsMap {
    private static final long serialVersionUID = 5402592446493207416L;
    public boolean mDiagnosticMode;
    public boolean mNarrowBeamEntranceX;
    public boolean mNarrowBeamEntranceY;
    public boolean mNarrowBeamExitX;
    public boolean mNarrowBeamExitY;
    public int mLayerIndex;
    public double mMetersetCorrectionFactor;
    public double mRangeAtNozzleEntrance;
    public double mLayerNozzleCurrentMax;
    public double mLayerCycloCurrentMax;
    public double mGlobalCycloCurrentMax;
    public double mIC2Gain;
    public double mIC3Gain;
    public double mEnergyInScanningMagnets;
    public double mMagneticRigidity;
    public double mSmxOffset;
    public double mSmyOffset;
    public double mIcxOffset;
    public double mIcyOffset;
    public double mIsoUnscanX;
    public double mIsoUnscanY;
    private double mIrradiationTime;
    private double mTotalCharge;
    private double mWeightedIc2gain;
    private double mWeightedIc3gain;
    private int mTotalNbrOfSpots;
    private List<PbsMapElement> mElements = new LinkedList();
    private List<PbsMapElement> mElementsForPhysics = new LinkedList();

    public PbsEquipmentMap() {
    }

    public List<PbsMapElement> getElements() {
        return this.mElements;
    }

    public List<PbsMapElement> getElementsForPhysics() {
        return Collections.unmodifiableList(this.mElementsForPhysics);
    }

    public boolean isDiagnosticMode() {
        return this.mDiagnosticMode;
    }

    public boolean isNarrowBeamEntranceX() {
        return this.mNarrowBeamEntranceX;
    }

    public boolean isNarrowBeamEntranceY() {
        return this.mNarrowBeamEntranceY;
    }

    public boolean isNarrowBeamExitX() {
        return this.mNarrowBeamExitX;
    }

    public boolean isNarrowBeamExitY() {
        return this.mNarrowBeamExitY;
    }

    public int getLayerIndex() {
        return this.mLayerIndex;
    }

    public double getMetersetCorrectionFactor() {
        return this.mMetersetCorrectionFactor;
    }

    public double getRangeAtNozzleEntrance() {
        return this.mRangeAtNozzleEntrance;
    }

    public double getTotalCharge() {
        return this.mTotalCharge;
    }

    public double getTotalIrradiationTime() {
        return this.mIrradiationTime;
    }

    public int getTotalNbrOfSpots() { return mTotalNbrOfSpots; }

    public double getSmxOffset() {
        return this.mSmxOffset;
    }

    public double getSmyOffset() {
        return this.mSmyOffset;
    }

    public double getIcxOffset() {
        return this.mIcxOffset;
    }

    public double getIcyOffset() {
        return this.mIcyOffset;
    }

    public double getWeightedIc2Gain() {
        return this.mWeightedIc2gain;
    }

    public double getWeightedIc3Gain() {
        return this.mWeightedIc3gain;
    }

    public void setElements(List<PbsMapElement> pElements) {
        this.mElements = pElements;
        this.mElementsForPhysics = clone(pElements);
    }

    private static List<PbsMapElement> clone(List<PbsMapElement> pElements) {
        List<PbsMapElement> ret = new LinkedList();
        Iterator var2 = pElements.iterator();

        while(var2.hasNext()) {
            PbsMapElement element = (PbsMapElement)var2.next();
            if (element instanceof PbsSpot) {
                ret.add(PbsEquipmentMapUtility.clonePbsSpot((PbsSpot)element));
            } else {
                ret.add(element);
            }
        }

        return ret;
    }

    public PbsEquipmentMap cloneWithoutElements() {
        PbsEquipmentMap result = new PbsEquipmentMap();
        result.mDiagnosticMode = this.mDiagnosticMode;
        result.mNarrowBeamEntranceX = this.mNarrowBeamEntranceX;
        result.mNarrowBeamEntranceY = this.mNarrowBeamEntranceY;
        result.mNarrowBeamExitX = this.mNarrowBeamExitX;
        result.mNarrowBeamExitY = this.mNarrowBeamExitY;
        result.mLayerIndex = this.mLayerIndex;
        result.mMetersetCorrectionFactor = this.mMetersetCorrectionFactor;
        result.mRangeAtNozzleEntrance = this.mRangeAtNozzleEntrance;
        result.mLayerNozzleCurrentMax = this.mLayerNozzleCurrentMax;
        result.mLayerCycloCurrentMax = this.mLayerCycloCurrentMax;
        result.mGlobalCycloCurrentMax = this.mGlobalCycloCurrentMax;
        result.mIC2Gain = this.mIC2Gain;
        result.mIC3Gain = this.mIC3Gain;
        result.mEnergyInScanningMagnets = this.mEnergyInScanningMagnets;
        result.mMagneticRigidity = this.mMagneticRigidity;
        result.mWeightedIc2gain = this.mWeightedIc2gain;
        result.mWeightedIc3gain = this.mWeightedIc3gain;
        return result;
    }

    public void updateAfterElementsChanged(boolean pUpdateWeightedIcGain) {
        double time = 0.0D;
        int spots = 0;
        double totalCharge = 0.0D;
        double weightedIc2gain = 0.0D;
        double weightedIc3gain = 0.0D;
        Iterator var11 = this.mElements.iterator();

        while(var11.hasNext()) {
            PbsMapElement e = (PbsMapElement)var11.next();
            time += (double)e.getMaxDuration();
            if (e.getType().equals(PbsElementType.SPOT)) {
                ++spots;
                totalCharge += (double)e.getTargetCharge();
                weightedIc2gain += ((PbsSpot)e).mIc2Gain * (double)e.getTargetCharge();
                weightedIc3gain += ((PbsSpot)e).mIc3Gain * (double)e.getTargetCharge();
            }
        }

        this.mIrradiationTime = time;
        this.mTotalNbrOfSpots = spots;
        this.mTotalCharge = totalCharge;
        if (pUpdateWeightedIcGain) {
            if (totalCharge > 0.0D) {
                this.mWeightedIc2gain = weightedIc2gain / totalCharge;
                this.mWeightedIc3gain = weightedIc3gain / totalCharge;
            } else {
                this.mWeightedIc2gain = 1.0D;
                this.mWeightedIc3gain = 1.0D;
            }
        }

    }
}
