// ///////////////////////////////////////////////////////////////////
// Copyright (c) by Ion Beam Applications S.A.
// All rights reserved
//
// Rue du cyclotron, 3
// B-1348 Louvain-la-Neuve
// Tel : +32.10.47.58.11
// ///////////////////////////////////////////////////////////////////

package com.iba.blakOverwrite;

public class LegacyBeamProfileMonitorImpl extends AbstractLegacyBeamProfileMonitor
{

   private static final double[] CHANNELS_CENTERS = { -26.3, -18, -14, -10, -7, -5, -3, -1, 1, 3, 5, 7, 10, 14, 18, 26.3 };
   private static final double[] CHANNELS_WIDTH = { 12.5, 4.0, 4.0, 4.0, 2.0, 2.0, 2.0, 2.0, 2.0, 2.0, 2.0, 2.0, 4.0, 4.0, 4.0, 12.5 };

   public LegacyBeamProfileMonitorImpl(String n)
   {
      super(n);
   }

   protected LegacyBeamProfileMonitorImpl(LegacyBeamProfileMonitorImpl bpm)
   {
      super(bpm);
   }

   public LegacyBeamProfileMonitorImpl copy()
   {
      return new LegacyBeamProfileMonitorImpl(this);
   }

   public double getChannelCenter(int c)
   {
      return CHANNELS_CENTERS[c];
   }

   public double getChannelWidth(int c)
   {
      return CHANNELS_WIDTH[c];
   }

}
