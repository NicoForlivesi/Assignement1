package pcd.lab08.rx;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;

//

public class Test03a_sched_subscribeon {

	public static void main(String[] args) throws Exception {

		System.out.println("\n=== TEST No schedulers ===\n");
		
		/*
		 * Without using schedulers, by default all the computation 
		 * is done by the calling thread.
		 * 
		 */
		Observable
			.just(100)	
			.map(v -> { log("map 1 " + v); return v * v; })
			.map(v -> { log("map 2 " + v); return v + 1; })
			.subscribe(v -> {						
				log("sub " + v);
			}); // Come abbiamo visto fin ora, tutto eseguito sul main thread
		
		System.out.println("\n=== TEST subscribeOn ===\n");

		/* 
		 * subscribeOn:
		 * 
		 * move the computational work of a flow (Observable) on a specified scheduler
		 */
		Observable<Integer> src = Observable
		    .just(100)	
			.map(v -> { log("map 1 " + v); return v * v; })		
			.map(v -> { log("map 2 " + v); return v + 1; });		

		src
			.subscribeOn(Schedulers.computation()) /* run the observable on a worker, stiamo dicendo, la pipeline descritta
			 qui sopra per questo flusso, falla eseguire da dei worker, non dal main thread.
			 Schedulers.computation serve per usare automaticamente un numero di worker per fare computazioni matematiche
			 in modo efficente.*/
			.subscribe(v -> {									
				log("sub 1 " + v);
			});

		src
			.subscribeOn(Schedulers.computation()) /* run the observable on another worker */		
			.subscribe(v -> {									
				log("sub 2 " + v);
			});

		Thread.sleep(100);
		
		System.out.println("\n=== TEST parallelism  ===\n");

		/* 
		 * Running independent flows on a different scheduler 
		 * and merging their results back into a single flow 
		 * warning: flatMap => no order in merging
		 *
		 * Esempio di riferimento.
		 * flatMap da usare quando abbiamo flussi di flussi.
		 */

		Flowable.range(1, 10)
		  .flatMap(v -> // Risultato del merge di tutti i flussi
		      Flowable.just(v) // Per ogni valore da 1 a 10 genero un nuovo flusso
		      	.subscribeOn(Schedulers.computation()) /* each flowable has its own thread, ogni flusso gestito da
		      	 thread diversi.*/
				.map(w -> { 
					log("map " + w); 
					return w * w; 
				})		
		  )
		  .blockingSubscribe(v -> { // Come prima voglio fare la subscribe, ma ora l'osservazione dei flussi che
              // vengono generati dinamicamente la voglio fare con un unico thread, nello specifico usa per osservarli lo
              // stesso thread che chiama .blockingsubscribe (in questo caso il main)
              // Visto che i flussi sono stati generati parallelamente da thread diversi, voglio che l'osservazione venga
              // fatta dallo stesso thread
			 log("sub > " + v); 
		  });
		
	}
		
	static private void log(String msg) {
		System.out.println("[" + Thread.currentThread().getName() + "] " + msg);
	}
	
}
