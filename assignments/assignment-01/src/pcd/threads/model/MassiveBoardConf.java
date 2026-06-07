package pcd.threads.model;

import java.util.ArrayList;
import java.util.List;

public class MassiveBoardConf implements BoardConf {
    @Override
    public Ball getPlayerBall() {
        return new Ball(new P2d(200, 400), new V2d(0, 0), 15, Ball.BallType.PLAYER);
    }

    @Override
    public Ball getBotBall() {
        return new Ball(new P2d(1000, 400), new V2d(0, 0), 15, Ball.BallType.BOT);
    }

    @Override
    public List<Ball> getSmallBalls() {
        var balls = new ArrayList<Ball>();
        double ballRadius = 3; // Raggio ridotto rispetto alle altre 2 conf per farne stare 4500

        for (int row = 0; row < 30; row++) {
            for (int col = 0; col < 150; col++) {
                // Distanza tra i centri = 7 per non avere overlap iniziale fra le palline
                // Diametro = 6
                double px = 80 + col * 7;
                double py = 100 + row * 7;
                balls.add(new Ball(new P2d(px, py), new V2d(0, 0), ballRadius, Ball.BallType.REGULAR));
            }
        }
        return balls;
    }
}