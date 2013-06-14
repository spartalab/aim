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

import java.util.List;

import aim4.im.TrackModel;
import aim4.im.v2i.policy.BasePolicy.ReserveParam;
import aim4.msg.i2v.Reject;
import aim4.msg.v2i.Request;

/**
 * The base policy's callback interface.
 */
public interface BasePolicyCallback {

  /**
   * Send a confirm message
   *
   * @param latestRequestId  the latest request id of the vehicle
   * @param reserveParam     the reservation parameter
   */
  void sendComfirmMsg(int latestRequestId,
                      BasePolicy.ReserveParam reserveParam);
  /**
   * Send a reject message
   *
   * @param vin              the VIN
   * @param latestRequestId  the latest request id of the vehicle
   * @param reason           the reason of rejection
   */
  void sendRejectMsg(int vin, int latestRequestId, Reject.Reason reason);

  /**
   * Compute the reservation parameter given the request message and a
   * set of proposals.
   *
   * @param msg        the request message
   * @param proposals  the set of proposals
   * @return the reservation parameters; null if the reservation is infeasible.
   */
  ReserveParam findReserveParam(Request msg, List<Request.Proposal> proposals);

  /**
   * Get the current time
   *
   * @return the current time
   */
  double getCurrentTime();

  /**
   * Check whether the vehicle currently has a reservation.
   *
   * @param vin  the VIN of the vehicle
   * @return whether the vehicle currently has a reservation.
   */
  boolean hasReservation(int vin);


  // TODO: remove this function
  TrackModel getTrackMode();
}
