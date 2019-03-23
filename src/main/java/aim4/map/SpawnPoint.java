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
package aim4.map;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.List;

import aim4.map.lane.Lane;
import aim4.vehicle.VehicleSpec;

/**
 * A spawn point.
 */
public class SpawnPoint {

  /////////////////////////////////
  // NESTED CLASSES
  /////////////////////////////////

  // TODO: make it sortable according to the time

  /**
   * The specification of a spawn.
   */
  public static class SpawnSpec {
    /** The spawn time */
    double spawnTime;
    /** The vehicle specification */
    VehicleSpec vehicleSpec;
    /** The destination road */
    Road destinationRoad;

    /**
     * Create a spawn specification.
     *
     * @param spawnTime        the spawn time
     * @param vehicleSpec      the vehicle specification
     * @param destinationRoad  the destination road
     */
    public SpawnSpec(double spawnTime, VehicleSpec vehicleSpec,
                     Road destinationRoad) {
      this.spawnTime = spawnTime;
      this.vehicleSpec = vehicleSpec;
      this.destinationRoad = destinationRoad;
    }

    /**
     * Get the spawn time.
     *
     * @return the spawn time
     */
    public double getSpawnTime() {
      return spawnTime;
    }

    /**
     * Get the vehicle specification.
     *
     * @return the vehicle specification
     */
    public VehicleSpec getVehicleSpec() {
      return vehicleSpec;
    }

    /**
     * Get the destination road.
     *
     * @return the destination road
     */
    public Road getDestinationRoad() {
      return destinationRoad;
    }
  }


  /**
   * The interface of the spawn specification genreator.
   */
  public static interface SpawnSpecGenerator {
    /**
     * Advance the time step.
     *
     * @param spawnPoint  the spawn point
     * @param timeStep    the time step
     * @return the list of spawn spec generated in this time step.
     */
    List<SpawnSpec> act(SpawnPoint spawnPoint, double timeStep);
  }


  /////////////////////////////////
  // PRIVATE FIELDS
  /////////////////////////////////

  /** The current time */
  private double currentTime;
  /** The initial position of the vehicle */
  private Point2D pos;
  /** The initial heading of the vehicle */
  private double heading;
  /** The initial steering angle of the vehicle */
  private double steeringAngle;
  /** The initial acceleration */
  private double acceleration;
  /** The lane */
  private Lane lane;
  /**
   * The area in which there should not have any other vehicle when the
   * vehicle is spawned.
   */
  private Rectangle2D noVehicleZone;
  /** The vehicle spec chooser */
  private SpawnSpecGenerator vehicleSpecChooser;

  /////////////////////////////////
  // CONSTRUCTORS
  /////////////////////////////////

  /**
   * Create a spawn point.
   *
   * @param currentTime         the current time
   * @param pos                 the initial position
   * @param heading             the initial heading
   * @param steeringAngle       the initial steering angle
   * @param acceleration        the initial acceleration
   * @param lane                the lane
   * @param noVehicleZone       the no vehicle zone
   * @param vehicleSpecChooser  the vehicle spec chooser
   */
  public SpawnPoint(double currentTime,
                    Point2D pos,
                    double heading,
                    double steeringAngle,
                    double acceleration,
                    Lane lane,
                    Rectangle2D noVehicleZone,
                    SpawnSpecGenerator vehicleSpecChooser) {
    this.currentTime = currentTime;
    this.pos = pos;
    this.heading = heading;
    this.steeringAngle = steeringAngle;
    this.acceleration = acceleration;
    this.lane = lane;
    this.noVehicleZone = noVehicleZone;
    this.vehicleSpecChooser = vehicleSpecChooser;
  }

  /**
   * Create a spawn point.
   *
   * @param currentTime         the current time
   * @param pos                 the initial position
   * @param heading             the initial heading
   * @param steeringAngle       the initial steering angle
   * @param acceleration        the initial acceleration
   * @param lane                the lane
   * @param noVehicleZone       the no vehicle zone
   */
  public SpawnPoint(double currentTime,
                    Point2D pos,
                    double heading,
                    double steeringAngle,
                    double acceleration,
                    Lane lane,
                    Rectangle2D noVehicleZone) {
    this.currentTime = currentTime;
    this.pos = pos;
    this.heading = heading;
    this.steeringAngle = steeringAngle;
    this.acceleration = acceleration;
    this.lane = lane;
    this.noVehicleZone = noVehicleZone;
    this.vehicleSpecChooser = null;
  }

  /////////////////////////////////
  // PUBLIC METHODS
  /////////////////////////////////

  /**
   * Advance the time step.
   *
   * @param timeStep  the time step
   * @return The list of spawn spec generated in this time step
   */
  public List<SpawnSpec> act(double timeStep) {
    assert vehicleSpecChooser != null;
    List<SpawnSpec> spawnSpecs = vehicleSpecChooser.act(this, timeStep);
    currentTime += timeStep;
    return spawnSpecs;
  }

  /////////////////////////////////
  // PUBLIC METHODS
  /////////////////////////////////

  // info retrieval

  /**
   * Get the current time.
   *
   * @return the current time
   */
  public double getCurrentTime() {
    return currentTime;
  }

  /**
   * Get the initial position.
   *
   * @return the initial position
   */
  public Point2D getPosition() {
    return pos;
  }

  /**
   * Get the initial heading.
   *
   * @return the initial heading
   */
  public double getHeading() {
    return heading;
  }

  /**
   * Get the initial steering angle.
   *
   * @return the initial steering angle
   */
  public double getSteeringAngle() {
    return steeringAngle;
  }

  /**
   * Get the initial acceleration.
   *
   * @return the initial acceleration
   */
  public double getAcceleration() {
    return acceleration;
  }

  /**
   * Get the lane.
   *
   * @return the lane
   */
  public Lane getLane() {
    return lane;
  }

  /**
   * Get the no vehicle zone.
   *
   * @return the no vehicle zone
   */
  public Rectangle2D getNoVehicleZone() {
    return noVehicleZone;
  }

  /**
   * Set the vehicle spec chooser.
   *
   * @param vehicleSpecChooser the vehicle spec chooser
   */
  public void setVehicleSpecChooser(SpawnSpecGenerator vehicleSpecChooser) {
    // assert this.vehicleSpecChooser == null;  // TODO think whether it is okay
    this.vehicleSpecChooser = vehicleSpecChooser;
  }

}
