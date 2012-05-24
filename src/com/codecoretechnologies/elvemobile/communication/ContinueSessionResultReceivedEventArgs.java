package com.codecoretechnologies.elvemobile.communication;

import android.graphics.Point;

public class ContinueSessionResultReceivedEventArgs
{
    public ContinueSessionResults Result;
    public Point TouchScreenSize;
    public int BackgroundColor;

    public ContinueSessionResultReceivedEventArgs(ContinueSessionResults result, Point touchScreenSize, int backgroundColor)
    {
        Result = result;
        TouchScreenSize = touchScreenSize;
        BackgroundColor = backgroundColor;
    }
}
