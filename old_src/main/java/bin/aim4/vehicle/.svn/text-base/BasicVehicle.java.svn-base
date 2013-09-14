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

import java.awt.Shape;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.Iterator;
import java.util.List;

import aim4.config.Constants;
import aim4.config.Debug;
import aim4.driver.Driver;
import aim4.map.track.TrackPosition;
import aim4.noise.DoubleGauge;
import aim4.util.GeomMath;
import aim4.util.GeomUtil;
import aim4.util.Util;

/**
 * The most basic form of a vehicle.
 */
public abstract class BasicVehicle implements VehicleSimView {

  /////////////////////////////////
  // CONSTANTS
  /////////////////////////////////

  /**
   * Constant used to determine what is "straight" for vehicles
   * because floating point numbers are stupid and computers don't know
   * how to do L'H&ocirc;pital's rule.
   */
  private static final double MIN_STEERING_THRESHOLD = 0.00001;

  /////////////////////////////////
  // NESTED CLASSES
  /////////////////////////////////

  /**
   * The movement of a vehicle.
   */
  public static interface Movement {
    /**
     * Get the position of the vehicle.
     *
     * @return the position of the vehicle
     */
    Point2D getPosition();

    /**
     * Get the heading of the vehicle
     *
     * @return the heading of the vehicle
     */
    double getHeading();

    /**
     * Get the velocity of the vehicle
     *
     * @return the velocity of the vehicle
     */
    double getVelocity();

    /**
     * Move the vehicle for a given period of time.
     *
     * @param currentTime  the current time
     * @param timeStep     the period of time for which the vehicle moves.
     */
    void move(double currentTime, double timeStep);
  }

  /**
   * The Movement Factory
   */
  public static interface MovementFactory {
    /**
     * Create a movement object.
     *
     * @param position  the position
     * @param heading   the heading
     * @param velocity  the velocity
     * @return the movement
     */
    Movement make(Point2D position, double heading, double velocity);
  }


  /**
   * The non-acceleration movement
   */
  public static abstract class NonAccelMovement implements Movement {

    /////////////////////////////////
    // PROTECTED FIELDS
    /////////////////////////////////

    /**
     * The specification of the vehicle
     */
    protected final VehicleSpec spec;
    /**
     * The position of the vehicle, represented by the point at
     * the center of the front of the Vehicle.
     */
    protected Point2D position;
    /** The direction of the vehicle */
    protected double heading;
    /** The velocity of the vehicle */
    protected double velocity;

    /////////////////////////////////
    // CONSTRUCTOR
    /////////////////////////////////

    /**
     * Create a non-acceleration movement.
     *
     * @param spec      the specification of the vehicle
     * @param position  the position
     * @param heading   the heading
     * @param velocity  the velocity
     */
    public NonAccelMovement(VehicleSpec spec, Point2D position, double heading,
                            double velocity) {
      this.spec = spec;
      this.position = position;
      this.heading = heading;
      this.velocity = velocity;
    }


    /////////////////////////////////
    // PUBLIC METHODS
    /////////////////////////////////

    // there should be no setters for these fields.

