package com.codecoretechnologies.elvemobile.communication;


import java.io.Closeable;
import java.io.IOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.nio.channels.UnresolvedAddressException;
import java.security.MessageDigest;
//import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.timeout.ReadTimeoutException;
import org.jboss.netty.util.Timeout;
import org.jboss.netty.util.Timer;
import org.jboss.netty.util.TimerTask;

import android.graphics.Point;
import android.graphics.Rect;
import android.util.Log;

import com.google.common.eventbus.EventBus;

/**
 * Keep reconnecting to the server while printing out the current uptime and
 * connection attempt status.
 */
public class UptimeClientHandler extends SimpleChannelUpstreamHandler implements Closeable
{

    private final ClientBootstrap _bootstrap;
    private final Timer _timer;
    private long _startTime = -1;
    private final String _username;
    private final String _password;
    private byte[] _sessionID;
    private final Point _screenSize;
    private final String _deviceID;
    private final EventBus _eventBus;
    
    Channel _channel; // gets set after connecting.
    
    private final AtomicLong transferredBytes = new AtomicLong(); // the number of transfered bytes

    ScheduledExecutorService _screenChangeTimer;
    List<Rect> _screenChanges = new ArrayList<Rect>();

	private boolean _isClosed = false;
	private boolean _failedAuthentication;
	
    public UptimeClientHandler(ClientBootstrap bootstrap, Timer timer, String username, String password, byte[] sessionID, String deviceID, Point screenSize, EventBus eventBus) {
        this._bootstrap = bootstrap;
        this._timer = timer;
        this._username = username;
        this._password = password;
        this._sessionID = sessionID;
        this._deviceID = deviceID;
        this._screenSize = screenSize;
        this._eventBus = eventBus;
    }

    InetSocketAddress getRemoteAddress()
    {
        return (InetSocketAddress) _bootstrap.getOption("remoteAddress");
    }
        
    @Override
    public void channelDisconnected(ChannelHandlerContext ctx, ChannelStateEvent e)
    {
    	// Invoked when a Channel was disconnected from its remote peer.
        println("Disconnected from: " + getRemoteAddress());
    }

