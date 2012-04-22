package com.codecoretechnologies.elvemobile.communication;

import android.graphics.Point;

public class TouchTcpAuthenticationResultReceivedEventArgs
{
    public TouchServiceTcpCommunicationAuthenticationResults Result;
    public Point TouchScreenSize;
    public byte[] SessionID;

    public TouchTcpAuthenticationResultReceivedEventArgs(TouchServiceTcpCommunicationAuthenticationResults result, Point touchScreenSize, byte[] sessionID)
    {
        Result = result;
        TouchScreenSize = touchScreenSize;
        SessionID = sessionID;
    }
}