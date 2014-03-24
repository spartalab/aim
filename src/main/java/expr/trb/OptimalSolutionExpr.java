package expr.trb;

import java.text.DecimalFormat;

import aim4.config.Condor;
import aim4.config.Debug;
import aim4.config.SimConfig;
import aim4.map.BasicMap;
import aim4.sim.Simulator;
import aim4.util.Util;

public class OptimalSolutionExpr extends TrafficSignalExpr {
  public static void main(String[] args) {
  	setup(args);
  	
  	// enable batch mode
  	basicSimSetup2.setBatchMode(true);
	
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
	  
	  /////////////////////////////////
	  // Generate data files
	  /////////////////////////////////
	
	  BasicMap map = sim.getMap();
	
	  // output the collected data of DCL
	  
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
	  map.printDataCollectionLinesData(dclOutFileName);
	  
	  System.out.printf("%s: done.\n", TrafficSignalExpr.class);
	}
}
