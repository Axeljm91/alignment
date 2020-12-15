package com.iba.ialign;

import com.iba.ialign.common.IbaColors;
import com.iba.icompx.ui.util.Colors;
import javafx.scene.text.TextAlignment;
import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.XYTitleAnnotation;
import org.jfree.chart.axis.*;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.labels.StandardXYItemLabelGenerator;
import org.jfree.chart.labels.XYItemLabelGenerator;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.time.DynamicTimeSeriesCollection;
import org.jfree.data.time.Second;
import org.jfree.ui.RectangleAnchor;
import org.jfree.ui.RectangleEdge;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.TimerTask;

public class S1EvsICC extends JPanel {

    private static Logger log = Logger.getLogger(new Throwable().getStackTrace()[0].getClassName());

    private Controller controller;
    private Gui gui;
    private JTextField requestedCurrent;
    private JPanel outputPanel;
    private JLabel outputVariance = new JLabel();
    private JLabel outputLabel = new JLabel();
    private JLabel bcreuLabel = new JLabel();
    private JButton mainCoilButton;
    private JButton cancelButton;
    private int width = 0;
    private int height = 0;
    public DynamicTimeSeriesChart beamChart;
    private ChartPanel chartPanel;
    private XYPlot plot;
    private ValueAxis valueAxis;
    public LinkedList<Float> list = null;
    public LinkedList<Float> list1 = null;
    public LinkedList<Float> list2 = null;
    public LinkedList<Float> beamCurrent = null;
    public List<Integer> ints =  new ArrayList<Integer>();
    final StandardXYItemRenderer renderer = new StandardXYItemRenderer();
    final StandardXYItemRenderer renderer2 = new StandardXYItemRenderer();
    public Timer timer;
    public double lastBcreuVal;
    public boolean readCurrent2 = false;


