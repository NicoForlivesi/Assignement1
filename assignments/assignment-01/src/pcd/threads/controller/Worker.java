package pcd.threads.controller;

import pcd.threads.model.*;
import pcd.threads.util.Barrier;
import pcd.threads.util.Configuration;

import java.util.List;

public class Worker extends Thread {

    private final Board board;
    private final List<Ball> allBalls;
    private final int workerId;
    private final int nWorkers;
    private final Barrier barrier;
    private final int tableWidth;
    private final int tableHeight;

    // Flag volatile controllato dal Master per gestire l'avanzamento dei frame
    private volatile boolean running = true;

    public Worker(Board board, int workerId, int nWorkers,
                  Barrier barrier, int tableWidth, int tableHeight) {
        this.board = board;
        this.allBalls = board.getBalls();
        this.workerId = workerId;
        this.nWorkers = nWorkers;
        this.barrier = barrier;
        this.tableWidth = tableWidth;
        this.tableHeight = tableHeight;
    }

    public void terminate() {
        this.running = false;
        this.interrupt(); // sveglia il Worker se è bloccato in wait() sulla barriera
    }

    @Override
    public void run() {
        try {
            while (running) {
                // CP 0: Attesa fine inizializzazione frame da parte del Master.
                // Quando tutti (Master + N Worker) sono arrivati si va.
                barrier.hitAndWaitAll();

                if (!running) break;

                // FASE 1: Aggiornamento posizioni e bordi per il sottoinsieme assegnato
                double dt = board.getDt();
                double frictionFactor = Configuration.FRICTION_FACTOR;
                for (int i = workerId; i < allBalls.size(); i += nWorkers) {
                    allBalls.get(i).updatePosition(dt, frictionFactor, tableWidth, tableHeight);
                }

                // CP 1: tutti hanno finito la Fase 1
                barrier.hitAndWaitAll();

                // FASE 2: Collisioni basate sulla griglia Grid2D
                Grid2D grid = board.getGrid2D();
                int totalCells = grid.getTotalCells();
                int cols = grid.getCols();

                // Ogni worker elabora le sue celle saltando di nWorkers
                for (int cellIndex = workerId; cellIndex < totalCells; cellIndex += nWorkers) {
                    int r = cellIndex / cols;
                    int c = cellIndex % cols;

                    List<Ball> currentCellBalls = grid.getBallsAt(r, c);
                    if (currentCellBalls.isEmpty()) continue;

                    // Prendo il lock specifico sulla singola cella
                    // I worker distanti sul tavolo non si danno fastidio così
                    synchronized (grid.getLockAt(r, c)) { // Collisioni interne alla cella
                        for (int i = 0; i < currentCellBalls.size(); i++) {
                            Ball a = currentCellBalls.get(i);
                            for (int j = i + 1; j < currentCellBalls.size(); j++) {
                                checkAndResolveCollision(a, currentCellBalls.get(j));
                            }

                            // Collisioni esterne controllo solo quelle adiacenti
                            checkWithNeighborCell(a, r, c + 1, grid);
                            checkWithNeighborCell(a, r + 1, c, grid);
                            checkWithNeighborCell(a, r + 1, c + 1, grid);
                            checkWithNeighborCell(a, r + 1, c - 1, grid);
                        }
                    }
                }
                // CP 2: tutti hanno finito la Fase 2
                barrier.hitAndWaitAll();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    // Metodo helper per testare una pallina contro tutte le palline di una cella vicina
    private void checkWithNeighborCell(Ball a, int nr, int nc, Grid2D grid) {
        if (!grid.isValidCell(nr, nc)) return;

        synchronized (grid.getLockAt(nr, nc)) {
            List<Ball> neighborBalls = grid.getBallsAt(nr, nc);
            for (Ball b : neighborBalls) {
                checkAndResolveCollision(a, b);
            }
        }
    }

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