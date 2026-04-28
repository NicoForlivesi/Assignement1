package pcd.lab07.vertx;

import io.vertx.core.AsyncResult;
import io.vertx.core.*;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.file.FileSystem;

/**
 * 
 * Simple Verticle - i.e. an event-loop in Vert.x
 * L'uso di Verticle ci consente di compiere un incapsulamento efficiente: definiamo il componente attivo-reattivo,
 * ovvero un istanza della classe "MyReactiveAgent" che voglio associare ad un singolo event-loop.
 */
class MyReactiveAgent extends VerticleBase { // Deve estendere questa classe, il punto chiave è che so con certezza che
    // questo codice verrà eseguito sempre e solo da un event-loop, a differenza dell'esempio precedente qui è molto
    // più chiaro chi esegue cosa.
    // Qui è presente solo il metodo start e stop ma in generale ci metterò tutto il codice che so che dovrà essere
    // eseguito da questo event-loop
	
	  private int cycle;
	
	  // Called when verticle is deployed
	  public Future<?> start() throws Exception { // Future è l'equivalente di una promise, non è la future
          // di java.util che è bloccante, qui non c'è niente di bloccante
		cycle = 0;
	
		log("1 (cycle: " + cycle + ") - doing the async call...");
		
		FileSystem fs = this.vertx.fileSystem(); // Qui l'event-loop è già definito e ci si accede mediante "this.vertx"
		Future<Buffer> f1 = fs.readFile("hello.md");
		f1.onComplete((AsyncResult<Buffer> res) -> {
			cycle++;			
			log("4 (cycle: " + cycle + ") - hello.md \n" + res.result().toString());
		});
	
		log("2 (cycle: " + cycle + ")- doing the second async call...");

		fs
		.readFile("pom.xml")
		.onComplete((AsyncResult<Buffer> res) -> {
			cycle++;
			log("4 (cycle: " + cycle + ") - POM \n" + res.result().toString().substring(0,160));
		});
		
		log("3 (cycle: " + cycle + ") - done");
	    return super.start();
        /*
        * Notare che vengono fatte 2 chiamate asincrone per ogni ciclo di start, è importante sottolineare che si,
        * l'event-loop è uno solo (un solo "coordinatore") ma sono presenti tanti background threads (worker threads)
        * che si occupano di servire le chiamate asincrone (leggere i due file)
        * e di aggiungere l'evento che la chiamata produce nella coda dell'event-loop.
        * In questo caso la lettura dei due file viene fatta da due background thread diversi e ognuno
        * quando ha finito aggiunge l'evento nella coda dell'event loop, qui non abbiamo garanzia sul quale dei due finirà prima,
        * il comportamento è infatti non deterministico, la garanzia è che tutto quello che viene eseguito da un handler
        * è atomico e che ciò che specifichiamo nella callback viene eseguito nel ciclo dopo, però se facciamo due
        * chiamate asincrone nello stesso ciclo abbiamo non determinismo.
        *
        * Punto importante: Il campo cycle viene incrementato da entrambe le callback, ciò potrebbe portare a corse
        * critiche? NO!!! L'event-loop garantisce che ad ogni ciclo un solo evento verrà servito, potrebbe essere che
        * entrambi gli eventi sono pronti ma comunque l'event-loop segue l'ordine degli eventi nel suo buffer, quindi
        * il secondo verrà eseguito al ciclo successivo anche se già pronto. Il punto chiave per evitare corse critiche è
        * che i background threads che lavorano non devono accedere a strutture dati dell'event-loop perchè loro si che
        * lavorano concorrentemente, ma l'esecuzione dei callback da parte dell'event-loop è fatta in maniera sequenziale.
        * */
	  }

	  // Optional - called when verticle is un-deployed
	  public Future<?> stop() throws Exception {
	    return super.stop();
	  }

	private void log(String msg) {
		System.out.println("[ " + System.currentTimeMillis() + " ][ " + Thread.currentThread() + " ] " + msg);
	}
}

public class Step2_withverticle {

	public static void main(String[] args) {
		Vertx  vertx = Vertx.vertx();
		vertx
		.deployVerticle(new MyReactiveAgent()) // .deployVerticle fa il deployment di un nuovo event-loop.
                /* Piccola parentesi: deployVerticle in realtà è a sua volta un metodo asincrono che restituisce una
                * promise (future per vertx) che è proprio quella che restituisce il metodo start di "MyReactiveAgent",
                * questo comportamento serve per poter agganciare al deployment il metodo ".onSuccess" per indicare
                * che il deployment è avvenuto con successo */
		.onSuccess(res -> {
			log("Reactive agent deployed.");
		});
	}

	static private void log(String msg) {
		System.out.println("[ " + System.currentTimeMillis() + " ][ " + Thread.currentThread() + " ] " + msg);
	}
}

