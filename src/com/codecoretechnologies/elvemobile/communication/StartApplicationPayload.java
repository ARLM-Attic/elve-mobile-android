package com.codecoretechnologies.elvemobile.communication;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.jboss.netty.buffer.ChannelBuffer;


public class StartApplicationPayload implements IBinaryTcpPayload
{
    public StartApplicationType ApplicationType;
    public String CommandLine;

    public StartApplicationPayload(ChannelBuffer buffer) throws UnsupportedEncodingException
    {
        ApplicationType = StartApplicationType.getFromValue(ChannelBufferIO.readInt32(buffer));
        CommandLine = ChannelBufferIO.readString(buffer);
    }

    public StartApplicationPayload(StartApplicationType applicationType, String commandLine)
    {
        ApplicationType = applicationType;
        CommandLine = commandLine;
    }

    public TouchServiceTcpCommunicationPayloadTypes PayloadType()
    {
        return TouchServiceTcpCommunicationPayloadTypes.StartApplication;
    }

    public byte[] ToByteArray() throws IOException
    {
    	BinaryStreamWriter sw = null;
        try
        {
        	sw = new BinaryStreamWriter();
        	sw.Write(ApplicationType.getValue());
            sw.Write(CommandLine);

            return sw.ToArray();
        }
        finally
        {
    		if (sw != null)
    			sw.close();        	
        }

    }
}