package com.learn.nio.groupchat;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

public class ChatServer {

    private final int port;
    private Selector selector;

    public ChatServer(int port) throws IOException {
        this.port = port;
        init();
    }

    private void init() throws IOException {
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.bind(new InetSocketAddress(port));
        serverSocketChannel.configureBlocking(false);

        this.selector = Selector.open();
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
    }


    public void handle() throws IOException {
        if (selector.select(500) > 0) {
            Iterator<SelectionKey> keys = selector.selectedKeys().iterator();
            while (keys.hasNext()) {
                SelectionKey next = keys.next();

                acceptHandle(next);

                try {
                    readHandle(next);
                } catch (IOException e) {
                    next.cancel();
                }

                keys.remove();
            }
        }
    }

    private void readHandle(SelectionKey selectionKey) throws IOException {
        if (selectionKey.isReadable()) {
            SocketChannel channel = (SocketChannel) selectionKey.channel();

            InetSocketAddress address = (InetSocketAddress) channel.getRemoteAddress();

            ByteBuffer buffer = ByteBuffer.allocate(1024);

            int i = channel.read(buffer);

            String value = address.getPort() + " : " + new String(buffer.array(), 0, i);

            forwardHandle(channel, value);
        }
    }

    private void forwardHandle(SocketChannel channel, String value) throws IOException {
        for (SelectionKey key : selector.keys()) {
            SelectableChannel toChannel1 = key.channel();
            if (toChannel1.validOps() == SelectionKey.OP_ACCEPT || channel == toChannel1) {
                continue;
            }
            SocketChannel toChannel = (SocketChannel) toChannel1;

            toChannel.write(ByteBuffer.wrap(value.getBytes()));
        }
    }


    private void acceptHandle(SelectionKey selectionKey) throws IOException {
        if (selectionKey.isAcceptable()) {
            ServerSocketChannel channel = (ServerSocketChannel) selectionKey.channel();
            SocketChannel socketChannel = channel.accept();
            socketChannel.configureBlocking(false);
            socketChannel.register(selector, SelectionKey.OP_READ);
        }
    }


    public static void main(String[] args) throws IOException {
        ChatServer server = new ChatServer(7999);
        while (true) {
            try {
                server.handle();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
