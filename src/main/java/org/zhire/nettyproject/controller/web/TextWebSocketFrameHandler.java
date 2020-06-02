package org.zhire.nettyproject.controller.web;


import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.concurrent.GlobalEventExecutor;

/**
 * 处理TextWebSocketFrame
 *
 */
public class TextWebSocketFrameHandler extends
        SimpleChannelInboundHandler<TextWebSocketFrame> {
	
	public static ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
	
	@Override
	protected void channelRead0(ChannelHandlerContext ctx,
                                TextWebSocketFrame msg) throws Exception { // (1)
		Channel incoming = ctx.channel();
		for (Channel channel : channels) {
            if (channel != incoming){
				channel.writeAndFlush(new TextWebSocketFrame(msg.text()));
			} else {
				channel.writeAndFlush(new TextWebSocketFrame("我发送的"+msg.text()));
			}
        }
	}
	
	@Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {  // (2)
        Channel incoming = ctx.channel();
        
        // 向多个管道广播消息
        channels.writeAndFlush(new TextWebSocketFrame("[SERVER] - " + incoming.remoteAddress() + " 加入"));
        
        channels.add(incoming);
		System.out.println("Client:"+incoming.remoteAddress() +"加入");
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {  // (3)
        Channel incoming = ctx.channel();
        
        // 向多个管道广播消息
        channels.writeAndFlush(new TextWebSocketFrame("[SERVER] - " + incoming.remoteAddress() + " 离开"));
        
		System.err.println("Client:"+incoming.remoteAddress() +"离开");

        // A closed Channel is automatically removed from ChannelGroup,
        // so there is no need to do "channels.remove(ctx.channel());"
    }

    // 客户端连接时候会被channelActive监听到
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception { // (5)
        Channel incoming = ctx.channel();
		System.out.println("Client:"+incoming.remoteAddress()+"在线");
	}

	// 客户端断开时会被channelInactive监听到
	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception { // (6)
        Channel incoming = ctx.channel();
		System.err.println("Client:"+incoming.remoteAddress()+"掉线");
	}

	// 异常监听
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)	// (7)
			throws Exception {
    	Channel incoming = ctx.channel();
		System.err.println("Client:"+incoming.remoteAddress()+"异常");
				// 当出现异常就关闭连接
				cause.printStackTrace();
        ctx.close();
	}

}
