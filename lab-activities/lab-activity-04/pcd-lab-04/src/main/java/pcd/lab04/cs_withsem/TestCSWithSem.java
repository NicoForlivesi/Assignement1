package pcd.lab04.cs_withsem;


// Semplice esempio di come implementare le sezioni critiche con i semafori, rimane comunque un costrutto di basso
// livello, per quanto riguarda l'assignment è più comodo usare i monitor.
import java.util.concurrent.Semaphore;

public class TestCSWithSem {

	public static void main(String[] args) {
		
		/* this is a strong semaphore, il secondo argomento a true indica che vogliamo un semaforo strong,
		* per strong si intende che i thread in attesa sono ordinati, quindi il metodo di accesso è una coda
		* FIFO, c'è fariness */
		Semaphore mutex = new Semaphore(1,true); // Semafori usati in ambito di sezione critica devono essere inizializati
        // ad 1, è un po come se quell'1 fosse il token che rappresenta il permesso di entrare, ce ne è solo 1 disponibile
        // e deve essere propriamente distribuito a turno sui vari thread.
        // Attenzione i semafori non sono rientranti: non viene tenuta traccia di chi ha il token in quel momento, quindi se
        // uno stesso thread fa due volte l'acquire, la prima volta procede la seconda si blocca anche se in realtà lo ha
        // già lui il token.
		
		new MyWorkerA("MyAgent-01", mutex).start();		
		new MyWorkerB("MyAgent-02", mutex).start();
		new MyWorkerC("MyAgent-03", mutex).start();
	}

}
