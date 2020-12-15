/*
* Application : iAlign
* Filename : Gui.java
* Author : Fran�ois Vander Stappen
* Date : 22/10/2010
* Company : IBA
* Version : 0.4.2
*/

package com.iba.ialign;

import com.google.gson.JsonElement;
import com.iba.blak.device.api.Insertable;
import com.iba.ialign.borders.TitledBorderNoEdge;
import com.iba.icompx.core.activity.ActivityStatus;
import com.iba.icompx.ui.util.ResourceManager;
import com.iba.pts.bms.bds.common.api.SmpsControllerActivityId;
import com.iba.pts.bms.bss.beamscheduler.api.BeamAllocation;
import com.iba.pts.bms.bss.beamscheduler.api.BeamPriority;
import com.iba.pts.bms.bss.beamscheduler.api.BeamSchedulerControl;
import com.iba.pts.bms.bss.beamscheduler.api.PendingBeamRequest;
import com.iba.pts.bms.bss.beamscheduler.ui.BeamSchedulerPanel;
import com.iba.pts.bms.bss.controller.api.BssActivityId;

import java.net.ConnectException;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import com.iba.ialign.charts.BpmDataset;
import com.iba.ialign.charts.GaussianDataset;
import com.iba.ialign.common.IbaColors;
import com.iba.ialign.common.TaskListDialog;

import com.iba.blak.common.Distribution;

import com.iba.pts.bms.tcrServiceScreens.TcuVceuPanel;
import com.iba.pts.treatmentroomsession.TreatmentRoomSession;
import com.opcOpenInterface.Rest;
import org.apache.commons.lang.time.DurationFormatUtils;
import org.apache.log4j.Logger;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.XYTitleAnnotation;
import org.jfree.chart.axis.*;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.labels.*;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.*;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.time.*;
import org.jfree.ui.RectangleAnchor;
import org.jfree.ui.RectangleEdge;
import org.python.google.common.collect.ImmutableSet;
import org.sikuli.basics.Debug;
import org.sikuli.script.ImagePath;
import org.sikuli.script.Screen;


import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
//import java.util.Timer;
import javax.annotation.Resource;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;

import org.jfree.data.xy.XYDataset;

import javax.swing.*;
import javax.swing.Timer;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

import java.awt.Dimension;
import java.util.List;


import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class Gui extends JFrame implements ActionListener, ItemListener {

    /**
     * The logger. See http://logging.apache.org/log4j/1.2/index.html for more information.
     */
    private static Logger log = Logger.getLogger(new Throwable().getStackTrace()[0].getClassName());
    public static boolean readyForServiceBeam;

    public Controller controller;
    private ConstantWorker mTask;
    private ConstantWorker2 mTask2;
    private BcreuWorker mBcreuProxy;
    private SetpointWorker mSetpointWorker;
    private RefreshWorker mRefreshWorker = null;
    private static int IPADY_DEFAULT = 10;

//    @Resource(name="blpscuReaderChecksumAnalogInputMap")
//    private Map<PropertyInfo, PLCAddress> blpscuReaderChecksumAnalogInputMap;

    @Resource
    private Properties blpscuReaderChecksumAnalogInputMap;

    @Resource
    private Properties blpscuAnalogOutputMap;

    // Decimal format with 3 decimal points and US locale.
    final private DecimalFormat mDecFormat = new DecimalFormat("#.###", (new DecimalFormatSymbols(Locale.US)));
    private double[] mTolerances, mSigmaTargets, mSigmaTolerances;
    private LogPanel logPanel;
    private JTabbedPane mTabs, mTabs2;
    public static EsbtsPanel mEsbtsPanel;
    public static BeamSchedulerPanel beamSchedulerPanel;
    public TcuVceuPanel VceuPanel;
    public JPanel mSchedulerPanel;
    public static JPanel mBeamTimer;
    public static File beamResults = new File("");
    public static JButton startBeamButton;
    public static JCheckBox timedRunCB;
    public static JTextField timedRunText;
    public static JComboBox postRunAction;
    public ScanningController mScanningControllerPanel;
    public JPanel mExtractionPanel, mMainCoilPanel, mVNCPanel, mRestorePanel, mSourcePanel, mAutomaticPanel, tuningPanel, MCTuningPanel, outputPanel, interlockPanel, interlockPanel_L, interlockPanel_R;
    public JPanel powerSavePanel, powerSaveTR1, powerSaveTR2, powerSaveTR3, powerSaveTR4, powerSaveInterlocks, mainCoilSweepPanel, iAlignPanel, ICCPanel;
    public JPanel mainPanel, displayPanel, blePanel, ble2Panel, bcreuPanel, cycloPanel, magnetPanel, dfPanel, VNCDisplayPanel, testPanel, magnetTuningPanel, sourceTuningPanel, LLRFTuningPanel, tuningInstructionPanel;
    private JButton refreshButton, computeButton, cancelButton, cancelButton2, applyButton, mainCoilButton, PCVueButton, cancelTuneButton, alignMeButton, saveMeButton, burnInLockButton, rfLUTButton,
            Dee1plus, Dee1minus, Dee2plus, Dee2minus, Filplus, Filminus, Arcplus, Arcminus, CCoilplus, CCoilminus, MCoilplus, MCoilminus, MCoilStep1, MCoilStep2, MCoilStep3, HCoil1plus, HCoil1minus, HCoil2plus, HCoil2minus;
    private BpmDataset mBpmXdata, mBpmYdata;
    private GaussianDataset mGaussXdata, mGaussYdata;
    private JFreeChart mBpmChartX, mBpmChartY;
    private TaskListDialog mPrepDialog = null;
    private JTextField positionText[], targetText[], oldCurrText[], newCurrText[], oldCurrText2[], newCurrText2[], diffText[], toleranceText[],
            sigmaText[], statusText[], statusText2[], bcreuText[], cycloText[], MCTuningText[], VDeeFB, VDeeSP, VDeeSP2, FilFB, FilSP, ArcFB, ArcSP, ArcVolt,
            CCoilFB, CCoilSP, MCoilFB, MCoilSP, HCoil1FB, HCoil1SP, HCoil2FB, HCoil2SP;
    private Screen screen = new Screen();
    private static Rest restManager = new Rest();
    private JLabel operModeLabel = new JLabel();
    private JLabel operModeLabel2 = new JLabel();
    private JLabel operModeLabel3 = new JLabel();
    private JLabel outputLabel = new JLabel();
    public JLabel LLRFLabel = new JLabel();
    public JLabel SourceLabel = new JLabel();
    public JLabel MagnetLabel = new JLabel();
    public JLabel outputVariance = new JLabel();
    public JLabel instructionLabel = new JLabel();
    public JLabel powerSaveLabel, powerSaveLabelTR1, powerSaveLabelTR2, powerSaveLabelTR3, powerSaveLabelTR4, powerSaveInterlockLabel;
    public JLabel allocatedLabelTR1, allocatedLabelTR2, allocatedLabelTR3, allocatedLabelTR4, queuedLabelTR1, queuedLabelTR2, queuedLabelTR3, queuedLabelTR4;
    public JLabel searchingLabelTR1, searchingLabelTR2, searchingLabelTR3, searchingLabelTR4, secureLabelTR1, secureLabelTR2, secureLabelTR3, secureLabelTR4;
    public JCheckBox MCTuning = new JCheckBox();
    public JCheckBox SourTuning = new JCheckBox();
    public JCheckBox PowerSave, psInterlock1, psInterlock2, psInterlock3, psInterlock4, psInterlock5, psInterlock6;
    public boolean readCurrent = false;
    public boolean isRefreshing = false;
    public boolean keepRefreshing = false;
    public boolean isAutomaticMode = false;
    public boolean isBeamline4Selected = false;
    public boolean isXrayTubeRetracted = false;
    public boolean isTR3SearchedInServiceMode = false;
    public SmpsControllerActivityId mScanMagActivity = null;
    public static boolean isADLogout = false;
    public int counter = 0;
    public JsonElement curr;
    public JsonElement var;
    private double setpoint = 0;
    private double setpointFeedback;
    private double setpointOffset;
    private JsonElement setpointraw;
    private JsonElement setpointFeedbackraw;
    public Color manual = Color.decode("#144f07");
    public Color automatic = Color.decode("#730605");
    public Color interlocked = Color.decode("0xff8c00");
    public LinkedList<Float> beamCurrent = null;
    private volatile int status = 1;
    private double changePerClick = 0.005;
    private boolean updated = false;
    public boolean idle = true;
    private int width = 0;
    private int height = 0;
    private boolean requestWarning = false;
    private boolean searchWarning = false;
    private boolean locked = true;
    private int minFilSP = 125;
    private int maxFilSP = 200;
    private int minArcSP = 0;
    private int maxArcSP = 125;
    private int minDuration = 0;
    private int maxDuration = 20;

    private double val = 0;
    private boolean printBeamline1 = true;
    private boolean printBeamline4 = true;

    private BssActivityId mCurrentActivityName;
    private ActivityStatus mCurrentActivityStatus;

    private TimeSeriesCollection dataset;
    private JComboBox comboBox;
    // private Rest restManager = new Rest();
    private JTextField tfMinY;
    private JTextField tfMaxY;
    private JLabel lblNewLabel;
    private JLabel lblNewLabel_1;
    private JFreeChart chart;
    private DynamicTimeSeriesChart beamChart;
    private ChartPanel chartPanel;
    private XYPlot plot;
    private ValueAxis valueAxis;
    public LinkedList<Float> list = null;
    public LinkedList<Float> list1 = null;
    public LinkedList<Float> list2 = null;
    public List<Integer> ints =  new ArrayList<Integer>();
    final StandardXYItemRenderer renderer = new StandardXYItemRenderer();
    final StandardXYItemRenderer renderer2 = new StandardXYItemRenderer();

    public static JLabel rfSparkDetected = new JLabel("RF OK", SwingConstants.CENTER);


    public Gui(Controller controller) throws IOException {

        this.setExtendedState(JFrame.MAXIMIZED_BOTH);
        this.controller = controller;

        UIManager.put("JCheckBox.disabledForeground", Color.BLACK);
        UIManager.put("ToolTip.font",
                new Font("SansSerif", Font.BOLD, 16));


        GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        width = gd.getDisplayMode().getWidth();
        height = gd.getDisplayMode().getHeight();

        int oneHundredFiftyPixels = width * (125 / 16) / 100;
        int oneHundredPixels = width * (125 / 24) / 100;
        int seventyFivePixels = width * (125 / 32) / 100;
        int fiftyPixels = width * (125 / 48) / 100;

        Dimension fullscreen = new Dimension(width, height);

        Font bigFont = new Font(Font.DIALOG, Font.BOLD, 16);
        Font bigTitle = new Font(Font.DIALOG, Font.BOLD, 14);

        readyForServiceBeam = false;

        logPanel = new LogPanel();
        mainPanel = new JPanel();
        mainPanel.setLayout(new GridBagLayout());
        mainPanel.setBackground(IbaColors.BT_GRAY);

        mSourcePanel = new TimerPanel();
        mSourcePanel.setBackground(IbaColors.BT_GRAY);

        tuningPanel = new JPanel();
        tuningPanel.setLayout(new GridBagLayout());
        tuningPanel.setBackground(IbaColors.BT_GRAY);

        powerSavePanel = new JPanel();
        powerSavePanel.setLayout(new GridBagLayout());
        powerSavePanel.setBackground(IbaColors.BT_GRAY);

        powerSaveTR1 = new JPanel();
        powerSaveTR1.setLayout(new GridBagLayout());
        powerSaveTR1.setBackground(IbaColors.BT_GRAY);

        powerSaveTR2 = new JPanel();
        powerSaveTR2.setLayout(new GridBagLayout());
        powerSaveTR2.setBackground(IbaColors.BT_GRAY);

        powerSaveTR3 = new JPanel();
        powerSaveTR3.setLayout(new GridBagLayout());
        powerSaveTR3.setBackground(IbaColors.BT_GRAY);

        powerSaveTR4 = new JPanel();
        powerSaveTR4.setLayout(new GridBagLayout());
        powerSaveTR4.setBackground(IbaColors.BT_GRAY);

        powerSaveInterlocks = new JPanel();
        powerSaveInterlocks.setLayout(new GridBagLayout());
        powerSaveInterlocks.setBackground(IbaColors.BT_GRAY);

        mRestorePanel = new JPanel();
        mRestorePanel.setLayout(new GridBagLayout());
        mRestorePanel.setBackground(IbaColors.BT_GRAY);

        mAutomaticPanel = new JPanel();
        mAutomaticPanel.setLayout(new GridBagLayout());
        mAutomaticPanel.setBackground(IbaColors.BT_GRAY);

        interlockPanel = new JPanel();
        interlockPanel.setLayout(new GridBagLayout());
        interlockPanel.setBackground(IbaColors.BT_GRAY);

        mainCoilSweepPanel = new MainCoilSweep();
        //mainCoilSweepPanel.setLayout(new GridBagLayout());
        mainCoilSweepPanel.setBackground(IbaColors.BT_GRAY);

        ICCPanel = new S1EvsICC(this);
        ICCPanel.setBackground(IbaColors.BT_GRAY);

        mEsbtsPanel = new EsbtsPanel();
        mEsbtsPanel.setBackground(IbaColors.BT_GRAY);


        mScanningControllerPanel = new ScanningController();
        mScanningControllerPanel.setLayout(new GridBagLayout());
        mScanningControllerPanel.setBackground(IbaColors.BT_GRAY);

//        VceuPanel = new TcuVceuPanel();
//        mBDSPanel.add("VCEU", VceuPanel);
//        mBDSPanel.add("SSEU", new TcuSseuPanel());
//        mBDSPanel.add("SMEU", new ScanningMagnetsPanel());

        mSchedulerPanel = new JPanel();
        mSchedulerPanel.setFont(bigTitle);
        mSchedulerPanel.setLayout(new GridBagLayout());
        mSchedulerPanel.setBackground(IbaColors.BT_GRAY);

        TitledBorder title = new TitledBorder("Beam Scheduler");
        title.setTitleFont(bigTitle);
        title.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.lightGray));
        mSchedulerPanel.setBorder(title);

        iAlignPanel = new JPanel();
        iAlignPanel.setLayout(new GridBagLayout());
        iAlignPanel.setBackground(IbaColors.BT_GRAY);

        /* Construction of the display sub-panel */
        displayPanel = new JPanel();
//        TitledBorder title = new TitledBorder("Beam Profile Monitors (BPMs)");
//        displayPanel.setBorder(title);
        displayPanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        // Pre-allocate the JTextFields
        //positionText    = new JTextField[4];
        targetText = new JTextField[4];
        oldCurrText = new JTextField[4];
        newCurrText = new JTextField[4];
        diffText = new JTextField[4];
        toleranceText = new JTextField[4];
        sigmaText = new JTextField[4];
        oldCurrText2 = new JTextField[4];
        newCurrText2 = new JTextField[4];
        for (int i = 0; i < 4; i++) {
            //positionText[i] = new JTextField();
            targetText[i] = new JTextField();
            targetText[i].setEditable(false);
            targetText[i].setHorizontalAlignment(SwingConstants.CENTER);
            targetText[i].setBackground(IbaColors.BT_GRAY);
            if (i == 3) {
                targetText[i].setBorder(BorderFactory.createMatteBorder(1, 1, 0, 1, Color.BLACK));
            }else {
                targetText[i].setBorder(BorderFactory.createMatteBorder(1, 1, 0, 0, Color.BLACK));
            }
            oldCurrText[i] = new JTextField();
            oldCurrText[i].setEditable(false);
            oldCurrText[i].setHorizontalAlignment(SwingConstants.CENTER);
            oldCurrText[i].setBackground(IbaColors.BT_GRAY);
            if (i == 3) {
                oldCurrText[i].setBorder(BorderFactory.createMatteBorder(1, 1, 0, 1, Color.BLACK));
            }else {
                oldCurrText[i].setBorder(BorderFactory.createMatteBorder(1, 1, 0, 0, Color.BLACK));
            }
            newCurrText[i] = new JTextField();
            newCurrText[i].setHorizontalAlignment(SwingConstants.CENTER);
            if (i == 3) {
                newCurrText[i].setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.BLACK));
            }else {
                newCurrText[i].setBorder(BorderFactory.createMatteBorder(1, 1, 1, 0, Color.BLACK));
            }
            diffText[i] = new JTextField();
            diffText[i].setEditable(false);
            diffText[i].setHorizontalAlignment(SwingConstants.CENTER);
            diffText[i].setBackground(IbaColors.BT_GRAY);
            if (i == 3) {
                diffText[i].setBorder(BorderFactory.createMatteBorder(1, 1, 0, 1, Color.BLACK));
            }else {
                diffText[i].setBorder(BorderFactory.createMatteBorder(1, 1, 0, 0, Color.BLACK));
            }
            toleranceText[i] = new JTextField();
            toleranceText[i].setEditable(false);
            toleranceText[i].setHorizontalAlignment(SwingConstants.CENTER);
            toleranceText[i].setBackground(IbaColors.BT_GRAY);
            if (i == 3) {
                toleranceText[i].setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.BLACK));
            }else {
                toleranceText[i].setBorder(BorderFactory.createMatteBorder(1, 1, 1, 0, Color.BLACK));
            }
            sigmaText[i] = new JTextField();
            sigmaText[i].setEditable(false);
            sigmaText[i].setHorizontalAlignment(SwingConstants.CENTER);
            sigmaText[i].setBackground(IbaColors.BT_GRAY);
            if (i == 3) {
                sigmaText[i].setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.BLACK));
            }else {
                sigmaText[i].setBorder(BorderFactory.createMatteBorder(1, 1, 1, 0, Color.BLACK));
            }
            oldCurrText2[i] = new JTextField();
            oldCurrText2[i].setEditable(false);
            oldCurrText2[i].setHorizontalAlignment(SwingConstants.CENTER);
            oldCurrText2[i].setBackground(IbaColors.BT_GRAY);
            if (i == 3) {
                oldCurrText2[i].setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.BLACK));
            }else {
                oldCurrText2[i].setBorder(BorderFactory.createMatteBorder(1, 1, 1, 0, Color.BLACK));
            }
            newCurrText2[i] = new JTextField();
            newCurrText2[i].setHorizontalAlignment(SwingConstants.CENTER);
            if (i == 3) {
                newCurrText2[i].setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.BLACK));
            }else {
                newCurrText2[i].setBorder(BorderFactory.createMatteBorder(1, 1, 1, 0, Color.BLACK));
            }
        }

        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1; // full width
        c.fill = GridBagConstraints.BOTH;
        JLabel tempLabel;
        String[] labels = new String[]{"Targets: ", "Difference: ", "Sigma: "}; //, "Tolerance: "};
//        String[] labels2 = new String[]{"P1E X: ", "P1E Y: ", "P2E X: ", "P2E Y: "};

        for (String str : Status.BPM_names) {
            c.gridx += 1;
            displayPanel.add(new JLabel(str, JLabel.CENTER), c);
        }

        c.gridx = 0;
        c.weightx = 0;
        for (String str : labels) {
            c.gridy += 1;   //3
            displayPanel.add(new JLabel(str, JLabel.RIGHT), c);
        }
        // new row
        c.ipady = IPADY_DEFAULT;
        c.weightx = 1;
        for (int i = 0; i < 4; i++) {
            c.gridx = i + 1;
            c.gridy = 1;
            //displayPanel.add(positionText[i], c);
            //c.gridy++;
            displayPanel.add(targetText[i], c);
            c.gridy++; //2
            displayPanel.add(diffText[i], c);
            c.gridy++; //3
            displayPanel.add(sigmaText[i], c);
//            displayPanel.add(toleranceText[i], c);
        }

//        magnetPanel = new JPanel();
//        title = new TitledBorder("Extraction Steering Magnets");
//        magnetPanel.setBorder(title);
//        magnetPanel.setLayout(new GridBagLayout());
        c.insets = new Insets(15, 0, 0, 0);  //top padding
        c.gridx = 0;
        c.gridy++; //4
        c.ipady = 0;
//        c.weightx = 0;
//        displayPanel.add(new JLabel("<html><b> Magnets</b></html>", JLabel.LEFT), c);
//        c.weightx = 1;
        for (String str : Status.Magnet_names) {
            c.gridx++;
            tempLabel = new JLabel(str, JLabel.CENTER);
            tempLabel.setVerticalAlignment(JLabel.BOTTOM);
            displayPanel.add(tempLabel, c);
        }
        // new row
        c.insets = new Insets(0, 0, 0, 0);  //top padding
        c.ipady = IPADY_DEFAULT;
        c.gridx = 0;
        c.gridy++; //5
        c.weightx = 0;
        displayPanel.add(new JLabel("Current Setpoint (A): ", JLabel.RIGHT), c);
        c.weightx = 1;
        for (JTextField text : oldCurrText) {
            c.gridx++;
            displayPanel.add(text, c);
        }
        // new row
        c.gridx = 0;
        c.gridy++; //6
        c.weightx = 0;
        displayPanel.add(new JLabel("New Setpoint (A): ", JLabel.RIGHT), c);
        c.weightx = 1;
        for (JTextField text : newCurrText) {
            c.gridx++;
            displayPanel.add(text, c);
        }


        //
        //OPER LABEL AND BUTTONS GO HERE
        //



        /* Construction of the display sub-panel */
        c.gridx = 0;
        c.gridy = 0;
        c.ipady = 0; // reset ipady
        c.weightx = 1; // full width
        int numElem = Status.BLE_names.length;
        blePanel = new JPanel();

        title = new TitledBorder("Beamline Components");
        title.setTitleFont(bigTitle);
        title.setBorder(BorderFactory.createMatteBorder(1,1,1,1, Color.lightGray));
        blePanel.setBorder(title);
        blePanel.setLayout(new GridBagLayout());
        for (String str : Status.BLE_names) {
            if (str.startsWith("Slit")) {
                c.weightx = 0.5;
            } else {
                c.weightx = 1;
            }
            tempLabel = new JLabel(str, JLabel.CENTER);
            tempLabel.setFont(new Font(Font.DIALOG, Font.BOLD, 16));
            tempLabel.setVerticalAlignment(JLabel.BOTTOM);
            tempLabel.setBackground(IbaColors.BT_GRAY);
            blePanel.add(tempLabel, c);
            c.gridx += 1;
        }
        // new row
        c.gridx = 0;
        c.gridy = 1;
        c.ipady = IPADY_DEFAULT;
        c.weightx = 1; // full width
        statusText = new JTextField[numElem];
        for (int i = 0; i < numElem; i++) {
            c.gridx = i;
            statusText[i] = new JTextField();
            statusText[i].setEditable(false);
            statusText[i].setHorizontalAlignment(SwingConstants.CENTER);
            statusText[i].setFont(new Font(Font.DIALOG, Font.BOLD, 16));
            statusText[i].setBackground(IbaColors.BT_GRAY);
            if (i == numElem-1) {
                statusText[i].setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.BLACK));
            }else {
                statusText[i].setBorder(BorderFactory.createMatteBorder(1, 1, 1, 0, Color.BLACK));
            }
            blePanel.add(statusText[i], c);
        }

        // new row
        c.gridx = 0;
        c.gridy = 0;
        c.ipady = 0; // reset ipady
        numElem = Status.BCREU_names.length;
        bcreuPanel = new JPanel();
        title = new TitledBorder("Beam Current Regulator (BCREU)");
        title.setTitleFont(bigTitle);
        title.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.lightGray));
        bcreuPanel.setBorder(title);
        bcreuPanel.setLayout(new GridBagLayout());
        for (String str : Status.BCREU_names) {
            if (c.gridx > 2) {
                c.weightx = 0.5;
            } else {
                c.weightx = 1;
            }
            tempLabel = new JLabel(str, JLabel.CENTER);
            tempLabel.setFont(new Font(Font.DIALOG, Font.BOLD, 16));
            tempLabel.setVerticalAlignment(JLabel.BOTTOM);
            bcreuPanel.add(tempLabel, c);
            c.gridx += 1;
        }

        // new row
        c.gridx = 0;
        c.gridy = 1;
        c.ipady = IPADY_DEFAULT;
        bcreuText = new JTextField[numElem];
        for (int i = 0; i < numElem; i++) {
            c.gridx = i;
            bcreuText[i] = new JTextField();
            bcreuText[i].setEditable(false);
            bcreuText[i].setFont(new Font(Font.DIALOG, Font.BOLD, 16));
            bcreuText[i].setHorizontalAlignment(SwingConstants.CENTER);
            bcreuText[i].setBackground(IbaColors.BT_GRAY);
            if (i == numElem-1){
                bcreuText[i].setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.BLACK));
            }else {
                bcreuText[i].setBorder(BorderFactory.createMatteBorder(1, 1, 1, 0, Color.BLACK));
            }
            bcreuPanel.add(bcreuText[i], c);
        }

        // new row
        c.gridx = 0;
        c.gridy = 0;
        c.ipady = 0; // reset ipady
        c.weightx = 1; // full width
        numElem = Status.Cyclo_names.length;
        cycloPanel = new JPanel();
        title = new TitledBorder("Cyclotron (BPS)");
        title.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.BLACK));
        cycloPanel.setBorder(title);
        cycloPanel.setLayout(new GridBagLayout());
        for (String str : Status.Cyclo_names) {
            tempLabel = new JLabel(str, JLabel.CENTER);
            tempLabel.setVerticalAlignment(JLabel.BOTTOM);
            cycloPanel.add(tempLabel, c);
            c.gridx += 1;
        }

        // new row
        c.gridx = 0;
        c.gridy = 1;
        c.ipady = IPADY_DEFAULT;
        cycloText = new JTextField[numElem];
        for (int i = 0; i < numElem; i++) {
            c.gridx = i;
            cycloText[i] = new JTextField();
            cycloText[i].setEditable(false);
            cycloText[i].setHorizontalAlignment(SwingConstants.CENTER);
            cycloText[i].setBackground(IbaColors.BT_GRAY);
            if (i == numElem-1){
                cycloText[i].setBorder(BorderFactory.createMatteBorder(1,1,1,1, Color.BLACK));
            }else {
                cycloText[i].setBorder(BorderFactory.createMatteBorder(1,1,1,0, Color.BLACK));
            }
            cycloPanel.add(cycloText[i], c);
        }

//        computePanel.add(refreshPanel);

        /* These two buttons are not used */
//        setButton=new JButton("Set Beam line elements (not yet available)");
//        setButton.setActionCommand("set");
//        setButton.addActionListener(this);
//
//        resetButton=new JButton("Reset Beam line elements (not yet available)");
//        resetButton.setActionCommand("reset");
//        resetButton.addActionListener(this);


        /* Configure the buttons */
        refreshButton = new JButton("Prepare Beamline & Refresh");
        refreshButton.setActionCommand("refresh");
        refreshButton.addActionListener(this);

        computeButton = new JButton("Manual Refresh");
        computeButton.setActionCommand("compute");
        computeButton.addActionListener(this);

        cancelButton = new JButton("Cancel & Idle");
        cancelButton.setActionCommand("cancel");
        cancelButton.addActionListener(this);

        cancelButton2 = new JButton("Idle");
        cancelButton2.setActionCommand("cancel");
        cancelButton2.addActionListener(this);

        applyButton = new JButton("Apply New Currents");
        applyButton.setActionCommand("apply");
        applyButton.addActionListener(this);

        mainCoilButton = new JButton("Set RF");
        mainCoilButton.setActionCommand("output");
        mainCoilButton.addActionListener(this);

        PCVueButton = new JButton("Update from PCVue");
        PCVueButton.setActionCommand("update");
        PCVueButton.addActionListener(this);

        cancelTuneButton = new JButton("Cancel");
        cancelTuneButton.setActionCommand("cancelTune");
        cancelTuneButton.addActionListener(this);

        saveMeButton = new JButton("Set Safe Currents");
        saveMeButton.setActionCommand("saveMe");
        saveMeButton.addActionListener(this);

        alignMeButton = new JButton("Automatic Alignment");
        alignMeButton.setActionCommand("alignMe");
        alignMeButton.addActionListener(this);

        rfLUTButton = new JButton("RF LUT");
        rfLUTButton.setActionCommand("RFLUT");
        rfLUTButton.addActionListener(this);


        mExtractionPanel = new JPanel();
        mExtractionPanel.setLayout(new GridBagLayout());
        mExtractionPanel.setBackground(IbaColors.BT_GRAY);
//        c = new GridBagConstraints();
//        c.gridx = 0;
//        c.gridy = 1;
//        c.gridheight = 0;  //1
//        c.gridwidth = 3;
//        c.weighty = 0;
//        c.weightx = 0;
//        c.anchor = GridBagConstraints.NORTH;
//        c.insets    = new Insets(0,125,0,0);
//
//        operModeLabel2.setFont(new Font("Dialog", Font.BOLD, 16));
//        operModeLabel2.setText("Operating Mode: Manual");
//
//        mExtractionPanel.add(operModeLabel2, c);


        c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.ipady = 0; // reset ipady
        c.weightx = 1; // full width
        c.gridx = 0;
        c.gridwidth = 3; // 3 magnets
        c.gridy = 0;
        c.weighty = 0;
        c.gridheight = 1;
        mExtractionPanel.add(displayPanel, c);


/////////////making ialign buttons into one panel to add to mExtractionPanel////////

        c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.gridx = 0;
        c.gridy = 0;
        //c.gridheight = 2;
        c.gridwidth = 3;
        c.weightx = 1;
        c.weighty = 0;


        c.gridy++;  //1
        c.weighty = 0.5;
        JPanel tempPanel = new JPanel();
        tempPanel.setBackground(IbaColors.BT_GRAY);
        iAlignPanel.add(tempPanel, c);

        c.insets = new Insets(0, 0, 0, 0);     // reset to just the top padding
        c.gridx = 0;
        c.weightx = 1;
        c.gridwidth = 3;
        c.weighty = 0.5;
        //c.gridheight =1;
        tempPanel = new JPanel();
        tempPanel.setBackground(IbaColors.BT_GRAY);
        iAlignPanel.add(tempPanel, c);

        operModeLabel2.setFont(new Font("Dialog", Font.BOLD, 16));
        operModeLabel2.setText("Operating Mode: Manual");
        operModeLabel2.setHorizontalAlignment(JLabel.CENTER);
        //c.anchor = GridBagConstraints.CENTER;

        c.weighty = 0.5;
        c.gridy++;
        //c.fill      = GridBagConstraints.BOTH;
        //c.insets    = new Insets(fiftyPixels/2,(seventyFivePixels + fiftyPixels),(oneHundredFiftyPixels + oneHundredPixels + oneHundredPixels),0);
        //c.insets = new Insets(0,0,seventyFivePixels,0);
        tempPanel.add(operModeLabel2, c);

        iAlignPanel.add(tempPanel, c);

        c.insets = new Insets(0, 0, 0, 0);     // reset to just the top padding
        c.gridx = 0;
        c.weightx = 1;
        c.gridwidth = 3;
        c.weighty = 0.5;
        //c.gridheight =1;
        //c.gridy++;
        tempPanel = new JPanel();
        tempPanel.setBackground(IbaColors.BT_GRAY);
        // iAlignPanel.add(tempPanel, c);


        // Row of buttons:
        // c.fill      = GridBagConstraints.BOTH;
        c.insets = new Insets(0, 30, 0, 0);     // reset to just the top padding
        c.ipady = IPADY_DEFAULT;
        c.gridy++;
        c.gridwidth = 1;
        c.weighty = 0;
        iAlignPanel.add(refreshButton, c);
        c.gridx++;
        c.weightx = 0.5;
        c.insets = new Insets(0, 30, 0, 30);   // add padding between buttons
        iAlignPanel.add(applyButton, c);
        c.gridx++;
        c.weightx = 1;                        // reset to weight of 1
        c.insets = new Insets(0, 0, 0, 30);     // reset to just the top padding
        //c.ipadx = 30;
        iAlignPanel.add(cancelButton, c);

        c.gridx = 0;
        //c.gridy++;
        c.gridwidth = 3;
        c.weighty = 0;
        tempPanel = new JPanel();
        tempPanel.setBackground(IbaColors.BT_GRAY);
        //iAlignPanel.add(tempPanel, c);

        c.insets = new Insets(0, 0, 0, 0);     // reset to just the top padding
        c.gridx = 0;
        c.weightx = 1;
        c.gridwidth = 3;
        c.weighty = 0.5;
        //c.gridheight =1;
        c.gridy++;
        tempPanel = new JPanel();
        tempPanel.setBackground(IbaColors.BT_GRAY);
        iAlignPanel.add(tempPanel, c);


        //c.fill      = GridBagConstraints.BOTH;
        //c.ipadx = 0;
        c.gridwidth = 1;
        c.gridx = 0;
        c.gridy++;
        c.weighty = 0;
        //c.weightx   = 1;
        c.insets = new Insets(0, 30, 0, 0);   // add padding between buttons
        iAlignPanel.add(alignMeButton, c);
        c.gridx++;
        c.insets = new Insets(0, 30, 0, 30);   // add padding between buttons
        iAlignPanel.add(saveMeButton, c);

        c.gridx = 0;
        c.gridy++;
        c.gridwidth = 3;
        c.weighty = 0;
        tempPanel = new JPanel();
        tempPanel.setBackground(IbaColors.BT_GRAY);
        //iAlignPanel.add(tempPanel, c);

        c.insets = new Insets(0, 0, 0, 0);     // reset to just the top padding
        c.gridx = 0;
        c.weightx = 1;
        c.gridwidth = 3;
        c.weighty = 0.5;
        //c.gridheight =1;
        c.gridy++;
        tempPanel = new JPanel();
        tempPanel.setBackground(IbaColors.BT_GRAY);
        iAlignPanel.add(tempPanel, c);

        /////////////////////////////////

        c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.ipady = 0; // reset ipady
        c.weightx = 1; // full width
        c.gridx = 0;
        c.gridwidth = 3; // 3 magnets
        c.gridy = 1;
        c.gridheight = 1;
        c.weighty = 0.5;
        mExtractionPanel.add(iAlignPanel, c);


        //////////////////////////////////////////


        //c.fill      = GridBagConstraints.BOTH;
        //c.weighty   = 1;
        c.insets = new Insets(0, 0, 0, 0);     // reset to just the top padding
        c.gridx = 0;
        c.weightx = 1;
        c.gridwidth = 3;
        c.weighty = 0.5;
        //c.gridheight =1;
        //c.gridy++;
        tempPanel = new JPanel();
        tempPanel.setBackground(IbaColors.LT_GRAY);
        //mExtractionPanel.add(tempPanel, c);
        c.gridy++;
        //c.gridheight =1;
        c.weighty = 0;
        //c.fill      = GridBagConstraints.HORIZONTAL;
        mExtractionPanel.add(blePanel, c);

        c.gridy++;
        mExtractionPanel.add(bcreuPanel, c);


        displayPanel.setBackground(IbaColors.BT_GRAY);
        blePanel.setBackground(IbaColors.BT_GRAY);
        bcreuPanel.setBackground(IbaColors.BT_GRAY);
        cycloPanel.setBackground(IbaColors.BT_GRAY);


        c.fill = GridBagConstraints.BOTH;
        c.gridx = 3;
        c.gridheight = c.gridy + 1;
        c.gridy = 0;
        c.weightx = 6;
        c.weighty = 0.5;
        c.gridwidth = 1;

        tempPanel = new JPanel(new GridBagLayout());
        GridBagConstraints d = new GridBagConstraints();
        d.fill = GridBagConstraints.BOTH;
        d.weightx = 1;
        d.weighty = 0.5;
        d.gridx = 0;
        d.gridy = 0;
        mBpmXdata = createBpmDataset(16);
        mGaussXdata = createGaussDataset();
        mBpmChartX = createChart(mBpmXdata, mGaussXdata, PlotOrientation.VERTICAL);
        JPanel chartPanel = new ChartPanel(mBpmChartX);

        tempPanel.add(chartPanel, d);
        d.gridy = 1;

        mBpmYdata = createBpmDataset(16);
        mGaussYdata = createGaussDataset();
        mBpmChartY = createChart(mBpmYdata, mGaussYdata, PlotOrientation.HORIZONTAL);
        chartPanel = new ChartPanel(mBpmChartY);
        tempPanel.add(chartPanel, d);
        mExtractionPanel.add(tempPanel, c);


        //TRYING TO ADD VNC TABS
        //mVNCPanel = new JPanel(new GridBagLayout());
        //mVNCPanel.add(new SwingVncImageView());

        mMainCoilPanel = new JPanel(new GridBagLayout());
        c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.gridx = 0;
        c.gridy = 0;
        c.ipady = 0; // reset ipady
        c.weightx = 1; // full width
        numElem = Status.BLE_names2.length;


        ble2Panel = new JPanel(new GridBagLayout());
        title = new TitledBorder("Beamline Components");
        //title.setTitleFont(bigTitle);
        title.setBorder(BorderFactory.createMatteBorder(1,1,1,1, Color.BLACK));
        ble2Panel.setBorder(title);
        ble2Panel.setBackground(IbaColors.BT_GRAY);
        for (String str : Status.BLE_names2) {
            if (str.startsWith("Slit")) {
                c.weightx = 0.5;
            } else {
                c.weightx = 1;
            }
            tempLabel = new JLabel(str, JLabel.CENTER);
            tempLabel.setVerticalAlignment(JLabel.BOTTOM);
            tempLabel.setBackground(IbaColors.BT_GRAY);
            ble2Panel.add(tempLabel, c);
            c.gridx++;
        }
        // new row
        c.gridx = 0;
        c.gridy = 1;
        c.ipady = IPADY_DEFAULT;
        c.weightx = 1; // full width
        statusText2 = new JTextField[numElem];
        for (int i = 0; i < numElem; i++) {
            c.gridx = i;
            statusText2[i] = new JTextField();
            statusText2[i].setEditable(false);
            statusText2[i].setHorizontalAlignment(SwingConstants.CENTER);
            statusText2[i].setBackground(IbaColors.BT_GRAY);
            if (i == numElem-1) {
                statusText2[i].setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.BLACK));
            }else {
                statusText2[i].setBorder(BorderFactory.createMatteBorder(1, 1, 1, 0, Color.BLACK));
            }
            ble2Panel.add(statusText2[i], c);
        }
