package server;

import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;

public class RunServer {
    public static void main(String[] args) throws Exception {
        GameServiceImpl service = new GameServiceImpl();
        GameService stub = (GameService) UnicastRemoteObject.exportObject(service, 0);
        LocateRegistry.createRegistry(1099).rebind("TTTService", stub);
        System.out.println("[Server] Ready.");
    }
}
