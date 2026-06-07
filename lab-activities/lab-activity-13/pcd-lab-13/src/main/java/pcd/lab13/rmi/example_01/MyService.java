package pcd.lab13.rmi.example_01;

import java.rmi.Remote;
import java.rmi.RemoteException;

// Oggetto remoto chiamato "MyService" per essere un oggetto remoto deve estendere "Remote"
// Remote in realtà è solo una flag interface (quindi è vuota), serve solo per specificare che le istanze
// degli oggetti che verranno creati sono destinati ad essere oggetti remoti.
// Un vincolo è che ogni metodo che andiamo a creare deve lanciare l'eccezione "RemoteException", questo
// perchè nel caso un oggetto remoto non sia reggiungibile deve essere lanciata questa eccezione.
public interface MyService extends Remote {
	
    void printHello() throws RemoteException;
    
    int getSum(int a, int b) throws RemoteException;

    void receive(Message msg) throws RemoteException;
    
}