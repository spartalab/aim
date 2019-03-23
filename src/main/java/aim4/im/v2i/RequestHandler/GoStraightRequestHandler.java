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

import aim4.im.v2i.policy.BasePolicy;
import aim4.im.v2i.policy.BasePolicyCallback;
import aim4.im.v2i.policy.BasePolicy.ProposalFilterResult;
import aim4.im.v2i.policy.BasePolicy.ReserveParam;
import aim4.msg.i2v.Reject;
import aim4.msg.v2i.Request;
import aim4.sim.StatCollector;

/**
 * The go straight request handler.
 */
public class GoStraightRequestHandler implements RequestHandler {

  /////////////////////////////////
  // CONSTANTS
  /////////////////////////////////

  /** The switch time interval */
  private static final double SWITCH_TIME_INTERVAL = 20.0;

  /////////////////////////////////
  // PRIVATE FIELDS
  /////////////////////////////////

  /** The base policy */
  private BasePolicyCallback basePolicy;
  /** The next switch time */
  private double nextSwitchTime;
  /** Whether the vehicle should go straight */
  private boolean isGoStraight;

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
    this.basePolicy = basePolicy;
    this.nextSwitchTime = basePolicy.getCurrentTime() + SWITCH_TIME_INTERVAL;
    this.isGoStraight = true;
  }

  /**
   * Let the request handler to act for a given time period.
   *
   * @param timeStep  the time period
   */
  @Override
  public void act(double timeStep) {
    if (basePolicy.getCurrentTime() > nextSwitchTime) {
      if (isGoStraight) {
        isGoStraight = false;
      } else {
        isGoStraight = true;
      }
      nextSwitchTime = basePolicy.getCurrentTime() + SWITCH_TIME_INTERVAL;
    }
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

    // remove proposals that is not in the correct turn direction.
    removeProposalWithIncorrectTurnDirection(proposals);
    if (proposals.isEmpty()) {
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

  /**
   * Remove proposals whose arrival time is prohibited from entering
   * the intersection according to {@code canEnterAtArrivalTime()}.
   *
   * @param proposals  a set of proposals
   */
  private void removeProposalWithIncorrectTurnDirection(
                                             List<Request.Proposal> proposals) {
    for (Iterator<Request.Proposal> tpIter = proposals.listIterator();
         tpIter.hasNext();) {
      Request.Proposal p = tpIter.next();
      if (isGoStraight) {
        if (p.getArrivalLaneID() == p.getDepartureLaneID()) {
          tpIter.remove();
        }
      } else {
        if (p.getArrivalLaneID() != p.getDepartureLaneID()) {
          tpIter.remove();
        }
      }
    }
  }

}
