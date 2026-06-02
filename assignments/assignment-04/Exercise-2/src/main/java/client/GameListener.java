package client;

import model.Board;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface GameListener extends Remote {

    void passTurn(Board board) throws RemoteException;

    void receiveMessage(String message) throws RemoteException;
}
