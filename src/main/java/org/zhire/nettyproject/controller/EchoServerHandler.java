package org.zhire.nettyproject.controller;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.util.CharsetUtil;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * 处理服务端 channel.
 */
public class EchoServerHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {

    private WebSocketServerHandshaker handshaker;
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, TextWebSocketFrame textWebSocketFrame) throws Exception {
        System.out.println("收到消息："+ textWebSocketFrame);
            //处理websocket客户端的消息
            handlerWebSocketFrame(channelHandlerContext, textWebSocketFrame);
    }
    private void handlerWebSocketFrame(ChannelHandlerContext ctx, WebSocketFrame frame){
        // 判断是否关闭链路的指令
        if (frame instanceof CloseWebSocketFrame) {
            handshaker.close(ctx.channel(), (CloseWebSocketFrame) frame.retain());
            return;
        }
        // 判断是否ping消息
        if (frame instanceof PingWebSocketFrame) {
            ctx.channel().write(
                    new PongWebSocketFrame(frame.content().retain()));
            return;
        }
        // 本例程仅支持文本消息，不支持二进制消息
        if (!(frame instanceof TextWebSocketFrame)) {
            System.out.println("本例程仅支持文本消息，不支持二进制消息");
            throw new UnsupportedOperationException(String.format(
                    "%s frame types not supported", frame.getClass().getName()));
        }
        // 返回应答消息
        String request = ((TextWebSocketFrame) frame).text();
        System.out.println("服务端收到：" + request);
        TextWebSocketFrame tws = new TextWebSocketFrame(new Date().toString()
                + ctx.channel().id() + "：" + request);
        // 群发
        ChannelSupervise.send2All(tws);
        // 返回【谁发的发给谁】
        // ctx.channel().writeAndFlush(tws);
    }
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        System.out.println("服务器端读取完成...");
        ctx.flush();
        TimeUnit.MILLISECONDS.sleep(200);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}