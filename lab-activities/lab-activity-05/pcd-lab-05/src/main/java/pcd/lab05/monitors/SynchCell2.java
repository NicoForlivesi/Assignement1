package pcd.lab05.monitors;

import java.util.concurrent.locks.*;


// Qui usiamo il supporto di libreria, prima la condition variable era l'oggetto
// stesso, ora la creiamo manualmente dalla libreria.
// Notare che quando usiamo questo approccio ci dimentichiamo completamente di synchronized, la mutua esclusione
// si ottiene tramite lock e unlock su mutex. Definiamo anche la condition variable, con questo approccio è anche
// possibile definirne più di una se necessario (con l'esempio SynchCell precedente invece no).
public class SynchCell2 {

	private int value;
	private boolean available;
	private Lock mutex;   
	private Condition isAvail;

	public SynchCell2(){
		available = false;
		mutex = new ReentrantLock(); 
		isAvail = mutex.newCondition(); // Metodo di costruzione sul Lock in cui diciamo "dammi una condition variable"
	}

	public void set(int v){
		try {
			mutex.lock();
			value = v;
			available = true;
			isAvail.signalAll();  // Politica signal and continue (default di Java), con la libreria si usa
            // signalAlll, non notifyAll.
		} finally {
			mutex.unlock();
		}
	}
	
	public int get() {
		try {
			mutex.lock();
			while (!available){ // Usare sempre while invece che if
				try {
					isAvail.await(); // Nell'implementazione con la libreria si usa await, non wait.
				} catch (InterruptedException ex){}
			} 
			return value;
		} finally {
			mutex.unlock();
		}
	}
}

