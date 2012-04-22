package com.codecoretechnologies.elvemobile.communication;

import java.io.IOException;

public interface IBinaryTcpPayload
{
	TouchServiceTcpCommunicationPayloadTypes PayloadType();
    byte[] ToByteArray() throws IOException;
}
