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

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.Shape;

/**
 * This is a base class for all lanes. Creates an ID system for lanes such
 * that all lanes will have a different integer ID. Also handles traffic
 * generation methods, and other things that are the same no matter
 * the implementation of the Lane.
 */
public interface Lane {

  /////////////////////////////////
  // PUBLIC METHODS
  /////////////////////////////////

  /**
   * Get the unique ID number of this Lane.
   *
   * @return the ID number of this lane
   */
  int getId();

  /**
   * Set the unique ID number of this Lane.
   *
   * @param id the ID number of this lane
   */
  void setId(int id);

  /**
   * Get the speed limit of this Lane, in meters per second.
   *
   * @return the speed limit of this Lane, in meters per second
   */
  double getSpeedLimit();

  /**
   * Get the LaneIM object that helps to locate the intersection managers on
   * a lane.
   *
   * @return the LaneIM object
   */
  LaneIM getLaneIM();


  /////////////////////////////////
  // PUBLIC METHODS
  /////////////////////////////////

  // next and prev lanes

  /**
   * Whether or not this Lane flows into another Lane.
   *
   * @return whether or not this Lane flows into another Lane
   */
  boolean hasNextLane();

  /**
   * Get the Lane into which this Lane leads, or <code>null</code> if none.
   *
   * @return the Lane into which this Lane leads, or <code>null</code> if none
   */
  Lane getNextLane();

  /**
   * Set the Lane into which this Lane leads.
   *
   * @param nextLane the Lane into which this Lane leads
   */
  void setNextLane(Lane nextLane);


  /**
   * Whether or not another Lane flows into this Lane.
   *
   * @return whether or not another Lane flows into this Lane
   */
  boolean hasPrevLane();

  /**
   * Get the Lane which leads into this Lane, or <code>null</code> if none.
   *
   * @return the Lane which leads into this Lane, or <code>null</code> if none
   */
  Lane getPrevLane();

  /**
   * Set the Lane which leads into this Lane.
   *
   * @param prevLane the Lane which leads into this Lane
   */
  void setPrevLane(Lane prevLane);


  /////////////////////////////////
  // PUBLIC METHODS
  /////////////////////////////////

  // neighor

  /**
   * Whether the Lane has another Lane immediately to its left travelling in
   * the same direction.  In other words, whether a vehicle can change lanes
   * to the left.
   *
   * @return whether the Lane has a neighbor to its left
   */
  boolean hasLeftNeighbor();

  /**
   * Get the left neighbor of this Lane.
   *
   * @return the left neighbor of this Lane, or <code>null</code> if none
   *         exists
   */
  Lane getLeftNeighbor();

  /**
   * Register another Lane as a left neighbor to this Lane.  This is like
   * having two lanes next to each other with a dotted line in between.
   *
   * @param ln the Lane to set as this Lane's left neighbor
   */
  void setLeftNeighbor(Lane ln);


  /**
   * Whether the Lane has another Lane immediately to its right travelling in
   * the same direction.  In other words, whether a vehicle can change lanes
   * to the right.
   *
   * @return whether the Lane has a neighbor to its right
   */
  boolean hasRightNeighbor();

  /**
   * Get the right neighbor of this Lane.
   *
   * @return the right neighbor of this Lane, or <code>null</code> if none
   *         exists
   */
  Lane getRightNeighbor();

  /**
   * Register another Lane as a right neighbor to this Lane.  This is like
   * having two lanes next to each other with a dotted line in between.
   *
   * @param ln the Lane to set as this Lane's right neighbor
   */
  void setRightNeighbor(Lane ln);


  /////////////////////////////////
  // PUBLIC METHODS
  /////////////////////////////////

  // lanes as lines

  /**
   * Get the length of this Lane.
   *
   * @return the length of this Lane, in meters
   */
  double getLength();

  /**
   * Get the starting point of the Lane.
   *
   * @return the starting point of the Lane
   */
  Point2D getStartPoint();

  /**
   * Get the ending point of the Lane.
   *
   * @return the ending point of the Lane
   */
  Point2D getEndPoint();

  /**
   * Get a point in the center of the lane at a particular normalized distance
   * from the start of the Lane.
   *
   * @param normalizedDistance the normalized distance (between 0 and 1) from
   *                           the start of the Lane
   * @return                   the point in the center of the Lane at the
   *                           given normalized distance
   */
  Point2D getPointAtNormalizedDistance(double normalizedDistance);

  // nearest positions

  /**
   * Get the point in the center of the Lane nearest to the provided point.
   *
   * @param p the point to which the nearest point in the center of the Lane
   *          should be found
   * @return  the point in the center of the Lane nearest to the given point
   */
  Point2D nearestPoint(Point2D p);

  /**
   * Get the distance from a point to the center of the Lane.
   *
   * @param pos the point from which to find the distance to the Lane
   * @return    the distance from the point to the center of the Lane
   */
  double nearestDistance(Point2D pos);

