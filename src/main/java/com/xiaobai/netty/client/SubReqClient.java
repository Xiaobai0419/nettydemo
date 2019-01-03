package com.xiaobai.netty.client;

import com.xiaobai.codec.protobuf.SubscribeReqProto;
import com.xiaobai.codec.protobuf.SubscribeRespProto;
import com.xiaobai.netty.client.handler.SubReqClientHandler;
import com.xiaobai.netty.server.handler.SubReqServerHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

public class SubReqClient {

    public void connect(int port,String host) throws InterruptedException {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY,true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            //解码器，反序列化
                            //半包处理，基于长度字段
                            socketChannel.pipeline().addLast(new ProtobufVarint32FrameDecoder());
                            //参数是生成的序列化类类型--这里是服务端返回的Resp类型
                            socketChannel.pipeline().addLast(new ProtobufDecoder(SubscribeRespProto.SubscribeResp.getDefaultInstance()));

                            //编码器，序列化
                            //半包，添加长度字段
                            socketChannel.pipeline().addLast(new ProtobufVarint32LengthFieldPrepender());
                            //编码时不需要指定序列化类类型，将类实例直接写入Channel
                            socketChannel.pipeline().addLast(new ProtobufEncoder());

                            //业务处理
                            socketChannel.pipeline().addLast(new SubReqClientHandler());
                        }
                    });
            //同步等待（异步）连接成功（多线程等待唤醒）
            ChannelFuture f = b.connect(host,port).sync();//await之类，抛出InterruptedException
            //同步等待（异步）客户端链路关闭--否则始终保持连接
            f.channel().closeFuture().sync();
        } finally {
            //在上面执行代码的客户端链路关闭以后，优雅退出
            group.shutdownGracefully();
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
        new SubReqClient().connect(port,"127.0.0.1");
    }
}
