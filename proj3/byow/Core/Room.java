package byow.Core;
import byow.TileEngine.*;
import java.util.*;


public class Room {
    private HashMap<String, Wall> wallMap;
    private Position entrance;
    private Collection<Position> exits;
    private int width;
    private int height;
    private World world;
    private Position roomOrigin;
    private boolean built;
    private static final TETile FLOOR = Tileset.FLOOR;
    private static final TETile WALL = Tileset.WALL;

    private static final int WALL_OFFSET = 1;
    private static final int CORNER_OFFSET = 1;
    private static final List<TETile> ROOM_TILES = List.of(Tileset.FLOOR, Tileset.WALL);

    public Room(World world, int width, int height, Position origin) {
        built = canBuild(world, width, height, origin);
        if (wasBuilt()) {
            this.width = width;
            this.height = height;
            this.world = world;
            this.roomOrigin = origin;

            //set the wall areas
            setWalls(origin);
            //room generator
            generateRoom(origin);
            exits = makePositionCollection();
        }
    }

    public Room(World world, int width, int height, Position origin, Position entrance) {
        built = canBuild(world, width, height, origin);
        if (wasBuilt()) {
            this.width = width;
            this.height = height;
            this.world = world;
            this.roomOrigin = origin;

            setWalls(origin);
            generateRoom(origin);
            setEntranceAt(entrance);
            exits = makePositionCollection();
        }
    }

    private Collection<Position> makePositionCollection() {
        return new HashSet<>();
    }

    // Generates a room at Position origin.

    private void generateRoom(Position origin) {
        TETile[][] tiles = world.getWorld();

        // Builds the area inside the room.

        for (int currX = origin.x(); currX <= origin.x() + width + WALL_OFFSET; currX++) {
            tiles[currX][origin.y()] = WALL;
            tiles[currX][origin.y() + height + WALL_OFFSET] = WALL;
        }

        // Builds the perimeter around the room.

        for (int currY = origin.y(); currY <= origin.y() + height + WALL_OFFSET; currY++) {
            tiles[origin.x()][currY] = Tileset.WALL;
            tiles[origin.x() + width + WALL_OFFSET][currY] = WALL;
        }

        for (int i = origin.x() + CORNER_OFFSET; i < origin.x() + width + CORNER_OFFSET; i++) {
            for (int j = origin.y() + CORNER_OFFSET; j < origin.y() + height + CORNER_OFFSET; j++) {
                tiles[i][j] = FLOOR;
            }
        }
    }

    // Sets up the Wall objects pertaining to the room.

    private void setWalls(Position origin) {
        Wall westWall = new Wall("west");
        Wall eastWall = new Wall("east");

        // Sets up west and east walls.

        for (int y = origin.y() + CORNER_OFFSET; y < origin.y() + height + CORNER_OFFSET; y++) {
            westWall.addPos(new Position(origin.x(), y));
            eastWall.addPos(new Position(origin.x() + width + WALL_OFFSET, y));
        }

        Wall southWall = new Wall("south");
        Wall northWall = new Wall("north");

        // Sets up south and north walls.

        for (int x = origin.x() + CORNER_OFFSET; x < origin.x() + width + CORNER_OFFSET; x++) {
            southWall.addPos(new Position(x, origin.y()));
            northWall.addPos(new Position(x, origin.y() + height + WALL_OFFSET));
        }

        wallMap = new HashMap<>();
        wallMap.put("west", westWall);
        wallMap.put("east", eastWall);
        wallMap.put("south", southWall);
        wallMap.put("north", northWall);

    }

    // Gets the Wall object based on the cardinal direction provided (north, south, west, east).
    public Wall getWall(String direction) {
        if (wasBuilt()) {
            return wallMap.get(direction);
        }
        return null;
    }

    // Returns if the room was successfully constructed.

    public boolean wasBuilt() {
        return built;
    }

    // Returns if a wall has an entrance.
    public boolean hasEntranceOrExit(Wall wall) {
        if (wall != null) {
            for (Position exit: exits) {
                if (wall.getWall().contains(exit)) {
                    return true;
                }
            }
            return wall.getWall().contains(entrance);
        }
        return false;
    }

    // Given a World object and the specified room specifications (dimensions and origin), return if it can be built.

    public static boolean canBuild(World world, int width, int height, Position origin) {
        if (!world.isValidArea(width + WALL_OFFSET, height + WALL_OFFSET, origin.x(), origin.y())) {
            return false;
        }
        return !roomCheck(world, width, height, origin);
    }

    private static boolean roomCheck(World world, int width, int height, Position origin) {
        for (int y = origin.y(); y < origin.y() + height + WALL_OFFSET; y++) {
            if (isRoomTileOrNull(world,  origin.x(), y) || isRoomTileOrNull(world,  origin.x() + width, y)) {
                return true;
            }
        }
        for (int x = origin.x(); x < origin.x() + width + WALL_OFFSET; x++) {
            if (isRoomTileOrNull(world, x, origin.y()) || isRoomTileOrNull(world,  x, origin.y() + height)) {
                return true;
            }
        }
        return false;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    private static boolean isRoomTileOrNull(World world, int x, int y) {
        if (world.tileAt(x, y) == null) {
            return true;
        }

        if (ROOM_TILES.contains(world.tileAt(x, y))) {
            return true;
        }

        return false;
    }

    // Sets the room's entrance as being the specified position.

    public void setEntranceAt(Position position) {
        TETile[][] tiles = world.getWorld();
        this.entrance = position;
        tiles[entrance.x()][entrance.y()] = FLOOR;
    }

    // Sets the room's exit as being the specified position.

    public void setExitAt(Position exit) {
        TETile[][] tiles = world.getWorld();
        exits.add(exit);
        tiles[exit.x()][exit.y()] = FLOOR;
    }

    public Hallway buildHallOutOf(String wallDirection, int tile, int length) {
        Wall wall = wallMap.get(wallDirection);
        if (!isValidChoice(wallDirection, tile) || hasEntranceOrExit(wall)) {
            return null;
        }
        String direction = wall.getDirection();
        ArrayList<Position> positions =  new ArrayList<>(wall.getWall());
        Position origin = positions.get(tile);

        Hallway hallway = null;
        switch (direction) {
            case "north":
                hallway = new Hallway(world, length, "up", origin);
                break;
            case "south":
                hallway = new Hallway(world, length, "down", origin);
                break;
            case "east":
                hallway = new Hallway(world, length, "right", origin);
                break;
            case "west":
                hallway = new Hallway(world, length, "left", origin);
                break;
            default:
                break;
        }
        if (hallway.wasBuilt()) {
            setExitAt(origin);
            return hallway;
        }
        return null;
    }

    private boolean isValidChoice(String wallDirection, int tile) {
        return wallMap.containsKey(wallDirection) && tile >= 0 && tile <= wallMap.get(wallDirection).getWall().size();
    }

    public Position getEntrance() {
        return entrance;
    }

    public Collection<Position> getExits() {
        return exits;
    }

    public Position getOrigin() {
        return roomOrigin;
    }

}

