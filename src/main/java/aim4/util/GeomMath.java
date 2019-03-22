/*
Copyright (c) 2011 Tsz-Chiu Au, Peter Stone
University of Texas at Austin
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

1. Redistributions of source code must retain the above copyright notice, this
list of conditions and the following disclaimer.

2. Redistributions in binary form must reproduce the above copyright notice,
this list of conditions and the following disclaimer in the documentation
and/or other materials provided with the distribution.

3. Neither the name of the University of Texas at Austin nor the names of its
contributors may be used to endorse or promote products derived from this
software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
package aim4.util;

import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.Shape;
import java.util.ArrayList;
import java.util.List;

/**
 * Class of static utility methods for geometric computation.
 */
public final class GeomMath {

  /////////////////////////////////
  // CONSTANTS
  /////////////////////////////////

  /**
   * Math.PI / 4.0
   */
  public static final double QUARTER_PI = Math.PI / 4.0;

  /**
   * Math.PI / 2.0
   */
  public static final double HALF_PI = Math.PI / 2.0;

  /**
   * Math.PI
   */
  public static final double PI = Math.PI;

  /**
   * Math.PI * 3.0 / 2.0
   */
  public static final double ONE_AND_HALF_PI = 3.0 * Math.PI / 2.0;

  /**
   * Math.PI * 2.0
   */
  public static final double TWO_PI = 2.0 * Math.PI;


  /////////////////////////////////
  // PUBLIC METHODS
  /////////////////////////////////

  /**
   * Turn a cardinal number into an ordinal number.
   *
   * @param num the cardinal number
   * @return    the ordinal version of the given number
   */
  public static String ordinalize(int num) {
    String suffix;
    if(num % 100 == 11 || num % 100 == 12 || num % 100 == 13) {
      suffix = "th";
    } else {
      switch(num % 10) {
      case 1:
        suffix = "st";
        break;
      case 2:
        suffix = "nd";
        break;
      case 3:
        suffix = "rd";
        break;
      default:
        suffix = "th";
        break;
      }
    }
    return num + suffix;
  }

  /**
   * Get the "canonical" angle. This is used for recentering angles in
   * [0.0, 2*Pi).
   *
   * @param angle  the angle
   * @return an equivalent angle that is greater than or equal to 0.0
   *         and less than 2*PI
   */
  public static double canonicalAngle(double angle) {
    return angle - Math.floor(angle/TWO_PI) * TWO_PI;
  }

  /**
   * Find the angle of the heading to a point from a given starting point.
   *
   * @param p          the point to which to find the angle
   * @param startPoint the point from which to start
   * @return           the angle of the heading to the first point when
   *                   starting at the second point
   */
  public static double angleToPoint(Point2D p, Point2D startPoint) {
    return Math.atan2(p.getY() - startPoint.getY(),
                      p.getX() - startPoint.getX());
  }

  /**
   * Find a point displaced by the given distance in the given direction.
   *
   * @param p     the starting point
   * @param r     the distance to move from the point
   * @param theta the angle at which to move from the point
   * @return      the point displaced the given distance in the given
   *              direction from the original point
   */
  public static Point2D polarAdd(Point2D p, double r, double theta) {
    return new Point2D.Double(p.getX() + Math.cos(theta) * r,
                              p.getY() + Math.sin(theta) * r);
  }

  /**
   * Subtract two 2D vectors.
   *
   * @param p1 the first vector
   * @param p2 the second vector
   * @return   a Point representing the subtraction of the second vector from
   *           the first
   */
  public static Point2D subtract(Point2D p1, Point2D p2) {
     return new Point2D.Double(p1.getX() - p2.getX(), p1.getY() - p2.getY());
  }

  /**
   * Compute the dot product of two 2D vectors.
   *
   * @param p1 the first vector
   * @param p2 the second vector
   * @return the dot product of the two vectors
   */
  public static double dotProduct(Point2D p1, Point2D p2) {
     return (p1.getX() * p2.getX()) + (p1.getY() * p2.getY());
  }

