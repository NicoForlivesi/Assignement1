package pcd.jpf;

import pcd.threads.util.BoundedBufferImpl;
import pcd.threads.util.BoundedBuffer;

public class TestBoundedBuffer {

    public static void main(String[] args) throws InterruptedException {
        BoundedBuffer<Integer> buffer = new BoundedBufferImpl<>(2);

        Thread producer = new Thread(() -> {
            try {
                buffer.put(1);
                buffer.put(2);
                buffer.put(3);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        Thread consumer = new Thread(() -> {
            try {
                buffer.get();
                buffer.get();
                buffer.get();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        producer.start();
        consumer.start();
        producer.join();
        consumer.join();
    }
}