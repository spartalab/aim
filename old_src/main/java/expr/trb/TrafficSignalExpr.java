package expr.trb;

import aim4.config.Condor;
import aim4.config.Debug;
import aim4.config.SimConfig;
import aim4.driver.pilot.V2IPilot;
import aim4.gui.Viewer;
import aim4.map.BasicMap;
import aim4.sim.Simulator;
import aim4.sim.setup.ApproxNPhasesTrafficSignalSimSetup;
import aim4.sim.setup.ApproxStopSignSimSetup;
import aim4.sim.setup.AutoDriverOnlySimSetup;
import aim4.sim.setup.BasicSimSetup;
import aim4.util.Util;


public class TrafficSignalExpr {

  private enum SIM_TYPE {
    FCFS,
    APPROX_TRAFFIC_SIGNAL,
    APPROX_STOP_SIGN
  }
  
  private static boolean GENERATE_BASELINE = false;
  private static final boolean SHOW_GUI = true;

  /**
   * The main function of this experiment
   * 
   * @param args  the command line argument:
   *   args[0] = the type of the simulation
   *   args[1] = traffic pattern/signal pattern data directory
   *   args[2] = static buffer size for FCFS
   *   args[3] = time buffer size for FCFSxi
   *   args[4] = edge time buffer size for FCFS
   *   args[5] = traffic level (0 - 0.7) (max is 2500 / 3600)
   *   args[6] = human portion
   *   args[7] = simulation time
   */
  public static void main(String[] args) {
    
    /////////////////////////////////
    // Settings
    /////////////////////////////////
        
    if (args.length == 0) {
      args = new String[]{"FCFS-SIGNAL", "6phases", ".25", "0.10", "0.25", "0.2", "0.1", "1"};
      // args = new String[]{"FCFS", "2phases", ".25", "0.10", "0.25"};
      // args = new String[]{"FCFS", "2phases", ".50", "0.50", "1.00"};
    }
    
    if (args[0] == "BASELINE") {
      args[0] = "FCFS";
      GENERATE_BASELINE = true;
    }
    
    SIM_TYPE simType;

    if (args[0].equals("FCFS")) {
      simType = SIM_TYPE.FCFS;
    } else if (args[0].equals("SIGNAL")) {
      simType = SIM_TYPE.APPROX_TRAFFIC_SIGNAL;
    } else if (args[0].equals("STOP")) {
      simType = SIM_TYPE.APPROX_STOP_SIGN;
    } else if (args[0].equals("FCFS-SIGNAL")) {
      simType = SIM_TYPE.APPROX_TRAFFIC_SIGNAL;
      SimConfig.FCFS_APPLIED_FOR_SIGNAL = true;
      SimConfig.HUMAN_PORTION = Double.parseDouble(args[6]);
    } else {
      throw new RuntimeException("Incorrect arguments: the sim type should " +
          "be equal to FCFS, LIGHT, STOP or FCFS-SIGNAL.");
    }

    String trafficSignalPhaseFileName = args[1] + "/AIM4Phases.csv";
    String trafficVolumeFileName = args[1] + "/AIM4Volumes.csv";

//    double staticBufferSize = 0.25;
//    double internalTileTimeBufferSize = 0.1;
//    double edgeTileTimeBufferSize = 0.25;

    double staticBufferSize = Double.parseDouble(args[2]);
    double internalTileTimeBufferSize = Double.parseDouble(args[3]);
    double edgeTileTimeBufferSize = Double.parseDouble(args[4]);
    double trafficLevel = Double.parseDouble(args[5]);
    // the number of (simulated) seconds the simulator should run
    SimConfig.TOTAL_SIMULATION_TIME = Double.parseDouble(args[7]); // seconds
    
    BasicSimSetup basicSimSetup =
        new BasicSimSetup(1, // columns
                          1, // rows
                          4, // lane width
                          25.0, // speed limit
                          3, // lanes per road
                          1, // median size
                          150, // distance between
                          0.28, // traffic level
                                // (for now, it can be any number)
                          1.0 // stop distance before intersection
        );

    BasicSimSetup basicSimSetup2 = null;
    // ReservationGridManager.Config fcfsPolicyConfig = null;
    
    switch(simType) {
    case FCFS:
      boolean isEdgeTileTimeBufferEnabled = true;
      double granularity = 1.0;

      AutoDriverOnlySimSetup autoDriverOnlySimSetup =
          new AutoDriverOnlySimSetup(basicSimSetup);
      autoDriverOnlySimSetup.setIsBaseLineMode(GENERATE_BASELINE);
      autoDriverOnlySimSetup.setBuffers(staticBufferSize,
                                        internalTileTimeBufferSize,
                                        edgeTileTimeBufferSize,
                                        isEdgeTileTimeBufferEnabled,
                                        granularity);
      autoDriverOnlySimSetup.setTrafficVolume(trafficVolumeFileName);
      autoDriverOnlySimSetup.setTrafficLevel(trafficLevel);
      basicSimSetup2 = autoDriverOnlySimSetup;
      break;
    case APPROX_TRAFFIC_SIGNAL:
      ApproxNPhasesTrafficSignalSimSetup approxNPhasesTrafficSignalSimSetup =
          new ApproxNPhasesTrafficSignalSimSetup(basicSimSetup,
                                                 trafficSignalPhaseFileName);
      approxNPhasesTrafficSignalSimSetup.setTrafficVolume(trafficVolumeFileName);
      approxNPhasesTrafficSignalSimSetup.setTrafficLevel(trafficLevel);
      basicSimSetup2 = approxNPhasesTrafficSignalSimSetup;
      
      Debug.SHOW_VEHICLE_COLOR_BY_MSG_STATE = false;
      break;
    case APPROX_STOP_SIGN:
      ApproxStopSignSimSetup approxStopSignSimSetup =
          new ApproxStopSignSimSetup(basicSimSetup);
      //approxStopSignSimSetup.setTrafficVolume(trafficVolumeFileName);
      approxStopSignSimSetup.setTrafficLevel(trafficLevel);
      SimConfig.MUST_STOP_BEFORE_INTERSECTION = true;
      Debug.SHOW_VEHICLE_COLOR_BY_MSG_STATE = false;
      basicSimSetup2 = approxStopSignSimSetup;
      break;
    }
    
    V2IPilot.DEFAULT_STOP_DISTANCE_BEFORE_INTERSECTION = 1.0;
    
    
    /////////////////////////////////
    // Instantiate the simulator
    /////////////////////////////////

//    AutoDriverOnlySimSetup autoDriverOnlySimSetup =
//        new AutoDriverOnlySimSetup(basicSimSetup);


    /////////////////////////////////
    // Run the simulator
    /////////////////////////////////

    if (SHOW_GUI) {
      new Viewer(basicSimSetup2, true);
    }
    else {
      // get the simulator
      //System.out.println(System.getProperty("user.dir"));
      Simulator sim = basicSimSetup2.getSimulator();
      // run the simulator
      double currentTime = 0.0;
      while (currentTime <= SimConfig.TOTAL_SIMULATION_TIME) {
        Debug.clearShortTermDebugPoints();
        sim.step(SimConfig.TIME_STEP);
        currentTime += SimConfig.TIME_STEP;
      }
      
      /*
      // Print how many vehicles are inhibited
      System.out.println("Prohibited: " + sim.getProhibitedVehiclesNum());
      System.out.println("Generated: " + sim.getGeneratedVehiclesNum());
      System.out.println("ETL: " + 1.0 * sim.getGeneratedVehiclesNum() / 12 / SimConfig.TOTAL_SIMULATION_TIME);
      System.out.println("TTL: " 
    		  			+ 1.0 * (sim.getGeneratedVehiclesNum() + sim.getProhibitedVehiclesNum()) / 12 / SimConfig.TOTAL_SIMULATION_TIME);
      */
      
      /////////////////////////////////
      // Generate data files
      /////////////////////////////////

      BasicMap map = sim.getMap();

      // output the collected data of DCL
      
      
      String dclOutFileName =
        "ts_dcl_" + Util.concatenate(args, "_").replace('/', '_')
        + Condor.CONDOR_FILE_SUFFIX + ".csv";
      map.printDataCollectionLinesData(dclOutFileName);
      
      System.out.printf("%s: done.\n", TrafficSignalExpr.class);
    }

  }
}