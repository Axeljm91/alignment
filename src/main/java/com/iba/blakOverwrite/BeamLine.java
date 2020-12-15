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
import com.csvreader.CsvWriter;
import com.iba.blak.Blak;
import com.iba.blak.BlakConstants;
import com.iba.blak.BlakPreferences;
import com.iba.blak.common.Distribution;
import com.iba.blak.common.PopupDisplayer;
import com.iba.blak.common.Utils;
import com.iba.blak.device.api.BeamCurrentMonitor;
import com.iba.blak.device.api.BeamProfileMonitor;
import com.iba.blak.device.api.Degrader;
import com.iba.blak.device.api.DegraderBeamProfileMonitor;
import com.iba.blak.device.api.EcubtcuCanMagnet;
import com.iba.blak.device.api.Electrometer;
import com.iba.blak.device.api.IonizationChamber;
import com.iba.blak.device.api.SteeringMagnet;
import com.iba.blak.device.api.SteeringPowerSupplyBackEnd;
import com.iba.blak.device.api.SteeringPowerSupplyRequest;
import com.iba.blak.device.api.Teslameter;
import com.iba.blak.device.impl.AbstractEcubtcuCanMagnet;
import com.iba.blak.device.impl.AggregatedIonizationChamber23;
import com.iba.blak.device.impl.BeamCurrentMonitorOnElectrometer;
import com.iba.blak.device.impl.BeamProfileMonitorOnElectrometer;
import com.iba.blak.device.impl.BeamStop;
import com.iba.blak.device.impl.Collimator;
import com.iba.blak.device.impl.ContinuousDegrader;
import com.iba.blak.device.impl.DegraderBeamProfileMonitorOnElectrometer;
//import com.iba.blak.device.impl.DegraderLegacyBeamProfileMonitor;
import com.iba.blak.device.impl.EcubtcuCommandException;
import com.iba.blak.device.impl.EcubtcuDipole;
import com.iba.blak.device.impl.EcubtcuModbusSteering;
import com.iba.blak.device.impl.EcubtcuQuadrupole;
import com.iba.blak.device.impl.EcubtcuSteering;
import com.iba.blak.device.impl.ElectrometerImpl;
import com.iba.blak.device.impl.ExtractionQuadrupole;
import com.iba.blak.device.impl.ExtractionSteering;
import com.iba.blak.device.impl.IonizationChamber1;
import com.iba.blak.device.impl.IonizationChamber1PBS;
import com.iba.blak.device.impl.IonizationChamber23;
//over write
//import com.iba.blak.device.impl.LegacyBeamProfileMonitorImpl;
import com.iba.blak.device.impl.Slit;
import com.iba.blak.device.impl.StairsDegrader;
import com.iba.blak.device.impl.SteeringPowerSupplyConverter;
import com.iba.blak.device.impl.SteeringPowerSupplyImpl;
import com.iba.blak.device.impl.TeslameterImpl;
import com.iba.icomp.core.util.Logger;

import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

//over write 
import com.iba.blak.config.BeamLineElement ;
import com.iba.blak.config.BeamLineElementNotFoundException ;
import com.iba.blak.config.BeamLineIterator ;
import com.iba.pts.bms.bss.bps.devices.impl.DegraderBeamStopProxy;


/**
 * Beam line configuration class.
 */
public class BeamLine
{
   public static final Class<?>[] BEAM_LINE_MAGNET_CLASSES = {EcubtcuQuadrupole.class, EcubtcuDipole.class,
         EcubtcuSteering.class, SteeringMagnet.class};

   private static final String[] CSV_BPM_FILE_COLUMNS_TITLE = {"BPM", "Centroid X", "Centroid Y", "Sigma X",
         "Sigma Y", "Integrated current"};

   private static final String[] CSV_IC_FILE_COLUMNS_TITLE = {"IC", "Mean X", "RMS X", "Mean Y", "RMS Y"};

   private List<BeamLineElement> mElementsList;
   private Map<String, BeamLineElement> mElementsMap;
   private Map<Class<?>, List<BeamLineElement>> mClassElements;

   private boolean[] mUsedRooms;

   public BeamLine()
   {
      mElementsList = new ArrayList<BeamLineElement>();
      mElementsMap = new HashMap<String, BeamLineElement>();
      mClassElements = new HashMap<Class<?>, List<BeamLineElement>>();
      mUsedRooms = new boolean[9];
   }

   private void addClassElements(BeamLineElement pBle)
   {
      List<BeamLineElement> bleList = mClassElements.get(pBle.getClass());
      if (bleList == null)
      {
         bleList = new ArrayList<BeamLineElement>();
         mClassElements.put(pBle.getClass(), bleList);
      }
      bleList.add(pBle);
   }

