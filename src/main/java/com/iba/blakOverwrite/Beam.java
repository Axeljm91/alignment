// ///////////////////////////////////////////////////////////////////
// Copyright (c) by Ion Beam Applications S.A.
// All rights reserved
//
// Rue du cyclotron, 3
// B-1348 Louvain-la-Neuve
// Tel : +32.10.47.58.11
// ///////////////////////////////////////////////////////////////////

package com.iba.blakOverwrite;

import com.csvreader.CsvReader;
import com.iba.blak.Blak;
import com.iba.blak.BlakConstants;
import com.iba.blak.BlakPreferences;
import com.iba.blak.BpsControllerProxy;
import com.iba.blak.common.BlakICompClient;
import com.iba.blak.common.PopupDisplayer;
import com.iba.blak.common.Utils;
import com.iba.blak.device.api.EcubtcuException;
import com.iba.blak.device.impl.BcreuFactory.ExtAbstractBcreuProxy;
import com.iba.blak.scanningcontroller.SCIrradiationController;
import com.iba.blak.scanningcontroller.ScanningControllerClient;
import com.iba.icomp.comm.daq.JavaCuDaqService;
import com.iba.icomp.core.component.AbstractPropertyChangeProvider;
import com.iba.icomp.core.event.Event;
import com.iba.icomp.core.event.EventFactoryWithConfirmationEvent;
import com.iba.icomp.core.event.EventReceiver;
import com.iba.icomp.core.util.Logger;
import com.iba.pts.bms.bds.common.proxy.SmpsControllerProxy;
import com.iba.pts.bms.bss.beamscheduler.proxy.BeamSchedulerProxy;
import com.iba.pts.bms.bss.bps.devices.api.Bcreu;
import com.iba.pts.bms.bss.bps.devices.api.Llrf;
import com.iba.pts.bms.bss.bps.devices.impl.DegraderBeamStopProxy;
import com.iba.pts.bms.bss.bps.devices.impl.LlrfProxy;
import com.iba.pts.bms.bss.controller.api.BssController;
import com.iba.pts.bms.bss.controller.proxy.BssControllerProxy;
import com.iba.pts.bms.common.settings.impl.BmsLayerSettings;
import com.iba.pts.bms.common.settings.impl.BmsSettings;
import com.iba.pts.bms.common.settings.impl.DefaultBmsLayerSettings;
import com.iba.pts.bms.common.settings.impl.DefaultBmsSettings;
import com.iba.pts.bms.common.settings.impl.pbs.DefaultPbsBdsLayerSettings;
import com.iba.pts.bms.common.settings.impl.pbs.PbsBdsLayerSettings;
import com.iba.pts.bms.common.settings.impl.pbs.PbsElementType;
import com.iba.pts.bms.common.settings.impl.pbs.PbsMap;
import com.iba.pts.bms.common.settings.impl.pbs.PbsMapElement;
import com.iba.pts.bms.datatypes.api.TreatmentMode;
import com.iba.pts.bms.datatypes.impl.pbs.PbsEquipmentElement;
import com.iba.pts.bms.datatypes.impl.pbs.PbsEquipmentMap;
import com.iba.pts.bms.datatypes.impl.pbs.PbsSlew;
import com.iba.pts.bms.datatypes.impl.pbs.PbsSpot;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.apache.poi.ss.usermodel.charts.ManualLayout;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import com.iba.blak.device.impl.BcreuFactory;
import com.iba.ialign.Controller;
import com.iba.pts.bms.datatypes.api.TreatmentMode;


public class Beam extends AbstractPropertyChangeProvider implements EventReceiver
{

//   public ExtAbstractBcreuProxy bcreu = BcreuFactory.createBcreuProxy(
//         BlakPreferences.getCurrentSiteString(BlakConstants.BCREU_FIRMWARE_VERSION));
	
	public ExtAbstractBcreuProxy bcreu = BcreuFactory.createBcreuProxy("1.23");
	public BpsControllerProxy bpsController = new BpsControllerProxy();
	
   public BssControllerProxy bssController = new BssControllerProxy();

