/* ===========================================================
 * JFreeChart : a free chart library for the Java(tm) platform
 * ===========================================================
 *
 * (C) Copyright 2000-2014, by Object Refinery Limited and Contributors.
 *
 * Project Info:  http://www.jfree.org/jfreechart/index.html
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301,
 * USA.
 *
 * [Oracle and Java are registered trademarks of Oracle and/or its affiliates. 
 * Other names may be trademarks of their respective owners.]
 *
 * ---------------------------------
 * NormalDistributionFunction2D.java
 * ---------------------------------
 * (C)opyright 2004-2014, by Object Refinery Limited.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   -;
 *
 * Changes
 * -------
 * 25-May-2004 : Version 1 (DG);
 * 21-Nov-2005 : Added getters for the mean and standard deviation (DG);
 * 12-Feb-2009 : Precompute some constants from the function - see bug
 *               2572016 (DG);
 * 28-May-2009 : Implemented equals() and hashCode(), and added serialization
 *               support (DG);
 *
 */

package com.iba.ialign.charts;

import org.jfree.chart.HashUtilities;
import org.jfree.data.function.Function2D;

import java.io.Serializable;

/**
 * A normal distribution function.  See
 * http://en.wikipedia.org/wiki/Normal_distribution.
 */
public class GaussianFunction2D implements Function2D, Serializable {

    /** The height. */
    private double height;

    /** The value that the function approaches. */
    private double offset;

    /** The mean. */
    private double mean;

    /** The standard deviation. */
    private double std;

    /** Precomputed denominator for the function value. */
    private double denominator;

    /**
     * Constructs a new normal distribution function.
     *
     * @param mean  the mean.
     * @param std  the standard deviation (&gt; 0).
     */
    public GaussianFunction2D(double height, double mean, double std) {
        if (std <= 0) {
            throw new IllegalArgumentException("Requires 'std' > 0.");
        }
        this.mean = mean;
        this.std = std;
        this.offset = 0;
        // calculate constant values
        this.height = height;
        this.denominator = 2 * std * std;
    }

    /**
     * Constructs a new normal distribution function.
     *
     * @param mean  the mean.
     * @param std  the standard deviation (&gt; 0).
     */
    public GaussianFunction2D(double height, double mean, double std, double offset) {
        if (std <= 0) {
            throw new IllegalArgumentException("Requires 'std' > 0.");
        }
        this.mean = mean;
        this.std = std;
        this.offset = offset;
        // calculate constant values
        this.height = height;
        this.denominator = 2 * std * std;
    }

    /**
     * Constructs a new normal distribution function.
     *
     * @param mean  the mean.
     * @param std  the standard deviation (&gt; 0).
     */
    public GaussianFunction2D(double mean, double std) {
        if (std <= 0) {
            throw new IllegalArgumentException("Requires 'std' > 0.");
        }
        this.mean = mean;
        this.std = std;
        this.offset = 0;
        // calculate constant values
        this.height = 1 / (std * Math.sqrt(2.0 * Math.PI));
        this.denominator = 2 * std * std;
    }

    /**
     * Returns the mean for the function.
     *
     * @return The mean.
     */
    public double getMean() {
        return this.mean;
    }
    
    /**
     * Returns the standard deviation for the function.
     *
     * @return The standard deviation.
     */
    public double getStandardDeviation() {
        return this.std;
    }

    /**
     * Returns the function value.
     *
     * @param x  the x-value.
     *
     * @return The value.
     */
    @Override
    public double getValue(double x) {
        double z = x - this.mean;
        return this.height * Math.exp(-z * z / this.denominator) + this.offset;
    }

    /**
     * Tests this function for equality with an arbitrary object.
     *
     * @param obj  the object (<code>null</code> permitted).
     *
     * @return A boolean.
     */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof GaussianFunction2D)) {
            return false;
        }
        GaussianFunction2D that = (GaussianFunction2D) obj;
        if (this.mean != that.mean) {
            return false;
        }
        if (this.std != that.std) {
            return false;
        }
        return true;
    }

    /**
     * Returns a hash code for this instance.
     *
     * @return A hash code.
     */
    @Override
    public int hashCode() {
        int result = 29;
        result = HashUtilities.hashCode(result, this.mean);
        result = HashUtilities.hashCode(result, this.std);
        return result;
    }

}