//        c = new GridBagConstraints();
//        c.fill      = GridBagConstraints.BOTH;
//        c.ipady     = 0; // reset ipady
//        c.weightx   = 1; // full width
//        c.weighty   = 1;
//        c.gridx     = 0;
//        c.gridy     = 0;
//        numElem     = Status.DF_names.length;
//
//        dfPanel = new JPanel(new GridBagLayout());
//        title = new TitledBorder("Deflector Current");
//        dfPanel.setBorder(title);
//        dfPanel.setBackground(IbaColors.BT_GRAY);
//        for (String str : Status.DF_names) {
//            if (str.startsWith("Slit")) {
//                c.weightx = 0.5;
//            } else {
//                c.weightx = 1;
//            }
//            tempLabel = new JLabel(str, JLabel.CENTER);
//            tempLabel.setVerticalAlignment(JLabel.BOTTOM);
//            tempLabel.setBackground(IbaColors.BT_GRAY);
//            dfPanel.add(tempLabel, c);
//            c.gridx++;
//        }
//        // new row
//        c.gridx = 0;
//        c.gridy = 1;
//        c.ipady = IPADY_DEFAULT;
//        c.weightx   = 1; // full width
//        statusText2      = new JTextField[numElem];
//        for (int i=0; i < numElem; i++) {
//            c.gridx = i;
//            statusText2[i] = new JTextField();
//            statusText2[i].setEditable(false);
//            statusText2[i].setBackground(IbaColors.BT_GRAY);
//            dfPanel.add(statusText2[i], c);
//        }
//        c = new GridBagConstraints();
//        c.fill      = GridBagConstraints.BOTH;
//        c.ipady     = 0; // reset ipady
//        c.weightx   = 1; // full width
//        c.weighty   = 1;
//        c.gridx     = 0;
//        c.gridy     = 0;


//        tempPanel = new JPanel();
//        tempPanel.setBackground(IbaColors.BT_GRAY);
//
//        mMainCoilPanel.add(tempPanel, c);
//        c.gridy     = 1;
//        c.weighty   = 0;
//        mMainCoilPanel.add(ble2Panel, c);
//        mMainCoilPanel.add(dfPanel, c);


        mTabs = new JTabbedPane();

        //mTabs.addTab("Extraction Steering", mExtractionPanel);
        //mTabs.addTab("VNC viewer", mVNCPanel);

        //mTabs.addTab("Restore Magnets", mRestorePanel);

        //mTabs.addTab("Main Coil Tuning", tuningPanel);

        //mTabs.addTab("Main Coil Sweep", mainCoilSweepPanel);

        //mTabs.addTab("Automatic Alignment", mAutomaticPanel);

        //mTabs.addTab("Source Burn-in", mSourcePanel);

        //mTabs.addTab("Power Save Mode", powerSavePanel);

        //mTabs.addTab("Interlocks", interlockPanel);

        //mTabs.addTab("S1E vs ICC", ICCPanel);

        //mTabs.addTab("Service Beam", mBlankPanel);


        ChangeListener changeListener = new ChangeListener() {
            public void stateChanged(ChangeEvent changeEvent) {
                JTabbedPane sourceTabbedPane = (JTabbedPane) changeEvent.getSource();
                int index = sourceTabbedPane.getSelectedIndex();
                System.out.println("Tab changed to: " + sourceTabbedPane.getTitleAt(index));

                mainPanel.remove(mScanningControllerPanel);

                GridBagConstraints c = new GridBagConstraints();
                c.fill = GridBagConstraints.BOTH;
                c.ipady = 0; // reset ipady
                c.weightx = 1; // full width
                c.gridx = 0;
                c.weighty = 0;
                c.insets = new Insets(5, 0, 0, 0);  //top padding

                if (index != 1) {
                    mainPanel.remove(MCTuningPanel);
                    //tuningPanel.remove(MCTuningPanel);
                    //tuningPanel.remove(logPanel);
                    c.gridheight = 1;
                    c.weighty = 0;
                    c.gridy = 1;
                    //mainPanel.add(bcreuPanel, c);
                    //c.gridy = 2;
                    mainPanel.add(cycloPanel, c);

                    c.gridy = 3;
                    c.gridwidth = 3;
                    c.gridheight = 2;
                    c.weighty = 20;   //request any extra vertical space
                    c.anchor = GridBagConstraints.PAGE_END; //bottom of space
                    c.fill = GridBagConstraints.BOTH;
                    //Dimension dim = new Dimension(1920, 300);
                    //logPanel.setPreferredSize(dim);
                    mainPanel.add(logPanel, c);
                    mainPanel.updateUI();
                    logPanel.updateUI();
                    //Gui.this.pack();
                }
                if (index == 1 || index == 5) {
                    mainPanel.remove(bcreuPanel);
                    mainPanel.remove(cycloPanel);
                    mainPanel.remove(logPanel);
                    c.gridheight = 1;
                    c.weighty = 0;
                    c.gridy = 1;
                    c.gridwidth = 3;
                    mainPanel.add(MCTuningPanel, c);

                    c.gridy = 3;
                    //c.gridwidth = 3;
                    c.gridheight = 2;
                    c.gridwidth = 3;
                    c.weighty = 20;   //request any extra vertical space
                    c.anchor = GridBagConstraints.PAGE_END; //bottom of space
                    //Dimension dim = new Dimension(1920, 300);
                    //logPanel.setPreferredSize(dim);
                    mainPanel.add(logPanel, c);
//                    SwingUtilities.invokeLater(() -> {
//                        tuningPanel.updateUI();
//                        tuningPanel.validate();
                    mainPanel.updateUI();
                    logPanel.updateUI();
//                    });
                }
                if (index == 4) {
                    mainPanel.remove(bcreuPanel);
                    mainPanel.remove(cycloPanel);
                    mainPanel.remove(logPanel);
                    c.gridy = 3;
                    c.weighty = 2;
                    c.gridwidth = 4;
                    c.anchor = GridBagConstraints.PAGE_END;
                    c.fill = GridBagConstraints.BOTH;
                    c.gridheight = 1;
                    powerSavePanel.add(logPanel, c);
                    mainPanel.updateUI();
                    logPanel.updateUI();
                }
                if (index == 6) {
                    mainPanel.remove(bcreuPanel);
                    mainPanel.remove(cycloPanel);
                    mainPanel.remove(logPanel);
                    mainPanel.add(mScanningControllerPanel, c);
//                    c.gridy = 3;
//                    c.weighty = 2;
//                    c.gridwidth = 4;
//                    c.anchor = GridBagConstraints.PAGE_END;
//                    c.fill = GridBagConstraints.BOTH;
//                    c.gridheight = 1;
//                    mainCoilSweepPanel.add(logPanel, c);
                    mainPanel.updateUI();
//                    logPanel.updateUI();
                }

            }
        };


        mTabs.addChangeListener(changeListener);


        ///////////////////////////////////////////////////////////////////////////////////////
        ///////////////////////////////Power Save Panel start ///////////////////////////////
        //////////////////////////////////////////////////////////////////////////////////////

        c = new GridBagConstraints();

        c.insets = new Insets(35, 0, 0, 0);
        c.gridx = 0;
        c.gridy = 1;
        c.weightx = 1;
        c.weighty = 0;
        c.gridwidth = 4;
        c.gridheight = 1;
        c.anchor = GridBagConstraints.NORTH;
        powerSaveLabel = new JLabel();

        powerSaveLabel.setFont(new Font("Dialog", Font.BOLD, 20));
        powerSaveLabel.setText("Power Save Mode: Disabled");

        powerSavePanel.add(powerSaveLabel, c);


        PowerSave = new JCheckBox();
        PowerSave.setText("Power Save");
        PowerSave.setSelected(true);
        PowerSave.setEnabled(true);

        c.insets = new Insets(70, 0, 0, 0);
        powerSavePanel.add(PowerSave, c);


        /////////////////////// Power Save TR1 ///////////////////////

        c = new GridBagConstraints();

        c.insets = new Insets(20, 0, 0, 0);
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 1;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.anchor = GridBagConstraints.CENTER;

        powerSaveLabelTR1 = new JLabel();
        powerSaveLabelTR1.setFont(new Font("Dialog", Font.BOLD, 30));
        powerSaveLabelTR1.setText("TR1");

        powerSaveLabelTR1.setBorder(BorderFactory.createMatteBorder(0, 0, 3, 0, IbaColors.BT_GRAY));

        powerSaveTR1.add(powerSaveLabelTR1, c);


        c.insets = new Insets(20, 50, 50, 50);
        c.gridx = 0;
        c.gridy = 1;
        c.weightx = 1;
        c.weighty = 1;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.anchor = GridBagConstraints.CENTER;
        c.fill = GridBagConstraints.BOTH;

        Dimension labelSize = new Dimension(120, 35);

        allocatedLabelTR1 = new JLabel();
        allocatedLabelTR1.setFont(new Font("Dialog", Font.BOLD, 20));
        allocatedLabelTR1.setText("Allocated");
        allocatedLabelTR1.setHorizontalAlignment(SwingConstants.CENTER);
        allocatedLabelTR1.setForeground(IbaColors.LT_GRAY);
        allocatedLabelTR1.setBorder(BorderFactory.createMatteBorder(3, 3, 3, 3, Color.darkGray));
        //allocatedLabelTR1.setPreferredSize(labelSize);

        powerSaveTR1.add(allocatedLabelTR1, c);


        c.insets = new Insets(0, 50, 50, 50);
        c.gridx = 0;
        c.gridy = 2;
        c.weightx = 1;
        c.weighty = 1;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.anchor = GridBagConstraints.CENTER;

        queuedLabelTR1 = new JLabel();
        queuedLabelTR1.setFont(new Font("Dialog", Font.BOLD, 20));
        queuedLabelTR1.setText("Queued");
        queuedLabelTR1.setHorizontalAlignment(SwingConstants.CENTER);
        queuedLabelTR1.setForeground(IbaColors.LT_GRAY);
        queuedLabelTR1.setBorder(BorderFactory.createMatteBorder(3, 3, 3, 3, Color.darkGray));
        //queuedLabelTR1.setPreferredSize(labelSize);

        powerSaveTR1.add(queuedLabelTR1, c);

        c.insets = new Insets(0, 50, 50, 50);
        c.gridx = 0;
        c.gridy = 3;
        c.weightx = 1;
        c.weighty = 1;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.anchor = GridBagConstraints.CENTER;

        searchingLabelTR1 = new JLabel();
        searchingLabelTR1.setFont(new Font("Dialog", Font.BOLD, 20));
        searchingLabelTR1.setText("Searching");
        searchingLabelTR1.setHorizontalAlignment(SwingConstants.CENTER);
        searchingLabelTR1.setForeground(IbaColors.LT_GRAY);
        searchingLabelTR1.setBorder(BorderFactory.createMatteBorder(3, 3, 3, 3, Color.darkGray));
        //searchingLabelTR1.setPreferredSize(labelSize);

        powerSaveTR1.add(searchingLabelTR1, c);


        c.insets = new Insets(0, 50, 30, 50);
        c.gridx = 0;
        c.gridy = 4;
        c.weightx = 1;
        c.weighty = 1;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.anchor = GridBagConstraints.CENTER;

        secureLabelTR1 = new JLabel();
        secureLabelTR1.setFont(new Font("Dialog", Font.BOLD, 20));
        secureLabelTR1.setText("Secured");
        secureLabelTR1.setHorizontalAlignment(SwingConstants.CENTER);
        secureLabelTR1.setForeground(IbaColors.LT_GRAY);
        secureLabelTR1.setBorder(BorderFactory.createMatteBorder(3, 3, 3, 3, Color.darkGray));
        //secureLabelTR1.setPreferredSize(labelSize);

        powerSaveTR1.add(secureLabelTR1, c);


        c.insets = new Insets(60, 80, 0, 40);
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 0.5;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.anchor = GridBagConstraints.CENTER;
        c.fill = GridBagConstraints.BOTH;

        //powerSaveTR1.setBorder(BorderFactory.createMatteBorder(3,3,3,3, Color.darkGray));

        powerSaveTR1.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, IbaColors.RED), BorderFactory.createMatteBorder(3, 3, 3, 3, Color.darkGray)));

        powerSavePanel.add(powerSaveTR1, c);


        /////////////////////// Power Save TR2 ///////////////////////

        c = new GridBagConstraints();

        c.insets = new Insets(20, 0, 0, 0);
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 1;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.anchor = GridBagConstraints.CENTER;

        powerSaveLabelTR2 = new JLabel();
        powerSaveLabelTR2.setFont(new Font("Dialog", Font.BOLD, 30));
        powerSaveLabelTR2.setText("TR2");

        powerSaveLabelTR2.setBorder(BorderFactory.createMatteBorder(0, 0, 3, 0, IbaColors.BT_GRAY));

        powerSaveTR2.add(powerSaveLabelTR2, c);


        c.insets = new Insets(20, 50, 50, 50);
        c.gridx = 0;
        c.gridy = 1;
        c.weightx = 1;
        c.weighty = 1;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.anchor = GridBagConstraints.CENTER;
        c.fill = GridBagConstraints.BOTH;

        allocatedLabelTR2 = new JLabel();
        allocatedLabelTR2.setFont(new Font("Dialog", Font.BOLD, 20));
        allocatedLabelTR2.setText("Allocated");
        allocatedLabelTR2.setHorizontalAlignment(SwingConstants.CENTER);
        allocatedLabelTR2.setForeground(IbaColors.LT_GRAY);
        allocatedLabelTR2.setBorder(BorderFactory.createMatteBorder(3, 3, 3, 3, Color.darkGray));
        //allocatedLabelTR2.setPreferredSize(labelSize);

        powerSaveTR2.add(allocatedLabelTR2, c);


        c.insets = new Insets(0, 50, 50, 50);
        c.gridx = 0;
        c.gridy = 2;
        c.weightx = 1;
        c.weighty = 1;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.anchor = GridBagConstraints.CENTER;

        queuedLabelTR2 = new JLabel();
        queuedLabelTR2.setFont(new Font("Dialog", Font.BOLD, 20));
        queuedLabelTR2.setText("Queued");
        queuedLabelTR2.setHorizontalAlignment(SwingConstants.CENTER);
        queuedLabelTR2.setForeground(IbaColors.LT_GRAY);
        queuedLabelTR2.setBorder(BorderFactory.createMatteBorder(3, 3, 3, 3, Color.darkGray));
        //queuedLabelTR2.setPreferredSize(labelSize);

        powerSaveTR2.add(queuedLabelTR2, c);

        c.insets = new Insets(0, 50, 50, 50);
        c.gridx = 0;
        c.gridy = 3;
        c.weightx = 1;
        c.weighty = 1;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.anchor = GridBagConstraints.CENTER;

        searchingLabelTR2 = new JLabel();
        searchingLabelTR2.setFont(new Font("Dialog", Font.BOLD, 20));
        searchingLabelTR2.setText("Searching");
        searchingLabelTR2.setHorizontalAlignment(SwingConstants.CENTER);
        searchingLabelTR2.setForeground(IbaColors.LT_GRAY);
        searchingLabelTR2.setBorder(BorderFactory.createMatteBorder(3, 3, 3, 3, Color.darkGray));
        //searchingLabelTR2.setPreferredSize(labelSize);

        powerSaveTR2.add(searchingLabelTR2, c);


        c.insets = new Insets(0, 50, 30, 50);
        c.gridx = 0;
        c.gridy = 4;
        c.weightx = 1;
        c.weighty = 1;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.anchor = GridBagConstraints.CENTER;

        secureLabelTR2 = new JLabel();
        secureLabelTR2.setFont(new Font("Dialog", Font.BOLD, 20));
        secureLabelTR2.setText("Secured");
        secureLabelTR2.setHorizontalAlignment(SwingConstants.CENTER);
        secureLabelTR2.setForeground(IbaColors.LT_GRAY);
        secureLabelTR2.setBorder(BorderFactory.createMatteBorder(3, 3, 3, 3, Color.darkGray));
        //secureLabelTR2.setPreferredSize(labelSize);

        powerSaveTR2.add(secureLabelTR2, c);


        c.insets = new Insets(60, 40, 0, 40);
        c.gridx = 1;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 0.5;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.anchor = GridBagConstraints.CENTER;
        c.fill = GridBagConstraints.BOTH;

        //powerSaveTR2.setBorder(BorderFactory.createMatteBorder(3,3,3,3, Color.darkGray));

        powerSaveTR2.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, IbaColors.YELLOW), BorderFactory.createMatteBorder(3, 3, 3, 3, Color.darkGray)));

        powerSavePanel.add(powerSaveTR2, c);


        /////////////////////// Power Save TR3 ///////////////////////

        c = new GridBagConstraints();

        c.insets = new Insets(20, 0, 0, 0);
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 1;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.anchor = GridBagConstraints.CENTER;

        powerSaveLabelTR3 = new JLabel();
        powerSaveLabelTR3.setFont(new Font("Dialog", Font.BOLD, 30));
        powerSaveLabelTR3.setText("TR3");

        powerSaveLabelTR3.setBorder(BorderFactory.createMatteBorder(0, 0, 3, 0, IbaColors.BT_GRAY));

        powerSaveTR3.add(powerSaveLabelTR3, c);


        c.insets = new Insets(20, 50, 50, 50);
        c.gridx = 0;
        c.gridy = 1;
        c.weightx = 1;
        c.weighty = 1;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.anchor = GridBagConstraints.CENTER;
        c.fill = GridBagConstraints.BOTH;

        allocatedLabelTR3 = new JLabel();
        allocatedLabelTR3.setFont(new Font("Dialog", Font.BOLD, 20));
        allocatedLabelTR3.setText("Allocated");
        allocatedLabelTR3.setHorizontalAlignment(SwingConstants.CENTER);
        allocatedLabelTR3.setForeground(IbaColors.LT_GRAY);
        allocatedLabelTR3.setBorder(BorderFactory.createMatteBorder(3, 3, 3, 3, Color.darkGray));
        //allocatedLabelTR3.setPreferredSize(labelSize);

        powerSaveTR3.add(allocatedLabelTR3, c);


        c.insets = new Insets(0, 50, 50, 50);
        c.gridx = 0;
        c.gridy = 2;
        c.weightx = 1;
        c.weighty = 1;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.anchor = GridBagConstraints.CENTER;

        queuedLabelTR3 = new JLabel();
        queuedLabelTR3.setFont(new Font("Dialog", Font.BOLD, 20));
        queuedLabelTR3.setText("Queued");
        queuedLabelTR3.setHorizontalAlignment(SwingConstants.CENTER);
        queuedLabelTR3.setForeground(IbaColors.LT_GRAY);
        queuedLabelTR3.setBorder(BorderFactory.createMatteBorder(3, 3, 3, 3, Color.darkGray));
        //queuedLabelTR3.setPreferredSize(labelSize);

        powerSaveTR3.add(queuedLabelTR3, c);

        c.insets = new Insets(0, 50, 50, 50);
        c.gridx = 0;
        c.gridy = 3;
        c.weightx = 1;
        c.weighty = 1;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.anchor = GridBagConstraints.CENTER;

        searchingLabelTR3 = new JLabel();
        searchingLabelTR3.setFont(new Font("Dialog", Font.BOLD, 20));
        searchingLabelTR3.setText("Searching");
        searchingLabelTR3.setHorizontalAlignment(SwingConstants.CENTER);
        searchingLabelTR3.setForeground(IbaColors.LT_GRAY);
        searchingLabelTR3.setBorder(BorderFactory.createMatteBorder(3, 3, 3, 3, Color.darkGray));
        //searchingLabelTR3.setPreferredSize(labelSize);

        powerSaveTR3.add(searchingLabelTR3, c);


        c.insets = new Insets(0, 50, 30, 50);
        c.gridx = 0;
        c.gridy = 4;
        c.weightx = 1;
        c.weighty = 1;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.anchor = GridBagConstraints.CENTER;

        secureLabelTR3 = new JLabel();
        secureLabelTR3.setFont(new Font("Dialog", Font.BOLD, 20));
        secureLabelTR3.setText("Secured");
        secureLabelTR3.setHorizontalAlignment(SwingConstants.CENTER);
        secureLabelTR3.setForeground(IbaColors.LT_GRAY);
        secureLabelTR3.setBorder(BorderFactory.createMatteBorder(3, 3, 3, 3, Color.darkGray));
        //secureLabelTR3.setPreferredSize(labelSize);

        powerSaveTR3.add(secureLabelTR3, c);


        c.insets = new Insets(60, 40, 0, 40);
        c.gridx = 2;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 0.5;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.anchor = GridBagConstraints.CENTER;
        c.fill = GridBagConstraints.BOTH;

        //powerSaveTR3.setBorder(BorderFactory.createMatteBorder(3,3,3,3, Color.darkGray));

        powerSaveTR3.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, IbaColors.GREEN), BorderFactory.createMatteBorder(3, 3, 3, 3, Color.darkGray)));

        powerSavePanel.add(powerSaveTR3, c);


        /////////////////////// Power Save TR4 ///////////////////////

        c = new GridBagConstraints();

        c.insets = new Insets(20, 0, 0, 0);
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 1;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.anchor = GridBagConstraints.CENTER;

        powerSaveLabelTR4 = new JLabel();
        powerSaveLabelTR4.setFont(new Font("Dialog", Font.BOLD, 30));
        powerSaveLabelTR4.setText("TR4");

        powerSaveLabelTR4.setBorder(BorderFactory.createMatteBorder(0, 0, 3, 0, IbaColors.BT_GRAY));

        powerSaveTR4.add(powerSaveLabelTR4, c);


        c.insets = new Insets(20, 50, 50, 50);
        c.gridx = 0;
        c.gridy = 1;
        c.weightx = 1;
        c.weighty = 1;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.anchor = GridBagConstraints.CENTER;
        c.fill = GridBagConstraints.BOTH;

        allocatedLabelTR4 = new JLabel();
        allocatedLabelTR4.setFont(new Font("Dialog", Font.BOLD, 20));
        allocatedLabelTR4.setText("Allocated");
        allocatedLabelTR4.setHorizontalAlignment(SwingConstants.CENTER);
        allocatedLabelTR4.setForeground(IbaColors.LT_GRAY);
        allocatedLabelTR4.setBorder(BorderFactory.createMatteBorder(3, 3, 3, 3, Color.darkGray));
        //allocatedLabelTR4.setPreferredSize(labelSize);

        powerSaveTR4.add(allocatedLabelTR4, c);


        c.insets = new Insets(0, 50, 50, 50);
        c.gridx = 0;
        c.gridy = 2;
        c.weightx = 1;
        c.weighty = 1;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.anchor = GridBagConstraints.CENTER;

        queuedLabelTR4 = new JLabel();
        queuedLabelTR4.setFont(new Font("Dialog", Font.BOLD, 20));
        queuedLabelTR4.setText("Queued");
        queuedLabelTR4.setHorizontalAlignment(SwingConstants.CENTER);
        queuedLabelTR4.setForeground(IbaColors.LT_GRAY);
        queuedLabelTR4.setBorder(BorderFactory.createMatteBorder(3, 3, 3, 3, Color.darkGray));
        //queuedLabelTR4.setPreferredSize(labelSize);

        powerSaveTR4.add(queuedLabelTR4, c);

        c.insets = new Insets(0, 50, 50, 50);
        c.gridx = 0;
        c.gridy = 3;
        c.weightx = 1;
        c.weighty = 1;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.anchor = GridBagConstraints.CENTER;

        searchingLabelTR4 = new JLabel();
        searchingLabelTR4.setFont(new Font("Dialog", Font.BOLD, 20));
        searchingLabelTR4.setText("Searching");
        searchingLabelTR4.setHorizontalAlignment(SwingConstants.CENTER);
        searchingLabelTR4.setForeground(IbaColors.LT_GRAY);
        searchingLabelTR4.setBorder(BorderFactory.createMatteBorder(3, 3, 3, 3, Color.darkGray));
        //searchingLabelTR4.setPreferredSize(labelSize);

        powerSaveTR4.add(searchingLabelTR4, c);


        c.insets = new Insets(0, 50, 30, 50);
        c.gridx = 0;
        c.gridy = 4;
        c.weightx = 1;
        c.weighty = 1;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.anchor = GridBagConstraints.CENTER;

        secureLabelTR4 = new JLabel();
        secureLabelTR4.setFont(new Font("Dialog", Font.BOLD, 20));
        secureLabelTR4.setText("Secured");
        secureLabelTR4.setHorizontalAlignment(SwingConstants.CENTER);
        ;
        secureLabelTR4.setForeground(IbaColors.LT_GRAY);
        secureLabelTR4.setBorder(BorderFactory.createMatteBorder(3, 3, 3, 3, Color.darkGray));
        //secureLabelTR4.setPreferredSize(labelSize);

        powerSaveTR4.add(secureLabelTR4, c);


        c.insets = new Insets(60, 40, 0, 80);
        c.gridx = 3;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 0.5;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.anchor = GridBagConstraints.CENTER;
        c.fill = GridBagConstraints.BOTH;

        //powerSaveTR4.setBorder(BorderFactory.createMatteBorder(3,3,3,3, Color.darkGray));

        //powerSaveTR4.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(3,3,3,3, Color.darkGray), BorderFactory.createMatteBorder(1,1,1,1, IbaColors.BLUE)));

        powerSaveTR4.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, IbaColors.BLUE), BorderFactory.createMatteBorder(3, 3, 3, 3, Color.darkGray)));

        powerSavePanel.add(powerSaveTR4, c);


        /////////////////////// Power Save Interlocks ///////////////////////

        c = new GridBagConstraints();

        c.insets = new Insets(0, 0, 0, 0);
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 0;
        c.weighty = 0.5;
        c.gridwidth = 6;
        c.gridheight = 1;
        c.anchor = GridBagConstraints.NORTH;


        powerSaveInterlockLabel = new JLabel();
        powerSaveInterlockLabel.setFont(new Font("Dialog", Font.BOLD, 20));
        powerSaveInterlockLabel.setText("Requirements");

        powerSaveInterlocks.add(powerSaveInterlockLabel, c);


        c.insets = new Insets(10, 20, 0, 40);
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 0;
        c.weighty = 1;
        c.gridwidth = 1;
        c.gridheight = 1;

        psInterlock1 = new JCheckBox();
        psInterlock1.setFont(new Font("Dialog", Font.BOLD, 14));
        psInterlock1.setText("Automatic Operating Mode");
        psInterlock1.setSelected(false);
        psInterlock1.setEnabled(false);

        c.anchor = GridBagConstraints.CENTER;
        c.fill = GridBagConstraints.HORIZONTAL;
        powerSaveInterlocks.add(psInterlock1, c);


        c.insets = new Insets(10, 40, 0, 40);
        c.gridx = 1;
        c.gridy = 0;
        c.weightx = 0;
        c.weighty = 1;
        c.gridwidth = 1;
        c.gridheight = 1;

        psInterlock2 = new JCheckBox();
        psInterlock2.setFont(new Font("Dialog", Font.BOLD, 14));
        psInterlock2.setText("Automatic Scheduling Mode");
        psInterlock2.setSelected(false);
        psInterlock2.setEnabled(false);

        c.anchor = GridBagConstraints.CENTER;
        c.fill = GridBagConstraints.HORIZONTAL;
        powerSaveInterlocks.add(psInterlock2, c);


        c.insets = new Insets(10, 40, 0, 40);
        c.gridx = 2;
        c.gridy = 0;
        c.weightx = 0;
        c.weighty = 1;
        c.gridwidth = 1;
        c.gridheight = 1;

        psInterlock3 = new JCheckBox();
        psInterlock3.setFont(new Font("Dialog", Font.BOLD, 14));
        psInterlock3.setText("No TR allocated");
        psInterlock3.setSelected(false);
        psInterlock3.setEnabled(false);

        c.anchor = GridBagConstraints.CENTER;
        c.fill = GridBagConstraints.HORIZONTAL;
        powerSaveInterlocks.add(psInterlock3, c);

        c.insets = new Insets(10, 40, 0, 40);
        c.gridx = 3;
        c.gridy = 0;
        c.weightx = 0;
        c.weighty = 1;
        c.gridwidth = 1;
        c.gridheight = 1;

        psInterlock4 = new JCheckBox();
        psInterlock4.setFont(new Font("Dialog", Font.BOLD, 14));
        psInterlock4.setText("No TR in Queue");
        psInterlock4.setSelected(false);
        psInterlock4.setEnabled(false);

        c.anchor = GridBagConstraints.CENTER;
        c.fill = GridBagConstraints.HORIZONTAL;
        powerSaveInterlocks.add(psInterlock4, c);

        c.insets = new Insets(10, 40, 0, 40);
        c.gridx = 4;
        c.gridy = 0;
        c.weightx = 0;
        c.weighty = 1;
        c.gridwidth = 1;
        c.gridheight = 1;

        psInterlock5 = new JCheckBox();
        psInterlock5.setFont(new Font("Dialog", Font.BOLD, 14));
        psInterlock5.setText("No TR Secured");
        psInterlock5.setSelected(false);
        psInterlock5.setEnabled(false);

        c.anchor = GridBagConstraints.CENTER;
        c.fill = GridBagConstraints.HORIZONTAL;
        powerSaveInterlocks.add(psInterlock5, c);

        c.insets = new Insets(10, 40, 0, 20);
        c.gridx = 5;
        c.gridy = 0;
        c.weightx = 0;
        c.weighty = 1;
        c.gridwidth = 1;
        c.gridheight = 1;

        psInterlock6 = new JCheckBox();
        psInterlock6.setFont(new Font("Dialog", Font.BOLD, 14));
        psInterlock6.setText("No TR Searching");
        psInterlock6.setSelected(false);
        psInterlock6.setEnabled(false);

        c.anchor = GridBagConstraints.CENTER;
        c.fill = GridBagConstraints.HORIZONTAL;
        powerSaveInterlocks.add(psInterlock6, c);


        c.insets = new Insets(120, 120, 80, 120);
        c.gridx = 0;
        c.gridy = 1;
        c.weightx = 1;
        c.weighty = 0.5;
        c.gridwidth = 4;
        c.gridheight = 1;
        c.anchor = GridBagConstraints.CENTER;
        c.fill = GridBagConstraints.BOTH;

        //powerSaveInterlocks.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED, IbaColors.BT_GRAY, Color.darkGray));

        //powerSaveInterlocks.setBorder(BorderFactory.createCompoundBorder((BorderFactory.createMatteBorder(2,2,2,2, Color.darkGray)), (BorderFactory.createBevelBorder(BevelBorder.RAISED, IbaColors.BT_GRAY, Color.darkGray))));

        powerSaveInterlocks.setBorder(BorderFactory.createCompoundBorder((BorderFactory.createBevelBorder(BevelBorder.RAISED, IbaColors.LT_GRAY, Color.darkGray)), (BorderFactory.createBevelBorder(BevelBorder.LOWERED, Color.darkGray, Color.gray))));

        powerSavePanel.add(powerSaveInterlocks, c);


        /////////////////////////////////////////////////////////////////////////////////////////////////
        //////////////////////// MC Tuning panel start////////////////////////////////////////////////////
        ///////////////////////////////////////////////////////////////////////////////////////////////

        c = new GridBagConstraints();

