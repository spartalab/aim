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
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import aim4.config.Debug;
import aim4.config.Constants.TurnDirection;
import aim4.map.Road;
import aim4.map.lane.Lane;
import aim4.map.track.WayPoint;
import aim4.util.GeomMath;
import aim4.util.Util;


/**
 * An intersection that is defined by the intersection of a set of roads.
 */
public class RoadBasedIntersection implements Intersection {

  /////////////////////////////////
  // CONSTANTS
  /////////////////////////////////

  /**
   * The distance outside of the strict intersection that the
   * IntersectionManager will control, in meters. {@value} meters.
   * This gives room for things like crosswalks.
   */
  public static final double EXPANSION_DISTANCE = 4; // meters

  /**
   * A small increase in size of the area such that the simulator can
   * determine whether a vehicle is close enough to the intersection.
   */
  private static final double AREA_PLUS_OFFSET = 0.000001;


  /////////////////////////////////
  // PRIVATE FIELDS
  /////////////////////////////////

  /**
   * The space governed by this intersection manager.
   */
  private Area area;

  /**
   * An area slightly larger than the area of the intersection.
   */
  private Area areaPlus;

  /**
   * The smallest rectangle that contains this intersection.
   */
  private Rectangle2D boundingBox;

  /**
   * The centroid of this intersection.
   */
  private Point2D centroid;


  // edge

  /**
   * A list of edges of the area.
   */
  private List<Path2D> edges = new ArrayList<Path2D>();


  // road

  /** The roads incident to this intersection. */
  private List<Road> roads = new ArrayList<Road>();

  /** The entry roads incidents to this intersection. */
  private List<Road> entryRoads = new ArrayList<Road>();

  /** The exit roads incidents to this intersection. */
  private List<Road> exitRoads = new ArrayList<Road>();


  // lanes

  /** The lanes incident to this intersection. */
  private List<Lane> lanes = new ArrayList<Lane>();

  // points

  /**
   * A list of the waypoints where lanes either enter or exit the intersection,
   * ordered by angle from the centroid.
   */
  private List<Point2D> points = new ArrayList<Point2D>();

  // heading

  private Map<Lane,Double> headings = new HashMap<Lane,Double>();

  // cache

  /**
   * A map from lanes to the waypoints at which those lanes enter the
   * intersection.
   */
  private Map<Lane,WayPoint> entryPoints = new LinkedHashMap<Lane,WayPoint>();

  /**
   * A map from lanes to the waypoints at which those lanes exit the
   * intersection.
   */
  private Map<Lane,WayPoint> exitPoints = new LinkedHashMap<Lane,WayPoint>();

  /**
   * A map from Lanes to the headings, in radians, of those Lanes at the
   * point at which they enter the space governed by this IntersectionManager.
   */
  private Map<Lane,Double> entryHeadings = new HashMap<Lane,Double>();

  /**
   * A map from Lanes to the headings, in radians, of those Lanes at the
   * point at which they exit the space governed by this IntersectionManager.
   */
  private Map<Lane,Double> exitHeadings = new HashMap<Lane,Double>();


  /////////////////////////////////
  // CLASS CONSTRUCTORS
  /////////////////////////////////

  /**
   * Basic class constructor.  Takes the Roads for which
   * an IntersectionManager is needed and extracts all the necessary
   * information.
   *
   * @param roads a list of Roads whose intersection this IntersectionManager
   *              will manage
   */
  public RoadBasedIntersection(List<Road> roads) {
    this.roads = roads;
    // Get the list of Lanes we are using.
    extractLanes(roads);
    // Now get the entry and exit points for each of the lanes.
    establishEntryAndExitPoints(findStrictIntersectionArea(roads));
    // Find the centroid of the intersection
    centroid = GeomMath.polygonalShapeCentroid(area);
    // Calculate the waypoints.
    calcWayPoints();
    // calculate the edges
    calcEdges();
    // Now build a GeneralPath using the waypoints.
    addWayPointsPath();
    // Calculate the bounding box
    boundingBox = area.getBounds2D();

    calcEntryRoads();
    calcExitRoads();
  }

  /////////////////////////////////
  // PRIVATE METHODS
  /////////////////////////////////

  /**
   * Given a List of Roads, pull out all the individual lanes.
   *
   * @param roads a list of Roads
   */
  private void extractLanes(List<Road> roads) {
    for(Road road : roads) {
      for(Lane lane : road.getLanes()) {
        lanes.add(lane);
      }
    }
  }

