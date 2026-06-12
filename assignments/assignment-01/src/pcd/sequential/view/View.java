package pcd.sequential.view;

import pcd.sequential.model.Board;
import pcd.sequential.util.BoundedBuffer;

public class View {

    private final ViewFrame frame;

    public View(Board board, BoundedBuffer<Integer> inputBuffer, RenderSynch renderSynch) {
        this.frame = new ViewFrame(board, inputBuffer, renderSynch);
    }

    public void display() {
        frame.display();
    }
}