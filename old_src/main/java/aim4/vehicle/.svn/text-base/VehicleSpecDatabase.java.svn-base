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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The vehicle specification database.
 */
public class VehicleSpecDatabase {

  /////////////////////////////////
  // PRIVATE FIELDS
  /////////////////////////////////

  /**
   * A list of VehicleSpecs
   */
  private static List<VehicleSpec> vehicleSpecs =
    new ArrayList<VehicleSpec>();

  /**
   * A map from VehicleSpecs' name to VehicleSpecs' id.
   */
  private static Map<String, Integer> nameToId =
    new HashMap<String,Integer>();


  /////////////////////////////////
  // PUBLIC METHODS
  /////////////////////////////////

  /**
   * Get the total number of vehicle specifications in the database
   *
   * @return the total number of vehicle specifications.
   */
  public static int getNumOfSpec() {
    return vehicleSpecs.size();
  }

  /**
   * Register a vehicle specification.  It requires that the name of the
   * given vehicle specification is not the same as the name of any
   * registered vehicle specifications.
   *
   * @param spec  the vehicle specification
   */
  public static void registerSpec(VehicleSpec spec) {
    assert !nameToId.containsKey(spec.getName());
    int id = vehicleSpecs.size();
    nameToId.put(spec.getName(), id);
    vehicleSpecs.add(spec);
  }

  /**
   * Place an registered vehicle spec with a new one.
   *
   * @param spec  the new spec
   * @return true if there exists a spec whose name is the same
   *         as the new one; false if otherwise.
   */
  public static boolean replaceSpec(VehicleSpec spec) {
    if (nameToId.containsKey(spec.getName())) {
      int id = nameToId.get(spec.getName());
      VehicleSpec oldSpec = vehicleSpecs.get(id);
      oldSpec.assign(spec);
      return true;
    } else {
      return false;
    }
  }


  /**
   * Get the id of the vehicle specification.  It returns < 0 if the
   * vehicle specification has not been registered.
   *
   * @param spec  the vehicle specification
   * @return      the id of vehicle specification; -1 if the vehicle
   *              specification has not been registered.
   */
  public static int getIdOfVehicleSpec(VehicleSpec spec) {
    Integer id = nameToId.get(spec.getName());
    return (id!=null)?id:(-1);
  }

  /**
   * Get the id of the vehicle specification.  It returns < 0 if the
   * vehicle specification has not been registered.
   *
   * @param name  the name of the vehicle specification
   * @return      the id of vehicle specification; -1 if the vehicle
   *              specification has not been registered.
   */
  public static int getIdByName(String name) {
    Integer id = nameToId.get(name);
    return (id!=null)?id:(-1);
  }

  /**
   * Get the vehicle specification. It returns null if the vehicle
   * specification id does not exist.
   *
   * @param id  the id of the vehicle specification
   * @return    the vehicle specification; null if the vehicle specification
   *            id does not exist.
   */
  public static VehicleSpec getVehicleSpecById(int id) {
    return vehicleSpecs.get(id);
  }

  /**
   * Get the vehicle specification. It returns null if the vehicle
   * specification id does not exist.
   *
   * @param name  the name of the vehicle specification
   * @return the vehicle specification; null if the vehicle specification
   *         id does not exist.
   */
  public static VehicleSpec getVehicleSpecByName(String name) {
    Integer id = nameToId.get(name);
    return (id != null)?vehicleSpecs.get(id):null;
  }

  /////////////////////////////////
  // STATIC CONSTRUCTORS
  /////////////////////////////////