  /**
   * Find the Area that represents the strict intersection of the given Roads,
   * specifically the union of all areas in which there is more than one lane.
   *
   * @param roads a list of Roads that enter the intersection
   * @return the area in which any two of these Roads intersect
   */
  private Area findStrictIntersectionArea(List<Road> roads) {
    // Lanes in the same road should never intersect. So use the union of
    // their shapes. Then find the pairwise intersections of all the roads,
    // and union all of that.
    // Create a place to store the Areas for each road
    List<Area> roadAreas = new ArrayList<Area>(roads.size());
    for(Road road : roads) {
      Area roadArea = new Area();
      // Find the union of the shapes of the lanes for each road
      for(Lane lane : road.getLanes()) {
        // Add the area from each constituent lane
        roadArea.add(new Area(lane.getShape()));
      }
      roadAreas.add(roadArea);
    }
    // Now we have the Areas for each road, we need to find the union of the
    // pairwise intersections
    Area strictIntersection = new Area();
    for(int i = 0; i < roadAreas.size(); i++) {
      // Want to make sure we only do the cases where j < i, i.e. don't do
      // both (i,j) and (j,i).
      for(int j = 0; j < i; j++) {
        // If the ith road and jth road are the duals of each other, there
        // won't be an intersection
        if(roads.get(i).getDual() != roads.get(j)) {
          // Now add the intersection of roads i and j
          // Make a copy because intersect is destructive
          Area ixn = new Area(roadAreas.get(i));
          ixn.intersect(roadAreas.get(j));
          strictIntersection.add(ixn);
        }
      }
    }
    return strictIntersection;
  }


  /**
   * Determine the points at which each Lane enters or exits the intersection
   * and record them, along with the headings of the Lanes at those points.
   * Also, extend the space governed by the IntersectionManager to include
   * the each Lane out to these points.
   */
  private void establishEntryAndExitPoints(Area strictIntersection) {
    Map<Lane,Double> entryFractions = new HashMap<Lane,Double>();
    Map<Lane,Double> exitFractions = new HashMap<Lane,Double>();
    List<Lane> intersectingLanes = new LinkedList<Lane>();
    List<Line2D> perimeterSegments =
      GeomMath.polygonalShapePerimeterSegments(strictIntersection);
    // Now for each segment in the perimeter...
    for(Line2D perimeterSegment : perimeterSegments) {
      // Check each lane's intersections
      for(Lane lane: lanes) {
        List<Double> intxns = new ArrayList<Double>(3);
        // Check the right, the left, and the center to see which is
        // least/greatest
        Point2D leftIntxn = lane.leftIntersectionPoint(perimeterSegment);
        if(leftIntxn != null) {
          intxns.add(lane.normalizedDistanceAlongLane(leftIntxn));
        }
        Point2D centerIntxn = lane.intersectionPoint(perimeterSegment);
        if(centerIntxn != null) {
          intxns.add(lane.normalizedDistanceAlongLane(centerIntxn));
        }
        Point2D rightIntxn = lane.rightIntersectionPoint(perimeterSegment);
        if(rightIntxn != null) {
          intxns.add(lane.normalizedDistanceAlongLane(rightIntxn));
        }
        // Now check against what we have, if anything hit
        if (!intxns.isEmpty()) {
          // This is an intersecting lane!
          intersectingLanes.add(lane);
          // First check entry point
          // Find the one of these that is closest to the start of the lane
          double min = Collections.min(intxns);
          if (!entryFractions.containsKey(lane) ||
             entryFractions.get(lane) > min) {
            // This is the new smallest entry point
            entryFractions.put(lane, min);
          }
          // Next check exit point
          // Find the one that is furthest from the start of the lane
          double max = Collections.max(intxns);
          if (!exitFractions.containsKey(lane) ||
             exitFractions.get(lane) < max) {
            // This is the new largest exit point
            exitFractions.put(lane, max);
          }
        }
      }
    }

    // We now know which lanes actually intersect the intersection, so we can
    // get rid of the ones that don't
    // Now just replace the list of lanes with the intersecting ones
    lanes = intersectingLanes;  // BAM!

    // Okay, now we have the "true" entry and exit points for each lane, so
    // let's set up everything we know: the true managed area, the actual
    // exit and entry points
    this.area = new Area(); // This is the "official" area
    this.areaPlus = new Area();  // This is a slightly enlarged version
                                 // of the area
    // For each lane managed by this intersection manager...
    for(Lane lane : lanes) {
      // Lane might start or end inside the intersection
      double entryFrac, exitFrac;
      // The amount to offset each entry/exit point by to make room for
      // things like crosswalks
      double expansionOffset = EXPANSION_DISTANCE / lane.getLength();
      // If a lane has a starting point inside the intersection, it has no
      // entry point
      if (strictIntersection.contains(lane.getStartPoint())) {
        entryFrac = 0;
      } else {
        entryFrac = Math.max(0, entryFractions.get(lane) - expansionOffset);
        this.entryPoints.put(lane,
          new WayPoint(lane.getPointAtNormalizedDistance(entryFrac)));
        this.entryHeadings.put(lane,
          lane.getHeadingAtNormalizedDistance(entryFrac));
      }
      // If a lane has an ending point inside the intersection, it has no
      // exit point.
      if(strictIntersection.contains(lane.getEndPoint())) {
        exitFrac = 1;
      } else {
        exitFrac = Math.min(1, exitFractions.get(lane) + expansionOffset);
        this.exitPoints.put(lane,
          new WayPoint(lane.getPointAtNormalizedDistance(exitFrac)));
        this.exitHeadings.put(lane,
          lane.getHeadingAtNormalizedDistance(exitFrac));
      }
      // Add the area between its true entry point and its exit point
      this.area.add(new Area(lane.getShape(entryFrac, exitFrac)));
      this.areaPlus.add(new Area(lane.getShape(entryFrac - AREA_PLUS_OFFSET,
                                               exitFrac + AREA_PLUS_OFFSET)));
    }
    // Fill in any of the holes
    area = GeomMath.filledArea(area);
    areaPlus = GeomMath.filledArea(areaPlus);
  }

