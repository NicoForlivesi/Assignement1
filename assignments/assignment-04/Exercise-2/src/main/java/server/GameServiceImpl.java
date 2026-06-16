package server;

import client.GameListener;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;

public class GameServiceImpl implements GameService {

    private final Map<String, Game> games = new HashMap<>();

    @Override
    public synchronized void createGame(String gameName, String playerName, GameListener listener) throws RemoteException {
        games.put(gameName, new Game(gameName, playerName, listener));
        System.out.println("[Server] Game created: " + gameName + " by " + playerName);
    }

    @Override
    public void joinGame(String gameName, String playerName, GameListener listener) throws RemoteException {
        Game g;
        synchronized (this) {
            g = games.get(gameName);
            if (g == null) { throw new RemoteException("Game '" + gameName + "' not found."); }
            if (!g.isWaiting()) { throw new RemoteException("Game '" + gameName + "' is already full."); }
        }
        System.out.println("[Server] " + playerName + " joined: " + gameName);
        g.join(playerName, listener); // Chiamata fuori dal lock, esegue chiamate remote che possono bloccarsi,
        // causando deadlock se fosse dentro il blocco synchronized
    }

    @Override
    public void makeMove(String gameName, String playerName, int r, int c) throws RemoteException {
        Game g;
        synchronized (this) {
            g = games.get(gameName);
            if (g == null) { throw new RemoteException("Game '" + gameName + "' not found."); }
        }
        g.applyMove(playerName, r, c);
    }

    @Override
    public synchronized String[] listOpenGames() throws RemoteException {
        return games.values().stream()
                .filter(Game::isWaiting)
                .map(Game::getGameName)
                .toArray(String[]::new);
    }
}
