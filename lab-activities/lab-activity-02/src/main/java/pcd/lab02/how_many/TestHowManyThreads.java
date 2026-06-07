package pcd.lab02.how_many;

import java.util.concurrent.atomic.AtomicInteger;

public class TestHowManyThreads {
	
	public static void main(String[] args) {
		AtomicInteger counter = new AtomicInteger(0);			
		try {
			while (true) {
				new Thread(() -> {
					System.out.println("Hello from " + Thread.currentThread().getName());
					counter.addAndGet(1);
					try {
						Thread.sleep(5000); // Se non mettessimo la sleep creerebbe un numero infinito, perchè in realtà
                        // viene creato un thread e termina e torna subito a disposizione è un po come se venissero ciclati.
                        // Con questo esempio vediamo quanti thread operativi (effettivamente attivi)
                        // riusciamo a creare al massimo.
					} catch (Exception ex) {}
				}).start();
			}
		} catch (java.lang.OutOfMemoryError ex) {
			// ex.printStackTrace();
		}
		System.out.println("Limit reached - num threads created: " + counter.intValue());
	}

}
