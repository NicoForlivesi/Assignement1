package pcd.lab05.monitors;

import java.util.LinkedList;

/**
 * 
 * Simple implementation of a bounded buffer
 * as a monitor, using raw mechanisms (ovvero senza la libreria), in questo approccio abbiamo detto
 * non è possibile usare più condition variable anche se in questo caso ci servirebbero, una per fermare i produttori
 * se il buffer è pieno e l'altra per fermare i consumatori se il buffer è vuoto.
 * In realtà qui potendone usare solo una, quando facciamo notifyAll, svegliamo tutti anche il gruppo che in quel
 * momento non ci interessa.
 * 
 * @param <Item>
 */
public class BoundedBufferImplRaw<Item> implements BoundedBuffer<Item> {

	private LinkedList<Item> buffer;
	private int maxSize;

	public BoundedBufferImplRaw(int size) {
		buffer = new LinkedList<Item>();
		maxSize = size;
	}

	public synchronized void put(Item item) throws InterruptedException {
		while (isFull()) {
			wait();
		}
		buffer.addLast(item);
		notifyAll();
	}

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
		return buffer.size() == 0;
	}
}
