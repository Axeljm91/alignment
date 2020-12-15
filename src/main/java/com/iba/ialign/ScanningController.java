package com.iba.ialign;

import com.csvreader.CsvReader;
import com.csvreader.CsvWriter;
import com.iba.blak.BlakConstants;
import com.iba.blak.common.PopupDisplayer;
import com.iba.blak.common.Utils;
import com.iba.icomp.core.checks.Check;
import com.iba.pts.bms.bds.Credits;
import com.iba.pts.bms.bds.SCClientFrame;
import com.iba.pts.bms.bds.UserPrefs;
import com.iba.pts.bms.bds.dosegrid.DoseGridPlayer;
import com.iba.pts.bms.bds.dosegrid.ICChart;
import com.iba.pts.bms.bds.dosegrid.VersionResult;
import com.iba.pts.bms.common.settings.BdsLayerSettings;
import com.iba.pts.bms.common.settings.impl.BmsLayerSettings;
import com.iba.pts.bms.common.settings.impl.DefaultIrradiationSettings;
import com.iba.pts.bms.common.settings.impl.pbs.*;
import com.iba.pts.bms.datatypes.impl.pbs.PbsEquipmentMap;
import com.iba.pts.bms.datatypes.impl.pbs.PbsSlew;
import com.iba.pts.bms.datatypes.impl.pbs.PbsEquipmentElement;
import com.iba.pts.bms.datatypes.impl.pbs.PbsSlewConstants;
import com.iba.pts.pbs.LayerPlayer;
import com.iba.tcs.beam.bds.devices.rpc.scanningcontroller.v1_35.*;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.thoughtworks.xstream.XStream;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.nio.file.FileAlreadyExistsException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.stream.Stream;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

import org.acplt.oncrpc.OncRpcException;
import org.acplt.oncrpc.XdrBufferDecodingStream;
import org.acplt.oncrpc.XdrBufferEncodingStream;
import org.acplt.oncrpc.apps.jportmap.OncRpcEmbeddedPortmap;
import org.apache.commons.jocl.JOCLContentHandler;
import org.apache.log4j.Logger;
import org.jfree.chart.ChartPanel;
import org.supercsv.io.CsvBeanReader;
import org.supercsv.prefs.CsvPreference;

public class ScanningController extends JPanel implements PreferenceChangeListener {

    private static org.apache.log4j.Logger log= Logger.getLogger(new Throwable().getStackTrace()[0].getClassName());

    private static final int CHANNEL_NUMBER = 32;
    private ScanningControllerClient mClient;
    private boolean mPlayingAll = false;
    private static JComboBox mAddressTF;
    private static JTextField mFileTF;
    private static JTextField mLayerIdTF;
    private static JButton connect;
    private static JButton getVersion;
    private static JButton selectFile;
    private static JButton setBeamline;
    private static JButton sendByXstream;
    private static JButton start;
    private static JButton cancel;
    private static JButton clearOperational;
    private static JButton clearAbnormal;
    private static JButton clear;
    private static final JTextField mBeamlineId = new JTextField("1");
    private boolean mRefresh = true;
    private final Thread mRefreshThread = new com.iba.ialign.ScanningController.RefreshState();
    private final JTextField mFailureCause = new JTextField("-");
    private static final JCheckBox mRefreshFailure = new JCheckBox("Refresh failure");
    private static final JTextField mKFactor = new JTextField("1.0");
    private final JLabel mLayerId = new JLabel("-1");
    private final JLabel mElementId = new JLabel("-1");
    private final DoseGridPlayer mDoseGridPlayer;
    private final DoseGridPlayer mDoseGridPlayer2;
    private final JLabel mDoseId = new JLabel("-1");
    private final JLabel mXId = new JLabel("-1");
    private final JLabel mYId = new JLabel("-1");
    private int mDose;
    private float mX;
    private float mY;
    private ICChart mChartIC2;
    private ChartPanel mChannelsPanelIC2;
    private ICChart mChartIC3;
    private ChartPanel mChannelsPanelIC3;
    private final JCheckBox[] mScanningStates = new JCheckBox[6];
    private static final DefaultListModel mRecorderEvents = new DefaultListModel();
    private static final DefaultListModel mFailureEvents = new DefaultListModel();
    private DefaultComboBoxModel mIpAddressModel;
    private File mCurrentDir = new File(".");

