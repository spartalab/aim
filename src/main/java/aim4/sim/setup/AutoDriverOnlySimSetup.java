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
package aim4.sim.setup;

import aim4.config.Debug;
import aim4.config.SimConfig;
import aim4.driver.pilot.V2IPilot;
import aim4.im.v2i.batch.RoadBasedReordering;
import aim4.im.v2i.reservation.ReservationGridManager;
import aim4.map.GridMap;
import aim4.map.GridMapUtil;
import aim4.sim.AutoDriverOnlySimulator;
import aim4.sim.Simulator;

/**
 * The setup for the simulator in which all vehicles are autonomous.
 */
public class AutoDriverOnlySimSetup extends BasicSimSetup implements SimSetup {

  /////////////////////////////////
  // NESTED CLASSES
  /////////////////////////////////

  /**
   * The traffic type.
   */
  public enum TrafficType {
    UNIFORM_RANDOM,
    UNIFORM_TURNBASED,
    HVDIRECTIONAL_RANDOM,
    FILE,
  }

  /////////////////////////////////
  // PRIVATE FIELDS
  /////////////////////////////////

  /** Whether the base line mode is on */
  private boolean isBaseLineMode = false;
  /** Whether the batch mode is on */
  private boolean isBatchMode = false;
  /** The traffic type */
  private TrafficType trafficType = TrafficType.UNIFORM_RANDOM;
  /** The traffic level in the horizontal direction */
  private double hTrafficLevel;
  /** The traffic level in the vertical direction */
  private double vTrafficLevel;
  /** The static buffer size */
  private double staticBufferSize = 0.25;
  /** The time buffer for internal tiles */
  private double internalTileTimeBufferSize = 0.1;
  /** The time buffer for edge tiles */
  private double edgeTileTimeBufferSize = 0.25;
  /** Whether the edge time buffer is enabled */
  private boolean isEdgeTileTimeBufferEnabled = true;
  /** The granularity of the reservation grid */
  private double granularity = 1.0;
  /** The processing interval for the batch mode */
  private double processingInterval = RoadBasedReordering.DEFAULT_PROCESSING_INTERVAL;
  /** The name of the file about the traffic volume */
  private String trafficVolumeFileName = null;

  /////////////////////////////////
  // CONSTRUCTORS
  /////////////////////////////////

  /**
   * Create a setup for the simulator in which all vehicles are autonomous.
   *
   * @param basicSimSetup  the basic simulator setup
   */
  public AutoDriverOnlySimSetup(BasicSimSetup basicSimSetup) {
    super(basicSimSetup);
  }

  /**
   * Create a setup for the simulator in which all vehicles are autonomous.
   *
   * @param columns                     the number of columns
   * @param rows                        the number of rows
   * @param laneWidth                   the width of lanes
   * @param speedLimit                  the speed limit
   * @param lanesPerRoad                the number of lanes per road
   * @param medianSize                  the median size
   * @param distanceBetween             the distance between intersections
   * @param trafficLevel                the traffic level
   * @param stopDistBeforeIntersection  the stopping distance before
   *                                    intersections
   */
  public AutoDriverOnlySimSetup(int columns, int rows,
                                double laneWidth,
                                double speedLimit,
                                int lanesPerRoad,
                                double medianSize,
                                double distanceBetween,
                                double trafficLevel,
                                double stopDistBeforeIntersection) {
    super(columns, rows, laneWidth, speedLimit, lanesPerRoad,
          medianSize, distanceBetween, trafficLevel,
          stopDistBeforeIntersection);
  }


  /////////////////////////////////
  // PUBLIC METHODS
  /////////////////////////////////

  /**
   * Turn on or off the base line mode.
   *
   * @param b  Whether the base line mode is on
   */
  public void setIsBaseLineMode(boolean b) {
    isBaseLineMode = b;
  }

  /**
   * Turn on or off the batch mode.
   *
   * @param b  Whether the batch mode is on
   */
  public void setIsBatchMode(boolean b) {
    isBatchMode = b;
  }

  /**
   * Set the processing interval in the batch mode
   *
   * @param processingInterval  the processing interval
   */
  public void setBatchModeProcessingInterval(double processingInterval) {
    this.processingInterval = processingInterval;
  }

  /**
   * Set the uniform random traffic.
   *
   * @param trafficLevel  the traffic level
   */
  public void setUniformRandomTraffic(double trafficLevel) {
    this.trafficType = TrafficType.UNIFORM_RANDOM;
    this.trafficLevel = trafficLevel;
  }

  /**
   * Set the uniform turn-based traffic.
   *
   * @param trafficLevel the traffic level
   */
  public void setUniformTurnBasedTraffic(double trafficLevel) {
    this.trafficType = TrafficType.UNIFORM_TURNBASED;
    this.trafficLevel = trafficLevel;
  }

