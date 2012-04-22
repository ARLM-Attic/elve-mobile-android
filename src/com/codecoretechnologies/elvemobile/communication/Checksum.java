package com.codecoretechnologies.elvemobile.communication;

public class Checksum
{
	public static byte CalculateByteChecksum(byte[] b)
    {
        return CalculateByteChecksum(b, 0, b.length);
    }
    public static byte CalculateByteChecksum(byte[] b, int startIndex, int length)
    {
        // Returns byte checksum. This is the two's complement of
        // the modulo-8bit sum of the byte array.

        long sum = 0;
        for (int i = startIndex; i < startIndex + length; i++)
            sum += (long)b[i];

        sum = -(sum % (0xFF + 1));

        return (byte)sum;
    }
}