   private List<BeamLineElement> createClassElements(Class<?> c)
   {
      List<BeamLineElement> bleList = new ArrayList<BeamLineElement>();
      for (BeamLineElement ble : mElementsList)
      {
         if (c.isInstance(ble))
         {
            bleList.add(ble);
         }
      }
      mClassElements.put(c, bleList);
      return bleList;
   }

   private void createNewElement(String pName, BeamLineElement pElement)
   {
      mElementsList.add(pElement);
      mElementsMap.put(pName, pElement);
      addClassElements(pElement);
   }

   private Degrader createDegrader(String pName, String degraderType, DegraderBeamProfileMonitor pDegraderBpm)
   {
      Degrader degrader = null;

      if (degraderType.equals("continuous"))
      {
         degrader = new ContinuousDegrader(pName, pDegraderBpm);
      }

      else if (degraderType.equals("stairs"))
      {
         degrader = new StairsDegrader(pName, pDegraderBpm);
      }

      if (degrader != null)
      {
         createNewElement(pName, degrader);
         Logger.getLogger().debug("Added new Degrader " + pName);
         if (pDegraderBpm != null)
         {
            pDegraderBpm.setDegrader(degrader);
         }
      }
      else
      {
         Logger.getLogger().warn("Unknown type of degrader : '" + degraderType + "'");
      }

      return degrader;
   }

   private void createSteeringMagnet(String pName, String pPowerSupplyName, int pIndex)
   {
      if (pPowerSupplyName == null || pPowerSupplyName.isEmpty())
      {
         // ECUBTCU (legacy) steering magnet
         EcubtcuSteering steering = new EcubtcuSteering(pName);
         createNewElement(pName, steering);
         Logger.getLogger().debug("Added new legacy steering " + pName);
      }
      else
      {
         SteeringPowerSupplyImpl powerSupply = (SteeringPowerSupplyImpl) mElementsMap.get(pPowerSupplyName);
         SteeringMagnet steering = new SteeringPowerSupplyConverter(pName, powerSupply, pIndex);
         createNewElement(pName, steering);
         Logger.getLogger().debug("Added new steering " + pName);
      }
   }

   private void createModbusMagnet(String pName)
   {
      EcubtcuModbusSteering steering = new EcubtcuModbusSteering(pName);
      createNewElement(pName, steering);
      Logger.getLogger().debug("Added new legacy modular steering " + pName);
   }

   private void createElectrometer(String pName, String pAddress, String pPort)
   {
      Electrometer electrometer;
      if (pAddress == null || pAddress.isEmpty())
      {
         electrometer = new ElectrometerImpl(pName);
         // TODO actually this is an error !
         Logger.getLogger().warn(pName + " electrometer has no address !");
      }
      else
      {
         if (pPort == null || pPort.isEmpty())
         {
            electrometer = new ElectrometerImpl(pName, pAddress);
            pPort = null;
         }
         else
         {
            electrometer = new ElectrometerImpl(pName, pAddress, Integer.parseInt(pPort));
         }
      }
      createNewElement(pName, electrometer);
      Logger.getLogger().debug(
            "Added new electrometer " + pName + " (" + pAddress + ":" + (pPort == null ? "default_port" :
                  pPort) + ")");
   }

   private void createTeslameter(String pName)
   {
      Teslameter tesla = new TeslameterImpl(pName);
      createNewElement(pName, tesla);
      Logger.getLogger().debug("Added new teslameter " + pName);
   }

   private void createSteeringPowerSupply(String pName, String pAddress, String pPort) throws IOException
   {
      SteeringPowerSupplyImpl powerSupply;
      if (pAddress == null || pAddress.isEmpty())
      {
         Logger.getLogger().error(pName + " power supply has no address !");
         throw new IOException(pName + " power supply has no address");
      }
      if (pPort == null || pPort.isEmpty())
      {
         powerSupply = new SteeringPowerSupplyImpl(pName, pAddress);
      }
      else
      {
         powerSupply = new SteeringPowerSupplyImpl(pName, pAddress, Integer.parseInt(pPort));
      }
      createNewElement(pName, powerSupply);
      powerSupply.connect();
      // regularly update the measurements feedbacks
      SteeringPowerSupplyRequest req = powerSupply.createRefreshRequest();
      powerSupply.queue(req, SteeringPowerSupplyBackEnd.DEFAULT_POLLING_INTERVAL);
      Logger.getLogger().debug("Added new steering power supply " + pName + " (" + pAddress + ":" + (
            pPort == null || pPort.isEmpty() ? "default_port" : pPort) + ")");
   }

