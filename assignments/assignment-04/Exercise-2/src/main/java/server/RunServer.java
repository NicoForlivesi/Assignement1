package server;

import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;

/**
 * Prima di runnare tutto bisogna creare il registry con "rmiregistry" in target/classes
 * */
public class RunServer {
    private static final String GAME_NAME = "TTTService";

    public static void main(String[] args) {

        try {
            var service = new GameServiceImpl();
            var stub = (GameService) UnicastRemoteObject.exportObject(service, 0);

            var registry = LocateRegistry.getRegistry();
            // System.setProperty("java.rmi.server.hostname", "127.0.0.1"); per registry da codice
            // Registry registry = LocateRegistry.createRegistry(1099); per creare registry dal codice,
            // senza doverlo lanciare a parte nel terminale nel classpath
            registry.rebind(GAME_NAME, stub);

            System.out.println("[Server] Ready.");

        } catch (Exception e) {
            System.out.println("Server exception: " + e.toString());
            e.printStackTrace();
        }
    }
}
