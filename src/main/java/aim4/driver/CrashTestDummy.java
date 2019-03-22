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
package aim4.driver;

import java.awt.geom.Point2D;

import aim4.config.Debug;
import aim4.driver.pilot.V2IPilot;
import aim4.map.lane.Lane;
import aim4.vehicle.AutoVehicleDriverView;

/**
 * A driver agent that only steers and changes lanes when appropriate.
 */
public class CrashTestDummy extends Driver {

  /////////////////////////////////
  // PRIVATE FIELDS
  /////////////////////////////////

  /** The vehicle this driver will control */
  private AutoVehicleDriverView vehicle;

  /** The Lane in which the vehicle should exit the intersection. */
  private Lane departureLane;


  /////////////////////////////////
  // CLASS CONSTRUCTORS
  /////////////////////////////////

  /**
   * Construct a new CrashTestDummy to pilot the simulated vehicle across
   * an intersection.
   *
   * @param vehicle       the simulated vehicle to pilot
   * @param arrivalLane   the Lane in which the vehicle should enter the
   *                      intersection
   * @param departureLane the Lane in which the vehicle should depart the
   *                      intersection
   */
  public CrashTestDummy(AutoVehicleDriverView vehicle,
                        Lane arrivalLane, Lane departureLane) {
    this.vehicle = vehicle;
    setCurrentLane(arrivalLane);
    this.departureLane = departureLane;
  }

  /////////////////////////////////
  // PUBLIC METHODS
  /////////////////////////////////

  /**
   * Take steering actions to guide a test vehicle through a simulated
   * traversal of the intersection.
   */
  @Override
  public void act() {
    super.act();
    // If we're not already in the departure lane
    if(getCurrentLane() != departureLane) {
      // If we're changing to a different Road
      if(Debug.currentMap.getRoad(getCurrentLane()) != Debug.currentMap.getRoad(departureLane)) {
        // If we're close enough...
        if(departureLane.nearestDistance(getVehicle().gaugePosition()) <
           calculateTraversingLaneChangeDistance()) {
          // Change to it
          setCurrentLane(departureLane);
        }
      } else {
        // Otherwise, we're changing to the same Road, so we need a different
        // criterion... in this case none
        setCurrentLane(departureLane);
      }
    }
    // Use the basic lane-following behavior
    followCurrentLane();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public AutoVehicleDriverView getVehicle() {
    return vehicle;
  }

  // TODO: think about merging the following code with those in AutoV2IPilot
  // actually we don't need this because we will eventually remove this
  // class

  /**
   * Turn the wheels to follow the current lane, using the
   * <code>DEFAULT_LEAD_TIME</code>. This involves first projecting
   * the Vehicle's current position onto the lane, and then projecting
   * forward by a distance equal to the Vehicle's velocity multiplied
   * by the lead time.
   */
  private void followCurrentLane() {
    followCurrentLane(DriverUtil.DEFAULT_LEAD_TIME);
  }


  /**
   * Turn the wheels to follow the current lane, using the given lead time.
   * This involves first projecting the Vehicle's current position onto the
   * lane, and then projecting forward by a distance equal to the Vehicle's
   * velocity multiplied by the lead time.
   *
   * @param leadTime the lead time to use
   */
  private void followCurrentLane(double leadTime) {
    double leadDist = leadTime * vehicle.gaugeVelocity() +
                      DriverUtil.MIN_LEAD_DIST;
    Point2D aimPoint;
    double remaining = getCurrentLane().
                       remainingDistanceAlongLane(vehicle.gaugePosition());
    // If there's not enough room in this Lane and there is a Lane that this
    // Lane leads into, use the next Lane
    if((leadDist > remaining) && (getCurrentLane().hasNextLane())) {
      // First make sure we shouldn't transfer to the next lane
      if(remaining <= 0) {
        // Switch to the next Lane
        setCurrentLane(getCurrentLane().getNextLane());
        // currentLane = currentLane.getNextLane();
        // And do this over
        followCurrentLane(leadTime);
        return;
      }
      // Use what's left over after this Lane to go into the next one.
      aimPoint = getCurrentLane().getNextLane().getLeadPoint(
                   getCurrentLane().getNextLane().getStartPoint(),
                   leadDist - remaining);
      // Indicate that this is the point for which we are aiming
//      addDebugPoint(new DebugPoint(aimPoint,
//                                   vehicle.gaugePointBetweenFrontWheels(),
//                                   "next lane", Color.RED));
    } else { // Otherwise, use the current Lane
      aimPoint = getCurrentLane().getLeadPoint(
                   vehicle.gaugePosition(), leadDist);
      // Indicate that this is the point for which we are aiming
//      addDebugPoint(new DebugPoint(aimPoint,
//                                   vehicle.gaugePointBetweenFrontWheels(),
//                                   "lane", Color.PINK.brighter()));
    }
    turnTowardPoint(aimPoint);
  }


  /**
   * Turn the wheels toward a given Point.
   *
   * @param p the Point toward which to turn the wheels
   */
  private void turnTowardPoint(Point2D p) {
    vehicle.turnTowardPoint(p);
  }


  /**
   * Determine the distance at which to change lanes during the
   * traversing inside an intersection.  Currently just
   * multiplies the TRAVERSING_LANE_CHANGE_LEAD_TIME by the
   * reading from the Vehicle's speedometer.
   *
   * @return the distance at which the Driver should turn the Vehicle into
   *         the lane to which it is changing
   */
  public double calculateTraversingLaneChangeDistance() {
    return V2IPilot.TRAVERSING_LANE_CHANGE_LEAD_TIME *
           vehicle.gaugeVelocity();
  }


}
