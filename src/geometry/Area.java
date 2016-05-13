package geometry;

/**
 * Created by Artem on 13.04.2016.
 */
public class Area {
    public double x, y;
    public double width, height;

    public Area() {
        x = 0.0;
        y = 0.0;
        width = 0.0;
        height = 0.0;
    }

    public Area(double x, double y, double width, double height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public Area(Point p, double width, double height) {
        this.x = p.getX();
        this.y = p.getY();
        this.width = width;
        this.height = height;
    }
}
