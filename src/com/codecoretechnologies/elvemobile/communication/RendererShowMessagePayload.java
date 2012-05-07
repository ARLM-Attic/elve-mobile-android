package com.codecoretechnologies.elvemobile.communication;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.jboss.netty.buffer.ChannelBuffer;

public class RendererShowMessagePayload implements IBinaryTcpPayload
{
	public ShowMessageDisplayMode DisplayMode;
	public ShowMessageImportance Importance;
    public String Title;
    public String Message;

    public RendererShowMessagePayload(ChannelBuffer buffer) throws UnsupportedEncodingException
    {
    	DisplayMode = ShowMessageDisplayMode.getFromValue(buffer.readByte());
    	Message = ChannelBufferIO.readString(buffer);
    	if (buffer.readableBytes() > 0) // title was added after Elve version 1.5
    	{
    		Title = ChannelBufferIO.readString(buffer);
    		Importance = ShowMessageImportance.getFromValue(buffer.readByte());
    	}
    	else
    	{
    		Title = "";
    		Importance = ShowMessageImportance.Normal;
    	}
    }

    public RendererShowMessagePayload(ShowMessageDisplayMode displayMode, ShowMessageImportance importance, String title, String message)
    {
    	DisplayMode = displayMode;
        Title = title;
        Message = message;
        Importance = importance;
    }

    public TouchServiceTcpCommunicationPayloadTypes PayloadType()
    {
        return TouchServiceTcpCommunicationPayloadTypes.ShowMessage;
    }

    public byte[] ToByteArray() throws IOException
    {
		BinaryStreamWriter sw = null;
        try
        {
        	sw = new BinaryStreamWriter();
        	sw.Write(DisplayMode.getValue());
            sw.Write(Message);
        	sw.Write(Title);
        	sw.Write(Importance.getValue());
        	
            return sw.ToArray();
        }
        finally
        {
    		if (sw != null)
    			sw.close();        	
        }
    }
}
