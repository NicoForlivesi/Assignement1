package pcd.lab05.monitors;


// Campiamo che è un monitor dal fatto che tutti i metodi pubblici sono synchronized, questa rimane l'implementazione
// di un monitor a basso livello
public class SynchCell {

	private int value;
	private boolean available;

	public SynchCell(){
		available = false;
	}

	public synchronized void set(int v){
		value = v;
		available = true;
		notifyAll();  
	}

	public synchronized int get() {
		while (!available){
			try {
				wait(); // Rilascia il lock sull'oggetto quando si ferma qui, così un altro thread può chiamare set
			} catch (InterruptedException ex){}
		}
		return value;
	}
}