   public DegraderBeamStopProxy degrader = new DegraderBeamStopProxy();
   public LlrfProxy llrf = new LlrfProxy();
   public JavaCuDaqService daq = new JavaCuDaqService();
   public BeamSchedulerProxy beamScheduler = new BeamSchedulerProxy();
//   public SmpsControllerProxy smpsController = new SmpsControllerProxy();
   String MAX_BEAM_CURRENT_PROPERTY = "maxBeamCurrent";
   String BEAM_CONTROL_MODE_PROPERTY = "beamControlMode";
   private int mBeamControlMode;// 0:through ecubtcu; 1: through bcp; 2: through
   // scanning controller.

   private boolean singlePulse = true;

   private PbsBdsLayerSettings mBdsLayerSettings;

   private float mMaxDuration;
   private float mBeamCurrent;

   private ScanningControllerClient mScClient;
   private SCIrradiationController mSC;
   private BmsSettings mBmsSettings;
   // hold the feedback from BCREU
   private double mMaxBeamCurrent = 10; // if bcreu.isRegulationRunning() is
   // true
   // it indicates the max beam current
   // that the Cyclotron can produce at
   // its exit



   public Beam()
   {
/*      Resource res = new ClassPathResource("ScanAlgoConfig/Single_Spot.csv");
      try
      {
         mBdsLayerSettings = PbsBdsLayerSettingsReader.load(res.getInputStream());
      }
      catch (IOException e)
      {
         Logger.getLogger().error("Read file error: " + e.getMessage());
         return;
      }
      updateSettings();
      createBmsSettings();

      mScClient = Blak.sc;
      mSC = new SCIrradiationController(mScClient);
      mSC.start();
      mSC.addPropertyChangeListener(new PropertyChangeListener()
      {
         @Override
         public void propertyChange(PropertyChangeEvent pEvt)
         {
            if (SCIrradiationController.STATE_PROPERTY.equals(pEvt.getPropertyName())
                  && mBeamControlMode == 2)
            {
               SCIrradiationController.State state = (SCIrradiationController.State) pEvt.getNewValue();
               switch (state)
               {
                  case UNKNOWN:
                  case CANCELING:
                  case CANCELED:
                  case FINISHED:
                  case ERROR:
                     updateSinglePulseMode(true);
                     break;
                  case INITIALIZING:
                  case PREPARING_MAP:
                  case IRRADIATING_MAP:
                     updateSinglePulseMode(false);
                     break;
               }
            }
         }
      });*/
   }

   public void setScanningControllerClient(ScanningControllerClient pClient)
   {
      mScClient = pClient;

      mSC = new SCIrradiationController(mScClient);
      mSC.start();
      mSC.addPropertyChangeListener(new PropertyChangeListener()
      {
         @Override
         public void propertyChange(PropertyChangeEvent pEvt)
         {
            if (SCIrradiationController.STATE_PROPERTY.equals(pEvt.getPropertyName())
                  && mBeamControlMode == 2)
            {
               SCIrradiationController.State state = (SCIrradiationController.State) pEvt.getNewValue();
               switch (state)
               {
                  case UNKNOWN:
                  case CANCELING:
                  case CANCELED:
                  case FINISHED:
                  case ERROR:
                     updateSinglePulseMode(true);
                     break;
                  case INITIALIZING:
                  case PREPARING_MAP:
                  case IRRADIATING_MAP:
                     updateSinglePulseMode(false);
                     break;
               }
            }
         }
      });
   }

   public ScanningControllerClient getScClient()
   {
      return mScClient;
   }

   public int getBeamControlMode()
   {
      return mBeamControlMode;
   }

   public void setBeamControlMode(int pMode)
   {
      firePropertyChange(BEAM_CONTROL_MODE_PROPERTY, mBeamControlMode, mBeamControlMode = pMode);
   }

