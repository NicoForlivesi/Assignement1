package pcd.lab06.virtual_threads;

import java.time.Duration;
import java.util.ArrayList;
/**
 * In questo esempio creiamo 100 000 VT, il compito è delegato alla JVM, non interviene il sistema
 * operativo in questo processo, sono veramente leggeri e per questo è possibile farlo.
 * Fare la stessa operazione coi thread classici sarebbe impossibile (errore out of memory) visto che ogni
 * thread in senso classico occupa uno spazio prefissato sullo stack.
 */
public class TestScalabilityVT {

	public static void main(String[] args) {
		var t0 = System.currentTimeMillis();
		var list = new ArrayList<Thread>();
		for (var i = 0; i < 100_000; i++) {
			
			Thread t = Thread.ofVirtual().unstarted(() -> { // Mettendo .unstarted ci ritorna un oggetto
                // (thread virtuale) pronto per essere mandato in esecuzione con t.start, il cui comportamento
                // è la lambda specificata dentro .unstarted (in questo caso Thread.sleep(Duration.ofSeconds(1))
				try {
					Thread.sleep(Duration.ofSeconds(1));
				} catch (Exception ex) {
				}
			});
			/*
			Thread t = new Thread(() -> {
				try {
					Thread.sleep(Duration.ofSeconds(1));
				} catch (Exception ex) {
				}
			});
			*/
			t.start();
			list.add(t);
		}
		
		list.forEach(t -> {
			try {
				t.join();
			} catch (Exception ex) {};
		});

		var t1 = System.currentTimeMillis();
		System.out.println("Time elapsed: " + (t1 - t0));

	}

}
