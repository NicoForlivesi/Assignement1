package pcd.lab11.actors.step0_hello;

import org.apache.pekko.actor.AbstractActor;

import pcd.lab11.actors.step0_hello.HelloWorldMsgProtocol.*;

public class HelloWorldActor extends AbstractActor {

	private int helloCounter;
	
	/* configure message handlers */
	
	public Receive createReceive() {
		return receiveBuilder()
				.match(SayHello.class, this::onSayHello) // Quando ricevo un messaggio del tipo SayHello -> manda in
                // esecuzione l'handler "onSayHello", notare che è un metodo privato della classe, in generale l'handler
                // va fatto così
	            .build();
	}

	/* message handlers */
	
	private void onSayHello(SayHello msg) {
 	   helloCounter++;
 	   System.out.println("Hello " + msg.content() + " from " + this.getContext().getSelf() + " - count " + helloCounter);	
	}
}