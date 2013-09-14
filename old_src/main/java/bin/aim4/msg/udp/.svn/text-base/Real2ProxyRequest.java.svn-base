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
import java.io.IOException;

import aim4.config.Constants;

/**
 * A real vehicle to proxy vehicle message for request message.
 */
public class Real2ProxyRequest extends Real2ProxyMsg {

  /////////////////////////////////
  // PUBLIC FINAL FIELDS
  /////////////////////////////////

  /** The VIN of the vehicle */
  public final int vin;
  /** The arrival time span */
  public final float arrivalTimeSpan;
  /** The arrival velocity */
  public final float arrivalVelocity;
  /** The departure lane ID */
  public final int departureLaneId;

  /////////////////////////////////
  // CONSTRUCTORS
  /////////////////////////////////

  /**
   * Create a real vehicle to proxy vehicle message for request message.
   *
   * @param dis           the I/O stream
   * @param receivedTime  the time stamp
   * @throws IOException
   */
  public Real2ProxyRequest(DataInputStream dis, double receivedTime)
      throws IOException {
    super(Type.REQUEST, receivedTime);
    vin = dis.readInt();
    arrivalTimeSpan = dis.readFloat();
    departureLaneId = dis.readInt();
    arrivalVelocity = dis.readFloat();
  }

  /////////////////////////////////
  // DEBUG
  /////////////////////////////////

  /**
   * {@inheritDoc}
   */
  @Override
  public String toString() {
    String s = "Real2ProxyRequest(";
    s += "vin=" + vin + ",";
    s += "arrivalTimeSpan=" + Constants.TWO_DEC.format(arrivalTimeSpan) + ",";
    s += "arrivalVelocity=" + Constants.TWO_DEC.format(arrivalVelocity) + ",";
    s += "departureLaneId=" + departureLaneId;
    s += ")";
    return s;
  }
}
