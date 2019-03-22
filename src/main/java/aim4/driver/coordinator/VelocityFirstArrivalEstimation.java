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

import aim4.config.Constants;
import aim4.util.Util;
import aim4.vehicle.AccelSchedule;

/**
 * This class provides functions to solve the optimization problem.
 */
public class VelocityFirstArrivalEstimation {

  /////////////////////////////////
  // CONSTANTS
  /////////////////////////////////

  /**
   * Whether the debug mode is on.
   */
  private static final boolean isDebugging = true;


  /////////////////////////////////
  // PUBLIC METHODS
  /////////////////////////////////

  /**
   * Compute the acceleration schedule and the arrival parameters
   * at the intersection.
   *
   * @param time1    the initial time
   * @param v1       the initial velocity
   * @param dTotal   the distance between the vehicle and the intersection
   * @param vTop     the speed limit of the road
   * @param vEndMax  the speed limit at the intersection
   * @param accel    the maximum acceleration
   * @param decel    the maximum deceleration
   *
   * @return the acceleration schedule and estimated arrival parameters
   *         at the intersection; null if there is no solution
   * @throws if the algorithm returns no solution.
   */
  public static ArrivalEstimationResult estimate(double time1,
                                                 double v1,
                                                 double dTotal,
                                                 double vTop,
                                                 double vEndMax,
                                                 double accel,
                                                 double decel)
                                           throws ArrivalEstimationException {
    assert 0.0 <= v1;  // this procedure will not handle v1 < 0
    assert 0.0 < accel && decel < 0.0;
    assert Util.isDoubleEqualOrLess(v1, vTop);  // current velocity must within
                                                // the feasible velocity
    assert vEndMax <= vTop;  // arrival velocity cannot be larger than vTop

    // Problem defintion:
    // Given:
    //   1) the distance to the intersection  (dTotal)
    //   2) the maximum road and vehicle speed limit  (vTop)
    //   3) the maximum arrival velocity (vEndMax)
    //   4) the initial (current) velocity of the vehicle (v1)
    //   5) the max acceleration (accel) and deceleration (decel)
    //      the vehicle can achieve
    // the objective is to find an acceleration profile for reaching the
    // the intersection such that
    //   1) the arrival velocity (vEnd) is as large as possible
    //   2) the total time (tTotal) is as small as possible,
    // This is an multi-objective optimization problem.
    // Thus, there can be multiple solutions.
    // But here we shall put a priority to the maximization of vEnd.
    // That is, we optimize vEnd first, and then minimize tTotal.
    // Thus, we call this estimation "velocity-first".
    //
    // Solution:
    // First, assume vEnd = vEndMax,
    //   We consider six cases:
    //     Case 1: v1 == vTop == vEndMax
    //     Case 2: v1 <  vTop == vEndMax
    //     Case 3: v1 == vTop > vEndMax
    //     Case 4: v1,vEndMax < vTop  &&  v1 == vEndMax
    //     Case 5: v1,vEndMax < vTop  &&  v1 < vEndMax
    //     Case 6: v1,vEndMax < vTop  &&  v1 > vEndMax
    // Second, if none of the above cases is feasible
    //   consider vEnd < vEndMax, and then maximize vEnd, regardless tTotal
    //

    ArrivalEstimationResult result = null;

    if (dTotal > 0.0) {
      if (vEndMax < vTop) {
        if (v1 < vTop) {
          if (v1 > vEndMax) {
            result = estimateForCase6(time1, v1, dTotal,
                                      vTop, vEndMax, accel, decel);
          } else if (v1 < vEndMax) {
            result = estimateForCase5(time1, v1, dTotal,
                                      vTop, vEndMax, accel, decel);
          } else {  // v1 == vEndMax
            result = estimateForCase4(time1, v1, dTotal,
                                      vTop, vEndMax, accel, decel);
          }
        } else {  // v1 == vTop
          result = estimateForCase3(time1, v1, dTotal,
                                    vTop, vEndMax, accel, decel);
        }
      } else {  // vEndMax == vTop
        if (v1 < vTop) {
          result = estimateForCase2(time1, v1, dTotal,
                                    vTop, vEndMax, accel, decel);
        } else {  // v1 == vTop
          result = estimateForCase1(time1, v1, dTotal,
                                    vTop, vEndMax, accel, decel);
        }
      }
    } else {  // dTotal == 0.0  since dTotal >= 0.0
      // Already arrive at the intersection
      // No time and space to change the velocity
      if (v1 <= vEndMax) {
        AccelSchedule ap = new AccelSchedule();
        ap.add(time1, 0.0);
        return new ArrivalEstimationResult(time1, v1, ap);
      } else {
        // impossible to decelerate instantly
        throw new ArrivalEstimationException(
          "Arrival estimation failed: distance is zero and the current " +
          "velocity is larger than the maximum final velocity") ;
      }
    }

    assert isResultValid(time1, v1,
                         dTotal,
                         vTop,
                         vEndMax,
                         accel, decel,
                         result);

    return result;
  }


