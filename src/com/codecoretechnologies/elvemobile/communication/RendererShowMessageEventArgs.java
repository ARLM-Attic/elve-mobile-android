package com.codecoretechnologies.elvemobile.communication;

public class RendererShowMessageEventArgs
{
	public ShowMessageDisplayMode DisplayMode;
    public String Title;
    public String Message;

    public RendererShowMessageEventArgs(ShowMessageDisplayMode displayMode, String title, String message)
    {
    	DisplayMode = displayMode;
        Title = title;
        Message = message;
    }
}
