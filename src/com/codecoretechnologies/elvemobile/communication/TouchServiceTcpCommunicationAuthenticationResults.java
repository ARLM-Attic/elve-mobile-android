package com.codecoretechnologies.elvemobile.communication;

import java.util.EnumSet;
import java.util.concurrent.ConcurrentMap;

import com.google.common.collect.Maps;


public enum TouchServiceTcpCommunicationAuthenticationResults
{
	Success (0),
    Invalid (1),
    Disabled (2),
    BlockedByTimeRestrictions (3);
	
	private final static ConcurrentMap<Byte, TouchServiceTcpCommunicationAuthenticationResults> values = Maps.newConcurrentMap();

	private final byte value;
    
	static
	{
		for (final TouchServiceTcpCommunicationAuthenticationResults x : EnumSet.allOf(TouchServiceTcpCommunicationAuthenticationResults.class))
		{
			values.put(x.value, x);
		}
	}

	TouchServiceTcpCommunicationAuthenticationResults(final int value)
	{
		this.value = (byte)value;
	}    
	
	public static TouchServiceTcpCommunicationAuthenticationResults getFromValue(final byte value)
	{
		return values.get(value);
	}
	    
    public byte getValue()
    {
    	return value;
    }
}

//public class TouchServiceTcpCommunicationAuthenticationResults
//{
//	public static final byte Success = 0;
//	public static final byte Invalid = 1;
//	public static final byte Disabled = 2;
//	public static final byte BlockedByTimeRestrictions = 3;
//}
