package geometry;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Created by Artem on 13.04.2016.
 */
public enum PolygonType {
    SQUARE,
    TRIANGLE,
    STAR,
    DIAMOND,
    SANDCLOCK,
    FIGURE_1;

    public static PolygonType randomType()  {
        return TYPES.get(RANDOM.nextInt(SIZE));
    }

    private static final List<PolygonType> TYPES =
            Collections.unmodifiableList(Arrays.asList(values()));

    private static final int SIZE = TYPES.size();

    private static final Random RANDOM = new Random();
}
