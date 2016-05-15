package processor;

import geometry.Point;
import org.jgrapht.util.FibonacciHeap;
import org.jgrapht.util.FibonacciHeapNode;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Created by Artem on 11.03.2016.
 */
public class VisibilityGraph {
    private HashMap<Point, HashMap<Point, Double>> graph;

    public VisibilityGraph() {
        graph = new HashMap<>();
    }

    public void addVertex(Point p, HashMap<Point, Double> visiblePoints) {
        if (graph.containsKey(p)) {
            graph.get(p).putAll(visiblePoints);
        } else {
            graph.put(p, visiblePoints);
        }

        reflectEntry(p, visiblePoints);
    }

    public void removeVertex(Point p) {
        if (graph.containsKey(p)) {
            for (Point v : graph.get(p).keySet()) {
                graph.get(v).remove(p);
            }

            graph.remove(p);
        }
    }

    private void reflectEntry(Point p, HashMap<Point, Double> visiblePoints) {
        Double dist;

        for (Point v : visiblePoints.keySet()) {
            dist = visiblePoints.get(v);

            graph.putIfAbsent(v, new HashMap<>());

            graph.get(v).put(p, dist);
        }
    }

    public List<Point> getShortestPath(Point start, Point end) {
        if (!Processor.insertQueryPoint(start))
            return null;

        if (!Processor.insertQueryPoint(end))
            return null;

        HashMap<Point, Double> start_visible = Processor.getQueryPointVisibleVertexes(start);
        HashMap<Point, Double> end_visible = Processor.getQueryPointVisibleVertexes(end);

        this.addVertex(start, start_visible);
        this.addVertex(end, end_visible);

        HashMap<Point, Point> paths = dijkstra(start);

        return restorePath(paths, start, end);
    }

    public HashMap<Point, Double> getVisibleVertexes(Point p) {
        return graph.get(p);
    }

    public void removeQueryPoints(Point start, Point end) {
        this.removeVertex(start);
        this.removeVertex(end);

        Processor.removeQueryPoint(start);
        Processor.removeQueryPoint(end);
    }

    public void saveGraph(String fileAddress) {
        try {
            List<String> lines = toStringLines();
            Path file = Paths.get(fileAddress);
            Files.write(file, lines, Charset.forName("UTF-8"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void readGraph(String fileAddress) {
        graph = new HashMap<>();

        try {
            File file = new File(fileAddress);
            InputStream inp = new FileInputStream(file);
            Scanner scan = new Scanner(inp);

            while (scan.hasNextLine()) {
                String[] vals = scan.nextLine().split("\t");

                if (vals.length != 3) {
                    throw new IOException("Wrong input format! Line: " + vals);
                }

                Point key = Point.parsePoint(vals[0].trim());
                Point visible = Point.parsePoint(vals[1].trim());
                Double distance = Double.parseDouble(vals[2].trim());

                graph.putIfAbsent(key, new HashMap<>());
                graph.get(key).putIfAbsent(visible, distance);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private List<String> toStringLines() {
        List<String> lines = new ArrayList<>();

        for (Point key : graph.keySet()) {
            for (Point visible : graph.get(key).keySet())
                lines.add(key + "\t" + visible + "\t" + graph.get(key).get(visible));
        }

        return lines;
    }

    private HashMap<Point, Point> dijkstra(Point start) {
        HashMap<Point, Double> distances = initializeDistances();
        HashMap<Point, Point> path = new HashMap<>();
        FibonacciHeap<Point> pq = new FibonacciHeap<>();
        FibonacciHeapNode<Point> node;

        HashMap<Point, FibonacciHeapNode<Point>> pointers = new HashMap<>();

        node = new FibonacciHeapNode<>(start, 0);

        pq.insert(node, 0);
        pointers.put(start, node);
        path.put(start, start);

        distances.put(start, 0.0);

        while (!pq.isEmpty()) {
            node = pq.removeMin();
            Point s = node.getData();

            for (Point i : graph.get(s).keySet()) {
                Double new_dist = distances.get(s) + graph.get(s).get(i);

                if (distances.get(i) > new_dist) {
                    distances.put(i, new_dist);

                    if (pointers.containsKey(i)) {
                        pq.decreaseKey(pointers.get(i), new_dist);
                    } else {
                        node = new FibonacciHeapNode<>(i, new_dist);
                        pq.insert(node, new_dist);
                        pointers.put(i, node);

                        //path.put(i, s);
                    }

                    path.put(i, s);
                }
            }
        }

        return path;
    }

    private List<Point> restorePath(HashMap<Point, Point> paths, Point start, Point end) {
        List<Point> path = new ArrayList<>();

        Point current = end;

        while (!current.equals(start)) {
            path.add(current);
            current = paths.get(current);
        }

        path.add(current);

        return path;
    }

    private HashMap<Point, Double> initializeDistances() {
        HashMap<Point, Double> distances = new HashMap<>();

        for (Point key : graph.keySet()) {
            distances.put(key, Double.MAX_VALUE / 2);
        }

        return distances;
    }
}
