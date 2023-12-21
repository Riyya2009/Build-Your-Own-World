package byow.Core;

import byow.InputDemo.InputSource;
import byow.InputDemo.KeyboardInputSource;
import byow.InputDemo.StringInputDevice;

import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;
import edu.princeton.cs.algs4.StdDraw;


import java.awt.*;
import java.util.ArrayList;
import java.util.Random;
import java.util.List;

import static org.junit.Assert.*;

public class Engine {
    /* Feel free to change the width and height. */
    private static final int TILE_SIZE = 16;
    private static final int WIDTH = 80;
    private static final int HEIGHT = 40;
    private static final int CENTER_WIDTH = WIDTH / 2;
    private static final int CENTER_HEIGHT = (HEIGHT + 8) / 2;
    private long seed;
    private TETile avatar;
    private InputSource inputSource;
    private Game game;
    private static final int OPTION_SIZE = 25;
    private static final int ONE_SECOND = 1000;
    private static final int MAX_COLOR_VAL = 255;
    private static final int SPACING = 4;

    /**
     * Method used for exploring a fresh world. This method should handle all inputs,
     * including inputs from the main menu.
     */
    public void interactWithKeyboard() {
        inputSource = new KeyboardInputSource();
        runGame();
    }

    /**
     * Method used for autograding and testing your code. The input string will be a series
     * of characters (for example, "n123sswwdasdassadwas", "n123sss:q", "lwww". The engine should
     * behave exactly as if the user typed these characters into the engine using
     * interactWithKeyboard.
     * <p>
     * Recall that strings ending in ":q" should cause the game to quite save. For example,
     * if we do interactWithInputString("n123sss:q"), we expect the game to run the first
     * 7 commands (n123sss) and then quit and save. If we then do
     * interactWithInputString("l"), we should be back in the exact same state.
     * <p>
     * In other words, running both of these:
     * - interactWithInputString("n123sss:q")
     * - interactWithInputString("lww")
     * <p>
     * should yield the exact same world state as:
     * - interactWithInputString("n123sssww")
     *
     * @param input the input string to feed to your program
     * @return the 2D TETile[][] representing the state of the world
     */
    public TETile[][] interactWithInputString(String input) {
        inputSource = new StringInputDevice(input);
        getSeedOrLoad();
        game.simulateGame();
        TETile[][] world = game.getWorldTiles();
        return world;
    }

    private void getSeedOrLoad() {
        while (inputSource.possibleNextInput()) {
            char menuChoice = inputSource.getNextKey();
            if (menuChoice == 'L') {
                game = new Game(inputSource);
                return;
            }
            if (menuChoice == 'N') {
                String seedSoFar = "";
                while (inputSource.possibleNextInput()) {
                    char key = inputSource.getNextKey();
                    if (key == 'S') {
                        seed = Long.parseLong(seedSoFar);
                        RandomWorldGenerator randomWorld = new RandomWorldGenerator(seed);
                        game = new Game(randomWorld, inputSource);
                        return;
                    }
                    seedSoFar += key;
                }
            }
        }
    }

    public void runGame() {
        showMainMenu();

        while (true) {
            if (inputSource.possibleNextInput()) {
                char key = inputSource.getNextKey();

                if (key == 'N') {
                    if (setSeed()) {
                        if (avatar == null) {
                            game = new Game(seed, inputSource);
                        } else {
                            game = new Game(seed, avatar, inputSource);
                        }
                    } else {
                        Random randomSeedGen = new Random();
                        if (avatar == null) {
                            game = new Game(randomSeedGen.nextLong(), inputSource);
                        } else {
                            game = new Game(randomSeedGen.nextLong(), avatar, inputSource);
                        }
                    }
                    game.startGame();
                    if (game.isGameOver()) {
                        gameOverScreen();
                        showMainMenu();
                    } else {
                        break;
                    }
                }

                if (key == 'L') {
                    game = new Game(inputSource);
                    game.startGame();
                    if (game.isGameOver()) {
                        gameOverScreen();
                        showMainMenu();
                    } else {
                        break;
                    }
                }

                if (key == 'C') {
                    chooseAvatarSelectionMethod();
                    showMainMenu();
                }

                if (key == 'Q') {
                    return;
                }
            }

        }

    }



