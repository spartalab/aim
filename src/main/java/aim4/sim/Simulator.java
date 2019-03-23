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
package aim4.sim;

import java.util.Set;

import aim4.map.BasicMap;
import aim4.vehicle.ProxyVehicleSimView;
import aim4.vehicle.VehicleSimView;

/**
 * An interface for simulators.
 */
public interface Simulator {

  /**
   * An interface denoting the result of a simulation step.
   */
  public static interface SimStepResult {}

  /**
   * Move the simulator a time step forward.
   *
   * @param timeStep  the amount of time the simulation should run in
   *                  this time step
   */
  SimStepResult step(double timeStep);

  /**
   * Get the layout of the simulation.
   *
   * @return the layout of the simulation.
   */
  BasicMap getMap();

  /**
   * Get the total amount of simulation time has run.
   *
   * @return the simulation time.
   */
  double getSimulationTime();

  /**
   * Get the number of vehicles that has reached their destinations.
   *
   * @return the number of vehicles that has reached their destinations
   */
  int getNumCompletedVehicles();

  /**
   * Get average number of bits transmitted by completed vehicles.
   *
   * @return the average number of bits transmitted by completed vehicles
   */
  double getAvgBitsTransmittedByCompletedVehicles();

  /**
   * Get average number of bits received by completed vehicles.
   *
   * @return the average number of bits received by completed vehicles
   */
  double getAvgBitsReceivedByCompletedVehicles();

  /**
   * Get the set of all active vehicles in the simulation.
   *
   * @return the set of all active vehicles in the simulation
   */
  Set<VehicleSimView> getActiveVehicles();

  /**
   * Get a particular active vehicle via a given VIN.
   *
   * @param vin  the VIN number of the vehicle
   * @return the active vehicle
   */
  VehicleSimView getActiveVehicle(int vin);

  /**
   * Add the proxy vehicle to the simulator for the mixed reality experiments.
   *
   * @param vehicle  the proxy vehicle
   */
  void addProxyVehicle(ProxyVehicleSimView vehicle);
}
