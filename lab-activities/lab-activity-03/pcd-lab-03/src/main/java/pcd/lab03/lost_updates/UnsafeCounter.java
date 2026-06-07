package pcd.lab03.lost_updates;

/**
 * 
 * This class is *NOT* Thread-safe
 * 
 */
public class UnsafeCounter {

	private int cont;
	
	public UnsafeCounter(int base){
		this.cont = base;
	}
	
	public void inc(){
		cont++;
//        synchronized (this) { INVECE CHE USARE SYNCHRONIZED LATO WORKER, IL METODO MIGLIORE è QUESTO,
//            cont++;           COSì ABBIAMO LA GARANZIA CHE TUTTE LE VOLTE CHE VIENE CHIAMATO INC SU UN OGGETTO
//        }                     COUNTER QUESTO METODO VIENE ESEGUITO IN MODO ATOMICO!!! (è IL RAGIONAMENTO CHE SI
	}                           // AVVICINA AI MONITOR. VISTO CHE QUESTO MODO è PARTICOLARMENTE USATO
                                // IN JAVA è POSSIBILE DEFINIRE UN METODO COME SYNCHRONIZED, CHE SIGNIFICA CHE TUTTO
                                // IL CODICE DI QUEL METODO DEVE ESSERE ESEGUITO IN MANIERA ATOMICA, QUINDI DEL TUTTO
                                // EQUIVALENTE SAREBBE STATO DEFINIRE IL METODO "public synchronized void inc()".
                                // Se ci sono operazioni all'interno di un metodo che non hanno la necessità di essere
                                // eseguite in maniera atomica è meglio usare synchronized (this) { ... } solo per le
                                // sezioni critiche, in modo da incidere il meno possibile sulle performance.
                                // Se mettiamo syncrhonized anche su getValue questo implica che solo un thread alla volta
    // può eseguire metodi di questa classe ed uno per volta, sono metodi diversi ma prendono il lock sullo stesso oggetto
    // quindi non si può eseguire getValue se un thread è dentro a inc se entrambi i metodi fossero definito come
    // synchronized.
    // Attenzione: Se il modello fosse che si legge 1000 volte e si incremneta 1, andrebbe bene ma si paga notevolmente in
    // performance se ci sono molti thread, un thread non può leggere se sta leggendo qualcun'altro anche se nello
    // specifico la lettura in quel momento è thread safe, equivale all'esempio lettori-scrittori con 1000 lettori e uno
    // scrittore che scrive raramente... vedremo quale approccio è il migliore per questo caso con i monitor.
	
	public int getValue(){
		return cont;
	}
}
