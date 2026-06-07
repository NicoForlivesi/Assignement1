package pcd.sequential.controller;

import pcd.sequential.model.*;
import pcd.sequential.view.*;

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