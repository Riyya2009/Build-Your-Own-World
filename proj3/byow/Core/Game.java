package byow.Core;

import byow.InputDemo.InputSource;
import byow.InputDemo.StringInputDevice;
import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;

import edu.princeton.cs.algs4.BreadthFirstPaths;
import edu.princeton.cs.algs4.Graph;
import edu.princeton.cs.algs4.StdDraw;

import java.io.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Game implements Serializable {
    private static final int X_BOUND = 80;
    private static final int Y_BOUND = 40;
    private static final double TEXT_X = 0.5;
    private static final double TEXT_Y = 41;
    private Avatar avatar;
    private World world;
    private String currentText;
    private TERenderer ter;
    private boolean colonFlag;
    private boolean gameActive;
    //    private RandomWorldGenerator worldGen;
    private String saveFileName = "save_data.txt";
    private Enemy enemy;
    private Graph worldGraph;
    private boolean pathVisualIsOn;
    private static final List<String> VALID_GRAPH_TILES = List.of("you", "path marker", "enemy", "floor");
    private boolean gameOver;
    private static final HashSet<Character> MOVEMENT_KEYS = new HashSet<>(Set.of('W', 'A', 'S', 'D'));
    private InputSource inputSource;

    public Game(long seed, InputSource inputSource) {
        RandomWorldGenerator randomWorld = new RandomWorldGenerator(seed);
        world = randomWorld.getWorldObject();
        int spawnX = randomWorld.getAvatarSpawn().x();
        int spawnY = randomWorld.getAvatarSpawn().y();
        avatar = new Avatar(spawnX, spawnY, world);
        this.inputSource = inputSource;
        if (!checkStringInputSource()) {
            initializeGraph();
            int enemySpawnX = randomWorld.getEnemySpawn().x();
            int enemySpawnY = randomWorld.getEnemySpawn().y();
            enemy = new Enemy(enemySpawnX, enemySpawnY, world);
            terInitialize();
        }
        gameOver = false;
    }

    public Game(long seed, TETile newAvatar, InputSource inputSource) {
        RandomWorldGenerator randomWorld = new RandomWorldGenerator(seed);
        world = randomWorld.getWorldObject();
        int spawnX = randomWorld.getAvatarSpawn().x();
        int spawnY = randomWorld.getAvatarSpawn().y();
        avatar = new Avatar(spawnX, spawnY, world, newAvatar);
        if (!checkStringInputSource()) {
            int enemySpawnX = randomWorld.getEnemySpawn().x();
            int enemySpawnY = randomWorld.getEnemySpawn().y();
            enemy = new Enemy(enemySpawnX, enemySpawnY, world);
            initializeGraph();
            terInitialize();
        }
        this.inputSource = inputSource;
        gameOver = false;
    }

    public Game(InputSource inputSource) {
        this.inputSource = inputSource;
        loadWorld();
        if (!checkStringInputSource()) {
            initializeGraph();
            terInitialize();
            startGame();
        }
    }

    public Game(RandomWorldGenerator randomWorld, InputSource inputSource) {
        world = randomWorld.getWorldObject();
        int spawnX = randomWorld.getAvatarSpawn().x();
        int spawnY = randomWorld.getAvatarSpawn().y();
        avatar = new Avatar(spawnX, spawnY, world);
        this.inputSource = inputSource;
    }

    private void terInitialize() {
        ter = new TERenderer();
        ter.initialize(X_BOUND, Y_BOUND + 2);
        ter.renderFrame(world.getWorld());
    }

    public void startGame() {
        gameActive = true;

        while (gameActive) {
            inputProcessor();
            updateHUD();

            if (enemy.hasCaughtUpWithAvatar(avatar)) {
                gameActive = false;
                gameOver = true;
            }
        }
    }

    private boolean checkStringInputSource() {
        return inputSource instanceof StringInputDevice;
    }

    public void simulateGame() {
        while (inputSource.possibleNextInput()) {
            char key = inputSource.getNextKey();
            if (key == ':') {
                colonFlag = true;
            } else if (colonFlag && key == 'Q') {
                saveWorld();
                return;
            } else {
                colonFlag = false;
            }
            avatar.move(key);
        }
    }

    private void updateHUD() {
        double x = StdDraw.mouseX();
        double y = StdDraw.mouseY();

        TETile tileAt = world.tileAt((int) x, (int) y);

        if (tileAt == null) {
            return;
        }

        String text = tileAt.description();

        if (text == null || text.equals(currentText)) {
            return;
        }

        clearText();

        currentText = text;

        displayCurrentText();

    }

    private void displayCurrentText() {
        StdDraw.setPenColor(StdDraw.WHITE);
        StdDraw.textLeft(TEXT_X, TEXT_Y, currentText);
        StdDraw.show();
    }

    public boolean isGameOver() {
        return gameOver;
    }

    private void clearText() {
        StdDraw.clear();
        ter.renderFrame(world.getWorld());
        StdDraw.show();
    }

    private void inputProcessor() {
        if (!inputSource.possibleNextInput()) {
            return;
        }

        char key = inputSource.getNextKey();

        if (key == ':') {
            colonFlag = true;
        } else if (colonFlag && key == 'Q') {
            saveWorld();
            gameActive = false;
            return;
        } else {
            colonFlag = false;
        }

        if (key == 'P') {
            togglePathVisual();
            return;
        }

        if (MOVEMENT_KEYS.contains(key)) {
            if (pathVisualIsOn) {
                changeTilesOnPath(Tileset.PATH_MARKER, Tileset.FLOOR);
            }

            avatar.move(key);

            enemy.moveTowardsAvatar(avatar, worldGraph);

            if (pathVisualIsOn) {
                changeTilesOnPath(Tileset.FLOOR, Tileset.PATH_MARKER);
            }
            if (!checkStringInputSource()) {
                ter.renderFrame(world.getWorld());
                displayCurrentText();
            }

        }

    }

    private void togglePathVisual() {
        if (pathVisualIsOn) {
            pathVisualIsOn = false;
            changeTilesOnPath(Tileset.PATH_MARKER, Tileset.FLOOR);
            ter.renderFrame(world.getWorld());
        } else {
            pathVisualIsOn = true;
            changeTilesOnPath(Tileset.FLOOR, Tileset.PATH_MARKER);
            ter.renderFrame(world.getWorld());
        }
    }

    private void changeTilesOnPath(TETile originalTile, TETile desiredTile) {
        int enemyVertex = convertCoordinates(enemy.getxPos(), enemy.getyPos());
        BreadthFirstPaths paths = new BreadthFirstPaths(worldGraph, enemyVertex);
        int avatarVertex = convertCoordinates(avatar.getxPos(), avatar.getyPos());
        Iterable<Integer> pathToAvatar = paths.pathTo(avatarVertex);
        TETile[][] tiles = world.getWorld();
        for (int vertex: pathToAvatar) {
            Position positionInWorld = convertVertexNumber(vertex);
            if (tileAtPosition(positionInWorld).equals(originalTile)) {
                changeTileAtPosition(positionInWorld, desiredTile);
            }
        }
    }

    private void initializeGraph() {
        worldGraph = new Graph(X_BOUND * Y_BOUND);
        for (int x = 0; x < X_BOUND; x++) {
            for (int y = 0; y < Y_BOUND; y++) {
                if (validGraphTileAt(x, y)) {
                    setEdgesFor(x, y);
                }
            }
        }
    }

    private void setEdgesFor(int x, int y) {
        int currentNode = convertCoordinates(x, y);
        int destination = 0;
        if (validGraphTileAt(x - 1, y)) {
            destination = convertCoordinates(x - 1, y);
            worldGraph.addEdge(currentNode, destination);
        }
        if (validGraphTileAt(x + 1, y)) {
            destination = convertCoordinates(x + 1, y);
            worldGraph.addEdge(currentNode, destination);
        }
        if (validGraphTileAt(x, y + 1)) {
            destination = convertCoordinates(x, y + 1);
            worldGraph.addEdge(currentNode, destination);
        }
        if (validGraphTileAt(x, y - 1)) {
            destination = convertCoordinates(x, y - 1);
            worldGraph.addEdge(currentNode, destination);
        }
    }

    private boolean validGraphTileAt(int x, int y) {
        return VALID_GRAPH_TILES.contains(world.tileAt(x, y).description());
    }

    private int convertCoordinates(int x, int y) {
        return (y * X_BOUND) + x;
    }

    private Position convertVertexNumber(int v) {
        int x = v % X_BOUND;
        int y = v / X_BOUND;
        Position returnPosition = new Position(x, y);
        return returnPosition;
    }

    private TETile tileAtPosition(Position position) {
        int x = position.x();
        int y = position.y();
        return world.tileAt(x, y);
    }

    private void changeTileAtPosition(Position position, TETile tile) {
        int x = position.x();
        int y = position.y();
        TETile[][] tiles = world.getWorld();
        tiles[x][y] = tile;
    }

    public TETile[][] getWorldTiles() {
        return world.getWorld();
    }

    private void saveWorld() {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(saveFileName))) {
            out.writeObject(world);
            out.writeObject(avatar);
            if (!checkStringInputSource()) {
                out.writeObject(enemy);
                out.writeObject(pathVisualIsOn);
            }
        } catch (IOException e) {
            System.out.println("Error saving game.");
        }
    }

    //    load the state of the game world and the avatar's position from a file using object deserialization.
    private void loadWorld() {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(saveFileName))) {
            World savedWorld = (World) in.readObject();
            Avatar savedAvatar = (Avatar) in.readObject();
            if (!checkStringInputSource()) {
                Enemy savedEnemy = (Enemy) in.readObject();
                boolean savedToggle = (boolean) in.readObject();
                enemy = savedEnemy;
                pathVisualIsOn = savedToggle;
            }
            world = savedWorld;
            avatar = savedAvatar;
        } catch (IOException | ClassNotFoundException e) {
            System.exit(0);
        }
    }


}
