/*********************************************************
  Copyright (c) 2001-2008 by Ion Beam Applications S.A.
  All rights reserved

  Rue du cyclotron, 3
  B-1348 Louvain-la-Neuve
  Tel : +32.10.47.58.11

  History:

  1.0  10/03/2008 JFD Creation
  1.1  20/03/2008 JFD Simplify error codes
  1.3  21/03/2008 YCL Supressed  BeamLess Scanning
  1.4  26/03/2008 YCL Removed AbnormalBeamDetection
                      Added safestate and clear
  1.5  26/03/2008 YCL Removed AbnormalBeamDetection
                      Added safestate and clear
  1.6  01/04/2008 JFD Numbering of enum
                      comments clarification
  1.7  16/04/2008 JFD Added layer id in 'prepare' request.
  1.8  21/04/2008 JFD 'ClearSafetyStatus' into 'ResetSafety'.
                      Use of constants
  1.9  29/05/2008 JFD Removed duplicate 'isInFailure' and 'isSafe'.
  1.10 07/10/2008 RFL The changes are:
                      - Remove the INITIALIZING and ERROR state
                      - Split in two parts: common and pyramid specificities
                      - Add a new prepare call with layer data instead of file.
                      - Add two additional status: Operational and AbnormalBeamDetected.
  1.11 7/11/2008 YCL ? Added Diagnostic mode & Call for integration period in service mode
  1.12 20/11/2008 RFL Merge trajectory and scanning controller RPC interface. 
  1.13 21/11/2008 CJP(PTC) Changes are:
                  - Added SC_CTRL_RPC_PTC_STATE enum to reflect internal PTC SC state.
                  - Added SC_CTRL_RPC_PTC_STATE to ScanningControllerRpcGetFullA500State_out.
                  - Removed Prepare2, Prepare3 functions.
                  - Removed ScanningControllerRpcPrepareLayer_in2 struct
  1.14 24/11/2008 CJP(PTC) Added back scanningControllerGetLastSpotPosition function.
  1.15 20/02/2009 RFL Add again the "Initializing" state between "Finalizing" and "Idle" state. 
  1.16 09/03/2009 RFL The changes are:
                  - PR 19019: Information useful for beam tuning checks
                  - PR 20130: Error reporting enhancement
  1.17 12/03/2009 CJP
                  -Modified A500ControllerErrorType, A500ControllerThresholdType to reflect latest A500 enumerations.
                  -Removed 20000 element limit ScanningControllerPbsLayer                   
                  -Modified padDoseQuadrant to contain 4 elements.
  1.18 16/03/2009 RFL Add A500 prefix for the two fileds of A500ControllerErrorReport union (error correction). 
  1.19 11/05/2009 RFL Add services to control the offsets
  1.20 12/05/2009 CJP Added function to write calibration file
  1.21 13/05/2009 CBA Add getVersion function
  1.22 18/06/2009 RFL The changes are:
                  - modify the getVersion function to return a string instead of int
                  - Add functions to control the multi-plexing feature
  1.23 30/09/2009 CJP added new multiplexer, hvps short A500 error codes, and new A500 state for switching rooms
  1.24 23/04/2010 RFL Add the K factor for DCEU
  1.25 13/05/2010 CJP Add scanningControllerShutdown() command
  1.26 18/04/2011 KVE Added IC1 position and width thresholds to the element definitions.
  1.27 22/08/2011 KVE Added elementId/xValid/yValid parameters to the data returned by the getLastSpotData calls.
  1.28 29/08/2012 CJP
                  - Removed NECU pad fields from ScanningControllerBeamDataAtNozzleEntrance struct  
              - Added layerId field to "last spot" return structs
              - Added IC1 charge thresholds to ScanningControllerPbsLayerElement
              - Added fast quadrupole control and monitoring fields to ScanningControllerPbsLayerElement
  1.29 04/09/2012 CJP
             - Changed NECU strip arrays to size 32 from size 12
  1.30 10/09/2012 WPN/CJP
             - Re-ordered fields in ScanningControllerPbsLayerElement_SC to better group categories
             - Added ScanningControllerRpcStatusMD5 to enhance data integrity on map transfer functions
  1.31 05/10/2012 CJP
             - Updated A500ControllerErrorType and A500ControllerThresholdType structs 
  1.32 11/10/2012 CJP
             - Updated A500ControllerErrorType struct 
  1.33 06/09/2013 CJP
             - Added scanningControllerResetFilterAccumulators function
             - Added narrowBeam field to ScanningControllerPbsLayer struct
             - Added ScanningControllerBeamDataMulti_SC, ScanningControllerBeamDataAtNozzleEntranceMulti_SC, 
               and ScanningControllerBeamPositionMulti structs
             - Changed scanningControllerGetLastElementSpotData, scanningControllerGetLastSpotDataAtNozzleEntrance, 
               and scanningControllerGetLastSpotPosition return types to above structs
  1.34 26/09/2013 CJP
             - Removed all _SC suffixes to normalize names across RPC interfaces 
              - Changed RPC_PROGRAM, RPC_VERSION to be unique to this interface
             - Updated A500ControllerErrorType enum
  1.35 27/11/2013 KVE
             - Added different beamwidth algorithm selection parameters

**********************************************************/

