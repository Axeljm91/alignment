/* ===========================================================
 * JFreeChart : a free chart library for the Java(tm) platform
 * ===========================================================
 *
 * (C) Copyright 2000-2013, by Object Refinery Limited and Contributors.
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
 * ---------------------------
 * SimpleHistogramDataset.java
 * ---------------------------
 * (C) Copyright 2005-2013, by Object Refinery Limited and Contributors.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   Sergei Ivanov;
 *
 * Changes
 * -------
 * 10-Jan-2005 : Version 1 (DG);
 * 21-May-2007 : Added clearObservations() and removeAllBins() (SI);
 * 10-Jul-2007 : Added null argument check to constructor (DG);
 * 03-Jul-2013 : Use ParamChecks (DG);
 *
 */

package com.iba.ialign.charts;

import org.jfree.chart.util.ParamChecks;
import org.jfree.data.DomainOrder;
import org.jfree.data.general.DatasetChangeEvent;
import org.jfree.data.statistics.HistogramType;
import org.jfree.data.xy.AbstractIntervalXYDataset;
import org.jfree.data.xy.IntervalXYDataset;
import org.jfree.util.ObjectUtilities;
import org.jfree.util.PublicCloneable;

import java.io.Serializable;
import java.util.*;

/**
 * A dataset used for creating simple histograms with custom defined bins.
 *
 * @see org.jfree.data.statistics.HistogramDataset
 */
