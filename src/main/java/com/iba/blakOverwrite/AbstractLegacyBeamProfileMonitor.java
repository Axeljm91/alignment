// ///////////////////////////////////////////////////////////////////
// Copyright (c) by Ion Beam Applications S.A.
// All rights reserved
//
// Rue du cyclotron, 3
// B-1348 Louvain-la-Neuve
// Tel : +32.10.47.58.11
// ///////////////////////////////////////////////////////////////////

package com.iba.blakOverwrite;

import com.iba.blak.Blak;
import com.iba.blak.common.Distribution;
import com.iba.blak.device.api.BeamMeasurementDevice;
import com.iba.blak.device.api.BeamProfile;
import com.iba.blak.device.api.BeamProfileMonitor;
import com.iba.blak.device.api.EcubtcuException;
import com.iba.blak.device.api.LegacyBeamProfileMonitor;
//overwrite
import com.iba.ialign.Controller ;

import com.iba.blak.device.impl.AcquisitionTimeoutException;

import com.iba.icomp.core.util.Logger;

public abstract class AbstractLegacyBeamProfileMonitor extends AbstractBeamProfileMonitor implements
      LegacyBeamProfileMonitor
{

   private BPMBeamProfile mLegacyProfile;
   public boolean mAcquiringProfile = false;

   public AbstractLegacyBeamProfileMonitor(String pName)
   {
      super(pName);
   }

   public AbstractLegacyBeamProfileMonitor(AbstractLegacyBeamProfileMonitor pBpm)
   {
      super(pBpm);
   }

   // Start Continuous Acquisition of BPM
   public void startContinuousAcquisition() throws EcubtcuException
   {
      Logger.getLogger().debug("  -> Request to start Continuous acquisition for BPM " + getName());
//      Blak.ecubtcu.bpmStartContinuousAcquisition(getName());
      Controller.ecubtcu.bpmStartContinuousAcquisition(getName());
   }

   // Stop Continuous Acquisition of BPM
   public void stopContinuousAcquisition() throws EcubtcuException
   {
      Logger.getLogger().debug("  -> Request to stop Continuous acquisition for BPM " + getName());
//      Blak.ecubtcu.bpmStopContinuousAcquisition(getName());
      Controller.ecubtcu.bpmStopContinuousAcquisition(getName());
   }

   // Request Start refresh data of BPM
   public void startProfileAcquisition() throws EcubtcuException
   {
      Logger.getLogger().debug("  -> Request to start profile acquisition for BPM " + getName());
//      Blak.ecubtcu.bpmStartProfileAcquisition(getName());
      Controller.ecubtcu.bpmStartProfileAcquisition(getName());
   }

   // Request Stop refresh data of BPM
   public void stopProfileAcquisition() throws EcubtcuException
   {
      Logger.getLogger().debug("  -> Request to stop profile acquisition for BPM " + getName());
//      Blak.ecubtcu.bpmStopProfileAcquisition(getName());
      Controller.ecubtcu.bpmStopProfileAcquisition(getName());
   }

   // Reset the BPM
   public void reset() throws EcubtcuException
   {
      Logger.getLogger().debug("  -> Request to reset BPM " + getName());
//      Blak.ecubtcu.bpmReset(getName());
      Controller.ecubtcu.bpmReset(getName());
   }

   //For site with BPM inversion/canbus BPMs
   public void acquireProfile() throws AcquisitionTimeoutException, EcubtcuException, InterruptedException
   {
      mAcquiringProfile = true;
       // Stop continuous acquisition if needed
       //added start acq testing 5Mar18 -AMO UNDONE
       startContinuousAcquisition();

       Thread.sleep(1100);

      if (getOperationMode()== BeamProfileMonitor.OperationMode.OPERATION_MODE_CONTINUOUS_ACQUISITION)
      {
         stopContinuousAcquisition();
      }
      Thread.sleep(400);
      mLegacyProfile = null;
      // Start profile acquisition;
      startProfileAcquisition();
      //over write
      //Blak.feedbackClient.setProfilingBpm(this);
      Controller.feedbackClient.setProfilingBpm(this);

      //Thread.sleep(1100);
      // Wait for data to be ready
      long startTime = System.currentTimeMillis();
      while (!isDataReady())
      {
         Thread.sleep(100);
         if (System.currentTimeMillis() - startTime > getProfileAcquisitionTimeOut())
         {
            stopProfileAcquisition();
            throw new AcquisitionTimeoutException();
         }
      }
      Thread.sleep(400);
      // As ECUBTCU need 200ms to start acquisition for new data ,Small delay to
      // ensure the BLAK receive the updated values

      if (mLegacyProfile != null)
      {
         verticalChannels = mLegacyProfile.getVerticalValues().Clone();
         horizontalChannels = mLegacyProfile.getHorizontalValues().Clone();
         setIntegratedCurrent(mLegacyProfile.getIntegratedCurrent());
      }

      //startContinuousAcquisition();

      mAcquiringProfile = false;
   }

   //Use for newer ethernet BPM no inversion
//   public void acquireProfile() throws AcquisitionTimeoutException, EcubtcuException, InterruptedException
//   {
//      // Stop continuous acquisition if needed
//      if (getOperationMode() == BeamProfileMonitor.OperationMode.OPERATION_MODE_CONTINUOUS_ACQUISITION)
//      {
//         stopContinuousAcquisition();
//      }
//      mLegacyProfile = null;
//      // Start profile acquisition;
//      startProfileAcquisition();
//      //over write
//      //Blak.feedbackClient.setProfilingBpm(this);
//      Controller.feedbackClient.setProfilingBpm(this);
//      mAcquiringProfile = true;
//      Thread.sleep(1100);
//      // Wait for data to be ready
//      long startTime = System.currentTimeMillis();
//      while (!isDataReady())
//      {
//         Thread.sleep(100);
//         if (System.currentTimeMillis() - startTime > getProfileAcquisitionTimeOut())
//         {
//            stopProfileAcquisition();
//            throw new AcquisitionTimeoutException();
//         }
//      }
//      // As ECUBTCU need 200ms to start acquisition for new data ,Small delay to
//      // ensure the BLAK receive the updated values
//
//      if (mLegacyProfile != null)
//      {
//         verticalChannels = mLegacyProfile.getVerticalValues().Clone();
//         horizontalChannels = mLegacyProfile.getHorizontalValues().Clone();
//         setIntegratedCurrent(mLegacyProfile.getIntegratedCurrent());
//      }
//   }

   @Override
   public void setOperationMode(OperationMode pMode)
   {
      super.setOperationMode(pMode);
      if (mAcquiringProfile)
      {
         if (pMode == OperationMode.OPERATION_MODE_DATA_READY)
         {
            try
            {
               stopProfileAcquisition();
            }
            catch (Exception pE)
            {
               Logger.getLogger().error(
                     getName() + " : Failed to stop profile acquisition (" + pE.getMessage() + ")");
            }
         }
         else if (pMode == OperationMode.OPERATION_MODE_NO_ACQUISITION)
         {
            mAcquiringProfile = false;
            if (mLegacyProfile != null)
            {
               notifyPropertyChange(BeamMeasurementDevice.PROFILE_PROPERTY, null, mLegacyProfile);
            }
         }
      }
   }

   public void setBeamProfile(double pIntegratedCurrent, double[] pXProfile, double[] pYProfile)
   {
      updateIntegratedCurrent(pIntegratedCurrent);
      for (int i = 0; i < pXProfile.length; ++i)
      {
         updateHorizontalChannelValue(i, pXProfile[i]);
         updateVerticalChannelValue(i, pYProfile[i]);
      }
      setBeamProfile(new BPMBeamProfile(getHorizontalDistribution(), getVerticalDistribution(), pIntegratedCurrent) );
      notifyPropertyChange(BeamMeasurementDevice.PROFILE_PROPERTY, null, getBeamProfile());
   }

   @Override
   public void startMeasurements() throws Exception
   {
      // Stop continuous acquisition if needed
      if (getOperationMode() == BeamProfileMonitor.OperationMode.OPERATION_MODE_CONTINUOUS_ACQUISITION)
      {
         stopContinuousAcquisition();
         //Possible mistake causing the continuous acquisition to turn off? 5Mar18 -AMO
         //startContinuousAcquisition();

      }
      mLegacyProfile = null;

      startProfileAcquisition();
      // over write
//      Blak.feedbackClient.setProfilingBpm(this);
      Controller.feedbackClient.setProfilingBpm(this);
      mAcquiringProfile = true;
   }

   @Override
   public void stopMeasurements() throws Exception
   {
//      Blak.feedbackClient.setProfilingBpm(null);
	   Controller.feedbackClient.setProfilingBpm(null);
   }

   @Override
   public BeamProfile getBeamProfile()
   {
      return mLegacyProfile;
   }

   public void setProfileHorizontalValue(int pIndex, double pValue)
   {
      if (mLegacyProfile == null)
      {
         mLegacyProfile = createEmptyBeamProfile();
      }
      mLegacyProfile.mHorizontalCurrentValues.setY(pIndex, pValue);
   }

   public void setProfileVerticalValue(int pIndex, double pValue)
   {
      if (mLegacyProfile == null)
      {
         mLegacyProfile = createEmptyBeamProfile();
      }
      mLegacyProfile.mVerticalCurrentValues.setY(pIndex, pValue);
   }

   public void setProfileIntegratedCurrent(double pValue)
   {
      if (mLegacyProfile == null)
      {
         mLegacyProfile = createEmptyBeamProfile();
      }
      mLegacyProfile.setIntegratedCurrent(pValue);
   }

   private BPMBeamProfile createEmptyBeamProfile()
   {
      Distribution hDist = new Distribution(getHorizontalDistribution());
      Distribution vDist = new Distribution(getVerticalDistribution());
      for (int c = 0; c < 16; ++c)
      {
         hDist.setY(c, 0);
         vDist.setY(c, 0);
      }
      return new BPMBeamProfile(hDist, vDist);
   }
}