  /////////////////////////////////
  // PRIVATE METHODS
  /////////////////////////////////

  /**
   * Compute the acceleration schedule and the arrival parameters
   * at the intersection (Case 1).
   *
   * @param time1    the initial time
   * @param v1       the initial velocity
   * @param dTotal   the distance between the vehicle and the intersection
   * @param vTop     the speed limit of the road
   * @param vEndMax  the speed limit at the intersection
   * @param accel    the maximum acceleration
   * @param decel    the maximum deceleration
   *
   * @return the acceleration schedule and estimated arrival parameters
   *         at the intersection; null if there is no solution
   * @throws if the algorithm returns no solution.
   */
  private static ArrivalEstimationResult estimateForCase1(double time1,
                                                          double v1,
                                                          double dTotal,
                                                          double vTop,
                                                          double vEndMax,
                                                          double accel,
                                                          double decel)
                                          throws ArrivalEstimationException {
    // Case 1: v1 == vTop == vEndMax
    //   Always feasible
    double tTotal = dTotal / vEndMax;
    AccelSchedule ap = new AccelSchedule();
    ap.add(time1, 0.0);
    ap.add(time1 + tTotal, 0.0);
    return new ArrivalEstimationResult(time1 + tTotal, vEndMax, ap);
  }

  /**
   * Compute the acceleration schedule and the arrival parameters
   * at the intersection (Case 2).
   *
   * @param time1    the initial time
   * @param v1       the initial velocity
   * @param dTotal   the distance between the vehicle and the intersection
   * @param vTop     the speed limit of the road
   * @param vEndMax  the speed limit at the intersection
   * @param accel    the maximum acceleration
   * @param decel    the maximum deceleration
   *
   * @return the acceleration schedule and estimated arrival parameters
   *         at the intersection; null if there is no solution
   * @throws if the algorithm returns no solution.
   */
  private static ArrivalEstimationResult estimateForCase2(double time1,
                                                          double v1,
                                                          double dTotal,
                                                          double vTop,
                                                          double vEndMax,
                                                          double accel,
                                                          double decel)
                                           throws ArrivalEstimationException {
    // Case 2: v1 <  vTop == vEndMax
    //   Case 2a: accelerate to vTop and then maintain the speed
    //   Case 2b: accelerate to vEndMax directly
    //   Case 2c: infeasible case; can't accelerate to vEndMax
    //            due to distance constraint
    double t1 = (vTop - v1) / accel;
    double d1 = t1 * (vTop + v1) / 2.0;

    if (d1 < dTotal) {  // Case 2a
      double d2 = dTotal - d1;
      double t2 = d2 / vTop;
      AccelSchedule ap = new AccelSchedule();
      ap.add(time1, accel);
      ap.add(time1 + t1, 0.0);
      ap.add(time1 + t1 + t2, 0.0);  // end
      return new ArrivalEstimationResult(time1 + t1 + t2, vEndMax, ap);
    } else if (Util.isDoubleEqual(d1, dTotal)) {  // Case 2b
      AccelSchedule ap = new AccelSchedule();
      ap.add(time1, accel);
      ap.add(time1 + t1, 0.0); // end
      return new ArrivalEstimationResult(time1 + t1, vEndMax, ap);
    } else {  // d1 > dTotal  // Case 2c
      return estimateMaxVEndForCase2AndCase5(time1, v1, dTotal, vTop,
                                             vEndMax, accel);
    }
  }