   private DegraderBeamProfileMonitor createBpm(String pName, String[] pFields, Degrader pDegrader)
   {
      BeamProfileMonitor bpm;
      DegraderBeamProfileMonitor degraderBpm = null;
      if (pFields[0] == null || pFields[0].isEmpty())
      {
         bpm = new LegacyBeamProfileMonitorImpl(pName);
         createNewElement(pName, bpm);
         Logger.getLogger().debug("Added new BPM " + pName);
      }
      else if (pFields[0].equalsIgnoreCase("degrader"))
      {
         if (pFields[1] == null || pFields[1].isEmpty())
         {
            degraderBpm = new DegraderLegacyBeamProfileMonitor(pName, pDegrader);
         }
         else if (pFields[2] != null && !pFields[2].isEmpty())
         {
            Electrometer electrometer = (Electrometer) mElementsMap.get(pFields[1]);
            int mezzId = Integer.valueOf(pFields[2]).intValue() - 1; // 0-indexed
            // TODO : check electrometer and mezzanine ID validity
            degraderBpm = new DegraderBeamProfileMonitorOnElectrometer(pName, pDegrader, electrometer,
                  mezzId);
         }
         if (pDegrader != null)
         {
            pDegrader.setBpm(degraderBpm);
         }
         createNewElement(pName, degraderBpm);
         Logger.getLogger().debug("Added new degrader BPM " + pName);
      }
      else
      {
         String name = pFields[0];
         Electrometer electrometer = (Electrometer) mElementsMap.get(name);
         int mezzId = Integer.valueOf(pFields[1]).intValue() - 1; // 0-indexed
         // TODO : check electrometer and mezzanine ID validity
         bpm = new BeamProfileMonitorOnElectrometer(pName, electrometer, mezzId);
         createNewElement(pName, bpm);
         Logger.getLogger().debug(
               "Added new BPM " + pName + " connected on the mezzanine " + (mezzId + 1) + " of " + name);
      }
      return degraderBpm;
   }

   private void createBeamStop(String pName, String pBcmName, int pIndex)
   {
      BeamStop bs = new BeamStop(pName);
      if (pBcmName != null && !pBcmName.isEmpty())
      {
         BeamCurrentMonitor bcm = (BeamCurrentMonitor) mElementsMap.get(pBcmName);
         if (bcm == null)
         {
            String msg = "The BCM connected to the" + pName + " beamstop does not exist !";
            Logger.getLogger().error(msg);
            SwingUtilities.invokeLater(
                  new PopupDisplayer(msg, "Invalid device name", JOptionPane.ERROR_MESSAGE));
            return;
         }
         bs.setBeamCurrentMeasurement(bcm, pIndex);
      }
      createNewElement(pName, bs);
      Logger.getLogger().debug("Added new Beam stop " + pName);
   }

   private void createIC(String pName, int pRoom, boolean pIsPbsIc, Map<Integer,
         IonizationChamber23> pFirstIc23Map) throws Exception
   {
      String fullName = pName + "R" + pRoom;
      Matcher m = Pattern.compile("\\d+").matcher(pName);
      if (!m.find())
      {
         Logger.getLogger().warn("Cannot guess IC number for " + pName);
      }
      int icNumber = Integer.parseInt(m.group());
      IonizationChamber ic;
      switch (icNumber)
      {
         case 1:
            ic = pIsPbsIc ? new IonizationChamber1PBS(fullName, pRoom, icNumber) :
                  new IonizationChamber1(fullName, pRoom, icNumber);
            break;

         case 2:
            ic = new IonizationChamber23(fullName, pRoom, icNumber, IonizationChamber.ORIENTATION_Y);
            break;

         case 3:
            ic = new IonizationChamber23(fullName, pRoom, icNumber, IonizationChamber.ORIENTATION_X);
            break;

         default:
            throw new Exception("IC type doesn't exist for " + pName);
      }
      if (ic instanceof IonizationChamber23)
      {
         if (pFirstIc23Map.containsKey(ic.getRoom()))
         {
            AggregatedIonizationChamber23 aggregatedIc = new AggregatedIonizationChamber23(
                  pFirstIc23Map.remove(ic.getRoom()), (IonizationChamber23) ic);
            createNewElement(aggregatedIc.getName(), aggregatedIc);
         }
         else
         {
            pFirstIc23Map.put(ic.getRoom(), (IonizationChamber23) ic);
         }
      }
      createNewElement(fullName, ic);
      Logger.getLogger().debug("Added new IC : " + pName);

      mUsedRooms[pRoom - 1] = true;
   }

