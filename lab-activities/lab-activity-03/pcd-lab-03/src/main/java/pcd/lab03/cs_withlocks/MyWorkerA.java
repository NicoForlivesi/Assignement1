package pcd.lab03.cs_withlocks;

import java.util.concurrent.locks.Lock;

public class MyWorkerA extends Worker {
	
	private Lock lock;
	
	public MyWorkerA(String name, Lock lock){
		super(name);
		this.lock = lock;
	}
	
	public void run(){
		while (true){
		  a1();	
		  try {
			  lock.lockInterruptibly(); // Questo metodo sull'oggetto cerca di prendere il lock ma può anche essere
              // sbloccato lanciando un eccezione, esiste anche lock.lock, quello non può essere sbloccato.
              // Esiste anche un altro metodo che se il lock non è disponibile restituisce "false" e va avanti.
              // Importante ricordarsi qualsiasi metodo usato che qualsiasi cosa succeda finita la sezione critica
              // il lock va rilasciato.
			  a2();	
			  a3();	
		  } catch (InterruptedException ex) {
		  } finally {
			  lock.unlock(); // Rilascia il lock uscito dalla sezione critica (sotto questi due costrutti usati
              // utilizzeranno synchronized).
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