    private void gameOverScreen() {
        if (inputSource instanceof StringInputDevice) {
            return;
        }
        Font titleFont = new Font("Monaco", Font.BOLD, OPTION_SIZE * 2);
        StdDraw.setFont(titleFont);
        StdDraw.setPenColor(StdDraw.WHITE);
        StdDraw.clear(StdDraw.BLACK);
        StdDraw.text(CENTER_WIDTH, CENTER_HEIGHT, "Game Over!!");
        StdDraw.show();
        StdDraw.pause(ONE_SECOND * 3);
    }

    private void chooseAvatarSelectionMethod() {
        subtext("Enter 0 to choose a pre-rendered avatar, 1 to create an avatar");
        while (true) {
            if (inputSource.possibleNextInput()) {
                char key = inputSource.getNextKey();
                if (Character.isDigit(key)) {
                    int choice = Integer.parseInt("" + key);
                    if (choice >= 0 && choice <= 1) {
                        switch (choice) {
                            case 0:
                                changeAvatar();
                                break;
                            case 1:
                                avatarCreator();
                                break;
                            default:
                                break;
                        }
                    }
                    break;
                }
            }
        }
    }

    private void showMainMenu() {
        if (inputSource instanceof StringInputDevice) {
            return;
        }
        initializeScreen();
        showTitle();
        showOptions();
    }

    private void initializeScreen() {
        StdDraw.setCanvasSize(WIDTH * TILE_SIZE, HEIGHT * TILE_SIZE);
        StdDraw.setXscale(0, WIDTH);
        StdDraw.setYscale(0, HEIGHT);
        StdDraw.clear(StdDraw.BLACK);
        StdDraw.enableDoubleBuffering();
        StdDraw.show();
    }

    private void showTitle() {
        Font titleFont = new Font("Monaco", Font.BOLD, OPTION_SIZE * 2);
        StdDraw.setFont(titleFont);
        StdDraw.setPenColor(StdDraw.WHITE);
        StdDraw.text(CENTER_WIDTH, CENTER_HEIGHT, "CS61B: THE GAME");
        StdDraw.show();
    }

    private void showOptions() {
        Font optionFont = new Font("Monaco", Font.PLAIN, OPTION_SIZE);
        StdDraw.setFont(optionFont);
        StdDraw.text(CENTER_WIDTH, CENTER_HEIGHT - SPACING, "(N) New Game");
        StdDraw.text(CENTER_WIDTH, CENTER_HEIGHT - SPACING * 2, "(L) Load Game");
        StdDraw.text(CENTER_WIDTH, CENTER_HEIGHT - SPACING * 3, "(C) Change Avatar"); // Add this line
        StdDraw.text(CENTER_WIDTH, CENTER_HEIGHT - SPACING * 4, "(Q) Quit Game");
        StdDraw.show();
    }

    private boolean setSeed() {
        String subtext = "Please enter a seed";
        subtext(subtext);
        String seedSoFar = "";
        while (true) {
            if (inputSource.possibleNextInput()) {
                char key = inputSource.getNextKey();
                if (key == 'S') {
                    break;
                }
                if (Character.isDigit(key)) {
                    seedSoFar += key;
                }
                displayInput(subtext, seedSoFar);
            }
        }
        if (seedSoFar.length() == 0) {
            return false;
        }
        Long finalSeed = Long.parseLong(seedSoFar);
        seed = finalSeed;
        return true;
    }

    private void subtext(String text) {
        if (inputSource instanceof StringInputDevice) {
            return;
        }
        StdDraw.clear(StdDraw.BLACK);
        showTitle();
        Font optionFont = new Font("Monaco", Font.PLAIN, OPTION_SIZE);
        StdDraw.setFont(optionFont);
        StdDraw.text(CENTER_WIDTH, CENTER_HEIGHT - 4, text);
        StdDraw.show();
    }

    private void displayInput(String subtext, String input) {
        if (inputSource instanceof StringInputDevice) {
            return;
        }
        subtext(subtext);
        Font optionFont = new Font("Monaco", Font.PLAIN, OPTION_SIZE);
        StdDraw.setFont(optionFont);
        StdDraw.text(CENTER_WIDTH, CENTER_HEIGHT - 8, input);
        StdDraw.show();
    }

    private void avatarCreator() {
        char character = askForCharacter();
        Color textColor = solicitColor("For character color: ");
        Color backgroundColor = solicitColor("For background color: ");
        confirmSelection(character, textColor, backgroundColor);
    }

    private char askForCharacter() {
        String subtext = "Please enter a character to represent your avatar.";
        subtext(subtext);
        while (true) {
            if (inputSource.possibleNextInput()) {
                char key = inputSource.getNextKey();
                displayInput(subtext, "" + key);
                StdDraw.pause(ONE_SECOND * 2);
                return key;
            }
        }
    }

