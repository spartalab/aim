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

import aim4.config.Constants;
import aim4.util.Util;

/**
 * The acceleration schedule
 */
public class AccelSchedule {

  /////////////////////////////////
  // NESTED CLASSES
  /////////////////////////////////

  /**
   * The time-acceleration pair.
   */
  public static class TimeAccel {
    /** The time */
    private double time;
    /** The acceleration */
    private double acceleration;

    /**
     * Create a time-acceleration pair.
     *
     * @param time          the time
     * @param acceleration  the acceleration
     */
    public TimeAccel(double time, double acceleration) {
      this.time = time;
      this.acceleration = acceleration;
    }

    /**
     * Get the time.
     *
     * @return the time
     */
    public double getTime() {
      return time;
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
     * {@inheritDoc}
     */
    @Override
    public String toString() {
      return "(" + Constants.TWO_DEC.format(acceleration) + " at time " +
             Constants.TWO_DEC.format(time) + ")";

    }
  }

  /////////////////////////////////
  // PRIVATE FIELDS
  /////////////////////////////////

  /** The list of time-acceleration pairs */
  private List<TimeAccel> timeAccelList;

  /////////////////////////////////
  // CONSTRUCTORS
  /////////////////////////////////

  /**
   * Create an acceleration schedule.
   */
  public AccelSchedule() {
    timeAccelList = new LinkedList<TimeAccel>();
  }

  /////////////////////////////////
  // PUBLIC METHODS
  /////////////////////////////////

  /**
   * Add a time-acceleration pair to the acceleration schedule.
   *
   * @param time   the time
   * @param accel  the acceleration
   */
  public void add(double time, double accel) {
    timeAccelList.add(new TimeAccel(time, accel));
  }

  /**
   * Get the number of time-acceleration pairs in the acceleration schedule.
   *
   * @return the number of time-acceleration pairs in the acceleration schedule.
   */
  public int size() {
    return timeAccelList.size();
  }

  /**
   * Get the list of time-acceleration pairs.
   */
  public List<TimeAccel> getList() {
    return timeAccelList;
  }

  /**
   * Calculate the velocity after executing the acceleration schedule,
   * starting from the initial velocity.  The last acceleration will
   * be ignored.
   *
   * @param v1  the initial velocity
   * @return the velocity after executing the acceleration schedule
   */
  public double calcFinalVelocity(double v1) {
    TimeAccel lastTa = null;
    for(TimeAccel ta : timeAccelList) {
      if (lastTa != null) {
        v1 += (ta.getTime() - lastTa.getTime()) * lastTa.getAcceleration();
      }
      lastTa = ta;
    }
    return v1;
  }

  /**
   * Whether the velocity does not exceed the velocity's upper limit
   * at any time point during the execution of the acceleration schedule
   *
   * @param v1    the initial velocity
   * @param vTop  the velocity's upper limit
   *
   * @return Whether the velocity does not exceed the velocity's upper limit
   *         at all time during the execution of this acceleration schedule.
   */
  public boolean checkVelocityUpperLimit(double v1, double vTop) {
    if (!Util.isDoubleEqualOrLess(v1, vTop)) {
      return false;
    }
    TimeAccel lastTa = null;
    for(TimeAccel ta : timeAccelList) {
      if (lastTa != null) {
        v1 += (ta.getTime() - lastTa.getTime()) * lastTa.getAcceleration();
        if (Util.isDoubleNotEqual(v1, vTop) && v1 > vTop) {
          return false;
        }
      }
      lastTa = ta;
    }
    return true;
  }

  /**
   * Calculate the distance traveled by the vehicle starting at a given
   * time and velocity and ending at a given time.
   *
   * @param time1    the initial time
   * @param v1       the initial velocity
   * @param timeEnd  the final time
   *
   * @return the distance traveled
   */
  public double calcTotalDistance(double time1, double v1, double timeEnd) {
    assert time1 < timeEnd;

    int i = 0;
    double accel = 0.0;

    while(i<timeAccelList.size()) {
      TimeAccel ta = timeAccelList.get(i);
      if (time1 < ta.getTime()) break;
      accel = ta.getAcceleration();
      i++;
    }

    double time = time1;
    double v = v1;
    double dTotal = 0.0;

    while(i<timeAccelList.size()) {
      TimeAccel ta = timeAccelList.get(i);
      if (timeEnd < ta.getTime()) break;  // can be <=
      double t = ta.getTime() - time;
      double v2 = v + accel * t;
      dTotal += t * (v + v2) / 2.0;
      time = ta.getTime();
      v = v2;
      accel = ta.getAcceleration();
      i++;
    }

    double t = timeEnd - time;
    double v2 = v + accel * t;
    dTotal += t * (v + v2) / 2.0;

    return dTotal;
  }


