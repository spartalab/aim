package aim4.config;

import java.util.LinkedList;

public class GreenPhaseData {
	/**
	 * the length for green phase can only be one of those 
	 */
	private static double[] _greenPhaseOptions = {10, 20, 40};
	
	/**
	 * When this is assigned, the corresponding combination is chosen.
	 */
	private static int _id;
	
	/**
	 * Data for length of different green phase
	 */
	private static LinkedList<Double> _greenPhases = new LinkedList<Double>();
	
	/**
	 * Switch on or off
	 */
	public static boolean on = false;
	
	/**
	 * Set the id of the green phase combination.
	 * This shoule be passed through TrafficSignalExpr
	 * 
	 * @param id
	 */
	public static void setId(int id) {
		_id = id;
		
		int length = _greenPhaseOptions.length;
		
		_greenPhases.push(_greenPhaseOptions[(int)(id / (length * length))]);
		_greenPhases.push(_greenPhaseOptions[(int)(id / length) % length]);
		_greenPhases.push(_greenPhaseOptions[(int)(id % length)]);
	}
	
	/**
	 * Get the green phases
	 * @return
	 */
	public static double getGreenPhase(int i) {
		return _greenPhases.get(i % _greenPhases.size());
	}
}
