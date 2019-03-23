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
package aim4.map;

import java.awt.Shape;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import aim4.vehicle.VehicleSimView;

/**
 * The data collection line.
 */
public class DataCollectionLine {

  /////////////////////////////////
  // CONSTANTS
  /////////////////////////////////

  /** The no repeat time period */
  private static final double NO_REPEAT_TIME_PERIOD = 1.0; // seconds

  /////////////////////////////////
  // PRIVATE FIELDS
  /////////////////////////////////

  /** The name of this data collection line */
  private String name;
  /** The ID of this data collection line */
  private int id;
  /** The line */
  private Line2D line;
  /** The record of the times of the vehicle passing through the line */
  private Map<Integer,List<Double>> vinToTime;
  /**
   * Whether vehicles should not be counted more than once when it passes
   * through the line more than once within the NO_REPEAT_TIME_PERIOD.
   */
  private boolean isNoRepeat;

  /////////////////////////////////
  // CONSTRUCTORS
  /////////////////////////////////

  /**
   * Create a data collection line.
   *
   * @param name        the name of the data collection line
   * @param id          the ID of the line
   * @param p1          the first point of the line
   * @param p2          the second point of the line
   * @param isNoRepeat  Whether vehicles should not be counted more than once
   *                    when it passes through the line more than once within
   *                    the NO_REPEAT_TIME_PERIOD.
   */
  public DataCollectionLine(String name, int id, Point2D p1, Point2D p2,
                            boolean isNoRepeat) {
    this.name = name;
    this.id = id;
    this.vinToTime = new HashMap<Integer,List<Double>>();
    this.line = new Line2D.Double(p1, p2);
    this.isNoRepeat = isNoRepeat;
  }

  /////////////////////////////////
  // PUBLIC METHODS
  /////////////////////////////////

  /**
   * Get the shape of the line.
   *
   * @return the shape of the line
   */
  public Shape getShape() {
    return line;
  }

  /**
   * Whether the vehicle intersects the line.
   *
   * @param v     the vehicle
   * @param time  the current time
   * @param p1    the first point of the vehicle
   * @param p2    the second point of the vehicle
   * @return whether the vehicle intersects the line
   */
  public boolean intersect(VehicleSimView v, double time,
                           Point2D p1, Point2D p2) {
    int vin = v.getVIN();
    if (!isNoRepeat
      || !vinToTime.containsKey(vin)
      || vinToTime.get(vin).get(vinToTime.get(vin).size()-1)
        + NO_REPEAT_TIME_PERIOD < time) {
      if (line.intersectsLine(p1.getX(), p1.getY(), p2.getX(), p2.getY())) {
        if (!vinToTime.containsKey(vin)) {
          List<Double> times = new LinkedList<Double>();
          times.add(time);
          vinToTime.put(vin, times);
        } else {
          vinToTime.get(vin).add(time);
        }
        return true;
      } else {
        return false;
      }
    } else {  // the vehicle passed through this data collection line
              // twice or more within last NO_REPEAT_TIME_PERIOD seconds
      return false;
    }
  }

  /**
   * Get the name of the line.
   *
   * @return the name of the line
   */
  public String getName() {
    return name;
  }

  /**
   * Get the ID of the line.
   *
   * @return the ID of the line
   */
  public int getId() {
    return id;
  }

  /**
   * Get the VINs of all vehicles.
   *
   * @return the VINs of all vehicles
   */
  public Set<Integer> getAllVIN() {
    return vinToTime.keySet();
  }

  /**
   * Get the time a vehicle passing through the line.
   *
   * @param vin  the VIN of the vehicle
   * @return the time the vehicle passing through the line
   */
  public List<Double> getTimes(int vin) {
    return vinToTime.get(vin);
  }

}
