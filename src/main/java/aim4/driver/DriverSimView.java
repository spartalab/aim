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

import java.util.Set;

import aim4.map.Road;
import aim4.map.SpawnPoint;
import aim4.map.lane.Lane;
import aim4.vehicle.VehicleDriverView;

/**
 * A driver from simulators' viewpoint.
 */
public interface DriverSimView {

  /////////////////////////////////
  // PUBLIC METHODS
  /////////////////////////////////

  // main function

  /**
   * Take control actions for driving the agent's Vehicle.  This includes
   * physical manipulation of the Vehicle as well as sending any messages
   * or performing any coordination tasks.
   */
  void act();

  /////////////////////////////////
  // PUBLIC METHODS
  /////////////////////////////////

  // vehicle

  /**
   * Get the Vehicle this DriverAgent is controlling.
   *
   * @return the Vehicle this DriverAgent is controlling
   */
  VehicleDriverView getVehicle();


  /////////////////////////////////
  // PUBLIC METHODS
  /////////////////////////////////

  // lanes

  /**
   * Get the Lane the driver is currently following.
   *
   * @return the Lane the driver is currently following
   */
  Lane getCurrentLane();

  /**
   * Get the lanes the driver's vehicle currently occupies.
   *
   * @return the lanes the driver's vehicle currently occupies
   */
  Set<Lane> getCurrentlyOccupiedLanes();

  /**
   * Set the Lane the driver is currently following.
   *
   * @param lane the Lane the driver should follow
   */
  void setCurrentLane(Lane lane);


  /////////////////////////////////
  // PUBLIC METHODS
  /////////////////////////////////

  // origin and destination

  /**
   * Get where this driver is coming from.
   *
   * @return the Road where this driver is coming from
   */
  SpawnPoint getSpawnPoint();

  /**
   * Set where this driver agent is coming from.
   *
   * @param spawnPoint the spawn point that generated the driver
   */
  void setSpawnPoint(SpawnPoint spawnPoint);

  /**
   * Get where this driver is going.
   *
   * @return the Road where this driver is going
   */
  Road getDestination();

  /**
   * Set where this driver is going.
   *
   * @param destination the Road where this driver should go
   */
  void setDestination(Road destination);




}
