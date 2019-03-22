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

import java.awt.Color;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeSet;

import aim4.config.Debug;
import aim4.im.v2i.batch.ReorderingStrategy;
import aim4.im.v2i.policy.BasePolicy;
import aim4.im.v2i.policy.BasePolicyCallback;
import aim4.im.v2i.policy.BasePolicy.ProposalFilterResult;
import aim4.im.v2i.policy.BasePolicy.ReserveParam;
import aim4.msg.i2v.Reject;
import aim4.msg.i2v.Reject.Reason;
import aim4.msg.v2i.Request;
import aim4.msg.v2i.Request.Proposal;
import aim4.sim.StatCollector;
import aim4.util.Util;


/**
 * The batch mode request handler.
 */
public class BatchModeRequestHandler implements RequestHandler {

  /////////////////////////////////
  // CONSTANTS
  /////////////////////////////////

  /**
   * Whether the vehicle in the batch should be highlighted.
   */
  private static final boolean IS_HIGHLIGHT_VEHICLE_IN_BATCH = true;

  /**
   * The color of the vehicle in the batch.
   */
  private static final Color VEHICLE_IN_BATCH_COLOR = Color.GREEN;


  /////////////////////////////////
  // NESTED CLASSES
  /////////////////////////////////

  /**
   * A proposal with a unique ID.
   */
  public static class IndexedProposal implements Comparator<IndexedProposal>,
                                                 Comparable<IndexedProposal> {
    /** The ID number of this indexed proposal */
    private int id;

    /** A proposal */
    private Proposal proposal;

    /** The request message of this proposal */
    private Request request;

    /** A reference to the group of proposals of the same request message */
    private List<IndexedProposal> proposalGroup;

    // NOTE: the submission time should be associated with the request,
    // not proposal. But since the submission time has limited usage,
    // we put it here as a member of an indexed proposal.  In the future,
    // if there are more things to assocate with the request,
    // we should define a new class containing the request message
    // and the associated data.

    /** The submission time of the request message */
    private double submissionTime;

    /////////////////////////////////
    // CONSTRUCTORS
    /////////////////////////////////

    /**
     * Create a new indexed proposal
     *
     * @param id             the ID of this request policy
     * @param proposal       a proposal
     * @param request        the request message of the proposal
     * @param proposalGroup  a reference to the group of proposals of a request
     *                       message
     * @param submissionTime the submission time
     */
    public IndexedProposal(int id,
                           Proposal proposal,
                           Request request,
                           List<IndexedProposal> proposalGroup,
                           double submissionTime) {
      this.id = id;
      this.proposal = proposal;
      this.request = request;
      this.proposalGroup = proposalGroup;
      this.submissionTime = submissionTime;
    }


    /////////////////////////////////
    // PUBLIC METHODS
    /////////////////////////////////

    /**
     * Get the proposal.
     *
     * @return the proposal
     */
    public Proposal getProposal() {
      return proposal;
    }

    /**
     * Get the request message of this proposal.
     *
     * @return the request message of this proposal.
     */
    public Request getRequest() {
      return request;
    }

    /**
     * Get the reference to the group of proposals of the request message in
     * to which the proposal belongs.
     *
     * @return a reference to the group of proposals
     */
    public List<IndexedProposal> getProposalGroup() {
      return proposalGroup;
    }

    /**
     * Get the submission time of the request.
     *
     * @return the submission time of the request.
     */
    public double getSubmissionTime() {
      return submissionTime;
    }

    /**
     * Whether or not this indexed proposal is equal to the given indexed
     * proposal.
     *
     * @param ip  an indexed proposal
     * @return whether or not this indexed proposal is equal to the given
     *         indexed proposal.
     */
    public boolean equals(IndexedProposal ip) {
      return (id == ip.id) && proposal.equals(ip.proposal);
    }

    /**
     * Compare two indexed proposals
     *
     * @param ip1  an indexed proposal
     * @param ip2  an indexed proposal
     *
     * @return -1 if ip1 < ip2; +1 if ip1 > ip2; 0 if ip1 == ip2
     */
    @Override
    public int compare(IndexedProposal ip1, IndexedProposal ip2) {
      if (ip1.proposal.getArrivalTime() < ip2.proposal.getArrivalTime()) {
        return -1;
      } else if (ip1.proposal.getArrivalTime() > ip2.proposal.getArrivalTime()){
        return 1;
      } else {
        return ip1.id - ip2.id;
      }
    }

    /**
     * Compare this indexed proposal with a given indexed proposal
     *
     * @param ip  an indexed proposal
     *
     * @return -1 if < ip; +1 if > ip; 0 if == ip
     */
    @Override
    public int compareTo(IndexedProposal ip) {
      return compare(this, ip);
    }

    /////////////////////////////////
    // FOR DEBUG
    /////////////////////////////////

    /**
     * Get the description of this batch policy.
     *
     * @return the description of this batch policy
     */
    @Override
    public String toString() {
      return proposal.toString() + ":id" + id;
    }

  }


