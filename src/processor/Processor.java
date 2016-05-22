package processor;

import geometry.*;
import main.Constants;
import utils.LinkedList;

import java.util.*;

/**
 * Created by Artem on 11.03.2016.
 */
public class Processor {
    private static List<Polygon> allPolygons;
    private static List<Point> allPoints;

    private static HashMap<Point, Polygon> pointPolygonMap;

    private static Polygon surroundingPolygon;
    private static final double INF = 10000000;

    private static Set<Point> pointsNotInProcess;

    public static VisibilityGraph buildVisibilityGraph(List<Polygon> polygons) {
        initialize(polygons);

        VisibilityGraph graph = new VisibilityGraph();
        HashMap<Point, Double> visiblePoints;

         for (Point v : allPoints) {
            //visiblePoints = getVisiblePointsN2(v, false);
            visiblePoints = getVisibleVertices(v, true);
            graph.addVertex(v, visiblePoints);
        }

        return graph;
    }

    public static HashMap<Point, Double> getQueryPointVisibleVertexes(Point p) {
        HashMap<Point, Double> visiblePoints = new HashMap<>();

        visiblePoints.putAll(getVisibleVertices(p, true));
        visiblePoints.putAll(getVisibleVertices(p, false));

        return visiblePoints;
    }

    private static HashMap<Point, Double> getVisibleVertices(Point v, boolean rightSide) {
        HashMap<Point, Double> visiblePoints = new HashMap<>();
        List<Point> points = new ArrayList<>();
        pointsNotInProcess = new HashSet<>();

        Segment ray = new Segment(v, new Point(new CartesianCoordinates(v.getX(), INF)));

        PointAngleComparator pointComparator = new PointAngleComparator(v, rightSide);
        //TreeSet<Segment> status = new TreeSet<>(new SegmentDistanceComparator2(ray, pointComparator));
        TreeSet<Segment> status = new TreeSet<>(new SegmentDistanceComparator2(v, pointComparator));
        //List<Segment> segments = new ArrayList<>();

        /*for (Polygon polygon : allPolygons) {
            for (Segment seg : polygon.getSegments())
                segments.add(seg);
        }*/

        /*Collections.sort(segments, new SegmentDistanceComparator2(v, pointComparator));

        Map<Segment, Integer> segmentOrderSegments = new HashMap<>();
        Map<Integer, Segment> segmentOrderIntegers = new HashMap<>();*/

        /*for (int i = 0; i < segments.size(); ++i) {
            segmentOrderSegments.put(segments.get(i), i);
            segmentOrderIntegers.put(i, segments.get(i));
        }*/

        //HashSet<Segment> status = new HashSet<>();

        /*for (Polygon polygon : allPolygons) {
            for (Segment seg : polygon.getSegments())
                if (ray.intersects(seg) && !v.getSegments().contains(seg))
                    status.add(segmentOrderSegments.get(seg));
        }*/

        for (Point p : allPoints) {
            if (!p.equals(v)){
                if (rightSide &&  v.getX() <= p.getX())
                    points.add(p);
                else if (!rightSide && v.getX() >= p.getX())
                    points.add(p);
                else
                    pointsNotInProcess.add(p);
            }
        }

        for (Polygon polygon : allPolygons) {
            for (Segment seg : polygon.getSegments()) {
                if (ray.intersects(seg) && !v.getSegments().contains(seg))
                    status.add(seg);
            }
        }

        try {
            Collections.sort(points, pointComparator);
        } catch (Exception e) {
            System.out.println("Sort failed");
        }


        for (Point p : points) {
            ray.setPoints(v, p);

            try {

                if (status.isEmpty() && !isInteriorDiagonal(ray)) {
                    visiblePoints.put(p, v.distanceToPoint(p));
                } else if (!status.isEmpty()) {
                    Segment closestSegment = getClosestSegment1(status, ray, pointComparator);
                    //Segment closestSegment = getClosestSegment(status, ray, pointComparator);
                    Point intersection = ray.getIntersection(closestSegment);
                    //Integer closestSegmentIndex = status.first();
                    //Point intersection = ray.getIntersection(segmentOrderIntegers.get(closestSegmentIndex));

                    if (intersection == null && !isInteriorDiagonal(ray))
                        visiblePoints.put(p, v.distanceToPoint(p));

                    if (intersection != null && (intersection.equals(p)) && !isInteriorDiagonal(ray))
                        visiblePoints.put(p, v.distanceToPoint(p));
                }

                Set<Segment> pointSegments = p.getSegments();

                for (Segment seg : pointSegments) {
                    if (!segmentStartsInPoint(seg, p, v, rightSide)) {
                        status.remove(seg);
                    }
                }

                for (Segment seg : pointSegments) {
                    if (segmentStartsInPoint(seg, p, v, rightSide)) {
                        status.add(seg);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        for (Segment seg : v.getSegments()) {
            Point p = seg.getAnotherEnd(v);
            visiblePoints.put(p, v.distanceToPoint(p));
        }

        pointsNotInProcess.clear();

        return visiblePoints;
    }

    public static HashMap<Point, Double> getVisiblePointsN2(Point v, boolean isQueryPoint) {
        HashMap<Point, Double> visiblePoints = new HashMap<>();
        boolean intersects;
        Segment vw;
        Point intersection;

        for (Point w : allPoints) {
            intersects = false;

            if (!v.equals(w) && ((!isQueryPoint && v.getX() <= w.getX()) || isQueryPoint)) {
                vw = new Segment(v, w);

                for (Polygon polygon : allPolygons) {
                    for (Segment seg : polygon.getSegments()) {
                        intersection = vw.getIntersection(seg);

                        if (intersection != null && !intersection.equals(v) && !intersection.equals(w)) {
                            intersects = true;
                            break;
                        }
                    }
                }

                if (intersects || isInteriorDiagonal(vw))
                    continue;

                visiblePoints.put(w, v.distanceToPoint(w));
            }
        }

        return visiblePoints;
    }

    public static boolean insertQueryPoint(Point p) {
        Polygon polygon = getHoldingPolygon(p);

        for (Polygon poly : allPolygons) {
            if ((poly.pointInsidePolygon(p) || poly.pointOnPolygon(p)) && !poly.equals(surroundingPolygon))
                return false;
        }

        if (polygon == null) {
            pointPolygonMap.put(p, new Polygon());
        } else {
            if (polygon.pointOnPolygon(p)) {
                polygon.insertPoint(p);
                pointPolygonMap.put(p, polygon);
            } else if (polygon.pointInsidePolygon(p) && !polygon.equals(surroundingPolygon))
                return false;
        }

        allPoints.add(p);

        return true;
    }

    public static void removeQueryPoint(Point p) {
        if (!pointPolygonMap.containsKey(p))
            return;

        Polygon polygon = pointPolygonMap.get(p);

        if (!polygon.equals(new Polygon())) {
            polygon.removeInsertedPoint(p);
        }

        pointPolygonMap.remove(p);

        allPoints.remove(allPoints.lastIndexOf(p));
    }

    private static void initialize(List<Polygon> polygons) {
        allPolygons = polygons;
        allPoints = new ArrayList<>();
        pointPolygonMap = new HashMap<>();

        LinkedList<Point> points;

        Point minPoint = null;

        for (Polygon polygon : allPolygons) {
            points = polygon.getPoints();

            for (Point p : points) {
                allPoints.add(p);
                pointPolygonMap.put(p, polygon);
            }

            Point leftmost = polygon.getLeftmostPoint();
            Point mPoint = Point.min(minPoint, leftmost);

            if (!mPoint.equals(minPoint)) {
                surroundingPolygon = polygon;
                minPoint = mPoint;
            }
        }
    }

    public static boolean isPointInsideSurroundingPolygon(Point p) {
        return surroundingPolygon.containsPoint(p);
    }

    private static boolean isInteriorDiagonal(Segment vw) {
        Point v = vw.getFirstPoint();
        Point w = vw.getSecondPoint();

        if (!onSamePolygon(v, w))
            return false;

        Polygon polygon = getHoldingPolygon(v);

        if (polygon == null)
            return false;

        if (!polygon.pointIsVertex(v) && polygon.pointOnPolygon(v)) {
            polygon.insertPoint(v);
        }

        Point v_next = polygon.getNextPoint(v);
        Point v_prev = polygon.getPreviousPoint(v);

        Point w_next = polygon.getNextPoint(w);
        Point w_prev = polygon.getPreviousPoint(w);

        boolean vRightTurn = isRightTurn(v_next, v, w) || noTurn(v_next, v, w);
        boolean vLeftTurn = isLeftTurn(v_prev, v, w) || noTurn(v_prev, v, w);

        boolean wRightTurn = isRightTurn(w_next, w, v) || noTurn(w_next, w, v);
        boolean wLeftTurn = isLeftTurn(w_prev, w, v) || noTurn(w_prev, w, v);

        return (vRightTurn && vLeftTurn) || (wRightTurn && wLeftTurn);
    }

    private static boolean isLeftTurn(Point b, Point a, Point c) {
        return getTurn(b, a, c) > 0;
    }

    private static boolean isRightTurn(Point b, Point a, Point c) {
        return getTurn(b, a, c) < 0;
    }

    private static boolean noTurn(Point b, Point a, Point c) {
        return getTurn(b, a, c) == 0;
    }

    private static double getTurn(Point b, Point a, Point c) {
        return (c.getX() - a.getX()) * (b.getY() - a.getY()) - (c.getY() - a.getY()) * (b.getX() - a.getX());
    }

    private static boolean onSamePolygon(Point v, Point w) {
        if (!pointPolygonMap.containsKey(v) || !pointPolygonMap.containsKey(w))
            return false;

        Polygon v_polygon = (pointPolygonMap.containsKey(v)) ? pointPolygonMap.get(v) : getHoldingPolygon(v);
        Polygon w_polygon = (pointPolygonMap.containsKey(w)) ? pointPolygonMap.get(w) : getHoldingPolygon(w);

        if (v_polygon == null || w_polygon == null)
            return false;

        if (!v_polygon.equals(w_polygon))
            return false;

        return v_polygon.pointOnPolygon(v) && w_polygon.pointOnPolygon(w);

    }

    private static Polygon getHoldingPolygon(Point p) {
        if (pointPolygonMap.containsKey(p))
            return pointPolygonMap.get(p);

        for (Polygon polygon : allPolygons) {
            if (polygon.containsPoint(p)) {
                return polygon;
            }
        }

        return null;
    }

    private static class SegmentDistanceComparator2 implements Comparator<Segment> {
        private Point basePoint;
        private PointAngleComparator pComparator;

        public SegmentDistanceComparator2(Point basePoint, PointAngleComparator pointComparator) {
            this.basePoint = basePoint;
            this.pComparator = pointComparator;
        }

        @Override
        public int compare(Segment segment1, Segment segment2) {
            if (segment1.equals(segment2))
                return 0;

            Point segment1Start = (pComparator.compare(segment1.getFirstPoint(), segment1.getSecondPoint()) < 0) ?
                    segment1.getFirstPoint() : segment1.getSecondPoint();
            Point segment1End = segment1.getAnotherEnd(segment1Start);

            Point segment2Start = (pComparator.compare(segment2.getFirstPoint(), segment2.getSecondPoint()) < 0) ?
                    segment2.getFirstPoint() : segment2.getSecondPoint();
            Point segment2End = segment2.getAnotherEnd(segment2Start);
            
            if (segment1Start.equals(segment2Start)) { //start in one point
                //return isRightTurn(segment1End, segment2Start, segment2End) ? -1 : 1;
                if (isRightTurn(segment1End, segment2Start, segment2End)) {
                    return pComparator.isClockwise() ? -1 : 1;
                } else {
                    return pComparator.isClockwise() ? 1 : -1;
                }
            }

            if (segment1End.equals(segment2End)) { //finish in one point
                //return isRightTurn(segment2Start, segment2End, segment1Start) ? -1 : 1;

                if (isRightTurn(segment2Start, segment2End, segment1Start)) {
                    return pComparator.isClockwise() ? -1 : 1;
                } else {
                    return pComparator.isClockwise() ? 1 : -1;
                }
            }

            //if collinear, then compare segment1Start and segment2Start
            if (noTurn(basePoint, segment1Start, segment2Start)) {
                return collinear_are_ordered_along_line(basePoint, segment1Start, segment2Start);
            }

            //a < b  if  segment2Start is behind a  or  b begins before a && segment2End is behind a
            if (pComparator.isClockwise()) {
                if ((isRightTurn(basePoint, segment1Start, segment2Start) && isRightTurn(segment1End, segment1Start, segment2Start)) ||
                        (isLeftTurn(basePoint, segment1Start, segment2Start) && isRightTurn(segment2Start, segment2End, segment1Start))) {
                    return -1;
                    //return pComparator.isClockwise() ? -1 : 1;
                } else {
                    return 1;
                    //return pComparator.isClockwise() ? 1 : -1;
                }
            } else {
                if (isRightTurn(basePoint, segment1Start, segment2Start) && isLeftTurn(segment2Start, segment2End, segment1Start)) {
                    return -1;
                } else if (isLeftTurn(basePoint, segment1Start, segment2Start) && isLeftTurn(segment1Start, segment2Start, segment1End)) {
                    return -1;
                } else {
                    return 1;
                }
            }
        }

        int collinear_are_ordered_along_line(Point a, Point b, Point c) {
            if ((Point.compareByCoordinates(a, b) <= 0 && Point.compareByCoordinates(b, c) <= 0) ||
                    (Point.compareByCoordinates(c, b) <= 0 && Point.compareByCoordinates(b, a) <= 0)) {
                return -1;
            } else {
                return 1;
            }
        }
    }

    private static class SegmentDistanceComparator1 implements Comparator<Segment> {
        private PointAngleComparator pointComparator;
        private Point basePoint;

        public SegmentDistanceComparator1(Point basePoint, PointAngleComparator pointComparator) {
            this.pointComparator = pointComparator;
            this.basePoint = basePoint;
        }

        @Override
        public int compare(Segment segment1, Segment segment2) {
            if (segment1.equals(segment2))
                return 0;

            Point seg1Point = (pointComparator.compare(segment1.getFirstPoint(), segment1.getSecondPoint()) < 0) ?
                    segment1.getFirstPoint() : segment1.getSecondPoint();

            Point seg2Point = (pointComparator.compare(segment2.getFirstPoint(), segment2.getSecondPoint()) < 0) ?
                    segment2.getFirstPoint() : segment2.getSecondPoint();

            double dist1 = basePoint.distanceToPoint(seg1Point);
            double dist2 = basePoint.distanceToPoint(seg2Point);

            return (int)(dist1 - dist2);
        }
    }

    private static class SegmentDistanceComparator implements Comparator<Segment> {
        private Segment baseSegment;
        private PointAngleComparator pComparator;

        private final double EPS = 0.0001;

        public SegmentDistanceComparator(Segment intersectingSegment, PointAngleComparator pComparator) {
            this.baseSegment = intersectingSegment;
            this.pComparator = pComparator;
        }

        @Override
        public int compare(Segment segment1, Segment segment2) {
            if (segment1.equals(segment2))
                return 0;

            Point basePoint = baseSegment.getFirstPoint();

            Segment ray = getCompareRay(basePoint, baseSegment.getAnotherEnd(basePoint));

            Point seg1_intersection = segment1.getIntersection(ray);
            Point seg2_intersection = segment2.getIntersection(ray);

            if (seg1_intersection == null && seg2_intersection == null) {
                return 0;
            } else if (seg1_intersection == null || seg2_intersection == null)
                return (seg1_intersection == null) ? 1 : -1;

            Double dist1 = basePoint.distanceToPoint(seg1_intersection);
            Double dist2 = basePoint.distanceToPoint(seg2_intersection);

            if (Math.abs(dist1 - dist2) < EPS) {
                if (segment1.intersects(new Segment(segment2.getFirstPoint(), basePoint))
                        && segment1.intersects(new Segment(segment2.getSecondPoint(), basePoint))) {
                    return -1;
                }

                if (segment2.intersects(new Segment(segment1.getFirstPoint(), basePoint))
                        && segment2.intersects(new Segment(segment1.getSecondPoint(), basePoint))) {
                    return 1;
                }

                Point seg1AnotherPoint = segment1.getAnotherEnd(seg1_intersection);
                Point seg2AnotherPoint = segment2.getAnotherEnd(seg2_intersection);

                try {
                    return pComparator.compare(seg2AnotherPoint, seg1AnotherPoint);
                } catch (Exception ex) {
                    System.out.println(seg1_intersection + " " + seg2_intersection);
                    System.out.println(segment1 + " " + seg1AnotherPoint);
                    System.out.println(segment2 + " " + seg2AnotherPoint);
                }
            }

            return dist1 < dist2 ? -1 : 1;
        }

        /* Gets infinite ray that has direction equal to baseSegment direction
            params:
                basePoint - point for which we are finding visible points
                baseSegmentAnotherEnd - point that is being checked if it is visible from basePoint or not
            returns:
                ray that has same direction with baseSegment and starts in basePoint
         */
        private Segment getCompareRay(Point basePoint, Point baseSegmentAnotherEnd) {
            /* Shift anotherEnd point as if basePoint was the center of coordinate system
              to find its angle counterclockwise
             */
            double temp_x = baseSegmentAnotherEnd.getX() - basePoint.getX();
            double temp_y = baseSegmentAnotherEnd.getY() - basePoint.getY();

            Point temp = new Point(new CartesianCoordinates(temp_x, temp_y)); // shifted anotherEnd point

            // point with infinite radius and counterclockwise angle of anotherPoint to set ray direction
            Point rayDirection = new Point(new PolarCoordinates(temp.getAngle(), INF));

            double newX = basePoint.getX() + rayDirection.getX();
            double newY = basePoint.getY() + rayDirection.getY();

            Point rayEnd = new Point(new CartesianCoordinates(newX, newY));

            return new Segment(basePoint, rayEnd);
        }
    }

    private static class PointAngleComparator implements Comparator<Point> {
        private Point basePoint;
        private boolean clockwise;

        public PointAngleComparator(Point basePoint, boolean clockwise) {
            this.basePoint = basePoint;
            this.clockwise = clockwise;
        }

        @Override
        public int compare(Point p1, Point p2) {
            if (p1.equals(p2))
                return 0;

            if (p1.equals(basePoint))
                return -1;

            if (p2.equals(basePoint))
                return 1;

            if (pointsNotInProcess.contains(p1) && pointsNotInProcess.contains(p2)) {
                if (p1.getY() > basePoint.getY() && p2.getY() < basePoint.getY()) {
                    return -1;
                } else if (p1.getY() < basePoint.getY() && p2.getY() > basePoint.getY()) {
                    return 1;
                } else if (p1.getY() > basePoint.getY() && p2.getY() > basePoint.getY()){
                    if (p1.getX() < p2.getY()) {
                        return clockwise ? -1 : 1;
                    } else if (p1.getX() > p2.getX()) {
                        return clockwise ? 1 : -1;
                    } else {
                        return (p1.getY() < p2.getY()) ? -1 : 1;
                    }
                }
            } else if (pointsNotInProcess.contains(p1)) {
                if (p1.getY() > basePoint.getY()) {
                    return -1;
                } else {
                    return 1;
                }
            } else if (pointsNotInProcess.contains(p2)) {
                if (p2.getY() > basePoint.getY()) {
                    return 1;
                } else {
                    return -1;
                }
            }

            Segment s1 = new Segment(basePoint, p1);
            Segment s2 = new Segment(basePoint, p2);

            int sgn = sgnMultiplySegments(s1, s2);

            double x1 = s1.getSecondPoint().getX() - s1.getFirstPoint().getX();
            double y1 = s1.getSecondPoint().getY() - s1.getFirstPoint().getY();

            double x2 = s2.getSecondPoint().getX() - s2.getFirstPoint().getX();
            double y2 = s2.getSecondPoint().getY() - s2.getFirstPoint().getY();

            if (sgn == 0) {
                if (s1.hasSameDirection(s2)) {
                    double d1 = Math.abs(x1) + Math.abs(y1);
                    double d2 = Math.abs(x2) + Math.abs(y2);

                    if (Math.abs(d1 - d2) < Constants.EPS) {
                        return 0;
                    }

                    return d1 < d2 ? -1 : 1;
                } else {
                    return x1 >= 0 != !clockwise ? -1 : 1;
                }
            }

            if (y1 > 0 && Math.abs(x1) < Constants.EPS) {
                return -1;
            }

            if (y2 > 0 && Math.abs(x2) < Constants.EPS) {
                return 1;
            }

            int sgn1 = (int) Math.signum(x1);
            int sgn2 = (int) Math.signum(x2);

            if (sgn1 == sgn2) {
                return !clockwise ? -sgn : sgn;
            }

            return sgn1 < sgn2 == !clockwise ? -1 : 1;
        }

        public boolean isClockwise() {
            return clockwise;
        }

        private int sgnMultiplySegments(Segment s1, Segment s2) {
            double x1 = s1.getSecondPoint().getX() - s1.getFirstPoint().getX();
            double y1 = s1.getSecondPoint().getY() - s1.getFirstPoint().getY();

            double x2 = s2.getSecondPoint().getX() - s2.getFirstPoint().getX();
            double y2 = s2.getSecondPoint().getY() - s2.getFirstPoint().getY();

            double multiplication = x1 * y2 - y1 * x2;

            if (Math.abs(multiplication) < Constants.EPS) {
                return 0;
            }

            if (multiplication < 0) {
                return -1;
            }

            return 1;
        }
    }

    private static Segment getClosestSegment(HashSet<Segment> status, Segment baseSegment, PointAngleComparator pointAngleComparator) {
        SegmentDistanceComparator comparator = new SegmentDistanceComparator(baseSegment, pointAngleComparator);
        Segment closest = null;


        for (Segment seg : status) {
            if (closest == null || comparator.compare(closest, seg) > 0)
                closest = seg;
        }

        return closest;
    }

    private static Segment getClosestSegment1(Set<Segment> status, Segment baseSegment, PointAngleComparator pointAngleComparator) {
        SegmentDistanceComparator comparator = new SegmentDistanceComparator(baseSegment, pointAngleComparator);
        Segment closest = null;
        int counter = 0;

        for (Segment seg : status) {
            if (closest == null || comparator.compare(closest, seg) > 0)
                closest = seg;

            if (counter == 1)
               break;

            ++counter;
        }

        return closest;
    }

    private static boolean segmentStartsInPoint(Segment segment, Point p, Point basePoint, boolean clockwise) {
        Point anotherEnd = segment.getAnotherEnd(p);

        if (anotherEnd == null)
            return false;

        if (clockwise && isLeftTurn(basePoint, anotherEnd, p)) {
            return true;
        } else if (!clockwise && isRightTurn(basePoint, anotherEnd, p)) {
            return true;
        } else if (noTurn(basePoint, anotherEnd, p)) {
            if (basePoint.distanceToPoint(p) < basePoint.distanceToPoint(anotherEnd))
                return true;
        }

        return false;
    }
}
