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
package aim4.map.lane;

import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.Shape;

import aim4.util.GeomMath;

/**
 * A lane class that can be represented by a directed line segment.
 */
public class LineSegmentLane extends AbstractLane {

  /////////////////////////////////
  // PRIVATE FIELDS
  /////////////////////////////////

  /**
   * The length of the Lane.  Won't change, so no point in recalculating every
   * time.
   */
  private double length;

  /**
   * The square of the lane length.  Used to speed up some calculations.
   */
  private double squaredLaneLength;

  /**
   * The width of the Lane, in meters.
   */
  private double width;

  /**
   * Half the width of the Lane, in meters.
   */
  private double halfWidth;

  /**
   * The line segment that represents the lane
   */
  private Line2D line;

  /**
   * A Shape describing the lane, including its width.
   */
  private Shape laneShape;

  /**
   * A vector representing the lane.  Used to speed up some calculations.
   */
  private Point2D laneVector;

  /**
   * The heading of the Lane, in radians. 0 represents due east.
   */
  private double heading;

  /**
   * The line that represents the left border of this Lane.
   */
  private Line2D leftBorder;

  /**
   * The line that represents the right border of this Lane.
   */
  private Line2D rightBorder;


  /////////////////////////////////
  // CONSTRUCTORS
  /////////////////////////////////

  /**
   * Constructs a line-segment lane using a Line.
   *
   * @param line       the line segment representing the center of the Lane
   * @param width      the width of the Lane, in meters
   * @param speedLimit the speed limit of the Lane, in meters per second
   */
  public LineSegmentLane(Line2D line, double width, double speedLimit) {
    super(speedLimit);

    this.line = line;
    this.width = width;
    this.halfWidth = width/2;
    laneVector = GeomMath.subtract(line.getP2(), line.getP1());
    squaredLaneLength = GeomMath.dotProduct(laneVector, laneVector);
    length = Math.sqrt(squaredLaneLength);
    heading = GeomMath.canonicalAngle(Math.atan2(line.getY2() - line.getY1(),
                                                 line.getX2() - line.getX1()));
    laneShape = calculateLaneShape();
    // Figure out the lines that represent the left and right borders.
    double xDifferential = halfWidth * Math.cos(heading + Math.PI/2);
    double yDifferential = halfWidth * Math.sin(heading + Math.PI/2);
    leftBorder = new Line2D.Double(line.getX1() - xDifferential,
                                   line.getY1() - yDifferential,
                                   line.getX2() - xDifferential,
                                   line.getY2() - yDifferential);
    rightBorder = new Line2D.Double(line.getX1() + xDifferential,
                                    line.getY1() + yDifferential,
                                    line.getX2() + xDifferential,
                                    line.getY2() + yDifferential);
  }

  /**
   * Constructs a line-segment lane using two points.
   *
   * @param p1         the starting Point of the Lane
   * @param p2         the ending Point of the Lane
   * @param width      the width of the Lane, in meters
   * @param speedLimit the speed limit of the Lane, in meters per second
   */
  public LineSegmentLane(Point2D p1, Point2D p2,
                         double width, double speedLimit) {
    // Call the previous version after making a Line...
    this(new Line2D.Double(p1, p2), width, speedLimit);
  }

  /**
   * Constructs a line-segment lane using two sets of coordinates.
   *
   * @param x1         the x coordinate of the starting point of the Lane
   * @param y1         the y coordinate of the starting point of the Lane
   * @param x2         the x coordinate of the ending point of the Lane
   * @param y2         the y coordinate of the ending point of the Lane
   * @param width      the width of the Lane, in meters
   * @param speedLimit the speed limit of the Lane, in meters per second
   */
  public LineSegmentLane(double x1, double y1, double x2, double y2,
                         double width, double speedLimit) {
    // Call the first version after making a Line...
    this(new Line2D.Double(x1, y1, x2, y2), width, speedLimit);
  }


  /////////////////////////////////
  // PUBLIC METHODS
  /////////////////////////////////

  // lanes as lines

