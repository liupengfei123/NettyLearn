package com.learn.netty.handle;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.util.CharsetUtil;

import java.net.SocketAddress;

public class MyClientOutHandler extends ChannelOutboundHandlerAdapter {

    @Override
    public void connect(ChannelHandlerContext ctx, SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise) throws Exception {
        super.connect(ctx, remoteAddress, localAddress, promise);

        System.out.println("建立连接  remoteAddress:" + remoteAddress + "  localAddress:" + localAddress);
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        System.out.println("write :" + msg);

        ByteBuf buf = Unpooled.copiedBuffer(msg.toString(), CharsetUtil.UTF_8);
        super.write(ctx, buf, promise);
    }
}
