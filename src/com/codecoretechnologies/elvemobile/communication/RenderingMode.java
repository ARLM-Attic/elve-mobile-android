package com.codecoretechnologies.elvemobile.communication;

import java.util.EnumSet;
import java.util.concurrent.ConcurrentMap;

import com.google.common.collect.Maps;

public enum RenderingMode
{
	Snapshots(0),     // Only prerendered snapshots are sent
	DrawCommands(1);  // Each draw command is sent.

	private final static ConcurrentMap<Byte, RenderingMode> values = Maps.newConcurrentMap();

	private final byte value;
	
	static
	{
		for (final RenderingMode x : EnumSet.allOf(RenderingMode.class))
		{
			values.put(x.value, x);
		}
	}
	
	RenderingMode(final int value)
	{
		this.value = (byte)value;
	}
	
	public static RenderingMode getFromValue(final byte value)
	{
		return values.get(value);
	}
	
	public byte getValue()
	{
		return value;
	}
}


//public enum RenderingMode {
//	 Snapshots (0), // Only prerendered snapshots are sent
//	 DrawCommands (1); // Each draw command is sent.
//	 
//    private int value;
//    private RenderingMode(int value)
//    {
//    	this.value = value;
//    }
//    
//    private static final Map<Integer, RenderingMode> intToTypeMap = new HashMap<Integer, RenderingMode>();
//    static
//    {
//        for (RenderingMode type : RenderingMode.values())
//        {
//            intToTypeMap.put(type.value, type);
//        }
//    }
//
//    public static RenderingMode fromByte(byte b)
//    {
//    	RenderingMode type = intToTypeMap.get(Integer.valueOf(b));
//        if (type == null) 
//            return RenderingMode.Snapshots; //TODO: what to return if unknown
//        return type;
//    }
//}

//public class RenderingMode
//{
//	public static final byte Snapshots = 0; // Only prerendered snapshots are sent
//	public static final byte DrawCommands = 1; // Each draw command is sent.
//}
