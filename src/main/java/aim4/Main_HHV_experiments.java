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
package aim4;

import aim4.config.Debug;
import aim4.driver.pilot.V2IPilot;
import aim4.gui.Viewer;
import aim4.map.BasicMap;
import aim4.sim.Simulator;
import aim4.sim.setup.AutoDriverOnlySimSetup;
import aim4.config.SimConfig;
import aim4.config.SimConfig.SIGNAL_TYPE;

/**
 * Alternative class to run a set simulation with no GUI.
 */
public class Main_HHV_experiments {

  private static boolean GENERATE_BASELINE = false;
  private static boolean SHOW_GUI = true;

  /////////////////////////////////
  // THE MAIN FUNCTION
  /////////////////////////////////

  /**
   * The main function of the simulator.
   * It starts the GUI.
   *
   * @param args  the command-line arguments
   *
   *              Arguments ->  OPTIONS PARAMETERS
   *              OPTIONS ->    [-d] [-o] [-b]
   *              PARAMETERS -> trafficLevel humanWithHUDpercentage bufferFactor
   *
   */
  public static void main(String[] args) {

    /////////////////////////////////
    // Settings
    /////////////////////////////////

    if (args.length < 3 || args[0].endsWith("-help")) {
      System.out.println("Arguments  -> OPTIONS PARAMETERS\n"
                        +"OPTIONS    -> -d  generate data file instead of showing GUI\n"
                        +"              -o  one lane version\n"
                        +"              -b  generates baseline (ignores parameters)\n"
                        +"PARAMETERS -> trafficLevel      Amount of traffic (as a proportion of 3600 veh/lane/hr)"
                        +"              humanWithHUDp     proportion of vehicles that are human-driven following a HUD"
                        +"                                (the rest are fully autonomous)"
                        +"              bufferFactor      Factor to apply to HUD vehicle buffers compared to AVs");

      return;
    }

    double trafficLevel = Double.parseDouble(args[args.length - 3]);
    SimConfig.HUD_HUMAN_PERCENTAGE = Double.parseDouble(args[args.length - 2]);
    double bufferFactor = Double.parseDouble(args[args.length - 1]);

    /*
      for source files to read
     */
    String trafficVolumeFileName;
    if (SimConfig.DEDICATED_LANES == 0) {
      trafficVolumeFileName = "src/main/java/6phases/AIM4Volumes.csv";
    } else {
      trafficVolumeFileName = "src/main/java/6phases/AIM4BalancedVolumes.csv";
    }

    SimConfig.TOTAL_SIMULATION_TIME = 1800;

    for (int i = 0; i < args.length - 2; i++) {
      String flag = args[i];

      switch (flag) {
        case "-d":
          SHOW_GUI = false;
          break;
        case "-o":
          // TODO: interpret differently?
          SimConfig.signalType = SIGNAL_TYPE.ONE_LANE_VERSION;
          break;
        case "-b":
          GENERATE_BASELINE = true;
          break;
      }
    }

    double staticBufferSize = .25; // TODO: make this CL arg?

    AutoDriverOnlySimSetup autoDriverOnlySimSetup =
            new AutoDriverOnlySimSetup(1, // columns
                                       1, // rows
                                       4, // lane width
                                       25.0, // speed limit
                                       3, // lanes per road
                                       1, // median size
                                       150, // distance between
                                       trafficLevel, // traffic level
                                       1.0 // stop distance before intersection
                      );
    autoDriverOnlySimSetup.setIsBaseLineMode(GENERATE_BASELINE);
    autoDriverOnlySimSetup.setBuffers(staticBufferSize,
                                      .1,
                                      .25,
                                      true,
                                      1.0);
    autoDriverOnlySimSetup.setBufferFactorForHUD(bufferFactor);
    autoDriverOnlySimSetup.setTrafficVolume(trafficVolumeFileName);
    autoDriverOnlySimSetup.setTrafficLevel(trafficLevel);

    V2IPilot.DEFAULT_STOP_DISTANCE_BEFORE_INTERSECTION = 1.0;


    /////////////////////////////////
    // Run the simulator
    /////////////////////////////////

    if (SHOW_GUI) {
      System.out.println("YELLOW : fully autonomous vehicles\n"
                        +"PINK   : human-driven vehicles following HUDs\n");
      new Viewer(autoDriverOnlySimSetup, true);
    } else {
      // get the simulator
      Simulator sim = autoDriverOnlySimSetup.getSimulator();
      // run the simulator
      double currentTime = 0.0;
      while (currentTime <= SimConfig.TOTAL_SIMULATION_TIME) {
        Debug.clearShortTermDebugPoints();
        sim.step(SimConfig.TIME_STEP);
        currentTime += SimConfig.TIME_STEP;
      }

      /////////////////////////////////
      // Generate data files
      /////////////////////////////////

      BasicMap map = sim.getMap();

      // output the collected data of DCL

      String dclOutFileName;
      if (GENERATE_BASELINE) {
        dclOutFileName = "output/ts_hud_baseline.csv";
      } else {
        dclOutFileName =
                "output/ts_hud_" + trafficLevel + "_" + SimConfig.HUD_HUMAN_PERCENTAGE + "_" +  + bufferFactor + ".csv";
      }
      map.printDataCollectionLinesData(dclOutFileName);

      System.out.printf("%s: done.\n", Main_HHV_experiments.class);

    }
  }
}