/* -------------------------------------------------------- */
/*        C O M M O N    P A R T                            */
/* -------------------------------------------------------- */

/*********************************************************
  Version: "1.34"
 **********************************************************/

/**
 * Unique id of a layer
 */
typedef unsigned int ScanningControllerLayerId;

/**
 * The functional state of the ScanningController.
 * See the ICD (MiD 16174) for the complete finite state machine specification.
 */
enum SC_CTRL_RPC_STATE {
  SC_CTRL_RPC_IDLE = 0,
  SC_CTRL_RPC_PREPARING_LAYER = 1,
  SC_CTRL_RPC_LAYER_PREPARED = 2,
  SC_CTRL_RPC_PERFORMING_LAYER = 3,
  SC_CTRL_RPC_FINALIZING_LAYER = 4,
  SC_CTRL_RPC_INITIALIZING = 5 
};

/**
 * The operational state of the ScanningController.
 */
enum SC_CTRL_RPC_OPERATIONAL_STATE {
  SC_CTRL_RPC_OPERATIONAL = 0,
  SC_CTRL_RPC_IN_FAILURE = 1
};

/**
 * The abnormal beam detected state of the ScanningController.
 */
enum SC_CTRL_RPC_ABNORMAL_BEAM_DETECTED_STATE {
  SC_CTRL_RPC_NO_ABNORMAL_BEAM_DETECTED = 0,
  SC_CTRL_RPC_ABNORMAL_BEAM_DETECTED = 1
};

/* ---- RPC 'Artefacts' ---- */

const SC_CTRL_MAX_CALLER_LENGTH = 100;

const SC_CTRL_MAX_LAYER_FILENAME_LENGTH = 1000;

struct ScanningControllerRpcDefault_in {
  char caller<SC_CTRL_MAX_CALLER_LENGTH>; /* 'Who' is calling */
};

const SC_CTRL_MAX_RPC_ERROR_MSG_LENGTH = 1000;

/**
 * Unique id of a layer
 */
typedef unsigned int PbsLayerId;

/**
 * Element type.
 */
enum PBS_ELEMENT_TYPE {
  PBS_ELEMENT_SLEW_SEGMENT = 0,
  PBS_ELEMENT_DOSE_POINT   = 1,
  PBS_ELEMENT_DOSE_SEGMENT = 2
};

/**
 * Element of a layer.
 */
struct ScanningControllerPbsLayerElement {
  PBS_ELEMENT_TYPE type;  /* Type of element */
  
  /* ---- Setpoints ---- */
  float targetCharge;         /* Desired charge on this element in C */
  float beamCurrentSetpoint; /* Desired 4-20 mA setpoint for beam current in A */
  float xCurrentSetpoint;     /* -10/+10V current setpoint on X */
  float yCurrentSetpoint;     /* -10/+10V current setpoint on Y */
  float xQuadCurrentSetpoint; /* -10/+10V current setpoint on X Quadrupole */
  float yQuadCurrentSetpoint; /* -10/+10V current setpoint on Y Quadrupole */
  
  /* ---- Safety Thresholds ---- */
  float maxDuration;  /* Maximum duration of element in ms */

  float minPrimaryCharge;   /* Minimum Charge expected on IC2 in C */
  float maxPrimaryCharge;   /* Maximum Charge expected on IC2 in C */
  float minSecondaryCharge; /* Minimum Charge expected on IC3 in C */
  float maxSecondaryCharge; /* Maximum Charge expected on IC3 in C */
  float minTernaryCharge;   /* Minimum Charge expected on IC3 quality channels in C */
  float maxTernaryCharge;   /* Maximum Charge expected on IC3 quality channels in C */
  float minNozzleEntranceCharge;  /* Minimum Charge expected on IC1 quality channels in C */
  float maxNozzleEntranceCharge;  /* Maximum Charge expected on IC1 quality channels in C */
  
  float minPrimaryDoseRate;    /* Minimum dose rate on IC2 in A */
  float maxPrimaryDoseRate;    /* Maximum dose rate on IC2 in A */
  float minSecondaryDoseRate;  /* Minimum dose rate on IC3 in A */
  float maxSecondaryDoseRate;  /* Maximum dose rate on IC3 in A */
  float minTernaryDoseRate;    /* Minimum dose rate on IC3 quality channels in A */
  float maxTernaryDoseRate;    /* Maximum dose rate on IC3 quality channels in A */
  float minNozzleEntranceDoseRate; /* Minimum dose rate on IC1 quality channels in A */
  float maxNozzleEntranceDoseRate; /* Maximum dose rate on IC1 quality channels in A */

  float minCycloBeam; /* -10/+10V minimum ISEU - Cyclotron feedback */
  float maxCycloBeam; /* -10/+10V Maximum ISEU - Cyclotron feedback */
  
