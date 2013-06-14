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

import java.util.LinkedList;
import java.util.List;

import aim4.util.Util;

/**
 * The acceleration profile.
 */
public class AccelProfile {

  /////////////////////////////////
  // NESTED CLASSES
  /////////////////////////////////

  /**
   * A pair of acceleration and duration.
   */
  public static class DurAccel {
    /** The duration */
    private double duration;
    /** The acceleration */
    private double acceleration;

    /**
     * Create a pair of acceleration and duration
     *
     * @param duration      the duration
     * @param acceleration  the acceleration
     */
    public DurAccel(double duration, double acceleration) {
      this.duration = duration;
      this.acceleration = acceleration;
    }

    /**
     * Get the duration.
     *
     * @return the duration
     */
    public double getDuration() {
      return duration;
    }

    /**
     * Get the acceleration.
     *
     * @return the acceleration
     */
    public double getAcceleration() {
      return acceleration;
    }

    /**
     * Set the duration.
     *
     * @param duration  the duration
     */
    public void setDuration(double duration) {
      this.duration = duration;
    }

    /**
     * Set the acceleration.
     *
     * @param acceleration  the acceleration
     */
    public void setAcceleration(double acceleration) {
      this.acceleration = acceleration;
    }
  }

  /////////////////////////////////
  // PRIVATE FIELDS
  /////////////////////////////////

  /** The list of pairs of acceleration and duration */
  private List<DurAccel> durAccelList;

  /////////////////////////////////
  // CONSTRUCTORS
  /////////////////////////////////

  /**
   * Create an acceleration profile.
   */
  public AccelProfile() {
    durAccelList = new LinkedList<DurAccel>();
  }

  /////////////////////////////////
  // PUBLIC METHODS
  /////////////////////////////////

  /**
   * Add a duration and acceleration pair to the profile.
   *
   * @param dur  the duration
   * @param acc  the acceleration
   */
  public void add(double dur, double acc) {
    if (!Util.isDoubleZero(dur)) {
      durAccelList.add(new DurAccel(dur, acc));
    }  // otherwise, ignore it
  }

  /**
   * Get the number of acceleration-duration pairs in the profile.
   *
   * @return the number of acceleration-duration pairs
   */
  public int size() {
    return durAccelList.size();
  }

  /**
   * Get the list of the acceleration-duration pairs.
   *
   * @return the list of the acceleration-duration pairs
   */
  public List<DurAccel> getDurAccelList() {
    return durAccelList;
  }

  /**
   * Convert the acceleration profile to an acceleration schedule.
   *
   * @param initTime  the initial time of the acceleration schedule
   */
  public AccelSchedule toAccelSchedule(double initTime) {
    AccelSchedule accelProfile = new AccelSchedule();
    double t = initTime;
    for(DurAccel durAccel : durAccelList) {
      accelProfile.add(t, durAccel.getAcceleration());
      t += durAccel.getDuration();
    }
    // ignore the last acceleration
    return accelProfile;
  }

  /**
   * Convert the acceleration profile to an acceleration schedule.
   *
   * @param initTime   the initial time of the acceleration schedule
   * @param lastAccel  the last acceleration
   */
  public AccelSchedule toAccelSchedule(double initTime, double lastAccel) {
    AccelSchedule accelProfile = new AccelSchedule();
    double t = initTime;
    for(DurAccel durAccel : durAccelList) {
      accelProfile.add(t, durAccel.getAcceleration());
      t += durAccel.getDuration();
    }
    accelProfile.add(t, lastAccel);
    return accelProfile;
  }

}
