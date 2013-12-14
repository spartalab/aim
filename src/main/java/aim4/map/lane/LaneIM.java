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
package aim4.map.lane;

import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import aim4.im.IntersectionManager;

/**
 * The lane and intersection manager relationship.
 */
public class LaneIM {

  /////////////////////////////////
  // PRIVATE FIELDS
  /////////////////////////////////

  /**
   * The lane
   */
  private Lane lane;

  /**
   * A map from normalized distances of exit points to intersection managers.
   */
  private SortedMap<Double, IntersectionManager> intersectionManagers =
    new TreeMap<Double, IntersectionManager>();

  /**
   * Memoization cache for {@link
   * #nextIntersectionManager(IntersectionManager im)}.
   */
  private Map<IntersectionManager, IntersectionManager>
    memoGetSubsequentIntersectionManager = null;

  /////////////////////////////////
  // CONSTRUCTORS
  /////////////////////////////////

  /**
   * Create a lane and intersection manager relationship object.
   *
   * @param lane  the lane.
   */
  public LaneIM(Lane lane) {
    this.lane = lane;
  }


  /////////////////////////////////
  // PUBLIC METHODS
  /////////////////////////////////

  // intersection manager

  /**
   * Register an {@link IntersectionManager} with this Lane.  If the Lane does
   * not intersect the area controlled by the IntersectionManager, it has no
   * effect. Otherwise, the IntersectionManager is stored and returned under
   * certain circumstances by the <code>nextIntersectionManager</code> method.
   *
   * @param im the IntersectionManager to register
   */
  public void registerIntersectionManager(IntersectionManager im) {
    // Only do this if this lane is managed by this intersection
    if(im.manages(lane)) {
      // Reset this cache
      memoGetSubsequentIntersectionManager = null;
      // Find out where this lane exits the intersection
      Point2D exitPoint = im.getIntersection().getExitPoint(lane);
      // If it's null, that means it doesn't exit.
      if(exitPoint == null) {
        exitPoint = lane.getEndPoint();
      }
      double normalizedDistanceToExit =
        lane.normalizedDistanceAlongLane(exitPoint);
      // Add the normalized distance to the exit point to the map
      // that gives us the "next intersection" for any point in the lane.
      intersectionManagers.put(normalizedDistanceToExit, im);
    }
  }

  /**
   * Get the first IntersectionManager that this Lane, or any Lane it leads
   * into enters. Recursively searches through all subsequent Lanes.
   *
   * @return the first IntersectionManager this Lane, or any Lane it leads
   *         into enters
   */
  public IntersectionManager firstIntersectionManager() {
    if(intersectionManagers.isEmpty()) {
      if(lane.hasNextLane()) {
        return lane.getNextLane().getLaneIM().firstIntersectionManager();
      }
      return null;
    }
    return intersectionManagers.get(intersectionManagers.firstKey());
  }

  /**
   * Get the distance from the start of this Lane to the first
   * IntersectionManager that this Lane, or any Lane it leads into intersects.
   * Recursively searches through all subsequent Lanes. Returns
   * <code>Double.MAX_VALUE</code> if no such IntersectionManager exists.
   *
   * @return the distance from the start of this Lane to the first
   *         IntersectionManager this Lane, or any lane it leads into
   *         intersects, or <code>Double.MAX_VALUE</code> if no such
   *         IntersectionManager exists
   */
  public double distanceToFirstIntersection() {
    if(intersectionManagers.isEmpty()) {
      if(lane.hasNextLane()) {
        return lane.getLength() +
               lane.getNextLane().getLaneIM().distanceToFirstIntersection();
      }
      return Double.MAX_VALUE;
    }
    // Otherwise, it's the distance from the start of the Lane to the entry
    // point of the first IntersectionManager
    IntersectionManager firstIM =
      intersectionManagers.get(intersectionManagers.firstKey());
    Point2D entry = firstIM.getIntersection().getEntryPoint(lane);
    if(entry == null) {
      return 0; // The Lane starts out in the intersection.
    }
    // Otherwise just return the distance from the start of this Lane to
    // the place it enters the first intersection
    return lane.getStartPoint().distance(entry);
  }

