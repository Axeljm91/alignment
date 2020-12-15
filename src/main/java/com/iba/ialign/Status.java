package com.iba.ialign;

import com.iba.blak.device.api.Insertable;
//import com.iba.blak.device.impl.AbstractLegacyBeamProfileMonitor;
import com.iba.blakOverwrite.AbstractLegacyBeamProfileMonitor;
import com.iba.device.Device;
import com.iba.ialign.common.IbaColors;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by cboyd on 6/13/2014.
 */
public class Status {
    static final public String T1X_read     = "B0.X1.SGC01.XPscsprvback";
    static final public String T1X_write    = "B0.X1.SGC01.XPscsprv";
    static final public String T1X_reg      = "B0.X1.SGC01.XRegulOk";
    static final public String T1Y_read     = "B0.X1.SGC01.YPscsprvback";
    static final public String T1Y_write    = "B0.X1.SGC01.YPscsprv";
    static final public String T1Y_reg      = "B0.X1.SGC01.YRegulOk";
    static final public String T2X_read     = "B0.X1.SGC02.XPscsprvback";
    static final public String T2X_write    = "B0.X1.SGC02.XPscsprv";
    static final public String T2X_reg      = "B0.X1.SGC02.XRegulOk";
    static final public String T2Y_read     = "B0.X1.SGC02.YPscsprvback";
    static final public String T2Y_write    = "B0.X1.SGC02.YPscsprv";
    static final public String T2Y_reg      = "B0.X1.SGC02.YRegulOk";
    static final public String DF_current   = "B0.X1.DFA01.Pscfeedbackrv";
    static final public String DF_voltage   = "B0.X1.DFA01.Psvfeedbackrv";
    static final public String MC_current   = "B0.M1.MCA01.Pscfeedbackrv";
    static final public String MC_setpoint  = "B0.M1.MCA01.Pscsprvback";
    static final public String MC_write     = "B0.M1.MCA01.Pscsprv";
    static final public String BS_current   = "E0.E1.BSB01.cfeedback";
    static final public String CC_current   = "B0.X1.CCA01.Pscfeedbackrv";
    static final public String CC_voltage   = "B0.X1.CCA01.Psvfeedbackrv";
    static final public String CCcurrent2   = "B0.M1.CCA01.Pscsprvback";
    static final public String CC_setpoint  = "B0.M1.CCA01.Pscsprv";
    static final public String CC_write     = "B0.M1.CCA01.Pscsprv";
    static final public String HC1_current  = "B0.M1.HCA01.Pscfeedbackrv";
    static final public String HC1_setpoint = "B0.M1.HCA01.Pscsprvback";
    static final public String HC1_write    = "B0.M1.HCA01.Pscsprv";
    static final public String HC2_current  = "B0.M1.HCA02.Pscfeedbackrv";
    static final public String HC2_setpoint = "B0.M1.HCA02.Pscsprvback";
    static final public String HC2_write    = "B0.M1.HCA02.Pscsprv";

    // modified to cc fb -AMO
    //static final public String Fil_voltage  = "B0.S1.FIV01.Psvfeedbackrv";
    //static final public String Fil_voltage  = "\"DB_GE\".FT_ISP_RV";

    static final public String Acu_test      = "E0.E1.BSB01.cfeedback";
    static final public String Acu_test2     = "E0.E1.CLA01.cfeedback";
    static final public String Acu_test3     = "E0.E1.BSB01.cfeedback";
    static final public String Acu_test4     = "E0.E1.BSB01.cfeedback";


