/* Copyright Ion Beam Applications */

/*******************************************************************************
    Description
    -----------
   
    RPCGEN source file for module: ecubtcu2
*******************************************************************************/

/*******************************************************************************
    Modification history
    --------------------
    01,PBR,30Jan2002      DEV-0008  Generated by python script
*******************************************************************************/

typedef unsigned char UINT8;

typedef unsigned short UINT16;

typedef char DEVICE_NAME[20];

typedef char ECUBTCU2_EQUIPMENT_NAME[20];

typedef char ECUBTCU2_DEVICE_NAME[20];

typedef char ECUBTCU2_INDEX_NAME[40];

typedef char ECUBTCU2_CONFIG_DATA[640];

enum BCM_CONVERSION_TYPE {
    BCM_NORMAL_GAIN = 0,
    BCM_HIGH_GAIN,
    BCM_NORMAL_CALIBRATE,
    BCM_HIGH_CALIBRATE,
    BCM_NORMAL_OFFSET,
    BCM_HIGH_OFFSET,
    BCM_NORMAL_FULL_CALIBRATE,
    BCM_HIGH_FULL_CALIBRATE
    };

enum BCM_TRIGGER_TYPE {
    BCM_STROBE_TRIGGER = 0,
    BCM_FALLING_TRIGGER,
    BCM_RISING_TRIGGER
    };

enum BPM_ACQUISITION_MODE {
    BPM_NORMAL_GAIN = 0,
    BPM_HIGH_GAIN,
    BPM_NORMAL_CALIBRATE,
    BPM_HIGH_CALIBRATE,
    BPM_NORMAL_OFFSET,
    BPM_HIGH_OFFSET,
    BPM_NORMAL_FULL_CALIBRATE,
    BPM_HIGH_FULL_CALIBRATE
    };

enum BPM_TRIGGER_TYPE {
    BPM_STROBE_TRIGGER = 0,
    BPM_FALLING_TRIGGER,
    BPM_RISING_TRIGGER
    };

enum CANMAGNET_ROOM {
    CANMAGNET_ROOM1,
    CANMAGNET_ROOM2,
    CANMAGNET_ROOM3_EYE,
    CANMAGNET_ROOM3_STAR,
    CANMAGNET_ROOM3_EXP,
    CANMAGNET_ROOM2_PBS
    };

enum BEAM_LINE_REF_SETPOINT_TYPE {
    BEAM_LINE_REF_SETPOINT_RANGE,
    BEAM_LINE_REF_SETPOINT_TUNING,
    BEAM_LINE_REF_SETPOINT_LAST_TYPE
    };

enum BEAM_TUNING_BEAM_LINES_NAMES {
    BEAM_TUNING_BEAM_LINE_1_DS,
    BEAM_TUNING_BEAM_LINE_2_DS,
    BEAM_TUNING_BEAM_LINE_3A,
    BEAM_TUNING_BEAM_LINE_3B,
    BEAM_TUNING_BEAM_LINE_3C,
    BEAM_TUNING_BEAM_LINE_2_PBS,
    BEAM_TUNING_BEAM_LINE_1_US,
    BEAM_TUNING_BEAM_LINE_2_US,
    BEAM_TUNING_BEAM_LINE_UNKNOWN
    };

enum BEAM_TUNING_MODE {
    BEAM_TUNING_AUTOMATIC = 0,
    BEAM_TUNING_IRRADIATION,
    BEAM_TUNING_IDLE,
    BEAM_TUNING_LAST_MODE
    };

struct ECU_TCU_RESPONSE {
    UINT8 xValueIsValid;
    UINT8 yValueIsValid;
    double xSkewness;
    double ySkewness;
    double xSigma;
    double ySigma;
    double xMean;
    double yMean;
    double totalCount;
    UINT8 acquisitionId;
    };

struct ecubtcu2SetConfiguration_in {
    ECUBTCU2_EQUIPMENT_NAME pEquipmentName;
    ECUBTCU2_DEVICE_NAME pDeviceName;
    ECUBTCU2_INDEX_NAME pParameterName;
    ECUBTCU2_CONFIG_DATA pValue;
};

struct ecubtcu2SetConfiguration_out {
    int returnValue;
};

struct ecubtcu2EndSetConfiguration_out {
    int returnValue;
};

struct ecubtcu2RequestShutdown_out {
    int returnValue;
};

struct analogPsSetVoltage_in {
    DEVICE_NAME pName;
    double setVoltage;
};

struct analogPsSetVoltage_out {
    int returnValue;
};

struct analogPsSetCurrent_in {
    DEVICE_NAME pName;
    double setCurrent;
};

