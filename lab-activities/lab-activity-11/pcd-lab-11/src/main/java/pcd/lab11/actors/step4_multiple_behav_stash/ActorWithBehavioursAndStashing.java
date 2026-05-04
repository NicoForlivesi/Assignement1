package pcd.lab11.actors.step4_multiple_behav_stash;
import org.apache.pekko.actor.AbstractActorWithStash;


public class ActorWithBehavioursAndStashing extends AbstractActorWithStash {

	private int state;
	
	/* Base behaviour */
	
	public ActorWithBehavioursAndStashing() {
		state = 0;
	}
	
	@Override
	public Receive createReceive() {
		return receiveBuilder()
				.match(MsgProtocol.MsgZero.class,this::onMsgZero)
				.match(MsgProtocol.MsgOne.class, (msg) -> { this.stash(); }) // Significa, parcheggia un attimo il messaggio
                // arrivato (Msg.One) che verrà poi ripreso in un secondo momento.
				.match(MsgProtocol.MsgTwo.class, (msg) -> { this.stash(); }) // Stessa cosa per MsgTwo
				.build();
	}

	private void onMsgZero(MsgProtocol.MsgZero msg) {
		log("msgZero - state: " + state);
		state++;
		this.unstashAll();
		this.getContext().become(receiverBehaviourA());
	}


	/* Behaviour A */

	public Receive receiverBehaviourA() {
		return receiveBuilder()
				.match(MsgProtocol.MsgOne.class,this::onMsgOne) // Qui vado a riprendere il MsgOne che avevo parcheggiato
				.match(MsgProtocol.MsgTwo.class, (msg) -> { this.stash(); })
				.build();
	}
	
	private void onMsgOne(MsgProtocol.MsgOne msg) {
		log("msgOne - state: " + state);	
		state++;
		this.unstashAll();
		this.getContext().become(receiverBehaviourB());
	}
	
	/* Behaviour B */
	
	public Receive receiverBehaviourB() {
		return receiveBuilder()
				.match(MsgProtocol.MsgTwo.class,this::onMsgTwo) // Prendo il MsgTwo che avevo parcheggiato
				.build();
	}

	private void onMsgTwo(MsgProtocol.MsgTwo msg) {
		log("msgTwo - state: " + state);		
		this.getContext().stop(this.getSelf());
	}


	private void log(String msg) {
		System.out.println("[ActorWithBehaviour] " + msg);
	}


	
}
