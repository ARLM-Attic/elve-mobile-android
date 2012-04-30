package com.codecoretechnologies.elvemobile.communication;

import java.io.IOException;

import org.jboss.netty.buffer.ChannelBuffer;

public class RendererHeader
{
    public final static int RENDERERHEADERBYTELENGTH = 11;

    public byte BeginningOfHeader; // 0;
    public short SequenceNumber; // a rolling incremental number (circular)   ---- UInt16 Java doesn't have unsigned types
    public TouchServiceTcpCommunicationPayloadTypes PayloadType;  // this is like a command id and therefore indicates the format of the payload.
    public int PayloadLength; // ---- UInt32 Java doesn't have unsigned types
    public byte PayloadChecksum; // the checksum of just the payload.
    public byte HeaderChecksum; // includes all of header except this checksum and EndOfHeader
    public byte EndOfHeader; // 13;

    public RendererHeader(short sequenceNumber, TouchServiceTcpCommunicationPayloadTypes payloadType, int payloadLength, byte payloadChecksum) throws IOException
    {
        this.BeginningOfHeader = 0; // always 0.
        this.SequenceNumber = sequenceNumber;
        this.PayloadType = payloadType;
        this.PayloadLength = payloadLength;
        this.PayloadChecksum = payloadChecksum;
        this.HeaderChecksum = 0;
        this.EndOfHeader = 13; // always 13.

        this.HeaderChecksum = calculateHeaderChecksum();
    }
    
    public RendererHeader(ChannelBuffer buffer) throws IOException 
    {
        this.BeginningOfHeader = buffer.readByte();
        this.SequenceNumber = buffer.readShort(); // UInt16 ---- Java doesn't support signed types
        this.PayloadType = TouchServiceTcpCommunicationPayloadTypes.getFromValue(buffer.readByte());
        this.PayloadLength = buffer.readInt(); // UInt32 ---- Java doesn't support signed types
        this.PayloadChecksum = buffer.readByte();
        this.HeaderChecksum = buffer.readByte();
        this.EndOfHeader = buffer.readByte();
    }

// old, replaced with channel buffer    
//    public RendererHeader(byte[] data) throws IOException 
//    {
//    	BinaryStreamReader sr = null;
//    	try
//    	{
//        	sr = new BinaryStreamReader(data);
//        	
//            this.BeginningOfHeader = sr.ReadByte();
//            this.SequenceNumber = sr.ReadUInt16(); // UInt16 ---- Java doesn't support signed types
//            this.PayloadType = TouchServiceTcpCommunicationPayloadTypes.getFromValue(sr.ReadByte());
//            this.PayloadLength = sr.ReadUInt32(); // UInt32 ---- Java doesn't support signed types
//            this.PayloadChecksum = sr.ReadByte();
//            this.HeaderChecksum = sr.ReadByte();
//            this.EndOfHeader = sr.ReadByte();
//    	}
//    	finally
//    	{
//    		if (sr != null)
//    			sr.close();
//    	}
//    }

    public boolean IsValid() throws IOException
    {
        if (BeginningOfHeader != 0 ||
            EndOfHeader != 13 ||
            calculateHeaderChecksum() != HeaderChecksum)
            return false;
        else 
            return true;
    }

    private byte calculateHeaderChecksum() throws IOException
    {
        byte[] b = this.ToByteArray();
        return Checksum.CalculateByteChecksum(b, 0, 9); // everything except the checksum byte and end of record byte.
    }

    public byte[] ToByteArray() throws IOException
    {
    	BinaryStreamWriter sw = null;
        try
        {
			sw = new BinaryStreamWriter();
            sw.Write(BeginningOfHeader);
            sw.Write(SequenceNumber);
            sw.Write(PayloadType.getValue());
            sw.Write(PayloadLength);
            sw.Write(PayloadChecksum);
            sw.Write(HeaderChecksum);
            sw.Write(EndOfHeader);

            return sw.ToArray();
        }
        finally
        {
    		if (sw != null)
    			sw.close();        	
        }
    }

    public byte[] ToByteArray(boolean allocatePayloadBytes) throws IOException
    {
        byte[] headerBytes = this.ToByteArray();

        byte[] data = new byte[headerBytes.length + this.PayloadLength];

        System.arraycopy(headerBytes, 0, data, 0, headerBytes.length);
        
        return data;
    }

    public byte[] ToByteArray(byte[] payloadToAppendToHeader) throws IOException
    {
        byte[] headerBytes = this.ToByteArray();

        byte[] data = new byte[headerBytes.length + this.PayloadLength];

        System.arraycopy(headerBytes, 0, data, 0, headerBytes.length);
        System.arraycopy(payloadToAppendToHeader, 0, data, headerBytes.length, (int)this.PayloadLength);
        
        return data;
    }
}
