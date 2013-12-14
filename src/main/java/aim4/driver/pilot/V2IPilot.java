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

import java.util.Iterator;
import java.util.Queue;

import aim4.config.Debug;
import aim4.config.SimConfig;
import aim4.driver.AutoDriver;
import aim4.driver.DriverUtil;
import aim4.driver.coordinator.V2ICoordinator.ReservationParameter;
import aim4.vehicle.AutoVehicleDriverView;
import aim4.vehicle.VehicleUtil;

/**
 * An agent that pilots a {@link AutoVehicleDriverView} autonomously. This agent
 * attempts to emulate the behavior of a real-world autonomous driver agent in
 * terms of physically controlling the Vehicle.
 */
public class V2IPilot extends BasicPilot {

  // ///////////////////////////////
  // CONSTANTS
  // ///////////////////////////////

  /**
   * The minimum distance to maintain between the Vehicle controlled by this
   * AutonomousPilot and the one in front of it. {@value} meters.
   */
  public static final double MINIMUM_FOLLOWING_DISTANCE = 0.5; // meters

  /**
   * The default shortest distance before an intersection at which the vehicle
   * stops if the vehicle can't enter the intersection immediately.
   */
  public static double DEFAULT_STOP_DISTANCE_BEFORE_INTERSECTION = 1.0;

  /**
   * The distance, expressed in units of the Vehicle's velocity, at which to
   * switch to a new lane when turning. {@value} seconds.
   */
  public static final double TRAVERSING_LANE_CHANGE_LEAD_TIME = 1.5; // sec

  // ///////////////////////////////
  // PRIVATE FIELDS
  // ///////////////////////////////

  private double stopDistanceBeforeIntersection;

  private AutoVehicleDriverView vehicle;

  private AutoDriver driver;


  // ///////////////////////////////
  // CONSTRUCTORS
  // ///////////////////////////////

  /**
   * Create an pilot to control a vehicle.
   *
   * @param vehicle      the vehicle to control
   * @param driver       the driver
   */
  public V2IPilot(AutoVehicleDriverView vehicle, AutoDriver driver) {
    this.vehicle = vehicle;
    this.driver = driver;
    stopDistanceBeforeIntersection = DEFAULT_STOP_DISTANCE_BEFORE_INTERSECTION;
  }

  // ///////////////////////////////
  // PUBLIC METHODS
  // ///////////////////////////////


  /**
   * Get the vehicle this pilot controls.
   */
  @Override
  public AutoVehicleDriverView getVehicle() {
    return vehicle;
  }

  /**
   * Get the driver this pilot controls.
   */
  @Override
  public AutoDriver getDriver() {
    return driver;
  }

  /////////////////////////////////
  // PUBLIC METHODS
  /////////////////////////////////

  // steering

  /**
   * Set the steering action when the vehicle is traversing an intersection.
   */
  public void takeSteeringActionForTraversing(ReservationParameter rp) {
    // If we're not already in the departure lane
    if (driver.getCurrentLane() != rp.getDepartureLane()) {
      // If we're changing to a different Road
      if (Debug.currentMap.getRoad(driver.getCurrentLane()) !=
        Debug.currentMap.getRoad(rp.getDepartureLane())) {
        // Find out how far from it we are
        double distToLane =
          rp.getDepartureLane().nearestDistance(vehicle.gaugePosition());
        // If we're close enough...
        double traversingLaneChangeDistance =
          TRAVERSING_LANE_CHANGE_LEAD_TIME * vehicle.gaugeVelocity();
        if (distToLane < traversingLaneChangeDistance) {
          // Change to it
          driver.setCurrentLane(rp.getDepartureLane());
        }
      }
    } // else, we're changing to the same Road, so we need a
      // different criterion... in this case none

    followCurrentLane();
    // Use the basic lane-following behavior
  }

  // throttle actions

  /**
   * Follow the acceleration profile received as part of a reservation
   * confirmation from an IntersectionManager. If none exists, or if it is
   * empty, just cruise. Modifies the acceleration profile to reflect the
   * portion it has consumed.
   *
   * TODO: do not modify the acceleration profile
   */
  public void followAccelerationProfile(ReservationParameter rp) {
    Queue<double[]> accelProf = rp.getAccelerationProfile();
    // If we have no profile or we have finished with it, then just do our
    // best to maintain a cruising speed
    if ((accelProf == null) || (accelProf.isEmpty())) {
      // Maintain a cruising speed while in the intersection, but slow for
      // other vehicles. Also do not go above the maximum turn velocity.
      vehicle.setTargetVelocityWithMaxAccel(calculateIntersectionVelocity(rp));
    } else {
      // Otherwise, we need to figure out what the next directive in the
      // profile is - peek at the front of the list
      double[] currentDirective = accelProf.element();
      // Now, we have three cases. Either there is more than enough duration
      // left at this acceleration to do only this acceleration:
      if (currentDirective[1] > SimConfig.TIME_STEP) {
        // This is easy, just do the requested acceleration and decrement
        // the duration
        vehicle
          .setAccelWithMaxTargetVelocity(currentDirective[0]);
        currentDirective[1] -= SimConfig.TIME_STEP;
      } else if (currentDirective[1] < SimConfig.TIME_STEP) {
        // Or we have to do a weighted average
        double totalAccel = 0.0;
        double remainingWeight = SimConfig.TIME_STEP;
        // Go through each of the acceleration, duration pairs and do a
        // weighted average of the first time step's worth of accelerations
        for (Iterator<double[]> iter = accelProf.iterator(); iter.hasNext();) {
          currentDirective = iter.next();
          if (currentDirective[1] > remainingWeight) {
            // Yay! More than enough here to finish out
            totalAccel += remainingWeight * currentDirective[0];
            // Make sure to record the fact that we used up some of it
            currentDirective[1] -= remainingWeight;
            // And that we satisfied the whole time step
            remainingWeight = 0.0;
            break;
          } else if (currentDirective[1] < remainingWeight) {
            // Ugh, we have to do it again
            totalAccel += currentDirective[1] * currentDirective[0];
            remainingWeight -= currentDirective[1];
            iter.remove(); // done with this one
          } else { // currentDirective[1] == remainingWeight
            // This finishes off the list perfectly
            totalAccel += currentDirective[1] * currentDirective[0];
            // And completes our requirements for a whole time step
            remainingWeight = 0.0;
            iter.remove(); // done with this oneo
            break;
          }
        }
        // Take care of the case in which we didn't have enough for the
        // whole time step
        if (remainingWeight > 0.0) {
          totalAccel += remainingWeight * currentDirective[0];
        }
        // Okay, totalAccel should now have our total acceleration in it
        // So we need to divide by the total weight to get an actual
        // acceleration
        vehicle.setAccelWithMaxTargetVelocity(totalAccel
          / SimConfig.TIME_STEP);
      } else { // Or things work out perfectly and we use this one up
        // This is easy, just do the requested acceleration and remove the
        // element from the queue
        accelProf.remove();
        vehicle
          .setAccelWithMaxTargetVelocity(currentDirective[0]);
      }
    }
  }

