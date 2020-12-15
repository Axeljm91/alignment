/*
* Application : Device
* Filename : BcreuDevice.java
* Author : Henrotin S�bastien
* Date : 06/04/2010
* Company : IBA
* Version : 0.1
*/

package com.iba.device;

import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.iba.ialign.Status;
import com.iba.ialign.common.IbaColors;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.log4j.Logger;

/**
 * This class represent a connector, able to connect to a BCREU. Use this class to retreive the lookup table or to change some parameters. 
 * This class communicates with the physical device using http requests
 * @author S�bastien Henrotin
 */
public class BcreuHttpDevice extends Device
{	
	/**
	 * The logger. See http://logging.apache.org/log4j/1.2/index.html for more information.
	 */
    private static org.apache.log4j.Logger log=Logger.getLogger(new Throwable().getStackTrace()[0].getClassName());

    private Integer[] mValues = new Integer[4];
    private Double mScaleFactor;

    static final public int STATUS_TCS_HW       = 0x0001;
    static final public int STATUS_REMOTE       = 0x0002;
    static final public int STATUS_SOURCE_EN    = 0x0004;
    static final public int STATUS_CONTINUOUS   = 0x0008;
    static final public int STATUS_TR           = 0x0010;
    static final public int STATUS_TR_MASK      = 0x00E0;
    static final public int STATUS_TR_OFFSET    = 5;
    static final public int REGULATION_RUNNING  = 0x0100;
    static final public int REGULATION_PAUSED   = 0x0200;
    static final public int LOOKUP_RUNNING      = 0x0400;
    static final public int STATUS_BYPASS       = 0x0800;
    static final public int STATUS_LOOKUP_VALID = 0x1000;
    static final public int REGULATION_OUT      = 0x2000;
    static final public int REGULATION_MODE     = 0x4000;
    static final public int STATUS_BEAM_ON      = 0x8000;
	
	private static String grep(HttpResponse response, String string)
	    {
		   try
		   {
			   BufferedReader br=new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
	
		    	while (true)
		    	{
		    		String line=br.readLine();
		    		
		    		if (line==null)
		    			return null;
		    				
		    		if (line.contains(string))
		    			return line;
		    	}
		   }
		   catch (IOException e)
		   {
			   log.error("Error with the communication with the BCREU (grep) : "+e.getMessage());
			   return null;
		   }
		}

	/**
	 * This object creates the http connections
	 */
    private CloseableHttpClient httpclient;
    
    /**
	 * The password used to get access to the admin mode
	 */
	private String password;

	/**
     * Constructor
     * @param site the site where is located the BCREU
     * @param name the name of the BCREU
     * @param ip the ip adress of the BCREU
     * @param info put the password allowing the access to the admin mode
     */
	public BcreuHttpDevice(Site site, String name, String ip, String info,DeviceType deviceType) 
	{
		super(site, name, ip, info,deviceType);
		password=infos[0];
        mScaleFactor = Double.parseDouble(infos[1]);
	}

	public boolean authenticate()
    {
		//Authentification
        HttpResponse response=doPost("http://"+ip+"/auth.cgi","WRITE_AUTH",password);
        if (response==null)
        	return false;

        close(response);
        
        return true;
    }

	private void close(HttpResponse response)
    {
       if (response==null)
       {
   			log.error("Error with the communication with the BCREU (closing the post)");
   			return;
       }   
    	   
    	HttpEntity entity = response.getEntity();
        if (entity!=null)
        {
        	try 
        	{
				entity.consumeContent();
			} 
        	catch (IOException e) 
        	{
        		log.error("Error with the communication with the BCREU (closing the post) : "+e.getMessage());
        	}
        }
    }

	/**
	 * Establish a connection with the BCREU. If you are already connected, you get a warning. 
	 */
	@Override
	public void connect() 
	{
        // Disable automatic retries.
        httpclient = HttpClientBuilder.create().disableAutomaticRetries().disableCookieManagement().disableRedirectHandling().build();
        log.debug("Created HTTP");
	}

