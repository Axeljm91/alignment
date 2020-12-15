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
import com.iba.blak.device.api.BeamProfile;
import com.iba.blak.device.api.BeamProfileMonitor;
import com.iba.blak.device.api.EcubtcuException;

//overwrite
import com.iba.blak.device.impl.AbstractInsertable;
import com.iba.blak.device.impl.BeamProfileConfiguration;



import com.iba.icomp.core.util.Logger;

public abstract class AbstractBeamProfileMonitor extends AbstractInsertable implements BeamProfileMonitor
{

   // TODO : Nb samples configurable et timeout en fct du nb samples
   public static final int MOVEMENT_CHECK_TIMEOUT = 4000;
   public static final int DEFAULT_PROFILE_ACQUISITION_TIMEOUT = 7500;
   private int mProfileAcquisitionTimeOut = DEFAULT_PROFILE_ACQUISITION_TIMEOUT;
   private static int mUpdatesCount = 0;
   protected volatile Distribution horizontalChannels;
   protected volatile Distribution verticalChannels;
   private boolean motionFeedbackAvailable = false;
   private int insertionFeedback = LS_POSITION_UNKNOWN;
   private int retractionFeedback = LS_POSITION_UNKNOWN;
   /**
    * Integrated current in nA.
    */
   private volatile double mIntegratedCurrent;
   /**
    * Configuration for the profile acquisition.
    */
   private BeamProfileConfiguration mProfileConfiguration = new BeamProfileConfiguration();
   /**
    * Storage for the acquired profile.
    */
   private BPMBeamProfile mProfile;
   /**
    * Current operation mode of the BPM device.
    */
   private volatile OperationMode mOperationMode = OperationMode.OPERATION_MODE_UNKNOWN;

   public AbstractBeamProfileMonitor(String n)
   {
      super(n);
      horizontalChannels = new Distribution(16);
      verticalChannels = new Distribution(16);
      updateChannelConfig();
   }

   protected AbstractBeamProfileMonitor(AbstractBeamProfileMonitor abpm)
   {
      super(abpm);
      motionFeedbackAvailable = abpm.motionFeedbackAvailable;
      insertionFeedback = abpm.insertionFeedback;
      retractionFeedback = abpm.retractionFeedback;
      horizontalChannels = abpm.horizontalChannels.Clone();
      verticalChannels = abpm.verticalChannels.Clone();
      mIntegratedCurrent = abpm.mIntegratedCurrent;
      mOperationMode = abpm.mOperationMode;
   }

   public double getHorizontalChannelValue(int c)
   {
      return horizontalChannels.getY(c);
   }

   public double getVerticalChannelValue(int c)
   {
      return verticalChannels.getY(c);
   }

   protected void updateChannelConfig()
   {
      for (int i = 0; i < horizontalChannels.getNbPoints(); ++i)
      {
         horizontalChannels.setX(i, getChannelCenter(i));
         verticalChannels.setX(i, getChannelCenter(i));
      }
   }

   public void updateHorizontalChannelValue(int c, double v)
   {
      horizontalChannels.setY(c, v);
      notifyNewData();
   }

   public void updateVerticalChannelValue(int c, double v)
   {
      verticalChannels.setY(c, v);
      notifyNewData();
   }

   public boolean isMotionFeedbackAvailable()
   {
      return motionFeedbackAvailable;
   }

   public void setMotionFeedbackAvailable(boolean mfa)
   {
      motionFeedbackAvailable = mfa;
   }

   public double getIntegratedCurrent()
   {
      return mIntegratedCurrent;
   }

   protected void setIntegratedCurrent(double pCurrent)
   {
      mIntegratedCurrent = pCurrent;
   }

   public void updateIntegratedCurrent(double ic)
   {
      mIntegratedCurrent = ic;
   }

   // Insert the BPM
   public void insert() throws EcubtcuException
   {
      Logger.getLogger().debug("  -> Request to insert BPM " + getName());
      Blak.ecubtcu.bpmInsert(getName());
   }

