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
package aim4.im.v2i;

import aim4.im.Intersection;
import aim4.im.TrackModel;
import aim4.im.v2i.reservation.AczManager;
import aim4.im.v2i.reservation.AdmissionControlZone;
import aim4.im.v2i.reservation.ReservationGrid;
import aim4.im.v2i.reservation.ReservationGridManager;
import aim4.msg.i2v.I2VMessage;

/**
 * An interface of the methods of V2IManager that are available for
 * the policies.
 */
public interface V2IManagerCallback {

  /**
   * A callback method for sending a I2V message.
   *
   * @param msg a I2V message
   */
  void sendI2VMessage(I2VMessage msg);

  /**
   * Get the id of the intersection manager.
   *
   * @return the id of the intersection manager.
   */
  int getId();

  /**
   * Get the current time
   *
   * @return the current time
   */
  double getCurrentTime();

  /**
   * Get the intersection managed by this intersection manager.
   *
   * @return the intersection managed by this intersection manager
   */
  Intersection getIntersection();


  // TODO: remove this function
  TrackModel getTrackModel();

  /**
   * Get the reservation grid.
   *
   * @return the reservation grid
   */
  ReservationGrid getReservationGrid();

  /**
   * Get the manager of the reservation grid
   *
   * @return the manager of the reservation grid
   */
  ReservationGridManager getReservationGridManager();

  /**
   * Get the Admission Control Zone of a given lane.
   *
   * @param laneId  the id of the lane
   * @return the admission control zone of the lane.
   */
  AdmissionControlZone getACZ(int laneId);

  /**
   * Get the manager of an ACZ
   */
  AczManager getAczManager(int laneId);

}
