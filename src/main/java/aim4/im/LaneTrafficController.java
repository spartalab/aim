package aim4.im;

import java.util.HashMap;
import java.util.Map;

import aim4.config.Resources;
import aim4.config.SimConfig;
import aim4.config.SimConfig.VEHICLE_TYPE;
import aim4.util.Util;

public abstract class LaneTrafficController {
	// laneId -> (VEHICLE_TYPE -> trafficLevel)
	protected Map<Integer, LaneInfo> trafficSpawnInfoList;

	// number of total lanes
	protected int laneNum;
	
	public static class SpawnCase {
		private VEHICLE_TYPE vehicleType;
		private boolean spawned;
		
		public SpawnCase() {
			spawned = false;
		}
		
		public SpawnCase(VEHICLE_TYPE vehicleType) {
			spawned = true;
			this.vehicleType = vehicleType; 
		}
		
		public boolean vehicleSpawned() {
			return spawned;
		}
		
		public VEHICLE_TYPE getVehicleType() {
			return vehicleType;
		}
	}
	
	/**
	 * This class describes information about how much to spawn and what to spawn in a certain
	 * traffic spawning point.
	 * 
	 * @author menie
	 *
	 */
	public static class LaneInfo {
		// useful info
		protected double autoPercent;
		protected double scPercent;
		protected double acPercent;
		protected double humanPercent;
		protected double ihPercent;
		
		LaneInfo(double trafficLevel, double humanPercentage, double ihPercentage,
				double scPercentage, double acPercentage) {
			double timeStep = SimConfig.SPAWN_TIME_STEP;
			
			humanPercent = timeStep * trafficLevel * humanPercentage;
			ihPercent = timeStep * trafficLevel * ihPercentage;
			scPercent = timeStep * trafficLevel * scPercentage;
			acPercent = timeStep * trafficLevel * acPercentage;
			
			autoPercent = timeStep * trafficLevel * (1 - humanPercentage - ihPercentage - scPercentage - acPercentage);
		}
		
		/**
		 * Spawn vehicle according to the current traffic level.
		 * @return a SpawnCase object. empty if nothing generated.
		 */
		public SpawnCase getSpawnVehicle() {
			double rand = Util.random.nextDouble();
			
			if (rand < autoPercent) {
				return new SpawnCase(VEHICLE_TYPE.AUTO);
			}
			else if (rand < autoPercent + humanPercent) {
				return new SpawnCase(VEHICLE_TYPE.HUMAN);
			}
			else if (rand < autoPercent + humanPercent + scPercent) {
				return new SpawnCase(VEHICLE_TYPE.CRUISE);
			}
			else if (rand < autoPercent + humanPercent + scPercent + acPercent) {
				return new SpawnCase(VEHICLE_TYPE.ADAPTIVE_CRUISE);
			}
			else if (rand < autoPercent + humanPercent + scPercent + acPercent + ihPercent) {
				return new SpawnCase(VEHICLE_TYPE.INFORMED_HUMAN);
			}
			else {
				// return nothing
				return new SpawnCase();
			}
		}
	}
	
	/**
	 * Pass in the average traffic level and the percentage of other types of vehicles.
	 */
	public LaneTrafficController() {
		trafficSpawnInfoList = new HashMap<Integer, LaneInfo>();
		
		laneNum = Resources.map.getLaneRegistry().getValues().size();
	}
	
	public LaneInfo getLaneInfo(int laneId) {
		return trafficSpawnInfoList.get(laneId);
	}
}