  /**
   * Find the next Lane, including this one, that enters an intersection at
   * any point.
   *
   * @return  the next Lane, following the chain of Lanes in which this Lane
   *          is, that enters an intersection, at any point
   */
  public Lane laneToFirstIntersection() {
    // If there aren't any more in this lane
    if(intersectionManagers.isEmpty()) {
      // Check the next Lane
      if(lane.hasNextLane()) {
        // Pass the buck to the next Lane after this one
        return lane.getNextLane().getLaneIM().laneToFirstIntersection();
      }
      // Otherwise, there are none.
      return null;
    }
    // Otherwise, it is this one.
    return lane;
  }

  /**
   * Get the last IntersectionManager that this Lane, or any Lane that leads
   * into it enters.  Recursively searches through all previous Lanes.
   *
   * @return the last IntersectionManager this Lane, or any Lane that leads
   *         into it enters.
   */
  public IntersectionManager lastIntersectionManager() {
    if(intersectionManagers.isEmpty()) {
      if(lane.hasPrevLane()) {
        return lane.getPrevLane().getLaneIM().lastIntersectionManager();
      }
      return null;
    }
    return intersectionManagers.get(intersectionManagers.lastKey());
  }

  /**
   * Get the distance from the end of this Lane to the last
   * IntersectionManager that this Lane, or any Lane that leads into it
   * entered.  Recursively searches through all previous Lanes.  Returns
   * <code>Double.MAX_VALUE</code> if no such IntersectionManager exists.
   *
   * @return the distance from the end of this Lane to the last
   *         IntersectionManager this Lane, or any lane that leads into it
   *         entered, or <code>Double.MAX_VALUE</code> if no such
   *         IntersectionManager exists
   */
  public double remainingDistanceFromLastIntersection() {
    if(intersectionManagers.isEmpty()) {
      if(lane.hasPrevLane()) {
        return lane.getLength() +
               lane.getPrevLane().getLaneIM().
               remainingDistanceFromLastIntersection();
      } else {
        return Double.MAX_VALUE;
      }
    } else {
      return (1 - intersectionManagers.lastKey()) * lane.getLength();
    }
  }


  // given a point -> next im

  /**
   * Find the next IntersectionManager a vehicle at the given position will
   * encounter. These are indexed based on how far along the lane the vehicle
   * is, from 0 (at the start) to 1 (at the end).
   *
   * @param p the location of the hypothetical vehicle
   * @return  the next IntersectionManager the vehicle will encounter, or
   *          <code>null</code> if none
   */
  public IntersectionManager nextIntersectionManager(Point2D p) {
    // First find how far along the point is.
    double index = lane.normalizedDistanceAlongLane(p);
    SortedMap<Double, IntersectionManager> remaining =
      intersectionManagers.tailMap(index);
    // If nothing left, then no more IntersectionManagers
    if (remaining.isEmpty()) {
      if (lane.hasNextLane()) {
        return lane.getNextLane().getLaneIM().firstIntersectionManager();
      } else {
        return null;
      }
    } else {
      return remaining.get(remaining.firstKey());
    }
  }

  /**
   * Find the distance to the next IntersectionManager a vehicle at the given
   * position will encounter.  First projects the point onto the Lane.
   *
   * @param p the current location of the vehicle
   * @return  the distance along the Lane from the point on the Lane nearest
   *          to the given point to the next IntersectionManager a vehicle
   *          at the given point will encounter; if there is no next
   *          intersection, return Double.MAX_VALUE
   */
  public double distanceToNextIntersection(Point2D p) {
    // First determine how far along the Lane we are
    double index = lane.normalizedDistanceAlongLane(p);
    // Now find all IntersectionManagers that are after this point (remember
    // they are indexed by exit point)
    SortedMap<Double, IntersectionManager> remaining =
      intersectionManagers.tailMap(index);
    // If there aren't any more in this lane
    if (remaining.isEmpty()) {
      // Check the next Lane
      if (lane.hasNextLane()) {
        return ((1 - index) * lane.getLength()) +
               lane.getNextLane().getLaneIM().distanceToFirstIntersection();
      } else {
        // Otherwise, just say it is really really far away
        return Double.MAX_VALUE;
      }
    } else {
      // Otherwise, we need to figure out where we are and where the current
      // Lane intersects the next intersection.
      IntersectionManager nextIM = remaining.get(remaining.firstKey());
      Point2D entry = nextIM.getIntersection().getEntryPoint(lane);
        // Where does this Lane enter?
      if (entry == null) { // It doesn't! It just exits! That means we're in it!
        return 0.0;
      } else {
        // Otherwise, there is an entry point.  Find out how far along it is in
        // the Lane
        double entryFraction = lane.normalizedDistanceAlongLane(entry);
        // Now, we want to return 0 if we are past the entry point, or the
        // distance to the entry point otherwise
        return Math.max(0.0, (entryFraction - index) * lane.getLength());
      }
    }
  }

