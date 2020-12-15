package com.iba.blakOverwrite;


import com.iba.blak.Blak;
import com.iba.blak.BlakConstants;
import com.iba.blak.BlakPreferences;
//import com.iba.blak.common.BlakICompClient;
import com.iba.blak.config.BeamLineElementNotFoundException;
import com.iba.blak.device.api.IonizationChamberBackend;
import com.iba.ialign.Controller;
//import com.iba.blak.feedback.BlakFeedbackClient;
import com.iba.icomp.comm.notif.NotifChannelReceiver;
import com.iba.icomp.comm.notif.NotifChannelSender;
import com.iba.icomp.core.component.AbstractBean;
import com.iba.icomp.core.component.ProxyPropertyChange;
import com.iba.icomp.core.event.DefaultPropertiesValueEvent;
import com.iba.icomp.core.event.Event;
import com.iba.icomp.core.event.EventBusToEventReceiver;
import com.iba.icomp.core.event.EventReceiver;
import com.iba.icomp.core.event.PropertiesChangedEvent;
import com.iba.icomp.core.event.PropertiesValueEvent;
import com.iba.icomp.core.event.xml.XmlEventConverter;
import com.iba.icomp.core.io.ChannelStream;
import com.iba.icomp.core.io.SocketChannelStream;
import com.iba.icomp.core.property.DefaultPropertyDefinition;
import com.iba.icomp.core.property.GenericPropertyFactory;
import com.iba.icomp.core.property.ListablePropertyDefinitionDictionary;
import com.iba.icomp.core.property.Property;
import com.iba.icomp.core.property.PropertyDefinition;
import com.iba.icomp.core.property.PropertyDefinition.Type;
import com.iba.icomp.core.service.AllOrNothingServiceController;
import com.iba.icomp.core.service.Service;
import com.iba.icomp.core.service.ServiceController;
import com.iba.icomp.core.service.ServiceListener;
import com.iba.icomp.core.timer.Scheduled;
import com.iba.icomp.core.timer.Timer;
import com.iba.icomp.core.util.Logger;
import com.iba.pts.bms.bds.devices.api.IonizationChamber;
import com.iba.pts.bms.bds.devices.impl.PbsIonizationChamberProxy;
import com.iba.pts.bms.common.config.constant.ChannelConstants;
import com.iba.pts.bms.common.settings.impl.IcStripConfig;
import com.iba.pts.bms.datatypes.api.BeamDeliveryPoint;
import com.iba.pts.bms.datatypes.api.NozzleType;
import com.iba.pts.bms.datatypes.api.TherapyCentre;
import com.iba.pts.bms.datatypes.impl.NozzleTypesBuilder;
import com.iba.pts.bms.datatypes.impl.TherapyCentreImpl;
import com.iba.pts.treatmentroomsession.TreatmentRoomSession;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * A class responsible for dispatching the feedback received from various devices using IComp events. There
 * are
 *
 * @author jmfranc
 * @modify by xjzhou
 */
public class blakNotifFeedbackClient extends BlakEcubtcuFeedbackClient implements EventReceiver, ServiceListener
{
//   private static final String EVENT_ORIGIN = BlakICompClient.CONTAINER_NAME;
   private static final String EVENT_ORIGIN = BlakICompNoSiteClient.CONTAINER_NAME;

   private static final String CONTAINER_ALIVE = "alive";
   private static final int DEFAULT_PORT = 16540;
   private static final PropertyDefinition DEFAULT_PROPERTY_DEFINITION = new DefaultPropertyDefinition(
         "CUDaq.LegacyVariable", Type.STRING, false, false);
   private static final PropertyDefinition TSM_PROPERTY_DEFINITION = new DefaultPropertyDefinition(
         "TSM.TreatmentRoomSession", Type.STRING, false, false);
   private static final String DR_FB_CHANNEL_PREFIX = ChannelConstants.MCR_TCR_BDS_DEVICES_NOTIF + "-"; // DR
   // feedback
   // channel
   private static final String DR_CMD_CHANNEL = "DataRecorderCommand"; // DR
   // command
   private static final String CONTAINER_FB_CHANNEL_NAME = "Container";// Container
   // alive
   // status
   private static final String BCP_CONTAINER_NAME = "BEAM-COMMON-PROCESS";
   private static final String BAPP_CONTAINER_NAME = "BEAM-ACCESS-POINT-PROCESS-IBTR3";
   private ContainerMonitor mBcpMonitor = new ContainerMonitor(BCP_CONTAINER_NAME);
   private ContainerMonitor mBapp3Monitor = new ContainerMonitor(BAPP_CONTAINER_NAME);
   /**
    * The 'alive' status is normally sent every 1s by the BCP, but it sometimes take about 3s.
    */
   private static final long BCP_MONITOR_TIMEOUT = 10000;
   private static final String BDS_CONTROLL_COMMAND_CHANNEL_NAME = "SmpsControllerCommand"; // Send
   // feedbacks
   // from
   //
   // BDS common controller
   // through
   // this
   // channel

