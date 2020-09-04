package xyz.lizhaorong.nio.chatroom;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Scanner;

public class ChatRoomClient {

    private static final int PORT = 6667;
    private static final String HOST = "127.0.0.1";
    private final Selector selector;
    private final SocketChannel socketChannel;
    private String username;
    private volatile boolean receiving;



    private ChatRoomClient() throws IOException {
        selector = Selector.open();
        socketChannel=SocketChannel.open(new InetSocketAddress(HOST,PORT));
        socketChannel.configureBlocking(false);
        socketChannel.register(selector,SelectionKey.OP_READ);
        receiving = true;
    };

    public static ChatRoomClient open(String username) throws IOException {
        ChatRoomClient client = new ChatRoomClient();
        client.username = username;
        client.readMsg();
        return client;
    }

    public void sendMsg(String msg) throws IOException {
        msg = username + " : " + msg;
        socketChannel.write(ByteBuffer.wrap(msg.getBytes()));
    }

    public void readMsg(){
        new Thread(()->{
            System.out.println("begin receiving");
            while (receiving){
                try {
                    int size = selector.select();
                    if (size>0) {
                        Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                        while (iterator.hasNext()){
                            SelectionKey k = iterator.next();
                            if(k.isReadable()){
                                SocketChannel sc = (SocketChannel) k.channel();
                                ByteBuffer buffer = ByteBuffer.allocate(1024);
                                if(sc.read(buffer)>0){
                                    String msg = new String(buffer.array());
                                    System.out.println(msg);
                                }
                            }
                            iterator.remove();
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }).start();
    }

    private void stop(){
        receiving = false;
    }

    public static void main(String[] args) throws IOException {
        ChatRoomClient chatRoomClient = ChatRoomClient.open("baby");
        Scanner scanner = new Scanner(System.in);
        String msg;
        while (!(msg = scanner.nextLine()).equals("exit")){
            chatRoomClient.sendMsg(msg);
        }
        chatRoomClient.stop();
        System.out.println("bye");
    }

}
