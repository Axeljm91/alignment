/*
* Application : iAlign
* Filename : Controller.java
* Author : Fran�ois Vander Stappen
* Date : 22/10/2010
* Company : IBA
* Version : 0.4.2
*/

package com.iba.ialign;

//AMO

import com.iba.icomp.core.property.PropertyDefinition;
import com.iba.icomp.core.property.PropertyDefinitionDictionary;
import com.iba.pts.bms.bss.esbts.BeamlineSection;
import com.iba.pts.bms.bss.esbts.solution.RangeConverter;
import com.iba.pts.bms.common.IrradiationStatus;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;

import com.iba.blak.device.impl.*;
import com.iba.icomp.core.property.PropertyChangeProvider;
import com.iba.pts.bms.bss.bps.devices.impl.DegraderBeamStopProxy;

import com.iba.pts.bms.bss.controller.api.BssActivityId;
import com.iba.pts.bms.bss.controller.api.BssController.OperatingMode;
//AMO

//import com.iba.blak.config.BeamLine;
import com.iba.blakOverwrite.BeamLine;
import com.iba.blak.config.BeamLineElementNotFoundException;
import com.iba.blak.device.api.*;
//import com.iba.blak.device.api.Insertable;
//import com.iba.blak.device.impl.DegraderLegacyBeamProfileMonitor;
import com.iba.blakOverwrite.DegraderLegacyBeamProfileMonitor;

//import com.iba.blak.device.impl.LegacyBeamProfileMonitorImpl;
import com.iba.blakOverwrite.LegacyBeamProfileMonitorImpl;
import com.iba.tcs.beam.bss.devices.api.Magnet;

//import com.iba.blak.ecubtcu.BlakEcubtcu;
//import com.iba.blak.ecubtcu.BlakEcubtcuImpl;
//import com.iba.blak.feedback.BlakFeedbackClient;
//import com.iba.blak.feedback.BlakRtClient;
import com.iba.device.*;
import com.iba.blak.common.Distribution;

import com.iba.blakOverwrite.BlakEcubtcuFeedbackClient;
import com.iba.blakOverwrite.blakEcubtcuClient;
import com.iba.blakOverwrite.blakNotifFeedbackClient;
import com.iba.blakOverwrite.Beam;

//import com.iba.pts.pms.poss.devices.api.PmsDevice;
//import com.iba.pts.pms.poss.devices.impl.retractable.RenovatedXrayProxy;
import com.opcOpenInterface.Rest;
import org.apache.commons.lang.ArrayUtils;
import org.apache.log4j.Logger;
import org.sikuli.basics.Settings;
import org.sikuli.script.Screen;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import javax.swing.*;
import javax.swing.Timer;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeSupport;
import java.io.*;
import java.lang.reflect.Field;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;


/* This object manages the interface b/w the hardware and the rest of the software
 * For the moment, some parameters are hardcoded => to be externalized in future developments
 */

public class Controller implements PropertyChangeListener, PropertyChangeProvider {
    /**
     * The logger. See http://logging.apache.org/log4j/1.2/index.html for more information.
     */
    private static org.apache.log4j.Logger log = Logger.getLogger(new Throwable().getStackTrace()[0].getClassName());

    private static DegraderLegacyBeamProfileMonitor P1E;
    private static LegacyBeamProfileMonitorImpl P2E;
    static public ContinuousDegrader DEGRADER;
    static public DegraderBeamStopProxy BEAMSTOP;
    static public BeamCurrentMonitor BCM1E;
    public static BeamStop S1E;
    public static BeamStop S2E;
    private static Slit SL1E, SL2E, SL3E;


    // ESS
    private static EcubtcuQuadrupole Q1E, Q2E, Q3E, Q47E, Q56E, Q8E, Q9E, Q10E;
    private static EcubtcuDipole B1234E;
    // TR1
    private static EcubtcuQuadrupole Q1B1, Q2B1, Q3B1, Q1F1, Q2F1, Q3F1, Q1N1, Q2N1;
    private static EcubtcuDipole B12B1;
    // SS1
    private static EcubtcuQuadrupole Q1S1, Q2S1, Q3S1, Q4S1, Q5S1;
    // TR2
    private static EcubtcuQuadrupole Q1B2, Q2B2, Q3B2, Q1F2, Q2F2, Q3F2, Q1N2, Q2N2, Q1I2, Q2I2, Q3I2, Q4I2, Q5I2;
    private static EcubtcuDipole B12B2, B1I2, B2I2;
    // SS2
    private static EcubtcuQuadrupole Q1S2, Q2S2, Q3S2, Q4S2, Q5S2;
    // TR3
    private static EcubtcuQuadrupole Q1B3, Q2B3, Q3B3, Q1F3, Q2F3, Q3F3, Q1I3, Q2I3, Q3I3, Q4I3, Q5I3;
    private static EcubtcuDipole B12B3, B1I3, B2I3;
    // SS3
    private static EcubtcuQuadrupole Q1S3, Q2S3, Q3S3, Q4S3, Q5S3;
    // TR4
    private static EcubtcuQuadrupole Q1B4, Q2B4, Q3B4, Q1G4, Q2G4, Q3G4, Q4G4, Q5G4, Q1N4, Q2N4;
    private static EcubtcuDipole B12B4, B1G4, B2G4;
    private static EcubtcuSteering TRB34E, T1F1, T2F1, T1S1, T2S1, T3S1, T1S2, T2S2, T3S2, T1S3, T2S3, T3S3, TRB2B4, T1B4, T1G4, T2G4;

    private EcubtcuQuadrupole[] ESSQuads = new EcubtcuQuadrupole[]{};
    private EcubtcuDipole[] ESSDipoles = new EcubtcuDipole[]{};
    private EcubtcuQuadrupole[] BP1Quads = new EcubtcuQuadrupole[]{};
    private EcubtcuDipole[] BP1Dipoles = new EcubtcuDipole[]{};
    private EcubtcuQuadrupole[] BP2Quads = new EcubtcuQuadrupole[]{};
    private EcubtcuDipole[] BP2Dipoles = new EcubtcuDipole[]{};
    private EcubtcuQuadrupole[] BP3Quads = new EcubtcuQuadrupole[]{};
    private EcubtcuDipole[] BP3Dipoles = new EcubtcuDipole[]{};
    private EcubtcuQuadrupole[] BP4Quads = new EcubtcuQuadrupole[]{};
    private EcubtcuDipole[] BP4Dipoles = new EcubtcuDipole[]{};
    private EcubtcuQuadrupole[] BP5Quads = new EcubtcuQuadrupole[]{};
    private EcubtcuDipole[] BP5Dipoles = new EcubtcuDipole[]{};
    private EcubtcuQuadrupole[] BP6Quads = new EcubtcuQuadrupole[]{};
    private EcubtcuDipole[] BP6Dipoles = new EcubtcuDipole[]{};

    private EcubtcuQuadrupole[] TR1Quads = new EcubtcuQuadrupole[]{};
    private EcubtcuDipole[] TR1Dipoles = new EcubtcuDipole[]{};
    private EcubtcuSteering[] TR1Steering = new EcubtcuSteering[]{};
    private EcubtcuQuadrupole[] TR4Quads = new EcubtcuQuadrupole[]{};
    private EcubtcuDipole[] TR4Dipoles = new EcubtcuDipole[]{};
    private EcubtcuSteering[] TR4Steering = new EcubtcuSteering[]{};

    private Map<String, BeamlineSection> mBeamlineSections = new HashMap<>();
    private List<BeamlineSection> mSections;
    private BeamlineSection ess = new BeamlineSection(new ArrayList<>(), new ArrayList<>());
    private BeamlineSection fbtr1 = new BeamlineSection(new ArrayList<>(), new ArrayList<>());

    private final Map<String, RangeConverter> mRangeConverter = new HashMap<>();
    private Set<Magnet> mOffMagnets;

    private double G11X, G11Y, G12X, G12Y, G21X, G21Y, G22X, G22Y;
    private double[] mTargets, mTolerances, mSigmaTarget, mSigmaTolerance, mSafeCurrents;
    private double[] mPositions = new double[4];
    private double[] mSigmas = new double[4];
    private double[] mCurrents = new double[4];
    private Status mStatus;
    private SiteManager siteManager;
    private TagManager tagManager;
    private String mNotifServerAddress;
    private Rest restManager;

    static public Field field[];

    private boolean isCommandNotifPort = false;
    private int mNotifPort = 16540;

    private String mEcubtcuAddress;

    private boolean mFilamentWarningDisplayed = false;
    private boolean mDeflectorWarningDisplayed = false;
    private boolean mDeflectorErrorDisplayed = false;

    static public Device llrf;

    private File resultsFileTR1 = new File("");
    private File resultsFileTR4 = new File("");
    private Timer TR1timer = new Timer(200, null);
    private Timer TR4timer = new Timer(200, null);
    private Timer oneHunMils = new Timer(100, null);


    static public Device acu;
    static public Device blpscu;
    static public BcreuHttpDevice bcreu;
    static public BeamLine beamLine = new BeamLine();
    static public blakEcubtcuClient ecubtcu;
    //static public LegacyBeam            legacyBeam  = new LegacyBeam();
    static public Beam beam = new Beam();
    static public blakNotifFeedbackClient feedbackClient = new blakNotifFeedbackClient();

    public static final int BLOCK_BS = 0;
    public static final int BLOCK_BPM = 1;
    public static final int BLOCK_PT = 2;
    //   static private BlakRtClient     rtClient;

    static public int siteID;

    static private FeedbackConnectionListener connectListener = new FeedbackConnectionListener();

    private String siteName, siteCode;

    private static int mScreen;
    private static double mMaxApply, mDeflectorVoltage;
    //private Screen screen = new Screen(mScreen);

    private Color color1;
    private Color color2;
    private Color color3;
    private Color color4;

    public static boolean aligned = false;

    public int burnInStep[] = {0, 125, 135, 145, 155, 165, 175, 185, 190, 195, 190, 85};
    //public int burnInStepArc = 85; // burnInStep[11]

    //private Region measurement = new Region(25,75,800,75);

    public Controller() {
    }

    public static void stopAlignment() {
        aligned = true;
    }

    public static double getMaxApply() {
        return mMaxApply;
    }

    public static Double[] setDeflectorVoltage(Double[] mins) {
        mins[0] = mDeflectorVoltage - 0.2;
        mins[1] = mDeflectorVoltage - 0.5;

        return mins;
    }

    public static void selectBP(int bp) {
        //if (getSelectedBeamline() != bp) {

        //}
    }

    public void setNotifPort(int nPort) {
        mNotifPort = nPort;
        isCommandNotifPort = true;
    }