  float xMinPrimaryCurrentFeedback; /* -10/+10V minimum primary feedback current on X */
  float xMaxPrimaryCurrentFeedback; /* -10/+10V maximum primary feedback current on X */
  float yMinPrimaryCurrentFeedback; /* -10/+10V minimum primary feedback current on Y */
  float yMaxPrimaryCurrentFeedback; /* -10/+10V maximum primary feedback current on Y */
  float xMinPrimaryVoltageFeedback; /* -10/+10V minimum primary feedback voltage on X */
  float xMaxPrimaryVoltageFeedback; /* -10/+10V maximum primary feedback voltage on X */
  float yMinPrimaryVoltageFeedback; /* -10/+10V minimum primary feedback voltage on Y */
  float yMaxPrimaryVoltageFeedback; /* -10/+10V maximum primary feedback voltage on Y */

  float xMinSecondaryCurrentFeedback; /* -10/+10V minimum secondary feedback current on X */
  float xMaxSecondaryCurrentFeedback; /* -10/+10V maximum secondary feedback current on X */
  float yMinSecondaryCurrentFeedback; /* -10/+10V minimum secondary feedback current on Y */
  float yMaxSecondaryCurrentFeedback; /* -10/+10V maximum secondary feedback current on Y */
  float xMinSecondaryVoltageFeedback; /* -10/+10V minimum secondary feedback voltage on X */
  float xMaxSecondaryVoltageFeedback; /* -10/+10V maximum secondary feedback voltage on X */
  float yMinSecondaryVoltageFeedback; /* -10/+10V minimum secondary feedback voltage on Y */
  float yMaxSecondaryVoltageFeedback; /* -10/+10V maximum secondary feedback voltage on Y */
  
  float xQuadMinPrimaryCurrentFeedback; /* -10/+10V minimum primary feedback current on X */
  float xQuadMaxPrimaryCurrentFeedback; /* -10/+10V maximum primary feedback current on X */
  float yQuadMinPrimaryCurrentFeedback; /* -10/+10V maximum primary feedback current on Y */
  float yQuadMaxPrimaryCurrentFeedback; /* -10/+10V maximum primary feedback current on Y */
  float xQuadMinPrimaryVoltageFeedback; /* -10/+10V minimum primary feedback voltage on X */
  float xQuadMaxPrimaryVoltageFeedback; /* -10/+10V maximum primary feedback voltage on X */
  float yQuadMinPrimaryVoltageFeedback; /* -10/+10V minimum primary feedback voltage on Y */
  float yQuadMaxPrimaryVoltageFeedback; /* -10/+10V maximum primary feedback voltage on Y */

  float xQuadMinSecondaryCurrentFeedback; /* -10/+10V minimum secondary feedback current on X */
  float xQuadMaxSecondaryCurrentFeedback; /* -10/+10V maximum secondary feedback current on X */
  float yQuadMinSecondaryCurrentFeedback; /* -10/+10V minimum secondary feedback current on Y */
  float yQuadMaxSecondaryCurrentFeedback; /* -10/+10V maximum secondary feedback current on Y */
  float xQuadMinSecondaryVoltageFeedback; /* -10/+10V minimum secondary feedback voltage on X */
  float xQuadMaxSecondaryVoltageFeedback; /* -10/+10V maximum secondary feedback voltage on X */
  float yQuadMinSecondaryVoltageFeedback; /* -10/+10V minimum secondary feedback voltage on Y */
  float yQuadMaxSecondaryVoltageFeedback; /* -10/+10V maximum secondary feedback voltage on Y */  

  float xMinField; /* -10/+10V minimum field feedback in X */
  float xMaxField; /* -10/+10V maximum field feedback in X */
  float yMinField; /* -10/+10V minimum field feedback in Y */
  float yMaxField; /* -10/+10V maximum field feedback in Y */
  
  float xMinBeamWidth; /* Minimum beam size on X in mm (0-200) */
  float xMaxBeamWidth; /* Maximum beam size on X in mm (0-200) */
  float yMinBeamWidth; /* Minimum beam size on Y in mm (0-200) */
  float yMaxBeamWidth; /* Maximum beam size on Y in mm (0-200) */

  float xPositionLow;  /* Lowest acceptable position on X axis in mm (-200/+200) */
  float xPositionHigh; /* Highest acceptable position on X axis in mm (-200/+200) */
  float yPositionLow;  /* Lowest acceptable position on Y axis in mm (-150/+150) */
  float yPositionHigh; /* Highest acceptable position on Y axis in mm (-150/+150) */

  float xMinNozzleEntranceWidthThreshold; /* Minimum threshold for the IC1 width on the X axis in mm. */
  float xMaxNozzleEntranceWidthThreshold; /* Maximum threshold for the IC1 width on the X axis in mm. */
  float yMinNozzleEntranceWidthThreshold; /* Minimum threshold for the IC1 width on the Y axis in mm. */
  float yMaxNozzleEntranceWidthThreshold; /* Maximum threshold for the IC1 width on the Y axis in mm. */

  float xMinNozzleEntrancePositionThreshold; /* Minimum threshold for the IC1 position on the X axis in mm. */
  float xMaxNozzleEntrancePositionThreshold; /* Maximum threshold for the IC1 position on the X axis in mm. */
  float yMinNozzleEntrancePositionThreshold; /* Minimum threshold for the IC1 position on the Y axis in mm. */
  float yMaxNozzleEntrancePositionThreshold; /* Maximum threshold for the IC1 position on the Y axis in mm. */
};

/**
 * Specification of a complete layer.
 */
