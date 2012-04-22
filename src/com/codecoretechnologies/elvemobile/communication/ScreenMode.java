package com.codecoretechnologies.elvemobile.communication;

import java.util.EnumSet;
import java.util.concurrent.ConcurrentMap;

import com.google.common.collect.Maps;


public enum ScreenMode
{
	FullScreen (0),
    Windowed (1),
    Minimized (2),
    ToggleFullscreen (3);
	
	private final static ConcurrentMap<Byte, ScreenMode> values = Maps.newConcurrentMap();

	private final byte value;
    
	static
	{
		for (final ScreenMode x : EnumSet.allOf(ScreenMode.class))
		{
			values.put(x.value, x);
		}
	}

	ScreenMode(final int value)
	{
		this.value = (byte)value;
	}    
	
	public static ScreenMode getFromValue(final byte value)
	{
		return values.get(value);
	}
	    
    public byte getValue()
    {
    	return value;
    }
}

//public class ScreenMode
//{
//	public static final byte FullScreen = 0;
//	public static final byte Windowed = 1;
//	public static final byte Minimized = 2;
//	public static final byte ToggleFullscreen = 3;
//}
