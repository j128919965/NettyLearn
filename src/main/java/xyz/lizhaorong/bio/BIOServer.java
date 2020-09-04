package xyz.lizhaorong.bio;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BIOServer {
    public static void main(String[] args) throws IOException {
        //线程池

        //思路：建立一个线程池
        //如果有客户端连接，就创建一个线程和它对应
        ExecutorService pool = Executors.newCachedThreadPool();
        ServerSocket serverSocket = new ServerSocket(6666);
        System.out.println("服务器启动了");
        while (true){
            Socket socket = serverSocket.accept();
            System.out.println("收到连接");
            pool.execute(new HttpHandler(socket));
        }
    }

    private static class HttpHandler implements Runnable{
        Socket socket;

        public HttpHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            byte[] bytes = new byte[1024];
            try {
                InputStream is = socket.getInputStream();
                int size;
                while((size=is.read(bytes))!=-1){
                    System.out.println(new String(bytes,0,size));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }finally {
                close(socket);
            }
        }

        private static void close(Closeable... closeables){
            if(closeables!=null){
                for (Closeable closeable : closeables) {
                    try {
                        closeable.close();
                    }catch (Exception ignored){}
                }
            }
        }
    }
}
