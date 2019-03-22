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
package aim4.vehicle;

import java.awt.Shape;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;

import aim4.config.SimConfig;

/**
 * The characteristics of the vehicle
 */
public class VehicleSpec {

  /////////////////////////////////
  // PRIVATE FIELDS
  /////////////////////////////////

  /**
   * The name of the specification
   */
  private String name;
  /**
   * The Vehicle's maximum acceleration in meters per second squared.
   */
  private double maxAcceleration;
  /**
   * The Vehicle's maximum deceleration (or alternatively,
   * minimum acceleration) in meters per second squared.
   */
  private double maxDeceleration;
  /**
   * The Vehicle's maximum velocity in meters per second.
   */
  private double maxVelocity;
  /**
   * The Vehicle's minimum velocity in meters per second.
   */
  private double minVelocity;
  /**
   * The Vehicle's length, in meters.
   */
  private double length;
  /**
   * The Vehicle's width, in meters.
   */
  private double width;
  /**
   * The distance from the front of the Vehicle to the front axle, in meters.
   */
  private double frontAxleDisplacement;
  /**
   * The distance from the front of the Vehicle to the rear axle, in meters.
   */
  private double rearAxleDisplacement;
  /**
   * The distance from the center each axle to the wheels on that axle, in
   * meters.
   */
  private double wheelSpan;
  /**
   * The radius of the wheels, in meters.
   */
  private double wheelRadius;
  /**
   * The width of the wheels, in meters.
   */
  private double wheelWidth;
  /**
   * The maximum angle away from straight ahead to which the front wheels can
   * be rotated, in radians.
   */
  private double maxSteeringAngle;
  /**
   * The maximum angle through which the wheels can be turned in one second,
   * in radians.
   */
  private double maxTurnPerSecond;

   // derived properties

  /**
   * The maximum angle through which the wheels can be turned in one time
   * step, in radians.
   */
  private double maxTurnPerCycle;
  /**
   * Simple variable to cache computation of the vehicle's radius.  Set on
   * construction.
   */
  private double radius;
  /**
   * Simple variable to cache computation of half the vehicle's width.  Set on
   * construction.
   */
  private double halfWidth;
  /**
   * Simple variable to cache computation of half the vehicle's length.  Set
   * on construction.
   */
  private double halfLength;
  /**
   * Get the wheelbase of this Vehicle.
   */
  private double wheelbase;

  /////////////////////////////////
  // CONSTRUCTORS
  /////////////////////////////////

  /**
   * Class constructor that overrides most of the defaults and also allows the
   * user to specify the Vehicle's VIN instead of using the default generator.
   *
   * @param name                  the name of this specification
   * @param maxAcceleration       the Vehicle's maximum acceleration, in
   *                              meters per second squared
   * @param maxDeceleration       the Vehicle's maximum decceleration, in
   *                              meters per second squared
   * @param maxVelocity           the Vehicle's maximum velocity, in meters
   *                              per second
   * @param minVelocity           the Vehicle's minimum velocity, in meters
   *                              per second
   * @param length                the Vehicle's length, in meters
   * @param width                 the Vehicle's width, in meters
   * @param frontAxleDisplacement the distance in meters from the front of the
   *                              Vehicle
   *                              to its front axle
   * @param rearAxleDisplacement  the distance in meters from the front of the
   *                              Vehicle
   *                              to its rear axle
   * @param wheelSpan             the distance from the center of an axle to
   *                              the wheels on that axle, in meters
   * @param wheelRadius           the radius, in meters, of the wheels
   * @param wheelWidth            the width of the wheels, in meters
   * @param maxSteeringAngle      the maximum angle away from center to which
   *                              the front wheels can be turned, in radians
   * @param maxTurnPerSecond      the maximum angle through which the front
   *                              wheels can be turned in one second, in
   *                              radians
   */
  public VehicleSpec(String name,
                     double maxAcceleration,
                     double maxDeceleration,
                     double maxVelocity,
                     double minVelocity,
                     double length,
                     double width,
                     double frontAxleDisplacement,
                     double rearAxleDisplacement,
                     double wheelSpan,
                     double wheelRadius,
                     double wheelWidth,
                     double maxSteeringAngle,
                     double maxTurnPerSecond) {
    this.name = name;
    this.maxAcceleration = maxAcceleration;
    this.maxDeceleration = maxDeceleration;
    this.maxVelocity = maxVelocity;
    this.minVelocity = minVelocity;
    this.length = length;
    this.width = width;
    this.frontAxleDisplacement = frontAxleDisplacement;
    this.rearAxleDisplacement = rearAxleDisplacement;
    this.wheelSpan = wheelSpan;
    this.wheelRadius = wheelRadius;
    this.wheelWidth = wheelWidth;
    this.maxSteeringAngle = maxSteeringAngle;
    this.maxTurnPerSecond = maxTurnPerSecond;
    // derived properties
    this.maxTurnPerCycle = (maxTurnPerSecond / SimConfig.CYCLES_PER_SECOND);
    this.radius = Math.sqrt(length * length + width * width)/2;
    this.halfWidth = width/2;
    this.halfLength = length/2;
    this.wheelbase = rearAxleDisplacement - frontAxleDisplacement;
  }


