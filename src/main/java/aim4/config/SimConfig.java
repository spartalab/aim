package aim4.config;

import java.awt.geom.Point2D;

/**
 * The configuration of a simulation.
 */
public class SimConfig {

	public static enum SIGNAL_TYPE {
		DEFAULT,
		TRADITIONAL,
		RED_PHASE_ADAPTIVE,
		ONE_LANE_VERSION,
		REVISED_PHASE,
		HUMAN_ADAPTIVE
	}

	public static enum VOLUME_TYPE {
		FILE,
		RANDOM
	}

	public static enum VEHICLE_TYPE {
		AUTO,
		HUMAN,
		CRUISE,
		ADAPTIVE_CRUISE,
		HUD
	}

	public static double RED_PHASE_LENGTH = 0;

	/**
	 * The specific type of fcfs_signal, if it's applied
	 */
	public static SIGNAL_TYPE signalType;

	/**
	 * How the traffic volume information is generated.
	 * Generally, it should be read from file.
	 * When doing experiments on best green signal length, this might need randomly generated
	 * to find the best green signal length for different volume.
	 */
	public static VOLUME_TYPE volumeType = VOLUME_TYPE.FILE;

	/**
	 * Whether dedicated lanes are enabled.
	 */
	public static int DEDICATED_LANES = 0;

  /**
   * The time the simulation should run.
   * If it is less than or equal to zero, the simulation will run forever.
   */
  public static double TOTAL_SIMULATION_TIME = -1.0;

  /**
   * The number of cycles per second ({@value}) at which the simulator runs.
   */
  public static final double CYCLES_PER_SECOND = 50.0;

  /**
   * The length of a time step (simulation time) in the simulator
   * ({@value} seconds).
   */
  public static final double TIME_STEP = 1 / CYCLES_PER_SECOND;

  /**
   * The length of a time step (simulation time) in the reservation grid
   * ({@value} seconds).
   */
  public static final double GRID_TIME_STEP = TIME_STEP;

  /**
   * How often the simulator should consider spawning vehicles.
   */
  public static final double SPAWN_TIME_STEP = TIME_STEP / 10;

  /**
   * The portion of human drivers
   * This data should be passed through command line for experiment.
   */
  public static double HUMAN_PERCENTAGE = 0;

  /**
   * These percentage of drivers are told by the IM whether they should slow down or speed up.
   * Sure, this info is inquired only when human_percentage > 0.
   */
  public static double CONSTANT_HUMAN_PERCENTAGE = 0;

  /**
   * The percentage of drivers who can strictly follow the vehicles in front of it.
   */
  public static double ADAPTIVE_HUMAN_PERCENTAGE = 0;

	/**
	 * The percentage of (human) drivers who are following heads-up display (HUD) trajectory.
	 */
	public static double HUD_HUMAN_PERCENTAGE = 0;

  /**
   * Allowing the assumption that the IM can also have the information of the positions of the
   * human-driven vehicles.
   */
  public static boolean FULLY_OBSERVING = true;

  /**
   * times for human of time buffer
   * NOT IN USE.
   */
  public static final double HUMAN_TARDINESS = 2;

  /**
   * This deals with a specific situation in FCFS-SIGNAL.
   * The autonomous vehicles have to check what's going on at time of currentTime+GREEN_LANE_PREDICTION_BUFFER,
   * too see whether it would collides into a human vehicle, coming from a green lanes, which
   * just turned from a red lane.
   */
  public static final int GREEN_LANE_PREDICTION_BUFFER = 3;

  /**
   * when green lights are on in two opposite directions, whether we allow vehicles turning left.
   * otherwise, it's only permitted when light in one road is on.
   */
  public static final boolean ALWAYS_ALLOW_TURNING_LEFT = false;
  /**
   * The longest time that the intersection should wait for human driver to go through.
   * This is the value that should be compared with the simulated time for the driver
   * of course, it cannot be infinite.
   */
  public static final double LONGEST_TIME_TO_WAIT_FOR_HUMAN = 0;

  /**
   * This is for for FCFS-SIGNAL
   * The time that vehicles in the red lanes before it arrives at the intersection
   */
  public static final double TIME_RED_LANE_CAN_PROPOSE = 0.7;

  /**
   * Whether or not the vehicle must stop before an intersection
   */
  public static boolean MUST_STOP_BEFORE_INTERSECTION = false;

  /**
   * The distance before the stopping distance before an intersection
   * such that a vehicle can consider moving again when
   * MUST_STOP_BEFORE_INTERSECTION is true.
   */
  public static final double ADDITIONAL_STOP_DIST_BEFORE_INTERSECTION = 0.01;

  /**
   * If an adaptive vehicle find a vehicle in front of it within such distance, it can follow
   */
	public static final double FOLLOW_DISTANTCE = 15;
}
