package com.napier.sudoku;

import java.util.*;

public class Board {
    int[][] board; // 2D array to store the board to play
    int[][] completeBoard; // original board to compare against
    int[][] initialBoard; // board with empty cells at the start of the game
    String[][] possibleValues; // 2D array to store all possible values for each cell
    int columns;
    int rows;
    int emptyCells;

    public Board() {
        this.columns = 9;
        this.rows = 9;
        this.board = new int[rows][columns];
        this.completeBoard = new int[rows][columns];
        this.initialBoard = new int[rows][columns];
        this.possibleValues = new String[rows][columns];
    }

    /**
     * Prints the entire sudoku board to the console
     */
    public void printBoard() {
        int rowsCounter = 1;
        // print columns numbering
        String columnNumbering = "       1 2 3   4 5 6   7 8 9 \n\n";
        System.out.print(columnNumbering);

        // print the grid
        for (int i = 0; i < rows; i++) {
            if(i % 3 == 0 && i != 0) {
                System.out.print("\n");
            }
            System.out.print(rowsCounter++ + "    ");  // print row number

            for (int j = 0; j < columns; j++) {
                if(j % 3 == 0) {
                    System.out.print("| ");
                }
                if(board[i][j] == 0) {
                    System.out.print("_ ");
                }
                else {
                    System.out.print(board[i][j] + " ");
                }
            }
            System.out.print("|\n");
        }
    }

    /**
     * Helper function
     */
    public void printOriginal() {
        for(int i = 0; i < rows; i++) {
            for(int j = 0; j < columns; j++) {
                System.out.print(completeBoard[i][j] + " ");
            }
            System.out.println();
        }
    }

    /**
     * Helper function
     */
    public void printInitial() {
        for(int i = 0; i < rows; i++) {
            for(int j = 0; j < columns; j++) {
                System.out.print(initialBoard[i][j] + " ");
            }
            System.out.println();
        }
    }

    /**
     * Generates a ready-to-play easy sudoku board
     */
    public void generateEasyBoard() {
        System.out.println("Preparing an easy board...");
        try {
            // generate a number of empty cells between 40-45
            Random rand = new Random();
            int empty = rand.nextInt(40, 46);  // upper is exclusive, lower inclusive
            this.emptyCells = empty;
            generateSudoku();
            determineEmptyCells(empty);
        }
        catch (Exception ex) {
            System.out.println("Something went wrong. Please try again.");
        }

    }

    /**
     * Generates a ready-to-play medium sudoku board
     */
    public void generateMediumBoard() {
        try {
            System.out.println("Preparing a medium board...");
            // generate a number of empty cells between 46-49
            Random rand = new Random();
            int empty = rand.nextInt(46, 50);  // upper is exclusive, lower inclusive
            this.emptyCells = empty;
            generateSudoku();
            determineEmptyCells(empty);
        }
        catch (Exception ex) {
            System.out.println("Something went wrong. Please try again.");
        }

    }

    /**
     * Generates a ready-to-play hard sudoku board
     */
    public void generateHardBoard() {
        try {
            System.out.println("Preparing a hard board...");
            // generate a number of empty cells between 50-53
            Random rand = new Random();
            int empty = rand.nextInt(50, 54);  // upper is exclusive, lower inclusive
            this.emptyCells = empty;
            generateSudoku();
            determineEmptyCells(empty);
        }
        catch (Exception ex) {
            System.out.println("Something went wrong. Please try again.");
        }

    }

