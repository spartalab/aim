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

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import aim4.config.Debug;
import aim4.driver.AutoDriver;
import aim4.driver.DriverSimView;
import aim4.map.lane.Lane;
import aim4.msg.i2v.I2VMessage;
import aim4.msg.v2i.V2IMessage;
import aim4.noise.DoubleGauge;
import aim4.vehicle.AutoVehicleDriverView.LRFMode;


/**
 * The basic autonomous vehicle.
 */
public class BasicAutoVehicle extends BasicVehicle
                              implements AutoVehicleSimView {

  /////////////////////////////////
  // CONSTANTS
  /////////////////////////////////

  // These control how the sensor range (side to side) changes during turning

  /**
   * Sensor angle multiplier (based on steering angle) for the side of the
   * Vehicle toward which the Vehicle is turning. Currently a factor of
   * {@value}.
   */
  private static final double SENSOR_RANGE_MULT_B = 1.5;

  /**
   * Sensor angle multiplier (based on steering angle) for the side of the
   * Vehicle away from which the Vehicle is turning. Currently a factor of
   * {@value}.
   */
  private static final double SENSOR_RANGE_MULT_S = 0.25;

  /**
   * The default distance the Vehicle can transmit messages.
   * {@value} meters.
   */
  public static final double DEFAULT_TRANSMISSION_POWER = 250; // meters


  /////////////////////////////////
  // PRIVATE FIELDS
  /////////////////////////////////

  /**
   * The Driver controlling this vehicle.
   */
  private AutoDriver driver;


  /////////////////////////////////
  // PRIVATE FIELDS
  /////////////////////////////////

  /**
   * The maximum distance the Vehicle can transmit a message, in meters.
   */
  private double transmissionPower = DEFAULT_TRANSMISSION_POWER;


  /////////////////////////////////
  // PRIVATE FIELDS
  /////////////////////////////////

  // V2I Communications systems

  /**
   * The messages waiting to be sent from the Vehicle to an
   * IntersectionManager.
   */
  private Queue<V2IMessage> v2iOutbox = new LinkedList<V2IMessage>();

  /**
   * The messages waiting to be received from an IntersectionManager and
   * processed by the Vehicle.
   */
  private Queue<I2VMessage> i2vInbox = new LinkedList<I2VMessage>();


  // Stats on communication

  /** The number of bits this Vehicle has received. */
  protected int bitsReceived;
  // TODO: change protected to private after figuring out where should
  // the proxy vehicle put.

  /** The number of bits this Vehicle has transmitted. */
  private int bitsTransmitted;

  //-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
  // Note: these should be changed to Vector if they need to be synchronized
  //       because ArrayList is not thread-safe, whereas Vector is. Another
  //       option would be to use
  //             Collections.synchronizedList(new ArrayList(...));
  //
  //       These are using an ArrayList instead of a LinkedList because we
  //       plan to do a lot of adding and removing, and while this might be
  //       faster with a LinkedList were we to delete items off the front
  //       one at a time, this causes a lot of memory allocation/cleanup.
  //       Instead, we use an ArrayList, process all messages at once, and
  //       then clear the whole list.  In this way, the memory that is
  //       allocated for the List sticks around and doesn't have to be
  //       reallocated every time new messages are added.
  //-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=



  /////////////////////////////////
  // PRIVATE FIELDS
  /////////////////////////////////

  // intervalometer

  /**
   * A gauge indicating the distance to the vehicle in front of this one. If
   * no vehicle is detected in front of this one, the gauge reads its maximum
   * value.
   */
  private DoubleGauge intervalometer = new DoubleGauge();

  /////////////////////////////////
  // PRIVATE FIELDS
  /////////////////////////////////

  // LRF

  /**
   * The current operating mode of the vehicle's laser range finder.
   */
  private LRFMode lrfMode = LRFMode.DISABLED;

  /**
   * A gauge indicating whether or not the Laser Range finder is currently
   * sensing anything.
   */
  private boolean lrfSensing = false;
  /**
   * A gauge indicating the angle from the vehicle to the object currently
   * sensed by the Laser Range Finder, in radians.  An angle of 0 means
   * straight ahead. As with the steerometer, positive angles are to the left,
   * negative are to the right.
   */
  private DoubleGauge lrfAngle = new DoubleGauge();
  /**
   * A gauge indicating the distance from the vehicle to the object currently
   * sensed by the Laser Range Finder, in meters.  This distance is measured
   * from the point between the front wheels of the vehicle.
   */
  private DoubleGauge lrfDistance = new DoubleGauge();


  /////////////////////////////////
  // PRIVATE FIELDS
  /////////////////////////////////

  // vehicle tracking

  /**
   * A gauge indicating whether or not the vehicle tracking devices
   * are currently sensing anything.
   */
  private boolean vehicleTracking = false;
  /**
   * The target lane for the vehicle tracking devices.
   */
  private Lane vehicleTrackingTargetLane = null;
  /**
   * A gauge holding the distance, in meters, between p1 and p2, both of them
   * are points on the target lane, where p1 is the point projected from the
   * center of the front of the vehicle, and p2 is the nearest point of
   * another vehicle in the front of the vehicle to p1 on the target lane.
   * If there is no vehicle in the front on the target lane, the value
   * should be Double.MAX_VALUE.
   */
  private DoubleGauge frontVehicleDistanceSensor = new DoubleGauge();
  /**
   * A gauge holding the distance, in meters, between p1 and p2, both of them
   * are points on the target lane, where p1 is the point projected from the
   * center of the front of the vehicle, and p2 is the nearest point of
   * another vehicle behind of the vehicle to p1 on the target lane.
   * If there is no vehicle behind on the target lane, the value
   * should be Double.MAX_VALUE.
   */
  private DoubleGauge rearVehicleDistanceSensor = new DoubleGauge();
  /**
   * A gauge holding the speed, in meters per second, of the vehicle in front
   * of the vehicle on the target lane.  If there is no vehicle in the front on
   * the target lane, the value should be Double.MAX_VALUE.
   */
  private DoubleGauge frontVehicleSpeedSensor = new DoubleGauge();
  /**
   * A gauge holding the speed, in meters per second, of the vehicle behind
   * the vehicle on the target lane.  If there is no vehicle behind on the
   * target lane, the balue should be Double.MAX_VALUE.
   */
  private DoubleGauge rearVehicleSpeedSensor = new DoubleGauge();


  /////////////////////////////////
  // PRIVATE FIELDS
  /////////////////////////////////

  // debug

  /**
   * The last V2I message
   */
  private V2IMessage lastV2IMessage;



  /////////////////////////////////
  // CONSTRUCTORS
  /////////////////////////////////

  // TODO: reorganize the parameter order.

  /**
   * Construct a vehicle
   *
   * @param spec            the vehicle's specification
   * @param pos             the initial position of the Vehicle
   * @param heading         the initial heading of the Vehicle
   * @param steeringAngle   the initial steering angle of the Vehicle
   * @param velocity        the initial velocity of the Vehicle
   * @param targetVelocity  the initial target velocity
   * @param acceleration    the initial acceleration of the Vehicle
   * @param currentTime     the current time
   */
  public BasicAutoVehicle(VehicleSpec spec,
                          Point2D pos,
                          double heading,
                          double steeringAngle,
                          double velocity,
                          double targetVelocity,
                          double acceleration,
                          double currentTime) {
    super(spec, pos, heading, velocity, steeringAngle, acceleration,
          targetVelocity, currentTime);
  }


  /////////////////////////////////
  // PUBLIC METHODS
  /////////////////////////////////

  /**
   * {@inheritDoc}
   */
  @Override
  public AutoDriver getDriver() {
    return driver;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setDriver(AutoDriver driver) {
    this.driver = driver;
  }


  /////////////////////////////////
  // PUBLIC METHODS
  /////////////////////////////////

  // intervalometer

  /**
   * {@inheritDoc}
   */
  @Override
  public DoubleGauge getIntervalometer() {
    return intervalometer;
  }


  /////////////////////////////////
  // PUBLIC METHODS
  /////////////////////////////////

  // LRFS sensor (not implemented)

  /**
   * Set the Vehicle's laser range finder operating mode.
   *
   * @param mode the new laser range finder mode
   * @see        LRFMode
   */
  public void setLRFMode(LRFMode mode) {
    this.lrfMode = mode;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public LRFMode getLRFMode() {
    return lrfMode;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isLRFSensing() {
    return lrfSensing;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setLRFSensing(boolean sensing) {
    lrfSensing = sensing;
  }

  /**
   * Get this Vehicle's laser range finder angle gauge.
   * This should <b>only</b> be followed by a call to <code>read</code>,
   * <b>except</b> in the actual physical simulator which is allowed to set
   * these values.
   *
   * @return the Vehicle's laser range finder angle gauge
   */
  public DoubleGauge getLRFAngle() {
    return lrfAngle;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public DoubleGauge getLRFDistance() {
    return lrfDistance;
  }


  /////////////////////////////////
  // PUBLIC METHODS
  /////////////////////////////////

  // vehicle tracking (for lane changing)

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isVehicleTracking() {
   return vehicleTracking;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setVehicleTracking(boolean sensing) {
    vehicleTracking = sensing;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setTargetLaneForVehicleTracking(Lane lane) {
    vehicleTrackingTargetLane = lane;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Lane getTargetLaneForVehicleTracking() {
    return vehicleTrackingTargetLane;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public DoubleGauge getFrontVehicleDistanceSensor() {
    return frontVehicleDistanceSensor;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public DoubleGauge getRearVehicleDistanceSensor() {
    return rearVehicleDistanceSensor;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public DoubleGauge getFrontVehicleSpeedSensor() {
    return frontVehicleSpeedSensor;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public DoubleGauge getRearVehicleSpeedSensor() {
    return rearVehicleSpeedSensor;
  }


  /////////////////////////////////
  // PUBLIC METHODS
  /////////////////////////////////

  // messaging

  /**
   * Set the Vehicle's transmission power.
   *
   * @param transmissionPower the new transmission power, in meters
   */
  public void setTransmissionPower(double transmissionPower) {
    this.transmissionPower = transmissionPower;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public double getTransmissionPower() {
    return transmissionPower;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @SuppressWarnings("ReturnOfCollectionOrArrayField")
  public Queue<V2IMessage> getV2IOutbox() {
    return v2iOutbox;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<I2VMessage> pollAllMessagesFromI2VInbox() {
    // TODO: many need to make this function atomic to avoid
    // putting messages in the queue and retrieve from it at the same time.
    List<I2VMessage> msgs = new ArrayList<I2VMessage>(i2vInbox);
    i2vInbox.clear();
    return msgs;
  }


  /**
   * {@inheritDoc}
   */
  @Override
  public void send(V2IMessage msg) {
    if (Debug.isPrintVehicleOutboxMessageOfVIN(msg.getVin())) {
      System.err.printf("vin %d sends message: %s\n", vin, msg);
    }
    v2iOutbox.add(msg);
    bitsTransmitted += msg.getSize();
    lastV2IMessage = msg;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void receive(I2VMessage msg) {
    i2vInbox.add(msg);
    bitsReceived += msg.getSize();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int getBitsReceived() {
    return bitsReceived;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int getBitsTransmitted() {
    return bitsTransmitted;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public V2IMessage getLastV2IMessage() {
    return lastV2IMessage;
  }

}
