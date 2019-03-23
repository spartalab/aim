/*
Copyright (c) 2011 Tsz-Chiu Au, Peter Stone
University of Texas at Austin
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

1. Redistributions of source code must retain the above copyright notice, this
list of conditions and the following disclaimer.

2. Redistributions in binary form must reproduce the above copyright notice,
this list of conditions and the following disclaimer in the documentation
and/or other materials provided with the distribution.

3. Neither the name of the University of Texas at Austin nor the names of its
contributors may be used to endorse or promote products derived from this
software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
package aim4.driver.coordinator;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Queue;

import aim4.config.Debug;
import aim4.config.SimConfig;
import aim4.config.Constants.TurnDirection;
import aim4.driver.AutoDriver;
import aim4.driver.AutoDriverCoordinatorView;
import aim4.driver.DriverUtil;
import aim4.driver.navigator.BasicNavigator;
import aim4.driver.navigator.Navigator;
import aim4.driver.pilot.V2IPilot;
import aim4.im.IntersectionManager;
import aim4.im.v2i.V2IManager;
import aim4.map.BasicMap;
import aim4.map.Road;
import aim4.map.lane.Lane;
import aim4.msg.i2v.Confirm;
import aim4.msg.i2v.I2VMessage;
import aim4.msg.i2v.Reject;
import aim4.msg.v2i.Away;
import aim4.msg.v2i.Cancel;
import aim4.msg.v2i.Done;
import aim4.msg.v2i.Request;
import aim4.util.Util;
import aim4.vehicle.AccelSchedule;
import aim4.vehicle.AutoVehicleDriverView;
import aim4.vehicle.VehicleUtil;

/**
 * An agent that autonomously controls the coordination of a
 * {@link AutoVehicleDriverView} with other Vehicles and with
 * {@link IntersectionManager}s. This agent is capable of both V2V and
 * V2I coordination, and uses the current readings of
 * the vehicle and state in the CoordinatingDriverAgent, along with an
 * optimism/pessimism heuristic to make reservations. It also
 * alters the state of the CoordinatingDriverAgent of which it is a part to
 * reflect the current reservation status.
 */
public class V2ICoordinator implements Coordinator {

  /////////////////////////////////
  // CONSTANTS
  /////////////////////////////////

  /**
   * The maximum amount of error in the clock of the vehicle. {@value} seconds.
   */
  private static final double MAX_CLOCK_ERROR = 0.5;

  /**
   * The maximum amount of time, in seconds, in the future, for which the
   * policy will accept reservation requests. This value
   * should be roughly the same as the corresponding value in the IM.
   */
  private static final double MAXIMUM_FUTURE_RESERVATION_TIME =
    V2IManager.MAXIMUM_FUTURE_RESERVATION_TIME - MAX_CLOCK_ERROR;

  /**
   * The precision at which the arrival velocity is considered valid.
   * TODO: it should be part of the confirmation message
   */
  private static final double ARRIVAL_VELOCITY_PRECISION = 3.0;

  /**
   * The minimum amount of time, in seconds, in the future for which the
   * Coordinator will attempt to make a reservation. This is needed because a
   * reservation cannot be made for <i>right now</i>&mdash;it will take time
   * for the request to be sent, processed, and returned. {@value} seconds.
   */
  private static final double MINIMUM_FUTURE_RESERVATION_TIME = 0.1;

  /**
   * The maximum number of Lanes from each Road that the Coordinator will
   * include in its request message when it approaches an intersection.
   * Set at {@value}.
   */
  private static final int MAX_LANES_TO_TRY_PER_ROAD = 1;

  /**
   * The maximum amount of time, in seconds, after sending a request that the
   * Coordinator will wait before giving up and trying again.  If it is less
   * than zero, the vehicle will wait for the request forever. {@value}
   * seconds.
   */
  private static final double REQUEST_TIMEOUT = -1.0;

  /**
   * The delay of sending another request message if the previous
   * preparation for sending a request message is failed.
   */
  private static final double SENDING_REQUEST_DELAY = 0.02;

  /**
   * The delay of the consideration of lane changing if the previous
   * lane changing process is failed.
   */
  private static final double CONSIDERING_LANE_CHANGE_DELAY = 1.0;

  /**
   * The maximum expected time that IM needs to reply a request message.
   */
  private static final double MAX_EXPECTED_IM_REPLY_TIME = 0.04;

  /**
   * The slight reduction of the acceleration of the vehicle
   * when computing an estimation of arrival time and velocity.
   */
  private static final double ARRIVAL_ESTIMATE_ACCEL_SLACK = 1.0;

  /////////////////////////////////
  // NESTED CLASSES
  /////////////////////////////////

  /**
   * An interface of the state handler.
   */
  private static interface StateHandler {
    /**
     * Perform the action defined by the state handler at the driver state.
     *
     * @return true if the driver agent should proceed to the next action
     * immediately.
     */
    boolean perform();
  }

  /**
   * The terminal state handler.
   */
  private static StateHandler terminalStateHandler =
    new StateHandler() {
      @Override
      public boolean perform() {
        return false;  // do nothing, not even the pilot
      }
    };


  /**
   * Potential states that a CoordinatingDriverAgent can be in.  This is one
   * aspect of how the two subagents, the Pilot and the Coordinator,
   * communicate.
   */
  public enum State {
    /**
     * The agent is planning what to do next
     */
    V2I_PLANNING,
    /**
     * The agent simply follows the current lane and does not enter
     * the intersection
     */
    V2I_DEFAULT_DRIVING_BEHAVIOR,
    /**
     * The agent is considering whether it should change lane.
     */
    V2I_LANE_CHANGE,
    /**
     * The agent is determining what the parameters of the requested
     * reservation will be.
     */
    V2I_PREPARING_RESERVATION,
    /**
     * The agent has sent a reservation request and is awaiting a response
     * from the IntersectionManager.
     */
    V2I_AWAITING_RESPONSE,
    /**
     * The agent has received a confirmation from the IntersectionManager and
     * must now attempt to keep that confirmed reservation.
     */
    V2I_MAINTAINING_RESERVATION,
    /**
     * The agent is crossing the intersection in accordance with the
     * reservation it made with the IntersectionManager.
     */
    V2I_TRAVERSING,
    /**
     * The agent has exited the intersection, but is still in the controlled
     * zone after the intersection.
     */
    V2I_CLEARING,
    /**
     * It signals the end of the interaction with the current V2I intersection
     * manager.
     */
    V2I_TERMINAL_STATE,
  }


  /////////////////////////////////
  // NESTED CLASSES
  /////////////////////////////////

  // lane change controller

  private static class LaneChangeController {

    /////////////////////////////////
    // CONSTANTS
    /////////////////////////////////

    /**
     * The minimum distance to maintain between two vehicles on the same lane
     * during lane changing. {@value} meters.
     */
    private static final double MIN_LANE_CHANGE_FOLLOWING_DISTANCE = 0.5;

    /**
     * The minimum velocity of a vehicle at which lane changing is feasible.
     * {@value} meters per second.
     */
    private static final double MIN_VELOCITY_FOR_LANE_CHANGE = 15.0;

    /**
     * The minimum distance from the next intersection at which lane changing
     * is feasible. {@value} meters.
     */
    private static final double MIN_DIST_FROM_IM_FOR_LANE_CHANGE = 30.0;

    /**
     * The time limit of which the lane changing must initiate after
     * the process has been started.
     */
    private static final double LC_INITIATE_TIME_LIMIT = 3.0;


    /////////////////////////////////
    // NESTED CLASSES
    /////////////////////////////////

    /**
     * States for lane changing.
     */
    private enum State {
      /**
       * The agent is waiting to change to the adjacent lane.
       */
      LC_WAITING_LANE_CHANGE,
      /**
       * The agent is changing to the adjacent lane.
       */
      LC_CHANGING_LANE,
      /**
       * The terminal state
       */
      LC_TERMINAL_STATE,
    }


    /////////////////////////////////
    // PRIVATE FIELDS
    /////////////////////////////////

    /**
     * The current state of the lane changing controller
     */
    private State state;

