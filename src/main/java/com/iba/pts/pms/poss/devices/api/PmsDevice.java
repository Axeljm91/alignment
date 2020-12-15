// ///////////////////////////////////////////////////////////////////
// Copyright (c) by Ion Beam Applications S.A.
// All rights reserved
//
// Rue du cyclotron, 3
// B-1348 Louvain-la-Neuve
// Tel : +32.10.47.58.11
// ///////////////////////////////////////////////////////////////////

package com.iba.pts.pms.poss.devices.api;

import com.iba.icomp.devices.Device;
import com.iba.pts.bms.bds.bapctrl.impl.pbs.actions.ScanningControllerSelectBeamlineActionActivity;

import java.util.HashSet;
import java.util.Set;

/**
 * The PMS device. Any device of PMS subsystem should implement this interface.
 */
public interface PmsDevice extends Device
{
    /** Property for the label. */
    String LABEL_PROPERTY = "label";
    /** Property for the state of the device. */
    String STATE_PROPERTY = "state";
    /** Device selected property name. */
    // TODO: either add a property, either remove this
    String DEVICE_SELECTED_PROPERTY = "Selected";

    public static final String KVKV_RAD_A = "radA";
    public static final String KVKV_RAD_B = "radB";
    public static final String KVKV_FALSE = "false";


    /**
     * Return a label for this device.
     *
     * @return the device label.
     * @see #LABEL_PROPERTY
     */
    Label getLabel();

    /**
     * Returns the state of the device.
     *
     * @return the state.
     * @see #STATE_PROPERTY
     */
    Device.State getState();


    /**
     * Labels representing all devices mentioned by PMS.
     */
    public enum Label
    {
        ACCESS_POINT_MOTION,
        /** {@link Pps} device. */
        LASER_PROTECTION,
        /** {@link Pps} device. */
        PPS,
        /** {@link Snout} device. */
        SNOUT,
        /** {@link Gantry} device. */
        GANTRY,
        /** {@link XRay} device. */
        XRAY,
        /** First {@link DigitalImagingDevice} device. */
        DIDA,
        /** Second {@link DigitalImagingDevice} device. */
        DIDB,
        /** Third {@link DigitalImagingDevice} device. */
        DIDC,
        /** Imagin System. */
        IMAGING,
        /** {@link Lasers} device. */
        LASERS,
        /** {@link TreatmentRoomLight}s. */
        TRLIGHTS,
        /** {@link LightField}s. */
        LFIELD,
        /** {@link Mlc Multi-leave collimator} device. */
        MLC,
        /** {@link AccessoryDrawer} device. */
        RSD,
        /** Axis? */
        AXIS,
        /** Snout translation. */
        STEU,
        /** Snout rotation. */
        SREU,
        /** PPS Control Unit. */
        PPSCU,
        /** Kinlib. */
        KINLIB,
        /** PMAC device. */
        PMAC,
        /** Counter device. */
        COUNTER,
        /** Encoder device. */
        ENCODER,
        /** Gantry correction. */
        GANTRY_CORRECTION,
        /** Position manager configuration. */
        POSMGR,
        /** PPVS virtual device. */
        PPVD,
        /** Accessory holder device for universal nozzle.*/
        AHUN,
        /** CBCT controller (CBCT feature). */
        CBCT_CONTROLLER,
        /** This device controls the imaging devices (Tube, Panel and Collimator) in a CGTR. Aka NozzleImager. */
        CBCT_DEVICES_MANAGER,
        /** CBCT Device manager for panel and collimator only. */
        CBCT_NOZZLE_IMAGER_FOV_MANAGER,
        /** Panel for a CGTR. This device is part of the CBCT_DEVICE_MANAGER and CBCT_NOZZLE_IMAGER_FOV_MANAGER. */
        CBCT_PANEL,
        /** Tube for a CGTR. This device is part of the CBCT_DEVICE_MANAGER. */
        CBCT_TUBE,
        /** Collimator for a CGTR. This device is part of the CBCT_DEVICE_MANAGER and CBCT_NOZZLE_IMAGER_FOV_MANAGER. */
        CBCT_COLLIMATOR,
        /** Imager manager for a GTR. Controls the Flat Panel and the Collimator. */
        IMAGER_MANAGER,
        /** Panel for a GTR. This device correspond to the DID-B, but controls the lateral movement. Part of the IMAGER_MANAGER. */
        IMAGER_PANEL,
        /** Collimator for a GTR. Controls all 4 collimator blades. This device is part of the IMAGER_MANAGER. */
        IMAGER_COLLIMATOR,
        /** Collimator Blade for a GTR. This device is one of the 4 blades of the IMAGER_COLLIMATOR. */
        IMAGER_COLLIMATOR_BLADE,
        /** Lasers of the Accessory Holder of the CGTR.*/
        AH_LASERS,
        /** Kuka loadcell.*/
        LOADCELL,
        /** In Room CT. */
        IN_ROOM_CT,
        /** Null device useful for treatment room config only.*/
        NULL;

        public static String toString(Label label){
            switch(label)
            {
                case ACCESS_POINT_MOTION:
                    return "ACCESS_POINT_MOTION";
                case LASER_PROTECTION:
                    return "LASER_PROTECTION";
                case PPS:
                    return "PPS";
                case SNOUT:
                    return "SNOUT";
                case GANTRY:
                    return "GANTRY";
                case XRAY:
                    return "XRAY";
            }
            return "Cannot find label";
        }

        /**
         * Compute a set where the devices belonging to the same selection group will be gathered a single label.
         * @return a filtered set
         */
        public static Set<Label> getGroupedSet(Set<Label> aNonFilteredSet)
        {
            Set<Label> result = new HashSet<>();

            for (Label label:aNonFilteredSet)
            {
                switch (label)
                {
                    case CBCT_PANEL:
                    case CBCT_TUBE:
                    case CBCT_COLLIMATOR:
                    case CBCT_NOZZLE_IMAGER_FOV_MANAGER:
                        result.add(CBCT_DEVICES_MANAGER); // Even if there are several calls, a set will keep a single entry only.
                        break;
                    default:
                        result.add(label);
                        break;
                }
            }
            return result;
        }
    }
}
