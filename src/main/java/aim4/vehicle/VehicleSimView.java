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
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.List;
import java.util.Queue;

import aim4.driver.AutoDriver;
import aim4.msg.v2i.V2IMessage;

/**
 * The interface of a vehicle from the viewpoint of a simulator.
 */
public interface VehicleSimView extends VehicleDriverView {

  /**
   * Set the VIN number of this Vehicle.
   *
   * @param vin the vehicle's VIN number.
   */
  void setVIN(int vin);

  /**
   * Set this Vehicle's Driver.
   *
   * @param driver  the new driver to control this Vehicle
   */
  void setDriver(AutoDriver driver);

  /**
   * Check whether this vehicle's time is current.
   *
   * @param currentTime  the current time
   */
  void checkCurrentTime(double currentTime);

  /////////////////////////////////
  // PUBLIC METHODS
  /////////////////////////////////

  // states

  /**
   * Get the position of the vehicle.
   *
   * @return the position of the vehicle
   */
  Point2D getPosition();

  /**
   * Get the heading of the vehicle
   *
   * @return the heading of the vehicle
   */
  double getHeading();

  /**
   * Get the velocity of the vehicle
   *
   * @return the velocity of the vehicle
   */
  double getVelocity();

  /**
   * Get the acceleration of the vehicle
   *
   * @return the acceleration of the vehicle
   */
  double getAcceleration();


  /////////////////////////////////
  // PUBLIC METHODS
  /////////////////////////////////

  // derived properties

  /**
   * Get a {@link Shape} describing the Vehicle.
   *
   * @return a Shape describing this Vehicle.
   */
  Shape getShape();

  /**
   * Get a {link Shape} describing this Vehicle, if it were larger in each
   * dimension.
   *
   * @param extra the fixed extra amount by which to increase the size of the
   *              Vehicle in each dimension
   * @return      a Shape describing a Vehicle larger in each dimension by the
   *              extra amount.
   */
  Shape getShape(double extra);

  /**
   * Get the edges that represent the boundaries of this Vehicle.
   *
   * @return an array of line segments that represent the edges of the
   *         Vehicle.
   */
  List<Line2D> getEdges();

  /**
   * Get the Shapes of each of the wheels.
   *
   * @return an array of wheel Shapes: front left, front right, rear left,
   *         rear right
   */
  Shape[] getWheelShapes();

  /**
   * Get the point in front of the middle point of the vehicle that is
   * at the distance of delta away from the vehicle.
   *
   * @param delta   the distance of the vehicle and the point
   *
   * @return the projected point
   */
  Point2D getPointAtMiddleFront(double delta);

  /**
   * Get the location of the center of the Vehicle at this point in time.
   *
   * @return the global coordinates of the center of the Vehicle.
   */
  Point2D getCenterPoint();

  /**
   * Get the current global coordinates of the corners of this Vehicle.
   *
   * @return an array of points representing the four corners.
   */
  Point2D[] getCornerPoints();

  /**
   * Get the point at the rear center of the Vehicle.
   *
   * @return the global coordinates of the point at the center of the
   * Vehicle's rear
   */
  Point2D getPointAtRear();


  /////////////////////////////////
  // PUBLIC METHODS
  /////////////////////////////////

  // messaging

  /**
   * Get the queue of V2I messages waiting to be delivered from this
   * Vehicle.
   *
   * @return the queue of V2I messages to be delivered from this Vehicle
   */
  Queue<V2IMessage> getV2IOutbox();

  /////////////////////////////////
  // PUBLIC METHODS
  /////////////////////////////////

  // controls

  /**
   * Move a single Vehicle according to some approximation of the laws
   * of physics.
   *
   * @param timeStep the size of the time step to simulate, in seconds
   */
  void move(double timeStep);

}
