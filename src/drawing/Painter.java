package drawing;

import geometry.Point;
import geometry.Polygon;

import javax.swing.*;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Artem on 25.03.2016.
 */
public class Painter {
    Panel polyPanel;
    JFrame polygonFrame;

    public Painter() {
        polygonFrame = new JFrame("Field");
        polygonFrame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);

        polyPanel = new Panel();

        polygonFrame.getContentPane().add(polyPanel);
    }


    public void drawPolygons(List<Polygon> polygons) {
        polyPanel.addPolygons(polygons);
        polygonFrame.pack();
        polygonFrame.setVisible(true);
    }

    public void drawPath(List<Point> path) {
        polyPanel.addPath(path);
        polygonFrame.pack();
        polygonFrame.setVisible(true);
    }

    public void clearPath() {
        polyPanel.clearPath();
    }

    public void drawVisibilitySegment(Point p, HashMap<Point, Double> visibleVertexes) {
        polyPanel.addVisibleVertexes(p, visibleVertexes);
        polygonFrame.pack();
        polygonFrame.setVisible(true);
    }
}
