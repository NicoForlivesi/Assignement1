package pcd.lab04.ex01_synchwithsem;

/**
 * Unsynchronized version
 * 
 * @TODO make it sync 
 * @author aricci
 *
 */

/*
* In questo esempio vogliamo utilizzare i semafori per fare sincornizzazione, vogliamo una stampa di ping seguita da
* una stampa di pong e viceversa.
* Quando usiamo i semafori per sincronizzazione dobbiamo implementare un numero di semafori uguale al numero di eventi
* che ci sono, in questo caso sono due: "aspettando ping" ed "aspettando pong"
* */
public class TestPingPong {
	public static void main(String[] args) {

        var pingDoneEvent = new Semaphore(0); // Semafori evento sempre inizializzati a zero
        var pongDoneEvent = new Semaphore(0);
		new Pinger(pongDoneEvent, pingDoneEvent).start(); // Componente attivo che stampa continuamente ping
		new Ponger(pingDoneEvent, pongDoneEvent).start(); // Componente attivo che stampa continuamente pong

        // Serve dal main lanciare la prima relase sennò tutte e due sono fermi sull'acquire aspettando l'altro
        // Scelgo chi parte per primo...

        pongDoneEvent.release(); // Parte per primo ping
	}

}
