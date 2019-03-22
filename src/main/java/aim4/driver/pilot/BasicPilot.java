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
package aim4.driver.pilot;

import java.awt.Color;
import java.awt.geom.Point2D;

import aim4.config.Debug;
import aim4.config.DebugPoint;
import aim4.driver.Driver;
import aim4.driver.DriverUtil;
import aim4.vehicle.VehicleDriverView;

/**
 * The basic pilot agent.
 */
public abstract class BasicPilot {

  /////////////////////////////////
  // PUBLIC METHODS
  /////////////////////////////////

  /**
   * Get the vehicle this pilot controls.
   */
  public abstract VehicleDriverView getVehicle();

  /**
   * Get the driver this pilot controls.
   */
  public abstract Driver getDriver();


  /////////////////////////////////
  // PUBLIC METHODS
  /////////////////////////////////

  // steering

  /**
   * Turn the wheel to follow the current lane, using the
   * <code>DEFAULT_LEAD_TIME</code>. This involves first projecting
   * the Vehicle's current position onto the lane, and then projecting
   * forward by a distance equal to the Vehicle's velocity multiplied
   * by the lead time.
   */
  public void followCurrentLane() {
    followCurrentLane(DriverUtil.DEFAULT_LEAD_TIME);
  }

  /**
   * Turn the wheel to shift to the target lane of the vehicle.
   */
  public void followNewLane() {
    double leadDist = DriverUtil.getLeadDistance(getVehicle());
    // currentLane is the targetLane
    Point2D aimPoint =
      getDriver().getCurrentLane().getLeadPoint(getVehicle().gaugePosition(),
                                                leadDist);
    // TODO: do the following only when debugging
    if (Debug.isTargetVIN(getVehicle().getVIN())) {
      Debug.addShortTermDebugPoint(new DebugPoint(aimPoint, getVehicle()
        .gaugePointBetweenFrontWheels(), "shift", Color.GREEN.brighter()));
    }
    getVehicle().turnTowardPoint(aimPoint);
  }

  // TODO: what is the difference between followCurrentLane and followNewLane


  /////////////////////////////////
  // PRIVATE FIELDS
  /////////////////////////////////

  // steering

  /**
   * Turn the wheels to follow the current lane, using the given lead time.
   * This involves first projecting the Vehicle's current position onto the
   * lane, and then projecting forward by a distance equal to the Vehicle's
   * velocity multiplied by the lead time.
   *
   * @param leadTime  the lead time to use
   */
  private void followCurrentLane(double leadTime) {

//    AutoDriverPilotView driver = vehicle.getDriver();
    Driver driver = getVehicle().getDriver();

    double leadDist = leadTime * getVehicle().gaugeVelocity() +
                      DriverUtil.MIN_LEAD_DIST;
    Point2D aimPoint;
    double remaining = driver.getCurrentLane().
                       remainingDistanceAlongLane(getVehicle().gaugePosition());
    // If there's not enough room in this Lane and there is a Lane that this
    // Lane leads into, use the next Lane
    if((leadDist > remaining) && (driver.getCurrentLane().hasNextLane())) {
      // First make sure we shouldn't transfer to the next lane
      if(remaining <= 0) {
        // Switch to the next Lane
        driver.setCurrentLane(driver.getCurrentLane().getNextLane());
        // currentLane = currentLane.getNextLane();
        // And do this over
        followCurrentLane(leadTime);
        return;
      }
      // Use what's left over after this Lane to go into the next one.
      aimPoint = driver.getCurrentLane().getNextLane().getLeadPoint(
                   driver.getCurrentLane().getNextLane().getStartPoint(),
                   leadDist - remaining);
      // Indicate that this is the point for which we are aiming
      if (Debug.isTargetVIN(getVehicle().getVIN())) {
        Debug.addShortTermDebugPoint(
          new DebugPoint(aimPoint,
                         getVehicle().gaugePointBetweenFrontWheels(),
                         "next lane",
                         Color.RED));
      }
    } else { // Otherwise, use the current Lane
      aimPoint = driver.getCurrentLane().getLeadPoint(
                   getVehicle().gaugePosition(), leadDist);
      // Indicate that this is the point for which we are aiming
      if (Debug.isTargetVIN(getVehicle().getVIN())) {
        Debug.addShortTermDebugPoint(
          new DebugPoint(aimPoint,
                         getVehicle().gaugePointBetweenFrontWheels(),
                         "lane",
                         Color.PINK.brighter()));
      }
    }

    getVehicle().turnTowardPoint(aimPoint);
  }

  /**
   * Maintain a cruising speed.
   */
  protected void cruise() {
    getVehicle().setTargetVelocityWithMaxAccel(
        DriverUtil.calculateMaxFeasibleVelocity(getVehicle()));
  }


}
