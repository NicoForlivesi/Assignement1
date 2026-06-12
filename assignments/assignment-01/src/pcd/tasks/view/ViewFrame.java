package pcd.tasks.view;

import pcd.tasks.controller.*;
import pcd.tasks.model.*;
import pcd.tasks.util.Configuration;
import pcd.tasks.util.BoundedBuffer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.HashSet;
import java.util.Set;

public class ViewFrame extends JFrame {

    private final Board board;
    private final RenderSynch renderSynch;
    private final VisualizerPanel panel;

    private final Set<Integer> pressedKeys = new HashSet<>();

    public ViewFrame(Board board, BoundedBuffer<Integer> inputBuffer, RenderSynch renderSynch) {
        this.board = board;
        this.renderSynch = renderSynch;

        setTitle("PCD Assignment 1 - Pool Game");
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                board.setGameOver(true); // segnala al GameEngine di terminare
                dispose();
            }
        });
        setResizable(false);

        this.panel = new VisualizerPanel();
        this.panel.setPreferredSize(new Dimension(Configuration.WINDOW_WIDTH, Configuration.WINDOW_HEIGHT));
        this.add(panel);

        this.pack();

        this.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                pressedKeys.add(e.getKeyCode());
            }

            @Override
            public void keyReleased(KeyEvent e) {
                pressedKeys.remove(e.getKeyCode());
            }
        });

        Timer inputTimer = new Timer(15, e -> {
            for (int key : pressedKeys) {
                try {
                    inputBuffer.put(key); // produttore per il BoundedBuffer
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }
        });
        inputTimer.start();

        setLocationRelativeTo(null);
        setVisible(true);
    }

    public void display() {
        SwingUtilities.invokeLater(() -> {
            panel.paintImmediately(0, 0, panel.getWidth(), panel.getHeight());
            renderSynch.notifyFrameRendered();
        });
    }

    private class VisualizerPanel extends JPanel {

        private int frames = 0;
        private int currentFPS = 0;
        private long lastTimeCheck = System.currentTimeMillis();

        public VisualizerPanel() {
            setBackground(Color.WHITE);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); // Un po di
            // antialiasing per rendere il gioco un po più bellino

            // Calcolo degli FPS
            long currentTime = System.currentTimeMillis();
            frames++;
            if (currentTime - lastTimeCheck >= 1000) {
                currentFPS = frames;
                frames = 0;
                lastTimeCheck = currentTime;
            }

            // Buche
            g2.setColor(Color.BLACK);
            int holeRadius = 50;
            g2.fillOval(-holeRadius, -holeRadius, holeRadius * 2, holeRadius * 2);
            g2.fillOval(Configuration.WINDOW_WIDTH - holeRadius, -holeRadius, holeRadius * 2, holeRadius * 2);

            // Palline, PlayerBall e BotBall
            for (Ball b : board.getBalls()) {
                if (!b.stillAlive()) continue;

                P2d pos = b.getPos();
                int x = (int) (pos.x() - b.getRadius());
                int y = (int) (pos.y() - b.getRadius());
                int size = (int) (b.getRadius() * 2);

                if (b.getType() == Ball.BallType.PLAYER) {
                    g2.setColor(Color.WHITE);
                    g2.fillOval(x, y, size, size);
                    g2.setColor(Color.BLACK);
                    g2.drawOval(x, y, size, size);
                } else if (b.getType() == Ball.BallType.BOT) {
                    g2.setColor(Color.GRAY);
                    g2.fillOval(x, y, size, size);
                } else {
                    g2.setColor(Color.BLACK);
                    g2.fillOval(x, y, size, size);
                }
            }

            // Punteggi
            g2.setColor(Color.RED);
            g2.setFont(new Font("Arial", Font.BOLD, 18));
            String scoreText = "PLAYER: " + board.getScorePlayer() + " | BOT: " + board.getScoreBot();
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(scoreText, (Configuration.WINDOW_WIDTH - fm.stringWidth(scoreText)) / 2, 25);

            // FPS
            g2.setColor(Color.RED);
            g2.setFont(new Font("Arial", Font.BOLD, 16));
            g2.drawString("FPS: " + currentFPS, 70, 20);

            // Game over
            if (board.isGameOver()) {
                g2.setFont(new Font("Arial", Font.BOLD, 50));
                g2.setColor(Color.RED);
                String gameOverText = "GAME OVER";
                g2.drawString(gameOverText, (getWidth() - g2.getFontMetrics().stringWidth(gameOverText)) / 2, getHeight() / 2);
            }
        }
    }
}