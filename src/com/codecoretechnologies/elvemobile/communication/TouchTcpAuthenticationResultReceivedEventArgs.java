package com.codecoretechnologies.elvemobile.communication;

import android.graphics.Point;

public class TouchTcpAuthenticationResultReceivedEventArgs
{
    public TouchServiceTcpCommunicationAuthenticationResults Result;
    public Point TouchScreenSize;
    public int BackgroundColor;
    public byte[] SessionID;

    public TouchTcpAuthenticationResultReceivedEventArgs(TouchServiceTcpCommunicationAuthenticationResults result, Point touchScreenSize, int backgroundColor, byte[] sessionID)
    {
        Result = result;
        TouchScreenSize = touchScreenSize;
        BackgroundColor = backgroundColor;
        SessionID = sessionID;
    }
}