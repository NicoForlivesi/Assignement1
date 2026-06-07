package pcd.tasks.model;

import java.util.List;

public class Board {
    private final List<Ball> balls;
    private final Grid2D grid;
    private int scorePlayer = 0;
    private int scoreBot = 0;
    private volatile boolean isGameOver = false;
    private volatile double dt = 0.016;

    public Board(List<Ball> balls) {
        this.balls = balls;
        this.grid = new Grid2D(40);
    }

    public void updateGrid2D() { // Chiamato solo dal master prima del checkpoint 0
        this.grid.populate(this.balls);
    }

    public Grid2D getGrid2D() { // chiamato dai worker ma accesso sicuro avendo gestito il tutto
        // con il metodo "barriera", non ci sono race condition
        return grid;
    }

    public void setDt(double dt) { // anche questo chiamato solo dal master prima del cp 0
        this.dt = dt;
    }

    public double getDt() { // anche per getDt non ci sono race condition per l'implementazione fatta
        // prima del successivo setDt del master, tutti i worker fanno una getDt
        return dt;
    }

    public List<Ball> getBalls() {
        return balls; // Le Ball sono già thread-safe
    }

    public Ball getPlayerBall() {
        return balls.stream()
                .filter(b -> b.getType() == Ball.BallType.PLAYER)
                .findFirst()
                .orElse(null);
    }

    public Ball getBotBall() {
        return balls.stream()
                .filter(b -> b.getType() == Ball.BallType.BOT)
                .findFirst()
                .orElse(null);
    }

    // Metodi synchronized per gestire i punteggi
    public synchronized void incrementPlayerScore() { scorePlayer++; }
    public synchronized void incrementBotScore() { scoreBot++; }
    public synchronized int getScorePlayer() { return scorePlayer; }
    public synchronized int getScoreBot() { return scoreBot; }

    // Il campo è volatile, per un boolean non serve synchronized per getter e setter
    public void setGameOver(boolean over) { this.isGameOver = over; }
    public boolean isGameOver() { return isGameOver; }
}