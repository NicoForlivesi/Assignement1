package pcd.threads.model;

/**
 * Entità protetta tramite metodi synchronized.
 */
public class Ball {
    public enum BallType { PLAYER, BOT, REGULAR }

    private final BallType type; // Tipo di pallina
    private BallType lastHitBy = null; // Traccia chi l'ha colpita per ultima

    private P2d pos;
    private V2d vel;

    private final double radius;
    private final double mass;
    private boolean stillAlive; // false se è caduta in un buco

    public Ball(P2d pos, V2d vel, double radius, BallType type) {
        this.type = type;
        this.pos = pos;
        this.vel = vel;
        this.radius = radius;
        this.mass = radius * radius;
        this.stillAlive = true;
    }

    public BallType getType() { return type; }
    public double getRadius() { return radius; }
    public double getMass() { return mass; }

    // Getters synchronized per l'accesso sicuro.
    // Non serve definire metodi synchronized per campi final
    public synchronized BallType getLastHitBy() { return lastHitBy; }
    public synchronized P2d getPos() { return pos; }
    public synchronized V2d getVel() { return vel; }
    public synchronized boolean stillAlive() { return stillAlive; }

    public synchronized void setLastHitBy(BallType hitter) { this.lastHitBy = hitter; }
    public synchronized void setStillAlive(boolean stillAlive) { this.stillAlive = stillAlive; }

    public synchronized void updatePosition (double dt, double frictionFactor, int width, int height) {
        if (!stillAlive) { return; }

        double speed = vel.abs();

        // Se la pallina si muove più velocemente della soglia minima, applico l'attrito lineare
        if (speed > 0.1) {
            double dec = frictionFactor * dt;
            double factor = Math.max(0, speed - dec) / speed;
            vel = vel.mul(factor);
        } else {
            // Sotto la soglia pallina stoppata del tutto
            vel = new V2d(0, 0);
        }

        pos = pos.sum(vel.mul(dt)); // Aggiornamento dello spazio

        // Gestione rimbalzi sui bordi
        if (pos.x() - radius < 0) {
            pos = new P2d(radius, pos.y());
            vel = vel.getSwappedX();
        } else if (pos.x() + radius > width) {
            pos = new P2d(width - radius, pos.y());
            vel = vel.getSwappedX();
        }
        if (pos.y() - radius < 0) {
            pos = new P2d(pos.x(), radius);
            vel = vel.getSwappedY();
        } else if (pos.y() + radius > height) {
            pos = new P2d(pos.x(), height - radius);
            vel = vel.getSwappedY();
        }
    }

    // Metodo per applicare un impulso istantaneo (usato dal Controller per il Player)
    public synchronized void kick(V2d newVel) {
        this.vel = newVel;
    }

    // Aggiornamento dello stato durante una collisione
    public synchronized void updateVelocityAndPosition(P2d newPos, V2d newVel) {
        this.pos = newPos;
        this.vel = newVel;
    }
}