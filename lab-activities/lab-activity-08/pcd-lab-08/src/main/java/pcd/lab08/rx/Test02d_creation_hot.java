package pcd.lab08.rx;

import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.flowables.ConnectableFlowable;
import io.reactivex.rxjava3.observables.ConnectableObservable;
/*
* Introduciamo i flussi "hot", tutti quelli visti negli esempi precedenti erano "cold", ovvero la generazione del
* flusso partiva solo alla chiamata del metodo subscribe.
* Qui la generazione parte */


public class Test02d_creation_hot {

	public static void main(String[] args) throws Exception {

		System.out.println("\n=== TEST Hot streams  ===\n");

        // Si creano i flussi come prima con create
		Observable<Integer> source = Observable.create(emitter -> {		     
			new Thread(() -> {
				int i = 0;
				while (i < 200){
					try {
						log("source: "+i); 
						emitter.onNext(i);
						Thread.sleep(10);
						i++;
					} catch (Exception ex){}
				}
			}).start();
		     //emitter.setCancellable(c::close);
		 });

        // Chiamando .publish si crea un flusso "HOT"
        // è la sequenza di comandi da usare per creare un flusso hot.
		ConnectableObservable<Integer> hotObservable = source.publish();
		hotObservable.connect(); // Qui il flusso hot inizia a comporsi, anche se non ci sono ancora subscribe, dopo
        // verrà chiamata .subscribe ma gli elementi già generati prima andranno persi
	
		/* give time for producing some data before any subscription */
		Thread.sleep(500);
		
		log("Subscribing A.");
		
		hotObservable.subscribe((s) -> {
			log("subscriber A: "+s);
			// Thread.sleep(5000);
		});	
		
		/* give time for producing some data before second subscriber */
		Thread.sleep(500);
		
		log("Subscribing B.");
		
		hotObservable.subscribe((s) -> { // Qui arriva un altro subscriber che si è perso tutti gli elementi iniziali
            // più quelli che A invece aveva catturato.
			log("subscriber B: "+s); 
		});	
		
		log("Done.");
		
		Thread.sleep(10_000);

	}
	/*
	* Notare che il thread che genera gli elementi è lo stesso che esegue le subscribe, quindi se una lambda dentro una
	* subscribe si inloppa, gli elementi non vengono più generati, c'è ancora questo accoppiamento che rimane un
	* problema da affrontare...
	* */
	static private void log(String msg) {
		System.out.println("[ " + Thread.currentThread().getName() + "  ] " + msg);
	}
	

}
