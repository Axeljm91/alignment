/*
* Application : LookupAnalyser
* Filename : Tag.java
* Author : Henrotin S�bastien
* Date : 06/04/2010
* Company : IBA
* Version : 0.1
*/

package com.iba.device;

import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public class Tag 
{
	/**
	 * The logger. See http://logging.apache.org/log4j/1.2/index.html for more information.
	 */
	private static Logger log=Logger.getLogger(new Throwable().getStackTrace()[0].getClassName());

	private String description;
	private TagGroup group;
	private int id;
	
	private String name;
	private Vector<Object> parameters;

	private Map<String,Object> dynamicParameters;
	
	private TagType type;
	
	public Tag(String name,String description,TagType type, TagGroup group)
	{
		this.name=name;
		this.type=type;
		this.description=description;
		this.group=group;
		this.parameters=new Vector<Object>();
		this.dynamicParameters=new HashMap<String,Object>();
	}
	
	public void addParameter(Object parameter)
	{
		this.parameters.add(parameter);
	}
	
	public double convertValueInDouble(Object value) 
	{
		if (value==null)
		{
			log.warn("Trying to convert null to double");
			return 0;
		}
		
		switch (this.type)
		{
		case INT:
		case WORD:
		case INT32:
		case UINT8:
		case UINT16:
			return (Integer)value;
		case FLOAT:
			return (Double)value;
		case BOOLEAN:
		case BOOL:
			return (Boolean)value?1.0:0.0;
		case BYTE:
			return (Byte)value;
		case DINT:
			return (Long)value;
		case STRING:
		case IPADDR:
		case OTHER:
		case TIME:
		case DATE:
			log.error("Impossible to convert this data: "+value+" into a double. Tag:"+this);
			return 0;
		case UINT32:
			return (Long)value;
		case INT8:
		case INT16:
			return (Short)value;
		case REAL:
			return (Float)value;
		}
		
		log.error("This TagType is not known. Impossible to make the conversion to double. "+value);
		return 0;
	}
	
	public String getDescription() 
	{
		return description;
	}
	
	public TagGroup getGroup()
	{
		return group;
	}

	public int getId() 
	{
		return id;
	}
	

	public String getName() 
	{
		return name;
	}	

	public Object getParameter(int index)
	{
		return this.parameters.get(index);
	}
	
	public Object getParameter(String name)
	{
		int index=group.getParameterIndex(name);
		if (index==-1||index>=parameters.size())
			return null;
		else
			return parameters.get(index);
	}

	public TagType getType() 
	{
		return type;
	}
	
	public void setId(int id)
	{
		this.id=id;
	}
	
	public Map<String,Object> getDynamicParameters()
	{
		return dynamicParameters;
	}

	@Override
	public String toString() 
	{
		StringBuffer result=new StringBuffer();
		result.append("[Tag : ");
		result.append("name:"+name+",");
		result.append("description:"+description+",");
		result.append("type:"+type.toString());
		result.append("]");
		return result.toString();
	}
}
