package pcd.lab03.cs_withlocks;

import java.util.concurrent.locks.*;

public class TestCSWithLocks {
	public static void main(String[] args) {
		Lock lock = new ReentrantLock(); // Lock definito usando la libreria di java
        // Si usa sempre un istanza di lock rientrante, per lock rientrante intendiamo che se uno stesso thread
        // cerca di riprendere il lock su un oggetto di cui già dispone il lock lo si lascia passare.
        // Anche synchronized funziona così
		new MyWorkerB("MyAgent-01", lock).start();
		new MyWorkerA("MyAgent-02", lock).start();		
	}
}
