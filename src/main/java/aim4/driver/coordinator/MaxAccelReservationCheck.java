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
package aim4.driver.coordinator;

import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.List;

import aim4.config.Constants;
import aim4.util.Util;
import aim4.vehicle.AccelSchedule;
import aim4.vehicle.AccelSchedule.TimeAccel;

/**
 * This class provides functions to solve the validation problem.
 */
public class MaxAccelReservationCheck {

  /////////////////////////////////
  // CONSTANTS
  /////////////////////////////////

  /**
   * Whether the debugging mode is on.
   */
  private static final boolean isDebugging = true;


  /////////////////////////////////
  // NESTED CLASSES
  /////////////////////////////////

  /**
   * A specification of a trapezoid.
   */
  private static class TrapezoidSpec {

    /////////////////////////////////
    // PRIVATE FIELDS
    /////////////////////////////////

    /**
     * The x-coordinate of the lower left point.
     */
    private double refX;

    /**
     * The y-coordinate of the lower left point.
     */
    private double refY;

    /**
     * The height.
     */
    private double h;

    /**
     * The length of the lower parallel side.
     */
    private double w1;

    /**
     * The length of the upper parallel side.
     */
    private double w2;

    /**
     * The difference of the x-coordinate of the left most point
     * of the lower parallel side and the  x-coordinator of the
     * left most point of the upper parallel side.
     */
    private double x;

    /**
     * The area of this trapezoid.
     */
    private double area;

    /////////////////////////////////
    // CONSTRUCTORS
    /////////////////////////////////

    /**
     *
     * @param refX  The x-coordinate of the lower left point
     * @param refY  The y-coordinate of the lower left point
     * @param h     The height
     * @param w1    The length of the lower parallel side
     * @param w2    The length of the upper parallel side
     * @param x     The difference of the x-coordinate of the left most point
     *              of the lower parallel side and the  x-coordinator of the
     *              left most point of the upper parallel side.
     */
    public TrapezoidSpec(double refX, double refY, double h, double w1,
                         double w2, double x) {
      this.refX = refX;
      this.refY = refY;
      this.h = h;
      this.w1 = w1;
      this.w2 = w2;
      this.x = x;
      this.area = h * (w1 + w2) / 2.0;
    }

    /////////////////////////////////
    // PUBLIC METHODS
    /////////////////////////////////

    /**
     * Get the x-coordinate of the lower left point.
     *
     * @return the x-coordinate of the lower left point.
     */
    public double getRefX() {
      return refX;
    }

    /**
     * Get the y-coordinate of the lower left point.
     *
     * @return the y-coordinate of the lower left point.
     */
    public double getRefY() {
      return refY;
    }

    /**
     * Get the height.
     *
     * @return the height
     */
    public double getH() {
      return h;
    }

    /**
     * Get the length of the lower parallel side.
     *
     * @return the length of the lower parallel side.
     */
    public double getW1() {
      return w1;
    }

    /**
     * Get the length of the upper parallel side.
     *
     * @return the length of the upper parallel side.
     */
    public double getW2() {
      return w2;
    }

    /**
     * Get the difference of the x-coordinate of the left most point
     * of the lower parallel side and the  x-coordinator of the
     * left most point of the upper parallel side.
     *
     * @return the difference
     */
    public double getX() {
      return x;
    }

    /**
     * Get the area of this trapezoid.
     *
     * @return get the area of this trapezoid.
     */
    public double getArea() {
      return area;
    }

    /////////////////////////////////
    // DEBUG
    /////////////////////////////////

    /**
     * Print the specification of this trapezoid.
     */
    public void print() {
      System.err.printf("TrapezoidSpec:\n");
      System.err.printf("  w1 = %.10f\n", w1);
      System.err.printf("  w2 = %.10f\n", w2);
      System.err.printf("  h  = %.10f\n", h);
      System.err.printf("  x  = %.10f\n", x);
      System.err.printf("  area = %.10f\n", area);
      System.err.printf("  refX = %.10f\n", refX);
      System.err.printf("  refY = %.10f\n", refY);
    }
  }


  /**
   * Calculate a simple acceleration schedule if the driver drives.
   *
   * @param time1   the initial time
   * @param v1      the initial velocity
   * @param timeEnd the arrival time
   * @param vEnd    the arrival velocity
   * @param dTotal  the distance between the vehicle and the intersection
   * @param vTop    the speed limit of the road
   * @param accel   the maximum acceleration
   * @param decel   the maximum deceleration
   *
   * @return the acceleration schedule; null if there is no acceleration
   *         schedule that satisfies the constraints.
   * @exception if no solution is found.
   */
  public static AccelSchedule check(double time1, double v1,
                                    double timeEnd, double vEnd,
                                    double dTotal,
                                    double vTop,
                                    double accel, double decel)
                                    throws ReservationCheckException {
    try {
      AccelSchedule as = check0(time1, v1, timeEnd, vEnd,
                                dTotal, vTop, accel, decel);
      assert isAccelScheduleValid(time1, v1, timeEnd, vEnd,
                                  dTotal, vTop, as);
      return as;
    } catch(ReservationCheckException e) {
      // TODO check why there is no accel schedule
      throw e;
    }
  }

