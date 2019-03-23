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
package aim4.config;

/**
 * The configuration of a simulation.
 */
public class SimConfig {
  /**
   * The time the simulation should run.
   * If it is less than or equal to zero, the simulation will run forever.
   */
  public static double TOTAL_SIMULATION_TIME = -1.0;

  /**
   * The number of cycles per second ({@value}) at which the simulator runs.
   */
  public static final double CYCLES_PER_SECOND = 50.0;

  /**
   * The length of a time step (simulation time) in the simulator
   * ({@value} seconds).
   */
  public static final double TIME_STEP = 1 / CYCLES_PER_SECOND;

  /**
   * The length of a time step (simulation time) in the reservation grid
   * ({@value} seconds).
   */
  public static final double GRID_TIME_STEP = TIME_STEP;

  /**
   * How often the simulator should consider spawning vehicles.
   */
  public static final double SPAWN_TIME_STEP = TIME_STEP / 5.0;

  /**
   * Whether or not the vehicle must stop before an intersection
   */
  public static boolean MUST_STOP_BEFORE_INTERSECTION = false;

  /**
   * The distance before the stopping distance before an intersection
   * such that a vehicle can consider moving again when
   * MUST_STOP_BEFORE_INTERSECTION is true.
   */
  public static final double ADDITIONAL_STOP_DIST_BEFORE_INTERSECTION = 0.01;
}
