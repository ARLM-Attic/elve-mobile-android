//package com.codecoretechnologies.elvemobile.communication;
//
//import java.util.EnumSet;
//import java.util.Map;
//import com.google.common.collect.Maps;
//
//public class EnumResolver
//{
//	private static Map<Class><? extends Enum><?>>, Map<Integer, Enum><?>>> enumMap = Maps.newHashMap();
//	
//	public static void main(final String[] args)
//	{
//		registerEnum(RenderingMode.class);
//		System.out.println(resolve(RenderingMode.class, 1));
//		System.out.println(resolve(RenderingMode.class, 0));
//	}
//	
//	public static <E extends Enum><E> & ValueEnum> void registerEnum(final Class<E> elementType)
//	{
//		final Map<Integer, Enum><?>> typeMap = Maps.newHashMap();
//	
//		for (final E valueEnum : EnumSet.allOf(elementType))
//		{
//			typeMap.put(valueEnum.getValue(), valueEnum);
//		}
//		
//		enumMap.put(elementType, typeMap);
//	}
//	
//	public static <E extends Enum><E>> E resolve(final Class<E> type, final int value)
//	{
//		final Map<Integer, Enum><?>> typeMap = enumMap.get(type);
//		return (E) typeMap.get(value);
//	}
//}