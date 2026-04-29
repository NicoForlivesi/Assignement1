package pcd.lab07.vertx;

import io.vertx.core.*;

/**
 * 
 * Making a (short-term) blocking/sync call/task asynchronously
 * by using executeBlocking
 * Se in vertex voglio fare qualcosa di bloccante? Non possiamo bloccare l'eventloop, tramite il metodo .executeBlocking
 * diciamo esplicitamente che la lambda che passiamo deve essere eseguita su un background thread perchè il suo tempo di
 * computazione non è banale.
 */
class TestExecBlocking extends VerticleBase {

	// private int x = 0;
	
	public Future<?> start() throws Exception {
		log("before");

		Future<Integer> res = this.vertx.executeBlocking(() -> { // Questa lambda viene mandata in eseuzione su
            // un background thread, però attenzione normalmente il background thread è uno solo per ogni verticle,
            // se mandassimo due exeuteBlocking verrebbero messe in coda su un unico backgoround thread (vedi es Step6b)
			// Call some blocking API that takes a significant amount of time to return
            // Abbiamo detto che questa lambda viene mandata in esecuzione su un background thread, il che significa che
            // non deve essere possibile da questo codice modificare lo stato dell'event loop (quindi per esempio i campi
            // privati del verticle). Un background thread deve avere tutti i dati possibili per poter lavorare ma non deve
            // accedere alla memoria dell'event-loop senò si potrebbero verificare race condition.
			log("blocking computation started");
			try {
				Thread.sleep(5000);
				/* notify promise completion */
				return 100;
			} catch (Exception ex) {
				
				/* notify failure */
				throw new Exception("exception");
			}
		});

		log("after triggering a blocking computation...");
		// x++;

		res.onComplete((AsyncResult<Integer> r) -> {
			log("result: " + r.result());
		});
		
		res.onSuccess((flatResult) -> {
			log("result: " + flatResult);

		});
		return super.start();
	}

	private void log(String msg) {
		System.out.println("[ " + System.currentTimeMillis() + " ][ " + Thread.currentThread() + " ] " + msg);
	}
}

public class Step6_withblocking_simple {

	public static void main(String[] args) {
		Vertx vertx = Vertx.vertx();
		vertx.deployVerticle(new TestExecBlocking());
	}
}
