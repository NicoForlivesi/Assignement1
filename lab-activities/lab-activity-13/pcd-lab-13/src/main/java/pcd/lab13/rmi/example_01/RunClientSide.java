package pcd.lab13.rmi.example_01;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class RunClientSide {

	private static final String OBJ_NAME = "myService";

    public static void main(String[] args) {

        String host = (args.length < 1) ? null : args[0];
        try {
            Registry registry = LocateRegistry.getRegistry(host); // get registry sull'host, se null
            // prende di default localhost
            MyService obj = (MyService) registry.lookup(OBJ_NAME); // associazione di un oggetto locale obj
            // all'oggetto remote sul server "myService"
            // da ora in poi è come se l'oggetto obj fosse locale
            obj.printHello(); // Viene stampato sulla JVM del server perchè obj è il riferimento a quello
            System.out.println("Remote method invoked."); // Nella JVM del client
            		
            int res = obj.getSum(1,2); // Qui finchè in remoto non è stato computato getSum, il thread parte client
            // che ha effettuato la chiamata al metodo remoto viene bloccato. Javam RMI in remoto ha un pool di thread
            // che si occupa di accedere ai metodi remoti
            System.out.println("Response: " + res);
            
            Message msg = new Message("Hello from Cesena"); // Qui ci è punto importante da analizzare:
            // la classe Message per come è definita (implementa serialized) chiama il metodo in remoto passando una copia
            // dell'oggetto creato msg, quindi passa il valore, non il riferimento il che significa che non avremmo side
            // effect sull'oggetto locale msg a seguito di chiamate di qualsiasi metodo remoto
            obj.receive(msg); // Sulla JVM del server, msg passato come copia del valore locale
            // Quindi gli oggetti che si devono "muovere fra nodi" devono derivare da una classe che implementa serialized
            
            System.out.println(msg.getContent());
            
            // Il vantaggio è che è possibile fare programmi in esecuzione in rete senza avere conoscenze profonde di UDP e
            // TCP, si rimane in una logica OOP.
            System.out.println("done.");
            
        } catch (Exception e) {
            System.err.println("Client exception: " + e.toString());
            e.printStackTrace();
        }
    }
}