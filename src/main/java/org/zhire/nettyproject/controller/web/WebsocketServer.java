package org.zhire.nettyproject.controller.web;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.stream.ChunkedWriteHandler;

/**
 * Websocket 聊天服务器-服务端
 * 参考：https://wiki.jikexueyuan.com/project/netty-4-user-guide/realize-chat.html
 */
public class WebsocketServer {

    private int port;

    public WebsocketServer(int port) {
        this.port = port;
    }


    public  void startNetty() throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup(2); // (1) 接收进来的连接
        EventLoopGroup workerGroup = new NioEventLoopGroup(3); // 处理已经被接收的连接
        try {
            ServerBootstrap b = new ServerBootstrap(); // (2) 启动 NIO 服务的辅助启动类
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class) // (3) 一个新的 Channel 如何接收进来的连接
                    // .childHandler(new WebsocketDanmuServerInitializer())  // (4)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast("http-decodec", new HttpRequestDecoder());
                            pipeline.addLast("http-aggregator", new HttpObjectAggregator(65536));
                            pipeline.addLast("http-encodec", new HttpResponseEncoder());
                            pipeline.addLast("http-chunked", new ChunkedWriteHandler());
                            pipeline.addLast("WebSocket-protocol", new WebSocketServerProtocolHandler("/ws"));
                            pipeline.addLast("WebSocket-request", new TextWebSocketFrameHandler());
                        }
                    })  // (4) 配置一个 Channel
                    .option(ChannelOption.SO_BACKLOG, 128) // (5) 提供给NioServerSocketChannel用来接收进来的连接
                    .childOption(ChannelOption.SO_KEEPALIVE, true); // (6) 配置参数

            System.out.println("websocketServer 启动了" + port);

            // 绑定端口，开始接收进来的连接
            ChannelFuture f = b.bind(port).sync(); // (7)

            // 等待服务器  socket 关闭 。
            // 在这个例子中，这不会发生，但你可以优雅地关闭你的服务器。
            f.channel().closeFuture().sync();

        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
            System.out.println("websocketServer 关闭了");
        }

    }
}