struct ScanningControllerPbsLayer {
  PbsLayerId id;      /* Unique id of the layer */
  double range;       /* Range (in g/cm2) of the layer */
  double totalCharge; /* Total charge (in C) to be deposited on this layer */
  int diagnosticMode; /* 1 -> no check performed on trajectory & 0 -> normal trajectory */
  /* 1 -> 3 point algorithm selection */
  int narrowBeamEntranceX; /* 1=use narrow beam for NECU-X */
  int narrowBeamEntranceY; /* 1=use narrow beam for NECU-Y */
  int narrowBeamExitX; /* 1=use narrow beam for FCU-X */
  int narrowBeamExitY; /* 1=use narrow beam for FCU-Y */
  double metersetCorrectionFactor;
  ScanningControllerPbsLayerElement elements<>;
};

struct ScanningControllerRpcPrepareLayer_in {
  char caller<SC_CTRL_MAX_CALLER_LENGTH>; /* 'Who' is calling */
  ScanningControllerPbsLayer layer;
};

struct ScanningControllerRpcPrepareLayerWithFile_in {
  char caller<SC_CTRL_MAX_CALLER_LENGTH>; /* 'Who' is calling */
  /**
   * The name of the file containing the layer description.
   */
  char filename<SC_CTRL_MAX_LAYER_FILENAME_LENGTH>;
};

/**
 * Status returned by all RPC calls.
 */
struct ScanningControllerRpcStatus {
  char success; /* 'Y': success, 'N': failure */
  char message<SC_CTRL_MAX_RPC_ERROR_MSG_LENGTH>; /* Details the problem in case of error */
};

/**
 * Enhanced MD5 status returned by all RPC calls.
 */
struct ScanningControllerRpcStatusMD5 {
  ScanningControllerRpcStatus rpcStatus;
  unsigned int md5[4];
};

struct ScanningControllerRpcGetState_out {
  ScanningControllerRpcStatus rpcStatus;
  int beamlineSelected;
  SC_CTRL_RPC_STATE functionalState;
  SC_CTRL_RPC_OPERATIONAL_STATE operationalState;
  SC_CTRL_RPC_ABNORMAL_BEAM_DETECTED_STATE abnormalBeamDetectedState;
};

struct ScanningControllerBeamPosition {
   unsigned long layerId; 
   float x;
   float y;
};

struct ScanningControllerBeamPositionMulti {
   ScanningControllerBeamPosition data[2];
};

const SC_CTRL_MAX_FAILURE_CAUSE_LENGTH = 1000;

/**
 * Primary errors returned by the A500 controller software
 */
enum A500ControllerErrorType
{
  SC_CTRL_RPC_A500_ERROR_TYPE_NONE = 0,      /* No error */
  SC_CTRL_RPC_A500_BEAM_ON_IN_ERROR_THRESHOLD = 1,   /* Beam is on according to threshold */
  SC_CTRL_RPC_A500_BEAM_ON_IN_ERROR_OVERRANGE = 2,   /* Beam is on according to overrange error */
  SC_CTRL_RPC_A500_BEAM_ON_IN_ERROR_F100_GATE = 3,   /* F100 beam enable gate stuck on */
  SC_CTRL_RPC_A500_HARDWARE = 4,       /* Hardware failure or message error detected */
  SC_CTRL_RPC_A500_MANUAL_ABORT = 5,      /* Manual abort requested by client */
  SC_CTRL_RPC_A500_MAP_CHECKSUM_ERROR = 6,     /* Error in map checksum */
  SC_CTRL_RPC_A500_MAP_ILLEGAL_SUBMAP_TYPE = 7,    /* Downloaded map has unexpected element type */
  SC_CTRL_RPC_A500_MAP_MEMORY_ERROR = 8,     /* A memory error occurred while downloading map */
  SC_CTRL_RPC_A500_MAP_SUBMAP_DISAGREEMENT = 9,    /* Downloaded map has unexpected element count */
  SC_CTRL_RPC_A500_SYNCHRONIZATION_I3200 = 10,    /* Incorrect time-slice received from SGCU */
  SC_CTRL_RPC_A500_SYNCHRONIZATION_SGCU_TIMEOUT = 11,  /* Time-out synchronizing with SGCU */
  SC_CTRL_RPC_A500_SYNCHRONIZATION_SGCU_WRONG_SLICE = 12, /* Incorrect time-slice received from SGCU */
  SC_CTRL_RPC_A500_SYNCHRONIZATION_TIME_SLICE = 13,   /* Time-out synchronizing with time slice */
  SC_CTRL_RPC_A500_THRESHOLD_COMPARISON = 14,    /* Threshold error due comparison violation */  
  SC_CTRL_RPC_A500_THRESHOLD_OVER_RANGE = 15,    /* Threshold error due to over range */
  SC_CTRL_RPC_A500_TIMEOUT_ENABLING_BEAM = 16,    /* Timeout trying to enable beam */
  SC_CTRL_RPC_A500_TIMEOUT_READY_STATE = 17,    /* Timeout while waiting in ready state */
  SC_CTRL_RPC_A500_TSS_ENABLE = 18,       /* TSS enable and disable disagree */
  SC_CTRL_RPC_A500_WATCH_DOG = 19,       /* A500 interlock board failure */
  SC_CTRL_RPC_A500_MULTIPLEXER = 20,      /* A500 multiplexer failure */
  SC_CTRL_RPC_A500_HV_SHORT_DETECTED = 21,     /* HVPS short detected */
  SC_CTRL_RPC_A500_BEAM_ON_IN_ERROR_INTEGRATED = 22,  /* Beam integral is on in error */
  SC_CTRL_RPC_A500_EWMA_MEAN = 23,      /* Error due to EWMA mean check */
  SC_CTRL_RPC_A500_EWMA_VARIANCE = 24,      /* Error due to EWMA variance check */
  SC_CTRL_RPC_A500_MD5_MISMATCH = 25,      /* MD5 has mismatch */
  SC_CTRL_RPC_A500_EWMA_MEAN_MONITORING = 26, /* Error due to EWMA mean monitoring check */
  SC_CTRL_RPC_A500_FILTERING_ABSOLUTE = 27,   /* Absolute filtering error */
  SC_CTRL_RPC_A500_FILTERING_ACCUMULATOR = 28,   /* Accumulator filtering error */  
  SC_CTRL_RPC_A500_UNKNOWN = 29      /* Unknown error */
};

