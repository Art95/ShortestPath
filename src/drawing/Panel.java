package drawing;

import geometry.Point;
import geometry.Polygon;
import main.Constants;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

/**
 * Created by Artem on 25.03.2016.
 */
public class Panel extends JPanel {
    private List<Polygon> polygons;
    private List<Point> path;

    private HashMap<Point, List<Point>> visibilitySegments;

    private final int radius = 2;

    public Panel() {
        setPreferredSize(new Dimension(Constants.PANEL_WIDTH, Constants.PANEL_HEIGHT));
        polygons = new ArrayList<>();
        path = new ArrayList<>();
        visibilitySegments = new HashMap<>();
    }

    public void addPolygons(List<Polygon> polygons) {
        this.polygons = polygons;
    }

    public void addPath(List<geometry.Point> path) {
        this.path = path;
    }

    public void addVisibleVertexes(Point p, HashMap<Point, Double> vertexes) {
        List<Point> visibleVertexes = new ArrayList<>(vertexes.keySet());

        visibilitySegments.put(p, visibleVertexes);
    }

    public void clearPath() {
        path.clear();
    }

    public void paintComponent(Graphics page) {
        //super.paintComponent(page);

        Graphics2D g2d = (Graphics2D) page;
        g2d.translate(0, Constants.PANEL_HEIGHT);
        g2d.scale(1.0, -1.0);

        Random rand = new Random();

        for (Polygon poly : polygons) {
            poly.draw(page);
        }


        if (path.size() == 1) {
            page.setColor(Color.RED);

            Point p1 = path.get(0);
            page.fillOval((int)p1.getX() - radius, (int)p1.getY() - radius, radius * 2, radius * 2);

            page.setColor(Color.BLACK);
        }

        for (int i = 0; i < path.size() - 1; ++i) {
            page.setColor(Color.RED);
            Point p1 = path.get(i);
            Point p2 = path.get(i + 1);

            page.fillOval((int)p1.getX() - radius, (int)p1.getY() - radius, radius * 2, radius * 2);
            page.fillOval((int)p2.getX() - radius, (int)p2.getY() - radius, radius * 2, radius * 2);

            page.drawLine((int)p1.getX(), (int)p1.getY(), (int)p2.getX(), (int)p2.getY());

            page.setColor(Color.BLACK);
        }

        for (Point p : visibilitySegments.keySet()) {
            float r = rand.nextFloat();
            float g = rand.nextFloat();
            float b = rand.nextFloat();

            Color c = new Color(r, g, b);
            page.setColor(c);

            page.fillOval((int)p.getX() - radius, (int)p.getY() - radius, radius * 2, radius * 2);

            for (Point v : visibilitySegments.get(p)) {
                //page.fillOval((int)v.getX() - radius, (int)v.getY() - radius, radius * 2, radius * 2);
                page.drawLine((int)p.getX(), (int)p.getY(), (int)v.getX(), (int)v.getY());
            }

            page.setColor(Color.BLACK);
        }
    }
}
