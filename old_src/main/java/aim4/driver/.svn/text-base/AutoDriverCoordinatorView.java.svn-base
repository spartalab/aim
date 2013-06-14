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
package aim4.driver;

import aim4.im.IntersectionManager;
import aim4.map.Road;
import aim4.map.lane.Lane;

/**
 * An autonomous driver's from the viewpoint of coordinators.
 */
public interface AutoDriverCoordinatorView {

  // lane

  /**
   * Get the Lane the DriverAgent is currently following.
   *
   * @return the Lane the DriverAgent is currently following
   */
  Lane getCurrentLane();

  /**
   * Set the Lane the DriverAgent is currently following.
   *
   * @param lane the Lane the DriverAgent should follow
   */
  void setCurrentLane(Lane lane);

  /**
   * Add a lane that the DriverAgent's vehicle currently occupies.
   *
   * @param lane a lane that the DriverAgent's vehicle currently occupies
   */
  void addCurrentlyOccupiedLane(Lane lane);


  // origin and destination

  /**
   * Get where this DriverAgent is going.
   *
   * @return the Road where this DriverAgent is going
   */
  Road getDestination();


  // IM

  /**
   * Get the IntersectionManager with which the agent is currently
   * interacting.
   *
   * @return the IntersectionManager with which the agent is currently
   *         interacting
   *
   */
  IntersectionManager getCurrentIM();

  /**
   * Find the next IntersectionManager that the Vehicle will need to
   * interact with, in this Lane. This version
   * overrides the version in {@link Driver}, but only to memoize it for
   * speed.
   *
   * @return the nextIntersectionManager that the Vehicle will need
   *         to interact with, in this Lane
   */
  IntersectionManager nextIntersectionManager();

  /**
   * Find the distance to the next intersection in the Lane in which
   * the Vehicle is, from the position at which the Vehicle is.  This version
   * overrides the version in {@link Driver}, but only to memoize it for
   * speed.
   *
   * @return the distance to the next intersection given the current Lane
   *         and position of the Vehicle.
   */
  double distanceToNextIntersection();

  /**
   * Find the distance from the previous intersection in the Lane in which
   * the Vehicle is, from the position at which the Vehicle is.  This
   * subtracts the length of the Vehicle from the distance from the front
   * of the Vehicle.  It overrides the version in DriverAgent, but only to
   * memoize it.
   *
   * @return the distance from the previous intersection given the current
   *         Lane and position of the Vehicle.
   */
  double distanceFromPrevIntersection();

  /**
   * Whether or not the Vehicle controlled by this driver agent
   * is inside the intersection managed by the current IntersectionManager.
   *
   * @return whether or not the Vehicle controlled by this
   *         CoordinatingDriverAgent is inside the intersection managed by the
   *         current IntersectionManager.
   */
  boolean inCurrentIntersection();


}
