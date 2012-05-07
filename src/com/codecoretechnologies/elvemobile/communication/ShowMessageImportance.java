package com.codecoretechnologies.elvemobile.communication;

import java.util.EnumSet;
import java.util.concurrent.ConcurrentMap;

import com.google.common.collect.Maps;

public enum ShowMessageImportance
{
	Normal(0),
	Important(1);

	private final static ConcurrentMap<Byte, ShowMessageImportance> values = Maps.newConcurrentMap();

	private final byte value;
	
	static
	{
		for (final ShowMessageImportance x : EnumSet.allOf(ShowMessageImportance.class))
		{
			values.put(x.value, x);
		}
	}
	
	ShowMessageImportance(final int value)
	{
		this.value = (byte)value;
	}
	
	public static ShowMessageImportance getFromValue(final byte value)
	{
		return values.get(value);
	}
	
	public byte getValue()
	{
		return value;
	}
}

