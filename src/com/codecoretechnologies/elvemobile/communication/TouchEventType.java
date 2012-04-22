package com.codecoretechnologies.elvemobile.communication;

import java.util.EnumSet;
import java.util.concurrent.ConcurrentMap;

import com.google.common.collect.Maps;


public enum TouchEventType
{
    /// <summary>Occurs immediately when the screen is touched.</summary>
    TouchDown (0),
    /// <summary>Occurs when the screen is touched and held for a 1/4 second, or when it is released (whichever occurs first).</summary>
    Touched (1),
    /// <summary>Occurs when the user released their touch from the screen.</summary>
    TouchUp (2),
    /// <summary>Occurs when the control is pressed twice in a short period of time.</summary>
    DoubleTouch (3),
    /// <summary>The user has moved their finger across the screen.</summary>
    TouchMove (4);
    
    private final static ConcurrentMap<Byte, TouchEventType> values = Maps.newConcurrentMap();

	private final byte value;
    
	static
	{
		for (final TouchEventType x : EnumSet.allOf(TouchEventType.class))
		{
			values.put(x.value, x);
		}
	}

	TouchEventType(final int value)
	{
		this.value = (byte)value;
	}    
	
	public static TouchEventType getFromValue(final byte value)
	{
		return values.get(value);
	}
	    
    public byte getValue()
    {
    	return value;
    }
}

//public class TouchEventType
//{
//	/// <summary>Occurs immediately when the screen is touched.</summary>
//	public static final byte TouchDown = 0;
//	/// <summary>Occurs when the screen is touched and held for a 1/4 second, or when it is released (whichever occurs first).</summary>
//	public static final byte Touched = 1;
//	/// <summary>Occurs when the user released their touch from the screen.</summary>
//	public static final byte TouchUp = 2;
//	/// <summary>Occurs when the control is pressed twice in a short period of time.</summary>
//	public static final byte DoubleTouch = 3;
//	/// <summary>The user has moved their finger across the screen.</summary>
//	public static final byte TouchMove = 4;
//}
