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

import java.awt.geom.Area;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import aim4.config.SimConfig;
import aim4.driver.CrashTestDummy;
import aim4.driver.Driver;
import aim4.im.IntersectionManager;
import aim4.map.lane.Lane;
import aim4.util.GeomMath;
import aim4.util.Util;

/**
 * The utility functions for vehicles.
 */
public class VehicleUtil {

  /////////////////////////////////
  // CONSTANTS
  /////////////////////////////////

  /**
   * The minimum max turn velocity
   */
  private static final double MIN_MAX_TURN_VELOCITY = 7.0;

  /**
   * The resolution to within which the vehicle will determine its maximum
   * safe turning velocity at an intersection, in meters per second. {@value}
   * meters per second.  Once the vehicle has found a safe velocity within
   * this value of the maximum safe velocity, it will stop searching.
   */
  private static final double MAX_TURN_VELOCITY_RESOLUTION = 0.25; // m/s

  /**
   * The maximum absolute steering angle, in radians, of the Vehicle upon
   * leaving the intersection, such that we consider the exit of the
   * intersection safe and controlled. {@value} radians.
   */
  private static final double SAFE_TRAVERSAL_STEERING_THRESHOLD = 0.4;

  /**
   * The maximum difference, in radians, between the heading of the vehicle
   * and the heading of the Lane, on exiting the intersection, such that we
   * consider the exit of the intersection safe and controlled. {@value}
   * radians.
   */
  private static final double SAFE_TRAVERSAL_HEADING_THRESHOLD = 0.35;

  /**
   * The maximum measure, in radians, that the steering wheel of the vehicle
   * can turn, in the direction to which it turns the least during a turn,
   * such that we consider the traversal of the vehicle safe and controlled.
   * {@value} radians.
   */
  private static final double SAFE_TRAVERSAL_STEERING_DELTA = 0.08;


  /////////////////////////////////
  // PRIVATE FIELDS
  /////////////////////////////////

  /**
   * Memoization caches for max turn velocities for various vehicle
   * specification.
   */
  private static Map<String,Map<List<Integer>, Double>>
    memoMaxTurnVelocity = new HashMap<String,Map<List<Integer>,Double>>();


  /////////////////////////////////
  // PUBLIC METHODS
  /////////////////////////////////

  /**
   * Get the maximum velocity that this Vehicle should make the
   * turn between lanes through an intersection.  This method relies heavily
   * on two types of caches.  First, there is a cache for each instance
   * that is used if the vehicle is a CUSTOM type.  Second, there is a
   * cache that belongs to the whole Vehicle class that is indexed by
   * VehicleType, which essentially keeps a shared cache for each VehicleType.
   *
   * @param spec          the vehicle's specification
   * @param arrivalLane   the Lane from which the Vehicle is turning
   * @param departureLane the Lane into which the Vehicle is turning
   * @param im            the IntersectionManager controlling the
   *                      intersection through which the Vehicle is making
   *                      the turn
   * @return              the maximum safe velocity at which the Vehicle
   *                      should make the turn
   */
  public static double maxTurnVelocity(VehicleSpec spec,
                                       Lane arrivalLane,
                                       Lane departureLane,
                                       IntersectionManager im) {

    // check to see if the spec has been seem before.
    if (!memoMaxTurnVelocity.containsKey(spec.getName())) {
      // if not, create a map for it
      memoMaxTurnVelocity.put(spec.getName(),
                              new HashMap<List<Integer>,Double>());
    }

    // check to see if the max turn velocity has been stored in the cache
    Map<List<Integer>, Double> mmtvs = memoMaxTurnVelocity.get(spec.getName());
    List<Integer> key = Arrays.asList(arrivalLane.getId(),
                                      departureLane.getId(),
                                      im.getId());
    if (!mmtvs.containsKey(key)) {
      // if not, calculate it and store it in the cache
      double mtv = calculateMaxTurnVelocity(spec,
                                            arrivalLane,
                                            departureLane,
                                            im);
      mmtvs.put(key, mtv);
    }

    // FIXME try to see why we need this hack
    return Math.max(mmtvs.get(key), MIN_MAX_TURN_VELOCITY);
  }


