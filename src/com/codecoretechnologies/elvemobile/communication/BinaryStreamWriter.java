package com.codecoretechnologies.elvemobile.communication;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import android.graphics.Rect;
import android.graphics.Point;
import android.graphics.Color;

public class BinaryStreamWriter implements Closeable
{
	ByteArrayOutputStream _bs;
	LEDataOutputStream _os; // Little-endian version of DataOutputStream

    public BinaryStreamWriter()
    {        	
    	_bs = new ByteArrayOutputStream();
    	_os = new LEDataOutputStream(_bs);
    }

    public void Write(Rect rect) throws IOException
    {
        _os.writeShort((short)rect.left);
        _os.writeShort((short)rect.top);
        _os.writeShort((short)rect.width());
        _os.writeShort((short)rect.height());
    }

    public void Write(Point p) throws IOException // used for both points and size/dimensions.
    {
        _os.writeShort((short)p.x);
        _os.writeShort((short)p.y);
    }

//    public void Write(Point d) throws IOException
//    {
//        _os.writeShort((short)d.x);
//        _os.writeShort((short)d.y);
//    }

    public void WriteColor(int c) throws IOException
    {
        _os.writeByte((byte)Color.alpha(c));
        _os.writeByte((byte)Color.red(c));
        _os.writeByte((byte)Color.green(c));
        _os.writeByte((byte)Color.blue(c));
        //_bw.write(c.ToArgb()); // DON'T USE ToArgb() since it doesn't really write the bytes n ARGB order.... It's little endian.
    }

//    public void Write(Font f)
//    {
//        this.WriteString(f.Name); // string
//        this.WriteFloat(f.Size); // float
//        _bw.writeByte((byte)f.Style);
//    }

    public void Write(String s) throws IOException
    {
        _os.writeInt((int)s.length());  //_bw.write(Encoding.UTF8.GetBytes(s)); // no BOM
        _os.write(s.getBytes("UTF-8")); // this produces the same result as .net (no BOM)
        //_os.writeUTF(s);  DON'T use writeUTF() since it uses a different format.
    }

    public void Write(byte b) throws IOException
    {
        _os.writeByte(b);
    }

    public void Write(short i) throws IOException // UInt16 ---- Java doesn't have unsigned types
    {
    	_os.writeShort(i);
    }
    
    public void Write(int i) throws IOException
    {
    	_os.writeInt(i);
    }

//    public void Write(UInt32 i)
//    {
//        _bw.write(i);
//    }
    


    public void Write(float f) throws IOException
    {
        // Based on Reflector this writes out the actual 4 bytes that are stored in memory for the float.
        // Single precision IEEE-754 floating point number
        // in little endian format

        // http://kirkwylie.blogspot.com/2008/11/ieee-754-floating-point-binary.html

        _os.writeFloat(f);
    }

    public void Write(double d) throws IOException
    {
        // Based on Reflector this writes out the actual 8 bytes that are stored in memory for the double.
        // Double precision IEEE-754 floating point number
        // in little endian format

        // http://kirkwylie.blogspot.com/2008/11/ieee-754-floating-point-binary.html

        _os.writeDouble(d);
    }

    public void Write(Boolean b) throws IOException
    {
        _os.writeByte((byte)(b ? 1 : 0)); // don't use underlying write(bool) since I don't know how it is implemented.
    }

//    public void Write(DateTime dt)
//    {
//        // The number of 100-nanosecond intervals that have elapsed since 12:00:00 midnight, January 1, 0001.
//
//        _bw.WriteLong(dt.Ticks); // Int64
//    }
//
//    public void Write(TimeSpan ts)
//    {
//        _bw.WriteLong(ts.Ticks); // Int64
//    }


    public void WriteByteArrayWithLength(byte[] data) throws IOException
    {
        _os.writeInt(data.length);
        _os.write(data);
    }

//    public void Write(Image img)
//    {
//        byte[] b = GraphicsHelper.ImageToByteArray(img, ImageFormat.Png);
//        WriteByteArrayWithLength(b);
//    }

    public byte[] ToArray() throws IOException
    {
        _os.flush();
        return _bs.toByteArray();
    }

	public void close() throws IOException
	{
        if (_os != null)
        {
            _os.flush();
            _os.close();
            _os = null;
        }

        if (_bs != null)
        {
            _bs.flush();
            _bs.close();
            _bs = null;
        }
	}
}



