import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Scanner;
// JFree
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;

/**
 * @author Mrinall Umasudhan
 */
public class Spline {
    public static XYSeries series = new XYSeries("Spline Data");
    static Point [] points;
    public static void main(String [] args){
        Scanner sc = new Scanner(System.in);
        int n = sc.nextInt();
        points = new Point[n];
        for (int i = 0; i < n; i++) {
            points[i] = new Point(sc.nextDouble(), sc.nextDouble());
        }

        Arrays.sort(points);
        cubicSplineInterpolation(points);

    }


    public static void cubicSplineInterpolation(Point [] p) {
        var row = 0;
        var solutionIndex = (p.length - 1) * 4;

        // initialize matrix
        BigDecimal [][] m = new BigDecimal[(p.length - 1) * 4][(p.length - 1) * 4 + 1]; // rows
        for (var i = 0; i < (p.length - 1) * 4; i++) {
            for (var j = 0; j <= (p.length - 1) * 4; j++) {
                m[i][j] =  BigDecimal.ZERO; // fill with zeros
            }
        }

        // n - 1 splines
        for (int functionNr = 0; functionNr < p.length-1; functionNr++, row++) {
            Point p0 = p[functionNr], p1 = p[functionNr+1];
            m[row][functionNr * 4] = new BigDecimal(p0.x, MathContext.DECIMAL64).pow(3, MathContext.DECIMAL64);

            m[row][functionNr*4+1] = new BigDecimal(p0.x, MathContext.DECIMAL64).pow(2, MathContext.DECIMAL64);

            m[row][functionNr*4+2] = new BigDecimal(p0.x, MathContext.DECIMAL64);

            m[row][functionNr*4+3] = new BigDecimal(1, MathContext.DECIMAL64);

            m[row][solutionIndex] = new BigDecimal(p0.y, MathContext.DECIMAL64);

            ++row;
            m[row][functionNr * 4] =  new BigDecimal(p1.x, MathContext.DECIMAL64).pow(3, MathContext.DECIMAL64);

            m[row][functionNr*4+1] =  new BigDecimal(p1.x, MathContext.DECIMAL64).pow(2, MathContext.DECIMAL64);

            m[row][functionNr*4+2] =  new BigDecimal(p1.x, MathContext.DECIMAL64);

            m[row][functionNr*4+3] =  new BigDecimal(1, MathContext.DECIMAL64);

            m[row][solutionIndex] = new BigDecimal(p1.y, MathContext.DECIMAL64);


        }

        // first derivative
        for (var functionNr = 0; functionNr < p.length - 2; functionNr++, row++) {
            var p1 = p[functionNr+1];
            m[row][functionNr * 4] = new BigDecimal(3, MathContext.DECIMAL64).multiply(new BigDecimal(p1.x).pow(2, MathContext.DECIMAL64));

            m[row][functionNr*4+1] = new BigDecimal(2, MathContext.DECIMAL64).multiply(new BigDecimal(p1.x), MathContext.DECIMAL64);

            m[row][functionNr*4+2] = new BigDecimal(1, MathContext.DECIMAL64);

            m[row][functionNr*4+4] = new BigDecimal(-3).multiply(new BigDecimal(p1.x).pow(2, MathContext.DECIMAL64));

            m[row][functionNr*4+5] = new BigDecimal(-2, MathContext.DECIMAL64).multiply(new BigDecimal(p1.x), MathContext.DECIMAL64);

            m[row][functionNr*4+6] = new BigDecimal(-1, MathContext.DECIMAL64);

        }


        // second derivative
        for (var functionNr = 0; functionNr < p.length - 2; functionNr++, row++) {
            var p1 = p[functionNr+1];
            m[row][functionNr * 4] =  new BigDecimal(6, MathContext.DECIMAL64).multiply(new BigDecimal(p1.x, MathContext.DECIMAL64), MathContext.DECIMAL64);

            m[row][functionNr*4 + 1] =  new BigDecimal(2, MathContext.DECIMAL64);

            m[row][functionNr*4 + 4] =  new BigDecimal(-6, MathContext.DECIMAL64).multiply(new BigDecimal(p1.x, MathContext.DECIMAL64), MathContext.DECIMAL64);

            m[row][functionNr*4 + 5] = new BigDecimal(-2, MathContext.DECIMAL64);

        }


        // check these calculations later
        m[row][0] = new BigDecimal(6, MathContext.DECIMAL64).multiply(new BigDecimal(p[0].x, MathContext.DECIMAL64), MathContext.DECIMAL64);

        m[row++][1] = new BigDecimal(2, MathContext.DECIMAL64);

        m[row][solutionIndex - 4] = new BigDecimal(6, MathContext.DECIMAL64).multiply(new BigDecimal(p[p.length-1].x, MathContext.DECIMAL64), MathContext.DECIMAL64);

        m[row][solutionIndex-4+1] = new BigDecimal(2, MathContext.DECIMAL64);


        BigDecimal [][] reducedRowEchelonForm = rref(m);
        BigDecimal [] coefficients = new BigDecimal[reducedRowEchelonForm.length];
        for (var i = 0; i < reducedRowEchelonForm.length; i++) {
            coefficients[i] = reducedRowEchelonForm[i][reducedRowEchelonForm[i].length - 1];
        }

        BigDecimal [][] functions = new BigDecimal[points.length - 1][4];
        // need better test data

        for (var i = 0; i < coefficients.length; i += 4) {
            System.out.println(coefficients[i]);
            System.out.println(coefficients[i + 1]);
            System.out.println(coefficients[i + 2]);
            System.out.println(coefficients[i + 3]);
            System.out.println();
            for (double j = p[i / 4].x; j <= p[(i / 4) + 1].x; j += 0.01) { // Edit increments as needed
                BigDecimal a = coefficients[i].multiply(BigDecimal.valueOf(j).pow(3, MathContext.DECIMAL64));
                BigDecimal b = coefficients[i + 1].multiply(BigDecimal.valueOf(j).pow(2, MathContext.DECIMAL64));
                BigDecimal c = coefficients[i + 2].multiply(BigDecimal.valueOf(j));
                BigDecimal d = coefficients[i + 3];
                series.add(j, a.add(b).add(c).add(d));
                // Place moveToPosition function for odometry here if needed.
                // xPos = j 
                // yPos = a.add(b).add(c).add(d)  
                // angle = ??
            }
        }

        Graph  g = new Graph("Cubic Spline Path");
        g.pack();
        RefineryUtilities.centerFrameOnScreen(g);
        g.setVisible(true);
    }

