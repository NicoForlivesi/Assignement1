package pcd.lab08.rx;

import io.reactivex.rxjava3.core.*;

public class Test02b_creation_async {

	public static void main(String[] args) throws Exception {

		log("Creating an observable (cold) using its own thread.");

        // Cosa succede se dentro create creo un thread che genera i primi 20 elementi e li metto nel flusso?
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
		
		source.subscribe((s) -> {
			log("Subscriber A: " + s); 
		});	

		// Thread.sleep(1000);

		log("Subscribing B.");

		source.subscribe((s) -> {
			log("Subscriber B: " + s); 
		});	

		log("Done.");
	}
	// Lo stream è cold, visto che ho due .subscription vengono creati due thread diversi
	static private void log(String msg) {
		System.out.println("[ " + Thread.currentThread().getName() + "  ] " + msg);
	}

}