  /**
   * The Request statistic collector.
   */
  public static class RequestStatCollector implements
                               StatCollector<BatchModeRequestHandler> {
    /** The total number of request */
    int totalNumOfRequest = 0;
    /** The number of confirmed another requests */
    int numOfConfirmedAnotherRequest = 0;
    /** The number of late request */
    int numOfLateRequest = 0;
    /** The number of queued request */
    int numOfQueuedRequest = 0;

    /**
     * Increase the total number of request.
     */
    public void incrTotalNumOfRequest() {
      totalNumOfRequest++;
    }

    /**
     * Increase the number of confirmed another request.
     */
    public void incrNumOfConfirmedAnotherRequest() {
      numOfConfirmedAnotherRequest++;
    }

    /**
     * Increase the number of late request.
     */
    public void incrNumOfLateRequest() {
      numOfLateRequest++;
    }

    /**
     * Increase the number of queued request.
     */
    public void incrNumOfQueuedRequest() {
      numOfQueuedRequest++;
    }

    /**
     * Collect the statistic in a batch mode request handler.
     *
     * @param obj  the batch mode request handler
     */
    @Override
    public void collect(BatchModeRequestHandler obj) {
      // do nothing
    }

    /**
     * Print the statistic.
     *
     * @param outfile  the output stream
     */
    @Override
    public void print(PrintStream outfile) {
      outfile.printf("totalNumOfRequest,%d\n", totalNumOfRequest);
      outfile.printf("numOfConfirmedAnotherRequest,%d\n",
                     numOfConfirmedAnotherRequest);
      outfile.printf("numOfLateRequest,%d\n", numOfLateRequest);
      outfile.printf("numOfQueuedRequest,%d\n", numOfQueuedRequest);
    }

  }

  /////////////////////////////////
  // PRIVATE FIELDS
  /////////////////////////////////

  /**
   * The base policy.
   */
  private BasePolicyCallback basePolicy;

  /**
   * A reordering strategy
   */
  private ReorderingStrategy reorderingStrategy;

  /**
   * The ID of the next indexed proposal.
   */
  private int nextIndexedProposalId;

  /**
   * The indexed proposal queue.
   */
  private NavigableSet<IndexedProposal> queue;

  /**
   * The time at which the proposals in the next batch will be processed.
   */
  private double nextProcessingTime;

  /**
   * The next proposal deadline for the next batch. No proposal in the
   * queue should has an arrival time less than the next proposal deadline.
   * (but the arrival time can be exactly the proposal deadline.)
   */
  private double nextProposalDeadline;

  // statistics

  /**
   * The request statistic collector.
   */
  private RequestStatCollector requestSC = null;

  // Debug

  /**
   * The set of vehicles' VIN in the last batch.
   */
  private Set<Integer> lastVinInBatch = new HashSet<Integer>();