    /**
     * The turn direction
     */
    private TurnDirection turnDirection;

    /**
     * Whether the lane changing process has begun.
     */
    private boolean hasBegun;

    /**
     * Whether the vehicle should change lane if there is a chance
     */
    private boolean shouldChangeLane;

    /**
     * The time limit of which the lane changing process must initiate
     */
    private double initiateTimeLimit;

    /**
     * Whether the lane changing process is successful
     */
    private boolean isLaneChangeSuccessful;

    /**
     * The state handlers
     */
    private EnumMap<State,StateHandler> stateHandlers;

    /** The Vehicle being coordinated by this coordinator. */
    private AutoVehicleDriverView vehicle;

    /** The driver of which this coordinator is a part. */
    private AutoDriverCoordinatorView driver;

    /** The sub-agent that controls physical manipulation of the vehicle */
    private V2IPilot pilot;

    /** The navigator */
    private Navigator navigator;


    /////////////////////////////////
    // CONSTRUCTORS
    /////////////////////////////////

    /**
     * Create a lane change controller object.
     *
     * @param vehicle    the vehicle
     * @param driver     the driver
     * @param pilot      the pilot
     * @param navigator  the navigator
     */
    public LaneChangeController(AutoVehicleDriverView vehicle,
                                AutoDriverCoordinatorView driver,
                                V2IPilot pilot,
                                Navigator navigator) {
      this.vehicle = vehicle;
      this.driver = driver;
      this.pilot = pilot;
      this.navigator = navigator;

      stateHandlers = new EnumMap<State,StateHandler>(State.class);

      stateHandlers.put(State.LC_WAITING_LANE_CHANGE,
          new LcWaitingLaneChangeStateHandler());
      stateHandlers.put(State.LC_CHANGING_LANE,
          new LcChangingLaneStateHandler());
      stateHandlers.put(State.LC_TERMINAL_STATE, terminalStateHandler);
    }

    /////////////////////////////////
    // PUBLIC METHODS
    /////////////////////////////////

    /**
     * Reset the controller.
     */
    public void reset() {
      turnDirection = getTurnDirection(navigator);

      switch(turnDirection) {
      case STRAIGHT:
        shouldChangeLane = false;
        break;
      case U_TURN:  // U-turn is not allowed yet
        throw new RuntimeException("The car cannot make a U-turn (yet).");
      case LEFT:
        shouldChangeLane = driver.getCurrentLane().hasLeftNeighbor();
        break;
      case RIGHT:
        shouldChangeLane = driver.getCurrentLane().hasRightNeighbor();
        break;
      default:
        throw new RuntimeException("Unknown turn direction.");
      }

      if (shouldChangeLane) {
        // make sure that the vehicle is not too close to the intersection
        shouldChangeLane = driver.distanceToNextIntersection() >=
                           MIN_DIST_FROM_IM_FOR_LANE_CHANGE;
      }

      hasBegun = false;
    }

    /**
     * Check to see whether the vehicle should change lane.
     *
     * @return whether the vehicle should change lane.
     */
    public boolean shouldChangeLaneIfPossible() {
      return shouldChangeLane;
    }

    /**
     * Advance the controller to next step.
     */
    public void step() {
      if (!hasBegun) {
        assert shouldChangeLane;
        vehicle.setVehicleTracking(true);
        if (turnDirection == TurnDirection.LEFT) {
          vehicle.setTargetLaneForVehicleTracking(
              driver.getCurrentLane().getLeftNeighbor());
        } else {  // Right
          vehicle.setTargetLaneForVehicleTracking(
              driver.getCurrentLane().getRightNeighbor());
        }
        initiateTimeLimit = vehicle.gaugeTime() + LC_INITIATE_TIME_LIMIT;
        isLaneChangeSuccessful = false;
        setState(State.LC_WAITING_LANE_CHANGE);
        hasBegun = true;
      } else {
        if (Debug.isPrintDriverStateOfVIN(vehicle.getVIN())) {
          System.err.printf("LaneChangeController state of vin %d: %s\n",
                            vehicle.getVIN(), state);
        }
        boolean shouldContinue = true;
        while(shouldContinue) {
          if (stateHandlers.containsKey(state)) {
            shouldContinue = stateHandlers.get(state).perform();
          } else {
            throw new RuntimeException("Unknown state.");
          }
        }
      }
    }

    /**
     * Interrupt the lane changing process
     *
     * @return Whether or not the interruption is successful.
     */
    public boolean interrupt() {
      switch(state) {
      case LC_WAITING_LANE_CHANGE:
        vehicle.setVehicleTracking(false);  // must turn it off
        isLaneChangeSuccessful = false;
        setState(State.LC_TERMINAL_STATE);
        return true;
      case LC_CHANGING_LANE:
        return false;   // can't interrupt after the lane changing has initiated
      case LC_TERMINAL_STATE:
        throw new RuntimeException("Interrupt lane changing process after " +
                                   "the process has terminated.");
      default:
        throw new RuntimeException("Unknown state.");
      }
    }

    /**
     * Whether the controller is in its terminal state.
     *
     * @return whether the controller is in its terminal state.
     */
    public boolean isTerminated() {
      return state == State.LC_TERMINAL_STATE;
    }

    /**
     * Whether the lane change is successful.
     *
     * @return whether the lane change is successful.
     */
    public boolean isSucceeded() {
      return isLaneChangeSuccessful;
    }


    /////////////////////////////////
    // PRIVATE FIELDS
    /////////////////////////////////

    // turn direction

    /**
     * Get the turning direction.
     *
     * @param navigator  the navigator
     * @return the turning direction.
     */
    private TurnDirection getTurnDirection(Navigator navigator) {
      IntersectionManager im = driver.nextIntersectionManager();
      Lane currentLane = driver.getCurrentLane();
      Road currentRoad = Debug.currentMap.getRoad(currentLane);
      Road departureRoad =
        navigator.navigate(currentRoad, im, driver.getDestination());
      Lane departureLane = departureRoad.getIndexLane();
      return im.getIntersection().calcTurnDirection(currentLane, departureLane);
    }


    // state handlers

    /**
     * The state handler for waiting to lane change.
     */
    private class LcWaitingLaneChangeStateHandler implements StateHandler {

      /**
       * Determine the vehicle can change lane immediately.
       */
      private boolean isLaneChangingOkay() {
        // check whether the vehicle is fast enough for lane changing, and
        // make sure that the vehicle is not too close to the intersection
        if (vehicle.gaugeVelocity() >= MIN_VELOCITY_FOR_LANE_CHANGE &&
            driver.distanceToNextIntersection() >=
              MIN_DIST_FROM_IM_FOR_LANE_CHANGE) {
          double leadDist = DriverUtil.getLeadDistance(vehicle);
          double stopDist =
            VehicleUtil.calcDistanceToStop(
              vehicle.gaugeVelocity(),
              vehicle.getSpec().getMaxDeceleration());

          double d1 = vehicle.getFrontVehicleDistanceSensor().read();
          if (d1 < Math.max(leadDist,stopDist) +
                  MIN_LANE_CHANGE_FOLLOWING_DISTANCE) {
            return false;
          }
          double d2 = vehicle.getRearVehicleDistanceSensor().read();
          if (d2 < vehicle.getSpec().getLength() +
                  MIN_LANE_CHANGE_FOLLOWING_DISTANCE) {
            return false;
          }
          return true;
        } else {
          return false;
        }
      }

      /**
       * {@inheritDoc}
       */
      @Override
      public boolean perform() {
        if (isLaneChangingOkay()) {
          // no more sensing once lane changing begins
          vehicle.setVehicleTracking(false);
          // extend the occupied lanes to the left
          // assume vehicle occupies no more than two lanes
          Lane currentLane = driver.getCurrentLane();
          Lane targetLane;
          if (turnDirection == TurnDirection.LEFT) {
            targetLane = currentLane.getLeftNeighbor();
          } else {  // RIGHT
            targetLane = currentLane.getRightNeighbor();
          }
          driver.setCurrentLane(targetLane);  // set the targetLane immediately
          driver.addCurrentlyOccupiedLane(currentLane);
          // update the driver's state
          setState(State.LC_CHANGING_LANE);
          return true;  // check stopping condition immediately
        } else if (vehicle.gaugeTime() > initiateTimeLimit) { // fail and stop
          // must turn off the sensor
          vehicle.setVehicleTracking(false) ;
          isLaneChangeSuccessful = false;
          setState(State.LC_TERMINAL_STATE);
          return false;
        } else {
          // keep going
          pilot.followCurrentLane();
          pilot.simpleThrottleAction();
          return false;
        }
      }
    }