   private void createCollimator(String elementName, String bcmName, String pJawId1, String pJawId2,
         String pJawId3, String pJawId4)
   {
      Collimator collimator = new Collimator(elementName);
      BeamCurrentMonitor bcm = null;
      if (bcmName != null && !bcmName.isEmpty())
      {
         bcm = (BeamCurrentMonitor) mElementsMap.get(bcmName);
         if (bcm == null)
         {
            String msg = "The BCM connected to the " + elementName + " collimator does not exist !";
            Logger.getLogger().error(msg);
            SwingUtilities.invokeLater(
                  new PopupDisplayer(msg, "Invalid device name", JOptionPane.ERROR_MESSAGE));
            return;
         }
      }
      if (pJawId4 != null && !pJawId4.isEmpty())
      {
         // Collimator with 4 jaws
         collimator.setBeamCurrentMeasurement(bcm, Integer.parseInt(pJawId1) - 1,
               Integer.parseInt(pJawId2) - 1, Integer.parseInt(pJawId3) - 1, Integer.parseInt(pJawId4) - 1);
      }
      else if (pJawId3 != null && !pJawId3.isEmpty())
      {
         if (pJawId3.toLowerCase().startsWith("vert"))
         {
            // TODO vertical collimator with 2 current measurements
            String msg = "Vertical collimator are not supported yet !";
            Logger.getLogger().warn(msg);
            SwingUtilities.invokeLater(
                  new PopupDisplayer(msg, "Unsupported device", JOptionPane.WARNING_MESSAGE));
         }
         else
         {
            // Horizontal collimator with 2 jaws
            collimator.setBeamCurrentMeasurement(bcm, Integer.parseInt(pJawId1) - 1,
                  Integer.parseInt(pJawId2) - 1);
         }
      }
      else if (pJawId2 != null && !pJawId2.isEmpty())
      {
         // Horizontal collimator with 2 jaws
         collimator.setBeamCurrentMeasurement(bcm, Integer.parseInt(pJawId1) - 1,
               Integer.parseInt(pJawId2) - 1);
      }
      else
      {
         // Collimator with only one current measurement
         collimator.setBeamCurrentMeasurement(bcm, Integer.parseInt(pJawId1) - 1);
      }
      createNewElement(elementName, collimator);
      Logger.getLogger().debug("Added new Collimator " + elementName);
   }

   private void updateTreatmentroom(String pRoomNb, String pTreatmentMode, String pBdp, String pBsp)
   {
      Blak.room.setRoomId(Integer.parseInt(pRoomNb));
      Blak.room.setTreatmentMode(pTreatmentMode);
      Blak.room.setBdp(pBdp);
      Blak.room.setBsp(pBsp);
   }

