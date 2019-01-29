/*
* Application : LookupAnalyser
* Filename : DeviceType.java
* Author : Henrotin S�bastien
* Date : 06/04/2010
* Company : IBA
* Version : 0.1
*/

package com.iba.device;

import org.apache.log4j.Logger;

import java.util.Vector;

public class DeviceType 
{
	/**
	 * The logger. See http://logging.apache.org/log4j/1.2/index.html for more information.
	 */
	private static Logger log=Logger.getLogger(new Throwable().getStackTrace()[0].getClassName());

	private Class<? extends Device> deviceClass;
	
	private String name;
	
	private Vector<TagGroup> tagGroups;
	
	public DeviceType(String name,Class<? extends Device> deviceClass)
	{
		this.name=name;
		this.deviceClass=deviceClass;
		this.tagGroups=new Vector<TagGroup>();
		log.info("DeviceType created:"+this);
	}
	
	public void addGroup(TagGroup group)
	{
		this.tagGroups.add(group);
	}
	
	public Class<? extends Device> getDeviceClass()
	{
		return deviceClass;
	}

	public String getName() 
	{
		return name;
	}
	
	public Tag getTagFromName(String name)
	{
		for (TagGroup group:tagGroups)
		{
			Tag tag=group.getTagFromName(name);
			if (tag!=null)
				return tag;
		}
		
		return null;
	}
	
	public Vector<TagGroup> getTagGroups() 
	{
		return tagGroups;
	}
	
	@Override
	public String toString() 
	{
		StringBuffer result=new StringBuffer();
		result.append("[DeviceType :");
		result.append("name:"+name+",");
		result.append("deviceClass:"+deviceClass.toString());	
		result.append("]");
		return result.toString();
	}
}
