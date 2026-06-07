package pcd.lab04.deadlock_simplest;

/**
 * Deadlock example 
 * 
 * @author aricci
 *
 */

// è l'esempio dei filosofi... per poter procedere bisogna prendere il lock su due oggetti
public class TestDeadlock {
	public static void main(String[] args) {
		Resource resAlfa = new Resource("Resource-ALFA");
		Resource resBeta = new Resource("Resource-BETA");
		new Worker(resAlfa, resBeta).start(); // passando le due risorse in ordine inverso si verifica deadlock,
        // entrambi prendon il lock su uno dei due oggetti e non possono procedere -> deadlock
        // L'azione di prendere la lock sui due oggetti deve essere eseguita in maniera atomica per evitare ciò.
        // Oppure fornire le risorse nello stesso ordine che quindi innesca una catena di lock in cui se il primo è
        // libero allora sono liberi anche gli altri.
		new Worker(resBeta, resAlfa).start();
	}

}
