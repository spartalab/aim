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
package aim4.map.destination;

import java.util.List;

import aim4.config.Debug;
import aim4.map.BasicMap;
import aim4.map.Road;
import aim4.map.SpawnPoint;
import aim4.map.TrafficVolume;
import aim4.map.lane.Lane;
import aim4.util.Util;
import java.util.HashMap;
import java.util.Map;

/**
 * The destination selector that
 */
public class RatioDestinationSelector implements DestinationSelector {

  /////////////////////////////////
  // PRIVATE FIELDS
  /////////////////////////////////

  /**
   * The set of roads that a vehicle can use as an ultimate destination.
   */
  private List<Road> destinationRoads;
  /**
   * The traffic volume object.
   */
  private TrafficVolume trafficVolume;
  /**
   * The probability of making a left turn.
   */
  private Map<Integer,Double> leftTurnProb;
  /**
   * The probability of making a right turn.
   */
  private Map<Integer,Double> rightTurnProb;


  /////////////////////////////////
  // CLASS CONSTRUCTORS
  /////////////////////////////////

  /**
   * Create a new RandomDestinationSelector from the given Layout.
   *
   * @param map            the Layout from which to create the
   *                       RandomDestinationSelector
   * @param trafficVolume  the traffic volume
   */
  public RatioDestinationSelector(BasicMap map, TrafficVolume trafficVolume) {
    destinationRoads = map.getDestinationRoads();
    this.trafficVolume = trafficVolume;
    leftTurnProb = new HashMap<Integer, Double>();
    rightTurnProb = new HashMap<Integer, Double>();

    for(SpawnPoint sp: map.getSpawnPoints()) {
      int laneId = sp.getLane().getId();
      leftTurnProb.put(laneId, trafficVolume.getLeftTurnVolume(laneId) /
                               trafficVolume.getThroughVolume(laneId));
      rightTurnProb.put(laneId, trafficVolume.getRightTurnVolume(laneId) /
                                trafficVolume.getThroughVolume(laneId));
    }
  }

  /////////////////////////////////
  // PUBLIC METHODS
  /////////////////////////////////

  /**
   * {@inheritDoc}
   */
  @Override
  public Road selectDestination(Lane currentLane) {
    Road currentRoad = Debug.currentMap.getRoad(currentLane);
    int laneId = currentLane.getId();
    double prob = Util.random.nextDouble();
    if (prob < leftTurnProb.get(laneId)) {
      return trafficVolume.getLeftTurnRoad(currentRoad);
    } else if (prob >= 1.0 - rightTurnProb.get(laneId)) {
      return trafficVolume.getRightTurnRoad(currentRoad);
    } else {
      return currentRoad;
    }
  }
}