  /**
   * Compute the cross product of two 2D vectors.
   *
   * @param p1 the first vector
   * @param p2 the second vector
   * @return the cross product of the two vectors
   */
  public static double crossProduct(Point2D p1, Point2D p2) {
     return (p1.getX() * p2.getY()) - (p2.getX() * p1.getY());
  }


  /**
   * Compute the intersection of two lines.
   *
   * @param l1 the first line
   * @param l2 the second line
   * @return   the intersection point of the two lines
   */
  public static Point2D findLineLineIntersection(Line2D l1, Line2D l2) {
    return findLineLineIntersection(l1.getX1(), l1.getY1(),
                                    l1.getX2(), l1.getY2(),
                                    l2.getX1(), l2.getY1(),
                                    l2.getX2(), l2.getY2());
  }

  /**
   * Compute the intersection of two lines defined by two points each.
   * Undefined behavior if the lines are parallel.
   *
   * @param x1 the x coordinate of the first point of the first line
   * @param y1 the y coordinate of the first point of the first line
   * @param x2 the x coordinate of the second point of the first line
   * @param y2 the y coordinate of the second point of the first line
   * @param x3 the x coordinate of the first point of the second line
   * @param y3 the y coordinate of the first point of the second line
   * @param x4 the x coordinate of the second point of the second line
   * @param y4 the y coordinate of the second point of the second line
   * @return   the intersection point of the two lines.
   */
  public static Point2D findLineLineIntersection(double x1, double y1,
                                                 double x2, double y2,
                                                 double x3, double y3,
                                                 double x4, double y4) {
    double d12 = determinant(x1, y1, x2, y2);
    double d34 = determinant(x3, y3, x4, y4);
    double diffx1x2 = x1 - x2;
    double diffy1y2 = y1 - y2;
    double diffx3x4 = x3 - x4;
    double diffy3y4 = y3 - y4;
    double d1234 = determinant(diffx1x2, diffy1y2, diffx3x4, diffy3y4);
    if(d1234 == 0) {
      // This is tricky: it means that one of the endpoints of one of the
      // lines is on the other line.  So, let's find which one it is and
      // if it is more than one, find the one closest to x1,y1.
      List<Point2D> candidates = new ArrayList<Point2D>(4);
      candidates.add(new Point2D.Double(x1,y1));
      candidates.add(new Point2D.Double(x2,y2));
      candidates.add(new Point2D.Double(x3,y3));
      candidates.add(new Point2D.Double(x4,y4));
      Line2D l1 = new Line2D.Double(x1,y1,x2,y2);
      Line2D l2 = new Line2D.Double(x3,y3,x4,y4);
      Point2D retval = null;
      double dist = Double.MAX_VALUE;
      // Go through all the Points
      for(Point2D p : candidates) {
        // If this Point is on both segments
        if(l1.ptSegDist(p) == 0 && l2.ptSegDist(p) == 0) {
          // And it is closer to x1,y1 than any of the other ones so far
          if(l1.getP1().distance(p) < dist) {
            // Then save it
            retval = p;
            // and its distance for future comparisons
            dist = l1.getP1().distance(p);
          }
        }
      }
      return retval;
    } else {
      double x = determinant(d12, diffx1x2, d34, diffx3x4) / d1234;
      double y = determinant(d12, diffy1y2, d34, diffy3y4) / d1234;
      return new Point2D.Double(x, y);
    }
  }

  /**
   * A class for storing the result of findLineLineIntersection.
   */
  public static class IntersectionPoint {
    private Point2D p;
    private double t1;
    private double t2;

    public IntersectionPoint(Point2D p, double t1, double t2) {
      this.p = p;
      this.t1 = t1;
      this.t2 = t2;
    }

    public Point2D getPoint() {
      return p;
    }

    public double getT1() {
      return t1;
    }