   public void loadFromFile(String filename)
   {
      // Delete previous beamline elements
      mElementsList.clear();
      mElementsMap.clear();
      mClassElements.clear();
      boolean trFlag = false;

      HashMap<Integer, IonizationChamber23> firstIc23Map = new HashMap<Integer, IonizationChamber23>();

      int pointIndex = filename.lastIndexOf(".");
      String fileExt = pointIndex == -1 ? "" : filename.substring(pointIndex + 1, filename.length());

      if (fileExt.equals("txt"))
      {
         Logger.getLogger().warn("Please ensure that you are not using an old description file format");
      }

      try
      {
         Logger.getLogger().debug("Starting reading file...");

         InputStreamReader fr = new InputStreamReader(new FileInputStream(filename),
               BlakConstants.DEFAULT_CHARSET.name());

         CsvReader csvr = new CsvReader(fr, ',');

         Degrader degrader = null;
         DegraderBeamProfileMonitor degraderBpm = null;
         DegraderBeamStopProxy degraderBcm = null;

         try
         {
            while (csvr.readRecord())
            {
               String elementName = csvr.get(0);
               // Skip comment lines
               if (elementName.isEmpty() || elementName.startsWith("#"))
               {
                  continue;
               }
               Logger.getLogger().debug("Loading " + elementName);
               String elementType = csvr.get(1).toLowerCase();

               if (elementType.equals("degrader"))
               {
                  degrader = createDegrader(elementName, csvr.get(2).toLowerCase(), degraderBpm);
               }

               else if (elementType.equals("quad"))
               {
                  EcubtcuQuadrupole quad = new EcubtcuQuadrupole(elementName);
                  createNewElement(elementName, quad);
                  Logger.getLogger().debug("Added new Quad " + elementName);
               }

               else if (elementType.equals("acuquad"))
               {
                  ExtractionQuadrupole quad = new ExtractionQuadrupole(elementName);
                  createNewElement(elementName, quad);
                  Logger.getLogger().debug("Added new Extraction quad " + elementName);
               }

               else if (elementType.equals("bend"))
               {
                  EcubtcuDipole bending = new EcubtcuDipole(elementName);
                  createNewElement(elementName, bending);
                  Logger.getLogger().debug("Added new Bending magnet " + elementName);
               }

               else if (elementType.equals("steering"))
               {
                  int index = -1;
                  String powerSupplyName = csvr.get(2);
                  if (powerSupplyName != null && !powerSupplyName.isEmpty())
                  {
                     index = Integer.valueOf(csvr.get(3)).intValue() - 1; // 0-indexed
                  }
                  createSteeringMagnet(elementName, powerSupplyName, index);
               }
               else if (elementType.equals("modbusmagnet"))
               {
                  createModbusMagnet(elementName);
               }

               else if (elementType.equals("acusteering"))
               {
                  ExtractionSteering steering = new ExtractionSteering(elementName);
                  createNewElement(elementName, steering);
                  Logger.getLogger().debug("Added new Extraction steering " + elementName);
               }

               else if (elementType.equals("slits"))
               {
                  Slit slit = new Slit(elementName);
                  createNewElement(elementName, slit);
                  Logger.getLogger().debug("Added new Slit " + elementName);
               }

               else if (elementType.equals("electrometer"))
               {
                  String address = csvr.get(2);
                  String port = csvr.get(3);
                  createElectrometer(elementName, address, port);
               }

               // new steering magnets power supply unit
               else if (elementType.equals("steeringsupply"))
               {
                  String address = csvr.get(2);
                  String port = csvr.get(3);
                  createSteeringPowerSupply(elementName, address, port);
               }

               else if (elementType.equals("bpm"))
               {
                  String[] fields = new String[3];
                  for (int i = 0; i < fields.length; ++i)
                  {
                     fields[i] = csvr.get(i + 2);
                  }
                  DegraderBeamProfileMonitor bpm = createBpm(elementName, fields, degrader);
                  if (bpm != null)
                  {
                     degraderBpm = bpm;
                  }
               }

               else if (elementType.equals("bs"))
               {
                  int index = -1;
                  if (csvr.get(3) != null && !csvr.get(3).isEmpty())
                  {
                     index = Integer.parseInt(csvr.get(3)) - 1;
                  }
                  createBeamStop(elementName, csvr.get(2), index);
               }

               else if (elementType.equals("tesla"))
               {
                  createTeslameter(elementName);
               }
               else if (elementType.equals("ic"))
               {
                  int icRoom = Integer.parseInt(csvr.get(2));
                  if (icRoom < 1 || icRoom > 9)
                  {
                     Logger.getLogger().warn("Room number doesn't exist for " + elementName);
                     break;
                  }
                  boolean isPbsIc = csvr.get(3).toLowerCase().equals("pbs");
                  createIC(elementName, icRoom, isPbsIc, firstIc23Map);
               }

               else if (elementType.equals("bcm"))
               {
                  String electrometerName = csvr.get(2);
                  if (electrometerName != null && !electrometerName.isEmpty())
                  {
                     Electrometer electrometer = (Electrometer) mElementsMap.get(electrometerName);
                     if (electrometer == null)
                     {
                        String msg = "Invalid device name specified in " + filename
                              + ". The electrometer connected to " + elementName + " does not exist !";
                        Logger.getLogger().error(msg);
                        SwingUtilities.invokeLater(
                              new PopupDisplayer(msg, "Invalid device name", JOptionPane.ERROR_MESSAGE));
                        continue;
                     }
                     int mezzId = Integer.parseInt(csvr.get(3)) - 1; // 0-indexed
                     BeamCurrentMonitor bcm = new BeamCurrentMonitorOnElectrometer(elementName, electrometer,
                           mezzId);
                     createNewElement(elementName, bcm);
                     Logger.getLogger().debug(
                           "Added new BCM " + elementName + " connected on the mezzanine " + (mezzId + 1)
                                 + " of " + electrometerName);
                  }
                  else
                  {
                     // TODO add the support for legacy BCM
                     String msg = "Legacy BCM not supported (" + elementName + ")";
                     Logger.getLogger().warn(msg);
                     SwingUtilities.invokeLater(
                           new PopupDisplayer(msg, Utils.getCurrentFunction(), JOptionPane.WARNING_MESSAGE));

                     String[] fields = new String[3];
                     for (int i = 0; i < fields.length; ++i)
                     {
                        fields[i] = csvr.get(i + 2);
                     }
                     int index = -1;
                     if (csvr.get(3) != null && !csvr.get(3).isEmpty())
                     {
                        index = Integer.parseInt(csvr.get(3)) - 1;
                     }
                     BeamCurrentMonitor bcm = new BeamCurrentMonitor() {
                        @Override
                        public OperationMode getOperationMode() {
                           return null;
                        }

                        @Override
                        public void startContinuousAcquisition() {

                        }

                        @Override
                        public void stopContinuousAcquisition() {

                        }

                        @Override
                        public Distribution getChannels() {
                           return null;
                        }

                        @Override
                        public String getName() {
                           return null;
                        }

                        @Override
                        public void setSelected(boolean b) {

                        }

                        @Override
                        public boolean isSelected() {
                           return false;
                        }

                        @Override
                        public void addPropertyChangeListener(String s, PropertyChangeListener propertyChangeListener) {

                        }

                        @Override
                        public void removePropertyChangeListener(String s, PropertyChangeListener propertyChangeListener) {

                        }

                        @Override
                        public void addPropertyChangeListener(PropertyChangeListener pListener) {

                        }

                        @Override
                        public void removePropertyChangeListener(PropertyChangeListener pListener) {

                        }
                     };
                     createNewElement(elementName, bcm);
                     Logger.getLogger().debug(
                             "Added new BCM " + elementName);
                     createNewElement(elementName, bcm);

                  }
               }

               else if (elementType.equals("collimator"))
               {
                  createCollimator(elementName, csvr.get(2), csvr.get(3), csvr.get(4), csvr.get(5),
                        csvr.get(6));
               }
               else if (elementType.equals("tr"))
               {
                  if (trFlag)
                  {
                     throw new Exception("Only one treatment room element is allowed!");
                  }
                  updateTreatmentroom(csvr.get(2), csvr.get(3), csvr.get(4), csvr.get(5));
                  trFlag = true;
               }

               else
               {
                  Logger.getLogger().warn("Unknown element type : " + elementType + " (" + elementName + ")");
               }

            }
         }
         catch (IOException e)
         {
            String msg = "Error reading beam line description file '" + filename + "'";
            Logger.getLogger().warn(msg);
            SwingUtilities.invokeLater(new PopupDisplayer(msg, "IO Error", JOptionPane.WARNING_MESSAGE));
         }
         catch (Exception e)
         {
            String msg = "Could not treat data in beam line description file : " + e.getMessage();
            Logger.getLogger().error(msg);
            SwingUtilities.invokeLater(
                  new PopupDisplayer(msg, Utils.getCurrentFunction(), JOptionPane.ERROR_MESSAGE));
         }

         csvr.close();
      }
      catch (IOException e)
      {
         String msg = "Could not open beam line description file '" + filename + "'";
         Logger.getLogger().warn(msg);
         SwingUtilities.invokeLater(new PopupDisplayer(msg, "IO Error", JOptionPane.WARNING_MESSAGE));
      }

      printBeamline();
   }

