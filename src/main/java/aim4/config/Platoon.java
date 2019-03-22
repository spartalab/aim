package aim4.config;

import aim4.util.Util;

/**
 * This is to do the expr on platooning human driven vehicles.
 * 
 * @author menie
 *
 */
public class Platoon {
  /**
   * In the spawning point, whether the vehicles are generated in platooning
   */
  public static boolean platooning = false;
  
  /**
   * How many platooned vehicles are supposed to spawned 
   */
  public static int vehiclesNumExpection = 5;
  
  /**
   * Return the vehicles generated based on E(vehiclesNum)
   * @return
   */
  public static int generateVehiclesNum() {
  	Double rand = Util.random.nextDouble();
  	int offset;
  	
  	if (rand < .68) {
  		offset = 0;
  	}
  	else if (rand < .95) {
  		offset = 1;
  	}
  	else {
  		offset = 2;
  	}
  	
  	// Decide left side or right side
  	if (Util.random.nextDouble() > .5) {
  		offset *= -1;
  	}
  	
  	return vehiclesNumExpection + offset;
  }
}
