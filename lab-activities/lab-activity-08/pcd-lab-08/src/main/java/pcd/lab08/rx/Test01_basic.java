package pcd.lab08.rx;

import java.util.Arrays;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Observable;

/*
* Il punto chiave per quanto riguarda RX è il pensare tutto come un flusso di dati asincrono,
* con la possibilità di creare nuovi flussi di dati partendo da flussi di dati esistenti e
* mergiare flussi di dati esistenti in uno nuovo, il punto principale è che NON sono presenti side-effect.
* L'idea è che a basso livello non vediamo come vengono distribuiti i thread, si ragiona tutto sulla
* base di questi flussi asincroni, senza preoccuparci di come le singole computazoni vengono distribuite
* dalla CPU.*/

public class Test01_basic {

	public static void main(String[] args){
				
		log("creating with just.");
		
	    Observable // Classe di riferimento che rappresenta un flusso asincrono
	    .just("Hello world") // Creo un flusso che ha un singolo elemento: Una stringa "Hello World".
                // just è una factory: crea un flusso che ha un solo elemento.
	    .subscribe(s -> {	 // Prende tutti gli elementi presenti nell'Observable ed esegue la lambda
	    		log(s);    	// subscribe è un metodo sincrono! Dall'output si vede subito che è il main che esegue il log
       // è un metodo che fa pull sugi elementi dell'observable e fa eseguire la lambda direttamente dal main thread.
       // Chiamiamo questa tipologia di observable "cold observable", vedremo poi gli "hot".
	    });
	    
	    // with inline method
	    
	    Flowable.just("Hello world") // Flowable è come un observable ma supporta strutture di gestione del
                // flusso più articolate, come la frequenza di push e pull sul flusso
	    	.subscribe(System.out::println);
	    
		// creating a flow (an observable stream) from a static collection
		
	    // simple subscription 
	    
		String[] words = { "Hello", " ", "World", "!" }; 
		
		Flowable.fromArray(words) // Creazione di un flowable direttamente da un array
			.subscribe((String s) -> {
				log(s);
			});
		
		// full subscription: onNext(), onError(), onCompleted()
		
		log("Full subscription...");
		
		Observable.fromArray(words)
			.subscribe((String s) -> {
				log("> " + s);
			},(Throwable t) -> { // Cosa fare in caso di errore.
				log("error  " + t);
			},() -> { // Questa terza lambda indica cosa vogliamo fare quando lo stream viene chiuso, per come sono
                // definiti gli stream devono avere sempre una chiusura finale
				log("completed");
			});
		
		// operators

		log("simple application of operators");
		
		Flowable<Integer> flow = 
		Flowable
			.range(1, 20)
			.map(v -> v * v)
			.filter(v -> v % 3 == 0);
        // Arrivati qui in realtà flow non ha ancora generato gli elementi, è un oggetto che è in grado di generarli,
        // ma verranno effettivamente generati solo quando viene fornita la subscribe.
		
		log("first subscription #1");
		flow.subscribe(System.out::println); // In realtà gli elementi vengono effettivamente generati solo quando chiamo
        // .subscribe, finchè scrivo solo Flowable sto solo defininend il generatore

		log("first subscription #2");
		flow.subscribe((v) -> {
			log("" + v);
		});

		// doOnNext for debugging... (molto utile per il debbuging)
		
		log("showing the flow...");
		
		Flowable.range(1, 20)
			.doOnNext(v -> log("1> " + v)) // Faccio questa "catena" per ogni elemento del flusso
                // una volta arrivato a subscribe (o se la filter restituisce false passo all'element successivo
			.map(v -> v * v)
			.doOnNext(v -> log("2> " + v))
			.filter(v -> v % 3 == 0)
			.doOnNext(v -> log("3> " + v))
			.subscribe(System.out::println);
						
		
		// simple composition
		
		log("simple composition");
		
		Observable<String> src1 = Observable.fromIterable(Arrays.asList(
				 "the",
				 "quick",
				 "brown",
				 "fox",
				 "jumped",
				 "over",
				 "the",
				 "lazy",
				 "dog"
				));

		Observable<Integer> src2 = Observable.range(1, 5);
		
		src1
			.zipWith(src2, (string, count) -> String.format("%2d. %s", count, string))
                // zipWith fa l'unione dei due fussi mettendo assieme i sinoli elementi e restituendo un nuovo
                // flusso, visto che src2 ha meno elementi di src1, gli elementi sgaffi non vengono presi in considerazione
                // nel nuovo flusso.
                /*
                1. the
                2. quick
                3. brown
                4. fox
                5. jumped
                */
			.subscribe(System.out::println);
		
	}
	
	private static void log(String msg) {
		System.out.println("[" + Thread.currentThread().getName() + "] " + msg);
	}
}

