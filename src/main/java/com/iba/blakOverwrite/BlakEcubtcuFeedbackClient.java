// ///////////////////////////////////////////////////////////////////
// Copyright (c) by Ion Beam Applications S.A.
// All rights reserved
//
// Rue du cyclotron, 3
// B-1348 Louvain-la-Neuve
// Tel : +32.10.47.58.11
// update for iAlignment
// ///////////////////////////////////////////////////////////////////

package com.iba.blakOverwrite;

import com.iba.blak.Blak;
import com.iba.blak.config.BeamLineElement;
import com.iba.blak.config.BeamLineElementNotFoundException;
import com.iba.blak.device.api.BeamProfileMonitor;
import com.iba.blak.device.api.Degrader;
import com.iba.blak.device.api.DegraderBackEnd;
import com.iba.blak.device.api.EcubtcuCanMagnetBackEnd;
import com.iba.blak.device.api.EcubtcuSteeringBackEnd;
import com.iba.blak.device.api.IonizationChamber;
import com.iba.blak.device.api.IonizationChamberBackend;
import com.iba.blak.device.api.LegacyBeamProfileMonitor;
import com.iba.blak.device.api.Magnet;
import com.iba.blak.device.api.MagnetBackEnd;
import com.iba.blak.device.api.SteeringMagnetBackEnd;
import com.iba.blak.device.api.Teslameter;
import com.iba.blak.device.impl.BeamStop;
import com.iba.blak.device.impl.Slit;
import com.iba.icomp.core.property.PropertyChangeProvider;
import com.iba.icomp.core.util.Logger;

import com.iba.ialign.Controller;

/**
 * Class responsible for retreiving and dispatching the feedback received from all devices.
 *
 * @author jmfranc
 * update for iAlignment: xiongjun Zhou
 */
public abstract class BlakEcubtcuFeedbackClient implements PropertyChangeProvider
{
   public static final String MCR_SYSMGR_SW_MCRSERVICESESSION = "mcr_sysmgr_sw_mcrservicesession";
   public static final String CONNECTION = "connection";

   private double[] profilingBpmXCurrent;
   private double[] profilingBpmYCurrent;
   private LegacyBeamProfileMonitor mProfilingBpm;

   public BlakEcubtcuFeedbackClient()
   {
      profilingBpmXCurrent = new double[16];
      profilingBpmYCurrent = new double[16];
   }

   abstract public boolean connect();

   abstract public void disconnect();

   abstract public void retreiveMcrFeedbacks();

   abstract public void retreiveTcrFeedbacks(int roomId);

   public abstract void sendRtBooleanVariable(String subject, String variable, boolean value);

   public abstract void sendRtNumericVariable(String subject, String variable, float value);

   public abstract boolean isConnected();

   public void setProfilingBpm(LegacyBeamProfileMonitor pBpm)
   {
      mProfilingBpm = pBpm;
   }

   public LegacyBeamProfileMonitor getProfilingBpm()
   {
      return mProfilingBpm;
   }

