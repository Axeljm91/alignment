// ///////////////////////////////////////////////////////////////////
// Copyright (c) by Ion Beam Applications S.A.
// All rights reserved
//
// Rue du cyclotron, 3
// B-1348 Louvain-la-Neuve
// Tel : +32.10.47.58.11
// ///////////////////////////////////////////////////////////////////

package com.iba.blakOverwrite;

import com.iba.blak.device.api.Degrader;
import com.iba.blak.device.api.DegraderBeamProfileMonitor;
import com.iba.blak.device.api.EcubtcuException;
import com.iba.icomp.core.util.Logger;

public class DegraderLegacyBeamProfileMonitor extends AbstractLegacyBeamProfileMonitor implements
      DegraderBeamProfileMonitor
{

  // private static final double[] CHANNELS_CENTERS = {-20, -14.1, -11, -8, -5.6, -4, -2.4, -0.8, 0.8, 2.4, 4, 5.6, 8, 11, 14.1, 20};
   private static final double[] CHANNELS_CENTERS = { -26.3, -18, -14, -10, -7, -5, -3, -1, 1, 3, 5, 7, 10, 14, 18, 26.3 };
   //private static final double[] CHANNELS_WIDTH = {8.4, 3.1, 3.1, 3.1, 1.6, 1.6, 1.6, 1.6, 1.6, 1.6, 1.6, 1.6, 3.1, 3.1, 3.1, 8.4};
    private static final double[] CHANNELS_WIDTH = { 12.5, 4.0, 4.0, 4.0, 2.0, 2.0, 2.0, 2.0, 2.0, 2.0, 2.0, 2.0, 4.0, 4.0, 4.0, 12.5 };

   private Degrader degrader;
   private double positionBeforeInsert;

   public DegraderLegacyBeamProfileMonitor(String n, Degrader d)
   {
      super(n);
      degrader = d;
   }

   protected DegraderLegacyBeamProfileMonitor(DegraderLegacyBeamProfileMonitor dbpm)
   {
      super(dbpm);
      positionBeforeInsert = dbpm.positionBeforeInsert;
      degrader = dbpm.degrader;
   }

   public DegraderLegacyBeamProfileMonitor copy()
   {
      return new DegraderLegacyBeamProfileMonitor(this);
   }

   public Degrader getDegrader()
   {
      return degrader;
   }

   public void setDegrader(Degrader d)
   {
      degrader = d;
   }

   public double getChannelCenter(int c)
   {
      return CHANNELS_CENTERS[c];
   }

   public double getChannelWidth(int c)
   {
      return CHANNELS_WIDTH[c];
   }

   // Insert the BPM
   @Override
   public void insert() throws EcubtcuException
   {
      Logger.getLogger().debug("  -> Request to insert Degrader BPM " + getName());
      if (degrader != null)
      {
         positionBeforeInsert = degrader.getMotorStepSetpoint();
         degrader.gotoBpm();
      }
      else
      {
         Logger.getLogger().warn("No degrader defined associated to " + getName());
      }
   }

   // Extract the BPM
   @Override
   public void retract() throws EcubtcuException
   {
      Logger.getLogger().debug("  -> Request to retract Degrader BPM " + getName());
      if (degrader != null)
      {
         degrader.setMotorStep((int) positionBeforeInsert);
      }
      else
      {
         Logger.getLogger().warn("No degrader defined associated to " + getName());
      }
   }

}