  /**
   * Calculate the list of points, ordered by angle to the centroid, where
   * Lanes either enter or exit the intersection.
   */
  private void calcWayPoints() {
    SortedMap<Double, Point2D> circumferentialPointsByAngle =
      new TreeMap<Double, Point2D>();
    for(Point2D p : exitPoints.values()) {
      circumferentialPointsByAngle.put(GeomMath.angleToPoint(p,centroid),p);
    }
    for(Point2D p : entryPoints.values()) {
      circumferentialPointsByAngle.put(GeomMath.angleToPoint(p,centroid),p);
    }
    for(Point2D p : circumferentialPointsByAngle.values()) {
      points.add(p);
    }
  }

  /**
   * Calculate the list of edges.
   */
  private void calcEdges() {
    // TODO: need to fix this problem.
    PathIterator iter = area.getBounds2D().getPathIterator(null);
    double[] coords = new double[6];

    double px = 0, py = 0;
    Path2D edge = null;

    while(!iter.isDone()) {
       int type = iter.currentSegment(coords);
      switch(type) {
      case PathIterator.SEG_MOVETO:
        assert edge == null;
        px = coords[0];
        py = coords[1];
        break;
      case PathIterator.SEG_LINETO:
        edge = new Path2D.Double();
        edge.moveTo(px, py);
        edge.lineTo(coords[0], coords[1]);
        px = coords[0];
        py = coords[1];
        edges.add(edge);
        break;
      case PathIterator.SEG_CLOSE:
        break;
      default:
        throw new RuntimeException("RoadBasedIntersection::calcEdges(): " +
                                   "unknown path iterator type.");
      }
      iter.next();
    }
  }

  /**
   * Take the Area formed by joining the circumferential points and add it
   * to the area of the intersection.
   */
  private void addWayPointsPath() {
    GeneralPath gp = null;
    for(Point2D p : points) {
      if(gp == null) {
        gp = new GeneralPath();
        gp.moveTo((float)p.getX(),(float)p.getY());
      } else {
        gp.lineTo((float)p.getX(),(float)p.getY());
      }
    }
    gp.closePath();
    area.add(new Area(gp));
  }

  /////////////////////////////////
  // PUBLIC METHODS
  /////////////////////////////////

  /**
   * Get the Roads incident to the space governed by this intersection.
   *
   * @return the roads managed by this intersection.
   */
  @Override
  public List<Road> getRoads() {
    return roads;
  }

  /**
   * Get the Lanes incident to the space governed by this intersection.
   *
   * @return the lanes managed by this intersection.
   */
  @Override
  public List<Lane> getLanes() {
    return lanes;
  }

  /**
   * Get the Area controlled by this intersection manager.
   *
   * @return the Area controlled by this intersection manager
   */
  @Override
  public Area getArea() {
    return area;
  }

  /**
   * Get the area slightly larger than the area controlled
   * by this intersection manager.
   *
   * @return the Area controlled by this intersection manager
   */
  @Override
  public Area getAreaPlus() {
    return areaPlus;
  }

  /**
   * Get the centroid of the intersection manager.
   *
   * @return the centroid of the intersection manager
   */
  @Override
  public Point2D getCentroid() {
    return centroid;
  }

  /**
   * Get the minimal rectangular region that encloses the intersection.
   *
   * @return the minimal rectangular region that encloses the intersection
   */
  @Override
  public Rectangle2D getBoundingBox() {
    return boundingBox;
  }

