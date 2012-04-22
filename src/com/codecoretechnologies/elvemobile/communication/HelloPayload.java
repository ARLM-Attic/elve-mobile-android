package com.codecoretechnologies.elvemobile.communication;

import java.io.IOException;

import android.graphics.Point;


public class HelloPayload implements IBinaryTcpPayload
{
    public byte ProtocolVersion; // should be 1 until a breaking change is made.
    public byte ApplicationID = 4; // the ID of the client application.
    public RenderingMode RenderingModeVal;
    public Point ScreenSize;
    public int PixelDepthByteCount; // usually 24 or 32.
    public boolean SupportsAlphaChannel;
    public String UniqueClientID; // The unique identifier of the device.  iphone should use UIDevice.UniqueIdentifier. This is used to help limit the # of touch screens.


	public HelloPayload(byte[] data) throws IOException
    {
    	BinaryStreamReader sr = null;
    	try
    	{
        	sr = new BinaryStreamReader(data);
        	
        	ProtocolVersion = sr.ReadByte();
            ApplicationID = sr.ReadByte();
            this.RenderingModeVal = RenderingMode.getFromValue(sr.ReadByte());
            ScreenSize = sr.ReadSize();
            PixelDepthByteCount = sr.ReadByte();
            SupportsAlphaChannel = sr.ReadBoolean();

            if (data.length > 9)
                UniqueClientID = sr.ReadString();
            else
                UniqueClientID = "";
    	}
    	finally
    	{
    		if (sr != null)
    			sr.close();
    	}
    }

    public HelloPayload(byte protocolVersion, byte applicationID, RenderingMode renderingMode, Point screenSize, int pixelDepthByteCount, boolean supportsAlphaChannel, String uniqueClientID)
    {
        ProtocolVersion = protocolVersion;
        ApplicationID = applicationID;
        RenderingModeVal = renderingMode;
        ScreenSize = screenSize;
        PixelDepthByteCount = pixelDepthByteCount;
        SupportsAlphaChannel = supportsAlphaChannel;
        UniqueClientID = uniqueClientID;
    }

    public TouchServiceTcpCommunicationPayloadTypes PayloadType()
    {
        return TouchServiceTcpCommunicationPayloadTypes.Hello;
    }

    public byte[] ToByteArray() throws IOException
    {
		BinaryStreamWriter sw = null;
        try
        {
        	sw = new BinaryStreamWriter();
        	sw.Write(ProtocolVersion);
            sw.Write((byte)ApplicationID);
            sw.Write(this.RenderingModeVal.getValue());
            sw.Write(ScreenSize);
            sw.Write((byte)PixelDepthByteCount);
            sw.Write(SupportsAlphaChannel);
            sw.Write(UniqueClientID);

            return sw.ToArray();
        }
        finally
        {
    		if (sw != null)
    			sw.close();        	
        }
    }
}
