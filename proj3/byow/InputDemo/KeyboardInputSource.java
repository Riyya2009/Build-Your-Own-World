package byow.InputDemo;

/**
 * Created by hug.
 */
import edu.princeton.cs.algs4.StdDraw;

import java.io.Serializable;

public class KeyboardInputSource implements InputSource, Serializable {
    private static final boolean PRINT_TYPED_KEYS = false;
    public KeyboardInputSource() {
    }

    public char getNextKey() {
        char c = Character.toUpperCase(StdDraw.nextKeyTyped());
        if (PRINT_TYPED_KEYS) {
            System.out.print(c);
        }
        return c;
    }

    public boolean possibleNextInput() {
        return StdDraw.hasNextKeyTyped();
    }
}
