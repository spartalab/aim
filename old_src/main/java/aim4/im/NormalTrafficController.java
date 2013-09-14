package aim4.im;

import aim4.map.TrafficVolume;

/**
 * For normal traffic spawning. It reads the volume file and decides the corresponding traffic level
 * for that lane.
 * @author menie
 *
 */
public class NormalTrafficController extends LaneTrafficController {

	public NormalTrafficController(double trafficLevel, double humanPercentage, double ihPercentage,
			double cstPercentage, double adhPercentage, TrafficVolume trafficVolume) {
		super();
		
		// build up traffic volume for each case
		double totalVolume = 0;
		for (int laneId = 0; laneId < laneNum; laneId++) {
	  	totalVolume += trafficVolume.getTotalVolume(laneId);
		}
		
		for (int laneId = 0; laneId < laneNum; laneId++) {
			double thisLevel = trafficLevel * laneNum * trafficVolume.getTotalVolume(laneId)/ totalVolume;
			trafficSpawnInfoList.put(laneId, new LaneInfo(thisLevel, humanPercentage, ihPercentage, cstPercentage, adhPercentage));
		}
	}
}
