package com.iba.ialign;

import com.iba.ialign.common.IbaColors;
import org.apache.log4j.Logger;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.labels.StandardXYItemLabelGenerator;
import org.jfree.chart.labels.XYItemLabelGenerator;
import org.jfree.chart.plot.FastScatterPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.category.ScatterRenderer;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.general.Dataset;
import org.jfree.data.time.DynamicTimeSeriesCollection;
import org.jfree.data.time.Second;
import org.jfree.data.time.ohlc.OHLCSeriesCollection;
import org.jfree.data.xy.OHLCDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.text.NumberFormat;

public class MainCoilSweep extends JPanel {

    private static Logger log = Logger.getLogger(new Throwable().getStackTrace()[0].getClassName());

    //private OHLCDataset dataset;
    private XYSeries series = new XYSeries("series");
    //private XYDataset dataset;
    //private final DynamicTimeSeriesCollection dataset;
    final XYSeriesCollection dataset = new XYSeriesCollection();
    //private Dataset dataset;
    private JButton startSweep;
    private ChartPanel chartPanel;
    //private XYPlot plot;
    private NumberAxis mainCoilAxis;
    private NumberAxis outputAxis;
    private XYPlot plot;
    private JFreeChart chart;
    private float[][] data = new float[][]{{},{}};
    final private XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
    public int count = 0;


    public MainCoilSweep() {

        //dataset = new DynamicTimeSeriesCollection(1, 1000, new Second());
        //dataset.setTimeBase(new Second(20, 0, 0, 1, 1, 2019));
        //dataset.addSeries(new float[1], 0, "");

        dataset.addSeries(series);

        //JFreeChart chart = ChartFactory.createCandlestickChart(null, null, null, dataset, false);
        //chart = ChartFactory.createScatterPlot(null, "MC setpoint(A)", "output(nA)", XYdataset);
        chart = ChartFactory.createXYLineChart(null, "MC setpoint(A)", "S1E output(nA)", dataset, PlotOrientation.VERTICAL, false, true, false);
        chart.setBackgroundPaint(IbaColors.BT_GRAY);

        chartPanel = new ChartPanel(chart);


        //chartPanel.setLayout(new GridBagLayout());

        GridBagLayout gridBagLayout = new GridBagLayout();
        gridBagLayout.columnWidths = new int[]{20, 0, 283, 26, 353, -187, 0, 20, 0};
        gridBagLayout.rowHeights = new int[]{20, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 20, 0};
        gridBagLayout.columnWeights = new double[]{0.0D, 0.0D, 1.0D, 0.0D, 1.0D, 1.0D, 0.0D, 0.0D, 4.9E-324D};
        gridBagLayout.rowWeights = new double[]{0.0D, 1.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 4.9E-324D};
        //chartPanel.setLayout(gridBagLayout);


        this.setLayout(new GridBagLayout());

        plot = chart.getXYPlot();
        //plot.setWeight(1);

        plot.setRenderer(renderer);

        mainCoilAxis = (NumberAxis) plot.getDomainAxis();
        mainCoilAxis.setLowerBound(744.4);
        mainCoilAxis.setUpperBound(744.6);
        NumberTickUnit domainTick = new NumberTickUnit(0.1D);
        mainCoilAxis.setTickUnit(domainTick);
        outputAxis = (NumberAxis) plot. getRangeAxis();
        outputAxis.setLowerBound(0.0);
        outputAxis.setUpperBound(500.0);
        NumberTickUnit rangeTick = new NumberTickUnit(100.0D);
        outputAxis.setTickUnit(rangeTick);


        JPanel blankPanelTop = new JPanel();

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = 3;
        gbc.gridheight = 1;
        gbc.insets = new Insets(0, 0, 0, 0);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weighty = 1;
        gbc.weightx = 1;

        this.add(blankPanelTop, gbc);

        JPanel blankPanelLeft = new JPanel();

        gbc = new GridBagConstraints();
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.insets = new Insets(0, 0, 0, 0);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weighty = 1;
        gbc.weightx = 1;

        this.add(blankPanelLeft, gbc);

        JPanel blankPanelRight = new JPanel();

        gbc = new GridBagConstraints();
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.insets = new Insets(0, 0, 0, 0);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.gridx = 2;
        gbc.gridy = 1;
        gbc.weighty = 1;
        gbc.weightx = 1;

        this.add(blankPanelRight, gbc);

        gbc = new GridBagConstraints();
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.insets = new Insets(0, 0, 0, 0);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weightx = 1;
        gbc.weighty = 1;

        this.add(chartPanel, gbc);

        startSweep = new JButton("Start MC LUT");
        startSweep.setActionCommand("sweep");
        startSweep.addActionListener(this::actionPerformed);

        gbc = new GridBagConstraints();
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.insets = new Insets(0, 50, 0, 0);
        //gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.weightx = 1;
        gbc.weighty = 1;

        this.add(startSweep, gbc);


    }

    public void actionPerformed(ActionEvent e) {

        switch (e.getActionCommand()) {
            case "sweep":

                count++;

                SwingUtilities.invokeLater(
                        new Runnable(){
                            public void run(){
                                try {
                                    startSweep.setText("Completed " + count + " times");

                                    series.addOrUpdate(744.5, 350.0 + (double)count);

                                    chart.fireChartChanged();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });


//                NumberFormat format = NumberFormat.getNumberInstance();
//                format.setMaximumFractionDigits(0);
//                XYItemLabelGenerator generator = new StandardXYItemLabelGenerator(StandardXYItemLabelGenerator.DEFAULT_ITEM_LABEL_FORMAT, format, format);
//
//                renderer.setBaseShapesVisible(false);
//                renderer.setBaseShapesFilled(true);
//                renderer.setSeriesStroke(0, new BasicStroke(1.5f));
//                renderer.setSeriesItemLabelsVisible(0, false);
//                renderer.setBaseItemLabelGenerator(generator);
//                renderer.setBaseItemLabelFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 9));
//                renderer.setSeriesPaint(0, Color.black);
//
//                plot.setRenderer(renderer);
//
//                mainCoilAxis = (NumberAxis) plot.getDomainAxis();
//                mainCoilAxis.setLowerBound(744.4);
//                mainCoilAxis.setUpperBound(744.6);
//                NumberTickUnit domainTick = new NumberTickUnit(0.1D);
//                mainCoilAxis.setTickUnit(domainTick);
//                outputAxis = (NumberAxis) plot. getRangeAxis();
//                outputAxis.setLowerBound(0.0);
//                outputAxis.setUpperBound(500.0);
//                NumberTickUnit rangeTick = new NumberTickUnit(100.0D);
//                outputAxis.setTickUnit(rangeTick);

                chart.fireChartChanged();

                //plot.setDataset(dataset);

                break;
            default:
                // Do nothing
        }
    }
}

