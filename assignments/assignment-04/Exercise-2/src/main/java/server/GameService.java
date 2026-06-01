package server;

import client.GameListener;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface GameService extends Remote {

    void createGame(String gameName, String playerName, GameListener listener) throws RemoteException;

    void joinGame(String gameName, String playerName, GameListener listener) throws RemoteException;

    String[] listOpenGames() throws RemoteException;

    void makeMove(String gameName, String playerName, int r, int c) throws RemoteException;
}
