package pcd.sequential;

import pcd.sequential.model.*;
import pcd.sequential.view.*;
import pcd.sequential.controller.*;

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
        RenderSynch renderSynch = new RenderSynch();
        InputController inputController = new InputController(board);
        inputController.start();
        BotController botController = new BotController(board);
        botController.start();
        View view = new View(board, inputController, renderSynch);

        GameEngine engine = new GameEngine(board, view, renderSynch);
        engine.run(); // Non più un Thread separato, chiamata bloccante che ritorna solo quando
        // finisce la partita, occupandosi anche di tutto il lavoro della risoluzione delle collisioni
        // che nella versione concorrente facevano i worker.
    }
}