struct analogPsSetCurrent_out {
    int returnValue;
};

struct bcmStartCalibration_in {
    DEVICE_NAME pName;
};

struct bcmStartCalibration_out {
    int returnValue;
};

struct bcmChangeGain_in {
    DEVICE_NAME pName;
    BCM_CONVERSION_TYPE gain;
};

struct bcmChangeGain_out {
    int returnValue;
};

struct bcmChangeMode_in {
    DEVICE_NAME pName;
    BCM_CONVERSION_TYPE mode;
    BCM_TRIGGER_TYPE acqType;
};

struct bcmChangeMode_out {
    int returnValue;
};

struct bcmReset_in {
    DEVICE_NAME pName;
};

struct bcmReset_out {
    int returnValue;
};

struct bpmStartContinuousAcquisition_in {
    DEVICE_NAME pName;
};

struct bpmStartContinuousAcquisition_out {
    int returnValue;
};

struct bpmStopContinuousAcquisition_in {
    DEVICE_NAME pName;
};

struct bpmStopContinuousAcquisition_out {
    int returnValue;
};

struct bpmStartCalibration_in {
    DEVICE_NAME pName;
};

struct bpmStartCalibration_out {
    int returnValue;
};

struct bpmStartProfileAcquisition_in {
    DEVICE_NAME pName;
    int sample;
};

struct bpmStartProfileAcquisition_out {
    int returnValue;
};

struct bpmStopProfileAcquisition_in {
    DEVICE_NAME pName;
};

struct bpmStopProfileAcquisition_out {
    int returnValue;
};

struct bpmChangeGain_in {
    DEVICE_NAME pName;
    int gain;
};

struct bpmChangeGain_out {
    int returnValue;
};

struct bpmChangeMode_in {
    DEVICE_NAME pName;
    BPM_ACQUISITION_MODE mode;
    BPM_TRIGGER_TYPE acqType;
};

struct bpmChangeMode_out {
    int returnValue;
};

struct bpmDecreaseRangeByFactor_in {
    DEVICE_NAME pName;
    double factor;
};

struct bpmDecreaseRangeByFactor_out {
    int returnValue;
};

struct bpmIncreaseRangeByFactor_in {
    DEVICE_NAME pName;
    double factor;
};

struct bpmIncreaseRangeByFactor_out {
    int returnValue;
};

struct bpmReset_in {
    DEVICE_NAME pName;
};

struct bpmReset_out {
    int returnValue;
};

struct canMagnetSelectTreatmentRoom_in {
    CANMAGNET_ROOM roomId;
};

struct canMagnetSelectTreatmentRoom_out {
    int returnValue;
};

struct canMagnetSetValue_in {
    DEVICE_NAME pName;
    UINT16 setPoint;
};

struct canMagnetSetValue_out {
    int returnValue;
};

struct canMagnetSetCurrent_in {
    DEVICE_NAME pName;
    double setCurrent;
};

struct canMagnetSetCurrent_out {
    int returnValue;
};

struct canMagnetSetField_in {
    DEVICE_NAME pName;
    double setField;
};

struct canMagnetSetField_out {
    int returnValue;
};

struct canMagnetStartCyclingDigit_in {
    DEVICE_NAME pName;
    UINT16 setPoint;
};

struct canMagnetStartCyclingDigit_out {
    int returnValue;
};

struct canMagnetStartCyclingCurrent_in {
    DEVICE_NAME pName;
    double setCurrent;
};

struct canMagnetStartCyclingCurrent_out {
    int returnValue;
};

struct canMagnetStartCyclingField_in {
    DEVICE_NAME pName;
    double setField;
};

struct canMagnetStartCyclingField_out {
    int returnValue;
};

struct canMagnetUpdateB1234Field_in {
    double newField;
};

struct canMagnetUpdateB1234Field_out {
    int returnValue;
};

struct degraderGoHome_out {
    int returnValue;
};

struct degraderHighGoPosition_in {
    int steps;
};

struct degraderHighGoPosition_out {
    int returnValue;
};

struct degraderGoStair_in {
    int stairIndex;
};

struct degraderGoStair_out {
    int returnValue;
};

struct degraderGoOnePlus_out {
    int returnValue;
};

struct degraderGoOneMinus_out {
    int returnValue;
};

struct degraderGoRelative_in {
    int relStair;
};

struct degraderGoRelative_out {
    int returnValue;
};

struct degraderCheckStair_in {
    int param;
};

struct degraderCheckStair_out {
    int returnValue;
};

struct degraderGoEnergy_in {
    double energy;
};