   public void singlePulse(boolean single) throws EcubtcuException
   {
      Logger.getLogger().debug("  -> Request " + (single ? "single" : "continuous") + " pulse");

      switch (mBeamControlMode)
      {
         case 0:
//            Blak.ecubtcu.iseuRequestSetSinglePulseMode(single);
            Controller.ecubtcu.iseuRequestSetSinglePulseMode(single);
            break;
         case 1:
            if (!single)
            {
               bcreu.setContinuousPulse(!single);
               bcreu.startBeamPulses();
            }
            else
            {
               bcreu.stopBeamPulses();
            }
            break;
         case 2:
            if (single)
            {
               mSC.cancel();
            }
            else
            {
               // irradiate the first bds layer
               PbsBdsLayerSettings settings = null;
               for (BmsLayerSettings bmsLayerSetting : mBmsSettings.getLayerSettings())
               {
                  settings = (PbsBdsLayerSettings) bmsLayerSetting.getBdsLayerSettings();
                  break;
               }
               mSC.setCurrentBdsLayerSettings(settings);
               mSC.irradiate();
            }
            break;
         default:
 //           Blak.ecubtcu.iseuRequestSetSinglePulseMode(single);
        	 Controller.ecubtcu.iseuRequestSetSinglePulseMode(single);

      }
   }

   public float getMaxDurationTime()
   {
      return mMaxDuration;
   }

   public float getBeamCurrent()
   {
      return mBeamCurrent;
   }

   public void setBeamCurrent(float pBeamCurrent)
   {
      for (PbsMap map : mBdsLayerSettings.getMaps())
      {
         for (PbsMapElement element : map.getElements())
         {
            if (element instanceof PbsSpot)
            {
               ((PbsSpot) element).mBeamCurrentSetpoint = pBeamCurrent / 1000; // from
               // mA
               // to
               // A
            }
         }
      }

      mBeamCurrent = pBeamCurrent;
   }

   public boolean selectBeamline(int pBeamline)
   {
      return Blak.sc.selectBeamline(pBeamline);
   }

   public void updateSinglePulseMode(boolean b)
   {
      singlePulse = b;
   }

   public boolean isSinglePulseMode()
   {
      return singlePulse;
   }

   public void requestMaxBeamCurrent(int roomid, double current)
   {
//      bpsController.startPrepareActivity(roomid, current,
//            TreatmentMode.fromShortName(Blak.room.getTreatmentMode().toUpperCase()));
//	   bpsController.startPrepareActivity(roomid, current, TreatmentMode.fromShortName("PBS"));
//DSL 12/08/2016 Method above no longer supported in blak.
        bpsController.startPrepareActivity(roomid, current);
   }

   public void updateMaxBeamCurrent(double current)
   {
      firePropertyChange(MAX_BEAM_CURRENT_PROPERTY, mMaxBeamCurrent, mMaxBeamCurrent = current);
   }

   public double getMaxBeamCurrent()
   {
      return mMaxBeamCurrent;
   }

   public void setupBeamScheduler()
   {
      // BlakICompFeedbackClient icompClient = (BlakICompFeedbackClient)
      // Blak.feedbackClient;
      BlakICompNoSiteClient icompClient = Controller.feedbackClient.getICompClient();
      beamScheduler.setName("beamSchedulerProxy");
      beamScheduler.setComponentName("beamScheduler");
      beamScheduler.setEventBus(icompClient.eventBus);
      beamScheduler.setEventFactory(icompClient.eventFactory);
      beamScheduler.setPropertyDefinitionDictionary(icompClient.propertyDefinitionDictionary);
      beamScheduler.setTimerFactory(icompClient.timerFactory);
   }

   public void setupBssController()
   {
      // BlakICompFeedbackClient icompClient = (BlakICompFeedbackClient)
      // Blak.feedbackClient;
//      BlakICompClient icompClient = Blak.feedbackClient.getICompClient();
	  BlakICompNoSiteClient icompClient = Controller.feedbackClient.getICompClient();
      bssController.setName("bssControllerProxy");
      bssController.setComponentName("bssController");
      bssController.setEventBus(icompClient.eventBus);
      bssController.setEventFactory(icompClient.eventFactory);
      bssController.setPropertyDefinitionDictionary(icompClient.propertyDefinitionDictionary);
      bssController.setTimerFactory(icompClient.timerFactory);
      bssController.setResponseTimeOut(2000);
   }

