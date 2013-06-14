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
package aim4.driver.navigator;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import aim4.config.Debug;
import aim4.im.IntersectionManager;
import aim4.map.BasicMap;
import aim4.map.Road;
import aim4.util.Util;
import aim4.vehicle.VehicleSpec;
import aim4.vehicle.VehicleUtil;

/**
 * A base class for an agent that chooses which way a vehicle should go.
 */
public class BasicNavigator implements Navigator {

  /////////////////////////////////
  // NESTED CLASSES
  /////////////////////////////////

  /**
   * A node in the A* search.
   */
  private class Node implements Comparable<Node> {

    /////////////////////////////////
    // PRIVATE FIELDS
    /////////////////////////////////

    /**
     * The list of nodes in the path.
     */
    private List<Integer> path;

    /**
     * The list of IMs in the path.
     */
    private List<Integer> pathIMs;
    /**
     * The distance from the origin to the current node.
     */
    private double actualMeasure;
    /**
     * The estimated distance from the current node to the destination.
     */
    private double estimatedRemainingMeasure;

    /////////////////////////////////
    // CLASS CONSTRUCTORS
    /////////////////////////////////

    /**
     * Create a node in the A* search.
     *
     * @param nodeId                      the node Id
     * @param pathIM                      the IM's Id
     * @param actualMeasure               the current distance from the origin
     *                                    to the current node
     * @param estimatedRemainingMeasure   the estimated distance from the
     *                                    current node to the destination
     */
    public Node(int nodeId,
                int pathIM,
                double actualMeasure,
                double estimatedRemainingMeasure) {
      this.path = new ArrayList<Integer>();
      this.pathIMs = new ArrayList<Integer>();
      this.path.add(nodeId);
      this.pathIMs.add(pathIM);
      this.actualMeasure = actualMeasure;
      this.estimatedRemainingMeasure = estimatedRemainingMeasure;
    }

    /**
     * Create a node in the A* search.
     *
     * @param path                       the path
     * @param pathIMs                    the list of IM's Ids of the path
     * @param actualMeasure              the estimated distance from the current
     *                                   node to the destination node
     * @param estimatedRemainingMeasure  the estimated distance from the
     *                                   current node to the destination
     */
    public Node(List<Integer> path,
                List<Integer> pathIMs,
                double actualMeasure,
                double estimatedRemainingMeasure) {
      this.path = path;
      this.pathIMs = pathIMs;
      this.actualMeasure = actualMeasure;
      this.estimatedRemainingMeasure = estimatedRemainingMeasure;
    }

    /////////////////////////////////
    // PUBLIC METHODS
    /////////////////////////////////

    /**
     * Compare to a node according to the estimated path length.
     *
     * @param np  the given node
     * @return the sign indicating the estimated path lengths difference.
     */
    @Override
    public int compareTo(Node np) {
      return Double.compare(actualMeasure + estimatedRemainingMeasure,
                            np.actualMeasure + np.estimatedRemainingMeasure);
    }


    /**
     * Create a new node with an additional one node.
     *
     * @param pathAddition               the new node ID
     * @param pathIM                     the new path to the new node
     * @param additionMeasure            the additional distance
     * @param estimatedRemainingMeasure  the new estimated distance from
     *                                   the current node to the destination
     * @return the new node
     */
    public Node makeUpdatedNode(int pathAddition,
                                int pathIM,
                                double additionMeasure,
                                double estimatedRemainingMeasure) {
      List<Integer> nextPath = new ArrayList<Integer>(path);
      nextPath.add(pathAddition);
      List<Integer> nextPathIMs = new ArrayList<Integer>(pathIMs);
      nextPathIMs.add(pathIM);
      return new Node(nextPath,
                      nextPathIMs,
                      actualMeasure + additionMeasure,
                      estimatedRemainingMeasure);
    }

    /**
     * Whether the destination has been reached.
     *
     * @return whether the destination has been reached.
     */
    public boolean isComplete() {
      return estimatedRemainingMeasure == 0;
    }

    /**
     * Get the list of node IDs on the path.
     *
     * @return the list of node Ids on the path
     */
    public List<Integer> getPath() {
      return path;
    }

    /**
     * Get the list of intersection managers' IDs on the path.
     *
     * @return the list of intersection managers' IDs
     */
    public List<Integer> getPathIMs() {
      return pathIMs;
    }

    /**
     * The road of the last node.
     *
     * @return the road of the last node
     */
    public Road getLastRoad() {
      return Debug.currentMap.getRoad(path.get(path.size() - 1));
    }

    /**
     * Get the last IM's ID.
     *
     * @return the last IM's ID
     */
    public int getLastIMid() {
      return pathIMs.get(pathIMs.size() - 1);
    }
  }


  /////////////////////////////////
  // PRIVATE FIELDS
  /////////////////////////////////

  /**
   * The map object
   */
  private BasicMap basicMap;

  /**
   * The vehicle for which this agent is navigating.
   */
  private VehicleSpec vehicleSpec;

  /**
   * A cache of the road leading away from the intersection with the fastest
   * path leading to the destination.
   */
  private Map<List<Integer>, Road> fastestMap =
    new HashMap<List<Integer>, Road>();


  /////////////////////////////////
  // CLASS CONSTRUCTORS
  /////////////////////////////////

  /**
   * Construct a new Navigator for the given Vehicle specification.
   * This will only be called by derived classes.
   *
   * @param vehicleSpec  the vehicle's specification
   * @param basicMap     the map object
   */
  public BasicNavigator(VehicleSpec vehicleSpec, BasicMap basicMap) {
    this.vehicleSpec = vehicleSpec;
    this.basicMap = basicMap;
  }

