package client;

import server.GameService;

import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Scanner;

public class RunJoiner {
    public static void main(String[] args) throws Exception {
        Scanner sc = new Scanner(System.in);

        String host = (args.length > 0) ? args[0] : null;
        GameService service = (GameService) LocateRegistry.getRegistry(host).lookup("TTTService");

        String[] open = service.listOpenGames();
        if (open.length == 0) { System.out.println("No open games available."); return; }
        System.out.println("Joinable games:");
        for (String g : open) System.out.println("  - " + g);

        System.out.print("Username: ");
        String playerName = sc.nextLine();
        System.out.print("Game you want to join: ");
        String gameName = sc.nextLine();

        GameListenerImpl listener = new GameListenerImpl(service, playerName, gameName);
        GameListener stub = (GameListener) UnicastRemoteObject.exportObject(listener, 0);

        service.joinGame(gameName, playerName, stub); // Questa chiamate è bloccante, quindi non c'è bisogno di usare
        // un trucco per mantenere la JVM viva per il joiner
    }
}
