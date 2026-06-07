package pcd.tasks.controller;

import pcd.tasks.model.*;
import pcd.tasks.util.Configuration;

import java.util.List;
import java.util.concurrent.Callable;

/**
 * E' la classe che meglio viene rimodellata sotto forma di task, come detto a lezione i task modellano bene
 * compiti finiti, quindi i compiti finiti che ho nel sistema alla fine sono per ogni frame il movimento e la
 * seguente risoluzione delle collisioni*/
public class WorkerTask implements Callable<Void> {

    private final Board board;
    private final int workerId;
    private final int nWorkers;
//    private final Barrier barrier; Non c'è più la barriera, qui gestisco la sincronizzazione con "invokeAll"

    public WorkerTask(Board board, int workerId, int nWorkers) {
        this.board = board;
        this.workerId = workerId;
        this.nWorkers = nWorkers;
    }

    // Nel call ho solo la gestione delle collisioni, la fase del movimento ho dovuto spostarla dentro una lambda
    // nel gameEngine, devono restare separate, perché la fase 2 (collisioni) richiede che tutti abbiano finito la fase 1.
    // Questo si risolve con due chiamate invokeAll distinte nel GameEngine, quindi quando devo distribuire i task
    // del movimento dal GameEngine devo passare la lambda che descrive il da farsi, per distribuire i task della
    // risoluzione delle collissioni invece basta chiamare .add.
    @Override
    public Void call() {
        Grid2D grid = board.getGrid2D();
        int totalCells = grid.getTotalCells();
        int cols = grid.getCols();
        // Distribuzione ciclica del lavoro: il task elabora solo le celle che gli spettano
        for (int cellIndex = workerId; cellIndex < totalCells; cellIndex += nWorkers) {
            int r = cellIndex / cols;
            int c = cellIndex % cols;
            List<Ball> currentCellBalls = grid.getBallsAt(r, c);
            if (currentCellBalls.isEmpty()) continue;
            // Acquisizione del lock sulla singola cella.
            // Permette a task diversi di lavorare in parallelo su zone diverse del tavolo.
            synchronized (grid.getLockAt(r, c)) {
                for (int i = 0; i < currentCellBalls.size(); i++) {
                    Ball a = currentCellBalls.get(i);
                    for (int j = i + 1; j < currentCellBalls.size(); j++) {
                        checkAndResolveCollision(a, currentCellBalls.get(j));
                    }
                    checkWithNeighborCell(a, r, c + 1, grid);
                    checkWithNeighborCell(a, r + 1, c, grid);
                    checkWithNeighborCell(a, r + 1, c + 1, grid);
                    checkWithNeighborCell(a, r + 1, c - 1, grid);
                }
            }
        }
        return null;
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