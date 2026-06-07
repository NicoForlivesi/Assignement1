package pcd.tasks.controller;

import pcd.tasks.model.*;

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