   public void setupBpsController()
   {
      // BlakICompFeedbackClient icompClient = (BlakICompFeedbackClient)
      // Blak.feedbackClient;
//      BlakICompClient icompClient = Blak.feedbackClient.getICompClient();
	  BlakICompNoSiteClient icompClient = Controller.feedbackClient.getICompClient();
      bpsController.setName("bpsControllerProxy");
      bpsController.setComponentName("bpsController");
      bpsController.setEventBus(icompClient.eventBus);
      bpsController.setEventFactory(icompClient.eventFactory);
      bpsController.setPropertyDefinitionDictionary(icompClient.propertyDefinitionDictionary);
      bpsController.setTimerFactory(icompClient.timerFactory);
   }

   public void setupDegrader()
   {
      // BlakICompFeedbackClient icompClient = (BlakICompFeedbackClient)
      // Blak.feedbackClient;
//      BlakICompClient icompClient = Blak.feedbackClient.getICompClient();
      BlakICompNoSiteClient icompClient = Controller.feedbackClient.getICompClient();
      degrader.setName("DEGRADER-PROXY");
      degrader.setComponentName("DEGRADER");
      degrader.setEventBus(icompClient.eventBus);
      degrader.setEventFactory(icompClient.eventFactory);
      degrader.setPropertyDefinitionDictionary(icompClient.propertyDefinitionDictionary);
      degrader.setTimerFactory(icompClient.timerFactory);
      //degrader.init();

   }

    public void setupLlrf()
    {
        // BlakICompFeedbackClient icompClient = (BlakICompFeedbackClient)
        // Blak.feedbackClient;
//      BlakICompClient icompClient = Blak.feedbackClient.getICompClient();
        BlakICompNoSiteClient icompClient = Controller.feedbackClient.getICompClient();
        llrf.setName("LLRF-PROXY");
        llrf.setComponentName("LLRF");
        llrf.setEventBus(icompClient.eventBus);
        llrf.setEventFactory(icompClient.eventFactory);
        llrf.setPropertyDefinitionDictionary(icompClient.propertyDefinitionDictionary);
        llrf.setTimerFactory(icompClient.timerFactory);

    }

   public void setupBdsController()
   {
      // BlakICompFeedbackClient icompClient = (BlakICompFeedbackClient)
      // Blak.feedbackClient;
/*      BlakICompClient icompClient = Blak.feedbackClient.getICompClient();
      smpsController.setName("smpsControllerProxy");
      smpsController.setComponentName("smpsController");
      smpsController.setEventBus(icompClient.eventBus);
      smpsController.setEventFactory(icompClient.eventFactory);
      smpsController.setPropertyDefinitionDictionary(icompClient.propertyDefinitionDictionary);
      smpsController.setTimerFactory(icompClient.timerFactory);*/
   }

   public void setupBcreu()
   {
      // BlakICompFeedbackClient icompClient = (BlakICompFeedbackClient)
      // Blak.feedbackClient;
//      BlakICompClient icompClient = Blak.feedbackClient.getICompClient();
      BlakICompNoSiteClient icompClient = Controller.feedbackClient.getICompClient();
      bcreu.setName("BCREU-PROXY");
      bcreu.setComponentName("BCREU");
      bcreu.setEventBus(icompClient.eventBus);
      bcreu.setEventFactory(new EventFactoryWithConfirmationEvent(icompClient.eventFactory));
      bcreu.setPropertyDefinitionDictionary(icompClient.propertyDefinitionDictionary);
      bcreu.setTimerFactory(icompClient.timerFactory);

      bcreu.addPropertyChangeListener(new PropertyChangeListener()
      {
         public void propertyChange(PropertyChangeEvent pEvt)
         {
            if (Bcreu.DISPLAYED_MAX_BEAM_CURRENT.equals(pEvt.getPropertyName()) || Bcreu.RUNNING_STATE.equals(
                  pEvt.getPropertyName()))
            {
               if (bcreu.isRegulationRunning())
               {
                  updateMaxBeamCurrent(bcreu.getDisplayedMaxBeamCurrent());
               }
            }
            if (mBeamControlMode == 1)
            {
               if (bcreu.getRoomId() == -1 && bcreu.isRegulationRunning()
                     && (bcreu.getRequestedState() & 1 << 4) != 0 && bcreu.isInContinuousPulseMode())
               {
                  updateSinglePulseMode(false);
               }
               else
               {
                  updateSinglePulseMode(true);
               }
            }
            // debug infomation for Bcreu, to be removed
            Logger.getLogger().debug("Feedback Maximal Beam Current: " + bcreu.getFeedbackMaxBeamCurrent());
            Logger.getLogger().debug("Displayed Maximal Beam Current: " + bcreu.getDisplayedMaxBeamCurrent());
            Logger.getLogger().debug(
                  "Regulation is " + (bcreu.isRegulationRunning() ? "" : "NOT") + " Running: ");
         }
      });
   }

