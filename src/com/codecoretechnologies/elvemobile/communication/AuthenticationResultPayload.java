package com.codecoretechnologies.elvemobile.communication;

import java.io.IOException;

import android.graphics.Point;


public class AuthenticationResultPayload implements IBinaryTcpPayload
{
    public TouchServiceTcpCommunicationAuthenticationResults AuthenticationResult;
    public Point TouchScreenSize;
    public int BackgroundColor;
    public byte[] SessionID; // added to version Elve 2.0 (however the protocol version has not changed).

    public AuthenticationResultPayload(byte[] data) throws IOException
    {
    	BinaryStreamReader sr = null;
    	try
    	{
    		sr = new BinaryStreamReader(data);
    		AuthenticationResult = TouchServiceTcpCommunicationAuthenticationResults.getFromValue(sr.ReadByte());
            TouchScreenSize = sr.ReadSize();
            BackgroundColor = sr.ReadColor();

            // Elve 2.0 added a new session id which we can detect using the payload data length.
            SessionID = null;
            if (data.length > 9)
            {
            	byte[] sessionID = sr.ReadByteArrayWithLength(); // guid=16 bytes
            	// If the session id is all zeros then there is no session id.
            	for (int i=0; i< sessionID.length; i++)
            	{
            		if (sessionID[i] != 0)
            		{
            			SessionID = sessionID;
            			break;
            		}
            	}
            }
    	}
    	finally
    	{
    		if (sr != null)
    			sr.close();
    	}
    }

    public AuthenticationResultPayload(TouchServiceTcpCommunicationAuthenticationResults authenticationResult, Point touchScreenSize, int backgroundColor, byte[] sessionID_16bytes)
    {
        AuthenticationResult = authenticationResult;
        TouchScreenSize = touchScreenSize;
        BackgroundColor = backgroundColor;
        SessionID = sessionID_16bytes;
    }

    public TouchServiceTcpCommunicationPayloadTypes PayloadType()
    {
        return TouchServiceTcpCommunicationPayloadTypes.AuthenticationResult;
    }

    public byte[] ToByteArray() throws IOException
    {
        BinaryStreamWriter sw = null;
        try
        {
        	sw = new BinaryStreamWriter();
        	sw.Write(AuthenticationResult.getValue());
            sw.Write(TouchScreenSize);
            sw.Write(BackgroundColor);
            sw.WriteByteArrayWithLength(SessionID); // guid=16 bytes

            return sw.ToArray();
        }
        finally
        {
    		if (sw != null)
    			sw.close();        	
        }
    }
}
