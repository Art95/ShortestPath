package geometry;

import utils.LinkedList;

import java.awt.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Artem on 14.03.2016.
 */
public class Polygon {
    private utils.LinkedList<Point> points;
    private utils.LinkedList<Segment> segments;

    private utils.LinkedList<Point> insertedPoints;

    private final int radius = 2;

    private Point top, left, bottom, right;

    private final int MIN_POSSIBLE_POINTS_NUMBER = 3;

    private static final Pattern pattern = Pattern.compile("\\([-+]?[0-9]*.?[0-9]+([eE][-+]?[0-9]+)?;\\s?[-+]?[0-9]*.?[0-9]+([eE][-+]?[0-9]+)?\\)");

    public Polygon() {
        this.points = new LinkedList<>();
        this.segments = new LinkedList<>();

        this.insertedPoints = new LinkedList<>();

        top = new Point();
        left = new Point();
        right = new Point();
        bottom = new Point();
    }

    public Polygon(LinkedList<Point> points) {
        if (points.size() < 3)
            throw new IllegalArgumentException("Polygon can not contain less than 3 points");

        this.points = points;
        this.segments = createSegments(points);

        this.insertedPoints = new LinkedList<>();

        findBoundaryPoints();
    }

    public static Polygon generatePolygon(PolygonType type, Area area, Orientation orientation) {
        switch (type) {
            case SQUARE:
                return generateSquare(area);
            case TRIANGLE:
                return generateTriangle(area, orientation);
            case DIAMOND:
                return generateDiamond(area);
            default:
                throw new IllegalArgumentException("Unknown polygon type " + type);
        }
    }

    public LinkedList<Point> getPoints() { return points; }

    public LinkedList<Segment> getSegments() { return segments; }

    public boolean containsPoint(Point p) {
        if (pointInsidePolygonArea(p)) {
            if (pointOnPolygon(p))
                return true;

            if (pointInsidePolygon(p))
                return true;
        }

        return false;
    }

    public boolean pointIsVertex(Point p) {
        return points.contains(p);
    }

    public boolean pointOnPolygon(Point p) {
        return pointInsidePolygonArea(p) && (pointIsVertex(p) || getHoldingSegment(p) != null);
    }

    public boolean pointInsidePolygon(Point p) {
        boolean result = false;
        Point j = points.getLast();

        for (Point i : points) {
            if ((i.getY() > p.getY()) != (j.getY() > p.getY()) &&
                    (p.getX() < (j.getX() - i.getX()) * (p.getY() - i.getY()) / (j.getY() - i.getY()) + i.getX())) {
                result = !result;
            }

            j = i;
        }

        return result;
    }

    public Segment getHoldingSegment(Point p) {
        for (Segment segment : segments) {
            if (segment.containsPoint(p))
                return segment;
        }

        return null;
    }

    public void insertPoint(Point p) {
        if (points.contains(p)) {
            Point this_p = points.get(p);

            for (Segment segment : this_p.getSegments()) {
                p.addSegment(segment);
            }

            return;
        }

        insertedPoints.add(p);

        Segment segment = getHoldingSegment(p);
        Segment prev_segment = segments.previous(segment);
        Segment next_segment = segments.next(segment);

        Point p1 = segment.getFirstPoint();
        Point p2 = segment.getSecondPoint();

        points.insertAfter(p1, p);

        segments.remove(segment);

        Segment newSegment1 = new Segment(p1, p);
        Segment newSegment2 = new Segment(p, p2);

        segments.insertAfter(prev_segment, newSegment1);
        segments.insertBefore(next_segment, newSegment2);

        p.addSegment(newSegment1);
        p.addSegment(newSegment2);
    }

    public void removeInsertedPoint(Point p) {
        if (!insertedPoints.contains(p))
            return;

        removePoint(p);

        insertedPoints.remove(p);
    }

    public void removePoint(Point p) {
        if (!points.contains(p))
            throw new IllegalArgumentException("Polygon does not contain point " + p);

        if (points.size() < MIN_POSSIBLE_POINTS_NUMBER + 1) {
            System.out.println("Can not delete point " + p +". At least " + MIN_POSSIBLE_POINTS_NUMBER + " points required.");
            return;
        }

        Point next_point = points.next(p);
        Point prev_point = points.previous(p);

        points.remove(p);

        segments.remove(new Segment(prev_point, p));
        segments.remove(new Segment(p, next_point));

        segments.insertAfter(new Segment(points.previous(prev_point), prev_point), new Segment(prev_point, next_point));
    }

    public boolean containsSegment(Segment segment) {
        return segments.contains(segment);
    }

    public Point getPreviousPoint(Point point) {
        if (points.contains(point))
            return points.previous(point);
        else
            return null;
    }

