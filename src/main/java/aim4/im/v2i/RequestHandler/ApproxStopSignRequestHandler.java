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
package aim4.im.v2i.RequestHandler;

import java.util.Iterator;
import java.util.List;

import aim4.config.Debug;
import aim4.im.v2i.policy.BasePolicy;
import aim4.im.v2i.policy.BasePolicyCallback;
import aim4.im.v2i.policy.ExtendedBasePolicyCallback;
import aim4.im.v2i.policy.BasePolicy.ProposalFilterResult;
import aim4.im.v2i.policy.BasePolicy.ReserveParam;
import aim4.map.Road;
import aim4.msg.i2v.Reject;
import aim4.msg.v2i.Request;
import aim4.sim.StatCollector;

/**
 * The approximate stop sign request handler.
 */
public class ApproxStopSignRequestHandler implements RequestHandler {

  /////////////////////////////////
  // CONSTANTS
  /////////////////////////////////

  /**
   * The time window before the last vehicle inside the intersection
   * leaving the intersection such that other vehicles can consider
   * entering the intersection.
   */
  public static final double DEFAULT_TIME_WINDOW_BEFORE_LAST_EXIT_VEHICLE = 0.1;

  /////////////////////////////////
  // PRIVATE FIELDS
  /////////////////////////////////

  /** The base policy */
  private ExtendedBasePolicyCallback basePolicy;
  /** The time window before last exit vehicle */
  private double timeWindowBeforeLastExitVehicle =
    DEFAULT_TIME_WINDOW_BEFORE_LAST_EXIT_VEHICLE;

  /** The name of the next road */
  private String nextRoadName = null;


  /////////////////////////////////
  // PUBLIC METHODS
  /////////////////////////////////

  /**
   * Set the base policy call-back.
   *
   * @param basePolicy  the base policy's call-back
   */
  @Override
  public void setBasePolicyCallback(BasePolicyCallback basePolicy) {
    if (basePolicy instanceof ExtendedBasePolicyCallback) {
      this.basePolicy = (ExtendedBasePolicyCallback)basePolicy;
    } else {
      throw new RuntimeException("The BasePolicyCallback for " +
                "AllStopRequestHandler must be ExtendedBasePolicyCallback.");
    }
  }

  /**
   * Let the request handler to act for a given time period.
   *
   * @param timeStep  the time period
   */
  @Override
  public void act(double timeStep) {
    // do nothing
  }

  /**
   * Process the request message.
   *
   * @param msg the request message
   */
  @Override
  public void processRequestMsg(Request msg) {
    int vin = msg.getVin();

    // If the vehicle has got a reservation already, reject it.
    if (basePolicy.hasReservation(vin)) {
      basePolicy.sendRejectMsg(vin, msg.getRequestId(),
                               Reject.Reason.CONFIRMED_ANOTHER_REQUEST);
      return;
    }

    // filter the proposals
    ProposalFilterResult filterResult =
      BasePolicy.standardProposalsFilter(msg.getProposals(),
                                         basePolicy.getCurrentTime());
    if (filterResult.isNoProposalLeft()) {
      basePolicy.sendRejectMsg(vin,
                               msg.getRequestId(),
                               filterResult.getReason());
    }

    List<Request.Proposal> proposals = filterResult.getProposals();

    // Remove proposals those arrival time is prohibited
    // according to canEnterAtArrivalTime().
    removeProposalWithInvalidArrivalTime(vin, proposals);
    if (proposals.isEmpty()) {
      basePolicy.sendRejectMsg(vin, msg.getRequestId(),
                               Reject.Reason.NO_CLEAR_PATH);
      return;
    }
    // If cannot enter from lane according to canEnterFromLane(), reject it.
    if (!canEnterFromLane(proposals.get(0).getArrivalLaneID())){
      basePolicy.sendRejectMsg(vin, msg.getRequestId(),
                               Reject.Reason.NO_CLEAR_PATH);
      return;
    }
    // try to see if reservation is possible for the remaining proposals.
    ReserveParam reserveParam = basePolicy.findReserveParam(msg, proposals);
    if (reserveParam != null) {
      basePolicy.sendComfirmMsg(msg.getRequestId(), reserveParam);
    } else {
      basePolicy.sendRejectMsg(vin, msg.getRequestId(),
                               Reject.Reason.NO_CLEAR_PATH);
    }
  }

  /**
   * Get the statistic collector.
   *
   * @return the statistic collector
   */
  @Override
  public StatCollector<?> getStatCollector() {
    return null;
  }

  /////////////////////////////////
  // PRIVATE METHODS
  /////////////////////////////////

  /**
   * Remove proposals whose arrival time is prohibited from entering
   * the intersection according to {@code canEnterAtArrivalTime()}.
   *
   * @param proposals  a set of proposals
   */
  private void removeProposalWithInvalidArrivalTime(int vin,
                                             List<Request.Proposal> proposals) {
    for (Iterator<Request.Proposal> tpIter = proposals.listIterator();
         tpIter.hasNext();) {
      Request.Proposal prop = tpIter.next();
      if (!canEnterAtArrivalTime(vin, prop.getArrivalTime())) {
        tpIter.remove();
      }
    }
  }

  /**
   * Check whether the vehicle can enter the intersection at the arrival time.
   *
   * @param vin          the VIN number of the vehicle
   * @param arrivalTime  the arrival time
   * @return whether the vehicle can enter the intersection
   */
  private boolean canEnterAtArrivalTime(int vin, double arrivalTime) {
    double lastReservedTime =
      basePolicy.getReservationGrid().getLastReservedTime();
    if (lastReservedTime - timeWindowBeforeLastExitVehicle > arrivalTime) {
      return false;
    } else {
      return true;
    }
  }


  /**
   * Check whether the vehicle can enter the intersection from a lane at
   * the current time.  This method is intended to be overridden by superclass.
   *
   * @param laneId  the id of the lane from which the vehicle enters
   *                the intersection.
   * @return whether the vehicle can enter the intersection
   */
  private boolean canEnterFromLane(int laneId) {
    Road road = Debug.currentMap.getRoad(laneId);
    String roadName = road.getName();

    if (nextRoadName != null) {
      double lastReservedTime =
        basePolicy.getReservationGrid().getLastReservedTime();
      if (roadName.equals(nextRoadName) ||
          lastReservedTime <= basePolicy.getCurrentTime()) {
        if (nextRoadName.equals("1st Avenue N")) {
          nextRoadName = "1st Street W";
        } else if (nextRoadName.equals("1st Street W")) {
          nextRoadName = "1st Avenue S";
        } else if (nextRoadName.equals("1st Avenue S")) {
          nextRoadName = "1st Street E";
        } else if (nextRoadName.equals("1st Street E")) {
          nextRoadName = "1st Avenue N";
        } else {
          throw new RuntimeException("Error in ApproxStopSignFCFSPolicy." +
                                     "canEnterFromRoad()");
        }
        return true;
      } else {
        return false;
      }
    } else {
      nextRoadName = roadName;
      return false;
    }
  }


}
