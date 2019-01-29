// ///////////////////////////////////////////////////////////////////
// Copyright (c) by Ion Beam Applications S.A.
// All rights reserved
//
// Rue du cyclotron, 3
// B-1348 Louvain-la-Neuve
// Tel : +32.10.47.58.11
// ///////////////////////////////////////////////////////////////////

package com.iba.ialign.common;

public class Gaussian
{

   public static final int NB_PARAM = 3;

   private final double amplitude;
   private final double centroid;
   private final double sigma;

   // Create a gaussian of equation y = A . exp( (x-B)^2 / 2.C^2)
   public Gaussian(double a, double c, double s)
   {
      amplitude = a;
      centroid = c;
      sigma = s;
   }

   public Gaussian(double[] param)
   {
      amplitude = param[0];
      centroid = param[1];
      sigma = param[2];
   }

   protected Gaussian(Gaussian g)
   {
      amplitude = g.amplitude;
      centroid = g.centroid;
      sigma = g.sigma;
   }

   public Gaussian Clone()
   {
      return new Gaussian(this);
   }

   public double getAmplitude()
   {
      return amplitude;
   }

   public double getCentroid()
   {
      return centroid;
   }

   public double getSigma()
   {
      return sigma;
   }

   public double[] getVector()
   {
      double[] v = new double[3];

      v[0] = amplitude;
      v[1] = centroid;
      v[2] = sigma;
      return v;
   }

   // Computes the value of the gaussian at specified value
   public double getValueAt(double x)
   {
      if (sigma == 0)
      {
         return x == centroid ? amplitude : 0.0;
      }
      else
      {
         double xMinusCentroid = x - centroid;
         double denominator = 2.0 * sigma * sigma;
         return amplitude * Math.exp(-xMinusCentroid * xMinusCentroid / denominator);
      }
   }

   // Computes the value of the partial derivative of the gaussian at specified value
   public double[] getPartialAt(double x)
   {
      double[] v = new double[3];
      double sigmaSquare = sigma * sigma;

      // Computes partial derivative for the amplitude
      if (sigma == 0)
      {
         v[0] = 0;
      }
      else
      {
         double nominator = -Math.pow(x - centroid, 2);
         double denominator = 2 * sigmaSquare;
         v[0] = Math.exp(nominator / denominator);
      }

      // Computes partial derivative for centroid and sigma
      if (sigma == 0)
      {
         // The derivative is a very large
         v[1] = Double.MAX_VALUE;
         v[2] = Double.MAX_VALUE;
      }
      else
      {
         double y = getValueAt(x);
         v[1] = y * (x - centroid) / sigmaSquare;
         v[2] = y * Math.pow(x - centroid, 2) / (sigmaSquare * sigma);
      }

      return v;
   }

   /*
    * double [] MaxAndSigma(double [] x, double [] y) { int length; double [] v = new double[2]; double
    * maxIndex = 0; double maxValue = 0;
    * 
    * length = Math.min(x.length, y.length);
    * 
    * // Find position of the maximum value for (int i = 0; i < length; ++i) { if (y[i] > maxValue) { maxIndex
    * = i; maxValue = y[i]; } }
    * 
    * v[0] = maxValue; v[1] = maxIndex;
    * 
    * return v; }
    */

   public double[][] inverseMatrix(double[][] matrix, int order) throws ArithmeticException
   {
      int[] ik = new int[NB_PARAM];
      int[] jk = new int[NB_PARAM];
      int i, j, k, l;

      for (k = 0; k < order; ++k)
      {
         /* Find largest element in matrix(i,j) */

         double amax = 0.0;
         for (i = k; i < order; ++i)
         {
            for (j = k; j < order; ++j)
            {
               if (Math.abs(amax) <= Math.abs(matrix[i][j]))
               {
                  amax = matrix[i][j];
                  ik[k] = i;
                  jk[k] = j;
               }
            }
         }
         /* Interchange rows and columns to put amax in matrix(k,k) */

         if (amax == 0.0)
         {
            throw new ArithmeticException("GAUSSIAN_FIT_MATRIX_INVERSION_ERROR");
         }

         i = ik[k];
         if (i < k)
         {
            throw new ArithmeticException("GAUSSIAN_FIT_MATRIX_INVERSION_ERROR");
         }

         else if (i > k)
         {
            for (j = 0; j < order; ++j)
            {
               double save = matrix[k][j];
               matrix[k][j] = matrix[i][j];
               matrix[i][j] = -save;
            }
         }

         j = jk[k];
         if (j < k)
         {
            throw new ArithmeticException("GAUSSIAN_FIT_MATRIX_INVERSION_ERROR");
         }

         else if (j > k)
         {
            for (i = 0; i < order; ++i)
            {
               double save = matrix[i][k];
               matrix[i][k] = matrix[i][j];
               matrix[i][j] = -save;
            }
         }

         /* Accumulate elements of inverse matrix */

         for (i = 0; i < order; ++i)
         {
            if (i != k)
            {
               matrix[i][k] = -matrix[i][k] / amax;
            }
         }

         for (i = 0; i < order; ++i)
         {
            for (j = 0; j < order; ++j)
            {
               if (i != k && j != k)
               {
                  matrix[i][j] += matrix[i][k] * matrix[k][j];
               }
            }
         }

         for (j = 0; j < order; ++j)
         {
            if (j != k)
            {
               matrix[k][j] /= amax;
            }
         }

         matrix[k][k] = 1.0 / amax;
      }

      /* Restore ordering of matrix */

      for (k = order - 1, l = 0; l < order; --k, ++l)
      {
         j = ik[k];
         if (j > k)
         {
            for (i = 0; i < order; ++i)
            {
               double save = matrix[i][k];
               matrix[i][k] = -matrix[i][j];
               matrix[i][j] = save;
            }
         }

         i = jk[k];
         if (i > k)
         {
            for (j = 0; j < order; ++j)
            {
               double save = matrix[k][j];
               matrix[k][j] = -matrix[i][j];
               matrix[i][j] = save;
            }
         }
      }

      return matrix;
   }

}
