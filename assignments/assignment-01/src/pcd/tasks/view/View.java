package pcd.tasks.view;

import pcd.tasks.model.Board;
import pcd.tasks.util.BoundedBuffer;

public class View {

    private final ViewFrame frame;

    public View(Board board, BoundedBuffer<Integer> inputBuffer, RenderSynch renderSynch) {
        this.frame = new ViewFrame(board, inputBuffer, renderSynch);
    }

    public void display() {
        frame.display();
    }
}