   public void printBeamline()
   {
      Logger.getLogger().info("Beamline configuration :  -------------------------------------");
      for (BeamLineElement ble : mElementsList)
      {
         Logger.getLogger().info(ble.getName() + " " + ble.getClass());
      }
      Logger.getLogger().info("---------------------------------------------------------------");
   }

   @Override
   public String toString()
   {
      final String[] elementTypeString = {"Degrader", "Dipole", "Quadrupole", "ECUBTCU Steering", "Steering",
            "BPM", "IC", "Slit", "Beam stop"};
      final Class<?>[] elementClass = {Degrader.class, EcubtcuDipole.class, EcubtcuQuadrupole.class,
            EcubtcuSteering.class, SteeringMagnet.class, BeamProfileMonitor.class, IonizationChamber.class,
            Slit.class, BeamStop.class};
      StringBuffer[] elementList = new StringBuffer[elementClass.length];
      int[] elementNb = new int[elementClass.length];

      StringBuffer sb = new StringBuffer();
      for (int i = 0; i < elementList.length; ++i)
      {
         elementList[i] = new StringBuffer();
         elementNb[i] = 0;
      }

      for (BeamLineElement e : mElementsList)
      {
         for (int t = 0; t < elementClass.length; t++)
         {
            if (elementClass[t].isInstance(e))
            {

               // Except for the first element
               if (elementNb[t] != 0)
               {

                  // Comas between elements
                  elementList[t].append(", ");

                  // Adds a CR after every 10 element
                  if (elementNb[t] % 10 == 0)
                  {
                     elementList[t].append(
                           "\n" + Utils.getConstantCharString(' ', elementTypeString[t].length() + 9));
                  }
               }

               // Element's name
               elementList[t].append(e.getName());
               elementNb[t]++;
            }
         }
      }

      for (int t = 0; t < elementClass.length; t++)
      {
         sb.append(elementTypeString[t] + "s (" + elementNb[t] + ") : " + elementList[t] + "\n");
      }

      return sb.toString();
   }

   public boolean contains(String name)
   {
      return mElementsMap.containsKey(name);
   }

   public int getNbElements()
   {
      return mElementsList.size();
   }

   public List<BeamLineElement> getElements()
   {
      return mElementsList;
   }

   public List<BeamLineElement> getElements(Class<?> c)
   {
      List<BeamLineElement> bleList = mClassElements.get(c);
      if (bleList == null)
      {
         bleList = createClassElements(c);
      }
      return bleList;
   }

   public BeamLineElement getElement(int index)
   {
      return mElementsList.get(index);
   }

   public boolean hasElement(String name)
   {
      return mElementsMap.containsKey(name);
   }

   public BeamLineElement getElement(String name) throws BeamLineElementNotFoundException
   {
      BeamLineElement ble = mElementsMap.get(name);
      if (ble != null)
      {
         return ble;
      }
      throw new BeamLineElementNotFoundException(name);
   }

