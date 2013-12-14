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

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import aim4.config.Debug;
import aim4.map.Road;
import aim4.map.lane.Lane;
import aim4.util.GeomMath;


/**
 * A track model for road based intersections
 */
public class RoadBasedTrackModel implements TrackModel {

  /////////////////////////////////
  // NESTED CLASSES
  /////////////////////////////////

  // It is really sad that I have to do this...

  /**
   * A comparator to sort Lanes by the distance from the point they exit the
   * intersection to the point at which a given Lane enters the intersection.
   * This is so that we can determine which exit lane represents the shortest
   * path through the intersection, given some entry lane.
   */
  private class ExitPointDistanceComparator implements Comparator<Lane> {

      /**
       * The point at which the given Lane enters this intersection.
       */
      private Point2D entry;

      /**
       * Class constructor that takes the lane at which a vehicle will
       * enter the intersection and constructs a comparator to compare
       * the distances from the exit points of various lanes to the
       * entry point of this lane into this intersection.
       *
       * @param entryLane the Lane from whose entry point into this
       *                  intersection we want to compare distances
       */
      public ExitPointDistanceComparator(Lane entryLane) {
        entry = intersection.getEntryPoint(entryLane);
      }

      /**
       * Given two lanes, compare them based on the distance from their
       * exit points in this intersection to the entry point of the Lane
       * around which this instance was constructed.
       *
       * @param l1 the first Lane
       * @param l2 the second Lane
       * @return   -1, 0, or 1, depending on whether the entry point of the
       *            Lane around which the instance was constructed to the exit
       *            point of <code>l1</code> is nearer, the same distance,
       *            or further from the the exit point of <code>l2</code>,
       *            respectively
       */
    @Override
      public int compare(Lane l1, Lane l2) {
        if(!intersection.isExitedBy(l1) || !intersection.isExitedBy(l2)) {
          throw new IllegalArgumentException("Both lanes being compared" +
                                             " must exit this intersection!");
        }
        Point2D l1Exit = intersection.getExitPoint(l1);
        Point2D l2Exit = intersection.getExitPoint(l2);
        return Double.compare(entry.distance(l1Exit), entry.distance(l2Exit));
      }
  }


  /////////////////////////////////
  // PRIVATE FIELDS
  /////////////////////////////////

  /**
   * The intersection.
   */
  private RoadBasedIntersection intersection;

  /**
   * A map from ordered pairs of (Lane, Road) (implemented as a map to a map)
   * to an ordered List of Lanes indicating the priority of other Lanes in the
   * same Road as the second Lane in the ordered pair, for turning into
   * that Lane from the first Lane in the ordered pair.  That is, if
   * <code>lanePriorities.get(l1).get(road)</code> returns the List
   * <code>{l2, l3, l4, l5}</code>, then a vehicle turning from
   * <code>l1</code> to a lane in <code>road</code> should consider turning
   * into <code>l2</code>, <code>l3</code>, <code>l4</code>, and
   * <code>l5</code> in that order, as that order specifies which lanes'
   * intersection exit points are closest to <code>l1</code>'s intersection
   * entry point.
   */
  private Map<Lane, Map<Road, List<Lane>>> lanePriorities =
    new HashMap<Lane, Map<Road, List<Lane>>>();

  /**
   * Memoization cache for {@link #traversalDistance(Road arrival, Road
   * departure)}.
   */
  private Map<List<Integer>, Double> memoTraversalDistance =
    new HashMap<List<Integer>, Double>();

  // TODO: remove this variable
  /**
   * A set of 4-tuples of lanes IDs such that if the tuple
   * <i>(l1, l2, l3, l4)</i> is in the set, the track from lane <i>l1</i> to
   * lane </i>l2</i> conflicts with the track from lane <i>l3</i> to <i>l4</i>.
   */
  private Set<List<Integer>> laneConflicts = new HashSet<List<Integer>>();


  /////////////////////////////////
  // CLASS CONSTRUCTORS
  /////////////////////////////////

