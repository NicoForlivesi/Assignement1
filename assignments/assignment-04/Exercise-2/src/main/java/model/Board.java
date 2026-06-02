package model;

import java.io.Serializable;

public class Board implements Serializable {

    public static final String EMPTY = "-";

    private final String[][] cells = new String[3][3];

    public Board() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                cells[i][j] = EMPTY;
            }
        }
    }

    /** Ritorna "false" se la cella è occupata, altrimenti posiziona il simbolo nella cella e torna "true". */
    public boolean set(int r, int c, String symbol) {
        if (!cells[r][c].equals(EMPTY)) return false;
        cells[r][c] = symbol;
        return true;
    }

    public boolean checkWinner(String s) {
        for (int i = 0; i < 3; i++) {
            // Controllo righe
            if (get(i, 0).equals(s) && get(i, 1).equals(s) && get(i, 2).equals(s)) return true;
            // Controllo colonne
            if (get(0, i).equals(s) && get(1, i).equals(s) && get(2, i).equals(s)) return true;
        }
        // Controllo diagonali
        return (get(0, 0).equals(s) && get(1, 1).equals(s) && get(2, 2).equals(s))
                || (get(0, 2).equals(s) && get(1, 1).equals(s) && get(2, 0).equals(s));
    }

    public boolean isFull() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (cells[i][j].equals(EMPTY)) return false;
            }
        }
        return true;
    }

    private String get(int r, int c) {
        return cells[r][c];
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("\n");
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                sb.append(cells[i][j]).append(" ");
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}