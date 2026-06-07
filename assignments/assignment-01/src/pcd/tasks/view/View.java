package pcd.tasks.view;

import pcd.tasks.controller.InputController;
import pcd.tasks.model.Board;

public class View {

    private final ViewFrame frame;

    public View(Board board, InputController inputController, RenderSynch renderSynch) {
        this.frame = new ViewFrame(board, inputController, renderSynch);
    }

    public void display() {
        frame.display();
    }
}