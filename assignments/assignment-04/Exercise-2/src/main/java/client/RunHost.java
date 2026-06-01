package client;

import server.GameService;

import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Scanner;

public class RunHost {
    public static void main(String[] args) throws Exception {
        Scanner sc = new Scanner(System.in);
        System.out.print("Username: ");
        String playerName = sc.nextLine();
        System.out.print("Insert game name you want to create: ");
        String gameName = sc.nextLine();

        String host = (args.length > 0) ? args[0] : null;
        GameService service = (GameService) LocateRegistry.getRegistry(host).lookup("TTTService");

        GameListenerImpl listener = new GameListenerImpl(service, playerName, gameName);
        GameListener stub = (GameListener) UnicastRemoteObject.exportObject(listener, 0);

        service.createGame(gameName, playerName, stub);
        System.out.println("Game created. Waiting for opponent...");

        // Metodo per mantenere il main in attesa infinita finché non si chiude manualmente,
        // serve per mantenere la JVM dell'host viva
        synchronized (RunHost.class) {
            RunHost.class.wait();
        }
    }
}
