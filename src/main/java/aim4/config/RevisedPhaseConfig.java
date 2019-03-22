package aim4.config;

import aim4.im.v2i.RequestHandler.ApproxNPhasesTrafficSignalRequestHandler.RevisedPhaseSignalController;

/**
 * For Revised Phase version of FCFS-SIGNAL
 * 
 * @author menie
 *
 */
public class RevisedPhaseConfig {
	/**
	 * useful timing variables
	 */
	private static double redTime = 10;
	private static double greenTimeForGoingStraight = 40;
	private static double greenTimeForTurningLeft = 5;
	
	/**
	 * create a controller according to lane id
	 * FIXME nearly hard coded
	 * 
	 * @param laneId
	 * @return greenDuration[0] is the starting time of this lane being green;
	 * greenDuration[1] is the end of its being green time
	 */
	public static RevisedPhaseSignalController getController(int laneId) {
		double totalTime = 2 * greenTimeForGoingStraight + 2 * greenTimeForTurningLeft + 4 * redTime;
		double greenDuration[] = new double[2];
		
		if (laneId % 3 != 0) {
			// This is not turning left lane
			if (laneId < 6) {
				// direction identified
				greenDuration[0] = 0;
				greenDuration[1] = greenTimeForGoingStraight;
			}
			else {
				greenDuration[0] = greenTimeForGoingStraight * 1 + redTime * 1;
				greenDuration[1] = greenTimeForGoingStraight * 2 + redTime * 1;
			}
		}
		else {
			if (laneId < 6) {
				greenDuration[0] = greenTimeForGoingStraight * 2 + redTime * 2;
				greenDuration[1] = greenTimeForGoingStraight * 2 + redTime * 2 + greenTimeForTurningLeft * 1;
			}
			else {
				greenDuration[0] = greenTimeForGoingStraight * 2 + redTime * 3 + greenTimeForTurningLeft * 1;
				greenDuration[1] = greenTimeForGoingStraight * 2 + redTime * 3 + greenTimeForTurningLeft * 2;
			}
		}
		
		return new RevisedPhaseSignalController(greenDuration, totalTime);
	}
}
