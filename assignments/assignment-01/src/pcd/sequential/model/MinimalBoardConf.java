package pcd.sequential.model;

import java.util.ArrayList;
import java.util.List;

public class MinimalBoardConf extends MassiveBoardConf {

    @Override
    public List<Ball> getSmallBalls() {
        var balls = new ArrayList<Ball>();
        double ballRadius = 6;

        // Creo solo 2 palline piccole
        // Per questo conf le posiziono inizialmente distanti fra loro per fare i test in maniera più semplice
        // (evitando di colpirne due assieme)
        balls.add(new Ball(new P2d(500, 400), new V2d(0, 0), ballRadius, Ball.BallType.REGULAR));
        balls.add(new Ball(new P2d(700, 400), new V2d(0, 0), ballRadius, Ball.BallType.REGULAR));

        return balls;
    }
}