public class BpmDataset extends AbstractIntervalXYDataset
        implements IntervalXYDataset, Cloneable, PublicCloneable,
            Serializable {

    private static final double[] CHANNELS_CENTERS = { -26.3, -18, -14, -10, -7, -5, -3, -1, 1, 3, 5, 7, 10,
            14, 18, 26.3 };
    private static final double[] CHANNELS_WIDTH = { 12.5, 4.0, 4.0, 4.0, 2.0, 2.0, 2.0, 2.0, 2.0, 2.0, 2.0,
            2.0, 4.0, 4.0, 4.0, 12.5 };

    /** For serialization. */
    private static final long serialVersionUID = 7997996479768018443L;

    /** The series key. */
    private Comparable key;

    /** The bins. */
    private List bins;

    /** A list of maps. */
    private List list;

    /** The histogram type. */
    private HistogramType type;

    /**
     * A flag that controls whether or not the bin count is divided by the
     * bin size.
     */
    private boolean adjustForBinSize;

    /**
     * Creates a new histogram dataset.  Note that the
     * <code>adjustForBinSize</code> flag defaults to <code>true</code>.
     *
     * @param key  the series key (<code>null</code> not permitted).
     */
    public BpmDataset(Comparable key) {
        ParamChecks.nullNotPermitted(key, "key");
        this.key = key;
        this.bins = new ArrayList();
        this.adjustForBinSize = true;
    }

    /**
     * Creates a new (empty) dataset with a default type of
     * {@link org.jfree.data.statistics.HistogramType}.FREQUENCY.
     */
    public BpmDataset() {
        this.list = new ArrayList();
        this.type = org.jfree.data.statistics.HistogramType.FREQUENCY;
    }

    /**
     * Returns a flag that controls whether or not the bin count is divided by
     * the bin size in the {@link #getXValue(int, int)} method.
     *
     * @return A boolean.
     *
     * @see #setAdjustForBinSize(boolean)
     */
    public boolean getAdjustForBinSize() {
        return this.adjustForBinSize;
    }

    /**
     * Sets the flag that controls whether or not the bin count is divided by
     * the bin size in the {@link #getYValue(int, int)} method, and sends a
     * {@link org.jfree.data.general.DatasetChangeEvent} to all registered listeners.
     *
     * @param adjust  the flag.
     *
     * @see #getAdjustForBinSize()
     */
    public void setAdjustForBinSize(boolean adjust) {
        this.adjustForBinSize = adjust;
        notifyListeners(new DatasetChangeEvent(this, this));
    }

    /**
     * Returns the order of the domain (or X) values returned by the dataset.
     *
     * @return The order (never <code>null</code>).
     */
    @Override
    public DomainOrder getDomainOrder() {
        return DomainOrder.ASCENDING;
    }

    /**
     * Adds a bin to the dataset.  An exception is thrown if the bin overlaps
     * with any existing bin in the dataset.
     *
     * @param bin  the bin (<code>null</code> not permitted).
     *
     * @see #removeAllBins()
     */
    public void addBin(BpmBin bin) {
        // check that the new bin doesn't overlap with any existing bin
        Iterator iterator = this.bins.iterator();
//        while (iterator.hasNext()) {
//            BpmBin existingBin
//                    = (BpmBin) iterator.next();
//            if (bin.overlapsWith(existingBin)) {
//                throw new RuntimeException("Overlapping bin");
//            }
//        }
        this.bins.add(bin);
        Collections.sort(this.bins);
    }

    /**
     * Adds a series to the dataset. Any data value less than minimum will be
     * assigned to the first bin, and any data value greater than maximum will
     * be assigned to the last bin.  Values falling on the boundary of
     * adjacent bins will be assigned to the higher indexed bin.
     *
     * @param key  the series key (<code>null</code> not permitted).
     * @param values  the raw observations.
     */
    public void addSeries(Comparable key, double[] values) {
        ParamChecks.nullNotPermitted(key, "key");
        ParamChecks.nullNotPermitted(values, "values");

        List binList = new ArrayList();
        for (int i = 0; i < values.length; i++) {
            BpmBin bin = new BpmBin(CHANNELS_CENTERS[i]-CHANNELS_WIDTH[i]/2,CHANNELS_CENTERS[i]+CHANNELS_WIDTH[i]/2);
            bin.setItemCount(values[i]);
            binList.add(bin);
        }
        Collections.sort(binList);

        // generic map for each series
        Map map = new HashMap();
        map.put("key", key);
        map.put("bins", binList);
        map.put("values.length", new Integer(values.length));
        this.list.add(map);
        fireDatasetChanged();
    }

    /**
     * Adds a series to the dataset. Any data value less than minimum will be
     * assigned to the first bin, and any data value greater than maximum will
     * be assigned to the last bin.  Values falling on the boundary of
     * adjacent bins will be assigned to the higher indexed bin.
     *
     * @param series  the series index (in the range <code>0</code> to
     *     <code>getSeriesCount() - 1</code>).
     * @param values  the raw observations.
     */
    public void updateSeries(int series, double[] values) {
        ParamChecks.nullNotPermitted(values, "values");

        List binList = new ArrayList();
        for (int i = 0; i < values.length; i++) {
            BpmBin bin = new BpmBin(CHANNELS_CENTERS[i]-CHANNELS_WIDTH[i]/2,CHANNELS_CENTERS[i]+CHANNELS_WIDTH[i]/2);
            bin.setItemCount(values[i]);
            binList.add(bin);
        }
        Collections.sort(binList);

        // generic map for each series
        Map map = (Map) this.list.get(series);
        map.put("bins", binList);
        map.put("values.length", new Integer(values.length));
        fireDatasetChanged();
    }

    /**
     * Removes all current observation data and sends a
     * {@link org.jfree.data.general.DatasetChangeEvent} to all registered listeners.
     *
     * @since 1.0.6
     *
     * @see #removeAllBins()
     */
    public void clearObservations() {
        Iterator iterator = this.bins.iterator();
        while (iterator.hasNext()) {
            BpmBin bin = (BpmBin) iterator.next();
            bin.setItemCount(0);
        }
        notifyListeners(new DatasetChangeEvent(this, this));
    }

    /**
     * Removes all bins and sends a {@link org.jfree.data.general.DatasetChangeEvent} to all
     * registered listeners.
     *
     * @since 1.0.6
     *
     * @see #addBin(BpmBin)
     */
    public void removeAllBins() {
        this.bins = new ArrayList();
        notifyListeners(new DatasetChangeEvent(this, this));
    }

    /**
     * Returns the bins for a series.
     *
     * @param series  the series index (in the range <code>0</code> to
     *     <code>getSeriesCount() - 1</code>).
     *
     * @return A list of bins.
     *
     * @throws IndexOutOfBoundsException if <code>series</code> is outside the
     *     specified range.
     */
    List getBins(int series) {
        Map map = (Map) this.list.get(series);
        return (List) map.get("bins");
    }

    /**
     * Returns the total number of observations for a series.
     *
     * @param series  the series index.
     *
     * @return The total.
     */
    private int getTotal(int series) {
        Map map = (Map) this.list.get(series);
        return ((Integer) map.get("values.length")).intValue();
    }

    /**
     * Returns the number of series in the dataset.
     *
     * @return The series count.
     */
    @Override
    public int getSeriesCount() {
        return this.list.size();
    }

    /**
     * Returns the key for a series.
     *
     * @param series  the series index (in the range <code>0</code> to
     *     <code>getSeriesCount() - 1</code>).
     *
     * @return The series key.
     *
     * @throws IndexOutOfBoundsException if <code>series</code> is outside the
     *     specified range.
     */
    @Override
    public Comparable getSeriesKey(int series) {
        Map map = (Map) this.list.get(series);
        return (Comparable) map.get("key");
    }

    /**
     * Returns the number of data items for a series.
     *
     * @param series  the series index (in the range <code>0</code> to
     *     <code>getSeriesCount() - 1</code>).
     *
     * @return The item count.
     *
     * @throws IndexOutOfBoundsException if <code>series</code> is outside the
     *     specified range.
     */
    @Override
    public int getItemCount(int series) {
        return getBins(series).size();
    }

    /**
     * Returns the X value for a bin.  This value won't be used for plotting
     * histograms, since the renderer will ignore it.  But other renderers can
     * use it (for example, you could use the dataset to create a line
     * chart).
     *
     * @param series  the series index (in the range <code>0</code> to
     *     <code>getSeriesCount() - 1</code>).
     * @param item  the item index (zero based).
     *
     * @return The start value.
     *
     * @throws IndexOutOfBoundsException if <code>series</code> is outside the
     *     specified range.
     */
    @Override
    public Number getX(int series, int item) {
        List bins = getBins(series);
        BpmBin bin = (BpmBin) bins.get(item);
        double x = (bin.getLowerBound() + bin.getUpperBound()) / 2.;
        return new Double(x);
    }

    /**
     * Returns the y-value for a bin (calculated to take into account the
     * histogram type).
     *
     * @param series  the series index (in the range <code>0</code> to
     *     <code>getSeriesCount() - 1</code>).
     * @param item  the item index (zero based).
     *
     * @return The y-value.
     *
     * @throws IndexOutOfBoundsException if <code>series</code> is outside the
     *     specified range.
     */
    @Override
    public Number getY(int series, int item) {
        List bins = getBins(series);
        BpmBin bin = (BpmBin) bins.get(item);
        double total = getTotal(series);

        if (this.type == HistogramType.FREQUENCY) {
            return new Double(bin.getItemCount());
        }
        else if (this.type == HistogramType.RELATIVE_FREQUENCY) {
            return new Double(bin.getItemCount() / total);
        }
        else if (this.type == HistogramType.SCALE_AREA_TO_1) {
            return new Double(bin.getItemCount() / (1 * total));
        }
        else { // pretty sure this shouldn't ever happen
            throw new IllegalStateException();
        }
    }

    /**
     * Returns the start value for a bin.
     *
     * @param series  the series index (in the range <code>0</code> to
     *     <code>getSeriesCount() - 1</code>).
     * @param item  the item index (zero based).
     *
     * @return The start value.
     *
     * @throws IndexOutOfBoundsException if <code>series</code> is outside the
     *     specified range.
     */
    @Override
    public Number getStartX(int series, int item) {
        List bins = getBins(series);
        BpmBin bin = (BpmBin) bins.get(item);
        return new Double(bin.getLowerBound());
    }

    /**
     * Returns the end value for a bin.
     *
     * @param series  the series index (in the range <code>0</code> to
     *     <code>getSeriesCount() - 1</code>).
     * @param item  the item index (zero based).
     *
     * @return The end value.
     *
     * @throws IndexOutOfBoundsException if <code>series</code> is outside the
     *     specified range.
     */
    @Override
    public Number getEndX(int series, int item) {
        List bins = getBins(series);
        BpmBin bin = (BpmBin) bins.get(item);
        return new Double(bin.getUpperBound());
    }

    /**
     * Returns the start y-value for a bin (which is the same as the y-value,
     * this method exists only to support the general form of the
     * {@link org.jfree.data.xy.IntervalXYDataset} interface).
     *
     * @param series  the series index (in the range <code>0</code> to
     *     <code>getSeriesCount() - 1</code>).
     * @param item  the item index (zero based).
     *
     * @return The y-value.
     *
     * @throws IndexOutOfBoundsException if <code>series</code> is outside the
     *     specified range.
     */
    @Override
    public Number getStartY(int series, int item) {
        return getY(series, item);
    }

    /**
     * Returns the end y-value for a bin (which is the same as the y-value,
     * this method exists only to support the general form of the
     * {@link org.jfree.data.xy.IntervalXYDataset} interface).
     *
     * @param series  the series index (in the range <code>0</code> to
     *     <code>getSeriesCount() - 1</code>).
     * @param item  the item index (zero based).
     *
     * @return The Y value.
     *
     * @throws IndexOutOfBoundsException if <code>series</code> is outside the
     *     specified range.
     */
    @Override
    public Number getEndY(int series, int item) {
        return getY(series, item);
    }

    /**
     * Tests this dataset for equality with an arbitrary object.
     *
     * @param obj  the object to test against (<code>null</code> permitted).
     *
     * @return A boolean.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof BpmDataset)) {
            return false;
        }
        BpmDataset that = (BpmDataset) obj;
        if (!ObjectUtilities.equal(this.type, that.type)) {
            return false;
        }
        if (!ObjectUtilities.equal(this.list, that.list)) {
            return false;
        }
        return true;
    }

    /**
     * Returns a clone of the dataset.
     *
     * @return A clone of the dataset.
     *
     * @throws CloneNotSupportedException if the object cannot be cloned.
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        BpmDataset clone = (BpmDataset) super.clone();
        int seriesCount = getSeriesCount();
        clone.list = new ArrayList(seriesCount);
        for (int i = 0; i < seriesCount; i++) {
            clone.list.add(new HashMap((Map) this.list.get(i)));
        }
        return clone;
    }

}
