// ///////////////////////////////////////////////////////////////////
// Copyright (c) by Ion Beam Applications S.A.
// All rights reserved
//
// Rue du cyclotron, 3
// B-1348 Louvain-la-Neuve
// Tel : +32.10.47.58.11
// ///////////////////////////////////////////////////////////////////

package com.iba.blakOverwrite;

import com.iba.blak.common.Distribution;
import com.iba.blak.common.Gaussian;
import com.iba.blak.device.api.BeamMeasurementDevice;
import com.iba.blak.device.api.BeamProfile;
import com.iba.blak.device.api.BeamProfileMonitor;
import com.iba.blak.device.api.EcubtcuException;
import com.iba.blak.device.api.LegacyBeamProfileMonitor;
//overwrite
import com.iba.device.Device;
import com.iba.ialign.Controller;

import com.iba.blak.device.impl.AcquisitionTimeoutException;

import com.iba.icomp.core.util.Logger;

public abstract class AbstractLegacyBeamProfileMonitor extends AbstractBeamProfileMonitor implements
      LegacyBeamProfileMonitor
{

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
    protected boolean isInserted;
    private int bufferSize = 1;
    private final int NB_CHANNELS = 16;
    protected double xCentroid;
    protected double yCentroid;
    protected double xSigma;
    protected double ySigma;
    private Distribution xDistrib;
    private Distribution yDistrib;
    private Gaussian xGaussian;
    private Gaussian yGaussian;
    private Device smartSocket;
    private String name;
    protected double[] abscissa;
    private double[] ordinates = new double[]{0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D};

    private double[] temp;

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
       // Stop continuous acquisition if needed
       //added start acq testing 5Mar18 -AMO
//       startContinuousAcquisition();
//
//       Thread.sleep(1100);
//
//      if (getOperationMode()== BeamProfileMonitor.OperationMode.OPERATION_MODE_CONTINUOUS_ACQUISITION)
//      {
//         stopContinuousAcquisition();
//      }
//      Thread.sleep(500);
//      mLegacyProfile = null;
//
//      // Start profile acquisition;
//      startProfileAcquisition();
//
//      //over write
//      //Blak.feedbackClient.setProfilingBpm(this);
////      while (Controller.feedbackClient.getProfilingBpm() != null){
////         Thread.sleep(100);
////      }
//      Controller.feedbackClient.setProfilingBpm(this);
//      mAcquiringProfile = true;
//
//
//      //Thread.sleep(1100);
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
//      Thread.sleep(500);
//      // As ECUBTCU need 200ms to start acquisition for new data ,Small delay to
//      // ensure the BLAK receive the updated values
//
//      if (mLegacyProfile != null)
//      {
//         verticalChannels = mLegacyProfile.getVerticalValues().Clone();
//         horizontalChannels = mLegacyProfile.getHorizontalValues().Clone();
//         setIntegratedCurrent(mLegacyProfile.getIntegratedCurrent());
//      }
//
//      //startContinuousAcquisition();
//
//      this.getHorizontalCentroid();
//      this.getVerticalCentroid();
//      this.getHorizontalSigma();
//      this.getVerticalSigma();
//
//      mAcquiringProfile = false;
//      Controller.feedbackClient.setProfilingBpm(null);
      this.mAcquiringProfile = true;
      this.startContinuousAcquisition();
      Thread.sleep(1100L);
      if (this.getOperationMode() == OperationMode.OPERATION_MODE_CONTINUOUS_ACQUISITION) {
         this.stopContinuousAcquisition();
      }

      Thread.sleep(500L);
      this.mLegacyProfile = null;
      this.startProfileAcquisition();
      Controller.feedbackClient.setProfilingBpm(this);
      long startTime = System.currentTimeMillis();

      do {
         if (this.isDataReady()) {
            Thread.sleep(500L);
            if (this.mLegacyProfile != null) {
               this.verticalChannels = this.mLegacyProfile.getVerticalValues().Clone();
               this.horizontalChannels = this.mLegacyProfile.getHorizontalValues().Clone();
               this.setIntegratedCurrent(this.mLegacyProfile.getIntegratedCurrent());
            }

            this.mAcquiringProfile = false;
            return;
         }

         Thread.sleep(100L);
      } while(System.currentTimeMillis() - startTime <= (long)this.getProfileAcquisitionTimeOut());

      this.stopProfileAcquisition();
      throw new AcquisitionTimeoutException();
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

//    public double getMeanX() {
//        return this.xCentroid;
//    }
//
//    public double getMeanY() {
//        return this.yCentroid;
//    }
//
//    public void setMeanX(double meanX) {
//        this.xCentroid = meanX;
//    }
//
//    public void setMeanY(double meanY) {
//        this.yCentroid = meanY;
//    }
//
//    public void refresh() {
//        new Random(5L);
//        boolean isAcquisitionDone = false;
//        Vector xBuffers = new Vector();
//        Vector yBuffers = new Vector();
//
//        for(int i = 0; i < 16; ++i) {
//            xBuffers.add(new RollingBuffer(this.bufferSize));
//            ((RollingBuffer)xBuffers.get(i)).setEqualityCheck(1.0E-4D, (double[])null);
//            ((RollingBuffer)xBuffers.get(i)).addLowMeanThresholdCheck(1.0E-5D);
//            ((RollingBuffer)xBuffers.get(i)).addStabilityCheck(1000000.0D);
//            yBuffers.add(new RollingBuffer(this.bufferSize));
//            ((RollingBuffer)yBuffers.get(i)).setEqualityCheck(1.0E-4D, (double[])null);
//            ((RollingBuffer)yBuffers.get(i)).addLowMeanThresholdCheck(1.0E-5D);
//            ((RollingBuffer)yBuffers.get(i)).addStabilityCheck(1000000.0D);
//        }
//
//        double startTime = (double)System.currentTimeMillis();
//        log.debug("startTime = " + startTime);
//        int debugCounter = 0;
//        int enoughCurrentMessage = 0;
//        double currentMemory = 0.0D;
//        int var11 = 0;
//
//        while(!isAcquisitionDone) {
//            log.trace("# of loop : " + debugCounter++);
//            double currentTime = (double)System.currentTimeMillis();
//            log.debug("currentTime = " + currentTime);
//            double diff = currentTime - startTime;
//            log.debug("difference : " + diff);
//            if (currentTime - startTime > 5000.0D) {
//                break;
//            }
//
//            for(int i = 0; i < 16; ++i) {
//                isAcquisitionDone = true;
//                if (!this.smartSocket.isConnected()) {
//                    this.smartSocket.connect();
//                }
//
//                double current = (Double)this.smartSocket.getTagValue(this.name + "_INT_CURRNENT");
//                if (current > 100.0D) {
//                    ++var11;
//                    if (i == 0) {
//                        ;
//                    }
//
//                    ((RollingBuffer)xBuffers.get(i)).pushValue((Double)this.smartSocket.getTagValue(this.name + "_Y_CHANNEL" + i));
//                    ((RollingBuffer)yBuffers.get(i)).pushValue((Double)this.smartSocket.getTagValue(this.name + "_X_CHANNEL" + i));
//                } else if (enoughCurrentMessage > -1) {
//                    log.warn("Acquisition failing on " + this.name + ". Verify that the BPM is on the beam path and the ISEU pulsing ~10 nA");
//                    enoughCurrentMessage = -1;
//                }
//
//                if (!((RollingBuffer)xBuffers.get(i)).checkBuffer() || !((RollingBuffer)yBuffers.get(i)).checkBuffer()) {
//                    isAcquisitionDone = false;
//                }
//            }
//        }
//
//        if (!isAcquisitionDone) {
//            log.error("I haven't been able to acquire on " + this.name + " in 5 sec.");
//            this.xCentroid = 0.0D;
//            this.yCentroid = 0.0D;
//            this.xSigma = 0.0D;
//            this.ySigma = 0.0D;
//        } else {
//            for(int i = 0; i < 16; ++i) {
//                this.xDistrib.setY(i, ((RollingBuffer)xBuffers.get(i)).getMean());
//                this.yDistrib.setY(i, ((RollingBuffer)yBuffers.get(i)).getMean());
//            }
//
//            this.xDistrib.setY(0, 0.0D);
//            this.xDistrib.setY(15, 0.0D);
//            this.yDistrib.setY(0, 0.0D);
//            this.yDistrib.setY(15, 0.0D);
//            this.xGaussian = this.xDistrib.getGaussian();
//            this.yGaussian = this.yDistrib.getGaussian();
//            this.xCentroid = this.xGaussian.getCentroid();
//            this.yCentroid = this.yGaussian.getCentroid();
//            this.xSigma = this.xGaussian.getSigma();
//            this.ySigma = this.yGaussian.getSigma();
//            log.info("BPM " + this.name + " size : " + Double.toString(-2.0D * this.abscissa[0]) + " mm");
//            log.info("BPM " + this.name + " : X centroid = " + (new DecimalFormat("#.##")).format(this.xCentroid).replaceAll(",", ".") + "  --  Y centroid = " + (new DecimalFormat("#.##")).format(this.yCentroid).replaceAll(",", "."));
//            log.info("BPM " + this.name + " : X sigma = " + (new DecimalFormat("#.##")).format(this.xSigma).replaceAll(",", ".") + "  --  Y sigma = " + (new DecimalFormat("#.##")).format(this.ySigma).replaceAll(",", "."));
//        }
//
//    }
}