    /**
     * The state handler for changing lane.
     */
    private class LcChangingLaneStateHandler implements StateHandler {

      @Override
      public boolean perform() {
        // check to see if the vehicle has moved into the target lane
        boolean isDone;
        if (turnDirection == TurnDirection.LEFT) {
          isDone = driver.getCurrentLane().
                     contains(vehicle.gaugeRearRightCornerPoint());
        } else {
          isDone = driver.getCurrentLane().
                     contains(vehicle.gaugeRearLeftCornerPoint());
        }
        // check whether the lane changing process should stop
        if (isDone) {
          // shrink the set of currently occupied lanes.
          driver.setCurrentLane(driver.getCurrentLane());
          // done
          isLaneChangeSuccessful = true;
          setState(State.LC_TERMINAL_STATE);
          return false;
        } else {
          // do nothing; continue lane changing
          pilot.followNewLane();
          pilot.simpleThrottleAction();
          return false;
        }
      }
    }


    /////////////////////////////////
    // PRIVATE METHODS
    /////////////////////////////////

    /**
     * Set the current state of the CoordinatingDriverAgent. This method is
     * primarily used by the Coordinator to let the Pilot know what it should
     * do.
     *
     * @param state the new state of the driver agent
     */
    private void setState(State state) {
      // log("Changing state to " + state.toString());
      if (Debug.isPrintDriverStateOfVIN(vehicle.getVIN())) {
        System.err.printf("vin %d changes state to %s\n",
                          vehicle.getVIN(), state);
      }
      this.state = state;
    }

  }

  /////////////////////////////////
  // NESTED CLASSES
  /////////////////////////////////

  // reservation parameter

  // TODO: may merge with the Proposal class in Request and eventually Confirm

  /**
   * Postprocessing the reservation parameters
   */
  public static class ReservationParameter {

    /**
     * The Lane in which the Vehicle should arrive at the intersection.
     */
    private Lane arrivalLane;

    /**
     * The Lane in which the Vehicle will depart the intersection.
     */
    private Lane departureLane;

    /**
     * The time at which the Vehicle should arrive at the intersection.
     */
    private double arrivalTime;

    /**
     * The allowed amount of time, in seconds before the exact planned arrival
     * time for which the Vehicle is allowed to arrive at the intersection.
     */
    private double earlyError;

    /**
     * The allowed amount of time, in seconds after the exact planned arrival
     * time for which the Vehicle is allowed to arrive at the intersection.
     */
    private double lateError;

    /**
     * The velocity, in meters per second, at which the Vehicle should arrive
     * at the intersection.
     */
    private double arrivalVelocity;

    /**
     * The distance after the intersection that is protected by an Admission
     * Control Zone.
     */
    private double aczDistance;

    /**
     * The list of acceleration/duration pairs the vehicle should use to
     * cross the intersection safely.  If empty or null, the vehicle should
     * accelerate to top speed or the speed limit, whichever is lower.
     */
    private Queue<double[]> accelerationProfile;

    /////////////////////////////////
    // CONSTRUCTORS
    /////////////////////////////////

    /**
     * Create a reservation parameter object
     */
    public ReservationParameter(Confirm msg) {
      this.arrivalLane =
        Debug.currentMap.getLaneRegistry().get(msg.getArrivalLaneID());
      this.departureLane =
        Debug.currentMap.getLaneRegistry().get(msg.getDepartureLaneID());
//      this.arrivalLane = LaneRegistry.getLaneFromId(msg.getArrivalLaneID());
//      this.departureLane = LaneRegistry.getLaneFromId(msg.getDepartureLaneID());
      this.arrivalTime = msg.getArrivalTime();
      this.earlyError = msg.getEarlyError();
      this.lateError = msg.getLateError();
      this.arrivalVelocity = msg.getArrivalVelocity();
      this.aczDistance = msg.getACZDistance();
      this.accelerationProfile = msg.getAccelerationProfile();
    }

    /**
     * Get the Lane in which this driver agent's Vehicle should
     * arrive to comply with the reservation this driver agent is holding. If
     * the driver agent is not holding a reservation, the return value is not
     *  defined.
     *
     * @return the arrival lane for the reservation this driver agent is holding
     */
    public Lane getArrivalLane() {
      return arrivalLane;
    }

    /**
     * Get the Lane in which this driver agent's Vehicle should
     * arrive to comply with the reservation this driver agent is holding. If
     * the driver agent is not holding a reservation, the return value is not
     * defined.
     *
     * @return the departure Lane for the reservation this driver agent is
     *         holding
     */
    public Lane getDepartureLane() {
      return departureLane;
    }

    /**
     * Get the arrival time of the reservation this driver agent is holding. If
     * the driver agent is not holding a reservation, the return value is not
     * defined.
     *
     * @return the arrival time of the reservation this driver agent is holding
     */
    public double getArrivalTime() {
      return arrivalTime;
    }

    /**
     * Get the maximum amount of time, in seconds, before the official arrival
     * time that the driver agent's vehicle can arrive at the intersection, for
     * the current reservation the driver agent is holding.  If the driver agent
     * is not holding a reservation, the return value is undefined.
     *
     * @return the maximum early error for the driver agent's current
     *         reservation
     */
    public double getEarlyError() {
      return earlyError;
    }

    /**
     * Get the maximum amount of time, in seconds, after the official arrival
     * time that the driver agent's vehicle can arrive at the intersection, for
     * the current reservation the driver agent is holding.  If the driver agent
     * is not holding a reservation, the return value is undefined.
     *
     * @return the maximum late error for the driver agent's current
     *         reservation
     */
    public double getLateError() {
      return lateError;
    }

    /**
     * Get the arrival velocity, in meters per second, of the reservation this
     * driver agent is holding. If the driver agent is not holding a
     * reservation, the return value is not defined.
     *
     * @return the arrival velocity of the reservation this driver agent is
     *         holding
     */
    public double getArrivalVelocity() {
      return arrivalVelocity;
    }

    /**
     * Get the distance past the intersection which is controlled by the
     * Admission Control Zone after the intersection for the reservation this
     * driver agent is holding.
     *
     * @return the distance of the Admission Control Zone after the intersection
     *         for the reservation this driver agent is holding
     */
    public double getACZDistance() {
      return aczDistance;
    }

    /**
     * Get the list of acceleration/duration pairs that describe the required
     * velocity profile of the driver agent's Vehicle as it crosses the
     * intersection in accordance with its current reservation.  If the driver
     * agent is not holding a reservation, the return value is not defined.
     *
     * @return the acceleration profile of the reservation this driver agent
     *         is currently holding
     */
    public Queue<double[]> getAccelerationProfile() {
      return accelerationProfile;
    }

  }

  /////////////////////////////////
  // PRIVATE FIELDS
  /////////////////////////////////

  // vehicle and agents

  /** The Vehicle being coordinated by this coordinator. */
  private AutoVehicleDriverView vehicle;

  /** The driver of which this coordinator is a part. */
  private AutoDriverCoordinatorView driver;

  /** The navigator that will choose which way to go. */
  private Navigator navigator;

  /** The sub-agent that controls physical manipulation of the vehicle */
  private V2IPilot pilot;

  /** The lane changing controller */
  private LaneChangeController lcController;


  // state

  /**
   * The current state of the agent. This is part of how the two sub-agents
   * communicate.
   */
  private State state;

  /**
   * The most recent time at which the state was changed.
   */
  private double lastStateChangeTime = 0.0;

  /**
   * The state handlers
   */
  private EnumMap<State,StateHandler> stateHandlers;


