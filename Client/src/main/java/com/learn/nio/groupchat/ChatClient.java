package com.learn.nio.groupchat;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

public class ChatClient {
    private final int port;
    private final BufferedReader reader;
    private Selector selector;
    private SocketChannel socketChannel;
    private InetSocketAddress address;


    public ChatClient(int port) throws IOException {
        this.port = port;
        this.reader = new BufferedReader(new InputStreamReader(System.in));

        init();
    }

    private void init() throws IOException {
        this.socketChannel = SocketChannel.open(new InetSocketAddress(port));
        socketChannel.configureBlocking(false);

        this.selector = Selector.open();
        socketChannel.register(selector, SelectionKey.OP_READ);

        this.address = (InetSocketAddress) socketChannel.getLocalAddress();
    }


    public void handle() throws IOException {
        if (reader.ready()) {
            String line = reader.readLine();

            System.out.println("me (" + address.getPort() + ") : " + line);

            socketChannel.write(ByteBuffer.wrap(line.getBytes()));
        }

        if (selector.select(500) > 0) {
            Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();

            while (iterator.hasNext()) {
                SelectionKey next = iterator.next();

                ByteBuffer buffer = ByteBuffer.allocate(1024);
                SocketChannel channel = (SocketChannel) next.channel();
                int i = channel.read(buffer);

                System.out.println(new String(buffer.array(), 0, i));

                iterator.remove();
            }
        }
    }


    public static void main(String[] args) throws IOException {
        ChatClient client = new ChatClient(7999);
        while (true) {
            try {
                client.handle();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
