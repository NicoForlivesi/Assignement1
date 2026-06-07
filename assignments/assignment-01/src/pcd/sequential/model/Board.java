package pcd.sequential.model;

import java.util.List;

public class Board {
    private final List<Ball> balls;
    private int scorePlayer = 0;
    private int scoreBot = 0;
    private volatile boolean isGameOver = false;

    public Board(List<Ball> balls) {
        this.balls = balls;
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