  /**
   * Create predefined vehicle specification.
   */
  static {
    /** A small vehicle with short wheelbase and good performance. */
    registerSpec(new VehicleSpec("COUPE",
                                 4.5,       // maxAcceleration (m/s/s)
                               -45.0,       // maxDeceleration (m/s/s)
                               //-15.0,       // maxDeceleration (m/s/s)
                                60.0,       // maxVelocity (m/s)
                               -17.0,       // minVelocity (m/s)
                                 4.0,       // length (meters)
                                 1.75,      // width (meters)
                                 1.0,       // frontAxleDisplacement (meters)
                                 3.5,       // rearAxleDisplacement (meters)
                               (1.75-0.25)/2, // wheelSpan (meters)
                                 0.3,       // wheelRadius (meters)
                                 0.25,      // wheelWidth (meters)
                               Math.PI/3,   // maxSteeringAngle (radian)
                               Math.PI/2)); // maxTurnPerSecond (radian)

    /** A medium vehicle with medium wheelbase and moderate performance. */
    registerSpec(new VehicleSpec("SEDAN",
                                 3.25,      // maxAcceleration (m/s/s)
                               -39.0,       // maxDeceleration (m/s/s)
                               //-13.0,       // maxDeceleration (m/s/s)
                                55.0,       // maxVelocity (m/s)
                               -15.0,       // minVelocity (m/s)
                                 5.0,       // length (meters)
                                 1.85,      // width (meters)
                                 1.2,       // frontAxleDisplacement (meters)
                                 4.0,       // rearAxleDisplacement (meters)
                               (1.85-0.25)/2, // wheelSpan (meters)
                                 0.33,      // wheelRadius (meters)
                                 0.25,      // wheelWidth (meters)
                               Math.PI/3,   // maxSteeringAngle (radian)
                               Math.PI/3)); // maxTurnPerSecond (radian)

    /** A large vehicle with medium wheelbase and moderate performance. */
    registerSpec(new VehicleSpec("SUV",
                                 3.83,      // maxAcceleration (m/s/s)
                               -39.0,       // maxDeceleration (m/s/s)
                               //-13.0,       // maxDeceleration (m/s/s)
                                52.0,       // maxVelocity (m/s)
                               -13.0,       // minVelocity (m/s)
                                 5.131,     // length (meters)
                                 2.007,     // width (meters)
                                 1.18,      // frontAxleDisplacement (meters)
                                 4.126,     // rearAxleDisplacement (meters)
                               (2.007-0.33)/2, // wheelSpan (meters)
                                 0.375,     // wheelRadius (meters)
                                 0.33,      // wheelWidth (meters)
                               Math.PI/3,   // maxSteeringAngle (radian)
                               Math.PI/3)); // maxTurnPerSecond (radian)

    /** A large vehicle with long wheelbase and poor performance. */
    registerSpec(new VehicleSpec("VAN",
                                 3.08,      // maxAcceleration (m/s/s)
                               -30.0,       // maxDeceleration (m/s/s)
                               //-10.0,       // maxDeceleration (m/s/s)
                                45.0,       // maxVelocity (m/s)
                               -10.0,       // minVelocity (m/s)
                                 5.385,     // length (meters)
                                 2.014,     // width (meters)
                                 0.58,      // frontAxleDisplacement (meters)
                                 4.085,     // rearAxleDisplacement (meters)
                               (2.014-0.33)/2, // wheelSpan (meters)
                                 0.375,     // wheelRadius (meters)
                                 0.33,      // wheelWidth (meters)
                               Math.PI/3,   // maxSteeringAngle (radian)
                               Math.PI/3)); // maxTurnPerSecond (radian)

    /** MARVIN */
//    registerSpec(new VehicleSpec("MARVIN",
//                                 1.01,        // maxAcceleration (m/s/s)
//                               -13.0,        // maxDeceleration (m/s/s)
//                                25.0,        // maxVelocity (m/s)
//                                -0.1,        // minVelocity (m/s)
//                                 5.131,      // length (meters)
//                                 2.0,        // width (meters)
//                                 1.18,       // frontAxleDisplacement (meters)
//                                 4.126,      // rearAxleDisplacement (meters)
//                               (2.0-0.33)/2, // wheelSpan (meters)
//                                 0.375,      // wheelRadius (meters)
//                                 0.33,       // wheelWidth (meters)
//                               Math.PI/3,    // maxSteeringAngle (radian)
//                               Math.PI/3));  // maxTurnPerSecond (radian)



//    registerSpec(new VehicleSpec("MARVIN",
//                                 0.5,        // maxAcceleration (m/s/s)
//                               -13.0,        // maxDeceleration (m/s/s)
//                                25.0,        // maxVelocity (m/s)
//                                -0.1,        // minVelocity (m/s)
//                                 5.131,      // length (meters)
//                                 2.0,        // width (meters)
//                                 1.18,       // frontAxleDisplacement (meters)
//                                 4.126,      // rearAxleDisplacement (meters)
//                               (2.0-0.33)/2, // wheelSpan (meters)
//                                 0.375,      // wheelRadius (meters)
//                                 0.33,       // wheelWidth (meters)
//                               Math.PI/3,    // maxSteeringAngle (radian)
//                               Math.PI/3));  // maxTurnPerSecond (radian)

    /** A very large vehicle with long wheelbase and very poor performance. */
//    registerSpec(new VehicleSpec("BUS",
//                                 1.3,       // maxAcceleration (m/s/s)
//                                -8.0,       // maxDeceleration (m/s/s)
//                                35.0,       // maxVelocity (m/s)
//                                -9.0,       // minVelocity (m/s)
//                                15.0,       // length (meters)
//                                 3.0,       // width (meters)
//                                 1.5,       // frontAxleDisplacement (meters)
//                                12.0,       // rearAxleDisplacement (meters)
//                               (3.0-0.4)/2, // wheelSpan (meters)
//                                 0.66666667,// wheelRadius (meters)
//                                 0.4,       // wheelWidth (meters)
//                               Math.PI/4,   // maxSteeringAngle (radian)
//                               Math.PI/3)); // maxTurnPerSecond (radian)
  }


  /////////////////////////////////
  // CONSTRUCTORS
  /////////////////////////////////

  /**
   * This class should never be instantiated.
   */
  private VehicleSpecDatabase(){};

}
