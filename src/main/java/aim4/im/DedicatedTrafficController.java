package aim4.im;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import aim4.config.SimConfig;
import aim4.config.SimConfig.VEHICLE_TYPE;
import aim4.im.LaneTrafficController.LaneInfo;
import aim4.util.Util;

/**
 * This would have dedicated lanes for some vehicles.
 * Currently, I'll hard cold the traffic level and vehicle type of the spawning vehicle.
 * TODO read from file?
 * 
 * @author menie
 *
 */
public class DedicatedTrafficController extends LaneTrafficController {
	
	// FIXME the percentage would be incorrect
	public DedicatedTrafficController(double trafficLevel,
			double humanPercentage, double ihdPercentage) {
		super();
		
		int humanOccupation = SimConfig.DEDICATED_LANES;
		
		double humanLaneLevel = trafficLevel * humanPercentage * (1 - ihdPercentage)
				* 12 / (4 * humanOccupation);
		double nonHumanLaneLevel = trafficLevel * (1 - humanPercentage * (1 - ihdPercentage))
				* 12 / (4 * (laneNum / 4 - humanOccupation));
		
		for (int laneId = 0; laneId < laneNum; laneId++) {
			if (laneId % 3 < SimConfig.DEDICATED_LANES) {
				trafficSpawnInfoList.put(laneId, new LaneInfo(humanLaneLevel, 1, 0, 0));
			}
			else {
				trafficSpawnInfoList.put(laneId, new LaneInfo(nonHumanLaneLevel, humanPercentage * ihdPercentage, 1, 0));
			}
		}
	}
}
