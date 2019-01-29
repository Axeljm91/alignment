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

import java.awt.*;
import java.beans.PropertyChangeListener;
import com.iba.blak.device.impl.*;
import com.iba.icomp.core.property.PropertyChangeProvider;
import com.iba.pts.bms.bss.bps.devices.impl.DegraderBeamStopProxy;

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

import com.opcOpenInterface.Rest;
import org.apache.log4j.Logger;
import org.sikuli.basics.Settings;
import org.sikuli.script.Screen;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import javax.swing.*;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeSupport;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.ConnectException;
import java.util.concurrent.TimeoutException;


/* This object manages the interface b/w the hardware and the rest of the software
 * For the moment, some parameters are hardcoded => to be externalized in future developments
 */

public class Controller implements PropertyChangeListener, PropertyChangeProvider {
	/**
	 * The logger. See http://logging.apache.org/log4j/1.2/index.html for more information.
	 */
	private static org.apache.log4j.Logger log=Logger.getLogger(new Throwable().getStackTrace()[0].getClassName());

    private static DegraderLegacyBeamProfileMonitor    P1E;
    private static LegacyBeamProfileMonitorImpl        P2E;
    static public ContinuousDegrader                   DEGRADER;
    static public DegraderBeamStopProxy                BEAMSTOP;
    static public BeamCurrentMonitor                   BCM1E;
    public static BeamStop                             S1E;
    public static BeamStop                             S2E;
    private static Slit                                SL1E, SL2E;
    // ESS
    private static EcubtcuQuadrupole                   Q1E, Q2E, Q3E, Q47E, Q56E, Q8E, Q9E, Q10E;
    private static EcubtcuDipole                       B1234E;
    // TR1
    private static EcubtcuQuadrupole                   Q1B1, Q2B1, Q3B1, Q1F1, Q2F1, Q3F1, Q1N1, Q2N1;
    private static EcubtcuDipole                       B12B1;
    // SS1
    private static EcubtcuQuadrupole                   Q1S1, Q2S1, Q3S1, Q4S1, Q5S1;
    // TR2
    private static EcubtcuQuadrupole                   Q1B2, Q2B2, Q3B2, Q1F2, Q2F2, Q3F2, Q1N2, Q2N2, Q1I2, Q2I2, Q3I2, Q4I2, Q5I2;
    private static EcubtcuDipole                       B12B2, B1I2, B2I2;
    // SS2
    private static EcubtcuQuadrupole                   Q1S2, Q2S2, Q3S2, Q4S2, Q5S2;
    // TR3
    private static EcubtcuQuadrupole                   Q1B3, Q2B3, Q3B3, Q1F3, Q2F3, Q3F3, Q1I3, Q2I3, Q3I3, Q4I3, Q5I3;
    private static EcubtcuDipole                       B12B3, B1I3, B2I3;
    // SS3
    private static EcubtcuQuadrupole                   Q1S3, Q2S3, Q3S3, Q4S3, Q5S3;
    // TR4
    private static EcubtcuQuadrupole                   Q1B4, Q2B4, Q3B4, Q1G4, Q2G4, Q3G4, Q4G4, Q5G4, Q1N4, Q2N4;
    private static EcubtcuDipole                       B12B4, B1G4, B2G4;

//    private EcubtcuQuadrupole[]                 BP1Quads   = new EcubtcuQuadrupole[] {Q1B1, Q2B1, Q3B1, Q1F1, Q2F1, Q3F1, Q1N1, Q2N1};
//    private EcubtcuDipole[]                     BP1Dipoles = new EcubtcuDipole[] {B12B1};
//    private EcubtcuQuadrupole[]                 BP2Quads   = new EcubtcuQuadrupole[] {Q1S1, Q2S1, Q3S1, Q4S1, Q5S1, Q1B2, Q2B2, Q3B2, Q1F2, Q2F2, Q3F2, Q1N2, Q2N2};
//    private EcubtcuDipole[]                     BP2Dipoles = new EcubtcuDipole[] {B12B2};
//    private EcubtcuQuadrupole[]                 BP3Quads   = new EcubtcuQuadrupole[] {Q1S1, Q2S1, Q3S1, Q4S1, Q5S1, Q1B2, Q2B2, Q3B2, Q1I2, Q2I2, Q3I2, Q4I2, Q5I2, Q1N2, Q2N2};
//    private EcubtcuDipole[]                     BP3Dipoles = new EcubtcuDipole[] {B12B2, B1I2, B2I2};
//    private EcubtcuQuadrupole[]                 BP4Quads   = new EcubtcuQuadrupole[] {Q1S1, Q2S1, Q3S1, Q4S1, Q5S1, Q1S2, Q2S2, Q3S2, Q4S2, Q5S2, Q1B3, Q2B3, Q3B3, Q1F3, Q2F3, Q3F3};
//    private EcubtcuDipole[]                     BP4Dipoles = new EcubtcuDipole[] {B12B3};
//    private EcubtcuQuadrupole[]                 BP5Quads   = new EcubtcuQuadrupole[] {Q1S1, Q2S1, Q3S1, Q4S1, Q5S1, Q1S2, Q2S2, Q3S2, Q4S2, Q5S2, Q1B3, Q2B3, Q3B3, Q1I3, Q2I3, Q3I3, Q4I3, Q5I3};
//    private EcubtcuDipole[]                     BP5Dipoles = new EcubtcuDipole[] {B12B3, B1I3, B2I3};
//    private EcubtcuQuadrupole[]                 BP6Quads   = new EcubtcuQuadrupole[] {Q1S1, Q2S1, Q3S1, Q4S1, Q5S1, Q1S2, Q2S2, Q3S2, Q4S2, Q5S2, Q1S3, Q2S3, Q3S3, Q4S3, Q5S3, Q1B4, Q2B4, Q3B4, Q1G4, Q2G4, Q3G4, Q4G4, Q5G4, Q1N4, Q2N4};
//    private EcubtcuDipole[]                     BP6Dipoles = new EcubtcuDipole[] {B12B4, B1G4, B2G4};

	private double G11X, G11Y, G12X, G12Y, G21X, G21Y, G22X, G22Y;
    private double[]    mTargets, mTolerances, mSigmaTarget, mSigmaTolerance, mSafeCurrents;
    private double[]    mPositions    = new double[4];
    private double[]    mSigmas       = new double[4];
    private double[]    mCurrents     = new double[4];
    private Status      mStatus;
	private SiteManager siteManager;
	private TagManager  tagManager;
	private String mNotifServerAddress ;
    private Rest restManager;

    static public Field field[];
	
	private boolean isCommandNotifPort = false;
	private int mNotifPort = 16540;
	
	private String mEcubtcuAddress;

    private boolean mFilamentWarningDisplayed  = false;
    private boolean mDeflectorWarningDisplayed = false;
    private boolean mDeflectorErrorDisplayed   = false;

    static public Device                llrf;

    static public Device                acu;
    static public BcreuHttpDevice       bcreu;
    static public BeamLine              beamLine    = new BeamLine();
    static public blakEcubtcuClient     ecubtcu;
    //static public LegacyBeam            legacyBeam  = new LegacyBeam();
    static public Beam                  beam        = new Beam();
    static public blakNotifFeedbackClient feedbackClient = new blakNotifFeedbackClient();

    public static final int BLOCK_BS = 0;
    public static final int BLOCK_BPM = 1;
    public static final int BLOCK_PT = 2;
 //   static private BlakRtClient     rtClient;

    static public int               siteID;

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
	    mins[0] = mDeflectorVoltage - 0.1;
	    mins[1] = mDeflectorVoltage - 0.5;