  /////////////////////////////////
  // PUBLIC METHODS
  /////////////////////////////////

  /**
   * {@inheritDoc}
   */
  @Override
  public Road navigate(Road current, IntersectionManager im, Road destination) {
    return fastestPath(current, im, destination);
  }


  /////////////////////////////////
  // PRIVATE METHODS
  /////////////////////////////////

  /**
   * Find the fastest path
   *
   * @param current     the Road on which the vehicle is currently traveling
   * @param im          the IntersectionManager the vehicle is approaching
   * @param destination the Road on which the vehicle would ultimately like to
   *                    end up
   * @return  The fastest road
   */
  private Road fastestPath(Road currentRoad, IntersectionManager im,
                           Road destinationRoad) {
    List<Integer> key = Arrays.asList(currentRoad.getIndexLane().getId(),
                                      im.getId(),
                                      destinationRoad.getIndexLane().getId());
    if(!fastestMap.containsKey(key)) {
      // Otherwise, we do an A* search for the route
      Node np = aStarSearchFastest(currentRoad, im, destinationRoad);
      List<Integer> path = np.getPath();
      List<Integer> pathIMs = np.getPathIMs();
      for(int i = 1; i < path.size(); i++) {
        List<Integer> currKey =
          Arrays.asList(path.get(i-1), pathIMs.get(i-1),
                        destinationRoad.getIndexLane().getId());
        fastestMap.put(currKey, Debug.currentMap.getRoad(path.get(i)));
      }
    }
    return fastestMap.get(key);
  }


  /**
   * Find the fastest path by A* search
   *
   * @param current     the Road on which the vehicle is currently traveling
   * @param im          the IntersectionManager the vehicle is approaching
   * @param destination the Road on which the vehicle would ultimately like to
   *                    end up
   * @return  The fastest road
   */
  private Node aStarSearchFastest(Road currentRoad, IntersectionManager im,
                                  Road destRoad) {
    // the queue
    PriorityQueue<Node> queue = new PriorityQueue<Node>();

    // initial point
    Point2D initPoint = im.getIntersection().getEntryPoint(
                        currentRoad.getIndexLane());

    // the initial node
    double estMeas = initPoint.distance(destRoad.getIndexLane().getEndPoint()) /
                     currentRoad.getMaximumConnectedSpeedLimit();

    Node initialNode = new Node(currentRoad.getIndexLane().getId(),
                                im.getId(),
                                0.0, // actual measure
                                estMeas);   // remaining estimate
    // kick off
    queue.add(initialNode);

    // Now we just do A* search. We remove items from the Queue.  If they are
    // complete, then YAY we have found the path.  If not, we explore the
    // neighbors, update and add them all.
    while(!queue.isEmpty() && !queue.peek().isComplete()) {
      Node node = queue.poll();  // the current node
      IntersectionManager nodeIM =
        basicMap.getImRegistry().get(node.getLastIMid());
      Road nodeRoad = node.getLastRoad();

      // for each departure road of the current node
      for(Road r : nodeIM.getIntersection().getExitRoads()) {

        // Don't come out the way we went in
        if(r == nodeRoad.getDual()) {
          continue;  // skip this node
        }

        // We need to find out how long it will take to cross the IM,
        // and get to the subsequent IM
        // Find out how fast we can take the turn
        double maxTurnVelocity =
          VehicleUtil.maxTurnVelocity(vehicleSpec,
                                      nodeRoad.getIndexLane(),
                                      r.getIndexLane(),
                                      nodeIM);

        // If this is 0, then we can't take this turn, so this is a no go
        if (Util.isDoubleZero(maxTurnVelocity)) {
          continue;  // skip this node
        }

        // Otherwise, we're good.
        double actualMeas = nodeIM.traversalDistance(nodeRoad, r) /
                            maxTurnVelocity;

        // Okay, now that we've accounted for crossing the intersection,
        // we have to figure out how far it is to the next intersection
        // after that.
        IntersectionManager nextIM =
          r.getIndexLane().getLaneIM().nextIntersectionManager(nodeIM);

        if(nextIM != null) {  // There is another IM to deal with
          // So find out how long it will take to get there
          actualMeas +=
            r.getIndexLane().getLaneIM().
            timeToNextIntersectionManager(nodeIM,
                                          vehicleSpec.getMaxVelocity());
          // Then estimate how long it will take to get from the
          // next intersection manager to the final destination.
          double estRemainingMeas =
            initPoint.distance(destRoad.getIndexLane().getEndPoint()) /
            currentRoad.getMaximumConnectedSpeedLimit();
          // Update with road we're going out on, the next IM
          queue.add(node.makeUpdatedNode(r.getIndexLane().getId(),
                                         nextIM.getId(),
                                         actualMeas,
                                         estRemainingMeas));

        } else if (r == destRoad) { //End of line,Are we where we want to be?
          // If so, this is how long it will take us to get out
          actualMeas +=
            r.getIndexLane().getLaneIM().
            remainingDistanceFromLastIntersection() /
            Math.min(r.getIndexLane().getSpeedLimit(),
                     vehicleSpec.getMaxVelocity());
          double estRemainingMeas = 0;
          // Update with road we're going out on, the next IM
          queue.add(node.makeUpdatedNode(r.getIndexLane().getId(),
                                         -1,
                                         actualMeas,
                                         estRemainingMeas));

        } // If not, then this is not a viable path, so just drop it.
      }
    }
    // Okay now either the queue is empty or the first one is complete
    return queue.peek();
  }

}

