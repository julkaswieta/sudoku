package com.napier.sudoku;

import java.util.Scanner;

public class GameLogic {
    private static final int EXIT = 0;
    private static final int NEW_GAME = 1;
    private static final int LOAD_GAME = 2;
    private static final int RULES = 3;
    private static final int EASY = 1;
    private static final int MEDIUM = 2;
    private static final int HARD = 3;

    public static void main(String[] args) {
        int actionCode = getUserAction();

        switch (actionCode) {
            case EXIT:
                System.out.println("Thank you for playing. Goodbye!");
                break;
            case NEW_GAME:
                // start a new game according to level of difficulty
                // int gameDifficulty = getGameDifficulty();
                break;
            default:
                System.out.println("Please enter a valid option code");
                break;
        }

    }

    private static int getUserAction() {
        System.out.println("Hello, sudoku master!");
        int choice = -1;
        Scanner scanner = new Scanner(System.in);
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
                scanner.close();
                return choice;
            }
        }

    }

    private static int getGameDifficulty() {
        int choice = -1;
        Scanner scanner = new Scanner(System.in);
        while(choice != EXIT) {
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
            }
        }
        return choice;
    }
}
