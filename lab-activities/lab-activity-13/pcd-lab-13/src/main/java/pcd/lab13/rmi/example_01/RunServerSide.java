package pcd.lab13.rmi.example_01;

import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
        
public class RunServerSide  {
                
	private static final String OBJ_NAME = "myService";
	
    public static void main(String args[]) {

        // L'idea è che ogni oggetto remoto che vogliamo sia accessibile deve essere registrato con un nome
        try {
//            System.setProperty("java.rmi.server.hostname", "127.0.0.1"); per registry da codice
            MyService myRemoteObj = new MyServiceImpl(); // Fin qui è un oggetto normale
            MyService myRemoteObjProxy = (MyService) UnicastRemoteObject.exportObject(myRemoteObj, 0); // Qui specifichiamo
            // che vogliamo che l'oggetto diventi remoto

            // Bind the remote object's stub in the registry
            Registry registry = LocateRegistry.getRegistry();
            // Registry registry = LocateRegistry.createRegistry(1099); per creare registry dal codice,
            // senza doverlo lanciare a parte nel terminale nel classpath
            
            registry.rebind(OBJ_NAME, myRemoteObjProxy); // Registro il proxy che ho ottenuto dall'oggetto creato su un
            // certo nome
            
            System.out.println("Object registered.");
        } catch (Exception e) {
            System.err.println("Server exception: " + e.toString());
            e.printStackTrace();
        }
    }
}