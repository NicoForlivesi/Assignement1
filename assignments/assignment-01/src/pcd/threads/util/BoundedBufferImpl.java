package pcd.threads.util;

import java.util.LinkedList;

/**
 * Implementazione thread-safe di un buffer limitato. Mi servirà per far comunicarela View (che cattura i tasti)
 * con l'InputController (che li "consuma"), alla fine ci riconduciamo al classico produttore-consumatore.
 * */
public class BoundedBufferImpl<Item> implements BoundedBuffer<Item> {

    private final LinkedList<Item> buffer;
    private final int maxSize;

    public BoundedBufferImpl(int size) {
        this.buffer = new LinkedList<>();
        this.maxSize = size;
    }

    @Override
    public synchronized void put(Item item) throws InterruptedException {
        while (isFull()) {
            wait();
        }
        buffer.addLast(item);
        notifyAll();
    }

    @Override
    public synchronized Item get() throws InterruptedException {
        while (isEmpty()) {
            wait();
        }
        Item item = buffer.removeFirst();
        notifyAll();
        return item;
    }

    private boolean isFull() {
        return buffer.size() == maxSize;
    }

    private boolean isEmpty() {
        return buffer.isEmpty();
    }
}