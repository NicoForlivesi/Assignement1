package pcd.threads.util;

/**
 * Raccolgo le costanti principali in questo file di configurazione
 * */
public class Configuration {

    /** Configurazione del Pool di Thread
     * La macchina che sto utilizzando ha 12 core fisici e 24 logici. */
    public static final int N_WORKERS = 13;

    // Dimensioni della finestra di gioco
    public static final int WINDOW_WIDTH = 1200;
    public static final int WINDOW_HEIGHT = 800;

    public static final double PLAYER_BALL_SPEED = 220; // in pixel/secondo
    public static final double BOT_BALL_SPEED = 220;
    public static final double FRICTION_FACTOR = 120; // è in pixel/secondo^2
}