    /**
     * Generates a full sudoku board
     */
    private void generateSudoku() {
        // initialise all possible values to be all possible
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < columns; c++) {
                possibleValues[r][c] = "123456789";
            }
        }
        // populate the cells by using backtracking algorithm to solve the board
        try {
            populateBoard();
        }
        catch (Exception ex) {
            // print an error message if something goes wrong
            System.out.println("There was an error trying to generate a new sudoku board. Please try again");
        }
    }

    /**
     * Backtracking method for populating the whole board
     */
    private boolean populateBoard() {
        boolean solved = false;
        String cellLeft = cellsLeftToFill();
        // first, check if there are any cells left to fill
        if(cellLeft.isEmpty()) {
            return true;
        }
        // else, start with the found cell and fill the rest
        else {
            // get the cell values
            int row = Character.getNumericValue(cellLeft.charAt(0));
            int column = Character.getNumericValue(cellLeft.charAt(1));
            // randomise the possible values
            String values = randomizeValues(possibleValues[row][column]);
            String[] splitValues = values.split("");
            for (String value : splitValues) {
                // check if it is safe to place the value there
                if(isStepPossible(row, column, Integer.valueOf(value))) {
                    // if safe, assign the value to the cell
                    board[row][column] = Integer.valueOf(value);
                    completeBoard[row][column] = Integer.valueOf(value);
                    initialBoard[row][column] = Integer.valueOf(value);
                    if(populateBoard()) {
                        return true;
                    }
                    else {
                        // else, empty the cell
                        board[row][column] = 0;
                        completeBoard[row][column] = 0;
                        initialBoard[row][column] = 0;
                        // remove the possible value for this cell
                        if(possibleValues[row][column].contains(value)) {
                            possibleValues[row][column].replace(value, "");
                        }
                    }
                }
            }
            return false;
        }
    }

    /**
     * Returns coordinates of a first cell with the least possible values
     * @return  String containing row and column coordinates
     */
    private String findCellWithLeastPossibles() {
        String cellCoordinates = "";
        int min = 10;
        for (int r = 1; r <= 9; r++) {
            for (int c = 1; c <= 9; c++) {
                if (((board[c][r] == 0) && (possibleValues[c][r].length() < min))) {
                    min = possibleValues[c][r].length();
                    cellCoordinates += r + c;
                }
            }
        }
        return cellCoordinates;
    }

    /**
     * Returns the coordinates of the first cell left to fill or empty string if no empty cells left
     * @return  String with row and column coordinates or an empty string if no empty cells found
     */
    private String cellsLeftToFill() {
        String coordinates = "";
        int row = -1;
        int column = -1;
        boolean cellsLeft = false;
        // look for at least one empty cell
        for(int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                // check if the cell is empty
                if(board[i][j] == 0) {
                    row = i;
                    column = j;
                    cellsLeft = true;
                    coordinates += row;
                    coordinates += column;
                    return coordinates;
                }
            }
        }
        return coordinates;
    }

    /**
     * Saves a move onto the steps stack
     * @param stack stack to push the move onto
     * @param row   row number of the cell
     * @param column    column number of the cell
     * @param value     value to save in the cell
     */
    private void saveStep(Stack stack, int row, int column, int value) {
        stack.push(row + column + value);
    }

    private boolean isStepPossible(int row, int column, int value) {
        if(isRowSafe(row, value)
                && isColumnSafe(column, value)
                && isSubGridSafe(row, column, value)) {
            return true;
        }
        else
            return false;
    }

    /**
     * Checks if the value is already somewhere else in the row
     * @param row   row to check
     * @param value value to check
     * @return  true if safe to place the value in the row
     */
    private boolean isRowSafe(int row, int value) {
        // iterate through the row
        for(int c = 0; c < 9; c++) {
            // check if the value is already in the row
            if(board[row][c] == value) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if the value is already somewhere else in the column
     * @param column   column to check
     * @param value value to check
     * @return  true if safe to place the value in the column
     */
    private boolean isColumnSafe(int column, int value) {
        // iterate through the row
        for(int r = 0; r < 9; r++) {
            // check if the value is already in the column
            if(board[r][column] == value) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if the value is already somewhere else in the subgrid
     * @param column   column of the cell to check
     * @param row       row of the cell to check
     * @param value value to check
     * @return  true if safe to place the value in the subgrid
     */
    private boolean isSubGridSafe(int row, int column, int value) {
        // calculate the coordinates of the box
        // sqrt - square root
        int squareOfBoard = (int)Math.sqrt(board.length); // in this case, it's always 3
        int boxStartRow = row - row % squareOfBoard;
        int boxStartColumn = column - column % squareOfBoard;

        // iterate through the box and check if it contains
        for(int r = boxStartRow; r < boxStartRow + squareOfBoard; r++) {
            for (int c = boxStartColumn; c < boxStartColumn + squareOfBoard; c++) {
                if(board[r][c] == value) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Shuffles possible values for a cell
     * @param possibleValues    String containing the values
     * @return  a String with shuffled values
     */
    private String randomizeValues(String possibleValues) {
        String values = possibleValues;
        List<String> splitValues = Arrays.asList(values.split(""));
        Collections.shuffle(splitValues, new Random());
        String shuffledValues = "";
        for (String value : splitValues) {
            shuffledValues += value;
        }
        return shuffledValues;
    }

    /**
     * Generates a given number of empty cell coordinates and erases them from the board
     * @param emptyNumber   number of cells to empty
     * @return  an array with the cells' coordinates
     */
    private String[] determineEmptyCells(int emptyNumber) {
        String[] emptyCells = new String[emptyNumber];
        Random rand = new Random();
        // determine half of the cells in the upper half of the board so that later it can be symmetrical
        for(int i = 0; i < (emptyNumber / 2); i++) {
            boolean isDuplicate = true;
            boolean duplicateFound = false;
            while(isDuplicate) {
                int row = rand.nextInt(0, 4);
                int column = rand.nextInt(0, 8);
                while(row == 4 && column > 4) {
                    row = rand.nextInt(0, 4);
                    column = rand.nextInt(0, 8);
                }

                String rowColumn = String.valueOf(row);
                rowColumn += String.valueOf(column);

                for(String cell : emptyCells) {
                    if(cell != null) {
                        if(cell == rowColumn) {
                            duplicateFound = true;
                        }
                    }
                }

                if(!duplicateFound) {
                    isDuplicate = false;
                    // save the cell
                    emptyCells[i] = String.valueOf(row);
                    emptyCells[i] += String.valueOf(column);
                    board[row][column] = 0;
                    initialBoard[row][column] = 0;
                    // reflect the cell to the bottom half of the board
                    emptyCells[(emptyNumber - 1 - i)] = String.valueOf(8-row) + String.valueOf(8-column);
                    board[8-row][8-column] = 0;
                    initialBoard[8-row][8-column] = 0;
                }
            }
        }
        return emptyCells;
    }

    public boolean isSolved() {
        boolean solved = true;
        for(int i = 0; i < board.length; i++) {
            for (int j = 0; j < board.length; j++) {
                if(board[i][j] == 0) {
                    solved = false;
                }
            }
        }
        if(solved) {
            solved = checkCompleteBoard();
        }
        return solved;
    }

    /**
     * Inserts a value into the cell with coordinates provided
     * @param row   row (1-9)
     * @param column    column (1-9)
     * @param value     value to enter
     * @return  value initially in the cell if successful, otherwise -1
     */
    public int insertValue(int row, int column, int value) {
        // check if the cell is not a given
        if(initialBoard[row - 1][column - 1] != 0) {
            System.out.println("Cannot modify a given cell");
            return -1;
        }
        // else, insert it into the board
        else {
            int initialValue = board[row - 1][column - 1];
            board[row - 1][column - 1] = value;
            return initialValue;
        }
    }

    /**
     * Compares the board against the complete board and determines if they're the same
     * @return  if the boards are the same
     */
    public boolean checkCompleteBoard() {
        for(int i = 0; i < board.length; i++) {
            for(int j = 0; j < board.length; j++) {
                if(board[i][j] != completeBoard[i][j]) {
                    System.out.println("You have some errors in your board.");
                    return false;
                }
            }
        }
        System.out.println("Congratulations! You completed the board.");
        return true;
    }

    public int[][] getInitialBoard() {
        return initialBoard;
    }

    /**
     * Counts how many of each value there are currently in the board
     * @return  int array with the value counts
     */
    public int[] countNumbersInBoard() {
        int oneCount = 0;
        int twoCount = 0;
        int threeCount = 0;
        int fourCount = 0;
        int fiveCount = 0;
        int sixCount = 0;
        int sevenCount = 0;
        int eightCount = 0;
        int nineCount = 0;
        // count the numbers
        for(int i = 0; i < board.length; i++) {
            for(int j = 0; j < board.length; j++) {
                switch(board[i][j]) {
                    case 1:
                        oneCount++;
                        break;
                    case 2:
                        twoCount++;
                        break;
                    case 3:
                        threeCount++;
                        break;
                    case 4:
                        fourCount++;
                        break;
                    case 5:
                        fiveCount++;
                        break;
                    case 6:
                        sixCount++;
                        break;
                    case 7:
                        sevenCount++;
                        break;
                    case 8:
                        eightCount++;
                        break;
                    case 9:
                        nineCount++;
                        break;
                    default:
                        break;
                }
            }
        }
        return new int[]{oneCount, twoCount, threeCount, fourCount, fiveCount, sixCount, sevenCount, eightCount, nineCount};
    }

    /**
     * Returns a list of empty cell coordinates at the point when the method is called
     * @return  String array of coordinates
     */
    public String[] getEmptyCells() {
        // at maximum, the board can have 53 empty cells
        String[] cells = new String[53];
        int cellCounter = 0;
        for(int i = 0; i < board.length; i++) {
            for(int j = 0; j < board.length; j++) {
                if(board[i][j] == 0) {
                    String coordinates = String.valueOf(i) + String.valueOf(j);
                    cells[cellCounter] = coordinates;
                    cellCounter++;
                }
            }
        }
        return cells;
    }

    /**
     * Gets the correct value for the cell specified (coordinates as stored in the array - 0-8)
     * @param row   row of the cell
     * @param column    column of the cell
     * @return  correct value for the cell at the given coordinates
     */
    public int getCorrectValue(int row, int column) {
        return completeBoard[row][column];
    }

    /**
     * Sets the playing board back to the initial board
     */
    public void startOver() {
        board = getInitialBoard();
    }

    public String originalToString() {
        String board = "";
        for(int i = 0; i < rows; i++) {
            for(int j = 0; j < columns; j++) {
                board += completeBoard[i][j];
                board += " ";
            }
        }
        return board;
    }

    public String initialToString() {
        String board = "";
        for(int i = 0; i < rows; i++) {
            for(int j = 0; j < columns; j++) {
                board += initialBoard[i][j];
                board += " ";
            }
        }
        return board;
    }

    public String boardToString() {
        String string = "";
        for(int i = 0; i < rows; i++) {
            for(int j = 0; j < columns; j++) {
                string += board[i][j];
                string += " ";
            }
        }
        return string;
    }
 }
