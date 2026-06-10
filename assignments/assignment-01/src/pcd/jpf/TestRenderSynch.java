package pcd.jpf;

import pcd.threads.view.RenderSynch;

public class TestRenderSynch {

    public static void main(String[] args) throws InterruptedException {
        RenderSynch renderSynch = new RenderSynch();

        // Simula il GameEngine
        Thread gameEngine = new Thread(() -> {
            try {
                for (int i = 0; i < 3; i++) {
                    long frame = renderSynch.nextFrameToRender();
                    // calcolo frame...
                    renderSynch.waitForFrameRendered(frame);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        // Simula l'EDT
        Thread edt = new Thread(() -> {
            try {
                for (int i = 0; i < 3; i++) {
                    renderSynch.notifyFrameRendered();
                }
            } catch (Exception e) {
                Thread.currentThread().interrupt();
            }
        });

        gameEngine.start();
        edt.start();
        gameEngine.join();
        edt.join();
    }
}