    public ScanningController() {
        UserPrefs.loadUserPrefs(this);
        //UIManager.put("CheckBox.foreground", UIManager.get("Checkbox.enabledText"));
        UIManager.put("JCheckBox.disabledText", UIManager.get("JCheckBox.foreground"));
        UIManager.put("CheckBox.disabledForeground", Color.BLACK);
//        this.setDefaultCloseOperation(2);
//        this.setTitle("Scanning controller client 0.1");
//        this.addWindowListener(new WindowAdapter() {
//            public void windowClosing(WindowEvent pWE) {
//                ScanningController.this.mRefresh = false;
//
//                try {
//                    ScanningController.this.mRefreshThread.join();
//                } catch (InterruptedException var3) {
//                    ScanningController.this.mRecorderEvents.addElement("Exception");
//                }
//
//                System.exit(0);
//            }
//        });

        this.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.weighty = 1;
        c.weightx = 1;
//        c.gridx = 0;
//        c.gridy = 0;
//        c.gridheight = 2;
        //c.insets = new Insets(50,50,50,50);
        //c.ipady = 50;
        //c.ipadx = 0;
        //c.weightx = 10;
        //c.weighty = 10;

        Font bigFont = new Font(Font.DIALOG, Font.BOLD, 16);
        Font bigTitle = new Font(Font.DIALOG, Font.BOLD, 14);

        JTabbedPane pane = new JTabbedPane();
        JPanel p1 = this.createElements();
        p1.setLayout(new GridBagLayout());
        Border border = BorderFactory.createLineBorder(Color.lightGray);
        TitledBorder title = new TitledBorder("Scanning Controller");
        title.setTitleFont(bigTitle);
        title.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.lightGray));
        p1.setBorder(title);
        JPanel mainPlayer = new JPanel(new GridBagLayout());
        this.mDoseGridPlayer = new DoseGridPlayer(true);
        this.mDoseGridPlayer2 = new DoseGridPlayer(false);
        mainPlayer.add(this.mDoseGridPlayer);
        mainPlayer.add(this.createPlayAllPanel());
        mainPlayer.add(this.mDoseGridPlayer2);
        pane.addTab("Client", p1);
        //pane.addTab("Dose grid", mainPlayer);
        //pane.addTab("Layer painter", new LayerPlayer());
        //pane.addTab("", new Credits());
        this.add(p1, c);
        this.updateUI();
        //this.getContentPane().add(pane);
        //this.pack();
        this.setVisible(true);
    }

    private JPanel createPlayAllPanel() {
        FormLayout form = new FormLayout("pref");
        DefaultFormBuilder builder = new DefaultFormBuilder(form);
        JButton playAll = new JButton("Play all");
        playAll.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent pE) {
                ScanningController.this.mPlayingAll = true;
                (ScanningController.this.new PlayTwoLayers()).start();
            }
        });
        JButton stopAll = new JButton("Stop all");
        stopAll.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent pE) {
                ScanningController.this.mPlayingAll = false;
            }
        });
        builder.append(playAll);
        builder.append(stopAll);
        return builder.getPanel();
    }

    public static void test(){

    }

    public void updatePanel(boolean serviceBeamMode){
        //mAddressTF.setEnabled(serviceBeamMode);
        connect.setEnabled(serviceBeamMode);
        mAddressTF.setEnabled(serviceBeamMode);
        if (serviceBeamMode == false) {
            mFileTF.setEnabled(serviceBeamMode);
            mLayerIdTF.setEnabled(serviceBeamMode);
            mBeamlineId.setEnabled(serviceBeamMode);
            mRefreshFailure.setEnabled(serviceBeamMode);
            mKFactor.setEnabled(serviceBeamMode);
            getVersion.setEnabled(serviceBeamMode);
            selectFile.setEnabled(serviceBeamMode);
            setBeamline.setEnabled(serviceBeamMode);
            sendByXstream.setEnabled(serviceBeamMode);
            start.setEnabled(serviceBeamMode);
            cancel.setEnabled(serviceBeamMode);
            clearOperational.setEnabled(serviceBeamMode);
            clearAbnormal.setEnabled(serviceBeamMode);
            clear.setEnabled(serviceBeamMode);
            mFailureCause.setEnabled(serviceBeamMode);
        }
    }

    public JPanel createElements() {

        Font bigFont = new Font(Font.DIALOG, Font.BOLD, 16);
        Font bigTitle = new Font(Font.DIALOG, Font.BOLD, 14);

        FormLayout form = new FormLayout("pref:none, 3dlu, fill:pref:grow, 6dlu");
        DefaultFormBuilder builder = new DefaultFormBuilder(form);
        connect = new JButton("Connect to SC");
        connect.setFont(bigFont);
        connect.addActionListener(new ScanningController.Connect());
        getVersion = new JButton("Get version");
        getVersion.setFont(bigFont);
        getVersion.addActionListener(new ScanningController.GetVersion());
        JButton shutdown = new JButton("Shutdown");
        shutdown.setFont(bigFont);
        shutdown.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent pArg0) {
                try {
                    ScanningControllerRpcStatus status = ScanningController.this.mClient.scanningControllerShutdown_1();
                    if (status.success == 78) {
                        ScanningController.this.mRecorderEvents.addElement("Shutdown failed: " + new String(status.message));
                    }
                } catch (OncRpcException var3) {
                    ScanningController.this.mRecorderEvents.addElement("Shutdown exception");
                } catch (IOException var4) {
                    ScanningController.this.mRecorderEvents.addElement("Shutdown exception");
                }

            }
        });
        //JButton sendByFile = new JButton("Prepare layer [path for the SC]");
        //sendByFile.addActionListener(new ScanningController.SendLayerByFile());
        JButton sendByStruct = new JButton("Prepare layer");
        sendByStruct.addActionListener(new ScanningController.SendLayerByStruct());
        sendByXstream = new JButton("Prepare layer [xdr, xml, csv]");
        sendByXstream.addActionListener(new ScanningController.SendLayerByXstream());
        sendByXstream.setFont(bigFont);
        start = new JButton("Start layer");
        start.setFont(bigFont);
        start.addActionListener(new ScanningController.StartLayer());
        cancel = new JButton("Cancel layer");
        cancel.setFont(bigFont);
        cancel.addActionListener(new ScanningController.CancelLayer());
        clearOperational = new JButton("Clear operational status");
        clearOperational.setFont(bigFont);
        clearOperational.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent pE) {
                ScanningControllerRpcDefault_in startIn = new ScanningControllerRpcDefault_in();
                startIn.caller = "TCU Test".getBytes();

                try {
                    ScanningControllerRpcStatus status = ScanningController.this.mClient.scanningControllerClearOperationalState_1(startIn);
                    if (status.success == 78) {
                        ScanningController.this.mRecorderEvents.addElement("Clear request failed: " + new String(status.message));
                        return;
                    }
                } catch (OncRpcException var5) {
                    ScanningController.this.mRecorderEvents.addElement("Exception");
                } catch (IOException var6) {
                    ScanningController.this.mRecorderEvents.addElement("Exception");
                }

            }
        });
        clearAbnormal = new JButton("Clear abnormal beam");
        clearAbnormal.setFont(bigFont);
        clearAbnormal.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent pE) {
                ScanningControllerRpcDefault_in startIn = new ScanningControllerRpcDefault_in();
                startIn.caller = "TCU Test".getBytes();

                try {
                    ScanningControllerRpcStatus status = ScanningController.this.mClient.scanningControllerClearAbnormalBeamDetectedState_1(startIn);
                    if (status.success == 78) {
                        ScanningController.this.mRecorderEvents.addElement("Clear request failed: " + new String(status.message));
                        return;
                    }
                } catch (OncRpcException var5) {
                    ScanningController.this.mRecorderEvents.addElement("Exception");
                } catch (IOException var6) {
                    ScanningController.this.mRecorderEvents.addElement("Exception");
                }

            }
        });
        selectFile = new JButton("Chose File");
        selectFile.setFont(bigFont);
        selectFile.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent pArg0) {
                JFileChooser chooser = new JFileChooser();
                chooser.setCurrentDirectory(ScanningController.this.mCurrentDir);
                int res = chooser.showOpenDialog((Component)null);
                if (res == 0) {
                    ScanningController.this.mCurrentDir = chooser.getSelectedFile().getParentFile();
                    ScanningController.this.mFileTF.setText(chooser.getSelectedFile().getAbsolutePath());
                }

            }
        });
        setBeamline = new JButton("Set beamline");
        setBeamline.setFont(bigFont);
        setBeamline.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent pE) {
                try {
                    ScanningControllerRpcStatus status = ScanningController.this.mClient.scanningControllerSelectBeamline_1(Integer.parseInt(ScanningController.this.mBeamlineId.getText()));
                    if (status.success == 78) {
                        ScanningController.this.mRecorderEvents.addElement("Select beamline failed: " + new String(status.message));
                        return;
                    }
                } catch (NumberFormatException var4) {
                    ScanningController.this.mRecorderEvents.addElement("Exception");
                } catch (OncRpcException var5) {
                    ScanningController.this.mRecorderEvents.addElement("Exception");
                } catch (IOException var6) {
                    ScanningController.this.mRecorderEvents.addElement("Exception");
                }

            }
        });
        this.mIpAddressModel = new DefaultComboBoxModel();

        int i;
        for(i = 0; i < UserPrefs.getNumberOfIpAddresses(); ++i) {
            this.mIpAddressModel.addElement(UserPrefs.getIpAddress(i));
        }

        this.mAddressTF = new JComboBox(this.mIpAddressModel);
        this.mAddressTF.setFont(bigFont);
        this.mAddressTF.setEditable(true);
        this.mFileTF = new JTextField("default.csv");
        this.mLayerIdTF = new JTextField("1000");
        JLabel IPLabel = new JLabel("SC IP address :", 11);
        IPLabel.setFont(bigFont);
        builder.append(IPLabel);
        builder.append(this.mAddressTF);
        builder.nextLine();
        builder.append(connect, 3);
        builder.append(getVersion, 3);
        //builder.append(shutdown, 3);
        builder.appendSeparator();
        JLabel FileLabel = new JLabel("Layer filename :", 11);
        FileLabel.setFont(bigFont);
        builder.append(FileLabel);
        builder.nextLine();
        builder.append(this.mFileTF, 3);
        this.mFileTF.setFont(bigFont);
        builder.nextLine();
        builder.append(selectFile, 3);
        builder.nextLine();
        //builder.append(new JLabel("Layer id :", 11));
        //builder.append(this.mLayerIdTF);
        //builder.nextLine();
        JLabel BeamLineIDLabel = new JLabel("Beamline id :", 11);
        BeamLineIDLabel.setFont(bigFont);
        builder.append(BeamLineIDLabel);
        builder.append(this.mBeamlineId);
        this.mBeamlineId.setFont(bigFont);
        builder.nextLine();
        JLabel KFactorLabel = new JLabel("K factor :", 11);
        KFactorLabel.setFont(bigFont);
        builder.append(KFactorLabel);
        this.mKFactor.setFont(bigFont);
        builder.append(this.mKFactor);
        builder.nextLine();
        UIManager.put("CheckBox.disabledForeground", Color.BLACK);
        JCheckBox IdleCB = new JCheckBox("<html><font color=black>Idle</font></html>");
        IdleCB.setForeground(Color.BLACK);
        IdleCB.setFont(bigTitle);
        JCheckBox PreparingCB = new JCheckBox("Preparing");
        PreparingCB.setFont(bigTitle);
        JCheckBox PreparedCB = new JCheckBox("Prepared");
        PreparedCB.setFont(bigTitle);
        JCheckBox PerformingCB = new JCheckBox("Performing");
        PerformingCB.setFont(bigTitle);
        JCheckBox FinalizingCB = new JCheckBox("Finalizing");
        FinalizingCB.setFont(bigTitle);
        JCheckBox InitializingCB = new JCheckBox("Initializing");
        InitializingCB.setFont(bigTitle);
        this.mScanningStates[0] = IdleCB;
        this.mScanningStates[1] = PreparingCB;
        this.mScanningStates[2] = PreparedCB;
        this.mScanningStates[3] = PerformingCB;
        this.mScanningStates[4] = FinalizingCB;
        this.mScanningStates[5] = InitializingCB;

        for(i = 0; i < this.mScanningStates.length; ++i) {
            this.mScanningStates[i].setEnabled(false);
            this.mScanningStates[i].setForeground(Color.BLACK);
            builder.append(this.mScanningStates[i], 3);
            builder.nextLine();
        }

        builder.append(setBeamline, 3);
        //builder.append(sendByFile, 3);
        builder.append(sendByXstream, 3);
        builder.append(start, 3);
        builder.append(cancel, 3);
        builder.append(clearOperational, 3);
        builder.append(clearAbnormal, 3);
//        builder.append(new JLabel("Failure cause :", 11));
//        builder.append(this.mFailureCause);
//        builder.nextLine();
//        builder.append(this.mRefreshFailure, 3);
//        this.mRefreshFailure.setSelected(true);
//        builder.appendSeparator();
//        builder.append(new JLabel("Layer id :", 11));
//        builder.append(this.mLayerId);
//        builder.nextLine();
//        builder.append(new JLabel("Element id :", 11));
//        builder.append(this.mElementId);
//        builder.nextLine();
//        builder.append(new JLabel("Dose :", 11));
//        builder.append(this.mDoseId);
//        builder.nextLine();
//        builder.append(new JLabel("X :", 11));
//        builder.append(this.mXId);
//        builder.nextLine();
//        builder.append(new JLabel("Y :", 11));
//        builder.append(this.mYId);
        builder.nextLine();
        //this.createCharts();
        //this.mChannelsPanelIC2 = new ChartPanel(this.mChartIC2.getChart());
        //this.mChannelsPanelIC3 = new ChartPanel(this.mChartIC3.getChart());
        clear = new JButton("Clear");
        clear.setFont(bigFont);
        clear.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent pE) {
                ScanningController.this.mRecorderEvents.clear();
                float[] ch = new float[32];

                for(int i = 0; i < 32; ++i) {
                    ch[i] = 0.0F;
                }

                ScanningController.this.setChannels(ch, true);
                ScanningController.this.setChannels(ch, false);
            }
        });
        FormLayout rightform = new FormLayout("pref:none, 3dlu, fill:pref:grow", "fill:pref:grow");
        DefaultFormBuilder rightbuilder = new DefaultFormBuilder(rightform);
        JLabel EventsLabel = new JLabel("Events :");
        EventsLabel.setFont(bigTitle);
        rightbuilder.append(EventsLabel);
        //rightbuilder.append(clear);
        rightbuilder.nextLine();
        rightbuilder.append(new JScrollPane(new JList(this.mRecorderEvents)), 3);
        rightbuilder.nextLine();
        rightbuilder.append(clear, 3);
        rightbuilder.nextLine();
        rightbuilder.appendSeparator();
        JLabel FailureLabel = new JLabel("Failure cause :", 11);
        FailureLabel.setFont(bigTitle);
        rightbuilder.append(FailureLabel);
        rightbuilder.nextLine();
        //rightbuilder.setRowSpan(4);
        Dimension dimension = new Dimension(200,150);
        this.mFailureCause.setPreferredSize(dimension);