  // Communication

  /**
   * The reservation parameter
   */
  private ReservationParameter rparameter;

  /**
   * The ID number of the latest reservation the agent has received a
   * confirmation for from the IntersectionManager.
   */
  private int latestReservationNumber;

  /**
   * The next request Id
   */
  private int nextRequestId;

  /**
   * The next time at which the vehicle is allowed to send out request messages
   */
  private double nextAllowedSendingRequestTime;

  /**
   * The next time at which the vehicle is allowed to consider lane changing.
   */
  private double nextAllowedConsideringLaneChangeTime;

  // Debugging

  /**
   * Whether or not this vehicle is being targeted for debugging purpose
   */
  private boolean isDebugging;


  /////////////////////////////////
  // CONSTRUCTORS
  /////////////////////////////////

  /**
   * Create an AutonomousCoordinator to coordinate a Vehicle.
   *
   * @param vehicle  the Vehicle to coordinate
   * @param driver   the driver
   * @param basicMap the map
   */
  public V2ICoordinator(AutoVehicleDriverView vehicle,
                        AutoDriver driver,
                        BasicMap basicMap) {
    this.vehicle = vehicle;
    this.driver = driver;
    this.pilot = new V2IPilot(vehicle, driver);
    this.navigator = new BasicNavigator(vehicle.getSpec(), basicMap);

    isDebugging = Debug.isTargetVIN(vehicle.getVIN());

    lcController = new LaneChangeController(vehicle, driver, pilot, navigator);

    initStateHandlers();

    assert (driver.nextIntersectionManager() != null);

    // We don't have a reservation yet
    rparameter = null;
    // We should be allowed to transmit now
    nextAllowedSendingRequestTime = vehicle.gaugeTime();
    // We should be allowed to consider lane changing now
    nextAllowedConsideringLaneChangeTime = vehicle.gaugeTime();
    // Reset our counter for the latest reservation number so that
    // we won't ignore future reservations
    latestReservationNumber = -1;
    // next request id is 0
    nextRequestId = 0;

    // Set the intial state
    setState(State.V2I_PLANNING);
  }


  /////////////////////////////////
  // PUBLIC METHODS
  /////////////////////////////////

  /**
   * Receive, process, and send messages between Vehicles and
   * IntersectionManagers, and maintain the reservation status in
   * the Vehicle.
   */
  @Override
  public void act() {
    isDebugging = Debug.isTargetVIN(vehicle.getVIN());

    // process the messages
    messageHandler();
    // call state handlers (and generate outgoing messages)
    callStateHandlers();
  }


  /**
   * Process the message in the inbox.
   */
  private void messageHandler() {
    // the incoming message queue one by one
    List<I2VMessage> msgs = vehicle.pollAllMessagesFromI2VInbox();
    for(I2VMessage msg : msgs) {
      // interpret the message (and potentially change the state)
      processMessages(msg);
    }
  }

  /**
   * The main loop for calling the state handlers
   */
  private void callStateHandlers() {
    boolean shouldContinue = true;
    while(shouldContinue) {
      if (stateHandlers.containsKey(state)) {
        shouldContinue = stateHandlers.get(state).perform();
      } else {
        throw new RuntimeException("Unknown state.");
      }
    }
  }

  // state

  /**
   * Get the current state of the CoordinatingDriverAgent.
   *
   * @return the current state of the driver agent
   */
  public State getState() {
    return state;
  }

  /**
   * Whether of not the coordinator has finished its job.
   */
  @Override
  public boolean isTerminated() {
    return state == State.V2I_TERMINAL_STATE;
  }

  /**
   * Whether or not the DriverAgent is waiting for a response from the
   * Intersection Manager.
   *
   * @return whether or not this DriverAgent is waiting for a response from the
   *         Intersection Manager.
   */
  public boolean isAwaitingResponse() {
    return state == State.V2I_AWAITING_RESPONSE;
  }

  // for pilot

  /**
   * Get the confirm message for this driver agent's reservation
   *
   * @return the confirm message; null if there is no confirm message
   */
  public ReservationParameter getReservationParameter() {
    return rparameter;
  }

  /**
   * Calculate the amount of time, in seconds, until the reservation's arrival
   * time.
   *
   * @return the amount of time, in seconds, until the reserved arrival time;
   *         -1.0 if there is no reservation
   */
  public double timeToReservation() {
    if (rparameter != null) {
      return rparameter.getArrivalTime() - vehicle.gaugeTime();
    } else {
      return -1.0;
    }
  }


  // debug

  /**
   * Whether the coordinator is in a debug mode
   *
   * @return Whether the coordinator is in a debug mode
   */
  public boolean getIsDebugging() {
    return isDebugging;
  }

  /////////////////////////////////
  // PRIVATE FIELDS
  /////////////////////////////////

  // messages processing

  /**
   * Called every time {@link #act()} is called, to process any waiting
   * messages.
   */
  private void processMessages(I2VMessage msg) {
    switch(msg.getMessageType()) {
    case CONFIRM:
      processConfirmMessage((Confirm)msg);
      break;
    case REJECT:
      processRejectMessage((Reject)msg);
      break;
    case ACZ_CONFIRM:
      // FIXME not implemented yet
      throw new RuntimeException("Not implemented yet: " +
                                  msg.getMessageType().toString());
      // break;
    case ACZ_REJECT:
      // FIXME not implemented yet
      throw new RuntimeException("Not implemented yet: " +
                                  msg.getMessageType().toString());
    }
  }


  /**
   * Process a received Confirm message.  Sets all the appropriate variables
   * relating to the current reservation in the driver agent, provided that
   * this reservation is newer than any reservation we have or have had in
   * the past.
   *
   * @param msg the Confirm message to process
   */
  private void processConfirmMessage(Confirm msg) {
    switch(state) {
    case V2I_AWAITING_RESPONSE:
      processConfirmMessageForAwaitingResponseState(msg);
      break;
    default:
      System.err.printf("vin %d receives a confirm message when it is not " +
                        "at the V2I_AWAITING_RESPONSE state\n",
                        vehicle.getVIN());
    }
  }

  private void processConfirmMessageForAwaitingResponseState(Confirm msg) {
    // if (msg.getReservationID() > latestReservationNumber) {
    latestReservationNumber = msg.getReservationId();
    // Replace our current reservation stats
    setReservationParameter(msg);

    // Check with pilot to see whether it is feasible to maintain
    // the reservation
    double time1 = vehicle.gaugeTime();
    double v1 = vehicle.gaugeVelocity();
    double timeEnd = rparameter.getArrivalTime();
    double vEnd = rparameter.getArrivalVelocity();
    double dTotal = driver.distanceToNextIntersection();
    double vTop = DriverUtil.calculateMaxFeasibleVelocity(vehicle);
    double accel = vehicle.getSpec().getMaxAcceleration();
    double decel = vehicle.getSpec().getMaxDeceleration();

    if (Debug.isPrintReservationAcceptanceCheck(vehicle.getVIN())) {
      System.err.printf("Parameters for reservation acceptance check:\n");
      System.err.printf("time1   = %.5f\n", time1);
      System.err.printf("v1      = %.5f\n", v1);
      System.err.printf("timeEnd = %.5f\n", timeEnd);
      System.err.printf("vEnd    = %.5f\n", vEnd);
      System.err.printf("dTotal  = %.2f\n", dTotal);
      System.err.printf("vTop    = %.2f\n", vTop);
      System.err.printf("accel   = %.2f\n", accel);
      System.err.printf("decel   = %.2f\n", decel);
    }

    AccelSchedule as = null;
    try {
      as = MaxAccelReservationCheck.check(time1, v1,
                                          timeEnd, vEnd,
                                          dTotal,
                                          vTop,
                                          accel,
                                          decel);
    } catch(ReservationCheckException e) {
      if (isDebugging) {
        System.err.printf("Cancel the reservation because vehicle " +
                          "can't accept the reservation.\n");
        System.err.printf("Reason: %s\n", e.getMessage());
      }
    }
    if (as != null) {
      // Great, it can accelerate to the intersection according to the
      // new acceleration schedule
      if (isDebugging) {
        System.err.printf("accelSchedule = %s\n", as);
      }
      vehicle.setAccelSchedule(as);
      setState(State.V2I_MAINTAINING_RESERVATION);
    } else {
      // must cancel the reservation
      sendCancelMessage(latestReservationNumber);
      removeReservationParameter();
      // remove the acceleration profile.
      vehicle.removeAccelSchedule();
      setState(State.V2I_PLANNING);
      // TODO: think whether it is better to let the state controller to
      // switch state.
    }
  }


