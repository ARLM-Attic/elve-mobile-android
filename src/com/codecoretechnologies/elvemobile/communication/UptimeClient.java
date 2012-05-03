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
import org.jboss.netty.handler.timeout.IdleStateHandler;
import org.jboss.netty.handler.timeout.ReadTimeoutHandler;
import org.jboss.netty.handler.timeout.WriteTimeoutHandler;
import org.jboss.netty.util.HashedWheelTimer;
import org.jboss.netty.util.Timer;

import android.graphics.Point;
import android.util.Log;

import com.google.common.eventbus.EventBus;


/**
 * Connects to a server periodically to measure and print the uptime of the
 * server.  This example demonstrates how to implement reliable reconnection
 * mechanism in Netty.
 */
public class UptimeClient implements Closeable
{
    // Reconnect when the server sends nothing for 60 seconds.
    private static final int READ_TIMEOUT = 30; // the server will send us a ping every 30 seconds of inactivity
    private static final int WRITE_TIMEOUT = 14;
    
    private final String _host;
    private final int _port;
    private EventBus _eventBus;
    private String _username;
    private String _password;
    private byte _imageFormat;
    private byte _jpegImageQuality;
    private byte[] _sessionID;
    private Point _screenSize;
    private String _deviceID;
    
    private ClientBootstrap _bootstrap;
    private ChannelFuture _future;
    private Timer _idleTimer;
    private UptimeClientHandler _uptimeHandler;

    public UptimeClient(String host, int port, String username, String password, byte imageFormat, byte jpegImageQuality, byte[] sessionID, String deviceID, Point screenSize, EventBus eventBus) {
        this._host = host;
        this._port = port;
        this._username = username;
        this._password = password;
        this._imageFormat = imageFormat;
        this._jpegImageQuality = jpegImageQuality;
        this._sessionID = sessionID;
        this._deviceID = deviceID;
        this._screenSize = screenSize;
        this._eventBus = eventBus;
    }

    public void run()
    {
        // Initialize the timer that schedules subsequent reconnection attempts.
        _idleTimer = new HashedWheelTimer();

        // Configure the client.
        final ChannelFactory channelFactory = new NioClientSocketChannelFactory(
                Executors.newCachedThreadPool(),
                Executors.newCachedThreadPool());
        _bootstrap = new ClientBootstrap(channelFactory);

        _uptimeHandler = new UptimeClientHandler(_bootstrap, _username, _password, _imageFormat, _jpegImageQuality, _sessionID, _deviceID, _screenSize, _eventBus);
        
        // Configure the pipeline factory.
        _bootstrap.setPipelineFactory(new ChannelPipelineFactory()
        {
            private final ChannelHandler idleStateHandler = new IdleStateHandler(_idleTimer, READ_TIMEOUT, WRITE_TIMEOUT, 0);
            
            public ChannelPipeline getPipeline()
            {
                return Channels.pipeline(idleStateHandler, new ElveIdleStateHandler(), _uptimeHandler);
            }
        });

        _bootstrap.setOption("remoteAddress", new InetSocketAddress(_host, _port));
        _bootstrap.setOption("keepAlive", true); // I don't know that this will have any effect since the default is to send keepalive packets typically every 2 hours.
        _bootstrap.setOption("tcpNoDelay", true); // Determines whether Nagle's algorithm is to be used. The Nagle's algorithm tries to conserve bandwidth by minimizing the number of segments that are sent. When applications wish to decrease network latency and increase performance, they can disable Nagle's algorithm (that is enable TCP_NODELAY). Data will be sent earlier, at the cost of an increase in bandwidth consumption.
        //_bootstrap.setOption("connectTimeoutMillis", 15000);
        
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
			e.printStackTrace();
		}
    }

	public void close() throws IOException
	{
		// see http://docs.jboss.org/netty/3.2/guide/html/start.html there is a section called: Shutting Down Your Application
	
		// WE MUST CLOSE THE handler because for some reason the event trigger in channelClosed causes bootstrap.releaseExternalResources to block.
		_uptimeHandler.close();
		
		// Release all resources acquired by the Timers and cancel all tasks which were scheduled but not executed yet. 
		_idleTimer.stop();
		
		// Close the connection and wait until the connection is closed or the connection attempt fails.  Make sure the close operation ends because all I/O operations are asynchronous in Netty.
		// THIS MUST NOT BE DONE ON THE IO THREAD! or you will get: An Executor cannot be shut down from the thread acquired from itself.  Please make sure you are not calling releaseExternalResources() from an I/O worker thread.
		_future.getChannel().close().awaitUninterruptibly();

		//Releases the external resources that this object depends on. You should not call this method if the external resources (e.g. thread pool) are in use by other objects. This method simply delegates the call to ChannelFactory.releaseExternalResources(). 
		_bootstrap.releaseExternalResources();  //This method simply delegates the call to ChannelFactory.releaseExternalResources().
		
		Log.d("", "RELEASED all connection related resources.");
	}

}
