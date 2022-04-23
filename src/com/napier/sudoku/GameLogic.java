package com.napier.sudoku;

import javax.rmi.ssl.SslRMIClientSocketFactory;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Scanner;

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
     * Starts a new sudoku game
     */
    private static void startGame(int gameDifficulty, Scanner scanner) {
        Board board = new Board();
        switch(gameDifficulty) {
            case 1:
                board.generateEasyBoard();
                playGame(board, scanner);
                break;
            case 2:
                board.generateMediumBoard();
                playGame(board, scanner);
                break;
            case 3:
                board.generateHardBoard();
                playGame(board, scanner);
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

    private static void playGame(Board board, Scanner scanner) {
        // ask if ready to play
        System.out.println("Press Enter to start the game");
        // once key is pressed, the timer will start
        try {
            System.in.read();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        printCommandsAndBoard(board);

        System.out.println("Format of the coordinates: \"row column\"");

        // keep asking the user to do something until there are no empty cells in the board
        while(!board.isSolved()) {
            // get the first letter that the user types
            System.out.println("Choose your next action");
            boolean validChoice = false;
            char choice = '0';
            while (!validChoice) {
                choice = scanner.next().charAt(0);
                validChoice = actOnGameChoice(choice, scanner, board);
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
     * @param board     board to operate on
     * @return  if successful
     */
    private static boolean actOnGameChoice(char choice, Scanner scanner, Board board) {
        switch(choice) {
            case 'E':
            case 'e':
                System.out.println("Chosen: E");
                // exit to main menu
                return true;
            case 'V':
            case 'v':
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
                // insert the coordinates into the board
                if(board.insertValue(row, column, value)) {
                    printCommandsAndBoard(board);
                }
                return true;
            case 'U':
            case 'u':
                System.out.println("Chosen: U");
                // undo a move
                return true;
            case 'R':
            case 'r':
                System.out.println("Chosen: R");
                // redo a move
                return true;
            case 'P':
            case 'p':
                System.out.println("Chosen: P");
                // replay from beginning
                return true;
            case 'C':
            case 'c':
                System.out.println("Chosen: C");
                // fill one random cell
                return true;
            case 'D':
            case 'd':
                System.out.println("Chosen: D");
                // print how many of which number is left to be filled
                return true;
            case 'S':
            case 's':
                System.out.println("Chosen: S");
                // save to disk
                return true;
            case 'H':
            case 'h':
                System.out.println("Chosen: H");
                // print help
                return true;
            default:
                System.out.println("Invalid value specified. Please try again.");
                return false;
        }
    }

    private static void printCommandsAndBoard(Board board) {
        // print the playing board and commands
        board.printOriginal();
        board.printBoard();
        String commands = "U - undo a move    R - redo a move    P - replay moves from beginning\nC - clue           D - digits statistics\nS - save to disk   H - help    E - exit\nV - enter value\n";
        String line = "-----------------------------------------------------------------------";
        System.out.println(line + "\n" + commands + line);
    }
}