  /**
   * Compute the acceleration schedule and the arrival parameters
   * at the intersection (Case 3).
   *
   * @param time1    the initial time
   * @param v1       the initial velocity
   * @param dTotal   the distance between the vehicle and the intersection
   * @param vTop     the speed limit of the road
   * @param vEndMax  the speed limit at the intersection
   * @param accel    the maximum acceleration
   * @param decel    the maximum deceleration
   *
   * @return the acceleration schedule and estimated arrival parameters
   *         at the intersection; null if there is no solution
   * @throws if the algorithm returns no solution.
   */
  private static ArrivalEstimationResult estimateForCase3(double time1,
                                                          double v1,
                                                          double dTotal,
                                                          double vTop,
                                                          double vEndMax,
                                                          double accel,
                                                          double decel)
                                           throws ArrivalEstimationException {
    // Case 3: v1 == vTop > vEndMax
    //   Case 3a: maintain the speed and then decelerate to vEndMax
    //   Case 3b: directly decelerate to vEndMax
    //   Case 3c: infeasible case; can't decelerate to vEndMax
    //            due to distance constraint
    double t2 = (vEndMax - vTop) / decel;
    double d2 = t2 * (vEndMax + vTop) / 2.0;

    if (d2 < dTotal) {  // Case 3a
      double d1 = dTotal - d2;
      double t1 = d1 / vTop;
      AccelSchedule ap = new AccelSchedule();
      ap.add(time1, 0.0);
      ap.add(time1 + t1, decel);
      ap.add(time1 + t1 + t2, 0.0);  // end
      return new ArrivalEstimationResult(time1 + t1 + t2, vEndMax, ap);
    } else if (Util.isDoubleEqual(d2, dTotal)) {  // Case 3b
      AccelSchedule ap = new AccelSchedule();
      ap.add(time1, 0.0);
      ap.add(time1 + t2, decel); // end
      return new ArrivalEstimationResult(time1 + t2, vEndMax, ap);
    } else {  // d2 > dTotal  // Case 3c
      // no matter how quick the vehicle decelerates,
      // it can't reduce its velocity below vEndMax
      // since the distance is too short
      throw new ArrivalEstimationException(
        "Arrival estimation failed: distance too small (Case 3)") ;
    }
  }


