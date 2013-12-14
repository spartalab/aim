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

import java.awt.Shape;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;


/**
 * This is a base class for all Lanes. Creates an ID system for lanes such
 * that all lanes will have a different integer ID. Also handles traffic
 * generation methods, and other things that are the same no matter
 * the implementation of the Lane.
 */
public abstract class AbstractLane implements Lane {

  /////////////////////////////////
  // PRIVATE FIELDS
  /////////////////////////////////

  /** The actual ID of this lane. */
  private int id = -1;

  /** The speed limit of this Lane, in meters per second. */
  private double speedLimit;

  /** The Lane this lane leads into, if any. */
  private Lane nextLane;

  /** The Lane that leads into this one, if any. */
  private Lane prevLane;

  /** The right neighbor of this Lane, if any. Otherwise, <code>null</code>. */
  private Lane rightNeighbor;

  /** The left neighbor of this Lane, if any.  Otherwise, <code>null</code>. */
  private Lane leftNeighbor;

  /** The LaneIM object that helps to locate the intersection managers. */
  private LaneIM laneIM;

  /////////////////////////////////
  // CONSTRUCTORS
  /////////////////////////////////

  /**
   * Create a new Lane.
   */
  public AbstractLane(double speedLimit) {
    this.speedLimit = speedLimit;
    this.laneIM = new LaneIM(this);
  }


  /////////////////////////////////
  // PUBLIC METHODS
  /////////////////////////////////

  /**
   * {@inheritDoc}
   */
  @Override
  public int getId() {
    return id;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setId(int id) {
    this.id = id;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public double getSpeedLimit() {
    return speedLimit;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public LaneIM getLaneIM() {
    return laneIM;
  }

  /////////////////////////////////
  // PUBLIC METHODS
  /////////////////////////////////

  // the adjacent lanes

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean hasNextLane() {
    return nextLane != null;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Lane getNextLane() {
    return nextLane;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setNextLane(Lane nextLane) {
    this.nextLane = nextLane;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean hasPrevLane() {
    return prevLane != null;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Lane getPrevLane() {
    return prevLane;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setPrevLane(Lane prevLane) {
    this.prevLane = prevLane;
  }


  /////////////////////////////////
  // PUBLIC METHODS
  /////////////////////////////////

  // neighor

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean hasLeftNeighbor() {
    return (leftNeighbor != null);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Lane getLeftNeighbor() {
    return leftNeighbor;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setLeftNeighbor(Lane ln) {
    leftNeighbor = ln;
  }


  /**
   * {@inheritDoc}
   */
  @Override
  public boolean hasRightNeighbor() {
    return (rightNeighbor != null);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Lane getRightNeighbor() {
    return rightNeighbor;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setRightNeighbor(Lane ln) {
    rightNeighbor = ln;
  }


  /////////////////////////////////
  // PUBLIC METHODS
  /////////////////////////////////

  // lanes as lines

  /**
   * {@inheritDoc}
   */
  @Override
  public abstract double getLength();

  /**
   * {@inheritDoc}
   */
  @Override
  public abstract Point2D getStartPoint();

  /**
   * {@inheritDoc}
   */
  @Override
  public abstract Point2D getEndPoint();

  /**
   * {@inheritDoc}
   */
  @Override
  public abstract Point2D getPointAtNormalizedDistance(double
                                                       normalizedDistance);

  // nearest positions

  /**
   * {@inheritDoc}
   */
  @Override
  public abstract Point2D nearestPoint(Point2D p);

  /**
   * {@inheritDoc}
   */
  @Override
  public abstract double nearestDistance(Point2D pos);

  /**
   * {@inheritDoc}
   */
  @Override
  public abstract Point2D getLeadPoint(Point2D pos, double leadDist);


  // distance along lane

  /**
   * {@inheritDoc}
   */
  @Override
  public abstract double distanceAlongLane(Point2D pos);

  /**
   * {@inheritDoc}
   */
  @Override
  public abstract double remainingDistanceAlongLane(Point2D pos);

  /**
   * {@inheritDoc}
   */
  @Override
  public abstract double normalizedDistanceAlongLane(Point2D pos);


  // heading

  /**
   * {@inheritDoc}
   */
  @Override
  public abstract double getInitialHeading();

  /**
   * {@inheritDoc}
   */
  @Override
  public abstract double getTerminalHeading();

  /**
   * {@inheritDoc}
   */
  @Override
  public abstract double getHeadingAtNormalizedDistance(double
                                                        normalizedDistance);


  // intersection point

  /**
   * {@inheritDoc}
   */
  @Override
  public abstract Point2D intersectionPoint(Line2D l);


  /////////////////////////////////
  // PUBLIC METHODS
  /////////////////////////////////

  // lanes as shapes

  /**
   * {@inheritDoc}
   */
  @Override
  public abstract double getWidth();

  /**
   * {@inheritDoc}
   */
  @Override
  public abstract Shape getShape();

  /**
   * {@inheritDoc}
   */
  @Override
  public abstract Shape getShape(double startFraction, double endFraction);

  /**
   * {@inheritDoc}
   */
  @Override
  public abstract boolean contains(Point2D pos);

  /**
   * {@inheritDoc}
   */
  @Override
  public abstract Shape leftBorder();

  /**
   * {@inheritDoc}
   */
  @Override
  public abstract Shape rightBorder();

  /**
   * {@inheritDoc}
   */
  @Override
  public abstract Point2D leftIntersectionPoint(Line2D l);

  /**
   * {@inheritDoc}
   */
  @Override
  public abstract Point2D rightIntersectionPoint(Line2D l);


}