    @Override
    public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e)
    {
    	// Invoked when a Channel was closed and all its related resources were released.
    	
    	if (_isClosed == false && _failedAuthentication == false) // I had to add this because to prevent reconnect AND because the event trigger below caused the call to bootstrap.releaseExternalResources() to hang.
    	{
    		_eventBus.post(new TouchTcpClientStateChangedEventArgs(TouchTcpClientState.AttemptingToReconnect));

	    	// Clear our incoming data buffer.
	    	_incomingBuffer.clear();

	        println("Sleeping for: " + UptimeClient.RECONNECT_DELAY + "s");
	        _timer.newTimeout(new TimerTask()
	        {
	            public void run(Timeout timeout) //throws Exception
	            {
	                println("Reconnecting to: " + getRemoteAddress());
	                _bootstrap.connect();
	            }
	        }, UptimeClient.RECONNECT_DELAY, TimeUnit.SECONDS);
    	}
    }

    @Override
    public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e)
    {
    	// Invoked when a Channel is open, bound to a local address, and connected to a remote address.

        if (_startTime < 0)
            _startTime = System.currentTimeMillis();

        println("Connected to: " + getRemoteAddress());
        
        _channel = e.getChannel();

        _eventBus.post(new TouchTcpClientStateChangedEventArgs(TouchTcpClientState.Authenticating));

        // If there is no previous session then send new hello message.
        if (_sessionID == null)
        {
			try
			{
				sendHello(e.getChannel());
			}
			catch (Exception e1)
			{
				e1.printStackTrace();
			}
        }
		else
        {
			// Attempt to continue previous session.
    		ContinueSessionPayload contSess = new ContinueSessionPayload(_sessionID, false);

    		try
    		{
				sendMessage(contSess, e.getChannel());
			}
    		catch (Exception e1)
    		{
				e1.printStackTrace();
			}
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e)
    {
    	// Invoked when an exception was raised by an I/O thread or a ChannelHandler.
    	
        Throwable cause = e.getCause();
        if (cause instanceof ConnectException) // this happens when the ip address is wrong or it just can't connect to a valid address
        {
            _startTime = -1;
            println("Failed to connect: " + cause.getMessage());
        }
        else if (cause instanceof UnresolvedAddressException)
        {
        	_eventBus.post(new TouchTcpClientUnresolvedAddressExceptionEventArgs());
        	
        	_startTime = -1;
        	println("Failed to connect: " + cause.getMessage());
        }
        else if (cause instanceof ReadTimeoutException)
        {
            // The connection was OK but there was no traffic for last period.
            //println("Disconnecting due to no inbound traffic");
        	println("Sending ping since no inbound traffic in a while.");
            try
			{
				sendMessage(TouchServiceTcpCommunicationPayloadTypes.Ping, null, ctx.getChannel());
				return; // don't close the channel.
			}
			catch (IOException e1)
			{
				e1.printStackTrace();
			}
        }
        else
        {
            cause.printStackTrace();
        }
        ctx.getChannel().close();
    }

    void println(String msg) {
        if (_startTime < 0) {
        	Log.d("", "[SERVER IS DOWN] " + msg);
            //System.err.format("[SERVER IS DOWN] %s%n", msg);
        } else {
        	Log.d("", String.format("[UPTIME: %5ds] %s%n", (System.currentTimeMillis() - _startTime) / 1000, msg));
            //System.err.format("[UPTIME: %5ds] %s%n", (System.currentTimeMillis() - _startTime) / 1000, msg);
        }
    }
    
    
    
    
    
    List<Byte> _incomingBuffer = new ArrayList<Byte>(); // TODO: use something more efficient, maybe ByteArrayOutputStream?
    RendererHeader _rendererHeader;
    boolean _haveRendererHeader = false;

    
    @Override
    public void messageReceived(ChannelHandlerContext ctx, org.jboss.netty.channel.MessageEvent e) throws IOException
    {
    	// Invoked when a message object (e.g: ChannelBuffer) was received from a remote peer.
    	
    	// Send back the received message to the remote peer.
    	ChannelBuffer msg = (ChannelBuffer)e.getMessage();
    	
    	int byteCount = msg.readableBytes();
    	transferredBytes.addAndGet(byteCount);
    	
    	// Read bytes
    	byte[] b = new byte[byteCount];
    	msg.getBytes(0, b);

    	// Add bytes to buffer
    	for (int i = 0; i < b.length; i++)
    		_incomingBuffer.add(b[i]);





    	while (_isClosed == false)
        {
            //******************************************************************
            // Find the start of the next packet.
            //******************************************************************
            if (_haveRendererHeader == false)
            {
            	RendererHeader rendererHeader = getNextValidRendererHeader();
                if (rendererHeader == null)
                    return; // no header is available yet

                _rendererHeader = rendererHeader;
                _haveRendererHeader = true;
            }
            

            //******************************************************************
            // Read and process the payload.
            //******************************************************************
            if (_incomingBuffer.size() < _rendererHeader.PayloadLength)
                return; // the full payload has not been received yet

            //******************************************************************
            // Read Payload
            //******************************************************************
            // TODO: THERE HAS GOT TO BE A BETTER WAY IN JAVA TO DO THIS!!!
            byte[] payload = new byte[_rendererHeader.PayloadLength];
            for (int i = 0; i < _rendererHeader.PayloadLength; i++)
            {
            	payload[i] = _incomingBuffer.get(0);
            	_incomingBuffer.remove(0); // Remove read byte from buffer
            }

            

            //******************************************************************
            // MessageReceived Event
            //******************************************************************
            onMessageReceived(_rendererHeader.SequenceNumber, _rendererHeader.PayloadType, payload, e.getChannel());

            _haveRendererHeader = false;
        }
    }

	private RendererHeader getNextValidRendererHeader() throws IOException
    {
        int HL = RendererHeader.RENDERERHEADERBYTELENGTH;

        while (true)
        {
            if (_incomingBuffer.size() < HL)
                break;

            long bolPos = _incomingBuffer.indexOf((byte)0);
            if (bolPos < 0 || _incomingBuffer.size() < bolPos + HL)
                break;

        	// Extract header bytes to temp buffer
            // TODO: THERE HAS GOT TO BE A BETTER WAY IN JAVA TO DO THIS!!!
            byte[] b = new byte[HL];
        	for (int i = 0; i < b.length; i++)
        	{
        		b[i] = _incomingBuffer.get(0);
        		_incomingBuffer.remove(0);
        	}
        	
            RendererHeader readHeader = new RendererHeader(b);
            if (readHeader.IsValid())
            {
                return readHeader;
            }
        }

        return null;
    }
    
    
    
    public long getTransferredBytes()
    {
    	return transferredBytes.get();
    }


    /**
     * Returns a little endian byte array representation of the full message (header + payload).
     * @throws IOException 
     */
    public byte[] generateMessage(IBinaryTcpPayload payload) throws IOException
    {
    	return generateMessage(payload.PayloadType(), payload.ToByteArray());
    }
    private static short _nextSequenceNumber = 0; // UInt16 - Java doesn't have unsigned types.
    /**
     * Returns a little endian byte array representation of the full message (header + payload).
     * @throws IOException 
     */
    public byte[] generateMessage(TouchServiceTcpCommunicationPayloadTypes payloadType, byte[] payload) throws IOException
    {
        if (payload == null)
            payload = new byte[0];

        // Calculate the payload checksum.
        Byte payloadChecksum = Checksum.CalculateByteChecksum(payload);

        RendererHeader header = new RendererHeader(_nextSequenceNumber, payloadType, (int)payload.length, payloadChecksum);

        byte[] data = header.ToByteArray(payload);

        // Increment the sequence number.
        if (_nextSequenceNumber == Short.MAX_VALUE)
            _nextSequenceNumber = 0;
        else
            _nextSequenceNumber++;

        return data;
    }

    /**
     * Generates a full message (header + payload) and writes it to the channel.
     * @throws IOException 
     */
    public void sendMessage(IBinaryTcpPayload payload) throws IOException
    {
    	sendMessage(payload.PayloadType(), payload.ToByteArray(), _channel);
    }
    /**
     * Generates a full message (header + payload) and writes it to the channel.
     * @throws IOException 
     */
    public void sendMessage(IBinaryTcpPayload payload, Channel channel) throws IOException
    {
    	sendMessage(payload.PayloadType(), payload.ToByteArray(), channel);
    }
    /**
     * Generates a full message (header + payload) and writes it to the channel.
     * @throws IOException 
     */
    private void sendMessage(TouchServiceTcpCommunicationPayloadTypes payloadType, byte[] payload, Channel channel) throws IOException
    {
    	byte[] msg = generateMessage(payloadType, payload);
		
    	ChannelBuffer buf = ChannelBuffers.copiedBuffer(msg);	
    	
		channel.write(buf);
    }
    
    
    
    
    
    private void onMessageReceived(short sequenceNumber, TouchServiceTcpCommunicationPayloadTypes payloadType, byte[] payload, Channel channel)
    {
    	try
    	{
    		println("Received Payload: " + payloadType.toString());
    		
			switch(payloadType)
			{
				case Ping:
					sendMessage(TouchServiceTcpCommunicationPayloadTypes.Pong, null, channel);
					break;
				
				case Pong:
                    break; // do nothing. It's just a response to Ping.

                case Disconnect:
                    //TODO...
                    break;
                    
                case Error:

                    // The server will automatically disconnect after sending the error to us.
                    ErrorPayload errorPayload = new ErrorPayload(payload);

                    //System.Diagnostics.Debug.WriteLine(DateTime.Now.ToString() + "  **** Error Command. Message: " + errorPayload.Message);

                	_eventBus.post(new TouchTcpClientExceptionEventArgs(errorPayload.Message));
                    
                    break;
                    
                case ContinueSessionResult:

                    ContinueSessionResultPayload contSessResult = new ContinueSessionResultPayload(payload);

                    if (contSessResult.Result == ContinueSessionResults.Success)
                    {
                    	_eventBus.post(new TouchTcpClientStateChangedEventArgs(TouchTcpClientState.Authenticated));

                    	_eventBus.post(new ContinueSessionResultReceivedEventArgs(contSessResult.Result, contSessResult.TouchScreenSize));
                    }
                    else
                    {
                    	_eventBus.post(new ContinueSessionResultReceivedEventArgs(contSessResult.Result, contSessResult.TouchScreenSize));

                        // Clear the session id since it wasn't valid.
                        _sessionID = null;

                        sendHello(channel);
                    }

                    break;

                case AuthenticateChallenge:

                    AuthenticationChallengePayload challenge = new AuthenticationChallengePayload(payload);

                    // Create new random token
                    byte[] t2 = new byte[16];
                    new Random().nextBytes(t2);

                    // Calculate password hash.
                    MessageDigest md = MessageDigest.getInstance("MD5");
                    byte[] passwordHash = md.digest(_password.getBytes("UTF-8")); // this produces the same result as .net (no BOM)
                    
                    // Concatenate the byte arrays for hashing
                    byte[] hashMe = concatenateByteArrays(t2, passwordHash, challenge.AuthenticationToken);

                    // Calculate the hash
                    byte[] helloHash = md.digest(hashMe);

                    AuthenticatePayload auth = new AuthenticatePayload(_username, t2, helloHash);

                    sendMessage(auth, channel);
                    break;

                case AuthenticationResult:

                    AuthenticationResultPayload authResult = new AuthenticationResultPayload(payload);


                    if (authResult.AuthenticationResult == TouchServiceTcpCommunicationAuthenticationResults.Success)
                    {
                        _sessionID = authResult.SessionID;

                        _eventBus.post(new TouchTcpClientStateChangedEventArgs(TouchTcpClientState.Authenticated));

                        _eventBus.post(new TouchTcpAuthenticationResultReceivedEventArgs(authResult.AuthenticationResult, authResult.TouchScreenSize, authResult.SessionID));
                    }
                    else
                    {
                    	_failedAuthentication = true;
                    	
                    	_eventBus.post(new TouchTcpClientStateChangedEventArgs(TouchTcpClientState.FailedAuthentication));

                    	_eventBus.post(new TouchTcpAuthenticationResultReceivedEventArgs(authResult.AuthenticationResult, null, null));
                    }

                    break;
                
                case ScreenChange:

                    RendererScreenChangePayload screenChange = new RendererScreenChangePayload(payload);

                    //Console.WriteLine(DateTime.Now.ToString() + "  **** TouchClient processing ScreenChange. X=" + screenChange.Bounds.X + ", Y=" + screenChange.Bounds.Y + ", Width=" + screenChange.Bounds.Width + ", Height=" + screenChange.Bounds.Height);

                    // For some reason the server is sending a 0,0,0,0 rectangle first.
                    if (screenChange.Bounds.width() > 0 && screenChange.Bounds.height() > 0)
                    {
		                // Add the screenchange bounds to the list.
		                _screenChanges.add(screenChange.Bounds);
		
		                // Delay the screen change area request so we can optimize the request based on more incoming requests.
						//Create timer service with a single thread.  From: http://stackoverflow.com/questions/2198360/how-to-run-a-timer
						if (_screenChangeTimer == null)
						{
							_screenChangeTimer = Executors.newSingleThreadScheduledExecutor();
							//Schedule a one-off task to run in x seconds.
							_screenChangeTimer.schedule(new Runnable()
							{
								public void run()
								{
									// Dispose the timer.
									_screenChangeTimer.shutdown(); // TODO: do I really need to do this since it is a one time use timer?
									_screenChangeTimer = null;
									
									// Process all the received screenchange bounds and request a snapshot.
									_screenChangeTimer_Elapsed();
								}
							}, 125, TimeUnit.MILLISECONDS);
						}
					}
                	break;
                    
                    
                case DrawImage:
             
                	RendererDrawImagePayload drawImagePayload = null;
                	try
                	{
                		drawImagePayload = new RendererDrawImagePayload(payload);
                    	
                		//Console.WriteLine(DateTime.Now.ToString() + "  **** TouchClient processing DrawImage payload for bounds: X=" + drawImagePayload.Bounds.X + ", Y=" + drawImagePayload.Bounds.Y + ", Width=" + drawImagePayload.Bounds.Width + ", Height=" + drawImagePayload.Bounds.Height);

                    	_eventBus.post(new DrawImageReceivedEventArgs(drawImagePayload.Bounds, drawImagePayload.Opacity, drawImagePayload.SizeMode, drawImagePayload.Image));
                	}
                	finally
                	{
                		if (drawImagePayload != null)
                			drawImagePayload.close();
                	}
                    break;
			}

    	}
    	catch (Exception e)
    	{
			e.printStackTrace();
		}
	}
    
    
    private void sendHello(Channel channel) throws IOException
    {
		HelloPayload hello = new HelloPayload((byte)1, (byte)4, RenderingMode.Snapshots, _screenSize, 4, true, _deviceID);

		sendMessage(hello, channel);
    }

//	private byte[] concatenateByteArrays(byte[] b1, byte[] b2)
//	{	
//		byte[] data = new byte[b1.length + b2.length];
//
//        System.arraycopy(b1, 0, data, 0, b1.length);
//        System.arraycopy(b2, 0, data, b1.length, b2.length);
//
//        return data;
//	}
	
	private static byte[] concatenateByteArrays(byte[] b1, byte[] b2, byte[] b3)
	{	
		byte[] data = new byte[b1.length + b2.length + b3.length];

	    System.arraycopy(b1, 0, data, 0, b1.length);
	    System.arraycopy(b2, 0, data, b1.length, b2.length);
	    System.arraycopy(b3, 0, data, b1.length + b2.length, b3.length);

	    return data;
	}
	
	
	
	
	

	void _screenChangeTimer_Elapsed()
    {
        // Get the current list.
        List<Rect> screenChanges = _screenChanges;

        // Create a new list for incoming requests.
        _screenChanges = new ArrayList<Rect>();

        // TODO: Try to optimize the screen change requests based on bounds. Right now it just gets a big rectangle that includes all changes.
        Rect r = new Rect(0, 0, -1, -1); // empty
        for(Rect rect : screenChanges ){
            if (r.isEmpty()) // isEmpty means negative width or height.
                r = rect;
            else
            	r.union(rect);
        }

        if (r.width() <= 0 && r.height() <= 0)
        {
            //Console.WriteLine(DateTime.Now.ToString() + "  >>>> TouchClient skipping snapshot request since the region was empty.");
            return;
        }

        //Console.WriteLine(DateTime.Now.ToString() + "  >>>> TouchClient requesting snapshot for X=" + r.X + ", Y=" + r.Y + ", Width=" + r.Width + ", Height=" + r.Height);

        // Request the screen snapshot.
        RequestSnapshotPayload requestSnapshot = new RequestSnapshotPayload(r);
        try
		{
			sendMessage(requestSnapshot, _channel);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
    }

	public void close() throws IOException
	{
		// YOU MUST CALL THIS BEFORE CALLING releaseExternalResources() on the factory or bootstrap!!!
		
		_isClosed = true;
		
		_channel = null;
		if (_screenChangeTimer != null)
		{
			_screenChangeTimer.shutdown();
			_screenChangeTimer = null;
		}		
	}
}
