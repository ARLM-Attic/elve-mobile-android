package com.codecoretechnologies.elvemobile.communication;

import java.io.IOException;

import android.graphics.Rect;



public class RendererScreenChangePayload implements IBinaryTcpPayload
{
    public Rect Bounds;

    public RendererScreenChangePayload(byte[] data) throws IOException
    {
    	BinaryStreamReader sr = null;
    	try
    	{
        	sr = new BinaryStreamReader(data);
        	
        	Bounds = sr.ReadRectangle();
    	}
    	finally
    	{
    		if (sr != null)
    			sr.close();
    	}
    }

    public RendererScreenChangePayload(Rect rect)
    {
        Bounds = rect;
    }

    public TouchServiceTcpCommunicationPayloadTypes PayloadType()
    {
        return TouchServiceTcpCommunicationPayloadTypes.ScreenChange;
    }

    public byte[] ToByteArray() throws IOException
    {
        BinaryStreamWriter sw = null;
        try
        {
			sw = new BinaryStreamWriter();
			sw.Write(Bounds);

            return sw.ToArray();
        }
        finally
        {
    		if (sw != null)
    			sw.close();        	
        }
    }
}
