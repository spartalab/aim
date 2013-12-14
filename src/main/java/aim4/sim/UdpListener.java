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
package aim4.sim;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;

import aim4.config.Debug;
import aim4.driver.ProxyDriver;
import aim4.msg.udp.Real2ProxyCancel;
import aim4.msg.udp.Real2ProxyDone;
import aim4.msg.udp.Real2ProxyMsg;
import aim4.msg.udp.Real2ProxyPVUpdate;
import aim4.msg.udp.Real2ProxyRequest;
import aim4.msg.udp.UdpHeader;
import aim4.vehicle.ProxyVehicle;
import aim4.vehicle.ProxyVehicleSimView;
import aim4.vehicle.VinRegistry;

/**
 * Listens for UDP datagrams from Marvin at a UPD port (default is 46000)
 * for communication from real cars and manages corresponding proxyvehicle's.
 */
public class UdpListener implements Runnable {

  /////////////////////////////////
  // CONSTANTS
  /////////////////////////////////

  /** The default listener UDP port */
  private static final int DEFAULT_LISTENER_UDP_PORT = 46000;

  // TODO: the UDP port of the vehicle should not be hard-coded.
  /** The default UPD port on the vehicle */
  private static final int DEFAULT_VEHICLE_UDP_PORT = 46042;

  /////////////////////////////////
  // PRIVATE FIELDS
  /////////////////////////////////

  /** The UDP port */
  private final int udpPort;

  /** the simulator */
  private final Simulator sim;

  /** Datagram socket for listening on a port over UDP. */
  private DatagramSocket ds;

  /**
   * A map of all the ProxyVehicles, indexed by their respective (unique)
   * socket addresses. the ProxyVehicles also assume this is the reply address
   * when they need to relay information back to the real vehicle.
   */
  private Map<SocketAddress,ProxyVehicleSimView> sa2ProxyVehicle;

  /** The thread of this UDP listener */
  private volatile Thread blinker;

  // For more information about the use of blinker, check
  // Ref: http://java.sun.com/javase/6/docs/technotes/guides/concurrency/
  //            threadPrimitiveDeprecation.html

  /////////////////////////////////
  // CONSTRUCTORS
  /////////////////////////////////

  /**
   * Default constructor uses UDP port 46000
   *
   * @param sim  the simulator
   */
  public UdpListener(Simulator sim) {
    this(DEFAULT_LISTENER_UDP_PORT, sim);
  }

  /**
   * Constructor for a UDP listener on the specified port.
   *
   * @param udpPort  the port to listen on
   * @param sim      the simulator
   */
  public UdpListener(int udpPort, Simulator sim) {
    this.udpPort = udpPort;
    this.sim = sim;
    ds = null;
    sa2ProxyVehicle = new HashMap<SocketAddress,ProxyVehicleSimView>();
    blinker = null;
  }

  /////////////////////////////////
  // DESTRUCTOR
  /////////////////////////////////

  /**
   * Finalize the class by closing the datagram sockets and delete the
   * proxy vehicles.
   */
  @Override
  protected void finalize() throws Throwable {
    blinker = null;
    closeSocket();
    super.finalize();
  }

  /////////////////////////////////
  // PUBLIC METHODS
  /////////////////////////////////

  // start and stop the thread

  /**
   * Whether or not the thread has started to listen to the UDP port.
   *
   * @return whether or not the thread has started to listen to the UDP port.
   */
  public synchronized boolean hasStarted() {
    return blinker != null;
  }

  /**
   * Start the listener thread.
   */
  public synchronized void start() {
    assert blinker == null;
    blinker = new Thread(this);
    blinker.start();
  }

  /**
   * Stop the listener thread.
   */
  public synchronized void stop() {
    assert blinker != null;
    blinker = null;
    closeSocket();
  }

  /////////////////////////////////
  // PUBLIC METHODS
  /////////////////////////////////