  /**
   * Determine whether or not it is safe to cross the intersection governed
   * by the given IntersectionManager, going from the given arrival Lane to
   * the given departure Lane, while traveling at the given velocity.
   *
   * @param spec               the characteristics of the vehicle
   * @param arrivalLane       the Lane in which the Vehicle will arrive
   * @param departureLane     the Lane in which the Vehicle will depart
   * @param im                the IntersectionManager governing the
   *                          intersection
   * @param traversalVelocity the velocity at which the Vehicle will attempt
   *                          to traverse the intersection
   * @return                  whether or not it is safe to cross with these
   *                          parameters
   */
  private static boolean safeToCross(VehicleSpec spec,
                                     Lane arrivalLane, Lane departureLane,
                                     IntersectionManager im,
                                     double traversalVelocity) {
    // We can't cross if we're not moving, and we can't go faster than
    // the vehicle's top speed
    if(traversalVelocity <= 0 || traversalVelocity > spec.getMaxVelocity()) {
      return false;
    }
    // We don't want this to go on forever, it should take at most the time
    // to traverse each segment of each lane.
    double maxTime = im.traversalDistance(arrivalLane, departureLane) /
                     traversalVelocity;
    // Create a test vehicle that is a copy of this vehicle to use in the
    // internal simulation
    BasicAutoVehicle testVehicle = new BasicAutoVehicle(
      spec,
      im.getIntersection().getEntryPoint(arrivalLane), // Position
      im.getIntersection().getEntryHeading(arrivalLane), // Heading
      0.0,  // Steering angle
      traversalVelocity, // velocity
      0.0, // target velocity
      0.0, // Acceleration
      0.0);

    // Create a dummy driver to steer it
    Driver dummy = new CrashTestDummy(testVehicle, arrivalLane, departureLane);
    // Use this to ensure that we don't abort before we actually get into
    // the intersection
    boolean enteredIntersection = false;
    // How long we've been testing
    double simulatedTime = 0;
    // The minimum and maximum steering angles during the traversal
    double minTraversalSteeringAngle = 0;
    double maxTraversalSteeringAngle = 0;
    while(simulatedTime <= maxTime &&
         (!enteredIntersection ||
          departureLane.getLaneIM().distanceToNextIntersection(
            testVehicle.getPosition()) == 0 ||
          im.getIntersection().getArea().contains(
            testVehicle.getPosition()))) {
      // Give the CrashTestDummy a chance to steer
      dummy.act();
      if (testVehicle.getSteeringAngle() <
          minTraversalSteeringAngle) {
        minTraversalSteeringAngle = testVehicle.getSteeringAngle();
      }
      if (testVehicle.getSteeringAngle() >
          maxTraversalSteeringAngle) {
        maxTraversalSteeringAngle = testVehicle.getSteeringAngle();
      }
      // Now move the vehicle. We haven't touched acceleration, so it should
      // be 0.
      testVehicle.move(SimConfig.TIME_STEP);
      // Record whether or not we've entered the intersection
      if(!enteredIntersection &&
         VehicleUtil.intersects(testVehicle,
                                im.getIntersection().getAreaPlus())) {
        enteredIntersection = true;
      }
      // Increment our simulated time
      simulatedTime += SimConfig.TIME_STEP;
    }
    // If we went over the max time, then it didn't work.
    if(simulatedTime > maxTime) {
      return false;
    }
    // If the steering wheel turned too far in both directions, then it didn't
    // work. This means that the vehicle was oversteering and correcting back
    // even if it exited in a safe manner.
    if(-1 * minTraversalSteeringAngle > SAFE_TRAVERSAL_STEERING_DELTA &&
       maxTraversalSteeringAngle > SAFE_TRAVERSAL_STEERING_DELTA) {
      return false;
    }
    // If we're at this point, then the Vehicle is now protruding from the
    // intersection for the first time.  Let's make sure it is in the right
    // place.  We need to check three things.  If they are all true, then it
    // was a safe traversal.  Otherwise, it was unsafe.
    // First, that it is in the middle of the Lane
    return (departureLane.nearestDistance(testVehicle.getPosition()) <
           (departureLane.getWidth() - testVehicle.getSpec().getWidth()) / 3 &&
    // Second, that it is done steering
            Math.abs(testVehicle.getSteeringAngle()) <
            SAFE_TRAVERSAL_STEERING_THRESHOLD &&
    // Third, that it is heading in the right direction
            GeomMath.angleDiff(testVehicle.getHeading(),
                           im.getIntersection().getExitHeading(departureLane)) <
            SAFE_TRAVERSAL_HEADING_THRESHOLD);
  }



