package pcd.lab03.cs_raw;

public class TestCSWithRawSynchBlocks {

	public static void main(String[] args) {

		var lock = new Object(); // Creo un oggetto generico per definire l'oggetto su cui i due worker devono
        // prendere il lock per eseguire il codice in sezione critica.


		// Quando il workerB per esempio è in sezione critica, questo non impedisce il workerA dall'eseguire
        // il proprio codice che non è in sezione critica.
		new MyWorkerB("MyWorker-01", lock).start();
		new MyWorkerA("MyWorker-02", lock).start();		
	
	}

}