  /**
   * Update all values in this specification to the given specification.
   *
   * @param spec  the value of the new specification.
   */
  public void assign(VehicleSpec spec) {
    this.name = spec.name;
    this.maxAcceleration = spec.maxAcceleration;
    this.maxDeceleration = spec.maxDeceleration;
    this.maxVelocity = spec.maxVelocity;
    this.minVelocity = spec.minVelocity;
    this.length = spec.length;
    this.width = spec.width;
    this.frontAxleDisplacement = spec.frontAxleDisplacement;
    this.rearAxleDisplacement = spec.rearAxleDisplacement;
    this.wheelSpan = spec.wheelSpan;
    this.wheelRadius = spec.wheelRadius;
    this.wheelWidth = spec.wheelWidth;
    this.maxSteeringAngle = spec.wheelWidth;
    this.maxTurnPerSecond = spec.maxTurnPerSecond;
    this.maxTurnPerCycle = spec.maxTurnPerCycle;
    this.radius = spec.radius;
    this.halfWidth = spec.halfWidth;
    this.halfLength = spec.halfLength;
    this.wheelbase = spec.wheelbase;
  }

  /////////////////////////////////
  // PUBLIC METHODS
  /////////////////////////////////

  /**
   * Get the name of this vehicle specification
   *
   * @return the name of this vehicle specification
   */
  public String getName() {
    return name;
  }

  /**
   * Get the maximum acceleration.
   *
   * @return the maximum acceleration
   */
  public double getMaxAcceleration() {
    return maxAcceleration;
  }

  /**
   * Get the maximum deceleration.
   *
   * @return the maximum deceleration
   */
  public double getMaxDeceleration() {
    return maxDeceleration;
  }

  /**
   * Get the maximum velocity.
   *
   * @return the maximum velocity
   */
  public double getMaxVelocity() {
    return maxVelocity;
  }

  /**
   * Get the minimum velocity.
   *
   * @return the minimum velocity
   */
  public double getMinVelocity() {
    return minVelocity;
  }

  /**
   * Get the length of the vehicle.
   *
   * @return the length of the vehicle
   */
  public double getLength() {
    return length;
  }

  /**
   * Get the width of the vehicle.
   *
   * @return the width of the vehicle
   */
  public double getWidth() {
    return width;
  }

  /**
   * Get the front axle displacement.
   *
   * @return the front axle displacement
   */
  public double getFrontAxleDisplacement() {
    return frontAxleDisplacement;
  }

  /**
   * Get the rear axle displacement.
   *
   * @return the rear axle displacement
   */
  public double getRearAxleDisplacement() {
    return rearAxleDisplacement;
  }

  /**
   * Get the wheel span.
   *
   * @return the wheel span
   */
  public double getWheelSpan() {
    return wheelSpan;
  }

  /**
   * Get the wheel radius.
   *
   * @return the wheel radius
   */
  public double getWheelRadius() {
    return wheelRadius;
  }

  /**
   * Get the wheel width.
   *
   * @return the wheel width
   */
  public double getWheelWidth() {
    return wheelWidth;
  }

  /**
   * Get the maximum steering angle.
   *
   * @return the maximum steering angle
   */
  public double getMaxSteeringAngle() {
    return maxSteeringAngle;
  }

  /**
   * Get the maximum turn per second.
   *
   * @return the maximum turn per second
   */
  public double getMaxTurnPerSecond() {
    return maxTurnPerSecond;
  }


  /////////////////////////////////
  // DERIVED PROPERTIES
  /////////////////////////////////

  // TODO: no use, remove getMaxTurnPerCycle()

