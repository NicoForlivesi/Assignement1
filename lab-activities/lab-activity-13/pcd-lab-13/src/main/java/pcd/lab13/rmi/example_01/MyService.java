package pcd.lab13.rmi.example_01;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface MyService extends Remote {
	
    void printHello() throws RemoteException;
    
    int getSum(int a, int b) throws RemoteException;

    void receive(Message msg) throws RemoteException;
    
}