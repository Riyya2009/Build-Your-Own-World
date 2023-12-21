package byow.Core;

import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;
import edu.princeton.cs.algs4.BreadthFirstPaths;
import edu.princeton.cs.algs4.Graph;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Enemy implements Serializable {
    private static final TETile ENEMY = Tileset.ENEMY;
    private static final TETile FLOOR = Tileset.FLOOR;
    private World world;
    //store the currPos using Saving and Loading
    private int xPos;
    private int yPos;
    private final Map<Character, Position> keys = new HashMap<>();

    //basic initialize
    public Enemy(int xPos, int yPos, World world) {
        this.xPos = xPos;
        this.yPos = yPos;
        this.world = world;

        placeEnemyInWorld(xPos, yPos);

        keys.put('W', new Position(0, 1));
        keys.put('A', new Position(-1, 0));
        keys.put('S', new Position(0, -1));
        keys.put('D', new Position(1, 0));

    }

    public int getxPos() {
        return xPos;
    }

    public int getyPos() {
        return yPos;
    }

    public boolean isWall(int x, int y) {
        return world.tileAt(x, y).equals(Tileset.WALL);
    }

    public Map<Character, Position> getKeys() {
        return this.keys;
    }

    public Position getValue(Character key) {
        return keys.get(key);
    }

    public boolean canMove(int x, int y) {
        if (isWall(x, y) || world.tileAt(x, y) == null) {
            return false;
        }
        return true;
    }

    private void placeEnemyInWorld(int x, int y) {
        TETile[][] tiles = world.getWorld();
        tiles[x][y] = ENEMY;
    }

    private void resetTileToFloor(int x, int y) {
        TETile[][] tiles = world.getWorld();
        tiles[x][y] = FLOOR;
    }

    public boolean moveInput(char input) {
        Position direction = getValue(input);
        if (direction == null) {
            return false;
        }

        int newXPos = direction.x() + xPos;
        int newYPos = direction.y() + yPos;

        if (!canMove(newXPos, newYPos)) {
            return false;
        }

        placeEnemyInWorld(newXPos, newYPos);
        resetTileToFloor(xPos, yPos);

        xPos = newXPos;
        yPos = newYPos;

        return true;
    }

    public World getWorld() {
        return world;
    }

    public void moveTowardsAvatar(Avatar avatar, Graph graph) {
        int enemyVertex = convertCoordinates(xPos, yPos);
        BreadthFirstPaths bfs = new BreadthFirstPaths(graph, enemyVertex);
        int avatarVertex = convertCoordinates(avatar.getxPos(), avatar.getyPos());
        Iterable<Integer> path = bfs.pathTo(avatarVertex);
        int width = world.getWidth();

        if (path != null) {
            for (int pathVertex : path) {
                if (pathVertex == enemyVertex) {
                    continue;
                }
                if (pathVertex == enemyVertex + 1) {
                    moveInput('D');
                } else if (pathVertex == enemyVertex - 1) {
                    moveInput('A');
                } else if (pathVertex == enemyVertex + width) {
                    moveInput('W');
                } else if (pathVertex == enemyVertex - width) {
                    moveInput('S');
                }
                break;
            }
        }
    }

    private int convertCoordinates(int x, int y) {
        return (y * world.getWidth()) + x;
    }

    public boolean hasCaughtUpWithAvatar(Avatar avatar) {
        return this.getxPos() == avatar.getxPos() && this.getyPos() == avatar.getyPos();
    }

}
