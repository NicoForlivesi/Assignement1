package pcd.tasks.controller;

import pcd.tasks.model.Board;
import pcd.tasks.util.BoundedBuffer;
import pcd.tasks.model.Ball;
import pcd.tasks.model.V2d;
import pcd.tasks.util.Configuration;

import java.awt.event.KeyEvent;

// Questo è il thread consumatore nel senso del buffer
public class InputController extends Thread {
    private final BoundedBuffer<Integer> buffer;
    private final Board board;

    public InputController(Board board, BoundedBuffer<Integer> buffer) {
        this.buffer = buffer;
        this.board = board;
        setDaemon(true);
    }

    @Override
    public void run() {
        while (!board.isGameOver()) {
            try {
                int keyCode = buffer.get();
                handleKey(keyCode);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private void handleKey(int keyCode) {
        Ball playerBall = board.getPlayerBall();
        if (playerBall == null || !playerBall.stillAlive()) return;
        double speed = Configuration.PLAYER_BALL_SPEED;
        switch (keyCode) {
            case KeyEvent.VK_W -> playerBall.kick(new V2d(0, -speed));
            case KeyEvent.VK_S -> playerBall.kick(new V2d(0, speed));
            case KeyEvent.VK_A -> playerBall.kick(new V2d(-speed, 0));
            case KeyEvent.VK_D -> playerBall.kick(new V2d(speed, 0));
        }
    }
}