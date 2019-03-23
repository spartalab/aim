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

import aim4.vehicle.VehicleDriverView;

/**
 * A utility class for drivers.
 */
public class DriverUtil {

  /////////////////////////////////
  // CONSTANTS
  /////////////////////////////////

  /**
   * The default amount of lead time for lane following, in seconds.
   * The lead time indicates how far along the Lane from the point nearest
   * to the Vehicle's current location the Vehicle should aim.
   * By default, lead distance = DEFAULT_LEAD_TIME * velocity + MIN_LEAD_DIST.
   * {@value} seconds.
   */
  public static final double DEFAULT_LEAD_TIME = 0.4; // seconds

  /**
   * The constant term in the formula of calculating lead distances.
   * It is the minimum possible lead distance.
   * By default, lead distance = leadTime * velocity + MIN_LEAD_DIST.
   * {@value} meters.
   */
  public static final double MIN_LEAD_DIST = 0.2; // meters


  /////////////////////////////////
  // PUBLIC METHODS
  /////////////////////////////////

  /**
   * Get the estimated lead distance, which is roughly equal to the speed of
   * the vehicle times the default lead time.
   *
   * @return the lead distance
   */
  public static double getLeadDistance(VehicleDriverView vehicle) {
    return DEFAULT_LEAD_TIME * vehicle.gaugeVelocity() + MIN_LEAD_DIST;
  }

  /**
   * Determine the maximum velocity at which the Vehicle should travel
   * given the Lane in which it is.
   *
   * @param vehicle  the vehicle
   * @return the maximum velocity at which the Vehicle should travel
   *         given the Lane in which it is
   */
  public static double calculateMaxFeasibleVelocity(VehicleDriverView vehicle) {
    // TODO: should remove this function
    // Whichever's smaller - speed limit or max velocity of the vehicle
    return Math.min(vehicle.getSpec().getMaxVelocity(),
                    vehicle.getDriver().getCurrentLane().getSpeedLimit());
  }


  /////////////////////////////////
  // CONSTRUCTORS
  /////////////////////////////////

  /**
   * This class should never be instantiated.
   */
  private DriverUtil(){};

}
