package pcd.lab03.lost_updates;


public class WorkerOK extends Thread {
	
	private UnsafeCounter counter;
	private long ntimes;
	
	public WorkerOK(String name, UnsafeCounter counter, long ntimes){
		super(name);
		this.counter = counter;
		this.ntimes = ntimes;
	}
	
	public void run(){
		log("started");
		for (long i = 0; i < ntimes; i++){
			synchronized (counter) { // Così è thread safe, ma prendere il lock è un operazione costosa!!
                // La safety però è una proprietà che non si può barattare ovviamente per performance.
                // GUARDA UNSAFE COUNTER
				counter.inc();
			}
		}
		log("completed");
	}
	
	private void log(String msg) {
		System.out.println("[ " + this.getName() + "] " + msg);
	}
	
}
