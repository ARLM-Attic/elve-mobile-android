package com.codecoretechnologies.elvemobile.communication;

import java.io.IOException;

import org.jboss.netty.buffer.ChannelBuffer;

public class AuthenticationChallengePayload implements IBinaryTcpPayload
{
    public byte[] AuthenticationToken;

    public AuthenticationChallengePayload(ChannelBuffer buffer)
    {
    	AuthenticationToken = ChannelBufferIO.readByteArrayWithLength(buffer);
    }

//    public AuthenticationChallengePayload(byte[] data) throws IOException
//    {
//    	BinaryStreamReader sr = null;
//    	try
//    	{
//        	sr = new BinaryStreamReader(data);
//        	
//        	AuthenticationToken = sr.ReadByteArrayWithLength();
//    	}
//    	finally
//    	{
//    		if (sr != null)
//    			sr.close();
//    	}
//    }

    public AuthenticationChallengePayload(byte[] authenticationToken, boolean junk)
    {
        AuthenticationToken = authenticationToken;
    }

    public TouchServiceTcpCommunicationPayloadTypes PayloadType()
    {
        return TouchServiceTcpCommunicationPayloadTypes.AuthenticateChallenge;
    }

    public byte[] ToByteArray() throws IOException
    {
        BinaryStreamWriter sw = null;
        try
        {
        	sw = new BinaryStreamWriter();
        	sw.WriteByteArrayWithLength(AuthenticationToken);

            return sw.ToArray();
        }
        finally
        {
    		if (sw != null)
    			sw.close();        	
        }
    }
}
