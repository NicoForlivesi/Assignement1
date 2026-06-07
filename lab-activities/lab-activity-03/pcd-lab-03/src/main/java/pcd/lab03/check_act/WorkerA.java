package pcd.lab03.check_act;

public class WorkerA extends Thread{
	
	private BoundedCounter counter;
	private int ntimes;
	
	public WorkerA(BoundedCounter c, int ntimes){
		counter = c;
		this.ntimes = ntimes;
	}
	
	public void run(){
		try {
			for (int i = 0; i < ntimes; i++){ // Counter è gia safe di per se, è il metodo in cui i worker lavorano
                // che non è safe, il controllo della condizione è il decremento devono essere eseguite assieme in
                // maniera atomica
				// synchronized (counter) {
					if (counter.getValue() > 0){
						counter.dec();
					}
				//}
			}
		} catch (Exception ex){
			ex.printStackTrace();
		}
	}
}