  /**
   * Compute the acceleration schedule and the arrival parameters
   * at the intersection (Case 4).
   *
   * @param time1    the initial time
   * @param v1       the initial velocity
   * @param dTotal   the distance between the vehicle and the intersection
   * @param vTop     the speed limit of the road
   * @param vEndMax  the speed limit at the intersection
   * @param accel    the maximum acceleration
   * @param decel    the maximum deceleration
   *
   * @return the acceleration schedule and estimated arrival parameters
   *         at the intersection; null if there is no solution
   * @throws if the algorithm returns no solution.
   */
  private static ArrivalEstimationResult estimateForCase4(double time1,
                                                          double v1,
                                                          double dTotal,
                                                          double vTop,
                                                          double vEndMax,
                                                          double accel,
                                                          double decel)
                                            throws ArrivalEstimationException {
    // Case 4: v1,vEndMax < vTop  &&  v1 == vEndMax
    //   Case 4a: accelerate to vTop, then maintain the speed, and then
    //            decelerate to vEndMax
    //   Case 4b: accelerate to v2 (v1 <= v2 < vTop), and then
    //            decelerate to vEndMax immediately
    //   Always feasible; there is no degenerated case such as
    //   since dTotal > 0

    double t1 = (vTop - v1) / accel;
    double d1 = t1 * (vTop + v1) / 2.0;
    double t3 = (vEndMax - vTop) / decel;
    double d3 = t3 * (vEndMax + vTop) / 2.0;

    if (d1 + d3 < dTotal) {  // Case 4a
      double d2 = dTotal - d1 - d3;
      double t2 = d2 / vTop;

      AccelSchedule ap = new AccelSchedule();
      ap.add(time1, accel);
      ap.add(time1 + t1, 0.0);
      ap.add(time1 + t1 + t2, decel);
      ap.add(time1 + t1 + t2 + t3, 0.0);  // end
      return new ArrivalEstimationResult(time1 + t1 + t2 + t3, vEndMax, ap);
    } else {  // d1+d3 >= dTotal  // Case 4b
      // Find v2 such that d1x + d3x = dTotal
      // Solve this system of equations:
      //   t1x = (v2 - v1) / accel;
      //   d1x = t1x * (v2 + v1) / 2.0;
      //   t3x = (vEndMax - v2) / decel;
      //   d3x = t3x * (vEndMax + v2) / 2.0;
      //   d1x + d3x = dTotal
      // Solution:
      //   dTotal = t1x * (v2 + v1) / 2.0 + t3x * (vEndMax + v2) / 2.0
      //   dTotal = ((v2 - v1) / accel) * (v2 + v1) / 2.0
      //            + ((vEndMax - v2) / decel) * (vEndMax + v2) / 2.0
      //   2.0 * dTotal = ((v2 - v1) / accel) * (v2 + v1)
      //                + ((vEndMax - v2) / decel) * (vEndMax + v2)
      //   2.0 * dTotal = ((v2 - v1) * (v2 + v1) / accel)
      //                + ((vEndMax - v2) * (vEndMax + v2) / decel)
      //   2.0 * dTotal = ((v2^2 - v1^2) / accel)
      //                + ((vEndMax^2 - v2^2) / decel)
      //   2.0 * accel * decel * dTotal =
      //     (decel * (v2^2 - v1^2)) + (accel * (vEndMax^2 - v2^2))
      //   2.0 * accel * decel * dTotal =
      //     (decel * v2^2 - decel * v1^2) + (accel * vEndMax^2 - accel *v2^2)
      //   2.0 * accel * decel * dTotal =
      //     decel * v2^2 - accel *v2^2 + accel * vEndMax^2 - decel * v1^2
      //   2.0 * accel * decel * dTotal =
      //     (decel - accel) * v2^2 + accel * vEndMax^2 - decel * v1^2
      //   - (decel - accel) * v2^2 =
      //      accel * vEndMax^2 - decel * v1^2 - 2.0*accel*decel*dTotal
      //   (accel - decel) * v2^2 =
      //      accel * vEndMax^2 - decel * v1^2 - 2.0*accel*decel*dTotal
      //   v2^2 = (accel * vEndMax^2 - decel * v1^2 - 2.0*accel*decel*dTotal)
      //          / (accel - decel)
      //   let delta = (accel * vEndMax^2 - decel * v1^2
      //               - 2.0*accel*decel*dTotal) / (accel - decel)
      //  Then v2 = sqrt(delta)
      //
      //  Error handling:
      //
      //  If delta < 0, v2 does not exist
      //  But if dTotal >= 0, v2 must exist. The reason is as follows:
      //    0 <= delta
      //    0 <= (accel * vEndMax^2 - decel * v1^2 - 2.0*accel*decel*dTotal)
      //         / (accel - decel)
      //    0 <= (accel * vEndMax^2 - decel * v1^2 - 2.0*accel*decel*dTotal)
      //    2.0*accel*decel*dTotal <= accel * vEndMax^2 - decel * v1^2
      //    dTotal >=  (accel * vEndMax^2 - decel * v1^2) / (2.0*accel*decel)
      //      (note that 2.0*accel*decel < 0 since accel > 0 and decel < 0)
      //    dTotal >=  vEndMax^2 / (2.0*decel) - v1^2 / (2.0*accel)
      //  And the right hand side is negative since decel < 0
      //  The the above inequality must be true if dTotal >= 0

      double delta = (accel * vEndMax * vEndMax - decel * v1 * v1
                     - 2.0*accel*decel*dTotal) / (accel - decel);
      assert delta >= 0.0;  // must be true

      double v2 = Math.sqrt(delta);
      assert v2 < vTop;  // It must be true; otherwise, it should have
                         // been handled by case 4a
      assert v1 <= v2;   // this implies vEndMax <= v2 as well
                         // if untrue, t1x < 0 and t3x < 0, which is wrong
      double t1x = (v2 - v1) / accel;
      double d1x = t1x * (v2 + v1) / 2.0;
      double t3x = (vEndMax - v2) / decel;
      double d3x = t3x * (vEndMax + v2) / 2.0;
      assert d1x >= 0.0 && d3x >= 0.0;  // this is important to check this
                                        // since vehicles can't go backward
                                        // But this must be true since
                                        // t1x >= 0 and t2x >=0 and v2 >= 0

      AccelSchedule ap = new AccelSchedule();
      ap.add(time1, accel);
      ap.add(time1 + t1x, decel);
      ap.add(time1 + t1x + t3x, 0.0);  // end
      return new ArrivalEstimationResult(time1 + t1x + t3x, vEndMax, ap);
    }
  }