  /**
   * Calculate a simple acceleration schedule if the driver drives (Case 0).
   *
   * @param time1   the initial time
   * @param v1      the initial velocity
   * @param timeEnd the arrival time
   * @param vEnd    the arrival velocity
   * @param dTotal  the distance between the vehicle and the intersection
   * @param vTop    the speed limit of the road
   * @param accel   the maximum acceleration
   * @param decel   the maximum deceleration
   *
   * @return the acceleration schedule; null if there is no acceleration
   *         schedule that satisfies the constraints.
   * @exception if no solution is found.
   */
  private static AccelSchedule check0(double time1, double v1,
                                      double timeEnd, double vEnd,
                                      double dTotal,
                                      double vTop,
                                      double accel, double decel)
                                      throws ReservationCheckException {
    assert time1 <= timeEnd;
    assert 0.0 <= v1;     // will not handle situation in which v1 < 0
    // current velocity must within the feasible velocity
    assert Util.isDoubleEqualOrLess(v1, vTop);
    assert 0.0 <= vEnd;   // arrival velocity must be greater than 0
    assert vEnd <= vTop;  // arrival velocity must within the feasible velocity
    assert 0.0 <= dTotal;
    assert 0.0 < accel;
    assert decel < 0.0;

    // Problem definition:
    //
    // Given
    //   1) the current time and velocity of the vehicle  (time1, v1)
    //   2) the arrival time and velocity  (timeEnd, vEnd)
    //   3) the distance to travel  (dTotal)
    //   4) the maximum velocity the vehicle can move  (vTop)
    //   5) the maximum acceleration and deceleration  (accel, decel)
    // The objective is to find an acceleration schedule such that
    // the vehicle can arrive at the intersection at timeEnd at
    // the velocity vEnd, after traveling a distance of dTotal and
    // a time (timeEnd-time1).
    //
    // Objective:
    //
    // Not every configuration is feasible.  We have to identify cases that
    // have no feasible solution.  For cases that have solutions, we need
    // find a feasible acceleration schedule.  There are many possible
    // solutions, but we will find one that is either (1) accelerate first,
    // maintain the speed, and then decelerate, or (2) decelerate first,
    // maintain the speed, and then accelerate.
    //
    // Solution:
    //
    // To help finding the solutions, we utilize the velocity-time graph.
    // we consider the rectangle spanned by 1) the starting point
    // p1, 2) the ending point pEnd, 3) accel and decel.
    // We call the point at the top of the rectangle pUp
    // and the point at the bottom of the rectangle bDown
    //
    // Since tTotal > 0, accel > 0 and decel < 0, the rectangle would
    // not degenerated into a point.  But it may be degenerated into
    // a line into two different ways:
    //   1) p1 = pDown && pUp = pEnd    (v1 < vEnd)
    //   2) p1 = pUp && pDown = pEnd    (vEnd < v1)
    // For non-degenerated cases, we have vDown < (v1, vEnd) < vUp
    // We got three cases
    //   3) vDown < (v1,vEnd) < vUp, v1 = vEnd
    //   4) vDown < (v1,vEnd) < vUp, v1 < vEnd
    //   5) vDown < (v1,vEnd) < vUp, v1 > vEnd
    //
    // All the above 5 cases concern with the shape of the rectangle
    // For each case, we consider how the vTop line and the v==0 line
    // intersect with the rectangle.
    //
    //   1ai) v1 > 0, vTop > vEnd
    //   1aj) v1 > 0, vTop = vEnd
    //   1bi) v1 = 0, vTop > vEnd
    //   1bj) v1 = 0, vTop = vEnd
    //
    //   2ai) vEnd > 0, vTop > v1
    //   2aj) vEnd > 0, vTop = v1
    //   2bi) vEnd = 0, vTop > v1
    //   2bj) vEnd = 0, vTop = v1
    //
    //   3a) vDown >= 0,
    //   3b) vDown < 0, 0 < (v1,vEnd)
    //   3c) vDown < 0, 0 = (v1,vEnd)
    //     and
    //   3x) vUp <= vTop
    //   3y) vUp > vTop, vTop > (v1,vEnd)
    //   3z) vUp > vTop, vTop = (v1,vEnd)
    //
    //   4a) vDown >= 0,
    //   4b) vDown < 0, 0 < v1
    //   4c) vDown < 0, 0 = v1
    //     and
    //   4x) vUp <= vTop
    //   4y) vUp > vTop, vTop > vEnd
    //   4z) vUp > vTop, vTop = vEnd
    //
    //   5a) vDown >= 0,
    //   5b) vDown < 0, 0 < vEnd
    //   5c) vDown < 0, 0 = vEnd
    //     and
    //   5x) vUp <= vTop
    //   5y) vUp > vTop, vTop > v1
    //   5z) vUp > vTop, vTop = v1
    //
    // We then consider how dTotal "fills" up the shape.


    if (time1 < timeEnd) {
      double tTotal = timeEnd - time1;

      // Compute pDown:
      //   vDown = v1 + decel * t14
      //   vEnd = vDown + accel * t15
      //   tTotal = t14 + t15
      // Solution:
      //   v1 + decel * t14 = vEnd - accel * (tTotal - t14)
      //   v1 + decel * t14 = vEnd - accel * tTotal + accel * t14
      //   decel * t14 - accel * t14 = vEnd - accel * tTotal - v1
      //   t14 = (vEnd - accel * tTotal - v1) / (decel - accel)
      // Then pDown = (t14, vDown)

      double t14 = (vEnd - accel * tTotal - v1) / (decel - accel);
      double t15 = tTotal - t14;
      double vDown = v1 + decel * t14;

      // Compute pUp:
      //   vUp = v1 + accel * t24
      //   vEnd = vUp + decel * t25
      //   tTotal = t24 + t25
      // Solution:
      //   v1 + accel * t24 = vUp
      //   v1 + accel * t24 = vEnd - decel * t25
      //   v1 + accel * t24 = vEnd - decel * (tTotal - t24)
      //   v1 + accel * t24 = vEnd - decel * tTotal + decel * t24
      //   v1 + accel * t24 = vEnd - decel * tTotal + decel * t24
      //   v1 + accel * t24 - decel * t24 = vEnd - decel * tTotal
      //   accel * t24 - decel * t24 = vEnd - decel * tTotal - v1
      //   (accel - decel)* t24 = vEnd - decel * tTotal - v1
      //   t24 = (vEnd - decel * tTotal - v1) / (accel - decel)
      // Then pUp = (t24, vUp)
      double t24 = (vEnd - decel * tTotal - v1) / (accel - decel);
      double t25 = tTotal - t24;
      double vUp = v1 + accel * t24;

      // Identify the basic seven cases:
      //   1) p1 = pDown && pUp = pEnd    (v1 < vEnd)
      //   2) p1 = pUp && pDown = pEnd    (vEnd < v1)
      //   3) vDown < (v1,vEnd) < vUp, v1 = vEnd
      //   4) vDown < (v1,vEnd) < vUp, v1 < vEnd
      //   5) vDown < (v1,vEnd) < vUp, v1 > vEnd
      //   6) vDown > v1   (infeasible)
      //   7) vUp < v1     (infeasible)

      if (Util.isDoubleZero(t14)) {
        // Case 1: p1 = pDown && pUp = pEnd    (v1 < vEnd)
        assert Util.isDoubleEqual(v1, vDown);
        assert Util.isDoubleEqual(vEnd, vUp);
        assert Util.isDoubleZero(t14) && Util.isDoubleZero(t25);

        double areaR = tTotal * (v1 + vEnd) / 2.0;
        if (Util.isDoubleEqual(dTotal, areaR,
                               Constants.DOUBLE_EQUAL_WEAK_PRECISION)) {
          AccelSchedule as = new AccelSchedule();
          as.add(time1, accel);
          as.add(time1 + tTotal, 0.0);  // end
          return as;
        } else {
          throw new ReservationCheckException(
            "Reservation check failed: can't accelerate linearly to meet " +
            "the arrival time and the arrival velocity (Case 1)");
        }
      } else if (t14 > 0.0) {
        if (Util.isDoubleZero(t24)) {
          // Case 2: p1 = pUp && pDown = pEnd    (vEnd < v1)
          double areaL = tTotal * (v1 + vEnd) / 2.0;
          if (Util.isDoubleEqual(dTotal, areaL)) {
            AccelSchedule as = new AccelSchedule();
            as.add(time1, decel);
            as.add(time1 + tTotal, 0.0);  // end
            return as;
          } else {
            throw new ReservationCheckException(
              "Reservation check failed: can't decelerate linearly to meet " +
              "the arrival time and the arrival velocity (Case 2)");
          }
        } else if (t24 > 0.0) {
          //  Case 3, 4, 5
          List<TrapezoidSpec> tzs = new ArrayList<TrapezoidSpec>(3);
          double area0 = dTotal;

          // 0 = v1 + decel * t11
          // accel * t13 = vEnd
          double t11 = - v1 / decel;
          double t13 = vEnd / accel;
          double t12 = tTotal - t11 - t13;  // maybe less than 0

          // vTop = v1 + accel * t21
          // vTop + decel * t23 = vEnd
          double t21 = (vTop - v1) / accel;
          double t23 = (vEnd - vTop) / decel;
          double t22 = tTotal - t21 - t23;  // maybe less than 0

          if (v1 > vEnd) {
            // Case 5: vDown < (v1,vEnd) < vUp, v1 > vEnd

            //   vEnd = v1 + t3x * decel
            // tTotal = t3 + t3x

            double t3x = (vEnd - v1) / decel;
            double t3 = tTotal - t3x;

            // Lower trapezoid
            if (0.0 <= vDown) {
              // Case 5a
              double areaL = t14 * (v1 + vDown) / 2.0;
              double areaR = t15 * (vDown + vEnd) / 2.0;
              area0 -= areaL + areaR;
              if (Util.isDoubleZero(area0)) { area0 = 0.0; }
              if (area0 < 0.0) {
                throw new ReservationCheckException(
                  "Reservation check failed: distance too small (Case 5a)");
              } // infeasible dTotal too small
              tzs.add(new TrapezoidSpec(time1+t14, vDown,  // refX, refY
                                        vEnd - vDown,      // h
                                        0.0, t3,           // w1, w2
                                        t14 - t3x));       // x
            } else if (0.0 < v1) {
              // Case 5b
              double areaL = t11 * v1 / 2.0;
              double areaR = t13 * vEnd / 2.0;
              area0 -= areaL + areaR;
              if (Util.isDoubleZero(area0)) { area0 = 0.0; }
              if (area0 < 0.0) {
                throw new ReservationCheckException(
                  "Reservation check failed: distance too small (Case 5b)");
              } // infeasible dTotal too small
              assert t12 > 0.0;
              tzs.add(new TrapezoidSpec(time1+t11, 0.0,  // refX, refY
                                        vEnd,            // h
                                        t12, t3,         // w1, w2
                                        t11 - t3x));     // x
            } else { // 0 == v1
              // Case 5c
              double areaL = t11 * v1 / 2.0;
              area0 -= areaL;
              if (Util.isDoubleZero(area0)) { area0 = 0.0; }
              if (area0 < 0.0) {
                throw new ReservationCheckException(
                  "Reservation check failed: distance too small (Case 5c)");
              } // infeasible dTotal too small
            }

            // Middle trapezoid
            tzs.add(new TrapezoidSpec(time1+t3x, vEnd,  // refX, refY
                                      v1 - vEnd,        // h
                                      t3, t3,           // w1, w2
                                      t3x));            // x
            // Upper trapezoid
            if (vUp <= vTop) {
              // Case 5x
              tzs.add(new TrapezoidSpec(time1, v1,   // refX, refY
                                        vUp - v1,    // h
                                        t3, 0.0,     // w1, w2
                                        -t24));      // x
            } else if (vEnd < vTop) {
              // Case 5y
              assert t22 > 0.0;
              tzs.add(new TrapezoidSpec(time1, v1,   // refX, refY
                                        vTop - v1,   // h
                                        t3, t22,     // w1, w2
                                        -t21));      // x
            }  // vTop == vEnd; Case 5z, do nothing


          } else if (v1 < vEnd) {
            // Case 4: vDown < (v1,vEnd) < vUp, v1 < vEnd

            //   vEnd = v1 + t3x * accel
            // tTotal = t3 + t3x
            double t3x = (vEnd - v1) / accel;
            double t3 = tTotal - t3x;

            // Lower trapezoid
            if (0.0 <= vDown) {
              // Case 4a
              double areaL = t14 * (v1 + vDown) / 2.0;
              double areaR = t15 * (vDown + vEnd) / 2.0;
              area0 -= areaL + areaR;
              if (Util.isDoubleZero(area0)) { area0 = 0.0; }
              if (area0 < 0.0) {
                throw new ReservationCheckException(
                  "Reservation check failed: distance too small (Case 4a)");
              } // infeasible dTotal too small
              tzs.add(new TrapezoidSpec(time1+t14, vDown,  // refX, refY
                                        v1 - vDown,        // h
                                        0.0, t3,           // w1, w2
                                        t14));             // x
            } else if (0.0 < v1) {
              // Case 4b
              double areaL = t11 * v1 / 2.0;
              double areaR = t13 * vEnd / 2.0;
              area0 -= areaL + areaR;
              if (Util.isDoubleZero(area0)) { area0 = 0.0; }
              if (area0 < 0.0) {
                throw new ReservationCheckException(
                  "Reservation check failed: distance too small (Case 4b)");
              } // infeasible dTotal too small
              assert t12 > 0.0;
              tzs.add(new TrapezoidSpec(time1+t11, 0.0,  // refX, refY
                                        v1,              // h
                                        t12, t3,         // w1, w2
                                        t11));           // x
            } else { // 0 == v1;
              // Case 4c
              double areaR = t13 * vEnd / 2.0;
              area0 -= areaR;
              if (Util.isDoubleZero(area0)) { area0 = 0.0; }
              if (area0 < 0.0) {
                throw new ReservationCheckException(
                  "Reservation check failed: distance too small (Case 4c)");
              } // infeasible dTotal too small
            }

            // Middle trapezoid
            tzs.add(new TrapezoidSpec(time1, v1,  // refX, refY
                                      vEnd - v1,  // h
                                      t3, t3,     // w1, w2
                                      -t3x));     // x
            // Upper trapezoid
            if (vUp <= vTop) {
              // Case 4x
              tzs.add(new TrapezoidSpec(time1+t3x, vEnd,  // refX, refY
                                        vUp - vEnd,       // h
                                        t3, 0.0,          // w1, w2
                                        t3x-t24));        // x
            } else if (vEnd < vTop) {
              // Case 4y
              assert t22 > 0.0;
              tzs.add(new TrapezoidSpec(time1+t3x, vEnd,  // refX, refY
                                        vTop - vEnd,      // h
                                        t3, t22,          // w1, w2
                                        t3x-t21));        // x
            }  // vTop == vEnd; Case 4z, do nothing


          } else {  // v1 == vEnd
            // Case 3: vDown < (v1,vEnd) < vUp; v1 = vEnd

            // Lower trapezoid
            if (0.0 <= vDown) {
              // Case 3a
              double areaL = t14 * (v1 + vDown) / 2.0;
              double areaR = t15 * (vDown + vEnd) / 2.0;
              area0 -= areaL + areaR;
              if (Util.isDoubleZero(area0)) { area0 = 0.0; }
              if (area0 < 0.0) {
                throw new ReservationCheckException(
                  "Reservation check failed: distance too small (Case 3a)");
              } // infeasible dTotal too small
              tzs.add(new TrapezoidSpec(time1+t14, vDown,  // refX, refY
                                        v1 - vDown,        // h
                                        0.0, tTotal,       // w1, w2
                                        t14));             // x
            } else if (0.0 < v1) {
              // Case 3b
              double areaL = t11 * v1 / 2.0;
              double areaR = t13 * vEnd / 2.0;
              area0 -= areaL + areaR;
              if (Util.isDoubleZero(area0)) { area0 = 0.0; }
              if (area0 < 0.0) {
                throw new ReservationCheckException(
                  "Reservation check failed: distance too small (Case 3b)");
              } // infeasible dTotal too small
              assert t12 > 0.0;
              tzs.add(new TrapezoidSpec(time1+t11, 0.0,  // refX, refY
                                        v1,              // h
                                        t12, tTotal,     // w1, w2
                                        t11));           // x
            }  // 0 == v1; Case 3c, do nothing

            // Upper trapezoid
            if (vUp <= vTop) {
              // Case 3x
              tzs.add(new TrapezoidSpec(time1, v1,     // refX, refY
                                        vUp - v1,      // h
                                        tTotal, 0.0,   // w1, w2
                                        -t24));        // x
            } else if (vEnd < vTop) {
              // Case 3y
              assert t22 > 0.0;
              tzs.add(new TrapezoidSpec(time1, v1,     // refX, refY
                                        vTop - v1,      // h
                                        tTotal, t22,   // w1, w2
                                        -t21));        // x
            }  // vTop == vEnd; Case 3z, do nothing
          }


          Line2D.Double line = findPartialTrapezoid(tzs, area0);
          if (line != null) {
//            assert isPartialTrapezoidValid(time1, v1,
//                                           timeEnd, vEnd,
//                                           dTotal, vTop,
//                                           accel, decel,
//                                           line);
            AccelSchedule as = makeAccelSchedule(time1, v1,
                                                 timeEnd, vEnd,
                                                 accel, decel,
                                                 line);
            return as;
          } else {
            // infeasible due to dTotal too large
            throw new ReservationCheckException(
              "Reservation check failed: distance too large (Case 3,4,5)");
          }
        } else { // t24 < 0.0
          // Case 7
          throw new ReservationCheckException(
            "Reservation check failed: can't decelerate to final velocity " +
            "Case 7)");
        }
      } else { // t14 < 0.0
        // Case 6
        throw new ReservationCheckException(
          "Reservation check failed: can't accelerate to final velocity " +
          "Case 6)");
      }
    } else {
      assert Util.isDoubleEqual(time1, timeEnd);
      if (dTotal > 0.0) {
        throw new ReservationCheckException(
          "Reservation check failed: distance is not zero when there is " +
          "no time to move"); // no solution since there is no time to move
      } else {  // dTotal == 0.0;
        assert Util.isDoubleZero(dTotal);
        if (Util.isDoubleEqual(v1, vEnd)) {
          // already arrive at right time and at right velocity
          AccelSchedule as = new AccelSchedule();
          as.add(time1, 0.0);
          return as;
        } else {
          throw new ReservationCheckException(
            "Reservation check failed: distance is not zero when there is " +
            "no time to change velocity");
        }
      }
    }
  }