    public double getT2() {
      return t2;
    }
  }

  /**
   * Compute the intersection of two lines defined by two points each.
   * Undefined behavior if the lines are parallel.
   *
   * @param p1      an intercepting point of the first line
   * @param slope1  the slope of the first line
   * @param p2      an intercepting point of the second line
   * @param slope2  the slope of the second line
   *
   * @return the intersection point of the two lines, with the distances
   *         between the intersection points and the intercepting points.
   */
  public static IntersectionPoint findLineLineIntersection(Point2D p1,
                                                           double slope1,
                                                           Point2D p2,
                                                           double slope2)
  {
    double dx1 = Math.cos(slope1);
    double dy1 = Math.sin(slope1);
    double dx2 = Math.cos(slope2);
    double dy2 = Math.sin(slope2);

    double a1 = dx1;
    double a2 = -dx2;
    double a3 = dy1;
    double a4 = -dy2;
    double c1 = p2.getX() - p1.getX();
    double c2 = p2.getY() - p1.getY();

    double det = determinant(a1, a2, a3, a4);
    double t1 = (a4*c1-a2*c2)/det;
    double t2 = (-a3*c1+a1*c2)/det;
    Point2D p3 = new Point2D.Double(p1.getX()+t1*dx1, p1.getY()+t1*dy1);

    // make sure that p3 is indeed the intersection point
    assert Util.isDoubleEqual(p3.getX(), p2.getX()+t2*dx2);
    assert Util.isDoubleEqual(p3.getY(), p2.getY()+t2*dy2);

    return new IntersectionPoint(p3, t1, t2);
  }


//  /**
//   * Find the point on a line that is a distance of t from a given point p on
//   * the line.
//   *
//   * @param p      the given point
//   * @param slope  the slope of the line
//   * @param t      the distance between the points
//   * @return  the projected point
//   */
//  public static Point2D findPointInTheDirection(Point2D p, double slope, double t) {
//    return new Point2D.Double(p.getX()+t*Math.cos(slope),
//                              p.getY()+t*Math.sin(slope));
//  }


  /**
   * Given a polygonal Shape, return a list of lists of points that are the
   * vertices of the closed polygonal sub-shapes.
   *
   * @param s the polygonal Shape
   * @return  the lists of sub-shape vertices
   * @throws IllegalArgumentException if the Shape is not polygonal
   */
  public static List<List<Point2D>> polygonalSubShapeVertices(Shape s) {
    List<List<Point2D>> answ = new ArrayList<List<Point2D>>();
    List<Point2D> currList = new ArrayList<Point2D>();
    int lastMoveType = PathIterator.SEG_MOVETO;
    double[] pts = new double[6];
    for(PathIterator iter = s.getPathIterator(new AffineTransform());
        !iter.isDone(); iter.next()) {
      int moveType = iter.currentSegment(pts);
      switch(moveType) {
      case PathIterator.SEG_MOVETO:
        if(lastMoveType == PathIterator.SEG_CLOSE) {
          currList.remove(0);
        }
        break;
      case PathIterator.SEG_LINETO:
        break;
      case PathIterator.SEG_CLOSE:
        answ.add(currList);
        currList = new ArrayList<Point2D>();
        break;
      default:
        throw new IllegalArgumentException("Shape is not polygonal!");
      }
      currList.add(new Point2D.Double(pts[0], pts[1]));
      lastMoveType = moveType;
    }
    return answ;
  }

  /**
   * Given a polygonal Shape, return a list of segments describing its
   * perimeter.
   *
   * @param s the polygonal Shape
   * @return  a list of segments describing the perimeter of the Shape
   * @throws IllegalArgumentException if the Shape is not polygonal
   */
  public static List<Line2D> polygonalShapePerimeterSegments(Shape s) {
    List<Line2D> perimeterSegments = new ArrayList<Line2D>();
    for(List<Point2D> vtcs: polygonalSubShapeVertices(s)) {
      for(int i = 0; i < vtcs.size(); i++) {
        Point2D p1 = vtcs.get(i);
        Point2D p2 = vtcs.get((i + 1) % vtcs.size());
        perimeterSegments.add(new Line2D.Double(p1, p2));
      }
    }
    return perimeterSegments;
  }

