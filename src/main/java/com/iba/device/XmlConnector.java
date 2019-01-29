/*
* Application : LookupAnalyser
* Filename : XmlConnector.java
* Author : Henrotin S�bastien
* Date : 06/04/2010
* Company : IBA
* Version : 0.1
*/

package com.iba.device;

import com.iba.device.util.Constants;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Vector;

/**
 * This class takes care of any interaction with the XML files (writing or parsing)
 * @author S�bastien Henrotin
 */
public class XmlConnector 
{
	/**
	 * This object will create th dom.
	 */
	protected static DocumentBuilder builder;

	/**
	 * The logger. See http://logging.apache.org/log4j/1.2/index.html for more information.
	 */
	private static Logger log=Logger.getLogger(new Throwable().getStackTrace()[0].getClassName());
	
	/**
	 * The builder will always be the same, we can create it at the loading of the class.
	 */
	static
	{
		DocumentBuilderFactory factory=DocumentBuilderFactory.newInstance();
		try 
		{
			builder=factory.newDocumentBuilder();
		} 
		catch (ParserConfigurationException e) 
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * The document object model. See http://www.w3schools.com/dom/default.asp for more info.
	 */
	protected Document dom;
	
	/**
	 * The current xml file you're playing with.
	 */
	protected Object stream;
	
	/**
	 * Constructor
	 * @param stream name of the file you want to read or write
	 */
	public XmlConnector(Object stream)
	{
		if (!((stream instanceof InputStream)||(stream instanceof OutputStream)))
			log.error("Trying to create an XMLConnector with a stream which is not a OutputStream neither a InputStream");
		
		this.stream=stream;
		
	}
		
	/**
	 * This function read the xml file containing all the sites information, devices, ip, port, etc..
	 * @return a Vector with the sites. Return null if there is a parsing problem.
	 */
	public Vector<Site> buildSites() 
	{
		if(!parse())
			return null;
		
		TagManager tagManager=TagManager.getTagManager();
		
		NodeList sitesNodeList=dom.getElementsByTagName(Constants.XML_SITES);
		Element sitesElement=(Element)sitesNodeList.item(0);

		Vector<Site> result=new Vector<Site>();

		NodeList siteNodeList=sitesElement.getElementsByTagName(Constants.XML_SITE);
		
		for (int i=0;i<siteNodeList.getLength();i++)
		{
			//it can be a space or another character, in that case, we skip it
			if (!(siteNodeList.item(i) instanceof Element))
					continue;
			
			Element siteElement=(Element)siteNodeList.item(i);
			
			String str_name = siteElement.getAttribute(Constants.XML_NAME); 
			String str_id = siteElement.getAttribute(Constants.XML_ID) ;
			String str_code = siteElement.getAttribute(Constants.XML_CODE) ;
			
			if(str_name == null | str_name =="" || str_code == null || str_code == "")
			{
				log.info("site name or site code is null, please check your site configuration.");
				return null;
			}
			
			log.info("current site name is " + str_name + ", id is " + str_id + ", and code of the is the site is " + str_code + ".");

			Site site=new Site(str_name, str_id, str_code);
			
			NodeList deviceNodeList=siteElement.getElementsByTagName(Constants.XML_DEVICE);
			for (int j=0;j<deviceNodeList.getLength();j++)
			{
				//it can be a space or another character, in that case, we skip it
				if (!(deviceNodeList.item(j) instanceof Element))
						continue;
				
				Element deviceElement=(Element)deviceNodeList.item(j);
				String name=deviceElement.getAttribute(Constants.XML_NAME);
				String type=deviceElement.getAttribute(Constants.XML_TYPE);				
				String ip=deviceElement.getAttribute(Constants.XML_IP);
				
				String info=deviceElement.getAttribute(Constants.XML_INFO);
				
				Device device=tagManager.createNewDevice(type,name,ip,info,site);
				site.addDevice(device);
			}
			
			NodeList gxParameterNodeList=siteElement.getElementsByTagName(Constants.XML_GX_PARAMETER);
			for (int j=0;j<gxParameterNodeList.getLength();j++)
			{
				//it can be a space or another character, in that case, we skip it
				if (!(gxParameterNodeList.item(j) instanceof Element))
						continue;
				
				Element deviceElement=(Element)gxParameterNodeList.item(j);
			//	String g11 = deviceElement.getAttribute(Constants.XML_G11) ;
				
		//		String g12 = deviceElement.getAttribute(Constants.XML_G12) ;
		//		String g21 = deviceElement.getAttribute(Constants.XML_G21) ;
				
				site.setG11x(Double.valueOf(deviceElement.getAttribute(Constants.XML_G11)));
				site.setG12x(Double.valueOf(deviceElement.getAttribute(Constants.XML_G12)));
				site.setG21x(Double.valueOf(deviceElement.getAttribute(Constants.XML_G21)));
				site.setG22x(Double.valueOf(deviceElement.getAttribute(Constants.XML_G22)));
			}
			
			NodeList gyParameterNodeList=siteElement.getElementsByTagName(Constants.XML_GY_PARAMETER);
			for (int j=0;j<gyParameterNodeList.getLength();j++)
			{
				//it can be a space or another character, in that case, we skip it
				if (!(gyParameterNodeList.item(j) instanceof Element))
						continue;
				
				Element deviceElement=(Element)gyParameterNodeList.item(j);
				site.setG11y(Double.valueOf(deviceElement.getAttribute(Constants.XML_G11)));
				site.setG12y(Double.valueOf(deviceElement.getAttribute(Constants.XML_G12)));
				site.setG21y(Double.valueOf(deviceElement.getAttribute(Constants.XML_G21)));
				site.setG22y(Double.valueOf(deviceElement.getAttribute(Constants.XML_G22)));
			}

            NodeList bpmCenterNodeList=siteElement.getElementsByTagName(Constants.XML_CENTER);
            for (int j=0;j<bpmCenterNodeList.getLength();j++)
            {
                //it can be a space or another character, in that case, we skip it
                if (!(bpmCenterNodeList.item(j) instanceof Element))
                    continue;

                Element deviceElement=(Element)bpmCenterNodeList.item(j);

                site.setBpmTargets(deviceElement.getAttribute(Constants.XML_TARGET));
                site.setBpmTolerances(deviceElement.getAttribute(Constants.XML_TOLERANCE));
            }

            NodeList bpmSigmaNodeList=siteElement.getElementsByTagName(Constants.XML_SIGMA);
            for (int j=0;j<bpmSigmaNodeList.getLength();j++)
            {
                //it can be a space or another character, in that case, we skip it
                if (!(bpmSigmaNodeList.item(j) instanceof Element))
                    continue;

                Element deviceElement=(Element)bpmSigmaNodeList.item(j);

                site.setSigmaTargets(deviceElement.getAttribute(Constants.XML_TARGET));
                site.setSigmaTolerances(deviceElement.getAttribute(Constants.XML_TOLERANCE));
            }


            NodeList beamlineNodeList=siteElement.getElementsByTagName(Constants.XML_BEAMLINE);
            for (int j=0;j<beamlineNodeList.getLength();j++)
            {
                //it can be a space or another character, in that case, we skip it
                if (!(beamlineNodeList.item(j) instanceof Element))
                    continue;

                Element deviceElement=(Element)beamlineNodeList.item(j);

                site.setBeamLine(deviceElement.getAttribute(Constants.XML_FILE));
            }
            
            
            NodeList notifServerList=siteElement.getElementsByTagName(Constants.XML_NOTIFSERVER);
            for (int j=0;j<notifServerList.getLength();j++)
            {
                //it can be a space or another character, in that case, we skip it
                if (!(notifServerList.item(j) instanceof Element))
                    continue;

                Element deviceElement=(Element)notifServerList.item(j);
                
                String notif=deviceElement.getAttribute(Constants.XML_IP_ADDRESS);
                site.setNotifServerAddress(notif);
                String port=deviceElement.getAttribute(Constants.XML_PORT_NUMBER);
                site.setNotifPort(Integer.parseInt(port));
            }
            
            NodeList ecubtcuList=siteElement.getElementsByTagName(Constants.XML_ECUBTCU);
            for (int j=0;j<ecubtcuList.getLength();j++)
            {
                //it can be a space or another character, in that case, we skip it
                if (!(ecubtcuList.item(j) instanceof Element))
                    continue;

                Element deviceElement=(Element)ecubtcuList.item(j);
                
                String ecubtcu=deviceElement.getAttribute(Constants.XML_IP_ADDRESS);

                site.setEcubtcuAddress(ecubtcu);
            }

			NodeList screenList=siteElement.getElementsByTagName(Constants.XML_SCREEN);
			for (int j=0;j<screenList.getLength();j++)
			{
				//it can be a space or another character, in that case, we skip it
				if (!(screenList.item(j) instanceof Element))
					continue;

				Element deviceElement=(Element)screenList.item(j);

				String screen=deviceElement.getAttribute(Constants.XML_SCREEN_NUMBER);

				site.setScreen(screen);
			}

			NodeList safeCurrentList=siteElement.getElementsByTagName(Constants.XML_RESTORE);
			for (int j=0;j<safeCurrentList.getLength();j++)
			{
				//it can be a space or another character, in that case, we skip it
				if (!(safeCurrentList.item(j) instanceof Element))
					continue;

				Element deviceElement=(Element)safeCurrentList.item(j);

				String safe=deviceElement.getAttribute(Constants.XML_RESTORE_CURRENTS);

				site.setSafeCurrents(safe);
			}

			NodeList maxCurrentChangeList=siteElement.getElementsByTagName(Constants.XML_APPLY);
			for (int j=0;j<maxCurrentChangeList.getLength();j++)
			{
				//it can be a space or another character, in that case, we skip it
				if (!(maxCurrentChangeList.item(j) instanceof Element))
					continue;

				Element deviceElement=(Element)maxCurrentChangeList.item(j);

				String maxChange=deviceElement.getAttribute(Constants.XML_MAX);

				site.setMaxCurrentChange(maxChange);
			}

			NodeList deflectorVoltageList=siteElement.getElementsByTagName(Constants.XML_DEFLECTOR);
			for (int j=0;j<deflectorVoltageList.getLength();j++)
			{
				//it can be a space or another character, in that case, we skip it
				if (!(deflectorVoltageList.item(j) instanceof Element))
					continue;

				Element deviceElement=(Element)deflectorVoltageList.item(j);

				String defVoltage=deviceElement.getAttribute(Constants.XML_VOLTAGE);

				site.setDeflectorVoltage(defVoltage);
			}
            
			
			result.add(site);
		}
		return result;
	}
	
	/**
	 * Load the tags from the xml file containing the deviceTypes and their tags (+id, description, type)
	 * @return a Vector containing all the deviceTypes and their tags. Returns null if there is a parsing problem.
	 */
	@SuppressWarnings("unchecked")
	public Vector<DeviceType> buildTags() 
	{		
		if (!parse())
			return null;
		
		NodeList deviceTypesNodeList=dom.getElementsByTagName(Constants.XML_DEVICE_TYPES);
		Element deviceTypesElement=(Element)deviceTypesNodeList.item(0);

		Vector<DeviceType> result=new Vector<DeviceType>();
		
		NodeList deviceTypeNodeList=deviceTypesElement.getElementsByTagName(Constants.XML_DEVICE_TYPE);
		for (int i=0;i<deviceTypeNodeList.getLength();i++)
		{
			//it can be a space or another character, in that case, we skip it
			if (!(deviceTypeNodeList.item(i) instanceof Element))
					continue;
			
			Element deviceTypeElement=(Element)deviceTypeNodeList.item(i);
			String name=deviceTypeElement.getAttribute(Constants.XML_NAME);
			String className=deviceTypeElement.getAttribute(Constants.XML_CLASS);
			
			DeviceType deviceType=null;
			try 
			{
				deviceType = new DeviceType(name,(Class<? extends Device>) Class.forName(className));
			} 
			catch (ClassNotFoundException e) 
			{
				log.error("Error during the parsing of the xml stream "+e.getMessage());
				return null;
			}

			result.add(deviceType);
			
			NodeList tagGroupsNodeList=deviceTypeElement.getElementsByTagName(Constants.XML_TAG_GROUP);
			for (int j=0;j<tagGroupsNodeList.getLength();j++)
			{
				//it can be a space or another character, in that case, we skip it
				if (!(tagGroupsNodeList.item(j) instanceof Element))
						continue;
				
				Element tagGroupElement=(Element)(tagGroupsNodeList.item(j));
				String type=tagGroupElement.getAttribute(Constants.XML_TYPE);
				TagGroup group=new TagGroup(TagGroupType.valueOf(type), deviceType);
				
				//Parameters
				NodeList parametersNodeList=tagGroupElement.getElementsByTagName(Constants.XML_PARAMETER);

				for (int k=0;k<parametersNodeList.getLength();k++)
				{
					//it can be a space or another character, in that case, we skip it
					if (!(parametersNodeList.item(k) instanceof Element))
							continue;
					
					Element parameterElement=(Element)parametersNodeList.item(k);
					
					int index=Integer.parseInt(parameterElement.getAttribute(Constants.XML_INDEX));
					String parameterType=parameterElement.getAttribute(Constants.XML_TYPE);
					String parameterName=parameterElement.getTextContent();
					
					
					
					
					group.addParameterName(index, parameterName,parameterType);
				}
				
				//Tags
				NodeList tagsNodeList=tagGroupElement.getElementsByTagName(Constants.XML_TAGS);
				
				if (tagsNodeList.getLength()==0)
				{
					log.warn("No tag \"tags\" in tags.xml for the deviceType :"+deviceType);
					continue;
				}
				
				Element tagsElement=(Element)tagsNodeList.item(0);			
				NodeList tagNodeList=tagsElement.getElementsByTagName(Constants.XML_TAG);
				for (int k=0;k<tagNodeList.getLength();k++)
				{
					//it can be a space or another character, in that case, we skip it
					if (!(tagNodeList.item(k) instanceof Element))
							continue;
					
					Element tagElement=(Element)tagNodeList.item(k);
					
					String tagName=tagElement.getAttribute(Constants.XML_NAME);
					String description=tagElement.getAttribute(Constants.XML_DESCRIPTION);
					TagType tagType=TagType.valueOf(tagElement.getAttribute(Constants.XML_TYPE));
					
					Tag tag=new Tag(tagName,description,tagType,group);
							
					for (int l=0;l<group.getNbParametersName();l++)
					{
						String parameterName=group.getParameterName(l);
						String parameterType=group.getParameterType(l);
						
						String parameterValue=tagElement.getAttribute(parameterName);
						if (parameterValue.length()!=0)
						{
							if (parameterType.equals("INT"))
								tag.addParameter(Integer.parseInt(parameterValue));
							else if (parameterType.equals("STRING"))
								tag.addParameter(parameterValue);
							else if (parameterType.equals("FLOAT"))
								tag.addParameter(Double.parseDouble(parameterValue));
							else
								log.error("Error during the parsing of the tags, the parameter : "+parameterName+" has an unknown type");
						}
					}
					
					group.addTag(tag);
				}
				deviceType.addGroup(group);
			}
		}
		
		return result;
	}
	
	/**
	 * This function had been provided just in case. Right now, it doesn't do anything. Just call it when you've finished to use the XmlConnector
	 */
	public void close()
	{
		//Nothing to do right now, but just in case
	}
	
	/**
	 * Private function used to read the file and fill the dom.
	 * @return true if the operation worked, false otherwise
	 */
	protected boolean parse()
	{
		if (!(stream instanceof InputStream))
		{
			log.error("XmlConnector is trying to parse a stream which is not deriving from InputStream. Cancelled");
			return false;
		}
		
		try 
		{		
			InputStream is=(InputStream)stream;
			dom=builder.parse(is);
			is.close();	
		} 
		catch (FileNotFoundException e) 
		{
			log.error("Error during the parsing of the xml stream: "+e.getMessage());
			return false;
		}
		catch (SAXException e) 
		{
			log.error("Error during the parsing of the xml stream: "+e.getMessage());
			return false;
		} 
		catch (IOException e) 
		{
			log.error("Error during the parsing of the xml stream: "+e.getMessage());
			return false;
		}
		
		return true;
	}
}
