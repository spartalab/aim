package aim4.im;

import aim4.map.TrafficVolume;

/**
 * For normal traffic spawning. It reads the volume file and decides the corresponding traffic level
 * for that lane.
 * @author menie
 *
 */
public class NormalTrafficController extends LaneTrafficController {

	public NormalTrafficController(double trafficLevel, double humanPercentage,
			double cstPercentage, double adhPercentage, double hudPercentage, TrafficVolume trafficVolume) {
		super();
		
		// Surely, the sum of these percentages cannot be more than 1.
		// Constitution of vehicles: auto, human, constant_velocity, adaptive_cruise_control
		if (humanPercentage + cstPercentage + adhPercentage + hudPercentage > 1) {
			try {
				throw new Exception("Percentage error! Sum of vehicle type percentages exceeds 1.");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		// build up traffic volume for each case
		double totalVolume = 0;
		for (int laneId = 0; laneId < laneNum; laneId++) {
	  	totalVolume += trafficVolume.getTotalVolume(laneId);
		}
		
		for (int laneId = 0; laneId < laneNum; laneId++) {
			double thisLevel = trafficLevel * laneNum * trafficVolume.getTotalVolume(laneId)/ totalVolume;
			trafficSpawnInfoList.put(laneId, new LaneInfo(thisLevel, humanPercentage, cstPercentage, adhPercentage, hudPercentage));
		}
	}
}