//        rightbuilder.append(this.mFailureCause, 3);
        rightbuilder.append(new JScrollPane(new JList(this.mFailureEvents)), 3);
        //rightbuilder.setRowSpan(1);
        rightbuilder.nextLine();
        this.mRefreshFailure.setFont(bigTitle);
        rightbuilder.append(this.mRefreshFailure, 3);
        this.mRefreshFailure.setSelected(true);
        JLabel LayerIDLabel = new JLabel("Layer id :", 11);
        LayerIDLabel.setFont(bigTitle);
        rightbuilder.appendSeparator();
        rightbuilder.append(LayerIDLabel);
        rightbuilder.append(this.mLayerId);
        rightbuilder.nextLine();
        JLabel ElementLabel = new JLabel("Element id :", 11);
        ElementLabel.setFont(bigTitle);
        rightbuilder.append(ElementLabel);
        rightbuilder.append(this.mElementId);
        rightbuilder.nextLine();
        JLabel DoseLabel = new JLabel("Dose :", 11);
        DoseLabel.setFont(bigTitle);
        rightbuilder.append(DoseLabel);
        rightbuilder.append(this.mDoseId);
        rightbuilder.nextLine();
        JLabel XLabel = new JLabel("X :", 11);
        XLabel.setFont(bigTitle);
        rightbuilder.append(XLabel);
        rightbuilder.append(this.mXId);
        rightbuilder.nextLine();
        JLabel YLabel = new JLabel("Y :", 11);
        YLabel.setFont(bigTitle);
        rightbuilder.append(YLabel);
        rightbuilder.append(this.mYId);
        rightbuilder.nextLine();

        JPanel all = builder.getPanel();
        JPanel grid = rightbuilder.getPanel();
        FormLayout chart2form = new FormLayout("200dlu");
        DefaultFormBuilder chart2builder = new DefaultFormBuilder(chart2form);
        //chart2builder.append(this.mChannelsPanelIC2);
        FormLayout chart3form = new FormLayout("200dlu");
        DefaultFormBuilder chart3builder = new DefaultFormBuilder(chart3form);
        //chart3builder.append(this.mChannelsPanelIC3);
        JPanel main = new JPanel();
        main.setLayout(new BorderLayout());
        main.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = 1;
        c.weighty = 1;
        c.weightx = 1;
        main.add(all, c);
        //main.add(chart2builder.getPanel());
        //main.add(chart3builder.getPanel(), "East");
        main.add(grid, c);
        return main;
    }

    public void addEvent(final String pEvent) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                ScanningController.this.mRecorderEvents.add(0, pEvent);
            }
        });
    }

    public void setLayerId(final int pLayerId) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                ScanningController.this.mLayerId.setText(Integer.toString(pLayerId));
            }
        });
    }

    public void setElementId(final int pEI) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                ScanningController.this.mElementId.setText(Integer.toString(pEI));
            }
        });
    }

    public final void setChannels(float[] pChannels, boolean pIsIC2) {
        ICChart currentChart = pIsIC2 ? this.mChartIC2 : this.mChartIC3;
        currentChart.setChannels(pChannels);
    }

    public void setCurrentDose(final int pEI, boolean pIsIC2) {
        this.mDose = pEI;
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                ScanningController.this.mDoseId.setText(Integer.toString(pEI));
            }
        });
    }

    public void setCurrentX(final float pEI) {
        this.mX = pEI;
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                ScanningController.this.mXId.setText(Float.toString(pEI));
            }
        });
    }

    public void setCurrentY(final float pEI) {
        this.mY = pEI;
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                ScanningController.this.mYId.setText(Float.toString(pEI));
                ScanningController.this.mDoseGridPlayer.addSpotAndDisplayIt((int)ScanningController.this.mX, (int)ScanningController.this.mY, ScanningController.this.mDose);
            }
        });
    }

    private int getSCState() {
        ScanningControllerRpcGetState_out result;
        try {
            result = this.mClient.scanningControllerRpcGetState_1();
        } catch (Exception var3) {
            this.mRecorderEvents.addElement("GetState failed: " + var3.getMessage());
            return -1;
        }

        if (result.rpcStatus.success == 78) {
            this.mRecorderEvents.addElement("GetState failed: " + new String(result.rpcStatus.message));
            return -1;
        } else {
            return result.functionalState;
        }
    }

    private String getFailureMessage() {
        ScanningControllerRpcGetFailureCause_out result;
        try {
            result = this.mClient.scanningControllerRpcGetFailureCause_1();
        } catch (Exception var3) {
            this.mRecorderEvents.addElement("getFailureMessage failed: " + var3.getMessage());
            return "RPC Problem";
        }

        if (result.rpcStatus.success == 78) {
            this.mRecorderEvents.addElement("getFailureMessage failed: " + new String(result.rpcStatus.message));
            return "RPC Problem";
        } else {
            return new String(result.failureCause);
        }
    }

    private void createCharts() {
        this.mChartIC2 = new ICChart(true);
        this.mChartIC3 = new ICChart(false);
    }

    public void preferenceChange(PreferenceChangeEvent pEvt) {
        this.mIpAddressModel.removeAllElements();

        for(int chb = 0; chb < UserPrefs.getNumberOfIpAddresses(); ++chb) {
            this.mIpAddressModel.addElement(UserPrefs.getIpAddress(chb));
        }

    }

    private ScanningControllerPbsLayer loadFromXdrFile(String pFilename) {
        File f = new File(pFilename);
        if (f.exists()) {
            try {
                FileInputStream file = new FileInputStream(pFilename);
                VersionResult vres = VersionResult.getVersionInfo(file, "1.25");
                if (vres.mIsValid) {
                    System.out.println("Converting version " + vres.mVersion);
                    int lg = (int)f.length() - vres.mVersionLength;
                    byte[] data = new byte[lg];
                    file.read(data);
                    file.close();
                    XdrBufferDecodingStream buffer = new XdrBufferDecodingStream(data);
                    buffer.beginDecoding();
                    ScanningControllerPbsLayer layer = new ScanningControllerPbsLayer();
                    layer.xdrDecode(buffer);
                    buffer.endDecoding();
                    buffer.close();
                    return layer;
                } else {
                    return null;
                }
            } catch (Exception var9) {
                return null;
            }
        } else {
            return null;
        }
    }

   /* private PbsBdsLayerSettings loadFromCsvFile(String filename) {
        File f = new File(filename);
        DefaultPbsBdsLayerSettings result = null;

        if (!f.exists()) {
            log.error("Could not find xml file");
            return null;
        } else {
            // mBdsLayerSettings;
            try {
                InputStreamReader fr = new InputStreamReader(new FileInputStream(f), BlakConstants.DEFAULT_CHARSET.name());

                CsvReader csvr = new CsvReader(fr, ',');

                int elemNbr = 0;

                List<String> header = new ArrayList<String>();

                // first line
//                while (csvr.readRecord()) {
//
//                    String elementName = csvr.get(0);
//                    // Skip comment lines
//                    if (elementName.isEmpty()) {
//                        continue;
//                    }
//
//                    if (elementName.equals("#LAYER_ID")) {
//                        header = Arrays.asList(csvr.getValues());
//                        continue;
//                    }
//
////                pLayer.getLayerIndex(), pLayer.getRangeAtNozzleEntrance(),
////                map.getTotalCharge(), elemNbr, diagn,
////                pLayer.getMetersetCorrectionFactor()
//               result = new DefaultPbsBdsLayerSettings(
//                     Integer.parseInt(csvr.get(header.indexOf("#LAYER_ID"))), true,"Spot1",
//                     Double.parseDouble(csvr.get(header.indexOf("RANGE"))),// range
////                      in
////                      patient
//                     Double.parseDouble(csvr.get(header.indexOf("RANGE"))),// range
////                      at
////                      nozzle
////                      entrance
//                     Double.parseDouble(csvr.get(header.indexOf("TOTAL_CHARGE"))), 100.0,// totalcharge
//                     1); // scatterer position [1..4]
//                    elemNbr = Integer.parseInt(csvr.get(5));
//                    break;
//                }

                while(csvr.readRecord()) {
                    String elementName = csvr.get(0);
                    if (!elementName.isEmpty()) {
                        if (!elementName.equals("#LAYER_ID")) {
                            result = new DefaultPbsBdsLayerSettings(
                                    Integer.parseInt(csvr.get(((List)header).indexOf("#LAYER_ID"))),
                                    false, "4.0", 230.0D,
                                    Double.parseDouble(csvr.get(((List)header).indexOf("RANGE"))),
                                    Double.parseDouble(csvr.get(((List)header).indexOf("RANGE"))),
                                    Double.parseDouble(csvr.get(((List)header).indexOf("TOTAL_CHARGE"))),
                                    1);

                            elemNbr = Integer.parseInt(csvr.get(5));
                            break;
                        }

                        header = Arrays.asList(csvr.getValues());
                    }
                }

                try {
                    PbsEquipmentMap map = new PbsEquipmentMap();

                    List<PbsMapElement> elements = new LinkedList<PbsMapElement>();
                    List<String> dataHeader = new ArrayList<String>();
                    for (int i = 0; i < elemNbr; ) {
                        csvr.readRecord();

                        String elementName = csvr.get(0);
                        // Skip comment lines
                        if (elementName.isEmpty()) {
                            continue;
                        }

                        if (elementName.equals("#ELEMENT_ID")) {
                            dataHeader = Arrays.asList(csvr.getValues());
                            continue;
                        }

                        if (csvr.getValues().length != 79) {
                            throw new Exception("The PbsBdsLayerSetting file format not supported!");
                        }

                        PbsEquipmentElement e;

                        switch (PbsElementType.values()[Integer.parseInt(
                                csvr.get(dataHeader.indexOf("ELEMENT_TYPE")))]) {
                            case SPOT:
                                e = new PbsSpot();
                                break;
                            case SLEW:
                                e = new PbsSlew();
                                break;
                            default:// spot
                                e = new PbsSpot();
                                break;
                        }

                        if (e instanceof PbsSpot) {
                            ((PbsSpot) e).mTargetCharge = Float.parseFloat(
                                    csvr.get(dataHeader.indexOf("TARGET_CHARGE")));
                            ((PbsSpot) e).mBeamCurrentSetpoint = Float.parseFloat(
                                    csvr.get(dataHeader.indexOf("BEAM_CURRENT_SP")));
                            // two INACTIVE
                            ((PbsSpot) e).mIrradiationTime = Float.parseFloat(
                                    csvr.get(dataHeader.indexOf("MAX_DURATION")));
                            ((PbsSpot) e).mChargeMinPrimary = Float.parseFloat(
                                    csvr.get(dataHeader.indexOf("MIN_CHARGE_PRIM")));
                            ((PbsSpot) e).mChargeMaxPrimary = Float.parseFloat(
                                    csvr.get(dataHeader.indexOf("MAX_CHARGE_PRIM")));
                            ((PbsSpot) e).mChargeMinSecondary = Float.parseFloat(
                                    csvr.get(dataHeader.indexOf("MIN_CHARGE_SEC")));
                            ((PbsSpot) e).mChargeMaxSecondary = Float.parseFloat(
                                    csvr.get(dataHeader.indexOf("MAX_CHARGE_SEC")));
                            ((PbsSpot) e).mChargeMinTernary = Float.parseFloat(
                                    csvr.get(dataHeader.indexOf("MIN_CHARGE_TER")));
                            ((PbsSpot) e).mChargeMaxTernary = Float.parseFloat(
                                    csvr.get(dataHeader.indexOf("MAX_CHARGE_TER")));
                            // two INACTIVE MIN_IC1_CHARGE MAX_IC1_CHARGE

                            ((PbsSpot) e).mChargeRateMinPrimary = Float.parseFloat(
                                    csvr.get(dataHeader.indexOf("MIN_DOSE_RATE_PRIM")));
                            ((PbsSpot) e).mChargeRateMaxPrimary = Float.parseFloat(
                                    csvr.get(dataHeader.indexOf("MAX_DOSE_RATE_PRIM")));
                            ((PbsSpot) e).mChargeRateMinSecondary = Float.parseFloat(
                                    csvr.get(dataHeader.indexOf("MIN_DOSE_RATE_SEC")));
                            ((PbsSpot) e).mChargeRateMaxSecondary = Float.parseFloat(
                                    csvr.get(dataHeader.indexOf("MAX_DOSE_RATE_SEC")));
                            ((PbsSpot) e).mChargeRateMinTernary = Float.parseFloat(
                                    csvr.get(dataHeader.indexOf("MIN_DOSE_RATE_TER")));
                            ((PbsSpot) e).mChargeRateMaxTernary = Float.parseFloat(
                                    csvr.get(dataHeader.indexOf("MAX_DOSE_RATE_TER")));
                            // two INACTIVE MIN_DOSE_RATE_IC1 MAX_DOSE_RATE_IC1
                            ((PbsSpot) e).mICCycloMin = Float.parseFloat(
                                    csvr.get(dataHeader.indexOf("MIN_BEAM_CURRENT_FB")));
                            ((PbsSpot) e).mICCycloMax = Float.parseFloat(
                                    csvr.get(dataHeader.indexOf("MAX_BEAM_CURRENT_FB")));
                        }
                        e.mCurrentSetpointX = Float.parseFloat(csvr.get(dataHeader.indexOf("X_CURRENT_TARGET_SP")));
                        e.mCurrentSetpointY = Float.parseFloat(csvr.get(dataHeader.indexOf("Y_CURRENT_TARGET_SP")));

                        e.mCurrentFeedbackMinXPrimary = Float.parseFloat(
                                csvr.get(dataHeader.indexOf("X_CURRENT_MIN_PRIM_FB")));
                        e.mCurrentFeedbackMaxXPrimary = Float.parseFloat(
                                csvr.get(dataHeader.indexOf("X_CURRENT_MAX_PRIM_FB")));
                        e.mCurrentFeedbackMinYPrimary = Float.parseFloat(
                                csvr.get(dataHeader.indexOf("Y_CURRENT_MIN_PRIM_FB")));
                        e.mCurrentFeedbackMaxYPrimary = Float.parseFloat(
                                csvr.get(dataHeader.indexOf("Y_CURRENT_MAX_PRIM_FB")));
                        e.mVoltageFeedbackMinXPrimary = Float.parseFloat(
                                csvr.get(dataHeader.indexOf("X_VOLT_MIN_PRIM_FB")));
                        e.mVoltageFeedbackMaxXPrimary = Float.parseFloat(
                                csvr.get(dataHeader.indexOf("X_VOLT_MAX_PRIM_FB")));
                        e.mVoltageFeedbackMinYPrimary = Float.parseFloat(
                                csvr.get(dataHeader.indexOf("Y_VOLT_MIN_PRIM_FB")));
                        e.mVoltageFeedbackMaxYPrimary = Float.parseFloat(
                                csvr.get(dataHeader.indexOf("Y_VOLT_MIN_PRIM_FB")));
                        e.mCurrentFeedbackMinXRedundant = Float.parseFloat(
                                csvr.get(dataHeader.indexOf("X_CURRENT_MIN_SEC_FB")));
                        e.mCurrentFeedbackMaxXRedundant = Float.parseFloat(
                                csvr.get(dataHeader.indexOf("X_CURRENT_MAX_SEC_FB")));
                        e.mCurrentFeedbackMinYRedundant = Float.parseFloat(
                                csvr.get(dataHeader.indexOf("Y_CURRENT_MIN_SEC_FB")));
                        e.mCurrentFeedbackMaxYRedundant = Float.parseFloat(
                                csvr.get(dataHeader.indexOf("Y_CURRENT_MAX_SEC_FB")));
                        e.mVoltageFeedbackMinXRedundant = Float.parseFloat(
                                csvr.get(dataHeader.indexOf("X_VOLT_MIN_SEC_FB")));
                        e.mVoltageFeedbackMaxXRedundant = Float.parseFloat(
                                csvr.get(dataHeader.indexOf("X_VOLT_MAX_SEC_FB")));
                        e.mVoltageFeedbackMinYRedundant = Float.parseFloat(
                                csvr.get(dataHeader.indexOf("Y_VOLT_MIN_SEC_FB")));
                        e.mVoltageFeedbackMaxYRedundant = Float.parseFloat(
                                csvr.get(dataHeader.indexOf("Y_VOLT_MAX_SEC_FB")));

                        // 20 INACTIVE

                        if (e instanceof PbsSpot) {
                            ((PbsSpot) e).getXMinField();
                            ((PbsSpot) e).mXMinBeamSizeFeedback = Float.parseFloat(
                                    csvr.get(dataHeader.indexOf("X_MIN_WIDTH")));
                            ((PbsSpot) e).mXMaxBeamSizeFeedback = Float.parseFloat(
                                    csvr.get(dataHeader.indexOf("X_MAX_WIDTH")));
                            ((PbsSpot) e).mYMinBeamSizeFeedback = Float.parseFloat(
                                    csvr.get(dataHeader.indexOf("Y_MIN_WIDTH")));
                            ((PbsSpot) e).mYMaxBeamSizeFeedback = Float.parseFloat(
                                    csvr.get(dataHeader.indexOf("Y_MAX_WIDTH")));

                            ((PbsSpot) e).mXMinPositionFeedback = Float.parseFloat(
                                    csvr.get(dataHeader.indexOf("X_POS_LOW")));
                            ((PbsSpot) e).mXMaxPositionFeedback = Float.parseFloat(
                                    csvr.get(dataHeader.indexOf("X_POS_HIGH")));
                            ((PbsSpot) e).mYMinPositionFeedback = Float.parseFloat(
                                    csvr.get(dataHeader.indexOf("Y_POS_LOW")));
                            ((PbsSpot) e).mYMaxPositionFeedback = Float.parseFloat(
                                    csvr.get(dataHeader.indexOf("Y_POS_HIGH")));

                            ((PbsSpot) e).mXMinNozzleEntranceWidth = Float.parseFloat(
                                    csvr.get(dataHeader.indexOf("X_MIN_WIDTH_IC1")));
                            ((PbsSpot) e).mXMaxNozzleEntranceWidth = Float.parseFloat(
                                    csvr.get(dataHeader.indexOf("X_MAX_WIDTH_IC1")));
                            ((PbsSpot) e).mYMinNozzleEntranceWidth = Float.parseFloat(
                                    csvr.get(dataHeader.indexOf("Y_MIN_WIDTH_IC1")));
                            ((PbsSpot) e).mYMaxNozzleEntranceWidth = Float.parseFloat(
                                    csvr.get(dataHeader.indexOf("Y_MAX_WIDTH_IC1")));

                            ((PbsSpot) e).mXMinNozzleEntrancePosition = Float.parseFloat(
                                    csvr.get(dataHeader.indexOf("X_POS_LOW_IC1")));
                            ((PbsSpot) e).mXMaxNozzleEntrancePosition = Float.parseFloat(
                                    csvr.get(dataHeader.indexOf("X_POS_HIGH_IC1")));
                            ((PbsSpot) e).mYMinNozzleEntrancePosition = Float.parseFloat(
                                    csvr.get(dataHeader.indexOf("Y_POS_LOW_IC1")));
                            ((PbsSpot) e).mYMaxNozzleEntrancePosition = Float.parseFloat(
                                    csvr.get(dataHeader.indexOf("Y_POS_HIGH_IC1")));
                        }

                        elements.add(e);
                        ++i;
                    }
                    map.setElements(elements);

                    if (result != null) {
                        //((DefaultPbsBdsLayerSettings) result).addMap(map);
                        //((DefaultPbsBdsLayerSettings) result).setCurrentMapToIrradiate(map);
                    } else {
                        com.iba.icomp.core.util.Logger.getLogger().warn("Layer settings are not loaded, file format not correct");
                    }
                } catch (IOException e) {
                    String msg = "Error reading PBS bds layer setting file '" + filename + "'";
                    com.iba.icomp.core.util.Logger.getLogger().error(msg);
                    SwingUtilities.invokeLater(new PopupDisplayer(msg, "IO Error", JOptionPane.ERROR_MESSAGE));
                } catch (Exception e) {
                    String msg = "Error reading PBS bds layer setting file: " + e.getMessage();
                    com.iba.icomp.core.util.Logger.getLogger().error(msg);
                    SwingUtilities.invokeLater(
                            new PopupDisplayer(msg, Utils.getCurrentFunction(), JOptionPane.ERROR_MESSAGE));
                }

                csvr.close();
            } catch (IOException e) {
                String msg = "Could not open PBS bds layer setting file '" + filename + "'";
                com.iba.icomp.core.util.Logger.getLogger().error(msg);
                SwingUtilities.invokeLater(new PopupDisplayer(msg, "IO Error", JOptionPane.ERROR_MESSAGE));
            }
        }

        return result;
    } */


    private ScanningControllerPbsLayer loadFromCsvFile(String pFilename) {
        File f = new File(pFilename);
        DefaultPbsBdsLayerSettings result = null;
        if (!f.exists()) {
            log.error("Could not find xml file");
            return null;
        } else {
            try {
                InputStreamReader fr = new InputStreamReader(new FileInputStream(f), BlakConstants.DEFAULT_CHARSET.name());

                Reader reader = new BufferedReader(new FileReader(f));

                CsvReader csvr = new CsvReader(fr, ',');

                ScanningControllerPbsLayer scLayer = new ScanningControllerPbsLayer();

                PbsEquipmentMap map = new PbsEquipmentMap();

                int elemNbr = 0;

                List<String> header = new ArrayList<String>();

                while(csvr.readRecord()) {
                    String elementName = csvr.get(0);
                    if (!elementName.isEmpty()) {
                        if (!elementName.equals("#LAYER_ID")) {
                            result = new DefaultPbsBdsLayerSettings(
                                    Integer.parseInt(csvr.get(((List)header).indexOf("#LAYER_ID"))),
                                    false, "4.0", 230.0D,
                                    Double.parseDouble(csvr.get(((List)header).indexOf("RANGE"))),
                                    Double.parseDouble(csvr.get(((List)header).indexOf("RANGE"))),
                                    Double.parseDouble(csvr.get(((List)header).indexOf("TOTAL_CHARGE"))),
                                    1);

                            scLayer.diagnosticMode = Integer.parseInt(csvr.get(3));
                            scLayer.metersetCorrectionFactor = Integer.parseInt(csvr.get(4));
                            elemNbr = Integer.parseInt(csvr.get(5));
                            scLayer.narrowBeamEntranceX = Integer.parseInt(csvr.get(6));
                            scLayer.narrowBeamEntranceY = Integer.parseInt(csvr.get(7));
                            scLayer.narrowBeamExitX = Integer.parseInt(csvr.get(8));
                            scLayer.narrowBeamExitY = Integer.parseInt(csvr.get(9));
                            break;
                        }

                        header = Arrays.asList(csvr.getValues());
                    }
                }

                scLayer.id = new PbsLayerId();
                scLayer.id.value = result.getLayerIndex();
                scLayer.range = result.getRangeAtNozzleEntrance();

                try
                {
                    List<PbsMapElement> elements = new LinkedList<PbsMapElement>();
                    List<String> dataHeader = new ArrayList<String>();
                    for (int i = 0; i < elemNbr; )
                    {
                        csvr.readRecord();

                        String elementName = csvr.get(0);
                        // Skip comment lines
                        if (elementName.isEmpty())
                        {
                            continue;
                        }

                        if (elementName.equals("#ELEMENT_ID"))
                        {
                            dataHeader = Arrays.asList(csvr.getValues());
                            continue;
                        }

                        if (csvr.getValues().length != 79)
                        {
                            throw new Exception("The PbsBdsLayerSetting file format not supported!");
                        }

                        PbsEquipmentElement e;

                        PbsSlewConstants mConstants = new PbsSlewConstants();
                        mConstants.mICCycloMin = -0.009f;
                        mConstants.mICCycloMax = 0.009f;

                        mConstants.mChargeMinIc1 = 0;
                        mConstants.mChargeMaxIc1 = 0;
                        mConstants.mChargeMinPrimary = -0.00000000001f;
                        mConstants.mChargeMinSecondary = -0.00000000001f;
                        mConstants.mChargeMinTernary = -0.00000000001f;
                        mConstants.mChargeMaxPrimary = 0.00000000001f;
                        mConstants.mChargeMaxSecondary = 0.00000000001f;
                        mConstants.mChargeMaxTernary = 0.00000000001f;

                        mConstants.mChargeRateMinPrimary = -0.00000001f;
                        mConstants.mChargeRateMinSecondary = -0.00000001f;
                        mConstants.mChargeRateMinTernary = -1f;
                        mConstants.mChargeRateMaxPrimary = 0.0000001f;
                        mConstants.mChargeRateMaxSecondary = 0.0000001f;
                        mConstants.mChargeRateMaxTernary = 1f;

                        mConstants.mDisabledIc1PositionMin = 1;
                        mConstants.mDisabledIc1PositionMax = 1;

                        mConstants.mIrradiationTime = 0.25f;

                        switch (PbsElementType.values()[Integer.parseInt(
                                csvr.get(dataHeader.indexOf("ELEMENT_TYPE")))])
                        {
                            case SPOT:
                                e = new PbsSpot();
                                break;
                            case SLEW:
                                e = new PbsSlew(mConstants);
                                break;
                            default:// spot
                                e = new PbsSpot();
                                break;
                        }

                        if (e instanceof PbsSpot)
                        {
                            ((PbsSpot) e).mTargetCharge = Float.parseFloat(
                                    csvr.get(dataHeader.indexOf("TARGET_CHARGE")));
                            ((PbsSpot) e).mBeamCurrentSetpoint = Float.parseFloat(
                                    csvr.get(dataHeader.indexOf("BEAM_CURRENT_SP")));
                            // two INACTIVE
                            ((PbsSpot) e).mIrradiationTime = Float.parseFloat(
                                    csvr.get(dataHeader.indexOf("MAX_DURATION")));
                            ((PbsSpot) e).mChargeMinPrimary = Float.parseFloat(
                                    csvr.get(dataHeader.indexOf("MIN_CHARGE_PRIM")));
                            ((PbsSpot) e).mChargeMaxPrimary = Float.parseFloat(
                                    csvr.get(dataHeader.indexOf("MAX_CHARGE_PRIM")));
                            ((PbsSpot) e).mChargeMinSecondary = Float.parseFloat(
                                    csvr.get(dataHeader.indexOf("MIN_CHARGE_SEC")));
                            ((PbsSpot) e).mChargeMaxSecondary = Float.parseFloat(
                                    csvr.get(dataHeader.indexOf("MAX_CHARGE_SEC")));
                            ((PbsSpot) e).mChargeMinTernary = Float.parseFloat(
                                    csvr.get(dataHeader.indexOf("MIN_CHARGE_TER")));
                            ((PbsSpot) e).mChargeMaxTernary = Float.parseFloat(
                                    csvr.get(dataHeader.indexOf("MAX_CHARGE_TER")));
                            // two INACTIVE MIN_IC1_CHARGE MAX_IC1_CHARGE

                            ((PbsSpot) e).mChargeRateMinPrimary = Float.parseFloat(
                                    csvr.get(dataHeader.indexOf("MIN_DOSE_RATE_PRIM")));
                            ((PbsSpot) e).mChargeRateMaxPrimary = Float.parseFloat(
                                    csvr.get(dataHeader.indexOf("MAX_DOSE_RATE_PRIM")));
                            ((PbsSpot) e).mChargeRateMinSecondary = Float.parseFloat(
                                    csvr.get(dataHeader.indexOf("MIN_DOSE_RATE_SEC")));
                            ((PbsSpot) e).mChargeRateMaxSecondary = Float.parseFloat(
                                    csvr.get(dataHeader.indexOf("MAX_DOSE_RATE_SEC")));
                            ((PbsSpot) e).mChargeRateMinTernary = Float.parseFloat(
                                    csvr.get(dataHeader.indexOf("MIN_DOSE_RATE_TER")));
                            ((PbsSpot) e).mChargeRateMaxTernary = Float.parseFloat(
                                    csvr.get(dataHeader.indexOf("MAX_DOSE_RATE_TER")));
                            // two INACTIVE MIN_DOSE_RATE_IC1 MAX_DOSE_RATE_IC1
                            ((PbsSpot) e).mICCycloMin = Float.parseFloat(
                                    csvr.get(dataHeader.indexOf("MIN_BEAM_CURRENT_FB")));
                            ((PbsSpot) e).mICCycloMax = Float.parseFloat(
                                    csvr.get(dataHeader.indexOf("MAX_BEAM_CURRENT_FB")));
                        }
                        e.mCurrentSetpointX = Float.parseFloat(csvr.get(dataHeader.indexOf("X_CURRENT_TARGET_SP")));
                        e.mCurrentSetpointY = Float.parseFloat(csvr.get(dataHeader.indexOf("Y_CURRENT_TARGET_SP")));

                        e.mCurrentFeedbackMinXPrimary = Float.parseFloat(
                                csvr.get(dataHeader.indexOf("X_CURRENT_MIN_PRIM_FB")));
                        e.mCurrentFeedbackMaxXPrimary = Float.parseFloat(
                                csvr.get(dataHeader.indexOf("X_CURRENT_MAX_PRIM_FB")));
                        e.mCurrentFeedbackMinYPrimary = Float.parseFloat(
                                csvr.get(dataHeader.indexOf("Y_CURRENT_MIN_PRIM_FB")));
                        e.mCurrentFeedbackMaxYPrimary = Float.parseFloat(
                                csvr.get(dataHeader.indexOf("Y_CURRENT_MAX_PRIM_FB")));
                        e.mVoltageFeedbackMinXPrimary = Float.parseFloat(
                                csvr.get(dataHeader.indexOf("X_VOLT_MIN_PRIM_FB")));
                        e.mVoltageFeedbackMaxXPrimary = Float.parseFloat(
                                csvr.get(dataHeader.indexOf("X_VOLT_MAX_PRIM_FB")));
                        e.mVoltageFeedbackMinYPrimary = Float.parseFloat(
                                csvr.get(dataHeader.indexOf("Y_VOLT_MIN_PRIM_FB")));
                        e.mVoltageFeedbackMaxYPrimary = Float.parseFloat(
                                csvr.get(dataHeader.indexOf("Y_VOLT_MIN_PRIM_FB")));
                        e.mCurrentFeedbackMinXRedundant = Float.parseFloat(
                                csvr.get(dataHeader.indexOf("X_CURRENT_MIN_SEC_FB")));
                        e.mCurrentFeedbackMaxXRedundant = Float.parseFloat(
                                csvr.get(dataHeader.indexOf("X_CURRENT_MAX_SEC_FB")));
                        e.mCurrentFeedbackMinYRedundant = Float.parseFloat(
                                csvr.get(dataHeader.indexOf("Y_CURRENT_MIN_SEC_FB")));
                        e.mCurrentFeedbackMaxYRedundant = Float.parseFloat(
                                csvr.get(dataHeader.indexOf("Y_CURRENT_MAX_SEC_FB")));
                        e.mVoltageFeedbackMinXRedundant = Float.parseFloat(
                                csvr.get(dataHeader.indexOf("X_VOLT_MIN_SEC_FB")));
                        e.mVoltageFeedbackMaxXRedundant = Float.parseFloat(
                                csvr.get(dataHeader.indexOf("X_VOLT_MAX_SEC_FB")));
                        e.mVoltageFeedbackMinYRedundant = Float.parseFloat(
                                csvr.get(dataHeader.indexOf("Y_VOLT_MIN_SEC_FB")));
                        e.mVoltageFeedbackMaxYRedundant = Float.parseFloat(
                                csvr.get(dataHeader.indexOf("Y_VOLT_MAX_SEC_FB")));

                        // 20 INACTIVE

                        if (e instanceof PbsSpot)
                        {
                            ((PbsSpot) e).getXMinField();
                            ((PbsSpot) e).mXMinBeamSizeFeedback = Float.parseFloat(
                                    csvr.get(dataHeader.indexOf("X_MIN_WIDTH")));
                            ((PbsSpot) e).mXMaxBeamSizeFeedback = Float.parseFloat(
                                    csvr.get(dataHeader.indexOf("X_MAX_WIDTH")));
                            ((PbsSpot) e).mYMinBeamSizeFeedback = Float.parseFloat(
                                    csvr.get(dataHeader.indexOf("Y_MIN_WIDTH")));
                            ((PbsSpot) e).mYMaxBeamSizeFeedback = Float.parseFloat(
                                    csvr.get(dataHeader.indexOf("Y_MAX_WIDTH")));

                            ((PbsSpot) e).mXMinPositionFeedback = Float.parseFloat(
                                    csvr.get(dataHeader.indexOf("X_POS_LOW")));
                            ((PbsSpot) e).mXMaxPositionFeedback = Float.parseFloat(
                                    csvr.get(dataHeader.indexOf("X_POS_HIGH")));
                            ((PbsSpot) e).mYMinPositionFeedback = Float.parseFloat(
                                    csvr.get(dataHeader.indexOf("Y_POS_LOW")));
                            ((PbsSpot) e).mYMaxPositionFeedback = Float.parseFloat(
                                    csvr.get(dataHeader.indexOf("Y_POS_HIGH")));

                            ((PbsSpot) e).mXMinNozzleEntranceWidth = Float.parseFloat(
                                    csvr.get(dataHeader.indexOf("X_MIN_WIDTH_IC1")));
                            ((PbsSpot) e).mXMaxNozzleEntranceWidth = Float.parseFloat(
                                    csvr.get(dataHeader.indexOf("X_MAX_WIDTH_IC1")));
                            ((PbsSpot) e).mYMinNozzleEntranceWidth = Float.parseFloat(
                                    csvr.get(dataHeader.indexOf("Y_MIN_WIDTH_IC1")));
                            ((PbsSpot) e).mYMaxNozzleEntranceWidth = Float.parseFloat(
                                    csvr.get(dataHeader.indexOf("Y_MAX_WIDTH_IC1")));

                            ((PbsSpot) e).mXMinNozzleEntrancePosition = Float.parseFloat(
                                    csvr.get(dataHeader.indexOf("X_POS_LOW_IC1")));
                            ((PbsSpot) e).mXMaxNozzleEntrancePosition = Float.parseFloat(
                                    csvr.get(dataHeader.indexOf("X_POS_HIGH_IC1")));
                            ((PbsSpot) e).mYMinNozzleEntrancePosition = Float.parseFloat(
                                    csvr.get(dataHeader.indexOf("Y_POS_LOW_IC1")));
                            ((PbsSpot) e).mYMaxNozzleEntrancePosition = Float.parseFloat(
                                    csvr.get(dataHeader.indexOf("Y_POS_HIGH_IC1")));
                        }

                        elements.add(e);
                        ++i;
                    }
                    map.setElements(elements);

                    if (result != null)
                    {
                        ((DefaultPbsBdsLayerSettings) result).addMap(map);
                        //((DefaultPbsBdsLayerSettings) result).setCurrentMapToIrradiate(map);
                    }
                    else
                    {
                        com.iba.icomp.core.util.Logger.getLogger().warn("Layer settings are not loaded, file format not correct");
                    }
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                    String msg = "Error reading PBS bds layer setting file '" + pFilename + "'";
                    com.iba.icomp.core.util.Logger.getLogger().error(msg);
                    SwingUtilities.invokeLater(new PopupDisplayer(msg, "IO Error", JOptionPane.ERROR_MESSAGE));
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    String msg = "Error reading PBS bds layer setting file: " + e.getMessage();
                    com.iba.icomp.core.util.Logger.getLogger().error(msg);
                    SwingUtilities.invokeLater(
                            new PopupDisplayer(msg, Utils.getCurrentFunction(), JOptionPane.ERROR_MESSAGE));
                }

                csvr.close();

                scLayer.elements = new ScanningControllerPbsLayerElement[map.getElements().size()];

                for(int i = 0; i < scLayer.elements.length; ++i) {
                    PbsEquipmentElement elem = (PbsEquipmentElement) map.getElements().get(i);
                    scLayer.elements[i] = new ScanningControllerPbsLayerElement();
                    scLayer.elements[i].beamCurrentSetpoint = elem.getBeamCurrentSetpoint();
                    scLayer.elements[i].maxCycloBeam = elem.getMaxCycloBeamFeedback();
                    scLayer.elements[i].maxDuration = elem.getMaxDuration();
                    scLayer.elements[i].maxPrimaryCharge = elem.getMaxPrimaryChargeFeedback();
                    scLayer.elements[i].maxPrimaryDoseRate = elem.getMaxPrimaryDoseRate();
                    scLayer.elements[i].maxSecondaryCharge = elem.getMaxSecondaryChargeFeedback();
                    scLayer.elements[i].maxSecondaryDoseRate = elem.getMaxSecondaryDoseRate();
                    scLayer.elements[i].maxTernaryCharge = elem.getMaxTernaryChargeFeedback();
                    scLayer.elements[i].maxTernaryDoseRate = elem.getMaxTernaryDoseRate();
                    scLayer.elements[i].minCycloBeam = elem.getMinCycloBeamFeedback();
                    scLayer.elements[i].minPrimaryCharge = elem.getMinPrimaryChargeFeedback();
                    scLayer.elements[i].minPrimaryDoseRate = elem.getMinPrimaryDoseRate();
                    scLayer.elements[i].minSecondaryCharge = elem.getMinSecondaryChargeFeedback();
                    scLayer.elements[i].minSecondaryDoseRate = elem.getMinSecondaryDoseRate();
                    scLayer.elements[i].minTernaryCharge = elem.getMinTernaryChargeFeedback();
                    scLayer.elements[i].minTernaryDoseRate = elem.getMinTernaryDoseRate();
                    scLayer.elements[i].targetCharge = elem.getTargetCharge();
                    scLayer.elements[i].type = elem.getType().ordinal();
                    scLayer.elements[i].xCurrentSetpoint = elem.getXMagnetCurrentSetpoint();
                    scLayer.elements[i].xMaxBeamWidth = elem.getXMaxBeamSizeFeedback();
                    scLayer.elements[i].xMaxField = elem.getXMaxField();
                    scLayer.elements[i].xMaxPrimaryCurrentFeedback = elem.getXMaxPrimaryCurrentFeedback();
                    scLayer.elements[i].xMaxPrimaryVoltageFeedback = elem.getXMaxPrimaryVoltageFeedback();
                    scLayer.elements[i].xMaxSecondaryCurrentFeedback = elem.getXMaxSecondaryCurrentFeedback();
                    scLayer.elements[i].xMaxSecondaryVoltageFeedback = elem.getXMaxSecondaryVoltageFeedback();
                    scLayer.elements[i].xMinBeamWidth = elem.getXMinBeamSizeFeedback();
                    scLayer.elements[i].xMinField = elem.getXMinField();
                    scLayer.elements[i].xMinPrimaryCurrentFeedback = elem.getXMinPrimaryCurrentFeedback();
                    scLayer.elements[i].xMinPrimaryVoltageFeedback = elem.getXMinPrimaryVoltageFeedback();
                    scLayer.elements[i].xMinSecondaryCurrentFeedback = elem.getXMinSecondaryCurrentFeedback();
                    scLayer.elements[i].xMinSecondaryVoltageFeedback = elem.getXMinSecondaryVoltageFeedback();
                    scLayer.elements[i].xPositionHigh = elem.getXMinPositionFeedback();
                    scLayer.elements[i].xPositionLow = elem.getXMaxPositionFeedback();
                    scLayer.elements[i].yCurrentSetpoint = elem.getYMagnetCurrentSetpoint();
                    scLayer.elements[i].yMaxBeamWidth = elem.getYMaxBeamSizeFeedback();
                    scLayer.elements[i].yMaxField = elem.getYMaxField();
                    scLayer.elements[i].yMaxPrimaryCurrentFeedback = elem.getYMaxPrimaryCurrentFeedback();
                    scLayer.elements[i].yMaxPrimaryVoltageFeedback = elem.getYMaxPrimaryVoltageFeedback();
                    scLayer.elements[i].yMaxSecondaryCurrentFeedback = elem.getYMaxSecondaryCurrentFeedback();
                    scLayer.elements[i].yMaxSecondaryVoltageFeedback = elem.getYMaxSecondaryVoltageFeedback();
                    scLayer.elements[i].yMinBeamWidth = elem.getYMinBeamSizeFeedback();
                    scLayer.elements[i].yMinField = elem.getYMinField();
                    scLayer.elements[i].yMinPrimaryCurrentFeedback = elem.getYMinPrimaryCurrentFeedback();
                    scLayer.elements[i].yMinPrimaryVoltageFeedback = elem.getYMinPrimaryVoltageFeedback();
                    scLayer.elements[i].yMinSecondaryCurrentFeedback = elem.getYMinSecondaryCurrentFeedback();
                    scLayer.elements[i].yMinSecondaryVoltageFeedback = elem.getYMinSecondaryVoltageFeedback();
                    scLayer.elements[i].yPositionHigh = elem.getYMinPositionFeedback();
                    scLayer.elements[i].yPositionLow = elem.getYMaxPositionFeedback();
                    scLayer.elements[i].xMinNozzleEntrancePositionThreshold = elem.getXMinNozzleEntrancePosition();
                    scLayer.elements[i].xMaxNozzleEntrancePositionThreshold = elem.getXMaxNozzleEntrancePosition();
                    scLayer.elements[i].yMinNozzleEntrancePositionThreshold = elem.getYMinNozzleEntrancePosition();
                    scLayer.elements[i].yMaxNozzleEntrancePositionThreshold = elem.getYMaxNozzleEntrancePosition();
                    scLayer.elements[i].xMinNozzleEntranceWidthThreshold = elem.getXMinNozzleEntranceWidth();
                    scLayer.elements[i].xMaxNozzleEntranceWidthThreshold = elem.getXMaxNozzleEntranceWidth();
                    scLayer.elements[i].yMinNozzleEntranceWidthThreshold = elem.getYMinNozzleEntranceWidth();
                    scLayer.elements[i].yMaxNozzleEntranceWidthThreshold = elem.getYMaxNozzleEntranceWidth();
                }


                return scLayer;
            } catch (Exception var13) {
                this.mRecorderEvents.addElement("Exception");
                var13.printStackTrace();
                log.error("exception making sclayer from xml");
                return null;
            }
        }
    }


    private ScanningControllerPbsLayer loadFromXmlFile(String pFilename) {
        File f = new File(pFilename);
        if (!f.exists()) {
            log.error("Could not find xml file");
            return null;
        } else {
            try {
                Reader reader = new BufferedReader(new FileReader(f));

                XStream s = new XStream();
                ObjectInputStream ois = s.createObjectInputStream(reader);
                DefaultIrradiationSettings irradiationSettings = (DefaultIrradiationSettings)ois.readObject();
                List<BmsLayerSettings> layerSettings = irradiationSettings.getBmsSettings().getLayerSettings();
                reader.close();
                ois.close();
                ScanningControllerPbsLayer scLayer = new ScanningControllerPbsLayer();

                if (layerSettings.size() > 0) {
                    PbsBdsLayerSettings pbsBdsLayerSettings = (PbsBdsLayerSettings)((BmsLayerSettings)layerSettings.get(0)).getBdsLayerSettings();
                    PbsMap map = (PbsMap)pbsBdsLayerSettings.getMaps().get(0);
                    scLayer.id = new PbsLayerId();
                    scLayer.id.value = pbsBdsLayerSettings.getLayerIndex();
                    scLayer.range = pbsBdsLayerSettings.getRangeAtNozzleEntrance();
                    scLayer.totalCharge = map.getTotalCharge();
                    scLayer.diagnosticMode = map.isDiagnosticMode() ? 1 : 0;
                    scLayer.narrowBeamEntranceX = map.isNarrowBeamEntranceX() ? 1 : 0;
                    scLayer.narrowBeamEntranceY = map.isNarrowBeamEntranceY() ? 1 : 0;
                    scLayer.narrowBeamExitX = map.isNarrowBeamExitX() ? 1 : 0;
                    scLayer.narrowBeamExitY = map.isNarrowBeamExitY() ? 1 : 0;
                    scLayer.metersetCorrectionFactor = map.getMetersetCorrectionFactor();
                    //scLayer.kFactor = Double.parseDouble(this.mKFactor.getText());
                    scLayer.elements = new ScanningControllerPbsLayerElement[map.getElements().size()];

                    for(int i = 0; i < scLayer.elements.length; ++i) {
                        PbsEquipmentElement elem = (PbsEquipmentElement) map.getElements().get(i);
                        scLayer.elements[i] = new ScanningControllerPbsLayerElement();
                        scLayer.elements[i].beamCurrentSetpoint = elem.getBeamCurrentSetpoint();
                        scLayer.elements[i].maxCycloBeam = elem.getMaxCycloBeamFeedback();
                        scLayer.elements[i].maxDuration = elem.getMaxDuration();
                        scLayer.elements[i].maxPrimaryCharge = elem.getMaxPrimaryChargeFeedback();
                        scLayer.elements[i].maxPrimaryDoseRate = elem.getMaxPrimaryDoseRate();
                        scLayer.elements[i].maxSecondaryCharge = elem.getMaxSecondaryChargeFeedback();
                        scLayer.elements[i].maxSecondaryDoseRate = elem.getMaxSecondaryDoseRate();
                        scLayer.elements[i].maxTernaryCharge = elem.getMaxTernaryChargeFeedback();
                        scLayer.elements[i].maxTernaryDoseRate = elem.getMaxTernaryDoseRate();
                        scLayer.elements[i].minCycloBeam = elem.getMinCycloBeamFeedback();
                        scLayer.elements[i].minPrimaryCharge = elem.getMinPrimaryChargeFeedback();
                        scLayer.elements[i].minPrimaryDoseRate = elem.getMinPrimaryDoseRate();
                        scLayer.elements[i].minSecondaryCharge = elem.getMinSecondaryChargeFeedback();
                        scLayer.elements[i].minSecondaryDoseRate = elem.getMinSecondaryDoseRate();
                        scLayer.elements[i].minTernaryCharge = elem.getMinTernaryChargeFeedback();
                        scLayer.elements[i].minTernaryDoseRate = elem.getMinTernaryDoseRate();
                        scLayer.elements[i].targetCharge = elem.getTargetCharge();
                        scLayer.elements[i].type = elem.getType().ordinal();
                        scLayer.elements[i].xCurrentSetpoint = elem.getXMagnetCurrentSetpoint();
                        scLayer.elements[i].xMaxBeamWidth = elem.getXMaxBeamSizeFeedback();
                        scLayer.elements[i].xMaxField = elem.getXMaxField();
                        scLayer.elements[i].xMaxPrimaryCurrentFeedback = elem.getXMaxPrimaryCurrentFeedback();
                        scLayer.elements[i].xMaxPrimaryVoltageFeedback = elem.getXMaxPrimaryVoltageFeedback();
                        scLayer.elements[i].xMaxSecondaryCurrentFeedback = elem.getXMaxSecondaryCurrentFeedback();
                        scLayer.elements[i].xMaxSecondaryVoltageFeedback = elem.getXMaxSecondaryVoltageFeedback();
                        scLayer.elements[i].xMinBeamWidth = elem.getXMinBeamSizeFeedback();
                        scLayer.elements[i].xMinField = elem.getXMinField();
                        scLayer.elements[i].xMinPrimaryCurrentFeedback = elem.getXMinPrimaryCurrentFeedback();
                        scLayer.elements[i].xMinPrimaryVoltageFeedback = elem.getXMinPrimaryVoltageFeedback();
                        scLayer.elements[i].xMinSecondaryCurrentFeedback = elem.getXMinSecondaryCurrentFeedback();
                        scLayer.elements[i].xMinSecondaryVoltageFeedback = elem.getXMinSecondaryVoltageFeedback();
                        scLayer.elements[i].xPositionHigh = elem.getXMinPositionFeedback();
                        scLayer.elements[i].xPositionLow = elem.getXMaxPositionFeedback();
                        scLayer.elements[i].yCurrentSetpoint = elem.getYMagnetCurrentSetpoint();
                        scLayer.elements[i].yMaxBeamWidth = elem.getYMaxBeamSizeFeedback();
                        scLayer.elements[i].yMaxField = elem.getYMaxField();
                        scLayer.elements[i].yMaxPrimaryCurrentFeedback = elem.getYMaxPrimaryCurrentFeedback();
                        scLayer.elements[i].yMaxPrimaryVoltageFeedback = elem.getYMaxPrimaryVoltageFeedback();
                        scLayer.elements[i].yMaxSecondaryCurrentFeedback = elem.getYMaxSecondaryCurrentFeedback();
                        scLayer.elements[i].yMaxSecondaryVoltageFeedback = elem.getYMaxSecondaryVoltageFeedback();
                        scLayer.elements[i].yMinBeamWidth = elem.getYMinBeamSizeFeedback();
                        scLayer.elements[i].yMinField = elem.getYMinField();
                        scLayer.elements[i].yMinPrimaryCurrentFeedback = elem.getYMinPrimaryCurrentFeedback();
                        scLayer.elements[i].yMinPrimaryVoltageFeedback = elem.getYMinPrimaryVoltageFeedback();
                        scLayer.elements[i].yMinSecondaryCurrentFeedback = elem.getYMinSecondaryCurrentFeedback();
                        scLayer.elements[i].yMinSecondaryVoltageFeedback = elem.getYMinSecondaryVoltageFeedback();
                        scLayer.elements[i].yPositionHigh = elem.getYMinPositionFeedback();
                        scLayer.elements[i].yPositionLow = elem.getYMaxPositionFeedback();
                        scLayer.elements[i].xMinNozzleEntrancePositionThreshold = elem.getXMinNozzleEntrancePosition();
                        scLayer.elements[i].xMaxNozzleEntrancePositionThreshold = elem.getXMaxNozzleEntrancePosition();
                        scLayer.elements[i].yMinNozzleEntrancePositionThreshold = elem.getYMinNozzleEntrancePosition();
                        scLayer.elements[i].yMaxNozzleEntrancePositionThreshold = elem.getYMaxNozzleEntrancePosition();
                        scLayer.elements[i].xMinNozzleEntranceWidthThreshold = elem.getXMinNozzleEntranceWidth();
                        scLayer.elements[i].xMaxNozzleEntranceWidthThreshold = elem.getXMaxNozzleEntranceWidth();
                        scLayer.elements[i].yMinNozzleEntranceWidthThreshold = elem.getYMinNozzleEntranceWidth();
                        scLayer.elements[i].yMaxNozzleEntranceWidthThreshold = elem.getYMaxNozzleEntranceWidth();
                    }
                }

                return scLayer;
            } catch (Exception var13) {
                this.mRecorderEvents.addElement("Exception");
                var13.printStackTrace();
                log.error("exception making sclayer from xml");
                return null;
            }
        }
    }

    public static void main(String[] pArgs) throws Exception {
        new ScanningController();
        OncRpcEmbeddedPortmap portmap = new OncRpcEmbeddedPortmap();
        portmap.shutdown();
    }

    class CancelLayer implements ActionListener {
        CancelLayer() {
        }

        public void actionPerformed(ActionEvent pE) {
            ScanningControllerRpcDefault_in startIn = new ScanningControllerRpcDefault_in();
            startIn.caller = "TCU Test".getBytes();

            try {
                ScanningControllerRpcStatus status = ScanningController.this.mClient.scanningControllerRpcCancel_1(startIn);
                if (status.success == 78) {
                    ScanningController.this.mRecorderEvents.addElement("Cancel request failed: " + new String(status.message));
                    return;
                }
            } catch (OncRpcException var5) {
                ScanningController.this.mRecorderEvents.addElement("Exception");
            } catch (IOException var6) {
                ScanningController.this.mRecorderEvents.addElement("Exception");
            }

        }
    }

    class StartLayer implements ActionListener {
        StartLayer() {
        }

        public void actionPerformed(ActionEvent pE) {
            ScanningControllerRpcDefault_in startIn = new ScanningControllerRpcDefault_in();
            startIn.caller = "TCU Test".getBytes();

            try {
                ScanningControllerRpcStatus status = ScanningController.this.mClient.scanningControllerRpcStart_1(startIn);
                if (status.success == 78) {
                    ScanningController.this.mRecorderEvents.addElement("Start request failed: " + new String(status.message));
                    return;
                }
            } catch (OncRpcException var5) {
                ScanningController.this.mRecorderEvents.addElement("Exception");
            } catch (IOException var6) {
                ScanningController.this.mRecorderEvents.addElement("Exception");
            }

        }
    }

    class SendLayerByXstream implements ActionListener {
        SendLayerByXstream() {
        }

        public void actionPerformed(ActionEvent pE) {
            ScanningController.this.mDoseGridPlayer.clearAndRemoveSpots();
            String filename = ScanningController.this.mFileTF.getText();
            ScanningControllerPbsLayer layer;
            if (filename.endsWith(".xdr")) {
                layer = ScanningController.this.loadFromXdrFile(filename);
            } else if (filename.endsWith(".csv")) {
                layer = ScanningController.this.loadFromCsvFile(filename);
            } else {
                layer = ScanningController.this.loadFromXmlFile(filename);
                log.info("layer made from Xml file");
            }

            if (layer != null) {
                ScanningControllerRpcPrepareLayer_in prepareIn = new ScanningControllerRpcPrepareLayer_in();
                prepareIn.caller = "TCU Test".getBytes();
                prepareIn.layer = layer;

                try {
                    ScanningControllerRpcStatusMD5 stat = ScanningController.this.mClient.scanningControllerRpcPrepare_1(prepareIn);
                    ScanningControllerRpcStatus status = stat.rpcStatus;
                    if (status.success == 78) {
                        ScanningController.this.mRecorderEvents.addElement("Preparation request failed: " + new String(status.message));
                        return;
                    }
                } catch (OncRpcException var7) {
                    ScanningController.this.mRecorderEvents.addElement("Exception");
                } catch (IOException var8) {
                    ScanningController.this.mRecorderEvents.addElement("Exception");
                }
            } else {
                ScanningController.this.mRecorderEvents.addElement("Unable to load xdr file");
            }

        }
    }

    class SendLayerByStruct implements ActionListener {
        SendLayerByStruct() {
        }

        public void actionPerformed(ActionEvent pE) {
            ScanningController.this.mDoseGridPlayer.clearAndRemoveSpots();
            String filename = ScanningController.this.mFileTF.getText();
            ScanningControllerPbsLayer layer = ScanningController.this.loadFromXdrFile(filename);
            if (layer != null) {
                ScanningControllerRpcPrepareLayer_in prepareIn = new ScanningControllerRpcPrepareLayer_in();
                prepareIn.caller = "TCU Test".getBytes();
                prepareIn.layer = layer;

                try {
                    ScanningControllerRpcStatusMD5 stat = ScanningController.this.mClient.scanningControllerRpcPrepare_1(prepareIn);
                    ScanningControllerRpcStatus status = stat.rpcStatus;
                    if (status.success == 78) {
                        ScanningController.this.mRecorderEvents.addElement("Preparation request failed: " + new String(status.message));
                        return;
                    }
                } catch (OncRpcException var7) {
                    ScanningController.this.mRecorderEvents.addElement("Exception");
                } catch (IOException var8) {
                    ScanningController.this.mRecorderEvents.addElement("Exception");
                }
            } else {
                ScanningController.this.mRecorderEvents.addElement("Unable to load xdr file");
            }

        }
    }

    class SendLayerByFile implements ActionListener {
        SendLayerByFile() {
        }

        public void actionPerformed(ActionEvent pE) {
            ScanningController.this.mDoseGridPlayer.clearAndRemoveSpots();
            String filename = ScanningController.this.mFileTF.getText();
            ScanningControllerRpcPrepareLayerWithFile_in prepareIn = new ScanningControllerRpcPrepareLayerWithFile_in();
            prepareIn.caller = "TCU Test".getBytes();
            prepareIn.filename = filename.getBytes();

            try {
                ScanningControllerRpcStatusMD5 stat = ScanningController.this.mClient.scanningControllerRpcPrepareWithFile_1(prepareIn);
                ScanningControllerRpcStatus status = stat.rpcStatus;
                if (status.success == 78) {
                    ScanningController.this.mRecorderEvents.addElement("Preparation request failed: " + new String(status.message));
                    return;
                }
            } catch (OncRpcException var6) {
                ScanningController.this.mRecorderEvents.addElement("Rpc exception");
            } catch (IOException var7) {
                ScanningController.this.mRecorderEvents.addElement("IO exception");
            }

        }
    }

    class GetVersion implements ActionListener {
        GetVersion() {
        }

        public void actionPerformed(ActionEvent pE) {
            try {
                String version = ScanningController.this.mClient.getVersion_1();
                ScanningController.this.mRecorderEvents.addElement("SC version is " + version);
            } catch (OncRpcException var3) {
                ScanningController.this.mRecorderEvents.addElement("Rpc exception");
            } catch (IOException var4) {
                ScanningController.this.mRecorderEvents.addElement("IO exception");
            }

        }
    }

    class Connect implements ActionListener {
        Connect() {
        }

        public void actionPerformed(ActionEvent pE) {
            try {
                InetAddress addr = InetAddress.getByName((String)ScanningController.this.mAddressTF.getSelectedItem());
                ScanningController.this.mClient = new ScanningControllerClient(addr, 6);
                ScanningController.this.mRecorderEvents.addElement("Connected on " + addr);
                ScanningController.this.mRecorderEvents.addElement("Connected");
                mFileTF.setEnabled(true);
                mLayerIdTF.setEnabled(true);
                mBeamlineId.setEnabled(false);
                mRefreshFailure.setEnabled(true);
                mKFactor.setEnabled(true);
                getVersion.setEnabled(true);
                selectFile.setEnabled(true);
                setBeamline.setEnabled(true);
                sendByXstream.setEnabled(true);
                start.setEnabled(true);
                cancel.setEnabled(true);
                clearOperational.setEnabled(true);
                clearAbnormal.setEnabled(true);
                clear.setEnabled(true);
                mFailureCause.setEnabled(true);
                mAddressTF.setEnabled(true);
                if (!ScanningController.this.mRefreshThread.isAlive()) {
                    ScanningController.this.mRefreshThread.start();
                }
            } catch (UnknownHostException var4) {
                ScanningController.this.mRecorderEvents.addElement("Unknown host");
            } catch (OncRpcException var5) {
                ScanningController.this.mRecorderEvents.addElement("Rpc exception");
            } catch (IOException var6) {
                ScanningController.this.mRecorderEvents.addElement("IO exception");
            }

        }
    }

    class RefreshState extends Thread {
        RefreshState() {
        }

        public void run() {
            while(ScanningController.this.mRefresh) {
                int state = ScanningController.this.getSCState();
                if (state != -1) {
                    if (state == 0) {
                        for(int i = 0; i < ScanningController.this.mScanningStates.length; ++i) {
                            ScanningController.this.mScanningStates[i].setSelected(false);
                        }
                    }

                    ScanningController.this.mScanningStates[state].setSelected(true);
                }

                if (ScanningController.this.mRefreshFailure.isSelected()) {
                    String text = ScanningController.this.getFailureMessage();
                    //log.warn(text);
                    List<String> strings = new ArrayList<String>();
                    int index = 0;
                    while (index < text.length()) {
                        strings.add(text.substring(index, Math.min(index + 40,text.length())));
                        index += 40;
                    }
                    //ScanningController.this.mFailureCause.setText(ScanningController.this.getFailureMessage());
                    ScanningController.this.mFailureEvents.clear();
                    //ScanningController.this.mFailureEvents.addElement(text);
                    for(String string:strings){
                        ScanningController.this.mFailureEvents.addElement(string);
                    }
                }

                try {
                    sleep(100L);
                } catch (InterruptedException var3) {
                    ScanningController.this.mRecorderEvents.addElement("Exception");
                }
            }

        }
    }

    private class PlayTwoLayers extends Thread {
        private PlayTwoLayers() {
        }

        public void run() {
            while(ScanningController.this.mPlayingAll && ScanningController.this.mDoseGridPlayer.hasNextSample() && ScanningController.this.mDoseGridPlayer2.hasNextSample()) {
                ScanningController.this.mDoseGridPlayer.setCurrentSpot();
                ScanningController.this.mDoseGridPlayer2.setCurrentSpot();

                try {
                    sleep(2L);
                } catch (InterruptedException var2) {
                    ScanningController.this.mRecorderEvents.addElement("Exception");
                }
            }

        }
    }
}