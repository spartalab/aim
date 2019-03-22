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
package aim4.map.track;

import aim4.config.Constants;
import java.awt.Shape;
import java.awt.geom.Arc2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import aim4.util.GeomMath;
import aim4.util.Util;

/**
 * The arc-shape track.
 */
public class ArcTrack implements Track {

  /////////////////////////////////
  // NESTED CLASSES
  /////////////////////////////////

  /**
   * A position of the arc-shape track.
   */
  public class Position implements TrackPosition {

    /** The angle from the center to the position */
    double theta;

    /**
     * Create a position object at the given distance from the starting
     * waypoint on this track
     *
     * @param dist  the distance
     */
    public Position(double dist) {
      if (thetaDiff >= 0) {
        this.theta = dist / radius;
      } else {
        this.theta = - dist / radius;
      }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getX() {
      return center.getX() + radius * Math.cos(thetaStart + theta);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getY() {
      return center.getY() + radius * Math.sin(thetaStart + theta);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getTangentSlope() {
      if (thetaDiff >= 0) {
        return GeomMath.canonicalAngle(thetaStart + theta + GeomMath.HALF_PI);
      } else {
        return GeomMath.canonicalAngle(thetaStart + theta - GeomMath.HALF_PI);
      }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double move(double dist) {
      if (theta != thetaDiff) {
        if (thetaDiff >= 0) {
          theta += dist / radius;
          if (theta <= thetaDiff) {
            return 0;
          } else {
            double d = (theta - thetaDiff) * radius;
            theta = thetaDiff;
            return d;
          }
        } else {
          theta -= dist / radius;
          if (theta >= thetaDiff) {
            return 0;
          } else {
            double d = (thetaDiff - theta) * radius;
            theta = thetaDiff;
            return d;
          }
        }
      } else {
        return dist;
      }
    }

    /////////////////////////////////
    // DEBUG
    /////////////////////////////////

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
      return "ArcTrack.Position(" +
             "theta=" + Constants.TWO_DEC.format(theta) + ")";
    }

  }

  /////////////////////////////////
  // PRIVATE FIELDS
  /////////////////////////////////

  /**
   * The reference of the given end points of this line
   */
  private WayPoint p1, p2;

  /**
   * The center of the arc
   */
  private Point2D center;

  /**
   * The radius of the arc
   */
  private double radius;

  /**
   * The starting angle
   */
  private double thetaStart;

  /**
   * The ending angle
   */
  private double thetaEnd;

  /**
   * The difference between the starting angle and the ending angle
   * in clockwise direction.
   */
  private double thetaDiff;

  /**
   * The arc representing this track segment
   */
  private Arc2D arc;


  /////////////////////////////////
  // CONSTRUCTORS
  /////////////////////////////////

  /**
   * Create an arc-shape track segment.
   *
   * @param p1                    the starting point
   * @param p2                    the ending point
   * @param center                the center of the arc
   * @param isClockwiseDirection  whether the arc should draw in the clockwise
   *                              direction
   */
  public ArcTrack(WayPoint p1, WayPoint p2, Point2D center,
                  boolean isClockwiseDirection) {
    this.p1 = p1;
    this.p2 = p2;
    this.center = center;

    radius = center.distance(p1);
    assert radius > 0;
    assert Util.isDoubleEqual(radius, center.distance(p2));

    thetaStart = GeomMath.canonicalAngle(GeomMath.angleToPoint(p1, center));
    thetaEnd = GeomMath.canonicalAngle(GeomMath.angleToPoint(p2, center));
    if (thetaEnd >= thetaStart) {
      thetaDiff = thetaEnd - thetaStart;
    } else {
      thetaDiff = (GeomMath.TWO_PI + thetaEnd) - thetaStart;
    }
    if (!isClockwiseDirection) {
      thetaDiff -= GeomMath.TWO_PI;
    }

    // create the shape

    Rectangle2D bounds =
      new Rectangle2D.Double(center.getX() - radius,
                             center.getY() - radius,
                             2 * radius,
                             2 * radius);

    // The unit of the angles must be in degree in order to create an Arc2D
    this.arc = new Arc2D.Double(bounds,
                                Math.toDegrees(-thetaStart),
                                Math.toDegrees(-thetaDiff),
                                Arc2D.OPEN);
  }

  /////////////////////////////////
  // PUBLIC METHODS
  /////////////////////////////////

  /**
   * {@inheritDoc}
   */
  @Override
  public WayPoint getStartWayPoint() {
    return p1;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public WayPoint getEndWayPoint() {
    return p2;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public double getLength() {
    return Math.abs(thetaDiff * radius);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public TrackPosition getPosition(double dist) {
    if (0.0 <= dist && dist <= getLength()) {
      return new Position(dist);
    } else {
      return null;
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Shape getShape() {
    return arc;
  }

}
