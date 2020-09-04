package xyz.lizhaorong.nio;

import java.io.*;
import java.nio.*;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.*;

/**
 * Channel
 * 通道
 * 用于源节点与目标节点的链接。
 * java.nio.Channels
 * @see FileChannel
 */
public class TransferFileTest {

    public static void main(String[] args) throws IOException {
        t5();
//        t1();
//        t2();
//        t3();
//        t4();
    }


    public static void t5(){
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        System.out.println(buffer.hasArray());

    }


    public static void t1() throws IOException {
        FileChannel in = FileChannel.open(Paths.get("src/File1.txt"), StandardOpenOption.READ);
        FileChannel out = FileChannel.open(Paths.get("o2.txt"), StandardOpenOption.READ,StandardOpenOption.WRITE,StandardOpenOption.CREATE);

        MappedByteBuffer inBuffer = in.map(FileChannel.MapMode.READ_ONLY,0,in.size());
        MappedByteBuffer outBuffer = out.map(FileChannel.MapMode.READ_WRITE,0,in.size());

        byte[] dst = new byte[inBuffer.limit()];

        long start = System.currentTimeMillis();
        inBuffer.get(dst);
        outBuffer.put(dst);

        in.close();
        out.close();

        long end = System.currentTimeMillis();
        System.out.println(end-start);

    }

    public static void t2() throws IOException{


        //利用通道完成文件复制
        FileInputStream fis = new FileInputStream("src/File1.txt");
        FileOutputStream fos = new FileOutputStream("output.java");

        FileChannel inChannel = fis.getChannel();
        FileChannel outChannel = fos.getChannel();

        ByteBuffer buf = ByteBuffer.allocateDirect(1024);

        long start = System.currentTimeMillis();
        while(inChannel.read(buf)!=-1){
            //切换成读取模式
            buf.flip();
            outChannel.write(buf);
            buf.clear();
        }
        outChannel.close();
        inChannel.close();
        fis.close();
        fos.close();

        long end = System.currentTimeMillis();
        System.out.println(end-start);
    }



    public static void t3() throws IOException{

        //普通的bufferedStream

        BufferedInputStream fis = new BufferedInputStream(new FileInputStream("src/File1.txt"));
        BufferedOutputStream fos = new BufferedOutputStream(new FileOutputStream("o3.java"));

        byte[] buf = new byte[1024];
        //ByteBuffer buf = ByteBuffer.allocateDirect(4096);

        long start = System.currentTimeMillis();
        while(fis.read(buf)!=-1){
            fos.write(buf);
        }
        fos.flush();;

        fis.close();
        fos.close();;

        long end = System.currentTimeMillis();
        System.out.println(end-start);
    }

    public static void t4() throws IOException {

        //直接transfer

        FileChannel in = FileChannel.open(Paths.get("src/File1.txt"), StandardOpenOption.READ);
        FileChannel out = FileChannel.open(Paths.get("o4.txt"),StandardOpenOption.READ,StandardOpenOption.WRITE,StandardOpenOption.CREATE);

        long start = System.currentTimeMillis();
        in.transferTo(0,in.size(),out);

        in.close();
        out.close();

        long end = System.currentTimeMillis();
        System.out.println(end-start);

    }
}