//        c.insets    = new Insets(30,0,0,30);
        c.insets = new Insets(oneHundredFiftyPixels / 5, 0, 0, oneHundredFiftyPixels / 5 + 10);
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 0;
        c.weighty = 0;
        c.gridwidth = 3;
        c.gridheight = 0;
        c.anchor = GridBagConstraints.NORTH;
        operModeLabel.setFont(new Font("Dialog", Font.BOLD, 16));
        operModeLabel.setText("Operating Mode: Manual");
        //operModeLabel.setHorizontalAlignment(SwingConstants.CENTER);


        tuningPanel.add(operModeLabel, c);


        ////////////////LLRF section /////////////////////


//        c = new GridBagConstraints();
//
//        c.insets    = new Insets(80,0,0,0);
//
//        LLRFLabel.setFont(new Font("Dialog", Font.BOLD, 20));
//        LLRFLabel.setText("LLRF");
//
//        c.anchor = GridBagConstraints.NORTH;
//
//        c.ipady      = IPADY_DEFAULT;
//        c.gridx      = 0;
//        c.gridy      = 0;
//        c.weightx    = 1;
//        c.gridwidth  = 1;
//        c.weighty    = 0.5;
//        c.gridheight = 1;
//
//        //tuningPanel.add(LLRFLabel, c);
//
//        LLRFTuningPanel = new JPanel();
//        LLRFTuningPanel.setLayout(new GridBagLayout());
//
//
//        VDeeFB =  new JTextField();
//        VDeeFB.setEditable(false);
//        title = new TitledBorder("kV");
//        title.setTitleJustification(TitledBorder.RIGHT);
//        VDeeFB.setBorder(title);
//        VDeeFB.setBackground(IbaColors.BT_GRAY);
//        VDeeFB.setHorizontalAlignment(SwingConstants.CENTER);
//        VDeeFB.setFont(new Font("Dialog", Font.BOLD, 20));
//        VDeeFB.setText("41.00");
        Dimension dimension = new Dimension(150, 60);
//        VDeeFB.setPreferredSize(dimension);
//
//        c.insets    = new Insets(50,0,0,0);
//
//        LLRFTuningPanel.add(VDeeFB, c);
//
//        VDeeSP = new JTextField();
//        title = new TitledBorder("Vdee1");
//        VDeeSP.setBorder(title);
//        VDeeSP.setColumns(7);
//        VDeeSP.setHorizontalAlignment(SwingConstants.CENTER);
//        VDeeSP.setText("41.00");
//        VDeeSP.addActionListener(this);
//        VDeeSP.addKeyListener(new KeyListener() {
//            @Override
//            public void keyTyped(KeyEvent e) {}
//            @Override
//            public void keyPressed(KeyEvent e) {
//                if (e.getKeyCode() == KeyEvent.VK_ENTER){
//                    controller.beam.llrf.setMaxVoltage(56.00);
//                    if (Double.parseDouble(VDeeSP.getText()) >= 41.00 && Double.parseDouble(VDeeSP.getText()) <= 56.00) {
//                        controller.beam.llrf.setDeeVoltage1(Double.parseDouble(VDeeSP.getText()));
//                        log.warn("VDee1 set to " + String.format("%.2f", Double.parseDouble(VDeeSP.getText())));
//                        beamCurrent.clear();
//                        outputVariance.setText("Variance: ");
//                    } else {log.error("VDee1 set point was out of bounds 41-56kV.");}
//                    if (Double.parseDouble(VDeeSP.getText()) < 41.00) {
//                        VDeeSP.setText("41.00");
//                    }
//                    if (Double.parseDouble(VDeeSP.getText()) > 56.00) {
//                        VDeeSP.setText("56.00");
//                    }
//                }
//            }
//            @Override
//            public void keyReleased(KeyEvent e) {}
//        });
//
//        c.anchor = GridBagConstraints.SOUTH;
//
//        c.insets    = new Insets(0,0,125,0);
//
//        LLRFTuningPanel.add(VDeeSP, c);
//
//        Dee1plus = new JButton("+");
//        dimension = new Dimension(25,20);
//        Dee1plus.setPreferredSize(dimension);
//        Dee1plus.setMargin(new Insets(0, 0, 0, 0));
//        Dee1plus.setFont(new Font("Dialog", Font.BOLD, 15));
//        Dee1plus.addActionListener(this);
//        Dee1plus.setActionCommand("VDee1plus");
//
//        c.insets    = new Insets(0,112,130,0);
//
//        LLRFTuningPanel.add(Dee1plus, c);
//
//        Dee1minus = new JButton("-");
//        Dee1minus.setPreferredSize(dimension);
//        Dee1minus.setMargin(new Insets(0, 0, 0, 0));
//        Dee1minus.setFont(new Font("Dialog", Font.BOLD, 18));
//        Dee1minus.addActionListener(this);
//        Dee1minus.setActionCommand("VDee1minus");
//
//        c.insets    = new Insets(0,0,130,112);
//
//        LLRFTuningPanel.add(Dee1minus, c);
//
//
//
//        VDeeSP2 = new JTextField();
//        title = new TitledBorder("Vdee2");
//        VDeeSP2.setBorder(title);
//        VDeeSP2.setColumns(7);
//        VDeeSP2.setHorizontalAlignment(SwingConstants.CENTER);
//        VDeeSP2.setText("41.00");
//        VDeeSP2.addActionListener(this);
//        VDeeSP2.addKeyListener(new KeyListener() {
//            @Override
//            public void keyTyped(KeyEvent e) {}
//            @Override
//            public void keyPressed(KeyEvent e) {
//                if (e.getKeyCode() == KeyEvent.VK_ENTER){
//                    controller.beam.llrf.setMaxVoltage(56.00);
//                    if (Double.parseDouble(VDeeSP2.getText()) >= 41.00 && Double.parseDouble(VDeeSP2.getText()) <= 56.00) {
//                        controller.beam.llrf.setDeeVoltage1(Double.parseDouble(VDeeSP2.getText()));
//                        log.warn("VDee2 set to " + String.format("%.2f", Double.parseDouble(VDeeSP2.getText())));
//                        beamCurrent.clear();
//                        outputVariance.setText("Variance: ");
//                    } else {log.error("VDee2 setpoint was out of bounds 41-56kV.");}
//                    if (Double.parseDouble(VDeeSP2.getText()) < 41.00) {
//                        VDeeSP2.setText("41.00");
//                    }
//                    if (Double.parseDouble(VDeeSP2.getText()) > 56.00) {
//                        VDeeSP2.setText("56.00");
//                    }
//                }
//            }
//            @Override
//            public void keyReleased(KeyEvent e) {}
//        });
//
//        c.anchor = GridBagConstraints.SOUTH;
//
//        c.insets    = new Insets(0,0,60,0);
//
//        LLRFTuningPanel.add(VDeeSP2, c);
//
//        Dee2plus = new JButton("+");
//        Dee2plus.setPreferredSize(dimension);
//        Dee2plus.setMargin(new Insets(0, 0, 0, 0));
//        Dee2plus.setFont(new Font("Dialog", Font.BOLD, 15));
//        Dee2plus.addActionListener(this);
//        Dee2plus.setActionCommand("VDee2plus");
//
//
//        c.insets    = new Insets(0,112,65,0);
//
//        LLRFTuningPanel.add(Dee2plus, c);
//
//        Dee2minus = new JButton("-");
//        Dee2minus.setPreferredSize(dimension);
//        Dee2minus.setMargin(new Insets(0, 0, 0, 0));
//        Dee2minus.setFont(new Font("Dialog", Font.BOLD, 18));
//        Dee2minus.addActionListener(this);
//        Dee2minus.setActionCommand("VDee2minus");
//
//        c.insets    = new Insets(0,0,65,112);
//
//        LLRFTuningPanel.add(Dee2minus, c);
//
//
//
//        c.gridx = 0;
//        c.gridy = 0;
//        c.gridwidth = 1;
//        c.gridheight = 1;
//        c.weightx = 1;
//        c.weighty = 0.5;
//        c.fill = GridBagConstraints.BOTH;
//        c.anchor = GridBagConstraints.CENTER;
//        c.insets    = new Insets(50,150,50,50);
//
//        title = new TitledBorder("LLRF");
//        title.setTitleJustification(TitledBorder.CENTER);
//        title.setTitleFont(new Font("Dialog", Font.BOLD, 20));
//        LLRFTuningPanel.setBorder(title);
//
//        tuningPanel.add(LLRFTuningPanel, c);
//
//        c.fill = GridBagConstraints.NONE;
//
//


        //////////////////////////////////////////

        outputPanel = new JPanel();
        outputPanel.setLayout(new GridBagLayout());

        c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = 0;
        c.weighty = 0.0D;
        c.weightx = 0.0D;
        c.insets = new Insets(15, 0, 0, 0);
        c.anchor = 11;
        this.outputVariance.setFont(new Font("Dialog", 1, 12));
        this.outputVariance.setText("Variance: ");
        this.outputPanel.add(this.outputVariance, c);
        c = new GridBagConstraints();
        c.insets = new Insets(0, 0, 160, 0);
        this.outputLabel.setFont(new Font("Dialog", 1, 30));
        title = new TitledBorder("nA");
        title.setTitleJustification(3);
        title.setBorder(BorderFactory.createMatteBorder(1,1,1,1, Color.BLACK));
        Border margin = new EmptyBorder(10, 20, 10, 20);
        this.outputLabel.setBorder(new CompoundBorder(title, margin));
        this.outputLabel.setHorizontalTextPosition(0);
        this.outputLabel.setText("S1E:  ...");
        c.anchor = 10;
        c.fill = 0;
        c.ipady = this.IPADY_DEFAULT;
        c.gridx = 1;
        c.gridy = 0;
        c.weightx = 1.0D;
        c.gridwidth = 1;
        c.weighty = 0.5D;
        c.gridheight = 1;
        this.outputPanel.add(this.outputLabel, c);
        c.weightx = 0.5D;
        c.weighty = 0.0D;
        c.ipadx = 30;
        c.insets = new Insets(35, 0, 0, 110);
        this.outputPanel.add(this.mainCoilButton, c);
        c.ipadx = 40;
        c.insets = new Insets(35, 110, 0, 0);
        this.outputPanel.add(this.cancelButton2, c);
        c.weighty = 0.0D;
        c.ipadx = 0;
        this.MCTuning.setText("MC Tuning");
        this.SourTuning.setText("Source Tuning");
        this.MCTuning.setHorizontalTextPosition(2);
        this.SourTuning.setHorizontalTextPosition(2);
        c.anchor = 15;
        c.insets = new Insets(0, 104, 105, 0);
        this.outputPanel.add(this.SourTuning, c);
        c.insets = new Insets(0, 127, 80, 0);
        this.outputPanel.add(this.MCTuning, c);
        c.weightx = 0.5D;
        c.weighty = 0.0D;
        c.ipadx = 25;
        c.insets = new Insets(0, 0, 90, 110);
        this.outputPanel.add(this.rfLUTButton, c);
        c.ipadx = 62;
        c.insets = new Insets(0, 0, 30, 0);
        this.outputPanel.add(this.PCVueButton, c);

//        c = new GridBagConstraints();
//        c.gridx = 0;
//        c.gridy = 0;
//        c.weighty = 0;
//        c.weightx = 0;
//        c.gridheight = 1;
//        c.insets = new Insets(15, 0, 0, 0);
//        c.anchor = GridBagConstraints.NORTH;
//        outputVariance.setFont(new Font("Dialog", Font.BOLD, 12));
//        outputVariance.setText("Variance: ");
//        //outputPanel.add(outputVariance, c);
//
//        c = new GridBagConstraints();
//
//        //c.insets = new Insets(0, 0, 160, 0);
//        outputLabel.setFont(new Font("Dialog", Font.BOLD, 30));
//        title = new TitledBorder("nA");
//        title.setTitleJustification(TitledBorder.RIGHT);
////        dimension = new Dimension(300,100);
////        outputLabel.setPreferredSize(dimension);
//        Border margin = new EmptyBorder(10, 20, 10, 20);
//        outputLabel.setBorder(new CompoundBorder(title, margin));
//        outputLabel.setHorizontalTextPosition(SwingConstants.CENTER);
//        outputLabel.setText("S1E:  " + "...");
//
//        //c.anchor = GridBagConstraints.NORTH;
//        c.fill = GridBagConstraints.NONE;
//        c.ipady = IPADY_DEFAULT;
//        c.gridx = 1;
//        c.gridy = 0;
//        c.weightx = 1;
//        c.gridwidth = 1;
//        c.weighty = 0.5;
//        c.gridheight = 1;
//
//        c.anchor = GridBagConstraints.CENTER;
//
//        outputPanel.add(outputLabel, c);
//
//        //c.anchor = GridBagConstraints.CENTER;
//
//        c.weightx = 0;
//        c.gridx = 0;
//        c.gridy = 1;
//        c.weighty = 0.3;
//        c.gridwidth = 1;
//        c.ipadx = 30;
//        c.insets = new Insets(0, 0, 0, 0);
//
//        outputPanel.add(mainCoilButton, c);
//
//        c.ipadx = 40;
//        //c.insets = new Insets(35, 110, 0, 0);
//        c.gridx = 1;
//
//        outputPanel.add(cancelButton2, c);
//
//        c.ipadx = 25;
//        c.gridx = 2;
//
//        outputPanel.add(rfLUTButton, c);
//
//        MCTuning.setText("MC Tuning");
//        SourTuning.setText("Source Tuning");
//
//        MCTuning.setHorizontalTextPosition(SwingConstants.LEFT);
//        SourTuning.setHorizontalTextPosition(SwingConstants.LEFT);
//
//        //c.anchor = GridBagConstraints.SOUTH;
//        c.gridy = 2;
//
//        c.gridx = 0;
//
//        c.ipadx = 62;
//        //c.gridy = 3;
//        c.weightx = 1;
//        c.insets = new Insets(0, 0, 0, 0);
//        //c.anchor = GridBagConstraints.CENTER;
//        outputPanel.add(PCVueButton, c);
//
//
//        c.gridx = 1;
//
//        c.insets = new Insets(0, 104, 35, 0);
//        outputPanel.add(SourTuning, c);
//
//        c.insets = new Insets(25, 127, 0, 0);
//        outputPanel.add(MCTuning, c);
//
//        c.weightx = 0.5;
//        c.weighty = 0.3;
//        //c.ipadx = 25;
//        c.insets = new Insets(0, 0, 10, 110);
//        //outputPanel.add(rfLUTButton, c);


//        c.gridx     = 0;
//        c.gridy++;
//        c.gridwidth = 3;
//        c.gridheight = 1;
//        c.weighty   = 0.5;
//        c.weightx   = 0;


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
        c.insets = new Insets(seventyFivePixels, seventyFivePixels, 0, seventyFivePixels);

        title = new TitledBorder("Output");
        title.setTitleJustification(TitledBorder.CENTER);
        title.setTitleFont(new Font("Dialog", Font.BOLD, 20));
        title.setBorder(BorderFactory.createMatteBorder(1,1,1,1, Color.BLACK));
        outputPanel.setBorder(title);

        tuningPanel.add(outputPanel, c);

        c.fill = GridBagConstraints.NONE;


        ////////////////Source section //////////////////////////////

        c.gridx = 2;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 0.5;
        c.gridheight = 1;
        c.insets = new Insets(80, 0, 0, 0);
        SourceLabel.setFont(new Font("Dialog", Font.BOLD, 20));
        SourceLabel.setText("Source");


        c.anchor = GridBagConstraints.NORTH;
        //tuningPanel.add(SourceLabel, c);

        sourceTuningPanel = new JPanel();
        sourceTuningPanel.setLayout(new GridBagLayout());


        /////////////////Filament section   ////////////////////////////////

        c.anchor = GridBagConstraints.NORTHWEST;

        FilFB = new JTextField();
        FilFB.setEditable(false);
        title = new TitledBorder("A");
        title.setTitleJustification(TitledBorder.RIGHT);
        title.setBorder(BorderFactory.createMatteBorder(1,1,1,1, Color.BLACK));
        FilFB.setBorder(title);
        FilFB.setBackground(IbaColors.BT_GRAY);
        FilFB.setHorizontalAlignment(SwingConstants.CENTER);
        FilFB.setFont(new Font("Dialog", Font.BOLD, 20));
        FilFB.setText("-");
        dimension = new Dimension(150, 60);
        FilFB.setPreferredSize(dimension);

        c.insets = new Insets(50, 55, 0, 0);

        sourceTuningPanel.add(FilFB, c);

        c.anchor = GridBagConstraints.WEST;

        FilSP = new JTextField();
        title = new TitledBorder("Fil");
        title.setBorder(BorderFactory.createMatteBorder(1,1,1,1, Color.BLACK));
        FilSP.setBorder(title);
        FilSP.setColumns(7);
        FilSP.setHorizontalAlignment(SwingConstants.CENTER);
        FilSP.setText("-");
        FilSP.addActionListener(this);
        FilSP.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
            }

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    if (Double.parseDouble(FilSP.getText()) >= 125.00 && Double.parseDouble(FilSP.getText()) <= 220.00) {
                        controller.acu.setTagValue(Status.Fil_write, Double.parseDouble(FilSP.getText()));
                        log.warn("Filament set to " + String.format("%.2f", Double.parseDouble(FilSP.getText())));
                        if (beamCurrent != null) {
                            beamCurrent.clear();
                        }
                        if (list != null){
                            list.clear();
                        }
                        outputVariance.setText("Variance: ");
                    } else {
                        log.error("Filament setpoint was out of bounds 125-220A.");
                    }
                    if (Double.parseDouble(FilSP.getText()) < 125.00) {
                        FilSP.setText("125.00");
                    }
                    if (Double.parseDouble(FilSP.getText()) > 220.00) {
                        FilSP.setText("220.00");
                    }
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
            }
        });

        c.anchor = GridBagConstraints.WEST;

        c.insets = new Insets(10, 86, 0, 0);

        sourceTuningPanel.add(FilSP, c);

        Filplus = new JButton("+");
        dimension = new Dimension(25, 20);
        Filplus.setPreferredSize(dimension);
        Filplus.setMargin(new Insets(0, 0, 0, 0));
        Filplus.setFont(new Font("Dialog", Font.BOLD, 15));
        Filplus.addActionListener(this);
        Filplus.setActionCommand("Filplus");

        c.insets = new Insets(18, 173, 0, 0);

        sourceTuningPanel.add(Filplus, c);

        Filminus = new JButton("-");
        Filminus.setPreferredSize(dimension);
        Filminus.setMargin(new Insets(0, 0, 0, 0));
        Filminus.setFont(new Font("Dialog", Font.BOLD, 18));
        Filminus.addActionListener(this);
        Filminus.setActionCommand("Filminus");

        c.insets = new Insets(18, 61, 0, 0);

        sourceTuningPanel.add(Filminus, c);


        //////////////Arc section   ////////////////////////////

        c.anchor = GridBagConstraints.NORTHEAST;

        ArcFB = new JTextField();
        ArcFB.setEditable(false);
        title = new TitledBorder("mA");
        title.setTitleJustification(TitledBorder.RIGHT);
        title.setBorder(BorderFactory.createMatteBorder(1,1,1,1, Color.BLACK));
        ArcFB.setBorder(title);
        ArcFB.setBackground(IbaColors.BT_GRAY);
        ArcFB.setHorizontalAlignment(SwingConstants.CENTER);
        ArcFB.setFont(new Font("Dialog", Font.BOLD, 20));
        ArcFB.setText("-");
        dimension = new Dimension(150, 60);
        ArcFB.setPreferredSize(dimension);

        c.insets = new Insets(50, 0, 0, 55);

        sourceTuningPanel.add(ArcFB, c);

        c.anchor = GridBagConstraints.EAST;

        ArcSP = new JTextField();
        title = new TitledBorder("Arc");
        title.setBorder(BorderFactory.createMatteBorder(1,1,1,1, Color.BLACK));
        ArcSP.setBorder(title);
        ArcSP.setColumns(7);
        ArcSP.setHorizontalAlignment(SwingConstants.CENTER);
        ArcSP.setText("-");
        ArcSP.addActionListener(this);
        ArcSP.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
            }

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    if (Double.parseDouble(ArcSP.getText()) >= 1.00 && Double.parseDouble(ArcSP.getText()) <= 250.00) {
                        controller.acu.setTagValue(Status.Arc_write, Double.parseDouble(ArcSP.getText()));
                        log.warn("[TUNE] Arc set to " + String.format("%.2f", Double.parseDouble(ArcSP.getText())));
                        if (beamCurrent != null) {
                            beamCurrent.clear();
                        }
                        if (list != null){
                            list.clear();
                        }
                        outputVariance.setText("Variance: ");
                    } else {
                        log.error("Arc setpoint was out of bounds 1-250mA.");
                    }
                    if (Double.parseDouble(ArcSP.getText()) < 1.00) {
                        ArcSP.setText("1.00");
                    }
                    if (Double.parseDouble(ArcSP.getText()) > 250.00) {
                        ArcSP.setText("250.00");
                    }
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
            }
        });

        c.anchor = GridBagConstraints.EAST;

        c.insets = new Insets(10, 0, 0, 86);

        sourceTuningPanel.add(ArcSP, c);

        Arcplus = new JButton("+");
        dimension = new Dimension(25, 20);
        Arcplus.setPreferredSize(dimension);
        Arcplus.setMargin(new Insets(0, 0, 0, 0));
        Arcplus.setFont(new Font("Dialog", Font.BOLD, 15));
        Arcplus.addActionListener(this);
        Arcplus.setActionCommand("Arcplus");

        c.insets = new Insets(18, 50, 0, 61);

        sourceTuningPanel.add(Arcplus, c);

        Arcminus = new JButton("-");
        Arcminus.setPreferredSize(dimension);
        Arcminus.setMargin(new Insets(0, 0, 0, 0));
        Arcminus.setFont(new Font("Dialog", Font.BOLD, 18));
        Arcminus.addActionListener(this);
        Arcminus.setActionCommand("Arcminus");

        c.insets = new Insets(18, 0, 0, 173);

        sourceTuningPanel.add(Arcminus, c);


        ArcVolt = new JTextField();
        ArcVolt.setEditable(false);
        title = new TitledBorder("V");
        title.setTitleJustification(TitledBorder.RIGHT);
        title.setBorder(BorderFactory.createMatteBorder(1,1,1,1, Color.BLACK));
        ArcVolt.setBorder(title);
        ArcVolt.setBackground(IbaColors.BT_GRAY);
        ArcVolt.setHorizontalAlignment(SwingConstants.CENTER);
        ArcVolt.setFont(new Font("Dialog", Font.BOLD, 20));
        ArcVolt.setText("-");
        dimension = new Dimension(150, 60);
        ArcVolt.setPreferredSize(dimension);

        c.anchor = GridBagConstraints.SOUTH;
        c.insets = new Insets(0, 0, 40, 0);

        sourceTuningPanel.add(ArcVolt, c);


        c.gridx = 2;
        c.gridy = 0;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 1;
        c.weighty = 0.5;
        c.fill = GridBagConstraints.BOTH;
        c.anchor = GridBagConstraints.CENTER;
        //c.insets    = new Insets(75,50,75,150);
        c.insets = new Insets(seventyFivePixels, fiftyPixels, 0, oneHundredFiftyPixels);

        title = new TitledBorder("Source");
        title.setTitleJustification(TitledBorder.CENTER);
        title.setTitleFont(new Font("Dialog", Font.BOLD, 20));
        title.setBorder(BorderFactory.createMatteBorder(1,1,1,1, Color.BLACK));
        sourceTuningPanel.setBorder(title);

        tuningPanel.add(sourceTuningPanel, c);

        c.fill = GridBagConstraints.NONE;


        ///////////////////////////////////////////////////////////////////////////////////


        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 1;
        c.gridheight = 1;
        c.gridwidth = 1;
        c.anchor = GridBagConstraints.NORTH;
        c.insets = new Insets(0, 0, 80, 0);
//        MagnetLabel.setFont(new Font("Dialog", Font.BOLD, 20));
//        MagnetLabel.setText("Magnet");

        //tuningPanel.add(MagnetLabel, c);


        magnetTuningPanel = new JPanel();
        magnetTuningPanel.setLayout(new GridBagLayout());


        /////Comp coil section /////////////////


//        c.gridwidth = 1;
//        c.gridx = 0;
//
//        CCoilFB =  new JTextField();
//        CCoilFB.setEditable(false);
//        title = new TitledBorder("A");
//        title.setTitleJustification(TitledBorder.RIGHT);
//        CCoilFB.setBorder(title);
//        CCoilFB.setBackground(IbaColors.BT_GRAY);
//        CCoilFB.setHorizontalAlignment(SwingConstants.CENTER);
//        CCoilFB.setFont(new Font("Dialog", Font.BOLD, 20));
//        CCoilFB.setText("13.00");
//        dimension = new Dimension(150,60);
//        CCoilFB.setPreferredSize(dimension);
//
//        c.anchor = GridBagConstraints.NORTH;
//        c.insets    = new Insets(10,0,0,0);
//
//        //tuningPanel.add(CCoilFB, c);
//        magnetTuningPanel.add(CCoilFB, c);
//
//        c.anchor = GridBagConstraints.CENTER;
//
//        CCoilSP = new JTextField();
//        title = new TitledBorder("Comp");
//        CCoilSP.setBorder(title);
//        CCoilSP.setColumns(7);
//        CCoilSP.setHorizontalAlignment(SwingConstants.CENTER);
//        CCoilSP.setText("13.00");
//        CCoilSP.addActionListener(this);
//        CCoilSP.addKeyListener(new KeyListener() {
//            @Override
//            public void keyTyped(KeyEvent e) {}
//            @Override
//            public void keyPressed(KeyEvent e) {
//                if (e.getKeyCode() == KeyEvent.VK_ENTER){
//                    if (Double.parseDouble(CCoilSP.getText()) >= 1.00 && Double.parseDouble(CCoilSP.getText()) <= 16.00) {
//                        controller.acu.setTagValue(Status.CC_write, Double.parseDouble(CCoilSP.getText()));
//                        log.warn("Compensation coil set to " + String.format("%.2f", Double.parseDouble(CCoilSP.getText())));
//                        beamCurrent.clear();
//                        outputVariance.setText("Variance: ");
//                    } else {log.error("Compensation coil setpoint was out of bounds 1-15A.");}
//                    if (Double.parseDouble(CCoilSP.getText()) < 1.00) {
//                        CCoilSP.setText("1.00");
//                    }
//                    if (Double.parseDouble(CCoilSP.getText()) > 15.00) {
//                        CCoilSP.setText("15.00");
//                    }
//                }
//            }
//            @Override
//            public void keyReleased(KeyEvent e) {}
//        });
//
//        c.anchor = GridBagConstraints.CENTER;
//
//        c.insets    = new Insets(60,0,0,0);
//
//        //tuningPanel.add(CCoilSP, c);
//        magnetTuningPanel.add(CCoilSP, c);
//
//        CCoilplus = new JButton("+");
//        dimension = new Dimension(25,20);
//        CCoilplus.setPreferredSize(dimension);
//        CCoilplus.setMargin(new Insets(0, 0, 0, 0));
//        CCoilplus.setFont(new Font("Dialog", Font.BOLD, 15));
//        CCoilplus.addActionListener(this);
//        CCoilplus.setActionCommand("CCoilplus");
//
//        c.insets    = new Insets(68,112,0,0);
//
//        //tuningPanel.add(CCoilplus, c);
//        magnetTuningPanel.add(CCoilplus, c);
//
//        CCoilminus = new JButton("-");
//        CCoilminus.setPreferredSize(dimension);
//        CCoilminus.setMargin(new Insets(0, 0, 0, 0));
//        CCoilminus.setFont(new Font("Dialog", Font.BOLD, 18));
//        CCoilminus.addActionListener(this);
//        CCoilminus.setActionCommand("CCoilminus");
//
//        c.insets    = new Insets(68,0,0,113);
//
//        //tuningPanel.add(CCoilminus, c);
//        magnetTuningPanel.add(CCoilminus, c);
//


        //////////MC section /////////

        c.gridx = 0;
        c.gridy = 0;


        MCoilFB = new JTextField();
        MCoilFB.setEditable(false);
        title = new TitledBorder("A");
        title.setTitleJustification(TitledBorder.RIGHT);
        title.setBorder(BorderFactory.createMatteBorder(1,1,1,1, Color.BLACK));
        MCoilFB.setBorder(title);
        MCoilFB.setBackground(IbaColors.BT_GRAY);
        MCoilFB.setHorizontalAlignment(SwingConstants.CENTER);
        MCoilFB.setFont(new Font("Dialog", Font.BOLD, 20));
        MCoilFB.setText("-");
        dimension = new Dimension(150, 60);
        MCoilFB.setPreferredSize(dimension);

        c.anchor = GridBagConstraints.NORTH;
        c.insets = new Insets(80, 10, 0, 0);

        //tuningPanel.add(MCoilFB, c);
        magnetTuningPanel.add(MCoilFB, c);


        MCoilSP = new JTextField();
        title = new TitledBorder("MCoil");
        title.setBorder(BorderFactory.createMatteBorder(1,1,1,1, Color.BLACK));
        MCoilSP.setBorder(title);
        MCoilSP.setColumns(7);
        MCoilSP.setHorizontalAlignment(SwingConstants.CENTER);
        MCoilSP.setText("-");
        MCoilSP.addActionListener(this);
        MCoilSP.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
            }

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    if (Double.parseDouble(MCoilSP.getText()) >= 735.00 && Double.parseDouble(MCoilSP.getText()) <= 755.00) {
                        controller.acu.setTagValue(Status.MC_write, Double.parseDouble(MCoilSP.getText()));
                        log.warn("Main coil set to " + String.format("%.3f", Double.parseDouble(MCoilSP.getText())));
                        if (beamCurrent != null) {
                            beamCurrent.clear();
                        }
                        if (list != null){
                            list.clear();
                        }
                        outputVariance.setText("Variance: ");
                    } else {
                        log.error("Main coil set point was out of bounds 735-755A.");
                    }
                    if (Double.parseDouble(MCoilSP.getText()) < 735.00) {
                        MCoilSP.setText("735.00");
                    }
                    if (Double.parseDouble(MCoilSP.getText()) > 755.00) {
                        MCoilSP.setText("755.00");
                    }
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
            }
        });

        c.anchor = GridBagConstraints.CENTER;

        c.insets = new Insets(60, 0, 0, 0);

        //tuningPanel.add(MCoilSP, c);
        magnetTuningPanel.add(MCoilSP, c);

        MCoilplus = new JButton("+");
        dimension = new Dimension(25, 20);
        MCoilplus.setPreferredSize(dimension);
        MCoilplus.setMargin(new Insets(0, 0, 0, 0));
        MCoilplus.setFont(new Font("Dialog", Font.BOLD, 15));
        MCoilplus.addActionListener(this);
        MCoilplus.setActionCommand("MCoilplus");

        c.insets = new Insets(68, 112, 0, 0);

        //tuningPanel.add(MCoilplus, c);
        magnetTuningPanel.add(MCoilplus, c);

        MCoilminus = new JButton("-");
        MCoilminus.setPreferredSize(dimension);
        MCoilminus.setMargin(new Insets(0, 0, 0, 0));
        MCoilminus.setFont(new Font("Dialog", Font.BOLD, 18));
        MCoilminus.addActionListener(this);
        MCoilminus.setActionCommand("MCoilminus");

        c.insets = new Insets(68, 0, 0, 112);
        magnetTuningPanel.add(MCoilminus, c);


        dimension = new Dimension(30, 20);

        MCoilStep1 = new JButton(".010");
        MCoilStep1.setPreferredSize(dimension);
        MCoilStep1.setMargin(new Insets(0, 0, 0, 0));
        MCoilStep1.setFont(new Font("Dialog", Font.BOLD, 10));
        MCoilStep1.addActionListener(this);
        MCoilStep1.setActionCommand("MCoilStep1");
        MCoilStep1.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.lightGray));

        c.insets = new Insets(210, 0, 0, 70);
        magnetTuningPanel.add(MCoilStep1, c);

        MCoilStep2 = new JButton(".005");
        MCoilStep2.setPreferredSize(dimension);
        MCoilStep2.setMargin(new Insets(0, 0, 0, 0));
        MCoilStep2.setFont(new Font("Dialog", Font.BOLD, 10));
        MCoilStep2.addActionListener(this);
        MCoilStep2.setActionCommand("MCoilStep2");
        MCoilStep2.setBorder(BorderFactory.createMatteBorder(2, 2, 2, 2, manual));

        c.insets = new Insets(210, 0, 0, 0);
        magnetTuningPanel.add(MCoilStep2, c);

        MCoilStep3 = new JButton(".001");
        MCoilStep3.setPreferredSize(dimension);
        MCoilStep3.setMargin(new Insets(0, 0, 0, 0));
        MCoilStep3.setFont(new Font("Dialog", Font.BOLD, 10));
        MCoilStep3.addActionListener(this);
        MCoilStep3.setActionCommand("MCoilStep3");
        MCoilStep3.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.lightGray));

        c.insets = new Insets(210, 70, 0, 0);
        magnetTuningPanel.add(MCoilStep3, c);


        JLabel MCoilStep = new JLabel("Step");
        MCoilStep.setFont(new Font("Dialog", Font.BOLD, 12));
        c.insets = new Insets(155, 0, 0, 0);
        magnetTuningPanel.add(MCoilStep, c);


        /////Harmonic coil 1-3 section ////////////////


