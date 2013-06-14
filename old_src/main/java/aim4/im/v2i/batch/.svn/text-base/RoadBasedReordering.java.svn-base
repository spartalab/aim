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
package aim4.im.v2i.batch;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;

import aim4.config.Debug;
import aim4.im.TrackModel;
import aim4.im.v2i.RequestHandler.BatchModeRequestHandler.IndexedProposal;
import aim4.map.Road;
import aim4.msg.v2i.Request.Proposal;

/**
 * The road-based reorder strategy.
 */
public class RoadBasedReordering implements ReorderingStrategy {

  /////////////////////////////////
  // CONSTANTS
  /////////////////////////////////

  /**
   * The time period between the processing times.
   */
  public static final double DEFAULT_PROCESSING_INTERVAL = 2.0;  // seconds

  /**
   * The estimated time this reordering strategy takes to compute the
   * reordering plus the time the intersection manager takes to send
   * the confirm and reject messages.
   */
  private static final double COMP_COMM_DELAY = 0.05; // seconds

  /**
   * The amount of lookahead (the period of time between the end time of the
   * last processed batch and the target batch).
   */
  private static final double LOOKAHEAD_TIME = 3.0;  // seconds

  /**
   * The amount of time of a batch.
   */
  private static final double BATCH_INTERVAL = DEFAULT_PROCESSING_INTERVAL;


  /////////////////////////////////
  // PRIVATE FIELDS
  /////////////////////////////////

  /**
   * The next processing time for the next batch.
   */
  private double nextProcessingTime;

  /**
   * The next proposal deadline for the next batch.
   */
  private double nextProposalDeadline;


  /**
   * The time period between the processing times.
   */
  private double processingInterval = DEFAULT_PROCESSING_INTERVAL;


  /////////////////////////////////
  // CONSTRUCTORS
  /////////////////////////////////

  /**
   * Create a road based reordering strategy
   *
   * @param processingInterval  the processing interval
   */
  public RoadBasedReordering(double processingInterval) {
    this.processingInterval = processingInterval;
  }

  /////////////////////////////////
  // PUBLIC METHODS
  /////////////////////////////////

  /**
   * {@inheritDoc}
   */
  @Override
  public void setInitialTime(double initTime) {
    nextProcessingTime = initTime + processingInterval;
    nextProposalDeadline = nextProcessingTime + COMP_COMM_DELAY;
  }


  /////////////////////////////////
  // PUBLIC METHODS
  /////////////////////////////////

  /**
   * {@inheritDoc}
   */
  @Override
  public List<IndexedProposal> getBatch(double currentTime,
                                        NavigableSet<IndexedProposal> queue,
                                        TrackModel trackModel) {

    List<IndexedProposal> proposals1 = selectProposals(currentTime, queue);
    List<IndexedProposal> proposals2 = reorderProposals(proposals1);

    nextProcessingTime = currentTime + processingInterval;
    nextProposalDeadline = nextProcessingTime + COMP_COMM_DELAY;
    return proposals2;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public double getNextProcessingTime() {
    return nextProcessingTime;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public double getNextProposalDeadline() {
    return nextProposalDeadline;
  }

  /////////////////////////////////
  // PRIVATE METHODS
  /////////////////////////////////

  /**
   * Select the set of proposals in a batch.
   *
   * @param currentTime  the current time
   * @param queue        the message queue
   * @return the set of proposals in a batch
   */
  private List<IndexedProposal> selectProposals(
                                  double currentTime,
                                  NavigableSet<IndexedProposal> queue) {
    List<IndexedProposal> result = new LinkedList<IndexedProposal>();

    double startTime = currentTime + LOOKAHEAD_TIME;
    double endTime = startTime + BATCH_INTERVAL;

    for(IndexedProposal iProposal : queue) {
      Proposal proposal = iProposal.getProposal();
      double arrivalTime = proposal.getArrivalTime();
      if (arrivalTime < endTime) {    // exclude the arrivalTime == maxTime
        result.add(iProposal);
      } else {
        // the remaining proposals in the queue have a larger arrival time
        break;
      }
    }

    return result;
  }

  /**
   * Reorder a list of indexed proposals.
   *
   * @param iProposals a list of indexed proposals
   * @return a reordered list of indexed proposals
   */
  private List<IndexedProposal> reorderProposals(
                                             List<IndexedProposal> iProposals) {
    // a partition of the proposals according to the road of the arrival lane.
    Map<Road,List<IndexedProposal>> partition =
      new HashMap<Road,List<IndexedProposal>>();

    for(IndexedProposal iProposal : iProposals) {
      int laneId = iProposal.getProposal().getArrivalLaneID();
      Road road = Debug.currentMap.getRoad(laneId);
      if (partition.containsKey(road)) {
        partition.get(road).add(iProposal);
      } else {
        List<IndexedProposal> l = new LinkedList<IndexedProposal>();
        l.add(iProposal);
        partition.put(road, l);
      }
    }

    // combine the proposals according to the partition
    List<IndexedProposal> result = new LinkedList<IndexedProposal>();

    for(List<IndexedProposal> iProposals2 : partition.values()) {
      result.addAll(iProposals2);
    }

    return result;
  }


}
