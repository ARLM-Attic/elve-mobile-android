package com.codecoretechnologies.elvemobile.communication;

public enum TouchTcpClientState {
	AttemptingToConnect,
	AttemptingToReconnect,
    Authenticating,
    Authenticated,
    FailedAuthentication
}
