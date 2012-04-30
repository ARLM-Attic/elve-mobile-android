package com.codecoretechnologies.elvemobile.communication;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.jboss.netty.buffer.ChannelBuffer;

public class RendererShowMessagePayload implements IBinaryTcpPayload
{
    public byte DisplayMode;
    public String Text;

    public RendererShowMessagePayload(ChannelBuffer buffer) throws UnsupportedEncodingException
    {
    	DisplayMode = buffer.readByte();
        Text = ChannelBufferIO.readString(buffer);
    }
    
//    public RendererShowMessagePayload(byte[] data) throws IOException
//    {
//    	BinaryStreamReader sr = null;
//    	try
//    	{
//        	sr = new BinaryStreamReader(data);
//        	
//        	DisplayMode = sr.ReadByte();
//            Text = sr.ReadString();
//    	}
//    	finally
//    	{
//    		if (sr != null)
//    			sr.close();
//    	}
//    }

    public RendererShowMessagePayload(byte displayMode, String text)
    {
        DisplayMode = displayMode;
        Text = text;
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
			sw.Write((byte)DisplayMode);
            sw.Write(Text);

            return sw.ToArray();
        }
        finally
        {
    		if (sw != null)
    			sw.close();        	
        }
    }
}
