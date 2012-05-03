package com.codecoretechnologies.elvemobile.communication;

import java.util.EnumSet;
import java.util.concurrent.ConcurrentMap;

import com.google.common.collect.Maps;

public enum TouchTcpImageFormat
{
    Png (0),
    Jpeg (1);
        
	private final static ConcurrentMap<Byte, TouchTcpImageFormat> values = Maps.newConcurrentMap();

	private final byte value;
    
	static
	{
		for (final TouchTcpImageFormat x : EnumSet.allOf(TouchTcpImageFormat.class))
		{
			values.put(x.value, x);
		}
	}

	TouchTcpImageFormat(final int value)
	{
		this.value = (byte)value;
	}    
	
	public static TouchTcpImageFormat getFromValue(final byte value)
	{
		return values.get(value);
	}
	    
    public byte getValue()
    {
    	return value;
    }
}