   // commands
   // to
   //
   // BDS common controller
   // through
   // this
   // channel
   private static final String BDS_CONTROLL_NOTIF_CHANNEL_NAME = "SmpsControllerNotif"; // receive
   // refactor when tsm-common.properties (pts-shared:treatmentroomsession) is
   // available
   private static final String TSM_ORIGIN = "TSM";
   private final PropertyChangeSupport mSupport;
   private ServiceController mServiceController;
   private NotifChannelSender mChannelSender;
   private NotifChannelSender mTsmChannelSender;
   private NotifChannelSender mDrChannelSender;
   private NotifChannelSender mBssCmdChannelSender;
   private NotifChannelSender mBssDevicesCmdChannelSender;
   private NotifChannelSender mBdsControllCmdChannelSender;
   private NotifChannelSender mBssSchedulerCmdChannelSender;
   private NotifChannelSender mDevicesCmdChannelSender;
   private NotifChannelSender mLlrfCmdChannelSender;
   private NotifChannelSender mBlpscuCmdChannelSender;
   private NotifChannelSender mTuneAndIrradiateTR1Sender;
   private NotifChannelSender mTuneAndIrradiateTR4Sender;
   private NotifChannelSender mTcuIseuTR1Sender;
   private NotifChannelSender mTcuIseuTR4Sender;
   private NotifChannelSender mVCEU3Sender;
   private NotifChannelSender mSMEU3Sender;
   private NotifChannelSender mSSEU3Sender;
   private NotifChannelSender mTSM1Sender;
   private NotifChannelSender mTSM3Sender;
   private boolean mDisconnectRequest = false;
   private Receiver mReceiver = new Receiver();
   private EventReceiver mBlakCmdChannelForwarder = new EventForwarder();
   private EventReceiver mBssCmdChannelForwarder = new EventForwarder();
   private EventReceiver mBssDevicesCmdChannelForwarder = new EventForwarder();
   private EventReceiver mSmpsControllCmdChannelForwarder = new EventForwarder();
   private EventReceiver mBssSchedulerCmdChannelForwarder = new EventForwarder();
   private EventReceiver mDevicesCmdChannelForwarder = new EventForwarder();
   private EventReceiver mLlrfCmdChannelForwarder = new EventForwarder();
   private EventReceiver mBlpscuCmdChannelForwarder = new EventForwarder();
   private EventReceiver mTuneAndIrradiateTR1Forwarder = new EventForwarder();
   private EventReceiver mTuneAndIrradiateTR4Forwarder = new EventForwarder();
   private EventReceiver mTcuIseuTR1Forwarder = new EventForwarder();
   private EventReceiver mTcuIseuTR4Forwarder = new EventForwarder();
   private EventReceiver mVCEU3Forwarder = new EventForwarder();
   private EventReceiver mSMEU3Forwarder = new EventForwarder();
   private EventReceiver mSSEU3Forwarder = new EventForwarder();
   private EventReceiver mTSM1Forwarder = new EventForwarder();
   private EventReceiver mTSM3Forwarder = new EventForwarder();
   private Timer mBCPMonitorTimer;
   public TherapyCentre tc = new TherapyCentreImpl(
        BlakPreferences.getCurrentSiteString(BlakConstants.SITE_DESCRIPTION));
   
   private String mNotifServerAddress = "";
   private int 	  mNotifServerPort=16540 ;

   private BlakICompNoSiteClient mICompClient = new BlakICompNoSiteClient();

   public blakNotifFeedbackClient()
   {
      mSupport = new PropertyChangeSupport(this);
   }

   static
   {
      ((DefaultPropertyDefinition) DEFAULT_PROPERTY_DEFINITION).setPropertyFactory(new GenericPropertyFactory());
      ((DefaultPropertyDefinition) TSM_PROPERTY_DEFINITION).setPropertyFactory(new GenericPropertyFactory());
   }

   /**
    * Decode a SmartSocket value encoded as a string. See JavaCuDaqService's equivalent method.
    *
    * @param pEncodedValue
    * @return an array containing two values (a property name as a string, and a property value), or
    *         {@code null} if a decoding error occurs
    */
   public static Object[] decodePropertyValue(String pPropId, String pEncodedValue)
   {
      if (TSM_PROPERTY_DEFINITION.getId().equals(pPropId))
      {
         return new Object[] { BlakEcubtcuFeedbackClient.MCR_SYSMGR_SW_MCRSERVICESESSION,
               TreatmentRoomSession.Treatment.name().equals(pEncodedValue) ? 0.0 : 1.0 };
      }

      final String[] token = pEncodedValue.split("#");
      if (token.length != 3)
      {
         return null;
      }
      final Object[] value = new Object[] { token[0], null };
      if (token[1].equals(Type.BOOLEAN.name()))
      {
         value[1] = Boolean.valueOf(token[2]);
      }
      else if (token[1].equals(Type.INTEGER.name()))
      {
         // value = Integer.valueOf(token[2]);
         value[1] = Float.valueOf(token[2]); // The TcpServer sends all numeric
         // data as float.
      }
      else if (token[1].equals(Type.DOUBLE.name()))
      {
         value[1] = Float.valueOf(token[2]);
      }
      else
      {
         return null;
      }
      return value;
   }

   public static double fixScanningControllerMean(double pPosition)
   {
      return Math.abs(pPosition) > 9999.0 ? Double.NaN : pPosition < -80.0 ? pPosition + 160.0
            : pPosition > 80.0 ? pPosition - 160.0 : pPosition;
   }

   public static double fixScanningControllerRms(double pPosition)
   {
      return Math.abs(pPosition) > 9999.0 ? Double.NaN : pPosition;
   }

   public BlakICompNoSiteClient getICompClient()
   {
      return mICompClient;
   }

