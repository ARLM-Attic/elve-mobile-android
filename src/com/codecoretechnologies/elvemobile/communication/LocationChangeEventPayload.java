package com.codecoretechnologies.elvemobile.communication;

import java.io.IOException;

public class LocationChangeEventPayload implements IBinaryTcpPayload
{
    public double Latitude;
    public double Longitude;
    public double Altitude;
    public double Course;
    public double SpeedMetersPerSecond;

    public LocationChangeEventPayload(byte[] data) throws IOException
    {
    	BinaryStreamReader sr = null;
    	try
    	{
        	sr = new BinaryStreamReader(data);
        	
            Latitude = sr.ReadDouble();
            Longitude = sr.ReadDouble();
            Altitude = sr.ReadDouble();
            Course = sr.ReadDouble();
            SpeedMetersPerSecond = sr.ReadDouble();
    	}
    	finally
    	{
    		if (sr != null)
    			sr.close();
    	}
    }

    public LocationChangeEventPayload(double latitude, double longitude, double altitude, double course, double speedMetersPerSecond)
    {
        Latitude = latitude;
        Longitude = longitude;
        Altitude = altitude;
        Course = course;
        SpeedMetersPerSecond = speedMetersPerSecond;
    }

    public TouchServiceTcpCommunicationPayloadTypes PayloadType()
    {
        return TouchServiceTcpCommunicationPayloadTypes.LocationChangeEvent;
    }

    public byte[] ToByteArray() throws IOException
    {
		BinaryStreamWriter sw = null;
        try
        {
        	sw = new BinaryStreamWriter();
            sw.Write(Latitude);
            sw.Write(Longitude);
            sw.Write(Altitude);
            sw.Write(Course);
            sw.Write(SpeedMetersPerSecond);

            return sw.ToArray();
        }
        finally
        {
    		if (sw != null)
    			sw.close();        	
        }
    }
}
