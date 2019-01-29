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
import com.iba.blak.BlakConstants;
import com.iba.blak.BlakPreferences;
import com.iba.ialign.Controller;
import com.iba.icomp.core.event.DefaultEventFactory;
import com.iba.icomp.core.event.EventBus;
import com.iba.icomp.core.event.EventLogger;
import com.iba.icomp.core.event.EventReceiver;
import com.iba.icomp.core.event.EventRouter;
import com.iba.icomp.core.event.ForwardingActiveEventQueue;
import com.iba.icomp.core.event.RoutingEventBus;
import com.iba.icomp.core.event.SequenceNumberGenerator;
import com.iba.icomp.core.property.GenericPropertyFactory;
import com.iba.icomp.core.property.ListablePropertyDefinitionDictionary;
import com.iba.icomp.core.property.XmlPropertyDefinitionDictionary;
import com.iba.icomp.core.timer.AsynchronousJavaTimerFactory;
import com.iba.icomp.core.timer.TimerFactory;
import com.iba.icomp.core.util.Logger;
import com.iba.pts.bms.common.URNEventRouter;
import com.iba.pts.bms.datatypes.api.TherapyCentre;
import com.iba.pts.bms.datatypes.impl.TherapyCentreImpl;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

/**
 * @author gyzhang
 */
public class BlakICompNoSiteClient
{
   // use a different container name for each instance of BLAK
   public static final String CONTAINER_NAME = "blak-" + System.currentTimeMillis();

   public final DefaultEventFactory eventFactory = new DefaultEventFactory(
         new SequenceNumberGenerator(CONTAINER_NAME));
   public final ListablePropertyDefinitionDictionary propertyDefinitionDictionary = createDictionary();
   public final ListablePropertyDefinitionDictionary propertyDefinitionDictionaryDegrader = createDictionary();
   public final EventBus eventBus = new RoutingEventBus();
   public final EventRouter eventRouter = new URNEventRouter();
   public final EventRouter appQueueRouter = new URNEventRouter();
   public final EventReceiver nullEventReceiver = new EventLogger();
   public final ForwardingActiveEventQueue applicationQueue = new ForwardingActiveEventQueue();
   public final TimerFactory timerFactory = new AsynchronousJavaTimerFactory();
   public TherapyCentre therapyCenter = null;

   protected Map<String, List<EventReceiver>> mP2pMap;
   protected Map<String, List<EventReceiver>> mNotifMap;
   protected Map<String, List<EventReceiver>> mAppQueueP2pMap;
   protected Map<String, List<EventReceiver>> mAppQueueNotifMap;

   public Map<String, List<EventReceiver>> getEventRouterP2pMap()
   {
      return mP2pMap;
   }

   public Map<String, List<EventReceiver>> getEventRouterNotifMap()
   {
      return mNotifMap;
   }

   public Map<String, List<EventReceiver>> getAppQueueRouterP2pMap()
   {
      return mAppQueueP2pMap;
   }

   public Map<String, List<EventReceiver>> getAppQueueRouterNotifMap()
   {
      return mAppQueueNotifMap;
   }

   public void updateEventRouterP2pMap(Map<String, List<EventReceiver>> map)
   {
      mP2pMap = map;
      ((URNEventRouter) eventRouter).setPointToPointRouteMap(map);
   }

   public void updateEventRouterNotifMap(Map<String, List<EventReceiver>> map)
   {
      mNotifMap = map;
      ((URNEventRouter) eventRouter).setNotificationRouteMap(map);
   }

   public void updateAppQueueRouterP2pMap(Map<String, List<EventReceiver>> map)
   {
      mAppQueueP2pMap = map;
      ((URNEventRouter) appQueueRouter).setPointToPointRouteMap(map);
   }

   public void updateAppQueueRouterNotifMap(Map<String, List<EventReceiver>> map)
   {
      mAppQueueNotifMap = map;
      ((URNEventRouter) appQueueRouter).setNotificationRouteMap(map);
   }

   public void updateEventRouterP2pMap(String target, EventReceiver r)
   {
      if (mP2pMap.containsKey(target) && mP2pMap.get(target) != null)
      {
         mP2pMap.get(target).add(r);
      }
      else
      {
         mP2pMap.put(target, new LinkedList<EventReceiver>(Arrays.<EventReceiver>asList(r)));
      }
      ((URNEventRouter) eventRouter).setPointToPointRouteMap(mP2pMap);
   }

   public void updateEventRouterNotifMap(String origin, EventReceiver r)
   {
      if (mNotifMap.containsKey(origin) && mNotifMap.get(origin) != null)
      {
         mNotifMap.get(origin).add(r);
      }
      else
      {
         mNotifMap.put(origin, new LinkedList<EventReceiver>(Arrays.<EventReceiver>asList(r)));
      }
      ((URNEventRouter) eventRouter).setNotificationRouteMap(mNotifMap);
   }

