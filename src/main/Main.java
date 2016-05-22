package main;

import drawing.MainForm;
import drawing.Painter;
import geometry.*;
import processor.Processor;
import utils.LinkedList;
import processor.VisibilityGraph;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Created by Artem on 11.03.2016.
 */
public class Main {
    public static void main(String[] args) {
        MainForm form = new MainForm();

        //manualDebug();
        //automatedDebug();
    }

    private static void manualDebug() {
        Point point1 = new Point(new CartesianCoordinates(300, 150));
        Point point2 = new Point(new CartesianCoordinates(400, 150));
        Point point3 = new Point(new CartesianCoordinates(450, 100));
        Point point4 = new Point(new CartesianCoordinates(500, 150));
        Point point5 = new Point(new CartesianCoordinates(450, 200));
        Point point6 = new Point(new CartesianCoordinates(500, 300));
        Point point7 = new Point(new CartesianCoordinates(350, 300));
        Point point8 = new Point(new CartesianCoordinates(350, 250));
        Point point9 = new Point(new CartesianCoordinates(400, 250));
        Point point10 = new Point(new CartesianCoordinates(400, 200));
        Point point11 = new Point(new CartesianCoordinates(350, 200));

        LinkedList<Point> points = new LinkedList<>();
        points.add(point1);
        points.add(point2);
        points.add(point3);
        points.add(point4);
        points.add(point5);
        points.add(point6);
        points.add(point7);
        points.add(point8);
        points.add(point9);
        points.add(point10);
        points.add(point11);


        Point ppoint1 = new Point(new CartesianCoordinates(450, 500));
        Point ppoint2 = new Point(new CartesianCoordinates(600, 500));
        Point ppoint3 = new Point(new CartesianCoordinates(550, 550));
        Point ppoint4 = new Point(new CartesianCoordinates(550, 600));
        Point ppoint5 = new Point(new CartesianCoordinates(600, 600));
        Point ppoint6 = new Point(new CartesianCoordinates(550, 650));
        Point ppoint7 = new Point(new CartesianCoordinates(400, 650));
        Point ppoint8 = new Point(new CartesianCoordinates(400, 550));
        Point ppoint9 = new Point(new CartesianCoordinates(450, 600));
        Point ppoint10 = new Point(new CartesianCoordinates(500, 550));

        LinkedList<Point> ppoints = new LinkedList<>();
        ppoints.add(ppoint1);
        ppoints.add(ppoint2);
        ppoints.add(ppoint3);
        ppoints.add(ppoint4);
        ppoints.add(ppoint5);
        ppoints.add(ppoint6);
        ppoints.add(ppoint7);
        ppoints.add(ppoint8);
        ppoints.add(ppoint9);
        ppoints.add(ppoint10);

        LinkedList<Point> surroundingPoints = new LinkedList<>();
        Point sPoint1 = new Point(new CartesianCoordinates(5, 5));
        Point sPoint2 = new Point(new CartesianCoordinates(1220, 5));
        Point sPoint3 = new Point(new CartesianCoordinates(1220, 200));
        Point sPoint4 = new Point(new CartesianCoordinates(1100, 300));
        Point sPoint5 = new Point(new CartesianCoordinates(1220, 500));
        Point sPoint6 = new Point(new CartesianCoordinates(1220, 700));
        Point sPoint7 = new Point(new CartesianCoordinates(5, 700));
        Point sPoint8 = new Point(new CartesianCoordinates(5, 400));
        Point sPoint9 = new Point(new CartesianCoordinates(100, 400));
        Point sPoint10 = new Point(new CartesianCoordinates(100, 200));
        Point sPoint11 = new Point(new CartesianCoordinates(5, 200));

        surroundingPoints.add(sPoint1);
        surroundingPoints.add(sPoint2);
        surroundingPoints.add(sPoint3);
        surroundingPoints.add(sPoint4);
        surroundingPoints.add(sPoint5);
        surroundingPoints.add(sPoint6);
        surroundingPoints.add(sPoint7);
        surroundingPoints.add(sPoint8);
        surroundingPoints.add(sPoint9);
        surroundingPoints.add(sPoint10);
        surroundingPoints.add(sPoint11);

        Polygon surrounding = new Polygon(surroundingPoints);

        List<Polygon> polygons = new ArrayList<>();
        polygons.add(surrounding);
        polygons.add(new Polygon(points));
        polygons.add(new Polygon(ppoints));

        /*Polygon square1 = Polygon.generatePolygon(PolygonType.SQUARE, new Area(0, 0, 500, 500), Orientation.DEFAULT);
        //Polygon square2 = Polygon.generatePolygon(PolygonType.SQUARE, new Area(100, 100, 300, 300), Orientation.DEFAULT);
        List<Polygon> polygons = new ArrayList<>();
        polygons.add(square1);
        polygons.add(square2);*/

        /*Polygon sPoly = Polygon.generatePolygon(PolygonType.SQUARE, new Area(0, 0, 700, 700), Orientation.DEFAULT);
        Polygon square = Polygon.generatePolygon(PolygonType.SQUARE, new Area(400, 400, 100, 100), Orientation.DEFAULT);
        Polygon diam = Polygon.generatePolygon(PolygonType.DIAMOND, new Area(100, 100, 200, 200), Orientation.DEFAULT);
        List<Polygon> polygons = new ArrayList<>();
        polygons.add(sPoly);
        polygons.add(square);
        polygons.add(diam);*/

        Painter painter = new Painter();
        painter.drawPolygons(polygons);

        VisibilityGraph graph = Processor.buildVisibilityGraph(polygons);

        Point start = new Point(new CartesianCoordinates(10, 10));
        Point end = new Point(new CartesianCoordinates(600, 200));

        //Point p = new Point(new CartesianCoordinates(200, 300));
        //Point p = end;
        //painter.drawVisibilitySegment(p, graph.getVisibleVertexes(p));


        List<Point> path = graph.getShortestPath(start, end);

        if (path == null)
            System.out.println("Point inside obstacle");
        else
            painter.drawPath(path);

        painter.drawVisibilitySegment(start, graph.getVisibleVertexes(start));
        painter.drawVisibilitySegment(end, graph.getVisibleVertexes(end));

        graph.removeQueryPoints(start, end);
    }

