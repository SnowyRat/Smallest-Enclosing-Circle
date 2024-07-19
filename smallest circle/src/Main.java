import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.util.*;
import java.util.List;

public class Main {
    public static class Circle {
        Point2D center;
        double radius;
        List<Point2D> definingPoints;

        public Circle(Point2D center, double radius, List<Point2D> definingPoints) {
            this.center = center;
            this.radius = radius;
            this.definingPoints = new ArrayList<>(definingPoints);
        }

        public boolean encloses(Point2D point) {
            return center.distance(point) <= radius;
        }
    }
    private static Circle circleFromPoints(Point2D p1, Point2D p2) {
        Point2D center = new Point2D.Double((p1.getX() + p2.getX()) / 2, (p1.getY() + p2.getY()) / 2);
        double radius = center.distance(p1);
        List<Point2D> definingPoints = Arrays.asList(p1, p2);
        return new Circle(center, radius, definingPoints);
    }

    // Helper function that constructs a circle from 3 given points
    /*
    private static Circle circleFromPoints(Point2D p1, Point2D p2, Point2D p3) {
        // Using circumcenter formula for a triangle
        double ax = p1.getX(), ay = p1.getY();
        double bx = p2.getX(), by = p2.getY();
        double cx = p3.getX(), cy = p3.getY();
        double d = 2 * (ax * (by - cy) + bx * (cy - ay) + cx * (ay - by));
        double ux = ((ax * ax + ay * ay) * (by - cy) + (bx * bx + by * by) * (cy - ay) + (cx * cx + cy * cy) * (ay - by)) / d;
        double uy = ((ax * ax + ay * ay) * (cx - bx) + (bx * bx + by * by) * (ax - cx) + (cx * cx + cy * cy) * (bx - ax)) / d;
        Point2D center = new Point2D.Double(ux, uy);
        double radius = center.distance(p1);
        List<Point2D> definingPoints = Arrays.asList(p1, p2, p3);
        return new Circle(center, radius, definingPoints);
    }
     */
    private static Circle circleFromPoints(Point2D p1, Point2D p2, Point2D p3){
        double [][] aMatrix =
                {
                        {p1.getX(),p1.getY(),1},
                        {p2.getX(),p2.getY(),1},
                        {p3.getX(),p3.getY(),1}
                };
        double [][] dMatrix =
                {
                        {Math.pow(p1.getX(), 2) + Math.pow(p1.getY(), 2), p1.getY(), 1},
                        {Math.pow(p2.getX(), 2) + Math.pow(p2.getY(), 2), p2.getY(), 1},
                        {Math.pow(p3.getX(), 2) + Math.pow(p3.getY(), 2), p3.getY(), 1}
                };
        double[][] eMatrix =
                {
                        {Math.pow(p1.getX(), 2) + Math.pow(p1.getY(), 2), p1.getX(), 1},
                        {Math.pow(p2.getX(), 2) + Math.pow(p2.getY(), 2), p2.getX(), 1},
                        {Math.pow(p3.getX(), 2) + Math.pow(p3.getY(), 2), p3.getX(), 1}
                };
        double[][] fMatrix =
                {
                        {Math.pow(p1.getX(), 2) + Math.pow(p1.getY(), 2), p1.getX(), p1.getY()},
                        {Math.pow(p2.getX(), 2) + Math.pow(p2.getY(), 2), p2.getX(), p2.getY()},
                        {Math.pow(p3.getX(), 2) + Math.pow(p3.getY(), 2), p3.getX(), p3.getY()}
                };
        double a = determinant(aMatrix);
        double d = determinant(dMatrix);
        double e = determinant(eMatrix);
        double f = determinant(fMatrix);
        double xC = d/(2*a);
        double yC = -e/(2*a);
        double radSQ = (Math.pow(d,2)+Math.pow(e,2))/(4 * Math.pow(a, 2)) - f / a;
        double rad = Math.sqrt(radSQ);
        Point2D center = new Point2D.Double(xC,yC);
        List<Point2D> definingPnts = Arrays.asList(p1,p2,p3);
        return new Circle(center,rad,definingPnts);

    }
    private static double determinant(double[][] matrix) {
        return matrix[0][0] * (matrix[1][1] * matrix[2][2] - matrix[1][2] * matrix[2][1]) -
                matrix[0][1] * (matrix[1][0] * matrix[2][2] - matrix[1][2] * matrix[2][0]) +
                matrix[0][2] * (matrix[1][0] * matrix[2][1] - matrix[1][1] * matrix[2][0]);
    }

