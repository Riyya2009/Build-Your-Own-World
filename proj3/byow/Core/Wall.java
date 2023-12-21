package byow.Core;


import java.util.HashSet;
import java.util.Set;


public class Wall {
    private HashSet<Position> pos;
    private String direction;
    private static Set<String> directions = Set.of("north", "south", "east", "west");

    public Wall(String direction) {
        if (directions.contains(direction)) {
            pos = new HashSet<>();
            this.direction = direction;
        } else {
            throw new IllegalArgumentException();
        }
    }

    public void addPos(Position position) {
        this.pos.add(position);
    }

    public HashSet<Position> getWall() {
        return pos;
    }

    public String getDirection() {
        return direction;
    }

    @Override
    public String toString() {
        return pos.toString();
    }

}

