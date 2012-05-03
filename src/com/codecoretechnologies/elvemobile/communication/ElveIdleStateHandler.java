package com.codecoretechnologies.elvemobile.communication;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.timeout.IdleState;
import org.jboss.netty.handler.timeout.IdleStateAwareChannelHandler;
import org.jboss.netty.handler.timeout.IdleStateEvent;

import android.util.Log;

//Handler should handle the IdleStateEvent triggered by IdleStateHandler.
public class ElveIdleStateHandler extends IdleStateAwareChannelHandler
{
	UptimeClient _comm;

	@Override
	  public void channelIdle(ChannelHandlerContext ctx, IdleStateEvent e)
	  {
	      if (e.getState() == IdleState.READER_IDLE)
	      {
	    	  Log.d("IdleStateHandler", "Sending ping since no inbound traffic in a while.");
	    	  try
			  {
	    		  UptimeClientHandler.sendMessage(TouchServiceTcpCommunicationPayloadTypes.Ping, null, e.getChannel());
			  }
	    	  catch (Exception ex)
			  {
	    		  ex.printStackTrace();
			  }
	      }
	      else if (e.getState() == IdleState.WRITER_IDLE)
	      {
	    	  Log.d("IdleStateHandler", "Sending ping since no outbound traffic in a while.");
	    	  try
			  {
	    		  UptimeClientHandler.sendMessage(TouchServiceTcpCommunicationPayloadTypes.Ping, null, e.getChannel());
			  }
	    	  catch (Exception ex)
			  {
	    		  ex.printStackTrace();
			  }
	      }
	  }
}
