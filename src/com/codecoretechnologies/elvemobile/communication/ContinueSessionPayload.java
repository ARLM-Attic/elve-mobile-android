package com.codecoretechnologies.elvemobile.communication;

import java.io.IOException;

public class ContinueSessionPayload implements IBinaryTcpPayload
{
    public byte[] SessionID; // added to version Elve 2.0 (however the protocol version has not changed).

// not used in client
//    public ContinueSessionPayload(byte[] data) throws IOException
//    {
//        try (BinaryStreamReader sr = new BinaryStreamReader(data))
//        {
//            SessionID = sr.ReadByteArrayWithLength(); // guid=16 bytes
//        }
//    }

    public ContinueSessionPayload(byte[] sessionID_16bytes, boolean junk)
    {
        SessionID = sessionID_16bytes;
    }

    public TouchServiceTcpCommunicationPayloadTypes PayloadType()
    {
        return TouchServiceTcpCommunicationPayloadTypes.ContinueSession;
    }

    public byte[] ToByteArray() throws IOException
    {
        BinaryStreamWriter sw = null;
        try
        {
        	sw = new BinaryStreamWriter();
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