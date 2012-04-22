package com.codecoretechnologies.elvemobile.communication;

import java.util.EnumSet;
import java.util.concurrent.ConcurrentMap;

import com.google.common.collect.Maps;


public enum TouchServiceTcpCommunicationPayloadTypes
{
    Ping (0),                   // used to test the network connection. No payload.
    Pong (1),                   // just used as a response to the ping command. No Payload.

    
    Error (2),
    Disconnect (3),             // Tells the client or server to close the connection. No Payload.

    Hello (4),                  // Client->Server: requests authentication token and specifies simple or full rendering, connection info like alpha channel support, native screen resolution and pixel bit depth.  Must be received within 30 seconds or the connection with drop.
    AuthenticateChallenge (5),  // Server->Client: asking to authenticate
    Authenticate (6),           // Client->Server: includes authentication info.
    AuthenticationResult (7),   // Server->Client: indicates if authentication was successful.

    ShowMessage (8),            // Server->Client: The client application should show a message.
    ChangeScreenMode (9),       // Server->Client: The client app should change it's screen mode.
    Exit (10),                  // Server->Client: The client app should close. (Usually from an Exit button press),.

    TouchEvent (11),            // Client->Server: the screen was touched, or the touch removed from the screen, or the touch was moved on the screen

    RequestRedraw (12),         // Client->Server: requests that draw command be sent from the server for an area of the screen.
    RequestScreenSnapshot (13), // Client->Server: requests a section of the current screen as an image. The server responds with a DrawImage command.
    RequestNamedImage (14),     // Client->Server: requests a named image. The server responds with a NamedImage command.

    NamedImage (15),            // Server->Client: This is the image data for the requested named image.

    StartDrawing (16),          // Server->Client: Indicates that a section of the screen is starting to be drawn (use double buffering to avoid flicker),. Includes the initial clipping area.
    EndDrawing (17),            // Server->Client: Indicates that a section of the screen has completed drawing. The clipping stack should be cleared and the final double buffered image should be painted.
    ScreenChange (18),          // Server->Client: Indicates that a section of the screen changed. This is only send for the Snapshot Rendering Mode.

    PushClippingArea (19),      // Server->Client: Pushes a graphics clipping area (Set, Intersect), Exclude), onto the stack.
    PopClippingArea (20),       // Server->Client: Pops the most recent clipping area off the stack.

    FillRectangle (21),               // Server->Client: draw a solid color filled rectangle
    FillLinearGradientRectangle (22), // Server->Client: draw a linear gradient filled rectangle
    DrawText (23),                    // Server->Client: draw text
    DrawBorder (24),                  // Server->Client: draw a border
    DrawImage (25),                   // Server->Client: draw an image... payload includes the png image
    DrawNamedImage (26),              // Server->Client: draw a named image
    DrawLine (27),                    // Server->Client: draw a line

    LocationChangeEvent (28),   // Server->Client: The viewer device's location changed.

    // Session support was added in Elve 2.0
    ContinueSession (29),              // Client->Server: Requests the continuing a previously disconnected session. The session may have already expired.
    ContinueSessionResult (30);        // Server->Client: indicates if the session could be continued.

    // TODO: need throttling commands and maybe a message command    
    
    private final static ConcurrentMap<Byte, TouchServiceTcpCommunicationPayloadTypes> values = Maps.newConcurrentMap();

	private final byte value;
    
	static
	{
		for (final TouchServiceTcpCommunicationPayloadTypes x : EnumSet.allOf(TouchServiceTcpCommunicationPayloadTypes.class))
		{
			values.put(x.value, x);
		}
	}

	TouchServiceTcpCommunicationPayloadTypes(final int value)
	{
		this.value = (byte)value;
	}    
	
	public static TouchServiceTcpCommunicationPayloadTypes getFromValue(final byte value)
	{
		return values.get(value);
	}
	    
    public byte getValue()
    {
    	return value;
    }
}

//public class TouchServiceTcpCommunicationPayloadTypes
//{
//	public static final byte Ping = 0;                   // used to test the network connection. No payload.
//	public static final byte Pong = 1;                   // just used as a response to the ping command. No Payload.
//
//    
//	public static final byte Error = 2;
//	public static final byte Disconnect = 3;             // Tells the client or server to close the connection. No Payload.
//
//	public static final byte Hello = 4;                  // Client->Server: requests authentication token and specifies simple or full rendering, connection info like alpha channel support, native screen resolution and pixel bit depth.  Must be received within 30 seconds or the connection with drop.
//	public static final byte AuthenticateChallenge = 5;  // Server->Client: asking to authenticate
//	public static final byte Authenticate = 6;           // Client->Server: includes authentication info.
//	public static final byte AuthenticationResult = 7;   // Server->Client: indicates if authentication was successful.
//
//	public static final byte ShowMessage = 8;            // Server->Client: The client application should show a message.
//	public static final byte ChangeScreenMode = 9;       // Server->Client: The client app should change it's screen mode.
//	public static final byte Exit = 10;                  // Server->Client: The client app should close. (Usually from an Exit button press),.
//
//	public static final byte TouchEvent = 11;            // Client->Server: the screen was touched, or the touch removed from the screen, or the touch was moved on the screen
//
//	public static final byte RequestRedraw = 12;         // Client->Server: requests that draw command be sent from the server for an area of the screen.
//	public static final byte RequestScreenSnapshot = 13; // Client->Server: requests a section of the current screen as an image. The server responds with a DrawImage command.
//	public static final byte RequestNamedImage = 14;     // Client->Server: requests a named image. The server responds with a NamedImage command.
//
//	public static final byte NamedImage = 15;            // Server->Client: This is the image data for the requested named image.
//
//	public static final byte StartDrawing = 16;          // Server->Client: Indicates that a section of the screen is starting to be drawn (use double buffering to avoid flicker),. Includes the initial clipping area.
//	public static final byte EndDrawing = 17;            // Server->Client: Indicates that a section of the screen has completed drawing. The clipping stack should be cleared and the final double buffered image should be painted.
//	public static final byte ScreenChange = 18;          // Server->Client: Indicates that a section of the screen changed. This is only send for the Snapshot Rendering Mode.
//
//	public static final byte PushClippingArea = 19;      // Server->Client: Pushes a graphics clipping area (Set, Intersect), Exclude), onto the stack.
//	public static final byte PopClippingArea = 20;       // Server->Client: Pops the most recent clipping area off the stack.
//
//	public static final byte FillRectangle = 21;               // Server->Client: draw a solid color filled rectangle
//	public static final byte FillLinearGradientRectangle = 22; // Server->Client: draw a linear gradient filled rectangle
//	public static final byte DrawText = 23;                    // Server->Client: draw text
//	public static final byte DrawBorder = 24;                  // Server->Client: draw a border
//	public static final byte DrawImage = 25;                   // Server->Client: draw an image... payload includes the png image
//	public static final byte DrawNamedImage = 26;              // Server->Client: draw a named image
//	public static final byte DrawLine = 27;                    // Server->Client: draw a line
//
//	public static final byte LocationChangeEvent = 28;   // Server->Client: The viewer device's location changed.
//
//    // Session support was added in Elve 2.0
//	public static final byte ContinueSession = 29;              // Client->Server: Requests the continuing a previously disconnected session. The session may have already expired.
//	public static final byte ContinueSessionResult = 30;        // Server->Client: indicates if the session could be continued.
//
//    // TODO: need throttling commands and maybe a message command    
//}