  /**
   * Calculate a partial trapezoid given the area.
   *
   * @param spec   The specification of the trapezoid
   * @param area0  The area of the partial trapezoid filling the upper part
   *               of the trapezoid
   * @return the line separating the top and the bottom of the trapezoid; null
   *         if no solution.
   */
  private static Line2D.Double calcPartialTrapezoid(TrapezoidSpec spec,
                                                    double area0) {
    double refX = spec.getRefX();
    double refY = spec.getRefY();
    double h = spec.getH();
    double w1 = spec.getW1();
    double w2 = spec.getW2();
    double x = spec.getX();
    double area = spec.getArea();

    // The trapezoid can be a triangle but not a line or a point.
    assert 0.0 < h;
    assert (0.0 <= w1 && 0.0 < w2) || (0.0 < w1 && 0.0 <= w2);
    assert Util.isDoubleEqual(area, h*(w1+w2)/2.0);
    // The area of the partial trapezoid must be smaller
//    assert Util.isDoubleZero(area0) || Util.isDoubleEqual(area0, area)
//           || (0.0 <= area0 && area0 <= area);

    // Let w0 be the length of the upper parallel side of the partial trapezoid
    // Let h0 be the height of the partial trapezoid
    // Let d0 be the difference of the x-coordinate of the left most point
    //   of the lower parallel side and the x-coordinator of the left most
    //   point of the upper parallel side of the partial trapezoid

    // If area0 is zero or area, the solution is trivial
    // If w1 == w2, the solution is also trivial
    // Otherwise, we have the following equations:
    //   1) area0 = h0 * (w0 + w1) / 2
    //   2a) (w0-w1) / h0 = (w2-w0) / (h-h0)  (requires 0 < h0, w1 < w2)
    //   2b) (w1-w0) / h0 = (w0-w2) / (h-h0)  (requires 0 < h0, w2 < w1)
    //   3) x0 / h0 = (x-x0) / (h-h0)         (requires 0 < h0)
    // Combine 2a and 2b:
    //   2)  (w0-w1) / h0 = (w2-w0) / (h-h0)
    // Simply 3):
    //       x0 / h0 = (x-x0) / (h-h0)
    //   x0 * (h-h0) = (x-x0) * h0
    //   (h-h0) * x0 = h0 * x - h0 * x0
    //        h0 * x = h * x0
    //            x0 = h0 * x / h
    // Then
    //       (h-h0) * (w0-w1) = (w2-w0) * h0
    //       (h-h0)*w0 - (h-h0)*w1 = w2 * h0 - h0 * w0
    //       h*w0 - (h-h0)*w1 = w2 * h0
    //       h*w0 = w2 * h0 + (h-h0)*w1
    //       h*w0 = w2 * h0 + (h-h0)*w1
    //       h*w0 = (w2-w1) * h0 + h*w1
    //         w0 = (w2-w1) * h0 / h + w1
    // Substitute w0 in equation 1:
    //      area0 = h0 * ((w2 - w1) * h0 / h + 2*w1) / 2
    //  2 * area0 = (w2 - w1) * h0^2 / h + 2*w1 * h0
    //          0 = ((w2 - w1)/h) * h0^2 + 2*w1 * h0 - 2 * area0
    //          0 = (w2 - w1) * h0^2 + (2*w1*h) * h0 - (2*area0*h)
    //
    // If w1 == w1,
    //   h0 = 2*area0*h / (2*w1*h) = area0 / w1
    //   w0 = w1
    //   x0 / h0 = (x-x0) / (h-h0)
    // If w1 != w1,
    //      delta = (2*w1*h)^2 + 4 * (w2 - w1) * (2*area0*h)
    //      delta = (2*w1*h)^2 + 4 * (w2 - w1) * (2*area0*h)
    //      delta = 4 * ((w1*h)^2 + (w2 - w1) * (2*area0*h))
    // If w2 >= w1, all terms are positive, delta >= 0
    // If w2 < w1,
    //   0 <= (w1*h)^2 + (w2 - w1) * (2*area0*h)
    //   (w1 - w2) * (2*area0*h) <= (w1*h)^2
    //                     area0 <= (w1*h)^2 / ((w1 - w2) * (2*h))
    //                     area0 <=  h * w1^2 / (2 * (w1 - w2))
    //                     area0 <=  (h * w1 / 2) * (w1 / (w1 - w2))
    // Thus, when w2 < w1, solution exists only when area0 is small enough.
    //
    // If w2 >= w1,
    //   h0 = (- (2*w1*h) + sqrt(delta)) / (2 * (w2 - w1))
    //      = (- (w1*h) + sqrt((w1*h)^2 + (w2 - w1) * (2*area0*h))) / (w2 - w1)
    //        (ignore the negative solution of h0)
    //   w0 = (w2-w1) * h0 / h + w1
    //   x0 = h0 * x / h
    // If w2 < w1,
    //   h0 = (- (2*w1*h) + sqrt(delta)) / (2 * (w2 - w1))
    //      = (- (w1*h) + sqrt((w1*h)^2 + (w2 - w1) * (2*area0*h))) / (w2 - w1)
    //        (ignore the larger solution of h0)
    //   w0 = (w2-w1) * h0 / h + w1
    //   x0 = h0 * x / h
    // Thus, in both case, the solution are the same.

    if (Util.isDoubleZero(area0)) {
      double w0 = w1;
      double h0 = 0.0;
      double x0 = 0.0;
      double p1x = refX - x0;
      double p1y = refY + h0;
      return new Line2D.Double(p1x, p1y, p1x+w0, p1y);
    } else if (Util.isDoubleEqual(area0, area)) {
      double w0 = w2;
      double h0 = h;
      double x0 = x;
      double p1x = refX - x0;
      double p1y = refY + h0;
      return new Line2D.Double(p1x, p1y, p1x+w0, p1y);
    } else if (0.0 <= area0 && area0 <= area) {
      if (Util.isDoubleEqual(w1, w2)) {
        double h0 = area0 / w1;
        double w0 = w1;
        double x0 = h0 * x / h;
        double p1x = refX - x0;
        double p1y = refY + h0;
        return new Line2D.Double(p1x, p1y, p1x+w0, p1y);
      } else {
        double h0 = (Math.sqrt((w1*w1*h*h) + (w2-w1)*(2*area0*h)) - (w1*h))
                    / (w2 - w1);
        double w0 = (w2-w1) * h0 / h + w1;
        double x0 = h0 * x / h;
        double p1x = refX - x0;
        double p1y = refY + h0;
        return new Line2D.Double(p1x, p1y, p1x+w0, p1y);
      }
    } else {
      throw new RuntimeException("Error in LevelOffReservationCheck::" +
                                 "calcPartialTrapezoid");
    }
  }


