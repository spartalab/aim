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
package aim4.config;

import java.awt.Color;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import aim4.gui.ViewerDebugView;
import aim4.map.BasicMap;

/**
 * The configuration for debugging purposes.
 */
public class Debug {

  /**
   * A global variable referring to the global map.
   */
  public static BasicMap currentMap;

  /**
   * A global variable referring to the GUI object.
   */
  public static ViewerDebugView viewer;

  ////////////////////////////////////////////////////////////
  // INFORMATION DISPLAY SETTINGS FOR ONE PARTICULAR VEHICLE
  ////////////////////////////////////////////////////////////

  /**
   * The VIN of the target vehicle for debugging.
   * Set it to -10 when no vehicle is being selected.
   */
  private static int targetVIN = -10;

  /**
   * The ID of the target intersection manager for debugging
   * Set it to -10 when no intersection manager is being selected.
   */
  private static int targetIMid = -10;

  /////////////////////////////////
  // SIMULATOR SETTINGS
  /////////////////////////////////

  /**
   * Whether or not the vehicle is allowed to change lanes
   */
  public static final boolean CAN_CHANGE_LANE = false;

  /**
   * Whether or not the driver considers the expected time the IM takes
   * to reply a request.
   */
  public static final boolean IS_EXPECTED_IM_REPLY_TIME_CONSIDERED = true;


  /////////////////////////////////
  // PUBLIC STATIC METHODS
  /////////////////////////////////

  /**
   * Whether or not the simulator shows the vin of the vehicles on screen.
   */
  public static boolean isTargetVIN(int vin) {
    return vin == targetVIN;
  }

  /**
   * Get the target vehicle's ID
   */
  public static int getTargetVIN() {
    return targetVIN;
  }

  /**
   * Set the target vehicle's ID
   */
  public static void setTargetVIN(int vin) {
    targetVIN = vin;
  }

  /**
   * Remove the target vehicle's ID
   */
  public static void removeTargetVIN() {
    targetVIN = -10;
  }

  /**
   * Get the target intersection manager's ID
   */
  public static int getTargetIMid() {
    return targetIMid;
  }

  /**
   * Set the target intersection manager's ID
   */
  public static void setTargetIMid(int vin) {
    targetIMid = vin;
  }

  /**
   * Remove the target intersection manager's ID
   */
  public static void removeTargetIMid() {
    targetIMid = -10;
  }

  /**
   * Whether or not the simulator print the state of the driver of the target
   * vehicle
   */
  public static boolean isPrintDriverStateOfVIN(int vin) {
    return (vin == targetVIN);
    // return false;
  }

  /**
   * Whether or not the simulator print the state of the target vehicle.
   */
  public static boolean isPrintVehicleStateOfVIN(int vin) {
    // return (vin == targetVIN);
    return false;
  }

  /**
   * Whether or not the simulator print the messages received by IM of the
   * target vehicle.
   */
  public static boolean isPrintIMInboxMessageOfVIN(int vin) {
    // return (vin == targetVIN);
    return false;
  }

  /**
   * Whether or not the simulator print the messages sent by IM of the target
   * vehicle.
   */
  public static boolean isPrintIMOutboxMessageOfVIN(int vin) {
    return (vin == targetVIN);
    // return false;
  }

  /**
   * Whether or not the simulator print the messages received by the target
   * vehicle.
   */
  public static boolean isPrintVehicleInboxMessageOfVIN(int vin) {
    // return (vin == targetVIN);
    return false;
  }

  /**
   * Whether or not the simulator print the messages sent by the target vehicle.
   */
  public static boolean isPrintVehicleOutboxMessageOfVIN(int vin) {
    // return (vin == targetVIN);
    return false;
  }


  /**
   * Whether or not the simulator print the parameters for arrival estimation
   */
  public static boolean isPrintArrivalEstimationParameters(int vin) {
    return (vin == targetVIN);
  }

  /**
   * Whether or not the simulator print the parameters for reservation
   * acceptence check
   */
  public static boolean isPrintReservationAcceptanceCheck(int vin) {
    return (vin == targetVIN);
  }

