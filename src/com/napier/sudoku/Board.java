package com.napier.sudoku;

public class Board {
    int[][] board; // 2D array to store the board
    int columns;
    int rows;
    int clues;

    public Board() {
        columns = 9;
        rows = 9;
        board = new int[rows][columns];
    }

    public void printBoard() {
        int rowsCounter = 1;
        // print columns numbering
        String columnNumbering = "    1 2 3   4 5 6   7 8 9 \n";
        System.out.print(columnNumbering);

        // print the grid
        for (int i = 0; i < rows; i++) {
            if(i % 3 == 0 && i != 0) {
                System.out.print("\n");
            }
            System.out.print(rowsCounter++ + " ");  // print row number

            for (int j = 0; j < columns; j++) {
                if(j % 3 == 0) {
                    System.out.print("| ");
                }
                System.out.print(board[i][j] + " ");
            }
            System.out.print("|\n");
        }
    }

    public void generateEasyBoard() {
    }

    public void generateMediumBoard() {
    }

    public void generateHardBoard() {
    }
}
