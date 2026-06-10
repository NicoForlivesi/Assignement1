package pcd.tasks.controller;

import pcd.tasks.model.*;
import pcd.tasks.util.*;
import pcd.tasks.view.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class GameEngine extends Thread {

    private final Board board;
    private final View view;
    private final RenderSynch renderSynch;
    private final int nWorkers;
    private final ExecutorService executor;
    private final InputController inputC;
    private volatile boolean running = true;

    public GameEngine(Board board, View view, RenderSynch renderSynch, InputController inputC) {
        this.board = board;
        this.view = view;
        this.renderSynch = renderSynch;
        this.inputC = inputC;
        this.nWorkers = Configuration.N_WORKERS;
        this.executor = Executors.newFixedThreadPool(nWorkers); // Creazione del pool di thread. Fissato a nWorkers
    }

    @Override
    public void run() {
        long frameCount = 0;
        long totalComputeTime = 0;
        try {
            long lastUpdateTime = System.currentTimeMillis();
            while (running && !board.isGameOver()) {
                long currentFrame = renderSynch.nextFrameToRender(); // Viene preso il "ticket" per il frame corrente

                // Calcolo dt reale in secondi
                long now = System.currentTimeMillis();
                double dt = (now - lastUpdateTime) * 0.001;
                lastUpdateTime = now;
                long computeStart = System.currentTimeMillis(); // Per il calcolo performance

                // Il Master aggiorna la griglia e il dt prima di dare il via ai Worker
                board.updateGrid2D();
                board.setDt(dt);

                // fase1: task per aggiornare le posizioni
                List<Callable<Void>> phase1Tasks = buildMoveTasks();
                // invokeAll è un'operazione bloccante, il Master si ferma finché tutti
                // i task della fase 1 non sono completati. Sostituisce la prima barriera del caso threads.
                for (Future<Void> f : executor.invokeAll(phase1Tasks)) {
                    f.get(); // rilancia come ExecutionException se il task ha fallito
                }

                // fase2: task per risolvere le collisioni
                List<Callable<Void>> phase2Tasks = buildCollisionTasks();
                // Analogamente, attende che tutti i task delle collisioni abbiano finito.
                // Sostituisce la seconda barriera.
                for (Future<Void> f : executor.invokeAll(phase2Tasks)) { // Il pool di threads creato dall'execuotor va
                    // automaticamente ad eseguire .call
                    f.get();
                }
                /*
                * Sui due for: in realtà qui le future di ritorno non servono, nel senso che invokeAll viene sfruttato
                * proprio al posto delle barriere, sempplicemente garantisce che tutti i task di quel tipo sono stati
                * completati, i for li ho messi solo per gestire l'eccezione nel caso in cui un task non vada a buon fine,
                * senza la lista di future di ritorno da executor.invokeAll(phase1Tasks) verrebbe completamente ignorata ma
                * funzionerebbe lo stesso. */

                checkHolesAndScores();
                checkGameOver();

                long computeEnd = System.currentTimeMillis(); // Calcolo performance, lo fermo prima della
                // parte che si interfaccia col EDT così da avere misure consistenti in tutte le versioni
                totalComputeTime += (computeEnd - computeStart);
                frameCount++;

                view.display(); // Display della view

                // Attesa rendering del frame corrente prima di procedere al successivo
                renderSynch.waitForFrameRendered(currentFrame);

            }
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
        } finally {
            if (frameCount > 0) {
                System.out.printf("Tempo medio per frame: %.2f ms (su %d frame)%n",
                        (double) totalComputeTime / frameCount, frameCount);
            }
            executor.shutdownNow(); // interrompe i thread del pool
            waitNSec(4); // Per far leggere "game over"
            System.exit(0);
        }
    }

    private List<Callable<Void>> buildMoveTasks() {
        List<Callable<Void>> tasks = new ArrayList<>();
        for (int i = 0; i < nWorkers; i++) {
            final int id = i;
            tasks.add(() -> { // comportamento descritto in una lambda
                double dt = board.getDt();
                List<Ball> balls = board.getBalls();
                for (int j = id; j < balls.size(); j += nWorkers) {
                    balls.get(j).updatePosition(dt, Configuration.FRICTION_FACTOR,
                            Configuration.WINDOW_WIDTH, Configuration.WINDOW_HEIGHT);
                }
                return null;
            });
        }
        return tasks;
    }

    private List<Callable<Void>> buildCollisionTasks() {
        List<Callable<Void>> tasks = new ArrayList<>();
        for (int i = 0; i < nWorkers; i++) {
            tasks.add(new WorkerTask(board, i, nWorkers)); // comportamento da eseguire nel .call dell'oggetto
        }
        return tasks;
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

    private void waitNSec(int n) {
        try {
            Thread.sleep(n * 1000L);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}