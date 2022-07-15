import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Scanner;

public class Spline {

    public static XYSeries series = new XYSeries("Spline Data");
    static ArrayList<Point> points;
    public static void main(String [] args){
        Scanner sc = new Scanner(System.in);
        int n = sc.nextInt();
        points = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            points.add(new Point(sc.nextDouble(), sc.nextDouble()));
        }

        interpSplinePath(points, new Point(0,0));
    }

    public static void interpSplinePath(ArrayList<Point> pts, Point curLoc) {

        boolean incX = true, none = true;
        ArrayList<Point> wp = new ArrayList<>(), cur = new ArrayList<>();
        cur.add(new Point(curLoc.xP, curLoc.yP));
        Point prev = new Point(curLoc.xP, curLoc.yP);
        if (pts.size() == 1) {
            wp.add(pts.get(0));
            return;
        }

        for (Point pt : pts) {
            if (!none) {
                if (incX) {
                    // test for conflicting
                    if (!(pt.xP > prev.xP)) {
                        wp.addAll(generateSplinePath(cur, 0.1));
                        cur.clear();
                        none = true;
                        incX = false;
                        cur.add(prev);
                    } else {
                        cur.add(pt);
                    }
                } else {
                    // test for conflicting
                    if (!(pt.xP < prev.xP)) {
                        ArrayList<Point> p = generateSplinePath(cur, 0.1);
                        Collections.reverse(p);
                        wp.addAll(p);

                        cur.clear();
                        none = true;
                        incX = true;
                        cur.add(prev);
                    } else {
                        cur.add(pt);
                    }
                }
            }

            if (none) {
                cur.add(pt);
                if (cur.get(0).xP < cur.get(1).xP) {
                    incX = true;
                    none = false;
                } else if (cur.get(0).xP > cur.get(1).xP) {
                    incX = false;
                    none = false;
                } else {
                    wp.addAll(generateLinearSpline(cur));
                    cur.clear();
                    cur.add(pt);
                }
            }

            prev = pt;
            System.out.println(incX);
        }

        if (cur.size() == 1) {
            wp.addAll(
                    generateLinearSpline(
                            new ArrayList<>(Arrays.asList(wp.get(wp.size() - 1), cur.get(0)))));
        } else if (cur.size() > 1) {
            System.out.println("yes");
            ArrayList<Point> p = generateSplinePath(cur, 0.1);
            if (!incX) {
                Collections.reverse(p);
            }
            wp.addAll(p);
        }

        for (Point p: wp
             ) {
            series.add(p.xP, p.yP);
        }

        Graph  g = new Graph("Cubic Spline Path");
        g.pack();
        RefineryUtilities.centerFrameOnScreen(g);
        g.setVisible(true);
    }

    /**
     * @param p Array of points which the spline will interpolate
     * @return A series of waypoints which a differential drive robot can follow.
     */
    public static ArrayList<Point> generateSplinePath(Point[] p, double step) {
        int row = 0;
        int solutionIndex = (p.length - 1) * 4;
        Arrays.sort(p);
        // initialize matrix
        BigDecimal[][] m = new BigDecimal[(p.length - 1) * 4][(p.length - 1) * 4 + 1]; // rows
        for (int i = 0; i < (p.length - 1) * 4; i++) {
            for (int j = 0; j <= (p.length - 1) * 4; j++) {
                m[i][j] = BigDecimal.ZERO; // fill with zeros
            }
        }

        // n - 1 splines
        for (int functionNr = 0; functionNr < p.length - 1; functionNr++, row++) {
            Point p0 = p[functionNr], p1 = p[functionNr + 1];
            m[row][functionNr * 4] =
                    new BigDecimal(p0.xP, MathContext.DECIMAL64).pow(3, MathContext.DECIMAL64);
            m[row][functionNr * 4 + 1] =
                    new BigDecimal(p0.xP, MathContext.DECIMAL64).pow(2, MathContext.DECIMAL64);
            m[row][functionNr * 4 + 2] = new BigDecimal(p0.xP, MathContext.DECIMAL64);
            m[row][functionNr * 4 + 3] = new BigDecimal(1, MathContext.DECIMAL64);
            m[row][solutionIndex] = new BigDecimal(p0.yP, MathContext.DECIMAL64);

            ++row;

            m[row][functionNr * 4] =
                    new BigDecimal(p1.xP, MathContext.DECIMAL64).pow(3, MathContext.DECIMAL64);
            m[row][functionNr * 4 + 1] =
                    new BigDecimal(p1.xP, MathContext.DECIMAL64).pow(2, MathContext.DECIMAL64);
            m[row][functionNr * 4 + 2] = new BigDecimal(p1.xP, MathContext.DECIMAL64);
            m[row][functionNr * 4 + 3] = new BigDecimal(1, MathContext.DECIMAL64);
            m[row][solutionIndex] = new BigDecimal(p1.yP, MathContext.DECIMAL64);
        }

        // first derivative
        for (int functionNr = 0; functionNr < p.length - 2; functionNr++, row++) {
            Point p1 = p[functionNr + 1];
            m[row][functionNr * 4] =
                    new BigDecimal(3, MathContext.DECIMAL64)
                            .multiply(new BigDecimal(p1.xP).pow(2, MathContext.DECIMAL64));
            m[row][functionNr * 4 + 1] =
                    new BigDecimal(2, MathContext.DECIMAL64)
                            .multiply(new BigDecimal(p1.xP), MathContext.DECIMAL64);
            m[row][functionNr * 4 + 2] = new BigDecimal(1, MathContext.DECIMAL64);
            m[row][functionNr * 4 + 4] =
                    new BigDecimal(-3).multiply(new BigDecimal(p1.xP).pow(2, MathContext.DECIMAL64));
            m[row][functionNr * 4 + 5] =
                    new BigDecimal(-2, MathContext.DECIMAL64)
                            .multiply(new BigDecimal(p1.xP), MathContext.DECIMAL64);
            m[row][functionNr * 4 + 6] = new BigDecimal(-1, MathContext.DECIMAL64);
        }

        // second derivative
        for (int functionNr = 0; functionNr < p.length - 2; functionNr++, row++) {
            Point p1 = p[functionNr + 1];
            m[row][functionNr * 4] =
                    new BigDecimal(6, MathContext.DECIMAL64)
                            .multiply(new BigDecimal(p1.xP, MathContext.DECIMAL64), MathContext.DECIMAL64);
            m[row][functionNr * 4 + 1] = new BigDecimal(2, MathContext.DECIMAL64);
            m[row][functionNr * 4 + 4] =
                    new BigDecimal(-6, MathContext.DECIMAL64)
                            .multiply(new BigDecimal(p1.xP, MathContext.DECIMAL64), MathContext.DECIMAL64);
            m[row][functionNr * 4 + 5] = new BigDecimal(-2, MathContext.DECIMAL64);
        }

        // check these calculations later
        m[row][0] =
                new BigDecimal(6, MathContext.DECIMAL64)
                        .multiply(new BigDecimal(p[0].xP, MathContext.DECIMAL64), MathContext.DECIMAL64);
        m[row++][1] = new BigDecimal(2, MathContext.DECIMAL64);
        m[row][solutionIndex - 4] =
                new BigDecimal(6, MathContext.DECIMAL64)
                        .multiply(
                                new BigDecimal(p[p.length - 1].xP, MathContext.DECIMAL64), MathContext.DECIMAL64);
        m[row][solutionIndex - 4 + 1] = new BigDecimal(2, MathContext.DECIMAL64);

        BigDecimal[][] reducedRowEchelonForm = rref(m);
        BigDecimal[] coefficients = new BigDecimal[reducedRowEchelonForm.length];
        for (int i = 0; i < reducedRowEchelonForm.length; i++) {
            coefficients[i] = reducedRowEchelonForm[i][reducedRowEchelonForm[i].length - 1];
        }

        ArrayList<Point> path = new ArrayList<>();
        for (int i = 0; i < coefficients.length; i += 4) {
            for (double j = p[i / 4].xP; j <= p[(i / 4) + 1].xP; j += step) {
                BigDecimal a =
                        coefficients[i].multiply(BigDecimal.valueOf(j).pow(3, MathContext.DECIMAL64));
                BigDecimal b =
                        coefficients[i + 1].multiply(BigDecimal.valueOf(j).pow(2, MathContext.DECIMAL64));
                BigDecimal c = coefficients[i + 2].multiply(BigDecimal.valueOf(j));
                BigDecimal d = coefficients[i + 3];
                path.add(new Point(j, a.add(b).add(c).add(d).doubleValue()));
            }
        }

        return path;
    }

    public static ArrayList<Point> generateSplinePath(ArrayList<Point> l, double step) {
        Point[] p = new Point[l.size()];
        for (int i = 0; i < l.size(); i++) {
            p[i] = l.get(i);
        }
        return generateSplinePath(p, step);
    }

    public static ArrayList<Point> generateLinearSpline(ArrayList<Point> pts) {
        ArrayList<Point> wp = new ArrayList<>();
        for (int i = 0; i < pts.size() - 1; i++) {
            double x1 = pts.get(i).xP, x2 = pts.get(i + 1).xP, y1 = pts.get(i).yP, y2 = pts.get(i + 1).yP;
            if (x1 == x2) {
                if (y1 < y2) {
                    for (double j = y1; j <= y2; j++) {
                        wp.add(new Point(x1, j));
                    }
                } else {
                    for (double j = y1; j >= y2; j--) {
                        wp.add(new Point(x1, j));
                    }
                }
                continue;
            }

            double slope = (y2 - y1) / (x2 - x1);
            if (pts.get(i).xP > pts.get(i + 1).xP) {
                slope *= -1;
                int stepCount = 0;
                for (double j = x1; j >= x2; j--) {
                    wp.add(new Point(j, y1 + (slope * stepCount)));
                    stepCount++;
                }
            } else {
                int stepCount = 0;
                for (double j = x1; j <= x2; j++) {
                    wp.add(new Point(j, y1 + (slope * stepCount)));
                    stepCount++;
                }
            }
        }
        return wp;
    }

    public static BigDecimal[][] rref(BigDecimal[][] mat) {
        int lead = 0;
        for (int r = 0; r < mat.length; r++) {
            int i = r;
            while (mat[i][lead].compareTo(BigDecimal.ZERO) == 0) {
                i++;
                if (mat.length == i) {
                    i = r;
                    lead++;
                }
            }

            BigDecimal[] tmp = mat[i];
            mat[i] = mat[r];
            mat[r] = tmp;

            BigDecimal val = mat[r][lead];
            for (int j = 0; j < mat[0].length; j++) {
                mat[r][j] = mat[r][j].divide(val, MathContext.DECIMAL64);
            }

            for (i = 0; i < mat.length; i++) {
                if (i == r) continue;
                val = mat[i][lead];
                for (int j = 0; j < mat[0].length; j++) {
                    mat[i][j] =
                            mat[i][j].subtract(
                                    val.multiply(mat[r][j], MathContext.DECIMAL64), MathContext.DECIMAL64);
                }
            }
            lead++;
        }
        return mat;
    }

    public static class Graph extends ApplicationFrame {


        public Graph(final String title) {
            super(title);

            final XYSeriesCollection data = new XYSeriesCollection(series);
            final JFreeChart chart = ChartFactory.createScatterPlot(
                    "Generated Spline",
                    "X",
                    "Y",
                    data,
                    PlotOrientation.VERTICAL,
                    true,
                    true,
                    false
            );

            final ChartPanel chartPanel = new ChartPanel(chart);
            chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
            setContentPane(chartPanel);

        }

    }

    static class Point implements Comparable<Point>{
        double xP, yP;
        public Point(double x, double y){
            this.xP=x;
            this.yP=y;
        }

        @Override
        public int compareTo(Point o) {
            if (this.xP < o.xP) return -1;
            if (this.xP == o.xP) return 0;
            return 1;
        }

        @Override
        public String toString() {
            return "{" + "xP=" + xP + "\n, yP=" + yP + "\n, " + '}';

        }
    }
}


