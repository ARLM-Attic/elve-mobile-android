package com.codecoretechnologies.elvemobile.communication;

import java.io.IOException;

public class AuthenticatePayload implements IBinaryTcpPayload
{
    public String Username;
    public byte[] Token; // Authentication Token from client in hex
    public byte[] Hash;  // resulting hash

    public AuthenticatePayload(byte[] data) throws IOException
    {
    	BinaryStreamReader sr = null;
    	try
    	{
        	sr = new BinaryStreamReader(data);
        	
            Username = sr.ReadString();
            Token = sr.ReadByteArrayWithLength();
            Hash = sr.ReadByteArrayWithLength();
    	}
    	finally
    	{
    		if (sr != null)
    			sr.close();
    	}
    }

    public AuthenticatePayload(String username, byte[] token, byte[] hash)
    {
        Username = username;
        Token = token;
        Hash = hash;
    }

    public TouchServiceTcpCommunicationPayloadTypes PayloadType()
    {
        return TouchServiceTcpCommunicationPayloadTypes.Authenticate;
    }

    public byte[] ToByteArray() throws IOException
    {
    	BinaryStreamWriter sw = null;
        try
        {
        	sw = new BinaryStreamWriter();
            sw.Write(Username);
            sw.WriteByteArrayWithLength(Token);
            sw.WriteByteArrayWithLength(Hash);

            return sw.ToArray();
        }
        finally
        {
    		if (sw != null)
    			sw.close();        	
        }
    }
}