  /**
   * Process a received Reject message.  Sets the driver state according to
   * the reason given in the Reject message.  Also handles the case where
   * this is a response to a Request message when we already have a confirmed
   * reservation.
   *
   * @param msg the Reject message to process
   */
  private void processRejectMessage(Reject msg) {
    switch(state) {
    case V2I_AWAITING_RESPONSE:
      processRejectMessageForAwaitingResponseState(msg);
      break;
    default:
      System.err.printf("vin %d receives a reject message when it is not " +
                        "at the V2I_AWAITING_RESPONSE state\n",
                        vehicle.getVIN());
    }
  }


  /**
   * Process the reject message when the vehicle is at the Awaiting Response
   * state.
   *
   * @param msg the reject message.
   */
  private void processRejectMessageForAwaitingResponseState(Reject msg) {
    switch(msg.getReason()) {
    case NO_CLEAR_PATH:
      // normal reason for rejection, just go back to the planning state.
      goBackToPlanningStateUponRejection(msg);
      break;
    case CONFIRMED_ANOTHER_REQUEST:
      // TODO: RETHINK WHAT WE SHOULD DO
      goBackToPlanningStateUponRejection(msg);
      break;
    case BEFORE_NEXT_ALLOWED_COMM:
      throw new RuntimeException("V2ICoordinator: Cannot send reqest "+
                                 "message before the next allowed " +
                                 "communication time");
    case ARRIVAL_TIME_TOO_LARGE:
      System.err.printf("vin %d\n", vehicle.getVIN());
      throw new RuntimeException("V2ICoordinator: cannot make reqest whose "+
                                 "arrival time is too far in the future");
    case ARRIVAL_TIME_TOO_LATE:
      // This means that by the time our message got to IM, the arrival time
      // had already passed.  It indicates an error in the proposal
      // preparation in coordinator.
      throw new RuntimeException("V2ICoordinator: Arrival time of request " +
                                 "has already passed.");
    default:
      System.err.printf("%s\n", msg.getReason());
      throw new RuntimeException("V2ICoordinator: Unknown reason for " +
                                 "rejection.");
    }
  }

  /**
   * Reset the coordinator to the planning state.
   *
   * @param msg the reject message.
   */
  private void goBackToPlanningStateUponRejection(Reject msg) {
    nextAllowedSendingRequestTime =
      Math.max(msg.getNextAllowedCommunication(),
               vehicle.gaugeTime() + SENDING_REQUEST_DELAY);
    vehicle.removeAccelSchedule();
    setState(State.V2I_PLANNING);
  }


  /////////////////////////////////
  // STATE HANDLERS
  /////////////////////////////////

  /**
   * Initialize the state handlers.
   */
  private void initStateHandlers() {
    stateHandlers = new EnumMap<State,StateHandler>(State.class);

    stateHandlers.put(State.V2I_PLANNING,
                      new V2IPlanningStateHandler());

    stateHandlers.put(State.V2I_LANE_CHANGE,
                      new V2ILaneChangeStateHandler());

    stateHandlers.put(State.V2I_DEFAULT_DRIVING_BEHAVIOR,
                      new V2IDefaultDrivingBehaviorStateHandler());

    stateHandlers.put(State.V2I_PREPARING_RESERVATION,
                      new V2IPreparingReservationStateHandler());

    stateHandlers.put(State.V2I_AWAITING_RESPONSE,
                      new V2IAwaitingResponseStateHandler());

    stateHandlers.put(State.V2I_MAINTAINING_RESERVATION,
                      new V2IMaintainingReservationStateHandler());

    stateHandlers.put(State.V2I_TRAVERSING,
                      new V2ITraversingStateHandler());

    stateHandlers.put(State.V2I_CLEARING,
                      new V2IClearingStateHandler());

    stateHandlers.put(State.V2I_TERMINAL_STATE,
                      terminalStateHandler);
  }



  /**
   * The state handler for the planning state.
   */
  private class V2IPlanningStateHandler implements StateHandler {
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean perform() {
      // clean up
      removeReservationParameter();
      // consider lane changing
      if (Debug.CAN_CHANGE_LANE &&
          vehicle.gaugeTime() >= nextAllowedConsideringLaneChangeTime) {
        lcController.reset();
        if (lcController.shouldChangeLaneIfPossible()) {
          setState(State.V2I_LANE_CHANGE);
          return true;
        }  // else fall through
      }  // else fall through
      if (vehicle.gaugeTime() >= nextAllowedSendingRequestTime) {
        if (!SimConfig.MUST_STOP_BEFORE_INTERSECTION ||
            driver.distanceToNextIntersection() <=
            V2IPilot.DEFAULT_STOP_DISTANCE_BEFORE_INTERSECTION +
            SimConfig.ADDITIONAL_STOP_DIST_BEFORE_INTERSECTION) {
          // prepare reservation
          setState(State.V2I_PREPARING_RESERVATION);
          return true;

        }
      }
      // neither lane changing nor reservation making,
      // using default driving behavior
      setState(State.V2I_DEFAULT_DRIVING_BEHAVIOR);
      return true;
    }
  }


  /**
   * The state handler for the lane changing state.
   */
  private class V2ILaneChangeStateHandler implements StateHandler {
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean perform() {
      lcController.step();
      if (lcController.isTerminated()) {
        if (lcController.isSucceeded()) {
          // can immediately consider lane changing
          nextAllowedConsideringLaneChangeTime = vehicle.gaugeTime();
        } else {
          // wait a bit
          nextAllowedConsideringLaneChangeTime =
            vehicle.gaugeTime() + CONSIDERING_LANE_CHANGE_DELAY;
        }
        setState(State.V2I_PLANNING);
        return false;
      } else {
        // no need to invoke pilot because
        // the pilot in lcController will control the vehicle
        return false; // keep going
      }
    }
  }


  /**
   * The state handler for the default driving behavior state.
   */
  private class V2IDefaultDrivingBehaviorStateHandler implements StateHandler {
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean perform() {
      pilot.followCurrentLane();
      pilot.simpleThrottleAction();
      // go back to the planning state in the next time step
      // since we don't want to stop using default driving behavior
      // as soon as possible
      setState(State.V2I_PLANNING);
      return false;
    }
  }

  /**
   * The state handler for the preparing reservation state.
   */
  private class V2IPreparingReservationStateHandler implements StateHandler {

