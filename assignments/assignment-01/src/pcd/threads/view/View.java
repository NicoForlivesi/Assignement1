package pcd.threads.view;

import pcd.threads.model.Board;
import pcd.threads.util.BoundedBuffer;

public class View {

    private final ViewFrame frame;

    public View(Board board, BoundedBuffer<Integer> inputBuffer, RenderSynch renderSynch) {
        this.frame = new ViewFrame(board, inputBuffer, renderSynch);
    }

    public void display() {
        frame.display();
    }
}