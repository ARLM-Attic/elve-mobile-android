package com.codecoretechnologies.elvemobile.communication;

import java.io.IOException;

import android.graphics.Rect;


public class RequestSnapshotPayload implements IBinaryTcpPayload
{
    public Rect Bounds;

    public RequestSnapshotPayload(byte[] data) throws IOException
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

    public RequestSnapshotPayload(Rect bounds)
    {
        Bounds = bounds;
    }

    public TouchServiceTcpCommunicationPayloadTypes PayloadType()
    {
        return TouchServiceTcpCommunicationPayloadTypes.RequestScreenSnapshot;
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
