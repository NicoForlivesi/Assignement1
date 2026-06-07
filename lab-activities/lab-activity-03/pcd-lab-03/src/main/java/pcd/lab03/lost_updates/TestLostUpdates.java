package pcd.lab03.lost_updates;

public class TestLostUpdates {

	public static void main(String[] args) throws Exception {
		
		long ntimes = 10000; // 1000_000_000_000_000l; // try with different values: 100, 200, 1000, 5000, ...
		
		if (args.length > 0) {
			ntimes = Integer.parseInt(args[0]);
		}
		
		UnsafeCounter c = new UnsafeCounter(0); // Classe non thread-safe che implementa un contatore
		Worker w1 = new Worker("Worker-A", c, ntimes); // ad entrambi i worker viene fornita la classe c
        // da incrementare ntimes, quindi alla fine c getValue dovrebbe essere 20000 (se la classe counter fosse
        // thread safe), anche in questa implementazione per ntimes bassi è molto probabile che il comportamento
        // sembri thread safe, per verificare correttamente che non lo è bisogna avere un'alta probabilità
        // che degli incrementi di sovrappongano (lo si ottiene mettendo un ntimes alto).
        // Se avessimo usato un tipo di dato che stava dentro i 32 bit, l'operazione sarebbe stata thread safe nel caso
        // avessimo fatto solo operazioni di assegnamento e lettura, usando un long invece, l'operazione anche di lettura
        // NON è atomica per la JVM.
        // In questo caso il problema ci sarebbe anche avessimo usato un int (dentro i 32 bit), perchè è l'operazione
        // int++ che non è atomica per definizione, la JVM legge il valore corrente e poi lo incrementa.
        // Per ogni scenario possibile il numero stampato sarà se tutto va bene uguale ntimes * 2, se c'è qualche
        // lost update sarà sempre minore, mai maggiore quindi.
        // Il metodo più semplice qui per renderlo thread safe è fare counter.inc(); dentro un syncrhonized,
        // per esempio sull'oggetto "UnsafeCounter" che è condiviso sui due worker, il problema è che prendere un lock
        // quindi usare synchronized è particolarmente costoso in termini di performance, il che si nota su operazioni
        // un po più complessi
		Worker w2 = new Worker("Worker-B", c, ntimes);

		Cron cron = new Cron();
		cron.start();
		
		w1.start();
		w2.start();

		w1.join(); // aspetto la terminazione di w1 e w2
		w2.join();
		
		cron.stop();
		
		System.out.println("Counter final value: " + c.getValue() + " in " + cron.getTime()+"ms.");
	}
}