  /////////////////////////////////
  // PRIVATE METHODS
  /////////////////////////////////

  /**
   * Calculate a partial trapezoid given the area in a sequence of trapezoids.
   *
   * @param trapezoids   The list of the specification of the trapezoid
   * @param area0  The area of the partial trapezoid filling the upper part
   *               of the trapezoid
   * @return the line separating the top and the bottom of the trapezoid; null
   *         if no solution.
   */
  private static Line2D.Double findPartialTrapezoid(
                                               List<TrapezoidSpec> trapezoids,
                                               double area0) {
//    System.err.printf("area0 = %.10f\n", area0);
//    for(TrapezoidSpec spec : trapezoids) {
//      spec.print();
//    }
    for(TrapezoidSpec spec : trapezoids) {
      if (Util.isDoubleEqual(area0, spec.getArea()) || area0 < spec.getArea()) {
         return calcPartialTrapezoid(spec, area0);
      } else {
        area0 -= spec.getArea();
      }
    }
    return null; // no solution
  }


  /**
   * Calculate a acceleration schedule given the line separating the trapezoid.
   *
   * @param time1   the initial time
   * @param v1      the initial velocity
   * @param timeEnd the arrival time
   * @param vEnd    the arrival velocity
   * @param accel   the maximum acceleration
   * @param decel   the maximum deceleration
   * @param line    the line separating the trapezoid
   *
   * @return the acceleration schedule; null if there is no acceleration
   *         schedule that satisfies the constraints.
   */
  private static AccelSchedule makeAccelSchedule(double time1, double v1,
                                                 double timeEnd, double vEnd,
                                                 double accel, double decel,
                                                 Line2D.Double line) {
    AccelSchedule as = new AccelSchedule();

    if (Util.isDoubleEqual(line.getX1(), time1)) {
      assert Util.isDoubleEqual(line.getY1(), v1,
                                Constants.DOUBLE_EQUAL_WEAK_PRECISION);
      // The starting point is equal to point 1
    } else {
      if (line.getY1() < v1) {
        as.add(time1, decel);
      } else {
        assert v1 < line.getY1();
        as.add(time1, accel);
      }
    }

    if (Util.isDoubleNotEqual(line.getX1(), line.getX2())) {
      as.add(line.getX1(), 0.0);
    }  // else the line is a point

    if (Util.isDoubleEqual(line.getX2(), timeEnd)) {
      assert Util.isDoubleEqual(line.getY2(), vEnd,
                                Constants.DOUBLE_EQUAL_WEAK_PRECISION);
      // The ending point is equal to point 2
    } else {
      if (line.getY2() < vEnd) {
        as.add(line.getX2(), accel);
      } else {
        assert vEnd < line.getY2();
        as.add(line.getX2(), decel);
      }
    }

    as.add(timeEnd, 0.0);
    return as;
  }


