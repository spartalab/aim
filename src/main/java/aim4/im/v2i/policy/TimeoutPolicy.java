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
package aim4.im.v2i.policy;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import aim4.im.Intersection;
import aim4.im.TrackModel;
import aim4.im.v2i.V2IManager;
import aim4.im.v2i.V2IManagerCallback;
import aim4.im.v2i.reservation.AczManager;
import aim4.im.v2i.reservation.AdmissionControlZone;
import aim4.im.v2i.reservation.ReservationGrid;
import aim4.im.v2i.reservation.ReservationGridManager;
import aim4.msg.i2v.Confirm;
import aim4.msg.i2v.I2VMessage;
import aim4.msg.i2v.Reject;
import aim4.msg.v2i.Request;
import aim4.msg.v2i.V2IMessage;
import aim4.sim.StatCollector;


/**
 * The timeout policy.
 */
public class TimeoutPolicy implements Policy, V2IManagerCallback {

  /////////////////////////////////
  // CONSTANTS
  /////////////////////////////////

  /**
   * The default length of timeout, in seconds. {@value} seconds.
   */
  private static final double DEFAULT_TIMEOUT_LENGTH = 1.0;


  /////////////////////////////////
  // NESTED CLASSES
  /////////////////////////////////

  /**
   * How the V2IManager treats vehicles that communicate before their timeout
   * is over.
   */
  public enum TimeoutPolicyType {
    /** The timeout is unaffected by new messages. */
    MAINTAIN,
    /** The timeout is extended based on the latest message. */
    COMPOUND,
    /**
     * The timeout is reset based on the latest message (but not made
     * earlier).
     */
    RESET,
  }


  /////////////////////////////////
  // PRIVATE FIELDS
  /////////////////////////////////

  /**
   * The intersection manager
   */
  private V2IManagerCallback im;

  /**
   * The underlying policy
   */
  private Policy policy;

  /**
   * The time-out length
   */
  private double timeoutLength = DEFAULT_TIMEOUT_LENGTH;

  /**
   * The type of timeout policy to deal with vehicles that retransmit before
   * their previous timeout is up.
   */
  private TimeoutPolicyType timeoutPolicyType = TimeoutPolicyType.MAINTAIN;

  /**
   * A map that keeps a record of the timeouts for each vehicle.  It maps
   * the VIN number of the vehicle to the next time at which that vehicle
   * is allowed to communicate with the V2IManager.
   */
  private Map<Integer, Double> timeouts =
    new LinkedHashMap<Integer, Double>();


  /////////////////////////////////
  // CONSTRUCTORS
  /////////////////////////////////


  /**
   * Create a timeout policy
   *
   * @param im      the intersection manager
   * @param policy  the underlying policy
   */
  public TimeoutPolicy(V2IManager im, Policy policy) {
    this.im = im;
    this.policy = policy;
    policy.setV2IManagerCallback(this);
  }


  /////////////////////////////////
  // PUBLIC METHODS
  /////////////////////////////////

  /**
   * {@inheritDoc}
   */
  @Override
  public void setV2IManagerCallback(V2IManagerCallback im) {
    this.im = im;
  }

  /////////////////////////////////
  // PUBLIC METHODS
  /////////////////////////////////

  // The IM callback interface

  /**
   * {@inheritDoc}
   */
  @Override
  public void sendI2VMessage(I2VMessage msg) {
    if (msg instanceof Confirm) {
      im.sendI2VMessage(msg);
    } else if (msg instanceof Reject) {
      // replace the nextComm
      Reject rejectMsg = (Reject)msg;
      double nextComm = im.getCurrentTime() + timeoutLength;
      timeouts.put(msg.getVin(), nextComm);
      im.sendI2VMessage(new Reject(rejectMsg.getImId(),
                                   rejectMsg.getVin(),
                                   rejectMsg.getRequestId(),
                                   nextComm, // the new nextAllowedComm
                                   rejectMsg.getReason()));
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int getId() {
    return im.getId();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public double getCurrentTime() {
    return im.getCurrentTime();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Intersection getIntersection() {
    return im.getIntersection();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public ReservationGrid getReservationGrid() {
    return im.getReservationGrid();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public ReservationGridManager getReservationGridManager() {
    return im.getReservationGridManager();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public AdmissionControlZone getACZ(int laneId) {
    return im.getACZ(laneId);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public AczManager getAczManager(int laneId) {
    return im.getAczManager(laneId);
  }


  /////////////////////////////////
  // PUBLIC METHODS
  /////////////////////////////////

  /**
   * {@inheritDoc}
   */
  @Override
  public void act(double timeStep) {
    // let policy.act() runs first (but can also runs second)
    policy.act(timeStep);
    // Clean out timeouts before the current time
    for(Iterator<Double> iter = timeouts.values().iterator(); iter.hasNext();) {
      if(iter.next() <= im.getCurrentTime()) {
        iter.remove();
      }
    }
  }


  // message handler

  /**
   * {@inheritDoc}
   */
  @Override
  public void processV2IMessage(V2IMessage msg) {
    if (msg instanceof Request) {
      // check to see if the request message within the timeout period.
      if (timeouts.containsKey(msg.getVin())) {
        // reject the message and increase the timeout
        double nextComm = calcTimeOut(msg.getVin());
        timeouts.put(msg.getVin(), nextComm);
        sendI2VMessage(new Reject(im.getId(),
                                  msg.getVin(),
                                  ((Request)msg).getRequestId(),
                                  nextComm,
                                  Reject.Reason.BEFORE_NEXT_ALLOWED_COMM));
      } else {
        policy.processV2IMessage(msg);
      }
    } else {
      policy.processV2IMessage(msg);
    }
  }


  /////////////////////////////////
  // PRIVATE FIELDS
  /////////////////////////////////

  /**
   * Calculate the time out.
   *
   * @param vin  the VIN of a vehicle
   *
   * @return the timeout
   */
  private double calcTimeOut(int vin) {
    double nextComm = timeouts.get(vin); // must exist

    switch(timeoutPolicyType) {
    case MAINTAIN:
      return nextComm;
    case RESET:
      return im.getCurrentTime() + timeoutLength;
    case COMPOUND:
      return nextComm + timeoutLength;
    default:
      throw new RuntimeException("Unhandled timeout policy: " +
                                 timeoutPolicyType);
    }
  }


  /////////////////////////////////
  // PUBLIC METHODS
  /////////////////////////////////

  // statistics

  /**
   * {@inheritDoc}
   */
  @Override
  public StatCollector<?> getStatCollector() {
    return policy.getStatCollector();
  }


  /**
   * {@inheritDoc}
   */
  @Override
  public TrackModel getTrackModel() {
    return im.getTrackModel();
  }


}