   @Override
   public void receive(Event pEvent)
   {

   }

   public void setMaxDuration(float pMaxDuration)
   {
      for (PbsMap map : mBdsLayerSettings.getMaps())
      {
         int spotSize = 0;
         for (PbsMapElement element : map.getElements())
         {
            if (element instanceof PbsSpot)
            {
               ++spotSize;
            }
         }

         for (PbsMapElement element : map.getElements())
         {
            if (element instanceof PbsSpot)
            {
               ((PbsSpot) element).mIrradiationTime = pMaxDuration / spotSize;
            }
         }
      }

      mMaxDuration = pMaxDuration;
   }

   @Override
   protected void publishLockedProperties()
   {
      // no locked properties
   }

   // /////////////////////////////////////////////////////////////////////////////////////////

   private void updateSettings()
   {
      for (PbsMap map : mBdsLayerSettings.getMaps())
      {
         for (PbsMapElement element : map.getElements())
         {
            if (element instanceof PbsSpot)
            {
               mBeamCurrent = ((PbsSpot) element).mBeamCurrentSetpoint * 1000; // from
               // A
               // to
               // mA
               mMaxDuration += ((PbsSpot) element).mIrradiationTime;
            }
         }
      }
   }

   private void createBmsSettings()
   {
      mBmsSettings = new DefaultBmsSettings();
      mBmsSettings.getLayerSettings();

      DefaultBmsLayerSettings bmsLayerSettings = new DefaultBmsLayerSettings();
      bmsLayerSettings.setOpticalSolution(mBdsLayerSettings.getSpotTuneId());
      bmsLayerSettings.setRangeAtNozzleEntrance(mBdsLayerSettings.getRangeAtNozzleEntrance());
      bmsLayerSettings.setRangeInPatient(mBdsLayerSettings.getRangeInPatient());
      bmsLayerSettings.setBdsLayerSettings(mBdsLayerSettings);
      bmsLayerSettings.setMetersetIC2(mBdsLayerSettings.getSpecifiedMeterset());
      bmsLayerSettings.setMetersetIC3(mBdsLayerSettings.getSpecifiedMeterset());
      mBmsSettings.getLayerSettings().add(bmsLayerSettings);
   }