    public static BigDecimal [][] rref(BigDecimal[][] mat) {
        int lead = 0;
        for (int r = 0; r < mat.length; r++) {

//           if (mat[0].length <= lead) {
//                return mat;
//           }
            int i = r;
            while (mat[i][lead].compareTo(BigDecimal.ZERO) == 0) {
                i++;
                if (mat.length == i) {
                    i = r;
                    lead++;
//                    if (mat[0].length == lead) {
//                        return mat;
//                    }
                }
            }

            BigDecimal [] tmp = mat[i];
            mat[i] = mat[r];
            mat[r] = tmp;

            BigDecimal val = mat[r][lead];
            for (int j = 0; j < mat[0].length; j++) {
                mat[r][j] = mat[r][j].divide(val, MathContext.DECIMAL64);
            }

            for (i = 0; i < mat.length; i++) {
                if (i == r) continue;
                val = mat[i][lead];
                for (var j = 0; j < mat[0].length; j++) {
                    mat[i][j] = mat[i][j].subtract(val.multiply(mat[r][j], MathContext.DECIMAL64), MathContext.DECIMAL64);
                }
            }
            lead++;
        }
        return mat;
    }
    static class Point implements Comparable<Point>{
        double x, y;
        public Point(double x, double y){
            this.x=x;
            this.y=y;
        }

        @Override
        public int compareTo(Point o) {
            if (this.x < o.x) return -1;
            if (this.x == o.x) return 0;
            return 1;
        }
    }

    public static class Graph extends ApplicationFrame {


        public Graph(final String title) {
            super(title);

            final XYSeriesCollection data = new XYSeriesCollection(series);
            final JFreeChart chart = ChartFactory.createXYLineChart(
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
}