/**
 * Thresholds monitor by A500 controllers. 
 */
enum A500ControllerThresholdType {
  SC_CTRL_RPC_THRESHOLD_NONE = 0,                                      /* Threshold not defined */  
  SC_CTRL_RPC_X_CURRENT_PRIM_FB = 1,                                   /* Primary X SMPS current */
  SC_CTRL_RPC_X_VOLT_PRIM_FB = 2,                                      /* Primary X SMPS voltage */
  SC_CTRL_RPC_X_CURRENT_SEC_FB = 3,                                    /* Secondary X SMPS current */
  SC_CTRL_RPC_X_VOLT_SEC_FB = 4,                                       /* Secondary X SMPS voltage */
  SC_CTRL_RPC_Y_CURRENT_PRIM_FB = 5,                                   /* Primary Y SMPS current */
  SC_CTRL_RPC_Y_VOLT_PRIM_FB = 6,                                      /* Primary Y SMPS voltage */
  SC_CTRL_RPC_Y_CURRENT_SEC_FB = 7,                                    /* Secondary Y SMPS current */
  SC_CTRL_RPC_Y_VOLT_SEC_FB = 8,                                       /* Secondary Y SMPS voltage */
  SC_CTRL_RPC_X_QUAD_CURRENT_PRIM_FB = 9,                         /* Primary X Quad current */
  SC_CTRL_RPC_X_QUAD_VOLT_PRIM_FB = 10,                           /* Primary X Quad voltage */
  SC_CTRL_RPC_X_QUAD_CURRENT_SEC_FB = 11,                         /* Secondary X Quad current */
  SC_CTRL_RPC_X_QUAD_VOLT_SEC_FB = 12,                                 /* Secondary X Quad voltage */
  SC_CTRL_RPC_Y_QUAD_CURRENT_PRIM_FB = 13,                             /* Primary Y Quad current */
  SC_CTRL_RPC_Y_QUAD_VOLT_PRIM_FB = 14,                                /* Primary Y Quad voltage */
  SC_CTRL_RPC_Y_QUAD_CURRENT_SEC_FB = 15,                              /* Secondary Y Quad current */
  SC_CTRL_RPC_Y_QUAD_VOLT_SEC_FB = 16,                                 /* Secondary Y Quad voltage */  
  SC_CTRL_RPC_X_FIELD = 17,                                             /* X field */
  SC_CTRL_RPC_Y_FIELD = 18,                                            /* Y field */
  SC_CTRL_RPC_BEAM_CURRENT_FB = 19,                                    /* ISEU beam current */
  SC_CTRL_RPC_MAX_CHARGE_PRIM = 20,                                    /* Primary charge maximum */
  SC_CTRL_RPC_MIN_CHARGE_PRIM = 21,                                    /* Primary charge minimum */
  SC_CTRL_RPC_DOSE_RATE_PRIM = 22,                                     /* Primary dose rate */
  SC_CTRL_RPC_MAX_CHARGE_SEC = 23,                                     /* Secondary charge maximum */
  SC_CTRL_RPC_MIN_CHARGE_SEC = 24,                                     /* Secondary charge minimum */
  SC_CTRL_RPC_DOSE_RATE_SEC = 25,                                      /* Secondary dose rate */
  SC_CTRL_RPC_MAX_CHARGE_TER = 26,                                     /* Tertiary charge maximum */
  SC_CTRL_RPC_MIN_CHARGE_TER = 27,                                     /* Tertiary charge minimum */
  SC_CTRL_RPC_DOSE_RATE_TER = 28,                                      /* Tertiary dose rate */
  SC_CTRL_RPC_MAX_CHARGE_IC1 = 29,                              /* IC1 charge maximum */
  SC_CTRL_RPC_MIN_CHARGE_IC1 = 30,                                     /* IC1 charge minimum */
  SC_CTRL_RPC_DOSE_RATE_IC1 = 31,                                      /* IC1 dose rate */ 
  SC_CTRL_RPC_X_POS = 32,                                              /* X position */
  SC_CTRL_RPC_X_WIDTH = 33,                                            /* X width */
  SC_CTRL_RPC_Y_POS = 34,                                              /* X position */
  SC_CTRL_RPC_Y_WIDTH = 35,                                            /* Y width */
  SC_CTRL_RPC_THRESHOLD_COUNT = 36                                     /* Count of thresholds */
};

