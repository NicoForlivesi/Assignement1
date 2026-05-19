package pcd.lab13.rmi.example_01;

import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
        
public class RunServerSide  {
                
	private static final String OBJ_NAME = "myService";
	
    public static void main(String args[]) {
        
        try {
            MyService myRemoteObj = new MyServiceImpl();
            MyService myRemoteObjProxy = (MyService) UnicastRemoteObject.exportObject(myRemoteObj, 0);

            // Bind the remote object's stub in the registry
            Registry registry = LocateRegistry.getRegistry();
            
            registry.rebind(OBJ_NAME, myRemoteObjProxy);
            
            System.out.println("Object registered.");
        } catch (Exception e) {
            System.err.println("Server exception: " + e.toString());
            e.printStackTrace();
        }
    }
}