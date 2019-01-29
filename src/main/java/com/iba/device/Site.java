/*
* Application : LookupAnalyser
* Filename : Site.java
* Author : Henrotin S�bastien
* Date : 06/04/2010
* Company : IBA
* Version : 0.1
*/

package com.iba.device;

import org.apache.log4j.Logger;

import java.util.Vector;

public class Site 
{
	/**
	 * The logger. See http://logging.apache.org/log4j/1.2/index.html for more information.
	 */
	private static Logger log=Logger.getLogger(new Throwable().getStackTrace()[0].getClassName());

	private Vector<Device> devices;

	private String name, code;
    private int id;
    private String beamLine;
	
	private double g11x, g12x, g21x, g22x;
	private double g11y, g12y, g21y, g22y;
	
	private double[] bpm_targets, bpm_tolerances, sigma_targets, sigma_tolerances, safe_currents;

	private int screen_number;
	private double max_apply, def_volt;
	
	private String mNotifServerAddress ;
	private int mNotifPort ;
	
	private String mEcubtcuAddress ;
	
	public String getNotifServerAddress()
	{
		return mNotifServerAddress ;
	}
	
	public String getEcubtcuAddress()
	{
		return mEcubtcuAddress ;
	}
	
	public int getNotifPort()
	{
		return mNotifPort ;
	}
	
	public double getG11x()
	{
		return g11x ;
	}
	
	public double getG12x()
	{
		return g12x ;
	}
	
	public double getG21x()
	{
		return g21x ;
	}
	
	public double getG22x()
	{
		return g22x ;
	}
	
	public double getG11y()
	{
		return g11y ;
	}
	
	public double getG12y()
	{
		return g12y ;
	}
	
	public double getG21y()
	{
		return g21y ;
	}
	
	public double getG22y()
	{
		return g22y;
	}
	
	
	public void setNotifServerAddress(String strAddress)
	{
		mNotifServerAddress = strAddress;
	}
	
	public void setEcubtcuAddress(String strAddress)
	{
		mEcubtcuAddress = strAddress;
	}
	
	
	public void setNotifPort(int nPort)
	{
		mNotifPort = nPort;
	}
	
	
	public void setG11x(double x)
	{
		g11x = x ;
	}
	
	public void setG12x(double x)
	{
		g12x = x ;
	}
	
	public void setG21x(double x)
	{
		g21x = x ;
	}
	
	public void setG22x(double x)
	{
		g22x = x ;
	}
	
	
	public void setG11y(double y)
	{
		g11y = y ;
	}
	
	public void setG12y(double y)
	{
		g12y = y ;
	}
	
	public void setG21y(double y)
	{
		g21y = y ;
	}
	
	public void setG22y(double y)
	{
		g22y = y ;
	}
	
	public Site(String name, String id, String code)
	{
		this.name   = name;
		if(id == null || id == "")
		{
			this.id = 0 ;
		}
		else
		{
			this.id = Integer.parseInt(id);
		}
        
        this.code   = code;
		this.devices    = new Vector<Device>();
		log.info("Site created:"+this);
	}
	
	public void addDevice(Device device) 
	{
		this.devices.add(device);
	}

	public Device getDevice(String name)
	{
		for (Device device:devices)
		{
			if (device.getName().equalsIgnoreCase(name))
				return device;
		}
		return null;
	}

	public Vector<Device> getDevices() 
	{
		return devices;
	}

	public String getName() 
	{
		return name;
	}

    public String getCode()
    {
        return code;
    }

    public int getID()
    {
        return id;
    }
	@Override
	public String toString() 
	{
		StringBuffer result=new StringBuffer();
		result.append("[Site : ");
		result.append("name:"+name+",");
		result.append("id:"+id+",");
		result.append("code:"+code+",");
		result.append("nb devices:"+devices.size());
		result.append("]");
		return result.toString();
	}

    public double[] getBpmTargets() {
        return bpm_targets;
    }

    public void setBpmTargets(String targets) {
        bpm_targets = parseStringToDoubles(targets);
    }

    public double[] getBpmTolerances() {
        return bpm_tolerances;
    }

    public void setBpmTolerances(String str) {
        bpm_tolerances = parseStringToDoubles(str);
    }

    public double[] getSigmaTargets() {
        return sigma_targets;
    }

    public void setSigmaTargets(String targets) {
        sigma_targets = parseStringToDoubles(targets);
    }

    public double[] getSigmaTolerances() {
        return sigma_tolerances;
    }

	public double[] getSafeCurrents() {
		return safe_currents;
	}

	public void setScreen(String str) {
		screen_number = Integer.parseInt(str);
	}

	public int getScreen() {
		return screen_number;
	}

	public void setSafeCurrents(String str) {
		safe_currents = parseStringToDoubles(str);
	}

	public void setMaxCurrentChange(String str) {
		max_apply = Double.parseDouble(str);
	}

	public double getMaxCurrentChange() {
		return max_apply;
	}

	public void setDeflectorVoltage(String str) {
		def_volt = Double.parseDouble(str);
	}

	public double getDeflectorVoltage() {
		return def_volt;
	}

    public void setSigmaTolerances(String str) {
        sigma_tolerances = parseStringToDoubles(str);
    }

    public double[] parseStringToDoubles(String str) {
        String[] strList = str.split(";");
        double[] doubles = new double[strList.length];
        for (int i = 0; i < strList.length; i++) {
            doubles[i] = Double.parseDouble(strList[i]);
        }
        return doubles;
    }

    public String getBeamLine() {
        return beamLine;
    }

    public void setBeamLine(String file) {
        this.beamLine = file;
    }
}
