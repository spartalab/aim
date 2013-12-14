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
package aim4.msg.i2v;

import java.util.Queue;

import aim4.config.Constants;

/**
 * Message sent from an Intersection Manager to a Vehicle to confirm a
 * reservation request.
 */
public class Confirm extends I2VMessage {

  /////////////////////////////////
  // PRIVATE FIELDS
  /////////////////////////////////

  /**
   * The unique ID number for the reservation confirmed by this message.
   */
  private int reservationId;

  /**
   * The ID of the request message that this confirm message corresponding
   * to.  More precisely, this confirm message is a reply to all the request
   * messages whose request id is equal to or less than this requestId,
   * and larger the requestId of last confirm message.
   */
  private int requestId;

  /**
   * The time at which the receiving vehicle should arrive at the
   * intersection.
   */
  private double arrivalTime;

  /**
   * The maximum amount of time before the arrival time that the receiving
   * vehicle can safely arrive at the intersection, in seconds.
   */
  private double earlyError;

  /**
   * The maximum amount of time after the arrival time that the receiving
   * vehicle can safely arrive at the intersection, in seconds.
   */
  private double lateError;

  /**
   * The velocity at which the vehicle should arrive at the intersection,
   * in meters per second.
   */
  private double arrivalVelocity;

  /**
   * The ID number of the lane in which the vehicle should arrive at the
   * intersection.
   */
  private int arrivalLaneID;

  /**
   * The ID number of the lane in which the vehicle should depart the
   * intersection.
   */
  private int departureLaneID;

  /**
   * The distance after the intersection that is protected by an Admission
   * Control Zone.
   */
  private double aczDistance;

  /**
   * A run-length encoded list of acceleration/duration pairs to be executed
   * by the vehicle during intersection traversal.
   */
  private Queue<double[]> accProfile;


  /////////////////////////////////
  // CLASS CONSTRUCTORS
  /////////////////////////////////

  /**
   * Create a confirm message.
   *
   * @param imId            the ID number of the IntersectionManager sending
   *                        this message
   * @param vin             the ID number of the Vehicle to which this message
   *                        is being sent
   * @param reservationID   the unique ID number for the reservation confirmed
   *                        by this message
   * @param requestId       the request id of the request message this confirm
   *                        message corresponds to.
   * @param arrivalTime     the time at which the receiving vehicle should
   *                        arrive at the intersection
   * @param earlyError      the maximum amount of time before the arrival
   *                        time that the receiving vehicle can safely arrive
   *                        at the intersection, in seconds
   * @param lateError       the maximum amount of time after the arrival time
   *                        that the receiving vehicle can safely arrive at
   *                        the intersection, in seconds
   * @param arrivalVelocity the velocity at which the vehicle should arrive at
   *                        the intersection, in meters per second
   * @param arrivalLaneID   the ID number of the lane in which the vehicle
   *                        should arrive at the intersection
   * @param departureLaneID the ID number of the lane in which the vehicle
   *                        should depart the intersection
   * @param aczDistance     The distance after the intersection that is
   *                        protected by an Admission Control Zone.
   * @param accProfile   a run-length encoded list of acceleration/duration
   *                        pairs to be executed by the vehicle during
   *                        intersection traversal
   */
  public Confirm(int imId, int vin,
                 int reservationID, int requestId,
                 double arrivalTime,
                 double earlyError, double lateError,
                 double arrivalVelocity,
                 int arrivalLaneID, int departureLaneID,
                 double aczDistance, Queue<double[]> accProfile) {
    super(imId, vin);
    this.reservationId = reservationID;
    this.requestId = requestId;
    this.arrivalTime = arrivalTime;
    this.earlyError = earlyError;
    this.lateError = lateError;
    this.arrivalVelocity = arrivalVelocity;
    this.arrivalLaneID = arrivalLaneID;
    this.departureLaneID = departureLaneID;
    this.aczDistance = aczDistance;
    this.accProfile = accProfile;
    messageType = Type.CONFIRM;
    size += 3 * Constants.INTEGER_SIZE +
              (5 + 2 * accProfile.size()) * Constants.DOUBLE_SIZE;
  }

