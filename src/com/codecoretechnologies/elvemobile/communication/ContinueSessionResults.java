package com.codecoretechnologies.elvemobile.communication;

import java.util.EnumSet;
import java.util.concurrent.ConcurrentMap;

import com.google.common.collect.Maps;

public enum ContinueSessionResults
{
    Success (0),
    Invalid (1);
        
	private final static ConcurrentMap<Byte, ContinueSessionResults> values = Maps.newConcurrentMap();

	private final byte value;
    
	static
	{
		for (final ContinueSessionResults x : EnumSet.allOf(ContinueSessionResults.class))
		{
			values.put(x.value, x);
		}
	}

	ContinueSessionResults(final int value)
	{
		this.value = (byte)value;
	}    
	
	public static ContinueSessionResults getFromValue(final byte value)
	{
		return values.get(value);
	}
	    
    public byte getValue()
    {
    	return value;
    }
}
//public class ContinueSessionResults
//{
//	public static final byte Success = 0;
//	public static final byte Invalid = 1;
//}