//        c.gridx = 1;
//
//
//
//        HCoil1FB =  new JTextField();
//        HCoil1FB.setEditable(false);
//        title = new TitledBorder("A");
//        title.setTitleJustification(TitledBorder.RIGHT);
//        HCoil1FB.setBorder(title);
//        HCoil1FB.setBackground(IbaColors.BT_GRAY);
//        HCoil1FB.setHorizontalAlignment(SwingConstants.CENTER);
//        HCoil1FB.setFont(new Font("Dialog", Font.BOLD, 20));
//        HCoil1FB.setText("0.55");
//        dimension = new Dimension(150,60);
//        HCoil1FB.setPreferredSize(dimension);
//
//        c.anchor = GridBagConstraints.NORTHEAST;
//        c.insets    = new Insets(10,0,0,10);
//
//        //tuningPanel.add(HCoil1FB, c);
//        magnetTuningPanel.add(HCoil1FB, c);
//
//
//        HCoil1SP = new JTextField();
//        title = new TitledBorder("Harm1-3");
//        HCoil1SP.setBorder(title);
//        HCoil1SP.setColumns(7);
//        HCoil1SP.setHorizontalAlignment(SwingConstants.CENTER);
//        HCoil1SP.setText("0.55");
//        HCoil1SP.addActionListener(this);
//        HCoil1SP.addKeyListener(new KeyListener() {
//            @Override
//            public void keyTyped(KeyEvent e) {}
//            @Override
//            public void keyPressed(KeyEvent e) {
//                if (e.getKeyCode() == KeyEvent.VK_ENTER){
//                    if (Double.parseDouble(HCoil1SP.getText()) >= -1.50 && Double.parseDouble(HCoil1SP.getText()) <= 1.50) {
//                        controller.acu.setTagValue(Status.HC1_write, Double.parseDouble(HCoil1SP.getText()));
//                        log.warn("Harmonic coil set to " + String.format("%.2f", Double.parseDouble(HCoil1SP.getText())));
//                        beamCurrent.clear();
//                        outputVariance.setText("Variance: ");
//                    } else {log.error("Harmonic coil setpoint was out of bounds (-1.50)-1.50A.");}
//                    if (Double.parseDouble(HCoil1SP.getText()) < -1.50) {
//                        HCoil1SP.setText("-1.50");
//                    }
//                    if (Double.parseDouble(HCoil1SP.getText()) > 1.50) {
//                        HCoil1SP.setText("1.50");
//                    }
//                }
//            }
//            @Override
//            public void keyReleased(KeyEvent e) {}
//        });
//
//        c.anchor = GridBagConstraints.EAST;
//
//        c.insets    = new Insets(60,0,0,40);
//
//        //tuningPanel.add(HCoil1SP, c);
//        magnetTuningPanel.add(HCoil1SP, c);
//
//        HCoil1plus = new JButton("+");
//        dimension = new Dimension(25,20);
//        HCoil1plus.setPreferredSize(dimension);
//        HCoil1plus.setMargin(new Insets(0, 0, 0, 0));
//        HCoil1plus.setFont(new Font("Dialog", Font.BOLD, 15));
//        HCoil1plus.addActionListener(this);
//        HCoil1plus.setActionCommand("HCoil1plus");
//
//        c.insets    = new Insets(68,0,0,15);
//
//        //tuningPanel.add(HCoil1plus, c);
//        magnetTuningPanel.add(HCoil1plus, c);
//
//        HCoil1minus = new JButton("-");
//        HCoil1minus.setPreferredSize(dimension);
//        HCoil1minus.setMargin(new Insets(0, 0, 0, 0));
//        HCoil1minus.setFont(new Font("Dialog", Font.BOLD, 18));
//        HCoil1minus.addActionListener(this);
//        HCoil1minus.setActionCommand("HCoil1minus");
//
//        c.insets    = new Insets(68,0,0,127);
//
//        //tuningPanel.add(HCoil1minus, c);
//        magnetTuningPanel.add(HCoil1minus, c);
//
//
//        //////////Harmonic coil 2-4 section /////////////////////
//
//        c.gridwidth = 1;
//        c.gridx = 2;
//
//        HCoil2FB =  new JTextField();
//        HCoil2FB.setEditable(false);
//        title = new TitledBorder("A");
//        title.setTitleJustification(TitledBorder.RIGHT);
//        HCoil2FB.setBorder(title);
//        HCoil2FB.setBackground(IbaColors.BT_GRAY);
//        HCoil2FB.setHorizontalAlignment(SwingConstants.CENTER);
//        HCoil2FB.setFont(new Font("Dialog", Font.BOLD, 20));
//        HCoil2FB.setText("0.25");
//        dimension = new Dimension(150,60);
//        HCoil2FB.setPreferredSize(dimension);
//
//        c.anchor = GridBagConstraints.NORTH;
//        c.insets    = new Insets(10,0,0,0);
//
//        //tuningPanel.add(HCoil2FB, c);
//        magnetTuningPanel.add(HCoil2FB, c);
//
//        c.anchor = GridBagConstraints.CENTER;
//
//        HCoil2SP = new JTextField();
//        title = new TitledBorder("Harm2-4");
//        HCoil2SP.setBorder(title);
//        HCoil2SP.setColumns(7);
//        HCoil2SP.setHorizontalAlignment(SwingConstants.CENTER);
//        HCoil2SP.setText("0.25");
//        HCoil2SP.addActionListener(this);
//        HCoil2SP.addKeyListener(new KeyListener() {
//            @Override
//            public void keyTyped(KeyEvent e) {}
//            @Override
//            public void keyPressed(KeyEvent e) {
//                if (e.getKeyCode() == KeyEvent.VK_ENTER){
//                    if (Double.parseDouble(HCoil2SP.getText()) >= -1.50 && Double.parseDouble(HCoil2SP.getText()) <= 1.50) {
//                        controller.acu.setTagValue(Status.HC2_write, Double.parseDouble(HCoil2SP.getText()));
//                        log.warn("Harmonic coil set to " + String.format("%.2f", Double.parseDouble(HCoil2SP.getText())));
//                        beamCurrent.clear();
//                        outputVariance.setText("Variance: ");
//                    } else {log.error("Harmonic coil setpoint was out of bounds (-1.50)-1.50A.");}
//                    if (Double.parseDouble(HCoil2SP.getText()) < -1.50) {
//                        HCoil2SP.setText("-1.50");
//                    }
//                    if (Double.parseDouble(HCoil2SP.getText()) > 1.50) {
//                        HCoil2SP.setText("1.50");
//                    }
//                }
//            }
//            @Override
//            public void keyReleased(KeyEvent e) {}
//        });
//
//        c.anchor = GridBagConstraints.CENTER;
//
//        c.insets    = new Insets(60,0,0,0);
//
//        //tuningPanel.add(HCoil2SP, c);
//        magnetTuningPanel.add(HCoil2SP, c);
//
//        HCoil2plus = new JButton("+");
//        dimension = new Dimension(25,20);
//        HCoil2plus.setPreferredSize(dimension);
//        HCoil2plus.setMargin(new Insets(0, 0, 0, 0));
//        HCoil2plus.setFont(new Font("Dialog", Font.BOLD, 15));
//        HCoil2plus.addActionListener(this);
//        HCoil2plus.setActionCommand("HCoil2plus");
//
//        c.insets    = new Insets(68,112,0,0);
//
//        //tuningPanel.add(HCoil2plus, c);
//        magnetTuningPanel.add(HCoil2plus, c);
//
//        HCoil2minus = new JButton("-");
//        HCoil2minus.setPreferredSize(dimension);
//        HCoil2minus.setMargin(new Insets(0, 0, 0, 0));
//        HCoil2minus.setFont(new Font("Dialog", Font.BOLD, 18));
//        HCoil2minus.addActionListener(this);
//        HCoil2minus.setActionCommand("HCoil2minus");
//
//        c.insets    = new Insets(68,0,0,113);
//
//        //tuningPanel.add(HCoil2minus, c);
//        magnetTuningPanel.add(HCoil2minus, c);
//
//


        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 1;
        c.weighty = 0.5;
        c.fill = GridBagConstraints.BOTH;
        c.anchor = GridBagConstraints.CENTER;

        //c.insets    = new Insets(75,150,75,50);
        c.insets = new Insets(seventyFivePixels, oneHundredFiftyPixels, 0, fiftyPixels);

        title = new TitledBorder("Magnet");
        title.setTitleJustification(TitledBorder.CENTER);
        title.setTitleFont(new Font("Dialog", Font.BOLD, 20));
        title.setBorder(BorderFactory.createMatteBorder(1,1,1,1, Color.BLACK));
        magnetTuningPanel.setBorder(title);

        tuningPanel.add(magnetTuningPanel, c);

        c.fill = GridBagConstraints.NONE;


        ////////////////////Adding graph trend of beam current and arc voltage //////////////////////


        beamChart = new DynamicTimeSeriesChart("ICCyclo");

        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth = 3;
        c.gridheight = 2;
        c.weightx = 1;
        c.weighty = 1;
        c.fill = GridBagConstraints.BOTH;
        c.anchor = GridBagConstraints.CENTER;


        //c.insets    = new Insets(0,150,75,150);
        c.insets = new Insets(0, oneHundredPixels + 10, fiftyPixels / 10, oneHundredPixels - 5);

        //title = new TitledBorder("Instructions");
        //title.setTitleJustification(TitledBorder.CENTER);
        //title.setTitleFont(new Font("Dialog", Font.BOLD, 20));
        //tuningInstructionPanel.setBorder(title);

        tuningPanel.add(beamChart, c);


        ////////////////////Adding instructions + update from PCVue button on second row ////////////////

        tuningInstructionPanel = new JPanel();
        tuningInstructionPanel.setLayout(new GridBagLayout());


        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 3;
        c.gridheight = 1;
        c.weighty = 0;
        c.weightx = 1;
        //c.insets = new Insets(10,10,0,0);
        c.anchor = GridBagConstraints.WEST;

        JTextPane instructions1 = new JTextPane();
        instructions1.setBackground(IbaColors.BT_GRAY);
        instructions1.setFont(new Font("Dialog", Font.BOLD, 12));
        instructions1.setText("1.) BSS Operating Mode must be set to manual via BCP to tune.");
        c.insets = new Insets(0, 30, 0, 0);
        tuningInstructionPanel.add(instructions1, c);
        //c.gridy ++;

        JTextPane instructions2 = new JTextPane();
        instructions2.setBackground(IbaColors.BT_GRAY);
        instructions2.setAlignmentX(SwingConstants.RIGHT);
        instructions2.setFont(new Font("Dialog", Font.BOLD, 12));
        instructions2.setText("4.) 'Set RF' will set Vdee2 = 56kV and move the degrader to beamstop position.");
        c.anchor = GridBagConstraints.EAST;
        c.insets = new Insets(0, 0, 0, 100);
        tuningInstructionPanel.add(instructions2, c);
        c.gridy++;

        JTextPane instructions3 = new JTextPane();
        instructions3.setBackground(IbaColors.BT_GRAY);
        instructions3.setFont(new Font("Dialog", Font.BOLD, 12));
        instructions3.setText("2.) You must press 'Update from PCVue' to get live values into adaPT Assist.");
        c.insets = new Insets(0, 30, 0, 0);
        c.anchor = GridBagConstraints.WEST;
        tuningInstructionPanel.add(instructions3, c);
        // c.gridy ++;

        JTextPane instructions4 = new JTextPane();
        instructions4.setBackground(IbaColors.BT_GRAY);
        instructions4.setFont(new Font("Dialog", Font.BOLD, 12));
        instructions4.setText("5.) MC +/- buttons use the step size selected under the Magnet section.");
        c.insets = new Insets(0, 0, 0, 143);
        c.anchor = GridBagConstraints.EAST;
        tuningInstructionPanel.add(instructions4, c);
        c.gridy++;

        JTextPane instructions5 = new JTextPane();
        instructions5.setBackground(IbaColors.BT_GRAY);
        instructions5.setFont(new Font("Dialog", Font.BOLD, 12));
        instructions5.setText("3.) You must confirm that MC tuning is off to be able to modify the MC setpoint, same as in PCVue.");
        c.insets = new Insets(0, 30, 0, 0);
        c.anchor = GridBagConstraints.WEST;
        tuningInstructionPanel.add(instructions5, c);
        //c.gridy ++;

        JTextPane instructions6 = new JTextPane();
        instructions6.setBackground(IbaColors.BT_GRAY);
        instructions6.setFont(new Font("Dialog", Font.BOLD, 12));
        instructions6.setText("6.) Idle before performing a RF Look Up Table or to end tuning.");
        c.insets = new Insets(0, 0, 0, 195);
        c.anchor = GridBagConstraints.EAST;
        tuningInstructionPanel.add(instructions6, c);


        c = new GridBagConstraints();


        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth = 3;
        c.gridheight = 1;
        c.weighty = 0;
        c.weightx = 0;
        c.ipadx = 30;
        c.insets = new Insets(0, 0, 0, 0);
        c.anchor = GridBagConstraints.CENTER;

        //tuningInstructionPanel.add(PCVueButton, c);

        //ImageIcon icon = new ImageIcon("checkmark.png");
        //JLabel thumb = new JLabel();
        //thumb.setIcon(icon);
        //c.insets = new Insets(0,0,0,0);
        //tuningInstructionPanel.add(thumb);


        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 2;
        c.gridwidth = 3;
        c.gridheight = 1;
        c.weightx = 1;
        c.weighty = 0.5;
        c.fill = GridBagConstraints.BOTH;
        //c.anchor = GridBagConstraints.CENTER;


        //c.insets    = new Insets(0,150,75,150);
        c.insets = new Insets(0, oneHundredFiftyPixels, fiftyPixels, oneHundredFiftyPixels);

        title = new TitledBorder("Instructions");
        title.setTitleJustification(TitledBorder.CENTER);
        title.setTitleFont(new Font("Dialog", Font.BOLD, 20));
        title.setBorder(BorderFactory.createMatteBorder(1,1,1,1, Color.BLACK));
        tuningInstructionPanel.setBorder(title);

        //tuningPanel.add(tuningInstructionPanel, c);

        c.fill = GridBagConstraints.NONE;


        ////// MC tuning status panel

        c.insets = new Insets(0, 0, 0, 0);
        c.gridwidth = 1;
        c.gridx = 0;
        c.gridy = 0;
        c.ipady = 0; // reset ipady
        numElem = Status.CycloTuning_names.length;
        MCTuningPanel = new JPanel();
        title = new TitledBorder("Cyclotron (BPS)");
        title.setBorder(BorderFactory.createMatteBorder(1,1,1,1, Color.BLACK));
        MCTuningPanel.setBorder(title);
        MCTuningPanel.setLayout(new GridBagLayout());
        for (String str : Status.CycloTuning_names) {
            if (c.gridx > 2) {
                c.weightx = 0.5;
            } else {
                c.weightx = 1;
            }
            tempLabel = new JLabel(str, JLabel.CENTER);
            tempLabel.setVerticalAlignment(JLabel.BOTTOM);
            MCTuningPanel.add(tempLabel, c);
            c.gridx += 1;
        }

        // new row
        c.gridx = 0;
        c.weightx = 1;
        c.gridy = 1;
        c.fill = GridBagConstraints.BOTH;
        c.ipady = IPADY_DEFAULT;
        c.insets = new Insets(0, 0, 0, 0);
        MCTuningText = new JTextField[numElem];
        for (int i = 0; i < numElem; i++) {
            c.gridx = i;
            MCTuningText[i] = new JTextField();
            MCTuningText[i].setEditable(false);
            MCTuningText[i].setHorizontalAlignment(SwingConstants.CENTER);
            MCTuningText[i].setBackground(IbaColors.BT_GRAY);
            if (i == numElem-1) {
                MCTuningText[i].setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.BLACK));
            }else{
                MCTuningText[i].setBorder(BorderFactory.createMatteBorder(1, 1, 1, 0, Color.BLACK));
            }
            MCTuningPanel.add(MCTuningText[i], c);
        }

        c = new GridBagConstraints();
        c.fill      = GridBagConstraints.BOTH;
        c.ipady     = 0; // reset ipady
        c.weightx   = 1; // full width
        c.gridx     = 0;
        c.insets = new Insets(5,0,0,0);
        c.gridy = 3;
        c.gridheight =1;
        c.gridwidth = 3;
        c.weighty = 0;

        //tuningPanel.add(MCTuningPanel, c);

        //c.anchor = GridBagConstraints.PAGE_END;
//        c.gridy = 4;
//        c.gridheight = 2;
//        c.weighty = 0.3;
//        tuningPanel.add(logPanel, c);
//
//        tuningPanel.validate();


        /////////////////Interlock panel////////////////////////////////////////


        interlockPanel_L = new JPanel();
        interlockPanel_L.setLayout(new GridLayout(5, 1));
        interlockPanel_L.setBackground(IbaColors.BT_GRAY);

        interlockPanel_R = new JPanel();
        interlockPanel_R.setBackground(IbaColors.BT_GRAY);

        c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1;
        c.weighty = 1;

        interlockPanel.add(interlockPanel_L, c);

        c.weightx = 10;

        interlockPanel.add(interlockPanel_R, c);


        JButton btn0 = new JButton("SRCU0");
        JButton btn1 = new JButton("SRCU1");
        JButton btn2 = new JButton("SRCU2");
        JButton btn3 = new JButton("SRCU3");
        JButton btn4 = new JButton("SRCU4");


        interlockPanel_L.add(btn0);
        interlockPanel_L.add(btn1);
        interlockPanel_L.add(btn2);
        interlockPanel_L.add(btn3);
        interlockPanel_L.add(btn4);


        CardLayout card = new CardLayout();
        Container container = interlockPanel_R;
        container.setLayout(card);


        JPanel srcu0 = new JPanel();
        JPanel srcu1 = new JPanel();
        JPanel srcu2 = new JPanel();
        JPanel srcu3 = new JPanel();
        JPanel srcu4 = new JPanel();


        srcu0.setBackground(IbaColors.BT_GRAY);
        srcu1.setBackground(IbaColors.BT_GRAY);
        srcu2.setBackground(IbaColors.BT_GRAY);
        srcu3.setBackground(IbaColors.BT_GRAY);
        srcu4.setBackground(IbaColors.BT_GRAY);


        srcu0.setBorder(BorderFactory.createMatteBorder(5, 5, 5, 5, Color.darkGray));
        srcu1.setBorder(BorderFactory.createMatteBorder(5, 5, 5, 5, new Color(237, 67, 55)));
        srcu2.setBorder(BorderFactory.createMatteBorder(5, 5, 5, 5, new Color(255, 252, 127)));
        srcu3.setBorder(BorderFactory.createMatteBorder(5, 5, 5, 5, Color.decode("#4DBD33")));
        srcu4.setBorder(BorderFactory.createMatteBorder(5, 5, 5, 5, Color.decode("#236B8E")));


        srcu0.add(new JLabel("Srcu 0"));
        srcu1.add(new JLabel("Srcu 1"));
        srcu2.add(new JLabel("Srcu 2"));
        srcu3.add(new JLabel("Srcu 3"));
        srcu4.add(new JLabel("Srcu 4"));


        interlockPanel_R.add(srcu0, "0");
        interlockPanel_R.add(srcu1, "1");
        interlockPanel_R.add(srcu2, "2");
        interlockPanel_R.add(srcu3, "3");
        interlockPanel_R.add(srcu4, "4");


        btn0.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                card.show(container, "0");
            }
        });

        btn1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                card.show(container, "1");
            }
        });

        btn2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                card.show(container, "2");
            }
        });

        btn3.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                card.show(container, "3");
            }
        });

        btn4.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                card.show(container, "4");
            }
        });


        ///////////////////////////////////////////////////////////////////


        c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.ipady = 0; // reset ipady
        c.weightx = 1; // full width
        c.weighty = 1; // full width
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 3;
        c.gridheight = 2;

        mainPanel.add(mEsbtsPanel, c);
        c.gridx = 3;
        c.ipadx = 0;
        c.ipady = 0;
        c.weightx = 2; // full width
        c.weighty = 2; // full width
        c.gridheight = 1;
        //mainPanel.add(mScanningControllerPanel, c);

        mBeamTimer = new TimerPanel();
        title = new TitledBorder("Beam Timer");
        title.setTitleFont(bigTitle);
        title.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.lightGray));
        mBeamTimer.setBorder(title);

        mainPanel.add(mBeamTimer, c);
        c.gridy = 1;
        mainPanel.add(mSchedulerPanel, c);

        c.weighty = 0;
        c.weightx = 1;
        c.insets = new Insets(5, 0, 0, 0);  //top padding
        c.gridy++;
        // mainPanel.add(bcreuPanel, c);
        // c.gridy     = 2;
        c.gridwidth = 6;
        c.gridx = 0;
        mainPanel.add(bcreuPanel, c);
        c.gridy++;
        mainPanel.add(blePanel, c);
        //c.gridy = 3;
        //c.gridwidth = 3;
        //c.gridheight = 20;
        c.weighty = 20;   //request any extra vertical space
        c.anchor = GridBagConstraints.PAGE_END; //bottom of space
        c.fill = GridBagConstraints.BOTH;
        c.gridy = 4;
        //c.gridwidth = 3;
        //c.gridheight = 1;
        //c.weighty = 1;   //request any extra vertical space
        //c.anchor = GridBagConstraints.PAGE_END; //bottom of space
        //c.fill = GridBagConstraints.BOTH;
        //Dimension dim = new Dimension(1920, 300);
        //logPanel.setPreferredSize(dim);
        //mainPanel.add(logPanel, c);
        this.add(mainPanel);


        // mAutomaticPanel.add(alignMeButton, c);


        //mRestorePanel.setLayout(new GridBagLayout());


        // Row of buttons:
        c.insets = new Insets(30, 0, 0, 0);     // reset to just the top padding
        c.ipady = IPADY_DEFAULT;
        c.ipadx = 20;
        c.gridwidth = 2;
        c.gridheight = 1;
        c.weightx = 0;
        c.weighty = 1;
        c.fill = 0;
        c.gridx = 2;
        c.gridy = 1;
        // SAVE ME BUTTON ADDED -AMO
        c.anchor = GridBagConstraints.CENTER;
        //mRestorePanel.add(saveMeButton, c);


        c.anchor = GridBagConstraints.CENTER;


        c.insets = new Insets(0, 0, 0, 0);     // reset to just the top padding
        c.ipadx = 0;
        c.gridy = 0;


        JLabel tempLabel2;
        c.fill = GridBagConstraints.BOTH;


        c.insets = new Insets(15, 0, 0, 0);  //top padding
        c.gridwidth = 1;
        c.gridx = 0;
        c.gridy++;
        c.ipady = 0;
//        c.weightx = 0;
//        displayPanel.add(new JLabel("<html><b> Magnets</b></html>", JLabel.LEFT), c);
//        c.weightx = 1;
        for (String str : Status.Magnet_names) {
            c.gridx++;
            tempLabel2 = new JLabel(str, JLabel.CENTER);
            tempLabel2.setVerticalAlignment(JLabel.BOTTOM);
            mRestorePanel.add(tempLabel2, c);
        }
        // new row
        c.insets = new Insets(0, 0, 0, 0);  //top padding
        c.ipady = IPADY_DEFAULT;
        c.gridx = 0;
        c.gridy++;
        c.weightx = 0;
        mRestorePanel.add(new JLabel("Current Setpoint (A): ", JLabel.RIGHT), c);
        c.weightx = 1;
        for (JTextField text : oldCurrText2) {
            c.gridx++;
            mRestorePanel.add(text, c);
        }
        // new row
        c.gridx = 0;
        c.gridy++;
        c.weightx = 0;
        mRestorePanel.add(new JLabel("Safe Setpoint (A): ", JLabel.RIGHT), c);
        c.weightx = 1;
        for (JTextField text : newCurrText2) {
            c.gridx++;
            mRestorePanel.add(text, c);
        }


        if (ImagePath.setBundlePath("src/main/resources/images")) {
            Debug.user("Image path found");
        } else {
            Debug.error("Image path not found");
        }

        this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        //this.setSize(new Dimension(screenSize.width / 2, screenSize.height * 3 / 4));
        this.setSize(fullscreen);
        //this.setLocation(screenSize.width / 4, screenSize.height / 12);
//		this.setResizable(false);
        this.setTitle("Service Beam GUI");
//		URL logoUrl=Main.class.getClassLoader().getResource("iba-logo.gif");
//		setIconImage(Toolkit.getDefaultToolkit().getImage(logoUrl));
        //Adding pack() to properly set GUI
//        this.pack();
//        this.setVisible(true);


        // Initialize controller
        controller.initialize();

        //ApplicationContext context = new ClassPathXmlApplicationContext("xml/config/bms/bms/controller/impl/properties/ui.service.uniNozzle.xml");

        //BeanFactory beanFactory = new XmlBeanFactory(new ClassPathResource("config/pts/bmsTcrServiceScreens/container.xml"));

//       Properties props = new Properties(new ClassPathResource("config/bms/beamCommonProcess/container.xml"));
//       BeanWrapper wrapper = new BeanWrapperImpl();
//            // will throw an exception if the Properties object
//            // contains any unknown keys
//        wrapper.setPropertyValues(props);

        //TcuVariableCollimator vceu =  (TcuVariableCollimator) context.getBean("abstractTcuDeviceEndPoint");

        //BeanFactory beanFactory = new XmlBeanFactory(new ClassPathResource("config/bms/beamCommonProcess/container.xml"));

//        Resource resource = new ClassPathResource("config/bms/beamCommonProcess/default.properties");
//        Properties props = PropertiesLoaderUtils.loadProperties(resource);
//        BeanWrapper wrapper = new BeanWrapperImpl(beanFactory.getBean("esBtsController"));
//            // will throw an exception if the Properties object
//            // contains any unknown keys
//        wrapper.setPropertyValues(props);

 /**       BeanFactory beanFactory = new XmlBeanFactory(new ClassPathResource("config/bms/beamCommonProcess/container.xml"));  */
        //BeanWrapper wrapper = new BeanWrapperImpl(beanFactory.getBean("esBtsController"));

        //EsBtsControllerImpl esbtscontroller = new EsBtsControllerImpl();

        //BeamlineConfig beamline = (BeamlineConfig) beanFactory.getBean("beamlineConfig");
        //esbtscontroller.setBeamlinesInfrastructure((BeamlinesInfrastructure)beanFactory.getBean("beamlinesInfrastructure"));

        //PropertyPlaceholderConfigurer configurer = (PropertyPlaceholderConfigurer)beanFactory.getBean("placeholderConfig");
        //configurer.postProcessBeanFactory(beanFactory);
        //EsBtsControllerImpl EsbtsController = (EsBtsControllerImpl) beanFactory.getBean("esBtsControllerJmx");


        //TcuVariableCollimator vceu =  (TcuVariableCollimator) context.getBean("abstractTcuDeviceEndPoint");

        beamSchedulerPanel = new BeamSchedulerPanel(Controller.beam.beamScheduler, new BeamSchedulerControl() {
            @Override
            public Set<String> getBeamSupplyPointsIds() {
                return ImmutableSet.of("IBTR3-90");
            }

            @Override
            public void setAutomaticScheduling(boolean pAutomaticScheduling) {
                Controller.beam.beamScheduler.setAutomaticScheduling(pAutomaticScheduling);
            }

            @Override
            public void cancelPendingBeamRequests() {
                Controller.beam.beamScheduler.cancelPendingBeamRequests();
            }

            @Override
            public void cancelPendingBeamRequests(BeamPriority pPriority) {
                Controller.beam.beamScheduler.cancelPendingBeamRequests(pPriority);
            }

            @Override
            public void stepUpBeamRequest(String pBeamSupplyPointId) {
                Controller.beam.beamScheduler.stepUpBeamRequest(pBeamSupplyPointId);
            }

            @Override
            public void stepDownBeamRequest(String pBeamSupplyPointId) {
                Controller.beam.beamScheduler.stepDownBeamRequest(pBeamSupplyPointId);
            }

            @Override
            public void acceptBeamRequest(String pBeamSupplyPointId) {
                Controller.beam.beamScheduler.acceptBeamRequest(pBeamSupplyPointId);
            }

            @Override
            public void rejectBeamRequest(String pBeamSupplyPointId) {
                Controller.beam.beamScheduler.rejectBeamRequest(pBeamSupplyPointId);
            }
        });
       // beamSchedulerPanel.setPreferredSize(new Dimension(800,800));

        c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.ipady = 0; // reset ipady
//        c.weightx = 1; // full width
//        c.weighty = 2; // full width
        c.gridx = 3;
        c.gridy = 2;
//        c.gridwidth = 2;
//        c.gridheight = 2;

//        mainPanel.add(mSchedulerPanel, c);
        c.gridx = 0;
        c.gridy = 2;
        c.ipadx = 0;
        c.ipady = 0;
        c.weightx = 1; // full width
        c.weighty = 0.5; // full width
        c.gridwidth = 1;
        c.gridheight = 2;
        //mainPanel.add(mScanningControllerPanel, c);
        mSchedulerPanel.add(beamSchedulerPanel.createQueuePanel(), c);

        c.gridx = 1;
        c.gridheight = 1;
        c.weighty = 0.5;
        mSchedulerPanel.add(beamSchedulerPanel.createBeamRequestPanel(), c);
        c.gridy = 3;
        mSchedulerPanel.add(beamSchedulerPanel.createAllocatedPanel(), c);




//        mBDSPanel.add(beamSchedulerPanel.createBeamRequestPanel());
//        mBDSPanel.add(beamSchedulerPanel.createAllocatedPanel());
//
//        VceuPanel = (TcuVceuPanel) beanFactory.getBean("tcuVceuPanel");
//
//        mBDSPanel.add("VCEU", VceuPanel);
//        mBDSPanel.add("SSEU", new TcuSseuPanel());
//        mBDSPanel.add("SMEU", new ScanningMagnetsPanel());
        //log.warn("controller initialize complete");
        //refreshTargets();
        //log.warn("refresh targets complete");

//        double[] dbl = controller.getSafeCurrents();
//        for (int i=0; i<4; i++){
//            newCurrText2[i].setText(String.valueOf(dbl[i]));
//        }


//        try {
//            SourTuning.setSelected(controller.sourceTuning());
//            MCTuning.setSelected(controller.mainCoilTuning());
//        }catch (Exception e){
//            e.printStackTrace();
//        }

        MCTuning.addItemListener((ItemListener) this);
        SourTuning.addItemListener((ItemListener) this);


        (mTask = new ConstantWorker()).execute();
        //(mTask2 = new ConstantWorker2()).execute();
        (mBcreuProxy = new BcreuWorker()).execute();
        //(mSetpointWorker = new SetpointWorker()).execute();


        controller.feedbackClient.retreiveMcrFeedbacks();

        this.pack();
        this.setVisible(true);

//        try{
//            getDegraderCurrent();
//        } catch (Exception e){
//            log.warn("Registered OPC open interface tag");
//        }


        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent event) {
                exitProcedure();
            }
        });
    }

    public static EsbtsPanel getEsbtsPanel() {
        return mEsbtsPanel;
    }

    /***********************************************

     END of GUI SETUP

     ***********************************************/


    public void exitProcedure() {
        log.info("Preparing to exit...");
        if (readyForServiceBeam){
            BeamAllocation allocation = Controller.beam.beamScheduler.getCurrentBeamAllocation();
            if (allocation != null) {
                if(allocation.getBeamSupplyPointId().contains("IBTR3")){
                    //Controller.beam.bssController.startIdleActivity(Controller.beam.beamScheduler.getCurrentBeamAllocation().getBeamSupplyPointId());
                    //controller.prepareForTreatment();
                    Controller.beam.bpsController.startIdleActivity();
                    Controller.beam.smpsController.startIdleActivity();
                    try {
                        Controller.beam.bssController.startDisableBeamActivity(Controller.beam.beamScheduler.getCurrentBeamAllocation().getBeamSupplyPointId());
                        Thread.sleep(1000);
                        if (Controller.S2E.getPosition() == Insertable.Position.MOVING || Controller.S2E.getPosition() == Insertable.Position.INSERTED) {
                            //do nothing
                        } else {
                            Controller.ecubtcu.bsInsert("S2E");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    Controller.beam.beamScheduler.releaseBeam(Controller.beam.beamScheduler.getCurrentBeamAllocation().getBeamSupplyPointId(), false);
                }
            } else {
                Controller.beam.bpsController.startIdleActivity();
                controller.prepareForTreatment();
                Controller.beam.smpsController.startIdleActivity();
            }

        if (controller.bcreu.isConnected()) {
            Controller.bcreu.disconnect();
        }
//            if (controller.beam.bssController.getOperatingMode() == OperatingMode.MANUAL) {
//                controller.prepareForTreatment();
//            }
        }
        this.dispose();
        System.exit(0);
    }


    private synchronized void setStatus(int status) {
        switch (this.status = status) {
        }
    }

    public void updateGUI(final int status) {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    updateGUI(status);
                }
            });
            return;
        }
        //Now edit your gui objects

