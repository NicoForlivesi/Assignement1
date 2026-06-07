package pcd.sequential.view;

import pcd.sequential.controller.InputController;
import pcd.sequential.model.Board;

public class View {

    private final ViewFrame frame;

    public View(Board board, InputController inputController, RenderSynch renderSynch) {
        this.frame = new ViewFrame(board, inputController, renderSynch);
    }

    public void display() {
        frame.display();
    }
}