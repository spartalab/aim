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
import java.awt.geom.Line2D;


/**
 * A track segment that is a straight line
 */
public class LineTrack implements Track {

  /////////////////////////////////
  // NESTED CLASSES
  /////////////////////////////////

  /**
   * A position on this track.
   */
  public class Position implements TrackPosition {

    /**
     * The normalized path length of the position
     */
    private double normDist;

    /**
     * The x-coordinate of the position
     */
    private double x;

    /**
     * The y-coordinate of the position
     */
    private double y;

    /**
     * Create a position object at the given distance from the starting
     * waypoint on this track
     *
     * @param dist  the distance
     */
    public Position(double dist) {
      this.normDist = dist / length;
      this.x = p1.getX() + xLen * normDist;
      this.y = p1.getY() + yLen * normDist;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getX() {
      return x;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getY() {
      return y;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getTangentSlope() {
      return slope;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double move(double dist) {
      if (normDist < 1.0) {
        normDist += dist / length;
        if (normDist > 1.0) {
          x = p2.getX();
          y = p2.getY();
          return dist - ((normDist - 1.0) * length);
        } else {
          x = p1.getX() + xLen * normDist;
          y = p1.getY() + yLen * normDist;
          return 0.0;
        }
      } else {
        return dist;  // the end waypoint has been reached.
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
      return "LineTrack.Position(" +
             "x=" + Constants.ONE_DEC.format(x) + ", " +
             "y=" + Constants.ONE_DEC.format(y) + ", " +
             "normDist=" + Constants.TWO_DEC.format(normDist) + ")";
    }

  }


  /////////////////////////////////
  // PRIVATE FIELDS
  /////////////////////////////////

  /**
   * The references to the given end points of this line.
   */
  private WayPoint p1, p2;

  /**
   * The length of the line.
   */
  private double length;

  /**
   * The projected length of the line on x-axis.
   */
  private double xLen;

  /**
   * The projected length of the line on y-axis.
   */
  private double yLen;

  /**
   * The slope of the line
   */
  private double slope;

  /**
   * The line representing this track segment.
   */
  private Line2D.Double line;


  /////////////////////////////////
  // CONSTRUCTORS
  /////////////////////////////////

  /**
   * Create a track segment that is a straight line
   *
   * @param p1  the starting point
   * @param p2  the ending point
   */
  public LineTrack(WayPoint p1, WayPoint p2) {
    this.p1 = p1;   // maintain the reference to the given points
    this.p2 = p2;
    this.line = new Line2D.Double(p1, p2);

    this.xLen = p2.getX() - p1.getX();
    this.yLen = p2.getY() - p1.getY();

    this.length = Math.sqrt(xLen * xLen + yLen * yLen);
    this.slope = Math.atan2(yLen, xLen);
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
    return length;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public TrackPosition getPosition(double dist) {
    if (0.0 <= dist && dist <= length) {
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
    return line;
  }

}