  /**
   * Compute the acceleration schedule and the arrival parameters
   * at the intersection (Case 5).
   *
   * @param time1    the initial time
   * @param v1       the initial velocity
   * @param dTotal   the distance between the vehicle and the intersection
   * @param vTop     the speed limit of the road
   * @param vEndMax  the speed limit at the intersection
   * @param accel    the maximum acceleration
   * @param decel    the maximum deceleration
   *
   * @return the acceleration schedule and estimated arrival parameters
   *         at the intersection; null if there is no solution
   * @throws if the algorithm returns no solution.
   */
  private static ArrivalEstimationResult estimateForCase5(double time1,
                                                          double v1,
                                                          double dTotal,
                                                          double vTop,
                                                          double vEndMax,
                                                          double accel,
                                                          double decel)
                                            throws ArrivalEstimationException {
    // Case 5: v1,vEndMax < vTop  &&  v1 < vEndMax
    //   Case 5a: accelerate to vTop, then maintain the speed, and then
    //            decelerate to vEndMax
    //   Case 5b: accelerate to v2 (vEndMax < v2 < vTop), and then
    //            decelerate to vEndMax immediately
    //   Case 5c: accelerate to vEndMax immediately  (vEndMax = v2)
    //   Case 5d: infeasible case; can't accelerate to vEndMax
    //            due to distance constraint  (vEndMax > v2)

    double t1 = (vTop - v1) / accel;
    double d1 = t1 * (vTop + v1) / 2.0;
    double t3 = (vEndMax - vTop) / decel;
    double d3 = t3 * (vEndMax + vTop) / 2.0;

    if (d1 + d3 < dTotal) {  // Case 5a
      double d2 = dTotal - d1 - d3;
      double t2 = d2 / vTop;

      AccelSchedule ap = new AccelSchedule();
      ap.add(time1, accel);
      ap.add(time1 + t1, 0.0);
      ap.add(time1 + t1 + t2, decel);
      ap.add(time1 + t1 + t2 + t3, 0.0);  // end
      return new ArrivalEstimationResult(time1 + t1 + t2 + t3, vEndMax, ap);
    } else {  // d1+d3 >= dTotal  // Case 5b or 5c or 5d
      // The computation is the same as case 4's computation,
      // but we need to distinguish case 5b, 5c, and 5d.
      double delta = (accel * vEndMax * vEndMax - decel * v1 * v1
          - 2.0*accel*decel*dTotal) / (accel - decel);
      assert delta >= 0.0;  // must be true

      double v2 = Math.sqrt(delta);
      assert v2 < vTop;  // It must be true; otherwise, it should have
                         // been handled by case 5a
      assert v1 < v2;   // if untrue, t1x < 0, which is unacceptible.
      double t1x = (v2 - v1) / accel;
      double d1x = t1x * (v2 + v1) / 2.0;
      assert d1x > 0.0;   // this is important to check this
                          // since vehicles can't go backward
      if (vEndMax < v2) {  // Case 5b  (must be put before case 5c)
        double t3x = (vEndMax - v2) / decel;
        double d3x = t3x * (vEndMax + v2) / 2.0;
        assert d3x > 0.0;  // not even d3x = 0 since vEndMax < v2
        AccelSchedule ap = new AccelSchedule();
        ap.add(time1, accel);
        ap.add(time1 + t1x, decel);
        ap.add(time1 + t1x + t3x, 0.0);  // end
        return new ArrivalEstimationResult(time1 + t1x + t3x, vEndMax, ap);
      } else if (Util.isDoubleEqual(vEndMax, v2)) {   // Case 5c
        assert (Util.isDoubleEqual(d1x, dTotal));
        AccelSchedule ap = new AccelSchedule();
        ap.add(time1, accel);
        ap.add(time1 + t1x, 0.0);  // end
        return new ArrivalEstimationResult(time1 + t1x, vEndMax, ap);
      } else {  // Case 5d: vEndMax > v2 => t3x < 0 => d3x < 0  (not okay)
        return estimateMaxVEndForCase2AndCase5(time1, v1, dTotal, vTop,
                                               vEndMax, accel);
      }
    }
  }

