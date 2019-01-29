package com.iba.ialign;

import com.iba.blak.BlakConstants;
import com.iba.blak.device.api.EcubtcuException;
import com.iba.blak.device.impl.BcreuFactory;
import com.iba.blak.device.impl.BcreuFactory.ExtAbstractBcreuProxy;
import com.iba.pts.bms.bss.bps.devices.impl.Bcreu124Proxy;

import org.apache.log4j.Logger;

/**
 * Created by cboyd on 6/18/2014.
 */
public class LegacyBeam
{
    private static org.apache.log4j.Logger log= Logger.getLogger(new Throwable().getStackTrace()[0].getClassName());

    String MAX_BEAM_CURRENT_PROPERTY = "maxBeamCurrent";
    String BEAM_CONTROL_MODE_PROPERTY = "beamControlMode";

    public ExtAbstractBcreuProxy bcreu = BcreuFactory.createBcreuProxy(BlakConstants.SITE_BCREU_DEFAULT_VERSION[Controller.siteID]);

    private int mBeamControlMode = 0;// 0:through ecubtcu; 1: through bcp; 2: through scanning controller.

    private boolean singlePulse = true;
    private boolean mLookupDone = false;

    // hold the feedback from BCREU
    private double mMaxBeamCurrent = 10; // if bcreu.isRegulationRunning() is true
    // it indicates the max beam current that the Cyclotron can produce at
    // its exit

    public LegacyBeam()
    {

    }

    public int getBeamControlMode()
    {
        return mBeamControlMode;
    }

    public void singlePulse(boolean single) throws EcubtcuException
    {
        log.info("  -> Request " + (single ? "single" : "continuous") + " pulse");

        switch (mBeamControlMode)
        {
            case 0:
                Controller.ecubtcu.iseuRequestSetSinglePulseMode(single);
                break;
            case 1:
                if (!single)
                {
                    bcreu.setContinuousPulse(!single);
//                    bcreu.startBeamPulses();
                }
                else
                {
//                    bcreu.stopBeamPulses();
                }
                break;
//            case 2:
//                if (single)
//                {
//                    mSC.cancel();
//                }
//                else
//                {
//                    // irradiate the first bds layer
//                    PbsBdsLayerSettings settings = null;
//                    for (BmsLayerSettings bmsLayerSetting : mBmsSettings.getLayerSettings())
//                    {
//                        settings = (PbsBdsLayerSettings) bmsLayerSetting.getBdsLayerSettings();
//                        break;
//                    }
//                    mSC.setCurrentBdsLayerSettings(settings);
//                    mSC.irradiate();
//                }
//                break;
            default:
                Controller.ecubtcu.iseuRequestSetSinglePulseMode(single);

        }
    }

    public void updateSinglePulseMode(boolean b)
    {
        singlePulse = b;
    }

    public boolean isSinglePulseMode()
    {
        return singlePulse;
    }
    public void updateLookupStatus(boolean b)
    {
        mLookupDone = b;
    }

    public boolean isLookupDone()
    {
        return mLookupDone;
    }

    public double getMaxBeamCurrent()
    {
        return mMaxBeamCurrent;
    }

}
