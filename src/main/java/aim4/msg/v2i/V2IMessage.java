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
package aim4.msg.v2i;

import aim4.config.Constants;

/**
 * A message sent from a Vehicle to an Intersection Manager.
 */
public abstract class V2IMessage {

  /////////////////////////////////
  // NESTED CLASSES
  /////////////////////////////////

  /**
   * The different types of Vehicle to Intersection Manager
   * messages.
   */
  public enum Type {
    /** Mesage requesting a reservation or a change of reservation. */
    REQUEST,
    /** Message cancelling a currently held reservation. */
    CANCEL,
    /** Message indicating that the vehicle has traversed the intersection. */
    DONE,
    /** Message requesting entry into the admission control zone. */
    ACZ_REQUEST,
    /** Message cancelling a previous ACZ_REQUEST. */
    ACZ_CANCEL,
    /**
     * Message indicating the vehicle has completed entering the admission
     * control zone.
     */
    ACZ_ENTERED,
    /**
     * Message indicating the vehicle has left the admission control zone by
     * leaving the roadway.
     */
    ACZ_EXIT,
    /**
     * Message indicating the vehicle has left the admission control zone by
     * driving straight out of it.
     */
    AWAY,
  };

  /////////////////////////////////
  // PRIVATE FIELDS
  /////////////////////////////////

  /**
   * The ID number of the Intersection Manager to which this message is
   * being sent.
   */
  private int imId;
  /**
   * The ID number of the Vehicle sending this message
   */
  private int vin;

  /////////////////////////////////
  // PROTECTED FIELDS
  /////////////////////////////////

  /** The type of this message. */
  protected Type messageType;

  /**
   * The size, in bits, of this message.
   */
  protected int size = Constants.ENUM_SIZE + 2 * Constants.INTEGER_SIZE;

  /////////////////////////////////
  // CLASS CONSTRUCTORS
  /////////////////////////////////

  /**
   * Class constructor to be called by subclasses to set the source and
   * destination ID numbers.
   *
   * @param vin      the ID number of the Vehicle sending this message
   * @param imID the ID number of the IntersectionManager to which
   *                      this message is being sent
   */
  public V2IMessage(int vin, int imID) {
    this.vin = vin;
    this.imId = imID;
  }

  public V2IMessage(V2IMessage msg) {
    this.vin = msg.vin;
    this.imId = msg.imId;
    this.messageType = msg.messageType;
    this.size = msg.size;
  }

  /////////////////////////////////
  // PUBLIC METHODS
  /////////////////////////////////

  /**
   * Get the ID number of the Intersection Manager to which this
   * message is being sent.
   *
   * @return the ID number of the IntersectionManager to which this message is
   *         being sent
   */
  public int getImId() {
    return imId;
  }

  /**
   * Get the ID number of the Vehicle sending this message.
   *
   * @return the ID number of the Vehicle sending this message
   */
  public int getVin() {
    return vin;
  }

  /**
   * Get the type of this message.
   *
   * @return the type of this message
   */
  public Type getMessageType() {
    return messageType;
  }

  /**
   * Get the size of this message in bits.
   *
   * @return the size of this message in bits
   */
  public int getSize() {
    return size;
  }
}
