package com.codecoretechnologies.elvemobile.communication;

public class TouchTcpClientStateChangedEventArgs
{
    public TouchTcpClientState State;

    public TouchTcpClientStateChangedEventArgs(TouchTcpClientState state)
    {
        State = state;
    }
}