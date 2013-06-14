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

import java.awt.Color;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import aim4.config.Debug;
import aim4.config.DebugPoint;
import aim4.im.IntersectionManager;
import aim4.map.Road;
import aim4.map.SpawnPoint;
import aim4.map.lane.Lane;
import aim4.util.GeomMath;
import aim4.vehicle.AutoVehicleDriverView;
import aim4.vehicle.VehicleDriverView;

/**
 * An agent that drives a {@link AutoVehicleDriverView}.
 */
public abstract class Driver implements DriverSimView {

  /////////////////////////////////
  // CONSTANTS
  /////////////////////////////////

  // debugging

  /**
   * The maximum length, in meters, for which to display the sensed interval
   * between this DriverAgent's vehicle and the one in front of it.
   * {@value} meters.
   */
  private static final double MAX_INTERVAL_DISPLAY_DIST = 40; // m


  /////////////////////////////////
  // PROTECTED FIELDS
  /////////////////////////////////

  // lane

  /** The Lane that the driver is currently following. */
  protected Lane currentLane;

  /** The set of Lanes that the vehicle is currently occupied */
  protected Set<Lane> currentlyOccupiedLanes;


  /////////////////////////////////
  // PRIVATE FIELDS
  /////////////////////////////////

  // origin and destination

  /** Where this DriverAgent is coming from. */
  private SpawnPoint spawnPoint;

  /** Where this DriverAgent is headed. */
  private Road destination;


  /////////////////////////////////
  // PUBLIC METHODS
  /////////////////////////////////

  /**
   * Take control actions for driving the agent's Vehicle.  This includes
   * physical manipulation of the Vehicle as well as sending any messages
   * or performing any coordination tasks.
   */
  @Override
  public void act() {
    if (Debug.isTargetVIN(getVehicle().getVIN())) {
      if (getVehicle() instanceof AutoVehicleDriverView) {  // TODO: it is ugly
        AutoVehicleDriverView autoVehicle =
          (AutoVehicleDriverView)getVehicle();
        if (autoVehicle.getIntervalometer().read() < MAX_INTERVAL_DISPLAY_DIST){
          Debug.addShortTermDebugPoint(
            new DebugPoint(
              GeomMath.polarAdd(autoVehicle.gaugePosition(),
                                autoVehicle.getIntervalometer().read(),
                                autoVehicle.gaugeHeading()),
              autoVehicle.gaugePosition(),
              "follow",
              Color.BLUE.brighter()));
        }
      }
    }
  }

  /////////////////////////////////
  // PUBLIC METHODS
  /////////////////////////////////

  /**
   * Get the Vehicle this driver agent is controlling.
   *
   * @return the Vehicle this driver agent is controlling
   */
  @Override
  public abstract VehicleDriverView getVehicle();


  /////////////////////////////////
  // PUBLIC METHODS
  /////////////////////////////////

  // origin and destination

  /**
   * Get where this DriverAgent is coming from.
   *
   * @return the Road where this DriverAgent is coming from
   */
  @Override
  public SpawnPoint getSpawnPoint() {
    if(spawnPoint == null) {
      throw new RuntimeException("Driver is without origin!");
    }
    return spawnPoint;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setSpawnPoint(SpawnPoint spawnPoint) {
    this.spawnPoint = spawnPoint;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Road getDestination() {
    if(destination == null) {
      throw new RuntimeException("Driver is without destination!");
    }
    return destination;
  }

  /**
   * Set where this driver agent is going.
   *
   * @param destination the Road where this DriverAgent should go
   */
  @Override
  public void setDestination(Road destination) {
    this.destination = destination;
  }


  /////////////////////////////////
  // PUBLIC METHODS
  /////////////////////////////////

  // lane

  /**
   * Get the Lane the DriverAgent is currently following.
   *
   * @return the Lane the DriverAgent is currently following
   */
  @Override
  public Lane getCurrentLane() {
    return currentLane;
  }

  /**
   * Set the Lane the DriverAgent is currently following.
   *
   * @param lane the Lane the DriverAgent should follow
   */
  @Override
  public void setCurrentLane(Lane lane) {
    currentLane = lane;
    currentlyOccupiedLanes = new HashSet<Lane>(1);
    currentlyOccupiedLanes.add(lane);
  }

  /**
   * Get the lanes the DriverAgent's vehicle currently occupies.
   *
   * @return the lanes the DriverAgent's vehicle currently occupies
   */
  @Override
  public Set<Lane> getCurrentlyOccupiedLanes() {
    return Collections.unmodifiableSet(currentlyOccupiedLanes);
  }

  /**
   * Add a lane that the DriverAgent's vehicle currently occupies.
   *
   * @param lane a lane that the DriverAgent's vehicle currently occupies
   */
  public void addCurrentlyOccupiedLane(Lane lane) {
    currentlyOccupiedLanes.add(lane);
  }


  /////////////////////////////////
  // PROTECTED METHODS
  /////////////////////////////////

  // IM

  /**
   * Find the next IntersectionManager that the Vehicle will need to
   * interact with, in this Lane.
   *
   * @return the nextIntersectionManager that the Vehicle will need
   *         to interact with, in this Lane
   */
  protected IntersectionManager nextIntersectionManager() {
    return getCurrentLane().getLaneIM().
           nextIntersectionManager(getVehicle().gaugePosition());
  }

  /**
   * Find the distance to the next intersection in the Lane in which
   * the Vehicle is, from the position at which the Vehicle is.
   *
   * @return the distance to the next intersection given the current Lane
   *         and position of the Vehicle.
   */
  protected double distanceToNextIntersection() {
    return getCurrentLane().getLaneIM().
           distanceToNextIntersection(getVehicle().gaugePosition());
  }

  /**
   * Find the distance from the previous intersection in the Lane in which
   * the Vehicle is, from the position at which the Vehicle is.  This
   * subtracts the length of the Vehicle from the distance from the front
   * of the Vehicle.
   *
   * @return the distance from the previous intersection given the current
   *         Lane and position of the Vehicle.
  */
   protected double distanceFromPrevIntersection() {
     double d = getCurrentLane().getLaneIM().
       distanceFromPrevIntersection(getVehicle().gaugePosition());
     return Math.max(0.0, d - getVehicle().getSpec().getLength());
   }


}
