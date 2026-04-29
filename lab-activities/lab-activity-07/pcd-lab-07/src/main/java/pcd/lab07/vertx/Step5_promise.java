package pcd.lab07.vertx;

import io.vertx.core.*;


/**
 * 
 * Using promises (i.e. the inner side of Vert.x futures) 
 * Ogni volta che si fa un deploy di un verticle è come se si creasse un ulteriore event-loop, come comunicano
 * questi event-loop se ne sono presenti più di uno? NON possono (non devono meglio dire) condividere memoria
 * fra di loro, vedremo poi che quando vogliono comunicare fra di loro manderemo la comunicazione su un "EVENT-BUS"
 * l'event bus è una sottospecie di canale su cui possono essere in ascolto più event-loop.
 *
 * - Qui esaminiamo come fare se vogliamo fare noi una funzione asincrona che torna una future e non usare una
 * predefinita come nel caso precedente readFile, qui entra il concetto delle promise per vertx, che sono le future
 * più "interne", quindi esternamente con vertx questa tipologia di dato si chiama future e internamente promise.
 */
class VerticleWithPromise extends VerticleBase {
	
	public Future<?> start() throws Exception {
		log("started.");	
		var fut = this.getDelayedRandom(1000); // Chiamiamo il nostro metodo asincrono che abbiamo creato.
		fut.onComplete((res) -> { // Qui niente di strano, il metodo asincrono ritorna una
            // future e su quella chiamiamo .onComplete
			System.out.println("Result: " + res.result());	
		});
		return super.start();
	}

	/**
	 * 
	 * Implementing an async method using promises.
	 * 
	 * The method returns a random value after 
	 * some specified time (delay)
	 * 
	 * @param delay
	 * @return
     * Esempio di metodo asincrono che aspetta un tempo casuale
     * e restituisce un numero casuale in output.
     * In questa funzione ce la creazione ad hoc di una promise che creiamo noi, data una promise è possibile ottenere
     * la future corrispondente
	 */
	protected Future<Double> getDelayedRandom(int delay){
		Promise<Double> promise = Promise.promise(); // Creo una promise associata ad un Double
		this.vertx.setTimer(delay, (res) -> { // Versione ancora CPS di funzione asincrona
            //Callback quando la promise è stata completata
			var num = Math.random();
			promise.complete(num); // La promise che avevo costruito è stata completata non esiste questo metodo
            // .complete direttamente sulle future
		});
		return promise.future(); // Data una promise posso agganciarci la future corrispondente, la promise quindi
        // serve per gestire internamente questo concetto, la future invece è la parte che "interessa" all'utente.
        // Quindi si ritorna sempre la future associata alla promise.
	}
	
	private void log(String msg) {
		System.out.println("[ " + System.currentTimeMillis() + " ][ " + Thread.currentThread() + " ] " + msg);
	}
}

public class Step5_promise {
	public static void main(String[] args) {
		
		Vertx vertx = Vertx.vertx();
		vertx.deployVerticle(new VerticleWithPromise());
		
	}
}