  /**
   * Get the maximum turn per cycle.
   *
   * @return the maximum turn per cycle
   */
  public double getMaxTurnPerCycle() {
    return maxTurnPerCycle;
  }

  /**
   * Get the radius.
   *
   * @return the radius
   */
  public double getRadius() {
    return radius;
  }

  /**
   * Get half of the width of the vehicle.
   *
   * @return the half of the width of the vehicle
   */
  public double getHalfWidth() {
    return halfWidth;
  }

  /**
   * Get half of the length of the vehicle.
   *
   * @return the half of the length of the vehicle
   */
  public double getHalfLength() {
    return halfLength;
  }

  /**
   * Get the wheelbase of this Vehicle.
   *
   * @return the difference between the rear and front wheel displacements
   */
  public double getWheelbase() {
    return wheelbase;
  }


  /////////////////////////////////
  // PARAMETRIC PROPERTIES
  /////////////////////////////////

  /**
   * Get the location of the center of the Vehicle at this point in time.
   *
   * @param pos      the position of the vehicle
   * @param heading  the heading of the vehicle
   * @return         the global coordinates of the center of the Vehicle.
   */
  public Point2D getCenterPoint(Point2D pos, double heading) {
    return new Point2D.Double(pos.getX() - (halfLength * Math.cos(heading)),
                              pos.getY() - (halfLength * Math.sin(heading)));
  }

  /**
   * Get the point between the front wheels.
   *
   * @param pos      the position of the vehicle
   * @param heading  the heading of the vehicle
   * @return         the global coordinates of the point between
   *                 the front wheels
   */
  public Point2D getPointBetweenFrontWheels(Point2D pos, double heading) {
    return new Point2D.Double(
       pos.getX() - frontAxleDisplacement * Math.cos(heading),
       pos.getY() - frontAxleDisplacement * Math.sin(heading));
  }

  /**
   * Get the current global coordinates of the corners of this Vehicle.
   *
   * @param pos      the position of the vehicle
   * @param heading  the heading of the vehicle
   * @return         an array of points representing the four corners.
   */
  public Point2D[] getCornerPoints(Point2D pos, double heading) {
    Point2D[] result = new Point2D.Double[4];
    double x;
    double y;
    // First point, counterclockwise
    x = pos.getX() + halfWidth * Math.cos(heading + Math.PI/2);
    y = pos.getY() + halfWidth * Math.sin(heading + Math.PI/2);
    result[0] = new Point2D.Double(x,y);
    // Second point
    x = x + length * Math.cos(heading + Math.PI);
    y = y + length * Math.sin(heading + Math.PI);
    result[1] = new Point2D.Double(x,y);
    // Fourth point
    x = pos.getX() + halfWidth * Math.cos(heading - Math.PI/2);
    y = pos.getY() + halfWidth * Math.sin(heading - Math.PI/2);
    result[3] = new Point2D.Double(x,y);
    // Third point
    x = x + length * Math.cos(heading - Math.PI);
    y = y + length * Math.sin(heading - Math.PI);
    result[2] = new Point2D.Double(x,y);
    return result;
  }


  /**
   * Get the current global coordinates of the corners of the Vehicle,
   * assuming it is larger in each dimension by a fixed amount.
   *
   * @param pos      the position of the vehicle
   * @param heading  the heading of the vehicle
   * @param extra    the fixed amount to add to each dimension of the Vehicle.
   * @return         an array of points representing the four "inflated" corners
   */
  public Point2D[] getCornerPoints(double extra, Point2D pos, double heading) {
    Point2D[] result = new Point2D.Double[4];
    double x;
    double y;
    // First point, counterclockwise
    x = pos.getX() + (extra/2) * Math.cos(heading) +
        ((width + extra)/2) * Math.cos(heading + Math.PI/2);
    y = pos.getY() + (extra/2) * Math.sin(heading) +
        ((width + extra)/2) * Math.sin(heading + Math.PI/2);
    result[0] = new Point2D.Double(x,y);
    // Second point
    x = x + (length + extra) * Math.cos(heading + Math.PI);
    y = y + (length + extra) * Math.sin(heading + Math.PI);
    result[1] = new Point2D.Double(x,y);
    // Fourth point
    x = pos.getX() + (extra/2) * Math.cos(heading) +
        ((width + extra)/2) * Math.cos(heading - Math.PI/2);
    y = pos.getY() + (extra/2) * Math.sin(heading) +
        ((width + extra)/2) * Math.sin(heading - Math.PI/2);
    result[3] = new Point2D.Double(x,y);
    // Third point
    x = x + (length + extra) * Math.cos(heading - Math.PI);
    y = y + (length + extra) * Math.sin(heading - Math.PI);
    result[2] = new Point2D.Double(x,y);
    return result;
  }