    public S1EvsICC(Gui gui) {

        this.gui = gui;

        GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        width = gd.getDisplayMode().getWidth();
        height = gd.getDisplayMode().getHeight();

        int oneHundredFiftyPixels = width * (125 / 16) / 100;
        int oneHundredPixels = width * (125 / 24) / 100;
        int seventyFivePixels = width * (125 / 32) / 100;
        int fiftyPixels = width * (125 / 48) / 100;

        this.setLayout(new GridBagLayout());

        mainCoilButton = new JButton("Start");
        mainCoilButton.setActionCommand("output");
        mainCoilButton.addActionListener(this::actionPerformed);

        cancelButton = new JButton("Cancel");
        cancelButton.setActionCommand("cancel");
        cancelButton.addActionListener(this::actionPerformed);

        outputPanel = new JPanel();
        outputPanel.setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
//        c.gridx = 0;
//        c.gridy = 0;
//        c.weighty = 0.0D;
//        c.weightx = 0.0D;
//        c.insets = new Insets(15, 0, 0, 0);
//        c.anchor = 11;
//        outputVariance.setFont(new Font("Dialog", 1, 12));
//        outputVariance.setText("Variance: ");
//        outputPanel.add(this.outputVariance, c);
//        c = new GridBagConstraints();
        c.insets = new Insets(0, 20, 20, 20);
        outputLabel.setFont(new Font("Dialog", Font.BOLD, 30));
        TitledBorder title = new TitledBorder("nA");
        title.setTitleJustification(3);
        title.setBorder(BorderFactory.createMatteBorder(1,1,1,1, Color.BLACK));
        Border margin = new EmptyBorder(10, 20, 10, 20);
        outputLabel.setBorder(new CompoundBorder(title, margin));
        outputLabel.setHorizontalTextPosition(0);
        outputLabel.setHorizontalAlignment(0);
        outputLabel.setText("S1E:  ...");
        c.anchor = 10;
        c.fill = GridBagConstraints.BOTH;
        c.ipady = 20;
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1.0D;
        c.gridwidth = 1;
        c.weighty = 0.5D;
        c.gridheight = 1;
        outputPanel.add(this.outputLabel, c);


        c = new GridBagConstraints();
        c.insets = new Insets(0, 20, 20, 20);
        bcreuLabel.setFont(new Font("Dialog", Font.BOLD, 30));
        title = new TitledBorder("nA");
        title.setTitleJustification(3);
        title.setBorder(BorderFactory.createMatteBorder(1,1,1,1, Color.BLACK));
        margin = new EmptyBorder(10, 20, 10, 20);
        bcreuLabel.setBorder(new CompoundBorder(title, margin));
        bcreuLabel.setHorizontalTextPosition(0);
        bcreuLabel.setHorizontalAlignment(0);
        bcreuLabel.setText("ICC:  ...");
        c.anchor = 10;
        c.fill = GridBagConstraints.BOTH;
        c.ipady = 20;
        c.gridx = 2;
        c.gridy = 0;
        c.weightx = 1.0D;
        c.gridwidth = 1;
        c.weighty = 0.5D;
        c.gridheight = 1;
        outputPanel.add(this.bcreuLabel, c);



        c.fill = GridBagConstraints.NONE;
        c.gridx = 1;
        c.gridy = 0;
        c.weightx = 0.5D;
        c.weighty = 0.0D;
        c.ipadx = 30;
        c.insets = new Insets(fiftyPixels, 0, 0, oneHundredPixels);
        outputPanel.add(this.mainCoilButton, c);
        c.ipadx = 20;
        c.insets = new Insets(fiftyPixels, oneHundredPixels, 0, 0);
        outputPanel.add(this.cancelButton, c);

        c.ipadx = 0;
        c.gridx = 1;
        c.insets = new Insets(0, 0, oneHundredPixels, 0);
        requestedCurrent = new JTextField("0.0");
        requestedCurrent.setBorder(new TitledBorder("Regulated Beam Current(nA)"));
        Font font = new Font("Times New Roman", Font.BOLD, 20);
        requestedCurrent.setFont(font);
        //requestedCurrent.setBackground(IbaColors.BT_GRAY);
        Dimension dimension = new Dimension(190, 50);
        requestedCurrent.setPreferredSize(dimension);
        requestedCurrent.setHorizontalAlignment(0);


        outputPanel.add(requestedCurrent, c);


        c.ipadx = 0;
        c.gridx = 1;
        c.gridy = 0;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 1;
        c.weighty = 0.5;
        c.fill = GridBagConstraints.BOTH;
        c.anchor = GridBagConstraints.CENTER;
        //c.insets    = new Insets(75,75,75,75);
        //c.insets = new Insets(seventyFivePixels, seventyFivePixels, 0, seventyFivePixels);
        c.insets = new Insets(seventyFivePixels, oneHundredFiftyPixels, fiftyPixels, oneHundredFiftyPixels);

        title = new TitledBorder("S1E vs ICC");
        title.setTitleJustification(TitledBorder.CENTER);
        title.setTitleFont(new Font("Dialog", Font.BOLD, 20));
        title.setBorder(BorderFactory.createMatteBorder(1,1,1,1, Color.BLACK));
        outputPanel.setBorder(title);

        this.add(outputPanel, c);


        /// Graph //////


        beamChart = new DynamicTimeSeriesChart("ICCyclo");

        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth = 3;
        c.gridheight = 3;
        c.weightx = 1;
        c.weighty = 2;
        c.fill = GridBagConstraints.BOTH;
        c.anchor = GridBagConstraints.CENTER;


        //c.insets    = new Insets(0,150,75,150);
        c.insets = new Insets(0, oneHundredPixels + 10, fiftyPixels / 10, oneHundredPixels - 5);

        //title = new TitledBorder("Instructions");
        //title.setTitleJustification(TitledBorder.CENTER);
        //title.setTitleFont(new Font("Dialog", Font.BOLD, 20));
        //tuningInstructionPanel.setBorder(title);

        this.add(beamChart, c);


        timer = new Timer(500, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                try {
                    lastBcreuVal = gui.controller.bcreu.getIcCyclo();
                    gui.curr = gui.getDegraderCurrent();

                    bcreuLabel.setText("ICC: " + String.format("%.2f", lastBcreuVal));
                    outputLabel.setText("S1E: " + String.format("%.2f", gui.curr.getAsFloat()));

                    beamChart.update(lastBcreuVal);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
        timer.setRepeats(true);

    }

    public class DynamicTimeSeriesChart extends JPanel {

        private final DynamicTimeSeriesCollection dataset;
        private final DynamicTimeSeriesCollection dataset2;
        private JFreeChart chart;
        private NumberAxis axis2;
        private NumberAxis numberAxis;
        private DateAxis axis;
        private JTextField tfMinY;
        private JTextField tfMaxY;
        private JLabel lblNewLabel;
        private JLabel lblNewLabel_1;
        //private LinkedList<Float> list;


        public DynamicTimeSeriesChart(final String title) {
            dataset = new DynamicTimeSeriesCollection(1, 1000, new Second());
            dataset.setTimeBase(new Second(20, 0, 0, 1, 1, 2019));
            dataset.addSeries(new float[1], 0, title);
            dataset2 = new DynamicTimeSeriesCollection(1, 1000, new Second());
            dataset2.setTimeBase(new Second(20, 0, 0, 1, 1, 2019));
            dataset2.addSeries(new float[1], 0, "S1E");
            chart = ChartFactory.createTimeSeriesChart(
                    null, null, null, dataset, false, true, false);
            plot = chart.getXYPlot();
            chart.setBackgroundPaint(IbaColors.BT_GRAY);
            plot.setBackgroundPaint(IbaColors.LT_GRAY);
            axis = (DateAxis) plot.getDomainAxis();
            axis.setAutoRange(true);
            axis.setFixedAutoRange(15000);
            axis.setTickUnit(new DateTickUnit(DateTickUnitType.SECOND, 1));
            axis.setDateFormatOverride(new SimpleDateFormat("ss"));
            numberAxis = (NumberAxis) plot.getRangeAxis();
            numberAxis.setLowerBound(0);
            numberAxis.setUpperBound(3.0D);
            NumberTickUnit rangeTick = new NumberTickUnit(1.0D);
            numberAxis.setTickUnit(rangeTick);
            numberAxis.setTickLabelFont(new Font("Dialog", Font.PLAIN, 12));
            numberAxis.setTickLabelPaint(Color.black);
            axis2 = new NumberAxis("");
            axis2.setAutoRange(true);
            axis2.setLowerBound(0);
            axis2.setUpperBound(11.0D);
            rangeTick = new NumberTickUnit(3.0D);
            axis2.setTickUnit(rangeTick);
            axis2.setTickLabelFont(new Font("Dialog", Font.PLAIN, 12));
            axis2.setTickLabelPaint(Color.blue);
            plot.setRangeAxis(0, numberAxis);
            plot.setDataset(0, dataset);
            plot.mapDatasetToRangeAxis(0, 0);

            plot.setRangeAxis(1, axis2);
            plot.setDataset(1, dataset2);
            plot.mapDatasetToRangeAxis(1, 1);

            LegendTitle lt = new LegendTitle(plot);
            lt.setItemFont(new Font("Dialog", Font.PLAIN, 12));
            lt.setBackgroundPaint(new Color(217, 222, 225, 100));
            lt.setFrame(new BlockBorder(Color.white));
            lt.setPosition(RectangleEdge.TOP);
            XYTitleAnnotation ta = new XYTitleAnnotation(0.98, 0.98, lt, RectangleAnchor.TOP_RIGHT);

            ta.setMaxWidth(0.48);
            plot.addAnnotation(ta);

            //DecimalFormat format = new DecimalFormat("###,###");

            //StandardCategoryItemLabelGenerator labelGenerator = new StandardCategoryItemLabelGenerator("{2}", format);
//            XYItemLabelGenerator labelGenerator1 = new XYItemLabelGenerator() {
//                @Override
//                public String generateLabel(XYDataset dataset, int series, int item) {
//                    return null;
//                }
//            };

            NumberFormat format = NumberFormat.getNumberInstance();
            format.setMaximumFractionDigits(0);
            XYItemLabelGenerator generator = new StandardXYItemLabelGenerator(StandardXYItemLabelGenerator.DEFAULT_ITEM_LABEL_FORMAT, format, format);

            //final StandardXYItemRenderer renderer = new StandardXYItemRenderer();
            renderer.setBaseShapesVisible(false);
            renderer.setBaseShapesFilled(true);
            renderer.setSeriesStroke(0, new BasicStroke(1.5f));
            renderer.setSeriesItemLabelsVisible(0, false);
            renderer.setBaseItemLabelGenerator(generator);
            renderer.setBaseItemLabelFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 9));
            renderer.setSeriesPaint(0, Color.black);
            renderer.setPlotImages(true);
            plot.setRenderer(0, renderer);

            //final StandardXYItemRenderer renderer2 = new StandardXYItemRenderer();
            renderer2.setBaseShapesVisible(false);
            renderer2.setBaseShapesFilled(true);
            renderer2.setSeriesStroke(0, new BasicStroke(1.5f));
            renderer2.setSeriesItemLabelsVisible(0, false);
            renderer2.setBaseItemLabelGenerator(generator);
            renderer2.setBaseItemLabelFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 9));
            renderer2.setBaseItemLabelPaint(Color.blue);
            renderer2.setSeriesPaint(0, Color.blue);
            renderer2.setPlotImages(true);
            plot.setRenderer(1, renderer2);

