package pcd.lab13.rmi.example_02;

import java.rmi.*;

public interface Counter extends Remote {
	
	void inc() throws RemoteException;
	
	int getValue() throws RemoteException;

	void addListener(RemoteCounterListener r) throws RemoteException;
}