   public void setupICompClient()
   {
      mICompClient.setupICompClient();
      //mICompClient.updateAppQueueRouterP2pMap("DEGRADER", mBssCmdChannelForwarder);
      mICompClient.updateAppQueueRouterP2pMap("LLRF", mBssDevicesCmdChannelForwarder);
      mICompClient.updateAppQueueRouterP2pMap("bpsController", mBssCmdChannelForwarder);
      mICompClient.updateAppQueueRouterP2pMap("smpsController", mSmpsControllCmdChannelForwarder);
      mICompClient.updateAppQueueRouterP2pMap("bssController", mBssCmdChannelForwarder);
      mICompClient.updateAppQueueRouterP2pMap("beamScheduler", mBssSchedulerCmdChannelForwarder);
      mICompClient.updateAppQueueRouterP2pMap("PLC", mBlpscuCmdChannelForwarder);
      //mICompClient.updateAppQueueRouterP2pMap("BLPSCU", mBlpscuCmdChannelForwarder);
      mICompClient.updateAppQueueRouterP2pMap("BCREU", mBssDevicesCmdChannelForwarder);
      mICompClient.updateAppQueueRouterP2pMap("BcreuComponent", mBssDevicesCmdChannelForwarder);
      mICompClient.updateEventRouterNotifMap(BCP_CONTAINER_NAME, mICompClient.applicationQueue);
      mICompClient.updateAppQueueRouterNotifMap(BCP_CONTAINER_NAME, mBcpMonitor);
      mICompClient.updateAppQueueRouterP2pMap("XRay", mDevicesCmdChannelForwarder);
      mICompClient.updateAppQueueRouterP2pMap("urn:guimodel:tuneandirradiate:FBTR1", mTuneAndIrradiateTR1Forwarder);
      mICompClient.updateAppQueueRouterP2pMap("urn:guimodel:tuneandirradiate:GTR4", mTuneAndIrradiateTR4Forwarder);
      mICompClient.updateAppQueueRouterP2pMap("ISA", mTcuIseuTR1Forwarder);
      mICompClient.updateAppQueueRouterP2pMap("urn:device:vc:IBTR3", mVCEU3Forwarder);
      mICompClient.updateAppQueueRouterP2pMap("urn:device:sm:IBTR3", mSMEU3Forwarder);
      mICompClient.updateAppQueueRouterP2pMap("urn:device:ss:IBTR3", mSSEU3Forwarder);
      mICompClient.updateEventRouterNotifMap(BAPP_CONTAINER_NAME, mICompClient.applicationQueue);
      mICompClient.updateAppQueueRouterNotifMap(BAPP_CONTAINER_NAME, mBapp3Monitor);
      mICompClient.updateAppQueueRouterP2pMap("urn:device:iseuchain:GTR4", mTcuIseuTR4Forwarder);
      //mICompClient.updateAppQueueRouterP2pMap("TSM", mTSM1Forwarder);
      mICompClient.updateAppQueueRouterP2pMap("TSM", mTSM3Forwarder);
   }

   @Override
   public void receive(Event e)
   {

      // let the feedback handle daq events first
      if (e instanceof PropertiesChangedEvent || e instanceof PropertiesValueEvent)
      {
         Set<Property<?>> props = e instanceof PropertiesValueEvent ? ((PropertiesValueEvent) e).getProperties()
               : ((PropertiesChangedEvent) e).getProperties();
         for (Property<?> p : props)
         {
            handleProperty(p);
            if (e instanceof PropertiesValueEvent)
            {
               PropertiesValueEvent pve = (PropertiesValueEvent) e;
               if (pve.getOrigin().equals("Blak"))
               {
                  return;
               }
            }
         }
      }
      // then publish to eventbus
      mICompClient.eventBus.publish(e);
   }

   private void handleProperty(Property<?> pProperty)
   {
      // daq values are encoded as a STRING
      if (!Type.STRING.equals(pProperty.getType()))
      {
         return;
      }
      final Object[] values = decodePropertyValue(pProperty.getId(), pProperty.getString());

      if (values == null)
      {
         Logger.getLogger().debug(
               "Cannot decode property value: " + pProperty.getString() + ". Event is sent to eventbus. ");
      }
      else if (values[1] instanceof Number)
      {
         mReceiver.setNumericCBValue((String) values[0], ((Number) values[1]).doubleValue());
      }
      else if (values[1] instanceof Boolean)
      {
         mReceiver.setNumericCBValue((String) values[0], (Boolean) values[1] ? 1.0 : 0.0);
      }
      else
      {
         Logger.getLogger().warn(
               "Property does not contain a numeric value or a boolean value: " + pProperty.getString());
      }
   }

   @Override
   public boolean connect()
   {
      if (isConnected())
      {
         disconnect();
      }

      mDisconnectRequest = false;
      Logger.getLogger().debug("Initializing communication structure...");
      createServices();

      Logger.getLogger().debug("Connecting to Notification Server...");
      mServiceController.start();
      if (mServiceController.isStarted())
      {
 //        publishTsm();
 //        sendTreatmentRoomMode();
         ((EventForwarder) mBlakCmdChannelForwarder).setEventReceiver(mChannelSender);
         ((EventForwarder) mBssCmdChannelForwarder).setEventReceiver(mBssCmdChannelSender);
         ((EventForwarder) mBssDevicesCmdChannelForwarder).setEventReceiver(mBssDevicesCmdChannelSender);
         ((EventForwarder) mSmpsControllCmdChannelForwarder).setEventReceiver(mBdsControllCmdChannelSender);
         ((EventForwarder) mBssSchedulerCmdChannelForwarder).setEventReceiver(mBssSchedulerCmdChannelSender);
         ((EventForwarder) mDevicesCmdChannelForwarder).setEventReceiver(mDevicesCmdChannelSender);
         ((EventForwarder) mBlpscuCmdChannelForwarder).setEventReceiver(mBlpscuCmdChannelSender);
         ((EventForwarder) mTuneAndIrradiateTR1Forwarder).setEventReceiver(mTuneAndIrradiateTR1Sender);
         ((EventForwarder) mTuneAndIrradiateTR4Forwarder).setEventReceiver(mTuneAndIrradiateTR4Sender);
         ((EventForwarder) mTcuIseuTR1Forwarder).setEventReceiver(mTcuIseuTR1Sender);
         ((EventForwarder) mTcuIseuTR4Forwarder).setEventReceiver(mTcuIseuTR4Sender);
         ((EventForwarder) mVCEU3Forwarder).setEventReceiver(mVCEU3Sender);
         ((EventForwarder) mSMEU3Forwarder).setEventReceiver(mSMEU3Sender);
         ((EventForwarder) mSSEU3Forwarder).setEventReceiver(mSSEU3Sender);
         //((EventForwarder) mTSM1Forwarder).setEventReceiver(mTSM1Sender);
         ((EventForwarder) mTSM3Forwarder).setEventReceiver(mTSM3Sender);
         //((EventForwarder) mLlrfCmdChannelForwarder).setEventReceiver(mLlrfCmdChannelSender);
         //publishBssProperties();
         
//         mBCPMonitorTimer = mICompClient.timerFactory.create(mBcpMonitor);
//         mBCPMonitorTimer.schedule(BCP_MONITOR_TIMEOUT, BCP_MONITOR_TIMEOUT);
         return true;
      }
      return false;

   }

