package com.codecoretechnologies.elvemobile.communication;

import java.util.EnumSet;
import java.util.concurrent.ConcurrentMap;

import com.google.common.collect.Maps;

public enum StartApplicationType
{
	DefaultWebBrowser (0);
        
	private final static ConcurrentMap<Integer, StartApplicationType> values = Maps.newConcurrentMap();

	private final int value;
    
	static
	{
		for (final StartApplicationType x : EnumSet.allOf(StartApplicationType.class))
		{
			values.put(x.value, x);
		}
	}

	StartApplicationType(final int value)
	{
		this.value = value;
	}    
	
	public static StartApplicationType getFromValue(final int value)
	{
		return values.get(value);
	}
	    
    public int getValue()
    {
    	return value;
    }
}