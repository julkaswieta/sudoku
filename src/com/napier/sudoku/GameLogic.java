package com.napier.sudoku;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Main class with driver code for the game
 */
public class GameLogic {
    // user action codes
    private static final int EXIT = 0;
    private static final int NEW_GAME = 1;
    private static final int LOAD_GAME = 2;
    private static final int RULES = 3;
    private static final int EASY = 1;
    private static final int MEDIUM = 2;
    private static final int HARD = 3;

    private static Board board;
    private static Stack<String> moves;
    private static Stack<String> undoneMoves;
    private static Queue<String> movesQueue;
    private static int cluesUsed;
    private static boolean saveUpToDate;
    private static String difficultyLevel;

    /**
     * Driver code
     */
    public static void main(String[] args) {
        System.out.println("Hello, sudoku master!");
        Scanner scanner = new Scanner(System.in);
        int actionCode = -1;

        while(actionCode != 0) {
            actionCode = getUserAction(scanner);
            performUserAction(actionCode, scanner);
        }

        scanner.close();
    }

    /**
     * Depending on the actionCode specified, executes a selected user action
     * @param actionCode    code of a user action selected
     * @param scanner       Scanner object to read user input
     */
    private static void performUserAction(int actionCode, Scanner scanner) {
        switch (actionCode) {
            case EXIT:
                System.out.println("Thank you for playing. Goodbye!");
                break;
            case NEW_GAME:
                // start a new game according to level of difficulty
                int gameDifficulty = getGameDifficulty(scanner);
                if(gameDifficulty != 0) {
                    System.out.println("Starting a new game");
                    startGame(gameDifficulty, scanner);
                }
                break;
            case LOAD_GAME:
                System.out.println("Loading a saved game");
                break;
            case RULES:
                printRules();
                break;
            default:
                System.out.println("An unexpected error occurred, please try again.");
                break;
        }
    }

    /**
     * Prompts the user to select their action in main menu
     * @param scanner   Scanner object to read user input
     * @return  user action code selected
     */
    private static int getUserAction(Scanner scanner) {
        int choice = -1;
        // keep asking for actions until the user chooses to exit
        while (true) {
            System.out.println("MAIN MENU");
            System.out.println("Choose your action:");
            System.out.println("1 - Start a new game");
            System.out.println("2 - Load a previous game");
            System.out.println("3 - See rules and information");
            System.out.println("0 - Exit");

            try {
                choice = scanner.nextInt();
            } catch (Exception ex) {
                System.out.println("Please enter a valid option code");
                scanner.next();
                continue;
            }
            if (choice != 0 && choice != 1 && choice != 2 && choice != 3) {
                System.out.println("Please enter a valid option code");
                continue;
            }
            else {
                return choice;
            }
        }
    }

    /**
     * Prompts the user to select game difficulty
     * @param scanner   Scanner object to read user input
     * @return  game difficulty code selected
     */
    private static int getGameDifficulty(Scanner scanner) {
        int choice = -1;
        while(true) {
            System.out.println("Choose level difficulty:");
            System.out.println("1 - Easy");
            System.out.println("2 - Medium");
            System.out.println("3 - Hard");
            System.out.println("0 - Exit");

            try {
                choice = scanner.nextInt();
            }
            catch (Exception ex) {
                System.out.println("Please enter a valid option code");
                scanner.next();
                continue;
            }

            if (choice != 0 && choice != 1 && choice != 2 && choice != 3) {
                System.out.println("Please enter a valid option code");
                continue;
            }
            else {
                return choice;
            }
        }
    }

    /**
     * Starts a new sudoku game and initialises board and stacks
     */
    private static void startGame(int gameDifficulty, Scanner scanner) {
        board = new Board();
        moves = new Stack<>();
        undoneMoves = new Stack<>();
        movesQueue = new LinkedList<>();
        cluesUsed = 0;
        saveUpToDate = false;

        switch(gameDifficulty) {
            case 1:
                board.generateEasyBoard();
                difficultyLevel = "easy";
                playGame(scanner);
                break;
            case 2:
                board.generateMediumBoard();
                difficultyLevel = "medium";
                playGame(scanner);
                break;
            case 3:
                board.generateHardBoard();
                difficultyLevel = "hard";
                playGame(scanner);
                break;
        }
    }

    /**
     * Prints the rules from a separate file
     * @return  true if the printing was successful
     */
    private static boolean printRules() {
        try {
            BufferedReader br = new BufferedReader(new FileReader("rules.txt"));
            String line;
            while ((line = br.readLine()) != null) {
                System.out.println(line);
            }
            return true;
        }
        catch (Exception ex) {
            System.out.println("Could not print the rules");
            return false;
        }
    }

