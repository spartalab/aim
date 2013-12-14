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
package aim4.im;

import java.util.List;

import aim4.map.Road;
import aim4.map.lane.Lane;

/**
 * The interface of track models
 */
public interface TrackModel {

  /**
   * Get the intersection managed by this track model
   *
   * @return  the intersection managed by this track model
   */
  RoadBasedIntersection getIntersection();


  /**
   * Get the distance from the entry of the given Road, to the departure of
   * the other given Road.
   *
   * @param arrival   the arrival Road
   * @param departure the departure Road
   * @return          the distance from the entry of the arrival Road to the
   *                  exit of the departure Road
   */
  double traversalDistance(Road arrival, Road departure);


  /**
   * Get the distance from the entry of the given Lane, to the departure of
   * the other given Lane, if traveling along segments through their point
   * of intersection.
   *
   * @param arrival   the arrival Lane
   * @param departure the departure Lane
   * @return          the distance from the entry of the arrival Lane to the
   *                  exit of the departure Lane through their intersection
   */
  double traversalDistance(Lane arrival, Lane departure);


  /**
   * Get the distance from the entry of the Lane with the first given ID, to
   * the departure of the Lane with the other given ID, if traveling along
   * segments through their point of intersection.
   *
   * @param arrivalID   the ID number of the arrival Lane
   * @param departureID the ID number of the departure Lane
   * @return            the distance from the entry of the arrival Lane to the
   *                    exit of the departure Lane through their intersection
   */
  double traversalDistance(int arrivalID, int departureID);


  /**
   * Given an arrival Lane and a departure Road, get an ordered List of Lanes
   * that represents the Lanes from highest to lowest priority based on
   * distance from the arrival Lane.
   *
   * @param arrivalLane the Lane in which the vehicle is arriving
   * @param departure   the Road by which the vehicle is departing
   * @return            the ordered List of Lanes, by priority, into which the
   *                    vehicle should try to turn
   */
  List<Lane> getSortedDepartureLanes(Lane arrivalLane, Road departure);


  // TODO: remove this function
  /**
   * Determine whether two trajectories overlap.  Errs on the side of caution.
   *
   * @param l11 the ID number of the entry lane for the first trajectory
   * @param l12 the ID number of the exit lane for the first trajectory
   * @param l21 the ID number of the entry lane for the second trajectory
   * @param l22 the ID number of the exit lane for the second trajectory
   * @return    whether the trajectories specified conflict
   */
  boolean trajectoriesConflict(int l11, int l12, int l21, int l22);
}