//        GridBagConstraints c = new GridBagConstraints();
//        c.fill = GridBagConstraints.BOTH;
//        c.ipady = 0; // reset ipady
//        c.weightx = 1; // full width
//        c.gridx = 0;
//        c.weighty = 0;
//        c.insets = new Insets(5, 0, 0, 0);  //top padding
//
//                if (status == 1) {
//                    mainPanel.remove(MCTuningPanel);
//                    c.gridheight = 1;
//                    c.weighty = 0;
//                    c.gridy = 1;
//                    mainPanel.add(bcreuPanel, c);
//                    c.gridy = 2;
//                    mainPanel.add(cycloPanel, c);
//
//                    c.gridy = 3;
//                    c.gridwidth = 3;
//                    c.gridheight = 5;
//                    c.weighty = 3;   //request any extra vertical space
//                    c.anchor = GridBagConstraints.PAGE_END; //bottom of space
//                    //c.fill = GridBagConstraints.BOTH;
//                    mainPanel.add(logPanel, c);
//                    mainPanel.updateUI();
//                }
//                if (status == 2) {
//                    mainPanel.remove(bcreuPanel);
//                    mainPanel.remove(cycloPanel);
//                    c.gridheight = 1;
//                    c.weighty = 0;
//                    c.gridy = 1;
//                    mainPanel.add(MCTuningPanel, c);
//
//                    c.gridy = 2;
//                    //c.gridwidth = 3;
//                    c.gridheight = 4;
//                    c.weighty = 1.25;   //request any extra vertical space
//                    c.anchor = GridBagConstraints.PAGE_END; //bottom of space
//                    mainPanel.add(logPanel, c);
//                    mainPanel.updateUI();
//                }
    }


    /**
     * Returns a sample BPM dataset.
     *
     * @return The dataset.
     */
    private static BpmDataset createBpmDataset(int numChannels) {
        // Double initialized with values set to 0.0d
        double[] values = new double[numChannels];
        BpmDataset bpmData = new BpmDataset();

        bpmData.addSeries("BPM 1", values);
        bpmData.addSeries("BPM 2", values);
        return bpmData;
    }

    /**
     * Returns a sample BPM dataset.
     *
     * @return The dataset.
     */
    private static GaussianDataset createGaussDataset() {
        GaussianDataset gaussData = new GaussianDataset();

        gaussData.addSeries("Fit 1", 0, 0, 1);
        gaussData.addSeries("Fit 2", 0, 0, 1);
        return gaussData;
    }

    /**
     * Creates a sample chart.
     *
     * @param bpmDataset the dataset.
     * @return The chart.
     */
    private static JFreeChart createChart(BpmDataset bpmDataset, GaussianDataset gaussDataset, PlotOrientation orient) {
        JFreeChart chart = ChartFactory.createHistogram(null, null, null, bpmDataset, orient, false, true, false);
        chart.setBackgroundPaint(IbaColors.BT_GRAY);
        XYPlot xyplot = (XYPlot) chart.getPlot();
        xyplot.setBackgroundPaint(IbaColors.LT_GRAY);
        xyplot.setDataset(1, gaussDataset);
        XYLineAndShapeRenderer xylinerend = new XYLineAndShapeRenderer(true, false);
        xylinerend.setSeriesPaint(0, IbaColors.PURPLE_OVERLAY);
        xylinerend.setSeriesPaint(1, IbaColors.UTORANGE);
        xyplot.setRenderer(1, xylinerend);

//        xyplot.setForegroundAlpha(0.85F);
        XYBarRenderer xybarrend = (XYBarRenderer) xyplot.getRenderer();
        xybarrend.setBarPainter(new StandardXYBarPainter());
        xybarrend.setSeriesPaint(0, IbaColors.BLUE_OVERLAY);
        xybarrend.setSeriesPaint(1, IbaColors.YELLOW);
//        xybarrenderer.setDrawBarOutline(false);
        LegendTitle lt = new LegendTitle(xyplot);
        lt.setItemFont(new Font("Dialog", Font.PLAIN, 9));
        lt.setBackgroundPaint(new Color(217, 222, 225, 100));
        lt.setFrame(new BlockBorder(Color.white));
        lt.setPosition(RectangleEdge.TOP);
        XYTitleAnnotation ta = new XYTitleAnnotation(0.98, 0.98, lt, RectangleAnchor.TOP_RIGHT);

        ta.setMaxWidth(0.48);
        xyplot.addAnnotation(ta);
        return chart;
    }

    private JFreeChart createChart(XYDataset dataset) {
        JFreeChart result = ChartFactory.createTimeSeriesChart(null, "Time", "Value", dataset, true, true, false);
        result.setBackgroundPaint(IbaColors.BT_GRAY);
        XYPlot plot = result.getXYPlot();
        plot.setBackgroundPaint(IbaColors.LT_GRAY);
        ValueAxis axis = plot.getDomainAxis();
        axis.setAutoRange(true);
        //axis.setFixedAutoRange(30000.0D);
        axis = plot.getRangeAxis();
        axis.setRange(0.0D, 500.0D);
        return result;
    }

    /**
     * refreshTargets(): Updates tolerances & target positions.
     */
    public void refreshTargets() {
        double[] targets = controller.getTargets();
        mTolerances = controller.getTolerances();
        mSigmaTargets = controller.getSigmaTargets();
        mSigmaTolerances = controller.getSigmaTolerances();

        for (int i = 0; i < 4; i++) {
            targetText[i].setText(mDecFormat.format(targets[i]));
            toleranceText[i].setText(mDecFormat.format(mTolerances[i]));
        }
    }

    /**
     * refresh(): Updates GUI and computes new currents.
     *
     * @return The computed current values.
     */
    public double[] refresh() {

        isRefreshing = true;
        // Display the refreshed data
        double[] positions = controller.getPositions();
        double[] sigmas = controller.getSigmas();
        double[] diffs = controller.getAdjustedPositions();
        Distribution[] dist = controller.getBpmDistributions();

        for (int i = 0; i < 2; i++) {
            mBpmXdata.updateSeries(i, dist[i * 2].getY());
            mBpmYdata.updateSeries(i, dist[i * 2 + 1].getY());
            mGaussXdata.updateSeries(i, dist[i * 2].getGaussian());
            mGaussYdata.updateSeries(i, dist[i * 2 + 1].getGaussian());
        }

        /* Compute the new currents */
        double[] newCurrents = controller.computeCurrents();
        for (int i = 0; i < 4; i++) {
            //positionText[i].setText(mDecFormat.format(positions[i]));
            newCurrText[i].setText(mDecFormat.format(newCurrents[i]));

            diffText[i].setText(mDecFormat.format(diffs[i]));
            Color color = Status.getColor(Math.abs(diffs[i]) < mTolerances[i]);
            diffText[i].setBackground(color);

            sigmaText[i].setText(mDecFormat.format(sigmas[i]));
            color = Status.getColor(Math.abs(sigmas[i] - mSigmaTargets[i]) < mSigmaTolerances[i]);
            sigmaText[i].setBackground(color);
        }

//            int t = 0;
//            while (t < 1) {
//                if (Math.abs(sigmas[i] - mSigmaTargets[i]) > mSigmaTolerances[i]) {
//                    log.warn("sigma" + i + ": " + sigmas[i] + " - " + mSigmaTargets[i] + " is greater than " + mSigmaTolerances[i]);
//                    log.warn("Refreshing again");
//                    if (controller.refreshAll()) {
//
//                        if (isRefreshing) {
//                            refresh();
//                        }
//
////
////                        log.warn("Refresh complete.");
////                        if (!refreshButton.isEnabled()) {
////                            refreshButton.setEnabled(true);
////                        }
//                    } else {
//                        log.error("Unable to acquire new BPM measurements.");
//                        if (!refreshButton.isEnabled()) {
//                            refreshButton.setEnabled(true);
//                        }
//                    }
//                }
//                t++;
//            }


        if (!keepRefreshing) {
            return newCurrents;
        }

        //AMO - check if data is good
//        if ((dist[0].getMaximumY() < 1) || (dist[1].getMaximumY() < 1) || (dist[2].getMaximumY() < 1) || (dist[3].getMaximumY() < 1)) {
//            log.error("[REFRESH] Need to recapture BPM data");
//            applyButton.setEnabled(false);
//            //keepRefreshing = true;
//
//            if (controller.refreshAll()) {
//                refresh();
//            }
//
//            if (!keepRefreshing) {
//                return newCurrents;
//            }
//        }

        //AMO - check if data is good
        if ((Math.abs(sigmas[0] - mSigmaTargets[0]) > mSigmaTolerances[0]) || (Math.abs(sigmas[1] - mSigmaTargets[1]) > mSigmaTolerances[1]) || (Math.abs(sigmas[2] - mSigmaTargets[2]) > mSigmaTolerances[2]) || (Math.abs(sigmas[3] - mSigmaTargets[3]) > mSigmaTolerances[3])) {
            log.error("[REFRESH] Sigma out of tolerance, refreshing again");
            keepRefreshing = true;

            if (controller.refreshAll()) {
                refresh();
            }
        }

//            else {
//                isRefreshing = false;
//                return newCurrents;
//            }

        isRefreshing = false;
        keepRefreshing = false;

//            if (Math.abs(sigmas[i] - mSigmaTargets[i]) > mSigmaTolerances[i]) {
//                log.warn("sigma" + i + ": " + sigmas[i] + " - " + mSigmaTargets[i] + " is greater than " + mSigmaTolerances[i]);
//                log.warn("Refreshing again");
//
//                if (controller.refreshAll()) {
//                    refresh();
//
//                } else {
//
//                    log.error("Unable to acquire new BPM measurements.");
//                    if (!refreshButton.isEnabled()) {
//                        refreshButton.setEnabled(true);
//                    }
//                }
//            }

        return newCurrents;
    }


    public void actionPerformed(ActionEvent e) {

        controller.beam.llrf.setMaxVoltage(56.00);

        switch (e.getActionCommand()) {
            case "refresh":

                keepRefreshing = true;

                mainPanel.setFocusable(true);
                mainPanel.requestFocusInWindow();
                mainPanel.addKeyListener(new KeyListener() {
                    @Override
                    public void keyPressed(KeyEvent e) {

                    }

                    @Override
                    public void keyTyped(KeyEvent e) {
                        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                    }

                    @Override
                    public void keyReleased(KeyEvent e) {
                        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                            log.error("Escape pressed by user, stopping automatic alignment.");
                            controller.stopAlignment();
                            keepRefreshing = false;
                            setTitle();
                            mainPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
                            mainPanel.setCursor(Cursor.getDefaultCursor());
                            Thread.currentThread().interrupt();
                        }
                        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                    }
                });

                mTabs.setFocusable(true);
                mTabs.requestFocusInWindow();
                mTabs.addKeyListener(new KeyListener() {
                    @Override
                    public void keyPressed(KeyEvent e) {

                    }

                    @Override
                    public void keyTyped(KeyEvent e) {
                        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                    }

                    @Override
                    public void keyReleased(KeyEvent e) {
                        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                            log.error("Escape pressed by user, stopping automatic alignment.");
                            controller.stopAlignment();
                            keepRefreshing = false;
                            setTitle();
                            mainPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
                            mainPanel.setCursor(Cursor.getDefaultCursor());
                            Thread.currentThread().interrupt();
                        }
                        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                    }
                });


                refreshButton.setEnabled(false);
                applyButton.setEnabled(false);