    private static void playGame(Scanner scanner) {
        // ask if ready to play
        System.out.println("Press Enter to start the game");
        // once key is pressed, the timer will start
        try {
            System.in.read();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        board.printBoard();
        printCommands();

        System.out.println("Format of the coordinates: \"row column\"");

        // keep asking the user to do something until there are no empty cells in the board
        while(!board.isSolved()) {
            // get the first letter that the user types
            System.out.println("Choose your next action");
            boolean validChoice = false;
            char choice = '0';
            while (!validChoice) {
                choice = scanner.next().charAt(0);
                validChoice = actOnGameChoice(choice, scanner);
                scanner.nextLine();
            }
            if(choice == 'E' || choice == 'e') {
                System.out.println("Are you sure you want to exit without saving [Y/N]?");
                char confirm = scanner.next().charAt(0);
                boolean exit = false;
                switch(confirm) {
                    case 'Y':
                    case 'y':
                        exit = true;
                        break;
                    case 'N':
                    case 'n':
                        exit = false;
                        break;
                    default:
                        System.out.println("Invalid input. Try again");
                        break;
                }
                if(exit) {
                    break;
                }
            }
        }
    }

    /**
     * Perform actions based on user choice during gameplay
     * @param choice    choice code
     * @param scanner   scanner for scanning input
     * @return  if successful
     */
    private static boolean actOnGameChoice(char choice, Scanner scanner) {
        switch(choice) {
            case 'E', 'e':
                // exit to main menu handled in the calling method
                // here only to check if input is correct
                return true;
            case 'V', 'v':
                // get the coordinates and value to modify
                int[] rowColumn= askForCoordinates(scanner);
                int row = rowColumn[0];
                int column = rowColumn[1];
                int value = askForValue(scanner);

                // insert the value into the board
                int initialValue = board.insertValue(row, column, value);
                if(initialValue != -1) {
                    // push the move onto the moves stack and store in the moves queue
                    board.printBoard();
                    printCommands();
                    moves.push(String.valueOf(row) + String.valueOf(column) + String.valueOf(initialValue) + String.valueOf(value));
                    movesQueue.add(String.valueOf(row) + String.valueOf(column) + String.valueOf(initialValue) + String.valueOf(value));
                    saveUpToDate = false;
                }
                return true;
            case 'U', 'u':
                // undo a move
                undoMove();
                return true;
            case 'R', 'r':
                // redo a move
                redoMove();
                return true;
            case 'M', 'm':
                // replay from beginning
                replayAllMoves(scanner);
                return true;
            case 'C', 'c':
                // fill one random cell
                // check if all clues have not been used
                if (cluesUsed >= 3) {
                    System.out.println("All clues have been used.");
                }
                else {
                    fillClue();
                }
                return true;
            case 'D', 'd':
                // print how many of which number is already in the board
                displayNumbersInBoard();
                return true;
            case 'O', 'o':
                // start over from the initial board
                startOver(scanner);
                return true;
            case 'S', 's':
                // save to disk
                saveGame();
                return true;
            case 'H', 'h':
                // print help
                printHelp();
                return true;
            default:
                System.out.println("Invalid value specified. Please try again.");
                return false;
        }
    }

    private static void saveGame() {
        // check if the latest progress has been saved
        if(saveUpToDate) {
            System.out.println("Progress already saved.");
        }
        else {
            System.out.println("Saving the game...");
            DateTimeFormatter formatDate = DateTimeFormatter.ofPattern("ddMMyyyy_HHmm");
            String formattedDate = LocalDateTime.now().format(formatDate);
            String filename = formattedDate + "_" + difficultyLevel + ".txt";
            try {
                File directory = new File(".\\saves");
                if (!directory.exists()){
                    directory.mkdirs();
                }
                File file = new File(".\\saves\\" + filename);

                if(!file.exists()){
                    file.createNewFile();
                }

                BufferedWriter writer = new BufferedWriter(new FileWriter(file, true));
                // each item will be saved on one line, with elements delimited by spaces
                // save the original board
                writer.write(board.originalToString());
                writer.newLine();
                // save the initial board
                writer.write(board.initialToString());
                writer.newLine();
                // save the playing board
                writer.write(board.boardToString());
                writer.newLine();
                // save moves
                writer.write(movesToString());
                writer.newLine();
                // save undone moves
                writer.write(undoneMovesToString());
                writer.newLine();
                // save moves queue
                writer.write(movesQueueToString());
                writer.newLine();
                writer.write(cluesUsed);
                writer.newLine();
                writer.close();
                System.out.println("Game saved successfully");
            }
            catch (Exception ex) {
                System.out.println("Could not save the game");
            }
        }
    }

    private static String movesToString() {
        String string = "";
        Iterator move = moves.iterator();

        while (move.hasNext()) {
            string += move.next();
            string += " ";
        }
        return string;
    }

    private static String undoneMovesToString() {
        String string = "";
        Iterator move = undoneMoves.iterator();

        while (move.hasNext()) {
            string += move.next();
            string += " ";
        }
        return string;
    }

    private static String movesQueueToString() {
        String string = "";
        Iterator move = movesQueue.iterator();

        while (move.hasNext()) {
            string += move.next();
            string += " ";
        }
        return string;
    }

    private static void printHelp() {
        try {
            BufferedReader br = new BufferedReader(new FileReader("help.txt"));
            String line;
            while ((line = br.readLine()) != null) {
                System.out.println(line);
            }
        }
        catch (Exception ex) {
            System.out.println("Could not print help instructions");
        }
        board.printBoard();
        printCommands();
    }

    private static void startOver(Scanner scanner) {
        // ask to confirm
        System.out.println("Are you sure you want to start from the beginning [Y/N]?");
        boolean correctInput = false;
        boolean startOver = true;
        while(!correctInput) {
            char confirm = scanner.next().charAt(0);
            switch(confirm) {
                case 'Y', 'y':
                    correctInput = true;
                    break;
                case 'N', 'n':
                    startOver = false;
                    correctInput = true;
                    break;
                default:
                    System.out.println("Invalid input. Try again");
                    break;
            }
        }
        if(startOver) {
            board.startOver();
            moves = new Stack<>();
            undoneMoves = new Stack<>();
            movesQueue = new LinkedList<>();
            cluesUsed = 0;
            saveUpToDate = false;
            System.out.println("Starting over...");
            board.printBoard();
            printCommands();
        }
    }

    private static void fillClue() {
        String[] emptyCells = board.getEmptyCells();
        // count non-zero coordinates
        int cellCounter = 0;
        for(String cell : emptyCells) {
            if(cell != null && !cell.isEmpty()) {
                cellCounter++;
            }
        }
        // pick a random cell
        Random rand = new Random();
        int cell = rand.nextInt(1, cellCounter);
        String coordinates = emptyCells[cell - 1];
        String[] split = coordinates.split("");
        int row = Integer.valueOf(split[0]);
        int column = Integer.valueOf(split[1]);
        // get the right value
        int value = board.getCorrectValue(row, column);
        board.insertValue(row + 1, column + 1, value);
        cluesUsed++;
        String move = String.valueOf(row + 1) + String.valueOf(column + 1) + String.valueOf(0) + String.valueOf(value);
        movesQueue.add(move);
        saveUpToDate = false;
        board.printBoard();
        printCommands();
        System.out.println("Clue filled at " + (row + 1) + ", " + (column + 1));
    }

    private static void displayNumbersInBoard() {
        int[] numbers = board.countNumbersInBoard();
        System.out.println("Values currently in the board: ");
        int total = 0;
        for(int i = 0; i < numbers.length; i++) {
            System.out.println((i + 1) + " - " + numbers[i] + "/9");
            total += numbers[i];
        }
        System.out.println("Total: " + total + "/81");
        printCommands();
    }


    private static int[] askForCoordinates(Scanner scanner) {
        // ask for coordinates
        int row = -1;
        int column = -1;
        boolean incorrectCoordinates = true;
        while(incorrectCoordinates) {
            try {
                System.out.print("Enter coordinates: ");
                row = scanner.nextInt();
                column = scanner.nextInt();
            }
            catch (Exception ex) {
                System.out.println("Invalid coordinates provided. Please try again.");
                scanner.nextLine();
                continue;
            }
            if(row != -1 && column != -1) {
                if(row < 1 || row > 9 || column < 1 || column > 9) {
                    System.out.println("Coordinates must be in range 1-9");
                }
                else {
                    incorrectCoordinates = false;
                }
            }
            else {
                System.out.println("Coordinates must be in range 1-9");
            }
        }
        return new int[]{row, column};
    }

    private static int askForValue(Scanner scanner) {
        // ask for a value
        int value = -1;
        boolean incorrectValue = true;
        while (incorrectValue) {
            try {
                System.out.print("Enter value: ");
                value = scanner.nextInt();
            }
            catch (Exception ex) {
                System.out.println("Invalid value provided. Please try again.");
                scanner.nextLine();
                continue;
            }
            if(value != -1) {
                if (value < 1 || value > 9) {
                    System.out.println("Value must be in range 1-9");
                }
                else{
                    incorrectValue = false;
                }
            }
            else {
                System.out.println("Value must be in range 1-9");
            }
        }
        return value;
    }

    /**
     * Undoes the last move the player made if any moves made
     */
    private static void undoMove() {
        // check if there were any moves made
        if(moves.empty()) {
            System.out.println("No moves to undo");
        }
        else {
            String lastMove = moves.pop();
            String[] values = lastMove.split("");
            int row = Integer.valueOf(values[0]);
            int column = Integer.valueOf(values[1]);
            int initialValue = Integer.valueOf(values[2]);

            if(board.insertValue(row, column, initialValue) != -1) {
                System.out.println("Move undone");
                // push the undone move to the undone moves stack
                // new value becomes initial value and vice versa
                lastMove = values[0] + values[1] + values[3] + values[2];
                undoneMoves.push(lastMove);
                // store in the moves queue
                movesQueue.add(lastMove);
                saveUpToDate = false;
                board.printBoard();
                printCommands();
            }
        }
    }

    /**
     * Redoes the last undone move if possible
     */
    private static void redoMove() {
        // check if there are any moves to redo
        if(undoneMoves.empty()) {
            System.out.println("No moves to redo");
        }
        else {
            String lastMove = undoneMoves.pop();
            String[] values = lastMove.split("");
            int row = Integer.valueOf(values[0]);
            int column = Integer.valueOf(values[1]);
            int value = Integer.valueOf(values[2]);

            if(board.insertValue(row, column, value) != -1) {
                System.out.println("Move redone");
                // push the undone move to the undone moves stack
                lastMove = values[0] + values[1] + values[3] + values[2];
                moves.push(lastMove);
                // store in the moves queue
                movesQueue.add(lastMove);
                saveUpToDate = false;
                board.printBoard();
                printCommands();
            }
        }
    }

    private static void replayAllMoves(Scanner scanner) {
        // check if there are any moves to replay at all
        if(!movesQueue.isEmpty()) {
            // get a copy of the initial board
            int[][] initialBoard = board.getInitialBoard();
            System.out.println("Initial board:");
            printBoard(initialBoard);   // print the initial board
            System.out.println("Press Enter to start replay.");
            // once key is pressed, the timer will start
            try {
                System.in.read();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            boolean exit = false;
            while(!exit) {
                int moveCounter = 1;
                for(String move : movesQueue){
                    String[] split = move.split("");
                    System.out.println("Move " + moveCounter++ + ": (" + split[0] + ", " + split[1] + ") " + split[3]);
                    makeMove(initialBoard, move);
                    // ask to continue
                    boolean correctInput = false;
                    while(!correctInput) {
                        System.out.println("Press N to continue or E to exit replay");
                        char choice = scanner.next().charAt(0);
                        switch(choice) {
                            case 'N':
                            case 'n':
                                correctInput = true;
                                break;
                            case 'E':
                            case 'e':
                                correctInput = true;
                                exit = true;
                                break;
                            default:
                                break;
                        }
                    }
                }
                if(moveCounter > movesQueue.size()) {
                    break;
                }
            }
            System.out.println("Replay finished");
        }
        else {
            System.out.println("No moves to replay");
        }
        printCommands();
    }

    /**
     * Prints the playing board and a menu of options
     */
    private static void printCommands() {
        String divider = "-----------------------------------------------------------------------";
        System.out.println(divider);
        try {
            BufferedReader br = new BufferedReader(new FileReader("commands.txt"));
            String line;
            while ((line = br.readLine()) != null) {
                System.out.println(line);
            }
        }
        catch (Exception ex) {
            System.out.println("Could not print commands");
        }
        System.out.println(divider);
        if(cluesUsed < 3) {
            System.out.println("Clues available: " + (3 - cluesUsed) + "/3");
        }
        else {
            System.out.println("No clues available");
        }
    }

    private static void printBoard(int[][] board) {
        int rowsCounter = 1;
        // print columns numbering
        String columnNumbering = "       1 2 3   4 5 6   7 8 9 \n\n";
        System.out.print(columnNumbering);

        // print the grid
        for (int i = 0; i < board.length; i++) {
            if(i % 3 == 0 && i != 0) {
                System.out.print("\n");
            }
            System.out.print(rowsCounter++ + "    ");  // print row number

            for (int j = 0; j < board.length; j++) {
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

    private static void makeMove(int[][] board, String move) {
        String[] values = move.split("");
        int row = Integer.valueOf(values[0]);
        int column = Integer.valueOf(values[1]);
        int newValue = Integer.valueOf(values[3]);

        board[row - 1][column - 1] = newValue;
        printBoard(board);
    }
}
