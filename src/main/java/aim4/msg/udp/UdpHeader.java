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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Small header included in all UDP messages sent to/from the real car.
 */
public class UdpHeader {

  /////////////////////////////////
  // CONSTANTS
  /////////////////////////////////

  /** The maximum length of a message package including the header (in bytes) */
  public static final int MAX_MESSENGE_PACKAGE_LENGTH = 1024;

  /** The size of a UDP header (in bytes) */
  public static final int LENGTH = 12;

  /////////////////////////////////
  // NESTED CLASSES
  /////////////////////////////////

  /**
   * The message type.
   */
  public enum UdpMessageType {
    PVUpdate,          // = 0,
    I2V_Confirm,       // = 1,
    I2V_Reject,        // = 2,
    I2V_Acknowledge,   // = 3,
    I2V_EmergencyStop, // = 4,   // TODO: delete this in the future.
    V2I_Request,       // = 5,
    V2I_Cancel,        // = 6,
    V2I_Done,          // = 7
    I2V_DistToFrontVehicle,  // = 8
  }

  /////////////////////////////////
  // PRIVATE FIELDS
  /////////////////////////////////

  /**
   * This reflects some notion of the real vehicle's time.
   *
   * The idea is that the real vehicle sends, as part of each UDP
   * packet's header, a timestamp. The timestamps the real vehicle
   * sends are assumed to be monotomically and strictly increasing,
   * so ProxyVehicle can detect if a UDP packet is received out of
   * order by comparing each UdpHeader's timestamp against the latest
   * received UDP packet.
   */
  private float timestamp;

  /** The type of message. */
  private UdpMessageType messageType;

  /**
   * A simple checksum to validate the UDP packet. UDP makes no effort
   * to protect against corrupted packets, so we attach a simple checksum
   * to each UDP package.
   * TODO: computing the checksums and checksum checking
   */
  private int checksum;

  /////////////////////////////////
  // CONSTRUCTORS
  /////////////////////////////////

  /**
   * Construct the header given a DataInputStream wrapped around a
   * DatagramPacket received over UDP from the real car.
   *
   * @param dis The DataInputStream.
   * @throws IOException
   */
  public UdpHeader(DataInputStream dis) throws IOException {
    assert(dis.available() >= LENGTH);
    // Populate the class members from the DataInputStream
    timestamp = dis.readFloat();
    messageType = UdpMessageType.values()[dis.readInt()];
    checksum = dis.readInt();
  }

  /**
   * Create a header for a particular message type
   *
   * @param currentTime  the current time
   * @param messageType  the message type
   */
  public UdpHeader(float currentTime, UdpMessageType messageType) {
    timestamp = currentTime;
    this.messageType = messageType;
    checksum = 0;  // need to fix it later.
  }

  /////////////////////////////////
  // PUBLIC METHODS
  /////////////////////////////////

  // info retrieval

  /**
   * Get the time stamp.
   *
   * @return the timestamp
   */
  public float getTimestamp() {
    return timestamp;
  }

  /**
   * Get the message type.
   *
   * @return the message type
   */
  public UdpMessageType getMessageType() {
    return messageType;
  }

  /**
   * Get the check sum.
   *
   * @return the check sum
   */
  public int getChecksum() {
    return checksum;
  }

  /////////////////////////////////
  // PUBLIC METHODS
  /////////////////////////////////

  // utility

  /**
   * Write the data header to an I/O stream.
   *
   * @param dos the I/O stream
   * @return the I/O stream
   * @throws IOException
   */
  public DataOutputStream writeToDataOutputStream(DataOutputStream dos)
      throws IOException {
    dos.writeFloat(timestamp);
    dos.writeInt(messageType.ordinal());
    dos.writeInt(checksum);
    return dos;
  }

  /////////////////////////////////
  // PUBLIC STATIC METHODS
  /////////////////////////////////

  /**
   * Compute the check sum for an array of bytes.
   *
   * @param data  the array of bytes
   * @return the check sum
   */
  public static int computeChecksum(byte[] data) {
    // TODO: implement this function later
    return 0;
  }

  /////////////////////////////////
  // DEBUG
  /////////////////////////////////

  /**
   * {@inheritDoc}
   */
  @Override
  public String toString() {
    return "Timestamp=" + timestamp + "\tMessage type=" + messageType
      + "\tchecksum=" + checksum;
  }

}
