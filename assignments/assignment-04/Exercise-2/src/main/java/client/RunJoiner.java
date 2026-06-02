package client;

import server.GameService;

import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Scanner;

public class RunJoiner {

    public static void main(String[] args) {

        try {
            Scanner sc = new Scanner(System.in);

            String host = (args.length > 0) ? args[0] : null;
            var registry = LocateRegistry.getRegistry(host);
            var service = (GameService) registry.lookup("TTTService");

            String[] open = service.listOpenGames();
            if (open.length == 0) { System.out.println("No open games available."); return; }
            System.out.println("Joinable games:");
            for (String g : open) { System.out.println("  - " + g); }

            System.out.print("Username: ");
            String playerName = sc.nextLine();
            System.out.print("Game you want to join: ");
            String gameName = sc.nextLine();

            var listener = new GameListenerImpl(service, playerName, gameName);
            var lproxy = (GameListener) UnicastRemoteObject.exportObject(listener, 0);

            service.joinGame(gameName, playerName, lproxy); // Questa è la chiamata che da l'inizio concreto alla partita

        } catch (Exception e) {
            log("Client exception: " + e.toString());
            e.printStackTrace();
        }
    }

    private static void log(String msg) {
        System.out.println("[ " + System.currentTimeMillis() + " ][ Joiner Main ] " + msg);
    }
}
