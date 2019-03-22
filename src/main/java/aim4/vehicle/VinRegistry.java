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
package aim4.vehicle;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

import aim4.map.Road;
import aim4.map.SpawnPoint;

/**
 * The Vehicle Registry, the class that issues VIN to vehicles.
 */
public class VinRegistry {

  /////////////////////////////////
  // PRIVATE FIELDS
  /////////////////////////////////

  /**
   * This generates a unique identifier for each vehicle, starting with 1000.
   */
  private static int vinGenerator = 1000;

  /**
   * A map from VINs to Vehicles.
   */
  private static Map<Integer,WeakReference<VehicleSimView>> vinToVehicle =
    new HashMap<Integer,WeakReference<VehicleSimView>>();

  /**
   * A map from VINs to VehicleSpec.
   */
  private static Map<Integer,VehicleSpec> vinToVehicleSpec =
    new HashMap<Integer,VehicleSpec>();

  // TODO: remove the following in the future

  /**
   * A map from VINs to spawn points.
   */
  private static Map<Integer,SpawnPoint> vinToSpawnPoint =
    new HashMap<Integer,SpawnPoint>();

  /**
   * A map from VINs to destination roads.
   */
  private static Map<Integer,Road> vinToDestRoad =
    new HashMap<Integer,Road>();


  /////////////////////////////////
  // PUBLIC METHODS
  /////////////////////////////////

  /**
   * Reset the registry.
   */
  public static void reset() {
    vinGenerator = 1000;
    vinToVehicle = new HashMap<Integer,WeakReference<VehicleSimView>>();
    vinToVehicleSpec = new HashMap<Integer,VehicleSpec>();
    vinToSpawnPoint = new HashMap<Integer,SpawnPoint>();
    vinToDestRoad = new HashMap<Integer,Road>();
  }

  /**
   * Put the vehicle to the registry.
   *
   * @param vehicle  the vehicle
   * @return  a new VIN for the vehicle
   */
  public static int registerVehicle(VehicleSimView vehicle) {
    assert vinToVehicle.get(vinGenerator) == null;
    int vin = vinGenerator;
    vinToVehicle.put(vin, new WeakReference<VehicleSimView>(vehicle));
    vinToVehicleSpec.put(vin, vehicle.getSpec());
    vinToSpawnPoint.put(vin, vehicle.getDriver().getSpawnPoint());
    vinToDestRoad.put(vin, vehicle.getDriver().getDestination());

    vehicle.setVIN(vin);

    vinGenerator++;
    return vin;
  }

  /**
   * Register the vehicle with an existing VIN.
   *
   * @param vehicle  the vehicle
   * @param vin      the given VIN
   * @return true if the VIN has not been issued to other vehicle; false if
   *         the VIN has been used by other vehicle.
   */
  public static boolean registerVehicleWithExistingVIN(VehicleSimView vehicle,
                                                       int vin) {
    assert vin >= 0;
    if (vinToVehicle.containsKey(vin)) {
      return false;  // the VIN has been used by some other vehicle
    } else {
      assert vehicle.getVIN() < 0;

      vinToVehicle.put(vin, new WeakReference<VehicleSimView>(vehicle));
      vinToVehicleSpec.put(vin, vehicle.getSpec());
      // TODO: think how to resolve the problem.
      if (vehicle.getDriver() != null) {
        vinToSpawnPoint.put(vin, vehicle.getDriver().getSpawnPoint());
        vinToDestRoad.put(vin, vehicle.getDriver().getDestination());
      } else {
        vinToSpawnPoint.put(vin, null);
        vinToDestRoad.put(vin, null);
      }

      vehicle.setVIN(vin);
      if (vin >= vinGenerator) {
        vinGenerator = vin + 1;
      }  // else vin < vinGenerator and it would not affect the next vehicle
      return true;
    }
  }

  /**
   * Remove the vehicle from the registry.
   * This function should only be used by BasicVehicle.java
   *
   * @param vin  the VIN of the vehicle
   */
  public static void unregisterVehicle(int vin) {
    if (vinToVehicle.containsKey(vin)) {
      vinToVehicle.remove(vin);
      // do not remove the following
//      vinToVehicleSpec.remove(vin);
//      vinToSpawnPoint.remove(vin);
//      vinToDestRoad.remove(vin);
    } else {
      throw new RuntimeException("VehicleRegistry:unregisterVehicle: " +
                                 "Cannot unregister a vehicle twice");
    }
  }

  /**
   * Whether or not the VIN has been issued.
   *
   * @param vin  the VIN of the vehicle
   * @return whether of not the VIN has been issued.
   */
  public static boolean isVINexist(int vin) {
    return vinToVehicleSpec.containsKey(vin);
  }

  /**
   * Given a VIN, get the vehicle with that VIN.
   *
   * @param vin the VIN of the desired vehicle
   * @return the corresponding vehicle object; null if the vehicle object
   *         has been destroyed.
   */
  public static VehicleSimView getVehicleFromVIN(int vin) {
    WeakReference<VehicleSimView> wr = vinToVehicle.get(vin);
    if(wr == null) {
      return null;
    }
    // Unwrap the reference
    VehicleSimView v = wr.get();
    // If it's null, then the Vehicle no longer exists
    if(v == null) {
      vinToVehicle.remove(vin);
    }
    return v;
  }

  /**
   * Given a VIN, get the vehicle specification with that VIN.
   *
   * @param vin  the VIN of the desired vehicle
   * @return the corresponding vehicle specification
   */
  public static VehicleSpec getVehicleSpecFromVIN(int vin) {
    return vinToVehicleSpec.get(vin);
  }

  /**
   * Get a spawn point from the VIN of a vehicle
   *
   * @param vin  the VIN of the vehicle
   * @return the spawn point
   */
  public static SpawnPoint getSpawnPointFromVIN(int vin) {
    return vinToSpawnPoint.get(vin);
  }

  /**
   * Get the destination road from the VIN of a vehicle
   *
   * @param vin  the VIN of the vehicle
   * @return the destination road
   */
  public static Road getDestRoadFromVIN(int vin) {
    return vinToDestRoad.get(vin);
  }

  /////////////////////////////////
  // CONSTRUCTORS
  /////////////////////////////////

  /**
   * This class should never be instantiated.
   */
  private VinRegistry(){};

}