  /**
   * Compute the acceleration schedule and the arrival parameters
   * at the intersection (Case 6).
   *
   * @param time1    the initial time
   * @param v1       the initial velocity
   * @param dTotal   the distance between the vehicle and the intersection
   * @param vTop     the speed limit of the road
   * @param vEndMax  the speed limit at the intersection
   * @param accel    the maximum acceleration
   * @param decel    the maximum deceleration
   *
   * @return the acceleration schedule and estimated arrival parameters
   *         at the intersection; null if there is no solution
   * @throws if the algorithm returns no solution.
   */
  private static ArrivalEstimationResult estimateForCase6(double time1,
                                                          double v1,
                                                          double dTotal,
                                                          double vTop,
                                                          double vEndMax,
                                                          double accel,
                                                          double decel)
                                           throws ArrivalEstimationException {
    // Case 6: v1,vEndMax < vTop  &&  v1 > vEndMax
    //   Case 6a: accelerate to vTop, then maintain the speed, and then
    //            decelerate to vEndMax
    //   Case 6b: accelerate to v2 (v1 < v2 < vTop), and then
    //            decelerate to vEndMax immediately
    //   Case 6c: decelerate to vEndMax immediately  (v1 = v2)
    //   Case 6d: infeasible case; can't accelerate to vEndMax
    //            due to distance constraint  (v2 < v1)

    double t1 = (vTop - v1) / accel;
    double d1 = t1 * (vTop + v1) / 2.0;
    double t3 = (vEndMax - vTop) / decel;
    double d3 = t3 * (vEndMax + vTop) / 2.0;

    if (d1 + d3 < dTotal) {  // Case 6a
      double d2 = dTotal - d1 - d3;
      double t2 = d2 / vTop;

      AccelSchedule ap = new AccelSchedule();
      ap.add(time1, accel);
      ap.add(time1 + t1, 0.0);
      ap.add(time1 + t1 + t2, decel);
      ap.add(time1 + t1 + t2 + t3, 0.0);  // end
      return new ArrivalEstimationResult(time1 + t1 + t2 + t3, vEndMax, ap);
    } else {  // d1+d3 >= dTotal  // Case 6b or 6c or 6d
      // The computation is the same as case 5's computation,
      // but some conditions are quite different
      double delta = (accel * vEndMax * vEndMax - decel * v1 * v1
          - 2.0*accel*decel*dTotal) / (accel - decel);
      assert delta >= 0.0;  // must be true

      double v2 = Math.sqrt(delta);
      assert v2 < vTop;  // It must be true; otherwise, it should have
                         // been handled by case 6a
      assert vEndMax < v2;   // if untrue, t3x < 0, which is unacceptible.
      double t3x = (vEndMax - v2) / decel;
      double d3x = t3x * (vEndMax + v2) / 2.0;
      assert d3x > 0.0;   // this is important to check this
                          // since vehicles can't go backward
      if (v1 < v2) {  // Case 6b  (must be put before case 6c)
        double t1x = (v2 - v1) / accel;
        double d1x = t1x * (v2 + v1) / 2.0;
        assert d1x > 0.0; // not even d1x = 0 since v1 < v2
        AccelSchedule ap = new AccelSchedule();
        ap.add(time1, accel);
        ap.add(time1 + t1x, decel);
        ap.add(time1 + t1x + t3x, 0.0);  // end
        return new ArrivalEstimationResult(time1 + t1x + t3x, vEndMax, ap);
      } else if (Util.isDoubleEqual(v1, v2)) {   // Case 6c
        assert (Util.isDoubleEqual(d3x, dTotal,
                                   Constants.DOUBLE_EQUAL_WEAK_PRECISION));
        AccelSchedule ap = new AccelSchedule();
        ap.add(time1, decel);
        ap.add(time1 + t3x, 0.0);  // end
        return new ArrivalEstimationResult(time1 + t3x, vEndMax, ap);
      } else {  // Case 6d: v1 > v2 => t1x < 0 => d1x < 0  (not okay)
        // no matter how quick the vehicle decelerates,
        // it can't reduce its velocity below vEndMax
        // since the distance is too short
        throw new ArrivalEstimationException(
          "Arrival estimation failed: distance too small (Case 6d)") ;
      }
    }
  }


