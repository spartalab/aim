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
package aim4.config;

import java.awt.Color;
import java.awt.geom.Point2D;

/**
 * A structure to hold information regarding debugging display information.
 * These allow a vehicle to sort of "point" at things that will show up in
 * the visualizer, with optional text at that point.
 */
public class DebugPoint {

  /////////////////////////////////
  // PRIVATE FIELDS
  /////////////////////////////////

  /**
   * The default color that will be used to display DebugPoints graphically.
   */
  private static final Color DEFAULT_COLOR = Color.BLACK;

  /**
   * The actual point.
   */
  private Point2D point;
  /**
   * The point from which to start the line.  If null, no line is indicated.
   */
  private Point2D startPoint;
  /**
   * A message to print at the debug point.  If null, no message is
   * indicated.
   */
  private String msg;

  /**
   * The color of the line to the point and text.
   */
  private Color color;

  /////////////////////////////////
  // CLASS CONSTRUCTORS
  /////////////////////////////////

  /**
   * Class constructor for only a point with no line.
   *
   * @param point the location of the debug point
   */
  public DebugPoint(Point2D point) {
    this(point, null, null, DEFAULT_COLOR);
  }

  /**
   * Class constructor for text only.
   *
   * @param msg the text for this text-only DebugPoint
   */
  public DebugPoint(String msg) {
    this(null, null, msg, DEFAULT_COLOR);
  }

  /**
   * Class constructor for a point with a line.
   *
   * @param point      the location of the debug point
   * @param startPoint the point from which to draw a line to the debug
   *                   point
   */
  public DebugPoint(Point2D point, Point2D startPoint) {
    this(point, startPoint, null, DEFAULT_COLOR);
  }

  /**
   * Class constructor for a point with text.
   *
   * @param point the location of the debug point
   * @param msg   the text to display alongside the debug point
   */
  public DebugPoint(Point2D point, String msg) {
    this(point, null, msg, DEFAULT_COLOR);
  }

  /**
   * Class constructor for a colored point with text.
   *
   * @param point the location of the debug point
   * @param msg   the text to display alongside the debug point
   * @param color the color with which to display the point and text
   */
  public DebugPoint(Point2D point, String msg, Color color) {
    this(point, null, msg, color);
  }

  /**
   * Class constructor for a line with text.
   *
   * @param point      the location of the debug point
   * @param startPoint the point from which to draw a line to the debug
   *                   point
   * @param msg        the text to display alongside the debug point
   */
  public DebugPoint(Point2D point, Point2D startPoint,
                    String msg) {
    this(point, startPoint, msg, DEFAULT_COLOR);
  }

  /**
   * Class constructor for a colored line.
   *
   * @param point      the location of the debug point
   * @param startPoint the point from which to draw a line to the debug
   *                   point
   * @param color      the color with which to display the point and line
   */
  public DebugPoint(Point2D point, Point2D startPoint,
                    Color color) {
    this(point, startPoint, null, color);
  }

  /**
   * Class constructor for a colored line with text.
   *
   * @param point      the location of the debug point
   * @param startPoint the point from which to draw a line to the debug
   *                   point
   * @param msg        the text to display alongside the debug point
   * @param color      the color with which to display the point, line, and
   *                   text
   */
  public DebugPoint(Point2D point, Point2D startPoint,
                    String msg, Color color) {
    this.point = point;
    this.startPoint = startPoint;
    this.msg = msg;
    this.color = color;
  }

  /////////////////////////////////
  // PUBLIC METHODS
  /////////////////////////////////

  /**
   * Whether or not this DebugPoint has associated text.
   *
   * @return whether or not this DebugPoint has associated text
   */
  public boolean hasText() {
    return (msg != null);
  }

  /**
   * Whether or not this DebugPoint has a start point.
   *
   * @return whether or not this DebugPoint has a start point
   */
  public boolean hasStartPoint() {
    return (startPoint != null);
  }

  /**
   * Get the point associated with this DebugPoint.
   *
   * @return the point associated with this DebugPoint
   */
  public Point2D getPoint() {
    return point;
  }

  /**
   * Get the start point associated with this DebugPoint.
   *
   * @return the start point associated with this DebugPoint
   */
  public Point2D getStartPoint() {
    return startPoint;
  }

  /**
   * Get the color of this DebugPoint.
   *
   * @return the color of this DebugPoint
   */
  public Color getColor() {
    return color;
  }

  /**
   * Get the text associated with this DebugPoint.
   *
   * @return the text associated with this DebugPoint
   */
  public String getText() {
    return msg;
  }

}