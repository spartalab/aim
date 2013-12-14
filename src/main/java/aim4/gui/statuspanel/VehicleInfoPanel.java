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
package aim4.gui.statuspanel;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JPanel;

import aim4.config.Constants;
import aim4.config.Debug;
import aim4.gui.StatusPanelInterface;
import aim4.gui.component.FormattedLabel;
import aim4.vehicle.AutoVehicleSimView;
import aim4.vehicle.VehicleSimView;
import aim4.vehicle.VinRegistry;

/**
 * The Vehicle Information Panel
 */
public class VehicleInfoPanel extends JPanel
                              implements StatusPanelInterface {

  private static final long serialVersionUID = 1L;

  // ///////////////////////////////
  // PRIVATE FIELDS
  // ///////////////////////////////

  /** The selected vehicle's ID number. */
  private FormattedLabel vehicleVINLabel =
    new FormattedLabel("VIN: ", "%6d", 6);
  /** The type of the selected vehicle. */
  private FormattedLabel vehicleSpecLabel =
    new FormattedLabel("Vehicle Type: ", "%-15s", 15);
  /** The selected vehicle's velocity. */
  private FormattedLabel vehicleVelocityLabel =
    new FormattedLabel("Velocity: ", "%5.2f m/s", 9);
  /** The selected vehicle's acceleration. */
  private FormattedLabel vehicleAccelerationLabel =
    new FormattedLabel("Acceleration: ", "%5.2f m/s/s", 11);
  /**
   * The amount of information that has been transmitted by the selected
   * vehicle.
   */
  private FormattedLabel vehicleTransmittedLabel =
    new FormattedLabel("Data Transmitted: ", "%5.2f kB", 8);
  /**
   * The amount of information that has been received by the selected vehicle.
   */
  private FormattedLabel vehicleReceivedLabel =
    new FormattedLabel("Data received: ", "%5.2f kB", 8);

  // ///////////////////////////////
  // CONSTRUCTORS
  // ///////////////////////////////

  /**
   * Create a vehicle information panel.
   */
  public VehicleInfoPanel() {
    GridBagLayout gridbag = new GridBagLayout();
    setLayout(gridbag);

    GridBagConstraints c = new GridBagConstraints();
    c.fill = GridBagConstraints.BOTH;
    c.weightx = 1.0;
    c.weighty = 1.0;

    // Vehicle ID Number
    c.gridwidth = 1; // restore default
    gridbag.setConstraints(vehicleVINLabel, c);
    add(vehicleVINLabel);
    // Vehicle Type
    c.gridwidth = GridBagConstraints.REMAINDER; // end row
    gridbag.setConstraints(vehicleSpecLabel, c);
    add(vehicleSpecLabel);
    // Velocity
    c.gridwidth = 1; // restore default
    gridbag.setConstraints(vehicleVelocityLabel, c);
    add(vehicleVelocityLabel);
    // Acceleration
    c.gridwidth = GridBagConstraints.REMAINDER; // end row
    gridbag.setConstraints(vehicleAccelerationLabel, c);
    add(vehicleAccelerationLabel);
    // Data transmitted
    c.gridwidth = 1; // default
    gridbag.setConstraints(vehicleTransmittedLabel, c);
    add(vehicleTransmittedLabel);
    // Data received
    c.gridwidth = GridBagConstraints.REMAINDER; // end row
    gridbag.setConstraints(vehicleReceivedLabel, c);
    add(vehicleReceivedLabel);
  }

  // ///////////////////////////////
  // PUBLIC METHODS
  // ///////////////////////////////

  /**
   * {@inheritDoc}
   */
  @Override
  public void update() {
    VehicleSimView v = VinRegistry.getVehicleFromVIN(Debug.getTargetVIN());
    if (v != null) {
      // Vehicle ID Number
      vehicleVINLabel.update(v.getVIN());
      // Vehicle Type
      vehicleSpecLabel.update(v.getSpec().getName());
      // Velocity
      vehicleVelocityLabel.update(v.getVelocity());
      // Acceleration
      vehicleAccelerationLabel.update(v.getAcceleration());

      if (v instanceof AutoVehicleSimView) {
        AutoVehicleSimView v2 = (AutoVehicleSimView) v;
        // Data Transmitted
        vehicleTransmittedLabel.update(v2.getBitsTransmitted()
          / (double) (Constants.BITS_PER_BYTE * Constants.BYTES_PER_KB));
        // Data Received
        vehicleReceivedLabel.update(v2.getBitsReceived()
          / (double) (Constants.BITS_PER_BYTE * Constants.BYTES_PER_KB));
      }
    } else { // No vehicle selected, clear everything
      clear();
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void clear() {
    vehicleVINLabel.clear();
    vehicleSpecLabel.clear();
    vehicleVelocityLabel.clear();
    vehicleAccelerationLabel.clear();
    vehicleTransmittedLabel.clear();
    vehicleReceivedLabel.clear();
  }
}