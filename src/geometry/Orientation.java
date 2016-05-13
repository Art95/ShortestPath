package geometry;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Created by Artem on 13.04.2016.
 */
public enum Orientation {
    DEFAULT,
    UP,
    DOWN,
    LEFT,
    RIGHT;

    public static Orientation randomOrientation()  {
        return ORIENTATIONS.get(RANDOM.nextInt(SIZE));
    }

    private static final List<Orientation> ORIENTATIONS =
            Collections.unmodifiableList(Arrays.asList(values()));

    private static final int SIZE = ORIENTATIONS.size();

    private static final Random RANDOM = new Random();
}