  /////////////////////////////////
  // CONSTRUCTORS
  /////////////////////////////////

  /**
   * Create a batch mode request handler.
   *
   * @param reorderingStrategy the reorder strategy
   */
  public BatchModeRequestHandler(ReorderingStrategy reorderingStrategy) {
    this(reorderingStrategy, null);
  }

  /**
   * Create a batch mode request handler.
   *
   * @param reorderingStrategy  the reorder strategy
   * @param requestSC           the request statistic collector
   */
  public BatchModeRequestHandler(ReorderingStrategy reorderingStrategy,
                                 RequestStatCollector requestSC) {
    this.reorderingStrategy = reorderingStrategy;
    this.queue = new TreeSet<IndexedProposal>();
    this.requestSC = requestSC;
    nextIndexedProposalId = 0;
  }


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
    reorderingStrategy.setInitialTime(basePolicy.getCurrentTime());
    nextProcessingTime = reorderingStrategy.getNextProcessingTime();
    nextProposalDeadline = reorderingStrategy.getNextProposalDeadline();
  }

  /**
   * Let the request handler to act for a given time period.
   *
   * @param timeStep  the time period
   */
  @Override
  public void act(double timeStep) {
    if (Util.isDoubleEqualOrGreater(basePolicy.getCurrentTime(),
                                    nextProcessingTime)) {
      Set<Integer> vinInBatch = processBatch();
      if (IS_HIGHLIGHT_VEHICLE_IN_BATCH) {
        for(int vin : lastVinInBatch) {
          Debug.removeVehicleColor(vin);
        }
        for(int vin : vinInBatch) {
          Debug.setVehicleColor(vin, VEHICLE_IN_BATCH_COLOR);
        }
        lastVinInBatch = vinInBatch;
      }

      nextProcessingTime = reorderingStrategy.getNextProcessingTime();
      nextProposalDeadline = reorderingStrategy.getNextProposalDeadline();

      // after updating the proposal deadline, immediately confirm/reject
      // the indexed proposal in the queue whose expiration time is before
      // the new proposal deadline.
      tryReserveForProposalsBeforeTime(nextProposalDeadline);
    } // else do nothing
  }

  /**
   * Process the request message.
   *
   * @param msg the request message
   */
  @Override
  public void processRequestMsg(Request msg) {
    int vin = msg.getVin();

    if (requestSC!=null) requestSC.incrTotalNumOfRequest();

    // If the vehicle has got a reservation already, reject it.
    // TODO: think about multiple reservation of the same vehicle.
    if (basePolicy.hasReservation(vin)) {
      basePolicy.sendRejectMsg(vin,
                               msg.getRequestId(),
                               Reject.Reason.CONFIRMED_ANOTHER_REQUEST);
      if (requestSC!=null) requestSC.incrNumOfConfirmedAnotherRequest();
      return;
    }

    // First, remove the proposals of the vehicle (if any) in the queue.
    removeProposalsByVIN(vin);

    double currentTime = basePolicy.getCurrentTime();
    List<Proposal> proposals = msg.getProposals();

    // filter the proposals
    ProposalFilterResult filterResult =
      BasePolicy.standardProposalsFilter(proposals, currentTime);

    if (filterResult.isNoProposalLeft()) {
      // reject it immediately (note that the existing proposals of the vehicle
      // has been removed from the queue.
      basePolicy.sendRejectMsg(vin,
                               msg.getRequestId(),
                               filterResult.getReason());
      return;
    }

    // If all proposals are late (i.e., their arrival times are less than the
    // last processing time.)
    if (isAllProposalsLate(msg)) {
      // Immediately confirm/reject the remaining proposals.
      ReserveParam reserveParam =
        basePolicy.findReserveParam(msg, filterResult.getProposals());
      if (reserveParam != null) {
        basePolicy.sendComfirmMsg(msg.getRequestId(), reserveParam);
      } else {
        basePolicy.sendRejectMsg(vin, msg.getRequestId(),
                                 Reject.Reason.NO_CLEAR_PATH);
      }
      if (requestSC!=null) requestSC.incrNumOfLateRequest();
    } else {
      // Put the proposals in the queue and postpone the processing of
      // these proposals.  Late proposal would not be put in the queue.
      putProposalsIntoQueue(msg, currentTime);
      if (requestSC!=null) requestSC.incrNumOfQueuedRequest();
    }
  }


  /**
   * Get the statistic collector.
   *
   * @return the statistic collector
   */
  @Override
  public StatCollector<?> getStatCollector() {
    return requestSC;
  }


  /////////////////////////////////
  // PRIVATE METHODS
  /////////////////////////////////

  /**
   * Process the target proposals in the target interval.
   *
   * @return the VIN of the vehicles in the batch
   */
  private Set<Integer> processBatch() {
    Set<Integer> vinInBatch = new HashSet<Integer>();

    double currentTime = basePolicy.getCurrentTime();

    // make sure that no proposal on the queue is before the deadline
    assert (queue.size()==0) || (queue.first().getProposal().getArrivalTime() >=
                                 nextProposalDeadline);

    // retrieve the batch (the set of indexed proposals)
    List<IndexedProposal> batch =
      reorderingStrategy.getBatch(currentTime,
                                  queue,
                                  basePolicy.getTrackMode());

    // confirm or reject the proposals in the batch according to
    // the new ordering
    for(IndexedProposal iProposal : batch) {
      tryReserve(iProposal);
      vinInBatch.add(iProposal.getRequest().getVin());
    }

    return vinInBatch;
  }

  /**
   * Try to make a reservation for a proposal and send the confirm message.
   * If no reservation is possible, send the reject message.
   *
   * @param iProposal  the indexed proposal
   */
  private void tryReserve(IndexedProposal iProposal){
    List<Proposal> l = new ArrayList<Proposal>(1);
    l.add(iProposal.getProposal());
    Request msg = iProposal.getRequest();
    ReserveParam reserveParam = basePolicy.findReserveParam(msg, l);
    if (reserveParam != null) {
      basePolicy.sendComfirmMsg(msg.getRequestId(), reserveParam);
      // Remove a set of indexed proposals (including the given one)
      // from the queue.
      for(IndexedProposal iProposal2 : iProposal.getProposalGroup()) {
        queue.remove(iProposal2); // efficient enough since queue is a TreeSet.
      }
    } else {
      // remove the indexed proposal from the queue.
      queue.remove(iProposal);
      // shrink the proposal group
      List<IndexedProposal> ipGroup = iProposal.getProposalGroup();
      if (ipGroup.remove(iProposal)) {
        // if the proposal group is empty, no proposal left for the request
        // message and need to send the reject message.
        if (ipGroup.isEmpty()) {
          basePolicy.sendRejectMsg(msg.getVin(),
                                   msg.getRequestId(),
                                   Reason.NO_CLEAR_PATH);
        }
      } else { // the removal is unsuccessful
        throw new RuntimeException("BatchModeRequestHandler: Proposal Group " +
                                   "error: unable to remove an indexed " +
                                   "proposal.");
      }
    }
  }


  /**
   * Try to make a reservation for all indexed proposals in the queue
   * before a given time.  If the reservation of an indexed proposals is
   * successful, send the confirm message; send the reject message.
   *
   * @param iProposal  the indexed proposal
   */
  private void tryReserveForProposalsBeforeTime(double time) {
    Iterator<IndexedProposal> iter = queue.iterator();
    while(iter.hasNext()) {
      IndexedProposal iProposal = iter.next();
      if (iProposal.getProposal().getArrivalTime() < time) {
        iter.remove(); // remove the proposal from the queue.
        tryReserve(iProposal);
      } else {
        // the remaining proposals in the queue have a larger arrival time
        break;
      }
    }
  }


  /////////////////////////////////
  // PRIVATE METHODS
  /////////////////////////////////

  // for processRequestMsg()

  /**
   * Remove the proposals made by a vehicle from the queue.
   *
   * @param vin  the vehicle ID
   */
  private void removeProposalsByVIN(int vin) {
    // search for the first indexed proposal with the given vin.
    // TODO: maybe able to make it runs faster
    IndexedProposal selectedIndexedProposal = null;
    for(IndexedProposal iProposal : queue) {
      if (iProposal.getRequest().getVin() == vin) {
        selectedIndexedProposal = iProposal;
      }
    }

    // remove the indexed proposal in the indexed proposal group.
    if (selectedIndexedProposal != null) {
      for(IndexedProposal ip : selectedIndexedProposal.getProposalGroup()) {
        queue.remove(ip); // efficient enough since queue is a TreeSet.
      }
    }
  }

  /**
   * Check to see if all proposals has an arrival time that are larger than
   * or equal to the last processing time.
   *
   * @param msg  The Request message sent by the vehicle
   * @return Whether or not the arrival times of all proposals are larger than
   *         or equal to the last processing time.
   */
  private boolean isAllProposalsLate(Request msg) {
    for(Proposal proposal: msg.getProposals()) {
      if (proposal.getArrivalTime() >= nextProposalDeadline) {
        return false;
      }
    }
    return true;
  }

  /**
   * Put the on-time proposals of a request message into the queue.
   * All the late proposals will be ignored.
   *
   * @param msg  the request message
   * @param msg  the current time
   */
  private void putProposalsIntoQueue(Request msg, double currentTime) {
    List<IndexedProposal> proposalGroup =
      new LinkedList<IndexedProposal>(); // use LinkedList such that elements
                                         // can be removed efficiently.
    for(Proposal proposal: msg.getProposals()) {
      // Put only the proposal whose arrival time is on or after the
      // next proposal deadline. Ignore the late proposals
      if (proposal.getArrivalTime() >= nextProposalDeadline) {
        IndexedProposal iProposal =
          new IndexedProposal(nextIndexedProposalId,
                              proposal,
                              msg,
                              proposalGroup,
                              currentTime); // the subsmission time
        nextIndexedProposalId++;
        proposalGroup.add(iProposal);
        queue.add(iProposal); // TreeSet will sort the IP automatically.
      }
    }
  }



  /////////////////////////////////
  // DEBUG
  /////////////////////////////////

  /**
   * Print the queue.
   */
  public void printQueue() {
    boolean shouldPrintNextProcessingTime = true;
    boolean shouldPrintNextProposalDeadline = true;

    System.out.printf("--- Queue BEGIN ---\n") ;
    for(IndexedProposal iProposal: queue) {
      double arrivalTime = iProposal.getProposal().getArrivalTime();
      // --- nextProcessingTime ---
      if (shouldPrintNextProcessingTime) {
        if (arrivalTime > nextProcessingTime) {
          System.out.printf("  --- nextBatchProcessingTime = %.2f ---\n",
                            nextProcessingTime);
          shouldPrintNextProcessingTime = false;
        }
      }
      // --- nextProposalDeadline ---
      if (shouldPrintNextProposalDeadline) {
        if (arrivalTime > nextProposalDeadline) {
          System.out.printf("  --- nextProposalDeadline = %.2f ---\n",
                            nextProposalDeadline);
          shouldPrintNextProposalDeadline = false;
        }
      }
      int vin = iProposal.getRequest().getVin();
      System.out.printf("vin %d %s\n", vin, iProposal);
    }
    if (shouldPrintNextProcessingTime) {
      System.out.printf("  --- nextProcessingTime = %.2f ---\n",
                        nextProcessingTime);
    }
    if (shouldPrintNextProposalDeadline) {
      System.out.printf("  --- nextProposalDeadline = %.2f ---\n",
                        nextProposalDeadline);
    }
    System.out.printf("--- Queue END ---\n") ;
  }

}
