package pcd.sequential.controller;

import pcd.sequential.model.*;
import pcd.sequential.util.Configuration;
import pcd.sequential.view.*;

import java.util.List;

// Semplice loop ora
public class GameEngine {

    private final Board board;
    private final View view;
    private final RenderSynch renderSynch;
    private volatile boolean running = true;

    public GameEngine(Board board, View view, RenderSynch renderSynch) {
        this.board = board;
        this.view = view;
        this.renderSynch = renderSynch;
    }

    public void run() {
        long lastUpdateTime = System.currentTimeMillis();
        long frameCount = 0;
        long totalComputeTime = 0;

        while (running && !board.isGameOver()) {
            long currentFrame = renderSynch.nextFrameToRender(); // Viene preso il "ticket" per il frame corrente

            // Calcolo dt reale in secondi
            long now = System.currentTimeMillis();
            double dt = (now - lastUpdateTime) * 0.001;
            lastUpdateTime = now;

            long computeStart = System.currentTimeMillis(); // Per il calcolo performance

            // Fase 1: aggiornamento posizioni sequenziale
            for (Ball b : board.getBalls()) {
                b.updatePosition(dt, Configuration.FRICTION_FACTOR,
                        Configuration.WINDOW_WIDTH,
                        Configuration.WINDOW_HEIGHT);
            }

            // Fase 2: collisioni sequenziale con nested loop
            List<Ball> balls = board.getBalls();
            for (int i = 0; i < balls.size() - 1; i++) {
                for (int j = i + 1; j < balls.size(); j++) {
                    checkAndResolveCollision(balls.get(i), balls.get(j));
                }
            }

            checkHolesAndScores();
            checkGameOver();

            long computeEnd = System.currentTimeMillis(); // Calcolo performance, lo fermo prima della
            // parte che si interfaccia col EDT così da avere misure consistenti in tutte le versioni
            totalComputeTime += (computeEnd - computeStart);
            frameCount++;

            view.display(); // Display della view

            try {
                // Attesa rendering del frame corrente prima di procedere al successivo
                renderSynch.waitForFrameRendered(currentFrame);
            } catch (InterruptedException e) {
                break;
            }
        }
        if (frameCount > 0) {
            System.out.printf("Tempo medio per frame: %.2f ms (su %d frame)%n",
                    (double) totalComputeTime / frameCount, frameCount);
        }
        System.exit(0);
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

    // Uguale a quello dei worker nella versione concorrente
    private void checkAndResolveCollision(Ball a, Ball b) {
        // Qui son tutte operazioni che faccio col lock sulla casella della griglia
        if (!a.stillAlive() || !b.stillAlive()) return;

        P2d posA = a.getPos();
        P2d posB = b.getPos();
        V2d velA = a.getVel();
        V2d velB = b.getVel();

        // Formule per la geometria
        double dx = posB.x() - posA.x();
        double dy = posB.y() - posA.y();
//        double distance = Math.hypot(dx, dy); Calcola la distanza euclidea, MA USA LA RADICE QUADRATA, uccide le
        // performance per questo assignemtn visto che va calcolata tantissime volte al secondo, uso un metodo più leggero
        double minDistance = a.getRadius() + b.getRadius();

        // Pre-check super veloce
        if (Math.abs(dx) > minDistance || Math.abs(dy) > minDistance) return;

        // OTTIMIZZAZIONE: Uso la distanza al quadrato e poi calcolo la radice SOLO se c'è una collisione
        double distSq = dx * dx + dy * dy;
        double minSq = minDistance * minDistance;

        if (distSq < minSq) {
            // vuol dire che c'è una collissione fra le due palline:
            // Siamo già dentro il blocco synchronized(grid.getLockAt(r, c)) del ciclo principale.
            double distance = Math.sqrt(distSq);
            if (distance == 0) return; // Protezione da divisioni per zero per sicurezza

            double nx = dx / distance; // urto elastico
            double ny = dy / distance;

            // Velocità relative
            double kx = velA.x() - velB.x();
            double ky = velA.y() - velB.y();
            double p = 2 * (nx * kx + ny * ky) / (a.getMass() + b.getMass());

            V2d newVelA = new V2d(velA.x() - p * b.getMass() * nx, velA.y() - p * b.getMass() * ny);
            V2d newVelB = new V2d(velB.x() + p * a.getMass() * nx, velB.y() + p * a.getMass() * ny);

            double overlap = minDistance - distance;
            P2d newPosA = new P2d(posA.x() - overlap * nx * 0.5, posA.y() - overlap * ny * 0.5);
            P2d newPosB = new P2d(posB.x() + overlap * nx * 0.5, posB.y() + overlap * ny * 0.5);

            a.updateVelocityAndPosition(newPosA, newVelA);
            b.updateVelocityAndPosition(newPosB, newVelB);

            // Aggiorno ultimo hit
            if (a.getType() == Ball.BallType.PLAYER || a.getType() == Ball.BallType.BOT) {
                b.setLastHitBy(a.getType());
            } else if (b.getType() == Ball.BallType.PLAYER || b.getType() == Ball.BallType.BOT) {
                a.setLastHitBy(b.getType());
            } else {
                // Se non siamo entrati in uno degli if precedenti vuol dire che l'ultimo tocco è stato di
                // una pallina regular e setto "lastHitBy" a regular
                a.setLastHitBy(Ball.BallType.REGULAR);
                b.setLastHitBy(Ball.BallType.REGULAR);
            }
        }
    }
}