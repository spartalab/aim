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
			double cstPercentage, double adhPercentage, TrafficVolume trafficVolume) {
		super();
		
		// Surely, the sume of these percentage cannot be more than 1.
		// Constitution of vehicles: auto, human, constant_velocity, adaptive_cruise_control
		if (humanPercentage + cstPercentage + adhPercentage > 1) {
			try {
				throw new Exception("Percentage error! Sum of them exceeds 1.");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		// build up traffic volume for each case
		double totalVolume = 0;
		for (int laneId = 0; laneId < laneNum; laneId++) {
	  	totalVolume += trafficVolume.getTotalVolume(laneId);
		}
		
		for (int laneId = 0; laneId < laneNum; laneId++) {
                    //Shun's code: less turning left
			//double thisLevel = trafficLevel * laneNum * trafficVolume.getTotalVolume(laneId)/ totalVolume;
                    //Guni's code: same number of vehicles in each lane
                        double thisLevel = trafficLevel;
 			trafficSpawnInfoList.put(laneId, new LaneInfo(thisLevel, humanPercentage, cstPercentage, adhPercentage));
		}
	}
}