  /**
   * Given a polygonal, non-overlapping Shape, return the areas of the closed
   * portions of the shape.  If the Shape is not polygonal or it overlaps
   * itself, behavior is undefined.
   *
   * @param s the polygonal Shape
   * @return  the areas of the closed portions of the Shape
   */
  public static List<Double> polygonalShapeAreas(Shape s) {
    List<Double> answ = new ArrayList<Double>();
    for(List<Point2D> vtcs: polygonalSubShapeVertices(s)) {
      double twiceCurrArea = 0;
      for(int i = 0; i < vtcs.size(); i++) {
        Point2D p1 = vtcs.get(i);
        Point2D p2 = vtcs.get((i + 1) % vtcs.size());
        twiceCurrArea += (p1.getX() * p2.getY() - p2.getX() * p1.getY());
      }
      answ.add(twiceCurrArea/2);
    }
    return answ;
  }

  /**
   * Given a polygonal Shape, return the centroid of the shape. If the
   * Shape is not polygonal, behavior is undefined.
   *
   * @param s the polygonal Shape
   * @return  the centroid of the Shape
   */
  public static Point2D polygonalShapeCentroid(Shape s) {
    List<Double> pAreas = polygonalShapeAreas(s);
    List<List<Point2D>> pVtcs = polygonalSubShapeVertices(s);
    double totalArea = 0;
    // Weighted x coordinate of centroid
    double cx = 0;
    // Weighted y coordinate of centroid
    double cy = 0;
    // For each subshape, get its centroid and add its weighted coordinates
    // too our totals (cx and cy).
    for(int i = 0; i < pAreas.size(); i++) {
      totalArea += Math.abs(pAreas.get(i));
      // Find the centroid of each sub-area
      double cxi = 0;
      double cyi = 0;
      List<Point2D> vtcs = pVtcs.get(i);
      for(int j = 0; j < vtcs.size(); j++) {
        Point2D p1 = vtcs.get(j);
        Point2D p2 = vtcs.get((j + 1) % vtcs.size());
        cxi += (p1.getX() + p2.getX()) *
                     (p1.getX() * p2.getY() - p2.getX() * p1.getY());
        cyi += (p1.getY() + p2.getY()) *
                     (p1.getX() * p2.getY() - p2.getX() * p1.getY());
      }
      // Centroid is (cxi/(6 * area), cyi/(6 * area)), but
      // we want to weight by area, so we are not going to divide by it.  We
      // do, however, need the sign to make sure it comes out with the correct
      // sign.
      cx += Math.signum(pAreas.get(i)) * cxi / 6;
      cy += Math.signum(pAreas.get(i)) * cyi / 6;
    }
    return new Point2D.Double(cx / totalArea, cy / totalArea);
  }

  /**
   * Find the centroid of a list of points.
   *
   * @param points the List of Points
   * @return       the centroid of the Points
   */
  public static Point2D centroid(List<Point2D> points) {
    double cx = 0;
    double cy = 0;
    for(Point2D p : points) {
      cx += p.getX();
      cy += p.getY();
    }
    return new Point2D.Double(cx / points.size(), cy / points.size());
  }

  /**
   * Find the Area corresponding to the provided Shape with all
   * holes filled in.  Takes the union of all the subareas according
   * to {@link #subareas(Shape s)}.
   *
   * @param s the Shape to fill in
   * @return  an Area representing the Shape with all holes filled in
   */
  public static Area filledArea(Shape s) {
    Area answ = new Area();
    for(Area a : subareas(s)) {
      answ.add(a);
    }
    return answ;
  }

