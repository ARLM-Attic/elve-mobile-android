package com.codecoretechnologies.elvemobile.communication;

import android.graphics.Bitmap;
import android.graphics.Rect;


public class DrawImageReceivedEventArgs
{
    public Rect Bounds;
    public float Opacity; // send 0-255 (but our variable really holds 0.0-1.0).
    public byte SizeMode;
    public Bitmap Image;

    public DrawImageReceivedEventArgs(Rect bounds, float opacity, byte sizeMode, Bitmap img)
    {
        Bounds = bounds;
        Opacity = opacity;
        SizeMode = sizeMode;
        Image = img;
    }
}
