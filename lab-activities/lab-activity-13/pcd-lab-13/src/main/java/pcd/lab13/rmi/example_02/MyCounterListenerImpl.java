package pcd.lab13.rmi.example_02;

import java.rmi.RemoteException;

public class MyCounterListenerImpl implements java.io.Serializable, RemoteCounterListener  {

	private int nTimes;
	
	public MyCounterListenerImpl(){
		nTimes = 0;
	}

	@Override
	public synchronized void countIncremented(int currentCount) throws RemoteException {
		nTimes++;
		log("new inc - " + nTimes);
	}

	private void log(String msg) {
		System.out.println("[ " + System.currentTimeMillis() + " ][ Listener ] " + msg);
	}
	
}
