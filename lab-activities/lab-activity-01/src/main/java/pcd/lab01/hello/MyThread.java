package pcd.lab01.hello;

public class MyThread extends Thread {

	public MyThread(String myName){
		super(myName);
	}
	
	public void run(){ // Classi che estendono la classe fornita da java "Thread" devono definire un metodo
        // "run" che contiene il comportamento attivo del componente, è come se creassimo un nuovo flusso che
        // lavora in parallelo rispetto al flusso principale e qui dentro scriviamo cosa deve fare questo nuovo flusso.
        // Per avviare un thread si crea un un oggetto di MyThread e si chiama "nome_oggetto".start(); Questa chiamata
        // avvierà il comportamento attivo descritto dentro questo metodo run. Sotto il SO manda in esecuzione questo
        // flusso su un altro core del processore dovendo lavorare in parallelo.
		log("Hello concurrent world!");
		log("Sleeping for 5 secs...");
		try {
			Thread.sleep(5000);
		} catch (InterruptedException ex) {
			ex.printStackTrace();
		}
		log("Done.");
	}
	
	private void log(String msg) {
		System.out.println("[ " + System.currentTimeMillis() +   " ][ " + getName()+ " ] " + msg); 
	}
}