  /**
   * Basic class constructor.  Takes an lane-based intersection and construct
   * a track model for the intersection.
   *
   * @param intersection  a lane-based intersection.
   */
  public RoadBasedTrackModel(RoadBasedIntersection intersection) {
    this.intersection = intersection ;
    // Determine the priorities for exit lanes
    calculateLanePriorities();
    calculateLaneConflicts(); // TODO remove this function
  }

  /////////////////////////////////
  // PRIVATE METHODS
  /////////////////////////////////

  /**
   * Given the current set of Lanes, calculate the turning lane priorities.
   * That is, for each ordered pair of Lanes, determine which other Lanes
   * in the second Lane's are viable exit Lanes (in the same Road) and order
   * them based on distance from the first Lane.
   */
  private void calculateLanePriorities() {
    for(Lane entryLane : intersection.getEntryLanes()) {
      Map<Road, List<Lane>> exitPriorities = new HashMap<Road, List<Lane>>();
      // Point2D entryPoint = intersection.getEntryPoint(entryLane);
      for(Road exitRoad : intersection.getExitRoads()) {
        // We're going to sort all the potential exit lanes by distance from
        // the entry lane
        // We have to make a copy so that we can sort it
        List<Lane> exitLanes = new ArrayList<Lane>(exitRoad.getLanes());
        // Now we should have all the lanes that exit in e's Road
        // Sort them by distance of exit point to entrance point of entryLane
        Comparator<Lane> c = new ExitPointDistanceComparator(entryLane);
        Collections.sort(exitLanes, c);
        // Now put them in the list for entryLane
        exitPriorities.put(exitRoad, exitLanes);
      }
      // Now that we've built up the exit priorities for this
      // lane, add it
      lanePriorities.put(entryLane, exitPriorities);
    }
  }


  /**
   * Given two Lanes, return a List of line segments that describe the track
   * from the entry point of the first Lane to the intersection with the
   * second Lane, to the exit point of the second Lane.
   *
   * @param l1 the lane entering the intersection
   * @param l2 the lane exiting the intersection
   * @return   the line segments comprising the track from the entry point of
   *           the first Lane to the intersection with the second Lane to the
   *           exit point of the second Lane.
   */
  private List<Line2D> inIntersectionSegments(Lane l1, Lane l2) {
    // This will store the answer
    List<Line2D> segments = new ArrayList<Line2D>();
    if(l1 == l2) {
      // Same lane, so only segment is a line from the entry point to
      // the exit point
      segments.add(new Line2D.Double(intersection.getEntryPoint(l1),
                                     intersection.getExitPoint(l2)));
    } else {
      // Otherwise, we need to consider both
      // Find out which parts of each are in the intersection
      Point2D l1End, l2Start;
      if(intersection.isExitedBy(l1)) {
        l1End = intersection.getExitPoint(l1);
      } else {
        l1End = l1.getEndPoint();
      }
      if(intersection.isEnteredBy(l2)) {
        l2Start = intersection.getEntryPoint(l2);
      } else {
        l2Start = l2.getStartPoint();
      }
      Line2D l1InIntersection =
        new Line2D.Double(intersection.getEntryPoint(l1), l1End);
      Line2D l2InIntersection =
        new Line2D.Double(l2Start, intersection.getExitPoint(l2));
      // If they intersect, we take the segment from the entry point of
      // the first to the intersection and the segment from the intersection
      // to the exit point of the second
      if(l1InIntersection.intersectsLine(l2InIntersection)) {
        Point2D ixn = GeomMath.findLineLineIntersection(l1InIntersection,
                                                    l2InIntersection);
        segments.add(new Line2D.Double(intersection.getEntryPoint(l1), ixn));
        segments.add(new Line2D.Double(ixn, intersection.getExitPoint(l2)));
      } else {
        // If they don't intersect, we just take both of them (being
        // conservative)
        //
        // TODO: From Chiu: it seems it doesn't make sense to just include
        // both segments, but ignore the mid-segment between the end point
        // of l1InIntersection and the start point of l2InIntersection.
        // Furthermore, there are other stricky cases to deal with.
        segments.add(l1InIntersection);
        segments.add(l2InIntersection);
      }
    }
    // Now we've got our segments, so return them!
    return segments;
  }