/**
 * Contains information regarding each A500 controller error that can occur while scanning.
 */
struct A500ControllerScanErrorReport {
  unsigned int timeSlice;                                              /* Time slice when the error occurred */
  unsigned int mapIndex;                                               /* Map element when the error occurred */
  A500ControllerErrorType error;                                       /* Type of error */
  A500ControllerThresholdType thresholdType;                           /* Applicable threshold type */
  float data;                                                          /* Violating parameter [unit: depend on the thresholdType] */
  float thresholdLow;                                                  /* Applicable low threshold [unit: depend on the thresholdType] */
  float thresholdHigh;                                                 /* Applicable high threshold [unit: depend on the thresholdType] */
};

/**
 * Contains information regarding each A500 controller error that can occur outside of a scan.
 */
struct A500ControllerNonScanErrorReport {
  A500ControllerErrorType error;                                       /* Type of error */
  float data;                                                          /* Violating parameter [unit: depend on the thresholdType] */
};

/**
 * The different kind of record.
 */
enum A500ControllerScanState {
    SC_CTRL_RPC_SCAN_STATE = 0,
    SC_CTRL_RPC_NON_SCAN_STATE = 1
};

union A500ControllerErrorReport switch (A500ControllerScanState scanState) {
  case SC_CTRL_RPC_SCAN_STATE:
    A500ControllerScanErrorReport scanErrorReport;        /* Errors during scan state */
  case SC_CTRL_RPC_NON_SCAN_STATE:
    A500ControllerNonScanErrorReport nonScanErrorReport;  /* Errors during non scan state */
  default:
    void;
};

/**
 * Primary errors returned by the Gateway Server software 
 */ 
enum ScanningControllerErrorType {
  SC_CTRL_RPC_ERROR_TYPE_NONE = 0,                      /* No error */
  SC_CTRL_RPC_A500_COMMUNICATION_TIMEOUT = 1,           /* A500 communications errors */
  SC_CTRL_RPC_A500_NON_SUCCESS_ERROR_RETURN_CODE = 2,   /* A500 non success error code */
  SC_CTRL_RPC_A500_UNEXPECTED_STATE = 3,                /* A500 unexpected state encountered */
  SC_CTRL_RPC_MAP_CAN_NOT_OPEN_FILE = 4,                /* Cannot open map file */
  SC_CTRL_RPC_MAP_INVALID_FILE_FORMAT = 5,              /* Invalid map file format */
  SC_CTRL_RPC_MAP_HEADER_INCOMPLETE = 6,                /* Incomplete map header */
  SC_CTRL_RPC_MAP_SUBMAP_DISAGREEMENT = 7,              /* Downloaded map has unexpected element count */
  SC_CTRL_RPC_PBS_COMMUNICATION_TIMEOUT = 8,            /* PBS communications error */
  SC_CTRL_RPC_PBS_NON_SUCCESS_ERROR_RETURN_CODE =9      /* PBS non success error code */
};

enum A500ControllerType {
  SC_CTRL_RPC_SGCU = 0,
  SC_CTRL_RPC_RCU  = 1,
  SC_CTRL_RPC_FCU  = 2,
  SC_CTRL_RPC_NECU = 3
};

/**
 * Contains information regarding each error.
 */
struct ScanningControllerErrorReport
{
   ScanningControllerErrorType error;        /* Type of error */
   A500ControllerType controller;            /* Offending controller, if applicable */
   int returnCode;                           /* Return code of error, if applicable */
};

struct ScanningControllerRpcGetFailureCause_out {
  ScanningControllerRpcStatus rpcStatus;
  char failureCause<SC_CTRL_MAX_FAILURE_CAUSE_LENGTH>; /* Only filled if in failure */
  ScanningControllerErrorReport gatewayErrorReport;
  A500ControllerErrorReport sgcuErrorReport; 
  A500ControllerErrorReport rcuErrorReport;
  A500ControllerErrorReport fcuErrorReport;
  A500ControllerErrorReport necuErrorReport;
};

/* Characteristics of the beam at nozzle exit */
struct ScanningControllerBeamData {
   unsigned long layerId; /* unit: -    */
   int elementId;         /* unit: - */
   float xDose;           /* unit: [C]  */
   float yDose;           /* unit: [C]  */
   float xPosition;       /* unit: [mm] */
   float yPosition;       /* unit: [mm] */
   float xWidth;          /* unit: [mm] */
   float yWidth;          /* unit: [mm] */
   float xChannels[32];   /* unit: [C]  */
   float yChannels[32];   /* unit: [C]  */
   int xValid;            /* xPosition and xWidth valid: 0=false, 1=true */
   int yValid;            /* yPosition and yWidth valid: 0=false, 1=true */
};

