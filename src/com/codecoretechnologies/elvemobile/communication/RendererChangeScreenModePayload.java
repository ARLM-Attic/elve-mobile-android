package com.codecoretechnologies.elvemobile.communication;

import java.io.IOException;

public class RendererChangeScreenModePayload implements IBinaryTcpPayload
{
    public ScreenMode ScreenModeVal;

    public RendererChangeScreenModePayload(byte[] data) throws IOException
    {
    	BinaryStreamReader sr = null;
    	try
    	{
        	sr = new BinaryStreamReader(data);
        	
        	ScreenModeVal = ScreenMode.getFromValue(sr.ReadByte());
    	}
    	finally
    	{
    		if (sr != null)
    			sr.close();
    	}
    }

    public RendererChangeScreenModePayload(ScreenMode screenMode)
    {
    	ScreenModeVal = screenMode;
    }

    public TouchServiceTcpCommunicationPayloadTypes PayloadType()
    {
        return TouchServiceTcpCommunicationPayloadTypes.ChangeScreenMode;
    }

    public byte[] ToByteArray() throws IOException
    {
		BinaryStreamWriter sw = null;
        try
        {
        	sw = new BinaryStreamWriter();
        	sw.Write(ScreenModeVal.getValue());

            return sw.ToArray();
        }
        finally
        {
    		if (sw != null)
    			sw.close();        	
        }
    }
}