   private void publishBssProperties()
   {
      // ask for BcreComponent#Service.Start
      mICompClient.eventBus.publish(mICompClient.eventFactory.createPublishPropertiesEvent(
            Controller.beam.bcreu.getName(), "BcreuComponent"));
      // ask for all properties of BCREU and BcreuComponent#Service.Confirmed
      Controller.beam.bcreu.proxyPublish();
      // ask for properties of BpsController
      Controller.beam.bpsController.proxyPublish();
      // ask for properties of BssController
      Controller.beam.bssController.proxyPublish();
      // ask for properties of BeamScheduler
      Controller.beam.beamScheduler.proxyPublish();
      Controller.beam.smpsController.proxyPublish();
      Controller.beam.llrf.proxyPublish();
      Controller.beam.blpscuCmdChannelProxy.proxyPublish();
      Controller.beam.blpscu.proxyPublish();
      Controller.beam.ISEU1.proxyPublish();
      Controller.beam.ISEU4.proxyPublish();
      Controller.beam.BAPP1.proxyPublish();
      Controller.beam.BAPP4.proxyPublish();
      Controller.beam.VCEU3.proxyPublish();
      Controller.beam.SMEU3.proxyPublish();
      Controller.beam.SSEU3.proxyPublish();
      //Controller.beam.TSM1.proxyPublish();
      Controller.beam.TSM3.proxyPublish();
      //Controller.beam.degrader.proxyPublish();
      //Controller.beam.xrayCtrl.proxyPublish();
   }

   private void createServices()
   {
      Logger.getLogger().debug("Start to create services");
      if (mServiceController != null)
      {
         return;
      }

      final Set<Service> services = new LinkedHashSet<>();
 //     services.add(createNotifReceiver(ChannelConstants.TSM_NOTIFICATIONS_MCR));
      services.add(createNotifReceiver(ChannelConstants.BLAK_NOTIF));
      services.add(createNotifReceiver(ChannelConstants.BSS_NOTIF));
      services.add(createNotifReceiver(ChannelConstants.BSS_DEVICES_NOTIF));
      //services.add(createNotifReceiver("NotificationFromPmsMiscTR4"));
      //services.add(createNotifReceiver(ChannelConstants.DAQ_NOTIF));
      services.add(createNotifReceiver("BlpscuCommand"));
      services.add(createNotifReceiver(BDS_CONTROLL_NOTIF_CHANNEL_NAME));
//      services.add(createNotifReceiver(CONTAINER_FB_CHANNEL_NAME));
      services.add(createNotifReceiver("bapBmsRemoteUINotif_GTR4"));
      services.add(createNotifReceiver("bapBmsRemoteUINotif_FBTR1"));
      //services.add(createNotifReceiver("ISEU"));
      NotifChannelReceiver vc3 = createNotifReceiver("BdsDevicesNotif-IBTR3");
      //vc3.setFilter(BAPP_CONTAINER_NAME);
      services.add(vc3);
      //services.add(createNotifReceiver("TSMNotifications-FBTR1"));
      services.add(createNotifReceiver("TSMNotifications-IBTR3"));
      //services.add(createNotifReceiver("ScanningControllerNotif"));
      services.add(createNotifReceiver(ChannelConstants.BEAM_SCHEDULER_NOTIF));
 //     if (Blak.room.getBdp().length() > 0)
  //    {
  //       services.add(createDataRecorderNotifReceiver());
   //      mDrChannelSender = createNotifSender(DR_CMD_CHANNEL);
//         services.add(mDrChannelSender);
 //     }
//      mTsmChannelSender = createNotifSender(ChannelConstants.TSM_COMMANDS_MCR);
//      services.add(mTsmChannelSender);
      mChannelSender = createNotifSender(ChannelConstants.BLAK_COMMAND);
      services.add(mChannelSender);
      mBssCmdChannelSender = createNotifSender(ChannelConstants.BSS_COMMAND);
      services.add(mBssCmdChannelSender);
      mBssDevicesCmdChannelSender = createNotifSender(ChannelConstants.BSS_DEVICES_COMMAND);
      services.add(mBssDevicesCmdChannelSender);
      mBdsControllCmdChannelSender = createNotifSender(BDS_CONTROLL_COMMAND_CHANNEL_NAME);
      services.add(mBdsControllCmdChannelSender);
      mBlpscuCmdChannelSender = createNotifSender("BlpscuCommand");
      services.add(mBlpscuCmdChannelSender);
      mTuneAndIrradiateTR1Sender = createNotifSender("bapBmsRemoteUICommand_FBTR1");
      mTuneAndIrradiateTR4Sender = createNotifSender("bapBmsRemoteUICommand_GTR4");
      services.add(mTuneAndIrradiateTR1Sender);
      services.add(mTuneAndIrradiateTR4Sender);
      mTcuIseuTR1Sender = createNotifSender("McrTcrBdsDevicesCommand-IBTR3");
      mTcuIseuTR4Sender = createNotifSender("McrTcrBdsDevicesCommand-GTR4");
      services.add(mTcuIseuTR1Sender);
      services.add(mTcuIseuTR4Sender);
      mVCEU3Sender = createNotifSender("BdsDevicesCommand-IBTR3");
      services.add(mVCEU3Sender);
      mSMEU3Sender = mVCEU3Sender;
      mSSEU3Sender = mVCEU3Sender;
      //mSMEU3Sender = createNotifSender("BdsDevicesCommand-IBTR3");
      //services.add(mSMEU3Sender);
      //mSSEU3Sender = createNotifSender("BdsDevicesCommand-IBTR3");
      //services.add(mSSEU3Sender);
//      mTSM1Sender = createNotifSender("TSMCommands-FBTR1");
//      services.add(mTSM1Sender);
      mTSM3Sender = createNotifSender("TSMCommands-IBTR3");
      services.add(mTSM3Sender);
      mBssSchedulerCmdChannelSender = createNotifSender(ChannelConstants.BEAM_SCHEDULER_COMMAND);
      services.add(mBssSchedulerCmdChannelSender);
     // mDevicesCmdChannelSender = createNotifSender("CommandPmsMiscTR4");
     // services.add(mDevicesCmdChannelSender);
     // mLlrfCmdChannelSender = createNotifSender(ChannelConstants.BSS_DEVICES_COMMAND);
     // services.add(mLlrfCmdChannelSender);

      final AllOrNothingServiceController sc = new AllOrNothingServiceController();
      sc.setServices(services);
      sc.addServiceListener(this);
      mServiceController = sc;
   }

