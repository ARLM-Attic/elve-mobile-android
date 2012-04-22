package com.codecoretechnologies.elvemobile.communication;

import java.io.IOException;

import android.graphics.Point;


public class ContinueSessionResultPayload implements IBinaryTcpPayload
{
    public ContinueSessionResults Result;
    public Point TouchScreenSize;
    public int BackgroundColor;

    public ContinueSessionResultPayload(byte[] data) throws IOException
    {
    	BinaryStreamReader sr = null;
    	try
    	{
        	sr = new BinaryStreamReader(data);
        	
        	Result = ContinueSessionResults.getFromValue(sr.ReadByte());
            TouchScreenSize = sr.ReadSize();
            BackgroundColor = sr.ReadColor();
    	}
    	finally
    	{
    		if (sr != null)
    			sr.close();
    	}
    }

    public ContinueSessionResultPayload(ContinueSessionResults continueSessionResult, Point touchScreenSize, int backgroundColor)
    {
        Result = continueSessionResult;
        TouchScreenSize = touchScreenSize;
        BackgroundColor = backgroundColor;
    }

    public TouchServiceTcpCommunicationPayloadTypes PayloadType()
    {
        return TouchServiceTcpCommunicationPayloadTypes.ContinueSessionResult;
    }

    public byte[] ToByteArray() throws IOException
    {
		BinaryStreamWriter sw = null;
        try
        {
			sw = new BinaryStreamWriter();
			sw.Write(Result.getValue());
            sw.Write(TouchScreenSize);
            sw.WriteColor(BackgroundColor);

            return sw.ToArray();
        }
        finally
        {
    		if (sw != null)
    			sw.close();        	
        }
    }
}
