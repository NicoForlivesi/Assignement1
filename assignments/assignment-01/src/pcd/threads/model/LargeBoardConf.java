package pcd.threads.model;

import java.util.ArrayList;
import java.util.List;

public class LargeBoardConf extends MassiveBoardConf{

    @Override
    public List<Ball> getSmallBalls() {
        var balls = new ArrayList<Ball>();
        double ballRadius = 6;

        // In questa configurazione creo 400 palline (20 righe x 20 colonne)
        for (int row = 0; row < 20; row++) {
            for (int col = 0; col < 20; col++) {
                double px = 550 + col * 4;
                double py = 200 + row * 10;

                balls.add(new Ball(new P2d(px, py), new V2d(0, 0), ballRadius, Ball.BallType.REGULAR));
            }
        }
        return balls;
    }
}
