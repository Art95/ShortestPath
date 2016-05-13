package geometry;

import main.Constants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Artem on 11.03.2016.
 */
public class Segment {
    private Point p1;
    private Point p2;

    private final int precision = 4;
    
    public Segment(Point p1, Point p2) {

        /*if (p1.equals(p2))
            throw new IllegalArgumentException("Segment with equal ends: " + p1);*/

        this.p1 = p1;
        this.p2 = p2;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Segment)) return false;

        Segment segment = (Segment) o;

        return (p1.equals(segment.p1) && p2.equals(segment.p2)) || (p1.equals(segment.p2) && p2.equals(segment.p1));

    }

    @Override
    public int hashCode() {
        int result = p1.hashCode();
        result = 31 * result + p2.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Segment{" +
                "p1 = " + p1 +
                ", p2 = " + p2 +
                '}';
    }

    public Point getFirstPoint() {
        return this.p1;
    }

    public Point getSecondPoint() {
        return this.p2;
    }

    public Point getAnotherEnd(Point p) {
        if (p.equals(p1))
            return p2;
        else if (p.equals(p2))
            return p1;
        else
            return null;
    }

    public boolean containsSegment(Segment segment) {
        if (!this.onSameLine(segment))
            return false;

        Point this_min = Point.min(p1, p2);
        Point another_min = Point.min(segment.p1, segment.p2);

        if (this_min.getX() > another_min.getX() || another_min.getX() > getAnotherEnd(this_min).getX() ||
                this_min.getX() > segment.getAnotherEnd(another_min).getX() ||
                segment.getAnotherEnd(another_min).getX() > getAnotherEnd(this_min).getX())
            return false;
        else if (this_min.getY() > another_min.getY() || another_min.getY() > getAnotherEnd(this_min).getY() ||
                this_min.getY() > segment.getAnotherEnd(another_min).getY() ||
                segment.getAnotherEnd(another_min).getY() > getAnotherEnd(this_min).getY())
            return false;
        else
            return true;
    }

    public double getLength() {
        double x = p2.getX() - p1.getX();
        double y = p2.getY() - p1.getY();

        return Math.sqrt (x * x + y * y);
    }

    public boolean hasSameDirection(Segment segment) {
        double x1 = this.getSecondPoint().getX() - this.getFirstPoint().getX();
        double y1 = this.getSecondPoint().getY() - this.getFirstPoint().getY();

        double x2 = segment.getSecondPoint().getX() - segment.getFirstPoint().getX();
        double y2 = segment.getSecondPoint().getY() - segment.getFirstPoint().getY();

        return Math.signum(x1) == Math.signum(x2) && Math.signum(y1) == Math.signum(y2) && isCollinear(segment);
    }

    private boolean isCollinear(Segment segment) {
        double x1 = this.getSecondPoint().getX() - this.getFirstPoint().getX();
        double y1 = this.getSecondPoint().getY() - this.getFirstPoint().getY();

        double x2 = segment.getSecondPoint().getX() - segment.getFirstPoint().getX();
        double y2 = segment.getSecondPoint().getY() - segment.getFirstPoint().getY();

        return Math.abs(x1 * y2 - x2 * y1) < Constants.EPS;
    }

    public Point getIntersection(Segment segment) {
        if (segment == null)
            return null;

        if (this.containsSegment(segment))
            return Point.max(segment.p1, segment.p2);
        else if (segment.containsSegment(this))
            return Point.max(p1, p2);


        double x1 = this.p1.getX();
        double y1 = this.p1.getY();
        double x2 = this.p2.getX();
        double y2 = this.p2.getY();

        double x3 = segment.p1.getX();
        double y3 = segment.p1.getY();
        double x4 = segment.p2.getX();
        double y4 = segment.p2.getY();

        double divisor = (x1 - x2) * (y3 - y4) - (y1 - y2) * (x3 - x4);

        if (divisor == 0)
            return null;

        double x = ((x1 * y2 - y1 * x2) * (x3 - x4) - (x1 - x2) * (x3 * y4 - y3 * x4)) / divisor;
        double y = ((x1 * y2 - y1 * x2) * (y3 - y4) - (y1 - y2) * (x3 * y4 - y3 * x4)) / divisor;

        Point p = new Point(new CartesianCoordinates(x, y));

        if (this.containsPoint(p) && segment.containsPoint(p))
            return p;
        else
            return null;
    }

    public boolean onSameLine(Segment segment) {
        Point a = p1;
        Point b = p2;
        Point c = segment.getFirstPoint();
        Point d = segment.getSecondPoint();

        return pointsOnSameLine(a, b, c) && pointsOnSameLine(a, b, d);
    }

    public double distanceToPoint(Point p) {
        if (p == null)
            throw new NullPointerException("Can't find distance to not existing or uninitialized point");

        double l = p1.distanceToPoint(p2);

        if (l == 0)
            return p.distanceToPoint(p1);

        double t = ((p.getX() - p1.getX()) * (p2.getX() - p1.getX()) + (p.getY() - p1.getY()) * (p2.getY() - p1.getY())) / (l * l);

        t = Math.max(0, Math.min(1, t));

        double x = p1.getX() + t * (p2.getX() - p1.getX());
        double y = p1.getY() + t * (p2.getY() - p1.getY());

        Point projection = new Point(new CartesianCoordinates(x, y));

        return p.distanceToPoint(projection);
    }

    public double distanceToSegment(Segment segment) {
        Point intersection = this.getIntersection(segment);

        final double dist_A = distanceToPoint(segment.getFirstPoint());
        final double dist_B = distanceToPoint(segment.getSecondPoint());
        final double dist_C = (intersection != null) ? this.distanceToPoint(intersection) : Double.POSITIVE_INFINITY;

        return Math.min(Math.min(dist_A, dist_B), dist_C);
    }

    public boolean containsPoint(Point p) {
        if (p1.equals(p) || p2.equals(p))
            return true;

        return (p1.distanceToPoint(p) + p2.distanceToPoint(p)) - p1.distanceToPoint(p2) < Constants.EPS;
    }

    public void setPoints(Point p1, Point p2) {
        this.p1 = p1;
        this.p2 = p2;
    }

    public boolean intersects(Segment segment) {
        return getIntersection(segment) != null;
    }

    private boolean pointsOnSameLine(Point a, Point b, Point c) {
        double temp = (a.getX() * (b.getY() - c.getY()) + b.getX() * (c.getY() - a.getY()) + c.getX() * (a.getY() - b.getY()) ) / 2;

        return temp == 0;
    }
}