  /**
   * Calculate the distance and velocity traveled by the vehicle starting at
   * a given time and velocity and ending at a given time.
   *
   * @param time1    the initial time
   * @param v1       the initial velocity
   * @param timeEnd  the final time
   *
   * @return  the velocity after executing the acceleration schedule after a
   *          period of time t
   */
  public double[] calcFinalDistanceAndVelocity(double time1,
                                               double v1,
                                               double timeEnd) {
    assert time1 < timeEnd;

    int i = 0;
    double accel = 0.0;

    while(i<timeAccelList.size()) {
      TimeAccel ta = timeAccelList.get(i);
      if (time1 < ta.getTime()) break;
      accel = ta.getAcceleration();
      i++;
    }

    double time = time1;
    double v = v1;
    double dTotal = 0.0;

    while(i<timeAccelList.size()) {
      TimeAccel ta = timeAccelList.get(i);
      if (timeEnd < ta.getTime()) break;  // can be <=
      double t = ta.getTime() - time;
      double v2 = v + accel * t;
      dTotal += t * (v + v2) / 2.0;
      time = ta.getTime();
      v = v2;
      accel = ta.getAcceleration();
      i++;
    }

    double t = timeEnd - time;
    double v2 = v + accel * t;
    dTotal += t * (v + v2) / 2.0;

    return new double[] { dTotal, v2 };
  }

  /**
   * Calculate the distance and velocity traveled by the vehicle starting at
   * a given time and velocity and ending at a given time.
   *
   * @param time1    the initial time
   * @param v1       the initial velocity
   * @param dTotal   the distance traveled
   *
   * @return  the velocity after executing the acceleration schedule after a
   *          period of time t
   */
  public double[] calcFinalTimeAndVelocity(double time1,
                                           double v1,
                                           double dTotal) {
    int i = 0;
    double accel = 0.0;

    while(i<timeAccelList.size()) {
      TimeAccel ta = timeAccelList.get(i);
      if (time1 < ta.getTime()) break;
      accel = ta.getAcceleration();
      i++;
    }

    double time = time1;
    double v = v1;
    double dSum = 0.0;

    while(i<timeAccelList.size()) {
      TimeAccel ta = timeAccelList.get(i);
      double t = ta.getTime() - time;
      double v2 = v + accel * t;
      double d = t * (v + v2) / 2.0;
      if (dSum + d >= dTotal) break;
      dSum += d;
      time = ta.getTime();
      v = v2;
      accel = ta.getAcceleration();
      i++;
    }

    // Solving:
    //   vEnd = v + accel * t
    //   d = t * (v + vEnd) / 2
    // Solutions:
    //   d = ((vEnd - v) / accel) * (v + vEnd) / 2
    //   d = (vEnd^2 - v^2) / (2 * accel)
    //   2 * accel * d = vEnd^2 - v^2
    //   vEnd^2 = 2 * accel * d + v^2
    //   vEnd = sqrt(2 * accel *d + v^2)
    //   t = (vEnd - v) / accel

    double d = dTotal - dSum;
    if (d > 0.0) {
      if (Util.isDoubleZero(accel)) {
        return null;  // cannot accelerate to finish the remaining distance
      } else {
        if (2.0 * accel * d + v * v >= 0.0) {
          double vEnd = Math.sqrt(2.0 * accel * d + v * v);
          double t = (vEnd - v) / accel;
          double timeEnd = time + t;

          return new double[] { timeEnd, vEnd };
        } else {
          return null;  // decelerate too quickly and cannot finish the
                        // remaining distance
        }
      }
    } else {
      assert Util.isDoubleZero(d);
      return new double[] { time, v };  // no need to move further
    }
  }


  /////////////////////////////////
  // DEBUG
  /////////////////////////////////

  /**
   * {@inheritDoc}
   */
  @Override
  public String toString() {
    String s = "";
    boolean isFirst = true;
    for(TimeAccel ta : timeAccelList) {
      if (isFirst) {
        s += "[";
        isFirst = false;
      } else {
        s += ",";
      }
      s += ta.toString();
    }
    s += "]";
    return s;
  }
}