  /////////////////////////////////
  // DEBUG
  /////////////////////////////////


  /**
   * Check to see if the trapezoid formed by a given line separating the
   * trapezoid is valid.
   *
   * @param time1   the initial time
   * @param v1      the initial velocity
   * @param timeEnd the arrival time
   * @param vEnd    the arrival velocity
   * @param dTotal  the distance from the vehicle to the intersection
   * @param vTop    the speed limit of the road
   * @param accel   the maximum acceleration
   * @param decel   the maximum deceleration
   * @param line    the line separating the trapezoid
   *
   * @return true if the trapezoid is valid.
   */
  private static boolean isPartialTrapezoidValid(double time1, double v1,
                                                 double timeEnd, double vEnd,
                                                 double dTotal,
                                                 double vTop,
                                                 double accel, double decel,
                                                 Line2D.Double line) {
    double v2 = v1 + ((v1 <= line.getY1())?accel:decel) * (line.getX1()-time1);
    if (Util.isDoubleNotEqual(v2, line.getY1())) {
      if (isDebugging) {
        System.err.printf("Error in isPartialTrapezoidValid(): " +
                          "line.getY1() is incorrect.\n");
      }
      return false;
    }
    // vEnd = v3 + ((line.getY2()<=vEnd)?accel:decel) * (timeEnd-line.getX2());
    double v3 = vEnd - ((line.getY2()<=vEnd)?accel:decel)
                       * (timeEnd-line.getX2());

    if (Util.isDoubleNotEqual(v3, line.getY2())) {
      if (isDebugging) {
        System.err.printf("Error in isPartialTrapezoidValid(): " +
                          "line.getY2() is incorrect.\n");
      }
      return false;
    }

//    System.err.printf("time1 = %.5f\n", time1);
//    System.err.printf("v1 = %.5f\n", v1);
//    System.err.printf("timeEnd = %.5f\n", timeEnd);
//    System.err.printf("vEnd = %.5f\n", vEnd);
//    System.err.printf("accel = %.2f\n", accel);
//    System.err.printf("decel = %.2f\n", decel);
//    printLine(line);

    return true;
  }


