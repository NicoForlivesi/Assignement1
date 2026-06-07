package pcd.lab05.monitors;
/**
 * Piccla introduzione sui monitor:
 * Per sfruttare i monitor abbiamo due strade, o utilizzare i costrutti base introddi da java: synchornized, wait
 * notify e notifyAll che sono tutti metodi chiamabili su qualsiasi oggetto*/
public interface BoundedBuffer<Item> {

    void put(Item item) throws InterruptedException;
    
    Item get() throws InterruptedException;
    
}
