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
package aim4.util;

import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

/**
 * A tiled area - a subdivision of an area into a grid of small rectangles.
 */
public class TiledArea {

  /////////////////////////////////
  // NESTED CLASSES
  /////////////////////////////////

  /**
   * A tile.
   */
  public static class Tile {
    /** The area controlled by this tile. */
    private final Rectangle2D rectangle;
    /** the x-coordinate of this tile */
    private final int x;
    /** the y-coordinate of this tile */
    private final int y;
    /** the id of this tile */
    private final int id;
    /** whether or not a tile is on the edge */
    private boolean edgeTile = false;

    /**
     * Create a tile.
     *
     * @param rectangle  the area of the tile
     * @param x          the x-coordinate of the tile
     * @param y          the y-coordinate of the tile
     * @param id         the ID of the tile
     */
    public Tile(Rectangle2D rectangle, int x, int y, int id) {
      this.rectangle = rectangle;
      this.x = x;
      this.y = y;
      this.id = id;
    }

    /** Get the area controlled by this ReservationTile. */
    public Rectangle2D getRectangle() {
      return rectangle;
    }

    /** Get the x-coordinate of this tile */
    public int getX() {
      return x;
    }

    /** Get the y-coordinate of this tile */
    public int getY() {
      return y;
    }

    /** Get the id of this tile */
    public int getId() {
      return id;
    }

    /** Whether or not this tile is on the edge */
    public boolean isEdgeTile() {
      return edgeTile;
    }

    /**
     * Set whether or not this tile is on the edge.
     *
     * @param edgeTile  whether or not this tile is on the edge
     */
    public void setEdgeTile(boolean edgeTile) {
      this.edgeTile = edgeTile;
    }
  }

  /////////////////////////////////
  // PRIVATE FIELDS
  /////////////////////////////////

  /** The area controlled by this tiled area. */
  private final Area area;
  /** The bounding rectangle controlled by this tiled area. */
  private final Rectangle2D rectangle;
  /** The number of tiles in the x-direction */
  private final int xNum;
  /** The number of tiles in the y-direction. */
  private final int yNum;
  /** The length of the tiles in the x-direction. */
  private final double xLength;
  /** The length of the tiles in the y-direction. */
  private final double yLength;
  /** The tiles in this area. */
  private final Tile[][] tiles;
  /** A mapping from id to tiles */
  private final ArrayList<Tile> idToTiles;
  /** The number of tiles */
  private int numberOfTiles;

  /////////////////////////////////
  // CLASS CONSTRUCTORS
  /////////////////////////////////

  /**
   * Create a tiled area
   *
   * @param area    the area
   * @param length  the length of a tile in both directions
   */
  public TiledArea(Area area, double length) {
    this(area, length, length);
  }

  /**
   * Create a tiled area
   *
   * @param area     the area
   * @param xLength  the length of a tile in the x-direction
   * @param yLength  the length of a tile in the y-direction
   */
  public TiledArea(Area area, double xLength, double yLength) {
    this.area = area;
    this.rectangle = area.getBounds2D();
    this.xLength = xLength;
    this.yLength = yLength;
    xNum = ((int)(rectangle.getWidth() / xLength)) + 1;
    yNum = ((int)(rectangle.getHeight() / yLength)) + 1;
    tiles = new Tile[xNum][yNum];
    idToTiles = new ArrayList<Tile>(xNum*yNum) ;
    createTiles();
    identifyEdgeTiles();
  }

  /**
   * Create the tiles
   */
  private void createTiles() {
    numberOfTiles = 0;
    for(int x = 0; x < xNum; x++) {
      for(int y = 0; y < yNum; y++) {
        // Create a tile
        // Start by finding the offset for this particular tile
        double xOffset = x * xLength;
        double yOffset = y * yLength;
        // These should be granularity most of the time, except on the
        // last row/column
        double width = Math.min(xLength, rectangle.getWidth() - xOffset);
        double height = Math.min(yLength, rectangle.getHeight() - yOffset);
        // Don't forget to offset from the starting coordinates of the
        // intersection bounding box
        Rectangle2D tileRect =
          new Rectangle2D.Double(rectangle.getMinX() + xOffset,
                                 rectangle.getMinY() + yOffset,
                                 width, height);
        // Now that we have a rectangle for the tile, we can figure out
        // whether it is actually in the area
        if(area.intersects(tileRect)) {
          // If it is in the area, let's make a new tile
          tiles[x][y] = new Tile(tileRect, x, y, numberOfTiles);
          idToTiles.add(tiles[x][y]);
          numberOfTiles++;
        }
      }
    }
  }