   public void updateAppQueueRouterP2pMap(String target, EventReceiver r)
   {
      if (mAppQueueP2pMap.containsKey(target) && mAppQueueP2pMap.get(target) != null)
      {
         mAppQueueP2pMap.get(target).add(r);
      }
      else
      {
         mAppQueueP2pMap.put(target, new LinkedList<EventReceiver>(Arrays.<EventReceiver>asList(r)));
      }
      ((URNEventRouter) appQueueRouter).setPointToPointRouteMap(mAppQueueP2pMap);
   }

   public void updateAppQueueRouterNotifMap(String origin, EventReceiver r)
   {
      if (mAppQueueNotifMap.containsKey(origin) && mAppQueueNotifMap.get(origin) != null)
      {
         mAppQueueNotifMap.get(origin).add(r);
      }
      else
      {
         mAppQueueNotifMap.put(origin, new LinkedList<EventReceiver>(Arrays.<EventReceiver>asList(r)));
      }
      ((URNEventRouter) appQueueRouter).setNotificationRouteMap(mAppQueueNotifMap);
   }

   public void setupICompClient()
   {

//      try
//      {
//         therapyCenter = new TherapyCentreImpl(
//               BlakPreferences.getCurrentSiteString(BlakConstants.SITE_DESCRIPTION));
//      }
//      catch (IllegalArgumentException e)
//      {
//         Logger.getLogger().error(String.format("Site description is not correct: %s", e.getMessage()));
//         return;
//      }

      // setting eventBus
      ((RoutingEventBus) eventBus).setEventRouter(eventRouter);

      // setting appQueue
      applicationQueue.setName("Application Queue");
      applicationQueue.setEventReceiver((URNEventRouter) appQueueRouter);

      setupRoutingMap();

      ((URNEventRouter) eventRouter).setPointToPointRouteMap(mP2pMap);
      ((URNEventRouter) eventRouter).setNotificationRouteMap(mNotifMap);
      ((URNEventRouter) eventRouter).setFallbackEventReceiver(nullEventReceiver);

//      ((URNEventRouter) eventRouter).setTherapyCentre(therapyCenter);

      ((URNEventRouter) appQueueRouter).setPointToPointRouteMap(mAppQueueP2pMap);
      ((URNEventRouter) appQueueRouter).setNotificationRouteMap(mAppQueueNotifMap);
      ((URNEventRouter) appQueueRouter).setFallbackEventReceiver(nullEventReceiver);

      // set time factory
      ((AsynchronousJavaTimerFactory) timerFactory).setName("Timer Factory");
      ((AsynchronousJavaTimerFactory) timerFactory).setActiveEventQueue(applicationQueue);

      applicationQueue.start();
      ((AsynchronousJavaTimerFactory) timerFactory).start();

      // setup bcreu and bpscontrollerproxy only if we connected to NotifServer
      // meaning we are using BMS8.0+
      Controller.beam.setupBpsController();
      Controller.beam.setupLlrf();
      //Controller.beam.setupDegrader();
      Controller.beam.setBeamControlMode(1);
      Controller.beam.setupBcreu();
      Controller.beam.setupBeamScheduler();
      Controller.beam.setupBssController();
 //     Blak.beam.setupBdsController();
   }