    private static void automatedDebug() {
        List<Polygon> obstacles = generateRandomObstacles();

        VisibilityGraph graph = Processor.buildVisibilityGraph(obstacles);

        System.out.println("Built!");

        Painter painter = new Painter();
        painter.drawPolygons(obstacles);


        Point start = new Point(new CartesianCoordinates(55, 160));
        Point end = new Point(new CartesianCoordinates(1100, 600));

        List<Point> path = graph.getShortestPath(start, end);

        if (path == null)
            System.out.println("Point inside obstacle");
        else
            painter.drawPath(path);

        graph.removeQueryPoints(start, end);
    }

    private static List<Polygon> generateRandomObstacles() {
        int numberOfObstacles = Constants.MAX_NUMBER_OF_OBSTACLES;

        List<Polygon> obstacles = new ArrayList<>();

        obstacles.add(generateSurroundingPolygon());

        int areasNumber = (Constants.PANEL_HEIGHT * Constants.PANEL_WIDTH) /
                (Constants.BLOCK_HEIGHT * Constants.BLOCK_WIDTH) - (Constants.PANEL_WIDTH / Constants.BLOCK_WIDTH) -
                (Constants.PANEL_HEIGHT / Constants.BLOCK_HEIGHT);

        Set<Integer> chosenAreas = chooseAreasForObstacles(areasNumber, numberOfObstacles);
        int currentArea = 0;

        for (int x = Constants.BLOCK_WIDTH; x + Constants.BLOCK_WIDTH < Constants.PANEL_WIDTH; x += Constants.BLOCK_WIDTH) {
            for (int y = Constants.BLOCK_HEIGHT; y + Constants.BLOCK_HEIGHT < Constants.PANEL_HEIGHT; y += Constants.BLOCK_HEIGHT) {
                if (chosenAreas.contains(currentArea)) {
                    Area area = new Area(x + Constants.PADDING, y + Constants.PADDING,
                            Constants.BLOCK_WIDTH - Constants.PADDING, Constants.BLOCK_HEIGHT - Constants.PADDING);
                    Polygon polygon = Polygon.randomPolygon(area);
                    obstacles.add(polygon);
                }

                ++currentArea;
            }
        }

        return obstacles;
    }

    private static Polygon generateSurroundingPolygon() {
        Point p1 = new Point(new CartesianCoordinates(Constants.PADDING, Constants.PADDING));
        Point p2 = new Point(new CartesianCoordinates(Constants.PANEL_WIDTH - Constants.PADDING, Constants.PADDING));
        Point p3 = new Point(new CartesianCoordinates(Constants.PANEL_WIDTH - Constants.PADDING, Constants.PANEL_HEIGHT - Constants.PADDING));
        Point p4 = new Point(new CartesianCoordinates(Constants.PADDING, Constants.PANEL_HEIGHT - Constants.PADDING));

        LinkedList<Point> points = new LinkedList<>();
        points.add(p1);
        points.add(p2);
        points.add(p3);
        points.add(p4);

        return new Polygon(points);
    }

    private static Set<Integer> chooseAreasForObstacles(int areasNumber, int obstaclesNumber) {
        Set<Integer> chosen = new HashSet<>();
        Random rand = new Random();
        Integer candidate;

        while (chosen.size() < obstaclesNumber) {
            candidate = rand.nextInt(areasNumber);
            chosen.add(candidate);
        }

        return chosen;
    }

    private static void saveField(List<Polygon> polygons, String address) {
        try {
            List<String> lines = obstaclesToString(polygons);
            Path file = Paths.get(address);
            Files.write(file, lines, Charset.forName("UTF-8"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static List<String> obstaclesToString(List<Polygon> polygons) {
        List<String> lines = new ArrayList<>();

        for (Polygon polygon : polygons) {
            lines.add(polygon.toString());
        }

        return lines;
    }
}