  /**
   * Determine the maximum velocity at which the Vehicle should travel in the
   * intersection given the Lanes in which it is.
   *
   * @return the maximum velocity at which the Vehicle should travel in the
   *         intersection given the Lane in which it is
   */
  private double calculateIntersectionVelocity(ReservationParameter rp) {
    // TODO: remove this function
    return VehicleUtil.maxTurnVelocity(vehicle.getSpec(),
                                       rp.getArrivalLane(),
                                       rp.getDepartureLane(),
                                       driver.getCurrentIM());
  }

  /**
   * The simple throttle action.
   */
  public void simpleThrottleAction() {
    cruise();
    dontHitVehicleInFront();
    dontEnterIntersection();
  }

  /**
   * Stop before hitting the car in front of us.
   *
   * @param vehicle  the vehicle
   */
  private void dontHitVehicleInFront() {
//    double stoppingDistance = distIfStopNextTimeStep(vehicle);
    double stoppingDistance =
      VehicleUtil.calcDistanceToStop(vehicle.gaugeVelocity(),
                                     vehicle.getSpec().getMaxDeceleration());
    double followingDistance = stoppingDistance + MINIMUM_FOLLOWING_DISTANCE;
    if (VehicleUtil.distanceToCarInFront(vehicle) < followingDistance) {
      vehicle.slowToStop();
    }
  }

  /**
   * Stop before entering the intersection.
   */
  private void dontEnterIntersection() {
    double stoppingDistance = distIfStopNextTimeStep();
//    double stoppingDistance =
//      VehicleUtil.calcDistanceToStop(vehicle.gaugeVelocity(),
//                                     vehicle.getSpec().getMaxDeceleration());
    double minDistanceToIntersection =
      stoppingDistance + DEFAULT_STOP_DISTANCE_BEFORE_INTERSECTION;
    if (vehicle.getDriver().distanceToNextIntersection() <
        minDistanceToIntersection) {
//      if (Debug.isTargetVIN(vehicle.getVIN())) {
//        System.err.printf("at time %.2f, slow down since %.5f < %.5f\n",
//                          vehicle.gaugeTime(),
//                          vehicle.getDriver().distanceToNextIntersection(),
//                          minDistanceToIntersection);
//        System.err.printf("v = %.5f\n", vehicle.gaugeVelocity());
//      }
      vehicle.slowToStop();
//    } else {
//      if (Debug.isTargetVIN(vehicle.getVIN())) {
//        System.err.printf("Skipped update of acceleration\n");
//        System.err.printf(" %.5f < %.5f\n",
//                          vehicle.getDriver().distanceToNextIntersection(),
//                          minDistanceToIntersection);
//      }
    }
  }

  /**
   * Determine how far the vehicle will go if it waits until the next time
   * step to stop.
   *
   * @param vehicle  the vehicle
   * @return How far the vehicle will go if it waits until the next time
   *         step to stop
   */
  private double distIfStopNextTimeStep() {
    double distIfAccel = VehicleUtil.calcDistanceIfAccel(
        vehicle.gaugeVelocity(),
        vehicle.getSpec().getMaxAcceleration(),  // TODO: why max accel here?
        DriverUtil.calculateMaxFeasibleVelocity(vehicle),
        SimConfig.TIME_STEP);
    double distToStop = VehicleUtil.calcDistanceToStop(
        speedNextTimeStepIfAccel(),
        vehicle.getSpec().getMaxDeceleration());
    return distIfAccel + distToStop;
  }


  /**
   * Calculate the velocity of the vehicle at the next time step, if we choose
   * to accelerate at this time step.
   *
   * @param vehicle  the vehicle
   * @return the velocity of the vehicle at the next time step, if we choose
   *         to accelerate at this time step
   */
  private double speedNextTimeStepIfAccel(){
    // Our speed at the next time step will be either the target speed
    // or as fast as we can go, whichever is smaller
    return Math.min(DriverUtil.calculateMaxFeasibleVelocity(vehicle),
                    vehicle.gaugeVelocity() +
                      vehicle.getSpec().getMaxAcceleration() *
                      SimConfig.TIME_STEP);
  }

}
