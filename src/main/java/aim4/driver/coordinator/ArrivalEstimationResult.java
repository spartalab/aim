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
package aim4.driver.coordinator;

import aim4.vehicle.AccelSchedule;

/**
 * The result of arrival estimations.
 */
public class ArrivalEstimationResult {

  /////////////////////////////////
  // PRIVATE FIELDS
  /////////////////////////////////

  /**
   * The arrival time.
   */
  double arrivalTime;

  /**
   * The arrival velocity.
   */
  double arrivalVelocity;

  /**
   * The acceleration schedule.
   */
  AccelSchedule accelSchedule;

  /////////////////////////////////
  // CONSTRUCTORS
  /////////////////////////////////

  /**
   * Construct an arrival estimation result object.
   *
   * @param arrivalTime      the arrival time
   * @param arrivalVelocity  the arrival velocity
   * @param accelSchedule    the acceleration schedule
   */
  public ArrivalEstimationResult(double arrivalTime,
                                 double arrivalVelocity,
                                 AccelSchedule accelSchedule) {
    this.arrivalTime = arrivalTime;
    this.arrivalVelocity = arrivalVelocity;
    this.accelSchedule = accelSchedule;
  }

  /////////////////////////////////
  // PUBLIC METHODS
  /////////////////////////////////

  /**
   * Get the arrival time.
   *
   * @return the arrival time.
   */
  public double getArrivalTime() {
    return arrivalTime;
  }

  /**
   * Get the arrival velocity.
   *
   * @return the arrival velocity.
   */
  public double getArrivalVelocity() {
    return arrivalVelocity;
  }

  /**
   * Get the acceleration schedule.
   *
   * @return the acceleration schedule.
   */
  public AccelSchedule getAccelSchedule() {
    return accelSchedule;
  }

}
