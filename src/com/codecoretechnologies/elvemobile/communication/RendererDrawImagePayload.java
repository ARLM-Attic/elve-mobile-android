package com.codecoretechnologies.elvemobile.communication;

import java.io.Closeable;
import java.io.IOException;

import org.jboss.netty.buffer.ChannelBuffer;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.util.Log;

public class RendererDrawImagePayload implements IBinaryTcpPayload, Closeable
{
    public Rect Bounds;
    public int Opacity; // 0-255
    public ImageSizeMode SizeMode;
    public Bitmap Image; // null if the image could not be processed or it has been disposed.

    public RendererDrawImagePayload(ChannelBuffer buffer) throws IOException
    {
    	Bounds = ChannelBufferIO.readRectangle(buffer);
        Opacity = buffer.readUnsignedByte();
        SizeMode = ImageSizeMode.getFromValue(buffer.readByte());
        Image = ChannelBufferIO.readImage(buffer);
        
        if (Image == null) // failed to process the image data
        	Log.d("RendererDrawImagePayload", "Failed to process image data for size: " + Bounds.width() + "x" + Bounds.height());
    }

//    public RendererDrawImagePayload(byte[] data) throws IOException
//    {
//    	BinaryStreamReader sr = null;
//    	try
//    	{
//        	sr = new BinaryStreamReader(data);
//        	
//            Bounds = sr.ReadRectangle();
//            Opacity = sr.ReadUnsignedByte();
//            SizeMode = ImageSizeMode.getFromValue(sr.ReadByte());
//            Image = sr.ReadImage();
//    	}
//    	finally
//    	{
//    		if (sr != null)
//    			sr.close();
//    	}
//    }

    public RendererDrawImagePayload(Rect bounds, int opacity, ImageSizeMode sizeMode, Bitmap img)
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
		if (Image != null)
		{
			if (Image.isRecycled() == false)
				Image.recycle(); // release any memory used by the bitmap right now.
			Image = null;
		}
	}
}