    static final public String Yoke_temp     = "B0.M1.MCA01.Tempfeedbackrv";
    static final public String Fil_current   = "B0.S1.FIA01.Pscfeedbackrv";
    static final public String Fil_write     = "B0.S1.FIA01.Pscsprv";
    static final public String Fil_setpoint  = "B0.S1.FIA01.Pscsprvback";
    static final public String Arc_write     = "B0.S1.ARA01.Pscsprv";
    static final public String Arc_current   = "B0.S1.ARA01.Pscfeedbackrv";
    static final public String Arc_voltage   = "B0.S1.ARA01.Psvfeedbackrv";
    static final public String Arc_setpoint  = "B0.S1.ARA01.Pscsprvback";
    static final public String VD1_setpoint  = "B0.R1.LLA01.VDee1feedbackrv";
    static final public String VD2_setpoint  = "B0.R1.LLA01.VDee1feedbackrv";
    static final public String VD1_equip     = "B0.R1.LLA01.VDee1feedbackrv";
    static final public String VD2_equip     = "B0.R1.LLA01.VDee2feedbackrv";
    static final public String Radial_probe  = "B0.D1.RPA01.Mmfeedbackrv";
    static final public String Fwd_power     = "B0.R1.FAA01.KwForwardPowerrv";
    static final public String Refl_power    = "B0.R1.FAA01.KwReflectedPowerrv";

    static final public String sour_tuning   = "B0.S1.GED01.PsTuningCmdStatus";
    static final public String mc_tuning     = "B0.M1.MCA01.TuningCmdStatus";

    static final public String Key_command   = "B0.__.ACU01.KeyCommand";

    // OPC SRCU tags
    static final public String TR1_SECURE           = "S1.T1.SZD01.Secure";
    static final public String TR1_SEARCH_ACTIVE    = "S1.T1.SZD01.SearchActive";
    static final public String TR2_SECURE           = "S2.T2.SZD01.Secure";
    static final public String TR2_SEARCH_ACTIVE    = "S2.T2.SZD01.SearchActive";
    static final public String TR3_SECURE           = "S3.T3.SZD01.Secure";
    static final public String TR3_SEARCH_ACTIVE    = "S3.T3.SZD01.SearchActive";
    static final public String TR4_SECURE           = "S4.T4.SZD01.Secure";
    static final public String TR4_SEARCH_ACTIVE    = "S4.T4.SZD01.SearchActive";

    // OPC beamline selected
    static final public String BP1_SELECTED         = "E0.__.BLA01.selected";
    static final public String BP2_SELECTED         = "E0.__.BLA02.selected";
    static final public String BP3_SELECTED         = "E0.__.BLA03.selected";
    static final public String BP4_SELECTED         = "E0.__.BLA04.selected";
    static final public String BP5_SELECTED         = "E0.__.BLA05.selected";
    static final public String BP6_SELECTED         = "E0.__.BLA06.selected";

    static final public String SMPS_ON              = "E0.__.SPA01.standby";

    static final public String[] BPM_names      = new String[]{"BPM 1 X",   "BPM 1 Y",  "BPM 2 X",      "BPM 2 Y"};
    static final public String[] Magnet_names   = new String[]{"Magnet 1 X","Magnet 1 Y", "Magnet 2 X", "Magnet 2 Y"};
    static final public String[] Magnet_names2  = new String[]{"Magnet 1 X","Magnet 1 Y", "Magnet 2 X", "Magnet 2 Y"};
    static final public String[] Magnet_read    = new String[]{T1X_read,    T1Y_read,   T2X_read,   T2Y_read};
    static final public String[] Magnet_write   = new String[]{T1X_write,   T1Y_write,  T2X_write,  T2Y_write};
    static final public String[] Magnet_reg     = new String[]{T1X_reg,     T1Y_reg,    T2X_reg,    T2Y_reg};
    static final public String[] Cyclo_names    = new String[]{"Main Coil (A)", "Yoke Temp (°C)", "Filament (A)", "Arc (mA)", "Arc (V)", "DF (mA)", "DF (V)"};
    static final public String[] Cyclo_read     = new String[]{MC_current, Yoke_temp, Fil_current, Arc_current, Arc_voltage, DF_current, DF_voltage};
    static final public String[] BLE_names      = {"ESS Beamstop", "X Slits", "Y Slits", "Momentum Slits", "Scanning Magnets"};
    static final public String[] Prep_names     = {"PCVue: Turn off ESS magnets.", "BCP: Prepare for 10 nA.", "Move degrader to BPM position.", "Retract beam stop.", "Open horizontal slit.", "Open vertical slit.", "Insert second BPM.", "BCREU pulsing beam."};
    static final public String[] DF_names       = new String[]{"Deflector Current"};
    static final public String[] DF_read        = new String[]{DF_current};
    static final public String[] BLE_names2     = new String[]{"Degrader", "Beam Stop", "ESS Magnets"};
    static final public String[] BCREU_names    = new String[]{"Connection", "Pulse", "State", "Beam Prepared", "Beam Out"};
    static final public String[] CycloTuning_names = new String[]{"Radial Probe", "Yoke Temp (°C)", "RF Fwd (kW)", "RF Refl (kW)", "DF (mA)", "DF (V)"};
    static final public String[] CycloTuning_read = new String[]{Radial_probe, Yoke_temp, Fwd_power, Refl_power, DF_current, DF_voltage};
    static final public String[] LLRF_read      = new String[]{VD1_equip, Fil_current, Arc_current, Arc_voltage, CCcurrent2, MC_current, HC1_current, HC2_current};


