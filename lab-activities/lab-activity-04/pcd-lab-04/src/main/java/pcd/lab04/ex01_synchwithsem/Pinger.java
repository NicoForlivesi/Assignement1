package pcd.lab04.ex01_synchwithsem;

public class Pinger extends ActiveComponent {

    private Semaphore pongDoneEvent, pingDoneEvent;

	public Pinger(Semaphore pongDoneEvent, Semaphore pingDoneEvent) {
        this.pongDoneEvent = pongDoneEvent;
        this.pingDoneEvent = pingDoneEvent;
	}
	
	public void run() {
		while (true) {
            try {
                pongDoneEvent.acquire();
                println("ping");
                pingDoneEvent.release();
            } catch (Exception e) {};
		}
	}
}