  /**
   * {@inheritDoc}
   */
  @Override
  public void run() {
    try {
      ds = new DatagramSocket(udpPort);
    } catch(SocketException e) {
      if (Debug.SHOW_PROXY_VEHICLE_DEBUG_MSG) {
        System.err.println("Cannot open UDP socket.\n");
        e.printStackTrace();
      }
      return;
    }

    int numBytes = UdpHeader.MAX_MESSENGE_PACKAGE_LENGTH;
    Thread thisThread = Thread.currentThread();

    // listen so long as the user hasn't called stop()
    while (blinker == thisThread) {
      DatagramPacket dp = new DatagramPacket(new byte[numBytes], numBytes);

      try {
        ds.receive(dp);   // blocks until data received
      } catch(IOException e) {  // both IOException and NullPointerException
        // Either stop() is called to close the socket, or
        // something is wrong with our socket.
        // Maybe we should distinguish the two cases and
        // inform the user about the second case since it is an
        // error.
        break;
      }
      processIncomingDatagram(dp);
      Thread.yield();  // give other threads a chance to execute
    }

    closeSocket();
    // TODO: also remove all ProxyVehicles from the simulator as well
  }

  /////////////////////////////////
  // PRIVATE METHODS
  /////////////////////////////////

  /**
   * A synchronized function for closing the UPD socket.
   * It prevents the situation in which both the listener thread and the
   * GUI thread close the socket at the same time.
   */
  private synchronized void closeSocket() {
    if (ds != null) {
      if (ds.isConnected()) {
        ds.disconnect();
      }
      if (!ds.isClosed()) {
        ds.close();
      }
      ds = null;
      if (Debug.SHOW_PROXY_VEHICLE_DEBUG_MSG) {
        System.err.println("The UDP socket is closed.");
      }
    }
  }


  /**
   * The main function for processing the incoming datagram.
   *
   * @param dp  the datagram.
   */
  private void processIncomingDatagram(DatagramPacket dp) {
    synchronized(sim) {
      SocketAddress sa = dp.getSocketAddress();
      Real2ProxyMsg msg = convertDatagramToReal2ProxyMsg(dp);

      if (Debug.SHOW_PROXY_VEHICLE_DEBUG_MSG) {
        if (Debug.SHOW_PROXY_VEHICLE_PVUPDATE_MSG ||
            !(msg instanceof Real2ProxyPVUpdate)) {
          System.err.printf("Proxy vehicle received a Real2Proxy msg: %s\n",
                            msg);
        }
      }

      if (msg == null) {
        System.err.println("Error: cannot parse the datagram package.");
        return;
      }

      if (sa2ProxyVehicle.containsKey(sa)) {
        // The datagram came from a real vehicle we're already tracking.
        // Simply forward the datagram to the corresponding proxy vehicle
        sa2ProxyVehicle.get(sa).processReal2ProxyMsg(msg);
      } else {
        // We haven't seem this SA before. This must be coming from
        // a new real vehicle that we're not tracking

        // If it is a PV_UPDATE message, instantiate the proxy vehicle and
        // associate the socket address to this proxy vehicle.
        // If not, ignore the message.
        if (msg.messageType == Real2ProxyMsg.Type.PV_UPDATE) {
          Real2ProxyPVUpdate pvUpdateMsg = (Real2ProxyPVUpdate)msg;
          // create a proxy vehicle for this real vehicle
          ProxyVehicleSimView vehicle = makeProxyVehicle(pvUpdateMsg);
          // check the VIN number
          if (VinRegistry.registerVehicleWithExistingVIN(vehicle,
                                                         pvUpdateMsg.vin)) {
            // update the socket address of the proxy vehicle
            // pull out just the IP <xxx.xxx.xxx.xxx> from the address only
            String address = sa.toString();
            address = address.substring(1, address.indexOf(':'));
            vehicle
              .setSa(new InetSocketAddress(address, DEFAULT_VEHICLE_UDP_PORT));
            // record the proxy vehicle
            sa2ProxyVehicle.put(sa, vehicle);
            // add the proxy vehicle to the simulator
            sim.addProxyVehicle(vehicle);
            if (Debug.SHOW_PROXY_VEHICLE_DEBUG_MSG) {
              System.err.printf("A proxy vehicle is created at time %.2f "
                + "(vin=%d).\n", sim.getSimulationTime(), vehicle.getVIN());
            }
          } else {
            System.err.println("Warning: the VIN of the UPD message has " +
                               "already been used by other vehicles.");
            // don't add the proxy vehicle to the simulator.
          }
        } else {
          // Ignore the message
          if (Debug.SHOW_PROXY_VEHICLE_DEBUG_MSG) {
            System.err.println("Warning: first message from a new real " +
                               "vehicle must be a PVUpdate.");
          }
        }
      }
    }
  }