  /////////////////////////////////
  // PUBLIC METHODS
  /////////////////////////////////

  // Getters

  /**
   * Get the unique ID number for the reservation confirmed by this message.
   *
   * @return the ID number for the reservation confirmed by this message
   */
  public int getReservationId() {
    return reservationId;
  }

  /**
   * Get the request ID of the request message this confirm message correspond
   * to.
   *
   * @return the id of the request message.
   */
  public int getRequestId() {
    return requestId;
  }

  /**
   * Get the time at which the receiving vehicle should arrive at the
   * intersection.
   *
   * @return the time at which the receiving vehicle should arrive at the
   *         intersection
   */
  public double getArrivalTime() {
    return arrivalTime;
  }

  /**
   * Get the maximum amount of time before the arrival time that the receiving
   * vehicle can safely arrive at the intersection.
   *
   * @return the maximum amount of time before the arrival time that the
   *         receiving vehicle can safely arrive at the intersection, in
   *         seconds
   */
  public double getEarlyError() {
    return earlyError;
  }

  /**
   * Get the maximum amount of time after the arrival time that the receiving
   * vehicle can safely arrive at the intersection.
   *
   * @return the maximum amount of time after the arrival time that the
   *         receiving vehicle can safely arrive at the intersection, in
   *         seconds
   */
  public double getLateError() {
    return lateError;
  }

  /**
   * Get the velocity at which the vehicle should arrive at the intersection.
   *
   * @return the velocity at which the vehicle should arrive at the
   *         intersection, in meters per second
   */
  public double getArrivalVelocity() {
    return arrivalVelocity;
  }

  /**
   * Get the ID number of the lane in which the vehicle should arrive at the
   * intersection.
   *
   * @return the ID number of the lane in which the vehicle should arrive at
   *         the intersection
   */
  public int getArrivalLaneID() {
    return arrivalLaneID;
  }

  /**
   * Get the ID number of the lane in which the vehicle should depart the
   * intersection.
   *
   * @return the ID number of the lane in which the vehicle should depart the
   *         intersection
   */
  public int getDepartureLaneID() {
    return departureLaneID;
  }

  /**
   * Get the distance the Admission Control Zone extends past the intersection
   * for this reservation.
   *
   * @return the length of the Admission Control Zone after the intersection
   *         for this reservation
   */
  public double getACZDistance() {
    return aczDistance;
  }

  /**
   * Get a run-length encoded list of acceleration/duration pairs to be
   * executed by the vehicle during intersection traversal.
   *
   * @return a run-length encoded list of acceleration/duration pairs (in
   * meters per second squared and meters, respectively) to be  executed by
   * the vehicle during intersection traversal
   */
  public Queue<double[]> getAccelerationProfile() {
    return accProfile;
  }

  /////////////////////////////////
  // PUBLIC METHODS
  /////////////////////////////////

  @Override
  public String toString() {
    return "Confirm(im" + getImId() + " -> vin" + getVin()
           + ", reservationID=" + reservationId
           + ", requestID=" + requestId
           + ", arrivalLane=" + arrivalLaneID
           + ", departureLane=" + departureLaneID
           + ", arrTime=" + Constants.TWO_DEC.format(arrivalTime)
           + ", arrVelocity=" + Constants.TWO_DEC.format(arrivalVelocity)
           + ", aczDistance=" + Constants.TWO_DEC.format(aczDistance)
           + ", accProfile=" + accProfileToString()
           + ", earlyError=" + Constants.TWO_DEC.format(earlyError)
           + ", lateError=" + Constants.TWO_DEC.format(lateError)
           + ")";
  }

  private String accProfileToString() {
    String result = "[";
    boolean isFirst = true;
    for(double[] pair : accProfile) {
      if (isFirst) {
        isFirst = false;
      } else {
        result += " ";
      }
      result +=
        "(" + Constants.TWO_DEC.format(pair[0]) + ","
          + Constants.TWO_DEC.format(pair[1]) + ")";
    }
    result += "]";
    return result;
  }

}
