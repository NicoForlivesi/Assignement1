package pcd.tasks;

import pcd.tasks.model.*;
import pcd.tasks.controller.*;
import pcd.tasks.util.*;
import pcd.tasks.view.*;
import pcd.tasks.view.View;

import java.util.ArrayList;
import java.util.List;

public class PoolGame {

    public static void main(String[] args) {
        System.out.println("Inizializzazione Pool Game...");

        GameEngine engine = buildGameEngine();

        System.out.println("Pool Game avviato con " + Configuration.N_WORKERS + " Worker Thread.");
        engine.start();
    }

    // collega tutti i componenti MVC e restituire direttamente il componente attivo pronto sul quale chiamare "start"
    private static GameEngine buildGameEngine() {

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


        inputController.start();
        botController.start();
        return new GameEngine(board, view, renderSynch); // Ritorno il GameEngine pronto
    }
}