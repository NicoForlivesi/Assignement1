package server;

import client.GameListener;
import model.Board;

import java.rmi.RemoteException;

class Game {

    private static final String X = "X";
    private static final String O = "O";

    private final String name;
    private final String hostName;

    private final GameListener hostListener;
    private GameListener joinerListener;

    private final Board board = new Board();
    private boolean waiting = true;

    Game(String name, String hostName, GameListener hostListener) {
        this.name = name;
        this.hostName = hostName;
        this.hostListener = hostListener;
    }

    String getName() { return name; }

    synchronized boolean isWaiting() { return waiting; }

    /** Joiner connects: notify host the game started, then kick off first turn (host plays X). */
    void join(String joinerName, GameListener joinerListener) throws RemoteException {
        synchronized (this) {
            this.joinerListener = joinerListener;
            this.waiting = false;
        }
        hostListener.receiveMessage(joinerName + " joined the game!");
        joinerListener.receiveMessage("Game started! You are 'O'. Waiting for '" + hostName + "' (X) to move...");
        hostListener.passTurn(board); // blocking: host reads input and calls makeMove()
    }

    /** Applies a move, notifies the opponent, and passes the turn. */
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
            opponent.receiveMessage("Game over: you lost!");
            current.receiveMessage("Game over: you won!");
        } else if (board.isFull()) {
            opponent.receiveMessage("Game over: draw!");
            current.receiveMessage("Game over: draw!");
        } else {
            opponent.passTurn(board); // passgio del turno all'avversario
        }
    }
}
