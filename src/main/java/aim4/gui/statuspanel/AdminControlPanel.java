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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JPanel;

import aim4.config.Debug;
import aim4.gui.StatusPanelInterface;
import aim4.gui.Viewer;
import aim4.im.IntersectionManager;
import aim4.im.v2i.V2IManager;
import aim4.im.v2i.RequestHandler.AllStopRequestHandler;
import aim4.im.v2i.RequestHandler.FCFSRequestHandler;
import aim4.im.v2i.RequestHandler.GoStraightRequestHandler;
import aim4.im.v2i.policy.BasePolicy;
import aim4.sim.Simulator;

/**
 * The Administration Control Panel
 */
public class AdminControlPanel extends JPanel
                               implements StatusPanelInterface,
                                          ActionListener {

  private static final long serialVersionUID = 1L;

  /////////////////////////////////
  // PRIVATE FIELDS
  /////////////////////////////////

  private JButton fcfsButton;
  private JButton stopButton;
  private JButton straightButton;
  private JButton mixedLightButton;

  /** The viewer object */
  Viewer viewer;

  // ///////////////////////////////
  // CONSTRUCTORS
  // ///////////////////////////////

  /**
   * Create an administration control panel.
   *
   * @param viewer the viewer object
   */
  public AdminControlPanel(Viewer viewer) {
    this.viewer = viewer;

    fcfsButton = new JButton("FCFS");
    stopButton = new JButton("Stop");
    straightButton = new JButton("Alternate");


    // layout
    GroupLayout layout = new GroupLayout(this);
    setLayout(layout);
    layout.setAutoCreateGaps(false);
    layout.setAutoCreateContainerGaps(false);

    layout.setHorizontalGroup(layout
      .createSequentialGroup()
        .addComponent(fcfsButton)
        .addComponent(stopButton)
        .addComponent(straightButton)
    );

    layout.setVerticalGroup(layout
      .createParallelGroup(GroupLayout.Alignment.CENTER)
        .addComponent(fcfsButton)
        .addComponent(stopButton)
        .addComponent(straightButton)
    );

    // add event handler
    fcfsButton.addActionListener(this);
    stopButton.addActionListener(this);
    straightButton.addActionListener(this);
  }

  /////////////////////////////////
  // PUBLIC METHODS
  /////////////////////////////////

  /**
   * {@inheritDoc}
   */
  @Override
  public void update() {
    // do nothing
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void clear() {
    // do nothing
  }


  /////////////////////////////////
  // EVENT HANDLERS
  /////////////////////////////////

  /**
   * {@inheritDoc}
   */
  @Override
  public void actionPerformed(ActionEvent e) {
    if (e.getSource() == fcfsButton ||
        e.getSource() == stopButton ||
        e.getSource() == straightButton) {
      int imId = Debug.getTargetIMid();
      if (imId >= 0) {
        Simulator sim = viewer.getSimulator();
        IntersectionManager im0 =
          sim.getMap().getIntersectionManagers().get(imId);
        assert im0.getId() == imId;
        if (im0 instanceof V2IManager) {
          V2IManager im = (V2IManager)im0;
          if (im.getPolicy() instanceof BasePolicy) {
            BasePolicy policy = (BasePolicy)im.getPolicy();
            if (e.getSource() == fcfsButton) {
              policy.setRequestHandler(new FCFSRequestHandler());
            } else if (e.getSource() == stopButton) {
              policy.setRequestHandler(new AllStopRequestHandler());
            } else if (e.getSource() == straightButton) {
              policy.setRequestHandler(new GoStraightRequestHandler());
            }
          }
        }
      }
    }
  }

}