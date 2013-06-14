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
package aim4.map.track;

/**
 * A position on a track.
 */
public interface TrackPosition {
  /**
   * Get the x-coordinate of the current position.
   *
   * @return the x-coordinate of the current position.
   */
  double getX();
  /**
   * Get the y-coordinate of the current position.
   *
   * @return the y-coordinate of the current position.
   */
  double getY();
  /**
   * Get the slope of the tangent at the location.
   *
   * @return The slope of the tangent at the location.
   */
  double getTangentSlope();
  /**
   * Move the position by a certain distance on the track but not beyond the
   * end waypoint. Return the remaining distance if it moves beyond the
   * end waypoint.
   *
   * @param dist  the distance
   *
   * @return the remaining distance of the traversal; exactly 0 (that can be
   *         tested with "== 0.0") if the traversal has not reached the
   *         end waypoint.
   */
  double move(double dist);
}
