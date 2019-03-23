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
import java.awt.geom.Point2D;

import aim4.driver.Driver;

/**
 * The interface of a vehicle from the viewpoint of a driver.
 */
public interface VehicleDriverView {

  /**
   * Get the ID number of this vehicle.  If the vehicle does not have a
   * VIN number, this function returns a negative number.
   *
   * @return the Vehicle's VIN number.
   */
  int getVIN();

  /**
   * Get the specification of the vehicle
   *
   * @return the specification of the vehicle
   */
  VehicleSpec getSpec();

  /**
   * Getter method for the Driver controlling this Vehicle.
   *
   * @return the Driver controlling this Vehicle, or <code>null</code>
   *         if none exists.
   */
  Driver getDriver();

  /**
   * Get the acceleration profile.
   *
   * @return the acceleration profile
   */
  AccelSchedule getAccelSchedule();

    /**
   * Read this Vehicle's clock (chronometer).
   *
   * @return the Vehicle's clock gauge.
   */
  double gaugeTime();


  /////////////////////////////////
  // PUBLIC METHODS
  /////////////////////////////////

  // states

  /**
   * Get the current position of this Vehicle, represented by the point at
   * the center of the front of the Vehicle, according to the Vehicle's
   * gauges.
   *
   * @return the position of this Vehicle according to the Vehicle's gauges
   */
  Point2D gaugePosition();

  /**
   * Get this Vehicle's compass gauge's reading.
   *
   * @return the Vehicle's compass gauge's reading
   */
  double gaugeHeading();

  /**
   * Get this Vehicle's speedometer gauge's reading.
   *
   * @return the Vehicle's speedometer gauge's reading
   */
  double gaugeVelocity();


  /////////////////////////////////
  // PUBLIC METHODS
  /////////////////////////////////

  // derived properties

  /**
   * Get a {@link Shape} describing the Vehicle, according to the Vehicle's
   * gauges.
   *
   * @return a Shape describing this Vehicle, according to the Vehicle's
   *         gauges.
   */
  Shape gaugeShape();

  /**
   * Get the point in front of the middle point of the vehicle that is
   * at the distance of delta away from the vehicle, according to the gauges.
   *
   * @param delta   the distance of the vehicle and the point,
   *                according to the gauges
   *
   * @return the projected point
   */
  Point2D gaugePointAtMiddleFront(double delta);

  /**
   * Get the point between the front wheels, according to our gauges.
   *
   * @return the global coordinates of the point between the front wheels,
   *         according to the gauges
   */
  Point2D gaugePointBetweenFrontWheels();

  /**
   * Get the point at the rear center of the vehicle, according to our gauges
   *
   * @return the global coordinates of the point at the center of the
   *         vehicle's rear, according to the gauges.
   */
  Point2D gaugePointAtRear();

  /**
   * Get the current global coordinate of the rear-left corner of this
   * Vehicle, according to the Vehicle's gauges.
   *
   * @return a point representing the rear-left corner, according to the
   *         Vehicle's gauges.
   */
  Point2D gaugeRearLeftCornerPoint();

  /**
   * Get the current global coordinate of the rear-right corner of this
   * Vehicle, according to the Vehicle's gauges.
   *
   * @return a point representing the rear-right corner, according to the
   *         Vehicle's gauges.
   */
  Point2D gaugeRearRightCornerPoint();


  /////////////////////////////////
  // PUBLIC METHODS
  /////////////////////////////////

  // control

  /**
   * Set the acceleration to the appropriate value to reach the target
   * velocity, based on the current speedometer reading.  Obeys limits on
   * acceleration and velocity as well as any disabilities.
   *
   * @param targetVelocity the desired ultimate velocity
   */
  void setTargetVelocityWithMaxAccel(double targetVelocity);

  /**
   * Set the acceleration profile.
   *
   * @param accelProfile  the acceleration profile
   */
  void setAccelSchedule(AccelSchedule accelProfile);

  /**
   * Remove the acceleration profile.
   */
  void removeAccelSchedule();

  /**
   * Turn the wheels toward a given Point.
   *
   * @param p the Point toward which to turn the wheels
   */
  void turnTowardPoint(Point2D p);

  /**
   * Set the acceleration to the specified value, using maximum and minimum
   * velocities as targets, automatically. Obeys limits on acceleration and
   * velocity as well as any disabilities.
   *
   * @param acceleration the desired acceleration.
   */
  void setAccelWithMaxTargetVelocity(double acceleration);

  /**
   * Set the acceleration to zero.  Obeys limits on acceleration and
   * disabilities.
   */
  void coast();

  /**
   * Set the Vehicle's acceleration to its minimum value without going
   * backward. Obeys limits on acceleration as well as disabilities.
   */
  void slowToStop();


  /////////////////////////////////
  // DEBUG
  /////////////////////////////////

  /**
   * print the state of the driver.
   */
  void printState();

}