struct degraderGoEnergy_out {
    int returnValue;
};

struct degraderCheckEnergy_in {
    double energy;
};

struct degraderCheckEnergy_out {
    int returnValue;
};

struct iseuRequestSetLookUpMode_out {
    int returnValue;
};

struct iseuRequestSetInternalPulseMode_out {
    int returnValue;
};

struct iseuRequestSetTreatmentRoom_in {
    int roomId;
};

struct iseuRequestSetTreatmentRoom_out {
    int returnValue;
};

struct iseuRequestSetEndOfTreatmentMode_out {
    int returnValue;
};

struct iseuRequestGeneratePulse_out {
    int returnValue;
};

struct iseuRequestSetSinglePulseMode_in {
    int flag;
};

struct iseuRequestSetSinglePulseMode_out {
    int returnValue;
};

struct iseuRequestBypassRegulation_in {
    int flag;
};

struct iseuRequestBypassRegulation_out {
    int returnValue;
};

struct iseuRequestSetCurrentAtCycloExit_in {
    double current;
};

struct iseuRequestSetCurrentAtCycloExit_out {
    int returnValue;
};

struct slitsCheckMm_in {
    DEVICE_NAME pName;
    double space;
    double limit;
};

struct slitsCheckMm_out {
    int returnValue;
};

struct slitsCheckStep_in {
    DEVICE_NAME pName;
    double step;
};

struct slitsCheckStep_out {
    int returnValue;
};

struct slitsGoHome_in {
    DEVICE_NAME pName;
};

struct slitsGoHome_out {
    int returnValue;
};

struct slitsHighGoPosition_in {
    DEVICE_NAME pName;
    int steps;
};

struct slitsHighGoPosition_out {
    int returnValue;
};

struct slitsGoRelative_in {
    DEVICE_NAME pName;
    int steps;
};

struct slitsGoRelative_out {
    int returnValue;
};

struct slitsGoOnePlus_in {
    DEVICE_NAME pName;
};

struct slitsGoOnePlus_out {
    int returnValue;
};

struct slitsGoOneMinus_in {
    DEVICE_NAME pName;
};

struct slitsGoOneMinus_out {
    int returnValue;
};

struct slitsHighGoMm_in {
    DEVICE_NAME pName;
    double space;
};

struct slitsHighGoMm_out {
    int returnValue;
};

struct beamLineSelectBeamLine_in {
    BEAM_TUNING_BEAM_LINES_NAMES beamLineName;
};

struct beamLineSelectBeamLine_out {
    int returnValue;
};

struct beamLineUnselectBeamLine_out {
    int returnValue;
};

struct beamLineGetSelectedBeamLine_out {
    int returnValue;
    BEAM_TUNING_BEAM_LINES_NAMES beamLineName;
};

struct beamLineSetRange_in {
    double range;
    double gantryAngle;
};

struct beamLineSetRange_out {
    int returnValue;
};

struct beamLineSetDegraderToRange_out {
    int returnValue;
};

struct beamLineSetDegraderToBeamStop_out {
    int returnValue;
};

struct beamLineStartTuning_in {
    BEAM_TUNING_MODE mode;
};

struct beamLineStartTuning_out {
    int returnValue;
};

struct beamLineStopTuning_out {
    int returnValue;
};

struct beamLineResetTuning_out {
    int returnValue;
};

struct beamLineTuneNextBlock_out {
    int returnValue;
};

struct beamLineRedoTuningBlock_out {
    int returnValue;
};

struct beamLineByPassTuningBlock_out {
    int returnValue;
};

struct beamLineManualAcquire_out {
    int returnValue;
};

struct beamLineStopManualAcquire_out {
    int returnValue;
};

struct beamLineSaveReferenceSetPoint_in {
    BEAM_TUNING_BEAM_LINES_NAMES beamLineName;
    BEAM_LINE_REF_SETPOINT_TYPE referenceType;
};

struct beamLineSaveReferenceSetPoint_out {
    int returnValue;
};

struct beamLineStartPhaseSpace_in {
    DEVICE_NAME pMeasName;
    DEVICE_NAME pMagnetName;
    int numberOfStep;
    double lowSetpoint;
    double highSetpoint;
};

struct beamLineStartPhaseSpace_out {
    int returnValue;
};

struct beamLineIrradSetRange_in {
    double range;
};

struct beamLineIrradSetRange_out {
    int returnValue;
};

struct ecuTcuBeamTuningNotStarted_out {
    int returnValue;
};

struct ecuTcuBeamTuningStarted_out {
    int returnValue;
};

struct ecuTcuCountingNotStarted_out {
    int returnValue;
};

