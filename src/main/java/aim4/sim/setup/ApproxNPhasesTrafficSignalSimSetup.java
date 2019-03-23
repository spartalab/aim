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
import aim4.im.v2i.reservation.ReservationGridManager;
import aim4.map.GridMap;
import aim4.map.GridMapUtil;
import aim4.sim.AutoDriverOnlySimulator;
import aim4.sim.Simulator;

/**
 * The setup for the simulator in which the intersections are controlled
 * by N-phases traffic signals.
 */
public class ApproxNPhasesTrafficSignalSimSetup extends BasicSimSetup
                                                implements SimSetup {

  /** The name of the file containing the traffic signal phases */
  private String trafficSignalPhaseFileName;
  /** The name of the file containing the traffic volume information */
  private String trafficVolumeFileName;

  /////////////////////////////////
  // CONSTRUCTORS
  /////////////////////////////////

  /**
   * Create the setup for the simulator in which the intersections are
   * controlled by N-phases traffic signals.
   *
   * @param basicSimSetup               the basic simulator setup
   * @param trafficSignalPhaseFileName  the name of the file containing the
   *                                    traffic signal phase
   */
  public ApproxNPhasesTrafficSignalSimSetup(BasicSimSetup basicSimSetup,
                                            String trafficSignalPhaseFileName) {
    super(basicSimSetup);

    this.trafficSignalPhaseFileName = trafficSignalPhaseFileName;
    this.trafficVolumeFileName = null;
  }

//  public ApproxNPhasesTrafficSignalSimSetup(int columns, int rows,
//                                     double laneWidth, double speedLimit,
//                                     int lanesPerRoad,
//                                     double medianSize, double distanceBetween,
//                                     double trafficLevel,
//                                     double stopDistBeforeIntersection) {
//    super(columns, rows, laneWidth, speedLimit, lanesPerRoad,
//          medianSize, distanceBetween, trafficLevel,
//          stopDistBeforeIntersection);
//  }


  /////////////////////////////////
  // PUBLIC METHODS
  /////////////////////////////////

  /**
   * Set the traffic volume according to the specification in a file.
   *
   * @param trafficVolumeFileName  the name of the file containing the
   *                               traffic volume information
   */
  public void setTrafficVolume(String trafficVolumeFileName) {
    this.trafficVolumeFileName = trafficVolumeFileName;
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

    ReservationGridManager.Config gridConfig =
      new ReservationGridManager.Config(SimConfig.TIME_STEP,
                                        SimConfig.GRID_TIME_STEP,
                                        0.1,
                                        0.15,
                                        0.15,
                                        true,
                                        1.0);

    Debug.SHOW_VEHICLE_COLOR_BY_MSG_STATE = false;

    GridMapUtil.setApproxNPhasesTrafficLightManagers(
        layout, currentTime, gridConfig, trafficSignalPhaseFileName);

    if (numOfColumns == 1 && numOfRows == 1) {
      GridMapUtil.setUniformRatioSpawnPoints(layout, trafficVolumeFileName);
      // GridLayoutUtil.setUniformTurnBasedSpawnPoints(layout, trafficLevel);
    } else {
      GridMapUtil.setUniformRandomSpawnPoints(layout, trafficLevel);
    }

    V2IPilot.DEFAULT_STOP_DISTANCE_BEFORE_INTERSECTION =
      stopDistBeforeIntersection;

    return new AutoDriverOnlySimulator(layout);
  }
}
