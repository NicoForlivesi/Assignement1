package pcd.tasks.controller;

import pcd.tasks.model.*;

import java.util.Random;

import static pcd.threads.util.Configuration.BOT_BALL_SPEED;

// Classe che semplicemente gestisce il comportamento del bot, dando un impulso in direzione casuale
// ogni 3000ms
public class BotController extends Thread {

    private final Board board;
    private final Random random = new Random();

    // Intervallo tra un impulso e il successivo (ms)
    private static final long KICK_INTERVAL_MS = 3000;

    public BotController(Board board) {
        this.board = board;
        setDaemon(true); // muore con il programma principale automaticamente
    }

    @Override
    public void run() {
        while (!board.isGameOver()) {
            try {
                Thread.sleep(KICK_INTERVAL_MS);
            } catch (InterruptedException e) {
                break;
            }

            Ball bot = board.getBotBall();
            if (bot == null || !bot.stillAlive()) { continue; }

            double angle = random.nextDouble() * 2 * Math.PI;
            bot.kick(new V2d(Math.cos(angle) * BOT_BALL_SPEED, Math.sin(angle) * BOT_BALL_SPEED));
        }
    }
}