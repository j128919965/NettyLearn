package xyz.lizhaorong.nio;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

public class Main {

    public static void main(String[] args) {
        IntBuffer intBuffer = IntBuffer.allocate(5);

        for (int i = 0; i < intBuffer.capacity(); i++) {
            intBuffer.put(i*2);
        }
        //读写切换
        // 会导致标志切换
        intBuffer.flip();

        //是否有剩余
        while (intBuffer.hasRemaining()){
            System.out.println(intBuffer.get());
        }

    }
}