  /**
   * Whether or not the simulator print the high level control of
   * the target vehicle.
   */
  public static boolean isPrintVehicleHighLevelControlOfVIN(int vin) {
    return false;
  }

  /////////////////////////////////
  // INFORMATION DISPLAY SETTINGS
  /////////////////////////////////

  /**
   * Whether or not the simulator shows the (expected) arrival time of the
   * request (if any) of the vehicles on screen.
   */
  public static final boolean SHOW_ARRIVAL_TIME = false;

  /**
   * Whether or not the simulator shows the (expected) arrival time of the
   * request (if any) of the vehicles minus the current time on screen.
   */
  public static final boolean SHOW_REMAINING_ARRIVAL_TIME = false;

  /**
   * Whether or not the simulator prints out the stage of the simulation in
   * the simulation's main loop.
   */
  public static final boolean PRINT_SIMULATOR_STAGE = false;

  /**
   * Whether or not the proxy vehicle shows the debug message.
   */
  public static final boolean SHOW_PROXY_VEHICLE_DEBUG_MSG = true;

  /**
   * Whether or not to show the PVUpdate message.
   */
  public static final boolean SHOW_PROXY_VEHICLE_PVUPDATE_MSG = false;

  /**
   * Whether or not to print the random seed.
   */
  public static final boolean IS_PRINT_RANDOM_SEED = true;

  // TODO: remove the following later.
  /**
   * Whether to show the vehicle according to its messaging state.
   */
  public static boolean SHOW_VEHICLE_COLOR_BY_MSG_STATE = true;


  /////////////////////////////////
  // Debug Points
  /////////////////////////////////

  /**
   * The list of long term debug points.
   */
  private static List<DebugPoint> longTermDebugPoints =
    new LinkedList<DebugPoint>();

  /**
   * The list of short term debug points.
   */
  private static List<DebugPoint> shortTermDebugPoints =
    new LinkedList<DebugPoint>();

  /**
   * Get the long-term debugging points.
   *
   * @return an list of long-term debug points.
   */
  public static List<DebugPoint> getLongTermDebugPoints() {
    return longTermDebugPoints;
  }

  /**
   * Clear out all the long-term debug points.
   */
  public static void clearLongTermDebugPoints() {
    longTermDebugPoints.clear();
  }

  /**
   * Add a new long-term debug point.
   *
   * @param dp  a new long-term debug point.
   */
  public static void addLongTermDebugPoint(DebugPoint dp) {
    longTermDebugPoints.add(dp);
  }

  /**
   * Get the short-term debugging points
   *
   * @return an list of short-term debug points
   */
  public static List<DebugPoint> getShortTermDebugPoints() {
    return shortTermDebugPoints;
  }

  /**
   * Clear out all the short-term debug points.
   */
  public static void clearShortTermDebugPoints() {
    shortTermDebugPoints.clear();
  }

  /**
   * Add a new short-term debug point.
   *
   * @param dp  a new short-term debug point
   */
  public static void addShortTermDebugPoint(DebugPoint dp) {
    shortTermDebugPoints.add(dp);
  }

  /////////////////////////////////
  // VEHICLE COLORING
  /////////////////////////////////

  /**
   * A mapping from vehicle's VINs to the color of the vehicles.
   */
  private static Map<Integer,Color> vinToVehicleColor =
    new HashMap<Integer,Color>();

  /**
   * Get the color of a vehicle.
   *
   * @param vin the VIN number of the vehicle
   * @return the color of the vehicle
   */
  public static Color getVehicleColor(int vin) {
    return vinToVehicleColor.get(vin);
  }

  /**
   * Set the color of a vehicle.
   *
   * @param vin   the VIN number of the vehicle
   * @param color the color of the vehicle
   */
  public static void setVehicleColor(int vin, Color color) {
    vinToVehicleColor.put(vin, color);
  }

  /**
   * Remove the color assignment of a vehicle.
   *
   * @param vin the VIN number of the vehicle
   */
  public static void removeVehicleColor(int vin) {
    vinToVehicleColor.remove(vin);
  }

  /////////////////////////////////
  // PRIVATE METHODS
  /////////////////////////////////

  /**
   * This private constructor ensures t hat this class is never instantiated.
   */
  private Debug(){};

}
