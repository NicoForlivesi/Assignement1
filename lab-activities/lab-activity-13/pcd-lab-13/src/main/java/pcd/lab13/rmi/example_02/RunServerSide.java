package pcd.lab13.rmi.example_02;

import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
        
public class RunServerSide  {
                
    public static void main(String args[]) {
        
        try {
            var count = new CounterImpl(0);
            var countStub = (Counter) UnicastRemoteObject.exportObject(count, 0);
            
            var registry = LocateRegistry.getRegistry();
            registry.rebind("countObj", countStub);

            log("Count object registered.");
            
                       
        } catch (Exception e) {
            log("Server exception: " + e.toString());
            e.printStackTrace();
        }
    }
    
	private static void log(String msg) {
		System.out.println("[ " + System.currentTimeMillis() + " ][ Main ] " + msg);
	}

    
}