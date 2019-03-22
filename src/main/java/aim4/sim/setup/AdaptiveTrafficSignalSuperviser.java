package aim4.sim.setup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import aim4.config.Resources;
import aim4.driver.Driver;
import aim4.driver.coordinator.V2ICoordinator.State;
import aim4.im.v2i.RequestHandler.ApproxNPhasesTrafficSignalRequestHandler.SignalController;
import aim4.map.Road;
import aim4.map.lane.Lane;
import aim4.vehicle.VehicleSimView;
import aim4.im.v2i.RequestHandler.ApproxNPhasesTrafficSignalRequestHandler.AdaptiveSignalController;

/**
 * it figures out which lane should be turned green, and tell the specific lane signalController
 * 
 * @author menie
 *
 */
public class AdaptiveTrafficSignalSuperviser {
	private static Map<Integer, AdaptiveSignalController> signalControllers = new HashMap<Integer, AdaptiveSignalController>();
	private static double redPhaseLength = 20;
	private static double postpone = 5;
	private static double greenPhaseLength = 15;
	
	public static SignalController addTrafficSignalController(Lane lane) {
		AdaptiveSignalController signalController = new AdaptiveSignalController();
		signalControllers.put(lane.getId(), signalController);
		
		return signalController;
	}
	
	public static double getRedPhaseLength() {
		return redPhaseLength;
	}
	
	public static double getGreenPhaseLength() {
		return greenPhaseLength;
	}
	
	public static double getPhaseLength() {
		return redPhaseLength + greenPhaseLength;
	}
	
	/**
	 * During a certain time, this would observe the oncoming human-driven traffic
	 * and turn on corresponding traffic light. 
	 */
	public static void runGreenLight(double currentTime) {
		Map<Road, Integer> numDriversWaiting = new HashMap<Road, Integer>();
		Map<Integer,VehicleSimView> vinToVehicles = Resources.vinToVehicles;
		
		// find out the number of human vehicles waiting in each ROAD
		for (VehicleSimView vehicle: vinToVehicles.values()) {
			Driver driver = vehicle.getDriver();
			
			// if the human is not approaching intersection
		  if (driver.getState() != State.V2I_CLEARING && vehicle.isHuman()) {
		  	Lane lane = driver.getCurrentLane();
		  	Road road = Resources.map.getRoad(lane);
		  	
		  	// number of vehicles waiting +1
		  	if (numDriversWaiting.keySet().contains(road)) {
		  		numDriversWaiting.put(road, numDriversWaiting.get(road) + 1);
		  	}
		  	else {
		  		numDriversWaiting.put(road, 1);
		  	}
		  }
		}
		
		Map.Entry<Road, Integer> maxEntry = null;
		
		for (Map.Entry<Road, Integer> entry: numDriversWaiting.entrySet()) {
			if (maxEntry == null || entry.getValue() > maxEntry.getValue()) {
				maxEntry = entry;
			}
		}
		
		if (maxEntry != null) {
			// only apply when there are human vehicles appearing
			for (Lane lane : maxEntry.getKey().getLanes()) {
				double startTime = currentTime + postpone;
				double endTime = currentTime + postpone + greenPhaseLength;
				signalControllers.get(lane.getId()).prepareGreenPhase(startTime, endTime);
			}
		}
	}
}