  /**
   * Get the point between the rear wheels.
   *
   * @param pos      the position of the vehicle
   * @param heading  the heading of the vehicle
   * @return         the global coordinates of the point between the rear wheels
   */
  public Point2D getPointBetweenRearWheels(Point2D pos, double heading) {
    return new Point2D.Double(pos.getX() - rearAxleDisplacement *
                              Math.cos(heading),
                              pos.getY() - rearAxleDisplacement *
                              Math.sin(heading));
  }


  /**
   * Get the point between all the wheels.
   *
   * @param pos      the position of the vehicle
   * @param heading  the heading of the vehicle
   * @return         the global coordinates of the point between all the wheels
   */
  public Point2D getPointBetweenAllWheels(Point2D pos, double heading) {
    double dist = (frontAxleDisplacement + rearAxleDisplacement) / 2;
    return new Point2D.Double(pos.getX() -
                              dist * Math.cos(heading),
                              pos.getY() -
                              dist * Math.sin(heading));
  }


  /**
   * Get the point at the rear center of the Vehicle.
   *
   * @param pos      the position of the vehicle
   * @param heading  the heading of the vehicle
   * @return         the global coordinates of the point at the center of the
   *                 Vehicle's rear
   */
  public Point2D getPointAtRear(Point2D pos, double heading) {
    return new Point2D.Double(pos.getX() - length * Math.cos(heading),
                              pos.getY() - length * Math.sin(heading));
  }


  /**
   * Get the point on the left side of the Vehicle, aligned with the front
   * axle.
   *
   * @param pos      the position of the vehicle
   * @param heading  the heading of the vehicle
   * @return         the global coordinates of this point
   */
  public Point2D getPointLeftSideFrontAxle(Point2D pos, double heading) {
    Point2D frontAxle = getPointBetweenFrontWheels(pos, heading);
    return new Point2D.Double(frontAxle.getX() +
                              halfWidth * Math.cos(heading + Math.PI/2),
                              frontAxle.getY() +
                              halfWidth * Math.sin(heading + Math.PI/2));
  }


  /**
   * Get the point on the right side of the Vehicle, aligned with the front
   * axle.
   *
   * @param pos      the position of the vehicle
   * @param heading  the heading of the vehicle
   * @return         the global coordinates of this point
   */
  public Point2D getPointRightSideFrontAxle(Point2D pos, double heading) {
    Point2D frontAxle = getPointBetweenFrontWheels(pos, heading);
    return new Point2D.Double(frontAxle.getX() +
                              halfWidth * Math.cos(heading - Math.PI/2),
                              frontAxle.getY() +
                              halfWidth * Math.sin(heading - Math.PI/2));
  }


  /**
   * Get the current global coordinate of the rear-left corner of this
   * Vehicle.
   *
   * @param pos      the position of the vehicle
   * @param heading  the heading of the vehicle
   * @return         a point representing the rear-left corner
   */
  public Point2D getRearLeftCornerPoint(Point2D pos, double heading) {
    double x;
    double y;
    // First point, counterclockwise
    x = pos.getX() + halfWidth * Math.cos(heading + Math.PI/2);
    y = pos.getY() + halfWidth * Math.sin(heading + Math.PI/2);
    // Second point
    x = x + length * Math.cos(heading + Math.PI);
    y = y + length * Math.sin(heading + Math.PI);
    return new Point2D.Double(x,y);
  }


  /**
   * Get the current global coordinate of the rear-right corner of this
   * Vehicle.
   *
   * @param pos      the position of the vehicle
   * @param heading  the heading of the vehicle
   * @return         a point representing the rear-right corner
   */
  public Point2D getRearRightCornerPoint(Point2D pos, double heading) {
    double x;
    double y;
    // Fourth point
    x = pos.getX() + halfWidth * Math.cos(heading - Math.PI/2);
    y = pos.getY() + halfWidth * Math.sin(heading - Math.PI/2);
    // Third point
    x = x + length * Math.cos(heading - Math.PI);
    y = y + length * Math.sin(heading - Math.PI);
    return new Point2D.Double(x,y);
  }


