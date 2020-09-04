package xyz.lizhaorong.nio.chatroom;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;

public class ChatRoomServer {

    private Selector selector;

    private ServerSocketChannel listener;

    private static final int PORT = 6667;

    private ChatRoomServer() {}

    /**
     * 静态工厂创建server
     * @return 一个server
     */
    public static ChatRoomServer open(){
        ChatRoomServer server = new ChatRoomServer();
        try {
            server.selector = Selector.open();
            server.listener = ServerSocketChannel.open();
            server.listener.socket().bind(new InetSocketAddress(PORT));
            server.listener.configureBlocking(false);
            server.listener.register(server.selector, SelectionKey.OP_ACCEPT);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return server;
    }

    public void listen() throws IOException {
        System.out.println("server listening at"+PORT);
        while (true){
            if( selector.select(2000)>0 ){
                Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                while (iterator.hasNext()){
                    SelectionKey key = iterator.next();

                    if(key.isAcceptable()){
                        SocketChannel socketChannel = listener.accept();
                        socketChannel.configureBlocking(false);
                        socketChannel.write(ByteBuffer.wrap("欢迎".getBytes()));
                        socketChannel.register(selector,SelectionKey.OP_READ, ByteBuffer.allocate(1024));
                        System.out.println(socketChannel.getRemoteAddress()+" login .");
                    }

                    else if (key.isReadable()){
                        readData(key);
                    }

                    iterator.remove();
                }
            }
        }
    }


    private void readData(SelectionKey key){
        SocketChannel channel = null;
        try{
            channel = (SocketChannel) key.channel();
            ByteBuffer buffer = (ByteBuffer) key.attachment();
            int count = channel.read(buffer);
            if(count>0){
                //发送给别的channel
                String msg = new String(buffer.array());
                System.out.println("from 客户端 : " + msg);
                buffer.clear();
                sendToOthers(msg,channel);
            }else{
                System.out.println(channel.getRemoteAddress()+" 客户端断开了");
                key.cancel();
                channel.close();
            }
        } catch (IOException e) {
            try {
                System.out.println(channel.getRemoteAddress() + " 客户端断开了");
                key.cancel();
                channel.close();
            }catch (IOException ex) {
                ex.printStackTrace();
            }

        }
    }

    private void sendToOthers(String msg,SocketChannel self) throws IOException {
        ByteBuffer buffer;
        for (SelectionKey key : selector.keys()) {
            Channel target = key.channel();
//            if (target == self){
//                System.out.println("print to self");
//                ByteBuffer buffer = (ByteBuffer) key.attachment();
//                System.out.println(new String(buffer.array()));
//                ((SocketChannel)target).write(buffer);
//                buffer.clear();
//            }
            if (target!=self && target instanceof SocketChannel){
                System.out.println("send to a user");
                buffer = ByteBuffer.wrap(msg.getBytes());
                SocketChannel dest = (SocketChannel) target;
                dest.write(buffer);
            }
        }
    }

    public static void main(String[] args) throws IOException {
        ChatRoomServer roomServer = ChatRoomServer.open();
        roomServer.listen();
    }

}
