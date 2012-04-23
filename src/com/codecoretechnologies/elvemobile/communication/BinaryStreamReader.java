package com.codecoretechnologies.elvemobile.communication;

import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import android.graphics.*;

public class BinaryStreamReader implements Closeable
{
	ByteArrayInputStream _bs;
	LEDataInputStream _is; // Little-endian version of DataInputStream

    public BinaryStreamReader(byte[] data)
    {	
    	this(new ByteArrayInputStream(data));
    }

    public BinaryStreamReader(ByteArrayInputStream bs)
    {
        _bs = bs;
        _is = new LEDataInputStream(_bs);
    }

    public Rect ReadRectangle() throws IOException
    {
        return new Rect(
            (int)_is.readShort(), // 2 bytes
            (int)_is.readShort(), // 2 bytes
            (int)_is.readShort(), // 2 bytes
            (int)_is.readShort()); // 2 bytes
    }

    public Point ReadPoint() throws IOException
    {
        return new Point(
            (int)_is.readShort(), // 2 bytes
            (int)_is.readShort()); // 2 bytes
    }

    public Point ReadSize() throws IOException
    {
        return new Point(
            (int)_is.readShort(), // 2 bytes
            (int)_is.readShort()); // 2 bytes
    }

    public int ReadColor() throws IOException
    {
    	// Received in this order: argb
    	// Since the values may be > 127 use readUnsignedByte()!!!
    	int a = _is.readUnsignedByte();
    	int r = _is.readUnsignedByte();
    	int g = _is.readUnsignedByte();
    	int b = _is.readUnsignedByte();

    	return Color.argb(a, r, g, b);
    }

    public String ReadString() throws IOException
    {
    	int length = _is.readInt(); // 32 bits
    	
    	byte bytes[] = new byte[length];        	
    	_is.read(bytes, 0, length);
    	
    	return new String(bytes, "UTF-8");
    }

    public byte ReadByte() throws IOException
    {
    	return _is.readByte();
    }

    public short ReadUInt16() throws IOException // UInt16 ---- Java doesn't support unsigned types so this returns a short (signed 16bits)
    {
        //return _br.ReadUInt16();
    	return _is.readShort();
    }

    
    public int ReadInt32() throws IOException
    {	
        return _is.readInt(); // 32 bits
    }

    public int ReadUInt32() throws IOException // UInt32 ---- Java doesn't support unsigned types so this returns a short (signed 16bits)
    {
        //return _br.ReadUInt32();
        return _is.readInt(); // 32 bits
    }
    
    public long ReadInt64() throws IOException
    {
    	return _is.readLong();
    }

    public float ReadFloat() throws IOException
    {
        // Based on Reflector this writes out the actual 4 bytes that are stored in memory for the float.
        // Single precision IEEE-754 floating point number
        // in little endian format

        // http://kirkwylie.blogspot.com/2008/11/ieee-754-floating-point-binary.html

    	return _is.readFloat();
    }

    public double ReadDouble() throws IOException
    {
        // Based on Reflector this writes out the actual 8 bytes that are stored in memory for the double.
        // Double precision IEEE-754 floating point number
        // in little endian format

        // http://kirkwylie.blogspot.com/2008/11/ieee-754-floating-point-binary.html

    	return _is.readDouble();
    }

    public boolean ReadBoolean() throws IOException
    {
        return (boolean)(_is.readByte() != 0); // _is.readBoolean() may use a different format so be explicit here. 
    }

    public byte[] ReadByteArrayWithLength() throws IOException
    {
    	int length = ReadInt32();  // 32 bits
    	byte b[] = new byte[length];
    	_is.read(b, 0, length);        	
    	return b;
    }

    public Bitmap ReadImage() throws IOException
    {
        byte[] b = ReadByteArrayWithLength();

        InputStream in = null;
        try
        {
        	in = new ByteArrayInputStream(b);
        	Bitmap image = BitmapFactory.decodeByteArray(b, 0, b.length);
        	return image;
        }
        finally
        {
        	if (in != null)
        		in.close();
        }
    }

	public int ReadUnsignedByte() throws IOException
	{
		return _is.readUnsignedByte();
	}
	
	public void close() throws IOException
	{
		if(_is != null)
    	{
    		_is.close();
    		_is = null;
    	}
    	
    	if (_bs != null)
    	{
    		_bs.close();
    		_bs = null;
    	}
	}
}
