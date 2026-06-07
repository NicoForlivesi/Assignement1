package pcd.lab03.cs_raw;

// In questo esempio vediamo l'uso di syncrhonized che è un costrutto di basso livello, noi useremo per l'assignment
// costrutti di alto livello (più semplici) come i monitor che vedremo poi.
public class MyWorkerA extends Worker {
	
	private Object lock;
	
	public MyWorkerA(String name, Object lock){
		super(name);
		this.lock = lock;
	}
	
	public void run(){
		while (true){
		  a1();			  
		  /* critical section */
		  synchronized(lock){ // workerA che workerB nella loro sezione critica non accedono a dati in memoria condivisa,
              // il concetto di sezione critica è più esteso rispetto al solo "accedere a dati condivisi", può anche essere
              // eseguito del codice in delle sezione definite critiche che lavora su dati diversi ma che per qualchè
              // motivo vogliamo proteggere
			  a2();	
			  a3();	
		  }
		}
	}
	
	protected void a1(){
		println("a1");
		wasteRandomTime(100,500);	
	}
	
	protected void a2(){
		println("a2");
		wasteRandomTime(300,700);	
	}
	protected void a3(){
		println("a3");
		wasteRandomTime(300,700);	
	}
}

