package com.napier.sudoku;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Scanner;

public class GameLogic {
    // user action codes
    private static final int EXIT = 0;
    private static final int NEW_GAME = 1;
    private static final int LOAD_GAME = 2;
    private static final int RULES = 3;
    private static final int EASY = 1;
    private static final int MEDIUM = 2;
    private static final int HARD = 3;

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
                    startGame(gameDifficulty);
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
    private static void startGame(int gameDifficulty) {
        Board board = new Board();
        switch(gameDifficulty) {
            case 1:
                board.generateEasyBoard();
                break;
            case 2:
                board.generateMediumBoard();
                break;
            case 3:
                board.generateHardBoard();
                break;
        }
        board.printBoard();

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
}