  /**
   * Find the next Lane, including this one, that will enter an intersection,
   * starting at the point in this Lane nearest the provided point.
   *
   * @param p the current location of the vehicle
   * @return  the next Lane, following the chain of Lanes in which this Lane
   *          is, that will enter an intersection, starting at the point in
   *          this Lane nearest the provided point
   */
  public Lane laneToNextIntersection(Point2D p) {
    // First determine how far along the Lane we are
    double index = lane.normalizedDistanceAlongLane(p);
    // Now find all IntersectionManagers that are after this point (remember
    // they are indexed by exit point)
    SortedMap<Double, IntersectionManager> remaining =
      intersectionManagers.tailMap(index);
    // If there aren't any more in this lane
    if(remaining.isEmpty()) {
      // Check the next Lane
      if(lane.hasNextLane()) {
        // Pass the buck to the next Lane after this one
        return lane.getNextLane().getLaneIM().laneToFirstIntersection();
      }
      // Otherwise, there are none.
      return null;
    }
    // Otherwise, it is this one.
    return lane;
  }


  // given a point -> prev im

  /**
   * Find the distance from a point, projected onto the Lane, to the previous
   * intersection that a vehicle at that position on the Lane would have
   * encountered.
   *
   * @param p the current location of the vehicle
   * @return  the distance from a point, projected onto the Lane, to the
   *          previous intersection that a vehicle at that position on the
   *          Lane would have encountered
   */
  public double distanceFromPrevIntersection(Point2D p) {
    // First determine how far along the Lane we are
    double index = lane.normalizedDistanceAlongLane(p);
    // Now find all IntersectionManagers that are before this point (remember
    // they are indexed by exit point)
    SortedMap<Double, IntersectionManager> preceding =
      intersectionManagers.headMap(index);
    // If there aren't any in this lane
    if(preceding.isEmpty()) {
      // Check the previous Lane
      if(lane.hasPrevLane()) {
        return (index * lane.getLength()) +
               lane.getNextLane().getLaneIM().
               remainingDistanceFromLastIntersection();
      }
      // Otherwise, just say it is really really far away
      return Double.MAX_VALUE;
    }
    // preceding.lastKey() is the relative distance to the exit point of the
    // last Intersection in the Lane before our position, so we subtract that
    // from our current relative position (index) to get the total relative
    // distance. Then, multiply that by length to get an absolute distance.
    // This can't be negative because the last key must be before index
    // since we did a headMap.
    return (index - preceding.lastKey()) * lane.getLength();
  }

  // given an im

  /**
   * Get the IntersectionManager that this Lane, or any Lane it leads into
   * enters, after the given IntersectionManager.
   *
   * @param im the IntersectionManager to which we would like the successor
   * @return   the IntersectionManager that this Lane, or any Lane it leads
   *           into enters, after the given IntersectionManager
   */
  public IntersectionManager nextIntersectionManager(IntersectionManager im) {
    // Build the cache if it doesn't exist
    if(memoGetSubsequentIntersectionManager == null) {
      memoGetSubsequentIntersectionManager =
        new HashMap<IntersectionManager, IntersectionManager>();
      IntersectionManager lastIM = null;
      // Now run through the IntersectionManagers in order and set up
      // the cache
      for(IntersectionManager currIM : intersectionManagers.values()) {
        // Don't include the first one as a value, since it isn't subsequent
        // to anything
        if(lastIM != null) {
          memoGetSubsequentIntersectionManager.put(lastIM, currIM);
        }
        lastIM = currIM;
      }
      // Link up to the next Lane
      if(lastIM != null && lane.hasNextLane()) {
        memoGetSubsequentIntersectionManager.put(lastIM,
                                                 lane.getNextLane().
                                                 getLaneIM().
                                                 firstIntersectionManager());
      }
    }
    return memoGetSubsequentIntersectionManager.get(im);
  }

