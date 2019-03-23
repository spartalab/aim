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
import java.util.HashMap;
import java.util.Map;

/**
 * The approximate N-Phases traffic signal request handler.
 */
public class ApproxNPhasesTrafficSignalRequestHandler implements
    TrafficSignalRequestHandler {

  /////////////////////////////////
  // NESTED CLASSES
  /////////////////////////////////

  /**
   * The interface of signal controllers.
   */
  public static interface SignalController {

    /**
     * Get the signal at the given time
     *
     * @param time  the given time
     * @return the signal
     */
    TrafficSignal getSignal(double time);
  }

  /**
   * The cyclic signal controller.
   */
  public static class CyclicSignalController implements SignalController {

    /** The durations of the signals */
    private double[] durations;
    /** The list of signals */
    private TrafficSignal[] signals;
    /** The duration offset */
    private double durationOffset;
    /** The total duration */
    private double totalDuration;


    public CyclicSignalController(double[] durations, TrafficSignal[] signals) {
      this(durations, signals, 0.0);
    }

    /**
     * Create a cyclic signal controller.
     *
     * @param durations       the durations of the signals
     * @param signals         the list of signals
     * @param durationOffset  the duration offset
     */
    public CyclicSignalController(double[] durations, TrafficSignal[] signals,
                                 double durationOffset) {
      this.durations = durations.clone();
      this.signals = signals.clone();
      this.durationOffset = durationOffset;

      totalDuration = 0.0;
      for (double d : durations) {
        totalDuration += d;
      }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TrafficSignal getSignal(double time) {
      double d = time - Math.floor((time + durationOffset) / totalDuration) *
                        totalDuration;
      assert 0.0 <= d && d < totalDuration;
      double maxd = 0.0;
      for(int i=0; i<durations.length; i++) {
        maxd += durations[i];
        if (d < maxd) {
          return signals[i];
        }
      }
      assert false:("Error in CyclicLightController()");
      return null;
    }
  }

  /////////////////////////////////
  // PRIVATE FIELDS
  /////////////////////////////////

  /**
   * A mapping from lane ID to the traffic signal controllers on the lane.
   */
  private Map<Integer,SignalController> signalControllers;
  /** The base policy */
  private BasePolicyCallback basePolicy;


  /////////////////////////////////
  // CONSTRUCTORS
  /////////////////////////////////

  /**
   * Create the approximate N-Phases traffic signal request handler.
   */
  public ApproxNPhasesTrafficSignalRequestHandler() {
    signalControllers = new HashMap<Integer,SignalController>();
  }

  /////////////////////////////////
  // PUBLIC METHODS
  /////////////////////////////////

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
  public void setBasePolicyCallback(BasePolicyCallback basePolicy) {
    this.basePolicy = basePolicy;
  }

  /**
   * Set the traffic signal controller of a lane
   *
   * @param laneId            the lane ID
   * @param signalController  the signal controller
   */
  public void setSignalControllers(int laneId,
                                   SignalController signalController) {
    signalControllers.put(laneId, signalController);
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
    if (!canEnterFromLane(proposals.get(0).getArrivalLaneID(),
                          proposals.get(0).getDepartureLaneID())) {
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
  public TrafficSignal getSignal(int laneId) {
    return signalControllers.get(laneId).getSignal(
        basePolicy.getCurrentTime());
  }


  /////////////////////////////////
  // PRIVATE METHODS
  /////////////////////////////////

  /**
   * Check whether the vehicle can enter the intersection from a lane at
   * the current time.  This method is intended to be overridden by superclass.
   *
   * @param arrivalLaneId  the id of the lane from which the vehicle enters
   *                the intersection.
   * @return whether the vehicle can enter the intersection
   */
  private boolean canEnterFromLane(int arrivalLaneId, int departureLaneId) {
    if (getSignal(arrivalLaneId) == TrafficSignal.GREEN) {
      return true;
    } else {
      // TODO: should not hard code it: need to make it more general
      if (arrivalLaneId == 2 && departureLaneId == 8) {
        return true;
      } else if (arrivalLaneId == 11 && departureLaneId == 2) {
        return true;
      } else if (arrivalLaneId == 5 && departureLaneId == 11) {
        return true;
      } else if (arrivalLaneId == 8 && departureLaneId == 5) {
        return true;
      } else {
        return false;
      }
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public StatCollector<?> getStatCollector() {
    return null;
  }

}