    static final public int MAGNET_OFFSET = BLE_names.length + BCREU_names.length;
    static final public int CYCLO_OFFSET        = MAGNET_OFFSET + Magnet_names.length;
    static final public int CYCLOTUNING_OFFSET  = CYCLO_OFFSET + Cyclo_names.length;
    static final public int LLRFTUNING_OFFSET   = CYCLOTUNING_OFFSET + CycloTuning_names.length;
    static final public int SOURCETUNING_OFFSET = LLRFTUNING_OFFSET + LLRF_read.length;

    static final public int P1E_STATUS   = 0;
//    static final public int DEGRADER     = 0;
    static final public int S2E_STATUS   = 0;
    static final public int SL1E_STATUS  = 1;
    static final public int SL2E_STATUS  = 2;
    static final public int SL3E_STATUS  = 3;
    //static final public int P2E_STATUS   = 4;
    static final public int ESS_MAGNETS  = 4;
    static final public int SCAN_MAGNETS = 4;
    static final public int BCREU_HW     = 5;
    static final public int PULSESOURCE  = 6;
//    static final public int REGULATION   = 8;
//    static final public int LOOKUP       = 9;
    static final public int BCREU_STATE  = 7;
    static final public int MAX_BEAM     = 8;
    static final public int IC_CYCLO     = 9;

    static final public int FILAMENT     = CYCLO_OFFSET + 2;
    static final public int DF_CURRENT   = CYCLO_OFFSET + 5;
    static final public int DF_VOLTAGE   = DF_CURRENT + 1;
    static final public int DF_CURRENT2  = CYCLOTUNING_OFFSET + 4;
    static final public int DF_VOLTAGE2  = DF_CURRENT2 + 1;

    static final public int RADIAL_PROBE = CYCLOTUNING_OFFSET;

    static final public int CC_SETPOINT  = CYCLO_OFFSET;
    static final public int FILAMENT_RES  = CYCLO_OFFSET + 1;

    static final public int PREP_USER   = 1;
    static final public int PREP_BCREU  = 7;

    // ***************************
    // Setting the healthy/unhealthy limits -AMO

    static final private Double[] fil_min = new Double[]{165.0d, 160.0d};

    static final private Double[] df_max = new Double[]{0.003d, 0.45d};

    private Double[] df_volt_min = new Double[2];

    static final private Double rp_min = 1045.00;

    static final private Double[] cc_sp_min = new Double[]{14.0, 0.2d};

    static final private Double[] fil_res_max = new Double[]{14.0, 0.2d};

    //static final private Double[] cc_fb_min = new Double[]{Double.parseDouble(Yoke_temp), 0.5d };


