/*
* Application : LookupAnalyser
* Filename : Device.java
* Author : Henrotin Sebastien
* Date : 06/04/2010
* Company : IBA
* Version : 0.1
*/

package com.iba.device;

import org.apache.log4j.Logger;

/**
 * This class represent an abstract Device (LLRF, ACU, SmartSocket or any other electrical unit)
 * @author Henrotin Sebastien
 */
public abstract class Device 
{
	/**
	 * The logger. See http://logging.apache.org/log4j/1.2/index.html for more information.
	 */
	private static Logger log=Logger.getLogger(new Throwable().getStackTrace()[0].getClassName());

	protected boolean connected;
	protected DeviceType deviceType;
	private int id;
	protected String[] infos;
	protected String ip;
	protected String name;
	protected Site site;
	
	protected Device(Site site,String name,String ip,String info,DeviceType deviceType)
	{
		connected=false;
		this.site=site;
		this.name=name;
		this.ip=ip;
		this.infos=info.split(";");
		this.deviceType=deviceType;
		this.id=0;
	}

	public abstract void connect();
	
	public abstract void disconnect();

	public DeviceType getDeviceType()
	{
		return deviceType;
	}
	
	public int getId() 
	{
		return id;
	}

	public String[] getInfos() 
	{
		return infos;
	}
	
	public String getIp() 
	{
		return ip;
	}
	
	public String getName()
	{
		return name;
	}

	public Site getSite()
	{
		return site;
	}

	public Object getTagValue(String name) 
	{
		Tag tag=this.getDeviceType().getTagFromName(name);
		if (tag==null)
		{
			log.warn("Impossible to get the tag:"+name);
			return null;
		}
		return getTagValue(tag);
	}
	
	public abstract Object getTagValue(Tag tag);
	
	public void initialize()
	{
		
	}

	
	public boolean isConnected() 
	{
		return connected;
	}	

	public void setId(int id)
	{
		this.id=id;
	}	
	
	public void setIp(String ip) 
	{
		this.ip=ip;
	}
	
	public void setName(String name) 
	{
		this.name=name;
	}
	public void setSite(Site site)
	{
		this.site=site;
	}
	public void setTagValue(String name, Object value) 
	{
		setTagValue(this.getDeviceType().getTagFromName(name), value);
	}

	public abstract void setTagValue(Tag tag,Object value);
	
	@Override
	public String toString() 
	{
		StringBuffer result=new StringBuffer();
		result.append("[Device:");
		result.append("name:"+name+",");
		result.append("ip:"+ip+",");
		result.append("info:"+infos+",");
		result.append("connected:"+connected+",");
		result.append("site:"+site+",");
		result.append("deviceType:"+deviceType);			
		result.append("]");
		return result.toString();
	}
}
