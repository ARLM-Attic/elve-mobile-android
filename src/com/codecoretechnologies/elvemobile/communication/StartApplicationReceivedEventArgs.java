package com.codecoretechnologies.elvemobile.communication;

public class StartApplicationReceivedEventArgs
{
    public StartApplicationType ApplicationType;
    public String CommandLine;

    public StartApplicationReceivedEventArgs(StartApplicationType applicationType, String commandLine)
    {
        ApplicationType = applicationType;
        CommandLine = commandLine;
    }
}