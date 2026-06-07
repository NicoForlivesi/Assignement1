package pcd.lab04.ex01_synchwithsem;

public class Ponger extends ActiveComponent {

    private Semaphore pingDoneEvent, pongDoneEvent;
	
	public Ponger(Semaphore pingDoneEvent, Semaphore pongDoneEvent) {
        this.pingDoneEvent = pingDoneEvent;
        this.pongDoneEvent = pongDoneEvent;
	}	
	
	public void run() {
		while (true) {
            try {
                pingDoneEvent.acquire();
                println("pong");
                pongDoneEvent.release();
            } catch (Exception e) {}
		}
	}
}