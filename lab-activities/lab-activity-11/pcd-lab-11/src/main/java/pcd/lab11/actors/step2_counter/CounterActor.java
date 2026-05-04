package pcd.lab11.actors.step2_counter;

import static pcd.lab11.actors.step2_counter.CounterMsgProtocol.*;

import org.apache.pekko.actor.*;

// Esempio di come modellare un contatore come un attore
public class CounterActor extends AbstractActor {

	private int count;
	
	private CounterActor() {
		count = 0;
	}

	public Receive createReceive() {
		return receiveBuilder()
				.match(IncMsg.class, this::onIncMsg)
				.match(GetValueMsg.class, this::onGetValueMsg)
	            .build();
	}

	private void onIncMsg(IncMsg msg) {
		count++;
	}

	private void onGetValueMsg(GetValueMsg msg) {
		msg.replyTo().tell(new CounterValueMsg(count), this.getSelf());
	} //
    // Ovviamente non è possibile mettere un "return" per rispondere all'attore che ci ha chiesto un valore dobbiamo
    // usare un replyTo e rispondere all'attore che ha fatto la domanda.
	
}