   private static class PbsBdsLayerSettingsReader
   {
      public static PbsBdsLayerSettings load(InputStream filename)
      {
         PbsBdsLayerSettings result = null;
         // mBdsLayerSettings;
         try
         {
            InputStreamReader fr = new InputStreamReader(filename, BlakConstants.DEFAULT_CHARSET.name());

            CsvReader csvr = new CsvReader(fr, ',');

            int elemNbr = 0;

            List<String> header = new ArrayList<String>();

            // first line
            while (csvr.readRecord())
            {

               String elementName = csvr.get(0);
               // Skip comment lines
               if (elementName.isEmpty())
               {
                  continue;
               }

               if (elementName.equals("#LAYER_ID"))
               {
                  header = Arrays.asList(csvr.getValues());
                  continue;
               }

//                pLayer.getLayerIndex(), pLayer.getRangeAtNozzleEntrance(),
//                map.getTotalCharge(), elemNbr, diagn,
//                pLayer.getMetersetCorrectionFactor()
//               result = new DefaultPbsBdsLayerSettings(
//                     Integer.parseInt(csvr.get(header.indexOf("#LAYER_ID"))), "Spot1",
//                     Double.parseDouble(csvr.get(header.indexOf("RANGE"))),// range
////                      in
////                      patient
//                     Double.parseDouble(csvr.get(header.indexOf("RANGE"))),// range
////                      at
////                      nozzle
////                      entrance
//                     Double.parseDouble(csvr.get(header.indexOf("TOTAL_CHARGE"))),// totalcharge
//                     1); // scatterer position [1..4]
               elemNbr = Integer.parseInt(csvr.get(5));
               break;
            }

            try
            {
               PbsEquipmentMap map = new PbsEquipmentMap();

               List<PbsMapElement> elements = new LinkedList<PbsMapElement>();
               List<String> dataHeader = new ArrayList<String>();
               for (int i = 0; i < elemNbr; )
               {
                  csvr.readRecord();

                  String elementName = csvr.get(0);
                  // Skip comment lines
                  if (elementName.isEmpty())
                  {
                     continue;
                  }

                  if (elementName.equals("#ELEMENT_ID"))
                  {
                     dataHeader = Arrays.asList(csvr.getValues());
                     continue;
                  }

                  if (csvr.getValues().length != 79)
                  {
                     throw new Exception("The PbsBdsLayerSetting file format not supported!");
                  }

                  PbsEquipmentElement e;

                  switch (PbsElementType.values()[Integer.parseInt(
                        csvr.get(dataHeader.indexOf("ELEMENT_TYPE")))])
                  {
                     case SPOT:
                        e = new PbsSpot();
                        break;
                     case SLEW:
                        e = new PbsSlew();
                        break;
                     default:// spot
                        e = new PbsSpot();
                        break;
                  }

                  if (e instanceof PbsSpot)
                  {
                     ((PbsSpot) e).mTargetCharge = Float.parseFloat(
                           csvr.get(dataHeader.indexOf("TARGET_CHARGE")));
                     ((PbsSpot) e).mBeamCurrentSetpoint = Float.parseFloat(
                           csvr.get(dataHeader.indexOf("BEAM_CURRENT_SP")));
                     // two INACTIVE
                     ((PbsSpot) e).mIrradiationTime = Float.parseFloat(
                           csvr.get(dataHeader.indexOf("MAX_DURATION")));
                     ((PbsSpot) e).mChargeMinPrimary = Float.parseFloat(
                           csvr.get(dataHeader.indexOf("MIN_CHARGE_PRIM")));
                     ((PbsSpot) e).mChargeMaxPrimary = Float.parseFloat(
                           csvr.get(dataHeader.indexOf("MAX_CHARGE_PRIM")));
                     ((PbsSpot) e).mChargeMinSecondary = Float.parseFloat(
                           csvr.get(dataHeader.indexOf("MIN_CHARGE_SEC")));
                     ((PbsSpot) e).mChargeMaxSecondary = Float.parseFloat(
                           csvr.get(dataHeader.indexOf("MAX_CHARGE_SEC")));
                     ((PbsSpot) e).mChargeMinTernary = Float.parseFloat(
                           csvr.get(dataHeader.indexOf("MIN_CHARGE_TER")));
                     ((PbsSpot) e).mChargeMaxTernary = Float.parseFloat(
                           csvr.get(dataHeader.indexOf("MAX_CHARGE_TER")));
                     // two INACTIVE MIN_IC1_CHARGE MAX_IC1_CHARGE

                     ((PbsSpot) e).mChargeRateMinPrimary = Float.parseFloat(
                           csvr.get(dataHeader.indexOf("MIN_DOSE_RATE_PRIM")));
                     ((PbsSpot) e).mChargeRateMaxPrimary = Float.parseFloat(
                           csvr.get(dataHeader.indexOf("MAX_DOSE_RATE_PRIM")));
                     ((PbsSpot) e).mChargeRateMinSecondary = Float.parseFloat(
                           csvr.get(dataHeader.indexOf("MIN_DOSE_RATE_SEC")));
                     ((PbsSpot) e).mChargeRateMaxSecondary = Float.parseFloat(
                           csvr.get(dataHeader.indexOf("MAX_DOSE_RATE_SEC")));
                     ((PbsSpot) e).mChargeRateMinTernary = Float.parseFloat(
                           csvr.get(dataHeader.indexOf("MIN_DOSE_RATE_TER")));
                     ((PbsSpot) e).mChargeRateMaxTernary = Float.parseFloat(
                           csvr.get(dataHeader.indexOf("MAX_DOSE_RATE_TER")));
                     // two INACTIVE MIN_DOSE_RATE_IC1 MAX_DOSE_RATE_IC1
                     ((PbsSpot) e).mICCycloMin = Float.parseFloat(
                           csvr.get(dataHeader.indexOf("MIN_BEAM_CURRENT_FB")));
                     ((PbsSpot) e).mICCycloMax = Float.parseFloat(
                           csvr.get(dataHeader.indexOf("MAX_BEAM_CURRENT_FB")));
                  }
                  e.mCurrentSetpointX = Float.parseFloat(csvr.get(dataHeader.indexOf("X_CURRENT_TARGET_SP")));
                  e.mCurrentSetpointY = Float.parseFloat(csvr.get(dataHeader.indexOf("Y_CURRENT_TARGET_SP")));

                  e.mCurrentFeedbackMinXPrimary = Float.parseFloat(
                        csvr.get(dataHeader.indexOf("X_CURRENT_MIN_PRIM_FB")));
                  e.mCurrentFeedbackMaxXPrimary = Float.parseFloat(
                        csvr.get(dataHeader.indexOf("X_CURRENT_MAX_PRIM_FB")));
                  e.mCurrentFeedbackMinYPrimary = Float.parseFloat(
                        csvr.get(dataHeader.indexOf("Y_CURRENT_MIN_PRIM_FB")));
                  e.mCurrentFeedbackMaxYPrimary = Float.parseFloat(
                        csvr.get(dataHeader.indexOf("Y_CURRENT_MAX_PRIM_FB")));
                  e.mVoltageFeedbackMinXPrimary = Float.parseFloat(
                        csvr.get(dataHeader.indexOf("X_VOLT_MIN_PRIM_FB")));
                  e.mVoltageFeedbackMaxXPrimary = Float.parseFloat(
                        csvr.get(dataHeader.indexOf("X_VOLT_MAX_PRIM_FB")));
                  e.mVoltageFeedbackMinYPrimary = Float.parseFloat(
                        csvr.get(dataHeader.indexOf("Y_VOLT_MIN_PRIM_FB")));
                  e.mVoltageFeedbackMaxYPrimary = Float.parseFloat(
                        csvr.get(dataHeader.indexOf("Y_VOLT_MIN_PRIM_FB")));
                  e.mCurrentFeedbackMinXRedundant = Float.parseFloat(
                        csvr.get(dataHeader.indexOf("X_CURRENT_MIN_SEC_FB")));
                  e.mCurrentFeedbackMaxXRedundant = Float.parseFloat(
                        csvr.get(dataHeader.indexOf("X_CURRENT_MAX_SEC_FB")));
                  e.mCurrentFeedbackMinYRedundant = Float.parseFloat(
                        csvr.get(dataHeader.indexOf("Y_CURRENT_MIN_SEC_FB")));
                  e.mCurrentFeedbackMaxYRedundant = Float.parseFloat(
                        csvr.get(dataHeader.indexOf("Y_CURRENT_MAX_SEC_FB")));
                  e.mVoltageFeedbackMinXRedundant = Float.parseFloat(
                        csvr.get(dataHeader.indexOf("X_VOLT_MIN_SEC_FB")));
                  e.mVoltageFeedbackMaxXRedundant = Float.parseFloat(
                        csvr.get(dataHeader.indexOf("X_VOLT_MAX_SEC_FB")));
                  e.mVoltageFeedbackMinYRedundant = Float.parseFloat(
                        csvr.get(dataHeader.indexOf("Y_VOLT_MIN_SEC_FB")));
                  e.mVoltageFeedbackMaxYRedundant = Float.parseFloat(
                        csvr.get(dataHeader.indexOf("Y_VOLT_MAX_SEC_FB")));

                  // 20 INACTIVE

                  if (e instanceof PbsSpot)
                  {
                     ((PbsSpot) e).getXMinField();
                     ((PbsSpot) e).mXMinBeamSizeFeedback = Float.parseFloat(
                           csvr.get(dataHeader.indexOf("X_MIN_WIDTH")));
                     ((PbsSpot) e).mXMaxBeamSizeFeedback = Float.parseFloat(
                           csvr.get(dataHeader.indexOf("X_MAX_WIDTH")));
                     ((PbsSpot) e).mYMinBeamSizeFeedback = Float.parseFloat(
                           csvr.get(dataHeader.indexOf("Y_MIN_WIDTH")));
                     ((PbsSpot) e).mYMaxBeamSizeFeedback = Float.parseFloat(
                           csvr.get(dataHeader.indexOf("Y_MAX_WIDTH")));

                     ((PbsSpot) e).mXMinPositionFeedback = Float.parseFloat(
                           csvr.get(dataHeader.indexOf("X_POS_LOW")));
                     ((PbsSpot) e).mXMaxPositionFeedback = Float.parseFloat(
                           csvr.get(dataHeader.indexOf("X_POS_HIGH")));
                     ((PbsSpot) e).mYMinPositionFeedback = Float.parseFloat(
                           csvr.get(dataHeader.indexOf("Y_POS_LOW")));
                     ((PbsSpot) e).mYMaxPositionFeedback = Float.parseFloat(
                           csvr.get(dataHeader.indexOf("Y_POS_HIGH")));

                     ((PbsSpot) e).mXMinNozzleEntranceWidth = Float.parseFloat(
                           csvr.get(dataHeader.indexOf("X_MIN_WIDTH_IC1")));
                     ((PbsSpot) e).mXMaxNozzleEntranceWidth = Float.parseFloat(
                           csvr.get(dataHeader.indexOf("X_MAX_WIDTH_IC1")));
                     ((PbsSpot) e).mYMinNozzleEntranceWidth = Float.parseFloat(
                           csvr.get(dataHeader.indexOf("Y_MIN_WIDTH_IC1")));
                     ((PbsSpot) e).mYMaxNozzleEntranceWidth = Float.parseFloat(
                           csvr.get(dataHeader.indexOf("Y_MAX_WIDTH_IC1")));

                     ((PbsSpot) e).mXMinNozzleEntrancePosition = Float.parseFloat(
                           csvr.get(dataHeader.indexOf("X_POS_LOW_IC1")));
                     ((PbsSpot) e).mXMaxNozzleEntrancePosition = Float.parseFloat(
                           csvr.get(dataHeader.indexOf("X_POS_HIGH_IC1")));
                     ((PbsSpot) e).mYMinNozzleEntrancePosition = Float.parseFloat(
                           csvr.get(dataHeader.indexOf("Y_POS_LOW_IC1")));
                     ((PbsSpot) e).mYMaxNozzleEntrancePosition = Float.parseFloat(
                           csvr.get(dataHeader.indexOf("Y_POS_HIGH_IC1")));
                  }

                  elements.add(e);
                  ++i;
               }
               map.setElements(elements);

               if (result != null)
               {
                  ((DefaultPbsBdsLayerSettings) result).addMap(map);
                  ((DefaultPbsBdsLayerSettings) result).setCurrentMapToIrradiate(map);
               }
               else
               {
                  Logger.getLogger().warn("Layer settings are not loaded, file format not correct");
               }
            }
            catch (IOException e)
            {
               String msg = "Error reading PBS bds layer setting file '" + filename + "'";
               Logger.getLogger().error(msg);
               SwingUtilities.invokeLater(new PopupDisplayer(msg, "IO Error", JOptionPane.ERROR_MESSAGE));
            }
            catch (Exception e)
            {
               String msg = "Error reading PBS bds layer setting file: " + e.getMessage();
               Logger.getLogger().error(msg);
               SwingUtilities.invokeLater(
                     new PopupDisplayer(msg, Utils.getCurrentFunction(), JOptionPane.ERROR_MESSAGE));
            }

            csvr.close();
         }
         catch (IOException e)
         {
            String msg = "Could not open PBS bds layer setting file '" + filename + "'";
            Logger.getLogger().error(msg);
            SwingUtilities.invokeLater(new PopupDisplayer(msg, "IO Error", JOptionPane.ERROR_MESSAGE));
         }

         return result;
      }

   }

}