    static final public DecimalFormat DEC_FORMAT = new DecimalFormat("#.###", (new DecimalFormatSymbols(Locale.US)));
    static final public Color HEALTHY   = IbaColors.BT_GREEN;
    static final public Color WARNING   = IbaColors.YELLOW;
    static final public Color UNHEALTHY = IbaColors.YELLOW;
    static final public Color NO_COLOR  = IbaColors.LT_GRAY;

    private boolean mDeflectorWarningDisplayed = false;
    private boolean mDeflectorErrorDisplayed = false;

    private boolean mDeflectorConditioningWarningDisplayed = false;
    private boolean mDeflectorConditioningErrorDisplayed = false;

    //Needed for CSV read/write
    static public Device acu;
    private static Logger log=Logger.getLogger(new Throwable().getStackTrace()[0].getClassName());

//    static final public Insertable.Position INSERTED    = Insertable.Position.INSERTED;
//    static final public Insertable.Position RETRACTED   = Insertable.Position.RETRACTED;
//    static final public Insertable.Position MOVING      = Insertable.Position.MOVING;

    private String[]    mStatus;
    private Boolean[]   mBool;
    private Color[]     mColor;
    final private int   mNum;


    public Status() {
        mNum = LLRFTUNING_OFFSET + Status.LLRF_read.length;
        mStatus = new String[mNum];
        mBool   = new Boolean[mNum];
        mColor  = new Color[mNum];
    }
    public Status(int num) {
        mNum = num;
        mStatus = new String[mNum];
        mBool = new Boolean[mNum];
    }

    public void set(int id, Double dval) {
        if (id == FILAMENT) {
            set(id, dval, fil_min);
        } else if (id == DF_CURRENT || id == DF_CURRENT2) {
            set(id, dval, df_max);
        } else if (id == DF_VOLTAGE || id == DF_VOLTAGE2) {
            set(id, dval, df_volt_min);
        } else if (id == RADIAL_PROBE) {
            set(id, dval, rp_min);

            //System.out.println("DF_CURRENT VALUE: " + DEC_FORMAT.format(dval));
         //   add in for comp coil feedback check -AMO
        //} else if (id == CC_CURRENT) {
       //     set(id, dval, cc_sp_min);
        } else {
            set(id, DEC_FORMAT.format(dval), NO_COLOR);
        }
    }

    /* sets status of a double, with a specified status */
    public void set(int id, Double dval, Boolean healthy) {
        if (healthy) {
            set(id, DEC_FORMAT.format(dval), HEALTHY);
            //System.out.println("healthy " + DEC_FORMAT.format(dval));
        } else {
            set(id, DEC_FORMAT.format(dval), UNHEALTHY);
            //System.out.println("unhealthy " + DEC_FORMAT.format(dval));
        }
    }

    /* sets status of a double, with a specified minimum */
    public void set(int id, Double dval, Double min) {
        if (dval > min) {
            set(id, DEC_FORMAT.format(dval), HEALTHY);
        } else {
            set(id, DEC_FORMAT.format(dval), UNHEALTHY);
        }
    }

//    public void set(int id, Double dval, Double min) {
//        if (id == FILAMENT){
//            if (dval > min) {
//                set(id, DEC_FORMAT.format(dval), HEALTHY);
//            } else {
//                set(id, DEC_FORMAT.format(dval), UNHEALTHY);
//            }
//        } else if (id == DF_CURRENT){
//            if (dval < min) {
//                set(id, DEC_FORMAT.format(dval), WARNING);
//            } else {
//                set(id, DEC_FORMAT.format(dval), UNHEALTHY);
//            }
//        }
//    }

    public boolean[] getPrep() {
        boolean[] prepStatus = new boolean[Prep_names.length];
//        prepStatus[0] = (mColor[BCREU_HW] == HEALTHY) && (mColor[MAX_BEAM] == HEALTHY);
        prepStatus[0] = (mColor[ESS_MAGNETS] == HEALTHY);
        prepStatus[1] = (mColor[MAX_BEAM] == HEALTHY) && (mColor[BCREU_STATE] == HEALTHY);
        for (int i = 0; i < ESS_MAGNETS; i++) {
            prepStatus[i+2] = (mColor[i] == HEALTHY);
        }
        prepStatus[PREP_BCREU] = (mColor[PULSESOURCE] == HEALTHY) && (mColor[BCREU_STATE] == HEALTHY) && (mColor[IC_CYCLO] == HEALTHY);
        return prepStatus;
    }

