package client;

import model.Board;
import server.GameService;

import java.rmi.RemoteException;
import java.util.Scanner;

public class GameListenerImpl implements GameListener {

    private final GameService service;
    private final String playerName;
    private final String gameName;
    private final Scanner scanner = new Scanner(System.in);

    public GameListenerImpl(GameService service, String playerName, String gameName) {
        this.service = service;
        this.playerName = playerName;
        this.gameName = gameName;
    }

    @Override
    public void passTurn(Board board) throws RemoteException {
        System.out.println("It's your turn! Insert your symbol.");
        int r, c;
        while (true) {
            System.out.print("Row (0-2): ");
            r = scanner.nextInt();
            System.out.print("Col (0-2): ");
            c = scanner.nextInt();
            scanner.nextLine();
            try {
                service.makeMove(gameName, playerName, r, c);
                return; // move accepted
            } catch (RemoteException e) {
                System.out.println("Invalid move: " + e.getMessage() + " Try again.");
            }
        }
    }

    @Override
    public void receiveMessage(String message) throws RemoteException {
        System.out.println(message);
    }
}
