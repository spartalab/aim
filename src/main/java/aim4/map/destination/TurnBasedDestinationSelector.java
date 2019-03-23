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
import aim4.map.lane.Lane;


//TODO: Need to fix this class to avoid hard-coding

/**
 * The turn based destination selector.
 */
public class TurnBasedDestinationSelector implements DestinationSelector {

  /////////////////////////////////
  // PRIVATE FIELDS
  /////////////////////////////////

  /**
   * The Set of legal Roads that a vehicle can use as an ultimate destination.
   */
  private List<Road> destinationRoads;

  /////////////////////////////////
  // CLASS CONSTRUCTORS
  /////////////////////////////////

  /**
   * Create a new identity destination selector from the given Layout.
   *
   * @param layout  the layout from which to create the new
   *                identity destination selector
   */
  public TurnBasedDestinationSelector(BasicMap layout) {
    destinationRoads = layout.getDestinationRoads();
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

    boolean hasLeft = currentLane.hasLeftNeighbor();
    boolean hasRight = currentLane.hasRightNeighbor();

    if (hasLeft && hasRight) {
      return currentRoad;
    } else if (!hasLeft && hasRight) {
      if (currentRoad.getName().equals("1st Street E")) {
        return destinationRoads.get(2);
      } else if (currentRoad.getName().equals("1st Street W")) {
        return destinationRoads.get(3);
      } else if (currentRoad.getName().equals("1st Avenue N")) {
        return destinationRoads.get(1);
      } else if (currentRoad.getName().equals("1st Avenue S")) {
        return destinationRoads.get(0);
      } else {
        throw new RuntimeException("Error in TurnBasedDestination");
      }
    } else if (hasLeft && !hasRight) {
      if (currentRoad.getName().equals("1st Street E")) {
        return destinationRoads.get(3);
      } else if (currentRoad.getName().equals("1st Street W")) {
        return destinationRoads.get(2);
      } else if (currentRoad.getName().equals("1st Avenue N")) {
        return destinationRoads.get(0);
      } else if (currentRoad.getName().equals("1st Avenue S")) {
        return destinationRoads.get(1);
      } else {
        throw new RuntimeException("Error in TurnBasedDestination");
      }
    } else {
      return currentRoad;
    }
  }
}
