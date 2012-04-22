package com.codecoretechnologies.elvemobile.communication;

import android.graphics.Point;

public class ContinueSessionResultReceivedEventArgs
{
    public ContinueSessionResults Result;
    public Point TouchScreenSize;

    public ContinueSessionResultReceivedEventArgs(ContinueSessionResults result, Point touchScreenSize)
    {
        Result = result;
        TouchScreenSize = touchScreenSize;
    }
}