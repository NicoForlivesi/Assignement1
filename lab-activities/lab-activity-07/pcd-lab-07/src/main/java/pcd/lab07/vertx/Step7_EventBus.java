package pcd.lab07.vertx;

import io.vertx.core.*;
import io.vertx.core.eventbus.EventBus;

class MyAgent1 extends VerticleBase {
	
	 public Future<?> start() throws Exception {
		log("started.");
		EventBus eb = vertx.eventBus(); // eventBus condiviso su tutti i vertical, l'eventBus ha l'architettura
         // produttori-consumatori
		eb.consumer(Step7_EventBus.TOPIC_NAME, message -> { // Topic-name indica il "canale" su cui
            // vogliamo ascoltare, possono essere presenti più canali
			log("received new message: " + message.body());
		});		
		log("Ready.");
		return super.start();
	}

	private void log(String msg) {
		System.out.println("[ " + System.currentTimeMillis() + " ][ Agent1 ] " + msg);
	}
}

class MyAgent2 extends AbstractVerticle {
	
	public void start() {
		log("started.");
		EventBus eb = this.getVertx().eventBus();
		var msg = "test";
		log("sending the message: " + msg + " on the event bus (topic: " + Step7_EventBus.TOPIC_NAME + ")...");
		eb.publish(Step7_EventBus.TOPIC_NAME, msg);
	}

	private void log(String msg) {
		System.out.println("[ " + System.currentTimeMillis() + " ][ Agent2 ] " + msg);
	}
}

/**
 *  
 *  Using the event bus for reactive agents communication and interaction. 
 * Qui andiamo a creare due event-loop e vediamo come questi possono comunicare, come abbiamo detto non devono avere
 * memoria condivisa
 */
public class Step7_EventBus {

	static final String TOPIC_NAME = "my-topic";

	public static void main(String[] args) {
		Vertx  vertx = Vertx.vertx();
		vertx
		.deployVerticle(new MyAgent1())
		.onSuccess(res -> {
			/* deploy the second verticle only when the first has completed */
			vertx.deployVerticle(new MyAgent2());
		});
	}
}

