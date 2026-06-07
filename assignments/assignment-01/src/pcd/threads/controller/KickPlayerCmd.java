package pcd.threads.controller;

import pcd.threads.model.Ball;
import pcd.threads.model.Board;
import pcd.threads.model.V2d;

public class KickPlayerCmd implements Cmd {
    private final V2d vel;

    public KickPlayerCmd(V2d vel) {
        this.vel = vel;
    }

    @Override
    public void execute(Board board) {
        Ball playerBall = board.getPlayerBall();
        if (playerBall != null && playerBall.stillAlive()) {
            playerBall.kick(vel);
        }
    }
}