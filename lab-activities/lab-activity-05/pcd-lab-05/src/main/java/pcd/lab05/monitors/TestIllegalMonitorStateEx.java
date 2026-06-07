package pcd.lab05.monitors;

public class TestIllegalMonitorStateEx {

	public static void main(String[] args)  {

		Object lock = new Object();
        
		try {
			// synchronized (lock){
		        lock.wait(); // Non si può fare, genera un errore
			// }
		} catch (Exception ex){
			ex.printStackTrace();
		}
	}

}
