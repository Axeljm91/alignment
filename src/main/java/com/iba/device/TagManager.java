/*
* Application : Device
* Filename : TagManager.java
* Author : Henrotin S�bastien
* Date : 06/04/2010
* Company : IBA
* Version : 0.1
*/

package com.iba.device;

import org.apache.log4j.Logger;

import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Vector;

/**
 * This function takes care of the different type of devices and the different tags
 * @author Henrotin S�bastien
 */
public class TagManager 
{
	/**
	 * The logger. See http://logging.apache.org/log4j/1.2/index.html for more information.
	 */
	private static Logger log=Logger.getLogger(new Throwable().getStackTrace()[0].getClassName());

	/**
	 * The singleton
	 */
	private static TagManager singleton=null;
	
	public static TagManager getTagManager()
	{
		if (singleton==null)
			singleton=new TagManager();
		
		return singleton;
	}
	
	/**
	 * The different type of devices (BCREU, LLRF, ACU, etc)
	 */
	private Vector<DeviceType> deviceTypes;
	
	//Singleton
	private TagManager()
	{		
		deviceTypes=new Vector<DeviceType>();		
		log.info("TagManager created");	
	}
	
	/**
	 * Parse the xml file containing all the tags from a resource (defined in the classpath)
	 * @param is
	 */
	public void buildTagsFromStream(InputStream is)
	{
		XmlConnector xmlConnector = new XmlConnector(is);
		deviceTypes=xmlConnector.buildTags();
		xmlConnector.close();
	}
	
	/**
	 * Create a new Device of the type corresponding to this object.
	 * @param type
	 * @param name
	 * @param ip
	 * @param info
	 * @param site
	 * @return
	 */
	public Device createNewDevice(String type, String name, String ip, String info, Site site)
	{		
		DeviceType deviceType=getDeviceType(type);
		
		if (deviceType==null)
			log.error("The device with the type: "+type+" has not been found. Check sites.xml");

		try 
		{
			Constructor<? extends Device> constructor=deviceType.getDeviceClass().getConstructor(Site.class,String.class,String.class,String.class,DeviceType.class);
			Device result=constructor.newInstance(site,name,ip,info,deviceType);
			
			log.info("Device created :"+result);
			return result;
		} 
		catch (InstantiationException e) 
		{
			log.error("Error during the creation of the device type="+deviceType+"."+e.getMessage());
		} 
		catch (IllegalAccessException e) 
		{
			log.error("Error during the creation of the device type="+deviceType+"."+e.getMessage());
		} 
		catch (SecurityException e) 
		{
			log.error("Error during the creation of the device type="+deviceType+"."+e.getMessage());
		}
		catch (NoSuchMethodException e) 
		{
			log.error("Error during the creation of the device type="+deviceType+"."+e.getMessage());
		} 
		catch (IllegalArgumentException e) 
		{
			log.error("Error during the creation of the device type="+deviceType+"."+e.getMessage());
		} 
		catch (InvocationTargetException e) 
		{
			log.error("Error during the creation of the device type="+deviceType+"."+e.getMessage());
		}		

		return null;
	}
	
	/**
	 * Returns the deviceType from the name. Ex: "Acu230", "Llrf", etc...
	 * @param name
	 * @return the deviceType
	 */
	public DeviceType getDeviceType(String name)
	{
		for (DeviceType deviceType:deviceTypes)
		{
			if (deviceType.getName().equalsIgnoreCase(name))
				return deviceType;
		}
		
		return null;
	}

	public Vector<DeviceType> getDeviceTypes() 
	{
		return deviceTypes;
	}
}