    public void initialize() {
        this.siteManager = SiteManager.getSiteManager();
        this.tagManager = TagManager.getTagManager();
        log.debug("Loading the tags");
//	    tagManager.buildTagsFromStream(ClassLoader.getSystemResourceAsStream("xml/tags.xml"));

        String pattern = "MM-dd";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        String date = simpleDateFormat.format(new Date());
        resultsFileTR1 = new File("./MagnetDump/BeamlineFeedback-FBTR1-" + date + ".csv");
        resultsFileTR4 = new File("./MagnetDump/BeamlineFeedback-GTR4-" + date + ".csv");

        TR1timer = new Timer(200, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                csvWriteFeedbacksTR1(resultsFileTR1);
            }
        });
        TR1timer.setRepeats(true);

        TR4timer = new Timer(200, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                csvWriteFeedbacksTR4(resultsFileTR4);
            }
        });
        TR4timer.setRepeats(true);

        try {
            Resource res = new ClassPathResource("xml/tags.xml");
            tagManager.buildTagsFromStream(res.getInputStream());
        } catch (IOException e) {
            log.info("Read file error: " + e.getMessage());
            return;
        }

        try {
            propertySourcesPlaceholderConfigurer();
            log.info("Site-config jar read in successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        log.debug("Loading the sites");

        Site firstSite;

//		FileInputStream siteConfig = new FileInputStream("./siteconfig.xml");
        try {
//			FileInputStream siteConfig = new FileInputStream("./siteconfig-local.xml");
            FileInputStream siteConfig = new FileInputStream("./siteconfig.xml");
            //	Resource res = new ClassPathResource("sites/siteconfig.xml");
            //	siteManager.buildSitesFromStream(res.getInputStream());
            siteManager.buildSitesFromStream(siteConfig);
            if (siteManager.getSites().size() > 1) {
                log.info("there are more than 1 site in the siteConfig.xml, the default site would be the first site in the file.");
            }

            firstSite = siteManager.getFirstSite();

            siteName = firstSite.getName();
            siteID = firstSite.getID();
            siteCode = firstSite.getCode();
            G11X = firstSite.getG11x();
            G12X = firstSite.getG12x();
            G21X = firstSite.getG21x();
            G22X = firstSite.getG22x();
            G11Y = firstSite.getG11y();
            G12Y = firstSite.getG12y();
            G21Y = firstSite.getG21y();
            G22Y = firstSite.getG22y();
            mNotifServerAddress = firstSite.getNotifServerAddress();
            if (!isCommandNotifPort) {
                mNotifPort = firstSite.getNotifPort();
                log.info("get notif port from siteConfig.xml which is " + mNotifPort);
            } else {
                log.info("get notif port from command line which is " + mNotifPort);
            }

            mEcubtcuAddress = firstSite.getEcubtcuAddress();
            mTargets = firstSite.getBpmTargets();
            mTolerances = firstSite.getBpmTolerances();
            mSigmaTarget = firstSite.getSigmaTargets();
            mSigmaTolerance = firstSite.getSigmaTolerances();
            mScreen = firstSite.getScreen();
            mSafeCurrents = firstSite.getSafeCurrents();
            mMaxApply = firstSite.getMaxCurrentChange();
            mDeflectorVoltage = firstSite.getDeflectorVoltage();

        } catch (FileNotFoundException e) {
            log.info("Cannot find the file siteConfig.xml.");
            return;
        }

        log.debug("Loading beamline from " + firstSite.getBeamLine());


        try {
            beamLine.loadFromFile(firstSite.getBeamLine());

            ecubtcu = new blakEcubtcuClient();
            ecubtcu.setEcubtcuAddress(mEcubtcuAddress);
            //            Resource ecubtcuResource = new ClassPathResource("x/ecubtcu" + siteCode + ".x");
            Resource ecubtcuResource = new ClassPathResource("x/ecubtcu.x");
            ((blakEcubtcuClient) ecubtcu).setRpcProgrameFile(ecubtcuResource);

            feedbackClient.setNotifServerAddress(mNotifServerAddress);
            feedbackClient.setNotifServerPort(mNotifPort);
            setFeedbackClient();
        } catch (FileNotFoundException e) {
            log.error("File not found : " + firstSite.getBeamLine());
        } catch (IOException e) {
            log.error("Error reading " + firstSite.getBeamLine() + " file.");
        } catch (Exception e) {
            log.error("set rpc program file error");
            e.printStackTrace();
        }


        //llrf = siteManager.getSite(siteName).getDevice("llrf");

        acu = siteManager.getSite(siteName).getDevice("acu230");
        //blpscu = siteManager.getSite(siteName).getDevice("blpscu");

        bcreu = (BcreuHttpDevice) siteManager.getSite(siteName).getDevice("bcreu-http");

//        if(!acu.isConnected()) {
//            acu.connect();
//        }

        //if(!blpscu.isConnected()) {
        //    blpscu.connect();
        //}

        bcreu.connect();


        try {
            P1E = (DegraderLegacyBeamProfileMonitor) beamLine.getElement("P1E");
            P2E = (LegacyBeamProfileMonitorImpl) beamLine.getElement("P2E");
            DEGRADER = (ContinuousDegrader) beamLine.getElement("DEGRADER");
            //BEAMSTOP = (DegraderBeamStopProxy) beamLine.getElement("DEGRADER");
            //BCM1E = (BeamCurrentMonitor) beamLine.getElement("BCM1E");
            //S1E = (BeamStop) beamLine.getElement("S1E");
            S2E = (BeamStop) beamLine.getElement("S2E");
            SL1E = (Slit) beamLine.getElement("SL1E");
            SL2E = (Slit) beamLine.getElement("SL2E");
            SL3E = (Slit) beamLine.getElement("SL3E");

            // ESS magnets
            Q1E = (EcubtcuQuadrupole) beamLine.getElement("Q1E");
            Q2E = (EcubtcuQuadrupole) beamLine.getElement("Q2E");
            Q3E = (EcubtcuQuadrupole) beamLine.getElement("Q3E");
            Q47E = (EcubtcuQuadrupole) beamLine.getElement("Q47E");
            Q56E = (EcubtcuQuadrupole) beamLine.getElement("Q56E");
            Q8E = (EcubtcuQuadrupole) beamLine.getElement("Q8E");
            Q9E = (EcubtcuQuadrupole) beamLine.getElement("Q9E");
            Q10E = (EcubtcuQuadrupole) beamLine.getElement("Q10E");

            B1234E = (EcubtcuDipole) beamLine.getElement("B1234E");

            ESSDipoles = new EcubtcuDipole[]{B1234E};
            ESSQuads = new EcubtcuQuadrupole[]{Q1E, Q2E, Q3E, Q47E, Q56E, Q8E, Q9E, Q10E};

            // TR 1 magnets
            Q1B1 = (EcubtcuQuadrupole) beamLine.getElement("Q1B1");
            Q2B1 = (EcubtcuQuadrupole) beamLine.getElement("Q2B1");
            Q3B1 = (EcubtcuQuadrupole) beamLine.getElement("Q3B1");
            Q1F1 = (EcubtcuQuadrupole) beamLine.getElement("Q1F1");
            Q2F1 = (EcubtcuQuadrupole) beamLine.getElement("Q2F1");
            Q3F1 = (EcubtcuQuadrupole) beamLine.getElement("Q3F1");
            Q1N1 = (EcubtcuQuadrupole) beamLine.getElement("Q1N1");
            Q2N1 = (EcubtcuQuadrupole) beamLine.getElement("Q2N1");

            B12B1 = (EcubtcuDipole) beamLine.getElement("B12B1");

            BP1Quads = new EcubtcuQuadrupole[]{Q1B1, Q2B1, Q3B1, Q1F1, Q2F1, Q3F1, Q1N1, Q2N1};
            BP1Dipoles = new EcubtcuDipole[]{B12B1};

            // SS 1 magnets
            Q1S1 = (EcubtcuQuadrupole) beamLine.getElement("Q1S1");
            Q2S1 = (EcubtcuQuadrupole) beamLine.getElement("Q2S1");
            Q3S1 = (EcubtcuQuadrupole) beamLine.getElement("Q3S1");
            Q4S1 = (EcubtcuQuadrupole) beamLine.getElement("Q4S1");
            Q5S1 = (EcubtcuQuadrupole) beamLine.getElement("Q5S1");

            // TR 2 magnets
            Q1B2 = (EcubtcuQuadrupole) beamLine.getElement("Q1B2");
            Q2B2 = (EcubtcuQuadrupole) beamLine.getElement("Q2B2");
            Q3B2 = (EcubtcuQuadrupole) beamLine.getElement("Q3B2");
            Q1F2 = (EcubtcuQuadrupole) beamLine.getElement("Q1F2");
            Q2F2 = (EcubtcuQuadrupole) beamLine.getElement("Q2F2");
            Q3F2 = (EcubtcuQuadrupole) beamLine.getElement("Q3F2");
            Q1I2 = (EcubtcuQuadrupole) beamLine.getElement("Q1I2");
            Q2I2 = (EcubtcuQuadrupole) beamLine.getElement("Q2I2");
            Q3I2 = (EcubtcuQuadrupole) beamLine.getElement("Q3I2");
            Q4I2 = (EcubtcuQuadrupole) beamLine.getElement("Q4I2");
            Q5I2 = (EcubtcuQuadrupole) beamLine.getElement("Q5I2");
            Q1N2 = (EcubtcuQuadrupole) beamLine.getElement("Q1N2");
            Q2N2 = (EcubtcuQuadrupole) beamLine.getElement("Q2N2");

            B12B2 = (EcubtcuDipole) beamLine.getElement("B12B2");
            B1I2 = (EcubtcuDipole) beamLine.getElement("B1I2");
            B2I2 = (EcubtcuDipole) beamLine.getElement("B2I2");

            BP2Quads = new EcubtcuQuadrupole[]{Q1S1, Q2S1, Q3S1, Q4S1, Q5S1, Q1B2, Q2B2, Q3B2, Q1F2, Q2F2, Q3F2, Q1N2, Q2N2};
            BP2Dipoles = new EcubtcuDipole[]{B12B2};
            BP3Quads = new EcubtcuQuadrupole[]{Q1S1, Q2S1, Q3S1, Q4S1, Q5S1, Q1B2, Q2B2, Q3B2, Q1I2, Q2I2, Q3I2, Q4I2, Q5I2, Q1N2, Q2N2};
            BP3Dipoles = new EcubtcuDipole[]{B12B2, B1I2, B2I2};

            // SS 2 magnets
            Q1S2 = (EcubtcuQuadrupole) beamLine.getElement("Q1S2");
            Q2S2 = (EcubtcuQuadrupole) beamLine.getElement("Q2S2");
            Q3S2 = (EcubtcuQuadrupole) beamLine.getElement("Q3S2");
            Q4S2 = (EcubtcuQuadrupole) beamLine.getElement("Q4S2");
            Q5S2 = (EcubtcuQuadrupole) beamLine.getElement("Q5S2");

            // TR 3 magnets
            Q1B3 = (EcubtcuQuadrupole) beamLine.getElement("Q1B3");
            Q2B3 = (EcubtcuQuadrupole) beamLine.getElement("Q2B3");
            Q3B3 = (EcubtcuQuadrupole) beamLine.getElement("Q3B3");
            Q1F3 = (EcubtcuQuadrupole) beamLine.getElement("Q1F3");
            Q2F3 = (EcubtcuQuadrupole) beamLine.getElement("Q2F3");
            Q3F3 = (EcubtcuQuadrupole) beamLine.getElement("Q3F3");
            Q1I3 = (EcubtcuQuadrupole) beamLine.getElement("Q1I3");
            Q2I3 = (EcubtcuQuadrupole) beamLine.getElement("Q2I3");
            Q3I3 = (EcubtcuQuadrupole) beamLine.getElement("Q3I3");
            Q4I3 = (EcubtcuQuadrupole) beamLine.getElement("Q4I3");
            Q5I3 = (EcubtcuQuadrupole) beamLine.getElement("Q5I3");

            B12B3 = (EcubtcuDipole) beamLine.getElement("B12B3");
            B1I3 = (EcubtcuDipole) beamLine.getElement("B1I3");
            B2I3 = (EcubtcuDipole) beamLine.getElement("B2I3");

            BP4Quads = new EcubtcuQuadrupole[]{Q1S1, Q2S1, Q3S1, Q4S1, Q5S1, Q1S2, Q2S2, Q3S2, Q4S2, Q5S2, Q1B3, Q2B3, Q3B3, Q1F3, Q2F3, Q3F3};
            BP4Dipoles = new EcubtcuDipole[]{B12B3};
            BP5Quads = new EcubtcuQuadrupole[]{Q1S1, Q2S1, Q3S1, Q4S1, Q5S1, Q1S2, Q2S2, Q3S2, Q4S2, Q5S2, Q1B3, Q2B3, Q3B3, Q1I3, Q2I3, Q3I3, Q4I3, Q5I3};
            BP5Dipoles = new EcubtcuDipole[]{B12B3, B1I3, B2I3};

            // SS 3 magnets
            Q1S3 = (EcubtcuQuadrupole) beamLine.getElement("Q1S3");
            Q2S3 = (EcubtcuQuadrupole) beamLine.getElement("Q2S3");
            Q3S3 = (EcubtcuQuadrupole) beamLine.getElement("Q3S3");
            Q4S3 = (EcubtcuQuadrupole) beamLine.getElement("Q4S3");
            Q5S3 = (EcubtcuQuadrupole) beamLine.getElement("Q5S3");

            // TR 4 magnets
            Q1B4 = (EcubtcuQuadrupole) beamLine.getElement("Q1B4");
            Q2B4 = (EcubtcuQuadrupole) beamLine.getElement("Q2B4");
            Q3B4 = (EcubtcuQuadrupole) beamLine.getElement("Q3B4");
            Q1G4 = (EcubtcuQuadrupole) beamLine.getElement("Q1G4");
            Q2G4 = (EcubtcuQuadrupole) beamLine.getElement("Q2G4");
            Q3G4 = (EcubtcuQuadrupole) beamLine.getElement("Q3G4");
            Q4G4 = (EcubtcuQuadrupole) beamLine.getElement("Q4G4");
            Q5G4 = (EcubtcuQuadrupole) beamLine.getElement("Q5G4");
            Q1N4 = (EcubtcuQuadrupole) beamLine.getElement("Q1N4");
            Q2N4 = (EcubtcuQuadrupole) beamLine.getElement("Q2N4");

            B12B4 = (EcubtcuDipole) beamLine.getElement("B12B4");
            B1G4 = (EcubtcuDipole) beamLine.getElement("B1G4");
            B2G4 = (EcubtcuDipole) beamLine.getElement("B2G4");

            BP6Quads = new EcubtcuQuadrupole[]{Q1S1, Q2S1, Q3S1, Q4S1, Q5S1, Q1S2, Q2S2, Q3S2, Q4S2, Q5S2, Q1S3, Q2S3, Q3S3, Q4S3, Q5S3, Q1B4, Q2B4, Q3B4, Q1G4, Q2G4, Q3G4, Q4G4, Q5G4, Q1N4, Q2N4};
            BP6Dipoles = new EcubtcuDipole[]{B12B4, B1G4, B2G4};

            // Trim/steering magnets TR1/TR4 full
            TRB34E = (EcubtcuSteering) beamLine.getElement("TRB34E");
            T1F1 = (EcubtcuSteering) beamLine.getElement("T1F1");
            T2F1 = (EcubtcuSteering) beamLine.getElement("T2F1");
            T1S1 = (EcubtcuSteering) beamLine.getElement("T1S1");
            T2S1 = (EcubtcuSteering) beamLine.getElement("T2S1");
            T3S1 = (EcubtcuSteering) beamLine.getElement("T3S1");
            T1S2 = (EcubtcuSteering) beamLine.getElement("T1S2");
            T2S2 = (EcubtcuSteering) beamLine.getElement("T2S2");
            T3S2 = (EcubtcuSteering) beamLine.getElement("T3S2");
            T1S3 = (EcubtcuSteering) beamLine.getElement("T1S3");
            T2S3 = (EcubtcuSteering) beamLine.getElement("T2S3");
            T3S3 = (EcubtcuSteering) beamLine.getElement("T3S3");
            TRB2B4 = (EcubtcuSteering) beamLine.getElement("TRB2B4");
            T1B4 = (EcubtcuSteering) beamLine.getElement("T1B4");
            T1G4 = (EcubtcuSteering) beamLine.getElement("T1G4");
            T2G4 = (EcubtcuSteering) beamLine.getElement("T2G4");

            TR1Steering = new EcubtcuSteering[]{TRB34E, T1F1, T2F1};
            TR1Quads = (EcubtcuQuadrupole[]) ArrayUtils.addAll(ESSQuads, BP1Quads);
            TR1Dipoles = (EcubtcuDipole[]) ArrayUtils.addAll(ESSDipoles, BP1Dipoles);

            TR4Steering = new EcubtcuSteering[]{TRB34E, T1S1, T2S1, T3S1, T1S2, T2S2, T3S2, T1S3, T2S3, T3S3, TRB2B4, T1B4, T1G4, T2G4};
            TR4Quads = (EcubtcuQuadrupole[]) ArrayUtils.addAll(ESSQuads, BP6Quads);
            TR4Dipoles = (EcubtcuDipole[]) ArrayUtils.addAll(ESSDipoles, BP6Dipoles);


            mBeamlineSections.put("ESS", ess);
            mBeamlineSections.put("FBTR1", fbtr1);


            // mSections.add(ess);
            // mSections.add(fbtr1);

            // Beamline mBeamlineBP1 = new Beamline("FBTR1", 1, 1, mRangeConverter, mSections, mOffMagnets);


        } catch (BeamLineElementNotFoundException e) {
            log.error("Not all necessary elements are defined: " + e);
        }

        field = Controller.class.getDeclaredFields();

        //updateBurnInSteps();

        restManager = new Rest();

        log.info("Aye aye, Sir. I'll align your beam !");
        mStatus = new Status();
    }

    private void updateBurnInSteps() {
        //burnInStep = new int[19];
//	    for (int i = 1; i <= 18; i++ ){
//	        burnInStep[i] = 0;
//        }

//        burnInStep[0]  = 0;
//        burnInStep[1]  = 125;
//        burnInStep[2]  = 135;
//        burnInStep[3]  = 145;
//        burnInStep[4]  = 155;
//        burnInStep[5]  = 165;
//        burnInStep[6]  = 175;
//        burnInStep[7]  = 185;
//        burnInStep[8]  = 190;
//        burnInStep[9]  = 195;
//        burnInStep[10] = 190;
//        burnInStep[11] = 85;
    }

    public Status checkBcreu() {
        String str = bcreu.getBcreuConnection();
        //Changed from "Remote: Hardwired" to make GUI box GREEN
        //Had to modify if(bool) below too 6Mar18 -AMO
        Boolean bool = str.equalsIgnoreCase("Remote: Ethernet");
        mStatus.set(Status.BCREU_HW, str, bool);
        if (bool) {
            str = bcreu.getPulseSource();
            //str = bcreu.getPulseSource(beam.isSinglePulseMode());
            if (Gui.mEsbtsPanel.mSelectBeamlinePanel.mContinuousBeamCB.isSelected()) {
                mStatus.set(Status.PULSESOURCE, str, str.equalsIgnoreCase("TR3"));
            }else{
                mStatus.set(Status.PULSESOURCE, str, str.equalsIgnoreCase("Internal: Continuous") || str.equalsIgnoreCase("Internal: Single"));
            }
        } else {
            str = bcreu.getPulseSource(beam.isSinglePulseMode());
            //str = bcreu.getPulseSource();
            mStatus.set(Status.PULSESOURCE, str, str.equalsIgnoreCase("Internal: Continuous"));
        }
        mStatus.set(Status.BCREU_STATE, bcreu.getRunningState(), bcreu.getRunningStateColor(false));
        mStatus.set(Status.MAX_BEAM, bcreu.getMaxBeam(), Double.parseDouble(Gui.getEsbtsPanel().mSelectBeamlinePanel.mBeamCurrentTextField.getText()) - 0.3d, Double.parseDouble(Gui.getEsbtsPanel().mSelectBeamlinePanel.mBeamCurrentTextField.getText()) + 0.3d);
        mStatus.set(Status.IC_CYCLO, bcreu.getIcCyclo(), (Double.parseDouble(Gui.getEsbtsPanel().mSelectBeamlinePanel.mBeamCurrentTextField.getText())*0.4d) - 0.5d, Double.parseDouble(Gui.getEsbtsPanel().mSelectBeamlinePanel.mBeamCurrentTextField.getText()) + 0.5d);


        return mStatus;
    }

    public Distribution[] getBpmDistributions() {
        Distribution[] dist = new Distribution[4];
        dist[0] = P1E.getHorizontalDistribution();
        //log.error("P1E hori max Y: " + String.valueOf(dist[0].getMaximumY()));
        dist[1] = P1E.getVerticalDistribution();
        //log.error("P1E vert max Y: " + String.valueOf(dist[1].getMaximumY()));
        dist[2] = P2E.getHorizontalDistribution();
        //log.error("P2E hori max Y: " + String.valueOf(dist[2].getMaximumY()));
        dist[3] = P2E.getVerticalDistribution();
        //log.error("P2E vert max Y: " + String.valueOf(dist[3].getMaximumY()));

//        if ((dist[0].getMaximumY() < 1) || (dist[1].getMaximumY() < 1) || (dist[2].getMaximumY() < 1) || (dist[3].getMaximumY() < 1)){
//            Distribution[] dist2 = new Distribution[4];
//            dist[0] = P1E.getHorizontalDistribution();
//            log.error("P1E hori max Y: " + String.valueOf(dist[0].getMaximumY()));
//            dist[1] = P1E.getVerticalDistribution();
//            log.error("P1E vert max Y: " + String.valueOf(dist[1].getMaximumY()));
//            dist[2] = P2E.getHorizontalDistribution();
//            log.error("P2E hori max Y: " + String.valueOf(dist[2].getMaximumY()));
//            dist[3] = P2E.getVerticalDistribution();
//            log.error("P2E vert max Y: " + String.valueOf(dist[3].getMaximumY()));
//
//            return dist2;
//        }

        return dist;
    }

    /* Should connect to ECUBTCU prior to calling check() */
    public Status checkStatus() {
//        if (!feedbackClient.isInitialized()) {
        //           feedbackClient.retreiveMcrFeedbacks();
        //      }

        //feedbackClient.retreiveMcrFeedbacks();

        // BeamLine Elements
        //mStatus.set(Status.P1E_STATUS, P1E);
        mStatus.set(Status.S2E_STATUS, S2E.getPosition());
        mStatus.set(Status.SL1E_STATUS, SL1E.getPosisionSetpoint(), Double.parseDouble(Gui.getEsbtsPanel().mSelectBeamlinePanel.mSLE1TextField.getText()) - 1.0d, Double.parseDouble(Gui.getEsbtsPanel().mSelectBeamlinePanel.mSLE1TextField.getText()) + 1.0d);
        mStatus.set(Status.SL2E_STATUS, SL2E.getPosisionSetpoint(), Double.parseDouble(Gui.getEsbtsPanel().mSelectBeamlinePanel.mSLE2TextField.getText()) - 1.0d, Double.parseDouble(Gui.getEsbtsPanel().mSelectBeamlinePanel.mSLE2TextField.getText()) + 1.0d);
        mStatus.set(Status.SL3E_STATUS, SL3E.getPosisionSetpoint(), Double.parseDouble(Gui.getEsbtsPanel().mSelectBeamlinePanel.mSLE3TextField.getText()) - 1.0d, Double.parseDouble(Gui.getEsbtsPanel().mSelectBeamlinePanel.mSLE3TextField.getText()) + 1.0d);
        try {
            Boolean bval = restManager.getVariable(Status.SMPS_ON).get("value").getAsBoolean();
            mStatus.set(Status.SCAN_MAGNETS, bval ? "Standby" : "ON", !bval);
            //log.warn(restManager.getVariable(Status.SMPS_ON).get("value").getAsBoolean());
        } catch (Exception e) {
            e.printStackTrace();
        }
        //mStatus.set(Status.P2E_STATUS, P2E);
        //Double dval = Math.abs(Q1E.getCurrentFeedback()) + Math.abs(Q2E.getCurrentFeedback()) + Math.abs(Q3E.getCurrentFeedback());
        //Boolean bval = dval > 1;
        //mStatus.set(Status.ESS_MAGNETS, bval ? "ON" : "OFF", !bval);


        //mCurrents = getCurrents();
        // Magnets
//        for (int i = 0; i < Status.Magnet_names.length; i++) {
//            mStatus.set(i + Status.MAGNET_OFFSET, mCurrents[i], (Boolean) acu.getTagValue(Status.Magnet_reg[i]));
//        }
//
//        for (int i = 0; i < Status.Cyclo_read.length; i++) {
//            mStatus.set(i + Status.CYCLO_OFFSET, (Double) acu.getTagValue(Status.Cyclo_read[i]));
//        }
//
//        //MC tuning tab
//        for (int i = 0; i < Status.CycloTuning_read.length; i++) {
//            mStatus.set(i + Status.CYCLOTUNING_OFFSET, (Double) acu.getTagValue(Status.CycloTuning_read[i]));
//        }
//
//        for (int i = 0; i < Status.LLRF_read.length; i++) {
//            mStatus.set(i + Status.LLRFTUNING_OFFSET, (Double) acu.getTagValue(Status.LLRF_read[i]));
//        }

        // For filament resistance calculation -AMO
        //mStatus.set(Status.CYCLO_OFFSET+1, ((Double)acu.getTagValue((Status.Cyclo_read[4]))/((Double)acu.getTagValue(Status.Cyclo_read[2]))));
//
//
// if (llrf.isConnected()) {
//            int i = Status.Cyclo_read.length;
//            mStatus.set(i + Status.CYCLO_OFFSET, (Double) llrf.getTagValue("V1 DEE voltage"));
//            i++;
//            mStatus.set(i + Status.CYCLO_OFFSET, (Double) llrf.getTagValue("V2 DEE voltage"));
//        }

        //mStatus.set(Status.DF_CURRENT, (Double) acu.getTagValue(Status.DF_current));

        return mStatus;
    }

    public Status getStatus() {
        return mStatus;
    }

    // Prepare the system for treating patients again.
    public void prepareForTreatment() {
        try {
            if (!ecubtcu.isConnected()) {
                ecubtcu.connect();
                Thread.sleep(100);
            }
            //           ecubtcu.iseuRequestSetEndOfTreatmentMode();
//            if (P2E.getPosition() != Insertable.Position.RETRACTED) {
//                ecubtcu.bpmRetract("P2E");
//            }

//            if (bcreu.isConnected()){
//                bcreu.disconnect();
//            }

            // Added for 30 degree beamlines, set-range will fail
            // without explicit in command on S2E since there is no TR BS at 30
            ecubtcu.bsInsert("S2E");

//            beam.bpsController.startIdleActivity();
//            beam.bpsController.proxyPublish();


        } catch (EcubtcuCommandException e) {
            log.error(e);
        } catch (EcubtcuNotConnectedException e) {
            log.error(e);
            //} catch (EcubtcuException e) {
            // log.error(e);
        } catch (InterruptedException e) {
            log.error(e);
        }

    }


    public void prepareForAlignment() {
        try {
            if (!ecubtcu.isConnected()) {
                ecubtcu.connect();
                Thread.sleep(100);
            }
            if (!bcreu.isConnected()) {
                bcreu.connect();
                Thread.sleep(100);
            }
            if (!mStatus.getBool(Status.P1E_STATUS)) {
                if (mStatus.getString(Status.P1E_STATUS).equalsIgnoreCase("UNKNOWN")) {
//                    P1E.getDegrader().goHome();
                    ecubtcu.degraderGoHome();
                } else {
                    //                   P1E.insert();
                    ecubtcu.degraderGoSpecialBlock(BLOCK_BPM);
                }
            }
            if (!mStatus.getBool(Status.SL1E_STATUS)) {
//                SL1E.setPosition(40.0d);
                ecubtcu.slitsGoMm("SL1E", 50.0);
            }
            if (!mStatus.getBool(Status.SL2E_STATUS)) {
//                SL2E.setPosition(40.0d);
                ecubtcu.slitsGoMm("SL2E", 50.0);
            }
            //if (!mStatus.getBool(Status.P2E_STATUS)) {
//                P2E.insert();
            //	ecubtcu.bpmInsert("P2E");

            //}
            // for this part, need to clear with site how to prepare beam. please refer to the code in BCP.
            if (!beam.bcreu.isLookupValid()) {
                log.info("Performing BCREU/ISEU lookup");
                beam.bpsController.startPrepareActivity(-1, 10.0);
                beam.bpsController.proxyPublish();
                //beam.bcreu.startBeamPulses();
                //had to add for PTS-8.6.5+ for prs-103037
                while (bcreu.getRunningState() != "Regulating") {
                    Thread.sleep(500);
                }
                beam.bcreu.setContinuousPulse(true);
                beam.bcreu.proxyPublish();

/*               beam.requestMaxBeamCurrent(-1, 10.0);
                 log.info("prepare bcreu/isue lookup with 10mA for mcr.");
                 beam.bcreu.setContinuousPulse(true);
                 beam.bcreu.startBeamPulses();

                 ecubtcu.iseuRequestSetEndOfTreatmentMode();
                 beam.bcreu.setTreatmentMode(null);
                 ecubtcu.iseuRequestSetCurrentAtCycloExit(10.0d);
                 ecubtcu.iseuRequestSetLookupMode();

                  beam.bcreu.setMaxBeamCurrent(10);
                  beam.bcreu.setRoomId(-1);
                                  beam.bcreu.proxyPublish();


                  Thread.sleep(2000);
                  ecubtcu.iseuRequestSetInternalPulseMode();
                  ecubtcu.iseuRequestSetSinglePulseMode(false);
                  beam.bcreu.setContinuousPulse(false);
                  beam.bcreu.proxyPublish();
                  beam.bcreu.stopBeamPulses();
*/

            }
/*          else if (!mStatus.getBool(Status.PULSESOURCE))
            {
                ecubtcu.iseuRequestSetInternalPulseMode();
               ecubtcu.iseuRequestSetSinglePulseMode(false);
            }
*/

            if (!mStatus.getBool(Status.S2E_STATUS)) {
                //S2E.retract();
                //ecubtcu.bsInsert("S2E");
                ecubtcu.bsRetract("S2E");

//                   log.warn("Retracting S2E");
//
//                   Thread.sleep(3500);

//                if (!mStatus.getBool(Status.S2E_STATUS)) {
//                    if (!beam.bcreu.isLookupValid()) {
//                        log.warn("RF spark detected, setting to IDLE");
//                        beam.bpsController.startIdleActivity();
//                        beam.bpsController.proxyPublish();
//
//                        Thread.sleep(5000);
//
//                        log.warn("Performing BCREU/ISEU lookup");
//                        beam.bpsController.startPrepareActivity(-1, 10.0);
//                        beam.bpsController.proxyPublish();
//                    }
//                }

            }
        } catch (EcubtcuException e) {
            log.error(e);
        } catch (InterruptedException e) {
            log.error(e);
        } finally {

        }
    }


    public void prepareForTune() {
        try {
            if (!ecubtcu.isConnected()) {
                ecubtcu.connect();
                Thread.sleep(100);
            }
            if (!bcreu.isConnected()) {
                bcreu.connect();
                Thread.sleep(100);
            }

            ecubtcu.degraderGoSpecialBlock(BLOCK_BS);

            log.info("[TUNE] Moving degrader to beamstop.");

            while (DEGRADER.getStatus() != Degrader.STATUS_AT_BS) {
                Thread.sleep(100);
            }

            log.info("[TUNE] Degrader has reached beamstop position.");

            //Set max voltage to satisfy LLRF proxy req
            if (beam.llrf.getMaxVoltage() != 56) {
                beam.llrf.setMaxVoltage(56);
                log.warn("[TUNE] LLRF max voltage set to " + beam.llrf.getMaxVoltage() + "kV");
            }

            //log.warn(beam.llrf.isRfOn());
            //log.warn(beam.llrf.isRfStandby());

            //Set VDee2 voltage
            if (beam.llrf.getDeeVoltage2() < 55.8) {
                beam.llrf.setDeeVoltage2(56.00);
                log.warn("[TUNE] VDee2 set to 56.00kV");
                beam.llrf.proxyPublish();
            }

            //Read back VDee2 voltage
            if (beam.llrf.getDeeVoltage2() > 55.8) {
                log.warn("[TUNE] VDee2 is at " + beam.llrf.getDeeVoltage2() + "kV");
            }

            //Measure beam current on S1E

        } catch (EcubtcuCommandException e) {
            log.error(e);
        } catch (EcubtcuNotConnectedException e) {
            log.error(e);
        } catch (InterruptedException e) {
            log.error(e);
        }
    }

    public void S1EvsICC(double beamCurrent) {
        try {
            if (!ecubtcu.isConnected()) {
                ecubtcu.connect();
                Thread.sleep(100);
            }
            if (!bcreu.isConnected()) {
                bcreu.connect();
                Thread.sleep(100);
            }

            ecubtcu.degraderGoSpecialBlock(BLOCK_BS);

            log.info("[S1EvsICC] Moving degrader to beamstop.");

            while (DEGRADER.getStatus() != Degrader.STATUS_AT_BS) {
                Thread.sleep(100);
            }

            log.info("[S1EvsICC] Degrader has reached beamstop position.");

            beam.bpsController.startPrepareActivity(-1, beamCurrent);

            log.info("[S1EvsICC] Preparing BCREU for " + beamCurrent + "nA, TRNB: -1");

            //Set max voltage to satisfy LLRF proxy req
//            if (beam.llrf.getMaxVoltage() != 56) {
//                beam.llrf.setMaxVoltage(56);
//                log.warn("[S1EvsICC] LLRF max voltage set to " + beam.llrf.getMaxVoltage() + "kV");
//            }

            //log.warn(beam.llrf.isRfOn());
            //log.warn(beam.llrf.isRfStandby());

            //Set VDee2 voltage
//            if (beam.llrf.getDeeVoltage2() < 55.8) {
//                beam.llrf.setDeeVoltage2(56.00);
//                log.warn("[TUNE] VDee2 set to 56.00kV");
//                beam.llrf.proxyPublish();
//            }

            //Read back VDee2 voltage
//            if (beam.llrf.getDeeVoltage2() > 55.8){
//                log.warn("[TUNE] VDee2 is at " + beam.llrf.getDeeVoltage2() + "kV");
//            }

            //Measure beam current on S1E

        } catch (EcubtcuCommandException e) {
            log.error(e);
        } catch (EcubtcuNotConnectedException e) {
            log.error(e);
        } catch (InterruptedException e) {
            log.error(e);
        }
    }


    /**
     * arraySubtract performs an element-by-element subtraction of two arrays of the same size.
     *
     * @param a Minuend
     * @param b Subtrahend
     * @return Element-by-Element Difference (a[i] - b[i])
     * @throws ArrayIndexOutOfBoundsException if the two arrays differ in size
     */
    public double[] arraySubtract(double[] a, double[] b) throws ArrayIndexOutOfBoundsException {
        if (a.length == b.length) {
            double[] c = new double[a.length];
            for (int i = 0; i < a.length; i++) {
                c[i] = a[i] - b[i];
            }
            return c;
        } else {
            throw new ArrayIndexOutOfBoundsException("Arrays differ in size.");
        }
    }

    public double[] getPositions() {
        return mPositions;
    }

    public double[] getSigmas() {
        return mSigmas;
    }

    public double[] getTargets() {
        return mTargets;
    }

    public double[] getTolerances() {
        return mTolerances;
    }

    public double[] getSigmaTargets() {
        return mSigmaTarget;
    }

    public double[] getSigmaTolerances() {
        return mSigmaTolerance;
    }

    public double[] getSafeCurrents() {
        return mSafeCurrents;
    }

    public double[] getAdjustedPositions() {
        return arraySubtract(mPositions, mTargets);
    }

    //Legacy with BPM inversion
    public double[] getOperands() {
        double[] adjMeans = getAdjustedPositions();


        double[] operands = new double[4];
        operands[0] = (adjMeans[1] - G21X * (adjMeans[3] - (G12X * adjMeans[1] / G11X)) / ((-G12X * G21X / G11X) + G22X)) / G11X;
        operands[1] = ((adjMeans[0] - G21Y * (adjMeans[2] - (G12Y * adjMeans[0] / G11Y)) / ((-G12Y * G21Y / G11Y) + G22Y)) / G11Y);
        operands[2] = ((adjMeans[3] - (G12X * adjMeans[1] / G11X)) / ((-G12X * G21X / G11X) + G22X));
        operands[3] = (adjMeans[2] - (G12Y * adjMeans[0] / G11Y)) / ((-G12Y * G21Y / G11Y) + G22Y);

        return operands;
    }


    //No BPM inversion
//    public double[] getOperands(){
//        double[] adjMeans = getAdjustedPositions();
//
//        double[] operands = new double[4];
//        operands[0] = (adjMeans[0] - G21X*(adjMeans[2] - (G12X*adjMeans[0]/G11X))/((-G12X*G21X/G11X)+G22X))/G11X;
//        operands[1] = -((adjMeans[1] - G21Y*(adjMeans[3] - (G12Y*adjMeans[1]/G11Y))/((-G12Y*G21Y/G11Y)+G22Y))/G11Y);
//        operands[2] = -((adjMeans[2] - (G12X*adjMeans[0]/G11X))/((-G12X*G21X/G11X)+G22X));
//        operands[3] = (adjMeans[3] - (G12Y*adjMeans[1]/G11Y))/((-G12Y*G21Y/G11Y)+G22Y);
//
//        return operands;
//    }


    public double[] computeCurrents() {
        double[] operands = getOperands();

        log.debug("Diff in T1X = " + operands[0]);
        log.debug("Diff in T1Y = " + operands[1]);
        log.debug("Diff in T2X = " + operands[2]);
        log.debug("Diff in T2Y = " + operands[3]);
        //(r*c) * (c*r)
        double[] newCurrents = arraySubtract(mCurrents, operands);

        // Debug messages
        log.info("Computed current for T1X = " + newCurrents[0]);
        log.info("Computed current for T1Y = " + newCurrents[1]);
        log.info("Computed current for T2X = " + newCurrents[2]);
        log.info("Computed current for T2Y = " + newCurrents[3]);

        return newCurrents;
    }

    public boolean setPositions(double[] positions) {
        boolean isNaN = false;
        for (double dval : positions) {
            isNaN |= Double.isNaN(dval);
            isNaN |= Double.isInfinite(dval);
        }
        if (!isNaN) {
            mPositions = positions;
            return true;
        }
        return false;
    }

    public static boolean isSystemManual() {
        return (beam.bssController.getOperatingMode() == OperatingMode.MANUAL);
    }

    public boolean isSystemPrepared() {
//        return (mStatus.getBool(Status.ESS_MAGNETS) && mStatus.getBool(Status.BCREU_HW));
        return (mStatus.getBool(Status.ESS_MAGNETS));
    }

    /* displayErrors displays any warning or error messages and then returns true if user action is required. */
    public boolean displayErrors() {
        // If the filament current is below the threshold, display a warning.
        if ((mStatus.getColor(Status.FILAMENT) == Status.UNHEALTHY) && !mFilamentWarningDisplayed) {
            // Only display the warning once.
            mFilamentWarningDisplayed = true;
            JOptionPane.showMessageDialog(null, "Filament current has dropped below 160 A,"
                    + "please replace the filament as soon as possible.", "Replace Filament", JOptionPane.WARNING_MESSAGE);
        }

//        if ((mStatus.getColor(Status.DF_CURRENT) == Status.WARNING) && !mDeflectorWarningDisplayed) {
//            // Only display the warning once.
//            mDeflectorWarningDisplayed = true;
//            JOptionPane.showMessageDialog(null, "Leakage current was over 0.1mA,"
//                    + "please monitor the deflector current.", "Deflector Warning Low", JOptionPane.WARNING_MESSAGE);
//            return true;
//        } else if ((mStatus.getColor(Status.DF_CURRENT) == Status.UNHEALTHY) && !mDeflectorErrorDisplayed) {
//            // Only display the warning once.
//            mDeflectorErrorDisplayed = true;
//            JOptionPane.showMessageDialog(null, "Leakage current was over 0.2mA,"
//                    + "please take action to reduce deflector arcing.", "Deflector Warning High", JOptionPane.ERROR_MESSAGE);
//            return true;
//        }

//        if (!isSystemManual()) {
//            String msg = "Could not prepare beam for the following reasons:\n";
//            if (!mStatus.getBool(Status.ESS_MAGNETS)) {
//                msg += "    - ESS magnets still power on, it should be turn off.\n";
//            }
////            if (!mStatus.getBool(Status.BCREU_HW)) {
////                msg += "    - \"Prepare for DS\\US\" at 10 nA in Beam Common Process.\n";
////            }
//            JOptionPane.showMessageDialog(null, msg, "Action Required", JOptionPane.ERROR_MESSAGE);
//            return true;
//        }

        // If the system isn't prepared, list actions required by user and return true.
        if (!isSystemPrepared() || !isSystemManual()) {
            String msg = "Could not prepare beam for the following reasons:\n";
            if (!mStatus.getBool(Status.ESS_MAGNETS)) {
                msg += "    - ESS magnets are on, they should be turned off.\n";
            }
            if (beam.bssController.getOperatingMode() != OperatingMode.MANUAL) {
                msg += "    - Beam Common Process should be in Manual mode.\n";
            }
            JOptionPane.showMessageDialog(null, msg, "Action Required", JOptionPane.ERROR_MESSAGE);
            return true;
        }
        return false;
    }

    public boolean refreshAll() {
        try {
//        if (!acu.isConnected()) {
//            acu.connect();
//        }

            if (!ecubtcu.isConnected()) {
                ecubtcu.connect();
            }

            if (!bcreu.isConnected()) {
                bcreu.connect();
            }


//        if (checkForSpark()) {
//            idleBCP();
//            Thread.sleep(5000);
//            prep10nA();
//        }
//        }
//        feedbackClient.retreiveMcrFeedbacks();
//
//        if (displayErrors()) {
//            return false;
//        }
//
//        // If the system isn't ready, try to prepareForAlignment.
//        if (!mStatus.andBool(Status.BLE_names.length)) {
//            prepareForAlignment();
//            return false;
//        }

//         if (P1E.getOperationMode() != BeamProfileMonitor.OperationMode.OPERATION_MODE_CONTINUOUS_ACQUISITION) {
//             P1E.startContinuousAcquisition();
//         }
//
//         if (P2E.getOperationMode() != BeamProfileMonitor.OperationMode.OPERATION_MODE_CONTINUOUS_ACQUISITION) {
//            P2E.startContinuousAcquisition();
//         }


//            try {
//                boolean var2;
//                try {
//                    P1E.acquireProfile();
//                    Thread.sleep(1000L);
//                    double[] temp = new double[4];
//                    double[] tempSigma = new double[4];
//                    temp[0] = P1E.getHorizontalCentroid();
//                    temp[1] = P1E.getVerticalCentroid();
//                    tempSigma[0] = P1E.getHorizontalSigma();
//                    tempSigma[1] = P1E.getVerticalSigma();
//                    P2E.acquireProfile();
//                    Thread.sleep(1000L);
//                    temp[2] = P2E.getHorizontalCentroid();
//                    temp[3] = P2E.getVerticalCentroid();
//                    tempSigma[2] = P2E.getHorizontalSigma();
//                    tempSigma[3] = P2E.getVerticalSigma();
//                    if (!this.setPositions(temp)) {
//                        boolean var23 = false;
//                        return var23;
//                    }
//
//                    this.mSigmas = tempSigma;
//                    int j = 1;
//                    log.info("Acquisition in " + j + ": p1eX=" + this.mPositions[0] + ";p1eY=" + this.mPositions[1] + ";p2eX=" + this.mPositions[2] + ";p2eY=" + this.mPositions[3]);
//                } catch (InterruptedException var18) {
//                    log.error(var18);
//                    var2 = false;
//                    return var2;
//                } catch (EcubtcuException var19) {
//                    log.error(var19);
//                    var2 = false;
//                    return var2;
//                } catch (Exception var20) {
//                    log.error(var20);
//                    var2 = false;
//                    return var2;
//                }
//            } finally {
//                try {
//                    P1E.stopProfileAcquisition();
//                    P2E.stopProfileAcquisition();
//                } catch (EcubtcuException var17) {
//                    log.error("ECUBTCU Communication Error: Could not stop profile acquisition.");
//                }
//
//            }

            Thread BPM1 = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        P1E.acquireProfile();
                    } catch (Exception f) {
                        log.error("Error acquiring BPM profile");
                        f.printStackTrace();
                        refreshAll();
                    }
                }
            });

