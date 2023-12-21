package byow.Core;


import byow.TileEngine.*;


import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;


public class Avatar implements Serializable {
    private TETile avatarTile;
    private static final TETile AVATAR = Tileset.AVATAR;
    private static final TETile FLOOR = Tileset.FLOOR;
    private World world;
    //store the currPos using Saving and Loading
    private int xPos;
    private int yPos;
    private final Map<Character, Position> keys = new HashMap<>();


    //basic initialize
    public Avatar(int xPos, int yPos, World world) {
        this.xPos = xPos;
        this.yPos = yPos;
        this.world = world;
        this.avatarTile = AVATAR;

        placeAvatarInWorld(xPos, yPos);

        keys.put('W', new Position(0, 1));
        keys.put('A', new Position(-1, 0));
        keys.put('S', new Position(0, -1));
        keys.put('D', new Position(1, 0));

    }

    public Avatar(int xPos, int yPos, World world, TETile newAvatar) {
        this.xPos = xPos;
        this.yPos = yPos;
        this.world = world;
        this.avatarTile = newAvatar;

        placeAvatarInWorld(xPos, yPos);

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

    private void placeAvatarInWorld(int x, int y) {
        TETile[][] tiles = world.getWorld();
        tiles[x][y] = avatarTile;
    }

    private void resetTileToFloor(int x, int y) {
        TETile[][] tiles = world.getWorld();
        tiles[x][y] = FLOOR;
    }

    public boolean move(char input) {
        Position direction = getValue(input);
        if (direction == null) {
            return false;
        }

        int newXPos = direction.x() + xPos;
        int newYPos = direction.y() + yPos;

        if (!canMove(newXPos, newYPos)) {
            return false;
        }

        placeAvatarInWorld(newXPos, newYPos);
        resetTileToFloor(xPos, yPos);

        xPos = newXPos;
        yPos = newYPos;

        return true;
    }

    public World getWorld() {
        return world;
    }

}
