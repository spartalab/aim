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

import aim4.config.TrafficSignal;
import java.util.List;

import aim4.im.v2i.policy.BasePolicy;
import aim4.im.v2i.policy.BasePolicyCallback;
import aim4.im.v2i.policy.BasePolicy.ProposalFilterResult;
import aim4.im.v2i.policy.BasePolicy.ReserveParam;
import aim4.msg.i2v.Reject;
import aim4.msg.v2i.Request;
import aim4.sim.StatCollector;


// see http://ops.fhwa.dot.gov/publications/fhwahop06006/chapter_7.htm

/**
 * The approximate 4-Phases traffic signal request handler.
 */
public class Approx4PhasesTrafficSignalRequestHandler implements
    TrafficSignalRequestHandler {

  /////////////////////////////////
  // CONSTANTS
  /////////////////////////////////

  /**
   * The length of the green light duration
   */
  private static final double DEFAULT_GREEN_LIGHT_DURATION = 15.0;

  /**
   * The length of the yellow light duration
   */
  private static final double DEFAULT_YELLOW_LIGHT_DURATION = 5.0;


  /////////////////////////////////
  // PRIVATE FIELDS
  /////////////////////////////////

  /** The duration of green signals */
  private double greenLightDuration = DEFAULT_GREEN_LIGHT_DURATION;
  /** The duration of yellow signals */
  private double yellowLightDuration = DEFAULT_YELLOW_LIGHT_DURATION;
  /** The base policy */
  private BasePolicyCallback basePolicy;

  /////////////////////////////////
  // CONSTRUCTORS
  /////////////////////////////////

  /**
   * Create the approximate 4-Phases traffic signal request handler.
   *
   * @param greenLightDuration  the duration of green signals
   * @param yellowLightDuration the duration of yellow signals
   */
  public Approx4PhasesTrafficSignalRequestHandler(double greenLightDuration,
                                                  double yellowLightDuration) {
    this.greenLightDuration = greenLightDuration;
    this.yellowLightDuration = yellowLightDuration;
  }

  /////////////////////////////////
  // PUBLIC METHODS
  /////////////////////////////////

  /**
   * Set the green signals duration.
   *
   * @param greenLightDuration the green signal duration
   */
  public void setGreenLightDuration(double greenLightDuration) {
    this.greenLightDuration = greenLightDuration;
  }

  /**
   * Set the yellow signals duration.
   *
   * @param yellowLightDuration the yellow signal duration
   */
  public void setYellowLightDuration(double yellowLightDuration) {
    this.yellowLightDuration = yellowLightDuration;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setBasePolicyCallback(BasePolicyCallback basePolicy) {
    this.basePolicy = basePolicy;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void act(double timeStep) {
    // do nothing
  }

  /**
   * {@inheritDoc}
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
   * {@inheritDoc}
   */
  @Override
  public StatCollector<?> getStatCollector() {
    return null;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public TrafficSignal getSignal(int laneId) {
    double period = greenLightDuration + yellowLightDuration;
    int id = (int) Math.floor(basePolicy.getCurrentTime() / period);
    double t = basePolicy.getCurrentTime() - id * period;

    int phaseId = id % 4;

    boolean canPass = false;
    if (phaseId == 0) {
      canPass = (laneId == 9) || (laneId == 6);
    } else if (phaseId == 1) {
      canPass = (laneId == 7) || (laneId == 8) ||
                (laneId == 10) || (laneId == 11);
    } else if (phaseId == 2) {
      canPass = (laneId == 0) || (laneId == 3);
    } else if (phaseId == 3) {
      canPass = (laneId == 4) || (laneId == 5) ||
                (laneId == 1) || (laneId == 2);
    } else {
      throw new RuntimeException("Error in isGreenTrafficLightNow()");
    }

    if (canPass) {
      if (t <= greenLightDuration) {
        return TrafficSignal.GREEN;
      } else {
        return TrafficSignal.YELLOW;
      }
    } else {
      // it is either yellow or red signal
      return TrafficSignal.RED;
    }
  }

  /////////////////////////////////
  // PRIVATE FIELDS
  /////////////////////////////////

  /**
   * Check whether the vehicle can enter the intersection from a lane at
   * the current time.  This method is intended to be overridden by superclass.
   *
   * @param laneId  the id of the lane from which the vehicle enters
   *                the intersection.
   * @return whether the vehicle can enter the intersection
   */
  private boolean canEnterFromLane(int laneId) {
    if (laneId == 2 || laneId == 11 || laneId == 5 || laneId == 8) {
      return true;
    }

    double period = greenLightDuration + yellowLightDuration;
    int id = (int) Math.floor(basePolicy.getCurrentTime() / period);
    double t = basePolicy.getCurrentTime() - id * period;

    if (t <= greenLightDuration) {
      int phaseId = id % 4;
      if (phaseId == 0) {
        return (laneId == 9) || (laneId == 6);
      } else if (phaseId == 1) {
        return (laneId == 7) || (laneId == 10);
      } else if (phaseId == 2) {
        return (laneId == 0) || (laneId == 3);
      } else if (phaseId == 3) {
        return (laneId == 4) || (laneId == 1);
      } else {
        throw new RuntimeException("Error in isGreenTrafficLightNow()");
      }
    } else {
      // it is either yellow or red signal
      return false;
    }
  }


}
