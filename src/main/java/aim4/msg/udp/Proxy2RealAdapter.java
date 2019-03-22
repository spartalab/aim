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
package aim4.msg.udp;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.SocketAddress;

import aim4.msg.i2v.Confirm;
import aim4.msg.i2v.Reject;
import aim4.msg.udp.UdpHeader.UdpMessageType;

/**
 * The proxy vehicle to real vehicle message adapter.
 */
public abstract class Proxy2RealAdapter {

  /**
   * Construct a DatagramPacket of this confirm message
   *
   * @param sa           SocketAddress of the intended destination of the
   *                     datagram
   * @param currentTime  absolute time in seconds
   *
   * @return a DatagramPacket object which can be sent over UDP representing
   *         this message
   * @throws IOException
   */
  public static DatagramPacket toDatagramPacket(Confirm msg,
                                                SocketAddress sa,
                                                double currentTime)
                                               throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    DataOutputStream dos =
      getDosWithHeader(baos, currentTime, UdpMessageType.I2V_Confirm);
    assert dos.size() == UdpHeader.LENGTH;

    dos.writeInt(msg.getReservationId());
    // arrival_time is relative
    dos.writeFloat((float) (msg.getArrivalTime() - currentTime));
    dos.writeFloat((float) msg.getEarlyError());
    dos.writeFloat((float) msg.getLateError());
    dos.writeFloat((float) msg.getArrivalVelocity());
    double accel = msg.getAccelerationProfile().peek()[0];
    // ignore other acceleration for now
    // TODO: fix it in the future
    dos.writeFloat((float) accel);
//    System.out.printf("I2V_Confirm: acceleration for car to use is %.2f\n",
//                      accel);

    int udpPacketSize = UdpHeader.LENGTH + 24;
    assert (baos.size() == udpPacketSize);
    DatagramPacket dp =
      new DatagramPacket(baos.toByteArray(), udpPacketSize, sa);
    return dp;
  }

  /**
   * Construct a DatagramPacket of this reject message
   *
   * @param sa           SocketAddress of the intended destination of the
   *                     datagram
   * @param currentTime  absolute time in seconds
   *
   * @return a DatagramPacket object which can be sent over UDP representing
   *         this message
   * @throws IOException
   */
  public static DatagramPacket toDatagramPacket(Reject msg,
                                                SocketAddress sa,
                                                double currentTime)
                                               throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    DataOutputStream dos =
      getDosWithHeader(baos, currentTime, UdpMessageType.I2V_Reject);
    assert dos.size() == UdpHeader.LENGTH;

    int udpPacketSize = UdpHeader.LENGTH;
    assert (baos.size() == udpPacketSize);
    DatagramPacket dp =
      new DatagramPacket(baos.toByteArray(), udpPacketSize, sa);
    return dp;
  }

  /**
   * Construct a DatagramPacket of this confirm message
   *
   * @param distToFrontVehicle  the distance of the vehicles in front
   * @param sa                  SocketAddress of the intended destination of the
   *                            datagram
   * @param currentTime  absolute time in seconds
   *
   * @return a DatagramPacket object which can be sent over UDP representing
   *         this message
   * @throws IOException
   */
  public static DatagramPacket toDatagramPacket(double distToFrontVehicle,
                                                SocketAddress sa,
                                                double currentTime)
                                               throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    DataOutputStream dos =
      getDosWithHeader(baos, currentTime,UdpMessageType.I2V_DistToFrontVehicle);
    assert dos.size() == UdpHeader.LENGTH;

    dos.writeFloat((float) distToFrontVehicle);

    int udpPacketSize = UdpHeader.LENGTH + 4;
    assert (baos.size() == udpPacketSize);
    DatagramPacket dp =
      new DatagramPacket(baos.toByteArray(), udpPacketSize, sa);
    return dp;
  }


  /**
   * Builds a header for the type of this message, and writes it to a new
   * DataOutputStream wrapped around a given ByteArrayOutputStream
   *
   * @param baos         The ByteArrayOutputStream
   * @param currentTime  The current, absolute time in seconds
   * @return A DataOutputStream object backed by the given baos containing a
   *         UdpHeader
   * @throws IOException
   */
  public static DataOutputStream getDosWithHeader(ByteArrayOutputStream baos,
                                                  double currentTime,
                                                  UdpMessageType type)
                                                 throws IOException
  {
    DataOutputStream dos = new DataOutputStream(baos);
    UdpHeader header = new UdpHeader((float)currentTime, type);
    // TODO: compute and set the checksum
    header.writeToDataOutputStream(dos);
    return dos;
  }
}
