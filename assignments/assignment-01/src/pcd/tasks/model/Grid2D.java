package pcd.tasks.model;

import pcd.tasks.util.Configuration;

import java.util.ArrayList;
import java.util.List;

public class Grid2D {
    private final int cellSize;
    private final int cols;
    private final int rows;

    private final List<Ball>[][] grid;
    private final Object[][] cellLocks;

    public Grid2D(int cellSize) {
        this.cellSize = cellSize;
        this.cols = (int) Math.ceil((double) Configuration.WINDOW_WIDTH / cellSize);
        this.rows = (int) Math.ceil((double) Configuration.WINDOW_HEIGHT / cellSize);

        this.grid = new ArrayList[rows][cols];
        this.cellLocks = new Object[rows][cols];

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                grid[r][c] = new ArrayList<>();
                cellLocks[r][c] = new Object();
            }
        }
    }

    /**
     * Svuota la griglia e riposiziona tutte le palline vive nelle rispettive celle.
     */
    public void populate(List<Ball> balls) {
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                grid[r][c].clear();
            }
        }

        for (Ball b : balls) {
            if (!b.stillAlive()) continue;
            P2d pos = b.getPos();
            int c = (int) (pos.x() / cellSize);
            int r = (int) (pos.y() / cellSize);

            // sicurezza per evitare "IndexOutOfBounds" sui bordi estremi
            c = Math.max(0, Math.min(c, cols - 1));
            r = Math.max(0, Math.min(r, rows - 1));

            grid[r][c].add(b);
        }
    }

    public List<Ball> getBallsAt(int r, int c) {
        return grid[r][c];
    }

    public Object getLockAt(int r, int c) {
        return cellLocks[r][c];
    }

    public int getCols() { return cols; }
    public int getTotalCells() { return rows * cols; }

    public boolean isValidCell(int r, int c) {
        return r >= 0 && r < rows && c >= 0 && c < cols;
    }
}