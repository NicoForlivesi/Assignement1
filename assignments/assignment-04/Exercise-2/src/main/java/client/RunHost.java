package client;

import server.GameService;

import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Scanner;

public class RunHost {

    public static void main(String[] args) {

        try {
            Scanner sc = new Scanner(System.in);
            System.out.print("Username: ");
            String playerName = sc.nextLine();
            System.out.print("Insert game name you want to create: ");
            String gameName = sc.nextLine();

            String host = (args.length > 0) ? args[0] : null;
            var registry = LocateRegistry.getRegistry(host);
            var service = (GameService) registry.lookup("TTTService");

            var listener = new GameListenerImpl(service, playerName, gameName);
            var lproxy = (GameListener) UnicastRemoteObject.exportObject(listener, 0);

            service.createGame(gameName, playerName, lproxy);
            System.out.println("Game created. Waiting for opponent...");

        } catch (Exception e) {
            log("Client exception: " + e.toString());
            e.printStackTrace();
        }
    }

    private static void log(String msg) {
        System.out.println("[ " + System.currentTimeMillis() + " ][ Host Main ] " + msg);
    }
}