    public Point getNextPoint(Point point) {
        if (points.contains(point))
            return points.next(point);
        else
            return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Polygon)) return false;

        Polygon polygon = (Polygon) o;

        if (radius != polygon.radius) return false;
        if (points != null ? !points.equals(polygon.points) : polygon.points != null) return false;
        if (segments != null ? !segments.equals(polygon.segments) : polygon.segments != null) return false;
        if (top != null ? !top.equals(polygon.top) : polygon.top != null) return false;
        if (left != null ? !left.equals(polygon.left) : polygon.left != null) return false;
        if (bottom != null ? !bottom.equals(polygon.bottom) : polygon.bottom != null) return false;
        return right != null ? right.equals(polygon.right) : polygon.right == null;

    }

    @Override
    public int hashCode() {
        int result = points != null ? points.hashCode() : 0;
        result = 31 * result + (segments != null ? segments.hashCode() : 0);
        result = 31 * result + radius;
        result = 31 * result + (top != null ? top.hashCode() : 0);
        result = 31 * result + (left != null ? left.hashCode() : 0);
        result = 31 * result + (bottom != null ? bottom.hashCode() : 0);
        result = 31 * result + (right != null ? right.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return points.toString();
    }

    public static Polygon parsePolygon(String text) {
        Matcher matcher = pattern.matcher(text);
        LinkedList<Point> points = new LinkedList<>();

        while (matcher.find()) {
            Point p = Point.parsePoint(matcher.group(0));
            points.add(p);
        }

        if (points.size() < 3)
            throw new IllegalArgumentException("Polygon: Wrong string format! Can't parse " + text);

        return new Polygon(points);
    }

    public static Polygon randomPolygon(Area area) {
        PolygonType type = PolygonType.randomType();
        Orientation orientation = Orientation.randomOrientation();

        return generatePolygon(type, area, orientation);
    }

    public void draw(Graphics page) {
        for (Point p : points) {
            page.fillOval((int)p.getX() - radius, (int)p.getY() - radius, radius * 2, radius * 2);
        }

        Point p1, p2;

        for (Segment seg : segments) {
            p1 = seg.getFirstPoint();
            p2 = seg.getSecondPoint();

            page.drawLine((int)p1.getX(), (int)p1.getY(), (int)p2.getX(), (int)p2.getY());
        }
    }

    public Point getLeftmostPoint() {
        return this.left;
    }

    private boolean pointInsidePolygonArea(Point p) {
        return p.getX() >= left.getX() && p.getX() <= right.getX() &&
                p.getY() >= bottom.getY() && p.getY() <= top.getY();
    }

    private LinkedList<Segment> createSegments(LinkedList<Point> points) {
        LinkedList<Segment> segments = new LinkedList<>();
        Segment seg;
        Point cur = points.getFirst(), next;

        for (int i = 0; i < points.size(); ++i) {
            next = points.next(cur);

            seg = new Segment(cur, next);

            cur.addSegment(seg);
            next.addSegment(seg);

            segments.add(seg);

            cur = next;
        }

        return segments;
    }

    private void findBoundaryPoints() {
        top = points.getFirst();
        left = points.getFirst();
        right = points.getFirst();
        bottom = points.getFirst();

        for (Point p : points) {
            if (top.getY() < p.getY())
                top = p;

            if (right.getX() < p.getX())
                right = p;

            if (left.getX() > p.getX())
                left = p;

            if (bottom.getY() > p.getY())
                bottom = p;
        }
    }

    private static Polygon generateSquare(Area area) {
        int side = (int)(Math.min(area.width, area.height));

        Point p1 = new Point(new CartesianCoordinates(area.x, area.y));
        Point p2 = new Point(new CartesianCoordinates(area.x + side, area.y));
        Point p3 = new Point(new CartesianCoordinates(area.x + side, area.y + side));
        Point p4 = new Point(new CartesianCoordinates(area.x, area.y + side));

        LinkedList<Point> points = new LinkedList<>();
        points.add(p1);
        points.add(p2);
        points.add(p3);
        points.add(p4);

        return new Polygon(points);
    }

    private static Polygon generateDiamond(Area area) {
        Point p1 = new Point(new CartesianCoordinates(area.x + area.width / 2, area.y));
        Point p2 = new Point(new CartesianCoordinates(area.x + area.width, area.y + area.height / 2));
        Point p3 = new Point(new CartesianCoordinates(area.x + area.width / 2, area.y + area.height));
        Point p4 = new Point(new CartesianCoordinates(area.x, area.y + area.height / 2));

        LinkedList<Point> points = new LinkedList<>();
        points.add(p1);
        points.add(p2);
        points.add(p3);
        points.add(p4);

        return new Polygon(points);
    }

    private static Polygon generateTriangle(Area area, Orientation orientation) {
        Point p1 = new Point();
        Point p2 = new Point();
        Point p3 = new Point();

        LinkedList<Point> points = new LinkedList<>();

        if (orientation == Orientation.DEFAULT)
            orientation = Orientation.UP;

        switch (orientation) {
            case UP:
                p1.setCartesianCoordinates(new CartesianCoordinates(area.x, area.y));
                p2.setCartesianCoordinates(new CartesianCoordinates(area.x + area.width, area.y));
                p3.setCartesianCoordinates(new CartesianCoordinates(area.x + area.width / 2, area.y + area.height));
                break;
            case DOWN:
                p1.setCartesianCoordinates(new CartesianCoordinates(area.x + area.width / 2, area.y));
                p2.setCartesianCoordinates(new CartesianCoordinates(area.x + area.width, area.y + area.height));
                p3.setCartesianCoordinates(new CartesianCoordinates(area.x, area.y + area.height));
                break;
            case LEFT:
                p1.setCartesianCoordinates(new CartesianCoordinates(area.x + area.width, area.y));
                p2.setCartesianCoordinates(new CartesianCoordinates(area.x + area.width, area.y + area.height));
                p3.setCartesianCoordinates(new CartesianCoordinates(area.x, area.y + area.height / 2));
                break;
            case RIGHT:
                p1.setCartesianCoordinates(new CartesianCoordinates(area.x, area.y));
                p2.setCartesianCoordinates(new CartesianCoordinates(area.x + area.width, area.y + area.height / 2));
                p3.setCartesianCoordinates(new CartesianCoordinates(area.x, area.y + area.height));
                break;
            default:
                throw new IllegalArgumentException("Unknown orientation " + orientation);
        }

        points.add(p1);
        points.add(p2);
        points.add(p3);

        return new Polygon(points);
    }
}