    /* min is an array of Doubles, with first being the "WARNING" limit and second being the "UNHEALTHY" limit */
    public void set(int id, Double dval, Double[] min) {

        if (id == DF_VOLTAGE) {
            Controller.setDeflectorVoltage(df_volt_min);
            min = df_volt_min;

            //log.error(min[0] + " = min[0] && min[1] = " + min[1]);
        }

        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd"); //Or whatever format fits best your needs.
        String dateStr = sdf.format(date);

        File resultsFile = new File("./Deflector/DF_results_" + dateStr + ".csv");

        //File CCresultsFile = new File("./CompCoil/CC_results_" + dateStr + ".csv");

        if (id == DF_CURRENT || id == DF_CURRENT2) {
            if (dval < min[0]) {
                set(id, DEC_FORMAT.format(dval), HEALTHY);
                //mDeflectorWarningDisplayed = false;
                mDeflectorErrorDisplayed = false;
                //create resultsFile if it does not exist
                if (resultsFile.isFile()) {
                   // csvWrite(resultsFile, dval);

                } else {
                   //csvCreate(resultsFile);
                }
            } else if ((dval > min[0]) && (dval < min[1])) {
                set(id, DEC_FORMAT.format(dval), WARNING);
                //if ((!mDeflectorWarningDisplayed)) {
                    // Only display the warning once.
                    //mDeflectorWarningDisplayed = true;
                 //   mDeflectorErrorDisplayed = false;

                    //Thread t = new Thread(new Runnable() {
                     //   public void run() {
                     //       JOptionPane.showMessageDialog(null, "Leakage current was over 0.1mA, "
                     //               + "it is recommended to keep the deflector current lower than this.", "Deflector Warning", JOptionPane.WARNING_MESSAGE);
                     //   }
                   // });
                   // t.start();
                //}
                //create resultsFile if it does not exist
                if (resultsFile.isFile()) {
                    //csvWrite(resultsFile, dval);

                } else {
                    //csvCreate(resultsFile);
                }
            } else if (dval > min[1]) {
                set(id, DEC_FORMAT.format(dval), UNHEALTHY);
                if ((!mDeflectorErrorDisplayed)) {
                    // Only display the warning once.
                    mDeflectorErrorDisplayed = true;
                   //mDeflectorWarningDisplayed = true;

                    Thread t = new Thread(new Runnable() {
                        public void run() {
                            JOptionPane.showMessageDialog(null, "Deflector current spiked to 0.5mA. "
                                    + "Take action to lower the deflector current.", "Deflector Warning High", JOptionPane.ERROR_MESSAGE);
                        }
                    });
                    t.start();
                }
                //create resultsFile if it does not exist
                if (resultsFile.isFile()) {
                    //csvWrite(resultsFile, dval);

                } else {
                    //csvCreate(resultsFile);
                }
            }
        } else if (id == DF_VOLTAGE) {
            if (dval > min[0]) {
                set(id, DEC_FORMAT.format(dval), HEALTHY);
                //mDeflectorConditioningErrorDisplayed = false;
            } else if ((dval > min[1]) && (dval < min[0])) {
                set(id, DEC_FORMAT.format(dval), WARNING);
                //mDeflectorConditioningErrorDisplayed = false;
            } else if (dval < min[1]) {
                set(id, DEC_FORMAT.format(dval), UNHEALTHY);
                //if ((!mDeflectorConditioningErrorDisplayed)) {
                    // Only display the warning once.
                    //mDeflectorConditioningErrorDisplayed = true;

                   // int popUp = JOptionPane.showConfirmDialog(null, "Deflector Voltage was at " + dval + "kV, "
                    //        + "check if deflector conditioning is on.", "Deflector Voltage ERROR", JOptionPane.OK_CANCEL_OPTION, JOptionPane.ERROR_MESSAGE);

                    //Thread t = new Thread(new Runnable() {
                    //    public void run() {

                     //       JOptionPane.showConfirmDialog(null, "Deflector Voltage was at " + dval + "kV, "
                     //               + "check if deflector conditioning is on.", "Deflector Voltage ERROR", JOptionPane.OK_CANCEL_OPTION, JOptionPane.ERROR_MESSAGE);
                     //   }
                    //});
                  //  t.start();
                //}
            }

//        } else if (id == CC_CURRENT) {
//            if ((min[0] - min[1]) < dval && dval < (min[0] + min[1])) {
//                set(id, DEC_FORMAT.format(dval), HEALTHY);
//            } //else if ((dval > min[1]) && (dval < min[0])) {
//            // set(id, DEC_FORMAT.format(dval), WARNING);
//            //mDeflectorConditioningErrorDisplayed = false;
//            else {
//                set(id, DEC_FORMAT.format(dval), UNHEALTHY);
//
//                Thread t = new Thread(new Runnable() {
//                       public void run() {
//                           JOptionPane.showMessageDialog(null, "Compensation coil dropped below setpoint, "
//                                   + "see results file for details.", "Compensation Coil PS Warning", JOptionPane.WARNING_MESSAGE);
//                       }
//                     });
//                     t.start();
//
//                //create resultsFile if it does not exist
//                if (CCresultsFile.isFile()) {
//                    csvWrite(CCresultsFile, dval);
//
//                } else {
//                    csvCreate(CCresultsFile);
//                }
//                //{
//                //    Thread t = new Thread(new Runnable() {
//                 //       public void run() {
//
//                //            JOptionPane.showConfirmDialog(null, "Deflector Voltage was at " + dval + "kV, "
//                 //                   + "check if deflector conditioning is on.", "Deflector Voltage ERROR", JOptionPane.OK_CANCEL_OPTION, JOptionPane.ERROR_MESSAGE);
//                //        }
//                //    });
//                //    t.start();
//               // }
//
//            }

            } else{
                if (dval > min[0]) {
                    set(id, DEC_FORMAT.format(dval), HEALTHY);
                } else if (dval > min[1]) {
                    set(id, DEC_FORMAT.format(dval), WARNING);
                } else {
                    set(id, DEC_FORMAT.format(dval), UNHEALTHY);
                }
            }
        }