   private void setupRoutingMap()
   {
      mP2pMap = new HashMap<String, List<EventReceiver>>();
      mNotifMap = new HashMap<String, List<EventReceiver>>();
      mAppQueueP2pMap = new HashMap<String, List<EventReceiver>>();
      mAppQueueNotifMap = new HashMap<String, List<EventReceiver>>();


//      mP2pMap.put("DEGRADER",
 //             new LinkedList<EventReceiver>(Arrays.<EventReceiver>asList(applicationQueue)));
      mP2pMap.put("LLRF",
              new LinkedList<EventReceiver>(Arrays.<EventReceiver>asList(applicationQueue)));
      mP2pMap.put("bpsController",
           new LinkedList<EventReceiver>(Arrays.<EventReceiver>asList(applicationQueue)));
//      mP2pMap.put("smpsController",
//            new LinkedList<EventReceiver>(Arrays.<EventReceiver>asList(applicationQueue)));
      mP2pMap.put("bssController",
            new LinkedList<EventReceiver>(Arrays.<EventReceiver>asList(applicationQueue)));
      mP2pMap.put("beamScheduler",
            new LinkedList<EventReceiver>(Arrays.<EventReceiver>asList(applicationQueue)));
      mP2pMap.put("BCREU", new LinkedList<EventReceiver>(Arrays.<EventReceiver>asList(applicationQueue)));
      mP2pMap.put("BcreuComponent",
            new LinkedList<EventReceiver>(Arrays.<EventReceiver>asList(applicationQueue)));



//      mNotifMap.put("DEGRADER",
 //            new LinkedList<EventReceiver>(Arrays.<EventReceiver>asList(applicationQueue)));
      mNotifMap.put("LLRF",
              new LinkedList<EventReceiver>(Arrays.<EventReceiver>asList(applicationQueue)));
      mNotifMap.put("bpsController",
            new LinkedList<EventReceiver>(Arrays.<EventReceiver>asList(applicationQueue)));
//      mNotifMap.put("smpsController",
//            new LinkedList<EventReceiver>(Arrays.<EventReceiver>asList(applicationQueue)));
      mNotifMap.put("bssController",
            new LinkedList<EventReceiver>(Arrays.<EventReceiver>asList(applicationQueue)));
      mNotifMap.put("beamScheduler",
            new LinkedList<EventReceiver>(Arrays.<EventReceiver>asList(applicationQueue)));
      mNotifMap.put("BCREU", new LinkedList<EventReceiver>(Arrays.<EventReceiver>asList(applicationQueue)));
      mNotifMap.put("BcreuComponent",
            new LinkedList<EventReceiver>(Arrays.<EventReceiver>asList(applicationQueue)));



//      mAppQueueNotifMap.put("DEGRADER",
  //            new LinkedList<EventReceiver>(Arrays.<EventReceiver>asList(Controller.beam.degrader)));
      mAppQueueNotifMap.put("LLRF",
              new LinkedList<EventReceiver>(Arrays.<EventReceiver>asList(Controller.beam.llrf)));
      mAppQueueNotifMap.put("bpsController",
            new LinkedList<EventReceiver>(Arrays.<EventReceiver>asList(Controller.beam.bpsController)));
//      mAppQueueNotifMap.put("smpsController",
//            new LinkedList<EventReceiver>(Arrays.<EventReceiver>asList(Blak.beam.smpsController)));
      mAppQueueNotifMap.put("bssController",
            new LinkedList<EventReceiver>(Arrays.<EventReceiver>asList(Controller.beam.bssController)));
      mAppQueueNotifMap.put("beamScheduler",
            new LinkedList<EventReceiver>(Arrays.<EventReceiver>asList(Controller.beam.beamScheduler)));
      mAppQueueNotifMap.put("BCREU",
            new LinkedList<EventReceiver>(Arrays.<EventReceiver>asList(Controller.beam.bcreu)));
      mAppQueueNotifMap.put("BcreuComponent",
            new LinkedList<EventReceiver>(Arrays.<EventReceiver>asList(Controller.beam.bcreu)));
   }



   private ListablePropertyDefinitionDictionary createDictionary()
   {
      XmlPropertyDefinitionDictionary dict = new XmlPropertyDefinitionDictionary();
      List<Resource> propertyList = new LinkedList<Resource>();
      propertyList.add(new ClassPathResource("config/properties/default-properties.xml"));
      propertyList.add(new ClassPathResource("config/bms/bds/devices/impl/properties/ic-properties.xml"));
      propertyList.add(new ClassPathResource("config/bms/bds/devices/impl/properties/ic-pbs-properties.xml"));
      propertyList.add(
            new ClassPathResource("config/bms/bds/devices/impl/properties/ic-dsus-properties.xml"));
      propertyList.add(
            new ClassPathResource("config/bms/bss/devices/api/properties/degrader-properties.xml"));
      propertyList.add(new ClassPathResource("config/bms/bss/devices/api/properties/slits-properties.xml"));
      propertyList.add(new ClassPathResource("config/bms/bss/devices/api/properties/group3-properties.xml"));
      propertyList.add(new ClassPathResource("config/properties/dtm151-properties.xml"));
      propertyList.add(new ClassPathResource("config/properties/datarecorder-properties.xml"));
      propertyList.add(new ClassPathResource("config/properties/container-properties.xml"));
      propertyList.add(new ClassPathResource("config/bms/bss/devices/api/properties/bcreu-properties.xml"));
      propertyList.add(new ClassPathResource("config/properties/device-properties.xml"));
      propertyList.add(new ClassPathResource("config/properties/service-properties.xml"));
      propertyList.add(new ClassPathResource("config/properties/beam-scheduler-properties.xml"));
      propertyList.add(new ClassPathResource("config/properties/bss-controller-properties.xml"));
      propertyList.add(new ClassPathResource("config/bms/bss/devices/api/properties/llrf-properties.xml"));
      propertyList.add(new ClassPathResource("config/bms/bss/devices/api/properties/degraderbeamstop-properties.xml"));
      //propertyList.add(new ClassPathResource("config/bms/bss/devices/api/properties/cuDaq-properties.xml"));
      dict.setPropertyFactory(new GenericPropertyFactory());
      dict.setXmlResources(propertyList);
      try
      {
         dict.load();
      }
      catch (IOException e)
      {
         Logger.getLogger().error(
               "Failed to load properties files, check is all of them are in the classpath");
      }
      return dict;
   }
}