  /**
   * Compute the acceleration schedule and the arrival parameters
   * at the intersection (a special case in Case 2 and Case 5).
   *
   * @param time1    the initial time
   * @param v1       the initial velocity
   * @param dTotal   the distance between the vehicle and the intersection
   * @param vTop     the speed limit of the road
   * @param vEndMax  the speed limit at the intersection
   * @param accel    the maximum acceleration
   *
   * @return the acceleration schedule and estimated arrival parameters
   *         at the intersection; null if there is no solution
   */
  private static ArrivalEstimationResult estimateMaxVEndForCase2AndCase5(
                                                               double time1,
                                                               double v1,
                                                               double dTotal,
                                                               double vTop,
                                                               double vEndMax,
                                                               double accel) {

    // Case 2c: v1 <  vTop == vEndMax, but d1 > dTotal
    // Case 5d: v1,vEndMax < vTop  &&  v1 < vEndMax, but
    //          vEndMax > v2 => t3x < 0 => d3x < 0

    // In both cases, in order to maximize vEnd,
    // the vehicle should accelerate immediately and
    // as much as possible.
    //
    // Solution:
    //    dTotal = t1x * (v1 + vEnd) / 2
    //    vEnd = v1 + t1x * accel
    //  Thus,
    //    dTotal = ((vEnd - v1) / accel) * (v1 + vEnd) / 2
    //    dTotal = (vEnd^2 - v1^2) / (2 * accel)
    //    2 * accel * dTotal = vEnd^2 - v1^2
    //    vEnd^2 = 2 * accel * dTotal + v1^2
    //  Let Delta = 2 * accel * dTotal + v1^2
    //    We can see that delta >= 0 since all variables are positive
    //    vEnd = sqrt(2 * accel * dTotal + v1^2)  // choose positive solution
    //    t1x = (vEnd - v1) / accel

    double vEnd = Math.sqrt(2 * accel * dTotal + v1 * v1);
    assert (vEnd < vEndMax);
    double t1x = (vEnd - v1) / accel;
    AccelSchedule ap = new AccelSchedule();
    ap.add(time1, accel);
    ap.add(time1 + t1x, 0.0); // end
    return new ArrivalEstimationResult(time1 + t1x, vEnd, ap);
  }


  /////////////////////////////////
  // DEBUG
  /////////////////////////////////

  /**
   * Validate the acceleration schedule and the arrival parameters
   * at the intersection.
   *
   * @param time1    the initial time
   * @param v1       the initial velocity
   * @param dTotal   the distance between the vehicle and the intersection
   * @param vTop     the speed limit of the road
   * @param vEndMax  the speed limit at the intersection
   * @param accel    the maximum acceleration
   * @param decel    the maximum deceleration
   * @param result   the acceleration schedule and the arrival parameters
   *                 at the intersection
   *
   * @return true if the result is valid.
   */
  private static boolean isResultValid(double time1,
                                       double v1,
                                       double dTotal,
                                       double vTop,
                                       double vEndMax,
                                       double accel,
                                       double decel,
                                       ArrivalEstimationResult result) {
    assert result != null;

    AccelSchedule as = result.getAccelSchedule();
    double vEnd = as.calcFinalVelocity(v1);
    if (Util.isDoubleNotEqual(vEnd, result.getArrivalVelocity(),
                              Constants.DOUBLE_EQUAL_WEAK_PRECISION)) {
      if (isDebugging) {
        System.err.printf("Error in VelocityFirstArrivalEstimation." +
                          "isResultValid(): " +
                          "vEnd != arrival velocity\n");
        System.err.printf("%.9f != %.9f\n", vEnd, result.getArrivalVelocity());
      }
      return false;
    }
    if (Util.isDoubleNotEqual(vEnd, vEndMax,
                              Constants.DOUBLE_EQUAL_WEAK_PRECISION)
      && vEnd > vEndMax) {
      if (isDebugging) {
        System.err.printf("Error in VelocityFirstArrivalEstimation." +
                          "isResultValid(): " +
                          "vEnd > vEndMax\n");
        System.err.printf("vEnd = %.9f\n", vEnd);
        System.err.printf("vEndMax = %.9f\n", vEndMax);
      }
      return false;
    }
    if (!as.checkVelocityUpperLimit(v1, vTop)) {
      if (isDebugging) {
        System.err.printf("Error in VelocityFirstArrivalEstimation." +
                          "isResultValid(): " +
                          "velocity exceeds upper limit\n");
      }
      return false;
    }

    double d = as.calcTotalDistance(time1, v1, result.getArrivalTime());
    if (Util.isDoubleNotEqual(d, dTotal,
                              Constants.DOUBLE_EQUAL_WEAK_PRECISION)) {
      if (isDebugging) {
        System.err.printf("d      = %.10f\n", d);
        System.err.printf("dTotal = %.10f\n", dTotal);
        System.err.printf("Error in VelocityFirstArrivalEstimation." +
                          "isResultValid(): d != dTotal\n");
      }
      return false;
    }

    return true;
  }

}

