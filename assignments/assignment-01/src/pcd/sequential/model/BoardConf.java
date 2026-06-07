package pcd.sequential.model;

import java.util.List;

public interface BoardConf {
    Ball getPlayerBall();
    Ball getBotBall();
    List<Ball> getSmallBalls();
}