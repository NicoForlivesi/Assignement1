package pcd.lab13.rmi.example_01;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class RunClientSide {

	private static final String OBJ_NAME = "myService";

    public static void main(String[] args) {

        String host = (args.length < 1) ? null : args[0];
        try {
            Registry registry = LocateRegistry.getRegistry(host);
            MyService obj = (MyService) registry.lookup(OBJ_NAME);
            
            obj.printHello();
            System.out.println("Remote method invoked.");
            		
            int res = obj.getSum(1,2);
            System.out.println("Response: " + res);
            
            Message msg = new Message("Hello from Cesena");
            obj.receive(msg);
            
            System.out.println(msg.getContent());
            
            
            System.out.println("done.");
            
        } catch (Exception e) {
            System.err.println("Client exception: " + e.toString());
            e.printStackTrace();
        }
    }
}