  /**
   * Find all the subareas of a Shape.  This means that if a Shape
   * is defined as a Shape with a piece missing from it, this will
   * return both an Area for the full Shape as well as one for
   * the hole in the middle.  If multiple pieces exist, they will
   * each be returned individually.
   *
   * @param s the Shape to deconstruct.
   * @return  a List of subareas that make up the Shape
   */
  public static List<Area> subareas(Shape s) {
    List<Area> answ = new ArrayList<Area>();
    for(List<Point2D> vtcs: polygonalSubShapeVertices(s)) {
      GeneralPath path = new GeneralPath();
      path.moveTo((float)vtcs.get(0).getX(), (float)vtcs.get(0).getY());
      for(int i = 1; i < vtcs.size(); i++) {
        path.lineTo((float)vtcs.get(i).getX(), (float)vtcs.get(i).getY());
      }
      path.closePath();
      answ.add(new Area(path));
    }
    return answ;
  }

  /**
   * Solve the quadratic formula <i>ax<sup>2</sup> + bx + c = 0</i>
   * given coefficients a, b, and c, returning the minimum nonnegative
   * root, or the largest root if both are negative.
   *
   * @param a the coefficient of the <i>x<sup>2</sup></i> term
   * @param b the coefficient of the <i>x</i> term
   * @param c the constant term
   * @return  the minimum nonnegative root, or the largest root if both
   *          are negative
   */
  public static double quadraticFormula(double a, double b, double c) {
    double sqrtDiscriminant = Math.sqrt(Math.pow(b,2) - 4 * a * c);
    double plusAnsw = (-b + sqrtDiscriminant) / (2 * a);
    double minusAnsw = (-b - sqrtDiscriminant) / (2 * a);
    return minNonnegative(plusAnsw, minusAnsw);
  }

  /**
   * Determine whether two intervals overlap. This is determined by checking
   * if either of two things is true: the start of the first interval falls
   * inside the duration of the second interval, or the start of the second
   * interval falls within the duration of the first interval. If neither is
   * true, they cannot overlap.
   *
   * @param t1start the start of the first interval
   * @param t1end   the end of the first interval
   * @param t2start the start of the second interval
   * @param t2end   the end of the second interval
   * @return        whether the provided two intervals overlap
   */
  public static boolean intervalsOverlap(double t1start, double t1end,
                                         double t2start, double t2end) {
    return ((t2start >= t1start && t2start <= t1end) ||
            (t1start >= t2start && t1start <= t2end));
  }

  /**
   * Determine the angle between two angles.  This includes the cases where
   * the shortest angle between the two crosses the positive X axis.
   *
   * @param ang1 the first angle
   * @param ang2 the second angle
   * @return     the angle between the two given angles
   */
  public static double angleDiff(double ang1, double ang2) {
    double absoluteDifference = Math.abs(ang1 - ang2);
    return Math.min(absoluteDifference, 2 * Math.PI - absoluteDifference);
  }


  /////////////////////////////////
  // PRIVATE STATIC METHODS
  /////////////////////////////////

  /**
   * Compute the determinant of a 2x2 matrix.
   * | a b |
   * | c d |
   */
  private static double determinant(double a, double b, double c, double d) {
    return a * d - b * c;
  }

  /**
   * Return the smaller, nonnegative number of two numbers.  If neither is
   * positive, return the larger of the two.
   *
   * @param a the first of the two numbers
   * @param b the second of the two numbers
   * @return  the smaller of the two numbers that is positive, or the
   *          larger of the two if neither is positive
   */
  private static double minNonnegative(double a, double b) {
    if(a > b) {
      if(b >= 0) {
        return b;
      }
      return a;
    } else {
      if(a >= 0) {
        return a;
      }
      return b;
    }
  }


  /////////////////////////////////
  // CLASS CONSTRUCTORS
  /////////////////////////////////

  /** This class should never be instantiated. */
  private GeomMath(){};

}
