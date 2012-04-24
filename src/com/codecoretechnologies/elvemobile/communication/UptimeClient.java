package com.codecoretechnologies.elvemobile.communication;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

//import org.jboss.netty.bootstrap.Bootstrap;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.handler.timeout.ReadTimeoutHandler;
import org.jboss.netty.util.HashedWheelTimer;
import org.jboss.netty.util.Timer;

import android.graphics.Point;

import com.google.common.eventbus.EventBus;


/**
 * Connects to a server periodically to measure and print the uptime of the
 * server.  This example demonstrates how to implement reliable reconnection
 * mechanism in Netty.
 */
public class UptimeClient implements Closeable
{

    // Sleep 5 seconds before a reconnection attempt.
    static final int RECONNECT_DELAY = 5;

    // Reconnect when the server sends nothing for 60 seconds.
    private static final int READ_TIMEOUT = 60;
    
    private final String _host;
    private final int _port;
    private EventBus _eventBus;
    private String _username;
    private String _password;
    private byte[] _sessionID;
    private Point _screenSize;
    private String _deviceID;
    
    private ClientBootstrap _bootstrap;
    private ChannelFuture _future;
    private Timer _timer;
    private UptimeClientHandler _uptimeHandler;

    public UptimeClient(String host, int port, String username, String password, byte[] sessionID, String deviceID, Point screenSize, EventBus eventBus) {
        this._host = host;
        this._port = port;
        this._username = username;
        this._password = password;
        this._sessionID = sessionID;
        this._deviceID = deviceID;
        this._screenSize = screenSize;
        this._eventBus = eventBus;
    }

    public void run()
    {
        // Initialize the timer that schedules subsequent reconnection attempts.
        _timer = new HashedWheelTimer();

        // Configure the client.
        final ChannelFactory channelFactory = new NioClientSocketChannelFactory(
                Executors.newCachedThreadPool(),
                Executors.newCachedThreadPool());
        _bootstrap = new ClientBootstrap(channelFactory);

        _uptimeHandler = new UptimeClientHandler(_bootstrap, _timer, _username, _password, _sessionID, _deviceID, _screenSize, _eventBus);
        
        // Configure the pipeline factory.
        _bootstrap.setPipelineFactory(new ChannelPipelineFactory()
        {
            private final ChannelHandler timeoutHandler = new ReadTimeoutHandler(_timer, READ_TIMEOUT);
            
            public ChannelPipeline getPipeline()
            {
                return Channels.pipeline(timeoutHandler, _uptimeHandler);
            }
        });

        _bootstrap.setOption("remoteAddress", new InetSocketAddress(_host, _port));
        //_bootstrap.setOption("child.keepAlive", true);
        //_bootstrap.setOption("child.tcpNoDelay", true);
        //_bootstrap.setOption("child.connectTimeoutMillis", 15000);
        
        // On Android 2.2 you must disable IP6 in the Android Emulator otherwise you will see "java.net.SocketException: Bad address family" in the LogCat window and NullPointerException.
        // See http://meteatamel.wordpress.com/2010/08/26/socketexceptions-with-android/
        // and http://code.google.com/p/android/issues/detail?id=9431
        //if ("google_sdk".equals(android.os.Build.PRODUCT) || "sdk".equals(android.os.Build.PRODUCT))
        if ("9774D56D682E549C".equals(_deviceID)); // Since Android 2.2 the emulator device id is "9774D56D682E549C".
        	java.lang.System.setProperty("java.net.preferIPv6Addresses", "false");
        
        

        _eventBus.post(new TouchTcpClientStateChangedEventArgs(TouchTcpClientState.AttemptingToConnect));
        
        // Initiate the first connection attempt - the rest is handled by
        _future = _bootstrap.connect();
    }
    
    public void SendTouchEvent(TouchEventType eventType, int x, int y)
    {
    	TouchEventPayload touchEvent = new TouchEventPayload(eventType, new Point(x, y));
    	try
		{
			_uptimeHandler.sendMessage(touchEvent);
		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    public void SendLocationChangeEvent(double latitude, double longitude, double altitude, double course, double speedMetersPerSecond)
    {
    	LocationChangeEventPayload locationChange = new LocationChangeEventPayload(latitude, longitude, altitude, course, speedMetersPerSecond);
    	try
		{
			_uptimeHandler.sendMessage(locationChange);
		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

	public void close() throws IOException
	{
		// see http://docs.jboss.org/netty/3.2/guide/html/start.html there is a section called: Shutting Down Your Application
	
		// WE MUST CLOSE THE handler because for some reason the event trigger in channelClosed causes bootstrap.releaseExternalResources to block.
		_uptimeHandler.close();
		
		// Release all resources acquired by the Timer and cancel all tasks which were scheduled but not executed yet. 
		_timer.stop();
		
		// Close the connection and wait until the connection is closed or the connection attempt fails.  Make sure the close operation ends because all I/O operations are asynchronous in Netty.
		_future.getChannel().close().awaitUninterruptibly();

		//Releases the external resources that this object depends on. You should not call this method if the external resources (e.g. thread pool) are in use by other objects. This method simply delegates the call to ChannelFactory.releaseExternalResources(). 
		_bootstrap.releaseExternalResources();  //This method simply delegates the call to ChannelFactory.releaseExternalResources().
	}
}
