package byow.Core;
import byow.TileEngine.*;

import java.io.Serializable;

public class World implements Serializable {
    private int width;
    private int height;
    private TETile[][] world;

    public World(int width, int height) {
        this.width = width;
        this.height = height;
        world = new TETile[width][height];

        //initializing
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                world[x][y] = Tileset.LOCKED_DOOR;
            }
        }
    }

    /** Returns the tiles associated with this World. */
    public TETile[][] getWorld() {
        return world;
    }

    // Given a certain area in the World, return true if no part of the area is out of bounds, false if not.
    public boolean isValidArea(int roomWidth, int roomHeight, int xPos, int yPos) {
        return !violatesBounds(xPos + roomWidth, yPos + roomHeight)
                && !violatesBounds(xPos, yPos);
    }


    // Checks if position (x,y) is out of bounds
    public boolean violatesBounds(int x, int y) {
        if ((x < width && x >= 0) && (y < height && y >= 0)) {
            return false;
        }
        return true;
    }

    // Returns tile at position (x, y) of the World.
    public TETile tileAt(int x, int y) {
        if (violatesBounds(x, y)) {
            return null;
        }
        return world[x][y];
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }


}



