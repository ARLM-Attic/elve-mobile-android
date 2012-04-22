package com.codecoretechnologies.elvemobile.communication;

import java.io.Closeable;
import java.io.IOException;

import android.graphics.Bitmap;
import android.graphics.Rect;

public class RendererDrawImagePayload implements IBinaryTcpPayload, Closeable
{
    public Rect Bounds;
    public float Opacity; // send 0-255 (but our variable really holds 0.0-1.0).
    public byte SizeMode;
    public Bitmap Image;

    public RendererDrawImagePayload(byte[] data) throws IOException
    {
    	BinaryStreamReader sr = null;
    	try
    	{
        	sr = new BinaryStreamReader(data);
        	
            Bounds = sr.ReadRectangle();
            Opacity = (float)(sr.ReadByte() / 255.0);
            SizeMode = sr.ReadByte();
            Image = sr.ReadImage();
    	}
    	finally
    	{
    		if (sr != null)
    			sr.close();
    	}
    }

    public RendererDrawImagePayload(Rect bounds, float opacity, byte sizeMode, Bitmap img)
    {
        Bounds = bounds;
        Opacity = opacity;
        SizeMode = sizeMode;
        Image = img;
    }

    public TouchServiceTcpCommunicationPayloadTypes PayloadType()
    {
        return TouchServiceTcpCommunicationPayloadTypes.DrawImage;
    }

    public byte[] ToByteArray() throws IOException
    {
    	return null;
    	
// not used in client
//		BinaryStreamWriter sw = null;
//        try
//        {
//			sw = new BinaryStreamWriter();
//     	    sw.Write(Bounds);
//          sw.Write((byte)(Opacity * 255)); // send 0-255 (but our variable really holds 0.0-1.0).
//          sw.Write((byte)SizeMode);
//          sw.Write(Image);
//
//            return sw.ToArray();
//        }
//        finally
//        {
//    		if (sw != null)
//    			sw.close();        	
//        }
    }


	public void close()
	{
		Image = null;
	}
   
}