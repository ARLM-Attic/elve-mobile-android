package com.codecoretechnologies.elvemobile.communication;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.jboss.netty.buffer.ChannelBuffer;

public class ErrorPayload implements IBinaryTcpPayload
{
    public String Title;
    public String Message;
    public String ExceptionTypeName;
    public String Source;

    public ErrorPayload(ChannelBuffer buffer) throws UnsupportedEncodingException
    {
    	Title = ChannelBufferIO.readString(buffer);
        Message = ChannelBufferIO.readString(buffer);
        ExceptionTypeName = ChannelBufferIO.readString(buffer);
        Source = ChannelBufferIO.readString(buffer);
    }
    
//    public ErrorPayload(byte[] data) throws IOException
//    {
//    	BinaryStreamReader sr = null;
//    	try
//    	{
//        	sr = new BinaryStreamReader(data);
//        	
//        	Title = sr.ReadString();
//            Message = sr.ReadString();
//            ExceptionTypeName = sr.ReadString();
//            Source = sr.ReadString();
//    	}
//    	finally
//    	{
//    		if (sr != null)
//    			sr.close();
//    	}
//    }

    public ErrorPayload(String title, String exceptionTypeName, String message, String source)
    {
        Title = title;
        Message = message;
        ExceptionTypeName = exceptionTypeName;
        Source = source;
    }

    public TouchServiceTcpCommunicationPayloadTypes PayloadType()
    {
        return TouchServiceTcpCommunicationPayloadTypes.Error;
    }

    public byte[] ToByteArray() throws IOException
    {
		BinaryStreamWriter sw = null;
        try
        {
        	sw = new BinaryStreamWriter();
        	sw.Write(Title);
            sw.Write(Message);
            sw.Write(ExceptionTypeName);
            sw.Write(Source);

            return sw.ToArray();
        }
        finally
        {
    		if (sw != null)
    			sw.close();        	
        }
    }
}
