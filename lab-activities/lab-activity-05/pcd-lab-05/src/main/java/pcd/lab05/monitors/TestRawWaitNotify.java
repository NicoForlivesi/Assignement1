package pcd.lab05.monitors;

public class TestRawWaitNotify {

	public static void main(String[] args) throws Exception  {
		Object obj = new Object();
		
		new Thread(() ->  {
			try {
				Thread.sleep(1000);
			} catch (Exception ex) {}
			System.out.println("notifying...");
			synchronized (obj) { // Sia per fare una wait che per fare una notify un thread deve avere il lock
                // su quell'oggetto, in questo esempi è importante che si raggiunta prima la wait che la notify
                // Nell'assignment non vanno usati in questo modo sono operazioni molto di basso livello.
				obj.notify();
			}
		}).start();
		
		new Thread(() ->  {
			try {
				synchronized (obj) {
					obj.wait(); // Quando mi fermo sulla wait rilascio il lock su obj, sennò l'altro thread non puo
                    // entrare
				}
				System.out.println("unblocked");
			} catch (InterruptedException ex) {
			}
		}).start();

		
	}

}