  /**
   * Identify the tiles that are on the edge of the area managed by this
   * tiled area.
   */
  private void identifyEdgeTiles() {
    // Now we must go through and discover which are edge tiles.  These are
    // simply the tiles that are not surrounded (in all 8 directions) by
    // other tiles
    for(int x = 0; x < xNum; x++) {
      for(int y = 0; y < yNum; y++) {
        // Don't process things that aren't tiles
        if(tiles[x][y] != null) {
          // If it's on the edge of the 2D array, it's definitely one
          if(x == 0 || y == 0 || x == xNum - 1 || y == yNum - 1) {
            tiles[x][y].setEdgeTile(true); // make it an edge tile
          } else {
            // These are guaranteed not to be on the edge of the 2D array
            // so we can just check around
            if(tiles[x - 1][y - 1] == null || // up, left
               tiles[x][y - 1] == null ||     // up
               tiles[x + 1][y - 1] == null || // up, right
               tiles[x - 1][y] == null ||     // left
               tiles[x + 1][y] == null ||     // right
               tiles[x - 1][y + 1] == null || // down, left
               tiles[x][y + 1] == null ||     // down
               tiles[x + 1][y + 1] == null) { // down, right
              // At least one of the surrounding spots doesn't have a tile
              tiles[x][y].setEdgeTile(true);
            }
          }
        }
      }
    }
  }

  //////////////////////////////////////////
  // PUBLIC METHODS (getters and setters)
  //////////////////////////////////////////

  /**
   * Get the area controlled by this tiled area
   *
   * @return the area controlled by this tiled area
   */
  public Area getArea() {
    return area;
  }

  /**
   * Get the number of tiles in the x-direction
   *
   * @return the number of tiles in the x-direction
   */
  public int getXNum() {
    return xNum;
  }

  /**
   * Get the number of tiles in the y-direction
   *
   * @return the number of tiles in the y-direction
   */
  public int getYNum() {
    return yNum;
  }

  /**
   * Get the length of a tile in the x-direction
   *
   * @return the length of a tile in the x-direction
   */
  public double getXLength() {
    return xLength;
  }

  /**
   * Get the length of a tile in the y-direction
   *
   * @return the length of a tile in the y-direction
   */
  public double getYLength() {
    return yLength;
  }

  /**
   * Get the tile at the (x,y) location in the grid
   *
   * @param x  the x-coordinate of the tile
   * @param y  the y-coordinate of the tile
   * @return  the tile; return null if the tile does not exist
   */
  public Tile getTile(int x, int y) {
    return tiles[x][y];
  }


  /**
   * Get the total number of tiles
   *
   * @return the total number of tiles
   */
  public int getNumberOfTiles() {
    return numberOfTiles;
  }


  /////////////////////////////////
  // PUBLIC METHODS
  /////////////////////////////////

  /**
   * Whether or not the tile are squares.
   *
   * @return whether or not the tile are squares
   */
  public boolean areTilesSquare() {
    return yLength == xLength;
  }

  /**
   * Get a tile according to its id.
   *
   * @param id  the id of the tile
   * @return the tile
   */
  public Tile getTileById(int id) {
    return idToTiles.get(id);
  }

  /**
   * Get the list of tiles that are occupied by the given Shape.
   *
   * @param shape   the Shape for which to find occupied tiles
   * @return  the List of tiles that are occupied by the given Shape
   */
  public List<Tile> findOccupiedTiles(Shape shape) {
    // A place to store the answer
    List<Tile> occupiedTiles = new ArrayList<Tile>();
    // We only need to check the tiles that are within the bounding box
    Rectangle2D boundingBox = shape.getBounds2D();
    // Now find out the actual indices that this bounding box corresponds to.
    // We do this by first finding out what the relative coordinate is (by
    // subtracting the two values), then finding how this fits into our
    // 2D array of tiles, by dividing by the width of the rectangle, and
    // multiplying by the number of columns or rows.
    int firstColumn =
      Math.max(0,
               (int)((boundingBox.getMinX() - rectangle.getMinX()) /
                     xLength));
    int lastColumn =
      Math.min(xNum - 1,
               (int)((boundingBox.getMaxX() - rectangle.getMinX()) /
                     xLength));
    int firstRow =
      Math.max(0,
               (int)((boundingBox.getMinY() - rectangle.getMinY()) /
                     yLength));
    int lastRow =
      Math.min(yNum - 1,
               (int)((boundingBox.getMaxY() - rectangle.getMinY()) /
                     yLength));
    // Now go through all the potential tiles and find the ones that this
    // shape intersects
    for(int c = firstColumn; c <= lastColumn; c++) {
      for(int r = firstRow; r <= lastRow; r++) {
        // If the tile exists, and it does intersect, add it to the list of
        // tiles that are occupied
        if(tiles[c][r] != null &&
           shape.intersects(tiles[c][r].getRectangle())) {
          occupiedTiles.add(tiles[c][r]);
        }
      }
    }
    return occupiedTiles;
  }

}
