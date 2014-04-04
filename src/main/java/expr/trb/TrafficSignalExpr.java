package expr.trb;

import java.text.DecimalFormat;

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

  protected enum SIM_TYPE {
    FCFS,
    APPROX_TRAFFIC_SIGNAL,
    APPROX_STOP_SIGN
  }
  
  protected static boolean GENERATE_BASELINE = false;
  protected static boolean SHOW_GUI = true;
  
  protected static BasicSimSetup basicSimSetup2 = null;
  protected static double trafficLevel;

  /**
   * The main function of this experiment
   * 
   * See the output for the arguments setting. MAKE SURE YOU UPDATE THEM EACH TIME AFTER MODIFICATION.
   */
  protected static void setup(String[] args) {
    /////////////////////////////////
    // Settings
    /////////////////////////////////
    //String[] myArgs = {"-r", "30", ".2", "0", "1", "0", "0"};
    //args = myArgs;
    
  	if (args.length < 4 || args[0].endsWith("-help")) {
  		System.out.println("Arguments -> OPTIONS PARAMETERS\n"
  	   +"OPTIONS -> [-ng] [-r redPhaseLength] [-d number of dedicated lanes] [-o] [-h] [-p] [-t] [-s] [-f]\n"
  	   +"-ng generate data file instead of showing GUI\n"
  	   +"-r set the red phase length manually. 4 secs by default.\n"
  	   +"-d with dedicated lanes\n"
  	   +"-o one lane version\n"
  	   +"-h the traffic signal phases would adapt to the human traffic - the lights are on for the lanes with most number of human drivers\n"
  	   +"-p turn on platooning\n"
  	   +"-t manually set simulation time, 1800s by default.\n"
  	   +"-s semi-autonomous experiments baseline.\n"
  	   +"-f fully observable. The IM knows the positions of human drivers. IM only knows the info of auto and semi-auto vehicles by default.\n"
  	   +"PARAMETERS -> trafficLeveL humanPercentage informedHumanPercentage simpleCruiseControlPercentage adaptiveCruiseControlPercentage");
  		
  		return;
  	}
  	
  	SimConfig.signalType = SIGNAL_TYPE.TRADITIONAL;
  	
  	// read parameters
  	trafficLevel = Double.parseDouble(args[args.length - 5]);
	SimConfig.HUMAN_PERCENTAGE = Double.parseDouble(args[args.length - 4]);
	SimConfig.INFORMED_HUMAN_PERCENTAGE = Double.parseDouble(args[args.length - 3]);
	SimConfig.SIMPLE_CRUISE_PERCENTAGE = Double.parseDouble(args[args.length - 2]);
	SimConfig.ADAPTIVE_CRUISE_PERCENTAGE = Double.parseDouble(args[args.length - 1]);
	
	if (SimConfig.HUMAN_PERCENTAGE
			+ SimConfig.INFORMED_HUMAN_PERCENTAGE
			+ SimConfig.SIMPLE_CRUISE_PERCENTAGE
			+ SimConfig.ADAPTIVE_CRUISE_PERCENTAGE > 1) {
		throw new RuntimeException("The sum of percentages of vehicles exceeds 1.");
	}
	
	// Volume file to read
  String trafficVolumeFileName = SimConfig.phaseDir + "/AIM4Volumes.csv";
		
	// read options
	boolean readRedPhase = false, readDedicatedLanes = false, readSimulationTime = false, readVolumeFile = false;
	
	for (int i = 0; i < args.length - 5; i++) {
		String flag = args[i];
		
		if (readRedPhase) {
			SimConfig.RED_PHASE_LENGTH = Double.parseDouble(flag);
			readRedPhase = false;
		}
		else if (readDedicatedLanes) {
			SimConfig.signalType = SIGNAL_TYPE.DEDICATED_LANES;
			
			SimConfig.DEDICATED_LANES = Integer.parseInt(flag);
			if (SimConfig.DEDICATED_LANES > 2) {
				throw new RuntimeException("The number of dedicated lanes should be smaller than the number of total lanes!");
			}
			else if (SimConfig.DEDICATED_LANES < 0) {
				throw new RuntimeException("The number of dedicated lanes should be positive!");
			}
			readDedicatedLanes = false;
		}
		else if (readSimulationTime) {
			SimConfig.TOTAL_SIMULATION_TIME = Double.parseDouble(flag);
			readSimulationTime = false;
		}
		else if (readVolumeFile) {
			trafficVolumeFileName = SimConfig.phaseDir + "/" + flag;
			readVolumeFile = false;
		}
		else if (flag.equals("-fb")) {
			SimConfig.BATCH_MODE = true;
			SimConfig.RANDOM_BATCH = false;
		}
		else if (flag.equals("-rb")) {
			SimConfig.BATCH_MODE = true;
			SimConfig.RANDOM_BATCH = true;
		}
		else if (flag.equals("-v")) {
			readVolumeFile = true;
		}
		// some implemented features for semi-auto
		/* 
		else if (flag.equals("-ng")) {
			SHOW_GUI = false;
		}
		else if (flag.equals("-o")) {
			SimConfig.signalType = SIGNAL_TYPE.ONE_LANE_VERSION;
		}
		else if (flag.equals("-h")) {
			SimConfig.signalType = SIGNAL_TYPE.HUMAN_ADAPTIVE;
		}
		else if (flag.equals("-f")) {
			SimConfig.FULLY_OBSERVING = true;
		}
		else if (flag.equals("-p")) {
			Platoon.platooning = true;
		}
		else if (flag.equals("-r")) {
			readRedPhase = true;
		}
		else if (flag.equals("-d")) {
			readDedicatedLanes = true;
		}
		else if (flag.equals("-t")) {
			readSimulationTime = true;
		}
		else if (flag.equals("-s")) {
			SimConfig.signalType = SIGNAL_TYPE.SEMI_AUTO_EXPR;
		}*/
		else {
			throw new RuntimeException("Unknown flag!");
		}
  }
    
	if (SHOW_GUI) {
		System.out.println("YELLOW : fully autonomous vehicles.\n" +
				"GREEN  : simple cruise control vehicles.\n" +
				"BLUE   : adaptive cruise control vehicles.\n" +
				"WHITE  : human-driven vehicles with communication devices.\n" +
				"MAGENTA: human-driven vehicles.\n");
	}
		
    SIM_TYPE simType = SIM_TYPE.FCFS;

    double staticBufferSize = 0.25;
    double internalTileTimeBufferSize = 0.10;
    double edgeTileTimeBufferSize = 0.25;
    
    /**
     * for source files to read
     */
    String trafficSignalPhaseFileName = SimConfig.phaseDir + "/AIM4Phases.csv";
    
    if (SimConfig.signalType == SIGNAL_TYPE.SEMI_AUTO_EXPR) {
    	// for the purpose of this experiment
    	SimConfig.DEDICATED_LANES = 1;
    }
    
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
      
      autoDriverOnlySimSetup.setBatchMode(SimConfig.BATCH_MODE);
      
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
  }
  

  public static void main(String[] args) {
  	setup(args);
	
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

      /*
      // keep the formats of #.## to keep consistency with python, octave, etc..
      DecimalFormat df = new DecimalFormat("0.00");
      String[] arguments = {df.format(trafficLevel).toString(),
      											df.format(SimConfig.HUMAN_PERCENTAGE).toString(),
      											df.format(SimConfig.INFORMED_HUMAN_PERCENTAGE).toString(),
      											df.format(SimConfig.SIMPLE_CRUISE_PERCENTAGE).toString(),
      											df.format(SimConfig.ADAPTIVE_CRUISE_PERCENTAGE).toString(),
      											df.format(SimConfig.RED_PHASE_LENGTH).toString()};
      
      String dclOutFileName =
        "ts_dcl_" + Util.concatenate(arguments, "_").replace('/', '_')
        + Condor.CONDOR_FILE_SUFFIX + ".csv";
      */
      
      map.printDataCollectionLinesData();
    }
  }
}
