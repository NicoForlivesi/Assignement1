package pcd.lab02.io_bound_test;

import java.util.*;


public class Test {
	
	public static void main(String[] args) {

		int nWorkers = 1; // Runtime.getRuntime().availableProcessors();
		
		long totalAmountOfCPUJob = 400_000_000;
		long totalAmountOfIOJob = 20000;
        // In un problema I/O bound, qual'è il numero ottimale di thread??
        // è dato dalla formula Ncpu * Ucpu * (1 + W/C)
        // Ucpu = target CPU utilization (0 <= Ucpu <= 1)
        // W / C = ratio of wait and compute time.

        // In questo caso visto che ci sono sia lavori CPU bound che I/O la cosa migliore è fare dei test,
        // sicuramente il numero ottimale di thread anche se c'è un minimo di I/O comunque è maggior del numero di
        // core + 1 (quindi del caso solamente CPU bound).
		
		int seed = 100;
		
		double howMuchCPUJob = 1.0/nWorkers;
		double howMuchIOJob = 1.0/nWorkers;

		var workers = new ArrayList<Worker>();
		for (int i = 0; i < nWorkers; i++) {
			workers.add(new Worker(howMuchCPUJob, howMuchIOJob, seed, totalAmountOfCPUJob, totalAmountOfIOJob));
			seed++;
		}

		for (var w: workers) {
			w.start();
		}
 
		log("Number of workers: " + nWorkers + " - Amount of IO job: " + howMuchIOJob);
		log("started");			
		var t0 = System.currentTimeMillis();

		for (var w: workers) {
			try {
				w.join();
			} catch (Exception ex) {}
		}
		
		var t1 = System.currentTimeMillis();
		log("Done. Time elapsed: " + (t1 - t0) + "ms");
		
		
	}

	private static void log(String msg) {
		System.out.println(msg);
	}
}
