package com.iba.pts.bms.common.settings.impl.pbs;

import java.io.Serializable;
import java.util.List;

/**
 * Type specifying a PBS map..
 */
public interface PbsMap extends Serializable
{
    /**
     * Returns true if the map should be run in diagnostic mode.
     * @return true if the map should be run in diagnostic mode.
     */
    boolean isDiagnosticMode();

    /**
     * Returns true if the 'narrow beam algorithm' needs to be used by the scanning controller for the nozzle entrance X beam
     * width.
     * @return true if the 'narrow beam algorithm' needs to be used by the scanning controller for the nozzle entrance X beam
     *         width.
     */
    boolean isNarrowBeamEntranceX();

    /**
     * Returns true if the 'narrow beam algorithm' needs to be used by the scanning controller for the nozzle entrance Y beam
     * width.
     * @return true if the 'narrow beam algorithm' needs to be used by the scanning controller for the nozzle entrance Y beam
     *         width.
     */
    boolean isNarrowBeamEntranceY();

    /**
     * Returns true if the 'narrow beam algorithm' needs to be used by the scanning controller for the nozzle exit X beam width.
     * @return true if the 'narrow beam algorithm' needs to be used by the scanning controller for the nozzle exit X beam width.
     */
    boolean isNarrowBeamExitX();

    /**
     * Returns true if the 'narrow beam algorithm' needs to be used by the scanning controller for the nozzle exit Y beam width.
     * @return true if the 'narrow beam algorithm' needs to be used by the scanning controller for the nozzle exit Y beam width.
     */
    boolean isNarrowBeamExitY();

    /**
     * Returns the index of the (parent) layer.
     * @return the index of the (parent) layer.
     */
    int getLayerIndex();

    /**
     * Returns the factor used to convert dose in MU. It is computed by the following formula: (Ref. Temperature / Temperature) *
     * (Pressure / Ref. Pressure) * Dose Correction Factor
     * @return the factor used to convert dose in MU.
     */
    double getMetersetCorrectionFactor();

    /**
     * Returns the range of the layer at nozzle entrance [unit g/cm2].
     * @return the range of the layer at nozzle entrance.
     */
    double getRangeAtNozzleEntrance();

    /**
     * Returns the total charge to be deposited on this map [unit C].
     * @return the total charge to be deposited on this map.
     */
    double getTotalCharge();

    /**
     * Returns the total irradiation time [msec].
     * @return the total irradiation time.
     */
    double getTotalIrradiationTime();

    /**
     * Returns the total number of spots.
     * @return the total number of spots.
     */
    int getTotalNbrOfSpots();

    /**
     * Returns the list of elements of this map.
     * @return the list of elements of this map.
     */
    List<PbsMapElement> getElements();

    /**
     * Returns the scanning magnet x offset used to translate the map.
     * @return the scanning magnet x offset used to translate the map.
     */
    double getSmxOffset();

    /**
     * Returns the scanning magnet y offset used to translate the map.
     * @return the scanning magnet y offset used to translate the map.
     */
    double getSmyOffset();

    /**
     * Returns the IC x offset used to translate the map.
     * @return the IC x offset used to translate the map.
     */
    double getIcxOffset();

    /**
     * Returns the IC y offset used to translate the map.
     * @return the IC y offset used to translate the map.
     */
    double getIcyOffset();

    /**
     * Returns the weighted IC2 gain factor for the datarecorder charge conversion.
     * @return the weighted IC2 gain factor.
     */
    double getWeightedIc2Gain();

    /**
     * Returns the weighted IC3 gain factor for the datarecorder charge conversion.
     * @return the weighted IC3 gain factor.
     */
    double getWeightedIc3Gain();
}