/* Characteristics of the beam at nozzle exit (list) */
struct ScanningControllerBeamDataMulti {
   ScanningControllerBeamData data[2]; /* first element is oldest spot data */
};

/* Characteristics of the beam at nozzle entrance (when available) */
struct ScanningControllerBeamDataAtNozzleEntrance {
   unsigned long layerId;     /* unit: -    */
   int elementId;             /* unit: - */
   float xDose;               /* unit: [C]  */
   float yDose;               /* unit: [C]  */
   float xPosition;           /* unit: [mm] */
   float yPosition;           /* unit: [mm] */
   float xWidth;              /* unit: [mm] */
   float yWidth;              /* unit: [mm] */
   float xChannels[32];       /* unit: [C]  */
   float yChannels[32];       /* unit: [C]  */
   int xValid;                /* xPosition and xWidth valid: 0=false, 1=true */
   int yValid;                /* yPosition and yWidth valid: 0=false, 1=true */
};

/* Characteristics of the beam at nozzle entrance (when available) (list) */
struct ScanningControllerBeamDataAtNozzleEntranceMulti {
   ScanningControllerBeamDataAtNozzleEntrance data[2]; /* first element is oldest spot data */
};

/**
 * Action ids for the write/clear offsets service.
 */
enum SC_CTRL_RPC_WRITE_OFFSET_ACTIONS {
  SC_CTRL_RPC_WRITE_FCU_OFFSETS = 0,
  SC_CTRL_RPC_WRITE_NECU_OFFSETS = 1,
  SC_CTRL_RPC_WRITE_SGCU_RCU_OFFSETS = 2,
  SC_CTRL_RPC_WRITE_ALL_OFFSETS = 3,
  SC_CTRL_RPC_CLEAR_FCU_OFFSETS = 4,
  SC_CTRL_RPC_CLEAR_NECU_OFFSETS = 5,
  SC_CTRL_RPC_CLEAR_SGCU_RCU_OFFSETS = 6,
  SC_CTRL_RPC_CLEAR_ALL_OFFSETS = 7
};

/**
 * Offsets of the FCU sub-system.
 */
struct ScanningControllerFcuOffsets {
  float xChannels[32];       /* unit: [C]  */
  float yChannels[32];       /* unit: [C]  */
};

struct ScanningControllerRpcFcuOffsets_out {
  ScanningControllerRpcStatus rpcStatus;
  ScanningControllerFcuOffsets fcuOffsets;
};

/**
 * Offsets of the NECU sub-system.
 */
struct ScanningControllerNecuOffsets {
  float xChannels[32];       /* unit: [C]  */
  float yChannels[32];       /* unit: [C]  */
};

struct ScanningControllerRpcNecuOffsets_out {
  ScanningControllerRpcStatus rpcStatus;
  ScanningControllerNecuOffsets necuOffsets;
};

/**
 * Offsets of the SGCU / RCU sub-systems.
 */
struct ScanningControllerSgcuRcuOffsets {
  float sgcuOffset;          /* unit: [C]  */
  float rcuOffset;           /* unit: [C]  */
};

struct ScanningControllerRpcSgcuRcuOffsets_out {
  ScanningControllerRpcStatus rpcStatus;
  ScanningControllerSgcuRcuOffsets sgcuRcuOffsets;
};

/* -------------------------------------------------------- */
/*   P Y R A M I D      S P E C I F I C I T I E S           */
/* -------------------------------------------------------- */

/**
 * The internal PTC states of the ScanningController.
 */
enum SC_CTRL_RPC_PTC_STATE {
    SC_CTRL_RPC_PTC_INITIALIZING = 0,
    SC_CTRL_RPC_PTC_IDLE = 1,
    SC_CTRL_RPC_PTC_PREPARING_LAYER = 2,
    SC_CTRL_RPC_PTC_LAYER_PREPARED = 3,
    SC_CTRL_RPC_PTC_PERFORMING_LAYER = 4,
    SC_CTRL_RPC_PTC_FINALIZING_LAYER = 5,
    SC_CTRL_RPC_PTC_ERROR = 6
};

/**
* The state of the individual A500
*/
enum SC_CTRL_RPC_A500_STATE {
  Initializing = 0,
  Idle = 1,
  LoadingTrajectory = 2,
  trajectoryAvailable = 3,
  EnablingBeam = 4,
  Ready = 5,
  Scanning = 6,
  Stopping = 7,
  ScanDone = 8,
  RawUpdate = 9,
  Error = 10,
  RoomSwitch = 11,
  NotActive = 253,
  Spawning = 254,
  CommsError = 255
};

struct ScanningControllerRpcGetFullA500State_out {
  ScanningControllerRpcStatus rpcStatus;
  SC_CTRL_RPC_PTC_STATE state;
  SC_CTRL_RPC_A500_STATE sgcuState;
  SC_CTRL_RPC_A500_STATE rcuState;
  SC_CTRL_RPC_A500_STATE fcuState;
  SC_CTRL_RPC_A500_STATE necuState;
};

/* ---- RPC Interface ---- */