   public void serviceStarted(Service pService)
   {
      // don't care
   }

   public void serviceStopped(Service pService)
   {
      Logger.getLogger().info("Service " + pService.getName() + " stopped.");
      if (!mDisconnectRequest)
      {
         mSupport.firePropertyChange(CONNECTION, true, false);// unexpectedly
         // service stop.
      }
   }

   private NotifChannelReceiver createDataRecorderNotifReceiver()
   {
      // create dataRecorder notification receiver
      final XmlEventConverter drConv = new XmlEventConverter();
      drConv.setPropertyDefinitionDictionary(mICompClient.propertyDefinitionDictionary);

      EventReceiver blakIcAdapter;
      try
      {
         if (Blak.room.getTreatmentMode().toUpperCase().equals("PBS"))
         {
            Logger.getLogger().debug("PBS mode");
            blakIcAdapter = new PbsBlackIcDataEventAdapter(mICompClient.propertyDefinitionDictionary);
         }
         else
         {
            Logger.getLogger().debug("DSUS mode");
            // if it is dsus, ic data is not routed through data recorder, use basic handling
            blakIcAdapter = new BlackIcDataEventAdapter(blakNotifFeedbackClient.this);
         }
      }
      catch (BeamLineElementNotFoundException e)
      {
         // Fall back to classic handling
         blakIcAdapter = new BlackIcDataEventAdapter(blakNotifFeedbackClient.this);
      }

      final String channel = DR_FB_CHANNEL_PREFIX + Blak.room.getBdp();
      final NotifChannelReceiver receiver = createNotifReceiver(channel);
      receiver.setEventConverter(drConv);
      receiver.setEventBus(new EventBusToEventReceiver(blakIcAdapter));
      return receiver;
   }

   private NotifChannelReceiver createNotifReceiver(String pChannel)
   {
      Logger.getLogger().debug("create notif receiver for channel: " + pChannel);
      final XmlEventConverter conv = new XmlEventConverter();
      conv.setPropertyDefinitionDictionary(mICompClient.propertyDefinitionDictionary);;

      NotifChannelReceiver receiver = new NotifChannelReceiver();
      receiver.setName(pChannel + "Receiver");
      receiver.setChannel(pChannel);
      receiver.setChannelStream(createChannelStream());
      receiver.setEventConverter(conv);
      receiver.setEventBus(new EventBusToEventReceiver(blakNotifFeedbackClient.this));
      receiver.setContainer(BlakICompNoSiteClient.CONTAINER_NAME); // not used
      receiver.setFilter(BlakICompNoSiteClient.CONTAINER_NAME); // not used

      return receiver;
   }

   private NotifChannelSender createNotifSender(String pChannel)
   {
      Logger.getLogger().debug("create notif sender for channel: " + pChannel);
      final XmlEventConverter conv = new XmlEventConverter();
      conv.setPropertyDefinitionDictionary(mICompClient.propertyDefinitionDictionary);

      final NotifChannelSender channelSender = new NotifChannelSender();
      channelSender.setName(pChannel + "Sender");
      channelSender.setChannel(pChannel);
      channelSender.setQueue(new LinkedBlockingQueue<Event>());
      channelSender.setChannelStream(createChannelStream());
      channelSender.setEventConverter(conv);
      channelSender.setContainer(BlakICompNoSiteClient.CONTAINER_NAME);
      channelSender.setFilter(BlakICompNoSiteClient.CONTAINER_NAME); // not used

      return channelSender;
   }

   private void publishTsm()
   {
      mTsmChannelSender.receive(mICompClient.eventFactory.createPublishPropertiesEvent(EVENT_ORIGIN,
            TSM_ORIGIN));
   }

