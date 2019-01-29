/* Copyright Ion Beam Applications */

/*******************************************************************************
    Description
    -----------
    
    RPCGEN source file for module: ecubtcu
*******************************************************************************/

/*******************************************************************************
    Modification history
    --------------------
    01,PBR,30Jan2002      DEV-0008  Generated by python script
*******************************************************************************/


typedef unsigned char UINT8;
typedef unsigned short UINT16;
typedef int STATUS;

typedef char DEVICE_NAME[50];

typedef char ECUBTCU_EQUIPMENT_NAME[20];

typedef char ECUBTCU_DEVICE_NAME[50];

typedef char ECUBTCU_INDEX_NAME[40];

typedef char ECUBTCU_CONFIG_DATA[1280];

typedef int ECUBTCU_CONFIG_TYPE;

typedef char BPM_DEVICE_NAME[50];

typedef char BS_DEVICE_NAME[50];

typedef char STEPPER_DEVICE_NAME[50];

enum BCM_CONVERSION_TYPE {
    BCM_NORMAL_GAIN = 0,
    BCM_DEFAULT_GAIN = BCM_NORMAL_GAIN,
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
    BCM_DEFAULT_TRIGGER = BCM_FALLING_TRIGGER,
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

enum BEAM_TUNING_BEAM_LINES_NAMES {
    BEAM_TUNING_BEAM_LINE_FIRST_LINE,
    BEAM_TUNING_BEAM_LINE_1 = BEAM_TUNING_BEAM_LINE_FIRST_LINE,
    BEAM_TUNING_BEAM_LINE_2,
    BEAM_TUNING_BEAM_LINE_3,
    BEAM_TUNING_BEAM_LINE_4,
    BEAM_TUNING_BEAM_LINE_5,
    BEAM_TUNING_BEAM_LINE_6,
    BEAM_TUNING_BEAM_LINE_0,
    BEAM_TUNING_BEAM_LINE_UNKNOWN
    };

enum BEAM_LINE_REF_SETPOINT_TYPE {
    BEAM_LINE_REF_SETPOINT_RANGE,
    BEAM_LINE_REF_SETPOINT_TUNING,
    BEAM_LINE_REF_SETPOINT_LAST_TYPE
    };

enum BEAM_TUNING_MODE {
    BEAM_TUNING_AUTOMATIC = 0,
    BEAM_TUNING_IRRADIATION,
    BEAM_TUNING_IDLE,
    BEAM_TUNING_LAST_MODE
    };

enum BEAM_TUNING_TREATMENT_MODE {
    BEAM_TUNING_TREATMENT_NO_MODE = 0,
    BEAM_TUNING_TREATMENT_WOBBLING,
    BEAM_TUNING_TREATMENT_DSCAT,
    BEAM_TUNING_TREATMENT_LAST_MODE
    };

struct ECU_TCU_RESPONSE {
    UINT8 xValueIsValid;
    UINT8 yValueIsValid;
    double xSkewness;
    double ySkewness;
    double IC23xSigma;
    double IC1xSigma;
    double IC23ySigma;
    double IC1ySigma;
    double xMean;
    double yMean;
    double IC23totalCount;
    double IC1totalCount;
    UINT8 acquisitionId;
    };

struct ecubtcuSetConfiguration_in {
    ECUBTCU_EQUIPMENT_NAME pEquipmentName;
    ECUBTCU_DEVICE_NAME pDeviceName;
    ECUBTCU_INDEX_NAME pParameterName;
    ECUBTCU_CONFIG_DATA pValue;
    ECUBTCU_CONFIG_TYPE type;
};

struct ecubtcuSetConfiguration_out {
    STATUS returnValue;
};

struct ecubtcuEndSetConfiguration_out {
    STATUS returnValue;
};

struct ecubtcuRequestShutdown_out {
    STATUS returnValue;
};

struct group3SetOffset_in {
    double offset;
};

struct group3SetOffset_out {
    STATUS returnValue;
};

struct group3SetGain_in {
    double gain;
};

struct group3SetGain_out {
    STATUS returnValue;
};

struct bpmInsert_in {
    BPM_DEVICE_NAME pName;
};

struct bpmInsert_out {
    STATUS returnValue;
};

struct bpmRetract_in {
    BPM_DEVICE_NAME pName;
};

struct bpmRetract_out {
    STATUS returnValue;
};

struct bsInsert_in {
    BS_DEVICE_NAME pName;
};

struct bsInsert_out {
    STATUS returnValue;
};

struct bsRetract_in {
    BS_DEVICE_NAME pName;
};

struct bsRetract_out {
    STATUS returnValue;
};

struct stepperIoExcite_in {
    STEPPER_DEVICE_NAME pName;
};

struct stepperIoExcite_out {
    STATUS returnValue;
};

struct stepperIoDesexcite_in {
    STEPPER_DEVICE_NAME pName;
};

struct stepperIoDesexcite_out {
    STATUS returnValue;
};

struct stepperIoCheckExcitation_in {
    STEPPER_DEVICE_NAME pName;
};

struct stepperIoCheckExcitation_out {
    STATUS returnValue;
};

struct bcmStartCalibration_in {
    DEVICE_NAME pName;
};

struct bcmStartCalibration_out {
    STATUS returnValue;
};

struct bcmAllStartCalibration_out {
    STATUS returnValue;
};

struct bcmChangeMode_in {
    DEVICE_NAME pName;
    BCM_CONVERSION_TYPE mode;
    BCM_TRIGGER_TYPE acqType;
};

struct bcmChangeMode_out {
    STATUS returnValue;
};

struct bcmReset_in {
    DEVICE_NAME pName;
};

struct bcmReset_out {
    STATUS returnValue;
};

struct bcmAllReset_out {
    STATUS returnValue;
};

struct bpmStartContinuousAcquisition_in {
    DEVICE_NAME pName;
};

struct bpmStartContinuousAcquisition_out {
    STATUS returnValue;
};

struct bpmStopContinuousAcquisition_in {
    DEVICE_NAME pName;
};

struct bpmStopContinuousAcquisition_out {
    STATUS returnValue;
};

struct bpmAllStartCalibration_out {
    STATUS returnValue;
};

struct bpmStartCalibration_in {
    DEVICE_NAME pName;
};

struct bpmStartCalibration_out {
    STATUS returnValue;
};

struct bpmStartProfileAcquisition_in {
    DEVICE_NAME pName;
    int sample;
};

struct bpmStartProfileAcquisition_out {
    STATUS returnValue;
};

struct bpmStopProfileAcquisition_in {
    DEVICE_NAME pName;
};

struct bpmStopProfileAcquisition_out {
    STATUS returnValue;
};

struct bpmChangeGain_in {
    DEVICE_NAME pName;
    int gain;
};

struct bpmChangeGain_out {
    STATUS returnValue;
};

struct bpmChangeMode_in {
    DEVICE_NAME pName;
    BPM_ACQUISITION_MODE mode;
    BPM_TRIGGER_TYPE acqType;
};

struct bpmChangeMode_out {
    STATUS returnValue;
};

struct bpmDecreaseRangeByFactor_in {
    DEVICE_NAME pName;
    double factor;
};

struct bpmDecreaseRangeByFactor_out {
    STATUS returnValue;
};

struct bpmIncreaseRangeByFactor_in {
    DEVICE_NAME pName;
    double factor;
};

struct bpmIncreaseRangeByFactor_out {
    STATUS returnValue;
};

struct bpmAllReset_out {
    STATUS returnValue;
};

struct bpmReset_in {
    DEVICE_NAME pName;
};

struct bpmReset_out {
    STATUS returnValue;
};

struct canMagnetSetValue_in {
    DEVICE_NAME pName;
    UINT16 setPoint;
};

struct canMagnetSetValue_out {
    STATUS returnValue;
};

struct canMagnetSetCurrent_in {
    DEVICE_NAME pName;
    double setCurrent;
};

struct canMagnetSetCurrent_out {
    STATUS returnValue;
};

struct canMagnetSetField_in {
    DEVICE_NAME pName;
    double setField;
};

struct canMagnetSetField_out {
    STATUS returnValue;
};

struct canMagnetStartCyclingDigit_in {
    DEVICE_NAME pName;
    UINT16 setPoint;
};

struct canMagnetStartCyclingDigit_out {
    STATUS returnValue;
};

struct canMagnetStartCyclingCurrent_in {
    DEVICE_NAME pName;
    double setCurrent;
};

struct canMagnetStartCyclingCurrent_out {
    STATUS returnValue;
};

struct canMagnetStartCyclingField_in {
    DEVICE_NAME pName;
    double setField;
};

struct canMagnetStartCyclingField_out {
    STATUS returnValue;
};

struct canMagnetAllStartCyclingCurrent_out {
    STATUS returnValue;
};

struct degraderGoHome_out {
    STATUS returnValue;
};

struct degraderHighGoPosition_in {
    int steps;
};

struct degraderHighGoPosition_out {
    STATUS returnValue;
};

struct degraderGoSpecialBlock_in {
    unsigned int blockId;
};

struct degraderGoSpecialBlock_out {
    STATUS returnValue;
};

struct degraderGoOnePlus_out {
    STATUS returnValue;
};

struct degraderGoOneMinus_out {
    STATUS returnValue;
};

struct degraderCheckSpecialBlock_in {
    unsigned int blockId;
};

struct degraderCheckSpecialBlock_out {
    STATUS returnValue;
};

struct degraderGoEnergy_in {
    double energy;
};

struct degraderGoEnergy_out {
    STATUS returnValue;
};

struct degraderCheckEnergy_in {
    double energy;
};

struct degraderCheckEnergy_out {
    STATUS returnValue;
};

struct degarderStop_out {
    STATUS returnValue;
};

struct iseuRequestSetLookUpMode_out {
    STATUS returnValue;
};

struct iseuRequestSetInternalPulseMode_out {
    STATUS returnValue;
};

struct iseuRequestSetTreatmentRoom_in {
    int roomId;
};

struct iseuRequestSetTreatmentRoom_out {
    STATUS returnValue;
};

struct iseuRequestSetEndOfTreatmentMode_out {
    STATUS returnValue;
};

struct iseuRequestGeneratePulse_out {
    STATUS returnValue;
};

struct iseuRequestSetSinglePulseMode_in {
    int flag;
};

struct iseuRequestSetSinglePulseMode_out {
    STATUS returnValue;
};

struct iseuRequestBypassRegulation_in {
    int flag;
};

struct iseuRequestBypassRegulation_out {
    STATUS returnValue;
};

struct iseuRequestSetCurrentAtCycloExit_in {
    double current;
};

struct iseuRequestSetCurrentAtCycloExit_out {
    STATUS returnValue;
};

struct motorisedSlitsHome_in {
    DEVICE_NAME pName;
};

struct motorisedSlitsHome_out {
    STATUS returnValue;
};

struct motorisedSlitsGoHome_in {
    DEVICE_NAME pName;
};

struct motorisedSlitsGoHome_out {
    STATUS returnValue;
};

struct motorisedSlitsGoToPosition_in {
    DEVICE_NAME pName;
    unsigned int pPosition;
};

struct motorisedSlitsGoToPosition_out {
    STATUS returnValue;
};

struct motorisedSlitsGoToOpening_in {
    DEVICE_NAME pName;
    double pOpening;
};

struct motorisedSlitsGoToOpening_out {
    STATUS returnValue;
};

struct motorisedSlitsStop_in {
    DEVICE_NAME pName;
};

struct motorisedSlitsStop_out {
    STATUS returnValue;
};

struct beamLineSelectBeamLine_in {
    BEAM_TUNING_BEAM_LINES_NAMES beamLineName;
    int roomId;
};

struct beamLineSelectBeamLine_out {
    STATUS returnValue;
};

struct beamLineUnselectBeamLine_out {
    STATUS returnValue;
};

struct beamLineSetRange_in {
    double range;
    double gantryAngle;
    int beamlineOptionId;
};

struct beamLineSetRange_out {
    STATUS returnValue;
};

struct beamLineSetDegraderToRange_out {
    STATUS returnValue;
};

struct beamLineSetLineToRange_in {
    double range;
    double gantryAngle;
    int beamlineOptionId;
};

struct beamLineSetLineToRange_out {
    STATUS returnValue;
};

struct beamLineSelectAndSetToRange_in {
    BEAM_TUNING_BEAM_LINES_NAMES beamLineName;
    double range;
    double gantryAngle;
    int beamlineOptionId;
};

struct beamLineSelectAndSetToRange_out {
    STATUS returnValue;
};

struct beamLineSetDegraderToBeamStop_out {
    STATUS returnValue;
};

struct beamLineStartTuning_in {
    BEAM_TUNING_MODE mode;
    BEAM_TUNING_TREATMENT_MODE treatmentMode;
};

struct beamLineStartTuning_out {
    STATUS returnValue;
};

struct beamLineStopTuning_out {
    STATUS returnValue;
};

struct beamLineResetTuning_out {
    STATUS returnValue;
};

struct beamLineTuneNextBlock_out {
    STATUS returnValue;
};

struct beamLineRedoTuningBlock_out {
    STATUS returnValue;
};

struct beamLineByPassTuningBlock_out {
    STATUS returnValue;
};

struct beamLineManualAcquire_out {
    STATUS returnValue;
};

struct beamLineStopManualAcquire_out {
    STATUS returnValue;
};

struct beamLineSaveReferenceSetPoint_in {
    BEAM_TUNING_BEAM_LINES_NAMES beamLineName;
    BEAM_LINE_REF_SETPOINT_TYPE referenceType;
};

struct beamLineSaveReferenceSetPoint_out {
    STATUS returnValue;
};

struct beamLineStartPhaseSpace_in {
    DEVICE_NAME pMeasName;
    DEVICE_NAME pMagnetName;
    int numberOfStep;
    double lowSetpoint;
    double highSetpoint;
};

struct beamLineStartPhaseSpace_out {
    STATUS returnValue;
};

struct ecuTcuBeamTuningNotStarted_out {
    STATUS returnValue;
};

struct ecuTcuBeamTuningStarted_out {
    STATUS returnValue;
};

struct ecuTcuCountingNotStarted_out {
    STATUS returnValue;
};

struct ecuTcuCountingStarted_out {
    STATUS returnValue;
};

struct ecuTcuOnRequestAcquisition_in {
    ECU_TCU_RESPONSE response;
};

struct ecuTcuOnRequestAcquisition_out {
    STATUS returnValue;
};

struct analogPsSetVoltage_in {
    DEVICE_NAME pName;
    double setVoltage;
};

struct analogPsSetVoltage_out {
    STATUS returnValue;
};

struct analogPsSetCurrent_in {
    DEVICE_NAME pName;
    double setCurrent;
};

struct analogPsSetCurrent_out {
    STATUS returnValue;
};

program RPC_PROGRAM {
    version RPC_VERSION {
        ecubtcuSetConfiguration_out ECUBTCUSETCONFIGURATION(ecubtcuSetConfiguration_in) = 1;
        ecubtcuEndSetConfiguration_out ECUBTCUENDSETCONFIGURATION(void) = 2;
        ecubtcuRequestShutdown_out ECUBTCUREQUESTSHUTDOWN(void) = 3;
        group3SetOffset_out GROUP3SETOFFSET(group3SetOffset_in) = 4;
        group3SetGain_out GROUP3SETGAIN(group3SetGain_in) = 5;
        bpmInsert_out BPMINSERT(bpmInsert_in) = 6;
        bpmRetract_out BPMRETRACT(bpmRetract_in) = 7;
        bsInsert_out BSINSERT(bsInsert_in) = 8;
        bsRetract_out BSRETRACT(bsRetract_in) = 9;
        stepperIoExcite_out STEPPERIOEXCITE(stepperIoExcite_in) = 10;
        stepperIoDesexcite_out STEPPERIODESEXCITE(stepperIoDesexcite_in) = 11;
        stepperIoCheckExcitation_out STEPPERIOCHECKEXCITATION(stepperIoCheckExcitation_in) = 12;
        bcmStartCalibration_out BCMSTARTCALIBRATION(bcmStartCalibration_in) = 13;
        bcmAllStartCalibration_out BCMALLSTARTCALIBRATION(void) = 14;
        bcmChangeMode_out BCMCHANGEMODE(bcmChangeMode_in) = 15;
        bcmReset_out BCMRESET(bcmReset_in) = 16;
        bcmAllReset_out BCMALLRESET(void) = 17;
        bpmStartContinuousAcquisition_out BPMSTARTCONTINUOUSACQUISITION(bpmStartContinuousAcquisition_in) = 18;
        bpmStopContinuousAcquisition_out BPMSTOPCONTINUOUSACQUISITION(bpmStopContinuousAcquisition_in) = 19;
        bpmAllStartCalibration_out BPMALLSTARTCALIBRATION(void) = 20;
        bpmStartCalibration_out BPMSTARTCALIBRATION(bpmStartCalibration_in) = 21;
        bpmStartProfileAcquisition_out BPMSTARTPROFILEACQUISITION(bpmStartProfileAcquisition_in) = 22;
        bpmStopProfileAcquisition_out BPMSTOPPROFILEACQUISITION(bpmStopProfileAcquisition_in) = 23;
        bpmChangeGain_out BPMCHANGEGAIN(bpmChangeGain_in) = 24;
        bpmChangeMode_out BPMCHANGEMODE(bpmChangeMode_in) = 25;
        bpmDecreaseRangeByFactor_out BPMDECREASERANGEBYFACTOR(bpmDecreaseRangeByFactor_in) = 26;
        bpmIncreaseRangeByFactor_out BPMINCREASERANGEBYFACTOR(bpmIncreaseRangeByFactor_in) = 27;
        bpmAllReset_out BPMALLRESET(void) = 28;
        bpmReset_out BPMRESET(bpmReset_in) = 29;
        canMagnetSetValue_out CANMAGNETSETVALUE(canMagnetSetValue_in) = 30;
        canMagnetSetCurrent_out CANMAGNETSETCURRENT(canMagnetSetCurrent_in) = 31;
        canMagnetSetField_out CANMAGNETSETFIELD(canMagnetSetField_in) = 32;
        canMagnetStartCyclingDigit_out CANMAGNETSTARTCYCLINGDIGIT(canMagnetStartCyclingDigit_in) = 33;
        canMagnetStartCyclingCurrent_out CANMAGNETSTARTCYCLINGCURRENT(canMagnetStartCyclingCurrent_in) = 34;
        canMagnetStartCyclingField_out CANMAGNETSTARTCYCLINGFIELD(canMagnetStartCyclingField_in) = 35;
        canMagnetAllStartCyclingCurrent_out CANMAGNETALLSTARTCYCLINGCURRENT(void) = 36;
        degraderGoHome_out DEGRADERGOHOME(void) = 37;
        degraderHighGoPosition_out DEGRADERHIGHGOPOSITION(degraderHighGoPosition_in) = 38;
        degraderGoSpecialBlock_out DEGRADERGOSPECIALBLOCK(degraderGoSpecialBlock_in) = 39;
        degraderGoOnePlus_out DEGRADERGOONEPLUS(void) = 40;
        degraderGoOneMinus_out DEGRADERGOONEMINUS(void) = 41;
        degraderCheckSpecialBlock_out DEGRADERCHECKSPECIALBLOCK(degraderCheckSpecialBlock_in) = 42;
        degraderGoEnergy_out DEGRADERGOENERGY(degraderGoEnergy_in) = 43;
        degraderCheckEnergy_out DEGRADERCHECKENERGY(degraderCheckEnergy_in) = 44;
        degarderStop_out DEGARDERSTOP(void) = 45;
        iseuRequestSetLookUpMode_out ISEUREQUESTSETLOOKUPMODE(void) = 46;
        iseuRequestSetInternalPulseMode_out ISEUREQUESTSETINTERNALPULSEMODE(void) = 47;
        iseuRequestSetTreatmentRoom_out ISEUREQUESTSETTREATMENTROOM(iseuRequestSetTreatmentRoom_in) = 48;
        iseuRequestSetEndOfTreatmentMode_out ISEUREQUESTSETENDOFTREATMENTMODE(void) = 49;
        iseuRequestGeneratePulse_out ISEUREQUESTGENERATEPULSE(void) = 50;
        iseuRequestSetSinglePulseMode_out ISEUREQUESTSETSINGLEPULSEMODE(iseuRequestSetSinglePulseMode_in) = 51;
        iseuRequestBypassRegulation_out ISEUREQUESTBYPASSREGULATION(iseuRequestBypassRegulation_in) = 52;
        iseuRequestSetCurrentAtCycloExit_out ISEUREQUESTSETCURRENTATCYCLOEXIT(iseuRequestSetCurrentAtCycloExit_in) = 53;
        motorisedSlitsHome_out MOTORISEDSLITSHOME(motorisedSlitsHome_in) = 54;
        motorisedSlitsGoHome_out MOTORISEDSLITSGOHOME(motorisedSlitsGoHome_in) = 55;
        motorisedSlitsGoToPosition_out MOTORISEDSLITSGOTOPOSITION(motorisedSlitsGoToPosition_in) = 56;
        motorisedSlitsGoToOpening_out MOTORISEDSLITSGOTOOPENING(motorisedSlitsGoToOpening_in) = 57;
        motorisedSlitsStop_out MOTORISEDSLITSSTOP(motorisedSlitsStop_in) = 58;
        beamLineSelectBeamLine_out BEAMLINESELECTBEAMLINE(beamLineSelectBeamLine_in) = 59;
        beamLineUnselectBeamLine_out BEAMLINEUNSELECTBEAMLINE(void) = 60;
        beamLineSetRange_out BEAMLINESETRANGE(beamLineSetRange_in) = 61;
        beamLineSetDegraderToRange_out BEAMLINESETDEGRADERTORANGE(void) = 62;
        beamLineSetLineToRange_out BEAMLINESETLINETORANGE(beamLineSetLineToRange_in) = 63;
        beamLineSelectAndSetToRange_out BEAMLINESELECTANDSETTORANGE(beamLineSelectAndSetToRange_in) = 64;
        beamLineSetDegraderToBeamStop_out BEAMLINESETDEGRADERTOBEAMSTOP(void) = 65;
        beamLineStartTuning_out BEAMLINESTARTTUNING(beamLineStartTuning_in) = 66;
        beamLineStopTuning_out BEAMLINESTOPTUNING(void) = 67;
        beamLineResetTuning_out BEAMLINERESETTUNING(void) = 68;
        beamLineTuneNextBlock_out BEAMLINETUNENEXTBLOCK(void) = 69;
        beamLineRedoTuningBlock_out BEAMLINEREDOTUNINGBLOCK(void) = 70;
        beamLineByPassTuningBlock_out BEAMLINEBYPASSTUNINGBLOCK(void) = 71;
        beamLineManualAcquire_out BEAMLINEMANUALACQUIRE(void) = 72;
        beamLineStopManualAcquire_out BEAMLINESTOPMANUALACQUIRE(void) = 73;
        beamLineSaveReferenceSetPoint_out BEAMLINESAVEREFERENCESETPOINT(beamLineSaveReferenceSetPoint_in) = 74;
        beamLineStartPhaseSpace_out BEAMLINESTARTPHASESPACE(beamLineStartPhaseSpace_in) = 75;
        ecuTcuBeamTuningNotStarted_out ECUTCUBEAMTUNINGNOTSTARTED(void) = 76;
        ecuTcuBeamTuningStarted_out ECUTCUBEAMTUNINGSTARTED(void) = 77;
        ecuTcuCountingNotStarted_out ECUTCUCOUNTINGNOTSTARTED(void) = 78;
        ecuTcuCountingStarted_out ECUTCUCOUNTINGSTARTED(void) = 79;
        ecuTcuOnRequestAcquisition_out ECUTCUONREQUESTACQUISITION(ecuTcuOnRequestAcquisition_in) = 80;
        analogPsSetVoltage_out ANALOGPSSETVOLTAGE(analogPsSetVoltage_in) = 81;
        analogPsSetCurrent_out ANALOGPSSETCURRENT(analogPsSetCurrent_in) = 82;
    } = 1;
} = 55555129;
