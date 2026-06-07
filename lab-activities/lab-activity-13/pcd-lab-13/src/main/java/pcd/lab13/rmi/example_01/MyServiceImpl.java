package pcd.lab13.rmi.example_01;

import java.rmi.RemoteException;

// Nell'implementazione a parte il fatto che i metodi lanciato un eccezione, non c'è niente di diverso dal caso
// di oop nel concentrato.
// Questi metodi potranno essere chiamati da remoto dai client che si trovano su un'altra JVM rispetto al server.
public class MyServiceImpl implements MyService {
        
    public MyServiceImpl() {}

    public void printHello() throws RemoteException {
    	System.out.println("Hello, world!");
    }
    
    public int getSum(int a, int b) throws RemoteException {
    	return a + b;
    }

    public void receive(Message m) throws RemoteException {
    	System.out.println("Message received: " + m.getContent());
    }
        
}