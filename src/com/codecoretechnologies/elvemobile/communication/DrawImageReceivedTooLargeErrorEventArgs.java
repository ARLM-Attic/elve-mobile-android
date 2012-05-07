package com.codecoretechnologies.elvemobile.communication;

public class DrawImageReceivedTooLargeErrorEventArgs
{
	public boolean CausedByBitmap;
	
	public DrawImageReceivedTooLargeErrorEventArgs(boolean causedByBitmap)
	{
		CausedByBitmap = causedByBitmap;
	}
}
