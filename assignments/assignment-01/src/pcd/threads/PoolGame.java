package pcd.threads;

import pcd.threads.controller.BotController;
import pcd.threads.controller.GameEngine;
import pcd.threads.controller.InputController;
import pcd.threads.model.*;
import pcd.threads.util.Configuration;
import pcd.threads.view.RenderSynch;
import pcd.threads.view.View;

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
        InputController inputController = new InputController(board); // Consumatore di input da tastiera
        inputController.start();
        BotController botController = new BotController(board); // Creazione del componente attivo che
        // gestisce il movimento casuale del bot
        botController.start();

        View view = new View(board, inputController, renderSynch); // Inizializzazione view

        return new GameEngine(board, view, renderSynch, inputController); // Ritorno il GameEngine pronto
    }
}