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

import java.util.List;
import java.util.NavigableSet;

import aim4.im.TrackModel;
import aim4.im.v2i.RequestHandler.BatchModeRequestHandler.IndexedProposal;

/**
 * The Reordering Strategy Interface.
 */
public interface ReorderingStrategy {

  /**
   * Set the initial time of the batch mode.
   *
   * @param initTime  the initial time of the batch mode.
   */
  void setInitialTime(double initTime);

  /**
   * Select a subset of indexed proposals from the queue and sort them in a
   * particular order such that they will be processed in that order.
   *
   * @param currentTime  the current time
   * @param queue        the current queue of all indexed proposals
   * @param trackModel   the track model
   * @return an ordered list of indexed proposals
   */
  List<IndexedProposal> getBatch(double currentTime,
                                 NavigableSet<IndexedProposal> queue,
                                 TrackModel trackModel);

  /**
   * Get the next processing time for the next batch.
   *
   * @return the next processing time.
   */
  double getNextProcessingTime();

  /**
   * Get the next proposal deadline for the next batch.
   *
   * @return the next proposal deadline.
   */
  double getNextProposalDeadline();
}