    /**
     * Get the vehicle specification.
     *
     * @return the vehicle specification
     */
    public final VehicleSpec getVehicleSpec() {
      return spec;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Point2D getPosition() {
      return position;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getHeading() {
      return heading;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getVelocity() {
      return velocity;
    }

    /////////////////////////////////
    // PROTECTED METHODS
    /////////////////////////////////

    /**
     * Set the velocity with respect to the physical limit of the vehicle.
     *
     * @param velocity  the velocity
     */
    protected void setVelocityWithBound(double velocity) {
      this.velocity = Util.constrain(velocity,
                                     spec.getMinVelocity(),
                                     spec.getMaxVelocity());
    }


    /////////////////////////////////
    // DEBUG
    /////////////////////////////////

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
      return "Pos=(" + Constants.ONE_DEC.format(position.getX()) + "," +
             Constants.ONE_DEC.format(position.getY()) + ")," +
             "Heading=" + Constants.TWO_DEC.format(heading) + "," +
             "Velocity=" + Constants.TWO_DEC.format(velocity);
    }
  }


  /**
   * The steering movement.
   */
  public static class SteeringMovement extends NonAccelMovement {

    /////////////////////////////////
    // PROTECTED FIELDS
    /////////////////////////////////

    /** The steering angle */
    private double steeringAngle;

    /////////////////////////////////
    // CONSTRUCTORS
    /////////////////////////////////

    /**
     * Create a steering movement.
     *
     * @param spec           the vehicle specification
     * @param position       the position
     * @param heading        the heading
     * @param velocity       the velocity
     * @param steeringAngle  the steering angle
     */
    public SteeringMovement(VehicleSpec spec, Point2D position, double heading,
                            double velocity, double steeringAngle) {
      super(spec, position, heading, velocity);
      this.steeringAngle = steeringAngle;
    }

    /////////////////////////////////
    // PUBLIC METHODS
    /////////////////////////////////

    /**
     * Get the steering angle.
     *
     * @return the steering angle
     */
    public double getSteeringAngle() {
      return steeringAngle;
    }

    /////////////////////////////////
    // PUBLIC METHODS
    /////////////////////////////////

    // control

    /**
     * Set the steering angle with respect to the physical limit of the vehicle.
     *
     * @param steeringAngle  the steering angle
     */
    public void setSteeringAngleWithBound(double steeringAngle) {
      this.steeringAngle = Util.constrain(steeringAngle,
                                          -1.0 * spec.getMaxSteeringAngle(),
                                          spec.getMaxSteeringAngle());
    }

    /////////////////////////////////
    // PROTECTED METHODS
    /////////////////////////////////

    /**
     * Move the vehicle while holding all vehicle properties constant.
     *
     * @param timeStep the length of time for which to move the Vehicle
     */
    @Override
    public void move(double currentTime, double timeStep) {
      // If the steering angle is sufficiently small, taking its tangent will
      // totally mess everything up.  Instead we assume the vehicle is moving
      // straight.
      if (Math.abs(steeringAngle) < MIN_STEERING_THRESHOLD) {
        double endX = position.getX() + velocity * Math.cos(heading) *
                                        timeStep;
        double endY = position.getY() + velocity * Math.sin(heading) *
                                        timeStep;
        position = new Point2D.Double(endX, endY);
        // no need to update the heading
      } else {
        // Things are more complicated now...
        // Use the following differential equations:
        // d(phi)/dt = v * tan(psi) / L
        // dx/dt = v * cos(phi)
        // dy/dt = v * sin(phi),
        // where L is the wheelbase, (x,y) are the coordinates of the point
        // between the back wheels, psi is the steering angle, phi is the
        // heading of the vehicle, and v is the vehicle's velocity.

        // First, calculate d(phi)/dt
        double rotationRate =
            velocity * (Math.tan(steeringAngle) / spec.getWheelbase());
        // Now calculate the final heading of the vehicle
        double endHeading =
            GeomMath.canonicalAngle(heading + rotationRate * timeStep);

        // We're going to move the point between the back wheels, so let's find
        // out where it is.
        Point2D p = spec.getPointBetweenRearWheels(position, heading);
        // These are the solved forms of the differential equations
        double endXdelta =
            p.getX() - (spec.getWheelbase() / Math.tan(steeringAngle)) *
                       (Math.sin(heading) - Math.sin(endHeading));
        double endYdelta =
            p.getY() - (spec.getWheelbase() / Math.tan(steeringAngle)) *
                       (Math.cos(endHeading) - Math.cos(heading));
        // Update the position and heading
        double endX =
            endXdelta + spec.getRearAxleDisplacement() * Math.cos(endHeading);
        double endY =
            endYdelta + spec.getRearAxleDisplacement() * Math.sin(endHeading);
        position = new Point2D.Double(endX, endY);
        heading = endHeading;
      }
    }

    /////////////////////////////////
    // DEBUG
    /////////////////////////////////

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
      return super.toString() + ", " +
             "steeringAngle=" + Constants.TWO_DEC.format(steeringAngle);
    }

  }


  /**
   * The track movement.
   */
  public static class TrackMovement extends NonAccelMovement {

    /////////////////////////////////
    // PRIVATE FIELDS
    /////////////////////////////////

    /** The position on the track */
    private TrackPosition trackPosition;
    /** The factory of the base movement */
    private MovementFactory movementFactory;
    /** The base movement in case the vehicle moves off the track */
    private Movement baseMovement;

    /////////////////////////////////
    // CONSTRUCTORS
    /////////////////////////////////

    /**
     * Create a track movement.
     *
     * @param spec                 the vehicle specification
     * @param position             the position
     * @param heading              the heading
     * @param velocity             the velocity
     * @param trackPosition        the track position
     * @param baseMovementFactory  the base movement factory
     */
    public TrackMovement(VehicleSpec spec, Point2D position, double heading,
                         double velocity, TrackPosition trackPosition,
                         MovementFactory baseMovementFactory) {
      super(spec, position, heading, velocity);
      this.trackPosition = trackPosition;
      this.movementFactory = baseMovementFactory;
      this.baseMovement = null;
    }

    /////////////////////////////////
    // PUBLIC METHODS
    /////////////////////////////////

    /**
     * {@inheritDoc}
     */
    @Override
    public void move(double currentTime, double timeStep) {
      if (baseMovement == null) {  // still on the track
        double dist = velocity * timeStep;
        double remainDist = trackPosition.move(dist);
        if (remainDist == 0.0) {  // still on the track
          position =
              new Point2D.Double(trackPosition.getX(), trackPosition.getY());
          heading = trackPosition.getTangentSlope();
          // no change in the velocity
        } else {  // reach the end of the track, continue with the base movement
          double remainTime = remainDist / velocity;
          // create the base movement;
          baseMovement =
              movementFactory.make(new Point2D.Double(trackPosition.getX(),
                                                      trackPosition.getY()),
                                   trackPosition.getTangentSlope(),
                                   velocity);
          baseMovement.move(currentTime + timeStep - remainTime, remainTime);
          position = baseMovement.getPosition();
          velocity = baseMovement.getVelocity();
          heading = baseMovement.getHeading();
        }
      } else {  // passed the end of the track, continue with the base movement.
        baseMovement.move(currentTime, timeStep);
        position = baseMovement.getPosition();
        velocity = baseMovement.getVelocity();
        heading = baseMovement.getHeading();
      }
    }

