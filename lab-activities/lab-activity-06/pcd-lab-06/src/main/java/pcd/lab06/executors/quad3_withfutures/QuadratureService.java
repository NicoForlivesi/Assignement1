package pcd.lab06.executors.quad3_withfutures;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.*;

public class QuadratureService extends Thread {
    // Qui compaiono le futures, ovvero quando andiamo a distribuire il lavoro
	private int numTasks;
	private ExecutorService executor;
	
	public QuadratureService (int numTasks, int poolSize){		
		this.numTasks = numTasks; // il numero di task non è legato al numero di thread
		executor = Executors.newFixedThreadPool(poolSize);
	}
	
	public double compute(IFunction mf, double a, double b) throws InterruptedException { 

		double x0 = a;
		double step = (b-a)/numTasks;		
	    List<Future<Double>> results = new LinkedList<Future<Double>>();
		for (int i = 0; i < numTasks; i++) {
			try {
				Future<Double> res = executor.submit(new ComputeAreaTask(x0, x0 + step, mf));
                // Il metodo submit restituisce una Future che rappresenta il risultato che sarà.
                // In generale quando usiamo gli executor si usano le futures perchè sono molto comodo, anche
                // nella versione a task dell'assignmetn 1 conviene ragionare così.
                /**
                 * I TASK MODELLANO BENE DEI COMPITI FINITI, QUANDO CI SONO COMPITI CHE NON TERMINANO MAI, OVVERO CHE NON
                 * HANNO UNA FINE TIPO COMPONENTE ATTIVO CHE HA UN WHILE TRUE NEL METODO RUN, NON HA TANTO SENSO INTRODURRE
                 * UN TASK, UN THREAD MODELLA MEGLIO QUEL COMPORTAMENTO, QUINDI ANCHE NELL'ASSIGNMENT 1 VERSIONE TASK,
                 * PARTIRE DALLA VERSIONE THREAD COMPLETATA E PENSARE DOVE HA SENSO INTRODURRE UN EXECUTOR, MA RIMARRANNO
                 * COMUNQUE DEI THREAD CHE SVOLGONO QUESTI COMPITI CHE NON HANNO UNA FINE.
                */
				results.add(res);
				log("submitted task " + x0 + " " + (x0+step));
				x0 += step;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}				
        // La submit non è bloccante, la get si.
	    double sum = 0;
	    for (Future<Double> res: results) { // Ho mandato in esecuzione tutti i task e qui sono pronto per
            // collezionare i risultati, senza bisogno di chiudere l'executor.
	    	try {
	    		sum += res.get(); // Qui si blocca finchè res non ha effettivamente il risultato
	    	} catch (Exception ex){
	    		ex.printStackTrace();
	    	}
	    }
	    System.out.printf("The result is %s\n", sum);
		return sum;
	}
	
	
	private void log(String msg){
		System.out.println("[SERVICE] "+msg);
	}
}