    /**
     * Estimates the arrival parameters at the intersection given a maximum
     * velocity.
     *
     * @param maxArrivalVelocity   the maximum desired arrival velocity
     *
     * @return the estimated arrival parameters at the intersection
     */
    private ArrivalEstimationResult estimateArrival(double maxArrivalVelocity) {
      // The basic parameters
      double time1 = vehicle.gaugeTime();
      double v1 = vehicle.gaugeVelocity();
      double dTotal = driver.distanceToNextIntersection();
      // vTop is equal to max(road's speed limit, vehicle' max speed)
      double vTop = DriverUtil.calculateMaxFeasibleVelocity(vehicle);
      double vEndMax = Math.min(vTop, maxArrivalVelocity);
      double accel = vehicle.getSpec().getMaxAcceleration();
      double decel = vehicle.getSpec().getMaxDeceleration();

      // If the im reply time heuristic is used.
      if (Debug.IS_EXPECTED_IM_REPLY_TIME_CONSIDERED) {
        // If an acceleration schedule exists,
        AccelSchedule estimateToStop = vehicle.getAccelSchedule();
        if (estimateToStop != null) {
          // update the initial time, velocity, and distance to
          // take the expected reply time into account

          double vd[] =
          estimateToStop.calcFinalDistanceAndVelocity(time1, v1, time1
            + MAX_EXPECTED_IM_REPLY_TIME);
          double d2 = vd[0];
          double v2 = vd[1];
          if (d2 <= dTotal) {
            // after MAX_EXPECTED_IM_REPLY_TIME second, the vehicle still hasn't
            // arrive at the intersection, therefore the estimation
            // would start at the reply time.
            time1 += MAX_EXPECTED_IM_REPLY_TIME;
            v1 = v2;
            dTotal -= d2;
          } else {
            // the vehicle arrives at the intersection probably
            // before IM replies,
            throw new RuntimeException("Error in V2ICoordinator::" +
                                       "V2IPreparingReservationStateHandler::" +
                                       "estimateArrival: vehicle should not " +
                                       "have been able to reach the " +
                                       "intersection before the IM reply ");
            // in the future, maybe consider the following
//            vd = estimateToStop.calcFinalTimeAndVelocity(time1, v1, dTotal);
//            assert vd != null;  // because d2 > dTotal
//            time1 += vd[0];
//            v1 = vd[1];
//            dTotal = 0.0;
          }
          // To avoid the numerical errors that a zero velocity
          // becomes negative, fix it to be zero when it is the case.
          if (Util.isDoubleZero(v1)) {
            v1 = 0.0;   // TODO: think how to get rid of this adjustment
          }
        } else { // if there is no acceleration schedule
          if (Util.isDoubleNotZero(v1)) {
            // vehicle is still moving, so use the simple heuristic
            // to make sure that there is enough time for vehicle to
            // arrive at the intersection when checking a confirmation.
            accel -= ARRIVAL_ESTIMATE_ACCEL_SLACK;
            decel += ARRIVAL_ESTIMATE_ACCEL_SLACK;
            if (accel < 0.0) { accel = 0.0; }
            if (decel > 0.0) { decel = 0.0; }
          } else { // else the vehicle has stopped.
            // no need to project the time and distance since the vehicle
            // is not moving, just update the initial time.
            time1 += MAX_EXPECTED_IM_REPLY_TIME;
          }
        }
      } else {  // If we do not use the im reply time heuristic
        // use a simple heuristic to make sure that
        // there is enough time for vehicle to arrive at the intersection
        // when checking a confirmation.
        accel -= ARRIVAL_ESTIMATE_ACCEL_SLACK;
        decel += ARRIVAL_ESTIMATE_ACCEL_SLACK;
        if (accel < 0.0) { accel = 0.0; }
        if (decel > 0.0) { decel = 0.0; }
      }

      if (Debug.isPrintArrivalEstimationParameters(vehicle.getVIN())) {
        System.err.printf("Parameters for arrival estimation:\n");
        System.err.printf("time1   = %.5f\n", time1);
        System.err.printf("v1      = %.5f\n", v1);
        System.err.printf("dTotal  = %.5f\n", dTotal);
        System.err.printf("vTop    = %.5f\n", vTop);
        System.err.printf("vEndMax = %.5f\n", vEndMax);
        System.err.printf("accel   = %.5f\n", accel);
        System.err.printf("decel   = %.5f\n", decel);
      }

      ArrivalEstimationResult result = null;
      try {
        result = VelocityFirstArrivalEstimation
          .estimate(time1, v1, dTotal, vTop, vEndMax, accel, decel);
      } catch(ArrivalEstimationException e) {
        if (isDebugging) {
          System.err.printf("vin %d: arrival estimation failed: %s",
                            vehicle.getVIN(), e.getMessage());
        }
        return null;
      }
      if (isDebugging) {
        System.err.printf("accelSchedule = %s\n", result.getAccelSchedule());
      }
      return result;
    }

    /**
     * Establish the parameters by which the vehicle can traverse the upcoming
     * intersection.  This is used to prepare parameters for both V2I and V2V
     * intersections.
     *
     * @return the parameters by which the vehicle can traverse the upcoming
     *         intersection; null if there is no proposal
     */
    private List<Request.Proposal> prepareProposals() {
      // First establish which departure lanes we are going to try for
      List<Lane> departureLanes = getDepartureLanes();
      int n = departureLanes.size();
      // The next Lane, including the one the Vehicle is in, that will enter an
      // intersection, starting at the point in this Lane nearest the Vehicle's
      // current position.
      Lane l = driver.getCurrentLane().getLaneIM().
                 laneToNextIntersection(vehicle.gaugePosition());
      // Nothing fancy for now, just fill the whole List with the ID of
      // the current Lane
      List<Lane> arrivalLanes = new ArrayList<Lane>(n);
      for(int i = 0; i < n; i++) {
        arrivalLanes.add(l);
      }
      // Calculate what the maximum velocity for each pair of arrival lane
      // and departure lane is
      List<Double> maximumVelocities = new ArrayList<Double>(n);
      // Now, for each configuration...
      for(int i=0; i<n; i++) {
        maximumVelocities.add(
          VehicleUtil.maxTurnVelocity(vehicle.getSpec(),
                                      arrivalLanes.get(i),
                                      departureLanes.get(i),
                                      driver.getCurrentIM()));
      }
      // Now build the Lists of arrival times and velocities
      List<Double> arrivalTimes = new ArrayList<Double>(n);
      List<Double> arrivalVelocities = new ArrayList<Double>(n);

      // Compute the estimated arrival time and velocity
      double minArrivalTime =
        vehicle.gaugeTime() + MINIMUM_FUTURE_RESERVATION_TIME;

      for (int i = 0; i < n; i++) {
        ArrivalEstimationResult result =
          estimateArrival(maximumVelocities.get(i));
        arrivalVelocities.add(result.getArrivalVelocity());
        // Make sure our arrival time is at least a certain amount
        arrivalTimes.add(Math.max(result.getArrivalTime(), minArrivalTime));
      }
      // Convert the arrival and departure lanes to ID numbers and put them
      // in lists to prepare to make the request
      List<Integer> arrivalLaneIDs = new ArrayList<Integer>(n);
      for(Lane arrivalLane : arrivalLanes) {
        arrivalLaneIDs.add(arrivalLane.getId());
      }
      List<Integer> departureLaneIDs = new ArrayList<Integer>(n);
      for(Lane departureLane : departureLanes) {
        departureLaneIDs.add(departureLane.getId());
      }
      // eliminate proposals that are not valid and then return the result.
      List<Request.Proposal> proposals = new ArrayList<Request.Proposal>(n);
      for(int i = 0; i<n; i++) {
        if (arrivalTimes.get(i) <
            vehicle.gaugeTime() + MAXIMUM_FUTURE_RESERVATION_TIME) {
          proposals.add(
            new Request.Proposal(
              arrivalLaneIDs.get(i),
              departureLaneIDs.get(i),
              arrivalTimes.get(i),
              arrivalVelocities.get(i),
              maximumVelocities.get(i)));
        }  // else ignore the proposal because the vehicle is too far away from
           // the intersection.
      }
      if (proposals.size() > 0) {
        return proposals;
      } else {
        return null;
      }
    }

