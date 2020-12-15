// ///////////////////////////////////////////////////////////////////
// Copyright (c) by Ion Beam Applications S.A.
// All rights reserved
//
// Rue du cyclotron, 3
// B-1348 Louvain-la-Neuve
// Tel : +32.10.47.58.11
// ///////////////////////////////////////////////////////////////////

package com.iba.pts.beam.bms.controller.impl.pbs.irradiate;

import com.iba.pts.beam.bms.controller.impl.BmsActivityContext;
import com.iba.pts.bms.common.settings.BdsLayerSettings;
import com.iba.pts.bms.common.settings.impl.pbs.PbsBdsLayerSettings;
import com.iba.pts.bms.common.settings.impl.pbs.PbsMap;
import com.iba.pts.bms.data.recorder.api.MapRecordSummary;
import com.iba.pts.bms.idt.pbs.MapRecalculator;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.apache.log4j.Logger;
import org.springframework.util.Assert;

/**
 * Manager to keep track of the irradiation state.
 */
public class BmsIrradiationManager
{
    public BmsIrradiationManager(PbsIrradiationOptions pIrradiationOptions, BmsTuningManager pTuningManager,
                                 MapRecalculator pMapRecalculator, BmsActivityContext pBmsActivityContext)
    {
        Assert.notNull(pIrradiationOptions);
        Assert.notNull(pTuningManager);
        Assert.notNull(pMapRecalculator);
        Assert.notNull(pBmsActivityContext);

        mTuningManager = pTuningManager;
        mOptions = pIrradiationOptions;
        mMapRecalculator = pMapRecalculator;
        mBmsActivityContext = pBmsActivityContext;
        mBmsActivityContext.addPropertyChangeListener(new PropertyChangeListener()
        {
            @Override
            public void propertyChange(PropertyChangeEvent pEvt)
            {
                if (pEvt.getPropertyName().equals(BmsActivityContext.BMS_SETTINGS) && pEvt.getNewValue() != null)
                {
                    LOGGER.info("initializing treatment");

                    Assert.isTrue(mBmsActivityContext.getLayerIndex() == -1, "should not be " + mBmsActivityContext.getLayerIndex());
                    Assert.isNull(mBmsActivityContext.getCurrentBdsLayerSettings());
                    mBmsActivityContext.selectNextLayer();
                    mBmsActivityContext.selectNextMap();
                    mMagnetCyclingNeeded = true; // cycle the magnets on the first layer map!
                    mNumberOfResumeAfterPauses = -1;
                    mLastIrradiationStatus = null;
                    mMapFullyIrradiated = false;
                    mSetRangeOk = false;
                }
            }
        });
    }

    public final void onIrradiationDone(MapRecordSummary pIrradiationStatus)
    {
        LOGGER.debug("irradiation done");

        mLastIrradiationStatus = pIrradiationStatus;

        BdsLayerSettings currentLayer = mBmsActivityContext.getCurrentBdsLayerSettings();
        PbsMap currentMap = mBmsActivityContext.getCurrentMap();
        int lastElementId = pIrradiationStatus.getLastElementId();
        float deliveredChargeOnLastElement = pIrradiationStatus.getDeliveredChargeForLastElement();

        mMapFullyIrradiated = pIrradiationStatus.getLastLayerId() == currentLayer.getLayerIndex()
                && mMapRecalculator.isMapFullyIrradiated(currentMap, lastElementId, deliveredChargeOnLastElement);
    }

    public final boolean isMapFullyIrradiated()
    {
        return mMapFullyIrradiated;
    }

    public final boolean isMapAlreadyDeliveredByTuningPulses()
    {
        PbsMap map = mBmsActivityContext.getCurrentMap();
        return mMapRecalculator.isMapFullyIrradiatedByTuningPulses(map, mTuningManager.getTotalTuningCharge());
    }

    public final BmsTuningManager getTuningManager()
    {
        return mTuningManager;
    }

    public final boolean hasMapsToIrradiate()
    {
        return mBmsActivityContext.hasLayerToIrradiate() || mBmsActivityContext.hasMapToIrradiate();
    }