            //StandardCategoryItemLabelGenerator labelGenerator = new StandardCategoryItemLabelGenerator("{2}", format);
            //StandardCategorySeriesLabelGenerator labelGenerator = new StandardCategorySeriesLabelGenerator();
//            final StandardXYItemRenderer rendererer = (StandardXYItemRenderer) plot.getRenderer();
//            rendererer.setBaseShapesVisible(true);
//            rendererer.setBaseShapesFilled(true);
//            rendererer.setSeriesStroke(0, new BasicStroke(1.5f));
//            rendererer.setSeriesItemLabelsVisible(0, true);
//            //rendererer.setBaseItemLabelGenerator(labelGenerator);
//            rendererer.setBaseItemLabelFont(new java.awt.Font("Arial", java.awt.Font.PLAIN, 9));
//            Shape circle = new Ellipse2D.Double(-2, -2, 4, 4);
//            rendererer.setSeriesShape(0, circle);
//            //plot.getRenderer().setSeriesPaint(0, Color.decode("#0066CC"));
//
//            rendererer.setSeriesPaint(0, Color.black);
//            rendererer.setPlotImages(true);
//            plot.setRenderer(0, rendererer);


            chartPanel = new ChartPanel(chart);

            chartPanel.setLayout(new GridBagLayout());