    private static boolean enclosesAllPoints(Circle circle, List<Point2D> points) {
        // Distance from the center to a point must be strictly less than the radius
        final double EPSILON = 1e-14;
        for (Point2D point : points) {
            if (circle.center.distance(point) > circle.radius + EPSILON) {
                return false;
            }
        }
        return true;
    }


    // Brute force algorithm to find the smallest enclosing circle
    public static Circle smallestEnclosingCircleBrute(List<Point2D> points) {
        Circle smallestCircle = null;
        if(points.size() == 1){
            return circleFromPoints(points.get(0), points.get(0));
        }
        if (points.size() == 2) {
            return circleFromPoints(points.get(0), points.get(1));
        }
        // Check all pairs of possible two point circles
        for (int i = 0; i < points.size(); i++) {
            for (int j = i + 1; j < points.size(); j++) {
                Circle circle = circleFromPoints(points.get(i), points.get(j));
                if (enclosesAllPoints(circle, points) && (smallestCircle == null || circle.radius < smallestCircle.radius)) {
                    smallestCircle = circle;
                }
            }
        }
        // Check all triples of possible three point circles
        for (int i = 0; i < points.size(); i++) {
            for (int j = i + 1; j < points.size(); j++) {
                for (int k = j + 1; k < points.size(); k++) {
                    Circle circle = circleFromPoints(points.get(i), points.get(j), points.get(k));
                    if (enclosesAllPoints(circle, points) && (smallestCircle == null || circle.radius < smallestCircle.radius)) {
                        smallestCircle = circle;
                    }
                }
            }
        }
        return smallestCircle;
    }
    public static Circle smallestEnclosingCircleSmart(List<Point2D> points, List<Point2D> boundary, int n)
    {
        Circle circle = null;
        // Base case, if all points are processed or 3 points left in boundaries
        if(n == 0 || boundary.size() == 3){
            circle = circleBoundary(boundary);
        }else{
            // Recursively finds the smallest circle without nth point
            // Repeats recursively until the base case is hit at which a circle with a radius of 0, center 0 is drawn
            // Backtracking checks if the new point is inside or outside the bounds of the current circle
            // If outside changes the circle's boundaries repeats recursively checking other points
            // If no more points are left to check, smallest enclosing circle is returned
            circle = smallestEnclosingCircleSmart(points, boundary, n-1);
            if(!circle.encloses(points.get(n-1))){
                // If the circle doesn't contain the nth point, it must be in the boundary of the smallest circle
                boundary.add(points.get(n-1));
                circle = smallestEnclosingCircleSmart(points,boundary,n-1);
                boundary.remove(boundary.size()-1); //backtrack
            }
        }
        return circle;
    }
    public static Circle circleBoundary(List<Point2D> boundary){
        if(boundary.isEmpty()){
            return new Circle(new Point2D.Double(0,0),0,boundary);
        }
        else if(boundary.size()==1){
            //Creates a circle from 1 point
            return new Circle(boundary.get(0),0,boundary);
        }
        else if(boundary.size()==2){
            //Creates a circle from 2 points
            return circleFromPoints(boundary.get(0),boundary.get(1));
        }
        else{
            //Creates a circle from 3 points
            return circleFromPoints(boundary.get(0),boundary.get(1), boundary.get(2));
        }
    }
    public static Circle smallesEnclosingCircleSmart(List<Point2D> points){
        if(points.isEmpty()){
            Circle circle = null;
            return circle;
        }
        List<Point2D> shuffle = new ArrayList<>(points);
        Collections.shuffle(shuffle);
        return smallestEnclosingCircleSmart(shuffle, new ArrayList<>(), points.size());
    }
    public static List<Point2D> startingPoints(int i) {
        List<Point2D> points = new ArrayList<>();
        Random rand = new Random();

        if (i == 1) {
            for (double j = 0.0; j <= 0.6; j += 0.1) {
                points.add(new Point2D.Double(j, j));
                points.add(new Point2D.Double(-j, -j));
                if (j != 0.0) {
                    points.add(new Point2D.Double(-j, j));
                    points.add(new Point2D.Double(j, -j));
                }
            }
            points.add(new Point2D.Double(0.7, -0.7));
            // Add 10 random points for i == 1
            for (int k = 0; k < 10; k++) {
                double x = -0.5 + rand.nextDouble(); // random x between -1.0 and 1.0
                double y = -0.5 + rand.nextDouble(); // random y between -1.0 and 1.0
                points.add(new Point2D.Double(x, y));
            }
        } else if (i == 2) {
            for (double j = 0.1; j <= 0.3; j += 0.1) {
                points.add(new Point2D.Double(j, j + 0.1));
            }
            // Add 10 random points for i == 2
            for (int k = 0; k < 10; k++) {
                double x = -0.5 + rand.nextDouble(); // random x between -1.0 and 1.0
                double y = -0.5 + rand.nextDouble(); // random y between -1.0 and 1.0
                points.add(new Point2D.Double(x, y));
            }
        } else if (i == 3) {
            points.add(new Point2D.Double(0.2, 0.3));
        }

        return points;
    }