  /**
   * Calculate the maximum starting velocity for which the vehicle can stop
   * within the given distance.
   *
   * @param spec     the vehicle specification
   * @param distance the distance over which the Vehicle will be changing
   *                 velocity
   * @return         the maximum starting velocity for which the vehicle can
   *                 stop within the given distance
   */
  public static double maxVelocityToStopOverDistance(VehicleSpec spec,
                                                     double distance) {
    return Math.sqrt(-2 *
                     spec.getMaxDeceleration() *
                     distance);
  }


  /**
   * Get the amount of distance it will take to stop, given a starting
   * velocity.
   *
   * @param startingVelocity  the velocity at which the Vehicle starts
   *                          decelerating
   * @param maxDeceleration   the maximum deceleration
   * @return the amount of distance it will take to stop
   */
  public static double calcDistanceToStop(double startingVelocity,
                                          double maxDeceleration) {
    // distance to stop is - v ^2 / 2 * a
    return (-1.0) * startingVelocity * startingVelocity
           / (2.0 * maxDeceleration);
  }

  /**
   * Determine how far the Vehicle will go in the given duration, if
   * it starts at the given starting velocity and accelerates at the given
   * acceleration toward the provided target velocity.
   *
   * @param startVelocity  the initial velocity of the Vehicle
   * @param accel          the acceleration of the Vehicle during this
   *                       time
   * @param targetVelocity the velocity at which the Vehicle will stop
   *                       accelerating
   * @param duration       the duration for which this all takes place
   * @return               how far the Vehicle will travel in this time
   */
  public static double calcDistanceIfAccel(double startVelocity,
                                           double accel,
                                           double targetVelocity,
                                           double duration) {
    // If we're speeding up
    if (accel >= 0.0) {
      // If we're already at or above the target velocity
      if (startVelocity >= targetVelocity) {
        // We won't setMaxAccelWithMaxTargetVelocity, so just use current velocity
        return startVelocity * duration;
      }
      // Otherwise we need to figure out how much accelerating will be done
      double maxChange = accel * duration;
      double requestedChange = targetVelocity - startVelocity;
      // If our requested change is at least our maxChange, we'll be
      // accelerating the whole time
      if (requestedChange >= maxChange) {
        return (startVelocity + (maxChange / 2.0)) * duration;
      } else {
        // Otherwise, we will setMaxAccelWithMaxTargetVelocity for part of it
        double accelDuration = (targetVelocity - startVelocity) / accel;
        // Find our average velocity during this time
        double avgAccelVelocity = (targetVelocity + startVelocity) / 2;
        // The distance is how far we go during acceleration plus how far
        // we go after that
        return avgAccelVelocity * accelDuration +
                 targetVelocity * (duration - accelDuration);
      }
    } else { // If we're decelerating
      // If we're already at or below the target velocity
      if (startVelocity <= targetVelocity) {
        // We won't decelerate, so just use current velocity
        return startVelocity * duration;
      }
      // Otherwise we need to figure out how much decelerating will be done
      double maxChange = accel * duration;
      double requestedChange = targetVelocity - startVelocity;
      // If our requested change is at least (as negative as) our maxChange,
      // we'll be decelerating the whole time
      if (requestedChange <= maxChange) {
        return (startVelocity + (maxChange / 2.0)) * duration;
      } else {
        // Otherwise, we will decelerate for part of it
        double decelDuration = (targetVelocity - startVelocity) / accel;
        // Find our average velocity during this time
        double avgDecelVelocity = (targetVelocity + startVelocity) / 2;
        // The distance is how far we go during deceleration plus how far
        // we go after that
        return avgDecelVelocity * decelDuration +
                 targetVelocity * (duration - decelDuration);
      }
    }
  }

