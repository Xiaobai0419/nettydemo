package com.xiaobai.netty.server;

import com.xiaobai.codec.protobuf.SubscribeReqProto;
import com.xiaobai.netty.server.handler.SubReqServerHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

public class SubReqServer {

    public void bind(int port) throws InterruptedException {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup,workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG,100)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            //解码器，反序列化
                            //半包处理，基于长度字段
                            socketChannel.pipeline().addLast(new ProtobufVarint32FrameDecoder());
                            //参数是生成的序列化类类型
                            socketChannel.pipeline().addLast(new ProtobufDecoder(SubscribeReqProto.SubscribeReq.getDefaultInstance()));

                            //编码器，序列化
                            //半包，添加长度字段
                            socketChannel.pipeline().addLast(new ProtobufVarint32LengthFieldPrepender());
                            //编码时不需要指定序列化类类型，将类实例直接写入Channel
                            socketChannel.pipeline().addLast(new ProtobufEncoder());

                            //业务处理
                            socketChannel.pipeline().addLast(new SubReqServerHandler());
                        }
                    });
            //同步等待（异步）绑定成功（多线程等待唤醒）
            ChannelFuture f = b.bind(port).sync();//await之类，抛出InterruptedException
            //同步等待（异步）监听端口关闭--否则始终保持启动
            f.channel().closeFuture().sync();
        } finally {
            //在上面执行代码的端口关闭以后，优雅退出
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) throws InterruptedException {
        int port = 1989;
        if(args != null && args.length > 0) {
            try {
                port = Integer.valueOf(args[0]);
            } catch (NumberFormatException ex) {
            }
        }
        new SubReqServer().bind(port);
    }
}