  /**
   * Get the distance from the given IntersectionManager to the next
   * one that that this Lane, or any Lane it leads into enters.
   *
   * @param im          the IntersectionManager at which to start
   * @return            the distance, in meters, departing the given
   *                    IntersectionManager, to reach the next
   *                    IntersectionManager
   */
  public double distanceToNextIntersectionManager(IntersectionManager im) {
    // Two cases: either the next intersection is in this Lane, or it is
    // in a Lane connected to this one
    IntersectionManager nextIM = nextIntersectionManager(im);
    if(nextIM == null) {
      // If there's no next intersection, we just return 0 since the
      // behavior isn't well defined
      return 0;
    }
    if(nextIM.getIntersection().isEnteredBy(lane)) {
      // This is the easy case: just find the distance to the next
      // intersection and divide by the speed limit
      return im.getIntersection().getExitPoint(lane).distance(
          nextIM.getIntersection().getEntryPoint(lane));
    } else {
      // This is more challenging.  We need to keep adding it up the Lanes
      // in between until we find it
      // Start with the distance to the end of this Lane
      double totalDist = remainingDistanceFromLastIntersection();
      Lane currLane = lane.getNextLane();
      // Okay, add up all the lanes until the IM
      while(!nextIM.getIntersection().isEnteredBy(currLane)) {
        totalDist += currLane.getLength();
        currLane = currLane.getNextLane();
      }
      // Now we're at the Lane that actually enters the next IM
      totalDist += currLane.getLaneIM().distanceToFirstIntersection();
      return totalDist;
    }
  }

  /**
   * Get the approximate time from the given IntersectionManager to the next
   * one that that this Lane, or any Lane it leads into enters, based on
   * distances and speed limits.
   *
   * @param im          the IntersectionManager at which to start
   * @param maxVelocity the maximum velocity of the vehicle
   * @return            the time, in seconds, that it should take once
   *                    departing the given IntersectionManager, to reach the
   *                    next IntersectionManager
   */
  public double timeToNextIntersectionManager(IntersectionManager im,
                                              double maxVelocity) {
    // Two cases: either the next intersection is in this Lane, or it is
    // in a Lane connected to this one
    IntersectionManager nextIM = nextIntersectionManager(im);
    if(nextIM == null) {
      // If there's no next intersection, we just return 0 since the
      // behavior isn't well defined
      return 0;
    }
    if(nextIM.getIntersection().isEnteredBy(lane)) {
      // This is the easy case: just find the distance to the next
      // intersection and divide by the speed limit
      return im.getIntersection().getExitPoint(lane).distance(
          nextIM.getIntersection().getEntryPoint(lane)) /
          Math.min(lane.getSpeedLimit(), maxVelocity);
    } else {
      // This is more challenging.  We need to keep adding it up the Lanes
      // in between until we find it
      // Start with the distance to the end of this Lane
      double totalTime = remainingDistanceFromLastIntersection() /
                         lane.getSpeedLimit();
      Lane currLane = lane.getNextLane();
      // Okay, add up all the lanes until the IM
      while(!nextIM.getIntersection().isEnteredBy(currLane)) {
        totalTime += currLane.getLength() /
                     Math.min(currLane.getSpeedLimit(), maxVelocity);
        currLane = currLane.getNextLane();
      }
      // Now we're at the Lane that actually enters the next IM
      totalTime += currLane.getLaneIM().distanceToFirstIntersection() /
                   Math.min(currLane.getSpeedLimit(), maxVelocity);
      return totalTime;
    }
  }

}
