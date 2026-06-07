package pcd.tasks.controller;

import pcd.tasks.model.Board;
import pcd.tasks.util.BoundedBuffer;
import pcd.tasks.util.BoundedBufferImpl;

// Questo è il thread consumatore nel senso del buffer
public class InputController extends Thread {
    private final BoundedBuffer<Cmd> cmdBuffer;
    private final Board board;

    public InputController(Board board) {
        this.cmdBuffer = new BoundedBufferImpl<>(100);
        this.board = board;
        setDaemon(true); // muore con il programma principale
    }

    @Override
    public void run() {
        while (!board.isGameOver()) {
            try {
                // Si blocca in attesa di comandi dalla View
                Cmd cmd = cmdBuffer.get();
                cmd.execute(board);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    // Metodo esposto alla View per iniettare i comandi
    public void notifyNewCmd(Cmd cmd) {
        try {
            cmdBuffer.put(cmd);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }
}