package pcd.lab13.rmi.example_02;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

public class CounterImpl implements Counter {
	private int value;
	private List<RemoteCounterListener> listeners;
	
	public CounterImpl(int value){
		this.value = value;
		listeners = new ArrayList<>();
	}
	
	public synchronized void inc(){
		value++;
		for (var l: listeners) {
			try {
				l.countIncremented(value);
			} catch (RemoteException ex) {
				ex.printStackTrace();
			}
		}
	}
	
	public synchronized int getValue(){
		return value;
	}

	@Override
	public void addListener(RemoteCounterListener l) {
		listeners.add(l);
		log("Added listener.");
	}

	private void log(String msg) {
		System.out.println("[ " + System.currentTimeMillis() + " ][ Counter ] " + msg);
	}
	
}
