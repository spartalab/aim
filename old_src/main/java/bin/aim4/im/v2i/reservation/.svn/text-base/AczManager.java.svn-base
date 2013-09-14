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

/**
 * The ACZ manager.
 */
public class AczManager implements
  ReservationManager<AczManager.Query,AczManager.Plan,Integer> {

  /////////////////////////////////
  // NESTED CLASSES
  /////////////////////////////////

  /**
   * The query for the ACZ manager.
   */
  public static class Query {
    /** The VIN */
    private int vin;
    /** The arrival time */
    private double arrivalTime;
    /** The arrival velocity */
    private double arrivalVelocity;
    /** The vehicle length */
    private double vehicleLength;
    /** The stop distance */
    private double stopDist;

    /**
     *  Create a query for the ACZ manager.
     *
     * @param vin              the VIN of a vehicle
     * @param arrivalTime      the arrival time
     * @param arrivalVelocity  the arrival velocity
     * @param vehicleLength    the vehicle length
     * @param stopDist         the stopping distance
     */
    public Query(int vin, double arrivalTime, double arrivalVelocity,
                 double vehicleLength, double stopDist) {
      this.vin = vin;
      this.arrivalTime = arrivalTime;
      this.arrivalVelocity = arrivalVelocity;
      this.vehicleLength = vehicleLength;
      this.stopDist = stopDist;
    }

    /**
     * Get the VIN of a vehicle
     *
     * @return the VIN of a vehicle
     */
    public int getVin() {
      return vin;
    }

    /**
     * Get the arrival time.
     *
     * @return the arrival time
     */
    public double getArrivalTime() {
      return arrivalTime;
    }

    /**
     * Get the arrival velocity.
     *
     * @return the arrival velocity
     */
    public double getArrivalVelocity() {
      return arrivalVelocity;
    }

    /**
     * Get the vehicle length.
     *
     * @return the vehicle length
     */
    public double getVehicleLength() {
      return vehicleLength;
    }

    /**
     * Get the stopping distance.
     *
     * @return the stopping distance
     */
    public double getStopDist() {
      return stopDist;
    }
  }

  /**
   * The plan of the reservation.
   */
  public static class Plan {
    /** The VIN of the vehicle */
    private int vin;
    /** The length of the vehicle */
    private double length;
    /** The stopping distance */
    private double stopDist;

    /**
     * Create a plan of the reservation.
     *
     * @param vin       the VIN of the vehicle
     * @param length    the length of the vehicle
     * @param stopDist  the stopping distance
     */
    public Plan(int vin, double length, double stopDist) {
      this.vin = vin;
      this.length = length;
      this.stopDist = stopDist;
    }

    /**
     * Get the VIN of the vehicle.
     *
     * @return the VIN of the vehicle
     */
    public int getVin() {
      return vin;
    }

    /**
     * Get the length of the vehicle.
     *
     * @return the length of the vehicle
     */
    public double getLength() {
      return length;
    }

    /**
     * Get the stopping distance.
     *
     * @return the stopping distance
     */
    public double getStopDist() {
      return stopDist;
    }
  }

  /////////////////////////////////
  // PRIVATE FIELDS
  /////////////////////////////////

  /** The ACZ */
  private AdmissionControlZone acz;

  /////////////////////////////////
  // CONSTRUCTORS
  /////////////////////////////////

  /**
   * Create a ACZ manager.
   *
   * @param acz  the ACZ
   */
  public AczManager(AdmissionControlZone acz) {
    this.acz = acz;
  }


  /////////////////////////////////
  // PUBLIC METHODS
  /////////////////////////////////

  /**
   * {@inheritDoc}
   */
  @Override
  public Plan query(Query q) {
    if (acz.isAdmissible(q.getVin(),
                         q.getVehicleLength(),
                         q.getStopDist())) {
      return new Plan(q.getVin(),
                      q.getVehicleLength(),
                      q.getStopDist());
    } else {
      return null;
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Integer accept(Plan plan) {
    acz.admit(plan.getVin(),
              plan.getLength(),
              plan.getStopDist());
    return plan.getVin();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void cancel(Integer vin) {
    acz.cancel(vin);
  }

}
