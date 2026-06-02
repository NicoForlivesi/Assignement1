package server;

import client.GameListener;
import model.Board;

import java.rmi.RemoteException;

class Game {

    private final String gameName;
    private final String hostName;

    private final GameListener hostListener;
    private GameListener joinerListener;

    private final Board board = new Board();
    private boolean waiting = true;

    Game(String gameName, String hostName, GameListener hostListener) {
        this.gameName = gameName;
        this.hostName = hostName;
        this.hostListener = hostListener;
    }

    String getGameName() { return gameName; }

    synchronized boolean isWaiting() { return waiting; }

    /** Quando entra un joiner in partita, la partita può iniziare e il suo inizio è gestito proprio dal joiner, è
     * lui che comunica all'host di essere entrato e da inizio al primo turno passando il controllo all'host */
    void join(String joinerName, GameListener joinerListener) throws RemoteException {
        synchronized (this) {
            this.joinerListener = joinerListener;
            this.waiting = false;
        }
        hostListener.receiveMessage(joinerName + " joined the game!");
        joinerListener.receiveMessage("Game started! You are 'O'. Waiting for '" + hostName + "' (X) to move...");
        hostListener.passTurn(board);
    }

    /** Metodo per compiere una mossa, notificare l'avversario sulla mossa appena fatta e passargli il turno (ci sono
     * anche tutti i controlli per controllare se la partita è terminata) */
    void applyMove(String playerName, int r, int c) throws RemoteException {
        boolean isHost = playerName.equals(hostName);
        String symbol = isHost ? "X" : "O";
        GameListener current = isHost ? hostListener : joinerListener;
        GameListener opponent = isHost ? joinerListener : hostListener;

        synchronized (this) {
            if (!board.set(r, c, symbol)) { throw new RemoteException("Invalid move: cell already occupied."); }
            // Se non viene lanciata l'eccezione vuol dire che la board è stata aggiornata
        }

        // Importante che le chiamate a metodi remoti siano fuori a scoop synchronized per evitare deadlock
        current.receiveMessage(board.toString() + "Waiting other player move..."); // Faccio vedere al giocatore corrente la mossa che ha fatto
        opponent.receiveMessage("The other player put '" + symbol + "' in position [" + r + ", " + c + "]");
        opponent.receiveMessage(board.toString()); // Mostro all'avversario la mossa fatta sulla griglia

        if (board.checkWinner(symbol)) {
            System.out.println("[Server] Partita '" + gameName + "' conclusa. Vincitore: " + playerName);
            opponent.receiveMessage("Game over: you lost!");
            current.receiveMessage("Game over: you won!");
        } else if (board.isFull()) {
            System.out.println("[Server] Partita '" + gameName + "' conclusa in pareggio.");
            opponent.receiveMessage("Game over: draw!");
            current.receiveMessage("Game over: draw!");
        } else {
            opponent.passTurn(board); // passgio del turno all'avversario
        }
    }
}