  /**
   * Check to see if an acceleration schedule is valid.
   *
   * @param time1   the initial time
   * @param v1      the initial velocity
   * @param timeEnd the arrival time
   * @param vEnd    the arrival velocity
   * @param dTotal  the distance from the vehicle to the intersection
   * @param vTop    the speed limit of the road
   * @param as      the acceleration schedule.
   *
   * @return true if the acceleration schedule is valid.
   */
  private static boolean isAccelScheduleValid(double time1, double v1,
                                              double timeEnd, double vEnd,
                                              double dTotal,
                                              double vTop,
                                              AccelSchedule as) {
    assert as != null;

    List<AccelSchedule.TimeAccel> list = as.getList();
    if (as.size() == 0 || as.size() > 4) {
      if (isDebugging) {
        System.err.printf("Error in isAccelScheduleValid(): " +
                          "Invalid accel schedule size.\n");
      }
      return false;
    }
    double v = v1;
    double d = 0.0;
    TimeAccel ta1 = list.get(0);
    if (!Util.isDoubleEqual(ta1.getTime(), time1)) {
      if (isDebugging) {
        System.err.printf("Error in isAccelScheduleValid(): " +
                          "Initial time is incorrect.\n");
      }
      return false;
    }

    for(int i=1; i<as.size(); i++) {
      TimeAccel ta2 = list.get(i);
      double t = ta2.getTime() - ta1.getTime();
      if (t <= 0.0) {
        if (isDebugging) {
          System.err.printf("Error in isAccelScheduleValid(): " +
                            "Duration cannot be negative.\n");
        }
        return false;
      }
      double v2 = v + ta1.getAcceleration() * t;
      if (Util
        .isDoubleNotEqual(v2, vTop, Constants.DOUBLE_EQUAL_WEAK_PRECISION)
        && v2 > vTop) {
        if (isDebugging) {
          System.err.printf("Error in isAccelScheduleValid(): " +
                            "Velocity is greater than vTop " +
                            "v2 = %.5f > %.5f\n", v2, vTop);
        }
        return false;
      }
      d += t * (v + v2) / 2.0 ;
      v = v2;
      ta1 = ta2;
    }

    if (Util.isDoubleNotEqual(ta1.getTime(), timeEnd)) {
      if (isDebugging) {
        System.err.printf("Error in isAccelScheduleValid(): " +
                          "The ending time is incorrect.\n");
      }
      return false;
    }
    if (Util.isDoubleNotEqual(v, vEnd, Constants.DOUBLE_EQUAL_WEAK_PRECISION)) {
      if (isDebugging) {
        System.err.printf("v    = %.10f\n", v);
        System.err.printf("vEnd = %.10f\n", vEnd);
        System.err.printf("Error in isAccelScheduleValid(): " +
                          "The ending velocity is incorrect.\n");
      }
      return false;
    }
    if (Util.isDoubleNotEqual(d, dTotal,
                              Constants.DOUBLE_EQUAL_WEAK_PRECISION)) {
      if (isDebugging) {
        System.err.printf("Error in isAccelScheduleValid(): " +
                          "The total distance is incorrect " +
                          "(actual = %.15f, expected = %.15f)\n",
                          d, dTotal);
        System.err.printf("as = %s\n", as);
      }
      return false;
    }
    return true;
  }

}

