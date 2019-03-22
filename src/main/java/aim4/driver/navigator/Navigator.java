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
package aim4.driver.navigator;

import aim4.driver.AutoDriver;
import aim4.im.IntersectionManager;
import aim4.map.Road;

/**
 * An agent that chooses which way a vehicle should go, and uses information
 * from a {@link AutoDriver} to do so.
 */
public interface Navigator {

  /////////////////////////////////
  // PUBLIC METHODS
  /////////////////////////////////

  /**
   * Given the current Road, the IntersectionManager being approached, and
   * a destination Road, find a road that leave the IntersectionManager that
   * will lead to the destination Road.
   *
   * @param current     the Road on which the vehicle is currently traveling
   * @param im          the IntersectionManager the vehicle is approaching
   * @param destination the Road on which the vehicle would ultimately like to
   *                    end up
   * @return            a road to take out of the intersection governed by
   *                    the given IntersectionManager
   */
  Road navigate(Road current, IntersectionManager im, Road destination);

}