   private void sendTreatmentRoomMode()
   {
      if (mDrChannelSender != null)
      {
         mDrChannelSender.receive(mICompClient.eventFactory.createRequestEvent(EVENT_ORIGIN,
               "treatmentModeTracker", "updateTreatmentMode", "TR" + Blak.room.getRoomId(),
               Blak.room.getTreatmentMode()));
      }
   }
   
   public void setNotifServerAddress(String strAddress)
   {
	   mNotifServerAddress =  strAddress ;
   }
   
   public String getNotifServerAddress()
   {
	   return mNotifServerAddress ;
   }
   
   
   public void setNotifServerPort(int nPort)
   {
	   mNotifServerPort =  nPort ;
   }
   
   public int getNotifServerPort()
   {
	   return mNotifServerPort ;
   }

   private ChannelStream createChannelStream()
   {
//      String address = BlakPreferences.getCurrentSiteString(BlakConstants.NETWORK_NOTIF_SERVER_ADDRESS);
	  
//      int port = DEFAULT_PORT;
	  int port = mNotifServerPort ;
      int colonIndex = mNotifServerAddress.indexOf(':');
      if (colonIndex >= 0)
      {
         try
         {
            port = Integer.parseInt(mNotifServerAddress.substring(colonIndex + 1));
         }
         catch (NumberFormatException e)
         {
            Logger.getLogger().warn("Invalid port number in Notification server address: " + mNotifServerAddress);
         }
                  mNotifServerAddress = mNotifServerAddress.substring(0, colonIndex);
      }
      Logger.getLogger().info("notif-server address " + mNotifServerAddress + " and notif-server port " + port);
      return new SocketChannelStream(mNotifServerAddress, port);
   }

   @Override
   public void disconnect()
   {
      mDisconnectRequest = true;
      if (mServiceController != null)
      {
         mServiceController.stop();
         mServiceController.removeServiceListener(this);
      }
      mServiceController = null;
      mChannelSender = null;
      mTsmChannelSender = null;
      mBCPMonitorTimer.cancel();
   }

   @Override
   public boolean isConnected()
   {
      return mServiceController != null && mServiceController.isStarted();
   }

   @Override
   public void retreiveMcrFeedbacks()
   {
      retreiveAllFeedback();
   }

   @Override
   public void retreiveTcrFeedbacks(int pRoomId)
   {
      retreiveAllFeedback();
   }

   private void retreiveAllFeedback()
   {
      if (!isConnected())
      {
         Logger.getLogger().warn("Notification server is not connected, please connect first!");
         return;
      }
      publishBssProperties();
      mChannelSender.receive(mICompClient.eventFactory.createPublishPropertiesEvent(EVENT_ORIGIN,
            "daqPublisher"));
   }

   @Override
   public void sendRtBooleanVariable(String pSubject, String pVariable, boolean pValue)
   {
      Logger.getLogger().warn("Cannot set RT variable (using IComp Notification Server)");
   }

   @Override
   public void sendRtNumericVariable(String pSubject, String pVariable, float pValue)
   {
      Logger.getLogger().warn("Cannot set RT variable (using IComp Notification Server)");
   }

   @Override
   public void addPropertyChangeListener(PropertyChangeListener pListener)
   {
      mSupport.addPropertyChangeListener(pListener);

   }

   @Override
   public void removePropertyChangeListener(PropertyChangeListener pListener)
   {
      mSupport.removePropertyChangeListener(pListener);
   }

   public void updateBlakCmdChannel(boolean pMcrInTreatmentMode)
   {
      if (pMcrInTreatmentMode)
      {
         Logger.getLogger().info("MCR in treatment mode, Blak cmds are not routed to BCP");
         ((EventForwarder) mBlakCmdChannelForwarder).setEventReceiver(mICompClient.nullEventReceiver);
         ((EventForwarder) mBssCmdChannelForwarder).setEventReceiver(mICompClient.nullEventReceiver);
         ((EventForwarder) mBssDevicesCmdChannelForwarder).setEventReceiver(mICompClient.nullEventReceiver);
         ((EventForwarder) mBssSchedulerCmdChannelForwarder).setEventReceiver(mICompClient.nullEventReceiver);
         //((EventForwarder) mLlrfCmdChannelForwarder).setEventReceiver(mICompClient.nullEventReceiver);
      }
      else if (mChannelSender.isStarted())
      {
         ((EventForwarder) mBlakCmdChannelForwarder).setEventReceiver(mChannelSender);
         ((EventForwarder) mBssCmdChannelForwarder).setEventReceiver(mBssCmdChannelSender);
         ((EventForwarder) mBssDevicesCmdChannelForwarder).setEventReceiver(mBssDevicesCmdChannelSender);
         ((EventForwarder) mBssSchedulerCmdChannelForwarder).setEventReceiver(mBssSchedulerCmdChannelSender);
         ((EventForwarder) mBlpscuCmdChannelForwarder).setEventReceiver(mBlpscuCmdChannelSender);
         //((EventForwarder) mLlrfCmdChannelForwarder).setEventReceiver(mLlrfCmdChannelSender);
      }
   }

   private static class EventForwarder implements EventReceiver
   {

      EventReceiver mReceiver;

      @Override
      public void receive(Event pEvent)
      {
         if (mReceiver != null)
         {
            mReceiver.receive(pEvent);
         }
      }

      public void setEventReceiver(EventReceiver receiver)
      {
         mReceiver = receiver;
      }
   }

   /**
    * PBS IC adapter.
    */
   private static class IonizationChamberAdapter extends AbstractBean
   {
      private final IonizationChamberBackend mIcBE1;
      private final IonizationChamberBackend mIcBE23X;
      private final IonizationChamberBackend mIcBE23Y;

