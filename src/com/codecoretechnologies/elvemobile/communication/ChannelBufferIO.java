package com.codecoretechnologies.elvemobile.communication;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import org.jboss.netty.buffer.ChannelBuffer;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;

public class ChannelBufferIO
{
	public static Rect readRectangle(ChannelBuffer buffer)
    {
    	int x = buffer.readShort(); // 2 bytes
        int y = buffer.readShort(); // 2 bytes
        int width = buffer.readShort(); // 2 bytes
        int height = buffer.readShort(); // 2 bytes
        
        return new Rect(x, y, x + width, y + height);
    }
	
	public static Point readPoint(ChannelBuffer buffer)
    {
        return new Point(
            (int)buffer.readShort(), // 2 bytes
            (int)buffer.readShort()); // 2 bytes
    }

	public static Point readSize(ChannelBuffer buffer)
    {
        return new Point(
            (int)buffer.readShort(), // 2 bytes
            (int)buffer.readShort()); // 2 bytes
    }

	public static int readColor(ChannelBuffer buffer)
    {
    	// Received in this order: argb
    	// Since the values may be > 127 use readUnsignedByte()!!!
    	int a = buffer.readUnsignedByte();
    	int r = buffer.readUnsignedByte();
    	int g = buffer.readUnsignedByte();
    	int b = buffer.readUnsignedByte();

    	return Color.argb(a, r, g, b);
    }

	public static String readString(ChannelBuffer buffer) throws UnsupportedEncodingException
    {
    	int length = buffer.readInt(); // 32 bits

    	byte bytes[] = new byte[length];        	
    	buffer.readBytes(bytes);

    	return new String(bytes, "UTF-8");
    }

//    public byte ReadByte(ChannelBuffer buffer)
//    {
//    	return buffer.readByte();
//    }

	public static short readUInt16(ChannelBuffer buffer) // UInt16 ---- Java doesn't support unsigned types so this returns a short (signed 16bits)
    {
        //return _br.ReadUInt16();
    	return buffer.readShort();
    }

    
	public static int readInt32(ChannelBuffer buffer)
    {	
        return buffer.readInt(); // 32 bits
    }

	public static int readUInt32(ChannelBuffer buffer) // UInt32 ---- Java doesn't support unsigned types so this returns a short (signed 16bits)
    {
        //return _br.ReadUInt32();
        return buffer.readInt(); // 32 bits
    }
    
	public static long readInt64(ChannelBuffer buffer)
    {
    	return buffer.readLong();
    }

	public static float readFloat(ChannelBuffer buffer)
    {
        // Based on Reflector this writes out the actual 4 bytes that are stored in memory for the float.
        // Single precision IEEE-754 floating point number
        // in little endian format

        // http://kirkwylie.blogspot.com/2008/11/ieee-754-floating-point-binary.html

    	return buffer.readFloat();
    }

	public static double readDouble(ChannelBuffer buffer)
    {
        // Based on Reflector this writes out the actual 8 bytes that are stored in memory for the double.
        // Double precision IEEE-754 floating point number
        // in little endian format

        // http://kirkwylie.blogspot.com/2008/11/ieee-754-floating-point-binary.html

    	return buffer.readDouble();
    }

	public static boolean readBoolean(ChannelBuffer buffer)
    {
        return (boolean)(buffer.readByte() != 0); // buffer.readBoolean() may use a different format so be explicit here. 
    }

	public static byte[] readByteArrayWithLength(ChannelBuffer buffer)
    {
    	int length = readInt32(buffer);  // 32 bits
    	byte b[] = new byte[length];
    	buffer.readBytes(b);        	
    	return b;
    }

	public static Bitmap readImage(ChannelBuffer buffer) throws IOException
    {
        byte[] b = readByteArrayWithLength(buffer);

        InputStream in = null;
        try
        {
        	in = new ByteArrayInputStream(b);

        	// You can reduce an images quality or scale(?) by setting a sample size if loading the bitmap throws an OutOfMemoryError.
        	// see http://stackoverflow.com/questions/477572/android-strange-out-of-memory-issue-while-loading-an-image-to-a-bitmap-object/823966#823966
        	//BitmapFactory.Options o = new BitmapFactory.Options();
        	//o.inSampleSize = 2;
        	//Bitmap image = BitmapFactory.decodeByteArray(b, 0, b.length, o);

        	Bitmap image = BitmapFactory.decodeByteArray(b, 0, b.length);
        	return image;
        }
//        catch (OutOfMemoryError ex)
//        {
//        	Log.e("READIMAGE", "ERROR occured in ReadImage().", ex);
//        }
        finally
        {
        	if (in != null)
        		in.close();
        }
    }

	public static int readUnsignedByte(ChannelBuffer buffer)
	{
		return buffer.readUnsignedByte();
	}
}
