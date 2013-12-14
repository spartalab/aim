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
package aim4.gui.parampanel;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

import aim4.gui.component.LabeledSlider;
import aim4.sim.setup.BasicSimSetup;

/**
 * The autonomous driver only simulation parameter panel.
 */
public class AutoDriverOnlyParamPanel extends JPanel {
  private static final long serialVersionUID = 1L;

  LabeledSlider trafficRateSlider;
  LabeledSlider speedLimitSlider;
  LabeledSlider stopDistToIntersectionSlider;
  LabeledSlider numOfColumnSlider;
  LabeledSlider numOfRowSlider;
  LabeledSlider lanesPerRoadSlider;

  /**
   * Create the autonomous driver only simulation parameter panel.
   *
   * @param simSetup  the simulation setup
   */
  public AutoDriverOnlyParamPanel(BasicSimSetup simSetup) {
    setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

    // create the components

    trafficRateSlider =
      new LabeledSlider(0.0, 2500.0,
                        simSetup.getTrafficLevel() * 3600.0,
                        500.0, 100.0,
                        "Traffic Level: %.0f vehicles/hour/lane",
                        "%.0f");
    add(trafficRateSlider);

    speedLimitSlider =
      new LabeledSlider(0.0, 80.0,
                        simSetup.getSpeedLimit(),
                        10.0, 5.0,
                        "Speed Limit: %.0f meters/second",
                        "%.0f");
    add(speedLimitSlider);

    stopDistToIntersectionSlider =
      new LabeledSlider(0.0, 50.0,
                        simSetup.getStopDistBeforeIntersection(),
                        10.0, 1.0,
                        "Stopping Distance Before Intersection: %.0f meters",
                        "%.0f");
    add(stopDistToIntersectionSlider);

    numOfColumnSlider =
        new LabeledSlider(1.0, 5.0,
                          simSetup.getColumns(),
                          1.0, 1.0,
                          "Number of North-bound/South-bound Roads: %.0f",
                          "%.0f");
    add(numOfColumnSlider);

    numOfRowSlider =
      new LabeledSlider(1.0, 5.0,
                        simSetup.getColumns(),
                        1.0, 1.0,
                        "Number of East-bound/West-bound Roads: %.0f",
                        "%.0f");
    add(numOfRowSlider);

    lanesPerRoadSlider =
      new LabeledSlider(1.0, 8.0,
                        simSetup.getLanesPerRoad(),
                        1.0, 1.0,
                        "Number of Lanes per Road: %.0f",
                        "%.0f");
    add(lanesPerRoadSlider);


  }

  /**
   * Get the traffic rate.
   *
   * @return the traffic rate
   */
  public double getTrafficRate() {
    return trafficRateSlider.getValue() / 3600.0;
  }

  /**
   * Get the speed limit.
   *
   * @return the speed limit
   */
  public double getSpeedLimit() {
    return speedLimitSlider.getValue();
  }

  /**
   * Get the stop distance to intersection.
   *
   * @return the stop distance to intersection
   */
  public double getStopDistToIntersection() {
    double d = stopDistToIntersectionSlider.getValue();
    return (d < 1.0)?1.0:d;
  }

  /**
   * Get the number of columns.
   *
   * @return the number of columns
   */
  public int getNumOfColumns() {
    return (int)numOfColumnSlider.getValue();
  }

  /**
   * Get the number of rows.
   *
   * @return the number of rows
   */
  public int getNumOfRows() {
    return (int)numOfRowSlider.getValue();
  }

  /**
   * Get the number of lanes per road.
   *
   * @return the number of lanes per road
   */
  public int getLanesPerRoad() {
    return (int)lanesPerRoadSlider.getValue();
  }
}