package com.iba.device.util;

/**
 * This class groups all the constants used in the code.
 * The constants beginning with XML are used to create or to parse XML (elements, attributes)
 * The constants beginning with INIT are the keys of the web.xml config file
 * The constants beginning with HTTP are the names of the parameters sent to the server during the POSTS or the GETS
 * The constants beginning with JSON are the keys used in the json answers.
 * The constants beginning with DB are the names of the fields or tables when communicating with the DB
 * 
 * @author S�bastien Henrotin 
 * @version 0.1
 */
public class Constants 
{	
	public static final String DB_BOOL_TABLE = "boolrecords";
	public static final String DB_BYTE_TABLE = "byterecords";
	public static final String DB_DATA_LENGTH = "Data_length";
	public static final String DB_DEVICES = "devices";
	public static final String DB_DINT_TABLE = "dintrecords";
	public static final String DB_INDEX_LENGTH = "Index_length";

	public static final String DB_INT_TABLE = "intrecords";
	public static final String DB_REAL_TABLE = "realrecords";
	public static final String DB_ROWS = "Rows";
	public static final String DB_STRING_TABLE = "stringrecords";
	public static final String DB_TAGS = "tags";
	public static final String XML_ADDRESS = "address";
	public static final String XML_CLASS = "class";
	public static final String XML_DEAD_BAND = "deadband";
	public static final String XML_DESCRIPTION = "description";

	public static final String XML_DEVICE = "device";
	public static final String XML_DEVICE_TYPE = "deviceType";
	public static final String XML_DEVICE_TYPES = "deviceTypes";
	public static final String XML_INDEX= "index";
	public static final String XML_INFO = "info";
	public static final String XML_IP = "ip";
	public static final String XML_LOCATION = "location";
	public static final String XML_LOGGER = "logger";
	public static final String XML_LOGGERS = "loggers";
	public static final String XML_MAIL_TRIGGER = "mailTrigger";
	public static final String XML_MAIL_TRIGGERS = "mailTriggers";

	
	public static final String XML_NAME = "name";
    public static final String XML_ID   = "id";
    public static final String XML_CODE = "code";
	public static final String XML_PARAMETER = "parameter";
	public static final String XML_PORT = "port";
	public static final String XML_SITE = "site";
	public static final String XML_SITES = "sites";
	public static final String XML_TAG = "tag";
	public static final String XML_TAG_GROUP = "tagGroup";
	public static final String XML_TAGS = "tags";
	public static final String XML_THRESHOLD = "threshold";
	public static final String XML_TIMEZONE = "timezone";
	public static final String XML_TYPE = "type";
	
	
	public static final String XML_GX_PARAMETER = "gx_parameters";
	public static final String XML_GY_PARAMETER = "gy_parameters";
	public static final String XML_G11 = "G11";
	public static final String XML_G12 = "G12";
	public static final String XML_G21 = "G21";
	public static final String XML_G22 = "G22";
	
	public static final String XML_CENTER_TARGET = "bpm_center_target";
    public static final String XML_CENTER_TOLERANCE = "bpm_center_tolerance";
    public static final String XML_CENTER = "bpm_center";
    public static final String XML_SIGMA = "bpm_sigma";
    public static final String XML_P1E_X = "p1e_x";
    public static final String XML_P1E_Y = "p1e_y";
    public static final String XML_P2E_X = "p2e_x";
    public static final String XML_P2E_Y = "p2e_y";
    public static final String XML_TARGET = "target";
    public static final String XML_TOLERANCE = "tolerance";
    
    public static final String XML_NOTIFSERVER = "notifserver";
    public static final String XML_ECUBTCU = "ecubtcu";
    public static final String XML_IP_ADDRESS = "ip_address";
    public static final String XML_PORT_NUMBER = "port";
	public static final String XML_SCREEN = "monitor";
	public static final String XML_SCREEN_NUMBER = "number";
	public static final String XML_RESTORE = "restore_magnets";
	public static final String XML_RESTORE_CURRENTS = "current";
	public static final String XML_APPLY = "apply";
	public static final String XML_MAX = "max";
	public static final String XML_DEFLECTOR = "deflector";
	public static final String XML_VOLTAGE = "voltage";

    public static final String XML_BEAMLINE = "beamline";
    public static final String XML_FILE = "file";
}
