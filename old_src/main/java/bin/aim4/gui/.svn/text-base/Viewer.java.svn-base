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

import java.awt.CardLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Point2D;
import java.io.IOException;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;

import aim4.config.Constants;
import aim4.config.Debug;
import aim4.config.SimConfig;
import aim4.gui.frame.VehicleInfoFrame;
import aim4.im.IntersectionManager;
import aim4.map.Road;
import aim4.map.lane.Lane;
import aim4.sim.Simulator;
import aim4.sim.UdpListener;
import aim4.sim.AutoDriverOnlySimulator.AutoDriverOnlySimStepResult;
import aim4.sim.Simulator.SimStepResult;
import aim4.sim.setup.BasicSimSetup;
import aim4.sim.setup.SimFactory;
import aim4.sim.setup.SimSetup;
import aim4.util.Util;
import aim4.vehicle.VehicleSimView;

/**
 * The viewer is a Graphical User Interface (GUI) that allows a user to run the
 * AIM Simulator while watching the vehicles in real time.
 */
public class Viewer extends JFrame implements ActionListener, KeyListener,
                                              MouseListener, ItemListener,
                                              ViewerDebugView {

  // ///////////////////////////////
  // CONSTANTS
  // ///////////////////////////////
  /** The serial version ID for serialization */
  private static final long serialVersionUID = 1L;
  /**
   * Whether or not the current simulation time is shown on screen.
   */
  public static final boolean IS_SHOW_SIMULATION_TIME = true;
  /**
   * Whether or not the simulator shows the vin of the vehicles on screen.
   */
  public static final boolean IS_SHOW_VIN_BY_DEFAULT = false;
  /**
   * Whether or not the IM Shapes are shown by default.
   */
  public static final boolean IS_SHOW_IM_DEBUG_SHAPES_BY_DEFAULT = false;
  // ///////////////////////////////
  // CONSTANTS
  // ///////////////////////////////
  // simulation speed
  /**
   * The number of simulation seconds per GUI second. If it is larger than or
   * equal to <code>TURBO_SIM_SPEED</code>, the simulation will run as fast as
   * possible.
   */
  public static final double DEFAULT_SIM_SPEED = 15.0;
  /**
   * The number of screen updates per GUI second. If it is larger than or
   * equal to SimConfig.CYCLES_PER_SECOND, the screen will be updated at
   * every time step of the simulation.
   */
  public static final double DEFAULT_TARGET_FRAME_RATE = 20.0;
  /**
   * The simulation speed (simulation seconds per GUI second) at or beyond which
   * the turbo mode is on (i.e., the simulation will run as fast as possible)
   */
  public static final double TURBO_SIM_SPEED = 15.0;
  /**
   * The String to display in the title bar of the main app.
   */
  private static final String TITLEBAR_STRING = "AIM Viewer";
  /**
   * Preferred maximum width for the canvas, in pixels. {@value} pixels.
   */
  private static final int PREF_MAX_CANVAS_WIDTH = 650;
  /**
   * Preferred maximum height for the canvas, in pixels. {@value} pixels.
   */
  private static final int PREF_MAX_CANVAS_HEIGHT = 650;
  /**
   * The width of the start/pause/resume button and the step buttons. {@value}
   * pixels.
   */
  private static final int DEFAULT_BUTTON_WIDTH = 100; // px
  /**
   * The height of the status pane. {@value} pixels.
   */
  private static final int DEFAULT_STATUS_PANE_HEIGHT = 200; // px
  /**
   * The inset size of the setup panels
   */
  private static final int SIM_SETUP_PANE_GAP = 50;

  /////////////////////////////////
  // NESTED CLASSES
  /////////////////////////////////
  //
  // TODO: SimThread should be a SwingWorker; but it works fine now.
  // http://java.sun.com/docs/books/tutorial/uiswing/concurrency/worker.html
  //
  /**
   * The simulation thread that holds the simulation process.
   */
  public class SimThread implements Runnable {

    // ///////////////////////////////
    // PRIVATE FIELDS
    // ///////////////////////////////
    /** The simulation thread */
    private volatile Thread blinker;
    /** Whether the turbo mode is on */
    private boolean isTurboMode;
    /**
     * In the turbo mode, it is the duration of each execution period In
     * the non turbo mode, it is the time period between simulation steps
     */
    private long timeDelay;
    /** Whether the stepping mode is on */
    private boolean isSteppingMode;
    /** Whether the simulation is stopped */
    private boolean isStopped;

    // ///////////////////////////////
    // CONSTRUCTORS
    // ///////////////////////////////
    /**
     * Create a simulation thread.
     *
     * @param isTurboMode  Whether the turbo mode is on
     * @param timeDelay    The time delay
     */
    public SimThread(boolean isTurboMode, long timeDelay) {
      this.blinker = null;
      this.isTurboMode = isTurboMode;
      this.timeDelay = timeDelay;
      this.isSteppingMode = false;
      this.isStopped = false;
    }

    // ///////////////////////////////
    // PUBLIC METHODS
    // ///////////////////////////////

    // information retrieval

    /**
     * Whether the thread is stopped.
     *
     * @return Whether the thread is stopped.
     */
    public boolean isPaused() {
      return isStopped;
    }

    /**
     * Whether the thread is in the turbo mode.
     *
     * @return Whether the thread is in the turbo mode.
     */
    public boolean isTurboMode() {
      return isTurboMode;
    }

    // Settings

    /**
     * Set whether the turbo mode is on
     *
     * @param isTurboMode  Whether the turbo mode is on
     */
    public synchronized void setTurboMode(boolean isTurboMode) {
      this.isTurboMode = isTurboMode;
    }

    /**
     * Set whether the stepping mode is on
     *
     * @param isSteppingMode  Whether the stepping mode is on
     */
    public synchronized void setSteppingMode(boolean isSteppingMode) {
      this.isSteppingMode = isSteppingMode;
    }

    /**
     * Set the time delay.
     *
     * @param timeDelay  the time delay.
     */
    public synchronized void setTimeDelay(long timeDelay) {
      this.timeDelay = timeDelay;
    }

    // ///////////////////////////////
    // PUBLIC METHODS
    // ///////////////////////////////

    // thread control

    /**
     * Start the simulation thread.
     */
    public synchronized void start() {
      assert blinker == null;
      this.blinker = new Thread(this, "AIM4 Simulator Thread");
      blinker.start();
    }

    /**
     * Terminate/Kill this simulation thread.
     */
    public synchronized void terminate() {
      assert blinker != null;
      blinker = null;
    }

    /**
     * Pause this simulation thread
     */
    public void pause() {
      // must have no synchronized keyword in order to avoid
      // funny behavior when the user clicks the "Pause" button.
      assert !isStopped;
      isStopped = true;
    }

    /**
     * Resume this simulation thread
     */
    public synchronized void resume() {
      assert isStopped;
      isStopped = false;
    }

    // ///////////////////////////////
    // PUBLIC METHODS
    // ///////////////////////////////

    // the run() function of the thread

    /**
     * {@inheritDoc}
     */
    @Override
    public void run() {
      Thread thisThread = Thread.currentThread();
      while (blinker == thisThread) {
        if (isStopped) {
          try {
            Thread.sleep(10L); // just sleep for a very short moment
          } catch (InterruptedException e) {
            // do nothing
          }
        } else if (isTurboMode) {
          runTurboMode();
        } else {
          runNormalMode();
        }
        // in any case, give other threads a chance to execute
        Thread.yield();
      }
      System.err.printf("The simulation has terminated.\n");
    }

    /**
     * Run the thread in the turbo mode.
     */
    private synchronized void runTurboMode() {
      double nextFastRunningStepTime = System.currentTimeMillis() + timeDelay;
      while (!isStopped) {
        runSimulationStep();
        // give GUI a chance to update the screen
        if (!updateScreenForOneStepInFastRunningMode()) {
          break;
        }
        // only one simulation step in stepping mode
        if (isSteppingMode) {
          break;
        }
        // check to see whether the time is up
        if (System.currentTimeMillis() >= nextFastRunningStepTime) {
          break;
        }
      }
      // give GUI a chance to update the screen
      updateScreenInTurboMode();
      // if in stepping mode, just stop until resume() is called
      if (isSteppingMode) {
        isStopped = true;
      }
    }

    /**
     * Run the thread in the normal mode.
     */
    private synchronized void runNormalMode() {
      long nextInvokeTime = System.currentTimeMillis() + timeDelay;
      // Advance the simulation for one step
      runSimulationStep();
      // give GUI a chance to update the screen
      updateScreenInNormalMode();
      // if in stepping mode, just stop until resume() is called
      if (isSteppingMode) {
        isStopped = true;
      } else {
        // else may sleep for a while
        long t = nextInvokeTime - System.currentTimeMillis();
        if (t > 0) {
          try {
            Thread.sleep(t);
          } catch (InterruptedException e) {
            // do nothing
          }
        } else {
          // System.err.printf("Warning: Simulation is slower than GUI\n");
        }
      }
    }

  }

  // ///////////////////////////////
  // PRIVATE FIELDS
  // ///////////////////////////////

  /** The initial configuration of the simulation */
  private BasicSimSetup initSimSetup;
  /** The Simulator running in this Viewer. */
  private Simulator sim;
  /** The simulation's thread */
  private SimThread simThread;
  /** UDP listener */
  private UdpListener udpListener;
  /** The target simulation speed */
  private double targetSimSpeed;
  /** The target frame rate */
  private double targetFrameRate;
  /** The time of the next screen update in millisecond */
  private long nextFrameTime;
  // recording
  // TODO: reset imageCounter after reset the simulator
  /** Whether or not to save the screen during simulation */
  boolean recording;
  /** Image's direction */
  String imageDir;
  /** The number of generated images */
  int imageCounter;
  // GUI Items
  /** The main pane */
  private JPanel mainPanel;
  /** The card layout for the canvas */
  private CardLayout canvasCardLayout;
  /** The canvas on which to draw the state of the simulator. */
  private Canvas canvas;
  /** The simulation setup pane */
  private SimSetupPanel simSetupPanel;
  /** The status pane on which to display statistics. */
  private StatusPanelContainer statusPanel;
  /** The Start/Pause/Resume Button */
  private JButton startButton;
  /** The Step Button */
  private JButton stepButton;
  /** The frame for showing a vehicle information */
  private VehicleInfoFrame vehicleInfoFrame;
  // Menu Items
  /** Menu item "Autonomous Vehicles Only" */
  // private JCheckBoxMenuItem autoOnlySimTypeMenuItem;
  /** Menu item "Human Drivers Only" */
  // private JCheckBoxMenuItem humanOnlySimTypeMenuItem;
  /** Menu item "Human Drivers Only" */
  // private JCheckBoxMenuItem mixedSimTypeMenuItem;
  /** Menu item "Start Simulation Process" */
  private JMenuItem startMenuItem;
  /** Menu item "Step" */
  private JMenuItem stepMenuItem;
  /** Menu item "Reset" */
  private JMenuItem resetMenuItem;
  /** Menu item "Dump Data Collection Lines' Data" */
  private JMenuItem dumpDataMenuItem;
  /** Menu item for activating recording. */
  private JMenuItem startRecordingMenuItem;
  /** Menu item for deactivating recording. */
  private JMenuItem stopRecordingMenuItem;
  /** Menu item for starting the UDP listener */
  private JMenuItem startUdpListenerMenuItem;
  /** Menu item for stopping the UDP listener */
  private JMenuItem stopUdpListenerMenuItem;
  /** Menu item for controlling whether to show the simulation time */
  private JCheckBoxMenuItem showSimulationTimeMenuItem;
  /** Menu item for controlling whether to show VIN numbers */
  private JCheckBoxMenuItem showVinMenuItem;
  /** Menu item for controlling whether to show debug shapes */
  private JCheckBoxMenuItem showIMShapesMenuItem;
  /** Menu item for clearing simulator's debug point */
  private JMenuItem clearDebugPointsMenuItem;

  // ///////////////////////////////
  // CLASS CONSTRUCTORS
  // ///////////////////////////////

  /**
   * Create a new viewer object.
   *
   * @param initSimSetup  the initial simulation setup
   */
  public Viewer(BasicSimSetup initSimSetup) {
    this(initSimSetup, false);
  }

  /**
   * Create a new viewer object.
   *
   * @param initSimSetup  the initial simulation setup
   * @param isRunNow      whether or not the simulation is run immediately
   */
  public Viewer(final BasicSimSetup initSimSetup, final boolean isRunNow) {
    super(TITLEBAR_STRING);
    this.initSimSetup = initSimSetup;
    this.sim = null;
    this.udpListener = null;
    this.simThread = null;

    targetSimSpeed = DEFAULT_SIM_SPEED;
    // the frame rate cannot be not larger than the simulation cycle
    targetFrameRate =
        Math.min(DEFAULT_TARGET_FRAME_RATE, SimConfig.CYCLES_PER_SECOND);
    this.nextFrameTime = 0; // undefined yet.

    this.recording = false;
    this.imageDir = null;
    this.imageCounter = 0;

    // for debugging
    Debug.viewer = this;

    // Lastly, schedule a job for the event-dispatching thread:
    // creating and showing this application's GUI.
    javax.swing.SwingUtilities.invokeLater(new Runnable() {

      /**
       * {@inheritDoc}
       */
      @Override
      public void run() {
        createAndShowGUI(initSimSetup, isRunNow);
      }

    });
  }

  /**
   * Create a new GUI and show it.
   *
   * @param initSimSetup  the initial simulation setup
   * @param isRunNow      whether or not the simulation is run immediately
   */
  private void createAndShowGUI(BasicSimSetup initSimSetup, boolean isRunNow) {
    // Apple specific property.
    System.setProperty("apple.laf.useScreenMenuBar", "true");
    System.setProperty("com.apple.mrj.application.apple.menu.about.name",
        "AIM Viewer");
    // Make sure that the program quits when we close the window
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    // Build the GUI
    createMenuBar();
    createComponents();
    setComponentsLayout();
    pack(); // pick the layout and show it
    setVisible(true);
    initGUIsetting();

    // Make self key listener
    setFocusable(true);
    requestFocusInWindow();
    addKeyListener(this);

    if (isRunNow) {
      startButtonHandler(initSimSetup);
      canvas.requestFocusInWindow();
    }
  }

  /**
   * Create the menu system.
   */
  private void createMenuBar() {
    JMenuBar menuBar = new JMenuBar();
    JMenu menu;
    JMenuItem menuItem;

    setJMenuBar(menuBar);

    // The File menu exists only on non-Mac OS X environment
    if (!System.getProperty("os.name").equals("Mac OS X")) {
      // File
      menu = new JMenu("File");
      menuBar.add(menu);
      // File->Quit
      menuItem = new JMenuItem("Quit AIM");
      menuItem.addActionListener(this);
      menu.add(menuItem);
    }

    // Simulation
    menu = new JMenu("Simulator");
    menuBar.add(menu);

    /*
    // Simulator->Autonomous Vehicles Only
    autoOnlySimTypeMenuItem =
        new JCheckBoxMenuItem("Autonomous Vehicles Only", true);
    autoOnlySimTypeMenuItem.addActionListener(this);
    menu.add(autoOnlySimTypeMenuItem);
    // Simulator->Human Drivers Only
    humanOnlySimTypeMenuItem =
        new JCheckBoxMenuItem("Human Drivers Only", false);
    humanOnlySimTypeMenuItem.addActionListener(this);
    menu.add(humanOnlySimTypeMenuItem);
    // Simulator->Mixed Drivers
    mixedSimTypeMenuItem = new JCheckBoxMenuItem("Mixed Drivers", false);
    mixedSimTypeMenuItem.addActionListener(this);
    menu.add(mixedSimTypeMenuItem);
    // Simulator->separator
    // menu.addSeparator();
    */

    // Simulator->Start Simulation
    startMenuItem = new JMenuItem("Start");
    startMenuItem.addActionListener(this);
    menu.add(startMenuItem);
    // Simulator->Step
    stepMenuItem = new JMenuItem("Step");
    stepMenuItem.addActionListener(this);
    menu.add(stepMenuItem);
    // Simulator->Reset
    resetMenuItem = new JMenuItem("Reset");
    resetMenuItem.addActionListener(this);
    menu.add(resetMenuItem);

    // Data
    menu = new JMenu("Data");
    menuBar.add(menu);
    // Data->Dump Data Collection Lines' Data
    dumpDataMenuItem = new JMenuItem("Dump Data Collection Lines' Data");
    dumpDataMenuItem.addActionListener(this);
    menu.add(dumpDataMenuItem);

    // Recording
    menu = new JMenu("Recording");
    menuBar.add(menu);
    // Recording->Start
    startRecordingMenuItem = new JMenuItem("Start");
    startRecordingMenuItem.addActionListener(this);
    menu.add(startRecordingMenuItem);
    // Recording->Stop
    stopRecordingMenuItem = new JMenuItem("Stop");
    stopRecordingMenuItem.addActionListener(this);
    menu.add(stopRecordingMenuItem);

    // UDP
    menu = new JMenu("UDP");
    menuBar.add(menu);
    // UDP->Start Listening
    startUdpListenerMenuItem = new JMenuItem("Start Listening");
    startUdpListenerMenuItem.addActionListener(this);
    startUdpListenerMenuItem.setEnabled(false);
    menu.add(startUdpListenerMenuItem);
    // UDP->Stop Listening
    stopUdpListenerMenuItem = new JMenuItem("Stop Listening");
    stopUdpListenerMenuItem.addActionListener(this);
    stopUdpListenerMenuItem.setEnabled(false);
    menu.add(stopUdpListenerMenuItem);

    // View
    menu = new JMenu("View");
    menuBar.add(menu);
    // View->Show simulation time
    showSimulationTimeMenuItem = new JCheckBoxMenuItem("Show Simulation Time",
        IS_SHOW_SIMULATION_TIME);
    showSimulationTimeMenuItem.addItemListener(this);
    menu.add(showSimulationTimeMenuItem);
    // View->Show VIN numbers
    showVinMenuItem = new JCheckBoxMenuItem("Show VINs",
        IS_SHOW_VIN_BY_DEFAULT);
    showVinMenuItem.addItemListener(this);
    menu.add(showVinMenuItem);
    // View->Show IM Shapes
    showIMShapesMenuItem = new JCheckBoxMenuItem("Show IM Shapes", false);
    showIMShapesMenuItem.addItemListener(this);
    menu.add(showIMShapesMenuItem);

    // Debug
    menu = new JMenu("Debug");
    menuBar.add(menu);
    // Debug->Clear Debug Points
    clearDebugPointsMenuItem = new JMenuItem("Clear Debug Points");
    clearDebugPointsMenuItem.addActionListener(this);
    menu.add(clearDebugPointsMenuItem);
  }

  /**
   * Create all components in the viewer
   */
  private void createComponents() {
    mainPanel = new JPanel();
    canvas = new Canvas(this);
    simSetupPanel = new SimSetupPanel(initSimSetup);
    statusPanel = new StatusPanelContainer(this);
    startButton = new JButton("Start");
    startButton.addActionListener(this);
    stepButton = new JButton("Step");
    stepButton.setEnabled(false);
    stepButton.addActionListener(this);
  }

  /**
   * Set the layout of the viewer
   */
  private void setComponentsLayout() {
    // set the card layout for the layered pane
    canvasCardLayout = new CardLayout();
    mainPanel.setLayout(canvasCardLayout);
    mainPanel.setPreferredSize(new Dimension(PREF_MAX_CANVAS_WIDTH,
        PREF_MAX_CANVAS_HEIGHT));

    // create the pane for containing the sim setup pane
    JPanel panel1 = new JPanel();
    panel1.setBackground(Canvas.GRASS_COLOR);
    panel1.setLayout(new GridBagLayout());
    GridBagConstraints c1 = new GridBagConstraints();
    c1.gridx = 0;
    c1.gridy = 0;
    c1.fill = GridBagConstraints.BOTH;
    c1.weightx = 1.0;
    c1.weighty = 1.0;
    c1.insets = new Insets(SIM_SETUP_PANE_GAP,
        SIM_SETUP_PANE_GAP,
        SIM_SETUP_PANE_GAP,
        SIM_SETUP_PANE_GAP);
    panel1.add(simSetupPanel, c1);
    // add the panel to the top layer
    mainPanel.add(panel1, "SIM_SETUP_PANEL");

    // create ...
//    JPanel panel2 = new JPanel();
//    panel2.setBackground(Color.BLUE);
//    panel2.setLayout(new GridBagLayout());
//    GridBagConstraints c2 = new GridBagConstraints();
//    c2.gridx = 0;
//    c2.gridy = 0;
//    c2.fill = GridBagConstraints.BOTH;
//    c2.weightx = 1.0;
//    c2.weighty = 1.0;
//    panel2.add(canvas, c2);
//    // add the canvas to the second layer
//    mainPanel.add(panel2, "CANVAS");


    // add the canvas to the second layer
    mainPanel.add(canvas, "CANVAS");

    // set the group layout
    Container pane = getContentPane();
    GroupLayout layout = new GroupLayout(pane);
    pane.setLayout(layout);
    // Turn on automatically adding gaps between components
    layout.setAutoCreateGaps(false);
    // Turn on automatically creating gaps between components that touch
    // the edge of the container and the container.
    layout.setAutoCreateContainerGaps(false);
    // layout for the horizontal axis
    layout.setHorizontalGroup(layout.createParallelGroup(
        GroupLayout.Alignment.LEADING).addComponent(mainPanel).addGroup(layout.createSequentialGroup().addGroup(
        layout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(
        startButton,
        DEFAULT_BUTTON_WIDTH,
        GroupLayout.DEFAULT_SIZE,
        DEFAULT_BUTTON_WIDTH).addComponent(
        stepButton, DEFAULT_BUTTON_WIDTH,
        GroupLayout.DEFAULT_SIZE,
        DEFAULT_BUTTON_WIDTH)).addComponent(
        statusPanel)));
    // layout for the vertical axis
    layout.setVerticalGroup(
        layout.createSequentialGroup().addComponent(mainPanel).addGroup(
        layout.createParallelGroup(GroupLayout.Alignment.CENTER).addGroup(layout.createSequentialGroup().addComponent(
        startButton).addComponent(stepButton)).addComponent(statusPanel,
        DEFAULT_STATUS_PANE_HEIGHT,
        GroupLayout.DEFAULT_SIZE,
        DEFAULT_STATUS_PANE_HEIGHT)));
  }

  // ///////////////////////////////
  // PUBLIC METHODS
  // ///////////////////////////////

  /**
   * Get the simulator object.
   *
   * @return the simulator object; null if the simulator object has not been
   *         created.
   */
  public Simulator getSimulator() {
    return sim;
  }

  // ///////////////////////////////
  // GUI settings
  // ///////////////////////////////

  /**
   * Initialize the GUI setting.
   */
  private void initGUIsetting() {
    resetButtonMenuItem();
    startRecordingMenuItem.setEnabled(true);
    stopRecordingMenuItem.setEnabled(false);
    startUdpListenerMenuItem.setEnabled(false);
    stopUdpListenerMenuItem.setEnabled(false);
    showSimulationTimeMenuItem.setSelected(IS_SHOW_SIMULATION_TIME);
    showVinMenuItem.setSelected(IS_SHOW_VIN_BY_DEFAULT);
    showIMShapesMenuItem.setSelected(IS_SHOW_IM_DEBUG_SHAPES_BY_DEFAULT);
  }

  /**
   * Use the simulation start GUI setting.
   */
  private void setSimStartGUIsetting() {
    canvasCardLayout.show(mainPanel, "CANVAS");
    canvas.initWithGivenMap(sim.getMap());
    statusPanel.init();

    // update the buttons
    startButton.setText("Pause");
    stepButton.setEnabled(false);
    // update the menu items
    /*
    autoOnlySimTypeMenuItem.setEnabled(false);
    humanOnlySimTypeMenuItem.setEnabled(false);
    mixedSimTypeMenuItem.setEnabled(false);
    */
    startMenuItem.setText("Pause");
    stepMenuItem.setEnabled(false);
    resetMenuItem.setEnabled(true);
    dumpDataMenuItem.setEnabled(true);
    startUdpListenerMenuItem.setEnabled(true);
    clearDebugPointsMenuItem.setEnabled(true);
  }

  /**
   * Use the simulation reset GUI setting.
   */
  private void setSimResetGUIsetting() {
    canvas.cleanUp();
    statusPanel.clear();
    resetButtonMenuItem();
  }

  /**
   * Reset the button menu items.
   */
  private void resetButtonMenuItem() {
    canvasCardLayout.show(mainPanel, "SIM_SETUP_PANEL");
    // update the buttons
    startButton.setText("Start");
    stepButton.setEnabled(false);
    // update the menu items
    /*
    autoOnlySimTypeMenuItem.setSelected(true);
    humanOnlySimTypeMenuItem.setSelected(false);
    mixedSimTypeMenuItem.setSelected(false);
    */
    startMenuItem.setText("Start");
    stepMenuItem.setEnabled(false);
    resetMenuItem.setEnabled(false);
    dumpDataMenuItem.setEnabled(false);
    startUdpListenerMenuItem.setEnabled(false);
    clearDebugPointsMenuItem.setEnabled(false);
  }

  // ///////////////////////////////
  // interface's event handlers
  // ///////////////////////////////

  /**
   * The handler when the user pressed the start button.
   */
  private void startButtonHandler() {
    startButtonHandler(simSetupPanel.getSimSetup());
  }

  /**
   * The handler when the user pressed the start button.
   *
   * @param initSimSetup  the initial simulation setup
   */
  private void startButtonHandler(SimSetup initSimSetup) {
    if (simThread == null) {
      startSimProcess(initSimSetup);
    } else if (!simThread.isPaused()) {
      pauseSimProcess();
    } else {
      resumeSimProcess();
    }
  }

  /**
   * The handler when the user pressed the step button.
   */
  private void stepButtonHandler() {
    stepSimProcess();
  }

  // ///////////////////////////////
  // Simulation controls
  // ///////////////////////////////

  /**
   * Start the simulation process.
   *
   * @param initSimSetup the initial simulation setup.
   */
  private void startSimProcess(SimSetup initSimSetup) {
    assert sim == null && udpListener == null;

    // create the simulator
    sim = SimFactory.makeSimulator(initSimSetup);
    // create the simulation thread
    createSimThread();
    // initialize the GUI
    setSimStartGUIsetting();
    // start the thread
    nextFrameTime = System.currentTimeMillis();
    simThread.start();
  }

  /**
   * Pause the simulation process.
   */
  private void pauseSimProcess() {
    assert simThread != null && !simThread.isPaused();

    simThread.pause();

    // update the buttons
    startButton.setText("Resume");
    stepButton.setEnabled(true);
    // update the menu items
    startMenuItem.setText("Resume");
    stepMenuItem.setEnabled(true);
  }


  /**
   * Resume the simulation process.
   */
  private void resumeSimProcess() {
    assert simThread != null && simThread.isPaused();

    simThread.setSteppingMode(false);
    simThread.resume();
    nextFrameTime = System.currentTimeMillis();

    // update the buttons
    startButton.setText("Pause");
    stepButton.setEnabled(false);
    // update the menu items
    startMenuItem.setText("Pause");
    stepMenuItem.setEnabled(false);
  }

  /**
   * Step the simulation process.
   */
  private void stepSimProcess() {
    assert simThread != null && simThread.isPaused();

    simThread.setSteppingMode(true);
    simThread.resume();
  }

  /**
   * Reset the simulation process.
   */
  private void resetSimProcess() {
    assert simThread != null;

    simThread.terminate();
    if (simThread.isPaused()) {
      simThread.setSteppingMode(false);
      simThread.resume();
    }
    simThread = null;
    sim = null;

    if (udpListener != null) {
      stopUdpListening();
    }

    setSimResetGUIsetting();
  }

  // //////////////////////////////////////////////////
  // Private methods for interface's event handlers
  // //////////////////////////////////////////////////

  /**
   * Initialize the default Simulator to use.
   */
  private void createSimThread() {
    if (0 < targetSimSpeed
        && targetSimSpeed < TURBO_SIM_SPEED) {
      long timerDelay =
          (long) (1000.0 * SimConfig.TIME_STEP / targetSimSpeed);
      simThread = new SimThread(false, timerDelay);
    } else {
      long timerDelay;
      if (targetFrameRate < SimConfig.CYCLES_PER_SECOND) {
        timerDelay = (long) (1000.0 / targetFrameRate);
      } else {
        timerDelay = (long) (1000.0 / SimConfig.CYCLES_PER_SECOND);
      }
      simThread = new SimThread(true, timerDelay);
    }
  }


  /**
   * Set the target simulation speed.
   *
   * @param simSpeed  set the target simulation speed
   */
  public void setTargetSimSpeed(double simSpeed) {
    this.targetSimSpeed = simSpeed;
    if (simThread != null) {
      if (Util.isDoubleZero(simSpeed)) {
        long timerDelay = (long) (1000.0 * SimConfig.TIME_STEP / 0.1);
        simThread.setTimeDelay(timerDelay);
        simThread.setTurboMode(false);
        if (!simThread.isPaused()) {
          pauseSimProcess();
        }
      } else if (Util.isDoubleEqualOrGreater(simSpeed, TURBO_SIM_SPEED)) {
        long timerDelay;
        if (targetFrameRate < SimConfig.CYCLES_PER_SECOND) {
          timerDelay = (long) (1000.0 / targetFrameRate);
        } else {
          timerDelay = (long) (1000.0 / SimConfig.CYCLES_PER_SECOND);
        }
        simThread.setTimeDelay(timerDelay);
        simThread.setTurboMode(true);
        if (simThread.isPaused()) {
          resumeSimProcess();
        }
      } else {
        long timerDelay = (long) (1000.0 * SimConfig.TIME_STEP / simSpeed);
        simThread.setTimeDelay(timerDelay);
        simThread.setTurboMode(false);
        if (simThread.isPaused()) {
          resumeSimProcess();
        }
      }
    }
    canvas.requestFocusInWindow();
  }

  /**
   * Set the target frame rate.
   *
   * @param targetFrameRate the target frame rate
   */
  public void setTargetFrameRate(double targetFrameRate) {
    this.targetFrameRate =
        Math.min(targetFrameRate, SimConfig.CYCLES_PER_SECOND);

    if (simThread != null) {
      if (simThread.isTurboMode()) {
        long timerDelay;
        if (0.0 < targetFrameRate) {
          timerDelay = (long) (1000.0 / targetFrameRate);
        } else {
          timerDelay = (long) (1000.0 / 10.0);
        }
        simThread.setTimeDelay(timerDelay);
      }
    }
  }

  // //////////////////////////////////////////////////
  // Methods invoked by SimThread
  // //////////////////////////////////////////////////

  /**
   * Run the simulation
   */
  private void runSimulationStep() {
    Debug.clearShortTermDebugPoints();
    SimStepResult simStepResult = sim.step(SimConfig.TIME_STEP);

    if (simStepResult instanceof AutoDriverOnlySimStepResult) {
      AutoDriverOnlySimStepResult simStepResult2 =
          (AutoDriverOnlySimStepResult) simStepResult;
      for (int vin : simStepResult2.getCompletedVINs()) {
        Debug.removeVehicleColor(vin);
      }
    }
  }

  /**
   * Update screen after a simulation step for the fast running mode
   *
   * @return true if the simulation's thread should continue to execute; false
   *         if the simulation's thread should take a break and call
   *         updateScreenInFastRunningMode();
   */
  private boolean updateScreenForOneStepInFastRunningMode() {
    return (0.0 < targetFrameRate)
        && (targetFrameRate < SimConfig.CYCLES_PER_SECOND);
  }

  /**
   * Update screen after an execution period in the fast running mode
   */
  private void updateScreenInTurboMode() {
    if (0.0 < targetFrameRate) {
      updateScreen();
      saveScreenShot();
    }
  }

  /**
   * Update screen for the non fast running mode
   */
  private void updateScreenInNormalMode() {

    if (targetFrameRate >= SimConfig.CYCLES_PER_SECOND) {
      // update as fast as possible
      updateScreen();
      saveScreenShot();
    } else if (0.0 < targetFrameRate) {
      if (System.currentTimeMillis() > nextFrameTime) {
        updateScreen();
        saveScreenShot();
        nextFrameTime =
            System.currentTimeMillis() + (long) (1000.0 / targetFrameRate);
      }
    } // else targetFrameRate == 0.0 then do nothing
  }

  // //////////////////////////////////////////////////
  // Basic fucntions for GUI updates
  // //////////////////////////////////////////////////

  /**
   * Update the screen
   */
  private void updateScreen() {
    canvas.update();
    statusPanel.update();
  }

  /**
   * Save a screenshot
   */
  private void saveScreenShot() {
    if (recording && imageDir != null) {
      String outFileName =
          imageDir + "/" + Constants.LEADING_ZEROES.format(imageCounter++)
          + ".png";
      canvas.saveScreenShot(outFileName);
    }
  }

  // ///////////////////////////////////////////
  // ActionListener interface for menu items
  // ///////////////////////////////////////////

  /**
   * {@inheritDoc}
   */
  @Override
  public void actionPerformed(ActionEvent e) {
    /*
    if (e.getSource() == autoOnlySimTypeMenuItem) {
      throw new RuntimeException("Cannot change simulation type yet.");
      // // very ugly code. need update
      // GridLayoutUtil.setFCFSManagers((GridLayout)sim.getLayout(),
      // sim.getSimulatedTime(),
      // 1.0);
      // GridLayoutUtil.setUniformRandomSpawnPoints((GridLayout)
      // sim.getLayout(),
      // 0.25);
      // sim = new AutoDriverOnlySimulator(sim.getLayout());
      // autoOnlySimTypeMenuItem.setSelected(true);
      // humanOnlySimTypeMenuItem.setSelected(false);
      // mixedSimTypeMenuItem.setSelected(false);
    } else if (e.getSource() == humanOnlySimTypeMenuItem) {
      throw new RuntimeException("Human drivers only simulation not "
          + "implemented yet");
    } else if (e.getSource() == mixedSimTypeMenuItem) {
      throw new RuntimeException("Mixed drivers simulation not "
          + "implemented yet");
    } else
    */
    if (e.getSource() == startMenuItem || e.getSource() == startButton) {
      startButtonHandler();
      canvas.requestFocusInWindow();
    } else if (e.getSource() == stepMenuItem || e.getSource() == stepButton) {
      stepButtonHandler();
      canvas.requestFocusInWindow();
    } else if (e.getSource() == resetMenuItem) {
      resetSimProcess();
    } else if (e.getSource() == dumpDataMenuItem) {
      JFileChooser chooser = new JFileChooser();
      chooser.setFileSelectionMode(JFileChooser.SAVE_DIALOG);
      int returnVal = chooser.showDialog(this, "Save");
      if (returnVal == JFileChooser.APPROVE_OPTION) {
        boolean isDumpData = false;
        String outFileName = null;
        try {
          outFileName = chooser.getSelectedFile().getCanonicalPath();
          isDumpData = true;
        } catch (IOException ioe) {
          // nothing
        }
        if (isDumpData) {
          sim.getMap().printDataCollectionLinesData(outFileName);
        }
      }
    } else if (e.getSource() == startRecordingMenuItem) {
      if (!recording) {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int returnVal = chooser.showDialog(this, "Choose Directory");
        if (returnVal == JFileChooser.APPROVE_OPTION) {
          try {
            imageDir = chooser.getSelectedFile().getCanonicalPath();
            recording = true;
          } catch (IOException ioe) {
            // nothing
          }
          if (recording) {
            startRecordingMenuItem.setEnabled(false);
            stopRecordingMenuItem.setEnabled(true);
          }
        }
      }
    } else if (e.getSource() == stopRecordingMenuItem) {
      if (recording) {
        recording = false;
        imageCounter = 0;
        startRecordingMenuItem.setEnabled(true);
        stopRecordingMenuItem.setEnabled(false);
      }
    } else if (e.getSource() == startUdpListenerMenuItem) {
      startUdpListening();
    } else if (e.getSource() == stopUdpListenerMenuItem) {
      stopUdpListening();
    } else if (e.getSource() == clearDebugPointsMenuItem) {
      Debug.clearLongTermDebugPoints();
    } else if ("Quit".equals(e.getActionCommand())) {
      System.exit(0);
    } // else ignore other events
  }

  // ///////////////////////////////
  // KeyListener interface
  // ///////////////////////////////

  /**
   * {@inheritDoc}
   */
  @Override
  public void keyPressed(KeyEvent e) {
    if (simThread != null) {
      switch (e.getKeyCode()) {
      case KeyEvent.VK_ENTER:
        startButtonHandler();
        break;
      case KeyEvent.VK_SPACE:
        if (simThread.isPaused()) {
          stepButtonHandler();
        } else {
          startButtonHandler();
        }
        break;
      case KeyEvent.VK_ESCAPE:
        resetSimProcess();
        break;
      default:
      // do nothing
      }
    } // else ignore the event
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void keyReleased(KeyEvent e) {
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void keyTyped(KeyEvent e) {
  }

  // ///////////////////////////////
  // MouseListener interface
  // ///////////////////////////////

  /**
   * {@inheritDoc}
   */
  @Override
  public void mouseClicked(MouseEvent e) {
    // TODO: may be move this function to canvas.
    // right click
    if (e.getButton() == MouseEvent.BUTTON1) {
      if (sim != null) {
        Point2D leftClickPoint = canvas.getMapPosition(e.getX(), e.getY());
        // See if we hit any vehicles
        for (VehicleSimView vehicle : sim.getActiveVehicles()) {
          if (vehicle.getShape().contains(leftClickPoint)) {
            if (Debug.getTargetVIN() != vehicle.getVIN()) {
              Debug.setTargetVIN(vehicle.getVIN());
              if (vehicleInfoFrame == null) {
                vehicleInfoFrame = new VehicleInfoFrame(this);
              }
              if (!vehicleInfoFrame.isVisible()) {
                vehicleInfoFrame.setVisible(true);
                this.requestFocusInWindow();
                this.requestFocus();
              }
              vehicleInfoFrame.setVehicle(vehicle);
            } else {
              Debug.removeTargetVIN();
              vehicleInfoFrame.setVehicle(null);
            }
            canvas.update();
            return;  // just exit
          }
        }
        // see if we hit any intersection
        for (IntersectionManager im : sim.getMap().getIntersectionManagers()) {
          if (im.getIntersection().getArea().contains(leftClickPoint)) {
            if (Debug.getTargetIMid() != im.getId()) {
              Debug.setTargetIMid(im.getId());
            } else {
              Debug.removeTargetIMid();
            }
            canvas.cleanUp();  // TODO: ugly code, one more reason to move this
            // function to canvas
            canvas.update();
            return;  // just exit
          }
        }
        // hit nothing, just unselect the vehicle and intersection manager.
        Debug.removeTargetVIN();
        if (vehicleInfoFrame != null) {
          vehicleInfoFrame.setVehicle(null);
        }
        Debug.removeTargetIMid();
        canvas.cleanUp();
        canvas.update();
      }
    } else if (e.getButton() == MouseEvent.BUTTON3) {
      if (sim != null) {
        Point2D rightClickPoint = canvas.getMapPosition(e.getX(), e.getY());
        System.err.printf("Right click at (%.0f, %.0f)\n",
            rightClickPoint.getX(), rightClickPoint.getY());
        // print the lane id
        for (Road r : sim.getMap().getRoads()) {
          for (Lane l : r.getLanes()) {
            if (l.getShape().contains(rightClickPoint)) {
              System.err.printf("Right click on lane %d\n", l.getId());
            }
          }
        }
      }
    } // else ignore other event
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void mouseEntered(MouseEvent e) {
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void mouseExited(MouseEvent e) {
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void mousePressed(MouseEvent e) {
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void mouseReleased(MouseEvent e) {
  }

  // ///////////////////////////////
  // ItemListener interface
  // ///////////////////////////////

  /**
   * {@inheritDoc}
   */
  @Override
  public void itemStateChanged(ItemEvent e) {
    Object source = e.getItemSelectable();

    if (source == showSimulationTimeMenuItem) {
      if (e.getStateChange() == ItemEvent.SELECTED) {
        canvas.setIsShowSimulationTime(true);
      } else {
        canvas.setIsShowSimulationTime(false);
      }
      canvas.update();
    } else if (source == showVinMenuItem) {
      if (e.getStateChange() == ItemEvent.SELECTED) {
        canvas.setIsShowVin(true);
      } else {
        canvas.setIsShowVin(false);
      }
      canvas.update();
    } else if (source == showIMShapesMenuItem) {
      if (e.getStateChange() == ItemEvent.SELECTED) {
        canvas.setIsShowIMDebugShapes(true);
      } else {
        canvas.setIsShowIMDebugShapes(false);
      }
      canvas.update();
    }
  }

  // /////////////////////////////////////
  // UDP listening menu item handlers
  // /////////////////////////////////////

  /**
   * Start the UDP listening.
   */
  private void startUdpListening() {
    assert startUdpListenerMenuItem.isEnabled();

    if (sim != null) {
      if (Debug.SHOW_PROXY_VEHICLE_DEBUG_MSG) {
        System.err.print("Starting UDP listener...\n");
      }

      // create the UDP listener thread
      udpListener = new UdpListener(sim);
      udpListener.start();

      if (udpListener.hasStarted()) {
        startUdpListenerMenuItem.setEnabled(false);
        stopUdpListenerMenuItem.setEnabled(true);
      } else {
        System.err.printf("Failed to start UDP listener...\n");
      }
    } else {
      System.err.printf("Must start the simulator before starting "
          + "UdpListener.\n");
    }
  }

  /**
   * Stop the UDP listening
   */
  private void stopUdpListening() {
    assert stopUdpListenerMenuItem.isEnabled();

    if (Debug.SHOW_PROXY_VEHICLE_DEBUG_MSG) {
      System.err.print("Stopping UDP listener...\n");
    }

    udpListener.stop();

    if (!udpListener.hasStarted()) {
      startUdpListenerMenuItem.setEnabled(true);
      stopUdpListenerMenuItem.setEnabled(false);
    } else {
      System.err.printf("Failed to stop UDP listener...\n");
    }
    udpListener = null;
  }

  /////////////////////////////////
  // DEBUG
  /////////////////////////////////

  /**
   * {@inheritDoc}
   */
  @Override
  public void highlightVehicle(int vin) {
    canvas.highlightVehicle(vin);

  }

}