  /**
   * Get the list of edges.
   *
   * @return the list of edges
   */
  public List<Path2D> getEdges() {
    return edges;
  }

  /**
   * Get the Roads that enter the space governed by this intersection manager.
   *
   * @return the Roads that enter the space governed by this
   *         IntersectionManager
   */
  @Override
  public List<Road> getEntryRoads() {
    return entryRoads;
  }

  /**
   * Create the entry roads.
   */
  private void calcEntryRoads() {
    for(Lane lane : getEntryLanes()) {
      if (!entryRoads.contains(Debug.currentMap.getRoad(lane))) {
        entryRoads.add(Debug.currentMap.getRoad(lane));
      }
    }
  }

  /**
   * Get the Lanes that enter the space governed by this intersection manager.
   *
   * @return the Lanes that enter the space governed by this
   *         IntersectionManager
   */
  @Override
  public List<Lane> getEntryLanes() {
    return new ArrayList<Lane>(entryPoints.keySet());
  }

  /**
   * Whether the given Lane enters this intersection.
   *
   * @param l the Lane to consider
   * @return  whether the Lane enters this intersection
   */
  @Override
  public boolean isEnteredBy(Lane l) {
    return entryPoints.containsKey(l);
  }

  /**
   * Get the Point at which the given Lane enters the intersection.
   *
   * @param l the Lane
   * @return  the Point at which the given Lane enters the intersection, or
   *          <code>null</code> if it does not
   */
  @Override
  public WayPoint getEntryPoint(Lane l) {
    return entryPoints.get(l);
  }

  /**
   * Get the heading at which the given lane enters the intersection.
   *
   * @param l the Lane
   * @return  the heading at which the Lane enters the intersection
   */
  @Override
  public double getEntryHeading(Lane l) {
    // TODO: what if l is not a lane entering this intersection?
    return entryHeadings.get(l);
  }

  /**
   * Get the Roads that exit the space governed by this intersection manager.
   *
   * @return the Roads that exit the space governed by this
   *         IntersectionManager
   */
  @Override
  public List<Road> getExitRoads() {
    return exitRoads;
  }

  private void calcExitRoads() {
    for(Lane lane : getExitLanes()) {
      if (!exitRoads.contains(Debug.currentMap.getRoad(lane))) {
        exitRoads.add(Debug.currentMap.getRoad(lane));
      }
    }
  }

  /**
   * Get the Lanes that exit the space governed by this intersection manager.
   *
   * @return the Lanes that exit the space governed by this
   *         IntersectionManager
   */
  @Override
  public List<Lane> getExitLanes() {
    return new ArrayList<Lane>(exitPoints.keySet());
  }

  /**
   * Whether the given Lane leaves this intersection.
   *
   * @param l the Lane to consider
   * @return  whether the Lane exits this intersection
   */
  @Override
  public boolean isExitedBy(Lane l) {
    return exitPoints.containsKey(l);
  }

  /**
   * Get the Point at which the given Lane exits the intersection.
   *
   * @param l the Lane
   * @return  the Point at which the given Lane exits the intersection, or
   *          <code>null</code> if it does not
   */
  @Override
  public WayPoint getExitPoint(Lane l) {
    return exitPoints.get(l);
  }

  /**
   * Get the heading at which the given Lane exits the intersection.
   *
   * @param l the Lane
   * @return  the heading at which the Lane exits the intersection
   */
  @Override
  public double getExitHeading(Lane l) {
    return exitHeadings.get(l);
  }

  /**
   * Get the turn direction of the vehicle at the next intersection.
   *
   * @param currentLane    the current lane.
   * @param departureLane  the departure lane.
   * @return the turn direction of the vehicle at the next intersection
   */
  @Override
  public TurnDirection calcTurnDirection(Lane currentLane, Lane departureLane) {
    Road currentRoad = Debug.currentMap.getRoad(currentLane);
    Road departureRoad = Debug.currentMap.getRoad(departureLane);
    if(departureRoad == currentRoad) {
      return TurnDirection.STRAIGHT;
    } else if(departureRoad == currentRoad.getDual()) {
      return TurnDirection.U_TURN;
    } else {
      double entryHeading = getEntryHeading(currentLane);
      double exitHeading = getExitHeading(departureLane);
      double theta = GeomMath.canonicalAngle(exitHeading-entryHeading);
      if(Util.isDoubleZero(theta)) {
        return TurnDirection.STRAIGHT; // despite they are different roads
      } else if(theta < Math.PI) {
        return TurnDirection.LEFT;
      } else if(theta > Math.PI) {
        return TurnDirection.RIGHT;
      } else {  // theta = Math.PI
        return TurnDirection.U_TURN;  // pretty unlikely.
      }
    }
  }

}