    /////////////////////////////////
    // DEBUG
    /////////////////////////////////

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
      return super.toString() + ", " +
             "trackPosition =" + trackPosition;
    }

  }


  /**
   * The movement with acceleration interface
   */
  public static interface MovementWithAccel extends Movement {

    /**
     * Get the acceleration of the vehicle
     *
     * @return the acceleration of the vehicle
     */
    double getAcceleration();

    /**
     * Set the acceleration with respect to the physical limit of the vehicle.
     *
     * @param acceleration  the acceleration
     */
    void setAccelerationWithBound(double acceleration);
  }


  /**
   * The physical movement.
   */
  public static class PhysicalMovement implements MovementWithAccel {

    /////////////////////////////////
    // PROTECTED FIELDS
    /////////////////////////////////

    /** The specification of the vehicle */
    protected final VehicleSpec spec;

    /** The underlying basic movement */
    private NonAccelMovement nonAccelMovement;

    /** The acceleration of the vehicle */
    private double acceleration;

    /////////////////////////////////
    // CONSTRUCTORS
    /////////////////////////////////

    /**
     * Create the physical movement.
     *
     * @param nonAccelMovement  the non-acceleration movement
     * @param acceleration      the acceleration
     */
    public PhysicalMovement(NonAccelMovement nonAccelMovement,
                            double acceleration) {
      this.spec = nonAccelMovement.getVehicleSpec();
      this.nonAccelMovement = nonAccelMovement;
      this.acceleration = acceleration;
    }

    /////////////////////////////////
    // PUBLIC METHODS
    /////////////////////////////////