   // Extract the BPM
   public void retract() throws EcubtcuException
   {
      Logger.getLogger().debug("  -> Request to retract BPM " + getName());
      Blak.ecubtcu.bpmRetract(getName());
   }

   public double getHorizontalAmplitude()
   {
      try
      {
         return horizontalChannels.getGaussian().getAmplitude();
      }
      catch (NullPointerException e)
      {
         return Double.NaN;
      }
   }

   public double getVerticalAmplitude()
   {
      try
      {
         return verticalChannels.getGaussian().getAmplitude();
      }
      catch (NullPointerException e)
      {
         return Double.NaN;
      }
   }

   public double getHorizontalCentroid()
   {
      try
      {
         return horizontalChannels.getGaussian().getCentroid();
      }
      catch (NullPointerException e)
      {
         return Double.NaN;
      }
   }

   public double getVerticalCentroid()
   {
      try
      {
         return verticalChannels.getGaussian().getCentroid();
      }
      catch (NullPointerException e)
      {
         return Double.NaN;
      }
   }

   public double getHorizontalSigma()
   {
      try
      {
         return horizontalChannels.getGaussian().getSigma();
      }
      catch (NullPointerException e)
      {
         return Double.NaN;
      }
   }

   public double getVerticalSigma()
   {
      try
      {
         return verticalChannels.getGaussian().getSigma();
      }
      catch (NullPointerException e)
      {
         return Double.NaN;
      }
   }

   // TODO : Check in tracer, useful or not ?
   public Distribution getHorizontalDistribution()
   {
      return horizontalChannels;
   }

   // TODO : Check in tracer, useful or not ?
   public Distribution getVerticalDistribution()
   {
      return verticalChannels;
   }

   public OperationMode getOperationMode()
   {
      return mOperationMode;
   }

   protected void setOperationMode(OperationMode pMode)
   {
      OperationMode oldMode = getOperationMode();
      mOperationMode = pMode;
      Logger.getLogger().debug(getName() + " goes into \"" + mOperationMode.toString() + "\" mode");
      notifyPropertyChange(BeamProfileMonitor.OPERATION_MODE_PROPERTY, oldMode, pMode);
   }

   public void updateOperationMode(int om)
   {
      for (OperationMode m : OperationMode.values())
      {
         if (m.getValue() == om)
         {
            setOperationMode(m);
         }
      }
   }

   public boolean isDataReady()
   {
      return mOperationMode == OperationMode.OPERATION_MODE_DATA_READY;
   }

   protected BeamProfileConfiguration getProfileConfiguration()
   {
      return mProfileConfiguration;
   }

   public void setProfileConfiguration(BeamProfileConfiguration pProfileConfiguration)
   {
      mProfileConfiguration = pProfileConfiguration;
   }

   // BeamMeasurement device interface ----------------------------------------
   @Override
   public double getXPosition()
   {
      return getHorizontalCentroid();
   }

   @Override
   public double getYPosition()
   {
      return getVerticalCentroid();
   }

   @Override
   public int getRoom()
   {
      return 0;
   }

   @Override
   public BeamProfile getBeamProfile()
   {
      return mProfile;
   }

   protected void setBeamProfile(BPMBeamProfile pProfile)
   {
      mProfile = pProfile;
   }

   @Override
   public int getProfileAcquisitionTimeOut()
   {
      return mProfileAcquisitionTimeOut;
   }

   @Override
   public void setProfileAcquisitionTimeOut(int pProfileAcquisitionTimeOut)
   {
      mProfileAcquisitionTimeOut = pProfileAcquisitionTimeOut;
   }

   /**
    * Notifies new data. Since the data are updated one by one upon the reception from the RT client, the
    * notifications are spread.
    */
   private void notifyNewData()
   {
      ++mUpdatesCount;
      if (mUpdatesCount == 16)
      {
         notifyPropertyChange(NEW_DATA_PROPERTY, null,
               new BPMBeamProfile(horizontalChannels, verticalChannels));
         mUpdatesCount = 0;
      }
   }
}
