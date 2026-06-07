package pcd.lab01.ex01;

import java.util.*;

public class SequentialSort {

	static final int VECTOR_SIZE = 400_000_000;
	
	public static void main(String[] args) {
	
		log("Num elements to sort: " + VECTOR_SIZE);
		log("Generating array.");
		var v = genArray(VECTOR_SIZE);
		
		log("Array generated.");
		log("Sorting.");
	
		long t0 = System.nanoTime();		
		Arrays.sort(v, 0, v.length); // Questo è un metodo che sfrutta un unico core, per quanto riguarda lavori CPU bound
        // quindi dove ci sono da fare molti calcoli per ottenere le massime performance ha senso sfruttare tutti i core
        // della macchina a disposizione quindi in generale N_Thread = N_core + 1 (vedremo poi perchè + 1).
        // Ragionamento diverso se si sta svolgendo un compito forntemente I/O bound, in quel caso in numero di thread dovrà
        // essere idealmente maggiore del numero di core visto che le operazione di I/O sono bloccanti.
		long t1 = System.nanoTime();
		log("Done. Time elapsed: " + ((t1 - t0) / 1000000) + " ms");
		
		// dumpArray(v);
	}


	private static int[] genArray(int n) {
		Random gen = new Random(System.currentTimeMillis());
		var v = new int[n];
		for (int i = 0; i < v.length; i++) {
			v[i] = gen.nextInt();
		}
		return v;
	}

	private static void dumpArray(int[] v) {
		for (var l:  v) {
			System.out.print(l + " ");
		}
		System.out.println();
	}

	private static void log(String msg) {
		System.out.println(msg);
	}
}
