package pcd.lab07.vertx;

import io.vertx.core.*;


/**
 * 
 * Using promises (i.e. the inner side of Vert.x futures) 
 * 
 */
class VerticleWithPromise extends VerticleBase {
	
	public Future<?> start() throws Exception {
		log("started.");	
		var fut = this.getDelayedRandom(1000); // Restituisce subito la feature
		fut.onComplete((res) -> {
			System.out.println("Result: " + res.result());	
		});
		return  super.start();
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
		Promise<Double> promise = Promise.promise();
		this.vertx.setTimer(delay, (res) -> {
            //Callback quando la promise è stata completata
			var num = Math.random();
			promise.complete(num);
		});
		return promise.future(); // Restituisco subito la Future (associata alla promise)
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

