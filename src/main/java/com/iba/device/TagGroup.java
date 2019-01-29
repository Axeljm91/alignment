package com.iba.device;

import java.util.Comparator;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Vector;

public class TagGroup 
{
	private DeviceType deviceType;
	private Vector<String> parametersName;
	private Vector<String> parametersType;
	
	private SortedSet<Tag> tags;
	private TagGroupType type;
	
	public TagGroup(TagGroupType tagGroupType,DeviceType deviceType)
	{
		this.deviceType=deviceType;
		this.type=tagGroupType;
		this.parametersName=new Vector<String>();
		this.parametersType=new Vector<String>();
		
		tags=new TreeSet<Tag>(new Comparator<Tag>() 
				{
			@Override
			public int compare(Tag tag1, Tag tag2) 
			{
				return tag1.getName().compareToIgnoreCase(tag2.getName());
			}
		});
	}
	
	public void addParameterName(int index,String name,String type)
	{
		parametersName.add(index,name);
		parametersType.add(index,type);		
	}

	public void addTag(Tag tag) 
	{
		tags.add(tag);
	}

	public DeviceType getDeviceType()
	{
		return deviceType;
	}

	public int getNbParametersName()
	{
		return parametersName.size();
	}
	
	protected int getParameterIndex(String name) 
	{
		return parametersName.indexOf(name);
	}
	
	public String getParameterName(int index)
	{
		return parametersName.get(index);
	}

	public String getParameterType(int index) 
	{
		return parametersType.get(index);
	}
	
	public Tag getTagFromName(String name) 
	{
		for (Tag tag:tags)
		{
			if (tag.getName().equals(name))
				return tag;
		}
		return null;	
	}
	
	public SortedSet<Tag> getTags() 
	{
		return tags;
	}

	public Object[] getTags(int start, int limit, String filter) 
	{
		filter=filter.toUpperCase();
		
		Vector<Tag> filteredTags=new Vector<Tag>(tags.size());
		
		for (Tag tag:tags)
		{
			if(isTagMatchingFilter(tag, filter))
				filteredTags.add(tag);
		}
				
		Object[] result=new Object[2];
		
		
		if (start+limit<=filteredTags.size())
			result[0]=filteredTags.subList(start, start+limit);
		else if(filteredTags.size()!=0)
			result[0]=filteredTags.subList(start, filteredTags.indexOf(filteredTags.lastElement())+1);
		else
			result[0]=null;
		
		result[1]=filteredTags.size();
		return result;
	}
	
	public TagGroupType getType()
	{
		return type;
	}
	
	private boolean isTagMatchingFilter(Tag tag,String filterString)
	{
		String description=tag.getDescription().toUpperCase();
		String id=tag.getName().toUpperCase();
		
		String[] filter=filterString.split(" ");
		
		for (String filterToken:filter)
		{
			if ((!description.contains(filterToken))&&((!id.contains(filterToken))))
				return false;
				break;
		}
		
		return true;
	}
}
