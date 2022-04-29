package com.napier.sudoku;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Main class with driver code for the game
 * @author 40495101
 * Last modified: 29/04/2022
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

    // game properties
    private static Board board;
    private static Stack<String> moves;
    private static Stack<String> undoneMoves;
    private static Queue<String> movesQueue;
    private static int cluesUsed;
    private static boolean saveUpToDate;
    private static String difficultyLevel;
    private static File save;
    private static File[] saves;

    /**
     * Driver code
     */
    public static void main(String[] args) {
        System.out.println("Hello, sudoku master!");
        Scanner scanner = new Scanner(System.in);
        int actionCode = -1;

        // stay in the game until the user selects 0 to exit
        while (actionCode != 0) {
            actionCode = getUserAction(scanner);
            performUserAction(actionCode, scanner);
        }
        // close the scanner
        scanner.close();
    }

    /**
     * Prompts the user to select their action in main menu
     * @param scanner   Scanner object to read user input
     * @return  user action code selected
     */
    private static int getUserAction(Scanner scanner) {
        int choice;
        // keep asking to make a choice until a valid code is entered
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
            // check if valid code
            if (choice != 0 && choice != 1 && choice != 2 && choice != 3) {
                System.out.println("Please enter a valid option code");
            } else {
                return choice;
            }
        }
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
                int savesNumber = displaySaves();
                int choice;
                choice = selectSave(scanner, savesNumber);
                if(choice == 0) {
                    break;
                }
                System.out.println("Save selected: " + choice);
                loadSavedGame(choice);
                playGame(scanner);
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
     * Prompts the user to select game difficulty
     * @param scanner   Scanner object to read user input
     * @return  game difficulty code selected
     */
    private static int getGameDifficulty(Scanner scanner) {
        int choice;
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
            //check if valid code
            if (choice != 0 && choice != 1 && choice != 2 && choice != 3) {
                System.out.println("Please enter a valid option code");
            }
            else {
                return choice;
            }
        }
    }

    /**
     * Starts a new sudoku game of selected difficulty and initialises board and stacks
     */
    private static void startGame(int gameDifficulty, Scanner scanner) {
        board = new Board();
        moves = new Stack<>();
        undoneMoves = new Stack<>();
        movesQueue = new LinkedList<>();
        cluesUsed = 0;
        saveUpToDate = true;

        switch (gameDifficulty) {
            case EASY:
                board.generateEasyBoard();
                difficultyLevel = "easy";
                createSaveFile();
                playGame(scanner);
                break;
            case MEDIUM:
                board.generateMediumBoard();
                difficultyLevel = "medium";
                createSaveFile();
                playGame(scanner);
                break;
            case HARD:
                board.generateHardBoard();
                difficultyLevel = "hard";
                createSaveFile();
                playGame(scanner);
                break;
        }
    }

    /**
     * Deals with game play
     * @param scanner   Scanner used for user input
     */
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
        while (!board.isSolved()) {
            // get the first letter that the user types
            System.out.println("Choose your next action");
            boolean validChoice = false;
            char choice = '0';
            while (!validChoice) {
                choice = scanner.next().charAt(0);
                validChoice = actOnGameChoice(choice, scanner);
                scanner.nextLine();
            }
            if (choice == 'E' || choice == 'e') {
                // check if the save is up-to-date
                if (!saveUpToDate) {
                    System.out.println("Are you sure you want to exit without saving [Y/N]?");
                    char confirm = scanner.next().charAt(0);
                    boolean exit = false;
                    switch (confirm) {
                        case 'Y':
                        case 'y':
                            exit = true;
                            break;
                        case 'N':
                        case 'n':
                            break;
                        default:
                            System.out.println("Invalid input. Try again");
                            break;
                    }
                    if (exit) {
                        break;
                    }
                } else {
                    break;
                }
            }
        }
    }

    /**
     * Reads in all game saves from the 'saves' directory and displays them to the console to choose from
     * @return  the number of saves read in
     */
    private static int displaySaves() {
        // open folder
        File directory = new File(".\\saves");
        saves = directory.listFiles();
        // check if saves is not empty
        int saveCounter = 1;
        if(saves != null) {
            System.out.println("Game saves: ");
            // display each save
            for(File save : saves) {
                String name = save.getName();
                String noExtension = name.split("\\.")[0];
                String[] split = noExtension.split("_");

                DateTimeFormatter format = DateTimeFormatter.ofPattern("ddMMyyyyHHmm");
                String date = split[0] + split[1];
                LocalDateTime dateTime = LocalDateTime.parse(date, format);
                DateTimeFormatter format2 = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

                String display = saveCounter++ + " - Level: " + split[2] + ", " + "Date: " + dateTime.format(format2);

                System.out.println(display);
            }
            System.out.println("0 - Exit");
        }
        return --saveCounter;
    }

    /**
     * Asks the user to select the code number of the save to load
     * @param scanner   Scanner to read user input
     * @param savesNumber   number of save file in the saves folder
     * @return  code number the user selected
     */
    private static int selectSave(Scanner scanner, int savesNumber) {
        int choice;
        // keep asking to select save until a valid code is input
        while(true) {
            System.out.println("Select the game code you want to load: ");
            try {
                choice = scanner.nextInt();
            }
            catch(Exception ex) {
                System.out.println("Please enter a valid option code");
                scanner.next();
                continue;
            }
            // if 0 selected - break and go back to main menu
            if(choice == 0) {
                return choice;
            }
            else {
                // check if valid code - if so, return it
                for(int i = 1; i <= savesNumber; i++) {
                    if(i == choice) {
                        return choice;
                    }
                }
                // if invalid code, try again
                System.out.println("Invalid code specified. Please try again.");
            }
        }
    }

    /**
     * Loads the information from the save file selected to the program
     * @param saveCode  code number of the save to load
     */
    private static void loadSavedGame(int saveCode) {
        // get the save selected
        File saveSelected = saves[saveCode - 1];
        String[] contents = new String[7];
        // read in the save line by line
        try {
            BufferedReader br = new BufferedReader(new FileReader(saveSelected));
            String line;
            for(int i = 0; i < 7; i++) {
                line = br.readLine();
                contents[i] = line;
            }
        }
        catch (Exception ex) {
            System.out.println("Could not load the game.");
        }

        // initialise attributes for the loading game
        board = new Board();
        moves = new Stack<>();
        undoneMoves = new Stack<>();
        movesQueue = new LinkedList<>();
        cluesUsed = 0;
        saveUpToDate = true;
        save = saveSelected;

        // read in the boards
        board.readInBoard(contents[0], 1);
        board.readInBoard(contents[1], 2);
        board.readInBoard(contents[2], 3);

        // read in the moves
        if(!contents[3].isEmpty()) {
            String[] movesSplit = contents[3].split(" ");
            for(String move : movesSplit) {
                moves.push(move);
            }
        }
        if(!contents[4].isEmpty()) {
            String[] undoneSplit = contents[4].split(" ");
            for(String move : undoneSplit) {
                undoneMoves.push(move);
            }
        }
        if(!contents[5].isEmpty()) {
            String[] queueSplit = contents[5].split(" ");
            movesQueue.addAll(Arrays.asList(queueSplit));
        }
        // read in the number of clues used
        cluesUsed = Integer.parseInt(contents[6]);
    }

    /**
     * Prints the rules from a separate file
     */
    private static void printRules() {
        // read in the rules file and print it out
        try {
            BufferedReader br = new BufferedReader(new FileReader("rules.txt"));
            String line;
            while ((line = br.readLine()) != null) {
                System.out.println(line);
            }
        }
        catch (Exception ex) {
            System.out.println("Could not print the rules");
        }
    }

    /**
     * Perform actions based on user choice during gameplay
     * @param choice    choice code
     * @param scanner   scanner for scanning input
     * @return  if user input correct and action successful
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

    /**
     * Asks the user to enter cell coordinates. Checks them for validity.
     * @param scanner   Scanner to read in the input
     * @return  int[] with row and column coordinates
     */
    private static int[] askForCoordinates(Scanner scanner) {
        // ask for coordinates
        int row = -1;
        int column = -1;
        boolean incorrectCoordinates = true;
        // keep asking until valid coordinates entered
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
            // check if they have been captured
            if(row != -1 && column != -1) {
                // check if in range
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

    /**
     * Asks the user to provide the value to enter to the board
     * @param scanner   Scanner to read in user input
     * @return  int representing the value to enter
     */
    private static int askForValue(Scanner scanner) {
        // ask for a value
        int value = -1;
        boolean incorrectValue = true;
        // keep asking until valid value entered
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
            // check if successfully captured
            if(value != -1) {
                // check if in range
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
            int row = Integer.parseInt(values[0]);
            int column = Integer.parseInt(values[1]);
            int initialValue = Integer.parseInt(values[2]);

            // check if the reverse of the move is possible
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

    /**
     * Manages replaying all the moves from beginning
     * @param scanner   Scanner to read in user input
     */
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
            // keep replaying moves until the user chooses to exit
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
                        System.out.println("Type N to continue or E to exit replay");
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
                // if all moves replayed, break
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
     * Picks a random empty cell from the board and fills it in with the correct value
     */
    private static void fillClue() {
        // get all empty cells
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
        int row = Integer.parseInt(split[0]);
        int column = Integer.parseInt(split[1]);

        // get the right value for the cell picked
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

    /**
     * Counts how many of each value there are already in the board and displays it in the console
     */
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

    /**
     * Resets the game progress to the initial board
     * @param scanner   Scanner for reading in user input
     */
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
        // if confirmed, start over
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

    /**
     * Saves a game to a file so that it can later be read
     */
    private static void saveGame() {
        // check if the latest progress has been saved
        if(saveUpToDate) {
            System.out.println("Progress already saved.");
        }
        // if not, save it
        else {
            try {
                if(!save.exists()){
                    save.createNewFile();
                }
                // check if the file contains some saved data already - if so, overwrite it with the first line and then keep appending
                BufferedWriter writer;
                if(save.length() != 0) {
                    writer = new BufferedWriter(new FileWriter(save));
                    writer.write(board.originalToString());
                    writer.close();
                    writer = new BufferedWriter(new FileWriter(save, true));
                    writer.newLine();
                }
                else {
                    writer = new BufferedWriter(new FileWriter(save, true));
                    writer.write(board.originalToString());
                    writer.newLine();
                }
                // get the data
                ArrayList<String> output = new ArrayList<>();
                output.add(board.initialToString());
                output.add(board.boardToString());
                output.add(movesToString());
                output.add(undoneMovesToString());
                output.add(movesQueueToString());
                output.add(String.valueOf(cluesUsed));

                // write to the file
                for(String line : output) {
                    writer.write(line);
                    writer.newLine();
                }
                writer.close();
                System.out.println("Game saved successfully");
                saveUpToDate = true;
            }
            catch (Exception ex) {
                System.out.println("Could not save the game");
            }
        }
    }

    /**
     * Creates a saves directory in the file system and a save file for the game instance
     */
    private static void createSaveFile() {
        try {
            // if saves directory doesn't exist, create it
            File directory = new File(".\\saves");
            if (!directory.exists()){
                directory.mkdirs();
            }
            DateTimeFormatter formatDate = DateTimeFormatter.ofPattern("ddMMyyyy_HHmm");
            String formattedDate = LocalDateTime.now().format(formatDate);
            String filename = formattedDate + "_" + difficultyLevel + ".txt";

            save = new File(".\\saves\\" + filename);
        }
        catch (Exception ex) {
            System.out.println("There was an error creating a save file");
        }
    }

    /**
     * Converts the moves stack to a string. Helper for saveGame().
     * @return  String with moves delimited by spaces
     */
    private static String movesToString() {
        String string = "";
        Iterator<String> move = moves.iterator();

        while (move.hasNext()) {
            string += move.next();
            string += " ";
        }
        return string;
    }

    /**
     * Converts the undone moves stack to a string. Helper for saveGame().
     * @return  String with undone moves delimited by spaces
     */
    private static String undoneMovesToString() {
        String string = "";
        Iterator<String> move = undoneMoves.iterator();

        while (move.hasNext()) {
            string += move.next();
            string += " ";
        }
        return string;
    }

    /**
     * Converts the moves queue to a string. Helper for saveGame().
     * @return  String with moves delimited by spaces
     */
    private static String movesQueueToString() {
        String string = "";
        Iterator<String> move = movesQueue.iterator();

        while (move.hasNext()) {
            string += move.next();
            string += " ";
        }
        return string;
    }

    /**
     * Reads in the help file and prints it to the console
     */
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
        printCommands();
    }

    /**
     * Prints the menu of game options
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

    /**
     * Prints the playing board to the console. Copy of the Board.printBoard() function to print intermediate boards
     * when replaying all the moves.
     * @param board int[][] to print
     */
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

    /**
     * Helper function used in replaying all moves
     * @param board int[][] - board where the moves will be reflected
     * @param move  move to play out
     */
    private static void makeMove(int[][] board, String move) {
        String[] values = move.split("");
        int row = Integer.parseInt(values[0]);
        int column = Integer.parseInt(values[1]);
        int newValue = Integer.parseInt(values[3]);

        board[row - 1][column - 1] = newValue;
        printBoard(board);
    }
}