    /**
     * Get a prioritized list of Lanes to try as departure Lanes in the
     * next reservation request. This method attempts to estimate the minimum
     * travel time for each potential departure Road, then selects some number
     * of Lanes from each of the best Roads.
     *
     * @return the List of Lanes to try in the next reservation request
     */
    private List<Lane> getDepartureLanes() {
      List<Lane> departureLanes =
        new ArrayList<Lane>(MAX_LANES_TO_TRY_PER_ROAD);
      Road departureRoad =
        navigator.navigate(Debug.currentMap.getRoad(driver.getCurrentLane()),
                           driver.getCurrentIM(),
                           driver.getDestination());
      // Let's just take the highest priority Lane from each Road
      // Get the prioritized list of Lanes based on the arrival Lane
      List<Lane> lanePriorities =
        driver.getCurrentIM().getSortedDepartureLanes(driver.getCurrentLane(),
                                                      departureRoad);
      // Take at most the first MAX_LANES_TO_TRY_PER_ROAD Lanes from each
      // List
      int numLanesToTry =
        Math.min(MAX_LANES_TO_TRY_PER_ROAD, lanePriorities.size());
      for(int i = 0; i < numLanesToTry; i++) {
        departureLanes.add(lanePriorities.get(i));
      }
      return departureLanes;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean perform() {
      // First, find an acceleration schedule such that the vehicle
      // can stop at the intersection.

      // Vehicles cannot have a prior acceleration schedule
      // TODO: change it later to make it possible to make use of
      // prior acceleration schedule
      if (vehicle.getAccelSchedule() != null) {
        System.err.printf("vin %d should not have an acceleration schedule " +
                          "when it consider preparing a proposal.",
                          vehicle.getVIN());
      }
      assert vehicle.getAccelSchedule() == null;

      AccelSchedule accelScheduleToStop = decelToStopAtIntersection();

      if (accelScheduleToStop != null) {
        vehicle.setAccelSchedule(accelScheduleToStop);
      } else {  // no matter why the vehicle can't stop at the intersection
                // just stop immediately.
        pilot.followCurrentLane();
        vehicle.slowToStop();
        if (isDebugging) {
          double dTotal =
            driver.distanceToNextIntersection()
            - V2IPilot.DEFAULT_STOP_DISTANCE_BEFORE_INTERSECTION;
          if (dTotal < 0.0) {
            System.err.printf("vin %d passed point of no return\n",
                              vehicle.getVIN());
          }
        }
      }

      List<Request.Proposal> proposals = null;
      if (isLaneClearToIntersection()) {
        proposals = prepareProposals();
        if (isDebugging && proposals == null) {
          System.err.printf("At time %.2f, vin %d failed to prepare " +
                            "a proposal: no feasible proposal.\n",
                            vehicle.gaugeTime(),
                            vehicle.getVIN());
        }
      } else { // else some other vehicle is blocking the road
        if (isDebugging) {
          System.err.printf("At time %.2f, vin %d failed to prepare " +
                            "a proposal: other vehicle in front\n",
                            vehicle.gaugeTime(),
                            vehicle.getVIN());
        }
      }
      if (proposals != null) {
        sendRequestMessage(proposals);
        setState(State.V2I_AWAITING_RESPONSE);
        return true;  // let the state controller for V2I_AWAITING_RESPONSE
                      // to control the vehicle.
      } else {
        // In any failure cases, just wait a bit and start all over again.
        nextAllowedSendingRequestTime =
          vehicle.gaugeTime() + SENDING_REQUEST_DELAY; // wait a bit
        setState(State.V2I_PLANNING);
        vehicle.removeAccelSchedule();
        return true; // the use of SENDING_REQUEST_DELAY prevents infinite loop
                     // between V2I_PLANNING and V2I_PREPARING_RESERVATION
      }
    }
  }

  /**
   * The state handler for the awaiting response state.
   */
  private class V2IAwaitingResponseStateHandler implements StateHandler {
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean perform() {
      if (REQUEST_TIMEOUT >= 0.0 && timeSinceStateChange() > REQUEST_TIMEOUT) {
        nextAllowedSendingRequestTime =
          vehicle.gaugeTime() + SENDING_REQUEST_DELAY; // wait a bit
        vehicle.removeAccelSchedule();
        setState(State.V2I_PLANNING);
        return true;  // no infinite loop due to SENDING_REQUEST_DELAY
      } else {
        if (vehicle.getAccelSchedule() != null) {
          // if there is no other vehicle in front
          if (isLaneClearToIntersection()) {
            // keep using the acceleration schedule
            return false;
          } else {
            // if the vehicle in front is too close, stop using the
            // acceleration schedule and use the reactive controller instead
            double stoppingDistance =
              VehicleUtil.calcDistanceToStop(vehicle.gaugeVelocity(),
                vehicle.getSpec().getMaxDeceleration());
            double followingDistance =
              stoppingDistance + V2IPilot.MINIMUM_FOLLOWING_DISTANCE;
            if (VehicleUtil.distanceToCarInFront(vehicle) > followingDistance) {
              // the vehicle in front is far away
              // keep using the acceleration schedule
              return false;
            } else {
              // the vehicle in front is too close
              // stop using acceleration schedule and start using reactive
              // controller
              vehicle.removeAccelSchedule();
              pilot.followCurrentLane();
              pilot.simpleThrottleAction();
              return false;
            }
          }
        } else {
          // using the reactive controller
          pilot.followCurrentLane();
          pilot.simpleThrottleAction();
          return false;
        }
      }
    }
  }

  /**
   * The state handler for the maintaining reservation state.
   */
  private class V2IMaintainingReservationStateHandler implements StateHandler {
    /**
     * Check whether it is possible for the vehicle to arrive at the
     * intersection at the arrival time in accordance with its reservation
     * parameters.
     */
    private boolean checkArrivalTime() {
      // The actual arrival time can be some point in
      //   ( vehicle.gaugeTime()-TIME_STEP, vehicle.gaugeTime() ]
      // The feasible arrival time interval is
      //   [rparameter.getArrivalTime()-rparameter.getEarlyError(),
      //    rparameter.getArrivalTime()-rparameter.getLateError() ]
      // check to see if both intervals intersect.
      double a1 = vehicle.gaugeTime()-SimConfig.TIME_STEP;
      double a2 = vehicle.gaugeTime();
      double b1 = rparameter.getArrivalTime() - rparameter.getEarlyError();
      double b2 = rparameter.getArrivalTime() + rparameter.getLateError();
      // does (a1,a2] intersect [b1,b2]?
      if (a1 < b1 && b1 <= a2) {
        return true;
      } else if (a1 < b2 && b2 <= a2) {
        return true;
      } else if (b1 <= a1 && a2 < b2) {
        return true;
      } else {
        return false;
      }
    }

    /**
     * Check whether it is possible for the vehicle to arrive at the
     * intersection at the arrival velocity in accordance with its reservation
     * parameters.
     */
    private boolean checkArrivalVelocity() {
      // TODO: if the vehicle is already inside the intersection,
      // the arrival velocity may be slightly different.
      // thus this procedure is not correct and need to be removed.
      double v1 = rparameter.getArrivalVelocity();
      double v2 = vehicle.gaugeVelocity();
      return Util.isDoubleEqual(v1, v2, ARRIVAL_VELOCITY_PRECISION);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean perform() {
      // First, determine if we are in the intersection.  If so, then we need
      // to switch to traversal mode.
      if (driver.inCurrentIntersection()) {
        // check whether the arrival time and velocity are correct.
        if (!checkArrivalTime()) {
          System.err.printf("At time %.2f, the arrival time of vin %d is " +
                            "incorrect.\n",
                            vehicle.gaugeTime(),
                            vehicle.getVIN());
          System.err.printf("The arrival time is time %.5f,\n",
                            rparameter.getArrivalTime());
          System.err.printf("but the vehicle arrives at time %.5f\n",
                            vehicle.gaugeTime());
          System.err.printf("distance to next intersection = %.5f\n",
                            vehicle.getDriver().distanceToNextIntersection());
          throw new RuntimeException("The arrival time is incorrect.\n");
        } else if (!checkArrivalVelocity()) {
          System.err.printf("At time %.2f, the arrival velocity of vin %d is " +
                            "incorrect:\n",
                            vehicle.gaugeTime(),
                            vehicle.getVIN());
          throw new RuntimeException("The arrival velocity is incorrect.\n");
        } else {
          // the arrival time and velocity are correct.
          if (isDebugging) {
            System.err.printf("At time %.2f, vin %d, starts traversing\n",
                              vehicle.gaugeTime(), vehicle.getVIN());
          }
          setState(State.V2I_TRAVERSING);
          return true;  // move immediately
        }
      } else {
        // Check to see if the vehicle can still keep up with the acceleration
        // profile.  The only thing to check is whether there is another
        // vehicle blocking the road.
        if (isLaneClearToIntersection()) {
          pilot.followCurrentLane();
          // throttle action is handled by acceleration schedule
          return false;   // everything alright, keep going
        } else {
          if (isDebugging) {
            System.err.printf("vin %d, can't keep up with the accel profile.\n",
                              vehicle.getVIN());
          }
          // must cancel the reservation
          sendCancelMessage(latestReservationNumber);
          removeReservationParameter();
          // remove the acceleration profile.
          vehicle.removeAccelSchedule();
          setState(State.V2I_PLANNING);
          return true; // can start planning right away.
                       // no infinite loop because of the delay IM responses
                       // to our request
        }
      }
    }
  }