    public void set(int id, Double dval, Double min, Double max) {
        if ((dval > min) && (dval < max)) {
            set(id, DEC_FORMAT.format(dval), HEALTHY);
        } else {
            set(id, DEC_FORMAT.format(dval), UNHEALTHY);
        }
    }

    /* Sets status of a BPM */
    public void set(int id, AbstractLegacyBeamProfileMonitor bpm) {
        // If it's the beam stop
//        if (!bpm.getCanbusStatus()) {
 //           set(id, "Not Connected", UNHEALTHY);
//        } else if (!bpm.getCalibrationStatus()) {
//            set(id, "Not Calibrated", UNHEALTHY);
 //       } else {
 //           set(id, bpm.getPosition());
 //       }
    	set(id, bpm.getPosition());
    }

    /* Sets status of an Insertable device */
    public void set(int id, Insertable.Position status) {
        // If it's the beam stop
        if (id == S2E_STATUS) {
            set(id, status.toString(), getColor(status, false));
        // Otherwise, assume it's a BPM
        } else {
            set(id, status.toString(), getColor(status, true));
        }
    }

    /* getColor returns GRAY if null, GREEN if true, RED if false */
    public static Color getColor(Boolean bool) {
        if (bool == null) {
            return NO_COLOR;
        }
        if (bool) {
            return HEALTHY;
        } else {
            return UNHEALTHY;
        }
    }