  /**
   * Set the directional random traffic.
   *
   * @param hTrafficLevel  the traffic level in the horizontal direction
   * @param vTrafficLevel  the traffic level in the Vertical direction
   */
  public void setHVdirectionalRandomTraffic(double hTrafficLevel,
                                            double vTrafficLevel) {
    this.trafficType = TrafficType.HVDIRECTIONAL_RANDOM;
    this.hTrafficLevel = hTrafficLevel;
    this.vTrafficLevel = vTrafficLevel;
  }

  /**
   * Set the traffic volume according to the specification in a file.
   *
   * @param trafficVolumeFileName  the file name of the traffic volume
   */
  public void setTrafficVolume(String trafficVolumeFileName) {
    this.trafficType = TrafficType.FILE;
    this.trafficVolumeFileName = trafficVolumeFileName;
  }

  /**
   * Set the buffer sizes.
   *
   * @param staticBufferSize             the static buffer size
   * @param internalTileTimeBufferSize   the time buffer size of internal tiles
   * @param edgeTileTimeBufferSize       the time buffer size of edge tiles
   * @param isEdgeTileTimeBufferEnabled  whether the edge time buffer is
   *                                     enabled
   * @param granularity                  the granularity of the simulation grid
   */
  public void setBuffers(double staticBufferSize,
                         double internalTileTimeBufferSize,
                         double edgeTileTimeBufferSize,
                         boolean isEdgeTileTimeBufferEnabled,
                         double granularity) {
    this.staticBufferSize = staticBufferSize;
    this.internalTileTimeBufferSize = internalTileTimeBufferSize;
    this.edgeTileTimeBufferSize = edgeTileTimeBufferSize;
    this.isEdgeTileTimeBufferEnabled = isEdgeTileTimeBufferEnabled;
    this.granularity = granularity;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Simulator getSimulator() {
    double currentTime = 0.0;
    GridMap layout = new GridMap(currentTime,
                                       numOfColumns,
                                       numOfRows,
                                       laneWidth,
                                       speedLimit,
                                       lanesPerRoad,
                                       medianSize,
                                       distanceBetween);
/* standard */
    ReservationGridManager.Config gridConfig =
      new ReservationGridManager.Config(SimConfig.TIME_STEP,
                                        SimConfig.GRID_TIME_STEP,
                                        staticBufferSize,
                                        internalTileTimeBufferSize,
                                        edgeTileTimeBufferSize,
                                        isEdgeTileTimeBufferEnabled,
                                        granularity);  // granularity

/* for demo */
/*
    ReservationGridManager.Config gridConfig =
      new ReservationGridManager.Config(SimConfig.TIME_STEP,
                                        SimConfig.GRID_TIME_STEP,
                                        0.25,  // staticBufferSize
                                        0.0,   // internalTileTimeBufferSize
                                        0.25,   // edgeTileTimeBufferSize
                                        true,  // edgeTileTimeBufferSize
                                        1.0);  // granularity
*/

/*  for Marvin */
/*
    ReservationGridManager.Config gridConfig =
      new ReservationGridManager.Config(SimConfig.TIME_STEP,
                                        SimConfig.GRID_TIME_STEP,
                                        0.9,  // staticBufferSize
                                        1.2,   // internalTileTimeBufferSize
                                        1.2,   // edgeTileTimeBufferSize
                                        true,  // edgeTileTimeBufferSize
                                        1.0);  // granularity
*/

    Debug.SHOW_VEHICLE_COLOR_BY_MSG_STATE = true;

    if (!isBaseLineMode) {
      if (isBatchMode) {
        GridMapUtil.setBatchManagers(layout, currentTime, gridConfig,
                                        processingInterval);
      } else {
        GridMapUtil.setFCFSManagers(layout, currentTime, gridConfig);
      }

      switch(trafficType) {
      case UNIFORM_RANDOM:
        GridMapUtil.setUniformRandomSpawnPoints(layout, trafficLevel);
        break;
      case UNIFORM_TURNBASED:
        GridMapUtil.setUniformTurnBasedSpawnPoints(layout, trafficLevel);
        break;
      case HVDIRECTIONAL_RANDOM:
        GridMapUtil.setDirectionalSpawnPoints(layout,
                                                 hTrafficLevel,
                                                 vTrafficLevel);
        break;
      case FILE:
        GridMapUtil.setUniformRatioSpawnPoints(layout, trafficVolumeFileName);
        break;
      }
    } else {
      GridMapUtil.setFCFSManagers(layout, currentTime, gridConfig);
      GridMapUtil.setBaselineSpawnPoints(layout, 12.0);
    }


    V2IPilot.DEFAULT_STOP_DISTANCE_BEFORE_INTERSECTION =
      stopDistBeforeIntersection;
    return new AutoDriverOnlySimulator(layout);
  }
}