  /**
   * Get the points where each of the wheels is presumably touching the road.
   *
   * @param pos      the position of the vehicle
   * @param heading  the heading of the vehicle
   * @return         an array of 4 points, representing the front left, front
   *                 right, rear left, and rear right wheels' points of contact
   *                 with the road.
   */
  public Point2D[] getWheelCenters(Point2D pos, double heading) {
    Point2D[] result = new Point2D.Double[4];
    Point2D frontAxle = getPointBetweenFrontWheels(pos, heading);
    Point2D rearAxle = getPointBetweenRearWheels(pos, heading);
    // left front
    result[0] = new Point2D.Double(frontAxle.getX() +
                                   wheelSpan *
                                   Math.cos(heading + Math.PI/2),
                                   frontAxle.getY() +
                                   wheelSpan *
                                   Math.sin(heading + Math.PI/2));
    // right front
    result[1] = new Point2D.Double(frontAxle.getX() +
                                   wheelSpan *
                                   Math.cos(heading - Math.PI/2),
                                   frontAxle.getY() +
                                   wheelSpan *
                                   Math.sin(heading - Math.PI/2));
    // left rear
    result[2] = new Point2D.Double(rearAxle.getX() +
                                   wheelSpan *
                                   Math.cos(heading + Math.PI/2),
                                   rearAxle.getY() +
                                   wheelSpan *
                                   Math.sin(heading + Math.PI/2));
    // right rear
    result[3] = new Point2D.Double(rearAxle.getX() +
                                   wheelSpan *
                                   Math.cos(heading - Math.PI/2),
                                   rearAxle.getY() +
                                   wheelSpan *
                                   Math.sin(heading - Math.PI/2));
    return result;
  }


  /**
   * Get the Shapes of each of the wheels.
   *
   * @param pos            the position of the vehicle
   * @param heading        the heading of the vehicle
   * @param steeringAngle  the steering angle
   * @return               an array of wheel Shapes: front left, front right,
   *                       rear left, rear right
   */
  public Shape[] getWheelShapes(Point2D pos, double heading,
                                double steeringAngle) {
    Shape[] result = new Shape[4];  // hold the shapes of the tires
    Point2D[][] wheelCorners = getWheelCorners(pos, heading, steeringAngle);
    // Construct the shape of each tire
    for(int tireIdx = 0; tireIdx < 4; tireIdx++) {
      GeneralPath tire = new GeneralPath();
      // Get to the starting point
      tire.moveTo((float)wheelCorners[tireIdx][0].getX(),
                  (float)wheelCorners[tireIdx][0].getY());
      // Now draw the rest of the tire
      for(int cornerIdx = 1; cornerIdx < 4; cornerIdx++) {
        tire.lineTo((float)wheelCorners[tireIdx][cornerIdx].getX(),
                    (float)wheelCorners[tireIdx][cornerIdx].getY());
      }
      // Close the path - going back to the start
      tire.closePath();
      result[tireIdx] = tire;
    }
    return result;
  }

  /////////////////////////////////
  // PRIVATE FIELDS
  /////////////////////////////////