  /**
   * The state handler for the traversing state.
   */
  private class V2ITraversingStateHandler implements StateHandler {
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean perform() {
      // Check to see if we are still in the intersection
      if (!driver.inCurrentIntersection()) {
        // The vehicle is out of the intersection.
        if (isDebugging) {
          System.err.printf("Sent done message at time %.2f\n",
                            vehicle.gaugeTime());
        }
        sendDoneMessage(latestReservationNumber);
        // And now get ready to clear
        setState(State.V2I_CLEARING);
        return true;  // can check clearance immediately
      } else {
        // do nothing keep going
        pilot.takeSteeringActionForTraversing(rparameter);
        pilot.followAccelerationProfile(rparameter);
        return false;
      }
    }
  }


  /**
   * The state handler for the clearing state.
   */
  private class V2IClearingStateHandler implements StateHandler {
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean perform() {
      // See if we are beyond the clearing distance
      if (driver.distanceFromPrevIntersection() > rparameter.getACZDistance()) {
        // Inform the intersection that we are away
        sendAwayMessage(latestReservationNumber);

        // Finish
        setState(State.V2I_TERMINAL_STATE);

        pilot.followCurrentLane();    // the last act before termination
        pilot.simpleThrottleAction();
        return false;
      } else {
        // remain in the same state
        pilot.followCurrentLane();
        pilot.simpleThrottleAction();
        return false;
      }
    }
  }


  /////////////////////////////////
  // PRIVATE METHODS
  /////////////////////////////////

  // messaging

  /**
   * Adds a Request message to the outgoing messages.
   *
   * @param proposals  the set of proposals
   */
  private void sendRequestMessage(List<Request.Proposal> proposals) {
    if (isDebugging) {
      System.err.printf("Sending %d proposals:\n", proposals.size());
      for(Request.Proposal p : proposals) {
        System.err.printf("%s\n", p);
      }
    }
    Request rqst =
      new Request(vehicle.getVIN(),  // sourceID
                  driver.getCurrentIM().getId(), // destinationID
                  nextRequestId,
                  new Request.VehicleSpecForRequestMsg(vehicle.getSpec()),
                  proposals);
    // If so, we put the message in the outbox to be delivered to the
    // IntersectionManager
    vehicle.send(rqst);
    nextRequestId++;
  }


  /**
   * Adds a Cancel message for the highest ID number reservation the
   * Coordinator has received so far to the outgoing messages, and addresses
   * it to the upcoming IntersectionManager.
   *
   * @param reservationID   the reservation ID
   */
  private void sendCancelMessage(int reservationID) {
    vehicle.send(new Cancel(vehicle.getVIN(), // sourceID
                            driver.getCurrentIM().getId(), // destinationID
                            reservationID)); // reservationID
  }

  /**
   * Adds a Done message to the outgoing messages, addressed to the current
   * IntersectionManager (even though the vehicle is be past it). This
   * indicates to the IntersectionManager that the vehicle has completed
   * its traversal of the intersection.
   *
   * @param reservationID   the reservation ID
   */
  private void sendDoneMessage(int reservationID) {
    vehicle.send(new Done(vehicle.getVIN(), // sourceID
                          driver.getCurrentIM().getId(),  // destinationID
                          reservationID));  // reservationID
  }

  /**
   * Adds an Away message to the outgoing messages, addressed to the current
   * IntersectionManager (even though the vehicle is be past it). This
   * indicates to the IntersectionManager that the vehicle has gotten far
   * enough away from the intersection to escape the AdmissionControlZone
   * for the Lane in which it is traveling.
   *
   * @param reservationID   the reservation ID
   */
  private void sendAwayMessage(int reservationID) {
    vehicle.send(new Away(vehicle.getVIN(), // sourceID
                          driver.getCurrentIM().getId(),  // destinationID
                          reservationID));  // reservationID
  }


  /////////////////////////////////
  // PRIVATE METHODS
  /////////////////////////////////

  // reservation parameter

  /**
   * Record the confirm message for this driver agent's reservation
   *
   * @param msg  the confirm message
   */
  private void setReservationParameter(Confirm msg) {
    rparameter = new ReservationParameter(msg);
  }

  /**
   * Remove the confirm message for this driver agent's reservation
   */
  private void removeReservationParameter() {
    rparameter = null;
  }

  /**
   * Set the current state of the CoordinatingDriverAgent. This method is
   * primarily used by the Coordinator to let the Pilot know what it should
   * do.
   *
   * @param state the new state of the driver agent
   */
  private void setState(State state) {
    // log("Changing state to " + state.toString());
    if (Debug.isPrintDriverStateOfVIN(vehicle.getVIN())) {
      System.err.printf("At time %.2f, vin %d changes state to %s\n",
                        vehicle.gaugeTime(), vehicle.getVIN(), state);
    }
    this.state = state;
    lastStateChangeTime = vehicle.gaugeTime();
  }

  /**
   * Get the amount of time, in seconds, since the state of this
   * CoordinatingDriverAgent last changed.
   *
   * @return the amount of time, in seconds, since the state of this
   *         CoordinatingDriverAgent last changed
   */
  private double timeSinceStateChange() {
    return vehicle.gaugeTime() - lastStateChangeTime;
  }

  /////////////////////////////////
  // PRIVATE METHODS
  /////////////////////////////////

  // other

  /**
   * Whether or not the lane in front of the vehicle is empty all the way to
   * the intersection.
   *
   * @return whether or not the lane in front of the vehicle is empty all the
   *         way to the intersection
   */
  private boolean isLaneClearToIntersection() {
    // TODO: need to fix this to make it better.
    double d1 = driver.distanceToNextIntersection();
    if (d1 >= Double.MAX_VALUE) return true;  // no intersection
    double d2 = VehicleUtil.distanceToCarInFront(vehicle);
    if (d2 >= Double.MAX_VALUE) return true;  // no car in front
    double d3 = d1 - d2;
    return (d3 <= V2IPilot.DEFAULT_STOP_DISTANCE_BEFORE_INTERSECTION);
  }


  /**
   * Find an acceleration schedule such that the vehicle can stop
   * at the intersection.
   *
   * @return an acceleration schedule such that it can stop at the
   *         intersection; null if either (1) the vehicle is beyond
   *         the point of no return; or (2) the vehicle is too close
   *         to the intersection.
   */
  private AccelSchedule decelToStopAtIntersection() {
    // stop at the buffer distance before intersection
    double dTotal =
      driver.distanceToNextIntersection()
      - V2IPilot.DEFAULT_STOP_DISTANCE_BEFORE_INTERSECTION;

    if (dTotal > 0.0) {
      double time1 = vehicle.gaugeTime();
      double v1 = vehicle.gaugeVelocity();
      double vTop = DriverUtil.calculateMaxFeasibleVelocity(vehicle);
      double vEndMax = 0.0;   // make sure that it stops at the intersection
      double accel = vehicle.getSpec().getMaxAcceleration();
      double decel = vehicle.getSpec().getMaxDeceleration();

      ArrivalEstimationResult result = null;
      try {
        result = aim4.driver.coordinator.VelocityFirstArrivalEstimation
          .estimate(time1, v1, dTotal, vTop, vEndMax, accel, decel);
      } catch(ArrivalEstimationException e) {
        if (isDebugging) {
          System.err.printf("vin %d: arrival estimation in " +
                            "decelToStopAtIntersection() failed: %s",
                            vehicle.getVIN(), e.getMessage());
        }
        return null;
      }
      return result.getAccelSchedule();
    } else {  // already inside the acceleration zone or the intersection
      return null;
    }
  }

}
