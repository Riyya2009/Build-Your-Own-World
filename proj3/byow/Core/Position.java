package byow.Core;
import java.io.Serializable;
import java.util.Comparator;
import java.util.List;

public class Position implements Serializable {
    private int x;
    private int y;

    public Position(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int x() {
        return x;
    }

    public int y() {
        return y;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (obj instanceof Position copy) {
            return copy.x() == this.x() && copy.y() == this.y();
        }
        return false;
    }

    @Override
    public int hashCode() {
        return this.toString().hashCode();
    }

    @Override
    public String toString() {
        return "(" + this.x() + " , " + this.y() + ")";
    }

    public static void main(String[] args) {
        Position position1 = new Position(5, 5);
        Position position2 = new Position(5, 2);
        System.out.println(position1.equals(position2));
    }

    public static Position findRightmostPosition(List<Position> positions) {
        return maxHelp(positions, new RightmostComparator<>());
    }

    private static Position maxHelp(List<Position> positions, Comparator<Position> c) {
        int maxIndex = 0;
        for (int i = 0; i < positions.size(); i++) {
            int result = c.compare(positions.get(i), positions.get(maxIndex));
            if (result > 0) {
                maxIndex = i;
            }
        }
        return positions.get(maxIndex);
    }

    private static class RightmostComparator<Positions> implements Comparator<Position> {
        @Override
        public int compare(Position o1, Position o2) {
            int x1 = o1.x();
            int x2 = o2.x();

            if (x1 > x2) {
                return 1;
            } else if (x1 < x2) {
                return -1;
            } else {
                return 0;
            }
        }

    }

}
