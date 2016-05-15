package drawing;

import geometry.*;
import geometry.Point;
import geometry.Polygon;
import main.Constants;
import processor.Processor;
import utils.LinkedList;
import processor.VisibilityGraph;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Artem on 21.04.2016.
 */
public class MainForm implements ActionListener {
    private JPanel rootPanel;
    private JTextField textField1;
    private JTextArea textArea1;
    private JButton drawThisFieldButton;
    private JButton randomFieldButton;
    private JButton findPathButton;
    private JMenuBar menuBar;
    private JMenu fileMenu;
    private JMenuItem saveMenuItem;
    private JMenuItem openMenuItem;

    private final String pathFileAddress = ".\\Fields\\Shortest_Path.txt";
    private final String fieldFileAddress = ".\\Fields\\Field.txt";

    private static final Pattern pointPattern = Pattern.compile("\\([-+]?[0-9]*.?[0-9]+([eE][-+]?[0-9]+)?;\\s?[-+]?[0-9]*.?[0-9]+([eE][-+]?[0-9]+)?\\)");

    private List<Polygon> obstacles;

    public MainForm() {
        JFrame frame = new JFrame("Shortest path");

        menuBar = new JMenuBar();

        fileMenu = new JMenu("File");
        openMenuItem = new JMenuItem("Open");
        openMenuItem.addActionListener(this);
        saveMenuItem = new JMenuItem("Save field");
        saveMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveField(obstacles, fieldFileAddress);
                JOptionPane.showMessageDialog(frame, "Filed saved to " + fieldFileAddress);
            }
        });
        fileMenu.add(openMenuItem);
        fileMenu.add(saveMenuItem);

        menuBar.add(fileMenu);
        frame.setJMenuBar(menuBar);

        obstacles = new ArrayList<>();
        Painter painter = new Painter();
        Processor proc = new Processor();
        final VisibilityGraph[] graph = {new VisibilityGraph()};

        randomFieldButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                painter.clearPath();
                obstacles = generateRandomObstacles();
                painter.drawPolygons(obstacles);
                graph[0] = proc.buildVisibilityGraph(obstacles);
            }
        });

        drawThisFieldButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                painter.clearPath();
                obstacles = formPolygons(textArea1.getText());

                if (obstacles.size() > Constants.MAX_NUMBER_OF_OBSTACLES) {
                    JOptionPane.showMessageDialog(frame, "To many obstacles. Field won't be shown but shortest path will" +
                            " be saved to file ." + pathFileAddress);
                } else {
                    painter.drawPolygons(obstacles);
                }

                graph[0] = proc.buildVisibilityGraph(obstacles);
            }
        });

        findPathButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String tQueryPoints = textField1.getText();

                if (tQueryPoints.isEmpty())
                    return;

                Matcher matcher = pointPattern.matcher(tQueryPoints);

                Point start = null;
                Point end = null;

                if (matcher.find()) {
                    start = Point.parsePoint(matcher.group(0));
                } else {
                    textField1.setText("Wrong input!");
                    return;
                }

                if (matcher.find()) {
                    end = Point.parsePoint(matcher.group(0));
                } else {
                    textField1.setText("Wrong input!");
                    return;
                }

                if (!proc.isPointInsideSurroundingPolygon(start)) {
                    textField1.setText("Point " + start + " should be inside surrounding polygon");
                    return;
                }

                if (!proc.isPointInsideSurroundingPolygon(end)) {
                    textField1.setText("Point " + end + " should be inside surrounding polygon");
                    return;
                }

                List<Point> path = graph[0].getShortestPath(start, end);

                if (path == null) {
                    textField1.setText("Point is inside obstacle");
                    return;
                }

                if (obstacles.size() > Constants.MAX_NUMBER_OF_OBSTACLES) {
                    savePathToFile(pathFileAddress, path);
                    JOptionPane.showMessageDialog(frame, "Path saved to " + pathFileAddress);
                } else {
                    painter.drawPath(path);
                }
            }
        });

        frame.add(rootPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);

    }

    public void actionPerformed(ActionEvent ev) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
        int result = fileChooser.showOpenDialog(rootPanel);

        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            textArea1.setText(readFile(selectedFile));
        }
    }

    private String readFile(File file) {
        StringBuilder text = new StringBuilder();

        try {
            InputStream inp = new FileInputStream(file);
            Scanner scanner = new Scanner(inp);

            while (scanner.hasNextLine()) {
                text.append(scanner.nextLine() + '\n');
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return text.toString();
    }

    private List<Polygon> formPolygons(String data) {
        List<Polygon> obstacles = new ArrayList<>();

        if (data.isEmpty())
            return obstacles;

        String[] polygonsData = data.split("\n");

        for (String polygonData : polygonsData) {
            Polygon polygon = Polygon.parsePolygon(polygonData.trim());
            obstacles.add(polygon);
        }

        return obstacles;
    }

    private List<Polygon> generateRandomObstacles() {
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

    private Polygon generateSurroundingPolygon() {
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

    private Set<Integer> chooseAreasForObstacles(int areasNumber, int obstaclesNumber) {
        Set<Integer> chosen = new HashSet<>();
        Random rand = new Random();
        Integer candidate;

        while (chosen.size() < obstaclesNumber) {
            candidate = rand.nextInt(areasNumber);
            chosen.add(candidate);
        }

        return chosen;
    }

    private void savePathToFile(String fileAddress, List<Point> path) {
        try {
            Writer writer = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(fileAddress), "utf-8"));

            Collections.reverse(path);

            for (Point p : path) {
                writer.write(p.toString());
            }

            writer.close();

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void saveField(List<Polygon> polygons, String address) {
        try {
            List<String> lines = obstaclesToString(polygons);
            Path file = Paths.get(address);
            Files.write(file, lines, Charset.forName("UTF-8"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private List<String> obstaclesToString(List<Polygon> polygons) {
        List<String> lines = new ArrayList<>();

        for (Polygon polygon : polygons) {
            lines.add(polygon.toString());
        }

        return lines;
    }
}