  /**
   * Calculate how much time the Vehicle will cover to change velocity
   * with a given acceleration.
   *
   * @param v0   the initial velocity of the Vehicle
   * @param vf   the final velocity of the Vehicle
   * @param acc  the acceleration
   * @return the time it will take the Vehicle to change between the
   *         two given velocities
   */
  public static double timeToChangeBetween(double v0, double vf, double acc) {
    if (Util.isDoubleEqual(v0, vf)) {
      return 0.0;
    } else if(v0 < vf) { // If accelerating
      if (acc > 0.0) {
        return (vf - v0) / acc;
      } else {
        return Double.MAX_VALUE;
      }
    } else { // Otherwise, decelerating
      if (acc < 0.0) {
        return (vf - v0) / acc;
      } else {
        return Double.MAX_VALUE;
      }
    }
  }

  /**
   * Calculate how much time the Vehicle will cover to change velocity
   * with a given acceleration and a given deceleration
   *
   * @param v0   the initial velocity of the Vehicle
   * @param vf   the final velocity of the Vehicle
   * @param acc  the acceleration
   * @param dec  the deceleration
   * @return     the time it will take the Vehicle to change between the
   *             two given velocities
   */
  public static double timeToChangeBetween(double v0, double vf,
                                           double acc, double dec) {
    assert (dec <= 0.0) && (acc >= 0.0);
    // TODO: remove dec parameter as it looks ugly to distinguish the
    // acc and dec cases.

    if (Util.isDoubleEqual(v0, vf)) {
      return 0.0;
    } else if (v0 < vf) { // If accelerating
      if (Util.isDoubleZero(acc)) {
        return Double.MAX_VALUE;  // can never reach the final speed
      } else {
        return (vf - v0)/acc;
      }
    } else { // Otherwise, decelerating
      if (Util.isDoubleZero(dec)) {
        return Double.MAX_VALUE;  // can never reach the final speed
      } else {
        return (vf - v0)/dec;
      }
    }
  }

  /**
   * Calculate how much distance the Vehicle will cover while changing
   * velocity.
   *
   * @param v0   the initial velocity of the Vehicle
   * @param vf   the final velocity of the Vehicle
   * @param acc  the acceleration
   * @return     the distance covered by the Vehicle while changing between
   *             the two given velocities
   */
  public static double distanceToChangeBetween(double v0, double vf,
                                               double acc) {
    // Distance = time * average velocity
    return timeToChangeBetween(v0, vf, acc) * (v0 + vf) / 2.0;
  }

  /**
   * Calculate how much distance the Vehicle will cover while changing
   * velocity.
   *
   * @param v0   the initial velocity of the Vehicle
   * @param vf   the final velocity of the Vehicle
   * @param acc  the acceleration
   * @param dec  the deceleration
   * @return     the distance covered by the Vehicle while changing between
   *             the two given velocities
   */
  public static double distanceToChangeBetween(double v0, double vf,
                                               double acc, double dec) {
    // Distance = time * average velocity
    return timeToChangeBetween(v0, vf, acc, dec) * (v0 + vf) / 2.0;
  }

  /**
   * Calculate the minimum amount of time, in seconds, it can take for the
   * vehicle to cover the indicated distance, given a starting velocity and
   * a top velocity allowed while covering the distance.
   *
   * @param distance      the distance to cover
   * @param startVelocity the velocity at the start of covering the distance
   * @param topVelocity   the top allowed velocity while covering the distance
   * @param acc           the acceleration
   * @return              the minimum amount of time, in seconds, to cover
   *                      the distance
   */
  public static double minimumTimeToCover(double distance, double startVelocity,
                                          double topVelocity, double acc) {
    assert (startVelocity < topVelocity);
    // Two cases: either we top out or we don't.
    double topOutDistance =
      distanceToChangeBetween(startVelocity, topVelocity, acc);
    if(topOutDistance >= distance) { // We won't top out.
      // So, it's just how long it takes us to make the maximum velocity
      // change we can make over that distance.
      return GeomMath.quadraticFormula(acc,
                                       2 * startVelocity,
                                       -2 * distance);
    } else { // We will top out before we hit the intersection.
      // So, it's just the time to top out, plus the time to cover the
      // remaining distance.
      return timeToChangeBetween(startVelocity, topVelocity, acc) +
             (distance - topOutDistance) / topVelocity;
    }
  }

  /////////////////////////////////
  // PRIVATE METHODS
  /////////////////////////////////

