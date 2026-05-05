package pcd.lab08.rx;

import io.reactivex.rxjava3.core.*;

// Come creare flussi personalizzati (che non partono da strutture dati predefinite)
public class Test02a_creation_synch {

	public static void main(String[] args){
		
	    log("Creating a observable (cold).");

	    Observable<Integer> source = Observable.create(emitter -> {
	        for (int i = 0; i <= 2; i++) { // Specifico all'observable che oggetto deve usare per comporre il flusso
                // ovvero emitter
	            log("source: " + i);
	            emitter.onNext(i);
	        }
	        emitter.onComplete(); // La lambda viene effettivamente eseguita sempre quando chiamiamo .subscribe,
            // Si capisce ciò dal fatto che "Subscribing A" viene stampato prima del log dentro il for.
            // Notare che però è sempre lo stesso thread che esegue le due stampe
	    });

	    log("Subscribing A");
	    
	    source.subscribe(v -> log("A: "+v));

	    log("Subscribing B");
	    
	    source.subscribe(v -> log("B: "+v));

	}
	
	static private void log(String msg) {
		System.out.println("[ " + Thread.currentThread().getName() + " ] " + msg);
	}
}
