package aim4.config;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import aim4.util.Util;

/**
 * This is the information got from redSignal.csv,
 * in which we describe the best red signal time given human percentage and traffic level.
 * This is for dynamic FCFS-SIGNAL
 * 
 * @author menie
 *
 */
public class RedPhaseData {
	
	/** the format of data stored */
	private static List<Entry> data;
	/** file name for the data file */
	private static String fileName = "redPhase.csv";
	
	/** initial red phase time - no observation at the first place */
	public static double defaultRedPhaseTime = 2;
	
	/**
	 * Constructor
	 */
	public static void readRedPhaseData() {
		data = new ArrayList<Entry>();
		
		// read data from file
		List<String> strs = null;
    try {
      strs = Util.readFileToStrArray(fileName);
    } catch (IOException e) {
      System.err.println("Error: " + e.getMessage());
    }
    if (strs != null) {
    	// parse the strings
    	for(int i=0; i<strs.size(); i++) {
        String[] tokens = strs.get(i).split(",");
        
        double hp = Double.parseDouble(tokens[0]);
        double tl = Double.parseDouble(tokens[1]);
        double rp = Double.parseDouble(tokens[2]);
        
        data.add(new Entry(hp, tl, rp));
    	}
    }
	}
	
	/**
	 * Find out the current red phase time
	 * 
	 * @param hp
	 * @param tl
	 * @return
	 */
	public static double getRedPhase(double hp, double tl) {
		double bestRp = 0; // best red phase
		double minDistance = Double.MAX_VALUE;
		
		for (Entry entry: data) {
			double distance = entry.getDistance(hp, tl);
			if (distance < minDistance) {
				minDistance = distance;
				bestRp = entry.getRedPhase();
			}
		}
		
		if (bestRp == 0) {
			System.err.println("Error occured in get red phase: returning red phase time of 0!");
		}
		
		return bestRp;
	}
}

/**
 * This is one entry got from the csv file.
 * It has human percentage, traffic level, and the corresponding red signal time.
 * @author menie
 *
 */
class Entry {
	/**
	 * Useful variables
	 */
	private double humanPercent;
	private double trafficLevel;
	private double redPhase;
	
	/**
	 * Constructor. Parameters are all read from file.
	 * @param hp
	 * @param tl
	 * @param rs
	 * @return 
	 */
	public Entry(double hp, double tl, double rp) {
		humanPercent = hp;
		trafficLevel = tl;
		redPhase = rp;
	}
	
	/**
	 * The Manhattan distance between the current observation and the data provided in this entry.
	 * Sure, we would pick up the one with the smallest distance to apply.
	 * 
	 * @param hp observed human percentage
	 * @param tl observed traffic level
	 * @return
	 */
	public double getDistance(double hp, double tl) {
		return Math.abs(hp - humanPercent) + Math.abs(tl - trafficLevel);
	}
	
	/**
	 * Get the redSignal of this entry. Nothing surprising.
	 * @return
	 */
	public double getRedPhase() {
		return redPhase;
	}
}