	    return mins;
    }

    public void setNotifPort(int nPort)
	{
		mNotifPort = nPort;
		isCommandNotifPort = true;
	}

	public void initialize(){
		this.siteManager = SiteManager.getSiteManager();
		this.tagManager = TagManager.getTagManager();
		log.debug("Loading the tags");
//	    tagManager.buildTagsFromStream(ClassLoader.getSystemResourceAsStream("xml/tags.xml"));
		
		try
	      {
			Resource res = new ClassPathResource("xml/tags.xml");
			tagManager.buildTagsFromStream(res.getInputStream());
	      }
	      catch (IOException e)
	      {
	         log.info("Read file error: " + e.getMessage());
	         return;
	      }
		
		log.debug("Loading the sites");
		
		Site firstSite;


				
//		FileInputStream siteConfig = new FileInputStream("./siteconfig.xml");
		try
	      {
//			FileInputStream siteConfig = new FileInputStream("./siteconfig-local.xml");
			FileInputStream siteConfig = new FileInputStream("./siteconfig.xml");
		//	Resource res = new ClassPathResource("sites/siteconfig.xml");
		//	siteManager.buildSitesFromStream(res.getInputStream());
			siteManager.buildSitesFromStream(siteConfig);
			if (siteManager.getSites().size() > 1) {
                log.info("there are more than 1 site in the siteConfig.xml, the default site would be the first site in the file.");
            }
			
			firstSite = siteManager.getFirstSite();
            
            siteName    = firstSite.getName();
            siteID      = firstSite.getID();
            siteCode    = firstSite.getCode();
            G11X = firstSite.getG11x();
            G12X = firstSite.getG12x();
            G21X = firstSite.getG21x();
            G22X = firstSite.getG22x();
            G11Y = firstSite.getG11y();
            G12Y = firstSite.getG12y();
            G21Y = firstSite.getG21y();
            G22Y = firstSite.getG22y();
            mNotifServerAddress = firstSite.getNotifServerAddress() ;
            if(!isCommandNotifPort)
            {
            	mNotifPort =firstSite.getNotifPort() ;
            	log.info("get notif port from siteConfig.xml which is " + mNotifPort);
            }
            else
            {
            	log.info("get notif port from command line which is " + mNotifPort);
            }
            
            mEcubtcuAddress   = firstSite.getEcubtcuAddress();
            mTargets          = firstSite.getBpmTargets();
            mTolerances       = firstSite.getBpmTolerances();
            mSigmaTarget      = firstSite.getSigmaTargets();
            mSigmaTolerance   = firstSite.getSigmaTolerances();
            mScreen           = firstSite.getScreen();
            mSafeCurrents     = firstSite.getSafeCurrents();
            mMaxApply         = firstSite.getMaxCurrentChange();
            mDeflectorVoltage = firstSite.getDeflectorVoltage();

	      }
		catch (FileNotFoundException e)
		{
			log.info("Cannot find the file siteConfig.xml." );
			return ;
		}
		
		log.debug("Loading beamline from " + firstSite.getBeamLine());
		
		
		
		
		  
		
		try
		{
			beamLine.loadFromFile(firstSite.getBeamLine());
			
            ecubtcu = new blakEcubtcuClient();
            ecubtcu.setEcubtcuAddress(mEcubtcuAddress);            
            //            Resource ecubtcuResource = new ClassPathResource("x/ecubtcu" + siteCode + ".x");
            Resource ecubtcuResource = new ClassPathResource("x/ecubtcu.x");
            ((blakEcubtcuClient) ecubtcu).setRpcProgrameFile(ecubtcuResource);
            
            feedbackClient.setNotifServerAddress(mNotifServerAddress);
            feedbackClient.setNotifServerPort(mNotifPort);
            setFeedbackClient();
		}
		catch (FileNotFoundException e)
        {
           log.error("File not found : " + firstSite.getBeamLine());
        }
        catch (IOException e)
        {
           log.error("Error reading " + firstSite.getBeamLine() + " file.");
        }
		catch (Exception e)
		{
			log.error("set rpc program file error");
		}


		//llrf = siteManager.getSite(siteName).getDevice("llrf");

        acu = siteManager.getSite(siteName).getDevice("acu230");

        bcreu = (BcreuHttpDevice) siteManager.getSite(siteName).getDevice("bcreu-http");

        if(!acu.isConnected()) 
        	acu.connect();

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
            B1G4  = (EcubtcuDipole) beamLine.getElement("B1G4");
            B2G4  = (EcubtcuDipole) beamLine.getElement("B2G4");



//            for (int i = 19; i< 27; i++) {
//                log.error(field[i].getName());
//            }

        } catch (BeamLineElementNotFoundException e) {
            log.error("Not all necessary elements are defined: " + e);
        }

        field = Controller.class.getDeclaredFields();

        restManager = new Rest();

		log.info("Aye aye, Sir. I'll align your beam !");
        mStatus = new Status();
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
            mStatus.set(Status.PULSESOURCE, str, str.equalsIgnoreCase("Internal: Continuous"));
        } else {
            str = bcreu.getPulseSource(beam.isSinglePulseMode());
            //str = bcreu.getPulseSource();
            mStatus.set(Status.PULSESOURCE, str, str.equalsIgnoreCase("Internal: Continuous"));
        }
        mStatus.set(Status.BCREU_STATE, bcreu.getRunningState(), bcreu.getRunningStateColor(false));
        mStatus.set(Status.MAX_BEAM, bcreu.getMaxBeam(), 9.6d);
        mStatus.set(Status.IC_CYCLO, bcreu.getIcCyclo(), 4.3d);

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
    	
    	feedbackClient.retreiveMcrFeedbacks();
    	
        // BeamLine Elements
        mStatus.set(Status.P1E_STATUS, P1E);
        mStatus.set(Status.S2E_STATUS, S2E.getPosition());
        mStatus.set(Status.SL1E_STATUS, SL1E.getPosisionFeedback(), 49.0d);
        mStatus.set(Status.SL2E_STATUS, SL2E.getPosisionFeedback(), 49.0d);
        mStatus.set(Status.P2E_STATUS, P2E);
        Double dval = Math.abs(Q1E.getCurrentFeedback()) + Math.abs(Q2E.getCurrentFeedback()) + Math.abs(Q3E.getCurrentFeedback());
        Boolean bval = dval > 1;
        mStatus.set(Status.ESS_MAGNETS, bval ? "ON" : "OFF", !bval);


        mCurrents = getCurrents();
        // Magnets
        for (int i = 0; i < Status.Magnet_names.length; i++) {
            mStatus.set(i + Status.MAGNET_OFFSET, mCurrents[i], (Boolean) acu.getTagValue(Status.Magnet_reg[i]));
        }

        for (int i = 0; i < Status.Cyclo_read.length; i++) {
            mStatus.set(i + Status.CYCLO_OFFSET, (Double) acu.getTagValue(Status.Cyclo_read[i]));
        }

        //MC tuning tab
        for (int i = 0; i < Status.CycloTuning_read.length; i++) {
            mStatus.set(i + Status.CYCLOTUNING_OFFSET, (Double) acu.getTagValue(Status.CycloTuning_read[i]));
        }

        for (int i = 0; i < Status.LLRF_read.length; i++) {
            mStatus.set(i + Status.LLRFTUNING_OFFSET, (Double) acu.getTagValue(Status.LLRF_read[i]));
        }

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
            if (P2E.getPosition() != Insertable.Position.RETRACTED){
            	ecubtcu.bpmRetract("P2E");
            }

            // Added for 30 degree beamlines, set-range will fail
            // without explicit in command on S2E since there is no TR BS at 30
            ecubtcu.bsInsert("S2E");
            beam.bpsController.startIdleActivity();
            beam.bpsController.proxyPublish();


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


	public void prepareForAlignment(){
        try
        {
            if (!ecubtcu.isConnected()) {
                ecubtcu.connect();
                Thread.sleep(100);
            }
            if (!bcreu.isConnected()) {
                bcreu.connect();
                Thread.sleep(100);
            }
            if (!mStatus.getBool(Status.P1E_STATUS)) {
                if (mStatus.getString(Status.P1E_STATUS).equalsIgnoreCase("UNKNOWN")){
//                    P1E.getDegrader().goHome();
                	ecubtcu.degraderGoHome();
                } 
                else 
                {
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
            if (!mStatus.getBool(Status.P2E_STATUS)) {
//                P2E.insert();
            	ecubtcu.bpmInsert("P2E");
            	
            }
            // for this part, need to clear with site how to prepare beam. please refer to the code in BCP.
            if (!beam.bcreu.isLookupValid()) 
            {
                log.info("Performing BCREU/ISEU lookup");
                beam.bpsController.startPrepareActivity(-1, 10.0);
                beam.bpsController.proxyPublish();

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


    public void prepareForTune(){
        try
        {
            if (!ecubtcu.isConnected()) {
                ecubtcu.connect();
                Thread.sleep(100);
            }
            if (!bcreu.isConnected()) {
                bcreu.connect();
                Thread.sleep(100);
            }

            ecubtcu.degraderGoSpecialBlock(BLOCK_BS);

            while (DEGRADER.getStatus() != Degrader.STATUS_AT_BS) {
                Thread.sleep(100);
            }

            //Set max voltage to satisfy LLRF proxy req
            if (beam.llrf.getMaxVoltage() != 56) {
                beam.llrf.setMaxVoltage(56);
                log.warn("LLRF max voltage set to " + beam.llrf.getMaxVoltage() + "kV");
            }

            //log.warn(beam.llrf.isRfOn());
            //log.warn(beam.llrf.isRfStandby());

            //Set VDee2 voltage
            if (beam.llrf.getDeeVoltage2() < 55.8) {
                beam.llrf.setDeeVoltage2(56.00);
                log.warn("VDee2 set to 56.00kV");
                beam.llrf.proxyPublish();
            }

            //Read back VDee2 voltage
            if (beam.llrf.getDeeVoltage2() > 55.8){
                log.warn("VDee2 is at " + beam.llrf.getDeeVoltage2() + "kV");
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




    /** arraySubtract performs an element-by-element subtraction of two arrays of the same size.
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

    public double[] getPositions(){
        return mPositions;
    }

    public double[] getSigmas(){
        return mSigmas;
    }

    public double[] getTargets(){
        return mTargets;
    }

    public double[] getTolerances(){
        return mTolerances;
    }

    public double[] getSigmaTargets(){
        return mSigmaTarget;
    }

    public double[] getSigmaTolerances(){
        return mSigmaTolerance;
    }

    public double[] getSafeCurrents(){
        return mSafeCurrents;
    }

    public double[] getAdjustedPositions(){
        return arraySubtract(mPositions, mTargets);
    }

    //Legacy with BPM inversion
    public double[] getOperands(){
        double[] adjMeans = getAdjustedPositions();

        double[] operands = new double[4];
        operands[0] = (adjMeans[1] - G21X*(adjMeans[3] - (G12X*adjMeans[1]/G11X))/((-G12X*G21X/G11X)+G22X))/G11X;
        operands[1] = ((adjMeans[0] - G21Y*(adjMeans[2] - (G12Y*adjMeans[0]/G11Y))/((-G12Y*G21Y/G11Y)+G22Y))/G11Y);
        operands[2] = ((adjMeans[3] - (G12X*adjMeans[1]/G11X))/((-G12X*G21X/G11X)+G22X));
        operands[3] = (adjMeans[2] - (G12Y*adjMeans[0]/G11Y))/((-G12Y*G21Y/G11Y)+G22Y);

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


    public double[] computeCurrents(){
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
	
	public boolean setPositions(double[] positions){
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

    public boolean isSystemManual() {
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

	public boolean refreshAll(){
        try {
//        if (!feedbackClient.isConnected()) {
//            feedbackClient.connect();
//        }

        if (!ecubtcu.isConnected()) {
        	ecubtcu.connect();
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


        //P1E.startContinuousAcquisition();
        //P2E.startContinuousAcquisition();


            Thread BPM1 = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        P1E.acquireProfile();
                    } catch (Exception f) {
                        log.error("Error acquiring BPM profile");
                        f.printStackTrace();
                    }
                }
            });
            BPM1.start();

            Thread BPM2 = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        P2E.acquireProfile();

                    } catch (Exception f) {
                        log.error("Error acquiring BPM profile");
                        f.printStackTrace();
                    }
                }
            });
            BPM2.start();


            //P1E.acquireProfile();
            //P2E.acquireProfile();
            //Thread.sleep(500);
            while (BPM1.isAlive())
            {
                Thread.sleep(100);
            }
            Thread.sleep(500);
            double temp[] = new double[4];
            double tempSigma[] = new double[4];
            temp[0] = P1E.getHorizontalCentroid();
            temp[1] = P1E.getVerticalCentroid();
            tempSigma[0] = P1E.getHorizontalSigma();
            tempSigma[1] = P1E.getVerticalSigma();
            

            //P2E.acquireProfile();
            while (BPM2.isAlive())
            {
                Thread.sleep(100);
            }
            Thread.sleep(500);
            temp[2] = P2E.getHorizontalCentroid();
            temp[3] = P2E.getVerticalCentroid();
            tempSigma[2] = P2E.getHorizontalSigma();
            tempSigma[3] = P2E.getVerticalSigma();
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
       // catch (AcquisitionTimeoutException e) 
     //   {
     //       log.error(e);
    //        return false;
    //    } 
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

	public void useKeyCommand(int command){
        acu.setTagValue(mStatus.Key_command, command);
        log.info("Acu key command #" + command + " sent.");
    }

    public boolean sourceTuning(){
        if (!acu.isConnected()) {
            acu.connect();
        }
        return (boolean) acu.getTagValue(Status.sour_tuning);
    }

    public boolean mainCoilTuning(){
        return (boolean)acu.getTagValue(Status.mc_tuning);
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

    public int getSelectedBeamline(){
        int bp = 0;

        try{
            if (restManager.getVariable(Status.BP1_SELECTED).get("value").getAsBoolean()) {
                bp = 1;
            }
            if (restManager.getVariable(Status.BP2_SELECTED).get("value").getAsBoolean()) {
                bp = 2;
            }
            if (restManager.getVariable(Status.BP3_SELECTED).get("value").getAsBoolean()) {
                bp = 3;
            }
            if (restManager.getVariable(Status.BP4_SELECTED).get("value").getAsBoolean()) {
                bp = 4;
            }
            if (restManager.getVariable(Status.BP5_SELECTED).get("value").getAsBoolean()) {
                bp = 5;
            }
            if (restManager.getVariable(Status.BP6_SELECTED).get("value").getAsBoolean()) {
                bp = 6;
            }

        }catch (Exception e){
            e.printStackTrace();
            log.error("Error getting beamline");
        }

        return bp;
    }



    public boolean isRequestPending() {
        return (beam.beamScheduler.getPendingBeamRequests().size() != 0);
    }

    public boolean isBeamAllocated() {
        if (beam.beamScheduler.getCurrentBeamAllocation() == null){
            return false;
        }
        else {
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
            }
            else if (restManager.getVariable(Status.TR1_SECURE).isJsonNull()) {
                return restManager.getVariable(Status.TR1_SECURE).get("value").getAsBoolean();
            }

        }catch (Exception e){
            log.error(e);
        }

        return false;
    }

    public boolean isTR1Searching() {

        try {
            if (!restManager.getVariable(Status.TR1_SEARCH_ACTIVE).isJsonNull()) {
                return restManager.getVariable(Status.TR1_SEARCH_ACTIVE).get("value").getAsBoolean();
            }
            else if (restManager.getVariable(Status.TR1_SEARCH_ACTIVE).isJsonNull()) {
                return restManager.getVariable(Status.TR1_SEARCH_ACTIVE).get("value").getAsBoolean();
            }

        }catch (Exception e){
            log.error(e);
        }

        return false;
    }

    public boolean isTR2Secured() {

        try {
            if (!restManager.getVariable(Status.TR2_SECURE).isJsonNull()) {
                return restManager.getVariable(Status.TR2_SECURE).get("value").getAsBoolean();
            }
            else if (restManager.getVariable(Status.TR2_SECURE).isJsonNull()) {
                return restManager.getVariable(Status.TR2_SECURE).get("value").getAsBoolean();
            }

        }catch (Exception e){
            log.error(e);
        }

        return false;
    }

    public boolean isTR2Searching() {

        try {
            if (!restManager.getVariable(Status.TR2_SEARCH_ACTIVE).isJsonNull()) {
                return restManager.getVariable(Status.TR2_SEARCH_ACTIVE).get("value").getAsBoolean();
            }
            else if (restManager.getVariable(Status.TR2_SEARCH_ACTIVE).isJsonNull()) {
                return restManager.getVariable(Status.TR2_SEARCH_ACTIVE).get("value").getAsBoolean();
            }

        }catch (Exception e){
            log.error(e);
        }

        return false;
    }

    public boolean isTR3Secured() {

        try {
            if (!restManager.getVariable(Status.TR3_SECURE).isJsonNull()) {
                return restManager.getVariable(Status.TR3_SECURE).get("value").getAsBoolean();
            }
            else if (restManager.getVariable(Status.TR3_SECURE).isJsonNull()) {
                return restManager.getVariable(Status.TR3_SECURE).get("value").getAsBoolean();
            }

        }catch (Exception e){
            log.error(e);
        }

        return false;
    }

    public boolean isTR3Searching() {

        try {
            if (!restManager.getVariable(Status.TR3_SEARCH_ACTIVE).isJsonNull()) {
                return restManager.getVariable(Status.TR3_SEARCH_ACTIVE).get("value").getAsBoolean();
            }
            else if (restManager.getVariable(Status.TR3_SEARCH_ACTIVE).isJsonNull()) {
                return restManager.getVariable(Status.TR3_SEARCH_ACTIVE).get("value").getAsBoolean();
            }

        }catch (Exception e){
            log.error(e);
        }

        return false;
    }

    public boolean isTR4Secured() {

        try {
            if (!restManager.getVariable(Status.TR4_SECURE).isJsonNull()) {
                return restManager.getVariable(Status.TR4_SECURE).get("value").getAsBoolean();
            }
            else if (restManager.getVariable(Status.TR4_SECURE).isJsonNull()) {
                return restManager.getVariable(Status.TR4_SECURE).get("value").getAsBoolean();
            }

        }catch (Exception e){
            log.error(e);
        }

        return false;
    }

    public boolean isTR4Searching() {

        try {
            if (!restManager.getVariable(Status.TR4_SEARCH_ACTIVE).isJsonNull()) {
                return restManager.getVariable(Status.TR4_SEARCH_ACTIVE).get("value").getAsBoolean();
            }
            else if (restManager.getVariable(Status.TR4_SEARCH_ACTIVE).isJsonNull()) {
                return restManager.getVariable(Status.TR4_SEARCH_ACTIVE).get("value").getAsBoolean();
            }

        }catch (Exception e){
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

    public void setSlits() {
        try {

            if (SL1E.getPosisionFeedback() < 50.00 && SL1E.getPosisionSetpoint() != 50.0){
                ecubtcu.slitsGoMm("SL1E", 50.0);
            }

//            if (SL2E.getPosisionFeedback() > 2.00 && SL2E.getPosisionSetpoint() != 1.30){
//                ecubtcu.slitsGoMm("SL2E", 1.3);
//            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void getESSCurrents() {

       if (ecubtcu.isConnected()) {

           try {
               log.warn("B1234E: " + String.valueOf(B1234E.getCurrentFeedback()) + "A");

               log.warn("Q1E: " + String.valueOf(Q1E.getCurrentFeedback()) + "A");
               log.warn("Q2E: " + String.valueOf(Q2E.getCurrentFeedback()) + "A");
               log.warn("Q3E: " + String.valueOf(Q3E.getCurrentFeedback()) + "A");
               log.warn("Q47E: " + String.valueOf(Q47E.getCurrentFeedback()) + "A");
               log.warn("Q56E: " + String.valueOf(Q56E.getCurrentFeedback()) + "A");
               log.warn("Q8E: " + String.valueOf(Q8E.getCurrentFeedback()) + "A");
               log.warn("Q9E: " + String.valueOf(Q9E.getCurrentFeedback()) + "A");
               log.warn("Q10E: " + String.valueOf(Q10E.getCurrentFeedback()) + "A");
           }catch (Exception e) {
               log.error(e);
           }
       }
    }

    public void setESSCurrents() {

        if (!ecubtcu.isConnected()) {
            ecubtcu.connect();
        }

        try {
            if (B1234E.getCurrentFeedback() >= 100.0) {
                ecubtcu.canMagnetSetCurrent("B1234E", (B1234E.getCurrentFeedback() / 1.5));
                //log.warn("B1234E: " + String.valueOf(B1234E.getCurrentFeedback()) + "A");
            } else if (B1234E.getCurrentFeedback() < 100.0 && B1234E.getCurrentSetpoint() != 0.0){
                ecubtcu.canMagnetSetCurrent("B1234E", 0.0);
                log.warn("B1234E set to 0A");
            }

            if (Q1E.getCurrentFeedback() >= 40.0) {
                ecubtcu.canMagnetSetCurrent("Q1E" , (Q1E.getCurrentFeedback() / 1.5));
                //log.warn("Q1E: " + String.valueOf(Q1E.getCurrentFeedback()) + "A");
            } else if (Q1E.getCurrentFeedback() < 40.0 && Q1E.getCurrentSetpoint() != 0.0){
                ecubtcu.canMagnetSetCurrent("Q1E", 0.0);
                log.warn("Q1E set to 0A");
            }

            if (Q2E.getCurrentFeedback() >= 40.0) {
                ecubtcu.canMagnetSetCurrent("Q2E" , (Q2E.getCurrentFeedback() / 1.5));
                //log.warn("Q2E: " + String.valueOf(Q2E.getCurrentFeedback()) + "A");
            } else if (Q2E.getCurrentFeedback() < 40.0 && Q2E.getCurrentSetpoint() != 0.0){
                ecubtcu.canMagnetSetCurrent("Q2E", 0.0);
                log.warn("Q2E set to 0A");
            }

            if (Q3E.getCurrentFeedback() >= 40.0) {
                ecubtcu.canMagnetSetCurrent("Q3E" , (Q3E.getCurrentFeedback() / 1.5));
                //log.warn("Q3E: " + String.valueOf(Q3E.getCurrentFeedback()) + "A");
            } else if (Q3E.getCurrentFeedback() < 40.0 && Q3E.getCurrentSetpoint() != 0.0){
                ecubtcu.canMagnetSetCurrent("Q3E", 0.0);
                log.warn("Q3E set to 0A");
            }

            if (Q47E.getCurrentFeedback() >= 40.0) {
                ecubtcu.canMagnetSetCurrent("Q47E" , (Q47E.getCurrentFeedback() / 1.5));
                //log.warn("Q47E: " + String.valueOf(Q47E.getCurrentFeedback()) + "A");
            } else if (Q47E.getCurrentFeedback() < 40.0 && Q47E.getCurrentSetpoint() != 0.0){
                ecubtcu.canMagnetSetCurrent("Q47E", 0.0);
                log.warn("Q47E set to 0A");
            }

            if (Q56E.getCurrentFeedback() >= 40.0) {
                ecubtcu.canMagnetSetCurrent("Q56E" , (Q56E.getCurrentFeedback() / 1.5));
                //log.warn("Q56E: " + String.valueOf(Q56E.getCurrentFeedback()) + "A");
            } else if (Q56E.getCurrentFeedback() < 40.0 && Q56E.getCurrentSetpoint() != 0.0){
                ecubtcu.canMagnetSetCurrent("Q56E", 0.0);
                log.warn("Q56E set to 0A");
            }

            if (Q8E.getCurrentFeedback() >= 40.0) {
                ecubtcu.canMagnetSetCurrent("Q8E" , (Q8E.getCurrentFeedback() / 1.5));
                //log.warn("Q8E: " + String.valueOf(Q8E.getCurrentFeedback()) + "A");
            } else if (Q8E.getCurrentFeedback() < 40.0 && Q8E.getCurrentSetpoint() != 0.0){
                ecubtcu.canMagnetSetCurrent("Q8E", 0.0);
                log.warn("Q8E set to 0A");
            }

            if (Q9E.getCurrentFeedback() >= 40.0) {
                ecubtcu.canMagnetSetCurrent("Q9E" , (Q9E.getCurrentFeedback() / 1.5));
                //log.warn("Q9E: " + String.valueOf(Q9E.getCurrentFeedback()) + "A");
            } else if (Q9E.getCurrentFeedback() < 40.0 && Q9E.getCurrentSetpoint() != 0.0){
                ecubtcu.canMagnetSetCurrent("Q9E", 0.0);
                log.warn("Q9E set to 0A");
            }

            if (Q10E.getCurrentFeedback() >= 40.0) {
                ecubtcu.canMagnetSetCurrent("Q10E" , (Q10E.getCurrentFeedback() / 1.5));
                //log.warn("Q10E: " + String.valueOf(Q10E.getCurrentFeedback()) + "A");
            } else if (Q10E.getCurrentFeedback() < 40.0 && Q10E.getCurrentSetpoint() != 0.0){
                ecubtcu.canMagnetSetCurrent("Q10E", 0.0);
                log.warn("Q10E set to 0A");
            }

        }catch (EcubtcuException e) {
            log.error("ECUBTCU exception " + e);
            e.printStackTrace();
        }catch (NullPointerException e){
            log.error("Null ptr at setESSMagnets");
            e.printStackTrace();
        }finally {
            try {
                Thread.sleep(2000);
            }catch (InterruptedException e){
                e.printStackTrace();
            }
        }
    }

    public void setBeamlineCurrents(int bp) {
        try {
            switch (bp) {
                case 1:
                    if (Q1B1.getCurrentFeedback() >= 40.0) {
                        ecubtcu.canMagnetSetCurrent("Q1B1" , (Q1B1.getCurrentFeedback() / 1.5));
                    } else if (Q1B1.getCurrentFeedback() < 40.0 && Q1B1.getCurrentSetpoint() != 0.0){
                        ecubtcu.canMagnetSetCurrent("Q1B1", 0.0);
                        log.warn("Q1B1 set to 0A");
                    }
                    if (Q2B1.getCurrentFeedback() >= 40.0) {
                        ecubtcu.canMagnetSetCurrent("Q2B1" , (Q2B1.getCurrentFeedback() / 1.5));
                    } else if (Q2B1.getCurrentFeedback() < 40.0 && Q2B1.getCurrentSetpoint() != 0.0){
                        ecubtcu.canMagnetSetCurrent("Q2B1", 0.0);
                        log.warn("Q2B1 set to 0A");
                    }
                    if (Q3B1.getCurrentFeedback() >= 40.0) {
                        ecubtcu.canMagnetSetCurrent("Q3B1" , (Q3B1.getCurrentFeedback() / 1.5));
                    } else if (Q3B1.getCurrentFeedback() < 40.0 && Q3B1.getCurrentSetpoint() != 0.0){
                        ecubtcu.canMagnetSetCurrent("Q3B1", 0.0);
                        log.warn("Q3B1 set to 0A");
                    }
                    if (Q1F1.getCurrentFeedback() >= 40.0) {
                        ecubtcu.canMagnetSetCurrent("Q1F1" , (Q1F1.getCurrentFeedback() / 1.5));
                    } else if (Q1F1.getCurrentFeedback() < 40.0 && Q1F1.getCurrentSetpoint() != 0.0){
                        ecubtcu.canMagnetSetCurrent("Q1F1", 0.0);
                        log.warn("Q1F1 set to 0A");
                    }
                    if (Q2F1.getCurrentFeedback() >= 40.0) {
                        ecubtcu.canMagnetSetCurrent("Q2F1" , (Q2F1.getCurrentFeedback() / 1.5));
                    } else if (Q2F1.getCurrentFeedback() < 40.0 && Q2F1.getCurrentSetpoint() != 0.0){
                        ecubtcu.canMagnetSetCurrent("Q2F1", 0.0);
                        log.warn("Q2F1 set to 0A");
                    }
                    if (Q3F1.getCurrentFeedback() >= 40.0) {
                        ecubtcu.canMagnetSetCurrent("Q3F1" , (Q3F1.getCurrentFeedback() / 1.5));
                    } else if (Q3F1.getCurrentFeedback() < 40.0 && Q3F1.getCurrentSetpoint() != 0.0){
                        ecubtcu.canMagnetSetCurrent("Q3F1", 0.0);
                        log.warn("Q3F1 set to 0A");
                    }
                    if (Q1N1.getCurrentFeedback() >= 40.0) {
                        ecubtcu.canMagnetSetCurrent("Q1N1" , (Q1N1.getCurrentFeedback() / 1.5));
                    } else if (Q1N1.getCurrentFeedback() < 40.0 && Q1N1.getCurrentSetpoint() != 0.0){
                        ecubtcu.canMagnetSetCurrent("Q1N1", 0.0);
                        log.warn("Q1N1 set to 0A");
                    }
                    if (Q2N1.getCurrentFeedback() >= 40.0) {
                        ecubtcu.canMagnetSetCurrent("Q2N1" , (Q2N1.getCurrentFeedback() / 1.5));
                    } else if (Q2N1.getCurrentFeedback() < 40.0 && Q2N1.getCurrentSetpoint() != 0.0){
                        ecubtcu.canMagnetSetCurrent("Q2N1", 0.0);
                        log.warn("Q2N1 set to 0A");
                    }
                    if (B12B1.getCurrentFeedback() >= 100.0) {
                        ecubtcu.canMagnetSetCurrent("B12B1", (B12B1.getCurrentFeedback() / 1.5));
                    } else if (B12B1.getCurrentFeedback() < 100.0 && B12B1.getCurrentSetpoint() != 0.0){
                        ecubtcu.canMagnetSetCurrent("B12B1", 0.0);
                        log.warn("B12B1 set to 0A");
                    }
                    break;
                case 2:
                    if (Q1S1.getCurrentFeedback() >= 40.0) {
                        ecubtcu.canMagnetSetCurrent("Q1S1" , (Q1S1.getCurrentFeedback() / 1.5));
                    } else if (Q1S1.getCurrentFeedback() < 40.0 && Q1S1.getCurrentSetpoint() != 0.0){
                        ecubtcu.canMagnetSetCurrent("Q1S1", 0.0);
                        log.warn("Q1S1 set to 0A");
                    }
                    if (Q2S1.getCurrentFeedback() >= 40.0) {
                        ecubtcu.canMagnetSetCurrent("Q2S1" , (Q2S1.getCurrentFeedback() / 1.5));
                    } else if (Q2S1.getCurrentFeedback() < 40.0 && Q2S1.getCurrentSetpoint() != 0.0){
                        ecubtcu.canMagnetSetCurrent("Q2S1", 0.0);
                        log.warn("Q2S1 set to 0A");
                    }
                    if (Q3S1.getCurrentFeedback() >= 40.0) {
                        ecubtcu.canMagnetSetCurrent("Q3S1" , (Q3S1.getCurrentFeedback() / 1.5));
                    } else if (Q3S1.getCurrentFeedback() < 40.0 && Q3S1.getCurrentSetpoint() != 0.0){
                        ecubtcu.canMagnetSetCurrent("Q3S1", 0.0);
                        log.warn("Q3S1 set to 0A");
                    }
                    if (Q4S1.getCurrentFeedback() >= 40.0) {
                        ecubtcu.canMagnetSetCurrent("Q4S1" , (Q4S1.getCurrentFeedback() / 1.5));
                    } else if (Q4S1.getCurrentFeedback() < 40.0 && Q4S1.getCurrentSetpoint() != 0.0){
                        ecubtcu.canMagnetSetCurrent("Q4S1", 0.0);
                        log.warn("Q4S1 set to 0A");
                    }
                    if (Q5S1.getCurrentFeedback() >= 40.0) {
                        ecubtcu.canMagnetSetCurrent("Q5S1" , (Q5S1.getCurrentFeedback() / 1.5));
                    } else if (Q5S1.getCurrentFeedback() < 40.0 && Q5S1.getCurrentSetpoint() != 0.0){
                        ecubtcu.canMagnetSetCurrent("Q5S1", 0.0);
                        log.warn("Q5S1 set to 0A");
                    }
                    if (Q1B2.getCurrentFeedback() >= 40.0) {
                        ecubtcu.canMagnetSetCurrent("Q1B2" , (Q1B2.getCurrentFeedback() / 1.5));
                    } else if (Q1B2.getCurrentFeedback() < 40.0 && Q1B2.getCurrentSetpoint() != 0.0){
                        ecubtcu.canMagnetSetCurrent("Q1B2", 0.0);
                        log.warn("Q1B2 set to 0A");
                    }
                    if (Q2B2.getCurrentFeedback() >= 40.0) {
                        ecubtcu.canMagnetSetCurrent("Q1B2" , (Q1B2.getCurrentFeedback() / 1.5));
                    } else if (Q1B2.getCurrentFeedback() < 40.0 && Q1B2.getCurrentSetpoint() != 0.0){
                        ecubtcu.canMagnetSetCurrent("Q1B2", 0.0);
                        log.warn("Q1B2 set to 0A");
                    }
                    if (Q3B2.getCurrentFeedback() >= 40.0) {
                        ecubtcu.canMagnetSetCurrent("Q3B2" , (Q3B2.getCurrentFeedback() / 1.5));
                    } else if (Q3B2.getCurrentFeedback() < 40.0 && Q3B2.getCurrentSetpoint() != 0.0){
                        ecubtcu.canMagnetSetCurrent("Q3B2", 0.0);
                        log.warn("Q3B2 set to 0A");
                    }
                    if (Q1F2.getCurrentFeedback() >= 40.0) {
                        ecubtcu.canMagnetSetCurrent("Q1F2" , (Q1F2.getCurrentFeedback() / 1.5));
                    } else if (Q1F2.getCurrentFeedback() < 40.0 && Q1F2.getCurrentSetpoint() != 0.0){
                        ecubtcu.canMagnetSetCurrent("Q1F2", 0.0);
                        log.warn("Q1F2 set to 0A");
                    }
                    if (Q2F2.getCurrentFeedback() >= 40.0) {
                        ecubtcu.canMagnetSetCurrent("Q2F2" , (Q2F2.getCurrentFeedback() / 1.5));
                    } else if (Q2F2.getCurrentFeedback() < 40.0 && Q2F2.getCurrentSetpoint() != 0.0){
                        ecubtcu.canMagnetSetCurrent("Q2F2", 0.0);
                        log.warn("Q2F2 set to 0A");
                    }
                    if (Q3F2.getCurrentFeedback() >= 40.0) {
                        ecubtcu.canMagnetSetCurrent("Q3F2" , (Q3F2.getCurrentFeedback() / 1.5));
                    } else if (Q3F2.getCurrentFeedback() < 40.0 && Q3F2.getCurrentSetpoint() != 0.0){
                        ecubtcu.canMagnetSetCurrent("Q3F2", 0.0);
                        log.warn("Q3F2 set to 0A");
                    }
                    if (Q1N2.getCurrentFeedback() >= 40.0) {
                        ecubtcu.canMagnetSetCurrent("Q1N2" , (Q1N2.getCurrentFeedback() / 1.5));
                    } else if (Q1N2.getCurrentFeedback() < 40.0 && Q1N2.getCurrentSetpoint() != 0.0){
                        ecubtcu.canMagnetSetCurrent("Q1N2", 0.0);
                        log.warn("Q1N2 set to 0A");
                    }
                    if (Q2N2.getCurrentFeedback() >= 40.0) {
                        ecubtcu.canMagnetSetCurrent("Q2N2" , (Q2N2.getCurrentFeedback() / 1.5));
                    } else if (Q2N2.getCurrentFeedback() < 40.0 && Q2N2.getCurrentSetpoint() != 0.0){
                        ecubtcu.canMagnetSetCurrent("Q2N2", 0.0);
                        log.warn("Q2N2 set to 0A");
                    }
                    if (B12B2.getCurrentFeedback() >= 100.0) {
                        ecubtcu.canMagnetSetCurrent("B12B2", (B12B2.getCurrentFeedback() / 1.5));
                    } else if (B12B2.getCurrentFeedback() < 100.0 && B12B2.getCurrentSetpoint() != 0.0){
                        ecubtcu.canMagnetSetCurrent("B12B2", 0.0);
                        log.warn("B12B2 set to 0A");
                    }
                    break;
                case 3:
                    if (Q1S1.getCurrentFeedback() >= 40.0) {
                        ecubtcu.canMagnetSetCurrent("Q1S1" , (Q1S1.getCurrentFeedback() / 1.5));
                    } else if (Q1S1.getCurrentFeedback() < 40.0 && Q1S1.getCurrentSetpoint() != 0.0){
                        ecubtcu.canMagnetSetCurrent("Q1S1", 0.0);
                        log.warn("Q1S1 set to 0A");
                    }
                    if (Q2S1.getCurrentFeedback() >= 40.0) {
                        ecubtcu.canMagnetSetCurrent("Q2S1" , (Q2S1.getCurrentFeedback() / 1.5));
                    } else if (Q2S1.getCurrentFeedback() < 40.0 && Q2S1.getCurrentSetpoint() != 0.0){
                        ecubtcu.canMagnetSetCurrent("Q2S1", 0.0);
                        log.warn("Q2S1 set to 0A");
                    }
                    if (Q3S1.getCurrentFeedback() >= 40.0) {
                        ecubtcu.canMagnetSetCurrent("Q3S1" , (Q3S1.getCurrentFeedback() / 1.5));
                    } else if (Q3S1.getCurrentFeedback() < 40.0 && Q3S1.getCurrentSetpoint() != 0.0){
                        ecubtcu.canMagnetSetCurrent("Q3S1", 0.0);
                        log.warn("Q3S1 set to 0A");
                    }
                    if (Q4S1.getCurrentFeedback() >= 40.0) {
                        ecubtcu.canMagnetSetCurrent("Q4S1" , (Q4S1.getCurrentFeedback() / 1.5));
                    } else if (Q4S1.getCurrentFeedback() < 40.0 && Q4S1.getCurrentSetpoint() != 0.0){
                        ecubtcu.canMagnetSetCurrent("Q4S1", 0.0);
                        log.warn("Q4S1 set to 0A");
                    }
                    if (Q5S1.getCurrentFeedback() >= 40.0) {
                        ecubtcu.canMagnetSetCurrent("Q5S1" , (Q5S1.getCurrentFeedback() / 1.5));
                    } else if (Q5S1.getCurrentFeedback() < 40.0 && Q5S1.getCurrentSetpoint() != 0.0){
                        ecubtcu.canMagnetSetCurrent("Q5S1", 0.0);
                        log.warn("Q5S1 set to 0A");
                    }
                    if (Q1B2.getCurrentFeedback() >= 40.0) {
                        ecubtcu.canMagnetSetCurrent("Q1B2" , (Q1B2.getCurrentFeedback() / 1.5));
                    } else if (Q1B2.getCurrentFeedback() < 40.0 && Q1B2.getCurrentSetpoint() != 0.0){
                        ecubtcu.canMagnetSetCurrent("Q1B2", 0.0);
                        log.warn("Q1B2 set to 0A");
                    }
                    if (Q2B2.getCurrentFeedback() >= 40.0) {
                        ecubtcu.canMagnetSetCurrent("Q1B2" , (Q1B2.getCurrentFeedback() / 1.5));
                    } else if (Q1B2.getCurrentFeedback() < 40.0 && Q1B2.getCurrentSetpoint() != 0.0){
                        ecubtcu.canMagnetSetCurrent("Q1B2", 0.0);
                        log.warn("Q1B2 set to 0A");
                    }
                    if (Q3B2.getCurrentFeedback() >= 40.0) {
                        ecubtcu.canMagnetSetCurrent("Q3B2" , (Q3B2.getCurrentFeedback() / 1.5));
                    } else if (Q3B2.getCurrentFeedback() < 40.0 && Q3B2.getCurrentSetpoint() != 0.0){
                        ecubtcu.canMagnetSetCurrent("Q3B2", 0.0);
                        log.warn("Q3B2 set to 0A");
                    }
                    if (Q1I2.getCurrentFeedback() >= 40.0) {
                        ecubtcu.canMagnetSetCurrent("Q1I2" , (Q1I2.getCurrentFeedback() / 1.5));
                    } else if (Q1I2.getCurrentFeedback() < 40.0 && Q1I2.getCurrentSetpoint() != 0.0){
                        ecubtcu.canMagnetSetCurrent("Q1I2", 0.0);
                        log.warn("Q1I2 set to 0A");
                    }
                    if (Q2I2.getCurrentFeedback() >= 40.0) {
                        ecubtcu.canMagnetSetCurrent("Q2I2" , (Q2I2.getCurrentFeedback() / 1.5));
                    } else if (Q2I2.getCurrentFeedback() < 40.0 && Q2I2.getCurrentSetpoint() != 0.0){
                        ecubtcu.canMagnetSetCurrent("Q2I2", 0.0);
                        log.warn("Q2I2 set to 0A");
                    }
                    if (Q3I2.getCurrentFeedback() >= 40.0) {
                        ecubtcu.canMagnetSetCurrent("Q3I2" , (Q3I2.getCurrentFeedback() / 1.5));
                    } else if (Q3I2.getCurrentFeedback() < 40.0 && Q3I2.getCurrentSetpoint() != 0.0){
                        ecubtcu.canMagnetSetCurrent("Q3I2", 0.0);
                        log.warn("Q3I2 set to 0A");
                    }
                    if (Q4I2.getCurrentFeedback() >= 40.0) {
                        ecubtcu.canMagnetSetCurrent("Q4I2" , (Q4I2.getCurrentFeedback() / 1.5));
                    } else if (Q4I2.getCurrentFeedback() < 40.0 && Q4I2.getCurrentSetpoint() != 0.0){
                        ecubtcu.canMagnetSetCurrent("Q4I2", 0.0);
                        log.warn("Q4I2 set to 0A");
                    }
                    if (Q5I2.getCurrentFeedback() >= 40.0) {
                        ecubtcu.canMagnetSetCurrent("Q5I2" , (Q5I2.getCurrentFeedback() / 1.5));
                    } else if (Q5I2.getCurrentFeedback() < 40.0 && Q5I2.getCurrentSetpoint() != 0.0){
                        ecubtcu.canMagnetSetCurrent("Q5I2", 0.0);
                        log.warn("Q5I2 set to 0A");
                    }
                    if (Q1N2.getCurrentFeedback() >= 40.0) {
                        ecubtcu.canMagnetSetCurrent("Q1N2" , (Q1N2.getCurrentFeedback() / 1.5));
                    } else if (Q1N2.getCurrentFeedback() < 40.0 && Q1N2.getCurrentSetpoint() != 0.0){
                        ecubtcu.canMagnetSetCurrent("Q1N2", 0.0);
                        log.warn("Q1N2 set to 0A");
                    }
                    if (Q2N2.getCurrentFeedback() >= 40.0) {
                        ecubtcu.canMagnetSetCurrent("Q2N2" , (Q2N2.getCurrentFeedback() / 1.5));
                    } else if (Q2N2.getCurrentFeedback() < 40.0 && Q2N2.getCurrentSetpoint() != 0.0){
                        ecubtcu.canMagnetSetCurrent("Q2N2", 0.0);
                        log.warn("Q2N2 set to 0A");
                    }
                    if (B12B2.getCurrentFeedback() >= 100.0) {
                        ecubtcu.canMagnetSetCurrent("B12B2", (B12B2.getCurrentFeedback() / 1.5));
                    } else if (B12B2.getCurrentFeedback() < 100.0 && B12B2.getCurrentSetpoint() != 0.0){
                        ecubtcu.canMagnetSetCurrent("B12B2", 0.0);
                        log.warn("B12B2 set to 0A");
                    }
                    if (B1I2.getCurrentFeedback() >= 100.0) {
                        ecubtcu.canMagnetSetCurrent("B1I2", (B1I2.getCurrentFeedback() / 1.5));
                    } else if (B1I2.getCurrentFeedback() < 100.0 && B1I2.getCurrentSetpoint() != 0.0){
                        ecubtcu.canMagnetSetCurrent("B1I2", 0.0);
                        log.warn("B1I2 set to 0A");
                    }
                    if (B2I2.getCurrentFeedback() >= 100.0) {
                        ecubtcu.canMagnetSetCurrent("B2I2", (B2I2.getCurrentFeedback() / 1.5));
                    } else if (B2I2.getCurrentFeedback() < 100.0 && B2I2.getCurrentSetpoint() != 0.0){
                        ecubtcu.canMagnetSetCurrent("B2I2", 0.0);
                        log.warn("B2I2 set to 0A");
                    }
                    break;
                case 4:
                    if (Q1S1.getCurrentFeedback() >= 40.0) {
                        ecubtcu.canMagnetSetCurrent("Q1S1" , (Q1S1.getCurrentFeedback() / 1.5));
                    } else if (Q1S1.getCurrentFeedback() < 40.0 && Q1S1.getCurrentSetpoint() != 0.0){
                        ecubtcu.canMagnetSetCurrent("Q1S1", 0.0);
                        log.warn("Q1S1 set to 0A");
                    }
                    if (Q2S1.getCurrentFeedback() >= 40.0) {
                        ecubtcu.canMagnetSetCurrent("Q2S1" , (Q2S1.getCurrentFeedback() / 1.5));
                    } else if (Q2S1.getCurrentFeedback() < 40.0 && Q2S1.getCurrentSetpoint() != 0.0){
                        ecubtcu.canMagnetSetCurrent("Q2S1", 0.0);
                        log.warn("Q2S1 set to 0A");
                    }
                    if (Q3S1.getCurrentFeedback() >= 40.0) {
                        ecubtcu.canMagnetSetCurrent("Q3S1" , (Q3S1.getCurrentFeedback() / 1.5));
                    } else if (Q3S1.getCurrentFeedback() < 40.0 && Q3S1.getCurrentSetpoint() != 0.0){
                        ecubtcu.canMagnetSetCurrent("Q3S1", 0.0);
                        log.warn("Q3S1 set to 0A");
                    }
                    if (Q4S1.getCurrentFeedback() >= 40.0) {
                        ecubtcu.canMagnetSetCurrent("Q4S1" , (Q4S1.getCurrentFeedback() / 1.5));
                    } else if (Q4S1.getCurrentFeedback() < 40.0 && Q4S1.getCurrentSetpoint() != 0.0){
                        ecubtcu.canMagnetSetCurrent("Q4S1", 0.0);
                        log.warn("Q4S1 set to 0A");
                    }
                    if (Q5S1.getCurrentFeedback() >= 40.0) {
                        ecubtcu.canMagnetSetCurrent("Q5S1" , (Q5S1.getCurrentFeedback() / 1.5));
                    } else if (Q5S1.getCurrentFeedback() < 40.0 && Q5S1.getCurrentSetpoint() != 0.0){
                        ecubtcu.canMagnetSetCurrent("Q5S1", 0.0);
                        log.warn("Q5S1 set to 0A");
                    }
                    if (Q1S2.getCurrentFeedback() >= 40.0) {
                        ecubtcu.canMagnetSetCurrent("Q1S2" , (Q1S2.getCurrentFeedback() / 1.5));
                    } else if (Q1S2.getCurrentFeedback() < 40.0 && Q1S2.getCurrentSetpoint() != 0.0){
                        ecubtcu.canMagnetSetCurrent("Q1S2", 0.0);
                        log.warn("Q1S2 set to 0A");
                    }
                    if (Q2S2.getCurrentFeedback() >= 40.0) {
                        ecubtcu.canMagnetSetCurrent("Q2S2" , (Q2S2.getCurrentFeedback() / 1.5));
                    } else if (Q2S2.getCurrentFeedback() < 40.0 && Q2S2.getCurrentSetpoint() != 0.0){
                        ecubtcu.canMagnetSetCurrent("Q2S2", 0.0);
                        log.warn("Q2S2 set to 0A");
                    }
                    if (Q3S2.getCurrentFeedback() >= 40.0) {
                        ecubtcu.canMagnetSetCurrent("Q3S2" , (Q3S2.getCurrentFeedback() / 1.5));
                    } else if (Q3S2.getCurrentFeedback() < 40.0 && Q3S2.getCurrentSetpoint() != 0.0){
                        ecubtcu.canMagnetSetCurrent("Q3S2", 0.0);
                        log.warn("Q3S2 set to 0A");
                    }
                    if (Q4S2.getCurrentFeedback() >= 40.0) {
                        ecubtcu.canMagnetSetCurrent("Q4S2" , (Q4S2.getCurrentFeedback() / 1.5));
                    } else if (Q4S2.getCurrentFeedback() < 40.0 && Q4S2.getCurrentSetpoint() != 0.0){
                        ecubtcu.canMagnetSetCurrent("Q4S2", 0.0);
                        log.warn("Q4S2 set to 0A");
                    }
                    if (Q5S2.getCurrentFeedback() >= 40.0) {
                        ecubtcu.canMagnetSetCurrent("Q5S2" , (Q5S2.getCurrentFeedback() / 1.5));
                    } else if (Q5S2.getCurrentFeedback() < 40.0 && Q5S2.getCurrentSetpoint() != 0.0){
                        ecubtcu.canMagnetSetCurrent("Q5S2", 0.0);
                        log.warn("Q5S2 set to 0A");
                    }
                    if (Q1B3.getCurrentFeedback() >= 40.0) {
                        ecubtcu.canMagnetSetCurrent("Q1B3" , (Q1B3.getCurrentFeedback() / 1.5));
                    } else if (Q1B3.getCurrentFeedback() < 40.0 && Q1B3.getCurrentSetpoint() != 0.0){
                        ecubtcu.canMagnetSetCurrent("Q1B3", 0.0);
                        log.warn("Q1B3 set to 0A");
                    }
                    if (Q2B3.getCurrentFeedback() >= 40.0) {
                        ecubtcu.canMagnetSetCurrent("Q2B3" , (Q2B3.getCurrentFeedback() / 1.5));
                    } else if (Q2B3.getCurrentFeedback() < 40.0 && Q2B3.getCurrentSetpoint() != 0.0){
                        ecubtcu.canMagnetSetCurrent("Q2B3", 0.0);
                        log.warn("Q2B3 set to 0A");
                    }
                    if (Q3B3.getCurrentFeedback() >= 40.0) {
                        ecubtcu.canMagnetSetCurrent("Q3B3" , (Q3B3.getCurrentFeedback() / 1.5));
                    } else if (Q3B3.getCurrentFeedback() < 40.0 && Q3B3.getCurrentSetpoint() != 0.0){
                        ecubtcu.canMagnetSetCurrent("Q3B3", 0.0);
                        log.warn("Q3B3 set to 0A");
                    }
                    if (Q1F3.getCurrentFeedback() >= 40.0) {
                        ecubtcu.canMagnetSetCurrent("Q1F3" , (Q1F3.getCurrentFeedback() / 1.5));
                    } else if (Q1F3.getCurrentFeedback() < 40.0 && Q1F3.getCurrentSetpoint() != 0.0){
                        ecubtcu.canMagnetSetCurrent("Q1F3", 0.0);
                        log.warn("Q1F3 set to 0A");
                    }
                    if (Q2F3.getCurrentFeedback() >= 40.0) {
                        ecubtcu.canMagnetSetCurrent("Q2F3" , (Q2F3.getCurrentFeedback() / 1.5));
                    } else if (Q2F3.getCurrentFeedback() < 40.0 && Q2F3.getCurrentSetpoint() != 0.0){
                        ecubtcu.canMagnetSetCurrent("Q2F3", 0.0);
                        log.warn("Q2F3 set to 0A");
                    }
                    if (Q3F3.getCurrentFeedback() >= 40.0) {
                        ecubtcu.canMagnetSetCurrent("Q3F3" , (Q3F3.getCurrentFeedback() / 1.5));
                    } else if (Q3F3.getCurrentFeedback() < 40.0 && Q3F3.getCurrentSetpoint() != 0.0){
                        ecubtcu.canMagnetSetCurrent("Q3F3", 0.0);
                        log.warn("Q3F3 set to 0A");
                    }
                    if (B12B3.getCurrentFeedback() >= 100.0) {
                        ecubtcu.canMagnetSetCurrent("B12B3", (B12B3.getCurrentFeedback() / 1.5));
                    } else if (B12B3.getCurrentFeedback() < 100.0 && B12B3.getCurrentSetpoint() != 0.0){
                        ecubtcu.canMagnetSetCurrent("B12B3", 0.0);
                        log.warn("B12B3 set to 0A");
                    }
                    break;
                case 5:
                    if (Q1S1.getCurrentFeedback() >= 40.0) {
                        ecubtcu.canMagnetSetCurrent("Q1S1" , (Q1S1.getCurrentFeedback() / 1.5));
                    } else if (Q1S1.getCurrentFeedback() < 40.0 && Q1S1.getCurrentSetpoint() != 0.0){
                        ecubtcu.canMagnetSetCurrent("Q1S1", 0.0);
                        log.warn("Q1S1 set to 0A");
                    }
                    if (Q2S1.getCurrentFeedback() >= 40.0) {
                        ecubtcu.canMagnetSetCurrent("Q2S1" , (Q2S1.getCurrentFeedback() / 1.5));
                    } else if (Q2S1.getCurrentFeedback() < 40.0 && Q2S1.getCurrentSetpoint() != 0.0){
                        ecubtcu.canMagnetSetCurrent("Q2S1", 0.0);
                        log.warn("Q2S1 set to 0A");
                    }
                    if (Q3S1.getCurrentFeedback() >= 40.0) {
                        ecubtcu.canMagnetSetCurrent("Q3S1" , (Q3S1.getCurrentFeedback() / 1.5));
                    } else if (Q3S1.getCurrentFeedback() < 40.0 && Q3S1.getCurrentSetpoint() != 0.0){
                        ecubtcu.canMagnetSetCurrent("Q3S1", 0.0);
                        log.warn("Q3S1 set to 0A");
                    }
                    if (Q4S1.getCurrentFeedback() >= 40.0) {
                        ecubtcu.canMagnetSetCurrent("Q4S1" , (Q4S1.getCurrentFeedback() / 1.5));
                    } else if (Q4S1.getCurrentFeedback() < 40.0 && Q4S1.getCurrentSetpoint() != 0.0){
                        ecubtcu.canMagnetSetCurrent("Q4S1", 0.0);
                        log.warn("Q4S1 set to 0A");
                    }
                    if (Q5S1.getCurrentFeedback() >= 40.0) {
                        ecubtcu.canMagnetSetCurrent("Q5S1" , (Q5S1.getCurrentFeedback() / 1.5));
                    } else if (Q5S1.getCurrentFeedback() < 40.0 && Q5S1.getCurrentSetpoint() != 0.0){
                        ecubtcu.canMagnetSetCurrent("Q5S1", 0.0);
                        log.warn("Q5S1 set to 0A");
                    }
                    if (Q1S2.getCurrentFeedback() >= 40.0) {
                        ecubtcu.canMagnetSetCurrent("Q1S2" , (Q1S2.getCurrentFeedback() / 1.5));
                    } else if (Q1S2.getCurrentFeedback() < 40.0 && Q1S2.getCurrentSetpoint() != 0.0){
                        ecubtcu.canMagnetSetCurrent("Q1S2", 0.0);
                        log.warn("Q1S2 set to 0A");
                    }
                    if (Q2S2.getCurrentFeedback() >= 40.0) {
                        ecubtcu.canMagnetSetCurrent("Q2S2" , (Q2S2.getCurrentFeedback() / 1.5));
                    } else if (Q2S2.getCurrentFeedback() < 40.0 && Q2S2.getCurrentSetpoint() != 0.0){
                        ecubtcu.canMagnetSetCurrent("Q2S2", 0.0);
                        log.warn("Q2S2 set to 0A");
                    }
                    if (Q3S2.getCurrentFeedback() >= 40.0) {
                        ecubtcu.canMagnetSetCurrent("Q3S2" , (Q3S2.getCurrentFeedback() / 1.5));
                    } else if (Q3S2.getCurrentFeedback() < 40.0 && Q3S2.getCurrentSetpoint() != 0.0){
                        ecubtcu.canMagnetSetCurrent("Q3S2", 0.0);
                        log.warn("Q3S2 set to 0A");
                    }
                    if (Q4S2.getCurrentFeedback() >= 40.0) {
                        ecubtcu.canMagnetSetCurrent("Q4S2" , (Q4S2.getCurrentFeedback() / 1.5));
                    } else if (Q4S2.getCurrentFeedback() < 40.0 && Q4S2.getCurrentSetpoint() != 0.0){
                        ecubtcu.canMagnetSetCurrent("Q4S2", 0.0);
                        log.warn("Q4S2 set to 0A");
                    }
                    if (Q5S2.getCurrentFeedback() >= 40.0) {
                        ecubtcu.canMagnetSetCurrent("Q5S2" , (Q5S2.getCurrentFeedback() / 1.5));
                    } else if (Q5S2.getCurrentFeedback() < 40.0 && Q5S2.getCurrentSetpoint() != 0.0){
                        ecubtcu.canMagnetSetCurrent("Q5S2", 0.0);
                        log.warn("Q5S2 set to 0A");
                    }
                    if (Q1B3.getCurrentFeedback() >= 40.0) {
                        ecubtcu.canMagnetSetCurrent("Q1B3" , (Q1B3.getCurrentFeedback() / 1.5));
                    } else if (Q1B3.getCurrentFeedback() < 40.0 && Q1B3.getCurrentSetpoint() != 0.0){
                        ecubtcu.canMagnetSetCurrent("Q1B3", 0.0);
                        log.warn("Q1B3 set to 0A");
                    }
                    if (Q2B3.getCurrentFeedback() >= 40.0) {
                        ecubtcu.canMagnetSetCurrent("Q2B3" , (Q2B3.getCurrentFeedback() / 1.5));
                    } else if (Q2B3.getCurrentFeedback() < 40.0 && Q2B3.getCurrentSetpoint() != 0.0){
                        ecubtcu.canMagnetSetCurrent("Q2B3", 0.0);
                        log.warn("Q2B3 set to 0A");
                    }
                    if (Q3B3.getCurrentFeedback() >= 40.0) {
                        ecubtcu.canMagnetSetCurrent("Q3B3" , (Q3B3.getCurrentFeedback() / 1.5));
                    } else if (Q3B3.getCurrentFeedback() < 40.0 && Q3B3.getCurrentSetpoint() != 0.0){
                        ecubtcu.canMagnetSetCurrent("Q3B3", 0.0);
                        log.warn("Q3B3 set to 0A");
                    }
                    if (Q1I3.getCurrentFeedback() >= 40.0) {
                        ecubtcu.canMagnetSetCurrent("Q1I3" , (Q1I3.getCurrentFeedback() / 1.5));
                    } else if (Q1I3.getCurrentFeedback() < 40.0 && Q1I3.getCurrentSetpoint() != 0.0){
                        ecubtcu.canMagnetSetCurrent("Q1I3", 0.0);
                        log.warn("Q1I3 set to 0A");
                    }
                    if (Q2I3.getCurrentFeedback() >= 40.0) {
                        ecubtcu.canMagnetSetCurrent("Q2I3" , (Q2I3.getCurrentFeedback() / 1.5));
                    } else if (Q2I3.getCurrentFeedback() < 40.0 && Q2I3.getCurrentSetpoint() != 0.0){
                        ecubtcu.canMagnetSetCurrent("Q2I3", 0.0);
                        log.warn("Q2I3 set to 0A");
                    }
                    if (Q3I3.getCurrentFeedback() >= 40.0) {
                        ecubtcu.canMagnetSetCurrent("Q3I3" , (Q3I3.getCurrentFeedback() / 1.5));
                    } else if (Q3I3.getCurrentFeedback() < 40.0 && Q3I3.getCurrentSetpoint() != 0.0){
                        ecubtcu.canMagnetSetCurrent("Q3I3", 0.0);
                        log.warn("Q3I3 set to 0A");
                    }
                    if (Q4I3.getCurrentFeedback() >= 40.0) {
                        ecubtcu.canMagnetSetCurrent("Q4I3" , (Q4I3.getCurrentFeedback() / 1.5));
                    } else if (Q4I3.getCurrentFeedback() < 40.0 && Q4I3.getCurrentSetpoint() != 0.0){
                        ecubtcu.canMagnetSetCurrent("Q4I3", 0.0);
                        log.warn("Q4I3 set to 0A");
                    }
                    if (Q5I3.getCurrentFeedback() >= 40.0) {
                        ecubtcu.canMagnetSetCurrent("Q5I3" , (Q5I3.getCurrentFeedback() / 1.5));
                    } else if (Q5I3.getCurrentFeedback() < 40.0 && Q5I3.getCurrentSetpoint() != 0.0){
                        ecubtcu.canMagnetSetCurrent("Q5I3", 0.0);
                        log.warn("Q5I3 set to 0A");
                    }
                    if (B12B3.getCurrentFeedback() >= 100.0) {
                        ecubtcu.canMagnetSetCurrent("B12B3", (B12B3.getCurrentFeedback() / 1.5));
                    } else if (B12B3.getCurrentFeedback() < 100.0 && B12B3.getCurrentSetpoint() != 0.0){
                        ecubtcu.canMagnetSetCurrent("B12B3", 0.0);
                        log.warn("B12B3 set to 0A");
                    }
                    if (B1I3.getCurrentFeedback() >= 100.0) {
                        ecubtcu.canMagnetSetCurrent("B1I3", (B1I3.getCurrentFeedback() / 1.5));
                    } else if (B1I3.getCurrentFeedback() < 100.0 && B1I3.getCurrentSetpoint() != 0.0){
                        ecubtcu.canMagnetSetCurrent("B1I3", 0.0);
                        log.warn("B1I3 set to 0A");
                    }
                    if (B2I3.getCurrentFeedback() >= 100.0) {
                        ecubtcu.canMagnetSetCurrent("B2I3", (B2I3.getCurrentFeedback() / 1.5));
                    } else if (B2I3.getCurrentFeedback() < 100.0 && B2I3.getCurrentSetpoint() != 0.0){
                        ecubtcu.canMagnetSetCurrent("B2I3", 0.0);
                        log.warn("B2I3 set to 0A");
                    }
                    break;
                case 6:
                    if (Q1S1.getCurrentFeedback() >= 40.0) {
                        ecubtcu.canMagnetSetCurrent("Q1S1" , (Q1S1.getCurrentFeedback() / 1.5));
                    } else if (Q1S1.getCurrentFeedback() < 40.0 && Q1S1.getCurrentSetpoint() != 0.0){
                        ecubtcu.canMagnetSetCurrent("Q1S1", 0.0);
                        log.warn("Q1S1 set to 0A");
                    }
                    if (Q2S1.getCurrentFeedback() >= 40.0) {
                        ecubtcu.canMagnetSetCurrent("Q2S1" , (Q2S1.getCurrentFeedback() / 1.5));
                    } else if (Q2S1.getCurrentFeedback() < 40.0 && Q2S1.getCurrentSetpoint() != 0.0){
                        ecubtcu.canMagnetSetCurrent("Q2S1", 0.0);
                        log.warn("Q2S1 set to 0A");
                    }
                    if (Q3S1.getCurrentFeedback() >= 40.0) {
                        ecubtcu.canMagnetSetCurrent("Q3S1" , (Q3S1.getCurrentFeedback() / 1.5));
                    } else if (Q3S1.getCurrentFeedback() < 40.0 && Q3S1.getCurrentSetpoint() != 0.0){
                        ecubtcu.canMagnetSetCurrent("Q3S1", 0.0);
                        log.warn("Q3S1 set to 0A");
                    }
                    if (Q4S1.getCurrentFeedback() >= 40.0) {
                        ecubtcu.canMagnetSetCurrent("Q4S1" , (Q4S1.getCurrentFeedback() / 1.5));
                    } else if (Q4S1.getCurrentFeedback() < 40.0 && Q4S1.getCurrentSetpoint() != 0.0){
                        ecubtcu.canMagnetSetCurrent("Q4S1", 0.0);
                        log.warn("Q4S1 set to 0A");
                    }
                    if (Q5S1.getCurrentFeedback() >= 40.0) {
                        ecubtcu.canMagnetSetCurrent("Q5S1" , (Q5S1.getCurrentFeedback() / 1.5));
                    } else if (Q5S1.getCurrentFeedback() < 40.0 && Q5S1.getCurrentSetpoint() != 0.0){
                        ecubtcu.canMagnetSetCurrent("Q5S1", 0.0);
                        log.warn("Q5S1 set to 0A");
                    }
                    if (Q1S2.getCurrentFeedback() >= 40.0) {
                        ecubtcu.canMagnetSetCurrent("Q1S2" , (Q1S2.getCurrentFeedback() / 1.5));
                    } else if (Q1S2.getCurrentFeedback() < 40.0 && Q1S2.getCurrentSetpoint() != 0.0){
                        ecubtcu.canMagnetSetCurrent("Q1S2", 0.0);
                        log.warn("Q1S2 set to 0A");
                    }
                    if (Q2S2.getCurrentFeedback() >= 40.0) {
                        ecubtcu.canMagnetSetCurrent("Q2S2" , (Q2S2.getCurrentFeedback() / 1.5));
                    } else if (Q2S2.getCurrentFeedback() < 40.0 && Q2S2.getCurrentSetpoint() != 0.0){
                        ecubtcu.canMagnetSetCurrent("Q2S2", 0.0);
                        log.warn("Q2S2 set to 0A");
                    }
                    if (Q3S2.getCurrentFeedback() >= 40.0) {
                        ecubtcu.canMagnetSetCurrent("Q3S2" , (Q3S2.getCurrentFeedback() / 1.5));
                    } else if (Q3S2.getCurrentFeedback() < 40.0 && Q3S2.getCurrentSetpoint() != 0.0){
                        ecubtcu.canMagnetSetCurrent("Q3S2", 0.0);
                        log.warn("Q3S2 set to 0A");
                    }
                    if (Q4S2.getCurrentFeedback() >= 40.0) {
                        ecubtcu.canMagnetSetCurrent("Q4S2" , (Q4S2.getCurrentFeedback() / 1.5));
                    } else if (Q4S2.getCurrentFeedback() < 40.0 && Q4S2.getCurrentSetpoint() != 0.0){
                        ecubtcu.canMagnetSetCurrent("Q4S2", 0.0);
                        log.warn("Q4S2 set to 0A");
                    }
                    if (Q5S2.getCurrentFeedback() >= 40.0) {
                        ecubtcu.canMagnetSetCurrent("Q5S2" , (Q5S2.getCurrentFeedback() / 1.5));
                    } else if (Q5S2.getCurrentFeedback() < 40.0 && Q5S2.getCurrentSetpoint() != 0.0){
                        ecubtcu.canMagnetSetCurrent("Q5S2", 0.0);
                        log.warn("Q5S2 set to 0A");
                    }
                    if (Q1S3.getCurrentFeedback() >= 40.0) {
                        ecubtcu.canMagnetSetCurrent("Q1S3" , (Q1S3.getCurrentFeedback() / 1.5));
                    } else if (Q1S3.getCurrentFeedback() < 40.0 && Q1S3.getCurrentSetpoint() != 0.0){
                        ecubtcu.canMagnetSetCurrent("Q1S3", 0.0);
                        log.warn("Q1S3 set to 0A");
                    }
                    if (Q2S3.getCurrentFeedback() >= 40.0) {
                        ecubtcu.canMagnetSetCurrent("Q2S3" , (Q2S3.getCurrentFeedback() / 1.5));
                    } else if (Q2S3.getCurrentFeedback() < 40.0 && Q2S3.getCurrentSetpoint() != 0.0){
                        ecubtcu.canMagnetSetCurrent("Q2S3", 0.0);
                        log.warn("Q2S3 set to 0A");
                    }
                    if (Q3S3.getCurrentFeedback() >= 40.0) {
                        ecubtcu.canMagnetSetCurrent("Q3S3" , (Q3S3.getCurrentFeedback() / 1.5));
                    } else if (Q3S3.getCurrentFeedback() < 40.0 && Q3S3.getCurrentSetpoint() != 0.0){
                        ecubtcu.canMagnetSetCurrent("Q3S3", 0.0);
                        log.warn("Q3S3 set to 0A");
                    }
                    if (Q4S3.getCurrentFeedback() >= 40.0) {
                        ecubtcu.canMagnetSetCurrent("Q4S3" , (Q4S3.getCurrentFeedback() / 1.5));
                    } else if (Q4S3.getCurrentFeedback() < 40.0 && Q4S3.getCurrentSetpoint() != 0.0){
                        ecubtcu.canMagnetSetCurrent("Q4S3", 0.0);
                        log.warn("Q4S3 set to 0A");
                    }
                    if (Q5S3.getCurrentFeedback() >= 40.0) {
                        ecubtcu.canMagnetSetCurrent("Q5S3" , (Q5S3.getCurrentFeedback() / 1.5));
                    } else if (Q5S3.getCurrentFeedback() < 40.0 && Q5S3.getCurrentSetpoint() != 0.0){
                        ecubtcu.canMagnetSetCurrent("Q5S3", 0.0);
                        log.warn("Q5S3 set to 0A");
                    }
                    if (Q1B4.getCurrentFeedback() >= 40.0) {
                        ecubtcu.canMagnetSetCurrent("Q1B4" , (Q1B4.getCurrentFeedback() / 1.5));
                    } else if (Q1B4.getCurrentFeedback() < 40.0 && Q1B4.getCurrentSetpoint() != 0.0){
                        ecubtcu.canMagnetSetCurrent("Q1B4", 0.0);
                        log.warn("Q1B4 set to 0A");
                    }
                    if (Q2B4.getCurrentFeedback() >= 40.0) {
                        ecubtcu.canMagnetSetCurrent("Q2B4" , (Q2B4.getCurrentFeedback() / 1.5));
                    } else if (Q2B4.getCurrentFeedback() < 40.0 && Q2B4.getCurrentSetpoint() != 0.0){
                        ecubtcu.canMagnetSetCurrent("Q2B4", 0.0);
                        log.warn("Q2B4 set to 0A");
                    }
                    if (Q3B4.getCurrentFeedback() >= 40.0) {
                        ecubtcu.canMagnetSetCurrent("Q3B4" , (Q3B4.getCurrentFeedback() / 1.5));
                    } else if (Q3B4.getCurrentFeedback() < 40.0 && Q3B4.getCurrentSetpoint() != 0.0){
                        ecubtcu.canMagnetSetCurrent("Q3B4", 0.0);
                        log.warn("Q3B4 set to 0A");
                    }
                    if (Q1G4.getCurrentFeedback() >= 40.0) {
                        ecubtcu.canMagnetSetCurrent("Q1G4" , (Q1G4.getCurrentFeedback() / 1.5));
                    } else if (Q1G4.getCurrentFeedback() < 40.0 && Q1G4.getCurrentSetpoint() != 0.0){
                        ecubtcu.canMagnetSetCurrent("Q1G4", 0.0);
                        log.warn("Q1G4 set to 0A");
                    }
                    if (Q2G4.getCurrentFeedback() >= 40.0) {
                        ecubtcu.canMagnetSetCurrent("Q2G4" , (Q2G4.getCurrentFeedback() / 1.5));
                    } else if (Q2G4.getCurrentFeedback() < 40.0 && Q2G4.getCurrentSetpoint() != 0.0){
                        ecubtcu.canMagnetSetCurrent("Q2G4", 0.0);
                        log.warn("Q2G4 set to 0A");
                    }
                    if (Q3G4.getCurrentFeedback() >= 40.0) {
                        ecubtcu.canMagnetSetCurrent("Q3G4" , (Q3G4.getCurrentFeedback() / 1.5));
                    } else if (Q3G4.getCurrentFeedback() < 40.0 && Q3G4.getCurrentSetpoint() != 0.0){
                        ecubtcu.canMagnetSetCurrent("Q3G4", 0.0);
                        log.warn("Q3G4 set to 0A");
                    }
                    if (Q4G4.getCurrentFeedback() >= 40.0) {
                        ecubtcu.canMagnetSetCurrent("Q4G4" , (Q4G4.getCurrentFeedback() / 1.5));
                    } else if (Q4G4.getCurrentFeedback() < 40.0 && Q4G4.getCurrentSetpoint() != 0.0){
                        ecubtcu.canMagnetSetCurrent("Q4G4", 0.0);
                        log.warn("Q4G4 set to 0A");
                    }
                    if (Q5G4.getCurrentFeedback() >= 40.0) {
                        ecubtcu.canMagnetSetCurrent("Q5G4" , (Q5G4.getCurrentFeedback() / 1.5));
                    } else if (Q5G4.getCurrentFeedback() < 40.0 && Q5G4.getCurrentSetpoint() != 0.0){
                        ecubtcu.canMagnetSetCurrent("Q5G4", 0.0);
                        log.warn("Q5G4 set to 0A");
                    }
                    if (Q1N4.getCurrentFeedback() >= 40.0) {
                        ecubtcu.canMagnetSetCurrent("Q1N4" , (Q1N4.getCurrentFeedback() / 1.5));
                    } else if (Q1N4.getCurrentFeedback() < 40.0 && Q1N4.getCurrentSetpoint() != 0.0){
                        ecubtcu.canMagnetSetCurrent("Q1N4", 0.0);
                        log.warn("Q1N4 set to 0A");
                    }
                    if (Q2N4.getCurrentFeedback() >= 40.0) {
                        ecubtcu.canMagnetSetCurrent("Q2N4" , (Q2N4.getCurrentFeedback() / 1.5));
                    } else if (Q2N4.getCurrentFeedback() < 40.0 && Q2N4.getCurrentSetpoint() != 0.0){
                        ecubtcu.canMagnetSetCurrent("Q2N4", 0.0);
                        log.warn("Q2N4 set to 0A");
                    }
                    if (B12B4.getCurrentFeedback() >= 100.0) {
                        ecubtcu.canMagnetSetCurrent("B12B4", (B12B4.getCurrentFeedback() / 1.5));
                    } else if (B12B4.getCurrentFeedback() < 100.0 && B12B4.getCurrentSetpoint() != 0.0){
                        ecubtcu.canMagnetSetCurrent("B12B4", 0.0);
                        log.warn("B12B4 set to 0A");
                    }
                    if (B1G4.getCurrentFeedback() >= 100.0) {
                        ecubtcu.canMagnetSetCurrent("B1G4", (B1G4.getCurrentFeedback() / 1.5));
                    } else if (B1G4.getCurrentFeedback() < 100.0 && B1G4.getCurrentSetpoint() != 0.0){
                        ecubtcu.canMagnetSetCurrent("B1G4", 0.0);
                        log.warn("B1G4 set to 0A");
                    }
                    if (B2G4.getCurrentFeedback() >= 100.0) {
                        ecubtcu.canMagnetSetCurrent("B2G4", (B2G4.getCurrentFeedback() / 1.5));
                    } else if (B2G4.getCurrentFeedback() < 100.0 && B2G4.getCurrentSetpoint() != 0.0){
                        ecubtcu.canMagnetSetCurrent("B2G4", 0.0);
                        log.warn("B2G4 set to 0A");
                    }
                    break;
            }


        }catch (EcubtcuException e) {
            log.error("ECUBTCU exception " + e);
            e.printStackTrace();
        }catch (NullPointerException e){
            log.error("Null ptr at setESSMagnets");
            e.printStackTrace();
        }finally {
            try {
                Thread.sleep(2000);
            }catch (InterruptedException e){
                e.printStackTrace();
            }
        }
    }

    public double[] getCurrents() {
        double[] currents = new double[4];
        for (int i = 0; i < 4; i++) {
            currents[i] = (Double) acu.getTagValue(Status.Magnet_read[i]);
        }
        return currents;
    }

	public void setCurrents(double[] newCurrents){
        log.debug("Is ACU connected ?");
        if(!acu.isConnected()){
            log.debug("Will try to connect to ACU");
            acu.connect();
            log.debug("Is connected");
        }

        for (int i = 0; i < 4; i++) {
            acu.setTagValue(Status.Magnet_write[i], newCurrents[i]);
        }
		log.debug("New currents sent to 4 magnets");
	}

    public void setSafeCurrents(){
        log.debug("Is ACU connected ?");
        if(!acu.isConnected()){
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
	
	public String getSiteName()
	{
		return siteName ;
	}

    public static void setFeedbackClient()
    {
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
    	
    	if (!feedbackClient.isConnected())
        {
    		feedbackClient.setupICompClient();
            feedbackClient.connect();
        }

        feedbackClient.addPropertyChangeListener(connectListener);

    }

    public void align() {
        Screen screen = new Screen(mScreen);
        aligned = false;

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

                    while (null != screen.exists("preparing", 0) || null != screen.exists("preparing2", 0) || null != screen.exists("afterRefresh", 0)) {
                        log.warn("Found " + screen.getLastMatch().getImageFilename());
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

                if (mTolerances[0] >= Math.abs(mPositions[0]) && mTolerances[1] >= Math.abs(mPositions[1]) && mTolerances[2] >= Math.abs(mPositions[2]) && mTolerances[3] >= Math.abs(mPositions[3])) {

                    log.warn("Positions within tolerance, ending automatic alignment");

                    if (null != screen.exists("cancel", 1)) {
                        log.warn("Found cancel button");
                        screen.getLastMatch().click();
                        robot.delay(250);
                        aligned = true;
                    } else { log.error("Could not find cancel button");}
                } else if (null != screen.exists("apply", 1) && !aligned) {
                    log.warn("Found apply button");
                    screen.getLastMatch().click();
                    robot.delay(500);
                    if (null != screen.exists("refresh", 0) || null != screen.exists("refresh2", 0)) {
                        log.warn("Found refresh button");
                        screen.getLastMatch().click();
                        robot.delay(400);

                        while (null != screen.exists("preparing", 1) || null != screen.exists("preparing2", 0) || null != screen.exists("afterRefresh", 1) && !aligned) {
                            log.warn("Found " + screen.getLastMatch().getImageFilename());
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
                          if (mTolerances[0] >= mPositions[0] && mTolerances[1] >= mPositions[1] && mTolerances[2] >= mPositions[2] && mTolerances[3] >= mPositions[3]) {

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


        }catch(AWTException e){
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
                        log.warn("Arc Power Supply has been turned OFF.");
                    }

                    if (Double.parseDouble(acu.getTagValue(mStatus.Fil_current).toString()) > 100) {
                        acu.setTagValue(mStatus.Key_command, 75);
                        log.warn("Filament Power Supply has been turned OFF.");
                    }
                    break;
                case 1:
                    acu.setTagValue(mStatus.Fil_write, 125.001);
                    acu.setTagValue(mStatus.Key_command, 71);
                    log.warn("Filament Power Supply has been turned ON to 125A.");
                    break;
                case 2:
                    acu.setTagValue(mStatus.Fil_write, 130.001);
                    log.warn("Filament Power Supply has been set to 130A.");
                    break;
                case 3:
                    acu.setTagValue(mStatus.Fil_write, 135.001);
                    log.warn("Filament Power Supply has been set to 135A.");
                    break;
                case 4:
                    acu.setTagValue(mStatus.Fil_write, 140.001);
                    log.warn("Filament Power Supply has been set to 140A.");
                    break;
                case 5:
                    acu.setTagValue(mStatus.Fil_write, 145.001);
                    log.warn("Filament Power Supply has been set to 145A.");
                    break;
                case 6:
                    acu.setTagValue(mStatus.Fil_write, 150.001);
                    log.warn("Filament Power Supply has been set to 150A.");
                    break;
                case 7:
                    acu.setTagValue(mStatus.Fil_write, 155.001);
                    log.warn("Filament Power Supply has been set to 155A.");
                    break;
                case 8:
                    acu.setTagValue(mStatus.Fil_write, 160.001);
                    log.warn("Filament Power Supply has been set to 160A.");
                    break;
                case 9:
                    acu.setTagValue(mStatus.Fil_write, 165.001);
                    log.warn("Filament Power Supply has been set to 165A.");
                    break;
                case 10:
                    acu.setTagValue(mStatus.Fil_write, 170.001);
                    log.warn("Filament Power Supply has been set to 170A.");
                    break;
                case 11:
                    acu.setTagValue(mStatus.Fil_write, 175.001);
                    log.warn("Filament Power Supply has been set to 175A.");
                    break;
                case 12:
                    acu.setTagValue(mStatus.Fil_write, 180.001);
                    log.warn("Filament Power Supply has been set to 180A.");
                    break;
                case 13:
                    acu.setTagValue(mStatus.Fil_write, 185.001);
                    log.warn("Filament Power Supply has been set to 185A.");
                    break;
                case 14:
                    acu.setTagValue(mStatus.Fil_write, 190.001);
                    log.warn("Filament Power Supply has been set to 190A.");
                    break;
                case 15:
                    acu.setTagValue(mStatus.Fil_write, 195.001);
                    log.warn("Filament Power Supply has been set to 195A.");
                    break;
                case 16:
                    acu.setTagValue(mStatus.Fil_write, 190.001);
                    log.warn("Filament Power Supply has been set to 190A.");
                    break;
                case 17:
                    acu.setTagValue(mStatus.Fil_write, 185.001);
                    log.warn("Filament Power Supply has been set to 185A.");
                    break;
                case 18:
                    acu.setTagValue(mStatus.Fil_write, 180.001);
                    log.warn("Filament Power Supply has been set to 180A.");
                    break;
                case 19:
                    acu.setTagValue(mStatus.Arc_write, 85.001);
                    acu.setTagValue(mStatus.Key_command, 72);
                    log.warn("Arc Power Supply has been turned ON to 85mA.");
                    break;
                case 20:
                    if(!sourceTuning()) {
                        acu.setTagValue(mStatus.Key_command, 73);
                        log.warn("Source tuning has been enabled.");
                    }
                    break;
                }
        }else {
            log.error("System is in automatic mode, please switch to manual.");
        }
    }

    public boolean checkForSpark() {
        if (!mStatus.getBool(Status.S2E_STATUS)) {
            log.warn("Spark detected");
            return true;
        }
        return false;
    }

    public void stopBPMs() {
        try {
            if (P1E.getOperationMode() == BeamProfileMonitor.OperationMode.OPERATION_MODE_CONTINUOUS_ACQUISITION) {
                P1E.stopContinuousAcquisition();
            }
            if (P2E.getOperationMode() == BeamProfileMonitor.OperationMode.OPERATION_MODE_CONTINUOUS_ACQUISITION) {
                P2E.stopContinuousAcquisition();
            }
        }catch (EcubtcuNotConnectedException e) {
            e.printStackTrace();
        }catch (EcubtcuException e) {
            e.printStackTrace();
        }
    }

    private static class FeedbackConnectionListener implements PropertyChangeListener
    {
        @Override
        public void propertyChange(PropertyChangeEvent evt)
        {
            if (evt.getPropertyName().equals(BlakEcubtcuFeedbackClient.CONNECTION)
                    && ((Boolean) evt.getNewValue()).equals(false))
            {
                log.error("Closing: Connection with RT/Notification server is lost, Blak needs to quit!");
                //System.exit(0);
            }
        }
    }


    @Override
    public void addPropertyChangeListener(PropertyChangeListener pListener)
    {
        mPropertyChangeSupport.addPropertyChangeListener(pListener);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener pListener)
    {
        mPropertyChangeSupport.removePropertyChangeListener(pListener);
    }

    @Override
    public void propertyChange(PropertyChangeEvent pEvent)
    {
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
}