    public static void main(String[] args) {
        //Starting data=1(3 points), 2 = (2 points), 3 = (1 point edge case)
        List<Point2D> points = startingPoints(2);
        System.out.println(checkStartingData(points));
        // Tests brute force algorithm
        executeTestBrute(points);
        // Tests efficient algorithm(welzl)
        executeTestSmart(points);
    }
    public static void executeTestBrute( List<Point2D> points){
        long start_brute = System.nanoTime();
        Circle smallestCircleBrute = smallestEnclosingCircleBrute(points);
        long end_brute = System.nanoTime();
        long brute = end_brute-start_brute;
        System.out.println(brute);
        printResults(smallestCircleBrute, points);
    }
    public static void executeTestSmart( List<Point2D> points){
        long start_smart = System.nanoTime();
        Circle smallestCircleSmart = smallesEnclosingCircleSmart(points);
        long end_smart = System.nanoTime();
        long smart = end_smart-start_smart;
        System.out.println(smart);
        printResults(smallestCircleSmart, points);
    }
    public static boolean checkStartingData(List<Point2D> points){
        // Precision for point comparison
        // float comparisons remove last couple digits
        final double eps = 1e-14;
        boolean removed = false;
        for (int i = 0; i < points.size() - 1; i++) {
            Point2D current = points.get(i);

            for (int j = points.size() - 1; j > i; j--) {
                Point2D compare = points.get(j);
                if (Math.abs(current.getX() - compare.getX()) < eps &&
                        Math.abs(current.getY() - compare.getY()) < eps) {
                    points.remove(j); // Remove the duplicate point
                    removed = true;
                }
            }
        }
        return removed;
    }
    public static void printResults(Circle smallestCircle, List<Point2D> points){
        if (smallestCircle != null) {
            System.out.println("Smallest Enclosing Circle: Center = (" + smallestCircle.center.getX() + ", " + smallestCircle.center.getY() + "), Radius = " + smallestCircle.radius);
            System.out.println("Defining Points:");
            for (Point2D p : smallestCircle.definingPoints) {
                System.out.println("(" + p.getX() + ", " + p.getY() + ")");
            }
            Visualizer.display(smallestCircle, points);
        } else {
            System.out.println("No enclosing circle found.");
        }
    }

    public static class Visualizer extends JPanel {
        private final Circle circle;
        private final List<Point2D> points;

        public Visualizer(Circle circle, List<Point2D> points) {
            this.circle = circle;
            this.points = points;
            setPreferredSize(new Dimension(400, 400));
            setBackground(Color.WHITE);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            // Draw the circle
            if (circle != null) {
                g2d.setColor(Color.BLUE);
                double radius = circle.radius * 200; // Scale for visibility
                double diameter = radius * 2;
                double x = (circle.center.getX() * 200 + 200) - radius;
                double y = (circle.center.getY() * 200 + 200) - radius;
                g2d.draw(new Ellipse2D.Double(x, y, diameter, diameter));
            }

            // Draw the points
            g2d.setColor(Color.RED);
            for (Point2D point : points) {
                double x = point.getX() * 200 + 190;
                double y = point.getY() * 200 + 190;
                g2d.fill(new Ellipse2D.Double(x, y, 20, 20));
            }
        }

        public static void display(Circle circle, List<Point2D> points) {
            JFrame frame = new JFrame("Smallest Enclosing Circle Visualizer");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.add(new Visualizer(circle, points));
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        }
    }
}