      /**
       * Constructor.
       *
       * @param pIcBE1
       * @param pIcBE23X
       * @param pIcBE23Y
       * @param pProxy
       */
      public IonizationChamberAdapter(IonizationChamberBackend pIcBE1, IonizationChamberBackend pIcBE23X,
            IonizationChamberBackend pIcBE23Y, IonizationChamber pProxy)
      {
         mIcBE1 = pIcBE1;
         mIcBE23X = pIcBE23X;
         mIcBE23Y = pIcBE23Y;
         pProxy.addPropertyChangeListener(this);
      }

      @ProxyPropertyChange
      public void onX1QualityChannels(int pIndex, double pValue)
      {
         if (pIndex < 12)
         {
            mIcBE1.updateQualityChannel(pIndex, pValue);
         }
      }

      @ProxyPropertyChange
      public void onY1QualityChannels(int pIndex, double pValue)
      {
         if (pIndex < 12)
         {
            mIcBE1.updateQualityChannel(pIndex + 12, pValue);
         }
      }

      @ProxyPropertyChange
      public void onX1GaussPosition(double pValue)
      {
         mIcBE1.updateXMean(fixScanningControllerMean(pValue));
      }

      @ProxyPropertyChange
      public void onX1GaussWidth(double pValue)
      {
         mIcBE1.updateXRms(fixScanningControllerRms(pValue));
      }

      @ProxyPropertyChange
      public void onY1GaussPosition(double pValue)
      {
         mIcBE1.updateYMean(fixScanningControllerMean(pValue));
      }

      @ProxyPropertyChange
      public void onY1GaussWidth(double pValue)
      {
         mIcBE1.updateYRms(fixScanningControllerRms(pValue));
      }

      @ProxyPropertyChange
      public void onX2QualityChannels(int pIndex, double pValue)
      {
         mIcBE23X.updateQualityChannel(pIndex, pValue);
      }

      @ProxyPropertyChange
      public void onY2QualityChannels(int pIndex, double pValue)
      {
         mIcBE23Y.updateQualityChannel(pIndex, pValue);
      }

      @ProxyPropertyChange
      public void onX2GaussPosition(double pValue)
      {
         mIcBE23X.updateXMean(fixScanningControllerMean(pValue));
      }

      @ProxyPropertyChange
      public void onY2GaussPosition(double pValue)
      {
         mIcBE23Y.updateYMean(fixScanningControllerMean(pValue));
      }

      @ProxyPropertyChange
      public void onX2GaussWidth(double pValue)
      {
         mIcBE23X.updateXRms(fixScanningControllerRms(pValue));
      }

      @ProxyPropertyChange
      public void onY2GaussWidth(double pValue)
      {
         mIcBE23Y.updateYRms(fixScanningControllerRms(pValue));
      }

      public IonizationChamberBackend getIcBE1()
      {
         return mIcBE1;
      }

      public IonizationChamberBackend getIcBE23X()
      {
         return mIcBE23X;
      }

      public IonizationChamberBackend getIcBE23Y()
      {
         return mIcBE23Y;
      }
   }

   /**
    * This Adapter receives ICx data from datarecorder then encode events into legacy variable format.
    */
   private class BlackIcDataEventAdapter implements EventReceiver
   {
      EventReceiver mReceiver;

      public BlackIcDataEventAdapter(EventReceiver pReceiver)
      {
         mReceiver = pReceiver;
      }

      @Override
      public void receive(Event pEvent)
      {
         String origin = pEvent.getOrigin();
         if (pEvent instanceof PropertiesValueEvent)
         {
            PropertiesValueEvent pv = (PropertiesValueEvent) pEvent;
            Set<Property<?>> props = pv.getProperties();
            for (Property<?> p : props)
            {
               PropertyDefinition def = p.getPropertyDefinition();

               int roomNb = 0;
               String[] origins = origin.split(":");
               String bdpOrBsp = origins[origins.length - 1];
               if (bdpOrBsp.equals(Blak.room.getBdp() + ".X") || bdpOrBsp.equals(Blak.room.getBdp() + ".Y")
                     || bdpOrBsp.equals(Blak.room.getBsp()))
               {
                  roomNb = Blak.room.getRoomId();
               }
               else
               {
                  Logger.getLogger().warn(
                        "Beam delivery(supply) point id of IC device  is not configured as treatment room, "
                              + "ICx data will be ignored!");
                  return;
               }

               String cu = String.format("tr%d_tcu", roomNb);
               String iceu = "iceu2";// default as y
               Set<Property<?>> properties = new TreeSet<Property<?>>();

               if (def.getId().contains("IonizationChamber23"))
               {
                  if (origin.charAt(origin.length() - 1) == 'X')
                  {
                     iceu = "iceu3";// x
                  }
                  else if (origin.charAt(origin.length() - 1) == 'Y')
                  {
                     iceu = "iceu2";// y
                  }
               }
               else if (def.getId().contains("IonizationChamber1"))
               {
                  iceu = "iceu1";
               }

               String newProperty = "";
               if (def.getName().equals("qualityChannels"))
               {
                  newProperty = String.format("qc%02d", p.getIndex() + 1);
               }
               else if (def.getName().equals("XQualityChannels"))// IC1
               {
                  newProperty = String.format("qc%02d", p.getIndex() + 1);
               }
               else if (def.getName().equals("YQualityChannels"))// IC1
               {
                  newProperty = String.format("qc%02d", p.getIndex() + 13);
               }

               else if (def.getName().equals("gaussPosition"))
               {
                  newProperty = "mean";
               }
               else if (def.getName().equals("XGaussPosition"))
               {
                  newProperty = "xmean";
               }
               else if (def.getName().equals("YGaussPosition"))
               {
                  newProperty = "ymean";
               }

               else if (def.getName().equals("gaussWidth"))
               {
                  newProperty = "rms";
               }
               else if (def.getName().equals("XGaussWidth"))
               {
                  newProperty = "xrms";
               }
               else if (def.getName().equals("YGaussWidth"))
               {
                  newProperty = "yrms";
               }

               if (newProperty.length() > 0)
               {
                  String var = cu + "." + iceu + "." + newProperty + "#";
                  var += "DOUBLE#";
                  var += p.getValue();
                  properties.add(DEFAULT_PROPERTY_DEFINITION.createProperty(var));
                  DefaultPropertiesValueEvent newE = new DefaultPropertiesValueEvent("Blak", properties);
                  mReceiver.receive(newE);
               }
            }
         }
      }
   }