struct ecuTcuCountingStarted_out {
    int returnValue;
};

struct ecuTcuOnRequestAcquisition_in {
    ECU_TCU_RESPONSE response;
};

struct ecuTcuOnRequestAcquisition_out {
    int returnValue;
};

program RPC_PROGRAM {
    version RPC_VERSION {
        ecubtcu2SetConfiguration_out ECUBTCU2SETCONFIGURATION(ecubtcu2SetConfiguration_in) = 1;
        ecubtcu2EndSetConfiguration_out ECUBTCU2ENDSETCONFIGURATION(void) = 2;
        ecubtcu2RequestShutdown_out ECUBTCU2REQUESTSHUTDOWN(void) = 3;
        analogPsSetVoltage_out ANALOGPSSETVOLTAGE(analogPsSetVoltage_in) = 4;
        analogPsSetCurrent_out ANALOGPSSETCURRENT(analogPsSetCurrent_in) = 5;
        bcmStartCalibration_out BCMSTARTCALIBRATION(bcmStartCalibration_in) = 6;
        bcmChangeGain_out BCMCHANGEGAIN(bcmChangeGain_in) = 7;
        bcmChangeMode_out BCMCHANGEMODE(bcmChangeMode_in) = 8;
        bcmReset_out BCMRESET(bcmReset_in) = 9;
        bpmStartContinuousAcquisition_out BPMSTARTCONTINUOUSACQUISITION(bpmStartContinuousAcquisition_in) = 10;
        bpmStopContinuousAcquisition_out BPMSTOPCONTINUOUSACQUISITION(bpmStopContinuousAcquisition_in) = 11;
        bpmStartCalibration_out BPMSTARTCALIBRATION(bpmStartCalibration_in) = 12;
        bpmStartProfileAcquisition_out BPMSTARTPROFILEACQUISITION(bpmStartProfileAcquisition_in) = 13;
        bpmStopProfileAcquisition_out BPMSTOPPROFILEACQUISITION(bpmStopProfileAcquisition_in) = 14;
        bpmChangeGain_out BPMCHANGEGAIN(bpmChangeGain_in) = 15;
        bpmChangeMode_out BPMCHANGEMODE(bpmChangeMode_in) = 16;
        bpmDecreaseRangeByFactor_out BPMDECREASERANGEBYFACTOR(bpmDecreaseRangeByFactor_in) = 17;
        bpmIncreaseRangeByFactor_out BPMINCREASERANGEBYFACTOR(bpmIncreaseRangeByFactor_in) = 18;
        bpmReset_out BPMRESET(bpmReset_in) = 19;
        canMagnetSelectTreatmentRoom_out CANMAGNETSELECTTREATMENTROOM(canMagnetSelectTreatmentRoom_in) = 20;
        canMagnetSetValue_out CANMAGNETSETVALUE(canMagnetSetValue_in) = 21;
        canMagnetSetCurrent_out CANMAGNETSETCURRENT(canMagnetSetCurrent_in) = 22;
        canMagnetSetField_out CANMAGNETSETFIELD(canMagnetSetField_in) = 23;
        canMagnetStartCyclingDigit_out CANMAGNETSTARTCYCLINGDIGIT(canMagnetStartCyclingDigit_in) = 24;
        canMagnetStartCyclingCurrent_out CANMAGNETSTARTCYCLINGCURRENT(canMagnetStartCyclingCurrent_in) = 25;
        canMagnetStartCyclingField_out CANMAGNETSTARTCYCLINGFIELD(canMagnetStartCyclingField_in) = 26;
        canMagnetUpdateB1234Field_out CANMAGNETUPDATEB1234FIELD(canMagnetUpdateB1234Field_in) = 27;
        degraderGoHome_out DEGRADERGOHOME(void) = 28;
        degraderHighGoPosition_out DEGRADERHIGHGOPOSITION(degraderHighGoPosition_in) = 29;
        degraderGoStair_out DEGRADERGOSTAIR(degraderGoStair_in) = 30;
        degraderGoOnePlus_out DEGRADERGOONEPLUS(void) = 31;
        degraderGoOneMinus_out DEGRADERGOONEMINUS(void) = 32;
        degraderGoRelative_out DEGRADERGORELATIVE(degraderGoRelative_in) = 33;
        degraderCheckStair_out DEGRADERCHECKSTAIR(degraderCheckStair_in) = 34;
        degraderGoEnergy_out DEGRADERGOENERGY(degraderGoEnergy_in) = 35;
        degraderCheckEnergy_out DEGRADERCHECKENERGY(degraderCheckEnergy_in) = 36;
        iseuRequestSetLookUpMode_out ISEUREQUESTSETLOOKUPMODE(void) = 37;
        iseuRequestSetInternalPulseMode_out ISEUREQUESTSETINTERNALPULSEMODE(void) = 38;
        iseuRequestSetTreatmentRoom_out ISEUREQUESTSETTREATMENTROOM(iseuRequestSetTreatmentRoom_in) = 39;
        iseuRequestSetEndOfTreatmentMode_out ISEUREQUESTSETENDOFTREATMENTMODE(void) = 40;
        iseuRequestGeneratePulse_out ISEUREQUESTGENERATEPULSE(void) = 41;
        iseuRequestSetSinglePulseMode_out ISEUREQUESTSETSINGLEPULSEMODE(iseuRequestSetSinglePulseMode_in) = 42;
        iseuRequestBypassRegulation_out ISEUREQUESTBYPASSREGULATION(iseuRequestBypassRegulation_in) = 43;
        iseuRequestSetCurrentAtCycloExit_out ISEUREQUESTSETCURRENTATCYCLOEXIT(iseuRequestSetCurrentAtCycloExit_in) = 44;
        slitsCheckMm_out SLITSCHECKMM(slitsCheckMm_in) = 45;
        slitsCheckStep_out SLITSCHECKSTEP(slitsCheckStep_in) = 46;
        slitsGoHome_out SLITSGOHOME(slitsGoHome_in) = 47;
        slitsHighGoPosition_out SLITSHIGHGOPOSITION(slitsHighGoPosition_in) = 48;
        slitsGoRelative_out SLITSGORELATIVE(slitsGoRelative_in) = 49;
        slitsGoOnePlus_out SLITSGOONEPLUS(slitsGoOnePlus_in) = 50;
        slitsGoOneMinus_out SLITSGOONEMINUS(slitsGoOneMinus_in) = 51;
        slitsHighGoMm_out SLITSHIGHGOMM(slitsHighGoMm_in) = 52;
        beamLineSelectBeamLine_out BEAMLINESELECTBEAMLINE(beamLineSelectBeamLine_in) = 53;
        beamLineUnselectBeamLine_out BEAMLINEUNSELECTBEAMLINE(void) = 54;
        beamLineGetSelectedBeamLine_out BEAMLINEGETSELECTEDBEAMLINE(void) = 55;
        beamLineSetRange_out BEAMLINESETRANGE(beamLineSetRange_in) = 56;
        beamLineSetDegraderToRange_out BEAMLINESETDEGRADERTORANGE(void) = 57;
        beamLineSetDegraderToBeamStop_out BEAMLINESETDEGRADERTOBEAMSTOP(void) = 58;
        beamLineStartTuning_out BEAMLINESTARTTUNING(beamLineStartTuning_in) = 59;
        beamLineStopTuning_out BEAMLINESTOPTUNING(void) = 60;
        beamLineResetTuning_out BEAMLINERESETTUNING(void) = 61;
        beamLineTuneNextBlock_out BEAMLINETUNENEXTBLOCK(void) = 62;
        beamLineRedoTuningBlock_out BEAMLINEREDOTUNINGBLOCK(void) = 63;
        beamLineByPassTuningBlock_out BEAMLINEBYPASSTUNINGBLOCK(void) = 64;
        beamLineManualAcquire_out BEAMLINEMANUALACQUIRE(void) = 65;
        beamLineStopManualAcquire_out BEAMLINESTOPMANUALACQUIRE(void) = 66;
        beamLineSaveReferenceSetPoint_out BEAMLINESAVEREFERENCESETPOINT(beamLineSaveReferenceSetPoint_in) = 67;
        beamLineStartPhaseSpace_out BEAMLINESTARTPHASESPACE(beamLineStartPhaseSpace_in) = 68;
        beamLineIrradSetRange_out BEAMLINEIRRADSETRANGE(beamLineIrradSetRange_in) = 69;
        ecuTcuBeamTuningNotStarted_out ECUTCUBEAMTUNINGNOTSTARTED(void) = 70;
        ecuTcuBeamTuningStarted_out ECUTCUBEAMTUNINGSTARTED(void) = 71;
        ecuTcuCountingNotStarted_out ECUTCUCOUNTINGNOTSTARTED(void) = 72;
        ecuTcuCountingStarted_out ECUTCUCOUNTINGSTARTED(void) = 73;
        ecuTcuOnRequestAcquisition_out ECUTCUONREQUESTACQUISITION(ecuTcuOnRequestAcquisition_in) = 74;
    } = 1;
} = 55555555;
