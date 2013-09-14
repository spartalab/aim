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

/**
 * The traffic signal parameter panel.
 */
public class TrafficSignalParamPanel extends JPanel {
  private static final long serialVersionUID = 1L;

  LabeledSlider trafficRateSlider;
  LabeledSlider greenLightDurationSlider;
  LabeledSlider yelloLightDurationSlider;
  LabeledSlider lanesPerRoadSlider;

  /**
   * Create a traffic signal parameter panel.
   */
  public TrafficSignalParamPanel() {
    setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

    // create the components

    trafficRateSlider =
      new LabeledSlider(0.0, 2500.0,
                        800.0,
                        500.0, 100.0,
                        "Traffic Level: %.0f vehicles/hour/lane",
                        "%.0f");
    add(trafficRateSlider);

    lanesPerRoadSlider =
      new LabeledSlider(1.0, 8.0, 3.0, 1.0, 1.0,
                        "Number of Lanes per Road: %.0f",
                        "%.0f");
    add(lanesPerRoadSlider);

    greenLightDurationSlider =
      new LabeledSlider(0.0, 60.0, 30.0, 5.0, 1.0,
                        "Green Signal Duration: %.1f seconds",
                        "%.0f");
    add(greenLightDurationSlider);

    yelloLightDurationSlider =
      new LabeledSlider(0.0, 60.0, 5.0, 5.0, 1.0,
                        "Yellow Signal Duration: %.1f seconds",
                        "%.0f");
    add(yelloLightDurationSlider);
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
   * Get the green light duration.
   *
   * @return the green light duration
   */
  public double getGreenLightDuration() {
    return greenLightDurationSlider.getValue();
  }

  /**
   * Get the yellow light duration.
   *
   * @return the yellow light duration
   */
  public double getYellowLightDuration() {
    return yelloLightDurationSlider.getValue();
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