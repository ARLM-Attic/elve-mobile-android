package com.codecoretechnologies.elvemobile.communication;

import android.graphics.Bitmap;
import android.graphics.Rect;


public class DrawImageReceivedEventArgs
{
    public Rect Bounds;
    public int Opacity; // 0-255
    public ImageSizeMode SizeMode;
    public Bitmap Image;

    public DrawImageReceivedEventArgs(Rect bounds, int opacity, ImageSizeMode sizeMode, Bitmap img)
    {
        Bounds = bounds;
        Opacity = opacity;
        SizeMode = sizeMode;
        Image = img;
    }
}