   /**
    * This Adapter receives ICx data from datarecorder then update the blak ic driver.
    */
   private class PbsBlackIcDataEventAdapter implements EventReceiver
   {
      private final PbsIonizationChamberProxy mProxy;
      private final IonizationChamberAdapter mAdapter;

      public PbsBlackIcDataEventAdapter(ListablePropertyDefinitionDictionary pDictionary)
            throws BeamLineElementNotFoundException
      {
         tc = new TherapyCentreImpl(
               BlakPreferences.getCurrentSiteString(BlakConstants.SITE_DESCRIPTION));
         Map<BeamDeliveryPoint, NozzleType> nt = NozzleTypesBuilder.getNozzleTypes(tc,
               BlakPreferences.getCurrentSiteString(BlakConstants.NOZZLE_TYPES));
         NozzleType nozzleType = nt.get(tc.getBeamDeliveryPoint(Blak.room.getBdp()));
         IcStripConfig strips = new IcStripConfig(nozzleType);

         Logger.getLogger().debug("Nozzle type: %s", nozzleType.getShortName());
         IonizationChamberBackend icBE1 = (IonizationChamberBackend) Blak.beamLine.getElement("ICEU1R"
               + Blak.room.getRoomId());
         IonizationChamberBackend icBE23X = (IonizationChamberBackend) Blak.beamLine.getElement("ICEU2R"
               + Blak.room.getRoomId());
         IonizationChamberBackend icBE23Y = (IonizationChamberBackend) Blak.beamLine.getElement("ICEU3R"
               + Blak.room.getRoomId());
         if (nozzleType.equals(NozzleType.UNIVERSAL_NOZZLE)) // swap IC23 X/Y
         {
            Logger.getLogger().debug("Universal Nozzle -> swap IC23 X/Y");
            IonizationChamberBackend temp = icBE23Y;
            icBE23Y = icBE23X;
            icBE23X = temp;
         }

         mProxy = new PbsIonizationChamberProxy(strips);
         mProxy.setPropertyDefinitionDictionary(pDictionary);
         mProxy.setComponentName(Blak.room.getBdp());
         mProxy.setName(Blak.room.getBdp() + "Proxy");
         mAdapter = new IonizationChamberAdapter(icBE1, icBE23X, icBE23Y, mProxy);
      }

      @Override
      public void receive(Event pEvent)
      {
         final String origin = pEvent.getOrigin();
         if (pEvent instanceof PropertiesValueEvent || pEvent instanceof PropertiesChangedEvent)
         {

            Logger.getLogger().debug("PbsBlackIcDataEventAdapter receive event: " + pEvent.toString());
            final String[] origins = origin.split(":");
            final String bdpOrBsp = origins[origins.length - 1];

            if (bdpOrBsp.equals(Blak.room.getBdp()))
            {
               mAdapter.getIcBE1().setInhibitReception(true);
               mAdapter.getIcBE23X().setInhibitReception(true);
               mAdapter.getIcBE23Y().setInhibitReception(true);
               mProxy.receive(pEvent);
               mAdapter.getIcBE1().setInhibitReception(false);
               mAdapter.getIcBE23X().setInhibitReception(false);
               mAdapter.getIcBE23Y().setInhibitReception(false);
            }
         }
      }
   }

   private class ContainerMonitor implements EventReceiver, Scheduled
   {
      // //////////////////////////////////////////
      private String mContainerToMonitor;
      private boolean mNeedsAliveTime;
      private boolean mAlive = true;
      private Date mAliveTime = new Date(0);

      public ContainerMonitor(String pContainer)
      {
         mContainerToMonitor = pContainer;
      }

      @Override
      public void receive(Event pEvent)
      {
         if (pEvent.getOrigin().equals(mContainerToMonitor) && pEvent instanceof PropertiesChangedEvent)
         {
            PropertiesChangedEvent pv = (PropertiesChangedEvent) pEvent;

            Set<Property<?>> props = pv.getProperties();
            for (Property<?> p : props)
            {
               PropertyDefinition def = p.getPropertyDefinition();
               if (def.getId().equals("Container.AliveTime"))
               {
                  setAliveTime(p.getDate());
               }
            }
         }
      }

      public void setAliveTime(Date pAliveTime)
      {
         mNeedsAliveTime = false;
         mSupport.firePropertyChange("AliveTime", mAliveTime, mAliveTime = pAliveTime);
         setAlive(true);
      }

      /**
       * Sets whether this container is alive.
       *
       * @param pAlive <code>true</code> if this container is alive, <code>false</code> otherwise.
       */
      protected void setAlive(boolean pAlive)
      {
         if (pAlive != mAlive)
         {
            mSupport.firePropertyChange(CONNECTION, mAlive, mAlive = pAlive);

            if (!mAlive)
            {
               disconnect();
            }
         }
      }

      @Override
      public void onTimer(Timer pTimer)
      {
         if (mAlive && mNeedsAliveTime)
         {
            setAlive(false);
         }

         mNeedsAliveTime = true;
      }

   }
}
