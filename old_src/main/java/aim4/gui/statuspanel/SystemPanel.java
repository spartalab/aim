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

import javax.swing.JLabel;
import javax.swing.JPanel;

import aim4.config.Constants;
import aim4.gui.StatusPanelInterface;
import aim4.gui.component.FormattedLabel;

/**
 * The System Panel
 */
public class SystemPanel extends JPanel
                         implements StatusPanelInterface {

  private static final long serialVersionUID = 1L;

  // ///////////////////////////////
  // PRIVATE FIELDS
  // ///////////////////////////////

  /**
   * The number of digits needed to display the maximum amount of memory, in
   * megabytes, available to the Java Virtual Machine.
   */
  private final int MAX_MEMORY_DIGITS =
    (int) Math.log10(Runtime.getRuntime().maxMemory()
      / Constants.BYTES_PER_MB) + 1;

  /**
   * The operating system name, version, and architecture.
   */
  private JLabel systemOSInfoLabel =
    new JLabel("Operating System: " + System.getProperty("os.name") + " "
      + System.getProperty("os.version") + " ("
      + System.getProperty("os.arch") + ")");
  /**
   * The version of Java under which the viewer is running.
   */
  private JLabel systemJavaInfoLabel =
    new JLabel("Java Version: " + System.getProperty("java.version"));
  /**
   * The total amount of memory that the Java Virtual Machine can obtain from
   * the operating system.
   */
  private JLabel systemMaximumMemoryLabel =
    new JLabel("Maximum JVM Memory: "
      + (Runtime.getRuntime().maxMemory() / Constants.BYTES_PER_MB) + " MB");

  /**
   * The amount of memory currently in use by the viewer and simulator.
   */
  private FormattedLabel systemMemUsageLabel =
    new FormattedLabel("Memory Usage: ", "%" + MAX_MEMORY_DIGITS + "d MB",
                       MAX_MEMORY_DIGITS + 3);
  /**
   * The total amount of memory currently allocated to the Java Virtual
   * Machine.
   */
  private FormattedLabel systemAvailableMemoryLabel =
    new FormattedLabel("Allocated JVM Memory: ", "%" + MAX_MEMORY_DIGITS
      + "d MB", MAX_MEMORY_DIGITS + 3);

  // ///////////////////////////////
  // CONSTRUCTORS
  // ///////////////////////////////

  /**
   * Create a system panel.
   */
  public SystemPanel() {
    GridBagLayout gridbag = new GridBagLayout();
    setLayout(gridbag);

    GridBagConstraints c = new GridBagConstraints();
    c.fill = GridBagConstraints.BOTH;
    c.weightx = 1.0;
    c.weighty = 1.0;

    // OS Info
    systemOSInfoLabel.setFont(FormattedLabel.FONT);
    c.gridwidth = 1; // reset to default
    gridbag.setConstraints(systemOSInfoLabel, c);
    add(systemOSInfoLabel);
    // Java Info
    systemJavaInfoLabel.setFont(FormattedLabel.FONT);
    c.gridwidth = GridBagConstraints.REMAINDER; // end row
    gridbag.setConstraints(systemJavaInfoLabel, c);
    add(systemJavaInfoLabel);
    // Memory Usage
    c.gridwidth = 1; // reset to default
    gridbag.setConstraints(systemMemUsageLabel, c);
    add(systemMemUsageLabel);
    // Available Memory
    c.gridwidth = GridBagConstraints.REMAINDER; // end row
    gridbag.setConstraints(systemAvailableMemoryLabel, c);
    add(systemAvailableMemoryLabel);
    // Maximum Memory
    systemMaximumMemoryLabel.setFont(FormattedLabel.FONT);
    c.gridwidth = GridBagConstraints.REMAINDER; // end row
    gridbag.setConstraints(systemMaximumMemoryLabel, c);
    add(systemMaximumMemoryLabel);
  }

  // ///////////////////////////////
  // PUBLIC METHODS
  // ///////////////////////////////

  /**
   * {@inheritDoc}
   */
  @Override
  public void update() {
    // Calculate the amount of memory we are using, in MB
    long memUsage =
      (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory())
        / Constants.BYTES_PER_MB;
    systemMemUsageLabel.update(memUsage);
    // Available Memory
    systemAvailableMemoryLabel.update(Runtime.getRuntime().totalMemory()
      / Constants.BYTES_PER_MB);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void clear() {
    systemMemUsageLabel.clear();
    systemAvailableMemoryLabel.clear();
  }

}