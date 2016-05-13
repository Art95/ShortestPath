package geometry;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Artem on 11.03.2016.
 */
public class Point {
    CartesianCoordinates cartesianCoordinates;
    PolarCoordinates polarCoordinates;

    private Set<Segment> segments;

    public Point() {
        cartesianCoordinates = new CartesianCoordinates();
        polarCoordinates = new PolarCoordinates();

        segments = new HashSet<>();
    }

    public Point(CartesianCoordinates cartesianCoordinates) {
        setCartesianCoordinates(cartesianCoordinates);
        segments = new HashSet<>();
    }

    public Point(PolarCoordinates polarCoordinates) {
        setPolarCoordinates(polarCoordinates);
        segments = new HashSet<>();
    }

    public void setCartesianCoordinates(CartesianCoordinates cartesianCoordinates) {
        this.cartesianCoordinates = cartesianCoordinates;
        this.polarCoordinates = cartesianCoordinates.toPolarCoordinates();
    }

    public void setPolarCoordinates(PolarCoordinates polarCoordinates) {
        this.polarCoordinates = polarCoordinates;
        this.cartesianCoordinates = polarCoordinates.toCartesianCoordinates();
    }

    public double getX() {
        return cartesianCoordinates.getX();
    }

    public double getY() {
        return cartesianCoordinates.getY();
    }

    public double getRadius() {
        return polarCoordinates.getRadius();
    }

    public double getAngle() {
        return polarCoordinates.getAngle();
    }

    public Set<Segment> getSegments() {
        return this.segments;
    }

    public void addSegment(Segment segment) {
        if (segments.size() < 2)
            segments.add(segment);
    }

    /*public void removeSegment(Segment segment) {
        this.segments.remove(segment);
    }*/

    @Override
    public String toString() {
        //return "{" + cartesianCoordinates +  ", " + polarCoordinates +  '}';
        return cartesianCoordinates.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Point)) return false;

        Point point = (Point) o;

        return cartesianCoordinates.equals(point.cartesianCoordinates);

    }

    @Override
    public int hashCode() {
        int result = cartesianCoordinates.hashCode();
        result = 31 * result + polarCoordinates.hashCode();
        return result;
    }

    public double distanceToPoint(Point p) {
        return Math.sqrt((p.getX() - this.getX()) * (p.getX() - this.getX()) +
                (p.getY() - this.getY()) * (p.getY() - this.getY()));
    }

    /*public void clearSegments() {
        segments.clear();
    }*/

    public static Point min(Point p1, Point p2) {
        if (p1 == null)
            return p2;
        else if (p2 == null)
            return p1;

        if (p1.getX() < p2.getX())
            return p1;
        else if (p2.getX() < p1.getX())
            return p2;
        else {
            return (p1.getY() < p2.getY()) ? p1 : p2;
        }
    }

    public static Point max(Point p1, Point p2) {
        if (p1.getX() > p2.getX())
            return p1;
        else if (p2.getX() > p1.getX())
            return p2;
        else {
            return (p1.getY() > p2.getY()) ? p1 : p2;
        }
    }

    public static Point parsePoint(String text) {
        return new Point(CartesianCoordinates.parseCartesianCoordinates(text));
    }
}