//            SwingUtilities.invokeLater(new Runnable() {
//                @Override
//                public void run() {
//                    BPM1.start();
//                }
//            });
            BPM1.start();

            Thread BPM2 = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        P2E.acquireProfile();
                    } catch (Exception f) {
                        log.error("Error acquiring BPM profile");
                        f.printStackTrace();
                        refreshAll();
                    }
                }
            });

//            SwingUtilities.invokeLater(new Runnable() {
//                @Override
//                public void run() {
//                    BPM2.start();
//                }
//            });

            //BPM2.start();

            //P1E.acquireProfile();
            //P2E.acquireProfile();
            //Thread.sleep(500);
            while (BPM1.isAlive()) {
                Thread.sleep(100);
            }
            //BPM2.start();
            Thread.sleep(500);

//            P1E.startContinuousAcquisition();
//            P2E.startContinuousAcquisition();

            double temp[] = new double[4];
            double tempSigma[] = new double[4];
            temp[0] = P1E.getHorizontalCentroid();
            temp[1] = P1E.getVerticalCentroid();
            tempSigma[0] = P1E.getHorizontalSigma();
            tempSigma[1] = P1E.getVerticalSigma();

            BPM2.start();

            //P2E.acquireProfile();
            while (BPM2.isAlive()) {
                Thread.sleep(100);
            }
            Thread.sleep(500);
            temp[2] = P2E.getHorizontalCentroid();
            temp[3] = P2E.getVerticalCentroid();
            tempSigma[2] = P2E.getHorizontalSigma();
            tempSigma[3] = P2E.getVerticalSigma();

            //Thread.sleep(500);
            // If couldn't setPositions (due to NaN values), then return false.
            if (!setPositions(temp)) {
                return false;
            }
            // If the positions were good, set the sigmas.
            mSigmas = tempSigma;
            int j = 1;
            log.info("Acquisition in " + j + ": p1eX=" + mPositions[0] + ";p1eY=" + mPositions[1] + ";p2eX="
                    + mPositions[2] + ";p2eY=" + mPositions[3]);

        } catch (InterruptedException e) {
            log.error(e);
            return false;
        }