  /**
   * Get the coordinates of the four corners of each of the wheels.
   * Helper method for generating the Shapes of the wheels.  The four corners
   * are in the following order for each wheel: front left, front right, rear
   * right, rear left.  The wheels are in the following order: front left,
   * front right, rear left, rear right.
   *
   * @param pos            the position of the vehicle
   * @param heading        the heading of the vehicle
   * @param steeringAngle  the steering angle
   * @return               an array with an array of corner Points for
   *                       each wheel
   */
  private Point2D[][] getWheelCorners(Point2D pos, double heading,
                                      double steeringAngle) {
    //  4 wheels, each with 4 pts
    Point2D[][] result = new Point2D.Double[4][4];
    Point2D[] points = getWheelCoords(pos, heading, steeringAngle);
    double halfWheelWidth = wheelWidth/2;
    // front wheels
    // First calculate some values we are going to use several times
    double frontWheelWidthXProjection =
      halfWheelWidth * Math.cos(heading + steeringAngle + Math.PI/2);
    double frontWheelWidthYProjection =
      halfWheelWidth * Math.sin(heading + steeringAngle + Math.PI/2);
    // Now get the points for each wheel
    for(int i = 0; i < 4; i += 2) {
      int wheelIdx = i/2; // {0, 1}
      result[wheelIdx][0] =
        new Point2D.Double(points[i].getX() + frontWheelWidthXProjection,
                           points[i].getY() + frontWheelWidthYProjection);
      result[wheelIdx][1] =
        new Point2D.Double(points[i].getX() - frontWheelWidthXProjection,
                           points[i].getY() - frontWheelWidthYProjection);
      result[wheelIdx][2] =
        new Point2D.Double(points[i+1].getX() - frontWheelWidthXProjection,
                           points[i+1].getY() - frontWheelWidthYProjection);
      result[wheelIdx][3] =
        new Point2D.Double(points[i+1].getX() + frontWheelWidthXProjection,
                           points[i+1].getY() + frontWheelWidthYProjection);
    }
    // rear wheels
    // First calculate some values we are going to use several times
    double rearWheelWidthXProjection =
      halfWheelWidth * Math.cos(heading + Math.PI/2);
    double rearWheelWidthYProjection =
      halfWheelWidth * Math.sin(heading + Math.PI/2);
    // Now get the points for each wheel
    for(int i = 4; i < 8; i += 2) {
      int wheelIdx = i/2; // {2,3}
      result[wheelIdx][0] =
        new Point2D.Double(points[i].getX() + rearWheelWidthXProjection,
                           points[i].getY() + rearWheelWidthYProjection);
      result[wheelIdx][1] =
        new Point2D.Double(points[i].getX() - rearWheelWidthXProjection,
                           points[i].getY() - rearWheelWidthYProjection);
      result[wheelIdx][2] =
        new Point2D.Double(points[i+1].getX() - rearWheelWidthXProjection,
                           points[i+1].getY() - rearWheelWidthYProjection);
      result[wheelIdx][3] =
        new Point2D.Double(points[i+1].getX() + rearWheelWidthXProjection,
                           points[i+1].getY() + rearWheelWidthYProjection);
    }
    return result;
  }


  /**
   * Get the coordinates of the front and backs of the wheels.  This is a
   * helper method, although in theory it could be used to draw lines
   * representing the wheels (instead we use Shapes, though).  There are 8
   * points, taken in groups of 2 they are (front, rear) pairs for the left
   * front, right front left rear, and right rear wheels, respectively.
   *
   * @param pos            the position of the vehicle
   * @param heading        the heading of the vehicle
   * @param steeringAngle  the steering angle
   * @return               an array of 8 points representing the fronts and
   *                       rears of 4 wheels
   */
  private Point2D[] getWheelCoords(Point2D pos, double heading,
                                   double steeringAngle) {
    Point2D[] result = new Point2D.Double[8];
    Point2D[] wheelCenters = getWheelCenters(pos, heading);
    Point2D leftFront = wheelCenters[0];
    Point2D rightFront = wheelCenters[1];
    Point2D leftRear = wheelCenters[2];
    Point2D rightRear = wheelCenters[3];
    // Calculate these only once
    double frontXProjection = wheelRadius * Math.cos(heading + steeringAngle);
    double frontYProjection = wheelRadius * Math.sin(heading + steeringAngle);
    // leftFrontFront
    result[0] = new Point2D.Double(leftFront.getX() + frontXProjection,
                                   leftFront.getY() + frontYProjection);
    // leftFrontRear
    result[1] = new Point2D.Double(leftFront.getX() - frontXProjection,
                                   leftFront.getY() - frontYProjection);
    // rightFrontFront
    result[2] = new Point2D.Double(rightFront.getX() + frontXProjection,
                                   rightFront.getY() + frontYProjection);
    // rightFrontRear
    result[3] = new Point2D.Double(rightFront.getX() - frontXProjection,
                                   rightFront.getY() - frontYProjection);
    // Calculate these only once
    double rearXProjection = wheelRadius*Math.cos(heading);
    double rearYProjection = wheelRadius*Math.sin(heading);
    // leftRearFront
    result[4] = new Point2D.Double(leftRear.getX() + rearXProjection,
                                   leftRear.getY() + rearYProjection);
    // leftRearRear
    result[5] = new Point2D.Double(leftRear.getX() - rearXProjection,
                                   leftRear.getY() - rearYProjection);
    // rightRearFront
    result[6] = new Point2D.Double(rightRear.getX() + rearXProjection,
                                   rightRear.getY() + rearYProjection);
    // rightRearRear
    result[7] = new Point2D.Double(rightRear.getX() - rearXProjection,
                                   rightRear.getY() - rearYProjection);
    return result;
  }

}