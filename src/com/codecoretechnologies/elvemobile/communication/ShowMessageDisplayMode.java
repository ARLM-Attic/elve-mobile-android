package com.codecoretechnologies.elvemobile.communication;

import java.util.EnumSet;
import java.util.concurrent.ConcurrentMap;

import com.google.common.collect.Maps;

public enum ShowMessageDisplayMode
{
	NonIntrusive(0),
	RequiresAcknowledgment(1);

	private final static ConcurrentMap<Byte, ShowMessageDisplayMode> values = Maps.newConcurrentMap();

	private final byte value;
	
	static
	{
		for (final ShowMessageDisplayMode x : EnumSet.allOf(ShowMessageDisplayMode.class))
		{
			values.put(x.value, x);
		}
	}
	
	ShowMessageDisplayMode(final int value)
	{
		this.value = (byte)value;
	}
	
	public static ShowMessageDisplayMode getFromValue(final byte value)
	{
		return values.get(value);
	}
	
	public byte getValue()
	{
		return value;
	}
}
