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
package aim4.map.track;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.awt.Shape;
import java.awt.geom.GeneralPath;

/**
 * A track created by joining multiple tracks.
 */
public class PathTrack implements Track {

  /////////////////////////////////
  // NESTED CLASSES
  /////////////////////////////////

  /**
   * A position on the track.
   */
  public class Position implements TrackPosition {

    /** The track position */
    TrackPosition pos;

    /** The track ID */
    int trackId;

    /**
     * Create a position object at the given distance from the starting
     * waypoint on this track
     *
     * @param dist  the distance
     */
    public Position(double dist) {
      try {
        trackId = 0;
        pos = tracks.getFirst().getPosition(0.0);
        if (dist > 0) {
          double d = move(dist);
          if (d == dist) {
            throw new RuntimeException("Cannot create track position because " +
                                       "the first track has zero length.");
          }
        }
      } catch(NoSuchElementException e) {
        throw new RuntimeException("Cannot create track position because " +
                                   "no track left in the path.");
      }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getX() {
      return pos.getX();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getY() {
      return pos.getY();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getTangentSlope() {
      return pos.getTangentSlope();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final double move(double dist) {
      while(true) {
        double d = pos.move(dist);
        if (d == 0) {
          return 0;  // we are done
        } else if (d < dist) {
          if (trackId < tracks.size()-1) {
            trackId++;
            pos = tracks.get(trackId).getPosition(0);
            dist = d;
          } else {  // trackId == tracks.size()
            pos = null; // no more track, just leave
            return d;
          }
        } else {  // d == dist, the current track has zero length
          assert d == dist;
          if (trackId < tracks.size()-1) {
            trackId++;
            pos = tracks.get(trackId).getPosition(0);
          } else {  // trackId == tracks.size()
            pos = null; // no more track, just leave
            return dist;
          }
        }
      }
    }
  }

  /////////////////////////////////
  // PRIVATE FIELDS
  /////////////////////////////////

  /**
   * The points of this path
   */
  private LinkedList<WayPoint> points;

  /**
   * The tracks on this path
   */
  private LinkedList<Track> tracks;

  /**
   * A shape describing this track
   */
  private GeneralPath shape;

  /**
   * The total length of this path
   */
  private double length;

  /////////////////////////////////
  // CONSTRUCTORS
  /////////////////////////////////

  /**
   * Create an empty path track.
   */
  public PathTrack() {
    points = new LinkedList<WayPoint>();
    tracks = new LinkedList<Track>();
    shape = new GeneralPath();
    length = 0.0;
  }

  /////////////////////////////////
  // PUBLIC METHODS
  /////////////////////////////////

  /**
   * Add a track to the path track.
   *
   * @param track  the track
   */
  public void add(Track track) {
    if (points.size()==0) {
      points.add(track.getStartWayPoint());
      points.add(track.getEndWayPoint());
    } else {
      assert track.getStartWayPoint().getId() < 0 ||
             points.getLast().getId() < 0 ||
             track.getStartWayPoint().getId() == points.getLast().getId();
      points.add(track.getEndWayPoint());
    }
    tracks.add(track);
    shape.append(track.getShape(), false);
    length += track.getLength();
  }

  /**
   * Append a path track to the end of this path track.
   *
   * @param track  the path track
   */
  public void append(PathTrack track) {
    for(Track seg : track.getTracks()) {
      add(seg);
    }
  }

  /**
   * Get the list of tracks that constitutes this path track.
   *
   * @return the list of tracks
   */
  public List<Track> getTracks() {
    return Collections.unmodifiableList(tracks);
  }


  /////////////////////////////////
  // PUBLIC METHODS
  /////////////////////////////////

  /**
   * {@inheritDoc}
   */
  @Override
  public WayPoint getStartWayPoint() {
    return points.getFirst();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public WayPoint getEndWayPoint() {
    return points.getLast();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public double getLength() {
    return length;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public TrackPosition getPosition(double dist) {
    if (0.0 <= dist && dist <= length) {
      return new Position(dist);
    } else {
      return null;
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Shape getShape() {
    return shape;
  }

}
