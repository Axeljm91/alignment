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
import java.util.List;

/** Sample for beam profile acquisition. */
public class BPMBeamProfile extends AbstractBeamProfile implements BeamProfile
{
   public BPMBeamProfile(Distribution pHorizontalCurrentValues, Distribution pVerticalCurrentValues,
         double pIntegratedCurrent)
   {
      super(pHorizontalCurrentValues, pVerticalCurrentValues, pIntegratedCurrent);
   }

   public BPMBeamProfile(Distribution pHorizontalCurrentValues, Distribution pVerticalCurrentValues)
   {
      super(pHorizontalCurrentValues, pVerticalCurrentValues);
   }

   @Override
   public double getXPosition()
   {
      return getHorizontalValues().getGaussian().getCentroid();
   }

   @Override
   public double getYPosition()
   {
      return getVerticalValues().getGaussian().getCentroid();
   }

   @Override
   public double getXSize()
   {
      return getHorizontalValues().getGaussian().getSigma();
   }

   @Override
   public double getYSize()
   {
      return getVerticalValues().getGaussian().getSigma();
   }

   public static BPMBeamProfile average(List<BeamProfile> mAcquisitions)
   {
      BPMBeamProfile profile = new BPMBeamProfile(mAcquisitions.get(0).getHorizontalValues(),
            mAcquisitions.get(0).getVerticalValues(), mAcquisitions.get(0).getIntegratedCurrent());
      for (int i = 1; i < mAcquisitions.size(); ++i)
      {
         BPMBeamProfile sample = (BPMBeamProfile) mAcquisitions.get(i);
         profile.mHorizontalCurrentValues.sum(sample.mHorizontalCurrentValues);
         profile.mVerticalCurrentValues.sum(sample.mVerticalCurrentValues);
         profile.mIntegratedCurrent += sample.mIntegratedCurrent;
      }

      for (int i = 0; i < profile.mHorizontalCurrentValues.getNbPoints(); ++i)
      {
         profile.mHorizontalCurrentValues.setY(i,
               profile.mHorizontalCurrentValues.getY(i) / mAcquisitions.size());
         profile.mVerticalCurrentValues.setY(i, profile.mVerticalCurrentValues.getY(i) / mAcquisitions.size());
         profile.mIntegratedCurrent /= mAcquisitions.size();
      }

      return profile;
   }
}
