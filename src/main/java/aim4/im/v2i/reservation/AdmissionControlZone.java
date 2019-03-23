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
package aim4.im.v2i.reservation;

import java.util.HashMap;
import java.util.Map;

/**
 * This is a data structure to manage the admission control zone,
 * the area after an intersection in one lane.  It essentially holds
 * a limited length's worth of vehicles and will only admit vehicles
 * if there is enough room.
 */
public class AdmissionControlZone {

  /////////////////////////////////
  // CONSTANTS
  /////////////////////////////////

  private static final double MIN_DIST_BETWEEN_VEHICLES = 0.5;

  /////////////////////////////////
  // PRIVATE FIELDS
  /////////////////////////////////

  /** The maximum length of the zone. */
  private double maxSize;


  /** The currently occupied length of the zone. */
  private double currentSize = 0.0;

  /**
   * A <code>Map</code> from the VIN number of the vehicle to the
   * length of the vehicle thus the stopping distance.
   */
  private Map<Integer, Double> vinToReservationLength =
    new HashMap<Integer, Double>();

  /////////////////////////////////
  // CLASS CONSTRUCTORS
  /////////////////////////////////

  /**
   * Construct a new AdmissionControlZone with the given maximum length.
   *
   * @param maxSize the maximum length's worth of cars that the zone
   *                will hold
   */
  public AdmissionControlZone(double maxSize) {
    this.maxSize = maxSize;
  }

  /////////////////////////////////
  // PUBLIC METHODS
  /////////////////////////////////

  /**
   * Get the maximum length's worth of vehicles allowed in this
   * admission control zone at one time.
   *
   * @return the maximum length's worth of vehicles allowed in this
   *         AdmissionControlZone at one time
   */
  public double getMaxSize() {
    return maxSize;
  }

  /**
   * Get the current size of the admission control zone.
   *
   * @return the current size of the admission control zone.
   */
  public double getCurrentSize() {
    return currentSize;
  }

  /**
   * Check to see if the vehicle can reserve space to enter this
   * admission control zone.
   *
   * @param vin              the VIN number of the vehicle to try to add
   * @param vehicleLength    the length of the vehicle to try to add
   * @param stoppingDistance the distance it will take the vehicle to
   *                         stop if it begins decelerating as soon as
   *                         it enters the admission control zone
   * @return whether or not the vehicle can be successfully added
   */
  public boolean isAdmissible(int vin, double vehicleLength,
                              double stoppingDistance) {
    if (!vinToReservationLength.containsKey(vin)) {
      return (currentSize + vehicleLength + stoppingDistance <= maxSize);
    } else {
      return false;
    }
  }

  /**
   * Reserve space for a vehicle to enter this admission control zone.
   *
   * @param vin              the VIN number of the vehicle to try to add
   * @param vehicleLength    the length of the vehicle to try to add
   * @param stoppingDistance the distance it will take the vehicle to
   *                         stop if it begins decelerating as soon as
   *                         it enters the admission control zone
   */
  public void admit(int vin, double vehicleLength, double stoppingDistance) {
    // If this vehicle is already scheduled to be admitted, we must be
    // extra careful
    if (!vinToReservationLength.containsKey(vin)) {
      assert (currentSize + vehicleLength + stoppingDistance <= maxSize);
      double reservationLength = vehicleLength + MIN_DIST_BETWEEN_VEHICLES;
      currentSize += reservationLength;
      vinToReservationLength.put(vin, reservationLength);
    } else {
      throw new RuntimeException("Error in ACZ: admitting vehicle " + vin +
                                 " already not exists in ACZ.");
    }
  }

  /**
   * Indicates that the vehicle with the given ID number no longer wants to
   * enter this AdmissionControlZone.
   *
   * @param vin the ID number of the vehicle canceling
   */
  public void cancel(int vin) {
    if (vinToReservationLength.containsKey(vin)) {
      currentSize -= vinToReservationLength.remove(vin);
    } else {
      throw new RuntimeException("Error in ACZ: canceling vehicle " + vin +
                                 " does not exist in ACZ.");
    }
  }

  /**
   * Indicates that the vehicle with the given ID number has left the
   * admission control zone by driving out of it within the lane.
   *
   * @param vin the ID number of the vehicle to remove
   */
  public void away(int vin) {
    if (vinToReservationLength.containsKey(vin)) {
      currentSize -= vinToReservationLength.remove(vin);
    } else {
      throw new RuntimeException("Error in ACZ: departing vehicle " + vin +
                                 " does not exist in ACZ.");
    }
  }

}
