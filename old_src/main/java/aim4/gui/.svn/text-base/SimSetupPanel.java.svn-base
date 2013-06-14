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
package aim4.gui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import aim4.gui.parampanel.AutoDriverOnlyParamPanel;
import aim4.gui.parampanel.TrafficSignalParamPanel;
import aim4.sim.setup.ApproxStopSignSimSetup;
import aim4.sim.setup.ApproxNPhasesTrafficSignalSimSetup;
import aim4.sim.setup.AutoDriverOnlySimSetup;
import aim4.sim.setup.BasicSimSetup;
import aim4.sim.setup.SimSetup;

/**
 * The simulation setup panel.
 */
public class SimSetupPanel extends JPanel implements ItemListener {

  private static final long serialVersionUID = 1L;

  final static String AUTO_DRIVER_ONLY_SETUP_PANEL = "AIM Protocol";
  final static String TRAFFIC_SIGNAL_SETUP_PANEL = "Traffic Signals";
  final static String STOP_SIGN_SETUP_PANEL = "Stop Signs";

  /** The combox box */
  private JComboBox comboBox;
  /** The card panel */
  private JPanel cards; //a panel that uses CardLayout
  /** The card layout */
  private CardLayout cardLayout;
  /** the auto driver only simulation setup panel */
  private AutoDriverOnlyParamPanel autoDriverOnlySetupPanel;
  /** The traffic signal setup panel */
  private TrafficSignalParamPanel trafficSignalSetupPanel;
  /** The simulation setup panel */
  private BasicSimSetup simSetup;

  /**
   * Create a simulation setup panel
   *
   * @param initSimSetup  the initial simulation setup
   */
  public SimSetupPanel(BasicSimSetup initSimSetup) {
    this.simSetup = initSimSetup;

    // create the combo box pane
    JPanel comboBoxPane = new JPanel(); //use FlowLayout
    comboBoxPane.setBackground(Color.WHITE);

    String comboBoxItems[] =
      { AUTO_DRIVER_ONLY_SETUP_PANEL,
        TRAFFIC_SIGNAL_SETUP_PANEL,
        STOP_SIGN_SETUP_PANEL };
    comboBox = new JComboBox(comboBoxItems);
    comboBox.setEditable(false);
    comboBox.addItemListener(this);
    comboBoxPane.add(comboBox);

    // create the cards pane
    cardLayout = new CardLayout();
    cards = new JPanel(cardLayout);

    // add the parameter panels
    autoDriverOnlySetupPanel =
      new AutoDriverOnlyParamPanel(simSetup);
    addParamPanel(autoDriverOnlySetupPanel, AUTO_DRIVER_ONLY_SETUP_PANEL);
    trafficSignalSetupPanel = new TrafficSignalParamPanel();
    cards.add(trafficSignalSetupPanel, TRAFFIC_SIGNAL_SETUP_PANEL);
    cards.add(new JPanel(), STOP_SIGN_SETUP_PANEL);

    // add the combo box pane and cards pane
    setLayout(new BorderLayout());
    add(comboBoxPane, BorderLayout.PAGE_START);
    add(cards, BorderLayout.CENTER);
  }

  /**
   * Add a parameter panel.
   *
   * @param paramPanel  the parameter panel
   * @param paramLabel  the label of the parameter panel
   */
  private void addParamPanel(JPanel paramPanel, String paramLabel) {
    JScrollPane scrollPane = new JScrollPane(paramPanel);
    scrollPane.setBorder(BorderFactory.createEmptyBorder());
    cards.add(scrollPane, paramLabel);
  }

  /**
   * Create and return a simulation setup object.
   *
   * @return the simulation setup object
   */
  public SimSetup getSimSetup() {
    if (comboBox.getSelectedIndex() == 0) {
      AutoDriverOnlySimSetup simSetup2 = new AutoDriverOnlySimSetup(simSetup);
      simSetup2.setTrafficLevel(autoDriverOnlySetupPanel.getTrafficRate());
      simSetup2.setSpeedLimit(autoDriverOnlySetupPanel.getSpeedLimit());
      simSetup2.setStopDistBeforeIntersection(
        autoDriverOnlySetupPanel.getStopDistToIntersection());
      simSetup2.setNumOfColumns(autoDriverOnlySetupPanel.getNumOfColumns());
      simSetup2.setNumOfRows(autoDriverOnlySetupPanel.getNumOfRows());
      simSetup2.setLanesPerRoad(autoDriverOnlySetupPanel.getLanesPerRoad());
      return simSetup2;
    } else if (comboBox.getSelectedIndex() == 1) {
      // ApproxNPhasesTrafficSignalSimSetup simSetup2 =
      //  new ApproxNPhasesTrafficSignalSimSetup(simSetup,
      //                                         "src/main/resources/SignalPhases/AIM4Phases.csv");
      // simSetup2.setTrafficVolume("src/main/resources/SignalPhases/AIM4Volumes.csv");
      ApproxNPhasesTrafficSignalSimSetup simSetup2 =
        new ApproxNPhasesTrafficSignalSimSetup(simSetup,
                                               "/SignalPhases/AIM4Phases.csv");
      simSetup2.setTrafficVolume("/SignalPhases/AIM4Volumes.csv");

      simSetup2.setLanesPerRoad(trafficSignalSetupPanel.getLanesPerRoad());
      simSetup2.setStopDistBeforeIntersection(1.0);
      return simSetup2;
    } else if (comboBox.getSelectedIndex() == 2) {
      ApproxStopSignSimSetup simSetup2 =
        new ApproxStopSignSimSetup(simSetup);
      simSetup2.setTrafficLevel(autoDriverOnlySetupPanel.getTrafficRate());
      simSetup2.setStopDistBeforeIntersection(
        autoDriverOnlySetupPanel.getStopDistToIntersection());
      return simSetup2;
    } else {
      throw new RuntimeException(
          "SimSetupPane::getSimSetup(): not implemented yet");
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void itemStateChanged(ItemEvent evt) {
    cardLayout.show(cards, (String)evt.getItem());
  }
}