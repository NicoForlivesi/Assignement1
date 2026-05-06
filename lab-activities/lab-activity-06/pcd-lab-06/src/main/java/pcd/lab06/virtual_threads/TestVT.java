package pcd.lab06.virtual_threads;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * L'idea base per quanto riguarda il discorso applicativo dei VT è che l'API non deve cambiare
 * rispetto ai thread classici forniti da java
 */
class MyMonitor {
	private Lock lock;
	
	public MyMonitor() {
		lock = new ReentrantLock();
	}
	public void m() throws Exception {
		try {
			lock.lock();
			System.out.println("[" + Thread.currentThread().getName()+ "] Entered into m ");
			Thread.sleep(5000);
			System.out.println("[" + Thread.currentThread().getName()+ "] Going to exit from m ");
		} finally {
			lock.unlock();
		}
	}
}

public class TestVT {
	
	static void main(String[] args) throws Exception {

		log("Launching.. " + Thread.currentThread());
		
		MyMonitor mon = new MyMonitor();
		
		for (int i = 0; i <  10; i++) {
			Thread
			.ofVirtual() // Specifichiamo che vogliamo creare un virtual thread
			.name("myVirtualThread-"+i)
			.start(() -> {
				log("Hello from " + Thread.currentThread());
				try {
					mon.m();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			});
		}
		
		Thread.sleep(100000);
	}
	
	private static void log(String msg) {
		System.out.println(msg);
	}

}