  //TODO remove this function
  /**
   * For all pairs of trajectories (4-tuples of Lanes), find the ones
   * that conflict and add them to <code>laneConflicts</code>. Assumes
   * that Lanes are linear within the intersection.
   */
  private void calculateLaneConflicts() {
    // N^4: blech!
    for(Lane l11: intersection.getEntryLanes()) {
      for(Lane l12: intersection.getExitLanes()) {
        // Find the line segments that represent the track
        List<Line2D> l1Segments = inIntersectionSegments(l11, l12);
        for(Lane l21: intersection.getEntryLanes()) {
          for(Lane l22: intersection.getExitLanes()) {
            // If the start or end lanes are the same, that's an automatic
            // conflict
            if(l11 == l21 || l12 == l22) {
              // Construct the key from the IDs of the various lanes
              List<Integer> key = Arrays.asList(l11.getId(), l12.getId(),
                                                l21.getId(), l22.getId());
              laneConflicts.add(key);
            } else {
              // Find the line segments that represent the track
              List<Line2D> l2Segments = inIntersectionSegments(l21, l22);
              // Now, if any of the l1 segments intersect any of the l2
              // segments, there is a conflict.
              findConflict: for(Line2D l1Seg: l1Segments) {
                for(Line2D l2Seg: l2Segments) {
                  if(l1Seg.intersectsLine(l2Seg)) {
                    // Construct the key from the IDs of the various lanes
                    List<Integer> key = Arrays.asList(l11.getId(),
                                                      l12.getId(),
                                                      l21.getId(),
                                                      l22.getId());
                    laneConflicts.add(key);
                    break findConflict;  // No need to keep calculating
                  }
                }
              }
            }

          }
        }
      }
    }
  }

  /////////////////////////////////
  // PUBLIC METHODS
  /////////////////////////////////


  /**
   * Get the intersection managed by this track model
   *
   * @return  the intersection managed by this track model
   */
  @Override
  public RoadBasedIntersection getIntersection() {
    return intersection;
  }

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
  @Override
  public List<Lane> getSortedDepartureLanes(Lane arrivalLane, Road departure) {
    return lanePriorities.get(arrivalLane).get(departure);
  }


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
  @Override
  public boolean trajectoriesConflict(int l11, int l12, int l21, int l22) {
    List<Integer> lookupKey = Arrays.asList(l11, l12, l21, l22);
    return laneConflicts.contains(lookupKey);
  }


  /**
   * Get the distance from the entry of the given Road, to the departure of
   * the other given Road.
   *
   * @param arrival   the arrival Road
   * @param departure the departure Road
   * @return          the distance from the entry of the arrival Road to the
   *                  exit of the departure Road
   */
  @Override
  public double traversalDistance(Road arrival, Road departure) {
    return traversalDistance(arrival.getIndexLane(),
                             departure.getIndexLane());
  }

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
  @Override
  public double traversalDistance(Lane arrival, Lane departure) {
    List<Integer> key = Arrays.asList(arrival.getId(),
                                      departure.getId());
    if(!memoTraversalDistance.containsKey(key)) {
      double totalDistance = 0;
      List<Line2D> segments =
        inIntersectionSegments(arrival, departure);
      // Add up the length of the segments
      for(Line2D line : segments) {
        totalDistance += line.getP1().distance(line.getP2());
      }
      memoTraversalDistance.put(key, totalDistance);
    }
    return memoTraversalDistance.get(key);
  }

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
  @Override
  public double traversalDistance(int arrivalID, int departureID) {
    List<Integer> key = Arrays.asList(arrivalID, departureID);
    Lane arrival = Debug.currentMap.getLaneRegistry().get(arrivalID);
    Lane departure = Debug.currentMap.getLaneRegistry().get(departureID);
    return traversalDistance(arrival, departure);
  }


}