//        catch (AcquisitionTimeoutException e)
//        {
//            log.error(e);
//            return false;
//        }
//        catch (EcubtcuException e) {
//            log.error(e);
//            return false;
//        }
        catch (Exception e) {
            log.error(e);
            return false;
        } finally {
            try {
                P1E.stopProfileAcquisition();
                P2E.stopProfileAcquisition();
            } catch (EcubtcuException e) {
                log.error("ECUBTCU Communication Error: Could not stop profile acquisition.");
                // Do nothing
            }
        }

        return true;
    }

    public void useKeyCommand(int command) {
        acu.setTagValue(mStatus.Key_command, command);
        log.info("Acu key command #" + command + " sent.");
    }

    public boolean sourceTuning() {
        if (!acu.isConnected()) {
            acu.connect();
        }
        return (boolean) acu.getTagValue(Status.sour_tuning);
    }

    public boolean mainCoilTuning() {
        return (boolean) acu.getTagValue(Status.mc_tuning);
    }

    public void idleBCP() {
        if (!mStatus.getBool(Status.S2E_STATUS)) {
            log.warn("RF spark detected, setting to IDLE");
            beam.bpsController.startIdleActivity();
            beam.bpsController.proxyPublish();
        }
    }

    public void prep10nA() {
        log.warn("Performing BCREU/ISEU lookup");
        beam.bpsController.startPrepareActivity(-1, 10.0);
        beam.bpsController.proxyPublish();
    }

    public boolean isTR3SearchedInServiceMode(){
        try {
            return restManager.getVariable("S3.T3.TRA01.ServiceMode").get("value").getAsBoolean();
        }catch (Exception e) {
            e.printStackTrace();
            log.error("Error getting TR search status");
        }
        return false;
    }

    public boolean isTR3XrayTubeExtracted(){
        try {
            return restManager.getVariable("S3.T3.XRC01.NozzleXrExtracted").get("value").getAsBoolean();
        }catch (Exception e) {
            e.printStackTrace();
            log.error("Error getting TR3 Xray Tube A position.");
        }
        return false;
    }

    public int getSelectedBeamline() {
        int bp = 0;

        try {
            if (restManager.getVariable(Status.BP1_SELECTED).get("value").getAsBoolean()) {
                bp = 1;
            } else if (restManager.getVariable(Status.BP2_SELECTED).get("value").getAsBoolean()) {
                bp = 2;
            } else if (restManager.getVariable(Status.BP3_SELECTED).get("value").getAsBoolean()) {
                bp = 3;
            } else if (restManager.getVariable(Status.BP4_SELECTED).get("value").getAsBoolean()) {
                bp = 4;
            } else if (restManager.getVariable(Status.BP5_SELECTED).get("value").getAsBoolean()) {
                bp = 5;
            } else if (restManager.getVariable(Status.BP6_SELECTED).get("value").getAsBoolean()) {
                bp = 6;
            }

        } catch (Exception e) {
            e.printStackTrace();
            log.error("Error getting beamline");
        }

        return bp;
    }


    public boolean isRequestPending() {
        return (beam.beamScheduler.getPendingBeamRequests().size() != 0);
    }

    public static boolean isBeamAllocated() {
        if (beam.beamScheduler.getCurrentBeamAllocation() == null) {
            return false;
        } else {
            return true;
        }
    }

    public boolean isSchedulingManual() {
        return (!beam.beamScheduler.isSchedulingAutomatic());
    }

    public boolean isTR1Secured() {

        try {
            if (!restManager.getVariable(Status.TR1_SECURE).isJsonNull()) {
                return restManager.getVariable(Status.TR1_SECURE).get("value").getAsBoolean();
            } else if (restManager.getVariable(Status.TR1_SECURE).isJsonNull()) {
                return restManager.getVariable(Status.TR1_SECURE).get("value").getAsBoolean();
            }

        } catch (Exception e) {
            log.error(e);
        }

        return false;
    }

    public boolean isTR1Searching() {

        try {
            if (!restManager.getVariable(Status.TR1_SEARCH_ACTIVE).isJsonNull()) {
                return restManager.getVariable(Status.TR1_SEARCH_ACTIVE).get("value").getAsBoolean();
            } else if (restManager.getVariable(Status.TR1_SEARCH_ACTIVE).isJsonNull()) {
                return restManager.getVariable(Status.TR1_SEARCH_ACTIVE).get("value").getAsBoolean();
            }

        } catch (Exception e) {
            log.error(e);
        }

        return false;
    }

    public boolean isTR2Secured() {

        try {
            if (!restManager.getVariable(Status.TR2_SECURE).isJsonNull()) {
                return restManager.getVariable(Status.TR2_SECURE).get("value").getAsBoolean();
            } else if (restManager.getVariable(Status.TR2_SECURE).isJsonNull()) {
                return restManager.getVariable(Status.TR2_SECURE).get("value").getAsBoolean();
            }

        } catch (Exception e) {
            log.error(e);
        }

        return false;
    }

    public boolean isTR2Searching() {

        try {
            if (!restManager.getVariable(Status.TR2_SEARCH_ACTIVE).isJsonNull()) {
                return restManager.getVariable(Status.TR2_SEARCH_ACTIVE).get("value").getAsBoolean();
            } else if (restManager.getVariable(Status.TR2_SEARCH_ACTIVE).isJsonNull()) {
                return restManager.getVariable(Status.TR2_SEARCH_ACTIVE).get("value").getAsBoolean();
            }

        } catch (Exception e) {
            log.error(e);
        }

        return false;
    }

    public boolean isTR3Secured() {

        try {
            if (!restManager.getVariable(Status.TR3_SECURE).isJsonNull()) {
                return restManager.getVariable(Status.TR3_SECURE).get("value").getAsBoolean();
            } else if (restManager.getVariable(Status.TR3_SECURE).isJsonNull()) {
                return restManager.getVariable(Status.TR3_SECURE).get("value").getAsBoolean();
            }

        } catch (Exception e) {
            log.error(e);
        }

        return false;
    }

    public boolean isTR3Searching() {

        try {
            if (!restManager.getVariable(Status.TR3_SEARCH_ACTIVE).isJsonNull()) {
                return restManager.getVariable(Status.TR3_SEARCH_ACTIVE).get("value").getAsBoolean();
            } else if (restManager.getVariable(Status.TR3_SEARCH_ACTIVE).isJsonNull()) {
                return restManager.getVariable(Status.TR3_SEARCH_ACTIVE).get("value").getAsBoolean();
            }

        } catch (Exception e) {
            log.error(e);
        }

        return false;
    }

    public boolean isTR4Secured() {

        try {
            if (!restManager.getVariable(Status.TR4_SECURE).isJsonNull()) {
                return restManager.getVariable(Status.TR4_SECURE).get("value").getAsBoolean();
            } else if (restManager.getVariable(Status.TR4_SECURE).isJsonNull()) {
                return restManager.getVariable(Status.TR4_SECURE).get("value").getAsBoolean();
            }

        } catch (Exception e) {
            log.error(e);
        }

        return false;
    }

    public boolean isTR4Searching() {

        try {
            if (!restManager.getVariable(Status.TR4_SEARCH_ACTIVE).isJsonNull()) {
                return restManager.getVariable(Status.TR4_SEARCH_ACTIVE).get("value").getAsBoolean();
            } else if (restManager.getVariable(Status.TR4_SEARCH_ACTIVE).isJsonNull()) {
                return restManager.getVariable(Status.TR4_SEARCH_ACTIVE).get("value").getAsBoolean();
            }

        } catch (Exception e) {
            log.error(e);
        }

        return false;
    }


    public boolean readyForPowerSave() {
        //Conditions that must be met to reduce magnet currents safely

        if (isRequestPending()) {
            return false;
        }
        if (isBeamAllocated()) {
            return false;
        }
        if (isSystemManual()) {
            return false;
        }
        if (isSchedulingManual()) {
            return false;
        }
        if (isTR1Searching()) {
            return false;
        }
        if (isTR1Secured()) {
            return false;
        }
        if (isTR2Searching()) {
            return false;
        }
        if (isTR2Secured()) {
            return false;
        }
        if (isTR3Searching()) {
            return false;
        }
        if (isTR3Secured()) {
            return false;
        }
        if (isTR4Searching()) {
            return false;
        }
        if (isTR4Secured()) {
            return false;
        }

        return true;
    }

    public boolean readyForVacuumSave() {
        //Conditions that must be met to move the ESS slits safely

        if (isBeamAllocated()) {
            return false;
        }
        if (isSystemManual()) {
            return false;
        }

        return true;
    }

    public static void setSlits(double sl1e, double sl2e, double sl3e) {
        try {

            ecubtcu.slitsGoMm("SL1E", sl1e);
            ecubtcu.slitsGoMm("SL2E", sl2e);
            ecubtcu.slitsGoMm("SL3E", sl3e);

//            if (SL1E.getPosisionFeedback() < 50.00 && SL1E.getPosisionSetpoint() != 50.0){
//                ecubtcu.slitsGoMm("SL1E", 50.0);
//            }

//            if (SL2E.getPosisionFeedback() > 2.00 && SL2E.getPosisionSetpoint() != 1.30){
//                ecubtcu.slitsGoMm("SL2E", 1.3);
//            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getDipoleCurrents(File resultsFile, EcubtcuDipole[] dipoles) {
        DecimalFormat df = new DecimalFormat("#0.00");
        try {
            for (int i = 0; i < dipoles.length; i++) {
                dipoles[i].getCurrentFeedback();
            }
        } catch (NullPointerException e) {
            log.error("Null ptr at setDipoleCurrents()");
            e.printStackTrace();
        }
    }

    public void setDipoleCurrents(EcubtcuDipole[] dipoles) {
        DecimalFormat df = new DecimalFormat("#0.00");
        try {
            for (int i = 0; i < dipoles.length; i++) {
                if (dipoles[i].getCurrentFeedback() >= 100.0) {
                    ecubtcu.canMagnetSetCurrent(dipoles[i].toString(), (dipoles[i].getCurrentFeedback() / 1.5));
                    log.info("[POWER SAVE] " + dipoles[i].toString() + " set to " + df.format(dipoles[i].getCurrentFeedback()) + "A");
                } else if (dipoles[i].getCurrentFeedback() < 100.0 && dipoles[i].getCurrentSetpoint() != 0.0) {
                    ecubtcu.canMagnetSetCurrent(dipoles[i].toString(), 0.0);
                    log.info("[POWER SAVE] " + dipoles[i].toString() + " set to 0A");
                }
            }
        } catch (EcubtcuException e) {
            log.error("ECUBTCU exception " + e);
            e.printStackTrace();
        } catch (NullPointerException e) {
            log.error("Null ptr at setDipoleCurrents()");
            e.printStackTrace();
        } finally {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void getQuadCurrents(File resultsFile, EcubtcuQuadrupole[] quads) {
        DecimalFormat df = new DecimalFormat("#0.00");
        try {
            for (int i = 0; i < quads.length; i++) {
                quads[i].getCurrentFeedback();
            }

        } catch (NullPointerException e) {
            log.error("Null ptr at setQuadCurrents()");
            e.printStackTrace();
        }
    }

    public void setQuadCurrents(EcubtcuQuadrupole[] quads) {
        DecimalFormat df = new DecimalFormat("#0.00");
        try {
            for (int i = 0; i < quads.length; i++) {
                if (quads[i].getCurrentFeedback() >= 40.0) {
                    ecubtcu.canMagnetSetCurrent(quads[i].toString(), (quads[i].getCurrentFeedback() / 1.5));
                    log.info("[POWER SAVE] " + quads[i].toString() + " set to " + df.format(quads[i].getCurrentFeedback()) + "A");
                } else if (quads[i].getCurrentFeedback() < 40.0 && quads[i].getCurrentSetpoint() != 0.0) {
                    ecubtcu.canMagnetSetCurrent(quads[i].toString(), 0.0);
                    log.info("[POWER SAVE] " + quads[i].toString() + " set to 0A");
                }
            }
        } catch (EcubtcuException e) {
            log.error("ECUBTCU exception " + e);
            e.printStackTrace();
        } catch (NullPointerException e) {
            log.error("Null ptr at setQuadCurrents()");
            e.printStackTrace();
        } finally {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void setESSCurrents() {
        if (!ecubtcu.isConnected()) {
            ecubtcu.connect();
        }

        setDipoleCurrents(ESSDipoles);
        setQuadCurrents(ESSQuads);
    }

    public void setBeamlineCurrents(int bp) {
        switch (bp) {
            case 1:
                setDipoleCurrents(BP1Dipoles);
                setQuadCurrents(BP1Quads);
                break;
            case 2:
                setDipoleCurrents(BP2Dipoles);
                setQuadCurrents(BP2Quads);
                break;
            case 3:
                setDipoleCurrents(BP3Dipoles);
                setQuadCurrents(BP3Quads);
                break;
            case 4:
                setDipoleCurrents(BP4Dipoles);
                setQuadCurrents(BP4Quads);
                break;
            case 5:
                setDipoleCurrents(BP5Dipoles);
                setQuadCurrents(BP5Quads);
                break;
            case 6:
                setDipoleCurrents(BP6Dipoles);
                setQuadCurrents(BP6Quads);
                break;
        }
    }

    public double[] getCurrents() {
        double[] currents = new double[4];
        for (int i = 0; i < 4; i++) {
            currents[i] = (Double) acu.getTagValue(Status.Magnet_read[i]);
        }
        return currents;
    }

    public void setCurrents(double[] newCurrents) {
        log.debug("Is ACU connected ?");
        if (!acu.isConnected()) {
            log.debug("Will try to connect to ACU");
            acu.connect();
            log.debug("Is connected");
        }

        for (int i = 0; i < 4; i++) {
            //Dampening affect added to P1e Y  (by applying to P1e X due to BPM inversion)
            if (i == 0) {
                acu.setTagValue(Status.Magnet_write[i], newCurrents[i] * 0.7);
            }
            acu.setTagValue(Status.Magnet_write[i], newCurrents[i]);
        }
        log.debug("New currents sent to 4 magnets");
    }

    public void setSafeCurrents() {
        log.debug("Is ACU connected ?");
        if (!acu.isConnected()) {
            log.debug("Will try to connect to ACU");
            acu.connect();
            log.debug("Is connected");
        }

        for (int i = 0; i < 4; i++) {
            acu.setTagValue(Status.Magnet_write[i], mSafeCurrents[i]);
        }
        //acu.setTagValue(Status.Key_command, 76);
        log.warn("Safe currents restored to 4 magnets");
    }

    public String getSiteName() {
        return siteName;
    }

    public static void setFeedbackClient() {
//        if (feedbackClient != null && feedbackClient.isConnected())
//       {
//            feedbackClient.disconnect();
//        }

//        if (feedbackClient != null)
//        {
//            feedbackClient.removePropertyChangeListener(connectListener);
//        }

//        if (rtClient == null)
//        {
//            rtClient = new BlakRtClient();
//        }

//        if (icompFeedback == null)
//        {
//            icompFeedback = new BlakICompFeedbackClient();
//            icompFeedback.setupICompClient();
//            icompFeedback.setTurnOnEssMagnetsCommandId(78);
//            icompFeedback.setTurnOffEssMagnetsCommandId(79);
//            icompFeedback.connect();
//            icompFeedback.retreiveMcrFeedbacks();
//            icompFeedback.retrieveBlpscuFeedback();
//            try {
//                blpscuSS = icompFeedback.createBlpscu();
//                blpscuSS.setTurnOffEssMagnetsCommandId((short) 78);
//                blpscuSS.setTurnOffEssMagnetsCommandId((short) 79);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
        //       feedbackClient = rtClient;

        if (!feedbackClient.isConnected()) {
            feedbackClient.setupICompClient();
            feedbackClient.connect();
        }

        feedbackClient.addPropertyChangeListener(connectListener);

    }

    public void align() {
        Screen screen = new Screen(mScreen);
        aligned = false;
        String lastMatch = "";

        Settings.MinSimilarity = 0.8;
        try {
            Robot robot = new Robot();

//            if (null != screen.exists("alignmentTab", 0)) {
//                log.warn("Found Extraction Steering Tab");
//                screen.getLastMatch().click();
//                robot.delay(300);
//
//            } else {
//                log.error("Could not find Extraction Steering tab");
//            }

            while (!aligned) {

                if (null != screen.exists("refresh", 2) || null != screen.exists("refresh2", 0)) {
                    log.warn("Found refresh button");
                    screen.getLastMatch().click();
                    robot.delay(400);

//                    if (null != screen.exists("ActionReqESS", 1)) {
//                        log.warn("Found ActionReqESS");
//                        aligned = true;
//                    }

                    while (null != screen.exists("preparing", 0) || null != screen.exists("preparing2", 0) || null != screen.exists("preparing3", 0) || null != screen.exists("afterRefresh", 0)) {
                        if (lastMatch != screen.getLastMatch().getImageFilename()) {
                            log.warn("Found " + screen.getLastMatch().getImageFilename());
                        }
                        lastMatch = screen.getLastMatch().getImageFilename();
                        robot.delay(200);
                    }

//                    log.error(MouseInfo.getPointerInfo().getLocation());
//                    log.error(MouseInfo.getPointerInfo().getLocation());
//                    log.error(MouseInfo.getPointerInfo().getLocation());

//                    if (mScreen == 1) {
//                        color1 = robot.getPixelColor(-1670, -62);
//                        color2 = robot.getPixelColor(-1495, -62);
//                        color3 = robot.getPixelColor(-1320, -62);
//                        color4 = robot.getPixelColor(-1145, -62);
//
//                    } else {
//                        color1 = robot.getPixelColor(250, 110);
//                        color2 = robot.getPixelColor(425, 110);
//                        color3 = robot.getPixelColor(580, 110);
//                        color4 = robot.getPixelColor(733, 110);
//                    }
                }

//                if (color1.getGreen() == 240 && color2.getGreen() == 240 && color3.getGreen() == 240 && color4.getGreen() >= 235 && !aligned && null == screen.exists("afterRefresh", 1)) {

//                if (Gui.checkTolerances()) {

                if (mTolerances[0] >= Math.abs(mPositions[0]) - mTargets[0] && mTolerances[1] >= Math.abs(mPositions[1]) - mTargets[1] && mTolerances[2] >= Math.abs(mPositions[2]) - mTargets[2] && mTolerances[3] >= Math.abs(mPositions[3] - mTargets[3])) {

                    log.warn("Positions within tolerance, ending automatic alignment");

                    if (null != screen.exists("cancel", 1)) {
                        log.warn("Found cancel button");
                        screen.getLastMatch().click();
                        robot.delay(250);
                        aligned = true;
                    } else {
                        log.error("Could not find cancel button");
                    }
                } else if (null != screen.exists("apply", 1) && !aligned) {
                    log.warn("Found apply button");
                    screen.getLastMatch().click();
                    robot.delay(500);
                    if (null != screen.exists("refresh", 0) || null != screen.exists("refresh2", 0)) {
                        log.warn("Found refresh button");
                        screen.getLastMatch().click();
                        robot.delay(400);

                        while (null != screen.exists("preparing", 1) || null != screen.exists("preparing2", 0) || null != screen.exists("preparing3", 0) || null != screen.exists("afterRefresh", 1) && !aligned) {
                            if (lastMatch != screen.getLastMatch().getImageFilename()) {
                                log.warn("Found " + screen.getLastMatch().getImageFilename());
                            }
                            lastMatch = screen.getLastMatch().getImageFilename();
                            robot.delay(200);
                        }

//                        if (mScreen == 1) {
//
//                            color1 = robot.getPixelColor(-1670, -62);
//                            color2 = robot.getPixelColor(-1495, -62);
//                            color3 = robot.getPixelColor(-1320, -62);
//                            color4 = robot.getPixelColor(-1145, -62);
//
//                        } else {
//                            color1 = robot.getPixelColor(250, 115);
//                            color2 = robot.getPixelColor(425, 115);
//                            color3 = robot.getPixelColor(600, 115);
//                            color4 = robot.getPixelColor(775, 115);
//                        }

//                        log.error("color1 = " + color1 + "  color2 = " + color2 + "  color3 = " + color3 + "  color4 = " + color4);

//                        if (color1.getGreen() == 240 && color2.getGreen() == 240 && color3.getGreen() == 240 && color4.getGreen() >= 235 && !aligned) {
                        if (mTolerances[0] >= Math.abs(mPositions[0]) - mTargets[0] && mTolerances[1] >= Math.abs(mPositions[1]) - mTargets[1] && mTolerances[2] >= Math.abs(mPositions[2]) - mTargets[2] && mTolerances[3] >= Math.abs(mPositions[3] - mTargets[3])) {

                            log.warn("Positions within tolerance, ending automatic alignment");

                            if (null != screen.exists("cancel", 1)) {
                                log.warn("Found cancel button");
                                screen.getLastMatch().click();
                                robot.delay(300);
                                aligned = true;
                            }
                        } else if (null != screen.exists("apply", 1) && !aligned) {
                            log.warn("Found apply button");
                            screen.getLastMatch().click();
                            robot.delay(500);
                        }
                    }
                }
                //log.error("Could not find refresh button");
            }


//            while(true) {
//                while(!aligned) {
//                    if(null != screen.exists("refresh", 0.0D) || null != screen.exists("refresh2", 0.0D)) {
//                        log.warn("Found refresh button");
//                        screen.getLastMatch().click();
//                        robot.delay(400);
//                        if(!aligned) {
//                            while(true) {
//                                if(null == screen.exists("preparing", 1.0D) && null == screen.exists("afterRefresh", 1.0D)) {
//                                    this.color1 = robot.getPixelColor(250, 115);
//                                    this.color2 = robot.getPixelColor(425, 115);
//                                    this.color3 = robot.getPixelColor(600, 115);
//                                    this.color4 = robot.getPixelColor(775, 115);
//                                    break;
//                                }
//
//                                log.warn("Found " + screen.getLastMatch().getImageFilename());
//                                robot.delay(200);
//                            }
//                        }
//                    }
//
//                    if(this.color1.getGreen() == 240 && this.color2.getGreen() == 240 && this.color3.getGreen() == 240 && this.color4.getGreen() == 240 && !aligned) {
//                        log.warn("Found allGreen");
//                        if(null != screen.exists("cancel", 1.0D)) {
//                            log.warn("Found cancel button");
//                            screen.getLastMatch().click();
//                            robot.delay(250);
//                            aligned = true;
//                        } else {
//                            log.error("Could not find cancel button");
//                        }
//                    } else if(null != screen.exists("apply", 1.0D) && !aligned) {
//                        log.warn("Found apply button");
//                        screen.getLastMatch().click();
//                        robot.delay(500);
//                        if(null != screen.exists("refresh", 0.0D) || null != screen.exists("refresh2", 0.0D)) {
//                            log.warn("Found refresh button");
//                            screen.getLastMatch().click();
//                            robot.delay(400);
//
//                            while(null != screen.exists("preparing", 1.0D) || null != screen.exists("afterRefresh", 1.0D) && !aligned) {
//                                log.warn("Found " + screen.getLastMatch().getImageFilename());
//                                robot.delay(200);
//                            }
//
//                            this.color1 = robot.getPixelColor(250, 115);
//                            this.color2 = robot.getPixelColor(425, 115);
//                            this.color3 = robot.getPixelColor(600, 115);
//                            this.color4 = robot.getPixelColor(775, 115);
//                            if(this.color1.getGreen() == 240 && this.color2.getGreen() == 240 && this.color3.getGreen() == 240 && this.color4.getGreen() == 240 && !aligned) {
//                                log.warn("Found allGreen");
//                                if(null != screen.exists("cancel", 1.0D)) {
//                                    log.warn("Found cancel button");
//                                    screen.getLastMatch().click();
//                                    robot.delay(300);
//                                    aligned = true;
//                                }
//                            } else if(null != screen.exists("apply", 1.0D) && !aligned) {
//                                log.warn("Found apply button");
//                                screen.getLastMatch().click();
//                                robot.delay(500);
//                            }
//                        }
//                    }
//                }
//
//                return;
//            }


        } catch (AWTException e) {
            e.printStackTrace();
        }
    }

    public void burnInSource(int step) {
        if (isSystemManual()) {

            log.debug("Is ACU connected ?");
            if (!acu.isConnected()) {
                log.debug("Will try to connect to ACU");
                acu.connect();
                log.debug("Is connected");
            }

            switch (step) {
                case 0:
                    if (Double.parseDouble(acu.getTagValue(mStatus.Arc_current).toString()) > 0.5) {
                        acu.setTagValue(mStatus.Key_command, 76);
                        log.warn("[BURN-IN] Arc Power Supply has been turned OFF.");
                    }

                    if (Double.parseDouble(acu.getTagValue(mStatus.Fil_current).toString()) > 100) {
                        acu.setTagValue(mStatus.Key_command, 75);
                        log.warn("[BURN-IN] Filament Power Supply has been turned OFF.");
                    }
                    break;
                case 1:
                    acu.setTagValue(mStatus.Fil_write, (double) burnInStep[1]);
                    acu.setTagValue(mStatus.Key_command, 71);
                    log.warn("[BURN-IN] Filament Power Supply has been turned ON to " + String.valueOf(burnInStep[1]) + "A.");
                    break;
                case 2:
                    acu.setTagValue(mStatus.Fil_write, (double) burnInStep[2]);
                    if (isSourceOn()) {
                        log.warn("[BURN-IN] Filament Power Supply has been set to " + String.valueOf(burnInStep[2]) + "A.");
                    } else {
                        acu.setTagValue(mStatus.Key_command, 71);
                        log.warn("[BURN-IN] Filament Power Supply has been turned ON to " + String.valueOf(burnInStep[2]) + "A.");
                    }
                    break;
                case 3:
                    acu.setTagValue(mStatus.Fil_write, (double) burnInStep[3]);
                    if (isSourceOn()) {
                        log.warn("[BURN-IN] Filament Power Supply has been set to " + String.valueOf(burnInStep[3]) + "A.");
                    } else {
                        acu.setTagValue(mStatus.Key_command, 71);
                        log.warn("[BURN-IN] Filament Power Supply has been turned ON to " + String.valueOf(burnInStep[3]) + "A.");
                    }
                    break;
                case 4:
                    acu.setTagValue(mStatus.Fil_write, (double) burnInStep[4]);
                    if (isSourceOn()) {
                        log.warn("[BURN-IN] Filament Power Supply has been set to " + String.valueOf(burnInStep[4]) + "A.");
                    } else {
                        acu.setTagValue(mStatus.Key_command, 71);
                        log.warn("[BURN-IN] Filament Power Supply has been turned ON to " + String.valueOf(burnInStep[4]) + "A.");
                    }
                    break;
                case 5:
                    acu.setTagValue(mStatus.Fil_write, (double) burnInStep[5]);
                    if (isSourceOn()) {
                        log.warn("[BURN-IN] Filament Power Supply has been set to " + String.valueOf(burnInStep[5]) + "A.");
                    } else {
                        acu.setTagValue(mStatus.Key_command, 71);
                        log.warn("[BURN-IN] Filament Power Supply has been turned ON to " + String.valueOf(burnInStep[5]) + "A.");
                    }
                    break;
                case 6:
                    acu.setTagValue(mStatus.Fil_write, (double) burnInStep[6]);
                    if (isSourceOn()) {
                        log.warn("[BURN-IN] Filament Power Supply has been set to " + String.valueOf(burnInStep[6]) + "A.");
                    } else {
                        acu.setTagValue(mStatus.Key_command, 71);
                        log.warn("[BURN-IN] Filament Power Supply has been turned ON to " + String.valueOf(burnInStep[6]) + "A.");
                    }
                    break;
                case 7:
                    acu.setTagValue(mStatus.Fil_write, (double) burnInStep[7]);
                    if (isSourceOn()) {
                        log.warn("[BURN-IN] Filament Power Supply has been set to " + String.valueOf(burnInStep[7]) + "A.");
                    } else {
                        acu.setTagValue(mStatus.Key_command, 71);
                        log.warn("[BURN-IN] Filament Power Supply has been turned ON to " + String.valueOf(burnInStep[7]) + "A.");
                    }
                    break;
                case 8:
                    acu.setTagValue(mStatus.Fil_write, (double) burnInStep[8]);
                    if (isSourceOn()) {
                        log.warn("[BURN-IN] Filament Power Supply has been set to " + String.valueOf(burnInStep[8]) + "A.");
                    } else {
                        acu.setTagValue(mStatus.Key_command, 71);
                        log.warn("[BURN-IN] Filament Power Supply has been turned ON to " + String.valueOf(burnInStep[8]) + "A.");
                    }
                    break;
                case 9:
                    acu.setTagValue(mStatus.Fil_write, (double) burnInStep[9]);
                    if (isSourceOn()) {
                        log.warn("[BURN-IN] Filament Power Supply has been set to " + String.valueOf(burnInStep[9]) + "A.");
                    } else {
                        acu.setTagValue(mStatus.Key_command, 71);
                        log.warn("[BURN-IN] Filament Power Supply has been turned ON to " + String.valueOf(burnInStep[9]) + "A.");
                    }
                    break;
                case 10:
                    acu.setTagValue(mStatus.Fil_write, (double) burnInStep[10]);
                    if (isSourceOn()) {
                        log.warn("[BURN-IN] Filament Power Supply has been set to " + String.valueOf(burnInStep[10]) + "A.");
                    } else {
                        acu.setTagValue(mStatus.Key_command, 71);
                        log.warn("[BURN-IN] Filament Power Supply has been turned ON to " + String.valueOf(burnInStep[10]) + "A.");
                    }
                    break;
                case 11:
                    acu.setTagValue(mStatus.Arc_write, (double) burnInStep[11]);
                    acu.setTagValue(mStatus.Key_command, 72);
                    log.warn("[BURN-IN] Arc Power Supply has been turned ON to " + String.valueOf(burnInStep[11]) + "mA.");
                    break;
                case 12:
                    if (!sourceTuning()) {
                        if (isArcOn()) {
                            acu.setTagValue(mStatus.Key_command, 73);
                            log.warn("[BURN-IN] Source tuning has been enabled.");
                        } else {
                            log.warn("[BURN-IN] Source tuning has NOT been enabled because the Arc PS is not on.");
                        }
                    }
                    break;
            }
        } else {
            log.error("[BURN-IN] System is in automatic mode, please switch to manual.");
        }
    }

    public boolean isArcOn() {
        if (Float.valueOf(acu.getTagValue(Status.Arc_current).toString()) > 1) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isSourceOn() {
        if (Float.valueOf(acu.getTagValue(Status.Fil_current).toString()) > 50) {
            return true;
        } else {
            return false;
        }
    }

    public boolean sparkDetected() {
        try {
            return restManager.getVariable("B0.R1.SKD01.RfSpaDe").getAsBoolean();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void stopBPMs() {
        try {
            if (P1E.getOperationMode() == BeamProfileMonitor.OperationMode.OPERATION_MODE_CONTINUOUS_ACQUISITION) {
                P1E.stopContinuousAcquisition();
            }
            if (P2E.getOperationMode() == BeamProfileMonitor.OperationMode.OPERATION_MODE_CONTINUOUS_ACQUISITION) {
                P2E.stopContinuousAcquisition();
            }
        } catch (EcubtcuNotConnectedException e) {
            e.printStackTrace();
        } catch (EcubtcuException e) {
            e.printStackTrace();
        }
    }

    public void updateButtonTest() {
//        beam.xrayCtrl.isMoving();
        //PmsDevice XRayA = beam.xrayCtrl;
        //XRayA.
//        ((RenovatedXrayProxy) XRayA).init();
//        log.error(((RenovatedXrayProxy) XRayA).isMoving());

//        log.error(beam.xrayCtrl.getLabel());
//        log.error(beam.xrayCtrl.getMotionVeto());
//        log.error(beam.xrayCtrl.getLegacyMotionStatus());
//        log.error(beam.xrayCtrl.isMotionAllowed());
//        log.error(beam.xrayCtrl.isTargetReached());
//        log.error(beam.xrayCtrl.isInitialized());
        //beam.xrayCtrl.softStop();


        //beam.xrayCtrl.retract();
        //beam.xrayCtrl.setActive(false);


        //States are UNKNOWN
        //log.error(beam.xrayCtrl.getRetractableDeviceState());
        //log.error(beam.xrayCtrl.getState());

//        beam.xrayCtrl.reset();
//        beam.xrayCtrl.setRetracted(false);
//        beam.xrayCtrl.setInserted(true);
//        beam.xrayCtrl.insert();
//        beam.xrayCtrl.proxyPublish();

    }

    public void trackMagnetFeedbackTR1() throws InterruptedException {
        Timer timer = new Timer(1000, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                log.warn("BSS activity is: " + beam.bssController.getCurrentActivityName().toString());
            }
        });
        timer.setRepeats(false);

        while (beam.bssController.getCurrentActivityName() != BssActivityId.PREPARE) {
            timer.start();
        }

        timer = new Timer(2000, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                log.warn("[MAGNETDUMP][FBTR1] Starting csv write.");
            }
        });
        timer.setRepeats(false);
        timer.start();

        if (resultsFileTR1.isFile()) {
            csvWriteSetpointsTR1(resultsFileTR1);
        } else {
            csvCreateTR1(resultsFileTR1);
        }

        //Collect while set range is ongoing
        timer = new Timer(200, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                csvWriteFeedbacksTR1(resultsFileTR1);
            }
        });
        timer.setRepeats(false);

        while (beam.bssController.getCurrentActivityName() == BssActivityId.PREPARE) {
            timer.start();
        }

        while (beam.BAPP1.getIrradiationStatus() == IrradiationStatus.NOT_READY) {
            timer.start();
        }

        //DCEU reset was just pressed
        csvWriteSetpointsTR1(resultsFileTR1);

        //Collect for 3 seconds after set range finishes
        TR1timer.start();

        timer = new Timer(200, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                log.warn("[MAGNETDUMP][FBTR1] IrradiationStatus: " + beam.BAPP1.getIrradiationStatus());
            }
        });
        timer.setRepeats(false);

        while (beam.BAPP1.getIrradiationStatus() != IrradiationStatus.IRRADIATING) {
            timer.start();
        }

        TR1timer.stop();
        try (BufferedWriter out = new BufferedWriter(new FileWriter(resultsFileTR1, true))) {
            out.newLine();
            out.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        log.warn("[MAGNETDUMP][FBTR1] Ending csv write.");

//        timer = new Timer(3000, new AbstractAction() {
//            @Override
//            public void actionPerformed(ActionEvent ae) {
//                TR1timer.stop();
//                try (BufferedWriter out = new BufferedWriter(new FileWriter(resultsFileTR1, true))) {
//                    out.newLine();
//                    out.newLine();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//                log.warn("[MAGNETDUMP][FBTR1] Ending csv write.");
//            }
//        });
//        timer.setRepeats(false);
//        timer.start();
    }

    public void trackMagnetFeedbackTR4() throws InterruptedException {
        Timer timer = new Timer(1000, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                log.warn("BSS activity is: " + beam.bssController.getCurrentActivityName().toString());
            }
        });
        timer.setRepeats(false);

        while (beam.bssController.getCurrentActivityName() != BssActivityId.PREPARE) {
            timer.start();
        }

        timer = new Timer(2000, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                log.warn("[MAGNETDUMP][GTR4] Starting csv write.");
            }
        });
        timer.setRepeats(false);
        timer.start();

        if (resultsFileTR4.isFile()) {
            csvWriteSetpointsTR4(resultsFileTR4);
        } else {
            csvCreateTR4(resultsFileTR4);
        }

        //Collect while set range is ongoing
        timer = new Timer(200, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                csvWriteFeedbacksTR4(resultsFileTR4);
            }
        });
        timer.setRepeats(false);

        while (beam.bssController.getCurrentActivityName() == BssActivityId.PREPARE) {
            timer.start();
        }

        while (beam.BAPP4.getIrradiationStatus() == IrradiationStatus.NOT_READY) {
            timer.start();
        }

        //DCEU reset was just pressed
        csvWriteSetpointsTR4(resultsFileTR4);

        //Collect for 3 seconds after set range finishes
        TR4timer.start();

        timer = new Timer(200, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                log.warn("[MAGNETDUMP][GTR4] IrradiationStatus: " + beam.BAPP4.getIrradiationStatus());
            }
        });
        timer.setRepeats(false);

        while (beam.BAPP4.getIrradiationStatus() != IrradiationStatus.IRRADIATING) {
            timer.start();
        }

        TR4timer.stop();
        try (BufferedWriter out = new BufferedWriter(new FileWriter(resultsFileTR4, true))) {
            out.newLine();
            out.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        log.warn("[MAGNETDUMP][GTR4] Ending csv write.");

//        timer = new Timer(3000, new AbstractAction() {
//            @Override
//            public void actionPerformed(ActionEvent ae) {
//                TR4timer.stop();
//                try (BufferedWriter out = new BufferedWriter(new FileWriter(resultsFileTR4, true))) {
//                    out.newLine();
//                    out.newLine();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//                log.warn("[MAGNETDUMP][GTR4] Ending csv write.");
//            }
//        });
//        timer.setRepeats(false);
//        timer.start();
    }

    private void csvCreateTR1(File newFile) {
        try (BufferedWriter out = new BufferedWriter(new FileWriter(newFile, true))) {
            out.write("Magnet,");
            for (int i = 0; i < TR1Quads.length; i++) {
                out.write(TR1Quads[i].getName() + ",");
            }
            for (int i = 0; i < TR1Dipoles.length; i++) {
                out.write(TR1Dipoles[i].getName() + ",");
            }
            for (int i = 0; i < TR1Steering.length; i++) {
                out.write(TR1Steering[i].getName() + ",");
            }
            out.write(SL1E.getName() + ",");
            out.write(SL1E.getName() + ",");
            out.write(SL2E.getName() + ",");
            out.write(SL2E.getName() + ",");
        } catch (IOException e) {
            log.error(e);
        }

        csvWriteSetpointsTR1(newFile);
    }

    private void csvWriteSetpointsTR1(File resultsFile) {
        DecimalFormat df = new DecimalFormat("#0.000");
        try (BufferedWriter out = new BufferedWriter(new FileWriter(resultsFile, true))) {
            out.newLine();
            out.write("Setpoint,");
            for (int i = 0; i < TR1Quads.length; i++) {
                out.write(df.format(TR1Quads[i].getCurrentSetpoint()) + ",");
            }
            for (int i = 0; i < TR1Dipoles.length; i++) {
                out.write(df.format(TR1Dipoles[i].getCurrentSetpoint()) + ",");
            }
            for (int i = 0; i < TR1Steering.length; i++) {
                out.write(df.format(TR1Steering[i].getCurrentSetpoint()) + ",");
            }
            out.write(df.format(SL1E.getPosisionSetpoint()) + ",");
            out.write(df.format(SL1E.getMotorSetpoint()) + ",");
            out.write(df.format(SL2E.getPosisionSetpoint()) + ",");
            out.write(df.format(SL2E.getMotorSetpoint()) + ",");
        } catch (IOException e) {
            log.error(e);
        }
    }

    private void csvWriteFeedbacksTR1(File resultsFile) {
        DecimalFormat df = new DecimalFormat("#0.000");
        String pattern = "HH:mm:ss:SSS";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        String time = simpleDateFormat.format(new Date());

        try (BufferedWriter out = new BufferedWriter(new FileWriter(resultsFile, true))) {
            out.newLine();
            out.write("FB@" + time + ",");
            for (int i = 0; i < TR1Quads.length; i++) {
                out.write(df.format(TR1Quads[i].getCurrentFeedback()) + ",");
            }
            for (int i = 0; i < TR1Dipoles.length; i++) {
                out.write(df.format(TR1Dipoles[i].getCurrentFeedback()) + ",");
            }
            for (int i = 0; i < TR1Steering.length; i++) {
                out.write(df.format(TR1Steering[i].getCurrentFeedback()) + ",");
            }
            out.write(df.format(SL1E.getPosisionFeedback()) + ",");
            out.write(df.format(SL1E.getMotorFeedback()) + ",");
            out.write(df.format(SL2E.getPosisionFeedback()) + ",");
            out.write(df.format(SL2E.getMotorFeedback()) + ",");
        } catch (IOException e) {
            log.error(e);
        }
    }

    private void csvCreateTR4(File newFile) {
        try (BufferedWriter out = new BufferedWriter(new FileWriter(newFile, true))) {
            out.write("Magnet,");
            for (int i = 0; i < TR4Quads.length; i++) {
                out.write(TR4Quads[i].getName() + ",");
            }
            for (int i = 0; i < TR4Dipoles.length; i++) {
                out.write(TR4Dipoles[i].getName() + ",");
            }
            for (int i = 0; i < TR4Steering.length; i++) {
                out.write(TR4Steering[i].getName() + ",");
            }
            out.write(SL1E.getName() + ",");
            out.write(SL1E.getName() + ",");
            out.write(SL2E.getName() + ",");
            out.write(SL2E.getName() + ",");
        } catch (IOException e) {
            log.error(e);
        }

        csvWriteSetpointsTR4(newFile);
    }

    private void csvWriteSetpointsTR4(File resultsFile) {
        DecimalFormat df = new DecimalFormat("#0.000");
        try (BufferedWriter out = new BufferedWriter(new FileWriter(resultsFile, true))) {
            out.newLine();
            out.write("Setpoint,");
            for (int i = 0; i < TR4Quads.length; i++) {
                out.write(df.format(TR4Quads[i].getCurrentSetpoint()) + ",");
            }
            for (int i = 0; i < TR4Dipoles.length; i++) {
                out.write(df.format(TR4Dipoles[i].getCurrentSetpoint()) + ",");
            }
            for (int i = 0; i < TR4Steering.length; i++) {
                out.write(df.format(TR4Steering[i].getCurrentSetpoint()) + ",");
            }
            out.write(df.format(SL1E.getPosisionSetpoint()) + ",");
            out.write(df.format(SL1E.getMotorSetpoint()) + ",");
            out.write(df.format(SL2E.getPosisionSetpoint()) + ",");
            out.write(df.format(SL2E.getMotorSetpoint()) + ",");
        } catch (IOException e) {
            log.error(e);
        }
    }

    private void csvWriteFeedbacksTR4(File resultsFile) {
        DecimalFormat df = new DecimalFormat("#0.000");
        String pattern = "HH:mm:ss:SSS";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        String time = simpleDateFormat.format(new Date());


        try (BufferedWriter out = new BufferedWriter(new FileWriter(resultsFile, true))) {
            out.newLine();
            out.write("FB@" + time + ",");
            for (int i = 0; i < TR4Quads.length; i++) {
                out.write(df.format(TR4Quads[i].getCurrentFeedback()) + ",");
            }
            for (int i = 0; i < TR4Dipoles.length; i++) {
                out.write(df.format(TR4Dipoles[i].getCurrentFeedback()) + ",");
            }
            for (int i = 0; i < TR4Steering.length; i++) {
                out.write(df.format(TR4Steering[i].getCurrentFeedback()) + ",");
            }
            out.write(df.format(SL1E.getPosisionFeedback()) + ",");
            out.write(df.format(SL1E.getMotorFeedback()) + ",");
            out.write(df.format(SL2E.getPosisionFeedback()) + ",");
            out.write(df.format(SL2E.getMotorFeedback()) + ",");
        } catch (IOException e) {
            log.error(e);
        }
    }

    private static class FeedbackConnectionListener implements PropertyChangeListener {
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getPropertyName().equals(BlakEcubtcuFeedbackClient.CONNECTION)
                    && ((Boolean) evt.getNewValue()).equals(false)) {
                log.error("Closing: Connection with RT/Notification server is lost, Blak needs to quit!");
                //System.exit(0);
            }
        }
    }


    @Override
    public void addPropertyChangeListener(PropertyChangeListener pListener) {
        mPropertyChangeSupport.addPropertyChangeListener(pListener);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener pListener) {
        mPropertyChangeSupport.removePropertyChangeListener(pListener);
    }

    @Override
    public void propertyChange(PropertyChangeEvent pEvent) {
        //log.info("Property change received: %s - Changed from %s to %s", pEvent.getPropertyName(), pEvent.getOldValue(),
        //         pEvent.getNewValue());

        //if (!mAcquiring)
        // {
        //  log.debug("No acquisition for the moment, discarding event");
        //  return;
        //}

        //if (DegraderBeamStop.DBS_CURRENT.equals(pEvent.getPropertyName()))
        //{
        //log.debug("Current value changed during acquisition, using this value");
        //if (!mDeflector.isArcDetected())
        //{
        //Double beamCurrent = (Double) pEvent.getNewValue();
        //log.debug("Beam current value to set: %s", beamCurrent);
        //  mFilteredBeamCurrent.increment(beamCurrent);

        // log.debug("Sample nbr = %d / %d", mFilteredBeamCurrent.getN(), mNumberOfSamples);
        // if (mFilteredBeamCurrent.getN() == mNumberOfSamples)
        // {
        // log.debug("All samples captured, completing acquisition...");
        //completeBeamCurrentAcquisition();
        // }
        //}
        //}
    }


    private PropertyChangeSupport mPropertyChangeSupport = new PropertyChangeSupport(this);


    public PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        PropertySourcesPlaceholderConfigurer properties = new PropertySourcesPlaceholderConfigurer();
        properties.setLocation(new FileSystemResource("room.properties"));
        properties.setIgnoreResourceNotFound(false);
        return properties;
    }
}