    /**
     * {@inheritDoc}
     */
    @Override
    public Point2D getPosition() {
      return nonAccelMovement.getPosition();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getHeading() {
      return nonAccelMovement.getHeading();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getVelocity() {
      return nonAccelMovement.getVelocity();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getAcceleration() {
      return acceleration;
    }

    /**
     * Get the non acceleration movement.
     *
     * @return the non acceleration movement
     */
    public NonAccelMovement getNonAccelMovement() {
      return nonAccelMovement;
    }

    /////////////////////////////////
    // PUBLIC METHODS
    /////////////////////////////////

    // control

    /**
     * {@inheritDoc}
     */
    @Override
    public void setAccelerationWithBound(double acceleration) {
      this.acceleration = Util.constrain(acceleration,
                                         spec.getMaxDeceleration(),
                                         spec.getMaxAcceleration());
    }

    /**
     * Maintain the speed of the vehicle.
     */
    public void coast() {
      setAccelerationWithBound(0.0);
    }

    /////////////////////////////////
    // PUBLIC METHODS
    /////////////////////////////////

    /**
     * {@inheritDoc}
     */
    @Override
    public void move(double currentTime, double timeStep) {
      if (Util.isDoubleZero(acceleration)) {
        nonAccelMovement.move(currentTime, timeStep);
      } else {
        double maxChange = acceleration * timeStep;
        double velocity = nonAccelMovement.getVelocity();
        nonAccelMovement.setVelocityWithBound(velocity + maxChange / 2.0);
        nonAccelMovement.move(currentTime, timeStep);
        nonAccelMovement.setVelocityWithBound(velocity + maxChange);
      }
    }

    /////////////////////////////////
    // PROTECTED METHODS
    /////////////////////////////////

    /**
     * Move the vehicle while holding all vehicle properties, including
     * the acceleration, constant.
     *
     * @param currentTime  the current time
     * @param timeStep     the length of time for which to move the Vehicle
     */
    protected void moveWithoutAcceleration(double currentTime, double timeStep) {
      nonAccelMovement.move(currentTime, timeStep);
    }

    /**
     * Set the velocity with respect to the physical limit of the vehicle.
     *
     * @param velocity  the velocity
     */
    protected void setVelocityWithBound(double velocity) {
      nonAccelMovement.setVelocityWithBound(velocity);
    }

    /////////////////////////////////
    // DEBUG
    /////////////////////////////////

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
      return nonAccelMovement.toString() + ", " +
             "acceleration=" + Constants.TWO_DEC.format(acceleration);
    }

  }


  /**
   * A move-to-target-velocity movement.
   */
  public static class MoveToTargetVelocityMovement extends PhysicalMovement {

    /////////////////////////////////
    // PROTECTED FIELDS
    /////////////////////////////////

    /** The velocity at which the driver would like to be traveling. */
    private double targetVelocity;


    /////////////////////////////////
    // CONSTRUCTORS
    /////////////////////////////////

    /**
     * Create a move-to-target-velocity movement.
     *
     * @param spec            the vehicle specification
     * @param position        the position
     * @param heading         the heading
     * @param velocity        the velocity
     * @param steeringAngle   the steering angle
     * @param acceleration    the acceleration
     * @param targetVelocity  the target velocity
     */
    public MoveToTargetVelocityMovement(VehicleSpec spec, Point2D position,
                                        double heading, double velocity,
                                        double steeringAngle,
                                        double acceleration,
                                        double targetVelocity) {
      this(new SteeringMovement(spec, position, heading, velocity,
                                steeringAngle),
           acceleration, targetVelocity);
    }

    /**
     * Create a move-to-target-velocity movement.
     *
     * @param basicMovement   the basic movement
     * @param acceleration
     * @param targetVelocity
     */
    public MoveToTargetVelocityMovement(NonAccelMovement basicMovement,
                                        double acceleration,
                                        double targetVelocity) {
      super(basicMovement, acceleration);
      this.targetVelocity = targetVelocity;
    }

    /////////////////////////////////
    // PUBLIC METHODS
    /////////////////////////////////

    // control

    /**
     * Set the target velocity of the velocity
     *
     * @param targetVelocity  the target velocity
     */
    public void setTargetVelocityWithBound(double targetVelocity) {
      this.targetVelocity = Util.constrain(targetVelocity,
                                           spec.getMinVelocity(),
                                           spec.getMaxVelocity());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setAccelerationWithBound(double acceleration) {
      super.setAccelerationWithBound(acceleration);
      double acceleration2 = getAcceleration();
      if (Util.isDoubleZero(acceleration2)) {
        targetVelocity = getVelocity();
      } else if (acceleration2 > 0.0) {
        targetVelocity = spec.getMaxVelocity();
      } else {  // this.acceleration < 0.0
        targetVelocity = spec.getMinVelocity();
      }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void coast() {
      setAccelerationWithBound(0.0);
      setTargetVelocityWithBound(getVelocity());
    }

    /**
     * Set the Vehicle's acceleration to its minimum value without going
     * backward. Obeys limits on acceleration as well as disabilities.
     */
    public void slowToStop() {
      if (getVelocity() > 0.0) {
        setAccelerationWithBound(spec.getMaxDeceleration());
      } else if (getVelocity() < 0.0) {
        setAccelerationWithBound(spec.getMaxAcceleration());
      } else {  // velocity == 0.0
        setAccelerationWithBound(0.0);
      }
      setTargetVelocityWithBound(0.0);
    }

    /**
     * Set the Vehicle's acceleration to its maximum value.  Obeys limits on
     * setMaxAccelWithMaxTargetVelocity as well as disabilities.
     */
    public void setMaxAccelWithMaxTargetVelocity() {
      setAccelerationWithBound(spec.getMaxAcceleration());
      setTargetVelocityWithBound(spec.getMaxVelocity());
    }

    /**
     * Set the target velocity (with maximum acceleration).
     *
     * @param targetVelocity  the target velocity
     */
    public void setTargetVelocityWithMaxAccel(double targetVelocity) {
      if (getVelocity() < targetVelocity) {
        setAccelerationWithBound(spec.getMaxAcceleration());
        setTargetVelocityWithBound(targetVelocity);
      } else if (getVelocity() > targetVelocity) {
        setAccelerationWithBound(spec.getMaxDeceleration());
        setTargetVelocityWithBound(targetVelocity);
      } else { // m2.getVelocity() == targetVelocity
        setAccelerationWithBound(0.0);
      }
    }

    /**
     * Set the acceleration with maximum target velocity.
     *
     * @param acceleration  the acceleration
     */
    public void setAccelWithMaxTargetVelocity(double acceleration) {
      setAccelerationWithBound(acceleration);
      if (acceleration > 0.0) {
        setTargetVelocityWithBound(spec.getMaxVelocity());
      } else if (acceleration < 0.0) {
        setTargetVelocityWithBound(spec.getMinVelocity());
      } else {  // acceleration == 0.0
        setTargetVelocityWithBound(getVelocity());
      }
    }


    /////////////////////////////////
    // PUBLIC METHODS
    /////////////////////////////////

    /**
     * {@inheritDoc}
     */
    @Override
    public void move(double currentTime, double timeStep) {
      double velocity = getVelocity();
      double acceleration = getAcceleration();
      // To be accurate, we may need to move this vehicle in two segments,
      // depending on whether it is accelerating the whole time or not.
      // First, determine which of these is the case.
      if (Util.isDoubleZero(acceleration)) {
        // no acceleration
        moveWithoutAcceleration(currentTime, timeStep);
      } else if (acceleration > 0.0) {
        // If we're at or above our target velocity, we won't do any
        // accelerating
        if (velocity >= targetVelocity) {
          moveWithoutAcceleration(currentTime, timeStep);
        } else {
          // Otherwise, we need to see if the vehicle will be accelerating the
          // whole time.
          double maxChange = acceleration * timeStep;
          double requestedChange = targetVelocity - velocity;
          // Yes, it is
          if (requestedChange >= maxChange) {
            // Set the velocity to half the change
            setVelocityWithBound(velocity + maxChange / 2.0);
            // Do the move
            moveWithoutAcceleration(currentTime, timeStep);
            // Set it the rest of the way
            setVelocityWithBound(velocity + maxChange);
          } else {
            // Otherwise, this is a two-parter
            // Find out how long it will take to make the change
            double accelDuration = requestedChange / acceleration;
            // Do the first part as above
            setVelocityWithBound(velocity + requestedChange / 2.0);
            moveWithoutAcceleration(currentTime, accelDuration);
            setVelocityWithBound(velocity + requestedChange);
            // Do the second part
            moveWithoutAcceleration(currentTime + accelDuration,
                                    timeStep - accelDuration);
          }
        }
      } else {  // (acceleration  0.0)
        // If we're at or above our target velocity, we won't do any
        // accelerating
        if (velocity <= targetVelocity) {
          moveWithoutAcceleration(currentTime, timeStep);
        } else {
          // Otherwise, we need to see if the vehicle will be accelerating the
          // whole time.
          double maxChange = acceleration * timeStep;
          double requestedChange = targetVelocity - velocity;
          // Yes, it is
          if (requestedChange <= maxChange) {
            // Set the velocity to half the change
            setVelocityWithBound(velocity + maxChange / 2.0);
            // Do the move
            moveWithoutAcceleration(currentTime, timeStep);
            // Set it the rest of the way
            setVelocityWithBound(velocity + maxChange);
          } else {
            // Otherwise, this is a two-parter
            // Find out how long it will take to make the change
            double accelDuration = requestedChange / acceleration;
            // Do the first part as above
            setVelocityWithBound(velocity + requestedChange / 2.0);
            moveWithoutAcceleration(currentTime, accelDuration);
            setVelocityWithBound(velocity + requestedChange);
            // Do the second part
            moveWithoutAcceleration(currentTime + accelDuration,
                                    timeStep - accelDuration);
          }
        }
      }
    }

    /////////////////////////////////
    // DEBUG
    /////////////////////////////////

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
      return super.toString() + ", " +
          "targetVelocity=" + Constants.TWO_DEC.format(targetVelocity);
    }

  }


  /**
   * The acceleration schedule movement.
   */
  public static class AccelScheduleMovement implements Movement {

    /////////////////////////////////
    // PRIVATE FIELDS
    /////////////////////////////////

    /** The underlying movement */
    private MovementWithAccel baseMovement;

    /** The acceleration schedule */
    private AccelSchedule accelSchedule;


    /////////////////////////////////
    // CONSTRUCTORS
    /////////////////////////////////


    /**
     * Create an acceleration schedule movement.
     *
     * @param baseMovement  the base movement
     */
    public AccelScheduleMovement(MovementWithAccel baseMovement) {
      this(baseMovement, null);
    }

    /**
     * Create an acceleration schedule movement.
     *
     * @param baseMovement   the base movement
     * @param accelSchedule  the acceleration schedule
     */
    public AccelScheduleMovement(MovementWithAccel baseMovement,
                                 AccelSchedule accelSchedule) {
      this.baseMovement = baseMovement;
      this.accelSchedule = accelSchedule;
    }

    /////////////////////////////////
    // PUBLIC METHODS
    /////////////////////////////////

    /**
     * {@inheritDoc}
     */
    @Override
    public Point2D getPosition() {
      return baseMovement.getPosition();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getHeading() {
      return baseMovement.getHeading();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getVelocity() {
      return baseMovement.getVelocity();
    }

    /**
     * Get the acceleration.
     *
     * @return the acceleration
     */
    public double getAcceleration() {
      return baseMovement.getAcceleration();
    }

    /**
     * Get the base movement.
     *
     * @return the base movement
     */
    public MovementWithAccel getBaseMovement() {
      return baseMovement;
    }

    /**
     * Get the acceleration schedule.
     *
     * @return the acceleration schedule
     */
    public AccelSchedule getAccelSchedule() {
      return accelSchedule;
    }


    /////////////////////////////////
    // PUBLIC METHODS
    /////////////////////////////////

    // control

    /**
     * Set the acceleration schedule.
     *
     * @param accelSchedule  the acceleration schedule
     */
    public void setAccelSchedule(AccelSchedule accelSchedule) {
      this.accelSchedule = accelSchedule;
    }


    /////////////////////////////////
    // PUBLIC METHODS
    /////////////////////////////////

    // TODO: rewrite the function to make it non-recursive.

    /**
     * {@inheritDoc}
     */
    @Override
    public void move(double currentTime, double timeStep) {
      if (accelSchedule != null) {
        List<AccelSchedule.TimeAccel> tas = accelSchedule.getList();
        Iterator<AccelSchedule.TimeAccel> iter = tas.iterator();

        if (iter.hasNext()) {
          AccelSchedule.TimeAccel ta = iter.next();

          if (ta.getTime() > currentTime) {
            // It means that the next acceleration update is in the future yet.
            // keep using the existing acceleration
            double dur = ta.getTime() - currentTime;
            if (dur < timeStep) {
              baseMovement.move(currentTime, dur);
              move(currentTime + dur, timeStep - dur); // recursive call
            } else {
              baseMovement.move(currentTime, timeStep);  // and then exit
            }
          } else if (Util.isDoubleEqual(ta.getTime(), currentTime)) {
            // update the acceleration
            baseMovement.setAccelerationWithBound(ta.getAcceleration());
            iter.remove();  // remove it since it has been consumed

            // check to see if there is next acceleration update
            if (iter.hasNext()) {
              ta = iter.next();  // don't remove it, just look at the time.
              double dur = ta.getTime() - currentTime;
              if (dur < timeStep) {
                baseMovement.move(currentTime, dur);
                move(currentTime + dur, timeStep - dur); // recursive call
              } else {
                baseMovement.move(currentTime, timeStep);  // and then exit
              }
            } else {
              // No more acceleration update, remove the acceleration profile
              accelSchedule = null;
              // continue with the base movement with the last acceleration
              baseMovement.move(currentTime, timeStep);  // and then exit
            }
          } else {
            iter.remove();  // remove it since the acceleration is in the past.
            move(currentTime, timeStep);  // recursive call
          }
        } else {
          // move without the acceleration schedule
          accelSchedule = null;
          baseMovement.move(currentTime, timeStep);
        }
      } else {
        // move without the acceleration schedule
        baseMovement.move(currentTime, timeStep);
      }
    }


    /////////////////////////////////
    // DEBUG
    /////////////////////////////////

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
      return baseMovement.toString() + ", " +
             "accelSchedule=" + accelSchedule;
    }

  }


  /////////////////////////////////
  // PROTECTED FIELDS
  /////////////////////////////////

  /**
   * The vehicle's ID number (i.e., the VIN number). If the VIN number is less
   * than zero, the vehicle has not been registered in the vehicle registry.
   */
  protected int vin;

  /** The characteristics of the vehicle */
  protected VehicleSpec spec;

  /** The movement mechanism */
  protected Movement movement;

  /** The current time */
  protected double currentTime;


  /////////////////////////////////
  // PRIVATE FIELDS
  /////////////////////////////////
  /**
   * The basic gauges
   */
  /**
   * A gauge holding the current time, as known to the vehicle, in seconds.
   */
  private DoubleGauge clock = new DoubleGauge();
  // TODO: think whether we should not allow human driven vehicle to have
  // a clock.
  /**
   * A gauge holding the global x-position of the vehicle, in meters.  Larger
   * values are further east.
   */
  private DoubleGauge xometer = new DoubleGauge();
  /**
   * A gauge holding the global y-position of the vehicle, in meters.
   * Larger values are further south.
   */
  private DoubleGauge yometer = new DoubleGauge();
  /**
   * A gauge holding the current heading of the vehicle, in radians.
   */
  private DoubleGauge compass = new DoubleGauge();
  /**
   * A gauge holding the vehicle's speed, in meters per second.
   */
  private DoubleGauge speedometer = new DoubleGauge();

  // memoization

  /**
   * Memoization cache for {@link #gaugePosition()}.
   */
  private Point2D memoGaugePosition;
  /**
   * Memoization cache for {@link #gaugePointBetweenFrontWheels()}.
   */
  private Point2D memoGaugePointBetweenFrontWheels;
  /**
   * Memoization cache for {@link #getShape()}.
   */
  private Shape memoGetShape;
  /**
   * Memoization cache for {@link #gaugeShape()}.
   */
  private Shape memoGaugeShape;

  /////////////////////////////////
  // CONSTRUCTORS
  /////////////////////////////////
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
  public BasicVehicle(VehicleSpec spec,
                      Point2D pos,
                      double heading,
                      double velocity,
                      double steeringAngle,
                      double acceleration,
                      double targetVelocity,
                      double currentTime) {
    this.vin = -1;    // no vin by default
    this.spec = spec;  // assume spec is a constant and will not change

    movement = new MoveToTargetVelocityMovement(spec,
                                                pos,
                                                heading,
                                                velocity,
                                                steeringAngle,
                                                acceleration,
                                                targetVelocity);

    // Update all the gauges and memos
    updateGaugesAndMemos();

    this.currentTime = currentTime;
    clock.record(currentTime);
  }

  /////////////////////////////////
  // FINALIZE
  /////////////////////////////////

  /**
   * {@inheritDoc}
   */
  @Override
  protected void finalize() throws Throwable {
    super.finalize();
    // Make sure that the vehicle is unregistered from the Vehicle Registry.
    if (vin >= 0) {
      VinRegistry.unregisterVehicle(vin);
      vin = -1;
    }
  }

  /////////////////////////////////
  // PUBLIC METHODS
  /////////////////////////////////

  /**
   * {@inheritDoc}
   */
  @Override
  public int getVIN() {
    return vin;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setVIN(int vin) {
    this.vin = vin;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public VehicleSpec getSpec() {
    return spec;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public double gaugeTime() {
    return clock.read();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public abstract Driver getDriver();

  /////////////////////////////////
  // PUBLIC METHODS
  /////////////////////////////////

  // states

  /**
   * {@inheritDoc}
   */
  @Override
  public Point2D getPosition() {
    return movement.getPosition();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public double getHeading() {
    return movement.getHeading();
  }

  // TODO remove this after reorganizing getSteeringAngle

  /**
   * Get the steering angle of the vehicle
   *
   * @return the steering angle of the vehicle
   */
  public double getSteeringAngle() {
    if (movement instanceof PhysicalMovement) {
      PhysicalMovement m2 = (PhysicalMovement)movement;
      NonAccelMovement m3 = m2.getNonAccelMovement();
      if (m3 instanceof SteeringMovement) {
        return ((SteeringMovement)m3).getSteeringAngle();
      } else {
        throw new UnsupportedOperationException();
      }
    } else {
      throw new UnsupportedOperationException();
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public double getVelocity() {
    return movement.getVelocity();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public double getAcceleration() {
    if (movement instanceof MovementWithAccel) {
      return ((MovementWithAccel)movement).getAcceleration();
    } else if (movement instanceof AccelScheduleMovement) {
      return ((AccelScheduleMovement)movement).getAcceleration();
    } else {
      throw new UnsupportedOperationException();
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public AccelSchedule getAccelSchedule() {
    if (movement instanceof AccelScheduleMovement) {
      return ((AccelScheduleMovement)movement).getAccelSchedule();
    } else {
      // TODO: it should throw new UnsupportedOperationException()
      // but for now, return null
      return null;
    }
  }


  /////////////////////////////////
  // PUBLIC METHODS
  /////////////////////////////////

  /**
   * {@inheritDoc}
   */
  @Override
  public Point2D gaugePosition() {
    return memoGaugePosition;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public double gaugeVelocity() {
    return speedometer.read();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public double gaugeHeading() {
    return compass.read();
  }


  /////////////////////////////////
  // PUBLIC METHODS
  /////////////////////////////////

  // derived properties

  /**
   * {@inheritDoc}
   */
  @Override
  public Shape getShape() {
    return memoGetShape;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Shape gaugeShape() {
    return memoGaugeShape;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Shape getShape(double extra) {
    Point2D[] points = spec.getCornerPoints(extra,
                                            movement.getPosition(),
                                            movement.getHeading());
    return GeomUtil.convertPointsToShape(points);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<Line2D> getEdges() {
    return GeomMath.polygonalShapePerimeterSegments(getShape());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Point2D getPointAtMiddleFront(double delta) {
    Point2D p =
        new Point2D.Double(movement.getPosition().getX()
                               + delta * Math.cos(movement.getHeading()),
                           movement.getPosition().getY()
                               + delta * Math.sin(movement.getHeading()));
    return p;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Point2D gaugePointAtMiddleFront(double delta) {
    Point2D mp = gaugePosition();
    double h = gaugeHeading();
    Point2D p = new Point2D.Double(mp.getX() + delta * Math.cos(h),
                                   mp.getY() + delta * Math.sin(h));
    return p;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Point2D gaugePointBetweenFrontWheels() {
    return memoGaugePointBetweenFrontWheels;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Point2D getCenterPoint() {
    return spec.getCenterPoint(movement.getPosition(), movement.getHeading());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Point2D[] getCornerPoints() {
    return spec.getCornerPoints(movement.getPosition(), movement.getHeading());
  }

  /**
   * Get the current global coordinates of the corners of this Vehicle,
   * according to the Vehicle's gauges.
   *
   * @return an array of points representing the four corners, according to
   *         the Vehicle's gauges.
   */
  public Point2D[] gaugeCornerPoints() {
    return spec.getCornerPoints(gaugePosition(),
                                gaugeHeading());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Point2D getPointAtRear() {
    return spec.getPointAtRear(movement.getPosition(),
                               movement.getHeading());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Point2D gaugePointAtRear() {
    return spec.getPointAtRear(gaugePosition(), gaugeHeading());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Point2D gaugeRearLeftCornerPoint() {
    return spec.getRearLeftCornerPoint(gaugePosition(), gaugeHeading());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Point2D gaugeRearRightCornerPoint() {
    return spec.getRearRightCornerPoint(gaugePosition(), gaugeHeading());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Shape[] getWheelShapes() {
    // assume the steering angle is 0
    return spec.getWheelShapes(movement.getPosition(),
                               movement.getHeading(),
                               0.0);
  }

  /////////////////////////////////
  // PUBLIC METHODS
  /////////////////////////////////

  // coontrol

  /**
   * {@inheritDoc}
   */
  @Override
  public void move(double timeStep) {
//    if (Debug.isTargetVIN(vin)) {
//      if (movement instanceof AccelScheduleMovement) {
//        System.err.printf("at time %.2f, as = %s\n", currentTime,
//            ((AccelScheduleMovement)movement).getAccelSchedule());
//      }
//      System.err.printf("%s\n", movement);
//    }
    movement.move(currentTime, timeStep);
    currentTime += timeStep;
    updateGaugesAndMemos();
  }


  /**
   * {@inheritDoc}
   */
  @Override
  public void turnTowardPoint(Point2D p) {
    if (Debug.isPrintVehicleHighLevelControlOfVIN(vin)) {
      System.err.printf("vin %d turnTowardPoint()\n", vin);
    }
    // Get the angle to the point, relative to the angle the vehicle is
    // already facing
    double angle = GeomMath.angleToPoint(p, memoGaugePointBetweenFrontWheels);
    // Need to recenter this value to [-pi, pi]
    double newSteeringAngle =
        Util.recenter(angle - movement.getHeading(), -1.0 * Math.PI, Math.PI);


    Movement m2 = movement;
    if (m2 instanceof AccelScheduleMovement) {
      m2 = ((AccelScheduleMovement) movement).getBaseMovement();
    }

    if (m2 instanceof PhysicalMovement) {
      PhysicalMovement m3 = (PhysicalMovement) m2;
      NonAccelMovement m4 = m3.getNonAccelMovement();
      if (m4 instanceof SteeringMovement) {
        SteeringMovement m5 = (SteeringMovement)m4;
        m5.setSteeringAngleWithBound(newSteeringAngle);
      } else {
        throw new UnsupportedOperationException();
      }
    } else {
      throw new UnsupportedOperationException();
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void coast() {
    if (Debug.isPrintVehicleHighLevelControlOfVIN(vin)) {
      System.err.printf("vin %d coast()\n", vin);
    }
    switchToMoveToTargetVelocityMovement().coast();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void slowToStop() {
    if (Debug.isPrintVehicleHighLevelControlOfVIN(vin)) {
      System.err.printf("vin %d slowToStop()\n", vin);
    }
    switchToMoveToTargetVelocityMovement().slowToStop();
  }

  /**
   * Set the Vehicle's acceleration to its maximum value.  Obeys limits on
   * setMaxAccelWithMaxTargetVelocity as well as disabilities.
   */
  public void setMaxAccelWithMaxTargetVelocity() {
    if (Debug.isPrintVehicleHighLevelControlOfVIN(vin)) {
      System.err.printf("vin %d accelerate()\n", vin);
    }
    switchToMoveToTargetVelocityMovement().setMaxAccelWithMaxTargetVelocity();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setTargetVelocityWithMaxAccel(double targetVelocity) {
    if (Debug.isPrintVehicleHighLevelControlOfVIN(vin)) {
      System.err.printf("vin %d accelToTargetVelocity()\n", vin);
    }
    switchToMoveToTargetVelocityMovement().setTargetVelocityWithMaxAccel(
        targetVelocity);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setAccelWithMaxTargetVelocity(double acceleration) {
    if (Debug.isPrintVehicleHighLevelControlOfVIN(vin)) {
      System.err.printf("vin %d setAccelWithExtremeTargetVelocity()\n", vin);
    }
    switchToMoveToTargetVelocityMovement().setAccelWithMaxTargetVelocity(
        acceleration);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setAccelSchedule(AccelSchedule accelSchedule) {
    if (Debug.isPrintVehicleHighLevelControlOfVIN(vin)) {
      System.err.printf("vin %d set accelerate schedule = %s\n", vin, accelSchedule);
    }
    switchToAccelScheduleMovement().setAccelSchedule(accelSchedule);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void removeAccelSchedule() {
    if (Debug.isPrintVehicleHighLevelControlOfVIN(vin)) {
      System.err.printf("vin %d removeAccelSchedule()\n", vin);
    }
    switchToMoveToTargetVelocityMovement();
  }


  /////////////////////////////////
  // PRIVATE METHODS
  /////////////////////////////////

  /**
   * Switch to acceleration schedule movement
   *
   * @return the acceleration schedule movement
   */
  private AccelScheduleMovement switchToAccelScheduleMovement() {
    if (movement instanceof AccelScheduleMovement) {
      return (AccelScheduleMovement) movement;
    } else {
      if (movement instanceof MovementWithAccel) {
        AccelScheduleMovement m =
            new AccelScheduleMovement((MovementWithAccel) movement);
        movement = m;
        return m;
      } else {
        throw new RuntimeException("Cannot switch to AccelScheduleMovement");
      }
    }
  }

  /**
   * Swith to move-to-target-velocity movement.
   *
   * @return move-to-target-velocity movement
   */
  private MoveToTargetVelocityMovement switchToMoveToTargetVelocityMovement() {
    if (movement instanceof MoveToTargetVelocityMovement) {
      return (MoveToTargetVelocityMovement)movement;
    } else {
      if (movement instanceof AccelScheduleMovement) {
        MovementWithAccel m =
            ((AccelScheduleMovement) movement).getBaseMovement();
        assert m instanceof MovementWithAccel;
        movement = m;
        return (MoveToTargetVelocityMovement)m;
      } else {
        throw new RuntimeException("Cannot switch to " +
                                   "MoveToTargetVelocityMovement");
      }
    }
  }

  /**
   * Update gauges and memoes.
   */
  private void updateGaugesAndMemos() {
    clock.record(currentTime);
    xometer.record(movement.getPosition().getX());
    yometer.record(movement.getPosition().getY());
    compass.record(movement.getHeading());
    speedometer.record(movement.getVelocity());

    memoGaugePosition = new Point2D.Double(xometer.read(), yometer.read());
    memoGetShape = GeomUtil.convertPointsToShape(getCornerPoints());
    memoGaugeShape = GeomUtil.convertPointsToShape(gaugeCornerPoints());
    memoGaugePointBetweenFrontWheels =
        spec.getPointBetweenFrontWheels(gaugePosition(), gaugeHeading());
  }

  /////////////////////////////////
  // DEBUG
  /////////////////////////////////

  /**
   * {@inheritDoc}
   */
  @Override
  public void checkCurrentTime(double currentTime) {
    if (!Util.isDoubleEqual(currentTime, this.currentTime,
                            Constants.DOUBLE_EQUAL_WEAK_PRECISION)) {
      System.err.printf("currentTime = %.10f\n", currentTime);
      System.err.printf("this.currentTime = %.10f\n", this.currentTime);
    }
    assert Util.isDoubleEqual(currentTime, this.currentTime,
                              Constants.DOUBLE_EQUAL_WEAK_PRECISION);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void printState() {
    System.err.printf("State of vin %d: %s\n", vin, movement.toString());
  }
}
