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
package aim4.map;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import aim4.map.lane.Lane;

/**
 * A group of lanes with a name.
 */
public class Road {

  /////////////////////////////////
  // PRIVATE FIELDS
  /////////////////////////////////

  /** The name of this road. */
  private String name;
  /** The lanes that make up this road, from left to right. */
  private List<Lane> lanes;
  /** The Road that follows this one in the opposite direction. */
  private Road dual;
  /** The Layout of which the Road is a part. */
  private BasicMap map;

  /////////////////////////////////
  // CLASS CONSTRUCTORS
  /////////////////////////////////

  /**
   * Create a new Road with no Lanes.  Lanes can then be added.
   *
   * @param name   the name of the Road
   * @param map    the map of which the Road is a part
   */
  public Road(String name, BasicMap map) {
    this(name, new ArrayList<Lane>(), map);
  }

  /**
   * Create a new Road with the given Lanes, ordered from left to right.
   *
   * @param name   the name of the Road
   * @param lanes  the Lanes from which to make the Road
   * @param map    the Layout of which the Road is a part
   */
  public Road(String name, List<Lane> lanes, BasicMap map) {
    this.name = name;
    this.lanes = new ArrayList<Lane>(lanes);
    this.map = map;
    // Now set up the proper relationships between them
    if(lanes.size() > 1) {
      for(int i = 0; i < lanes.size() - 1; i++) {
        lanes.get(i).setRightNeighbor(lanes.get(i + 1));
        lanes.get(i + 1).setLeftNeighbor(lanes.get(i));
      }
    }
  }

  /////////////////////////////////
  // PUBLIC METHODS
  /////////////////////////////////

  /**
   * Get the maximum speed limit of any connected road.
   *
   * @return the maximum speed limit, in meters per second, of any connected
   *         road
   */
  public double getMaximumConnectedSpeedLimit() {
    return map.getMaximumSpeedLimit();
  }

  /**
   * Get the Lanes that make up this Road, in order from left to right.
   *
   * @return the Lanes that make up this Road, in order from left to right
   */
  public List<Lane> getLanes() {
    return Collections.unmodifiableList(lanes);
  }

  /**
   * Get the leftmost Lane in this Road.
   *
   * @return the leftmost Lane in this Road
   */
  public Lane getIndexLane() {
    if(lanes.isEmpty()) {
      return null;
    }
    return lanes.get(0);
  }

  /**
   * Get the Road that follows this Road in the opposite direction.
   *
   * @return the Road that follows this Road in the opposite direction
   */
  public Road getDual() {
    return dual;
  }

  /**
   * Set the Road that follows this Road in the opposite direction.
   *
   * @param dual the Road that follows this Road in the opposite direction
   */
  public void setDual(Road dual) {
    this.dual = dual;
    // Also make sure the reciprocal relationship is set.
    dual.dual = this;
  }

  /**
   * Whether or not this Road has a dual.
   *
   * @return whether or not this Road has a dual
   */
  public boolean hasDual() {
    return dual != null;
  }

  /**
   * Add a right most lane to this Road.
   *
   * @param lane the Lane to add
   */
  public void addTheRightMostLane(Lane lane) {
    if(!lanes.isEmpty()) {
      Lane rightmost = lanes.get(lanes.size() - 1);
      rightmost.setRightNeighbor(lane);
      lane.setLeftNeighbor(rightmost);
    }
    lanes.add(lane);
  }

  /**
   * Get the name of this Road. An alias for {@link #getName()}.
   *
   * @return the name of this Road
   */
  @Override
  public String toString() {
    return getName();
  }

  /**
   * Get the name of this Road.
   *
   * @return the name of this Road
   */
  public String getName() {
    return name;
  }
}
