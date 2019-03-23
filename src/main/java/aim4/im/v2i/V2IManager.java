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
package aim4.im.v2i;

import java.awt.Shape;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;

import aim4.config.Debug;
import aim4.im.Intersection;
import aim4.im.IntersectionManager;
import aim4.im.TrackModel;
import aim4.im.v2i.policy.Policy;
import aim4.im.v2i.reservation.AczManager;
import aim4.im.v2i.reservation.AdmissionControlZone;
import aim4.im.v2i.reservation.ReservationGrid;
import aim4.im.v2i.reservation.ReservationGridManager;
import aim4.map.lane.Lane;
import aim4.msg.i2v.I2VMessage;
import aim4.msg.v2i.V2IMessage;
import aim4.sim.StatCollector;
import aim4.util.Registry;
import aim4.util.TiledArea;

/**
 * An intersection manager that takes requests from vehicles and coordinates
 * their traversals of the intersection to ensure that there are no
 * collisions.  The V2IManager makes its decisions using an intersection
 * control {@link Policy}.
 */
public class V2IManager extends IntersectionManager
                        implements V2IManagerCallback {

  /////////////////////////////////
  // CONSTANTS
  /////////////////////////////////

  /**
   * The maximum amount of time, in seconds, in the future, for which the
   * policy will accept reservation requests. {@value} seconds.
   */
  public static final double MAXIMUM_FUTURE_RESERVATION_TIME = 10.0; // sec

  /**
   * The default distance the IntersectionManager can transmit messages.
   * {@value} meters.
   */
  private static final double DEFAULT_TRANSMISSION_POWER = 350.0; // meters
  /**
   * The default size (capacity) of an {@link AdmissionControlZone} for Lanes
   * exiting the intersection managed by this V2IManager, in meters. {@value}
   * meters.
   */
  private static final double DEFAULT_ACZ_SIZE = 40.0; // meters
  /**
   * The length, in meters, of the AdmissionControlZone for which to return
   * a debug shape.
   */
  private static final double ACZ_DISTANCE_SHAPE_LENGTH = 1; // meter


  /////////////////////////////////
  // PRIVATE FIELDS
  /////////////////////////////////

  /**
   * The intersection control policy
   */
  private Policy policy;

  // messaging

  /**
   * The maximum distance the intersection manager can transmit a message, in
   * meters.
   */
  private double transmissionPower = DEFAULT_TRANSMISSION_POWER;
  /** A List of messages received from Vehicles waiting to be processed. */
  private List<V2IMessage> inbox = new ArrayList<V2IMessage>();
  /** A List of messages waiting to be sent to Vehicles. */
  private List<I2VMessage> outbox = new ArrayList<I2VMessage>();
  /** The number of bits this IntersectionManager has received. */
  private int bitsReceived;
  /** The number of bits this IntersectionManager has transmitted. */
  private int bitsTransmitted;


  // intersection

  /**
   * The tiled area of the intersection
   */
  private TiledArea tiledArea;
  /**
   * The reservation System
   */
  private ReservationGrid reservationGrid;
  /**
   * The manager for the reservation grid
   */
  private ReservationGridManager reservationGridManager;

  // aczs

  /**
   * A map from each outgoing lane's id to the admission control zone that
   * governs the lane just outside the intersection.
   */
  private Map<Integer,AdmissionControlZone> aczs =
    new LinkedHashMap<Integer,AdmissionControlZone>();

  /**
   * The ACZ managers
   */
  private Map<Integer,AczManager> aczManagers =
    new LinkedHashMap<Integer,AczManager>();


  /////////////////////////////////
  // CLASS CONSTRUCTORS
  /////////////////////////////////

  /**
   * Construct a new V2IManager given the structure of Lanes in the
   * intersection.
   *
   * @param intersection  an intersection
   * @param trackModel    a path model of the intersection
   * @param currentTime   the current time
   * @param registry      an intersection manager registry
   */
  public V2IManager(Intersection intersection,
                    TrackModel trackModel,
                    double currentTime,
                    ReservationGridManager.Config config,
                    Registry<IntersectionManager> registry) {
    // Use the superclass's constructor to set up all the physical
    // properties of the intersection
    super(intersection, trackModel, currentTime, registry);
    // Set up the reservation grid
    this.tiledArea = new TiledArea(intersection.getArea(),
                                   config.getGranularity());
    this.reservationGrid = new ReservationGrid(tiledArea.getXNum(),
                                               tiledArea.getYNum(),
                                               config.getGridTimeStep());
    this.reservationGridManager = new ReservationGridManager(config,
                                                             intersection,
                                                             tiledArea,
                                                             reservationGrid);
    // Set up the AdmissionControlZones for the exit lanes
    for(Lane l : getIntersection().getExitLanes()) {
      // This controls how much "length" of vehicles is allowed in at once
      AdmissionControlZone acz = new AdmissionControlZone(DEFAULT_ACZ_SIZE);
      aczs.put(l.getId(), acz);
      aczManagers.put(l.getId(), new AczManager(acz));
    }
  }


  /////////////////////////////////
  // PUBLIC METHODS
  /////////////////////////////////

  /**
   * Get the policy.
   */
  public Policy getPolicy() {
    return policy;
  }

  /**
   * set the policy.
   *
   * @param policy  the policy
   */
  public void setPolicy(Policy policy) {
    this.policy = policy;
  }


  /////////////////////////////////
  // PUBLIC METHODS
  /////////////////////////////////

  // act()

  /**
   * Give the V2IManager a chance to respond to messages from vehicles, change
   * policies, and so forth.
   *
   * @param timeStep  the size of the time step to simulate, in seconds
   */
  @Override
  public void act(double timeStep) {
    // First, process all the incoming messages waiting for us
    for(Iterator<V2IMessage> iter = inboxIterator(); iter.hasNext();) {
      V2IMessage msg = iter.next();
      if (Debug.isPrintIMInboxMessageOfVIN(msg.getVin())) {
        System.err.printf("im %d process message of vin %d: %s\n",
                          getId(), msg.getVin(), msg);
      }
      processV2IMessage(msg);
    }
    // Done processing, clear the inbox.
    clearInbox();
    // Second, allow the policy to act, and send outgoing messages.
    policy.act(timeStep);
    // Third, allow the reservation grid manager to act
    reservationGridManager.act(timeStep);
    // Advance current time.
    super.act(timeStep);
  }


  /////////////////////////////////
  // PUBLIC METHODS
  /////////////////////////////////

  // messaging

  /**
   * Get the IntersectionManager's transmission power.
   *
   * @return the IntersectionManager's transmission power, in meters
   */
  public double getTransmissionPower() {
    return transmissionPower;
  }

  // Communications

  /**
   * Get an iterator for the messages waiting to be read.
   *
   * @return an iterator for the messages waiting to be read.
   */
  public Iterator<V2IMessage> inboxIterator() {
    return inbox.iterator();
  }

  /**
   * Clear out the inbox.
   */
  public void clearInbox() {
    inbox.clear();
  }

  /**
   * Get an iterator for the messages waiting to be delivered from this
   * IntersectionManager.
   *
   * @return an iterator for the messages waiting to be delivered from
   *         this IntersectionManager
   */
  public Iterator<I2VMessage> outboxIterator() {
    return outbox.iterator();
  }

  /**
   * Clear out the outbox.
   */
  public void clearOutbox() {
    outbox.clear();
  }

  /**
   * Adds a message to the incoming queue of messages delivered to this
   * IntersectionManager.
   *
   * @param msg the message to be received
   */
  public void receive(V2IMessage msg) {
    // Just tack the message on to the end of the inbox list
    inbox.add(msg);
    // And count the bits.
    bitsReceived += msg.getSize();
  }

  /**
   * Get the number of bits this IntersectionManager has received.
   *
   * @return the number of bits this IntersectionManager has received
   */
  public int getBitsReceived() {
    return bitsReceived;
  }

  /**
   * Get the number of bits this IntersectionManager has transmitted.
   *
   * @return the number of bits this IntersectionManager has transmitted
   */
  public int getBitsTransmitted() {
    return bitsTransmitted;
  }


  /////////////////////////////////
  // PUBLIC METHODS
  /////////////////////////////////

  // V2IManagerCallback

  /**
   * Get the reservation grid.
   *
   * @return the reservation grid
   */
  @Override
  public ReservationGrid getReservationGrid() {
    return reservationGrid;
  }


  /**
   * Get the manager of the reservation grid
   *
   * @return the manager of the reservation grid
   */
  @Override
  public ReservationGridManager getReservationGridManager() {
    return reservationGridManager;
  }

  /**
   * Get the Admission Control Zone of a given lane.
   *
   * @param laneId  the id of the lane
   * @return the admission control zone of the lane.
   */
  @Override
  public AdmissionControlZone getACZ(int laneId) {
    return aczs.get(laneId);
  }

  /**
   * Get the manager of an ACZ
   */
  @Override
  public AczManager getAczManager(int laneId) {
    return aczManagers.get(laneId);
  }


  /**
   * Adds a message to the outgoing queue of messages to be delivered to a
   * Vehicle.
   *
   * @param msg the message to send to a Vehicle
   */
  @Override
  public void sendI2VMessage(I2VMessage msg) {
    if (Debug.isPrintIMOutboxMessageOfVIN(msg.getVin())) {
      System.err.printf("im %d sends a message to vin %d: %s\n",
                        getId(), msg.getVin(), msg);
    }
    outbox.add(msg);
    bitsTransmitted += msg.getSize();
  }


  /////////////////////////////////
  // PRIVATE METHODS
  /////////////////////////////////

  // process V2I messages

  /**
   * Process a V2I message
   *
   * @param msg  the V2I message
   */
  private void processV2IMessage(V2IMessage msg) {
    policy.processV2IMessage(msg);
  }


  /////////////////////////////////
  // PUBLIC METHODS
  /////////////////////////////////

  // statistics

  /**
   * Print the collected data to a file.
   *
   * @param outFileName  the name of the file to which the data are outputted.
   */
  @Override
  public void printData(String outFileName) {
    PrintStream outfile = null;
    try {
      outfile = new PrintStream(outFileName);
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
    if (outfile != null) {
      StatCollector<?> statCollector = policy.getStatCollector();
      if (statCollector != null) {
        statCollector.print(outfile);
      }

      StatCollector<ReservationGridManager> gridStatCollector =
        reservationGridManager.getStatCollector();
      if (gridStatCollector != null) {
        gridStatCollector.print(outfile);
      }
    }
  }


  /////////////////////////////////
  // DEBUG
  /////////////////////////////////

  /**
   * Get any shapes to display for debugging purposes.
   *
   * @return any shapes to display for debugging purposes
   */
  @Override
  public List<? extends Shape> getDebugShapes() {
    List<Shape> dbg = getACZDebugShapes();
    dbg.addAll(reservationGridManager.getDebugShapes());
    return dbg;
  }


  /**
   * Get the outside part of the lanes that are within the ACZ distance.
   *
   * @return the outside part of the lanes that are within the ACZ distance
   */
  private List<Shape> getACZDebugShapes() {
    List<Shape> laneShapes = new ArrayList<Shape>();
    for(Lane lane : getIntersection().getExitLanes()) {
      double aczEndPos =
        lane.normalizedDistanceAlongLane(getIntersection().getExitPoint(lane))
          + aczs.get(lane.getId()).getMaxSize() / lane.getLength();
      if (aczEndPos > 0) {
        double end = aczEndPos + ACZ_DISTANCE_SHAPE_LENGTH / lane.getLength();
        laneShapes.add(lane.getShape(aczEndPos, end));
      }

      double aczCapPos =
        lane.normalizedDistanceAlongLane(getIntersection().getExitPoint(lane))
          + (aczs.get(lane.getId()).getMaxSize() - aczs.get(lane.getId())
            .getCurrentSize()) / lane.getLength();
      if (aczCapPos > 0) {
        double end = aczCapPos + ACZ_DISTANCE_SHAPE_LENGTH / lane.getLength();
        laneShapes.add(lane.getShape(aczCapPos, end));
      }
    }
    return laneShapes;
  }


}
