package pcd.threads.controller;

import pcd.threads.model.Ball;
import pcd.threads.model.Board;
import pcd.threads.model.P2d;
import pcd.threads.util.Barrier;
import pcd.threads.util.Configuration;
import pcd.threads.util.CyclicBarrierImpl;
import pcd.threads.view.RenderSynch;
import pcd.threads.view.View;

import java.util.ArrayList;
import java.util.List;

public class GameEngine extends Thread {

    private final Board board;
    private final View view;
    private final RenderSynch renderSynch;
    private final List<Worker> workers;
    private final Barrier masterBarrier;
    private final InputController inputC;
    private volatile boolean running = true;

    public GameEngine(Board board, View view, RenderSynch renderSynch, InputController inputC) {
        this.board = board;
        this.view = view;
        this.renderSynch = renderSynch;
        this.inputC = inputC;
        this.workers = new ArrayList<>();

        int nWorkers = Configuration.N_WORKERS;
        // La barriera ha nWorkers + 1 (i Worker impostati + il Master)
        // Viene usata per 3 checkpoint per frame:
        // CP 0: Master ha aggiornato la griglia, i Worker possono iniziare la Fase 1
        // CP 1: Tutti i Worker hanno aggiornato le posizioni
        // CP 2: Tutti i Worker hanno concluso le collisioni
        this.masterBarrier = new CyclicBarrierImpl(nWorkers + 1);

        for (int i = 0; i < nWorkers; i++) {
            workers.add(new Worker(
                    board, i, nWorkers, masterBarrier,
                    Configuration.WINDOW_WIDTH, Configuration.WINDOW_HEIGHT
            ));
        }
    }

    @Override
    public void start() { // Avvio i worker solo quando parte l'engine, è l'unico metodo effettivamente
        // chiamato dall'esterno di questo componente attivo
        for (Worker worker : workers) {
            worker.start();
        }
        super.start();
    }

    @Override
    public void run() {
        try {
            long lastUpdateTime = System.currentTimeMillis();
            while (running && !board.isGameOver()) {
                long currentFrame = renderSynch.nextFrameToRender(); // Viene preso il "ticket" per il frame corrente

                // Calcolo dt reale in secondi
                long now = System.currentTimeMillis();
                double dt = (now - lastUpdateTime) * 0.001;
                lastUpdateTime = now;

                // Il Master aggiorna la griglia e il dt prima di dare il via ai Worker
                board.updateGrid2D();
                board.setDt(dt);

                masterBarrier.hitAndWaitAll(); // CP 0: Il Master dà il via al frame
                masterBarrier.hitAndWaitAll(); // CP 1: Attesa fine FASE 1 (Movimento)
                masterBarrier.hitAndWaitAll(); // CP 2: Attesa fine FASE 2 (Collisioni)

                checkHolesAndScores();
                checkGameOver();

                view.display(); // Display della view

                // Attesa rendering del frame corrente prima di procedere al successivo
                renderSynch.waitForFrameRendered(currentFrame);

            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            for (Worker worker : workers) {
                worker.terminate();
            }
            inputC.interrupt(); // sblocca la get() bloccante sennoò il componente potrebbe non accorgersi
            // che è Game Over essendo fermo sulla get
        }
    }

    private void checkHolesAndScores() {
        double holeRadius = 50;
        double holeRadiusSq = holeRadius * holeRadius;
        double[][] holes = {{0, 0}, {Configuration.WINDOW_WIDTH, 0}};

        List<Ball> balls = board.getBalls();
        Ball playerBall = board.getPlayerBall();
        Ball botBall = board.getBotBall();

        for (Ball b : balls) {
            if (!b.stillAlive()) continue;
            P2d pos = b.getPos();
            for (double[] hole : holes) {
                // Uso la distanza al quadrato per ottimizzare così evito la radice
                double dx = pos.x() - hole[0];
                double dy = pos.y() - hole[1];
                double distSq = dx * dx + dy * dy;
                if (distSq < holeRadiusSq) {
                    b.setStillAlive(false);

                    // Il gioco finisce se la pallina di un giocatore va in buca.
                    if (b == playerBall) {
                        System.out.println("GAME OVER: Il Player è caduto in buca! Vince il Bot.");
                        board.setGameOver(true);
                        running = false;
                    } else if (b == botBall) {
                        System.out.println("GAME OVER: Il Bot è caduto in buca! Vince il Player.");
                        board.setGameOver(true);
                        running = false;
                    } else {
                        // È caduta una pallina REGULAR, deleghiamo l'incremento alla Board se a colpirla
                        // è stato il player o il bot.
                        if (b.getLastHitBy() == Ball.BallType.PLAYER) {
                            board.incrementPlayerScore();
                        } else if (b.getLastHitBy() == Ball.BallType.BOT) {
                            board.incrementBotScore();
                        }
                    }
                }
            }
        }
    }

    private void checkGameOver() {
        // Il gioco finisce quando non ci sono più palline sul tavolo.
        for (Ball b : board.getBalls()) {
            if (b.getType() == Ball.BallType.REGULAR && b.stillAlive()) {
                return; // stop se almeno una è viva
            }
        }
        System.out.println("GAME OVER: Non ci sono più palline! Vince chi ha il punteggio più alto.");
        board.setGameOver(true);
        running = false;
    }
}