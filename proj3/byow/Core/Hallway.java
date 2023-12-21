package byow.Core;
import byow.TileEngine.TERenderer;
public class Hallway {
    private World world;
    private String direction;
    private boolean built = false;
    private Room hallway;
    private Position entrance;
    private Position exit;
    private static final int WIDTH = 1;
    private static final int EXIT_OFFSET = 1;
    public Hallway(World world, int length, String direction, Position origin) {
        this.direction = direction;
        this.world = world;

        switch (direction) {
            case "right":
                entrance = new Position(origin.x() + EXIT_OFFSET, origin.y());
                exit = new Position (entrance.x() + length + EXIT_OFFSET, entrance.y());
                origin = new Position(entrance.x(), entrance.y() - EXIT_OFFSET);
                generateHallway(length, WIDTH, origin, entrance);
                break;
            case "left":
                entrance = new Position(origin.x() - EXIT_OFFSET, origin.y());
                exit = new Position (entrance.x() - length, entrance.y());
                origin = new Position(entrance.x() - length, entrance.y() - EXIT_OFFSET);
                generateHallway(length, WIDTH, origin, entrance);
                break;
            case "up":
                entrance = new Position(origin.x(), origin.y() + EXIT_OFFSET);
                exit = new Position (entrance.x(), entrance.y() + length + EXIT_OFFSET);
                origin = new Position(entrance.x() - EXIT_OFFSET, entrance.y());
                generateHallway(WIDTH, length, origin, entrance);
                break;
            case "down":
                entrance = new Position(origin.x(), origin.y() - EXIT_OFFSET);
                exit = new Position (entrance.x(), entrance.y() - length);
                origin = new Position(entrance.x() - EXIT_OFFSET, entrance.y() - length);
                generateHallway(WIDTH, length, origin, entrance);
                break;
        }
    }

    private void generateHallway(int width, int height, Position origin, Position entrance) {
        built = Room.canBuild(world, width, height, origin);
        if (wasBuilt()) {
            hallway = new Room(world, width, height, origin, entrance);
        }
    }

    // Attempts to generate and return a room located at the hallway's exit. Returns null if it wasn't built.

    public Room generateRoomAtExit(int width, int height, int offset) {
        Room finalRoom = null;
        Position roomOrigin = null;
        if (!isValidOffset(width, height, offset)) {
            return null;
        }
        Position entrance = null;
        switch (direction) {
            case "right":
                entrance = new Position(exit.x() + EXIT_OFFSET, exit.y());
                roomOrigin = new Position(exit.x() + EXIT_OFFSET, exit.y() - EXIT_OFFSET - offset);
                break;
            case "left":
                entrance = new Position(exit.x() - EXIT_OFFSET, exit.y());
                roomOrigin = new Position(exit.x() - width - EXIT_OFFSET - EXIT_OFFSET, exit.y() - EXIT_OFFSET - offset);
                break;
            case "up":
                entrance = new Position(exit.x(), exit.y() + EXIT_OFFSET);
                roomOrigin = new Position(exit.x() - EXIT_OFFSET - offset, exit.y() + EXIT_OFFSET);
                break;
            case "down":
                entrance = new Position(exit.x(), exit.y() - EXIT_OFFSET);
                roomOrigin = new Position(exit.x() - EXIT_OFFSET - offset, exit.y() - height - EXIT_OFFSET - EXIT_OFFSET);
                break;
        }
        finalRoom = new Room(world, width, height, roomOrigin, entrance);
        if (finalRoom.wasBuilt()) {
            hallway.setExitAt(exit);
            return finalRoom;
        }
        return null;
    }

    public Hallway buildCorner(int length, int choice) {
        Room corner = generateRoomAtExit(1, 1, 0);

        if (corner == null) {
            return null;
        }

        Hallway newHallway;
        switch (direction) {
            case "up", "down":
                switch (choice) {
                    case 0:
                        newHallway = corner.buildHallOutOf("west", 0, length);
                        return newHallway;
                    case 1:
                        newHallway = corner.buildHallOutOf("east", 0, length);
                        return newHallway;
                }
            case "left", "right":
                switch (choice) {
                    case 0:
                        newHallway = corner.buildHallOutOf("north", 0, length);
                        return newHallway;
                    case 1:
                        newHallway = corner.buildHallOutOf("south", 0, length);
                        return newHallway;
                }
        }

        return null;
    }

    private boolean isValidOffset(int width, int height, int offset) {
        switch (direction) {
            case "right", "left":
                return offset >= 0 && offset < height;
            case "up", "down":
                return offset >= 0 && offset < width;
        }
        return false;
    }

    public boolean wasBuilt() {
        return built;
    }

    public String getDirection() {
        return direction;
    }

    public Position getEntrance() {
        return entrance;
    }

    public Position getExit() {
        return exit;
    }

    public Room getRoom() {
        return hallway;
    }

    public static void main(String args[]) {
        int worldWidth = 70;
        int worldHeight = 50;
        World world = new World(worldWidth, worldHeight);


        int hallLength = 4;

        Position origin = new Position(10, 20);
        Room room1 = new Room(world, 5, 3, origin);
        Hallway hallway1 = room1.buildHallOutOf("south", 2, 4);
        Room room2 = hallway1.generateRoomAtExit(3, 4, 2);
        System.out.println(room2.getEntrance());
        System.out.println(hallway1.getExit());
        Hallway hallway2 = room1.buildHallOutOf("east", 2, 4);
        Hallway hallway3 = hallway2.buildCorner(5, 0);
        System.out.println(hallway3 == null);


/*        Hallway hallway2 = room2.buildHallOutOf("east", 3, 7);
        Room room3 = hallway2.generateRoomAtExit(10, 10, 5);
        room3.buildHallOutOf("west", 2, 5);*/


        TERenderer ter = new TERenderer();
        ter.initialize(worldWidth, worldHeight);
        ter.renderFrame(world.getWorld());


    }

}





