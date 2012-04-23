package com.codecoretechnologies.elvemobile.communication;

import java.util.EnumSet;
import java.util.concurrent.ConcurrentMap;

import com.google.common.collect.Maps;

public enum ImageSizeMode
{
	/**
     * The image is placed in the upper-left corner of the System.Windows.Forms.PictureBox.
     * The image is clipped if it is larger than the System.Windows.Forms.PictureBox
     * it is contained in.
     */
    Normal(0),
    /**
    * The image within the System.Windows.Forms.PictureBox is stretched or shrunk
    * to fit the size of the System.Windows.Forms.PictureBox.
    */
    StretchImage(1),
    /**
    * The System.Windows.Forms.PictureBox is sized equal to the size of the image
    * that it contains. This means that the control's actual bounds are changed to
    * be equal to the size of the image.
    */
    AutoSize(2),
    /**
    * The image is displayed in the center if the System.Windows.Forms.PictureBox
    * is larger than the image. If the image is larger than the System.Windows.Forms.PictureBox,
    * the picture is placed in the center of the System.Windows.Forms.PictureBox
    * and the outside edges are clipped.
    */
    CenterImage(3),
    /**
    * The size of the image is increased or decreased maintaining the size ratio.
    */
    Zoom(4);

	private final static ConcurrentMap<Byte, ImageSizeMode> values = Maps.newConcurrentMap();

	private final byte value;
	
	static
	{
		for (final ImageSizeMode x : EnumSet.allOf(ImageSizeMode.class))
		{
			values.put(x.value, x);
		}
	}
	
	ImageSizeMode(final int value)
	{
		this.value = (byte)value;
	}
	
	public static ImageSizeMode getFromValue(final byte value)
	{
		return values.get(value);
	}
	
	public byte getValue()
	{
		return value;
	}
}