    /**
     * Increment the current map to irradiate.
     */
    public final void incrementCurrentMap()
    {
        if (mBmsActivityContext.hasMapToIrradiate())
        {
            int currentMapIndex = mBmsActivityContext.getMapIndex();
            LOGGER.info("incrementing map index " + currentMapIndex + "->" + (currentMapIndex + 1));
            mBmsActivityContext.selectNextMap();
        }
        else if (mBmsActivityContext.hasLayerToIrradiate())
        {
            PbsBdsLayerSettings previousLayer = (PbsBdsLayerSettings) mBmsActivityContext.getCurrentBdsLayerSettings();
            int currentLayerIndex = mBmsActivityContext.getLayerIndex();
            LOGGER.info("incrementing layer index " + currentLayerIndex + "->" + (currentLayerIndex + 1));
            mBmsActivityContext.selectNextLayer();
            mBmsActivityContext.selectNextMap();
            PbsBdsLayerSettings currentLayer = (PbsBdsLayerSettings) mBmsActivityContext.getCurrentBdsLayerSettings();
            boolean differentSpotId = !currentLayer.getSpotTuneId().equals(previousLayer.getSpotTuneId());
            boolean differentRange = currentLayer.getRangeAtNozzleEntrance() != previousLayer.getRangeAtNozzleEntrance(); // NOSONAR
            // - exact
            // floating-point
            // comparison
            if (differentSpotId || differentRange)
            {
                // reset/redo the tuning, reset layer doses.
                mSetRangeOk = false;
                if (currentLayer.getRangeAtNozzleEntrance() > previousLayer.getRangeAtNozzleEntrance())
                {
                    // if range increasing -> redo the magnet cycling!
                    mMagnetCyclingNeeded = true;
                }
                mTuningManager.prepareForTuning();
            }
        }
        mLastIrradiationStatus = null;
        mNumberOfResumeAfterPauses = -1;
    }

    public final boolean isMagnetCyclingNeeded()
    {
        return mMagnetCyclingNeeded;
    }

    public final void forceSetRangeAndTuning(boolean pCycling)
    {
        LOGGER.info("forcing " + (pCycling ? "cycling " : "") + "setrange and tuning on resume");
        mMagnetCyclingNeeded = pCycling;
        mSetRangeOk = false;
        mTuningManager.forceRetuning();
    }

    public final PbsIrradiationOptions getOptions()
    {
        return mOptions;
    }

    public final void setTuningPhase(boolean pTuningPhase)
    {
        mTuningPhase = pTuningPhase;
    }

    public final boolean isTuningPhase()
    {
        return mTuningPhase;
    }

    public final boolean isSetRangeOk()
    {
        return mSetRangeOk;
    }

    public final TuningResult onTuningDone(MapRecordSummary pInfo)
    {
        TuningResult result = mTuningManager.onTuningDone(pInfo);
        if (result.isSetRangeNeeded())
        {
            // apply corrections on the beamline magnets by requesting a new setRange
            mSetRangeOk = false;
        }
        return result;
    }

    public final void setInterrupted(boolean pInterrupted)
    {
        mInterrupted = pInterrupted;
    }

    public final boolean isInterrupted()
    {
        return mInterrupted;
    }

    public final void onSetRangeDone()
    {
        mSetRangeOk = true;
        mMagnetCyclingNeeded = false;
    }

    public final int getNumberOfResumeAfterPauses()
    {
        return mNumberOfResumeAfterPauses;
    }

    public final PbsMap recalculateCurrentMap() throws Exception
    {
        ++mNumberOfResumeAfterPauses;
        int currentElement = -1;
        double alreadyDeliveredChargeOnCurrentElement = 0;
        if (mLastIrradiationStatus != null)
        {
            // resume from pause!
            BdsLayerSettings layer = mBmsActivityContext.getCurrentBdsLayerSettings();
            Assert.isTrue(layer.getLayerIndex() == mLastIrradiationStatus.getLastLayerId());
            currentElement = mLastIrradiationStatus.getLastElementId();
            alreadyDeliveredChargeOnCurrentElement = mLastIrradiationStatus.getDeliveredChargeForLastElement();
        }
        PbsMap result = mTuningManager.recomputeMap(currentElement, alreadyDeliveredChargeOnCurrentElement);
        // prevent subtracting the already delivered dose twice!
        mLastIrradiationStatus = null;
        return result;
    }

    // /////////////////////////////////////////////////////////////////////////

    protected static final Logger LOGGER = Logger.getLogger(BmsIrradiationManager.class);

    private final PbsIrradiationOptions mOptions;
    private final BmsTuningManager mTuningManager;
    private final MapRecalculator mMapRecalculator;
    private final BmsActivityContext mBmsActivityContext;

    // State variables /////////////////////////////////////////////////////////

    private boolean mTuningPhase;
    private boolean mSetRangeOk;
    private MapRecordSummary mLastIrradiationStatus;
    private boolean mMapFullyIrradiated;
    private boolean mInterrupted;
    private int mNumberOfResumeAfterPauses;
    private boolean mMagnetCyclingNeeded;
}