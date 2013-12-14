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

import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.List;

import aim4.config.Constants.TurnDirection;
import aim4.map.Road;
import aim4.map.lane.Lane;
import aim4.map.track.WayPoint;


/**
 * The interface of an intersection
 */
public interface Intersection {

  /**
   * Get the Area controlled by this IntersectionManager.
   *
   * @return the Area controlled by this IntersectionManager
   */
  Area getArea();

  /**
   * Get the area slightly larger than the area controlled
   * by this IntersectionManager.
   *
   * @return the Area controlled by this IntersectionManager
   */
  Area getAreaPlus();

  /**
   * Get the centroid of the IntersectionManager.
   *
   * @return the centroid of the IntersectionManager
   */
  Point2D getCentroid();

  /**
   * Get the minimal rectangular region that encloses the intersection.
   *
   * @return the minimal rectangular region that encloses the intersection
   */
  Rectangle2D getBoundingBox();


  /**
   * Get the Roads incident to the space governed by this intersection.
   *
   * @return  the roads managed by this intersection.
   */
  List<Road> getRoads();

  /**
   * Get the Lanes incident to the space governed by this intersection.
   *
   * @return  the lanes managed by this intersection.
   */
  List<Lane> getLanes();


  // entry points

  /**
   * Get the Roads that enter  the space governed by this IntersectionManager.
   *
   * @return the Roads that enter the space governed by this
   *         IntersectionManager
   */
  List<Road> getEntryRoads();

  /**
   * Get the Lanes that enter the space governed by this IntersectionManager.
   *
   * @return the Lanes that enter the space governed by this
   *         IntersectionManager
   */
  List<Lane> getEntryLanes();

  /**
   * Whether the given Lane enters this intersection.
   *
   * @param l the Lane to consider
   * @return  whether the Lane enters this intersection
   */
  boolean isEnteredBy(Lane l);

  /**
   * Get the Point at which the given Lane enters the intersection.
   *
   * @param l the Lane
   * @return  the Point at which the given Lane enters the intersection, or
   *          <code>null</code> if it does not
   */
  WayPoint getEntryPoint(Lane l);

  /**
   * Get the heading at which the given Lane enters the intersection.
   *
   * @param l the Lane
   * @return  the heading at which the Lane enters the intersection
   */
  double getEntryHeading(Lane l);


  // exit points

  /**
   * Get the Roads that exit the space governed by this IntersectionManager.
   *
   * @return the Roads that exit the space governed by this
   *         IntersectionManager
   */
  List<Road> getExitRoads();

  /**
   * Get the Lanes that exit the space governed by this IntersectionManager.
   *
   * @return the Lanes that exit the space governed by this
   *         IntersectionManager
   */
  List<Lane> getExitLanes();

  /**
   * Whether the given Lane leaves this intersection.
   *
   * @param l the Lane to consider
   * @return  whether the Lane exits this intersection
   */
  boolean isExitedBy(Lane l);

  /**
   * Get the Point at which the given Lane exits the intersection.
   *
   * @param l the Lane
   * @return  the Point at which the given Lane exits the intersection, or
   *          <code>null</code> if it does not
   */
  WayPoint getExitPoint(Lane l);

  /**
   * Get the heading at which the given Lane exits the intersection.
   *
   * @param l the Lane
   * @return  the heading at which the Lane exits the intersection
   */
  double getExitHeading(Lane l);


  // comparisons

  /**
   * Get the turn direction of the vehicle at the next intersection.
   *
   * @param currentLane    the current lane.
   * @param departureLane  the departure lane.
   * @return the turn direction of the vehicle at the next intersection
   */
  TurnDirection calcTurnDirection(Lane currentLane, Lane departureLane);

}
