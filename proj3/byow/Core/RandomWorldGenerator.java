package byow.Core;

import byow.TileEngine.TETile;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class RandomWorldGenerator {
    private static final int X_BOUND = 80;
    private static  final int Y_BOUND = 40;
    private static final int LOWER_ROOMSIZE_BOUND = 2;
    private static final int UPPER_ROOMSIZE_BOUND = 9;
    private static final int LOWER_HALL_BOUND = 2;
    private static final int UPPER_HALL_BOUND = 10;
    private static final int LOWER_NUM_ROOMS = 10;
    private static final int UPPER_NUM_ROOMS = 30;
    private Random generator;
    private World world;
    private static final int START_BOUND = X_BOUND / 4;
    private static final int END_BOUND = X_BOUND - START_BOUND;
    private static final int MAX_ATTEMPTS = 5000;
    private static final int CURRENT_ROOM_NULL_COUNTER = 50;
    private Room first;
    private Position avatarSpawn;
    private Position enemySpawn;
    private ArrayList<Hallway> hallsBuilt;
    private ArrayList<Room> roomsBuilt;

    public RandomWorldGenerator(long seed) {
        generator = new Random(seed);
        world = new World(X_BOUND, Y_BOUND);
        roomsBuilt = new ArrayList<>();
        hallsBuilt = new ArrayList<>();

        Room currentRoom = constructingFirstRoom();
        int currentRoomNullCounter = CURRENT_ROOM_NULL_COUNTER;

        while (currentRoomNullCounter >= 0) {
            Hallway hall = createHallFromRoom(currentRoom);
            if (hall == null) {
                hall = pickHallAndBuildNewHall();
                if (hall == null) {
                    iterateOverRoomsAndCreateHalls();
                    iterateOverHallsAndCreateCorners();
                    hall = chooseRandomHallway();
                }
            }

            int cornerOrNot = generator.nextInt(0, 2);
            switch (cornerOrNot) {
                case 0:
                    break;
                case 1:
                    hall = createCorner(hall);
                    break;
                default:
                    break;
            }

            currentRoom = createRoomFromHallway(hall);
            if (currentRoom == null) {
                // iterateOverRoomsAndCreateHalls();
                currentRoom = chooseRandomRoom();
                currentRoomNullCounter -= 1;
            }
        }

        Position rightmostRoom = Position.findRightmostPosition(createListOfRoomOrigins());
        enemySpawn = new Position(rightmostRoom.x() + 1, rightmostRoom.y() + 1);
    }

    private void iterateOverRoomsAndCreateHalls() {
        ArrayList<Room> copy = new ArrayList<>(roomsBuilt);
        for (Room room: copy) {
            createHallFromRoom(room);
        }
    }

    private void iterateOverHallsAndCreateCorners() {
        ArrayList<Hallway> copy = new ArrayList<>(hallsBuilt);
        for (Hallway hall: copy) {
            createCorner(hall);
        }
    }

    private Hallway pickHallAndBuildNewHall() {
        Hallway chosenHall = chooseRandomHallway();
        Room hallRoom = chosenHall.getRoom();
        Hallway returnHall = createHallFromRoom(hallRoom);
        return returnHall;
    }

    private Hallway pickRoomAndBuildNewHall() {
        Room chosenRoom = chooseRandomRoom();
        Hallway returnHall = createHallFromRoom(chosenRoom);
        return returnHall;
    }

    private Hallway createCorner(Hallway hall) {
        Hallway backup = hall;
        hall = null;
        int counter = MAX_ATTEMPTS;
        while (hall == null && counter >= 0) {
            int length = generator.nextInt(LOWER_HALL_BOUND, UPPER_HALL_BOUND);
            int choice = generator.nextInt(0, 2);
            hall = backup.buildCorner(length, choice);
            counter -= 1;
        }
        if (hall == null) {
            return backup;
        }
        hallsBuilt.add(hall);
        return hall;
    }

    private Room constructingFirstRoom() {
        int startX = generator.nextInt(0, START_BOUND);
        int startY = generator.nextInt(0, Y_BOUND);
        Position origin = new Position(startX, startY);
        int width = generator.nextInt(LOWER_ROOMSIZE_BOUND, UPPER_ROOMSIZE_BOUND);
        int height = generator.nextInt(LOWER_ROOMSIZE_BOUND, UPPER_ROOMSIZE_BOUND);
        Room firstRoom = new Room(world, width, height, origin);
        if (firstRoom.wasBuilt()) {
            roomsBuilt.add(firstRoom);
            this.first = firstRoom;
            avatarSpawn = new Position(startX + 1, startY + 1);
            return firstRoom;
        }
        return constructingFirstRoom();
    }


    private Hallway createHallFromRoom(Room room) {
        Hallway returnHall = null;
        int counter = MAX_ATTEMPTS;
        while (returnHall == null && counter >= 0) {
            int length = generator.nextInt(LOWER_HALL_BOUND, UPPER_HALL_BOUND);
            String[] dir = {"north", "east", "west", "south"};
            int c = generator.nextInt(0, 4);
            String direction = dir[c];
            int tile = 0;
            switch (direction) {
                case "south", "north":
                    tile = generator.nextInt(0, room.getWidth());
                    break;
                case "east", "west":
                    tile = generator.nextInt(0, room.getHeight());
                    break;
                default:
                    break;
            }
            returnHall = room.buildHallOutOf(direction, tile, length);
            counter -= 1;
        }
        if (returnHall != null) {
            hallsBuilt.add(returnHall);
            Room returnHallRoom = returnHall.getRoom();

        }
        return returnHall;
    }

    private Room createRoomFromHallway(Hallway hall) {
        Room returnRoom = null;
        int counter = MAX_ATTEMPTS;
        while (returnRoom == null && counter >= 0) {
            int width = generator.nextInt(LOWER_ROOMSIZE_BOUND, UPPER_ROOMSIZE_BOUND);
            int height = generator.nextInt(LOWER_ROOMSIZE_BOUND, UPPER_ROOMSIZE_BOUND);
            int offset = 0;
            switch (hall.getDirection()) {
                case "up", "down":
                    offset = generator.nextInt(0, width);
                    break;
                case "right", "left":
                    offset = generator.nextInt(0, height);
                    break;
                default:
                    break;
            }
            returnRoom = hall.generateRoomAtExit(width, height, offset);
            counter -= 1;
        }
        if (returnRoom != null) {
            roomsBuilt.add(returnRoom);
        }
        return returnRoom;
    }

    private Hallway chooseRandomHallway() {
        int choice = generator.nextInt(0, hallsBuilt.size());
        Hallway chosenHall = hallsBuilt.get(choice);
        return chosenHall;
    }
    private Room chooseRandomRoom() {
        int choice = generator.nextInt(0, roomsBuilt.size());
        Room chosenRoom = roomsBuilt.get(choice);
        return chosenRoom;
    }

    private List<Position> createListOfRoomOrigins() {
        ArrayList<Position> returnList = new ArrayList<>();
        for (Room room: roomsBuilt) {
            returnList.add(room.getOrigin());
        }
        return returnList;
    }

    public World getWorldObject() {
        return world;
    }
    public TETile[][] getWorld() {
        return world.getWorld();
    }

    public Position getAvatarSpawn() {
        return avatarSpawn;
    }
    public Position getEnemySpawn() {
        return enemySpawn;
    }

}