            GridBagLayout gridBagLayout = new GridBagLayout();
            gridBagLayout.columnWidths = new int[]{20, 0, 283, 26, 353, -187, 0, 20, 0};
            gridBagLayout.rowHeights = new int[]{20, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 20, 0};
            gridBagLayout.columnWeights = new double[]{0.0D, 0.0D, 1.0D, 0.0D, 1.0D, 1.0D, 0.0D, 0.0D, 4.9E-324D};
            gridBagLayout.rowWeights = new double[]{0.0D, 1.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 4.9E-324D};
            this.setLayout(gridBagLayout);

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridwidth = 6;
            gbc.gridheight = 4;
            gbc.insets = new Insets(0, 0, 5, 5);
            gbc.fill = GridBagConstraints.BOTH;
            gbc.anchor = GridBagConstraints.CENTER;
            gbc.gridx = 0;
            gbc.gridy = 0;

            this.add(chartPanel, gbc);

            list1 = new LinkedList<>();
            list2 = new LinkedList<>();
        }

        public void update(double value) {
            float[] newData = new float[1];
            //newData[0] = (float)value;
            float[] newData2 = new float[1];
            //newData2[0] = (float)value2;
            float average = 0;

            newData[0] = (float) value;

            newData2[0] = gui.curr.getAsFloat();


            SwingUtilities.invokeLater(
                    new Runnable(){
                        public void run(){
                            try {
                                dataset.advanceTime();
                                dataset.appendData(newData);
                                dataset2.advanceTime();
                                dataset2.appendData(newData2);

                                renderer.setBaseShapesVisible(true);
                                renderer.setSeriesItemLabelsVisible(0, true);
                                renderer2.setBaseShapesVisible(true);
                                renderer2.setSeriesItemLabelsVisible(0, true);


                                list1.addLast(newData2[0]);
                                if (list1.size() > 15){
                                    list1.removeFirst();
                                }


                                if (value > 1) {
                                    list2.addLast((float) value);
                                    if (list2.size() > 15) {
                                        list2.removeFirst();
                                    }
                                }

//                                numberAxis.setLowerBound(value - 1.5D);
//                                numberAxis.setUpperBound(value + 1.5D);

//                                axis2.setLowerBound(newData2[0] - 5.0D);
//                                axis2.setUpperBound(newData2[0] + 5.0D);


                                int min = Math.round(newData2[0]);
                                int max = Math.round(newData2[0]);



                                for (int i=0; i<list1.size(); i++)
                                {
                                    if (list1.get(i) > max)
                                    {
                                        max = Math.round(list1.get(i));
                                    }
                                    if (list1.get(i) < min)
                                    {
                                        min = Math.round(list1.get(i));
                                    }
                                }

                                int range = max - min;

                                axis2.setLowerBound(newData2[0] - (newData2[0]/10));
                                axis2.setUpperBound(newData2[0] + (newData2[0]/10));
                                NumberTickUnit rangeTick = new NumberTickUnit(((max+5)-(min-5))/4);
                                rangeTick = new NumberTickUnit(2.0);
                                axis2.setTickUnit(rangeTick);



                                int min2 = (int)value;
                                int max2 = (int)value;


                                for (int i=0; i<list2.size(); i++)
                                {
                                    if (list2.get(i) > max2)
                                    {
                                        max2 = Math.round(list2.get(i));
                                    }
                                    if (list2.get(i) < min2)
                                    {
                                        min2 = Math.round(list2.get(i));
                                    }
                                }

                                min2 = (int)Math.rint(min2);
                                max2 = (int)Math.rint(max2);
                                int tick = (int)Math.rint(((max2+5)-(min2-5))/4);
                                int range2 = max - min;

                                numberAxis.setLowerBound(value - (value/10));
                                numberAxis.setUpperBound(value + (value/10));
                                rangeTick = new NumberTickUnit((double)tick);
                                rangeTick = new NumberTickUnit(2.0);
                                numberAxis.setTickUnit(rangeTick);



                                chart.fireChartChanged();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });

//            try {
//                dataset.advanceTime();
//                dataset.appendData(newData);
//                newData2[0] = beamCurrent.getFirst();
//                dataset2.advanceTime();
//                dataset2.appendData(newData2);
//                list.addLast(newData2[0]);
//                if (list.size() > 14){
//                    list.removeFirst();
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//            }

//            numberAxis.setLowerBound(value - 1.5D);
//            numberAxis.setUpperBound(value + 1.5D);
//
//            axis2.setLowerBound(newData2[0] - 5.0D);
//            axis2.setUpperBound(newData2[0] + 5.0D);

//            for (int i = 0; i < list.size(); i++) {
//                average += list.get(i);
//            }

//            axis2.setLowerBound(((double)(average/list.size()) - 5.0D));
//            axis2.setUpperBound(((double)(average/list.size()) + 5.0D));

        }
    }

    public void actionPerformed(ActionEvent e) {

        switch (e.getActionCommand()) {
            case "cancel":
                // Cancel and interrupt
                gui.idle = false;
                readCurrent2 = false;
//                gui.isRefreshing = false;
//                keepRefreshing = false;
                //log.info("[IDLE] Preparing system for treatment and idling.");
                //mainCoilButton.setEnabled(false);
                //rfLUTButton.setEnabled(false);
                //cancelButton.setEnabled(false);
                //controller.stopAlignment();
                //this.setTitle("adaPT Assist");
                //mainPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
                //mainPanel.setCursor(Cursor.getDefaultCursor());

//                controller.stopBPMs();
//
//                if (mRefreshWorker != null) {
//                    mRefreshWorker.cancel(true);
//                }

                //mainCoilButton.setEnabled(false);
                //rfLUTButton.setEnabled(false);
                //cancelButton2.setEnabled(false);

                if (gui.controller.isSystemManual()) {
//                    VDeeSP.setText("41.00");
//                    VDeeSP2.setText("41.00");

                    SwingUtilities.invokeLater(
                            new Runnable(){
                                public void run(){
                                    try {
                                        gui.controller.prepareForTreatment();

                                        try {
                                            Thread.sleep(4500);
                                        } catch (InterruptedException ex) {
                                            log.error(ex);
                                        }

                                        gui.idle = true;
                                        mainCoilButton.setEnabled(true);
                                        //rfLUTButton.setEnabled(true);
                                        cancelButton.setEnabled(true);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            });

//                   controller.prepareForTreatment();

                } else {
                    log.error("System is in automatic mode, cannot prepare for treatment.");
                }

//                try {
//                    Thread.sleep(2000);
//                } catch (InterruptedException ex) {
//                    log.error(ex);
//                }
                // AMO test to keep BCREU feedback going
                // Controller.bcreu.disconnect();
//                if (!refreshButton.isEnabled()) {
//                    refreshButton.setEnabled(true);
//                }
//                if (!applyButton.isEnabled()) {
//                    applyButton.setEnabled(true);
//                }
                break;
            case "output":
                readCurrent2 = true;
                gui.idle = false;

                log.info("[S1EvsICC] Start button has been pressed");

                mainCoilButton.setEnabled(false);
                //rfLUTButton.setEnabled(false);

                if (!gui.controller.isSystemManual()) {
                    log.error("[S1EvsICC] System is in automatic mode. Please switch to manual mode.");
                    mainCoilButton.setEnabled(true);
                    //rfLUTButton.setEnabled(true);
                    break;
                }


                SwingUtilities.invokeLater(
                        new Runnable(){
                            public void run(){
                                try {
                                    gui.controller.S1EvsICC(Double.parseDouble(requestedCurrent.getText()));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });

                Thread read = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        beamCurrent = new LinkedList<Float>();
                        if (beamCurrent != null) {
                            beamCurrent.clear();
                        }
                        while (readCurrent2) {
                            try {
                                timer.start();

                            } catch (Exception f) {
                                log.error("[S1EvsICC] Could not get degrader current from OPC server");
                                f.printStackTrace();
//                                VDeeSP.setText("41.00");
//                                VDeeSP2.setText("41.00");
                                log.info("[S1EvsICC] Cancel button has been pressed");
                                log.info("[IDLE] Preparing system for treatment and idling.");
                                gui.controller.prepareForTreatment();
                                gui.idle = true;
                                mainCoilButton.setEnabled(true);
                                //rfLUTButton.setEnabled(true);
                            }

//                            if (gui.curr.getAsDouble() > 2.50) {
//
//                                if (beamCurrent != null) {
//
//                                    if (!beamCurrent.contains(gui.curr.getAsFloat())) {
//
//                                        beamCurrent.addFirst(gui.curr.getAsFloat());
//
//
//                                        if (beamCurrent.size() >= 30) {
//
//                                            if (gui.isStable(beamCurrent)) {
//                                                outputLabel.setForeground(gui.manual);
//                                            } else {
//                                                outputLabel.setForeground(gui.automatic);
//                                            }
//
//                                            beamCurrent.removeLast();
//                                        }
//                                    }
//                                }
//                            }
                        }
                        timer.stop();
                    }
                });
                read.start();

                break;
        }
    }
}