program RPC_PROGRAM_SC {

  version RPC_VERSION_SC {

    /* -------------------------------------------------------- */
    /*          C O M M O N     P A R T                         */
    /* -------------------------------------------------------- */

    /**
     * Requests the ScanningController to prepare the equipments based on the layer parameters.
     *
     * Only accepted in IDLE state.
     *
     * @param layer data.
     */
    ScanningControllerRpcStatusMD5 scanningControllerRpcPrepare(ScanningControllerRpcPrepareLayer_in ) = 1;

    /**
     * Requests the ScanningController to read a file containing a layer
     * specification (XDR version), and prepare for performing it.
     *
     * Only accepted in IDLE state.
     *
     * @param filename name of the file
     */
    ScanningControllerRpcStatusMD5 scanningControllerRpcPrepareWithFile(ScanningControllerRpcPrepareLayerWithFile_in ) = 2;

    /**
     * Requests the ScanningController to start scanning.
     *
     * Only accepted in LAYER_PREPARED state. 
     */
    ScanningControllerRpcStatus scanningControllerRpcStart(ScanningControllerRpcDefault_in ) = 3;

    /**
     * Requests the ScanningController to cancel current layer.
     *
     * Only accepted in LAYER_PREPARED and PERFORMING_LAYER state. 
     */
    ScanningControllerRpcStatus scanningControllerRpcCancel(ScanningControllerRpcDefault_in ) = 4;

    /**
     * Returns the current state and safety status of the ScanningController.
     *
     * @return current functional state, operational state and abnormalBeamDetected state.
     */
    ScanningControllerRpcGetState_out scanningControllerRpcGetState(void) = 5;

    /**
     * If any failure occurred, returns the cause.
     *
     * @return failure cause. 
     */
    ScanningControllerRpcGetFailureCause_out scanningControllerRpcGetFailureCause(void) = 6;

    /**
     * Resets the operational state. The operational status is cleared (i.e. force it to 'Operational'), and
     * checks are reset.
     *
     * Only accepted in IDLE state. 
     */
    ScanningControllerRpcStatus scanningControllerClearOperationalState(ScanningControllerRpcDefault_in ) = 7;

    /**
     * Resets the abnormal beam detected state.
     *
     * Only accepted in IDLE state. 
     */
    ScanningControllerRpcStatus scanningControllerClearAbnormalBeamDetectedState(ScanningControllerRpcDefault_in ) = 8;

    /**
     * Set the integration period when not scanning (in seconds between 0.1 and 1 second)
     */
    ScanningControllerRpcStatus scanningControllerSetIntegrationPeriod(float ) = 9;

    /**
     * Get the average positions of the 2 last spot submaps.
     *  
     */
    ScanningControllerBeamPositionMulti scanningControllerGetLastSpotPosition(void) = 10;

    /**
     * Get data from the last spot.
     * Precondition: Scanning Controller functional state == SC_CTRL_RPC_IDLE
     */
    ScanningControllerBeamDataMulti scanningControllerGetLastElementSpotData(void) = 11;

    /**
     * Get data from the last spot at nozzle entrance.
     * Precondition: Scanning Controller functional state == SC_CTRL_RPC_IDLE
     */
    ScanningControllerBeamDataAtNozzleEntranceMulti scanningControllerGetLastSpotDataAtNozzleEntrance(void) = 12;

    /**
     * Request to write or clear offsets of a sub-system or the complete system.
     */
    ScanningControllerRpcStatus scanningControllerWriteOffsets(SC_CTRL_RPC_WRITE_OFFSET_ACTIONS ) = 13;

    /**
     * Read the offsets of the FCU sub-system.
     */
    ScanningControllerRpcFcuOffsets_out scanningControllerReadFcuOffsets(void) = 14;

    /**
     * Read the offsets of the NECU sub-system.
     */
    ScanningControllerRpcNecuOffsets_out scanningControllerReadNecuOffsets(void) = 15;

    /**
     * Read the offsets of the SGCU / RCU sub-system.
     */
    ScanningControllerRpcSgcuRcuOffsets_out scanningControllerReadSgcuRcuOffsets(void) = 16;
    
    /**
     * Request to write device calibration file
     */
    ScanningControllerRpcStatus scanningControllerWriteCalibrationFile(void) = 17;

    /**
     * Request to select a beam line (scanning controller multiplexor)
     * Remark: we can retrieve the beamline selected via scanningControllerRpcGetState method.
     * Precondition: Scanning Controller functional state == SC_CTRL_RPC_IDLE
     */
    ScanningControllerRpcStatus scanningControllerSelectBeamline(int) = 18;
    
    /**
     * Shut down SC process
     */
    ScanningControllerRpcStatus scanningControllerShutdown(void) = 19;    
   
    /**
     * Reset dose filter accumulators
     */
    ScanningControllerRpcStatus scanningControllerResetFilterAccumulators(void) = 20;     

    /**
     * Return the current scanning controller interface version.
     */
    string getVersion(void) = 50;

    /* -------------------------------------------------------- */
    /*  P Y R A M I D      S P E C I F I C I T I E S            */
    /* -------------------------------------------------------- */

    /* Start the function id at 100 */

    /**
     * Returns the current state and safety status of the ScanningController.
     *
     * @return current state and safety status.
     */
    ScanningControllerRpcGetFullA500State_out scanningControllerRpcGetFullA500State(void) = 100;

   
  } = 1;

} = 55555555; 