    private Color solicitColor(String colorBeingSet) {
        List<String> colors = List.of("red", "green", "blue");
        List<Integer> colorValues = new ArrayList<>();
        String invalidSubtext = "Invalid value. Try again. Enter a value between 0-255";
        for (int i = 0; i < 3; i++) {
            String subtext = colorBeingSet + "Please enter a " + colors.get(i)
                    + " value (numbers 0 to 255). Press 'C' to confirm.";
            subtext(subtext);
            int currColor = solicitNumInputAndEndByPressing('C', subtext);
            if (currColor < 0 || currColor > MAX_COLOR_VAL) {
                while (currColor < 0 || currColor > MAX_COLOR_VAL) {
                    currColor = solicitNumInputAndEndByPressing('C', invalidSubtext);
                }
            }
            colorValues.add(currColor);
        }
        return new Color(colorValues.get(0), colorValues.get(1), colorValues.get(2));
    }

    private int solicitNumInputAndEndByPressing(char c, String subtext) {
        String input = "";
        while (true) {
            if (inputSource.possibleNextInput()) {
                char key = inputSource.getNextKey();
                if (key == c) {
                    if (!input.equals("")) {
                        break;
                    }
                }
                if (Character.isDigit(key)) {
                    input += key;
                }
                displayInput(subtext, input);
            }
        }
        return Integer.parseInt(input);
    }

    private void confirmSelection(char character, Color textColor, Color backgroundColor) {
        TETile createdAvatar = new TETile(character, textColor, backgroundColor, "you");
        subtext("Your avatar has been created. Press Y to confirm your selection or N to delete it.");
        createdAvatar.draw(CENTER_WIDTH, CENTER_HEIGHT - 8);
        StdDraw.show();
        StdDraw.setPenColor(StdDraw.WHITE);
        while (true) {
            if (inputSource.possibleNextInput()) {
                char key = inputSource.getNextKey();
                if (key == 'Y') {
                    avatar = createdAvatar;
                    return;
                }
                if (key == 'N') {
                    return;
                }
            }
        }
    }

    private void changeAvatar() {
        TETile[] avatarOptions = {Tileset.AMPERSAND, Tileset.QUESTION, Tileset.AT, Tileset.PLUS, Tileset.DOLLAR};
        subtext("Select the avatar you want by pressing 0 - 4.");

        for (int i = 0; i < avatarOptions.length; i++) {
            StdDraw.setPenColor(StdDraw.WHITE);
            StdDraw.text(CENTER_WIDTH - 5, CENTER_HEIGHT - 8 - (3 * i), Integer.toString(i));
            avatarOptions[i].draw(CENTER_WIDTH, CENTER_HEIGHT - 8 - (3 * i));
        }

        StdDraw.show();

        while (true) {
            if (inputSource.possibleNextInput()) {
                char key = inputSource.getNextKey();
                if (Character.isDigit(key)) {
                    int choice = Integer.parseInt("" + key);
                    if (choice >= 0 && choice <= 4) {
                        avatar = avatarOptions[choice];
                        break;
                    }
                }
            }
        }
    }

    @Override
    public String toString() {
        return game.getWorldTiles().toString();
    }


    public static void main(String[] args) {
        Engine engine = new Engine();
        // case 1
        TETile[][] case1Tiles = engine.interactWithInputString("N999SDDDWWWDDD");
        // case 2
        engine.interactWithInputString("N999SDDD:Q");
        TETile[][] case2Tiles = engine.interactWithInputString("LWWWDDD");
        // case 3
        engine.interactWithInputString("N999SDDD:Q");
        engine.interactWithInputString("LWWW:Q");
        TETile[][] case3Tiles = engine.interactWithInputString("LDDD:Q");
        // case 4
        engine.interactWithInputString("N999SDDD:Q");
        engine.interactWithInputString("L:Q");
        engine.interactWithInputString("L:Q");
        TETile[][] case4Tiles = engine.interactWithInputString("LWWWDDD");

        String string1 = TETile.toString(case1Tiles);
        String string2 = TETile.toString(case2Tiles);
        String string3 = TETile.toString(case3Tiles);
        String string4 = TETile.toString(case4Tiles);
        System.out.println(string1.equals(string2));
        System.out.println(string2.equals(string3));
        System.out.println(string3.equals(string4));
        System.exit(0);
    }

}

