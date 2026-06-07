package pcd.lab01.hello;

public class Main {

	public static void main(String[] args) throws Exception {

		log("Hello from the main thread");
		
		log("Number of processors: " + Runtime.getRuntime().availableProcessors());
		
		log("Launching new threads...");

		var myThreadOne = new MyThread("ThreadOne");
		myThreadOne.start();		

		var myThreadTwo = new Thread(new MyRunnable("ThreadTwo")); // Altro modo per creare un thread passando per l'interfaccia
        // runnable, c'è questa possibilità per esempio se noi abbiamo una classe che ne estende già un'altra.
        // "Thread" è una classe da estendere, runnable un interfaccia da implementare che contiene un solo metodo "run".
        // In entrambi i casi comunque non chiamiamo mai da fuori il metodo "run", chiamiamo start, sono i thread sotto alla
        // JVM che si occupano di cercare il metodo run.
		myThreadTwo.start();		
		
		var t0 = System.currentTimeMillis(); // Millis va bene anche per valori assoluti, è possibile usare anche nanoSec,
        // ma va usato solo per misurare differenze non puo essere usato come valore assoluto, sotto c'è la JVM che è
        // performante ma lavore nell'ordine dei micro.
		
		var tnano = System.nanoTime();
		
		
		log("Threads spawned at: " + t0);
		
		log("Waiting for their termination.");

		/* launch also a clock thread, showing time elapsed */
		
		var clock = new SimpleClock(1000);
		clock.start();
		
		/* main thread blocks until the other threads terminate */
		
		myThreadOne.join(); // Ferma il main thread finchè threadOne e Two non sono finiti
		myThreadTwo.join();

		/* Notify clock to stop (deferred cancellation) -- "stop" method is deprecated */
		
		clock.notifyStop(); // Quando threadOne e Two sono finiti il main thread esegue questa istruzione, deve essere stat
        // definito un campo stopped volatile e definito cosa deve succedere nel metodo run della classe, non è un istruzione
        // che fa tutto in automatico
		
		var t1 = System.currentTimeMillis();
		log("Completed at " + t1 + " (" + (t1 - t0) + " secs)");
		
	}

	private static void log(String msg) {
		System.out.println("[ " + System.currentTimeMillis() +   " ][ " + Thread.currentThread().getName()+ " ] " + msg); 
	}
	
}
