package pcd.threads.util;

public interface BoundedBuffer<Item> {
    void put(Item item) throws InterruptedException;
    Item get() throws InterruptedException;
}