  /**
   * Get a point further down the Lane from the nearest point to the given
   * position.
   *
   * @param pos      the position from which to calculate the nearest point in
   *                 the Lane
   * @param leadDist the lead distance further down the Lane, in meters
   * @return         a point <code>leadDist</code> further down the Lane from
   *                 the closest point in the Lane to <code>pos</code>
   */
  Point2D getLeadPoint(Point2D pos, double leadDist);


  // distance along lane

  /**
   * Get the distance along the Lane from the start of the Lane to the Point
   * in the center of the Lane nearest the given Point.
   *
   * @param pos the point to which to find the distance along the Lane
   * @return    the distance along the Lane from the start of the Lane to the
   *            Point in the center of the Lane nearest the given Point
   */
  double distanceAlongLane(Point2D pos);

  /**
   * Get the amount of distance left in this Lane from the point on the Lane
   * nearest to the given point.  This is used so a vehicle can determine how
   * much of the Lane is left, given its current position.
   *
   * @param pos the point nearest which to find the distance to the end of the
   *            Lane
   * @return    the distance from the nearest point on the Lane to the given
   *            point to the end of the Lane.
   */
  double remainingDistanceAlongLane(Point2D pos);

  /**
   * Get the normalized distance to the point in the center of the lane
   * nearest the provided point. That is, given a point, find the nearest
   * point in the center of the lane, then find the proportion of the
   * way from the start point of the lane to the endpoint of the lane that
   * that point is.
   *
   * @param pos the point near to which to find the normalized distance
   * @return    the normalized distance to the point in the center of the lane
   *            nearest to the given point
   */
  double normalizedDistanceAlongLane(Point2D pos);

  /**
   * Get the normalized distance at the distance from the starting point.
   *
   * @param distance  the distance along the center of the lane.
   * @return the normalized distance along the center of the lane.
   */
  double normalizedDistance(double distance);

  // heading

  /**
   * Get the heading of this Lane at its starting point.
   *
   * @return the initial heading of the Lane, in radians
   */
  double getInitialHeading();

  /**
   * Get the heading of this Lane at its ending point.
   *
   * @return the terminal heading of the Lane, in radians
   */
  double getTerminalHeading();

  /**
   * Get the heading of the Lane at a particular normalized distance from the
   * start of the Lane.
   *
   * @param normalizedDistance the normalized distance (between 0 and 1) from
   *                           the start of the Lane
   * @return                   the heading of the lane at the given normalized
   *                           distance
   */
  double getHeadingAtNormalizedDistance(double normalizedDistance);


  // intersection point

  /**
   * The point at which the the given Line intersects the center of this Lane.
   *
   * @param l the Line with which to find the intersection
   * @return  the point at which the given Line intersects the center of this
   *          Lane, or <code>null</code> if it doesn't intersect
   */
  Point2D intersectionPoint(Line2D l);


  /////////////////////////////////
  // PUBLIC METHODS
  /////////////////////////////////

  // lanes as shapes

  /**
   * Get the width of this Lane, in meters.
   *
   * @return the width of this Lane, in meters
   */
  double getWidth();

  /**
   * Get a Shape describing this Lane, including its width.
   *
   * @return a Shape describing this Lane
   */
  Shape getShape();

  /**
   * Get a Shape describing an interval of this lane, using normalized
   * distances.
   *
   * @param startFraction the normalized distance from the start of the lane
   *                      to the start of the segment which the Shape
   *                      represents
   * @param endFraction   the normalized distance from the start of the lane
   *                      to the end of the segment which the Shape represents
   * @return              a Shape describing the segment
   * @throws IllegalArgumentException if <code>startFraction</code> or
   *                                  <code>endFraction</code> are not between
   *                                  0 and 1
   */
  Shape getShape(double startFraction, double endFraction);

  /**
   * Whether or not the provided point can be considered "in" this Lane.  This
   * is equivalent to determining whether the distance from the point to the
   * Lane is less than half of the width of the Lane.
   *
   * @param pos the point to check
   * @return    whether or not the point is in the Lane
   */
  boolean contains(Point2D pos);


  /**
   * Get a Shape representing the left border of this Lane.
   *
   * @return a Shape representing the left border of this Lane
   */
  Shape leftBorder();

  /**
   * Get a Shape representing the right border of this Lane.
   *
   * @return a Shape representing the right border of this Lane
   */
  Shape rightBorder();

  /**
   * The point at which the the given Line intersects the left border of this
   * Lane.
   *
   * @param l the Line with which to find the intersection
   * @return  the point at which the given Line intersects the left border of
   *          this Lane, or <code>null</code> if it doesn't intersect
   */
  Point2D leftIntersectionPoint(Line2D l);

  /**
   * The point at which the the given Line intersects the right border of this
   * Lane.
   *
   * @param l the Line with which to find the intersection
   * @return  the point at which the given Line intersects the right border of
   *          this Lane, or <code>null</code> if it doesn't intersect
   */
  Point2D rightIntersectionPoint(Line2D l);


}
