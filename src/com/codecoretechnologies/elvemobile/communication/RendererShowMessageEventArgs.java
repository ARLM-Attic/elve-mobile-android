package com.codecoretechnologies.elvemobile.communication;

public class RendererShowMessageEventArgs
{
	public ShowMessageDisplayMode DisplayMode;
	public ShowMessageImportance Importance;
    public String Title;
    public String Message;

    public RendererShowMessageEventArgs(ShowMessageDisplayMode displayMode, ShowMessageImportance importance, String title, String message)
    {
    	DisplayMode = displayMode;
    	Importance = importance;
        Title = title;
        Message = message;
    }
}
