/*
* Application : Device
* Filename : SiteManager.java
* Author : Henrotin S�bastien
* Date : 06/04/2010
* Company : IBA
* Version : 0.1
*/

package com.iba.device;

import org.apache.log4j.Logger;

import java.io.InputStream;
import java.util.Vector;

public class SiteManager 
{
	/**
	 * The logger. See http://logging.apache.org/log4j/1.2/index.html for more information.
	 */
	private static Logger log=Logger.getLogger(new Throwable().getStackTrace()[0].getClassName());

	private static SiteManager singleton=null;

	public static SiteManager getSiteManager()
	{
		if (singleton==null)
			singleton=new SiteManager();
		
		return singleton;
	}
	
	private Vector<Site> sites;

	//Singleton
	private SiteManager()
	{
		sites=new Vector<Site>();
		log.info("SiteManager created");			
	}
	
	public void buildSitesFromStream(InputStream is)
	{
		XmlConnector xmlConnector = new XmlConnector(is);
		this.sites=xmlConnector.buildSites();
		xmlConnector.close();
	}
	
	
	public Vector<Device> getDevices()
	{
		Vector<Device> result=new Vector<Device>();
		
		for (Site site:sites)
		{
			result.addAll(site.getDevices());
		}
		
		return result;
	}
	
	public Vector<Device> getDevices(DeviceType deviceType) 
	{
		Vector<Device> result=new Vector<Device>();
		for (Device device:getDevices())
		{
			if (device.getDeviceType()==deviceType)
				result.add(device);
		}
		return result;
	}
	
	public Site getFirstSite()
	{
		return sites.firstElement();
	}

	public Site getSite(String name)
	{
		for (Site site:sites)
		{
			if (site.getName().equals(name))
				return site;
		}
		log.warn("Unable to find the site: "+name);
		return null;
	}
	
	public Vector<Site> getSites()
	{
		return sites;
	}
}