	@Override
	public void disconnect()
	{
        try {
            httpclient.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
	
	
	private HttpResponse doGet(String host)
    {
        log.debug("Post to "+host);
    	HttpGet httpget = new HttpGet(host);

        HttpResponse response=null;
        
		try 
		{
			response = httpclient.execute(httpget);
	        log.debug("Answer: " + response.getStatusLine());
		}
        catch (SocketException e) {
            log.error("Error communicating with BCREU: " + e);
            return null;
        }
		catch (ClientProtocolException e) 
		{
    		log.error("Error communicating with BCREU: " + e);
		} 
		catch (IOException e) 
		{
    		log.error("Error communicating with BCREU: " + e);
		}
        return response;
    }
	
	private HttpResponse doPost(String host,String param,String value)
    {
        log.debug("Post to "+host+" with param="+param+" and value="+value);
		HttpPost httppost = new HttpPost(host);

        List <NameValuePair> nvps = new ArrayList <NameValuePair>();
        nvps.add(new BasicNameValuePair(param, value));

        try 
        {
			httppost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
		} 
        catch (UnsupportedEncodingException e) 
        {
        	log.error("Error with the communication with the BCREU (HTTP post) :"+e.getMessage());
        	return null;
        }

        HttpResponse response=null;
		try 
		{
			response = httpclient.execute(httppost);
	        log.debug("Answer: " + response.getStatusLine());
		} 
		catch (ClientProtocolException e) 
		{
        	log.error("Error with the communication with the BCREU (HTTP post) :"+e.getMessage());
		} 
		catch (IOException e) 
		{
        	log.error("Error with the communication with the BCREU (HTTP post) :"+e.getMessage());
		}
              
        return response;
    }

    public Double getScaleFactor() {
        return mScaleFactor;
    }

    public Double getIcCyclo() {
        Double dval = (double) mValues[3];
        if (Math.floor(dval * mScaleFactor)/5.0d > 0.0d) {
            return Math.floor(dval * mScaleFactor) / 5.0d;
        }else {
            return 0.0d;
        }
    }

    public Double getMaxBeam() {
        Double dval = (double) mValues[2];
        return Math.floor(dval * mScaleFactor)/5.0d;
    }

    public int getTR() {
        return (((mValues[0] & STATUS_TR_MASK) >> STATUS_TR_OFFSET) + 1);
    }

    public String getPulseSource() {
        if ((mValues[0] & STATUS_SOURCE_EN) == 0) {
            return "Disabled";
        } else if ((mValues[0] & STATUS_TR) == STATUS_TR) {
            return "TR" + getTR();
        } else {
            return "Internal: " + (((mValues[0] & STATUS_CONTINUOUS) == 0) ? "Single" : "Continuous");
        }
    }

    public String getPulseSource(boolean isSingle) {
        if ((mValues[0] & STATUS_SOURCE_EN) == 0) {
            return "Disabled";
        } else if ((mValues[0] & STATUS_TR) == STATUS_TR) {
            return "TR" + getTR();
        } else {
            return "Internal: " + ((isSingle) ? "Single" : "Continuous");
        }
    }

    public Integer[] getValues() {
        return mValues;
    }
	
	public Integer[] refreshValues()
	{
       	HttpResponse response=doGet("http://"+ip+"/pageload.cgi?page=advstsys.htm");
        
       	if (response==null)
        	return null;

        String line;
        String[] parsedLine;
        try (BufferedReader br=new BufferedReader(new InputStreamReader(response.getEntity().getContent()))) {
            int check = 0;

            // Timeout check for data retrieval
            long startTime = System.currentTimeMillis();
            // readLine() returns null if end of the stream.
            while (((line = br.readLine()) != null) & (check < 0xF)) {
                if (System.currentTimeMillis() - startTime > 1000) {
                    log.error("Couldn't retrieve HTTP data from BCREU");
                    return null;
                }
                if (line.contains("iStatusWord =")) {
                    parsedLine = line.split("\"");
                    mValues[0] = Integer.parseInt(parsedLine[1]);
                    check |= 0x1;
                } else if (line.contains("iRunState =")) {
                    parsedLine = line.split("\"");
                    mValues[1] = Integer.parseInt(parsedLine[1]);
                    check |= 0x2;
                } else if (line.contains("iMaxBeamI =")) {
                    parsedLine = line.split("\"");
                    mValues[2] = Integer.parseInt(parsedLine[1]);
                    check |= 0x4;
                } else if (line.contains("iAvgIcCyclo =")) {
                    parsedLine = line.split("\"");
                    mValues[3] = Integer.parseInt(parsedLine[1]);
                    check |= 0x8;
                }
            }
        }
        catch (IOException e)
        {
            log.error("Error with the communication with the BCREU (grep) : "+e.getMessage());
        }
	    close(response);
       	return mValues;
	}

	public Vector<Double> getLookup() 
	{
		String curveRequest="1";			
	
		if (!authenticate())
			return null;
		
        //Ask the BCREU to bring the lookup tables back to the microcontroller
        HttpResponse response=doPost("http://"+ip+"/setdata.cgi","CURVE_REQUEST",curveRequest);
        if (response==null)
        	return null;
        
        close(response);        

		try 
		{
			Thread.sleep(3000);
		} 
		catch (InterruptedException e1) 
		{
			log.error("Error with the Thread.sleep. Shouldn't happen");
		}

        while(true)
        {
        	response=doGet("http://"+ip+"/pageload.cgi?page=curves.htm");
            if (response==null)
            	return null;
            
        	String line=grep(response,"document.getElementById(\"trcprogress\").innerHTML");
            close(response);

        	int p=Integer.parseInt(line.substring(74, 77));
    		
    		log.debug("Progress :"+p);
        	if (p==100)
        		break;
        	
        	try 
        	{
				Thread.sleep(2000);
			} 
        	catch (InterruptedException e) 
        	{
				e.printStackTrace();
			}
        }
        
        log.debug("Lookup transfered to the network microcontroller.");
        log.debug("Requesting the csv file");
        response=doGet("http://"+ip+"/lkp00.csv");
        if (response==null)
        	return null;
        
        Vector<Double> values=getLookupValues(response);
        close(response);
        return values;	
	}

    public String getBcreuConnection() {
        if ((mValues[0] & STATUS_REMOTE) == 0) {
            return "Local";
        } else {
            return "Remote: " + (((mValues[0] & STATUS_TCS_HW) == 0) ? "Ethernet" : "Hardwired");
        }
    }
	
	public String getLookupTableState()
	{
        if ((mValues[0] & LOOKUP_RUNNING) == LOOKUP_RUNNING) {
            return "Generating";
        } else if ((mValues[0] & STATUS_LOOKUP_VALID) == STATUS_LOOKUP_VALID) {
	    	return "Valid";
	    } else {
	    	return "Empty / Invalid";
	    }
	}

    public Color getLookupTableColor()
    {
        if ((mValues[0] & LOOKUP_RUNNING) == LOOKUP_RUNNING) {
            return Status.WARNING;
        } else if ((mValues[0] & STATUS_LOOKUP_VALID) == STATUS_LOOKUP_VALID) {
            return Status.HEALTHY;
        } else {
            return Status.UNHEALTHY;
        }

    }
	
   private Vector<Double> getLookupValues(HttpResponse response)
{
   try
   {
	   Vector<Double> result=new Vector<Double>();
	   BufferedReader br=new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

    	while (true)
    	{
    		String line=br.readLine();
    		
    		if (line==null)
    			break;
    		
    		result.add(Double.parseDouble(line));
    	}
    	
    	return result;
   }
   catch (IOException e)
   {
	   log.error("Error with the communication with the BCREU (grep) : "+e.getMessage());
	   return null;
   }	
}

    public String getRegulationState() {
        if ((mValues[0] & STATUS_BYPASS) == STATUS_BYPASS) {
            return "Bypassed";
        } else if ((mValues[0] & REGULATION_PAUSED) == REGULATION_PAUSED) {
            return "Paused";
        } else if ((mValues[0] & REGULATION_RUNNING) == REGULATION_RUNNING) {
            return "Running";
        } else {
            return "Unknown";
        }
    }

    public String getRunningState()
	{
        if ((mValues[0] & STATUS_BYPASS) == STATUS_BYPASS) {
            return "Bypassed";
        } else {
            switch (mValues[1]) {
                case 0:
                    return "Initializing";
                case 1:
                    return "Idle";
                case 2:
                    return "Config Update";
                case 3:
                    return "Start Lookup";
                case 4:
                    return "Generate Lookup";
                case 5:
                    return "Compute Lookup";
                case 6:
                    return "Start Regulation";
                case 7:
                    return "Regulating";
                case 8:
                    return "Lookup Failed";
                default:
                    return "Unknown: " + mValues[1];
            }
        }
	}

    public Color getRunningStateColor(boolean bypass)
    {
        if ((mValues[0] & STATUS_BYPASS) == STATUS_BYPASS) {
            return (bypass ? Status.HEALTHY : Status.UNHEALTHY);
        } else {
            switch (mValues[1]) {
                case 0:
                    return Status.WARNING;
                case 1:
                    return Status.NO_COLOR;
                case 2:
                    return Status.WARNING;
                case 3:
                    return Status.WARNING;
                case 4:
                    return Status.WARNING;
                case 5:
                    return Status.WARNING;
                case 6:
                    return Status.WARNING;
                case 7:
                    return (bypass ? Status.UNHEALTHY : Status.HEALTHY);
                case 8:
                    return Status.UNHEALTHY;
                default:
                    return Status.UNHEALTHY;
            }
        }
    }

    
    @Override
	public Object getTagValue(Tag tag) 
	{
		TagGroupType type=tag.getGroup().getType();
		if (!(type.equals(TagGroupType.READ)||type.equals(TagGroupType.READWRITE)))
		{
			log.warn("Trying to read a tag and its group is not from the type READ nor READWRITE");
			return null;
		}
		
		if (tag.getName().equals("Running State"))
            return getRunningState();

		if (tag.getName().equals("Max Beam Current in nA"))
			return getMaxBeam();

		if (tag.getName().equals("Max Beam Current in digits"))
            return mValues[2];

		if (tag.getName().equals("IC Cyclo Current in nA"))
			return getIcCyclo();
		
		if (tag.getName().equals("IC Cyclo Current in digits"))
			return mValues[3];
		
		if (tag.getName().equals("Lookup Table State"))
			return getLookupTableState();
		
		if (tag.getName().equals("Lookup"))
			return getLookup();
		
		return null;
	}

    @Override
	public void setTagValue(Tag tag, Object value) 
	{
		log.warn("Trying to change a tag in an BCREU : not implemented");		
	}
}
