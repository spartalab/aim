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
package aim4.util;

/**
 * This class provides utility functions for calculating the motion of the
 * vehicles.
 */
public class MotionMath {

  /////////////////////////////////
  // PUBLIC METHODS
  /////////////////////////////////

  /**
   * Calculate the velocity after accelerating for a given distance, starting
   * with a given velocity.
   *
   * @param v1     the initial velocity
   * @param accel  the (constant) acceleration
   * @param dist   the distance traveled
   * @return the end velocity; Double.NaN if there is no solution
   */
  public static double calcEndVelocity(double v1, double accel, double dist) {
    return Math.sqrt(v1 * v1 + 2.0 * accel * dist);
  }

  /**
   * Calculate the time it takes to accelerate (or decelerate) from the
   * initial velocity to the end velocity.
   *
   * @param v1     the initial velocity
   * @param v2     the end velocity
   * @param accel  the acceleration
   * @return the time it takes to accelerate from v1 to v2; Double.NaN if
   *         the acceleration is zero.
   */
  public static double calcDuration(double  v1, double v2, double accel) {
    if (!Util.isDoubleZero(accel)) {
      return (v2 - v1) / accel;
    } else {
      return Double.NaN;
    }
  }

  /**
   * Calculate the time it takes to accelerate (or decelerate) from the
   * initial velocity to the end velocity.  If the acceleration is zero,
   * it uses the distance traveled to compute the time.
   *
   * @param v1       the initial velocity
   * @param v2       the end velocity
   * @param accel    the acceleration
   * @param distance the distance traveled
   * @return the time it takes to accelerate from v1 to v2; Double.NaN if
   *         both the acceleration and the initial velocity is zero.
   */
  public static double calcDuration(double v1, double v2, double accel,
                                    double distance) {
    if (!Util.isDoubleZero(accel)) {
      return (v2 - v1) / accel;
    } else {
      if (!Util.isDoubleZero(v1)) {
        return distance / v1;
      } else {
        return Double.NaN;
      }
    }
  }

  /////////////////////////////////
  // CLASS CONSTRUCTORS
  /////////////////////////////////

  /** This class should never be instantiated. */
  private MotionMath(){};


}
