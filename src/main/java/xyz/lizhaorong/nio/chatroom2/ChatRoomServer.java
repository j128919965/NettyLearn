package xyz.lizhaorong.nio.chatroom2;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;

/**
 * 两个selector分工
 */
public class ChatRoomServer {

    private Selector accessSelector;
    private ChatRoomServerHandler handler;

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
            server.accessSelector = Selector.open();
            server.handler = new ChatRoomServerHandler();
            server.listener = ServerSocketChannel.open();
            server.listener.socket().bind(new InetSocketAddress(PORT));
            server.listener.configureBlocking(false);
            server.listener.register(server.accessSelector, SelectionKey.OP_ACCEPT);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return server;
    }

    public void listen() throws IOException {
        System.out.println("server listening at"+PORT);
        handler.start();
        while (true){
            if( accessSelector.select(2000)>0 ){
                Iterator<SelectionKey> iterator = accessSelector.selectedKeys().iterator();
                while (iterator.hasNext()){
                    SelectionKey key = iterator.next();

                    //建立连接
                    if(key.isAcceptable()){
                        SocketChannel socketChannel = listener.accept();
                        socketChannel.configureBlocking(false);
                        socketChannel.write(ByteBuffer.wrap("欢迎".getBytes()));
                        handler.bind(socketChannel);
                        System.out.println(socketChannel.getRemoteAddress()+" login .");
                    }
                    iterator.remove();
                }
            }
        }
    }




    public static void main(String[] args) throws IOException {
        ChatRoomServer roomServer = ChatRoomServer.open();
        roomServer.listen();
    }

}

class ChatRoomServerHandler{
    private final Selector selector;
    // 0 新建 1 运行中 2 最后一次处理 3 关闭连接中
    private volatile int state;

    ChatRoomServerHandler() throws IOException {
        selector = Selector.open();
        state = 0;
    }

    public void start() {
        new Thread(()->{
            state = 1;
            while (state == 1){
                try {
                    int count = selector.select(1000);
                    if(count>0){
                        Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                        while (iterator.hasNext()){
                            SelectionKey key = iterator.next();

                            if(key.isReadable()){
                                readData(key);
                            }

                            iterator.remove();
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
            state=3;
        }).start();
    }

    public void close() throws IOException {
        state=2;
        //自旋等待所有io处理完毕
        while (state==2){}
        for (SelectionKey key : selector.keys()) {
            key.channel().close();
            key.cancel();
        }
    }

    /**
     * 将完成连接的通道注册到读写选择器上
     * @param channel
     */
    public void bind(SocketChannel channel) throws ClosedChannelException {
        channel.register(selector,SelectionKey.OP_READ, ByteBuffer.allocate(1024));
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
                //正常关闭
                System.out.println(channel.getRemoteAddress()+" 客户端断开了");
                key.cancel();
                channel.close();
            }
        } catch (IOException e) {
            //客户端强行退出
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
            if (target!=self){
                System.out.println("send to a user");
                buffer = ByteBuffer.wrap(msg.getBytes());
                SocketChannel dest = (SocketChannel) target;
                dest.write(buffer);
            }
        }
    }

}