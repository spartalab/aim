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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.TexturePaint;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import aim4.config.Debug;
import aim4.config.DebugPoint;
import aim4.driver.AutoDriver;
import aim4.driver.coordinator.V2ICoordinator;
import aim4.im.IntersectionManager;
import aim4.im.v2i.V2IManager;
import aim4.im.v2i.RequestHandler.TrafficSignalRequestHandler;
import aim4.im.v2i.policy.BasePolicy;
import aim4.im.v2i.policy.Policy;
import aim4.map.DataCollectionLine;
import aim4.map.BasicMap;
import aim4.map.Road;
import aim4.map.lane.Lane;
import aim4.map.track.ArcTrack;
import aim4.map.track.LineTrack;
import aim4.map.track.PathTrack;
import aim4.map.track.TrackPosition;
import aim4.map.track.WayPoint;
import aim4.msg.v2i.Request;
import aim4.msg.v2i.V2IMessage;
import aim4.sim.Simulator;
import aim4.util.Util;
import aim4.vehicle.AutoVehicleSimView;
import aim4.vehicle.VehicleSimView;
import java.io.InputStream;

/**
 * The Canvas is the visual area on which the Layout, IntersectionManagers,
 * Vehicles, and so forth are drawn for the user to see.
 */
public class Canvas extends JPanel implements ComponentListener,
                                              MouseListener,
                                              MouseWheelListener,
                                              MouseMotionListener {

  // ///////////////////////////////
  // CONSTANTS
  // ///////////////////////////////
  /** The serial version ID for serialization */
  private static final long serialVersionUID = 1L;
  /**
   * An AffineTransform that does nothing. This is used mainly for when text
   * must be drawn to the Canvas, but we do not want it distorted by the usual
   * AffineTransform, which transforms between Simulator space and Graphics
   * coordinates.
   */
  private static final AffineTransform IDENTITY_TRANSFORM =
      new AffineTransform();
  /**
   * The factor on the zooming scale for each notch
   */
  private static final double SCALE_FACTOR = 1.10;
  /**
   * The maximum number of zooming in steps
   */
  private static final int ZOOM_IN_SCALE_NUM = 10;
  /**
   * The maximum number of zooming out steps
   */
  private static final int ZOOM_OUT_SCALE_NUM = 25;
  /**
   * The maximum number of zooming steps
   */
  private static final int SCALE_NUM =
      ZOOM_IN_SCALE_NUM + ZOOM_OUT_SCALE_NUM + 1;
  /**
   * The margin of the view that must stay on screen.
   */
  private static final int VIEW_MARGIN = 50;
  // Drawing elements for background
  /** The file name of the file containing the image to use for grass. */
  private static final String GRASS_TILE_FILE = "/images/grass128.png";
  /** The file name of the file containing the image to use for asphalt. */
  private static final String ASPHALT_TILE_FILE = "/images/asphalt32.png";
  /** The color of the grass, if the image does not load properly. */
  public static final Color GRASS_COLOR = Color.GREEN.darker().darker();
  /** The color of the asphalt, if the image does not load properly. */
  public static final Color ASPHALT_COLOR = Color.BLACK.brighter();
  /** The color of the background */
  public static final Color BACKGROUND_COLOR = Color.gray;
  // Drawing elements for vehicle
  /** The stroke used of vehicles. */
  private static final Stroke VEHICLE_STROKE = new BasicStroke(0.1f);
  /** The color of ordinary Vehicles. */
  private static final Color VEHICLE_COLOR = Color.YELLOW;
  /** The color of vehicles that have reservations. */
  private static final Color VEHICLE_HAS_RESERVATION_COLOR = Color.WHITE;
  /** The color of vehicles that are waiting for a response */
  private static final Color VEHICLE_WAITING_FOR_RESPONSE_COLOR =
      Color.blue.brighter().brighter().brighter();
  /** MARVIN's coloring */
  private static final int MARVIN_VEHICLE_VIN = 42;
  /** MARVIN's color */
  private static final Color MARVIN_VEHICLE_COLOR = Color.RED;
  /** The colors that emergency Vehicles cycle through. */
  // private static final Color[] EMERGENCY_VEHICLE_COLORS =
  //  { Color.RED, Color.BLUE };
  /**
   * The period, in seconds, that emergency Vehicles take to cycle through
   * each of their Colors. {@value} seconds.
   */
  // private static final double EMERGENCY_VEHICLE_COLOR_PERIOD = 0.5; // sec
  /** The color of vehicles that have been clicked on by the user */
  private static final Color VEHICLE_SELECTED_COLOR = Color.ORANGE;
  /** The color of vehicle's tires. */
  private static final Color TIRE_COLOR = Color.BLACK;
  /** The tire color */
  private static final Stroke TIRE_STROKE = new BasicStroke(0.1f);
  /** The vehicle information string color */
  private static final Color VEHICLE_INFO_STRING_COLOR = Color.RED;
  /** The vehicle information string font */
  private static final Font VEHICLE_INFO_STRING_FONT =
      new Font("Monospaced", Font.PLAIN, 5);
  // Drawing elements for intersections
  /**
   * The color with which to draw the outline of an intersection that uses a
   * V2I system to manage traffic.
   */
  private static final Color IM_OUTLINE_COLOR = Color.CYAN;
  /** Selected IM's outline color */
  private static final Color SELECTED_IM_OUTLINE_COLOR = Color.ORANGE;
  /** IM's stroke */
  private static final Stroke IM_OUTLINE_STROKE = new BasicStroke(0.3f);
  // Drawing elements for lanes
  /** The color of the road boundary */
  private static final Color ROAD_BOUNDARY_COLOR = Color.YELLOW;
  /** The stroke of the road boundary */
  private static final Stroke ROAD_BOUNDARY_STROKE = new BasicStroke(0.3f);
  // Drawing elements for lane separators
  /**
   * The color with which to draw lines separating traffic traveling in the
   * same direction on the road.
   */
  private static final Color LANE_SEPARATOR_COLOR = Color.WHITE;
  /**
   * The stroke used to draw the broken white lines separating the lanes in
   * the same direction on the road.
   */
  private static final Stroke LANE_SEPARATOR_STROKE =
      new BasicStroke(.3f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0,
      new float[]{1, 3}, 0);
  // Drawing elements for data collection lines
  /** The color of the data collection lines */
  private static final Color DCL_COLOR = Color.WHITE;
  /** The stroke of the data collection lines */
  private static final Stroke DCL_STROKE = new BasicStroke(0.3f);
  // Drawing elements for traffic lights
  /**
   * The radius, in meters, of the sectors used to visualize the state of
   * traffic lights. {@value} meters.
   */
  private static final double TRAFFIC_LIGHT_RADIUS = 2; // meter
  // IM debug shapes
  /** The color of IM debug shapes */
  private static final Color IM_DEBUG_SHAPE_COLOR = Color.CYAN;
  // simulation time
  /** Simulation time's string color */
  private static final Color SIMULATION_TIME_STRING_COLOR = Color.YELLOW;
  /** Simulation time's font */
  private static final Font SIMULATION_TIME_STRING_FONT =
      new Font("Monospaced", Font.PLAIN, 18);
  /** Simulation time location X */
  private static final int SIMULATION_TIME_LOCATION_X = 12;
  /** Simulation time location Y */
  private static final int SIMULATION_TIME_LOCATION_Y = 24;
  // the highlighted vehicle
  /** The color of the highlighted vehicle */
  private static final Color HIGHLIGHTED_VEHICLE_COLOR = Color.GREEN;
  /** The stroke of the highlighted vehicle */
  private static final Stroke HIGHLIGHTED_VEHICLE_STROKE =
      new BasicStroke(0.3f);
  // Debug points
  /** Debut point stroke */
  private static final Stroke DEBUG_POINT_STROKE = new BasicStroke(0.3f);
  /** Debut point font */
  private static final Font DEBUG_POINT_FONT =
      new Font("Monospaced", Font.PLAIN, 5);
  /**
   * The radius, in meters, of the circles used to display DriverAgents'
   * DebugPoints. {@value} meters.
   */
  private static final double DEBUG_POINT_RADIUS = 0.5;
  // Tracks
  /** The color of the track */
  private static final Color TRACK_COLOR = Color.RED;
  /** The stroke of the track */
  private static final Stroke TRACK_STROKE = new BasicStroke(0.3f);
  /////////////////////////////////
  // PRIVATE FIELDS
  /////////////////////////////////
  /** The map */
  private BasicMap basicMap;
  /** The position of the x-coordinate of the origin in the sim space */
  private int posOfOriginX;
  /** The position of the y-coordinate of the origin in the sim space */
  private int posOfOriginY;
  /** The current scale index */
  private int scaleIndex;
  /** The scale of the map on screen */
  private double[] scaleTable;
  /** The last position of the x-coordinate of the cursor */
  private int lastCursorX;
  /** The last position of the y-coordinate of the cursor */
  private int lastCursorY;
  /** The image for grass texture. */
  private BufferedImage grassImage;
  /** The image for asphalt texture. */
  private BufferedImage asphaltImage;
  /**
   * A cache of the background so that we do not need to redraw it every time
   * a vehicle moves.
   */
  private Image[] mapImageTable;
  /**
   * The buffer for the image that will be drawn to the canvas whenever it is
   * repainted.
   */
  private Image displayImage;
  /**
   * The graphic context in which we will use to draw to the displayImage.
   */
  private Graphics2D displayBuffer;
  /**
   * the viewer
   */
  private Viewer viewer;
  /**
   * Whether other threads can update the canvas via update()
   */
  private boolean canUpdateCanvas;
  /**
   * Whether to show the simulation time on canvas
   */
  private boolean isShowSimulationTime;
  /**
   * Whether to show the VIN numbers
   */
  private boolean isShowVin;
  /**
   * Whether or not the Canvas will try to draw the IntersectionManagers'
   * debugging shapes.
   */
  private boolean isShowIMDebugShapes;

  /////////////////////////////////
  // CLASS CONSTRUCTORS
  /////////////////////////////////
  /**
   * Create a new canvas.
   *
   * @param viewer the viewer object
   */
  public Canvas(Viewer viewer) {
    this.viewer = viewer;

    basicMap = null;

    posOfOriginX = 0;
    posOfOriginY = 0;
    scaleIndex = 0;
    scaleTable = null;

    lastCursorX = -1;
    lastCursorY = -1;

    grassImage = loadImage(GRASS_TILE_FILE);
    if (grassImage == null) {
      System.err.println("Could not load image from file: " + GRASS_TILE_FILE);
    }
    asphaltImage = loadImage(ASPHALT_TILE_FILE);
    if (asphaltImage == null) {
      System.err.println("Could not load image from file: " + ASPHALT_TILE_FILE);
    }

    mapImageTable = null;
    displayImage = null;
    displayBuffer = null;

    canUpdateCanvas = false;

    isShowSimulationTime = Viewer.IS_SHOW_SIMULATION_TIME;
    isShowVin = Viewer.IS_SHOW_VIN_BY_DEFAULT;
    isShowIMDebugShapes = Viewer.IS_SHOW_IM_DEBUG_SHAPES_BY_DEFAULT;

    addMouseListener(viewer);
    addKeyListener(viewer);
    addComponentListener(this);
    addMouseListener(this);
    addMouseWheelListener(this);
    addMouseMotionListener(this);
  }

  /**
   * Load an image from an image file.
   *
   * @param imageFileName  the path to the image file
   * @return the image object
   */
  private BufferedImage loadImage(String imageFileName) {
    InputStream is = this.getClass().getResourceAsStream(imageFileName);
    BufferedImage image = null;
    if (is != null) {
      try {
        image = ImageIO.read(is);
      } catch (IOException e) {
        image = null;
      }
    }
    return image;
  }

  /**
   * Initialize the canvas with a given map.
   *
   * @param basicMap  the layout the canvas will be visualizing
   */
  public void initWithGivenMap(BasicMap basicMap) {
    this.basicMap = basicMap;

    posOfOriginX = 0;
    posOfOriginY = 0;
    setupScale();

    lastCursorX = -1;
    lastCursorY = -1;

    // create the displayBuffer
    makeDisplayBuffer();

    // Set up the affine transform so we draw in the right coordinate space
    // resetAffineTransform();

    // create the background
    mapImageTable = new Image[SCALE_NUM];
    for (int i = 0; i < SCALE_NUM; i++) {
      mapImageTable[i] = null;
    }
    // create the map image for the initial scale
    mapImageTable[scaleIndex] =
        createMapImage(basicMap, scaleTable[scaleIndex]);

    canUpdateCanvas = true;
  }

  /**
   * Create the display buffer
   */
  private void makeDisplayBuffer() {
    displayImage = createImage(getWidth(), getHeight());
    displayBuffer = (Graphics2D) displayImage.getGraphics();
    displayBuffer.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
        RenderingHints.VALUE_ANTIALIAS_ON);
  }

  /**
   * Paint the entire buffer with the given color
   *
   * @param buffer the display buffer
   * @param color  the color
   */
  private void paintEntireBuffer(Graphics2D buffer, Color color) {
    AffineTransform tf = buffer.getTransform();
    // set the transform
    buffer.setTransform(IDENTITY_TRANSFORM);
    // paint
    buffer.setPaint(color); // no need to set the stroke
    buffer.fillRect(0, 0, getSize().width, getSize().height);
    // Restore the original transform.
    buffer.setTransform(tf);
  }

  /**
   * Reset the affine transform.
   */
  private void resetAffineTransform() {
    AffineTransform tf = new AffineTransform();
    tf.translate(posOfOriginX, posOfOriginY);
    tf.scale(scaleTable[scaleIndex], scaleTable[scaleIndex]);
    displayBuffer.setTransform(tf);
  }

  /**
   * Move the origin to stay within view.
   */
  private void moveOriginToStayWithinView() {
    Rectangle2D r = basicMap.getDimensions();
    if (getWidth() >= VIEW_MARGIN) {
      if (posOfOriginX > getWidth() - VIEW_MARGIN) {
        posOfOriginX = getWidth() - VIEW_MARGIN;
      } else {
        int w = (int) (r.getWidth() * scaleTable[scaleIndex]);
        if (posOfOriginX + w < VIEW_MARGIN) {
          posOfOriginX = VIEW_MARGIN - w;
        }
      }
    }  // else just do nothing
    if (getHeight() >= VIEW_MARGIN) {
      if (posOfOriginY > getHeight() - VIEW_MARGIN) {
        posOfOriginY = getHeight() - VIEW_MARGIN;
      } else {
        int w = (int) (r.getHeight() * scaleTable[scaleIndex]);
        if (posOfOriginY + w < VIEW_MARGIN) {
          posOfOriginY = VIEW_MARGIN - w;
        }
      }
    }  // else just do nothing
  }

  /**
   * Setup the scale.
   */
  private void setupScale() {
    Rectangle2D mapRect = basicMap.getDimensions();
    // the initial scale
    scaleTable = new double[SCALE_NUM];
    scaleIndex = ZOOM_IN_SCALE_NUM;
    // calculate the scale at the middle (current scale)
    scaleTable[scaleIndex] = Math.min(getWidth() / mapRect.getWidth(),
        getHeight() / mapRect.getHeight());
    for (int i = scaleIndex - 1; i >= 0; i--) {
      scaleTable[i] = scaleTable[i + 1] * SCALE_FACTOR;
    }
    for (int i = scaleIndex + 1; i < SCALE_NUM; i++) {
      scaleTable[i] = scaleTable[i - 1] / SCALE_FACTOR;
    }
  }

  /**
   * Get the map image at a given scale index.  If the map image does not
   * exist in the cache, create it.
   *
   * @param scaleIndex  the scale index
   * @return the map image at the given scale index
   */
  private Image getMapImageTable(int scaleIndex) {
    if (mapImageTable[scaleIndex] == null) {
      mapImageTable[scaleIndex] =
          createMapImage(basicMap, scaleTable[scaleIndex]);
    }
    return mapImageTable[scaleIndex];
  }

  /////////////////////////////////
  // PRIVATE METHODS
  /////////////////////////////////
  /**
   * Create a background image for the given Layout.
   *
   * @param map  the Layout for which to create a background image
   *        s    the scale of the map
   */
  private Image createMapImage(BasicMap map, double scale) {
    Rectangle2D mapRect = basicMap.getDimensions();
    // First, set up an image buffer
    Image bgImage = createImage((int) (mapRect.getWidth() * scale),
        (int) (mapRect.getHeight() * scale));
    Graphics2D bgBuffer = (Graphics2D) bgImage.getGraphics();
    // Set the transform
    AffineTransform tf = new AffineTransform();
    tf.translate(0, 0);
    tf.scale(scale, scale);
    bgBuffer.setTransform(tf);
    // create the textures that depends on the scale
    TexturePaint grassTexture = makeScaledTexture(grassImage, scale);
    TexturePaint asphaltTexture = makeScaledTexture(asphaltImage, scale);
    // paint the background with the red color in order to
    // show that no space in the buffer is not redrawn.
    paintEntireBuffer(bgBuffer, Color.RED);
    // draw the grass on the map only
    drawGrass(bgBuffer, mapRect, grassTexture);
    // Next, loop through the roads and draw them all
    for (Road road : map.getRoads()) {
      drawRoad(bgBuffer, road, asphaltTexture);
    }
    // Draw all the intersections
    for (IntersectionManager im : map.getIntersectionManagers()) {
      drawIntersectionManager(bgBuffer, im, asphaltTexture);
    }
    // Then draw the data collection lines
    drawDataCollectionLines(bgBuffer, map.getDataCollectionLines());

    return bgImage;
  }

  /**
   * Create a scaled image.
   *
   * @param image  the image object
   * @param scale  the scaling factor
   * @return the new image
   */
  private TexturePaint makeScaledTexture(BufferedImage image, double scale) {
    if (image != null) {
      // Make sure to scale it properly so it doesn't get all distorted
      Rectangle2D textureRect =
          new Rectangle2D.Double(0, 0,
          image.getWidth() / scale,
          image.getHeight() / scale);
      // Now set up an easy-to-refer-to texture.
      return new TexturePaint(image, textureRect);
    } else {
      return null;
    }
  }

  /**
   * Paint the rectangle with the grass picture.
   *
   * @param buffer        the image buffer
   * @param rect          the rectangle
   * @param grassTexture  the grass texture
   */
  private void drawGrass(Graphics2D buffer,
                         Rectangle2D rect,
                         TexturePaint grassTexture) {
    // draw the grass everywhere
    if (grassTexture == null) {
      buffer.setPaint(GRASS_COLOR); // no need to set the stroke
    } else {
      buffer.setPaint(grassTexture); // no need to set the stroke
    }
    buffer.fill(rect);
  }

  /**
   * Draw a road on the display buffer.
   *
   * @param bgBuffer        the display buffer
   * @param road            the road
   * @param asphaltTexture  the grass texture
   */
  private void drawRoad(Graphics2D bgBuffer, Road road,
                        TexturePaint asphaltTexture) {
    for (Lane lane : road.getLanes()) {
      drawLane(bgBuffer, lane, asphaltTexture);
    }
  }

  /**
   * Draw a lane on the display buffer.
   *
   * @param bgBuffer        the display buffer
   * @param lane            the lane
   * @param asphaltTexture  the asphalt texture
   */
  private void drawLane(Graphics2D bgBuffer,
                        Lane lane,
                        TexturePaint asphaltTexture) {
    // Draw the lane itself
    if (asphaltTexture == null) {
      bgBuffer.setPaint(ASPHALT_COLOR);
    } else {
      bgBuffer.setPaint(asphaltTexture);
    }
    bgBuffer.fill(lane.getShape());
    // Draw the left boundary
    if (lane.hasLeftNeighbor()) {
      bgBuffer.setPaint(LANE_SEPARATOR_COLOR);
      bgBuffer.setStroke(LANE_SEPARATOR_STROKE);
    } else {
      bgBuffer.setPaint(ROAD_BOUNDARY_COLOR);
      bgBuffer.setStroke(ROAD_BOUNDARY_STROKE);
    }
    bgBuffer.draw(lane.leftBorder());
    // Draw the right boundary
    if (lane.hasRightNeighbor()) {
      bgBuffer.setPaint(LANE_SEPARATOR_COLOR);
      bgBuffer.setStroke(LANE_SEPARATOR_STROKE);
    } else {
      bgBuffer.setPaint(ROAD_BOUNDARY_COLOR);
      bgBuffer.setStroke(ROAD_BOUNDARY_STROKE);
    }
    bgBuffer.draw(lane.rightBorder());
  }

  /**
   * Draw an intersection on the display buffer.
   *
   * @param bgBuffer        the display buffer
   * @param im              the intersection manager
   * @param asphaltTexture  the asphaltTexture
   */
  private void drawIntersectionManager(Graphics2D bgBuffer,
                                       IntersectionManager im,
                                       TexturePaint asphaltTexture) {

    boolean selected = (Debug.getTargetIMid() == im.getId());

    // First, fill in the intersection with asphalt color/texture
    if (asphaltTexture == null) {
      bgBuffer.setPaint(ASPHALT_COLOR);
    } else {
      bgBuffer.setPaint(asphaltTexture);
    }
    bgBuffer.fill(im.getIntersection().getArea());
    // Then, outline it with the appropriate color
    if (im instanceof V2IManager) {
      if (selected) {
        bgBuffer.setPaint(SELECTED_IM_OUTLINE_COLOR);
      } else {
        bgBuffer.setPaint(IM_OUTLINE_COLOR);
      }
      bgBuffer.setStroke(IM_OUTLINE_STROKE);
      bgBuffer.draw(im.getIntersection().getArea());
    }

  }

  /**
   * Draw the data collection lines.
   *
   * @param bgBuffer             the display buffer
   * @param dataCollectionLines  the list of data collection lines
   */
  private void drawDataCollectionLines(Graphics2D bgBuffer,
                                       List<DataCollectionLine> dataCollectionLines) {
    for (DataCollectionLine line : dataCollectionLines) {
      bgBuffer.setPaint(DCL_COLOR);
      bgBuffer.setStroke(DCL_STROKE);
      bgBuffer.draw(line.getShape());
    }
  }

  /////////////////////////////////
  // PUBLIC METHODS
  /////////////////////////////////
  /**
   * Clean up the canvas
   */
  public synchronized void cleanUp() {
    paintEntireBuffer(displayBuffer, BACKGROUND_COLOR);
    for (int i = 0; i < SCALE_NUM; i++) {
      mapImageTable[i] = null;
    }
    repaint();
  }

  /**
   * Update the canvas to visualize the current state of simulation.
   */
  public void update() {
    if (canUpdateCanvas) {
      // TODO: think how to avoid multiple calls of update() are queued here
      // by synchronized when the the canvas is resized, rescaled, or
      // repositioned.
      updateCanvas();
    }
  }

  /**
   * Update the canvas to visualize the current state of simulation.
   */
  private synchronized void updateCanvas() {
    // reset the affine transform
    resetAffineTransform();
    // Clear the screen
    paintEntireBuffer(displayBuffer, BACKGROUND_COLOR);
    // draw the map
    drawImageOnBuffer(displayBuffer, getMapImageTable(scaleIndex));
    // Get the simulator
    Simulator sim = viewer.getSimulator();
    // if the simulator exists, draw the current view
    if (sim != null) {
      Collection<IntersectionManager> ims =
          sim.getMap().getIntersectionManagers();
      // draw the intersection managers' debug shapes
      if (isShowIMDebugShapes) {
        for (IntersectionManager im : ims) {
          drawIMDebugShapes(displayBuffer, im);
        }
      }
      // draw the vehicles
      for (VehicleSimView v : sim.getActiveVehicles()) {
        drawVehicle(displayBuffer, v, sim.getSimulationTime());
      }
      // draw the traffic lights
      for (IntersectionManager im : ims) {
        drawTrafficLights(displayBuffer, im);
      }
      // draw simulation time.
      if (isShowSimulationTime) {
        drawSimulationTime(displayBuffer, sim.getSimulationTime());
      }
      // draw the debug points
      drawDebugPoints(displayBuffer, Debug.getLongTermDebugPoints());
      drawDebugPoints(displayBuffer, Debug.getShortTermDebugPoints());
      // draw tracks
      // drawTracks(displayBuffer);
      // lastly, draw the vehicles' information string
      for (VehicleSimView v : sim.getActiveVehicles()) {
        drawVehicleInfoString(displayBuffer, v, sim.getSimulationTime());
      }
      // Finally display the new image
      repaint();
    } // else no simulator no drawing
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public synchronized void paint(Graphics g) {
    if (displayImage != null) {
      g.drawImage(displayImage, 0, 0, this);
    }
  }

  /////////////////////////////////
  // PRIVATE METHODS
  /////////////////////////////////
  /**
   * Draw an image on screen.
   *
   * @param buffer  the display buffer
   * @param image   the image
   */
  private void drawImageOnBuffer(Graphics2D buffer, Image image) {
    // Save the current transform so we can restore it.
    AffineTransform tf = buffer.getTransform();
    // set the identity transform
    buffer.setTransform(IDENTITY_TRANSFORM);
    // Copy the background image over to the display image
    buffer.drawImage(image, posOfOriginX, posOfOriginY, null);
    // Restore the original transform.
    buffer.setTransform(tf);
  }

  /**
   * Draw an individual Vehicle, and any associated debug information, if this
   * Vehicle is a debug Vehicle.
   *
   * @param buffer       the display buffer
   * @param vehicle      the Vehicle to draw now
   * @param currentTime  the current simulated time
   */
  private void drawVehicle(Graphics2D buffer,
                           VehicleSimView vehicle,
                           double currentTime) {
    // whether the vehicle is selected
    boolean selectedVehicle = (Debug.getTargetVIN() == vehicle.getVIN());
    // check to see if we use another color
    if (selectedVehicle) {
      buffer.setPaint(VEHICLE_SELECTED_COLOR);
    } else if (vehicle.getVIN() == MARVIN_VEHICLE_VIN) {
      buffer.setPaint(MARVIN_VEHICLE_COLOR);
    } else if (Debug.getVehicleColor(vehicle.getVIN()) != null) {
      buffer.setPaint(Debug.getVehicleColor(vehicle.getVIN()));
    } else if (Debug.SHOW_VEHICLE_COLOR_BY_MSG_STATE) {
      if (vehicle.getDriver() instanceof AutoDriver) {
        AutoDriver autoDriver = (AutoDriver) vehicle.getDriver();
        if (autoDriver.getCurrentCoordinator() instanceof V2ICoordinator) {
          V2ICoordinator coordinator =
              (V2ICoordinator) autoDriver.getCurrentCoordinator();
          if (coordinator.isAwaitingResponse()) {
            buffer.setPaint(VEHICLE_WAITING_FOR_RESPONSE_COLOR);
          } else if (coordinator.getReservationParameter() != null) {
            buffer.setPaint(VEHICLE_HAS_RESERVATION_COLOR);
          } else {
            buffer.setPaint(VEHICLE_COLOR);  // the default color
          }
        } else {
          buffer.setPaint(VEHICLE_COLOR);  // the default color
        }
      } else {
        buffer.setPaint(VEHICLE_COLOR);  // the default color
      }
    } else {
      buffer.setPaint(VEHICLE_COLOR);  // the default color
    }

    buffer.setStroke(VEHICLE_STROKE);

    // Now draw the vehicle's shape
    buffer.fill(vehicle.getShape());
    // Draw wheels and stuff if needed
    if (selectedVehicle) {
      buffer.setPaint(TIRE_COLOR);
      buffer.setStroke(TIRE_STROKE);
      for (Shape wheel : vehicle.getWheelShapes()) {
        buffer.fill(wheel);
      }
    }
  }

  /**
   * Draw the information string of the vehicle on screen
   *
   * @param buffer       the display buffer
   * @param vehicle      the vehicle
   * @param currentTime  the current simulated time
   */
  private void drawVehicleInfoString(Graphics2D buffer,
                                     VehicleSimView vehicle,
                                     double currentTime) {
    List<String> infos = new LinkedList<String>();

    // display the vin
    if (isShowVin) {
      infos.add(Integer.toString(vehicle.getVIN()));
    }

    if (vehicle instanceof AutoVehicleSimView
        && vehicle.getDriver() instanceof AutoDriver) {
      AutoDriver da = (AutoDriver) vehicle.getDriver();
      if (da.getCurrentCoordinator() instanceof V2ICoordinator) {
        V2ICoordinator coordinator =
            (V2ICoordinator) da.getCurrentCoordinator();

        // display the arrival time of the request (if any)
        if (Debug.SHOW_ARRIVAL_TIME) {
          if (coordinator.isAwaitingResponse()
              || coordinator.getReservationParameter() != null) {
            V2IMessage msg =
                ((AutoVehicleSimView) vehicle).getLastV2IMessage();
            if (msg instanceof Request) {
              Request request = (Request) msg;
              if (request.getProposals().size() > 0) {
                // one arrival time is enough.
                double arrival_time =
                    request.getProposals().get(0).getArrivalTime();
                infos.add(String.format("%.2f", arrival_time));
              } else {
                infos.add("No Proposals");
              }
            } // else ignore other types of messages
          }
        }

        if (Debug.SHOW_REMAINING_ARRIVAL_TIME) {
          if (coordinator.isAwaitingResponse()
              || coordinator.getReservationParameter() != null) {
            V2IMessage msg =
                ((AutoVehicleSimView) vehicle).getLastV2IMessage();
            if (msg instanceof Request) {
              Request request = (Request) msg;
              if (request.getProposals().size() > 0) {
                // one arrival time is enough.
                double arrival_time =
                    request.getProposals().get(0).getArrivalTime();
                if (coordinator.getReservationParameter() == null
                    || arrival_time - currentTime >= 0) {
                  infos.add(String.format("%.2f", arrival_time - currentTime));
                }
              } else {
                infos.add("No Proposals");
              }
            } // else ignore other types of messages
          }
        }
      }
    }

    if (infos.size() > 0) {
      Point2D centerPoint = vehicle.getCenterPoint();
      buffer.setColor(VEHICLE_INFO_STRING_COLOR);
      buffer.setFont(VEHICLE_INFO_STRING_FONT);
      buffer.drawString(Util.concatenate(infos, ","),
          (float) centerPoint.getX(),
          (float) centerPoint.getY());
    }
  }

  /**
   * Draw the current state of the lights for all IntersectionManagers.
   *
   * @param buffer  the display buffer
   * @param im      the intersection manager whose traffic lights to draw
   */
  private void drawTrafficLights(Graphics2D buffer, IntersectionManager im) {
    if (im instanceof V2IManager) {
      Policy policy = ((V2IManager) im).getPolicy();
      if (policy instanceof BasePolicy) {
        BasePolicy basePolicy = (BasePolicy) policy;
        if (basePolicy.getRequestHandler() instanceof TrafficSignalRequestHandler) {
          TrafficSignalRequestHandler requestHandler =
              (TrafficSignalRequestHandler) basePolicy.getRequestHandler();
          for (Lane entryLane : im.getIntersection().getEntryLanes()) {
            switch (requestHandler.getSignal(entryLane.getId())) {
            case GREEN:
              buffer.setPaint(Color.GREEN);
              break;
            case YELLOW:
              buffer.setPaint(Color.YELLOW);
              break;
            case RED:
              buffer.setPaint(Color.RED);
              break;
            default:
              throw new RuntimeException("Unknown traffic signals.\n");
            }
            // Now create the shape we will use to draw the light
            // For some reason, Java's angles increase to the right instead of
            // to the left
            // TODO: cache it
            Arc2D lightShape =
                new Arc2D.Double(im.getIntersection().getEntryPoint(entryLane).getX()
                - TRAFFIC_LIGHT_RADIUS, // x
                im.getIntersection().getEntryPoint(entryLane).getY()
                - TRAFFIC_LIGHT_RADIUS, // y
                TRAFFIC_LIGHT_RADIUS * 2, // width
                TRAFFIC_LIGHT_RADIUS * 2, // height
                90 - // start
                Math.toDegrees(im.getIntersection().getEntryHeading(entryLane)), 180.0, // extent
                Arc2D.PIE); // type
            // Now draw it!
            buffer.fill(lightShape);
          }
        }
      }
    }
  }

  /**
   * Draw the debugging shapes that the IntersectionManagers provide. These
   * are usually things like used tiles for a tile-based reservation policy,
   * current heuristic values and so forth.
   *
   * @param buffer  the display buffer
   * @param im      the intersection manager whose debug shapes to draw
   */
  private void drawIMDebugShapes(Graphics2D buffer, IntersectionManager im) {
    for (Shape s : im.getDebugShapes()) {
      buffer.setPaint(IM_DEBUG_SHAPE_COLOR);
      buffer.fill(s);
    }
  }

  /**
   * Draw the simulation time.
   *
   * @param buffer       the display buffer
   * @param currentTime  the time
   */
  private void drawSimulationTime(Graphics2D buffer, double currentTime) {
    // Save the current transform so we can restore it.
    AffineTransform tf = buffer.getTransform();
    // Set the identity transform
    buffer.setTransform(IDENTITY_TRANSFORM);
    // Draw the time
    buffer.setColor(SIMULATION_TIME_STRING_COLOR);
    buffer.setFont(SIMULATION_TIME_STRING_FONT);
    buffer.drawString(String.format("%.2fs", currentTime),
        SIMULATION_TIME_LOCATION_X,
        SIMULATION_TIME_LOCATION_Y);
    // Restore the original transform.
    buffer.setTransform(tf);
  }

  /**
   * Draw a series of debug points.
   *
   * @param buffer       the display buffer
   * @param debugPoints  a set of debug points
   */
  private void drawDebugPoints(Graphics2D buffer,
                               List<DebugPoint> debugPoints) {
    for (DebugPoint p : debugPoints) {
      drawDebugPoint(buffer, p);
    }
  }

  /**
   * Draw a debug point.
   *
   * @param buffer  the display buffer
   * @param p       a debug point
   */
  private void drawDebugPoint(Graphics2D buffer, DebugPoint p) {
    if (p.getPoint() != null) {
      buffer.setPaint(p.getColor());
      buffer.setStroke(DEBUG_POINT_STROKE);
      // If there's supposed to be a start point, draw a line from it
      // to the point
      if (p.hasStartPoint()) {
        buffer.draw(new Line2D.Double(p.getStartPoint(), p.getPoint()));
      }
      // Always draw the point
      buffer.fill(new Ellipse2D.Double(
          p.getPoint().getX() - DEBUG_POINT_RADIUS,
          p.getPoint().getY() - DEBUG_POINT_RADIUS,
          DEBUG_POINT_RADIUS * 2,
          DEBUG_POINT_RADIUS * 2));
      // We need to change the transform here so our text winds up facing
      // the right way and at the right size
      if (p.hasText()) {
        buffer.setFont(DEBUG_POINT_FONT);
        buffer.drawString(
            p.getText(),
            (float) (p.getPoint().getX() - DEBUG_POINT_RADIUS),
            (float) (p.getPoint().getY() - DEBUG_POINT_RADIUS));
      }
    } // else skip the debug point
  }

  /**
   * Draw the tracks.
   *
   * @param buffer  the display buffer
   */
  private void drawTracks(Graphics2D buffer) {
    PathTrack track = new PathTrack();

    track.add(new ArcTrack(new WayPoint(50, 100),
        new WayPoint(100, 50),
        new WayPoint(100, 100), true));
    track.add(new LineTrack(new WayPoint(100, 50),
        new WayPoint(200, 50)));
    track.add(new ArcTrack(new WayPoint(200, 50),
        new WayPoint(250, 100),
        new WayPoint(200, 100), false));


    buffer.setPaint(TRACK_COLOR);
    buffer.setStroke(TRACK_STROKE);
    buffer.draw(track.getShape());

    TrackPosition pos = track.new Position(0);
    buffer.setPaint(Color.MAGENTA);
    do {
      double x = pos.getX();
      double y = pos.getY();
      double slope = pos.getTangentSlope();
      double d = 30.0;
      double x2 = x + d * Math.cos(slope);
      double y2 = y + d * Math.sin(slope);
      buffer.draw(new Line2D.Double(x, y, x2, y2));

      double r = 2.0;
      buffer.draw(new Ellipse2D.Double(x - r, y - r, 2 * r, 2 * r));
    } while (pos.move(10.0) == 0);
  }

  /////////////////////////////////
  // PUBLIC METHODS
  /////////////////////////////////

  // component listener

  /**
   * {@inheritDoc}
   */
  @Override
  public void componentHidden(ComponentEvent e) {
    // do nothing
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void componentMoved(ComponentEvent e) {
    // do nothing
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public synchronized void componentResized(ComponentEvent e) {
    canUpdateCanvas = false;
    makeDisplayBuffer();
    if (basicMap != null) {
      moveOriginToStayWithinView();
      updateCanvas();
      canUpdateCanvas = true;
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void componentShown(ComponentEvent e) {
    // do nothing
  }

  /////////////////////////////////
  // PUBLIC METHODS
  /////////////////////////////////

  // mouse listener

  /**
   * {@inheritDoc}
   */
  @Override
  public void mouseClicked(MouseEvent e) {
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
    lastCursorX = e.getX();
    lastCursorY = e.getY();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void mouseReleased(MouseEvent e) {
    lastCursorX = -1;
    lastCursorY = -1;
  }

  /////////////////////////////////
  // PUBLIC METHODS
  /////////////////////////////////

  // mouse wheel listener

  /**
   * {@inheritDoc}
   */
  @Override
  public void mouseWheelMoved(MouseWheelEvent e) {
    if (canUpdateCanvas) {
      synchronized (this) {
        canUpdateCanvas = false;
        // First, save the old position at the user space
        Point2D p = getMapPosition(e.getX(), e.getY());
        // Second, update the scale
        int notches = e.getWheelRotation();
        scaleIndex += notches;
        if (scaleIndex < 0) {
          scaleIndex = 0;
        }
        if (scaleIndex >= SCALE_NUM) {
          scaleIndex = SCALE_NUM - 1;
        }
        // Third, recalculate the origin's screen location
        posOfOriginX = (int) (e.getX() - scaleTable[scaleIndex] * p.getX());
        posOfOriginY = (int) (e.getY() - scaleTable[scaleIndex] * p.getY());
        moveOriginToStayWithinView();
        updateCanvas();
        canUpdateCanvas = true;
      }
    }
  }

  /////////////////////////////////
  // PUBLIC METHODS
  /////////////////////////////////

  // mouse drag listener

  /**
   * {@inheritDoc}
   */
  @Override
  public synchronized void mouseDragged(MouseEvent e) {
    if (SwingUtilities.isLeftMouseButton(e)
        && lastCursorX >= 0 && lastCursorY >= 0) {
      canUpdateCanvas = false;
      posOfOriginX += e.getX() - lastCursorX;
      posOfOriginY += e.getY() - lastCursorY;
      moveOriginToStayWithinView();
      updateCanvas();
      canUpdateCanvas = true;
    }
    lastCursorX = e.getX();
    lastCursorY = e.getY();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public synchronized void mouseMoved(MouseEvent e) {
  }

  /////////////////////////////////
  // PUBLIC METHODS
  /////////////////////////////////

  /**
   * Set whether to show the simulation time.
   *
   * @param b whether to show the simulation time
   */
  public void setIsShowSimulationTime(boolean b) {
    isShowSimulationTime = b;
  }

  /**
   * Set whether to show the VIN numbers.
   *
   * @param b whether to show the VIN numbers
   */
  public void setIsShowVin(boolean b) {
    isShowVin = b;
  }

  /**
   * Set whether or not the canvas draws the IM shapes.
   *
   * @param useIMDebugShapes  whether or not the canvas should draw the shapes
   */
  public void setIsShowIMDebugShapes(boolean useIMDebugShapes) {
    this.isShowIMDebugShapes = useIMDebugShapes;
  }

  /**
   * Save the screen to a file in PNG format.
   *
   * @param outFileName  the output file name
   */
  public void saveScreenShot(String outFileName) {
    File outfile = new File(outFileName);
    try {
      if (!ImageIO.write((BufferedImage) displayImage, "png", outfile)) {
        System.err.printf("Error in Canvas::saveScreenShot(): "
            + "no appropriate writer is found\n");
      }
    } catch (IOException ioe) {
      System.err.println("Error: " + ioe);
    }
  }

  /**
   * Convert the position on screen to the position on the map.
   *
   * @param screenPosX  The x-coordinate of the screen position
   * @param screenPosY  The y-coordinate of the screen position
   *
   * @return The corresponding position on the map; null if there is no map
   */
  public Point2D getMapPosition(int screenPosX, int screenPosY) {
    return new Point2D.Double(
        (screenPosX - posOfOriginX) / scaleTable[scaleIndex],
        (screenPosY - posOfOriginY) / scaleTable[scaleIndex]);
  }

  /////////////////////////////////
  // DEBUG
  /////////////////////////////////

  /**
   * Highlight a particular vehicle.
   *
   * @param vin  the VIN number of the vehicle
   */
  public void highlightVehicle(int vin) {
    Simulator sim = viewer.getSimulator();
    if (sim != null) {
      VehicleSimView vehicle = sim.getActiveVehicle(vin);
      if (vehicle != null) {
        displayBuffer.setPaint(HIGHLIGHTED_VEHICLE_COLOR);
        displayBuffer.setStroke(HIGHLIGHTED_VEHICLE_STROKE);
        displayBuffer.fill(vehicle.getShape());
        repaint();
      }
    }
  }
}
