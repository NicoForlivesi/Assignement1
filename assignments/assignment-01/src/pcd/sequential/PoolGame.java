package pcd.sequential;

import pcd.sequential.model.*;
import pcd.sequential.view.*;
import pcd.sequential.controller.*;
import pcd.sequential.util.*;

import java.util.ArrayList;
import java.util.List;

public class PoolGame {

    public static void main(String[] args) {
        // Scegliere una delle configurazioni, lascio le altre commentate per poter testare velocemente
        // configurazioni diverse
//        BoardConf conf = new MinimalBoardConf();
//        BoardConf conf = new LargeBoardConf();
        BoardConf conf = new MassiveBoardConf();

        List<Ball> allBalls = new ArrayList<>();
        allBalls.add(conf.getPlayerBall());
        allBalls.add(conf.getBotBall());
        allBalls.addAll(conf.getSmallBalls());

        Board board = new Board(allBalls);
        RenderSynch renderSynch = new RenderSynch(); // Monitor di sincronizzazione fra GameEngine e EDT
        BoundedBuffer<Integer> inputBuffer = new BoundedBufferImpl<>(100);
        InputController inputController = new InputController(board, inputBuffer); // Consumatore di input da tastiera
        BotController botController = new BotController(board); // Creazione del componente attivo che
        // gestisce il movimento casuale del bot
        View view = new View(board, inputBuffer, renderSynch); // Inizializzazione view
        GameEngine engine = new GameEngine(board, view, renderSynch);

        inputController.start();
        botController.start();
        engine.run(); // Non più un Thread separato, chiamata bloccante che ritorna solo quando
        // finisce la partita, occupandosi anche di tutto il lavoro della risoluzione delle collisioni
        // che nella versione concorrente facevano i worker.
    }
}