    public Color getColor(Insertable.Position status, boolean bpm) {
        if (bpm) {
            switch (status) {
                case INSERTED:
                    return HEALTHY;
                case MOVING:
                    return WARNING;
                default:
                    return UNHEALTHY;
            }
        } else {
            switch (status) {
                case RETRACTED:
                    return HEALTHY;
                case MOVING:
                    return WARNING;
                default:
                    return UNHEALTHY;
            }
        }
    }

    public void set(int id, String status) {
        set(id, status, NO_COLOR);
    }

    public void set(int id, String status, Boolean ok) {
        if (ok) {
            set(id, status, HEALTHY);
        } else {
            set(id, status, UNHEALTHY);
        }
    }

    public void set(int id, Boolean ok) {
        if (ok) {
            set(id, ok, UNHEALTHY);
        } else {
            set(id, ok, HEALTHY);
        }
    }

    public void set(int id, Boolean ok, Color color) {
        if(id < mNum) {
            //mStatus[id] = status;
            mColor[id]  = color;
            mBool[id]   = (color == HEALTHY);
        } else {
            // TODO: Throw error
        }
    }

    public void set(int id, String status, Color color) {
        if(id < mNum) {
            mStatus[id] = status;
            mColor[id]  = color;
            mBool[id]   = (color == HEALTHY);
        } else {
            // TODO: Throw error
        }
    }

    public int getSize() {
        return mNum;
    }

    public String[] getStrings(){
        return mStatus;
    }

    public String getString(int id) {
        if(id < mNum) {
            return mStatus[id];
        } else {
            return null;
        }
    }

    public Boolean andBool() {
        Boolean temp = true;
        for (int i = 0; i < mNum; i++) {
            temp = (temp && mBool[i]);
        }
        return temp;
    }

    public Boolean andBool(int end) {
        return andBool(0, end);
    }

    public Boolean andBool(int start, int end) {
        if ((start >= 0) && (end < mNum)) {
            Boolean temp = true;
            for (int i = start; i <= end; i++) {
                temp = (temp && mBool[i]);
            }
            return temp;
        } else {
            return null;
        }
    }

    public Boolean getBool(int id) {
        if(id < mNum) {
            return mBool[id];
        } else {
            return null;
        }
    }

    public Boolean[] getBools(){
        return mBool;
    }

    public Color getColor(int id) {
        if(id < mNum) {
            return mColor[id];
        } else {
            return null;
        }
    }

    //Adding CSV read/write
    private void csvCreate(File newFile) {
        try(BufferedWriter out = new BufferedWriter(new FileWriter(newFile, true))) {
            log.info("Creating new CSV file");
            out.write("Time                        ,");
            //for (int i = 0; i < Status.Magnet_names.length; i++) {
           //     out.write(Status.Magnet_names[i] + ",");
           // }
           // for (int i = 0; i < Status.Cyclo_names.length; i++) {
           //     out.write(Status.Cyclo_names[5]);
           //     if (i < (Status.Cyclo_names.length - 1)) {
                    out.write("Deflector Current(mA),value");
           //     } else {
                    out.newLine();
           //     }
           // }
        }catch (IOException e) {
            log.error(e);
        }

        //csvWrite(newFile, 0.0);
    }

    private void csvWrite(File resultsFile, double dval) {
        log.info("Adding line to CSV file");
        try(BufferedWriter out = new BufferedWriter(new FileWriter(resultsFile, true))) {
            out.write(new Date().toString() + ",");
           // for (int i = 0; i < newCurrText.length; i++) {
           //     out.write(newCurrText[i].getText() + ",");
           // }
            out.write("Deflector Current(mA)," + String.format("%.4g", dval));
            out.newLine();
            //for (int i = 0; i < Status.Cyclo_read.length; i++) {
            //    acu.getTagValue(Status.Cyclo_read[i]);
            //    if (i < (Cyclo_read.length - 1)) {
            //        out.write(",");
            //    } else {
            //        out.newLine();
            //    }
            //}
        }catch (IOException e) {
            log.error(e);
        }
    }
}
