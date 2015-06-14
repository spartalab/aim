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

import java.awt.geom.Rectangle2D;
import java.util.List;

import aim4.im.IntersectionManager;
import aim4.map.lane.Lane;
import aim4.util.Registry;

/**
 * Essentially a structured grouping of Roads and IntersectionManagers that
 * allows a unified interface so that we can re-use certain layouts and
 * create classes of layouts.
 */
public interface BasicMap {

  /**
   * Get the Roads that are part of this Layout.
   *
   * @return the Roads that are part of this Layout
   */
  List<Road> getRoads();

  /**
   * Get the Roads that exit this Layout.
   *
   * @return the Roads exit this Layout
   */
  List<Road> getDestinationRoads();

  /**
   * Get the dimensions of this Layout, in Rectangle form.
   *
   * @return a Rectangle representing the dimensions of this Layout
   */
  Rectangle2D getDimensions();

  /**
   * Get the maximum speed limit of any Road in the Layout.
   *
   * @return the maximum speed, in meters per second, of any Lane in any Road
   *         in the Layout
   */
  double getMaximumSpeedLimit();

  /**
   * Get the intersection manager registry.
   *
   * @return the intersection manager registry.
   */
  Registry<IntersectionManager> getImRegistry();

  /**
   * Get the lane registry.
   *
   * @return the lane registry.
   */
  Registry<Lane> getLaneRegistry();

  /**
   * Given a Lane, get the Road of which that Lane is a part.
   *
   * @param lane the Lane for which to get the enclosing Road
   * @return     the Road of which the given Lane is a part.
   */
  Road getRoad(Lane lane);

  /**
   * Given a Lane ID number, get the Road of which that Lane is a part.
   *
   * @param laneID the ID of the Lane for which to get the enclosing Road
   * @return       the Road of which the given Lane is a part.
   */
  Road getRoad(int laneID);

  /**
   * Get the IntersectionManagers that are part of this Layout.
   *
   * @return the IntersectionManagers that are part of this Layout
   */
  List<IntersectionManager> getIntersectionManagers();

  /**
   * Get the list of data collection line.
   *
   * @return the data collection lines
   */
  List<DataCollectionLine> getDataCollectionLines();

  /**
   * Get the list of spawn points.
   *
   * @return the lkist of spawn points
   */
  List<SpawnPoint> getSpawnPoints();

  /**
   * Set the intersection manager of a particular intersection.
   *
   * @param column  the column of the intersection
   * @param row     the row of the intersection
   * @param im      the intersection manager
   */
  void setManager(int column, int row, IntersectionManager im);

  /**
   * Print the data collected in data collection lines to the given file
   *
   * @param outFileName  the name of the file to which the data are outputted.
   */
  void printDataCollectionLinesData(String outFileName);
}
