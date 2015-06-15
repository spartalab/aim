package expr.trb;

import aim4.config.Condor;
import aim4.config.Debug;
import aim4.config.GreenPhaseData;
import aim4.config.Platoon;
import aim4.config.RedPhaseData;
import aim4.config.SimConfig;
import aim4.config.SimConfig.SIGNAL_TYPE;
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
  private static boolean SHOW_GUI = true;

  /**
   * The main function of this experiment
   * 
   * Arguments -> OPTIONS PARAMETERS
   * OPTIONS -> [-d] [-r redPhaseLength] [-o] [-h] [-p] [-nf]
   * PARAMETERS -> trafficLeveL humanPercentage simpleCruiseControlPercentage adaptiveCruiseControlPercentage
   */
  public static void main(String[] args) {
    
    /////////////////////////////////
    // Settings
    /////////////////////////////////
    
  	if (args.length < 4 || args[0].endsWith("-help")) {
  		System.out.println("Arguments -> OPTIONS PARAMETERS\n"
  	   +"OPTIONS -> [-d] [-r redPhaseLength] [-o] [-h] [-p] [-nf]\n"
  		 +"-d generate data file instead of showing GUI\n"
  	   +"-r set the red phase length manually\n"
  		 +"-o one lane version\n"
  	   +"-h the traffic signal phases would adapt to the human traffic - the lights are on for the lanes with most number of human drivers\n"
  		 +"-p turn on platooning\n"
  	   +"-nf not fully observable. The IM knows nothing with the position of human drivers - it knows autonomous and semi-autonomous ones only.\n"
  	   +"PARAMETERS -> trafficLeveL humanPercentage simpleCruiseControlPercentage adaptiveCruiseControlPercentage");
  		
  		return;
  	}
  	
  	System.out.println("YELLOW : fully autonomous vehicles.\n" +
  			"GREEN  : simple cruise control vehicles.\n" +
  			"BLUE   : adaptive cruise control vehicles.\n" + 
  			"MAGENTA: human-driven vehicles.");
  	
  	SimConfig.signalType = SIGNAL_TYPE.TRADITIONAL;
  	
    String trafficLevelStr = args[args.length - 4];
		String humanPercentage = args[args.length - 3];
		String simpleCruiseControl = args[args.length - 2];
		String adaptiveCruiseControl = args[args.length - 1];
		String simulationTime = "1800"; // default simulation time
		
		boolean readRedPhase = false;
		
		for (int i = 0; i < args.length - 4; i++) {
  		String flag = args[i];
  		
  		if (readRedPhase) {
  			SimConfig.RED_PHASE_LENGTH = Double.parseDouble(flag);
  			readRedPhase = false;
  		}
  		else if (flag.equals("-d")) {
  			SHOW_GUI = false;
  		}
  		else if (flag.equals("-o")) {
  			SimConfig.signalType = SIGNAL_TYPE.ONE_LANE_VERSION;
  		}
  		else if (flag.equals("-h")) {
  			SimConfig.signalType = SIGNAL_TYPE.HUMAN_ADAPTIVE;
  		}
  		else if (flag.equals("-nf")) {
  			SimConfig.FULLY_OBSERVING = false;
  		}
  		else if (flag.equals("-p")) {
  			Platoon.platooning = true;
  		}
  		else if (flag.equals("-r")) {
  			readRedPhase = true;
  		}
  	}
		
		args = new String[]{"FCFS-SIGNAL-TRADITIONAL", // FIXME need this field no more
    		// for "FCFS-SIGNAL-DEBUG", config which to run in following part
    		"6phases",
    		".25",
    		"0.10",
    		"0.25",
    		trafficLevelStr, // 5) traffic level
    		humanPercentage, // 6) human percentage
    		simpleCruiseControl, // 7) constant human drivers percentage
    		simulationTime, // 8) simulation time
    		adaptiveCruiseControl // 9) adaptive human drivers percentage
    };
    
    if (args[0] == "BASELINE") {
      args[0] = "FCFS";
      GENERATE_BASELINE = true;
    }
    
    SIM_TYPE simType = SIM_TYPE.APPROX_TRAFFIC_SIGNAL;

    if (SimConfig.signalType != null && SimConfig.signalType != SimConfig.SIGNAL_TYPE.DEFAULT) {
    	SimConfig.HUMAN_PERCENTAGE = Double.parseDouble(args[6]);
    	SimConfig.CONSTANT_HUMAN_PERCENTAGE = Double.parseDouble(args[7]);
    }
    else {
    	// in this case, this parameter is not called.
    	// just make it safe.
    	SimConfig.HUMAN_PERCENTAGE = 0;
    }
    
//    double staticBufferSize = 0.25;
//    double internalTileTimeBufferSize = 0.1;
//    double edgeTileTimeBufferSize = 0.25;

    double staticBufferSize = Double.parseDouble(args[2]);
    double internalTileTimeBufferSize = Double.parseDouble(args[3]);
    double edgeTileTimeBufferSize = Double.parseDouble(args[4]);
    double trafficLevel = Double.parseDouble(args[5]);
    
    /**
     * for source files to read
     */
    String trafficSignalPhaseFileName = args[1] + "/AIM4Phases.csv";
    
    String trafficVolumeFileName = "";
    if (SimConfig.DEDICATED_LANES == 0) {
    	trafficVolumeFileName = args[1] + "/AIM4Volumes.csv";
    }
    else {
    	trafficVolumeFileName = args[1] + "/AIM4BalancedVolumes.csv";
    }
    
    /*
    // when we're doing experiments...
    if (args[8] != "") {
    	trafficSignalPhaseFileName = args[1] + "/AIM4Phases_" + args[8] + ".csv";
    }
    
    if (args[9] != "") {
    	trafficVolumeFileName = args[1] + "/AIM4Volumes_" + args[9] + ".csv";
    }
    */
    
    // the number of (simulated) seconds the simulator should run
    SimConfig.TOTAL_SIMULATION_TIME = Double.parseDouble(args[8]); // seconds
    
    SimConfig.ADAPTIVE_HUMAN_PERCENTAGE = Double.parseDouble(args[9]);
    
    /*
     * For dedicated lanes exp
    SimConfig.DEDICATED_LANES = Integer.parseInt(args[9]);
    */
    
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
        "ts_dcl_" + trafficLevel + "_" + humanPercentage + "_" + simpleCruiseControl + "_" + adaptiveCruiseControl + ".csv";
      map.printDataCollectionLinesData(dclOutFileName);
      
      System.out.printf("%s: done.\n", TrafficSignalExpr.class);
    }

  }
}