  /**
   * Determine the maximum velocity that this Vehicle should make the
   * turn between lanes through an intersection.  Works by doing an actual
   * simulation of the Vehicle through the intersection, using a binary
   * search&ndash;style method to determine the highest safe velocity.  Once
   * the parameters of the search are in a small enough range, specified by
   * {@link #MAX_TURN_VELOCITY_RESOLUTION} it takes the lower end of the
   * range.
   *
   * @param spec            the characteristics of the vehicle
   * @param arrivalLane    the Lane from which the Vehicle is turning
   * @param departureLane  the Lane into which the Vehicle is turning
   * @param im             the IntersectionManager controlling the
   *                       intersection through which the Vehicle is making
   *                       the turn
   * @return               the maximum safe velocity at which the Vehicle
   *                       should make the turn
   */
  private static double calculateMaxTurnVelocity(VehicleSpec spec,
                                                 Lane arrivalLane,
                                                 Lane departureLane,
                                                 IntersectionManager im) {
    // Start the search with a minimum of 0
    double lowerBound = 0;
    // and a maximum of the smallest of the Vehicle's maximum velocity,
    // and the speed limits of the arrival and departure lanes
    double upperBound = Math.min(spec.getMaxVelocity(),
                                 Math.min(arrivalLane.getSpeedLimit(),
                                          departureLane.getSpeedLimit()));
    // If we're not changing lanes, then no need to compute this.
    if(arrivalLane == departureLane) {
      return upperBound; // This one is easy
    } else {
      // Now as long as the range we are searching is at least
      // MAX_TURN_VELOCITY_RESOLUTION, we keep refining the search using
      // a binary search process.
      while(upperBound - lowerBound > MAX_TURN_VELOCITY_RESOLUTION) {
        // Try the velocity right in the middle
        double trialVelocity = (upperBound + lowerBound) / 2;
        // Now try to cross the intersection with at this velocity, in
        // simulation
        if (VehicleUtil.safeToCross(spec, arrivalLane, departureLane,
                                   im, trialVelocity)) {
          // It worked out, so increase the lower bound
          lowerBound = trialVelocity;
        } else {
          // It didn't work, so decrease the upper bound
          upperBound = trialVelocity;
        }
      }
      // Now we return the lower bound, to be safe.  If we return 0, it means
      // that we couldn't find a safe velocity at which to take this turn.
      return lowerBound;
    }
  }

  /**
   * Determine whether the given Vehicle is currently inside an area
   *
   * @param v     the Vehicle
   * @param area  the area
   * @return      whether the Vehicle is currently in the area
   */
  public static boolean intersects(VehicleSimView v, Area area) {
    // As a quick check, see if the front or rear point is in the intersection
    // Most of the time this should work
    if (area.contains(v.getPosition()) || area.contains(v.getPointAtRear())) {
      return true;
    } else {
      // We actually have to check to see if the Area of the
      // Vehicle and the Area of the IntersectionManager have a nonempty
      // intersection
      Area vehicleArea = new Area(v.getShape());
      // Important that it is in this order, as it is destructive to the caller
      vehicleArea.intersect(area);
      return !vehicleArea.isEmpty();
    }
  }

  /////////////////////////////////
  // PUBLIC METHODS
  /////////////////////////////////

  // TODO: temporarily put this function here. move it later.

  /**
   * Calculate the distance to the Vehicle in front of this one, in meters.
   * Uses either the supplied distance in the intervalometer or the laser
   * range finder, depending on whether the laser range finder is activated or
   * not.
   *
   * @param vehicle  the vehicle
   * @return the distance to the Vehicle in front of this one, in meters
   */
  public static double distanceToCarInFront(AutoVehicleDriverView vehicle) {
    switch(vehicle.getLRFMode()) {
    case DISABLED:
      // The laser range finder isn't on, so there's no good value in it
      return vehicle.getIntervalometer().read();
    case LIMITED:
    case ENABLED:
      // Check to see if the LRF is sensing anything
      if(vehicle.isLRFSensing()) {
        return vehicle.getLRFDistance().read();
      } else {
        return Double.MAX_VALUE;
      }
    default:
      // If not, just use the largest possible value
      return Double.MAX_VALUE;
    }
  }


  /////////////////////////////////
  // CONSTRUCTORS
  /////////////////////////////////

  /**
   * This class should never be instantiated.
   */
  private VehicleUtil(){};

}