//                if (!controller.isSystemManual()) {
//                    log.error("System is in automatic mode. Please switch to manual mode to align.");
//                    refreshButton.setEnabled(true);
//                    break;
//                }

                log.warn("[REFRESH] Button has been pressed");
                if (!controller.bcreu.isConnected()) {
                    controller.bcreu.connect();
                }
                // If there is no current Refresh process, start one.
                if ((mRefreshWorker == null) || mRefreshWorker.isDone()) {
                    mRefreshWorker = new RefreshWorker();
                    mRefreshWorker.execute();

                    if (mPrepDialog != null) {
                        mPrepDialog.setFocusable(true);
                        mPrepDialog.requestFocusInWindow();
                        mPrepDialog.addKeyListener(new KeyListener() {
                            @Override
                            public void keyPressed(KeyEvent e) {

                            }

                            @Override
                            public void keyTyped(KeyEvent e) {
                                //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                            }

                            @Override
                            public void keyReleased(KeyEvent e) {
                                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                                    log.error("Escape pressed by user, stopping automatic alignment.");
                                    controller.stopAlignment();
                                    keepRefreshing = false;
                                    setTitle();
                                    mainPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
                                    mainPanel.setCursor(Cursor.getDefaultCursor());
                                    Thread.currentThread().interrupt();
                                }

                                //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                            }
                        });
                    }

                } else {
                    log.error("[REFRESH] Process already running.");
                    //log.error("Refresh process test complete.");
                }
                break;
            case "cancel":
                // Cancel and interrupt
                idle = false;
                readCurrent = false;
                isRefreshing = false;
                keepRefreshing = false;
                log.info("[IDLE] Preparing system for treatment and idling.");
                mainCoilButton.setEnabled(false);
                rfLUTButton.setEnabled(false);
                cancelButton2.setEnabled(false);
                controller.stopAlignment();
                this.setTitle("adaPT Assist");
                mainPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
                mainPanel.setCursor(Cursor.getDefaultCursor());

                controller.stopBPMs();

                if (mRefreshWorker != null) {
                    mRefreshWorker.cancel(true);
                }

                mainCoilButton.setEnabled(false);
                rfLUTButton.setEnabled(false);
                cancelButton2.setEnabled(false);

                if (controller.isSystemManual()) {
//                    VDeeSP.setText("41.00");
//                    VDeeSP2.setText("41.00");

                    SwingUtilities.invokeLater(
                            new Runnable(){
                                public void run(){
                                    try {
                                        controller.prepareForTreatment();

                                        try {
                                            Thread.sleep(4500);
                                        } catch (InterruptedException ex) {
                                            log.error(ex);
                                        }

                                        idle = true;
                                        mainCoilButton.setEnabled(true);
                                        rfLUTButton.setEnabled(true);
                                        cancelButton2.setEnabled(true);
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
                if (!refreshButton.isEnabled()) {
                    refreshButton.setEnabled(true);
                }
                if (!applyButton.isEnabled()) {
                    applyButton.setEnabled(true);
                }
                break;
            case "compute":
                log.debug("[COMPUTE] Button has been pressed");
                /* Send to BPM objects the positions in the text fields on the screen */
                double[] centerings = new double[4];
                for (int i = 0; i < 4; i++) {
                    centerings[i] = Double.parseDouble(positionText[i].getText());
                    // if (lastPosition[0] != 0) {
                    //    System.out.println("ABSOLUTE VALUE OF lastPosition - centerings is: " + Math.abs(lastPosition[i] - centerings[i]));
                    //    System.out.println("ABSOLUTE VALUE OF lastPosition - centerings is: " + Math.abs(lastPosition[i] - centerings[i]));
                    //    System.out.println("ABSOLUTE VALUE OF lastPosition - centerings is: " + Math.abs(lastPosition[i] - centerings[i]));
                    //}
                }
                controller.setPositions(centerings);

                refresh();
                break;
            case "apply":

                mainPanel.setFocusable(true);
                mainPanel.requestFocusInWindow();
                mainPanel.addKeyListener(new KeyListener() {
                    @Override
                    public void keyPressed(KeyEvent e) {

                    }

                    @Override
                    public void keyTyped(KeyEvent e) {
                        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                    }

                    @Override
                    public void keyReleased(KeyEvent e) {
                        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                            log.error("Escape pressed by user, stopping automatic alignment.");
                            controller.stopAlignment();
                            keepRefreshing = false;
                            setTitle();
                            mainPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
                            mainPanel.setCursor(Cursor.getDefaultCursor());
                            Thread.currentThread().interrupt();
                        }
                        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                    }
                });

                mTabs.setFocusable(true);
                mTabs.requestFocusInWindow();
                mTabs.addKeyListener(new KeyListener() {
                    @Override
                    public void keyPressed(KeyEvent e) {

                    }

                    @Override
                    public void keyTyped(KeyEvent e) {
                        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                    }

                    @Override
                    public void keyReleased(KeyEvent e) {
                        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                            log.error("Escape pressed by user, stopping automatic alignment.");
                            controller.stopAlignment();
                            keepRefreshing = false;
                            setTitle();
                            mainPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
                            mainPanel.setCursor(Cursor.getDefaultCursor());
                            Thread.currentThread().interrupt();
                        }
                        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                    }
                });

                double maxCurrentChange = Controller.getMaxApply();
                if (controller.isSystemManual()) {
                    String rounded = "";
                    log.debug("[APPLY] Button has been pressed");
                    if (newCurrText[0].getText().equals("")) {
                        log.error("New currents need to be specified before they can be applied.");
                    }
                    if (Math.abs(((Double.valueOf(newCurrText[0].getText())) - (Double.valueOf(oldCurrText[0].getText())))) >= maxCurrentChange) {
                        log.error("New Magnet 1X setpoint was more than " + maxCurrentChange + "A off from previous setpoint, applied a " + maxCurrentChange + "A change.");
                        if (Double.valueOf(newCurrText[0].getText()) >= Double.valueOf(oldCurrText[0].getText())) {
                            rounded = String.format("%.3f", (Double.valueOf(oldCurrText[0].getText())) + (maxCurrentChange - 0.1));
                            newCurrText[0].setText(rounded);
                        } else {
                            rounded = String.format("%.3f", (Double.valueOf(oldCurrText[0].getText())) - (maxCurrentChange - 0.1));
                            newCurrText[0].setText(rounded);
                        }
                        //   break;
                    }
                    if (Math.abs(((Double.valueOf(newCurrText[1].getText())) - (Double.valueOf(oldCurrText[1].getText())))) >= maxCurrentChange) {
                        log.error("New Magnet 1Y setpoint is more than " + maxCurrentChange + "A off from previous setpoint, applied a " + maxCurrentChange + "A change.");
                        if (Double.valueOf(newCurrText[1].getText()) >= Double.valueOf(oldCurrText[1].getText())) {
                            rounded = String.format("%.3f", (Double.valueOf(oldCurrText[1].getText())) + (maxCurrentChange - 0.1));
                            newCurrText[1].setText(rounded);
                        } else {
                            rounded = String.format("%.3f", (Double.valueOf(oldCurrText[1].getText())) - (maxCurrentChange - 0.1));
                            newCurrText[1].setText(rounded);
                        }


                        //    break;
                    }
                    if (Math.abs(((Double.valueOf(newCurrText[2].getText())) - (Double.valueOf(oldCurrText[2].getText())))) >= maxCurrentChange) {
                        log.error("New Magnet 2X setpoint is more than " + maxCurrentChange + "A off from previous setpoint, applied a " + maxCurrentChange + "A change.");
                        if (Double.valueOf(newCurrText[2].getText()) >= Double.valueOf(oldCurrText[2].getText())) {
                            rounded = String.format("%.3f", (Double.valueOf(oldCurrText[2].getText())) + (maxCurrentChange - 0.1));
                            newCurrText[2].setText(rounded);
                        } else {
                            rounded = String.format("%.3f", (Double.valueOf(oldCurrText[2].getText())) - (maxCurrentChange - 0.1));
                            newCurrText[2].setText(rounded);
                        }
                        //  break;
                    }
                    if (Math.abs(((Double.valueOf(newCurrText[3].getText())) - (Double.valueOf(oldCurrText[3].getText())))) >= maxCurrentChange) {
                        log.error("New Magnet 2Y setpoint is more than " + maxCurrentChange + "A off from previous setpoint, applied a " + maxCurrentChange + "A change.");
                        if (Double.valueOf(newCurrText[3].getText()) >= Double.valueOf(oldCurrText[3].getText())) {
                            rounded = String.format("%.3f", (Double.valueOf(oldCurrText[3].getText())) + (maxCurrentChange - 0.1));
                            newCurrText[3].setText(rounded);
                        } else {
                            rounded = String.format("%.3f", (Double.valueOf(oldCurrText[3].getText())) - (maxCurrentChange - 0.1));
                            newCurrText[3].setText(rounded);
                        }
                        // break;
                    }

                    double[] newCurrents = new double[4];
                    for (int i = 0; i < 4; i++) {
                        //  lastPosition[i] = Double.parseDouble(positionText[i].getText()); // added by AMO to check for large divergence from last position
                        newCurrents[i] = Double.parseDouble(newCurrText[i].getText());
                    }

                    controller.setCurrents(newCurrents);
                    log.debug("New currents sent to controller");
                    for (int i = 0; i < 4; i++) {
//                        positionText[i].setText("");
//                    oldCurrText[i].setText("");
                    }

                    File resultsFile = new File("./result.csv");

                    if (resultsFile.isFile()) {
                        csvWrite(resultsFile);

                    } else {
                        csvCreate(resultsFile);
                    }
                } else {
                    log.error("System is in automatic mode. Please switch to manual mode.");
                }
                break;
            case "output":
                readCurrent = true;
                idle = false;

                log.info("[TUNE] Set RF has been pressed");

                mainCoilButton.setEnabled(false);
                rfLUTButton.setEnabled(false);

                if (!controller.isSystemManual()) {
                    log.error("[TUNE] System is in automatic mode. Please switch to manual mode.");
                    mainCoilButton.setEnabled(true);
                    rfLUTButton.setEnabled(true);
                    break;
                }


                SwingUtilities.invokeLater(
                        new Runnable(){
                            public void run(){
                                try {
                                    controller.prepareForTune();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });

                //controller.prepareForTune();

//                VDeeSP.setText("41.00");
//                VDeeSP2.setText("56.00");

                //mainCoilButton.setEnabled(true);

                Thread read = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        beamCurrent = new LinkedList<Float>();
                        while (readCurrent) {
                            try {
                                curr = getDegraderCurrent();

                                outputLabel.setText("S1E: " + String.format("%.2f", curr.getAsFloat()));


                            } catch (Exception f) {
                                log.error("[SET RF] Could not get degrader current from OPC server");
                                f.printStackTrace();
//                                VDeeSP.setText("41.00");
//                                VDeeSP2.setText("41.00");
                                log.info("[IDLE] Preparing system for treatment and idling.");
                                controller.prepareForTreatment();
                                idle = true;
                                mainCoilButton.setEnabled(true);
                                rfLUTButton.setEnabled(true);
                            }

                            if (curr.getAsDouble() > 250.0) {

                                if (!beamCurrent.contains(curr.getAsFloat())) {

                                    beamCurrent.addFirst(curr.getAsFloat());


                                    if (beamCurrent.size() >= 30) {

                                        if (isStable(beamCurrent)) {
                                            outputLabel.setForeground(manual);
                                        } else {
                                            outputLabel.setForeground(automatic);
                                        }

                                        beamCurrent.removeLast();
                                    }
                                }
                            }
                        }
                    }
                });
                read.start();

                break;
            case "cancelTune":
                // Cancel and interrupt

                controller.stopAlignment();
                this.setTitle("adaPT Assist");
                mainPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
                mainPanel.setCursor(Cursor.getDefaultCursor());

                if (mRefreshWorker != null) {
                    mRefreshWorker.cancel(true);
                }

                if (controller.isSystemManual()) {
                    log.info("[IDLE] Preparing system for treatment and idling.");
                    controller.prepareForTreatment();
                    idle = true;
                } else {
                    log.error("System is in automatic mode, cannot prepare for treatment.");
                }

                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ex) {
                    log.error(ex);
                }
                break;
            case "saveMe":
                // sets magnets back to known good/close values
//                double[] safeCurrents = new double[4];
                double[] dbl = controller.getSafeCurrents();
//
//                safeCurrents[0] = -42.0;
//                safeCurrents[1] = -50.0;
//                safeCurrents[2] = -13.0;
//                safeCurrents[3] = 60.0;

                if (controller.isSystemManual()) {
                    log.warn("Setting new currents to known good values.");
                    //controller.setSafeCurrents();

                    log.debug("Is ACU connected ?");
                    if (!controller.acu.isConnected()) {
                        log.debug("Will try to connect to ACU");
                        controller.acu.connect();
                        log.debug("Is connected");
                    }

//                    for (int i = 0; i < 4; i++) {
//                        controller.acu.setTagValue(Status.Magnet_write[i], Double.parseDouble(newCurrText2[i].getText()));
//                    }

                    for (int i = 0; i < 4; i++) {
                        newCurrText[i].setText(String.valueOf(dbl[i]));
                    }

                    //controller.setESSCurrents();

                    //log.warn("Safe currents restored to 4 magnets");
                } else {
                    log.error("System is in automatic mode, please switch to manual.");
                }
                break;
            case "alignMe":
                if (controller.isSystemManual()) {

//                    if (!SwingUtilities.isEventDispatchThread()) {
//                        SwingUtilities.invokeLater(new Runnable() {
//                            @Override
//                            public void run() {
//                                controller.align();
//                            }
//                        });
//                        return;
//                    }
                    Thread safe = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            controller.align();
                        }
                    });
                    safe.start();

                    this.setTitle("adaPT Assist - Automatic alignment in progress - press Esc to cancel");

                    Cursor cursor = new Cursor(Cursor.WAIT_CURSOR);
                    mainPanel.setCursor(cursor);

                    mainPanel.setBorder(BorderFactory.createMatteBorder(
                            5, 5, 5, 5, Color.red));

                    mainPanel.setFocusable(true);
                    mainPanel.requestFocusInWindow();
                    mainPanel.addKeyListener(new KeyListener() {
                        @Override
                        public void keyPressed(KeyEvent e) {
                        }

                        @Override
                        public void keyTyped(KeyEvent e) {
                        }

                        @Override
                        public void keyReleased(KeyEvent e) {
                            if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                                log.error("Escape pressed by user, stopping automatic alignment.");
                                controller.stopAlignment();
                                keepRefreshing = false;
                                setTitle();
                                mainPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
                                mainPanel.setCursor(Cursor.getDefaultCursor());

                                Thread.currentThread().interrupt();
                            }
                        }
                    });

                    mTabs.setFocusable(true);
                    mTabs.requestFocusInWindow();
                    mTabs.addKeyListener(new KeyListener() {
                        @Override
                        public void keyPressed(KeyEvent e) {

                        }

                        @Override
                        public void keyTyped(KeyEvent e) {
                            //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                        }

                        @Override
                        public void keyReleased(KeyEvent e) {
                            if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                                log.error("Escape pressed by user, stopping automatic alignment.");
                                controller.stopAlignment();
                                keepRefreshing = false;
                                setTitle();
                                mainPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
                                mainPanel.setCursor(Cursor.getDefaultCursor());
                                Thread.currentThread().interrupt();
                            }
                            //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                        }
                    });

                } else {
                    log.error("System is in automatic mode, please switch to manual.");
                }
                break;

            case "VDee1plus":
                if ((Double.parseDouble(VDeeSP.getText()) + 1.00) <= 56.00) {
                    VDeeSP.setText(String.format("%.2f", Double.parseDouble(VDeeSP.getText()) + 1.00));
                    controller.beam.llrf.setDeeVoltage1(Double.parseDouble(VDeeSP.getText()));
                    log.warn("VDee1 set to " + String.format("%.2f", Double.parseDouble(VDeeSP.getText())));
                    if (beamCurrent != null) {
                        beamCurrent.clear();
                    }
                    outputVariance.setText("Variance: ");
                } else {
                    VDeeSP.setText("56.00");
                    controller.beam.llrf.setDeeVoltage1(56.00);
                    log.warn("VDee1 set to max of 56.00kV");
                }
                break;
            case "VDee1minus":
                if ((Double.parseDouble(VDeeSP.getText()) - 1.00) >= 41.00) {
                    VDeeSP.setText(String.format("%.2f", Double.parseDouble(VDeeSP.getText()) - 1.00));
                    controller.beam.llrf.setDeeVoltage1(Double.parseDouble(VDeeSP.getText()));
                    log.warn("VDee1 set to " + String.format("%.2f", Double.parseDouble(VDeeSP.getText())));
                    if (beamCurrent != null) {
                        beamCurrent.clear();
                    }
                    outputVariance.setText("Variance: ");
                } else {
                    VDeeSP.setText("41.00");
                    controller.beam.llrf.setDeeVoltage1(41.00);
                    log.warn("VDee1 set to minimum of 41.00kV");
                }
                break;
            case "VDee2plus":
                if ((Double.parseDouble(VDeeSP2.getText()) + 1.00) <= 56.00) {
                    VDeeSP2.setText(String.format("%.2f", Double.parseDouble(VDeeSP2.getText()) + 1.00));
                    controller.beam.llrf.setDeeVoltage2(Double.parseDouble(VDeeSP2.getText()));
                    log.warn("VDee2 set to " + String.format("%.2f", Double.parseDouble(VDeeSP2.getText())));
                    if (beamCurrent != null) {
                        beamCurrent.clear();
                    }
                    outputVariance.setText("Variance: ");
                } else {
                    VDeeSP2.setText("56.00");
                    controller.beam.llrf.setDeeVoltage2(56.00);
                    log.warn("VDee2 set to max of 56.00kV");
                }
                break;
            case "VDee2minus":
                if ((Double.parseDouble(VDeeSP2.getText()) - 1.00) >= 41.00) {
                    VDeeSP2.setText(String.format("%.2f", Double.parseDouble(VDeeSP2.getText()) - 1.00));
                    controller.beam.llrf.setDeeVoltage2(Double.parseDouble(VDeeSP2.getText()));
                    log.warn("VDee2 set to " + String.format("%.2f", Double.parseDouble(VDeeSP2.getText())));
                    if (beamCurrent != null) {
                        beamCurrent.clear();
                    }
                    outputVariance.setText("Variance: ");
                } else {
                    VDeeSP2.setText("41.00");
                    controller.beam.llrf.setDeeVoltage2(41.00);
                    log.warn("VDee2 set to minimum of 41.00kV");
                }
                break;
            case "Filminus":
                if ((Double.parseDouble(FilSP.getText()) - 1.00) >= 125.00) {
                    FilSP.setText(String.format("%.2f", Double.parseDouble(FilSP.getText()) - 1.00));
                    controller.acu.setTagValue(Status.Fil_write, Double.parseDouble(FilSP.getText()));
                    log.warn("[TUNE] Filament set to " + String.format("%.2f", Double.parseDouble(FilSP.getText())));
                    if (beamCurrent != null) {
                        beamCurrent.clear();
                    }
                    if (list != null){
                        list.clear();
                    }
                    outputVariance.setText("Variance: ");
                } else {
                    FilSP.setText("125.00");
                    controller.acu.setTagValue(Status.Fil_write, 125.00);
                    log.warn("[TUNE] Filament set to minimum of 125.00A");
                }
                break;
            case "Filplus":
                if ((Double.parseDouble(FilSP.getText()) + 1.00) <= 220.00) {
                    FilSP.setText(String.format("%.2f", Double.parseDouble(FilSP.getText()) + 1.00));
                    controller.acu.setTagValue(Status.Fil_write, Double.parseDouble(FilSP.getText()));
                    log.warn("[TUNE] Filament set to " + String.format("%.2f", Double.parseDouble(FilSP.getText())));
                    if (beamCurrent != null) {
                        beamCurrent.clear();
                    }
                    if (list != null){
                        list.clear();
                    }
                    outputVariance.setText("Variance: ");
                } else {
                    FilSP.setText("220.00");
                    controller.acu.setTagValue(Status.Fil_write, 220.00);
                    log.warn("[TUNE] Filament set to maximum of 220.00A");
                }
                break;
            case "Arcminus":
                if ((Double.parseDouble(ArcSP.getText()) - 1.00) >= 1.00) {
                    try {
                        setpointraw = getRestVariable(Status.Arc_setpoint);
                        setpoint = setpointraw.getAsDouble();

                        controller.acu.setTagValue(Status.Arc_write, (setpoint - 1.00D));

                        Thread.sleep(500);

                        setpointFeedbackraw = getRestVariable(Status.Arc_setpoint);
                        setpointFeedback = setpointFeedbackraw.getAsDouble();

                        ArcSP.setText(String.valueOf(Double.parseDouble(String.format("%.2f", setpointFeedback))));
                    } catch (Exception f) {
                        f.printStackTrace();
                    }

                    log.warn("[TUNE] Arc set to " + String.format("%.2f", Double.parseDouble(ArcSP.getText())));
                    if (beamCurrent != null) {
                        beamCurrent.clear();
                    }
                    if (list != null){
                        list.clear();
                    }
                    outputVariance.setText("Variance: ");
//                    ArcSP.setText(String.format("%.2f", Double.parseDouble(ArcSP.getText()) - 1.00));
//                    controller.acu.setTagValue(Status.Arc_write, Double.parseDouble(ArcSP.getText()));
//                    log.warn("Arc set to " + String.format("%.2f", Double.parseDouble(ArcSP.getText())));
//                    if (beamCurrent != null) {
//                        beamCurrent.clear();
//                    }
//                    outputVariance.setText("Variance: ");
                } else {
                    ArcSP.setText("1.00");
                    controller.acu.setTagValue(Status.Arc_write, 1.00D);
                    log.warn("[TUNE] Arc set to minimum of 1.00mA");
                }
                break;
            case "Arcplus":
                if ((Double.parseDouble(ArcSP.getText()) + 1.00) <= 250.00) {
                    try {
                        setpointraw = getRestVariable(Status.Arc_setpoint);
                        setpoint = setpointraw.getAsDouble();

                        controller.acu.setTagValue(Status.Arc_write, (setpoint + 1.00D));

                        Thread.sleep(500);

                        setpointFeedbackraw = getRestVariable(Status.Arc_setpoint);
                        setpointFeedback = setpointFeedbackraw.getAsDouble();

                        ArcSP.setText(String.valueOf(Double.parseDouble(String.format("%.2f", setpointFeedback))));
                    } catch (Exception f) {
                        f.printStackTrace();
                    }

                    log.warn("[TUNE] Arc set to " + String.format("%.2f", Double.parseDouble(ArcSP.getText())));
                    if (beamCurrent != null) {
                        beamCurrent.clear();
                    }
                    if (list != null){
                        list.clear();
                    }
                    outputVariance.setText("Variance: ");
//                    ArcSP.setText(String.format("%.2f", Double.parseDouble(ArcSP.getText()) + 1.00));
//                    controller.acu.setTagValue(Status.Arc_write, Double.parseDouble(ArcSP.getText()));
//                    log.warn("Arc set to " + String.format("%.2f", Double.parseDouble(ArcSP.getText())));
//                    if (beamCurrent != null) {
//                        beamCurrent.clear();
//                    }
//                    outputVariance.setText("Variance: ");
                } else {
                    ArcSP.setText("250.00");
                    controller.acu.setTagValue(Status.Arc_write, 250.00);
                    log.warn("[TUNE] Arc set to maximum of 250.00mA");
                }
                break;
            case "CCoilminus":
                if ((Double.parseDouble(CCoilSP.getText()) - 0.20) >= 1.00) {
                    CCoilSP.setText(String.format("%.2f", Double.parseDouble(CCoilSP.getText()) - 0.20));
                    controller.acu.setTagValue(Status.CC_write, Double.parseDouble(CCoilSP.getText()));
                    log.warn("Compensation coil set to " + String.format("%.2f", Double.parseDouble(CCoilSP.getText())));
                    if (beamCurrent != null) {
                        beamCurrent.clear();
                    }
                    outputVariance.setText("Variance: ");
                } else {
                    CCoilSP.setText("1.00");
                    controller.acu.setTagValue(Status.CC_write, 1.00);
                    log.warn("Compensation coil set to minimum of 1.00A");
                }
                break;
            case "CCoilplus":
                if ((Double.parseDouble(CCoilSP.getText()) + 0.20) <= 15.00) {
                    CCoilSP.setText(String.format("%.2f", Double.parseDouble(CCoilSP.getText()) + 0.20));
                    controller.acu.setTagValue(Status.CC_write, Double.parseDouble(CCoilSP.getText()));
                    log.warn("Compensation coil set to " + String.format("%.2f", Double.parseDouble(CCoilSP.getText())));
                    if (beamCurrent != null) {
                        beamCurrent.clear();
                    }
                    outputVariance.setText("Variance: ");
                } else {
                    CCoilSP.setText("15.00");
                    controller.acu.setTagValue(Status.CC_write, 15.00);
                    log.warn("Compensation coil set to maximum of 15.00A");
                }
                break;
            case "MCoilminus":
                if ((Double.parseDouble(MCoilSP.getText()) - changePerClick) >= 735.00) {
                    try {
                        setpointraw = getRestVariable(Status.MC_setpoint);
                        setpoint = setpointraw.getAsDouble();

                        controller.acu.setTagValue(Status.MC_write, (setpoint - changePerClick));

                        Thread.sleep(500);

                        setpointFeedbackraw = getRestVariable(Status.MC_setpoint);
                        setpointFeedback = setpointFeedbackraw.getAsDouble();

                        MCoilSP.setText(String.valueOf(Double.parseDouble(String.format("%.3f", setpointFeedback))));
                    } catch (Exception g) {
                        g.printStackTrace();
                    }

                    log.warn("[TUNE] Main coil set to " + String.format("%.3f", Double.parseDouble(MCoilSP.getText())));
                    if (beamCurrent != null) {
                        beamCurrent.clear();
                    }
                    if (list != null){
                        list.clear();
                    }
                    outputVariance.setText("Variance: ");
                } else {
                    MCoilSP.setText("735.00");
                    controller.acu.setTagValue(Status.MC_write, 735.00);
                    log.warn("[TUNE] Main coil set to minimum of 735.00A");
                }
                break;
            case "MCoilplus":
                if ((Double.parseDouble(MCoilSP.getText()) + changePerClick) <= 755.00) {
                    try {
                        setpointraw = getRestVariable(Status.MC_setpoint);
                        setpoint = setpointraw.getAsDouble();

                        controller.acu.setTagValue(Status.MC_write, (setpoint + changePerClick));

                        Thread.sleep(500);

                        setpointFeedbackraw = getRestVariable(Status.MC_setpoint);
                        setpointFeedback = setpointFeedbackraw.getAsDouble();

                        MCoilSP.setText(String.valueOf(Double.parseDouble(String.format("%.3f", setpointFeedback))));
                    } catch (Exception g) {
                        g.printStackTrace();
                    }

                    log.warn("[TUNE] Main coil set to " + String.format("%.3f", Double.parseDouble(MCoilSP.getText())));
                    if (beamCurrent != null) {
                        beamCurrent.clear();
                    }
                    if (list != null){
                        list.clear();
                    }
                    outputVariance.setText("Variance: ");
                } else {
                    MCoilSP.setText("755.00");
                    controller.acu.setTagValue(Status.MC_write, 755.00);
                    log.warn("[TUNE] Main coil set to maximum of 755.00A");
                }
                break;
            case "HCoil1minus":
                if ((Double.parseDouble(HCoil1SP.getText()) - 0.05) >= -1.50) {
                    HCoil1SP.setText(String.format("%.2f", Double.parseDouble(HCoil1SP.getText()) - 0.05));
                    controller.acu.setTagValue(Status.HC1_write, Double.parseDouble(HCoil1SP.getText()));
                    log.warn("Harmonic coil set to " + String.format("%.2f", Double.parseDouble(HCoil1SP.getText())));
                    if (beamCurrent != null) {
                        beamCurrent.clear();
                    }
                    outputVariance.setText("Variance: ");
                } else {
                    HCoil1SP.setText("-1.50");
                    controller.acu.setTagValue(Status.HC1_write, -1.50);
                    log.warn("Harmonic coil set to minimum of -1.50A");
                }
                break;
            case "HCoil1plus":
                if ((Double.parseDouble(HCoil1SP.getText()) + 0.05) <= 1.50) {
                    HCoil1SP.setText(String.format("%.2f", Double.parseDouble(HCoil1SP.getText()) + 0.05));
                    controller.acu.setTagValue(Status.HC1_write, Double.parseDouble(HCoil1SP.getText()));
                    log.warn("Harmonic coil set to " + String.format("%.2f", Double.parseDouble(HCoil1SP.getText())));
                    if (beamCurrent != null) {
                        beamCurrent.clear();
                    }
                    outputVariance.setText("Variance: ");
                } else {
                    HCoil1SP.setText("1.50");
                    controller.acu.setTagValue(Status.HC1_write, 1.50);
                    log.warn("Harmonic coil set to maximum of 1.50A");
                }
                break;
            case "HCoil2minus":
                if ((Double.parseDouble(HCoil2SP.getText()) - 0.05) >= -1.50) {
                    HCoil2SP.setText(String.format("%.2f", Double.parseDouble(HCoil2SP.getText()) - 0.05));
                    controller.acu.setTagValue(Status.HC2_write, Double.parseDouble(HCoil2SP.getText()));
                    log.warn("Harmonic coil set to " + String.format("%.2f", Double.parseDouble(HCoil2SP.getText())));
                    if (beamCurrent != null) {
                        beamCurrent.clear();
                    }
                    outputVariance.setText("Variance: ");
                } else {
                    HCoil2SP.setText("-1.50");
                    controller.acu.setTagValue(Status.HC2_write, -1.50);
                    log.warn("Harmonic coil set to minimum of -1.50A");
                }
                break;
            case "HCoil2plus":
                if ((Double.parseDouble(HCoil2SP.getText()) + 0.05) <= 1.50) {
                    HCoil2SP.setText(String.format("%.2f", Double.parseDouble(HCoil2SP.getText()) + 0.05));
                    controller.acu.setTagValue(Status.HC2_write, Double.parseDouble(HCoil2SP.getText()));
                    log.warn("Harmonic coil set to " + String.format("%.2f", Double.parseDouble(HCoil2SP.getText())));
                    if (beamCurrent != null) {
                        beamCurrent.clear();
                    }
                    outputVariance.setText("Variance: ");
                } else {
                    HCoil2SP.setText("1.50");
                    controller.acu.setTagValue(Status.HC2_write, 1.50);
                    log.warn("Harmonic coil set to maximum of 1.50A");
                }
                break;
            case "RFLUT":
                if (controller.isSystemManual()) {

                    idle = false;
                    rfLUTButton.setEnabled(false);
                    cancelButton2.setEnabled(false);
                    mainCoilButton.setEnabled(false);
                    log.warn("[RF LUT] Generating RF look up table, please wait.");

                    SwingUtilities.invokeLater(
                            new Runnable(){
                                public void run(){
                                    try {
                                        controller.beam.llrf.setMaxVoltage(56.00);
                                        controller.beam.bpsController.startGenerateRfLookupActivity(47.00, 56.00, 9);

                                        Thread.sleep(8000);

                                        while(Double.parseDouble(controller.acu.getTagValue(Status.VD1_equip).toString()) > 42.00 && !controller.sparkDetected()) {
                                            Thread.sleep(1000);
                                        }
                                        if (controller.sparkDetected()){
                                            log.error("[RF LUT] Spark detected, LUT has failed. Please try again.");
                                        }else {
                                            log.warn("[RF LUT] Table generation has completed.");
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    } finally {
                                        log.info("[IDLE] Preparing system for treatment and idling.");
                                        SwingUtilities.invokeLater(
                                                new Runnable(){
                                                    public void run(){
                                                        try {
                                                            controller.prepareForTreatment();

                                                            Thread.sleep(4500);

                                                            idle = true;
                                                            mainCoilButton.setEnabled(true);
                                                            rfLUTButton.setEnabled(true);
                                                            cancelButton2.setEnabled(true);
                                                        } catch (Exception e) {
                                                            e.printStackTrace();
                                                        }
                                                    }
                                                });
                                    }
                                }
                            });


//                    log.info("[IDLE] Preparing system for treatment and idling.");
//                    SwingUtilities.invokeLater(
//                            new Runnable(){
//                                public void run(){
//                                    try {
//                                        controller.prepareForTreatment();
//
//                                        Thread.sleep(4500);
//
//                                        idle = true;
//                                        mainCoilButton.setEnabled(true);
//                                        rfLUTButton.setEnabled(true);
//                                        cancelButton2.setEnabled(true);
//                                    } catch (Exception e) {
//                                        e.printStackTrace();
//                                    }
//                                }
//                            });
                }
                break;
            case "update":
                updated = true;

                //controller.updateButtonTest();

                while (FilSP.getText().equalsIgnoreCase("-")) {
                    try {
                        var = getRestVariable(Status.Fil_setpoint);
                        FilSP.setText(String.format("%.2f", var.getAsDouble()));
                    } catch (Exception f) {
                        f.printStackTrace();
                    }
                }

                try {
                    if (getRestVariable(Status.Arc_setpoint) == null) {
                        var = getRestVariable(Status.Arc_setpoint);
                        ArcSP.setText(String.format("%.2f", var.getAsDouble()));
                    } else {
                        var = getRestVariable(Status.Arc_setpoint);
                        ArcSP.setText(String.format("%.2f", var.getAsDouble()));
                    }
                } catch (Exception f) {
                    f.printStackTrace();
                    //log.warn("Trying to register tags with OPC open interface, make sure it is running!");
                }

                try {
                    if (getRestVariable(Status.MC_setpoint) == null) {
                        var = getRestVariable(Status.MC_setpoint);
                        MCoilSP.setText(String.format("%.3f", var.getAsDouble()));
                    } else {
                        var = getRestVariable(Status.MC_setpoint);
                        MCoilSP.setText(String.format("%.3f", var.getAsDouble()));
                    }
                } catch (Exception f) {
                    f.printStackTrace();
                    //log.warn("Trying to register tags with OPC open interface, make sure it is running!");
                }

//                int i = 0;
//                while (i < 100){
//
//                    beamChart.update(400.00);
//                    //beamChart.chart.getXYPlot().
//                    //beamChart.dataset.
//
//                    i++;
//                }


                SourTuning.setSelected(controller.sourceTuning());

                MCTuning.setSelected(controller.mainCoilTuning());
                break;
            case "MCoilStep1":
                changePerClick = 0.010;
                MCoilStep1.setBorder(BorderFactory.createMatteBorder(2, 2, 2, 2, manual));
                MCoilStep2.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.lightGray));
                MCoilStep3.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.lightGray));
                log.warn("[TUNE] Main Coil step changed to 0.010");
                break;
            case "MCoilStep2":
                changePerClick = 0.005;
                MCoilStep1.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.lightGray));
                MCoilStep2.setBorder(BorderFactory.createMatteBorder(2, 2, 2, 2, manual));
                MCoilStep3.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.lightGray));
                log.warn("[TUNE] Main Coil step changed to 0.005");
                break;
            case "MCoilStep3":
                changePerClick = 0.001;
                MCoilStep1.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.lightGray));
                MCoilStep2.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.lightGray));
                MCoilStep3.setBorder(BorderFactory.createMatteBorder(2, 2, 2, 2, manual));
                log.warn("[TUNE] Main Coil step changed to 0.001");
                break;
            default:
                // Do nothing
        }
    }

    public void itemStateChanged(ItemEvent e) {

        Object source = e.getItemSelectable();

        if (controller.isSystemManual()) {

            if (e.getStateChange() == ItemEvent.SELECTED) {
                if (source == SourTuning) {
                    //turn on source tuning
                    if (controller.sourceTuning() && !SourTuning.isSelected()) {
                        controller.useKeyCommand(73);
                        log.warn("Source tuning has been disabled.");
                    } else if (!controller.sourceTuning() && SourTuning.isSelected()) {
                        controller.useKeyCommand(73);
                        log.warn("Source tuning has been enabled.");
                    }
                    //log.warn("Source tuning has been enabled.");
                } else if (source == MCTuning) {
                    //turn on MC tuning
                    if (controller.mainCoilTuning() && !MCTuning.isSelected()) {
                        controller.useKeyCommand(44);
                        log.warn("Main coil tuning has been disabled.");
                    } else if (!controller.mainCoilTuning() && MCTuning.isSelected()) {
                        controller.useKeyCommand(44);
                        MCoilplus.setEnabled(false);
                        MCoilminus.setEnabled(false);
                        log.warn("Main coil tuning has been enabled.");
                    }
                    //log.warn("Main coil tuning has been enabled.");
                }
            }

            if (e.getStateChange() == ItemEvent.DESELECTED) {
                if (source == SourTuning) {
                    //turn off source tuning
                    if (controller.sourceTuning() && !SourTuning.isSelected()) {
                        controller.useKeyCommand(73);
                        log.warn("Source tuning has been disabled.");
                    } else if (!controller.sourceTuning() && SourTuning.isSelected()) {
                        controller.useKeyCommand(73);
                        log.warn("Source tuning has been enabled.");
                    }
                    //controller.useKeyCommand(73);
                    //log.warn("Source tuning has been disabled.");
                } else if (source == MCTuning) {
                    //turn off MC tuning
                    if (controller.mainCoilTuning() && !MCTuning.isSelected()) {
                        controller.useKeyCommand(44);
                        log.warn("Main coil tuning has been disabled.");
                    } else if (!controller.mainCoilTuning() && MCTuning.isSelected()) {
                        controller.useKeyCommand(44);
                        MCoilplus.setEnabled(false);
                        MCoilminus.setEnabled(false);
                        log.warn("Main coil tuning has been enabled.");
                    }
                    //controller.useKeyCommand(44);
                    //log.warn("Main coil tuning has been disabled.");

                }
            }
        }
    }

    public JsonElement getDegraderCurrent() throws Exception {
        String degraderCurr = "E0.E1.BSB01.cfeedback";

        JsonElement current = restManager.getVariable(degraderCurr).get("value");

        if (current.isJsonNull()) {
            current = restManager.getVariable(degraderCurr).get("value");
        }

        return current;
    }

    public static JsonElement getRestVariable(String str) throws Exception {

        JsonElement var = restManager.getVariable(str).get("value");

        try {

            if (var == null) {
                var = restManager.getVariable(str).get("value");
            }
        } catch (ConnectException e) {
            log.error("EXCEPTION CAUGHT getRestVariable");
        }

        return var;
    }

    public boolean isStable(LinkedList<Float> list) {
        Double firstSum = 0.0;
        Double secondSum = 0.0;
        Double totalSum;
        Double variance;
        Double average;

        for (int i = 0; i < 15; i++) {
            firstSum += list.get(i);
        }
        for (int i = 15; i < 30; i++) {
            secondSum += list.get(i);
        }

        totalSum = firstSum + secondSum;

        average = totalSum / 30.0;

        variance = (Math.abs(((firstSum / 15.0) - (secondSum / 15.0)) / (average)) * 100.0);

        outputVariance.setText("Variance: " + String.format("%.1f", variance) + "%");

        return (variance < 1.0);
    }

    public boolean checkTolerances() {

        if (mTolerances[0] >= Double.parseDouble(toleranceText[0].toString()) && mTolerances[1] >= Double.parseDouble(toleranceText[1].toString()) && mTolerances[2] >= Double.parseDouble(toleranceText[2].toString()) && mTolerances[3] >= Double.parseDouble(toleranceText[3].toString())) {

            return true;
        }

        return false;
    }

    public static void setServiceBeamMode(){
        readyForServiceBeam = true;
    }

    public static boolean getServiceBeamMode(){
        return readyForServiceBeam;
    }

    private void getStability() throws Exception {
        String degraderCurr = "E0.E1.BSB01.cfeedback";

        outputLabel.setText("S1E: " + restManager.getVariable(degraderCurr).get("value").getAsString());

//        ArrayDeque beamCurrent = new ArrayDeque();
//
//        beamCurrent.addLast(restManager.getVariable(degraderCurr).get("value").getAsDouble());
//
//        restManager.getVariable(degraderCurr).get("value").getAsDouble();
    }

    private void setTitle() {
        this.setTitle("Service Beam GUI - PTS-8.7.1");
    }

    private void csvCreate(File newFile) {
        try (BufferedWriter out = new BufferedWriter(new FileWriter(newFile, true))) {
            log.info("Creating new CSV file");
            out.write("Time,");
            for (int i = 0; i < Status.Magnet_names.length; i++) {
                out.write(Status.Magnet_names[i] + ",");
            }
            for (int i = 0; i < Status.Cyclo_names.length; i++) {
                out.write(Status.Cyclo_names[i]);
                if (i < (Status.Cyclo_names.length - 1)) {
                    out.write(",");
                } else {
                    out.newLine();
                }
            }
        } catch (IOException e) {
            log.error(e);
        }

        csvWrite(newFile);
    }

    private void csvWrite(File resultsFile) {
        log.info("Adding line to CSV file");
        try (BufferedWriter out = new BufferedWriter(new FileWriter(resultsFile, true))) {
            out.write(new Date().toString() + ",");
            for (int i = 0; i < newCurrText.length; i++) {
                out.write(newCurrText[i].getText() + ",");
            }
            for (int i = 0; i < cycloText.length; i++) {
                out.write(cycloText[i].getText());
                if (i < (cycloText.length - 1)) {
                    out.write(",");
                } else {
                    out.newLine();
                }
            }
        } catch (IOException e) {
            log.error(e);
        }
    }

    private static void csvCreateBeamResults(File newFile) {
        try (BufferedWriter out = new BufferedWriter(new FileWriter(newFile, true))) {
            log.info("[Beam Record] Creating new beam results CSV file");
            out.write("Time,");
            out.write("Beam,");
            out.write("RF(kV),");
            out.write("BCREU(nA),");
            out.write("Control,");
            out.write("Duration,");
            out.newLine();
        } catch (IOException e) {
            log.error(e);
        }

        csvWriteBeamOn(newFile);
    }

    private static void csvWriteBeamOn(File resultsFile) {
        log.info("[Beam Record] Adding line to beam result CSV file");
        try (BufferedWriter out = new BufferedWriter(new FileWriter(resultsFile, true))) {
            out.write(new GregorianCalendar().getTime() + ",");
            out.write("ON,");
            out.write(mEsbtsPanel.mActionsPanel.getDeeVoltage() + ",");
            out.write(String.valueOf(Controller.bcreu.getMaxBeam()));
            out.write(",");
            out.write(",");
            out.newLine();
        } catch (IOException e) {
            log.error(e);
        }
    }

    private static void csvWriteBeamOff(File resultsFile, int control, long duration) {
        log.info("[Beam Record] Adding line to beam result CSV file");
        try (BufferedWriter out = new BufferedWriter(new FileWriter(resultsFile, true))) {
            out.write(new GregorianCalendar().getTime() + ",");
            out.write("OFF,");
            out.write(",");
            out.write(",");
            if(control==1){
                out.write("MANUAL,");
            } else if (control == 2) {
                out.write("TIMER,");
            } else if (control==3) {
                out.write("SPARK,");
            } else{
                out.write("UNKNOWN,");
            }
            out.write(DurationFormatUtils.formatDuration(duration, "mm:ss:SSS") + ",");
            out.newLine();
        } catch (IOException e) {
            log.error(e);
        }
    }

    // Define workers to improve the responsiveness of the GUI
    class ApplyWorker extends SwingWorker<Void, Void> {
        @Override
        protected Void doInBackground() throws Exception {
            //background task


            if (newCurrText[0].getText().equals("")) {
                log.error("New currents need to be specified before they can be applied.");
            } else {
                double[] newCurrents = new double[4];
                for (int i = 0; i < 4; i++) {
                    newCurrents[i] = Double.parseDouble(newCurrText[i].getText());
                }
                controller.setCurrents(newCurrents);
                log.debug("New currents sent to controller");
                for (int i = 0; i < 4; i++) {
                    positionText[i].setText("");
//                    oldCurrText[i].setText("");
                }

                File resultsFile = new File("./result.csv");

                if (resultsFile.isFile()) {
                    csvWrite(resultsFile);

                } else {
                    csvCreate(resultsFile);
                }
            }
            return null;
        }

        @Override
        protected void done() {
            // will be executed when background execution is done
            log.debug("Saved results to results.csv");
        }
    }

    class SetpointWorker extends SwingWorker<Void, Gui> {
        @Override
        protected Void doInBackground() throws Exception {
            //background task

            while (!isCancelled()) {
                try {
                    if (!controller.isSystemManual()) {
                        var = getRestVariable(Status.Fil_write);
                        FilSP.setText(String.format("%.2f", var.getAsDouble()));

                        var = getRestVariable(Status.Arc_write);
                        ArcSP.setText(String.format("%.2f", var.getAsDouble()));

                        var = getRestVariable(Status.MC_setpoint);
                        MCoilSP.setText(String.format("%.3f", var.getAsDouble()));

                        SourTuning.setSelected(controller.sourceTuning());

                        MCTuning.setSelected(controller.mainCoilTuning());

//                        var = getRestVariable(Status.CC_setpoint);
//                        CCoilSP.setText(String.format("%.2f", var.getAsDouble()));

//                        var = getRestVariable(Status.HC1_setpoint);
//                        HCoil1SP.setText(String.format("%.2f", var.getAsDouble()));
//
//                        var = getRestVariable(Status.HC2_setpoint);
//                        HCoil2SP.setText(String.format("%.2f", var.getAsDouble()));
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    log.warn("Trying to register tags with OPC open interface, make sure it is running!");
                    PowerSave.setSelected(false);
                } finally {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

//                try {
////                    if (!controller.isSystemManual()) {
//                        var = getRestVariable(Status.Fil_write);
//                        //FilSP.setText(String.format("%.2f", var.getAsDouble()));
//                   // }
//
//                } catch (Exception e) {
//                    e.printStackTrace();
//                    log.warn("Trying to read from OPC open interface, make sure it is running!");
//                    PowerSave.setSelected(false);
//                } finally {
//                    try {
//                        Thread.sleep(3000);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                }
            }
            return null;
        }

        private void publish() {
            //mainPanel.updateUI();
        }

        @Override
        protected void process(List<Gui> states) {
            // will be executed when background execution is done
            //log.debug("Saved results to results.csv");
//            Gui info = states.get(states.size() - 1);
//            info.mainPanel.updateUI();
        }
    }

    class BcreuWorker extends SwingWorker<Void, Status> {
        @Override
        protected Void doInBackground() throws Exception {
            //here you make heavy task this is running in another thread not in EDT
            //process after some time call publish()
            while (!isCancelled()) {
                if (!controller.bcreu.isConnected()) {
                    controller.bcreu.connect();
                }
                try {
                    controller.bcreu.refreshValues();
                    Status info = controller.checkBcreu();
                    publish(info);
                } finally {
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        log.error(e);
                    }
                }
            }
            return null;
        }

        @Override
        protected void process(List<Status> states) {
            //this is executed in the EDT
            // Only care about the latest values
            Status info = states.get(states.size() - 1);
            for (int i = 0; i < (Status.CYCLO_OFFSET + Status.Cyclo_names.length); i++) {
                if ((i >= Status.BLE_names.length) && (i < (Status.BLE_names.length + Status.BCREU_names.length))) {
                    bcreuText[i - Status.BLE_names.length].setText(info.getString(i));
                    bcreuText[i - Status.BLE_names.length].setBackground(info.getColor(i));
                }
            }
        }
    }

    class ConstantWorker2 extends SwingWorker<Void, Void> {
        @Override
        protected Void doInBackground() throws Exception {

            while (!isCancelled() && controller.acu.isConnected()) {
                try {
                    if (newCurrText[0] == null || newCurrText[1] == null || newCurrText[2] == null || newCurrText[3] == null ) {
                        applyButton.setEnabled(false);
                    }
//                    if (controller.isSystemManual() && locked){
//                        startBeamButton.setEnabled(true);
//                    }
                    //setSelectedBeamline(controller.getSelectedBeamline());
                    if (!PowerSave.isSelected()) {
                        powerSaveLabel.setText("Power Save Mode: Disabled");
                        powerSaveLabel.setForeground(automatic);
                    }
                    if (PowerSave.isSelected()) {
                        if (controller.readyForPowerSave()) {
                            powerSaveLabel.setText("Power Save Mode: Enabled");
                            powerSaveLabel.setForeground(manual);
                            controller.setESSCurrents();
                            controller.setBeamlineCurrents(controller.getSelectedBeamline());
                        } else {
                            powerSaveLabel.setText("Power Save Mode: Interlocked");
                            powerSaveLabel.setForeground(interlocked);
                        }
//                        if (controller.readyForVacuumSave()){
//                            controller.setSlits();
//                        }
                    }
                    if (controller.isSchedulingManual()) {
                        psInterlock2.setSelected(false);
                    } else {
                        psInterlock2.setSelected(true);
                    }
                    if (!controller.isBeamAllocated()){
                        printBeamline1 = false;
                        printBeamline4 = false;
                    }
                    if (controller.isBeamAllocated()) {
                        psInterlock3.setSelected(false);
                        if (controller.beam.beamScheduler.getCurrentBeamAllocation().getBeamSupplyPointId().toString().equalsIgnoreCase("FBTR1")) {
                            allocatedLabelTR1.setForeground(Color.BLACK);
                            if (!printBeamline1) {
                                //controller.trackMagnetFeedbackTR1();
                            }
                            printBeamline1 = true;
                        } else {
                            allocatedLabelTR1.setForeground(IbaColors.LT_GRAY);
                            printBeamline1 = true;
                        }
                        if (controller.beam.beamScheduler.getCurrentBeamAllocation().getBeamSupplyPointId().toString().equalsIgnoreCase("IBTR2-30") || controller.beam.beamScheduler.getCurrentBeamAllocation().getBeamSupplyPointId().toString().equalsIgnoreCase("IBTR2-90")) {
                            allocatedLabelTR2.setForeground(Color.BLACK);
                        } else {
                            allocatedLabelTR2.setForeground(IbaColors.LT_GRAY);
                        }
                        if (controller.beam.beamScheduler.getCurrentBeamAllocation().getBeamSupplyPointId().toString().equalsIgnoreCase("IBTR3-30") || controller.beam.beamScheduler.getCurrentBeamAllocation().getBeamSupplyPointId().toString().equalsIgnoreCase("IBTR3-90")) {
                            allocatedLabelTR3.setForeground(Color.BLACK);
                        } else {
                            allocatedLabelTR3.setForeground(IbaColors.LT_GRAY);
                        }
                        if (controller.beam.beamScheduler.getCurrentBeamAllocation().getBeamSupplyPointId().toString().equalsIgnoreCase("GTR4")) {
                            allocatedLabelTR4.setForeground(Color.BLACK);
                            if (!printBeamline4) {
                                //controller.trackMagnetFeedbackTR4();
                            }
                            printBeamline4 = true;
                        } else {
                            allocatedLabelTR4.setForeground(IbaColors.LT_GRAY);
                            printBeamline4 = false;
                        }
                    } else {
                        psInterlock3.setSelected(true);
                        allocatedLabelTR1.setForeground(IbaColors.LT_GRAY);
                        allocatedLabelTR2.setForeground(IbaColors.LT_GRAY);
                        allocatedLabelTR3.setForeground(IbaColors.LT_GRAY);
                        allocatedLabelTR4.setForeground(IbaColors.LT_GRAY);
                    }
                    if (controller.isRequestPending()) {
                        if (controller.isSystemManual()) {
                            if (!requestWarning){
                                log.error("[REQUEST] Pending BCP request, switch BSS to AUTOMATIC mode.");
                                requestWarning = true;
                            }
                            mainPanel.setBorder(BorderFactory.createMatteBorder(
                                    3, 3, 3, 3, Color.YELLOW));
                            Thread.sleep(1000);
                            mainPanel.setBorder(BorderFactory.createEmptyBorder(0,0,0,0));
                        }else {
                            mainPanel.setBorder(BorderFactory.createEmptyBorder(0,0,0,0));
                            requestWarning = false;
                        }

                        psInterlock4.setSelected(false);
                        for (PendingBeamRequest req : controller.beam.beamScheduler.getPendingBeamRequests()) {
                            if (req.getBeamSupplyPointId().equalsIgnoreCase("FBTR1")) {
                                queuedLabelTR1.setForeground(Color.BLACK);
                            } else {
                                queuedLabelTR1.setForeground(IbaColors.LT_GRAY);
                            }
                            if (req.getBeamSupplyPointId().equalsIgnoreCase("IBTR2-30") || req.getBeamSupplyPointId().equalsIgnoreCase("IBTR2-90")) {
                                queuedLabelTR2.setForeground(Color.BLACK);
                            } else {
                                queuedLabelTR2.setForeground(IbaColors.LT_GRAY);
                            }
                            if (req.getBeamSupplyPointId().equalsIgnoreCase("IBTR3-30") || req.getBeamSupplyPointId().equalsIgnoreCase("IBTR3-90")) {
                                queuedLabelTR3.setForeground(Color.BLACK);
                            } else {
                                queuedLabelTR3.setForeground(IbaColors.LT_GRAY);
                            }
                            if (req.getBeamSupplyPointId().equalsIgnoreCase("GTR4")) {
                                queuedLabelTR4.setForeground(Color.BLACK);
                            } else {
                                queuedLabelTR4.setForeground(IbaColors.LT_GRAY);
                            }
                        }
                    } else {
                        psInterlock4.setSelected(true);
                        queuedLabelTR1.setForeground(IbaColors.LT_GRAY);
                        queuedLabelTR2.setForeground(IbaColors.LT_GRAY);
                        queuedLabelTR3.setForeground(IbaColors.LT_GRAY);
                        queuedLabelTR4.setForeground(IbaColors.LT_GRAY);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    log.error("Exception in ConstantWorker2");
                } finally {
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        log.error(e);
                    }
                }
            }
            return null;
        }
    }

    class ConstantWorker extends SwingWorker<Void, Status> {
        @Override
        protected Void doInBackground() throws Exception {
            //here you make heavy task this is running in another thread not in EDT
            //process after some time call publish()

            while (!isCancelled()) {
                try {
//                    setSelectedBeamline(controller.getSelectedBeamline());
//                    if (!PowerSave.isSelected()){
//                        powerSaveLabel.setText("Power Save Mode: Disabled");
//                        powerSaveLabel.setForeground(automatic);
//                    }
                    isAutomaticMode = !controller.isSchedulingManual();
                    isADLogout = controller.beam.TSM3.getTreatmentRoomSession() != TreatmentRoomSession.Treatment;
                    isBeamline4Selected = controller.getSelectedBeamline() == 4;
                    isTR3SearchedInServiceMode = controller.isTR3SearchedInServiceMode();
                    mScanMagActivity = controller.beam.smpsController.getCurrentActivityName();
                    isXrayTubeRetracted = controller.isTR3XrayTubeExtracted();


                    if (controller.isBeamAllocated()) {
                        mEsbtsPanel.mBeamDeliveryPoint = Controller.feedbackClient.tc.getBeamDeliveryPoint(controller.beam.beamScheduler.getCurrentBeamAllocation().getBeamSupplyPointId());
                    }

//                    log.warn(controller.beam.BAPP1.getIrradiationStatus());
//                    log.warn(controller.beam.BAPP4.getIrradiationStatus());
//                    log.warn(controller.beam.BAPP1.getTreatmentRoomSession());
//                    log.warn(controller.beam.BAPP1.getTreatmentRoomSession());
//                    log.warn(controller.beam.TSM2.getTreatmentRoomSession().toString());
                    //log.warn("TSM3: " + controller.beam.TSM3.getTreatmentRoomSession().toString());
//                    log.warn(controller.beam.TSM4.getTreatmentRoomSession().toString());
                    //controller.beam.BAPP4.getTreatmentRoomSession();
                    mEsbtsPanel.updateSubPanelsStatus(readyForServiceBeam);

                    //mScanningControllerPanel.updateUI();
                    //mScanningControllerPanel.updatePanel(readyForServiceBeam);

                    //log.warn(controller.beam.bssController.getCurrentActivityName());
                    //log.warn(controller.beam.bssController.getCurrentActivityStatus());

                    mCurrentActivityName = controller.beam.bssController.getCurrentActivityName();
                    mCurrentActivityStatus = controller.beam.bssController.getCurrentActivityStatus();

                    if (mCurrentActivityName == BssActivityId.IDLE) {
                        mEsbtsPanel.mActionsPanel.mSelectBeamlineStatusLabel.setIcon(ResourceManager.getInstance().getImageIcon("bms/bss/controller/ui/inactive"));
                        mEsbtsPanel.mActionsPanel.mSetRangeStatusLabel.setIcon(ResourceManager.getInstance().getImageIcon("bms/bss/controller/ui/inactive"));
                        mEsbtsPanel.mActionsPanel.mEnableBeamStatusLabel.setIcon(ResourceManager.getInstance().getImageIcon("bms/bss/controller/ui/inactive"));
                        mEsbtsPanel.mActionsPanel.mDisableBeamStatusLabel.setIcon(ResourceManager.getInstance().getImageIcon("bms/bss/controller/ui/inactive"));
                        switch (mCurrentActivityStatus) {
                            case IDLE:
                                mEsbtsPanel.mActionsPanel.mIdleStatusLabel.setIcon(ResourceManager.getInstance().getImageIcon("bms/bss/controller/ui/inactive"));
                                break;
                            case ONGOING:
                                mEsbtsPanel.mActionsPanel.mIdleStatusLabel.setIcon(ResourceManager.getInstance().getImageIcon("bms/bss/controller/ui/running"));
                                break;
                            case COMPLETED:
                                mEsbtsPanel.mActionsPanel.mIdleStatusLabel.setIcon(ResourceManager.getInstance().getImageIcon("bms/bss/controller/ui/good"));
                                break;
                            case ERROR:
                                mEsbtsPanel.mActionsPanel.mIdleStatusLabel.setIcon(ResourceManager.getInstance().getImageIcon("bms/bss/controller/ui/bad"));
                                break;
                            case CANCELED:
                                mEsbtsPanel.mActionsPanel.mIdleStatusLabel.setIcon(ResourceManager.getInstance().getImageIcon("bms/bss/controller/ui/bad"));
                                break;
                            case DISABLED:
                                mEsbtsPanel.mActionsPanel.mIdleStatusLabel.setIcon(ResourceManager.getInstance().getImageIcon("bms/bss/controller/ui/inactive"));
                                break;
                        }
                    } else if (mCurrentActivityName == BssActivityId.PREPARE) {
                        mEsbtsPanel.mActionsPanel.mIdleStatusLabel.setIcon(ResourceManager.getInstance().getImageIcon("bms/bss/controller/ui/inactive"));
                        mEsbtsPanel.mActionsPanel.mSelectBeamlineStatusLabel.setIcon(ResourceManager.getInstance().getImageIcon("bms/bss/controller/ui/inactive"));
                        mEsbtsPanel.mActionsPanel.mEnableBeamStatusLabel.setIcon(ResourceManager.getInstance().getImageIcon("bms/bss/controller/ui/inactive"));
                        mEsbtsPanel.mActionsPanel.mDisableBeamStatusLabel.setIcon(ResourceManager.getInstance().getImageIcon("bms/bss/controller/ui/inactive"));
                        switch (mCurrentActivityStatus) {
                            case IDLE:
                                mEsbtsPanel.mActionsPanel.mSetRangeStatusLabel.setIcon(ResourceManager.getInstance().getImageIcon("bms/bss/controller/ui/inactive"));
                                break;
                            case ONGOING:
                                mEsbtsPanel.mActionsPanel.mSetRangeStatusLabel.setIcon(ResourceManager.getInstance().getImageIcon("bms/bss/controller/ui/running"));
                                break;
                            case COMPLETED:
                                mEsbtsPanel.mActionsPanel.mSetRangeStatusLabel.setIcon(ResourceManager.getInstance().getImageIcon("bms/bss/controller/ui/good"));
                                break;
                            case ERROR:
                                mEsbtsPanel.mActionsPanel.mSetRangeStatusLabel.setIcon(ResourceManager.getInstance().getImageIcon("bms/bss/controller/ui/bad"));
                                break;
                            case CANCELED:
                                mEsbtsPanel.mActionsPanel.mSetRangeStatusLabel.setIcon(ResourceManager.getInstance().getImageIcon("bms/bss/controller/ui/bad"));
                                break;
                            case DISABLED:
                                mEsbtsPanel.mActionsPanel.mSetRangeStatusLabel.setIcon(ResourceManager.getInstance().getImageIcon("bms/bss/controller/ui/bad"));
                                break;
                        }
                    } else if (mCurrentActivityName == BssActivityId.SET_RANGE) {
                        mEsbtsPanel.mActionsPanel.mIdleStatusLabel.setIcon(ResourceManager.getInstance().getImageIcon("bms/bss/controller/ui/inactive"));
                        mEsbtsPanel.mActionsPanel.mSelectBeamlineStatusLabel.setIcon(ResourceManager.getInstance().getImageIcon("bms/bss/controller/ui/inactive"));
                        mEsbtsPanel.mActionsPanel.mEnableBeamStatusLabel.setIcon(ResourceManager.getInstance().getImageIcon("bms/bss/controller/ui/inactive"));
                        mEsbtsPanel.mActionsPanel.mDisableBeamStatusLabel.setIcon(ResourceManager.getInstance().getImageIcon("bms/bss/controller/ui/inactive"));
                        switch (mCurrentActivityStatus) {
                            case IDLE:
                                mEsbtsPanel.mActionsPanel.mSetRangeStatusLabel.setIcon(ResourceManager.getInstance().getImageIcon("bms/bss/controller/ui/inactive"));
                                break;
                            case ONGOING:
                                mEsbtsPanel.mActionsPanel.mSetRangeStatusLabel.setIcon(ResourceManager.getInstance().getImageIcon("bms/bss/controller/ui/running"));
                                break;
                            case COMPLETED:
                                mEsbtsPanel.mActionsPanel.mSetRangeStatusLabel.setIcon(ResourceManager.getInstance().getImageIcon("bms/bss/controller/ui/good"));
                                break;
                            case ERROR:
                                mEsbtsPanel.mActionsPanel.mSetRangeStatusLabel.setIcon(ResourceManager.getInstance().getImageIcon("bms/bss/controller/ui/bad"));
                                break;
                            case CANCELED:
                                mEsbtsPanel.mActionsPanel.mSetRangeStatusLabel.setIcon(ResourceManager.getInstance().getImageIcon("bms/bss/controller/ui/bad"));
                                break;
                            case DISABLED:
                                mEsbtsPanel.mActionsPanel.mSetRangeStatusLabel.setIcon(ResourceManager.getInstance().getImageIcon("bms/bss/controller/ui/bad"));
                                break;
                        }
                    } else if (mCurrentActivityName == BssActivityId.ENABLE_BEAM) {
                        mEsbtsPanel.mActionsPanel.mIdleStatusLabel.setIcon(ResourceManager.getInstance().getImageIcon("bms/bss/controller/ui/inactive"));
                        mEsbtsPanel.mActionsPanel.mSelectBeamlineStatusLabel.setIcon(ResourceManager.getInstance().getImageIcon("bms/bss/controller/ui/inactive"));
                        mEsbtsPanel.mActionsPanel.mSetRangeStatusLabel.setIcon(ResourceManager.getInstance().getImageIcon("bms/bss/controller/ui/inactive"));
                        mEsbtsPanel.mActionsPanel.mDisableBeamStatusLabel.setIcon(ResourceManager.getInstance().getImageIcon("bms/bss/controller/ui/inactive"));
                        switch (mCurrentActivityStatus) {
                            case IDLE:
                                mEsbtsPanel.mActionsPanel.mEnableBeamStatusLabel.setIcon(ResourceManager.getInstance().getImageIcon("bms/bss/controller/ui/inactive"));
                                break;
                            case ONGOING:
                                mEsbtsPanel.mActionsPanel.mEnableBeamStatusLabel.setIcon(ResourceManager.getInstance().getImageIcon("bms/bss/controller/ui/running"));
                                break;
                            case COMPLETED:
                                mEsbtsPanel.mActionsPanel.mEnableBeamStatusLabel.setIcon(ResourceManager.getInstance().getImageIcon("bms/bss/controller/ui/good"));
                                break;
                            case ERROR:
                                mEsbtsPanel.mActionsPanel.mEnableBeamStatusLabel.setIcon(ResourceManager.getInstance().getImageIcon("bms/bss/controller/ui/bad"));
                                break;
                            case CANCELED:
                                mEsbtsPanel.mActionsPanel.mEnableBeamStatusLabel.setIcon(ResourceManager.getInstance().getImageIcon("bms/bss/controller/ui/bad"));
                                break;
                            case DISABLED:
                                mEsbtsPanel.mActionsPanel.mEnableBeamStatusLabel.setIcon(ResourceManager.getInstance().getImageIcon("bms/bss/controller/ui/bad"));
                                break;
                        }
                    } else if (mCurrentActivityName == BssActivityId.DISABLE_BEAM) {
                        mEsbtsPanel.mActionsPanel.mIdleStatusLabel.setIcon(ResourceManager.getInstance().getImageIcon("bms/bss/controller/ui/inactive"));
                        mEsbtsPanel.mActionsPanel.mSelectBeamlineStatusLabel.setIcon(ResourceManager.getInstance().getImageIcon("bms/bss/controller/ui/inactive"));
                        mEsbtsPanel.mActionsPanel.mSetRangeStatusLabel.setIcon(ResourceManager.getInstance().getImageIcon("bms/bss/controller/ui/inactive"));
                        mEsbtsPanel.mActionsPanel.mEnableBeamStatusLabel.setIcon(ResourceManager.getInstance().getImageIcon("bms/bss/controller/ui/inactive"));
                        switch (mCurrentActivityStatus) {
                            case IDLE:
                                mEsbtsPanel.mActionsPanel.mDisableBeamStatusLabel.setIcon(ResourceManager.getInstance().getImageIcon("bms/bss/controller/ui/inactive"));
                                break;
                            case ONGOING:
                                mEsbtsPanel.mActionsPanel.mDisableBeamStatusLabel.setIcon(ResourceManager.getInstance().getImageIcon("bms/bss/controller/ui/good"));
                                break;
                            case COMPLETED:
                                mEsbtsPanel.mActionsPanel.mDisableBeamStatusLabel.setIcon(ResourceManager.getInstance().getImageIcon("bms/bss/controller/ui/good"));
                                break;
                            case ERROR:
                                mEsbtsPanel.mActionsPanel.mDisableBeamStatusLabel.setIcon(ResourceManager.getInstance().getImageIcon("bms/bss/controller/ui/bad"));
                                break;
                            case CANCELED:
                                mEsbtsPanel.mActionsPanel.mDisableBeamStatusLabel.setIcon(ResourceManager.getInstance().getImageIcon("bms/bss/controller/ui/bad"));
                                break;
                            case DISABLED:
                                mEsbtsPanel.mActionsPanel.mDisableBeamStatusLabel.setIcon(ResourceManager.getInstance().getImageIcon("bms/bss/controller/ui/bad"));
                                break;
                        }
                    }
                    if (isTR3SearchedInServiceMode) {
                        mEsbtsPanel.mDevicesPanel.mIdleLabel2.setIcon(ResourceManager.getInstance().getImageIcon("icompx/check/OK"));
                    }else{
                        mEsbtsPanel.mDevicesPanel.mIdleLabel2.setIcon(ResourceManager.getInstance().getImageIcon("icompx/check/ERROR"));
                    }
                    if (isAutomaticMode) {
                        mEsbtsPanel.mDevicesPanel.mIdleLabel4.setIcon(ResourceManager.getInstance().getImageIcon("icompx/check/OK"));
                    }else{
                        mEsbtsPanel.mDevicesPanel.mIdleLabel4.setIcon(ResourceManager.getInstance().getImageIcon("icompx/check/ERROR"));
                    }
                    if (isADLogout) {
                        mEsbtsPanel.mDevicesPanel.mIdleLabel5.setIcon(ResourceManager.getInstance().getImageIcon("icompx/check/OK"));
                    }else{
                        mEsbtsPanel.mDevicesPanel.mIdleLabel5.setIcon(ResourceManager.getInstance().getImageIcon("icompx/check/ERROR"));
                    }
                    if (isBeamline4Selected) {
                        mEsbtsPanel.mDevicesPanel.mIdleLabel3.setIcon(ResourceManager.getInstance().getImageIcon("icompx/check/OK"));
                    }else{
                        mEsbtsPanel.mDevicesPanel.mIdleLabel3.setIcon(ResourceManager.getInstance().getImageIcon("icompx/check/ERROR"));
                    }
                    if (isXrayTubeRetracted){
                        mEsbtsPanel.mDevicesPanel.mIdleLabel6.setIcon(ResourceManager.getInstance().getImageIcon("icompx/check/OK"));
                    }else{
                        mEsbtsPanel.mDevicesPanel.mIdleLabel6.setIcon(ResourceManager.getInstance().getImageIcon("icompx/check/ERROR"));
                    }
                    if (isAutomaticMode && isADLogout && isBeamline4Selected && isTR3SearchedInServiceMode && isXrayTubeRetracted) {
                        //if (controller.beam.beamScheduler.getCurrentBeamAllocation().getBeamSupplyPointId().toString().equalsIgnoreCase("FBTR1")) {
                            mEsbtsPanel.mDevicesPanel.mIdleLabelOperMode.setText("Main Control Room is ready for service beam");
                            mEsbtsPanel.mDevicesPanel.mStartServiceBeam.setEnabled(true);
                            //readyForServiceBeam = true;
                        } else {
                            mEsbtsPanel.mDevicesPanel.mIdleLabelOperMode.setText("Main Control Room is in patient treatment mode");
                            readyForServiceBeam = false;
                            mEsbtsPanel.mDevicesPanel.mStartServiceBeam.setEnabled(false);
                        }


//                    } else {
//                        readyForServiceBeam = false;
//                        mBlankPanel.mDevicesPanel.mStartServiceBeam.setEnabled(false);
//                        readyForServiceBeam = true;
//                        mBlankPanel.mDevicesPanel.mStartServiceBeam.setEnabled(true);

                   // }
                    //log.warn(controller.beam.ISEU1.isChainClosed());
                    //log.warn(controller.beam.ISEU4.isChainClosed());
                   // if (!controller.isSystemManual()) {
                        //mBlankPanel.mDevicesPanel.mIdleLabelOperMode.setText("Main Control Room is in service mode");
                        //mBlankPanel.mDevicesPanel.mStartServiceBeam.setEnabled(true);
//                        if (controller.isRequestPending()) {
//                            for (PendingBeamRequest req : controller.beam.beamScheduler.getPendingBeamRequests()) {
//                                if (req.getBeamSupplyPointId().equalsIgnoreCase("FBTR1")) {
//                                    mBlankPanel.mDevicesPanel.mStartServiceBeam.setEnabled(false);
//                                    mBlankPanel.mDevicesPanel.mAllocateButton.setEnabled(true);
//                                }
//                            }
//                        }
                        if (readyForServiceBeam) {
                            //mBlankPanel.mDevicesPanel.mStartServiceBeam.setEnabled(true);
                            mEsbtsPanel.mSelectBeamlinePanel.mBeamCurrentTextField.setEnabled(true);
                            mEsbtsPanel.mSelectBeamlinePanel.mSLE1TextField.setEnabled(true);
                            mEsbtsPanel.mSelectBeamlinePanel.mSLE2TextField.setEnabled(true);
                            mEsbtsPanel.mSelectBeamlinePanel.mSLE3TextField.setEnabled(true);
                            beamSchedulerPanel.setEnabled(true);
                            //beamSchedulerPanel.setBeamRequestEnabled(true);
                            beamSchedulerPanel.updateAllocatedInfo();
                            beamSchedulerPanel.updatePendingList();

                            BeamAllocation allocation = Controller.beam.beamScheduler.getCurrentBeamAllocation();
                            if (allocation != null) {
                                Boolean allocated = allocation.getBeamSupplyPointId().contains("IBTR3");
                                // System.out.println(allocation.getBeamSupplyPointId());
                                beamSchedulerPanel.setBeamRequestEnabled(!allocated);
                                mEsbtsPanel.mSelectBeamlinePanel.mSMPSStartButton.setEnabled(allocated && mScanMagActivity.equals(SmpsControllerActivityId.IDLE));
                                mEsbtsPanel.mSelectBeamlinePanel.mSMPSStandbyButton.setEnabled(allocated && mScanMagActivity.equals(SmpsControllerActivityId.PREPARE));
                                mEsbtsPanel.mSelectBeamlinePanel.mSLE1Button.setEnabled(allocated);
                                //mEsbtsPanel.mSelectBeamlinePanel.mStartButton.setEnabled(allocated && controller.S2E.getPosition() == Insertable.Position.INSERTED);
                                mEsbtsPanel.getSetRangePanel().getStartButton().setEnabled(allocated && mCurrentActivityStatus != ActivityStatus.ONGOING);
                                mEsbtsPanel.updateActionButtonStatus(allocated);
                                if (allocated){
                                    if (controller.S2E.getPosition() == Insertable.Position.RETRACTED){
                                        startBeamButton.setEnabled(true);
                                        timedRunCB.setEnabled(true);
                                        postRunAction.setEnabled(true);
                                        timedRunText.setEnabled(timedRunCB.isSelected());

                                        mEsbtsPanel.mSelectBeamlinePanel.mStartButton.setEnabled(false);
                                        mEsbtsPanel.mSelectBeamlinePanel.mContinuousBeamCB.setEnabled(false);
                                    }else {
                                        startBeamButton.setEnabled(false);
                                        timedRunCB.setEnabled(false);
                                        timedRunText.setEnabled(false);
                                        postRunAction.setEnabled(false);
                                        mEsbtsPanel.mSelectBeamlinePanel.mStartButton.setEnabled(mCurrentActivityStatus != ActivityStatus.ONGOING);
                                        mEsbtsPanel.mSelectBeamlinePanel.mContinuousBeamCB.setEnabled(true);
                                    }
                                }
                            } else {
                                beamSchedulerPanel.setBeamRequestEnabled(true);
                                mEsbtsPanel.mSelectBeamlinePanel.mStartButton.setEnabled(false);
                                mEsbtsPanel.mSelectBeamlinePanel.mContinuousBeamCB.setEnabled(false);
                                mEsbtsPanel.mSelectBeamlinePanel.mSMPSStartButton.setEnabled(false);
                                mEsbtsPanel.mSelectBeamlinePanel.mSMPSStandbyButton.setEnabled(false);
                                mEsbtsPanel.mSelectBeamlinePanel.mSLE1Button.setEnabled(false);
                                mEsbtsPanel.getSetRangePanel().getStartButton().setEnabled(false);
                                mEsbtsPanel.updateActionButtonStatus(false);
                                startBeamButton.setEnabled(false);
                                timedRunCB.setEnabled(false);
                                timedRunText.setEnabled(false);
                                postRunAction.setEnabled(false);
                            }
                    } else {
                        mEsbtsPanel.mDevicesPanel.mIdleLabelOperMode.setText("Main Control Room is in patient treatment mode");

                        mEsbtsPanel.mSelectBeamlinePanel.mSLE1TextField.setEnabled(false);
                        mEsbtsPanel.mSelectBeamlinePanel.mSLE2TextField.setEnabled(false);
                        mEsbtsPanel.mSelectBeamlinePanel.mSLE3TextField.setEnabled(false);
                        mEsbtsPanel.mSelectBeamlinePanel.mSMPSStartButton.setEnabled(false);
                        mEsbtsPanel.mSelectBeamlinePanel.mSMPSStandbyButton.setEnabled(false);
                        mEsbtsPanel.mSelectBeamlinePanel.mStartButton.setEnabled(false);
                        mEsbtsPanel.mSelectBeamlinePanel.mContinuousBeamCB.setEnabled(false);
                        mEsbtsPanel.mSelectBeamlinePanel.mSLE1Button.setEnabled(false);
                        mEsbtsPanel.getSetRangePanel().getStartButton().setEnabled(false);
                        mEsbtsPanel.mSelectBeamlinePanel.mBeamCurrentTextField.setEnabled(false);
                        mEsbtsPanel.mDevicesPanel.mAllocateButton.setEnabled(false);
                        mEsbtsPanel.updateActionButtonStatus(false);
                        beamSchedulerPanel.setEnabled(false);
                        beamSchedulerPanel.setBeamRequestEnabled(false);
                        beamSchedulerPanel.updateAllocatedInfo();
                        beamSchedulerPanel.updatePendingList();
                        startBeamButton.setEnabled(false);
                        timedRunCB.setEnabled(false);
                        timedRunText.setEnabled(false);
                        postRunAction.setEnabled(false);

                    }
//                    if (PowerSave.isSelected()){
//                        if (controller.readyForPowerSave()) {
//                            controller.setESSCurrents();
//                            controller.setBeamlineCurrents(controller.getSelectedBeamline());
//                            powerSaveLabel.setText("Power Save Mode: Enabled");
//                            powerSaveLabel.setForeground(manual);
//                        } else {
//                            powerSaveLabel.setText("Power Save Mode: Interlocked");
//                            powerSaveLabel.setForeground(interlocked);
//                        }
//                    }
//                    if (controller.isSchedulingManual()){
//                        psInterlock2.setSelected(false);
//                    }else {psInterlock2.setSelected(true);}
//                    if (controller.isBeamAllocated()){
//                        psInterlock3.setSelected(false);
//                        if (controller.beam.beamScheduler.getCurrentBeamAllocation().getBeamSupplyPointId().toString().equalsIgnoreCase("FBTR1")){
//                            allocatedLabelTR1.setForeground(Color.BLACK);
//                        }else{ allocatedLabelTR1.setForeground(IbaColors.LT_GRAY);}
//                        if (controller.beam.beamScheduler.getCurrentBeamAllocation().getBeamSupplyPointId().toString().equalsIgnoreCase("IBTR2-30") || controller.beam.beamScheduler.getCurrentBeamAllocation().getBeamSupplyPointId().toString().equalsIgnoreCase("IBTR2-90")){
//                            allocatedLabelTR2.setForeground(Color.BLACK);
//                        }else{ allocatedLabelTR2.setForeground(IbaColors.LT_GRAY);}
//                        if (controller.beam.beamScheduler.getCurrentBeamAllocation().getBeamSupplyPointId().toString().equalsIgnoreCase("IBTR3-30") || controller.beam.beamScheduler.getCurrentBeamAllocation().getBeamSupplyPointId().toString().equalsIgnoreCase("IBTR3-90")){
//                            allocatedLabelTR3.setForeground(Color.BLACK);
//                        }else{ allocatedLabelTR3.setForeground(IbaColors.LT_GRAY);}
//                        if (controller.beam.beamScheduler.getCurrentBeamAllocation().getBeamSupplyPointId().toString().equalsIgnoreCase("GTR4")){
//                            allocatedLabelTR4.setForeground(Color.BLACK);
//                        }else{ allocatedLabelTR4.setForeground(IbaColors.LT_GRAY);}
//                    }else {psInterlock3.setSelected(true);
//                        allocatedLabelTR1.setForeground(IbaColors.LT_GRAY);
//                        allocatedLabelTR2.setForeground(IbaColors.LT_GRAY);
//                        allocatedLabelTR3.setForeground(IbaColors.LT_GRAY);
//                        allocatedLabelTR4.setForeground(IbaColors.LT_GRAY);
//                    }
//                    if (controller.isRequestPending()){
//                        psInterlock4.setSelected(false);
//                        for (PendingBeamRequest req : controller.beam.beamScheduler.getPendingBeamRequests()) {
//                            if (req.getBeamSupplyPointId().equalsIgnoreCase("FBTR1")) {
//                                queuedLabelTR1.setForeground(Color.BLACK);
//                            }else{queuedLabelTR1.setForeground(IbaColors.LT_GRAY);}
//                            if (req.getBeamSupplyPointId().equalsIgnoreCase("IBTR2-30") || req.getBeamSupplyPointId().equalsIgnoreCase("IBTR2-90")) {
//                                queuedLabelTR2.setForeground(Color.BLACK);
//                            }else{queuedLabelTR2.setForeground(IbaColors.LT_GRAY);}
//                            if (req.getBeamSupplyPointId().equalsIgnoreCase("IBTR3-30") || req.getBeamSupplyPointId().equalsIgnoreCase("IBTR3-90")) {
//                                queuedLabelTR3.setForeground(Color.BLACK);
//                            }else{queuedLabelTR3.setForeground(IbaColors.LT_GRAY);}
//                            if (req.getBeamSupplyPointId().equalsIgnoreCase("GTR4")) {
//                                queuedLabelTR4.setForeground(Color.BLACK);
//                            }else{queuedLabelTR4.setForeground(IbaColors.LT_GRAY);}
//                        }
//                    }else {psInterlock4.setSelected(true);
//                        queuedLabelTR1.setForeground(IbaColors.LT_GRAY);
//                        queuedLabelTR2.setForeground(IbaColors.LT_GRAY);
//                        queuedLabelTR3.setForeground(IbaColors.LT_GRAY);
//                        queuedLabelTR4.setForeground(IbaColors.LT_GRAY);
//                    }
//                    if (controller.isTR1Secured() || controller.isTR2Secured() || controller.isTR3Secured() || controller.isTR4Secured()) {
//                        psInterlock5.setSelected(false);
//                        if (controller.isTR1Secured()) {
//                            secureLabelTR1.setForeground(Color.BLACK);
//                        } else {
//                            secureLabelTR1.setForeground(IbaColors.LT_GRAY);
//                        }
//                        if (controller.isTR2Secured()) {
//                            secureLabelTR2.setForeground(Color.BLACK);
//                        } else {
//                            secureLabelTR2.setForeground(IbaColors.LT_GRAY);
//                        }
//                        if (controller.isTR3Secured()) {
//                            secureLabelTR3.setForeground(Color.BLACK);
//                        } else {
//                            secureLabelTR3.setForeground(IbaColors.LT_GRAY);
//                        }
//                        if (controller.isTR4Secured()) {
//                            secureLabelTR4.setForeground(Color.BLACK);
//                        } else {
//                            secureLabelTR4.setForeground(IbaColors.LT_GRAY);
//                        }
//                    } else {
//                        psInterlock5.setSelected(true);
//                        secureLabelTR1.setForeground(IbaColors.LT_GRAY);
//                        secureLabelTR2.setForeground(IbaColors.LT_GRAY);
//                        secureLabelTR3.setForeground(IbaColors.LT_GRAY);
//                        secureLabelTR4.setForeground(IbaColors.LT_GRAY);
//                    }
//                    if (controller.isTR1Searching() || controller.isTR2Searching() || controller.isTR3Searching() || controller.isTR4Searching()) {
//                        if (!idle) {
//                            if (!searchWarning){
//                                log.error("[SEARCH] TR search is active, IDLE BCP and switch BSS to AUTOMATIC mode.");
//                                searchWarning = true;
//                            }
//                            mainPanel.setBorder(BorderFactory.createMatteBorder(
//                                    3, 3, 3, 3, Color.YELLOW));
//                            Thread.sleep(1000);
//                            mainPanel.setBorder(BorderFactory.createEmptyBorder(0,0,0,0));
//                        }else {
//                            mainPanel.setBorder(BorderFactory.createEmptyBorder(0,0,0,0));
//                            searchWarning = false;
//                        }
//                        psInterlock6.setSelected(false);
//                        if (controller.isTR1Searching()) {
//                            searchingLabelTR1.setForeground(Color.BLACK);
//                        } else {
//                            searchingLabelTR1.setForeground(IbaColors.LT_GRAY);
//                        }
//                        if (controller.isTR2Searching()) {
//                            searchingLabelTR2.setForeground(Color.BLACK);
//                        } else {
//                            searchingLabelTR2.setForeground(IbaColors.LT_GRAY);
//                        }
//                        if (controller.isTR3Searching()) {
//                            searchingLabelTR3.setForeground(Color.BLACK);
//                        } else {
//                            searchingLabelTR3.setForeground(IbaColors.LT_GRAY);
//                        }
//                        if (controller.isTR4Searching()) {
//                            searchingLabelTR4.setForeground(Color.BLACK);
//                        } else {
//                            searchingLabelTR4.setForeground(IbaColors.LT_GRAY);
//                        }
//                    } else {
//                        psInterlock6.setSelected(true);
//                        searchingLabelTR1.setForeground(IbaColors.LT_GRAY);
//                        searchingLabelTR2.setForeground(IbaColors.LT_GRAY);
//                        searchingLabelTR3.setForeground(IbaColors.LT_GRAY);
//                        searchingLabelTR4.setForeground(IbaColors.LT_GRAY);
//                    }
//                    if (controller.isSystemManual()) {
//                        operModeLabel.setText("Operating Mode: Manual");
//                        operModeLabel.setForeground(manual);
//                        operModeLabel2.setText("Operating Mode: Manual");
//                        operModeLabel2.setForeground(manual);
//                        operModeLabel3.setText("Operating Mode: Manual");
//                        operModeLabel3.setForeground(manual);
//                        alignMeButton.setEnabled(true);
//                        cancelButton.setEnabled(true);
//                        computeButton.setEnabled(true);
//                        saveMeButton.setEnabled(true);
//                        psInterlock1.setSelected(false);
//                        SourTuning.setEnabled(true);
//                        MCTuning.setEnabled(true);
//                        //applyButton.setEnabled(true);
//                        cancelTuneButton.setEnabled(true);
//                        mainCoilButton.setEnabled(true);
//                        cancelButton2.setEnabled(true);
//                        burnInStartButton.setEnabled(true);
////                        Dee1plus.setEnabled(true);
////                        Dee1minus.setEnabled(true);
////                        Dee2plus.setEnabled(true);
////                        Dee2minus.setEnabled(true);
////                        VDeeSP.setEditable(true);
////                        VDeeSP2.setEditable(true);
//                        FilSP.setEditable(true);
//                        Filplus.setEnabled(true);
//                        Filminus.setEnabled(true);
//                        ArcSP.setEditable(true);
//                        Arcplus.setEnabled(true);
//                        Arcminus.setEnabled(true);
////                        CCoilSP.setEditable(true);
////                        CCoilplus.setEnabled(true);
////                        CCoilminus.setEnabled(true);
////                        MCoilSP.setEditable(true);
////                        MCoilplus.setEnabled(true);
////                        MCoilminus.setEnabled(true);
////                        HCoil1SP.setEditable(true);
////                        HCoil1plus.setEnabled(true);
////                        HCoil1minus.setEnabled(true);
////                        HCoil2SP.setEditable(true);
////                        HCoil2plus.setEnabled(true);
////                        HCoil2minus.setEnabled(true);
//                        rfLUTButton.setEnabled(true);
//                    }
////                    if (updated){
////                        ImageIcon icon = new ImageIcon("checkmark.png");
////                        Dimension dimension = new Dimension(10,10);
////                        JLabel thumb = new JLabel();
////                        thumb.setIcon(icon);
////                        thumb.setLayout(new GridBagLayout());
////                        thumb.setPreferredSize(dimension);
////                        GridBagConstraints c = new GridBagConstraints();
////                        c.insets = new Insets(0,75,50,0);
////                        //c.gridx = 1;
////                       // c.anchor = GridBagConstraints.SOUTH;
////                        tuningInstructionPanel.add(thumb, c);
////                        tuningInstructionPanel.repaint();
////                        //tuningInstructionPanel.updateUI();
////
////                    }
//                    if (controller.isSystemManual() && updated && idle) {
//                        cancelTuneButton.setEnabled(true);
//                        mainCoilButton.setEnabled(true);
//                        rfLUTButton.setEnabled(true);
//                        FilSP.setEditable(true);
//                        Filplus.setEnabled(true);
//                        Filminus.setEnabled(true);
//                        ArcSP.setEditable(true);
//                        Arcplus.setEnabled(true);
//                        Arcminus.setEnabled(true);
//                        SourTuning.setEnabled(true);
//                        MCTuning.setEnabled(true);
////                        MCoilSP.setEditable(true);
////                        MCoilplus.setEnabled(true);
////                        MCoilminus.setEnabled(true);
//                    }
//                    if (controller.isSystemManual() && updated){
//                        cancelButton2.setEnabled(true);
//                    }
//                    if (controller.isSystemManual() && updated && !MCTuning.isSelected()) {
//                        MCoilplus.setEnabled(true);
//                        MCoilminus.setEnabled(true);
//                    }
//                    if ((controller.isSystemManual() && !updated) || (controller.isSystemManual() && !idle)){
//                        mainCoilButton.setEnabled(false);
//                        rfLUTButton.setEnabled(false);
//                    }
//                    if (controller.isSystemManual() && !updated) {
//                        cancelButton2.setEnabled(false);
//                        mainCoilButton.setEnabled(false);
//                        rfLUTButton.setEnabled(false);
//                        MCoilSP.setEditable(false);
//                        MCoilplus.setEnabled(false);
//                        MCoilminus.setEnabled(false);
//                        FilSP.setEditable(false);
//                        Filplus.setEnabled(false);
//                        Filminus.setEnabled(false);
//                        ArcSP.setEditable(false);
//                        Arcplus.setEnabled(false);
//                        Arcminus.setEnabled(false);
//                        SourTuning.setEnabled(false);
//                        MCTuning.setEnabled(false);
//                    }
//                    if (!controller.isSystemManual()) {
//                        updated = false;
//                        operModeLabel.setText("Operating Mode: Automatic");
//                        operModeLabel.setForeground(automatic);
//                        operModeLabel2.setText("Operating Mode: Automatic");
//                        operModeLabel2.setForeground(automatic);
//                        operModeLabel3.setText("Operating Mode: Automatic");
//                        operModeLabel3.setForeground(automatic);
//                        SourTuning.setEnabled(false);
//                        MCTuning.setEnabled(false);
//                        alignMeButton.setEnabled(false);
//                        applyButton.setEnabled(false);
//                        cancelButton.setEnabled(false);
//                        computeButton.setEnabled(false);
//                        cancelTuneButton.setEnabled(false);
//                        mainCoilButton.setEnabled(false);
//                        cancelButton2.setEnabled(false);
//                        saveMeButton.setEnabled(false);
//                        burnInStartButton.setEnabled(false);
//                        FilSP.setEditable(false);
//                        Filplus.setEnabled(false);
//                        Filminus.setEnabled(false);
//                        ArcSP.setEditable(false);
//                        Arcplus.setEnabled(false);
//                        Arcminus.setEnabled(false);
//                        MCoilSP.setEditable(false);
//                        MCoilplus.setEnabled(false);
//                        MCoilminus.setEnabled(false);
//                        rfLUTButton.setEnabled(false);
//                        psInterlock1.setSelected(true);
////                        Dee1plus.setEnabled(false);
////                        Dee1minus.setEnabled(false);
////                        Dee2plus.setEnabled(false);
////                        Dee2minus.setEnabled(false);
////                        VDeeSP.setEditable(false);
////                        VDeeSP2.setEditable(false);
////                        CCoilSP.setEditable(false);
////                        CCoilplus.setEnabled(false);
////                        CCoilminus.setEnabled(false);
////                        HCoil1SP.setEditable(false);
////                        HCoil1plus.setEnabled(false);
////                        HCoil1minus.setEnabled(false);
////                        HCoil2SP.setEditable(false);
////                        HCoil2plus.setEnabled(false);
////                        HCoil2minus.setEnabled(false);
//                    }
//                    if (controller.isSystemManual() && counter != 1) {
//                        refreshButton.setEnabled(true);
//                        applyButton.setEnabled(true);
//                        counter = 1;
//                    }
//                    if (!controller.isSystemManual() && counter != 2) {
//                        refreshButton.setEnabled(false);
//                        applyButton.setEnabled(false);
//                        counter = 2;
//                    }
//                    if (!controller.acu.isConnected()) {
//                        controller.acu.connect();
//                    }

                    if (restManager.getVariable("S0.C0.GEI01.AcuOkTre").get("value").getAsBoolean()){
                        rfSparkDetected.setIcon(ResourceManager.getInstance().getImageIcon("icompx/check/OK"));
                    }else {
                        rfSparkDetected.setIcon(ResourceManager.getInstance().getImageIcon("icompx/check/ERROR"));
                        if (TimerPanel.getTimer().isRunning()){
                            if (mEsbtsPanel.mSelectBeamlinePanel.mContinuousBeamCB.isSelected()){
                                Controller.beam.bcreu.pauseRegulation();
                            }else{
                                Controller.beam.bcreu.setContinuousPulse(false);
                            }

                            if (beamResults.isFile()) {
                                csvWriteBeamOff(beamResults, 3, TimerPanel.getClockTime());
                            } else {
                                log.error("[Beam Record] Could not find Beam Results file. Path: " + beamResults.getAbsolutePath());
                            }

                            TimerPanel.getTimer().stop();
                            startBeamButton.setText("Start Beam");

                            if (postRunAction.getSelectedItem().toString().equalsIgnoreCase("RF")){
                                Controller.beam.llrf.setDeeVoltage2(41.00d);
                            }else{
                                try {
                                    Controller.beam.bssController.startDisableBeamActivity(Controller.beam.beamScheduler.getCurrentBeamAllocation().getBeamSupplyPointId());
                                    Thread.sleep(1000);
                                    if (Controller.S2E.getPosition() == Insertable.Position.MOVING || Controller.S2E.getPosition() == Insertable.Position.INSERTED) {
                                        //do nothing
                                    } else {
                                        Controller.ecubtcu.bsInsert("S2E");
                                    }
                                } catch (Exception g) {
                                    g.printStackTrace();
                                }
                            }
                        }
                    }

                    if (!controller.feedbackClient.isConnected()) {
                        controller.feedbackClient.connect();
                    }
                    if (!controller.ecubtcu.isConnected()) {
                        controller.ecubtcu.connect();
                    }
                    if (!controller.feedbackClient.isConnected()) {
                        if (mBcreuProxy.getState().equals(StateValue.STARTED)) {
                            mBcreuProxy.cancel(true);
                        }
//                        if (mRefreshWorker.getState().equals(StateValue.STARTED)) {
//                            mRefreshWorker.cancel(true);
//                        }
//                        if (controller.ecubtcu.isConnected()) {
//                            controller.ecubtcu.disconnect();
//                        }
//                        if (controller.acu.isConnected()) {
//                            controller.acu.disconnect();
//                        }
//                        if (mTask2.getState().equals(StateValue.STARTED)) {
//                            mTask2.cancel(true);
//                        }
//                        if (mTask.getState().equals(StateValue.STARTED)) {
//                            mTask.cancel(true);
//                        }
                        this.cancel(false);
                        log.error("Connection Error: Please restart Service Beam GUI");
                        return null;
                    }
                    Status info = controller.checkStatus();
                    publish(info);
                } catch (Exception e) {
                    e.printStackTrace();
                    log.error("Exception in ConstantWorker");
                } finally {
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        log.error(e);
                    }
                }
            }
            return null;
        }

        @Override
        protected void process(List<Status> states) {
            //this is executed in the EDT
            // Only care about the latest values
            Status info = states.get(states.size() - 1);
            for (int i = 0; i < (Status.BLE_names.length); i++) {

                statusText[i].setText(info.getString(i));
                statusText[i].setBackground(info.getColor(i));
            }
//                } else if ((i >= Status.MAGNET_OFFSET) && (i < (Status.MAGNET_OFFSET + Status.Magnet_names.length))) {
//                    oldCurrText[i - Status.MAGNET_OFFSET].setText(info.getString(i));
//                    oldCurrText[i - Status.MAGNET_OFFSET].setBackground(info.getColor(i));
//                    oldCurrText2[i - Status.MAGNET_OFFSET].setText(info.getString(i));
//                    oldCurrText2[i - Status.MAGNET_OFFSET].setBackground(info.getColor(i));
//                } else if ((i >= Status.CYCLO_OFFSET) && (i < Status.CYCLO_OFFSET + Status.Cyclo_names.length)) {
//                    cycloText[i - Status.CYCLO_OFFSET].setText(info.getString(i));
//                    cycloText[i - Status.CYCLO_OFFSET].setBackground(info.getColor(i));
//                } else if ((i >= Status.CYCLOTUNING_OFFSET) && (i < Status.CYCLOTUNING_OFFSET + Status.CycloTuning_names.length)) {
//                    MCTuningText[i - Status.CYCLOTUNING_OFFSET].setText(info.getString(i));
//                    MCTuningText[i - Status.CYCLOTUNING_OFFSET].setBackground(info.getColor(i));
//                } else if ((i >= Status.LLRFTUNING_OFFSET) && (i < Status.LLRFTUNING_OFFSET + Status.LLRF_read.length)) {
//                    String rounded = "";
//                    rounded = String.format("%.2f", (Double.valueOf(info.getString(i))));
//                    if (i == Status.LLRFTUNING_OFFSET) {
//                        VDeeFB.setText(rounded);
//                    }
//                    if (i == Status.LLRFTUNING_OFFSET + 1) {
//                        FilFB.setText(rounded);
//                    }
//                    if (i == Status.LLRFTUNING_OFFSET + 2) {
//                        ArcFB.setText(rounded);
//                    }
//                    if (i == Status.LLRFTUNING_OFFSET + 3) {
//                        ArcVolt.setText(rounded);
//                    }
//                    if (i == Status.LLRFTUNING_OFFSET + 4) {
//                        CCoilFB.setText(rounded);
//                    }
//                    if (i == Status.LLRFTUNING_OFFSET + 5) {
//                        MCoilFB.setText(String.format("%.3f", (Double.valueOf(info.getString(i)))));
//                    }
//                    if (i == Status.LLRFTUNING_OFFSET + 6){
//                        HCoil1FB.setText(rounded);
//                    }
//                    if (i == Status.LLRFTUNING_OFFSET + 7){
//                        HCoil2FB.setText(rounded);
//                    }
//
//                }
//            }
//            if (controller.isSystemManual() && readCurrent) {
//                val = controller.bcreu.getIcCyclo();
            //var = restManager.getVariable(Status.Arc_voltage);

            //double val2 = (Double) Controller.acu.getTagValue(Status.Arc_voltage);

            //double val2 = var.getAsDouble();
//                if (val > 1.0) {
//                    beamChart.update(val);
//                    //log.warn("ICCyclo: " + val);
//                    //log.warn("Arc Voltage: " + val2);
//                }
//            }
//            if (mPrepDialog != null) {
//                if (mPrepDialog.isVisible()) {
//                    mPrepDialog.setCheckBoxes(info.getPrep());
//                }
//            }
//        }
        }
    }

    private void setSelectedBeamline(int bp) {
        switch (bp) {
            case 1:
                powerSaveLabelTR1.setBorder(BorderFactory.createMatteBorder(0, 0, 3, 0, Color.ORANGE));
                powerSaveLabelTR2.setBorder(BorderFactory.createMatteBorder(0, 0, 3, 0, IbaColors.BT_GRAY));
                powerSaveLabelTR3.setBorder(BorderFactory.createMatteBorder(0, 0, 3, 0, IbaColors.BT_GRAY));
                powerSaveLabelTR4.setBorder(BorderFactory.createMatteBorder(0, 0, 3, 0, IbaColors.BT_GRAY));
                break;
            case 2:
                powerSaveLabelTR1.setBorder(BorderFactory.createMatteBorder(0, 0, 3, 0, IbaColors.BT_GRAY));
                powerSaveLabelTR2.setBorder(BorderFactory.createMatteBorder(0, 0, 3, 0, Color.ORANGE));
                powerSaveLabelTR3.setBorder(BorderFactory.createMatteBorder(0, 0, 3, 0, IbaColors.BT_GRAY));
                powerSaveLabelTR4.setBorder(BorderFactory.createMatteBorder(0, 0, 3, 0, IbaColors.BT_GRAY));
                break;
            case 3:
                powerSaveLabelTR1.setBorder(BorderFactory.createMatteBorder(0, 0, 3, 0, IbaColors.BT_GRAY));
                powerSaveLabelTR2.setBorder(BorderFactory.createMatteBorder(0, 0, 3, 0, Color.ORANGE));
                powerSaveLabelTR3.setBorder(BorderFactory.createMatteBorder(0, 0, 3, 0, IbaColors.BT_GRAY));
                powerSaveLabelTR4.setBorder(BorderFactory.createMatteBorder(0, 0, 3, 0, IbaColors.BT_GRAY));
                break;
            case 4:
                powerSaveLabelTR1.setBorder(BorderFactory.createMatteBorder(0, 0, 3, 0, IbaColors.BT_GRAY));
                powerSaveLabelTR2.setBorder(BorderFactory.createMatteBorder(0, 0, 3, 0, IbaColors.BT_GRAY));
                powerSaveLabelTR3.setBorder(BorderFactory.createMatteBorder(0, 0, 3, 0, Color.ORANGE));
                powerSaveLabelTR4.setBorder(BorderFactory.createMatteBorder(0, 0, 3, 0, IbaColors.BT_GRAY));
                break;
            case 5:
                powerSaveLabelTR1.setBorder(BorderFactory.createMatteBorder(0, 0, 3, 0, IbaColors.BT_GRAY));
                powerSaveLabelTR2.setBorder(BorderFactory.createMatteBorder(0, 0, 3, 0, IbaColors.BT_GRAY));
                powerSaveLabelTR3.setBorder(BorderFactory.createMatteBorder(0, 0, 3, 0, Color.ORANGE));
                powerSaveLabelTR4.setBorder(BorderFactory.createMatteBorder(0, 0, 3, 0, IbaColors.BT_GRAY));
                break;
            case 6:
                powerSaveLabelTR1.setBorder(BorderFactory.createMatteBorder(0, 0, 3, 0, IbaColors.BT_GRAY));
                powerSaveLabelTR2.setBorder(BorderFactory.createMatteBorder(0, 0, 3, 0, IbaColors.BT_GRAY));
                powerSaveLabelTR3.setBorder(BorderFactory.createMatteBorder(0, 0, 3, 0, IbaColors.BT_GRAY));
                powerSaveLabelTR4.setBorder(BorderFactory.createMatteBorder(0, 0, 3, 0, Color.ORANGE));
                break;
        }
    }

    class RefreshWorker extends SwingWorker<Void, Status> {
        @Override
        protected Void doInBackground() throws Exception {
            if (!controller.acu.isConnected()) {
                //controller.acu.connect();
                //controller.bcreu.disconnect();
            }
            //           if (!controller.feedbackClient.isConnected()) {
            //               controller.feedbackClient.connect();
            //           }
            if (!controller.ecubtcu.isConnected()) {
               // controller.ecubtcu.connect();
            }

//            if (!controller.llrf.isConnected()) {
//                controller.llrf.connect();
//            }

            // AMO trying to get bcreu feedback to restart after cancel is pressed
            if (!controller.bcreu.isConnected()) {
               // controller.bcreu.connect();
            }

            // Display if user-action is needed
            if (controller.displayErrors()) {
                if (!refreshButton.isEnabled()) {
                    refreshButton.setEnabled(controller.isSystemManual());
                }
                return null;
            }

            Status checks = controller.getStatus();
            // If the system isn't ready, try to prepareForAlignment.
            if (!checks.andBool(10)) {
                // TODO: Re-work code so that dialog displays before prepareForAlignment() is called
                // AMO - added new thread runnable for prep dialog

                Thread prepDialog = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if ((mPrepDialog == null) || !mPrepDialog.isVisible()) {
                                mPrepDialog = new TaskListDialog(null, refreshButton, "Preparing...", Status.Prep_names,
                                        checks.getPrep(), Status.PREP_USER);
                                mPrepDialog.setVisible(true);
                            }
                        } catch (Exception f) {
                            log.error("Prep Dialog could not be started");
                            f.printStackTrace();
                        }
                    }
                });
                prepDialog.start();


                controller.prepareForAlignment();
                // Prevent multiple instances of the prep dialog