  /**
   * Covert a datagram to a Real2Proxy message.
   *
   * @param dp  the datagram
   * @return the Real2Proxy message
   */
  private Real2ProxyMsg convertDatagramToReal2ProxyMsg(DatagramPacket dp) {
    // prepare the raw data for reading
    byte[] data = dp.getData();  // it will contain numBytes bytes.
    ByteArrayInputStream bais = new ByteArrayInputStream(data);
    DataInputStream dis = new DataInputStream(bais);
    // read the header
    UdpHeader header = null;
    try {
      header = new UdpHeader(dis);
    } catch(IOException e) {
      System.err.println("Error: Datagram has a corrupted header.");
      return null;
    }

    if (header.getChecksum() != UdpHeader.computeChecksum(data)) {
      // the datagram is corrupted, can't use it
      System.err.println("Error: Datagram has a corrupted checksum.");
      return null;
    }

    Real2ProxyMsg msg = null;
    switch(header.getMessageType()) {
    case PVUpdate:
      try {
        msg = new Real2ProxyPVUpdate(dis, sim.getSimulationTime());
      } catch(IOException e) {
        System.err.println("Error: Datagram has a corrupted body for " +
                           "a PV_UPDATE message.");
      }
      break;
    case V2I_Request:
      try {
        msg = new Real2ProxyRequest(dis, sim.getSimulationTime());
      } catch(IOException e) {
        System.err.println("Error: Datagram has a corrupted body for " +
                           "a REQUEST message.");
      }
      break;
    case V2I_Cancel:
      try {
        msg = new Real2ProxyCancel(dis, sim.getSimulationTime());
      } catch(IOException e) {
        System.err.println("Error: Datagram has a corrupted body for " +
                           "a CANCEL message.");
      }
      break;
    case V2I_Done:
      try {
        msg = new Real2ProxyDone(dis, sim.getSimulationTime());
      } catch(IOException e) {
        System.err.println("Error: Datagram has a corrupted body for " +
                           "a DONE message.");
      }
      break;
    default:
      System.err.println("Error: Unknown UDP message type");
    }

//    try {
//      int n = dis.available();
//      if (n > 0) {
//        System.err.println("Warning: processed the datagram, but there " +
//                           "are still " + n + " bytes available.");
//      }
//    } catch(IOException e) {
//      System.err.println("Error: Cannot estimate the number of remaining " +
//                         "data in the datagram");
//    }

    return msg;
  }


  /**
   * Create a proxy vehicle
   *
   * @param msg  the PV update message
   * @return the proxy vehicle
   */
  private ProxyVehicleSimView makeProxyVehicle(Real2ProxyPVUpdate msg) {
    ProxyVehicleSimView vehicle = new ProxyVehicle(msg.position,
                                                   msg.heading,
                                                   msg.steeringAngle,
                                                   msg.velocity,
                                                   msg.targetVelocity,
                                                   msg.acceleration,
                                                   msg.receivedTime);
    vehicle.setDriver(new ProxyDriver(vehicle, sim.getMap()));

    assert vehicle.getDriver() != null;
    return vehicle;
  }

}
