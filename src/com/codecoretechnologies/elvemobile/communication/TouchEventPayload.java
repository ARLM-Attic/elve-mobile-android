package com.codecoretechnologies.elvemobile.communication;

import java.io.IOException;

import android.graphics.Point;


public class TouchEventPayload implements IBinaryTcpPayload
{
    public TouchEventType EventType;
    public Point Location;

    public TouchEventPayload(byte[] data) throws IOException
    {
    	BinaryStreamReader sr = null;
    	try
    	{
        	sr = new BinaryStreamReader(data);
        	
        	EventType = TouchEventType.getFromValue(sr.ReadByte());
            Location = sr.ReadPoint();
    	}
    	finally
    	{
    		if (sr != null)
    			sr.close();
    	}
    }

    public TouchEventPayload(TouchEventType eventType, Point location)
    {
        EventType = eventType;
        Location = location;
    }

    public TouchServiceTcpCommunicationPayloadTypes PayloadType()
    {
        return TouchServiceTcpCommunicationPayloadTypes.TouchEvent;
    }

    public byte[] ToByteArray() throws IOException
    {
    	BinaryStreamWriter sw = null;
        try
        {
			sw = new BinaryStreamWriter();
			sw.Write(EventType.getValue());
            sw.Write(Location);

            return sw.ToArray();
        }
        finally
        {
    		if (sw != null)
    			sw.close();        	
        }
    }
}