//                if ((mPrepDialog == null) || !mPrepDialog.isVisible()) {
//                    mPrepDialog = new TaskListDialog(null, refreshButton, "Preparing...", Status.Prep_names,
//                            checks.getPrep(), Status.PREP_USER);
//                    mPrepDialog.setVisible(true);
//                }

                if (!checks.getBool(Status.IC_CYCLO)) {
                    Thread.sleep(6000);
                }

                if (!checks.getBool(Status.S2E_STATUS) && checks.getBool(Status.IC_CYCLO)) {
                    log.warn("BCREU is prepped, but S2E is not retracted.");
                    controller.idleBCP();
                    Thread.sleep(7000);
                    controller.prep10nA();
                }

                // If the dialog was closed or cancelled prematurely, stop
                if (mPrepDialog.isCancelled()) {
                    if (!refreshButton.isEnabled()) {
                        refreshButton.setEnabled(true);
                    }
                    if (!applyButton.isEnabled()) {
                        applyButton.setEnabled(true);
                    }
                    log.info("System preparation cancelled by user. Setting end of treatment.");
//                    controller.ecubtcu.iseuRequestSetEndOfTreatmentMode();
                    return null;
                }
            }


            while (!mPrepDialog.isDone()) {
                Thread.sleep(100);
            }

            log.info("System Prepared. Starting measurements.");

            if (controller.refreshAll()) {
                refresh();

                log.warn("[REFRESH] complete.");
                if (!refreshButton.isEnabled()) {
                    refreshButton.setEnabled(true);
                }
                if (!applyButton.isEnabled()) {
                    applyButton.setEnabled(true);
                }
            } else {
                log.error("[REFRESH] Unable to acquire new BPM measurements.");
                if (!refreshButton.isEnabled()) {
                    refreshButton.setEnabled(true);
                }
//                if (!applyButton.isEnabled()) {
//                    applyButton.setEnabled(true);
//                }
            }

            //here you make heavy task this is running in another thread not in EDT
            //process after some time call publish()
