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
package aim4.noise;

import aim4.util.Util;

/**
 * A gauge that holds doubles.  This gauge also can apply noise
 * functions to incoming data to simulate a noisy gauge.
 */
public class DoubleGauge {

  /**
   * The actual value being measured by the gauge.
   */
  private double value = 0;
  /**
   * The maximum value the gauge can read.
   */
  private double maxValue = Double.MAX_VALUE;
  /**
   * The minimum value the gauge can read
   */
  private double minValue = Double.MIN_VALUE;
  /**
   * The function that determines how the gauge reads.
   */
  private NoiseFunction noiseFunction = BasicNoiseFunction.noNoise;

  // Constructors
  /**
   * Class constructor for unlimited, uninitialized, noiseless gauge.
   */
  public DoubleGauge() {}

  /**
   * Class constructor for unlimited, uninitialized gauge with noise.
   *
   * @param noiseFunction the noise function this gauge will apply to values
   */
  public DoubleGauge(NoiseFunction noiseFunction) {
    this.noiseFunction = noiseFunction;
  }

  /**
   * Class constructor for unlimited, noiseless gauge with initial value.
   *
   * @param value the initial value of the gauge
   */
  public DoubleGauge(double value) {
    this.value = value;
  }

  /**
   * Class constructor for unlimited, initialized gauge with noise.
   *
   * @param value the initial value of the gauge
   * @param noiseFunction the noise function this gauge will apply to values
   */
  public DoubleGauge(double value, NoiseFunction noiseFunction) {
    this.value = value;
    this.noiseFunction = noiseFunction;
  }

  /**
   * Class constructor for limited, initialized, noiseless gauge.
   *
   * @param value    the initial value of the gauge
   * @param minValue the minimum value the gauge can store/read
   * @param maxValue the maximum value the gauge can store/read
   */
  public DoubleGauge(double value, double minValue, double maxValue) {
    this.value = value;
    this.minValue = minValue;
    this.maxValue = maxValue;
  }

  /**
   * Class Constructor for limited, initialized, noisy gauge.
   *
   * @param value         the initial value of the gauge
   * @param minValue      the minimum value the gauge can store/read
   * @param maxValue      the maximum value the gauge can store/read
   * @param noiseFunction the noise function this gauge will apply to values
   */
  public DoubleGauge(double value, double minValue, double maxValue,
                     NoiseFunction noiseFunction) {
    this.value = value;
    this.minValue = minValue;
    this.maxValue = maxValue;
    this.noiseFunction = noiseFunction;
  }

  // Get and Set

  /**
   * Read the value of the gauge.
   *
   * @return the value of the gauge.
   */
  public double read() {
    return value;
  }

  /**
   * Records a value to the gauge, with noise according to the
   * gauge's {@link NoiseFunction}.
   *
   * @param recValue the value to be written to the gauge
   */
  public void record(double recValue) {
    double v = noiseFunction.apply(recValue);
    value = Util.constrain(v, minValue, maxValue);
  }
}
