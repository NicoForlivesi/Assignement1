package pcd.lab08.rx;

import io.reactivex.rxjava3.core.*;
// La programmazione reattiva da un certo punto di vista rappresenta una generalizzazione della programmazione
// asincrona in cui non abbiamo un solo risultato ma diversi nel tempo.
public class Test02c_creation_fromCallable {

	public static void main(String[] args) throws Exception {

		System.out.println("\n=== TEST fromCallable | main thread ===\n");

		Flowable.fromCallable(() -> {
		    log("started.");
		    Thread.sleep(1000); 
		    log("completed.");
		    return "Done";
		}).subscribe(s -> {
			log("result: " + s);
		});

		
		Thread.sleep(2000); // <--- wait for the flow to finish
	}
	
	static private void log(String msg) {
		System.out.println("[ " + Thread.currentThread().getName() + " ] " + msg);
	}

}
