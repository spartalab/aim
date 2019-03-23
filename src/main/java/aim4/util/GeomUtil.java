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

import java.awt.Shape;
import java.awt.geom.Arc2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;

/**
 * Utility methods for geometric manipulation.
 */
public class GeomUtil {

  /////////////////////////////////
  // PUBLIC METHODS
  /////////////////////////////////

  /**
   * Convert a line into a rectangle
   *
   * @param line       the line
   * @param halfWidth  half of the width of the rectangle
   *
   * @return the rectangle
   */
  public Shape convertLineIntoRectangle(Line2D line, double halfWidth) {
    double heading = GeomMath.canonicalAngle(
                         Math.atan2(line.getY2() - line.getY1(),
                                    line.getX2() - line.getX1()));
    GeneralPath result = new GeneralPath();
    double xDifferential = halfWidth * Math.cos(heading + Math.PI/2);
    double yDifferential = halfWidth * Math.sin(heading + Math.PI/2);
    result.moveTo((line.getX1() + xDifferential),
                  (line.getY1() + yDifferential));
    result.lineTo((line.getX2() + xDifferential),
                  (line.getY2() + yDifferential));
    result.lineTo((line.getX2() - xDifferential),
                  (line.getY2() - yDifferential));
    result.lineTo((line.getX1() - xDifferential),
                  (line.getY1() - yDifferential));
    result.closePath();
    return result;
  }


  /**
   * Convert an arc into a fan (i.e., an arc-shape shape with width)
   *
   * @param arc        the arc
   * @param halfWidth  half of the width of the fan
   *
   * @return the fan
   */
  public Shape convertArcIntoFan(Arc2D arc, double halfWidth) {
    // create the outer and inner arcs
    double x = arc.getX();
    double y = arc.getY();
    double w = arc.getWidth();
    double h = arc.getHeight();
    double start = arc.getAngleStart();
    double extent = arc.getAngleExtent();

    Arc2D arc1 = new Arc2D.Double(x-halfWidth, y-halfWidth,
                                  w+2*halfWidth, h+2*halfWidth,
                                  start, extent, Arc2D.OPEN);
    Arc2D arc2 = new Arc2D.Double(x+halfWidth, y+halfWidth,
                                  w-2*halfWidth, h-2*halfWidth,
                                  start+extent, -extent, Arc2D.OPEN);

    // create the shape
    GeneralPath result = new GeneralPath();
    result.append(arc1, false);
    result.append(arc2, true);
    result.closePath();

    return result;
  }


  /**
   * Construct a shape out of a set of corner points.
   *
   * @param points  a set of points at the corners of the shape
   * @return        the shape
   */
  public static Shape convertPointsToShape(Point2D[] points) {
    GeneralPath result = new GeneralPath();
    result.moveTo((float)points[0].getX(), (float)points[0].getY());
    for(int i = 1; i < points.length; i++) {
      result.lineTo((float)points[i].getX(), (float)points[i].getY());
    }
    result.closePath();
    return result;
  }


  /////////////////////////////////
  // CLASS CONSTRUCTORS
  /////////////////////////////////


  /** This class should never be instantiated. */
  private GeomUtil(){};


}
