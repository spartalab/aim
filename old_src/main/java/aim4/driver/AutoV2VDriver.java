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

import aim4.map.BasicMap;
import aim4.map.lane.Lane;
import aim4.vehicle.AutoVehicleDriverView;

/**
 * An autonomous V2V driver.
 */
public class AutoV2VDriver extends AutoDriver {

  /////////////////////////////////
  // NESTED CLASSES
  /////////////////////////////////

  /**
   * Potential states that a CoordinatingDriverAgent can be in.  This is one
   * aspect of how the two subagents, the Pilot and the Coordinator,
   * communicate.
   */
  public enum State {
    /**
     * The agent has not yet initiated communication with the
     * IntersectionManager and is determining what kind of management
     * is present at the intersection.
     */
    ANALYZING_INTERSECTION_MANAGEMENT,

    /** There was no intersection detected ahead - might just be too far. */
    CRUISING,

    /////////////////////////////////
    // V2V STATES
    /////////////////////////////////

    /**
     * The agent is silently listening to the CLAIM messages of other vehicles
     * nearing the intersection.
     */
    V2V_LURKING,
    /** The agent is preparing to broadcast a CLAIM message. */
    V2V_CALCULATING_CLAIM,
    /**
     * The agent is broadcasting a CLAIM message, but has not broadcast it
     * long enough to cross the intersection safely.
     */
    V2V_CLAIMING,
    /**
     * The agent is still broadcasting a CLAIM message, and has broadcast
     * it long enough to cross the intersection safely.
     */
    V2V_CLAIMED,
    /**
     * The agent is traversing the intersection in accordance with its CLAIM
     * message.
     */
    V2V_TRAVERSING;
  }

  /////////////////////////////////
  // PRIVATE METHODS
  /////////////////////////////////

  /** The vehicle this driver will control */
  private AutoVehicleDriverView vehicle;

  /////////////////////////////////
  // PRIVATE METHODS
  /////////////////////////////////

  // reservation parameters

  /**
   * Whether or not this driver agent currently has a valid reservation.
   */
  private boolean hasReservation;

  /**
   * The time at which the Vehicle should arrive at the intersection.
   */
  private double arrivalTime;

  /**
   * The velocity, in meters per second, at which the Vehicle should arrive
   * at the intersection.
   */
  private double arrivalVelocity;

  /**
   * The Lane in which the Vehicle should arrive at the intersection.
   */
  private Lane arrivalLane;

  /**
   * The Lane in which the Vehicle will depart the intersection.
   */
  private Lane departureLane;

  /////////////////////////////////
  // CONSTRUCTORS
  /////////////////////////////////

  public AutoV2VDriver(AutoVehicleDriverView vehicle, BasicMap basicMap) {
    super(vehicle, basicMap);  // TODO: temporarily make it compilable. remove it later.
    this.vehicle = vehicle;
    // TODO Auto-generated constructor stub
  }

  /**
   * Get the Vehicle this DriverAgent is controlling.
   *
   * @return the Vehicle this DriverAgent is controlling
   */
  @Override
  public AutoVehicleDriverView getVehicle() {
    return vehicle;
  }


  /////////////////////////////////
  // PUBLIC METHODS
  /////////////////////////////////

  // claim parameters

  /**
   * Set the parameters for this driver agent's Claim.  This method
   * is used by the Coordinator to inform the Pilot about the Claim that the
   * Coordinator has secured.
   *
   * @param arrivalTime         the time at which the Vehicle should arrive at
   *                            the intersection
   * @param arrivalVelocity     the minimum arrival Velocity in order to
   *                            complete the Claim on time
   * @param arrivalLane         the Lane in which the Vehicle should arrive at
   *                            the intersection
   * @param departureLane       the Lane in which the Vehicle should depart
   *                            the intersection
   */
  public void setClaimParameters(double arrivalTime, double arrivalVelocity,
                                 Lane arrivalLane, Lane departureLane) {
    this.arrivalTime = arrivalTime;
    this.arrivalVelocity = arrivalVelocity;
    this.arrivalLane = arrivalLane;
    this.departureLane = departureLane;
  }

  /**
   * Inform the driver agent that the Claim it is currently holding has stood
   * up for the required amount of time and can be used to cross the
   * intersection.
   */
  public void finalizeClaimParameters() {
    hasReservation = true;
  }



}
