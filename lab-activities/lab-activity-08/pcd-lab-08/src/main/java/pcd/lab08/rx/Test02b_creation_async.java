package pcd.lab08.rx;

import io.reactivex.rxjava3.core.*;

public class Test02b_creation_async {

	public static void main(String[] args) throws Exception {

		log("Creating an observable (cold) using its own thread.");

        // Cosa succede se dentro create nella lambda creo un thread che genera i primi 20 elementi e li metto nel flusso?
		Observable<Integer> source = Observable.create(emitter -> {		     
			new Thread(() -> {
				int i = 0;
				while (i < 20){
					try {
						log("source: "+i); 
						emitter.onNext(i);
						Thread.sleep(200);
						i++;
					} catch (Exception ex){}
				}
				emitter.onComplete();
			}).start();
		 });

		Thread.sleep(1000);
		
		log("Subscribing A.");
		
		source.subscribe((s) -> { // Facendo .subscribe parte un nuovo thread
			log("Subscriber A: " + s); 
		});	

		// Thread.sleep(1000);

		log("Subscribing B.");

		source.subscribe((s) -> { // nuovo thread ancora
			log("Subscriber B: " + s); 
		});	

		log("Done.");
        // Il comportamento è simile a quello di prima ma qui la lambda di create viene eseguita da un thread,
        // che sarà un thread "nuovo" per ogni subscribe chiamata.
        // Qui quindi abbiamo 3 thread: il main e i due creati con le due subscribe
	}
	// Lo stream è cold, visto che ho due .subscription vengono creati due thread diversi
	static private void log(String msg) {
		System.out.println("[ " + Thread.currentThread().getName() + "  ] " + msg);
	}

}