   public class Receiver
   {
      public void setNumericCBValue(String name, double value)
      {
         String[] splitName = name.split("\\.");
         String cu = splitName[0];
         String element = splitName.length > 1 ? splitName[1].toUpperCase() : "";
         String variable = splitName.length > 2 ? splitName[2] : "";

         Logger.getLogger().debug(cu + " > " + element + "." + variable + " = " + value);

         try
         {
 //           if (cu.equals(MCR_SYSMGR_SW_MCRSERVICESESSION))
 //           {
 //              Blak.security.setMcrInTreatmentMode(value == 0.0);
 //           }

            if (cu.equals("mcr_ecubtcu"))
            {
               if (element.equals("BPM"))
               {
                  if (variable.contains("_cavgchnl"))
                  {
                     String[] channelParam = variable.split("_cavgchnl");
                     int channelIndex = Integer.parseInt(channelParam[1]);
                     if (channelParam[0].equals("profile_x"))
                     {
                        profilingBpmXCurrent[channelIndex] = value;
                        if (mProfilingBpm != null)
                        {
                           mProfilingBpm.setProfileHorizontalValue(channelIndex, value);
                        }
                     }
                     else
                     {
                        profilingBpmYCurrent[channelIndex] = value;
                        if (mProfilingBpm != null)
                        {
                           mProfilingBpm.setProfileVerticalValue(channelIndex, value);
                        }
                     }
                  }
                  else if (variable.equals("profile_cavgtot"))
                  {
                     if (mProfilingBpm != null)
                     {
                        mProfilingBpm.setProfileIntegratedCurrent(value);
                     }
                  }

               }
               else if (element.equals("ISEU"))
               {
                  if (variable.equals("pulse_status"))
                  {
                	  Controller.beam.updateSinglePulseMode(value != 0.0);
                  }
//                  else if (variable.equals("lookupdone"))
//                  {
//                      Controller.beam.updateLookupStatus(value != 0.0);
//                  }
               }
               if (element.equals("SW"))
               {
                  if (variable.equals("alive_counter"))
                  {
                	  Logger.getLogger().debug("ecubtcu still alive.");
                  }
               }

               else
               {
                  BeamLineElement ble = Controller.beamLine.getElement(element);

                  if (ble instanceof Degrader)
                  {
                     DegraderBackEnd d = (DegraderBackEnd) ble;
                     if (variable.equals("status"))
                     {
                        d.updateStatus((int) value);
                     }
                     else if (variable.equals("feedbackctrl"))
                     {
                        d.updateMotorStepFeedback(value);
                     }
                     else if (variable.equals("feedbackstep"))
                     {
                        d.updateStairFeedback(value);
                     }
                     else if (variable.equals("maxenergyfeedback"))
                     {
                        d.updateMaxEnergyFeedback(value);
                     }
                     else if (variable.equals("minenergyfeedback"))
                     {
                        d.updateMinEnergyFeedback(value);
                     }
                     else if (variable.equals("energyfeedback"))
                     {
                        d.updateEnergyFeedback(value);
                     }
                     else if (variable.equals("stepsetpoint"))
                     {
                        d.updateMotorStepSetpoint(value);
                     }
                     else if (variable.equals("stairsetpoint"))
                     {
                        d.updateStairSetpoint(value);
                     }
                     else if (variable.equals("energysetpoint"))
                     {
                        d.updateEnergySetpoint(value);
                     }
                  }

                  else if (ble instanceof Magnet)
                  {
                     Magnet m = (Magnet) ble;
                     if (m instanceof MagnetBackEnd)
                     {
                        MagnetBackEnd em = (MagnetBackEnd) ble;
                        if (variable.equals("cfeedback"))
                        {
                           em.updateCurrentFeedback(value);
                        }
                        else if (variable.equals("csetpoint"))
                        {
                           em.updateCurrentSetpoint(value);
                        }
                        else if (em instanceof EcubtcuCanMagnetBackEnd)
                        {
                           EcubtcuCanMagnetBackEnd ecm = (EcubtcuCanMagnetBackEnd) em;
                           if (variable.equals("ffeedback"))
                           {
                              ecm.updateFieldFeedback(value);
                           }
                           else if (variable.equals("dfeedback"))
                           {
                              ecm.updateDigitalFeedback(value);
                           }
                           else if (variable.equals("fsetpoint"))
                           {
                              ecm.updateFieldSetpoint(value);
                           }
                           else if (variable.equals("dsetpoint"))
                           {
                              ecm.updateDigitalSetpoint(value);
                           }
                        }
                        else if (em instanceof EcubtcuSteeringBackEnd)
                        {
                           EcubtcuSteeringBackEnd esm = (EcubtcuSteeringBackEnd) em;
                           if (variable.equals("vfeedback"))
                           {
                              esm.updateVoltageFeedback(value);
                           }
                           else if (variable.equals("vsetpoint"))
                           {
                              esm.updateVoltageSetpoint(value);
                           }
                        }
                        else if (m instanceof SteeringMagnetBackEnd)
                        {
                           Logger.getLogger().error(
                                 "BlakRTClient could not receive a message aimed at a new steering magnet !"
                                       + "\nMessage : " + name + "\nSkipped.");
                        }

                     }
                     else
                     {
                        Logger.getLogger().error("BlakRTClient received a message aimed at the " + m.getName()
                              + " magnet.\n This is not managed by the ECU/BTCU ! Skipped.");
                     }
                  }

                  else if (ble instanceof BeamProfileMonitor)
                  {
                     BeamProfileMonitor bpm = (BeamProfileMonitor) ble;
                     if (variable.equals("instatus"))
                     {
                        bpm.updateInsertionFeedback((int) value);
                     }
                     else if (variable.equals("outstatus"))
                     {
                        bpm.updateRetractionFeedback((int) value);
                     }
                     else if (variable.equals("integrated_current"))
                     {
                        bpm.updateIntegratedCurrent(value);
                     }
                     else if (variable.equals("operation_mode"))
                     {
                        bpm.updateOperationMode((int) value);
                     }
                     else if (variable.equals("beamcentroid"))
                     {
                    	 String[] channelParam = variable.split("_beamcentroid");
                  
                         if (channelParam[0].equals("x"))
                         {
                        	 Logger.getLogger().info(variable + "X_position = " + value);
                         }
                         else
                         {
                        	 Logger.getLogger().info(variable + "Y_position = " + value);
                         }
                     }
                     else if (variable.contains("_cchnl"))
                     {
                        String[] channelParam = variable.split("_cchnl");
                        int channelIndex = Integer.parseInt(channelParam[1]);
                        if (channelParam[0].equals("x"))
                        {
                           bpm.updateHorizontalChannelValue(channelIndex, value);
                        }
                        else
                        {
                           bpm.updateVerticalChannelValue(channelIndex, value);
                        }
                     }
                  }

                  else if (ble instanceof BeamStop)
                  {
                     BeamStop bs = (BeamStop) ble;
                     if (variable.equals("instatus"))
                     {
                        bs.updateInsertionFeedback((int) value);
                     }
                     else if (variable.equals("outstatus"))
                     {
                        bs.updateRetractionFeedback((int) value);
                     }
                  }

                  else if (ble instanceof Slit)
                  {
                     Slit s = (Slit) ble;
                     if (variable.equals("motor_feedback"))
                     {
                        s.updateMotorFeedback(value);
                     }
                     else if (variable.equals("motor_setpoint"))
                     {
                        s.updateMotorSetpoint(value);
                     }
                     else if (variable.equals("lpot_position"))
                     {
                        s.updateMmLFeedback(value);
                     }
                     else if (variable.equals("setpoint"))
                     {
                        s.updateMmSetpoint(value);
                     }
                     else if (variable.equals("status"))
                     {
                        s.updateStatus((int) value);
                     }
                     else if (variable.equals("ls_activated"))
                     {
                        s.updateHomingLS(value > 0.5);
                     }
                  }
                  else if (ble instanceof Teslameter)
                  {
                     if (variable.equals("raw_ffeedback"))
                     {
                        ((Teslameter) ble).setRawFieldFeedback(value);

                     }
                     else if (variable.equals("corrected_ffeedback"))
                     {
                        ((Teslameter) ble).setCorrectedFieldFeedback(value);
                     }
                  }
               }
            }

/*            else if (cu.equals("mcr_acu"))
            {
               if (element.equals("SW"))
               {
                  if (variable.equals("counter"))
                  {
                     Blak.acu.updateHeartbeat((int) value);
                  }
               }
            }

            else if (cu.equals("mcr_scu"))
            {
               if (element.equals("SW"))
               {
                  if (variable.equals("counter"))
                  {
                     Blak.scu.updateHeartbeat((int) value);
                  }
               }
            }

            else if (cu.equals("mcr_blpscu"))
            {
               if (element.equals("SW"))
               {
                  if (variable.equals("counter"))
                  {
                     Blak.blpscu.updateHeartbeat((int) value);
                  }
               }
            }
*/
            else if (cu.matches("tr\\d_tcu"))
            {
               int room = Character.digit(cu.charAt(2), 10);
               String trElement = element + "R" + room;
               BeamLineElement ble = Controller.beamLine.getElement(trElement);

               if (ble instanceof IonizationChamberBackend)
               {
                  IonizationChamberBackend ic = (IonizationChamberBackend) ble;

                  if (variable.matches("qc\\d\\d"))
                  {
                     int channel = Integer.parseInt(variable.substring(2)) - 1;
                     ic.updateQualityChannel(channel, value);
                  }
                  else if (variable.equals("xmean"))
                  {
                     ic.updateXMean(value);
                  }
                  else if (variable.equals("ymean"))
                  {
                     ic.updateYMean(value);
                  }
                  else if (variable.equals("xrms"))
                  {
                     ic.updateXRms(value);
                  }
                  else if (variable.equals("yrms"))
                  {
                     ic.updateYRms(value);
                  }
                  else if (variable.equals("mean"))
                  {
                     if (ic.getOrientation() == IonizationChamber.ORIENTATION_X)
                     {
                        ic.updateXMean(value);
                     }
                     else
                     {
                        ic.updateYMean(value);
                     }
                  }
                  else if (variable.equals("rms"))
                  {
                     if (ic.getOrientation() == IonizationChamber.ORIENTATION_X)
                     {
                        ic.updateXRms(value);
                     }
                     else
                     {
                        ic.updateYRms(value);
                     }
                  }
               }
               else
               {
                  if (trElement.startsWith("IC"))
                  {
                     Logger.getLogger().warn("Cannot find the " + trElement + " element in the beamline");
                  }
               }
            }
         }
         catch (BeamLineElementNotFoundException e)
         {
            Logger.getLogger().debug(
                  "Unhandled " + cu + " element : " + element + " (" + variable + " = " + value + ")");
         }
      }
   }
}