//            while (!isCancelled()) {
//                try {
//
//                } finally {
//                    try {
//                        Thread.sleep(200);
//                    } catch (InterruptedException e) {
//                        log.error(e);
//                    }
//                }
//            }
            return null;
        }

        @Override
        protected void process(List<Status> states) {

        }
    }


    /**
     * Preconditions for source burn-in
     * -RF standby
     * -Arc OFF
     * -Filament set to 125A
     * -Filament ON
     **/
    public static class TimerPanel extends JPanel {

        public static Timer timer;
        private long startTime = -1;
        public long now;
        public static long clockTime;

        private long oneMin = 60000;
        private long fiveMin = oneMin * 5;

        private long step0 = 0;
        private long step1 = fiveMin;
        private long step2 = step1 + fiveMin;
        private long step3 = step2 + (oneMin*4);
        private long step4 = step3 + (oneMin*3);
        private long step5 = step4 + (oneMin*3);
        private long step6 = step5 + (oneMin*3);
        private long step7 = step6 + (oneMin*2);
        private long step8 = step7 + (oneMin*2);
        private long step9 = step8 + (oneMin*2);
        private long step10 = step9 + (oneMin*2);
        private long step11 = step10 + (oneMin*2);

        private long step[];

        private long stepArc;
        private long stepTuning = stepArc;

        private long totalDuration = stepTuning + 1000;

        private int flag[];

        private JLabel label;
        private JLabel statusLabel;
        private JTextArea textArea;
        private String progressString;

        private JTextField setpointTitle;
        private JTextField setpointTitle2;
        private JTextField setpoint[];
        private JTextField setpointArc;
        private JTextField setpointTuning;

        private JTextField durationTitle;
        private JTextField durationTitle2;
        private JTextField duration[];
        private JTextField durationArc;
        private JTextField durationTuning;

        private TitledBorderNoEdge title;
        private TitledBorder title2;

        private DurationFormatUtils dfu = new DurationFormatUtils();


        public TimerPanel() {

            String pattern = "MM-dd";
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
            String date = simpleDateFormat.format(new Date());
            beamResults = new File("./BeamResults/ServiceBeam_" + date + ".csv");

//            step = new long[13];
//            step[0] = 0;
//            step[1] = fiveMin;
//            step[2] = step[1] + fiveMin;
//            step[3] = step[2] + (oneMin*4);
//            step[4] = step[3] + (oneMin*3);
//            step[5] = step[4] + (oneMin*3);
//            step[6] = step[5] + (oneMin*3);
//            step[7] = step[6] + (oneMin*3);
//            step[8] = step[7] + (oneMin*2);
//            step[9] = step[8] + (oneMin*2);
//            step[10] = step[9] + (oneMin*2);
//            step[11] = step[10] + (oneMin*2);
//            step[12] = step[11] + (oneMin);
//            stepTuning = step[12];
//            totalDuration = step[12];
//
//            flag = new int[13];
//            for (int i = 1; i <= 12; i++){
//                flag[i] = 0;
//            }

            setLayout(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 1;
            gbc.gridy = 0;
//            gbc.gridwidth = 2;
//            gbc.insets = new Insets(0, 0, 0, 0);
//            gbc.anchor = GridBagConstraints.CENTER;
//            statusLabel = new JLabel("  Status: Not Running   ");
//            statusLabel.setFont(new Font("Dialog", Font.BOLD, 24));
//           // add(statusLabel, gbc);
//
//            operModeLabel3.setFont(new Font("Dialog", Font.BOLD, 16));
//            operModeLabel3.setText("Operating Mode: Manual");
//            operModeLabel3.setHorizontalAlignment(JLabel.CENTER);
//
//            gbc.gridy = 2;
//           // add(operModeLabel3, gbc);
//
//            gbc.gridx = 2;
//            gbc.gridy = 6;
//            gbc.gridheight = 2;
//            gbc.insets = new Insets(0, 0, 0, 0);
            add(new JPanel(), gbc);
            gbc.gridy++;
            add(new JPanel(), gbc);
            gbc.gridy++;
            add(new JPanel(), gbc);
            gbc.gridy++;
            add(new JPanel(), gbc);
            gbc.gridy++;
            add(new JPanel(), gbc);
            gbc.gridy++;
            Font bigTitle = new Font(Font.DIALOG, Font.BOLD, 18);
            rfSparkDetected.setFont(bigTitle);
            rfSparkDetected.setIcon(ResourceManager.getInstance().getImageIcon("icompx/icons/disabled"));
            gbc.anchor = GridBagConstraints.CENTER;
            //rfSparkDetected.setHorizontalAlignment(JLabel.LEFT);
            add(rfSparkDetected, gbc);

            add(new JPanel(), gbc);
            gbc.gridy++;
            add(new JPanel(), gbc);
            gbc.gridy++;
//            add(new JPanel(), gbc);
//            gbc.gridy++;
//            add(new JPanel(), gbc);
//            gbc.gridy++;
            add(new JPanel(), gbc);
            gbc.gridy++;
            add(new JPanel(), gbc);
            gbc.gridy++;
            add(new JPanel(), gbc);
            gbc.gridy++;
            add(new JPanel(), gbc);
            gbc.gridy++;
            add(new JPanel(), gbc);
            gbc.gridy++;
            add(new JPanel(), gbc);
            gbc.gridy++;

            gbc.anchor = GridBagConstraints.CENTER;
            label = new JLabel("00:00");
            label.setText(dfu.formatDuration(0, "mm:ss:SSS"));
            label.setFont(new Font("Dialog", Font.BOLD, 48));
            add(label, gbc);

            //gbc.gridx = 2;
            gbc.gridy++;
            gbc.gridheight = 1;
            gbc.weightx = 1; // full width
            gbc.weighty = 1;
            gbc.ipady = IPADY_DEFAULT;
            //gbc.insets = new Insets(0, 0, 0, 0);
            gbc.anchor = GridBagConstraints.CENTER;
            startBeamButton = new JButton("Start Beam");
            startBeamButton.setToolTipText("Start/Pause the beam output.");
            startBeamButton.setEnabled(false);
            add(startBeamButton, gbc);

            //gbc.gridx =0;
            timedRunCB = new JCheckBox("Timed Run");
            timedRunCB.setToolTipText("Un-check for open-ended beam runs (requires manually pausing/stopping beam)");
            timedRunCB.setFont(new Font("Dialog", Font.BOLD, 16));
            timedRunCB.setEnabled(false);
            timedRunCB.setSelected(false);


//            gbc.gridy++;
//            add(new JLabel(), gbc);

            gbc.gridy++;
            gbc.ipady = 0;
            gbc.anchor = GridBagConstraints.SOUTH;
            add(timedRunCB, gbc);

            timedRunText = new JTextField("0.0");
            Dimension dimension = new Dimension(100, 40);
            timedRunText.setFont(new Font("Dialog", Font.BOLD, 16));
            timedRunText.setBackground(IbaColors.BT_GRAY);
            timedRunText.setHorizontalAlignment(SwingConstants.CENTER);
            timedRunText.setEnabled(false);
            timedRunText.setPreferredSize(dimension);
            title2 = new TitledBorder("sec");
            title2.setTitleJustification(TitledBorder.RIGHT);
            title2.setBorder(BorderFactory.createMatteBorder(1,1,1,1, Color.BLACK));
            timedRunText.setBorder(title2);


            gbc.gridy++;
            gbc.anchor = GridBagConstraints.CENTER;
            add(timedRunText, gbc);

            String[] actionStrings = { "RF", "Beamstop" };
            postRunAction = new JComboBox(actionStrings);
            postRunAction.setToolTipText("Defines the post-run action: \n Set RF to a non-beam producing voltage, or insert a beamstop to prevent dark current from entering the TR. ");

            ((JLabel)postRunAction.getRenderer()).setHorizontalAlignment(JLabel.CENTER);

            postRunAction.setEnabled(false);
            postRunAction.setFont(new Font("Dialog", Font.BOLD, 14));
            postRunAction.setBackground(IbaColors.BT_GRAY);
            dimension = new Dimension(100, 30);
            postRunAction.setPreferredSize(dimension);

            gbc.gridy++;
            gbc.anchor = GridBagConstraints.NORTH;
            add(postRunAction, gbc);

            timer = new Timer(10, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (startTime < 0) {
                        startTime = System.currentTimeMillis();
                    }
                    now = System.currentTimeMillis();
                    clockTime = now - startTime;


                    label.setText(dfu.formatDuration(clockTime, "mm:ss:SSS"));

                    if (Controller.S2E.getPosition() != Insertable.Position.RETRACTED){
                        timer.stop();
                    }

                    if (timedRunCB.isSelected()){
                        if (clockTime >= Double.parseDouble(timedRunText.getText())*1000){
                            if (mEsbtsPanel.mSelectBeamlinePanel.mContinuousBeamCB.isSelected()){
                                Controller.beam.bcreu.pauseRegulation();
                            }else{
                                Controller.beam.bcreu.setContinuousPulse(false);
                            }
                            if (postRunAction.getSelectedItem().toString().equalsIgnoreCase("RF")){
                                Controller.beam.llrf.setDeeVoltage2(41.00d);
                            }else{
                                try {
                                    Controller.beam.bssController.startDisableBeamActivity(Controller.beam.beamScheduler.getCurrentBeamAllocation().getBeamSupplyPointId());
                                    Thread.sleep(1000);
                                    if (Controller.S2E.getPosition() == Insertable.Position.MOVING || Controller.S2E.getPosition() == Insertable.Position.INSERTED) {
                                        //do nothing
                                    } else {
                                        Controller.ecubtcu.bsInsert("S2E");
                                    }
                                } catch (Exception f) {
                                    f.printStackTrace();
                                }
                            }
                            timer.stop();
                            clockTime = (long) Double.parseDouble(timedRunText.getText())*1000;
                            label.setText(dfu.formatDuration(clockTime, "mm:ss:SSS"));
                            startBeamButton.setText("Start Beam");
                            if (beamResults.isFile()) {
                                csvWriteBeamOff(beamResults, 2, TimerPanel.getClockTime());
                            } else {
                                log.error("[Beam Record] Could not find Beam Results file. Path: " + beamResults.getAbsolutePath());
                            }
                        }
                    }

                  //  mainPanel.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

                }
            });
            timer.setInitialDelay(0);

            startBeamButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (!timer.isRunning()) {
                        startTime = -1;
                        if (!Controller.isSystemManual()) {
                            if (mEsbtsPanel.mSelectBeamlinePanel.mContinuousBeamCB.isSelected()) {
                                Controller.beam.llrf.setMaxVoltage(56.00d);
                                try {
                                    if (mEsbtsPanel.mActionsPanel.deeVoltage==0) {
                                        mEsbtsPanel.mActionsPanel.deeVoltage = getRestVariable("B0.R1.LLA01.VDee1feedbackrv").getAsDouble();
                                        mEsbtsPanel.mActionsPanel.deeVoltage = Math.floor(mEsbtsPanel.mActionsPanel.deeVoltage);
                                    }
                                } catch (Exception h) {
                                    h.printStackTrace();
                                }
                                System.out.println(mEsbtsPanel.mActionsPanel.deeVoltage);
                                try {
                                    Controller.beam.bssController.startEnableBeamActivity(Controller.beam.beamScheduler.getCurrentBeamAllocation().getBeamSupplyPointId());
                                    Thread.sleep(500);
                                    if (Controller.S2E.getPosition() == Insertable.Position.MOVING || Controller.S2E.getPosition() == Insertable.Position.RETRACTED) {
                                        //do nothing
                                    } else {
                                        Controller.ecubtcu.bsRetract("S2E");
                                    }
                                } catch (Exception g) {
                                    g.printStackTrace();
                                }
//                if (deeVoltage > 41) {
//                    Controller.beam.llrf.setDeeVoltage2(deeVoltage);
//                }
                            }else {
                                try {
                                    Controller.beam.bssController.startEnableBeamActivity(Controller.beam.beamScheduler.getCurrentBeamAllocation().getBeamSupplyPointId());
                                    Thread.sleep(500);
                                    if (Controller.S2E.getPosition() == Insertable.Position.MOVING || Controller.S2E.getPosition() == Insertable.Position.RETRACTED) {
                                        //do nothing
                                    } else {
                                        Controller.ecubtcu.bsRetract("S2E");
                                    }
                                } catch (Exception f) {
                                    f.printStackTrace();
                                }
                            }
                            if (mEsbtsPanel.mActionsPanel.deeVoltage > 0) {
                                Controller.beam.llrf.setDeeVoltage2(mEsbtsPanel.mActionsPanel.deeVoltage);
                            }
                            if (mEsbtsPanel.mSelectBeamlinePanel.mContinuousBeamCB.isSelected()){
                                Controller.beam.bcreu.startRegulation(true);
                                Controller.beam.bcreu.startBeamPulses();
                            }else{
                                Controller.beam.bcreu.setContinuousPulse(true);
                            }
                            //Controller.beam.bcreu.startBeamPulses();
                            //Controller.beam.bcreu.setContinuousPulse(true);
                            timer.start();
                        } else {
                            log.error("System is in manual mode, please switch to automatic mode to start beam.");
                        }
                        startBeamButton.setText("Pause Beam");

                        if (beamResults.isFile()) {
                            csvWriteBeamOn(beamResults);
                        } else {
                            csvCreateBeamResults(beamResults);
                        }

                    } else if (timer.isRunning()) {
                        if (mEsbtsPanel.mSelectBeamlinePanel.mContinuousBeamCB.isSelected()){
                            Controller.beam.bcreu.pauseRegulation();
                        }else{
                            Controller.beam.bcreu.setContinuousPulse(false);
                        }

                        timer.stop();
                        startBeamButton.setText("Start Beam");

                        if (beamResults.isFile()) {
                            csvWriteBeamOff(beamResults, 1, TimerPanel.getClockTime());
                        } else {
                            log.error("[Beam Record] Could not find Beam Results file. Path: " + beamResults.getAbsolutePath());
                        }

                        if (postRunAction.getSelectedItem().toString().equalsIgnoreCase("RF")){
                            Controller.beam.llrf.setDeeVoltage2(41.00d);
                        }else{
                            try {
                                Controller.beam.bssController.startDisableBeamActivity(Controller.beam.beamScheduler.getCurrentBeamAllocation().getBeamSupplyPointId());
                                Thread.sleep(1000);
                                if (Controller.S2E.getPosition() == Insertable.Position.MOVING || Controller.S2E.getPosition() == Insertable.Position.INSERTED) {
                                    //do nothing
                                } else {
                                    Controller.ecubtcu.bsInsert("S2E");
                                }
                            } catch (Exception g) {
                                g.printStackTrace();
                            }
                        }

                    }
                    if (startTime < 0) {
                        startTime = System.currentTimeMillis();
                    }
                    long now = System.currentTimeMillis();
                    long clockTime = now - startTime;
                    label.setFont(new Font("Dialog", Font.BOLD, 48));
                    label.setText(dfu.formatDuration(clockTime, "mm:ss:SSS"));
                }
            });

            timedRunCB.addActionListener(new ActionListener() {
                 @Override
                 public void actionPerformed(ActionEvent e) {
                 timedRunText.setEnabled(timedRunCB.isSelected());
                }
            });



//            burnInLockButton.addActionListener(new ActionListener() {
//                @Override
//                public void actionPerformed(ActionEvent e) {
//                    if (locked) {
//                        locked = false;
//                        burnInStartButton.setEnabled(false);
//
//                        for (int i = 0; i <= 11; i++) {
//                            if (i == 0){
//                                duration[i].setEditable(true);
//                                duration[i].setBackground(Color.WHITE);
//
//                                title = new TitledBorderNoEdge("Step" + String.valueOf(i+1));
//                                title.setBorder(border);
//                                title.setTitleJustification(TitledBorder.RIGHT);
//                                title.setTitlePosition(TitledBorder.BELOW_TOP);
//                                title.setTitleFont(new Font("Dialog", Font.BOLD, 10));
//                                setpoint[i].setBorder(BorderFactory.createMatteBorder(2,2,1,1, Color.BLACK));
//                                duration[i].setBorder(title);
//                            }else if (i==11){
//                                setpoint[i].setEditable(true);
//                                setpoint[i].setBackground(Color.WHITE);
//                                duration[i].setEditable(true);
//                                duration[i].setBackground(Color.WHITE);
//
//                                title = new TitledBorderNoEdge("Step" + String.valueOf(i+1));
//                                title.setBorder(BorderFactory.createMatteBorder(2,1,2,2, Color.BLACK));
//                                title.setTitleJustification(TitledBorder.RIGHT);
//                                title.setTitlePosition(TitledBorder.BELOW_TOP);
//                                title.setTitleFont(new Font("Dialog", Font.BOLD, 10));
//                                setpoint[i].setBorder(BorderFactory.createMatteBorder(2,2,2,1, Color.BLACK));
//                                duration[i].setBorder(title);
//                            } else {
//                                setpoint[i].setEditable(true);
//                                setpoint[i].setBackground(Color.WHITE);
//                                duration[i].setEditable(true);
//                                duration[i].setBackground(Color.WHITE);
//
//                                title = new TitledBorderNoEdge("Step" + String.valueOf(i+1));
//                                title.setBorder(border);
//                                title.setTitleJustification(TitledBorder.RIGHT);
//                                title.setTitlePosition(TitledBorder.BELOW_TOP);
//                                title.setTitleFont(new Font("Dialog", Font.BOLD, 10));
//                                setpoint[i].setBorder(BorderFactory.createMatteBorder(2,2,1,1, Color.BLACK));
//                                duration[i].setBorder(title);
//                            }
//                        }
//
//                        burnInLockButton.setText("Lock");
//                    } else {
//                        locked = true;
//
//                        for (int i = 0; i <= 10; i++) {
//                            if (i==0){
//                                setpoint[i].setEditable(false);
//                                setpoint[i].setBackground(IbaColors.BT_GRAY);
//                                setpoint[i].setBorder(BorderFactory.createMatteBorder(2,2,1,1, Color.BLACK));
//                            }else {
//                                setpoint[i].setEditable(false);
//                                setpoint[i].setBackground(IbaColors.BT_GRAY);
//                                if (Float.valueOf(setpoint[i].getText()) > maxFilSP) {
//                                    controller.burnInStep[i] = maxFilSP;
//                                    setpoint[i].setText(String.valueOf(controller.burnInStep[i]));
//                                } else if (Float.valueOf(setpoint[i].getText()) < minFilSP) {
//                                    controller.burnInStep[i] = minFilSP;
//                                    setpoint[i].setText(String.valueOf(controller.burnInStep[i]));
//                                } else {
//                                    controller.burnInStep[i] =
//                                            Math.round(Float.valueOf(setpoint[i].getText()));
//                                    setpoint[i].setText(String.valueOf(controller.burnInStep[i]));
//                                }
//                            }
//                        }
//
//
//                        setpoint[11].setEditable(false);
//                        setpoint[11].setBackground(IbaColors.BT_GRAY);
//                        if (Float.valueOf(setpoint[11].getText()) > maxArcSP) {
//                            controller.burnInStep[11] = maxArcSP;
//                            setpoint[11].setText(String.valueOf(controller.burnInStep[11]));
//                        } else if (Float.valueOf(setpoint[11].getText()) < minArcSP) {
//                            controller.burnInStep[11] = minArcSP;
//                            setpoint[11].setText(String.valueOf(controller.burnInStep[11]));
//                        } else {
//                            controller.burnInStep[11] =
//                                    Math.round(Float.valueOf(setpoint[11].getText()));
//                            setpoint[11].setText(String.valueOf(controller.burnInStep[11]));
//                        }
//
//
//                        for (int i = 0; i <= 11; i++) {
//                            duration[i].setEditable(false);
//                            duration[i].setBackground(IbaColors.BT_GRAY);
//                            if (Float.valueOf(duration[i].getText()) > maxDuration) {
//                                step[i + 1] = step[i] + (maxDuration * oneMin);
//                                duration[i].setText(String.valueOf((step[i + 1] / oneMin) - (step[i] / oneMin)));
//                                if (i==11){
//                                    durationTuning.setText("ON");
//                                }
//                            } else if (Float.valueOf(duration[i].getText()) <= minDuration) {
//                                if (i != 0) {
//                                    setpoint[i].setBackground(Color.gray);
//                                }
//                                if (i==11){
//                                    durationTuning.setText("OFF");
//                                }
//                                step[i + 1] = step[i] + minDuration;
//                                duration[i].setText(String.valueOf((step[i + 1] / oneMin) - (step[i] / oneMin)));
//                                duration[i].setBackground(Color.gray);
//                            } else {
//                                step[i + 1] = step[i] +
//                                        Math.round(Float.valueOf(duration[i].getText())) * oneMin;
//                                duration[i].setText(String.valueOf((step[i + 1] / oneMin) - (step[i] / oneMin)));
//                                if (i==11){
//                                    durationTuning.setText("ON");
//                                }
//                            }
//
//                            totalDuration = step[12];
//                            dfu = new DurationFormatUtils();
//                            label.setText(dfu.formatDuration(totalDuration, "mm:ss:SSS"));
//                            if (totalDuration >= oneMin * 100) {
//                                label.setText(dfu.formatDuration(totalDuration, "mmm:ss:SSS"));
//                            }
//                            burnInLockButton.setText("Unlock");
//                        }
//                    }
//                }
//            });
        }
        @Override
        public Dimension getPreferredSize() {
            return new Dimension(400, 400);
        }

        public static long getClockTime(){
            return clockTime;
        }

        public static Timer getTimer(){
            return timer;
        }
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

            newData2[0] = beamCurrent.getFirst();


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

                                axis2.setLowerBound(min - (range/2));
                                axis2.setUpperBound(max + (range/2));
                                NumberTickUnit rangeTick = new NumberTickUnit(((max+5)-(min-5))/4);
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

                                numberAxis.setLowerBound(min2 - (range2/2));
                                numberAxis.setUpperBound(max2 + (range2/2));
                                rangeTick = new NumberTickUnit((double)tick);
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
}
