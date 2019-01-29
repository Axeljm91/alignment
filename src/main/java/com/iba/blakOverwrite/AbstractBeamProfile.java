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
import com.iba.blak.device.api.BeamProfile;
import java.util.Arrays;

public abstract class AbstractBeamProfile implements BeamProfile
{
   protected Distribution mHorizontalCurrentValues;
   protected Distribution mVerticalCurrentValues;

   protected double mIntegratedCurrent;
   private boolean mCurrentSet = false;

   public AbstractBeamProfile(Distribution pHorizontalCurrentValues, Distribution pVerticalCurrentValues,
         double pIntegratedCurrent)
   {
      this(pHorizontalCurrentValues, pVerticalCurrentValues);
      mIntegratedCurrent = pIntegratedCurrent;
      mCurrentSet = true;
   }

   public AbstractBeamProfile(Distribution pHorizontalCurrentValues, Distribution pVerticalCurrentValues)
   {
      mHorizontalCurrentValues = new Distribution(pHorizontalCurrentValues);
      mVerticalCurrentValues = new Distribution(pVerticalCurrentValues);
   }

   public void setIntegratedCurrent(double value)
   {
      mIntegratedCurrent = value;
      mCurrentSet = true;
   }
   
   public Distribution getHorizontalValues()
   {
      return mHorizontalCurrentValues;
   }

   public Distribution getVerticalValues()
   {
      return mVerticalCurrentValues;
   }

   public double getIntegratedCurrent()
   {
      if (!mCurrentSet)
      {
         mIntegratedCurrent = 0.0;
         for (Distribution d : Arrays.asList(mHorizontalCurrentValues, mVerticalCurrentValues))
         {
            for (int i = 0; i < d.getNbPoints(); ++i)
            {
               mIntegratedCurrent += d.getY(i);
            }
         }
         mCurrentSet = true;
      }
      return mIntegratedCurrent;
   }

}