   public BeamLineElement getElement(String name, Class<?> c) throws BeamLineElementNotFoundException
   {
      BeamLineElement ble = mElementsMap.get(name);
      if (ble != null && c.isInstance(ble))
      {
         return ble;
      }
      throw new BeamLineElementNotFoundException(name);
   }

   public int getNbElements(Class<?> c)
   {
      return getElements(c).size();
   }

   public void saveBpmsToFile(String filename, String range)
   {
      try
      {
         Logger.getLogger().debug("Creating bpm file " + filename);
         OutputStreamWriter fw = new OutputStreamWriter(new FileOutputStream(filename),
               BlakConstants.DEFAULT_CHARSET.name());
         CsvWriter csvw = new CsvWriter(fw, ',');

         // Write range
         csvw.write("Range");
         csvw.write(range);
         csvw.endRecord();

         // Write columns titles
         csvw.writeRecord(CSV_BPM_FILE_COLUMNS_TITLE);

         // Write data
         BeamLineIterator bpmIterator = new BeamLineIterator(Blak.beamLine, BeamProfileMonitor.class);
         while (bpmIterator.hasNext())
         {
            BeamProfileMonitor bpm = (BeamProfileMonitor) bpmIterator.next();
            if (bpm.isSelected())
            {
               csvw.write(bpm.getName());
               csvw.write(Double.isNaN(bpm.getHorizontalCentroid()) ? "" :
                     Double.toString(bpm.getHorizontalCentroid()));
               csvw.write(Double.isNaN(bpm.getVerticalCentroid()) ? "" :
                     Double.toString(bpm.getVerticalCentroid()));
               csvw.write(
                     Double.isNaN(bpm.getHorizontalSigma()) ? "" : Double.toString(bpm.getHorizontalSigma()));
               csvw.write(
                     Double.isNaN(bpm.getVerticalSigma()) ? "" : Double.toString(bpm.getVerticalSigma()));
               csvw.write(Double.isNaN(bpm.getIntegratedCurrent()) ? "" :
                     Double.toString(bpm.getIntegratedCurrent()));
               csvw.endRecord();
            }
         }

         csvw.close();

         Logger.getLogger().debug("Magnet file " + filename + " was exported successfuly");

      }
      catch (IOException e)
      {
         String msg = "Could not write file " + filename;
         Logger.getLogger().warn(msg);
         SwingUtilities.invokeLater(new PopupDisplayer(msg, "IO Error", JOptionPane.WARNING_MESSAGE));
      }
   }

   public void saveIcsToFile(String filename, String range)
   {
      try
      {
         Logger.getLogger().debug("Creating IC file " + filename);
         CsvWriter csvw = new CsvWriter(new FileWriter(filename), ',');

         // Write range
         csvw.write("Range");
         csvw.write(range);
         csvw.endRecord();

         // Write columns titles
         for (int i = 0; i < CSV_IC_FILE_COLUMNS_TITLE.length; ++i)
         {
            csvw.write(CSV_IC_FILE_COLUMNS_TITLE[i]);
         }
         csvw.write("");
         for (int i = 0; i < 32; ++i)
         {
            csvw.write("qcx" + i);
         }
         csvw.write("");
         for (int i = 0; i < 32; ++i)
         {
            csvw.write("qcy" + i);
         }
         csvw.endRecord();

         // Write data
         BeamLineIterator icIterator = new BeamLineIterator(Blak.beamLine, IonizationChamber.class);
         while (icIterator.hasNext())
         {
            IonizationChamber ic = (IonizationChamber) icIterator.next();
            if (ic.isSelected())
            {
               csvw.write(ic.getName());
               csvw.write(Double.isNaN(ic.getXMean()) ? "" : Double.toString(ic.getXMean()));
               csvw.write(Double.isNaN(ic.getXRms()) ? "" : Double.toString(ic.getXRms()));
               csvw.write(Double.isNaN(ic.getYMean()) ? "" : Double.toString(ic.getYMean()));
               csvw.write(Double.isNaN(ic.getYRms()) ? "" : Double.toString(ic.getYRms()));

               csvw.write("");
               for (int qc = 0; qc < 32; qc++)
               {
                  String qcValue = "";
                  if (qc < ic.getNbHorizontalQualityChannels())
                  {
                     qcValue = Double.toString(ic.getHorizontalChannel(qc));
                  }
                  csvw.write(qcValue);
               }

               csvw.write("");
               for (int qc = 0; qc < 32; qc++)
               {
                  String qcValue = "";
                  if (qc < ic.getNbVerticalQualityChannels())
                  {
                     qcValue = Double.toString(ic.getVerticalChannel(qc));
                  }
                  csvw.write(qcValue);
               }

               csvw.endRecord();
            }
         }

         csvw.close();

         Logger.getLogger().debug("Magnet file " + filename + " was exported successfuly");

      }
      catch (IOException e)
      {
         String msg = "Could not write file " + filename;
         Logger.getLogger().warn(msg);
         SwingUtilities.invokeLater(new PopupDisplayer(msg, "IO Error", JOptionPane.WARNING_MESSAGE));
      }
   }