  /**
   * {@inheritDoc}
   */
  @Override
  public double getLength() {
    return length;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Point2D getStartPoint() {
    return line.getP1();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Point2D getEndPoint() {
    return line.getP2();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Point2D getPointAtNormalizedDistance(double normalizedDistance) {
    return new Point2D.Double(line.getX1() + normalizedDistance *
                                             laneVector.getX(),
                              line.getY1() + normalizedDistance *
                                             laneVector.getY());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Point2D nearestPoint(Point2D p) {
    double fraction = normalizedDistanceAlongLane(p);
    if(fraction <= 0) {
      return line.getP1();
    } else if (fraction >= 1) {
      return line.getP2();
    } else {
      return getPointAtNormalizedDistance(fraction);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public double nearestDistance(Point2D pos) {
     return line.ptSegDist(pos);
  }

  /**
  /**
   * {@inheritDoc}
   */
  @Override
  public Point2D getLeadPoint(Point2D pos, double leadDist) {
    double fraction = normalizedDistanceAlongLane(pos);
    Point2D pp = getPointAtNormalizedDistance(fraction);
    return new Point2D.Double(pp.getX() + leadDist * Math.cos(heading),
                              pp.getY() + leadDist * Math.sin(heading));
  }


  // distance along lane

  /**
   * {@inheritDoc}
   */
  @Override
  public double distanceAlongLane(Point2D pos) {
    Point2D w = GeomMath.subtract(pos, line.getP1());  // Really a vector
    // If A is the vector from P1 to pos and B is the laneVector
    // then what we want is the length of the projection of A onto B
    // divided by the length of B.
    //
    // The length of the projection of A onto B is |A| cos(theta)
    //
    // A dot B = |A||B| cos(theta)
    // What we want is (A dot B) / |B|
    return (GeomMath.dotProduct(w, laneVector) / length);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public double remainingDistanceAlongLane(Point2D pos) {
    return length - distanceAlongLane(pos);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public double normalizedDistanceAlongLane(Point2D pos) {
    Point2D w = GeomMath.subtract(pos, line.getP1());  // Really a vector
    // If A is the vector from P1 to pos and B is the laneVector
    // then what we want is the length of the projection of A onto B
    // divided by the length of B.
    //
    // The length of the projection of A onto B is |A| cos(theta)
    //
    // A dot B = |A||B| cos(theta)
    // B dot B = squaredLaneLength = |B||B|
    // (A dot B)/(B dot B) = (A dot B)/(squaredLaneLength)
    // This is exactly what we want: (|A| cos (theta))/|B|
    return (GeomMath.dotProduct(w, laneVector) / squaredLaneLength);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public double normalizedDistance(double distance) {
    return distance / length;
  }


  // heading

  /**
   * {@inheritDoc}
   */
  @Override
  public double getInitialHeading() {
    return heading;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public double getTerminalHeading() {
    return heading;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public double getHeadingAtNormalizedDistance(double normalizedDistance) {
    return heading;
  }

  // intersection point

  /**
   * {@inheritDoc}
   */
  @Override
  public Point2D intersectionPoint(Line2D l) {
    if(line.intersectsLine(l)) {
      return GeomMath.findLineLineIntersection(line, l);
    }
    return null;
  }


  /////////////////////////////////
  // PUBLIC METHODS
  /////////////////////////////////

  // lanes as shapes

  /**
   * {@inheritDoc}
   */
  @Override
  public double getWidth() {
    return width;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Shape getShape() {
    return laneShape;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Shape getShape(double startFraction, double endFraction) {
    if(startFraction < 0 || endFraction < 0 ||
       startFraction > 1 || endFraction > 1) {
      throw new IllegalArgumentException("Normalized distances must be" +
                                         " between 0 and 1! Got: " +
                                         "startFraction = " + startFraction +
                                         ", endFraction = " + endFraction +
                                         ".");
    }
    // This is cute in that it works even if startFraction > endFraction
    GeneralPath result = new GeneralPath();
    double xDifferential = halfWidth * Math.cos(heading + Math.PI/2);
    double yDifferential = halfWidth * Math.sin(heading + Math.PI/2);
    Point2D p1 = getPointAtNormalizedDistance(startFraction);
    Point2D p2 = getPointAtNormalizedDistance(endFraction);
    result.moveTo((float) (p1.getX() + xDifferential),
                  (float) (p1.getY() + yDifferential));
    result.lineTo((float) (p2.getX() + xDifferential),
                  (float) (p2.getY() + yDifferential));
    result.lineTo((float) (p2.getX() - xDifferential),
                  (float) (p2.getY() - yDifferential));
    result.lineTo((float) (p1.getX() - xDifferential),
                  (float) (p1.getY() - yDifferential));
    result.closePath();
    return result;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean contains(Point2D pos) {
      return (nearestDistance(pos) < halfWidth);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Shape leftBorder() {
    return leftBorder;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Shape rightBorder() {
    return rightBorder;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Point2D leftIntersectionPoint(Line2D l) {
    if(leftBorder.intersectsLine(l)) {
      return GeomMath.findLineLineIntersection(leftBorder, l);
    }
    return null;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Point2D rightIntersectionPoint(Line2D l) {
    if(rightBorder.intersectsLine(l)) {
      return GeomMath.findLineLineIntersection(rightBorder, l);
    }
    return null;
  }



  /////////////////////////////////
  // PRIVATE METHODS
  /////////////////////////////////

  /**
   * Calculate lane shape, including the width of the lane.
   *
   * @return a Shape describing the lane, including its width.
   */
  private Shape calculateLaneShape() {
    GeneralPath result = new GeneralPath();
    double xDifferential = halfWidth * Math.cos(heading + Math.PI/2);
    double yDifferential = halfWidth * Math.sin(heading + Math.PI/2);
    result.moveTo((float) (line.getX1() + xDifferential),
                  (float) (line.getY1() + yDifferential));
    result.lineTo((float) (line.getX2() + xDifferential),
                  (float) (line.getY2() + yDifferential));
    result.lineTo((float) (line.getX2() - xDifferential),
                  (float) (line.getY2() - yDifferential));
    result.lineTo((float) (line.getX1() - xDifferential),
                  (float) (line.getY1() - yDifferential));
    result.closePath();
    return result;
  }

}