   public void generateTransportFile(String filename) throws Exception
   {

      final String VAR_TOKEN = "VAR_";

      String templateFilename = BlakPreferences.getString(BlakConstants.FILES_SAVE_TRANSPORT_TEMPLATE);

      BufferedReader fin;
      BufferedWriter fout;

      try
      {
         InputStreamReader fr = new InputStreamReader(new FileInputStream(templateFilename),
               BlakConstants.DEFAULT_CHARSET.name());

         fin = new BufferedReader(fr);

         OutputStreamWriter fw = new OutputStreamWriter(new FileOutputStream(filename),
               BlakConstants.DEFAULT_CHARSET.name());

         fout = new BufferedWriter(fw);
      }
      catch (FileNotFoundException e)
      {
         throw new Exception("Could not open transport template file '" + templateFilename + "'");
      }
      catch (IOException e)
      {
         throw new Exception("Could not write in file '" + filename + "'");
      }

      NumberFormat magnetNf = NumberFormat.getInstance();
      magnetNf.setMinimumFractionDigits(0);
      magnetNf.setMaximumFractionDigits(4);
      NumberFormat currentNf = NumberFormat.getInstance();
      currentNf.setMinimumFractionDigits(0);
      currentNf.setMaximumFractionDigits(2);

      // Continue to read lines in the template file while there are still some
      // left to read
      while (fin.ready())
      {
         String line = fin.readLine();
         if (line == null)
         {
            break;
         }

         while (line.contains(VAR_TOKEN))
         {

            int varIndex = line.indexOf(VAR_TOKEN);
            int endVarIndex = line.indexOf(" ", varIndex);

            if (endVarIndex == -1)
            {
               throw new Exception("Parsing error");
            }

            String variable = line.substring(varIndex, endVarIndex);
            String[] variableSplit = variable.split("_");
            String element = variableSplit.length >= 2 ? variableSplit[1] : "";
            String axis = variableSplit.length >= 3 ? variableSplit[2] : "";
            String replacementString = "";

            BeamLineElement ble = getElement(element);

            if (ble instanceof EcubtcuCanMagnet)
            {
               EcubtcuCanMagnet m = (EcubtcuCanMagnet) ble;
               replacementString = magnetNf.format(m.getFieldSetpoint());
            }

            else if (ble instanceof BeamProfileMonitor)
            {
               BeamProfileMonitor bpm = (BeamProfileMonitor) ble;
               double sigma = axis.equals("X") ? bpm.getHorizontalSigma() : bpm.getVerticalSigma();
               replacementString = currentNf.format(0.1 * sigma); // value in cm
            }

            else
            {
               throw new Exception("Unsupported element type : " + element);
            }

            line = line.replaceFirst(variable, replacementString);
         }

         fout.write(line + "\n");
      }
      fin.close();
      fout.close();
   }

   public void loadTransportFile(String filename) throws Exception
   {
      BufferedReader fin;
      int lineNumber = 0;
      try
      {
         InputStreamReader fr = new InputStreamReader(new FileInputStream(filename),
               BlakConstants.DEFAULT_CHARSET.name());

         fin = new BufferedReader(fr);
      }
      catch (FileNotFoundException e)
      {
         throw new Exception("Could not open transport file '" + filename + "'");
      }

      Pattern pattern = Pattern.compile("\\s+");
      while (fin.ready())
      {
         String line = fin.readLine();

         // Replace all duplicate white space by a single space
         Matcher matcher = pattern.matcher(line);
         matcher.find();
         line = matcher.replaceAll(" ");

         String[] lineFields = line.split(" ");
         ++lineNumber;

         if (lineFields.length > 0 && lineFields[0].length() > 0)
         {
            switch (lineFields[0].charAt(0))
            {
               case '4': // Bendings
               case '5': // Quads
                  String name = lineFields[4].replaceAll("/", "");
                  try
                  {
                     double setPoint = Double.parseDouble(lineFields[2]);
                     BeamLineElement ble = Blak.beamLine.getElement(name);
                     if (ble instanceof AbstractEcubtcuCanMagnet)
                     {
                        EcubtcuCanMagnet m = (EcubtcuCanMagnet) ble;
                        m.setField(setPoint);
                     }
                  }
                  catch (EcubtcuCommandException e)
                  {
                     Logger.getLogger().warn("Could not set element " + name + " on line " + lineNumber);
                  }
                  catch (Exception e)
                  {
                     throw new Exception(e.getMessage() + " on line " + lineNumber);
                  }
                  break;
            }
         }
      }
      fin.close();
   }

}
