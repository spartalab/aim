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
package aim4.im.v2i.reservation;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.TreeMap;

/**
 * The Reservation Array.
 */
public class ReservationArray {

  /////////////////////////////////
  // CONSTANTS
  /////////////////////////////////

  private static final boolean SHOULD_CHECK_CONSISTENCY = false;

  /////////////////////////////////
  // PUBLIC NESTED CLASSES
  /////////////////////////////////

  /**
   * The smallest unit of space-time in the FCFS policy.  This
   * keeps track of both a ReservationTile and a discrete time,
   * which we use to keep track of the space-time reserved by
   * vehicles.
   */
  public static class TimeTile {
    /**
     * The discrete time.
     */
    private int dt;

    /**
     * The tile ID
     */
    private int tid;

    /**
     * Create a time-tile.
     *
     * @param dt    the discrete time
     * @param tid   the tile ID
     */
    public TimeTile(int dt, int tid) {
      this.dt = dt;
      this.tid = tid;
    }

    /**
     * Get the tile ID.
     * @return  the tile ID.
     */
    public int getTileId() {
      return tid;
    }

    /**
     * Get the discrete time.
     *
     * @return  the discrete time
     */
    public int getDiscreteTime() {
      return dt;
    }

    /**
     * Check whether this time-tile is equal to the given time-tile.
     *
     * @param tt  the given time-tile
     * @return whether this time-tile is equal to the given time-tile.
     */
    public boolean equals(TimeTile tt) {
      return (tid==tt.tid) && (dt==tt.dt);
    }

    /**
     * Convert this time-tile to a string representation
     */
    public String toString() {
      return "TT(" + tid + "," + dt + ")";
    }
  }


  /////////////////////////////////
  // PRIVATE FIELDS
  /////////////////////////////////

  /**
   * The total number of tiles.
   */
  private final int numOfTiles;

  /**
   * The grid table, a mapping from discrete times to grids.
   */
  private NavigableMap<Integer, int[]> grids;

  /**
   * A mapping from discrete times to mappings from reservation IDs to
   * the tile IDs that is reserved by the vehicle at the time.
   */
  private NavigableMap<Integer,Map<Integer,Set<Integer>>> timeToRidToTid;

  /**
   * A mapping from reservation IDs to mappings from discrete times to
   * the tile IDs that is reserved by the vehicle at the time.
   */
  private Map<Integer,NavigableMap<Integer,Set<Integer>>> ridToTimeToTid;


  /////////////////////////////////
  // CLASS CONSTRUCTORS
  /////////////////////////////////

  /**
   * Create a new reservation system.
   *
   * @param numOfTiles  The number of tiles in the intersection
   */
  public ReservationArray(int numOfTiles) {
    this.numOfTiles = numOfTiles;
    grids = new TreeMap<Integer, int[]>();
    timeToRidToTid = new TreeMap<Integer,Map<Integer,Set<Integer>>>();
    ridToTimeToTid = new HashMap<Integer,NavigableMap<Integer,Set<Integer>>>();
  }


  ///////////////////////////
  // PUBLIC METHODS
  ///////////////////////////

  /**
   * Get the number of tiles
   */
  public int getNumberOfTiles() {
    return numOfTiles;
  }

  /**
   * Whether the time-tile has been reserved.
   *
   * @param dt   the discrete time
   * @param tid  the tile ID
   */
  public boolean isReserved(int dt, int tid) {
    if (grids.containsKey(dt)) {
      return grids.get(dt)[tid] >= 0;
    } else {
      return false;
    }
  }

  /**
   * Get the reservation ID that reserved the given time-tile.
   *
   * @param dt   the discrete time
   * @param tid  the id of the tile
   * @return the reservation ID; -1 if the reservation ID does not exist
   */
  public int getReservationId(int dt, int tid) {
    if (grids.containsKey(dt)) {
      return grids.get(dt)[tid];
    } else {
      return -1;
    }
  }

  /**
   * Check whether a given reservation ID exists
   *
   * @param rid  the reservation ID
   * @return whether the reservation ID exists
   */
  public boolean hasReservation(int rid) {
    return ridToTimeToTid.containsKey(rid);
  }

  /**
   * Get the last time at which any time-tile has been reserved.
   *
   * @return the last time at which any time-tile has been reserved;
   *         -1 if there is currently no reservation.
   */
  public int getLastReservedDiscreteTime() {
    try {
      return grids.lastKey();
    } catch(NoSuchElementException e) {
      return -1;
    }
  }

  /**
   * Get the last discrete time of a particular reservation ID.
   *
   * @param  rid  the reservation ID
   * @return the last discrete time of the reservation;
   *         less than zero if the reservation id does not exist
   */
  public int getLastReservedDiscreteTime(int rid) {
    if (ridToTimeToTid.containsKey(rid)) {
      try {
        return ridToTimeToTid.get(rid).lastKey();
      } catch(NoSuchElementException e) {
        return -1;
      }
    } else {
      return -1;
    }
  }

  /**
   * Make the reservation of a set of time-tiles with a given reservation id.
   * If the reservation is not successful, no time-tiles will be reserved.
   *
   * @param rid          the reservation ID
   * @param workingList  a collection of time-tiles to be reserved
   *
   * @return whether the reservation is successful
   */
  public boolean reserve(int rid, Collection<? extends TimeTile> workingList) {
    // check to see if any time-tile is reserved in the past
    for(TimeTile tt : workingList) {
      int dt = tt.getDiscreteTime();
      if (grids.containsKey(dt) && grids.get(dt)[tt.getTileId()] >= 0) {
        return false; // the time-tile has been reserved.
      }
    }

    // actually make the reservation
    int timeBegin = 0;
    try {
      timeBegin = grids.firstKey();
    } catch(NoSuchElementException e) {
      // It means the grid is empty. All time-tiles are acceptable.
    }

    for(TimeTile tt : workingList) {
      int dt = tt.getDiscreteTime();
      int tid = tt.getTileId();

      if (dt >= timeBegin) {
        // update grids;
        int[] grid = grids.get(dt);
        if (grid == null) {
          grid = new int[numOfTiles];
          for(int i=0; i<numOfTiles; i++) { // initialize the grid
            grid[i] = -1;
          }
          grids.put(dt, grid);
        }
        grid[tid] = rid;

        // update timeToRidToTid
        Map<Integer,Set<Integer>> ridToTid = timeToRidToTid.get(dt);
        if (ridToTid == null) {
          ridToTid = new HashMap<Integer,Set<Integer>>();
          timeToRidToTid.put(dt, ridToTid);
        }
        Set<Integer> tidSet = ridToTid.get(rid);
        if (tidSet == null) {
          tidSet = new HashSet<Integer>();
          ridToTid.put(rid, tidSet);
        }
        tidSet.add(tid);

        // update ridToTimeToTid
        NavigableMap<Integer,Set<Integer>> timeToTid =
          ridToTimeToTid.get(rid);
        if (timeToTid == null) {
          timeToTid = new TreeMap<Integer,Set<Integer>>();
          ridToTimeToTid.put(rid, timeToTid);
        }
        tidSet = timeToTid.get(dt);
        if (tidSet == null) {
          tidSet = new HashSet<Integer>();
          timeToTid.put(dt, tidSet);
        }
        tidSet.add(tid);
      }  // else ignore timetile that is before timeBegin
    }
    assert (!SHOULD_CHECK_CONSISTENCY) || checkConsistency();
    return true;
  }

  /**
   * Cancel a reservation
   *
   * @param rid  the reservation ID
   * @return whether the cancellation is successful
   */
  public boolean cancel(int rid) {
    // remove elements in ridToTimeToTid
    NavigableMap<Integer,Set<Integer>> timeToTid = ridToTimeToTid.remove(rid);
    if (timeToTid != null) {
      for(int dt : timeToTid.keySet()) {
        // remove elements in timeToRidToTid
        if (timeToRidToTid.containsKey(dt)) {
          timeToRidToTid.get(dt).remove(rid);
        }
        // remove time-tiles in grids
        if (grids.containsKey(dt)) {
          int[] grid = grids.get(dt);
          for(int tid : timeToTid.get(dt)) {
            grid[tid] = -1;
          }
        }
      }
      assert (!SHOULD_CHECK_CONSISTENCY) || checkConsistency();
      return true;
    } else {
      return false; // the rid is not found
    }
  }

  /**
   * Remove all reservations before a given discrete time.
   *
   * @param dt  the discrete time before which the reservations will be removed.
   */
  public void cleanUp(int dt) {
    // clean up grids and timeToRidToTid
    try {
      while(grids.firstKey() < dt) {
        int dt1 = grids.firstKey();
        grids.remove(dt1);
        timeToRidToTid.remove(dt1);
      }
    } catch(NoSuchElementException e) {
      // do nothing
    }

    List<Integer> removeRid = new LinkedList<Integer>();
    for(int rid : ridToTimeToTid.keySet()){
      NavigableMap<Integer,Set<Integer>> timeToTid = ridToTimeToTid.get(rid);
      try {
        while(timeToTid.firstKey() < dt) {
          timeToTid.remove(timeToTid.firstKey());
        }
      } catch(NoSuchElementException e) {
        // do nothing
      }
      if (timeToTid.isEmpty()) {
        removeRid.add(rid);
      }
    }

    for(int rid : removeRid) {
      ridToTimeToTid.remove(rid);
    }

    assert (!SHOULD_CHECK_CONSISTENCY) || checkConsistency();
  }

  /**
   * Get the set of all reserved tiles at a given discrete time.
   *
   * @param dt  the discrete time
   * @return the list of tile IDs that are reserved at the given discrete time.
   */
  public List<Integer> getReservedTilesAtTime(int dt) {
    Map<Integer,Set<Integer>> ridToTid = timeToRidToTid.get(dt);
    if (ridToTid != null) {
      List<Integer> dts = new LinkedList<Integer>();
      for(int rid : ridToTid.keySet()) {
        dts.addAll(ridToTid.get(rid));
      }
      return dts;
    } else {
      return new LinkedList<Integer>(); // return an empty list
    }
  }

  /**
   * Get the VINs of all reserved tiles at a given discrete time.
   *
   * @param dt  the discrete time
   * @return a set of reservation IDs.
   */
  public Set<Integer> getVinOfReservedTilesAtTime(int dt) {
    Map<Integer,Set<Integer>> ridToTid = timeToRidToTid.get(dt);
    if (ridToTid != null) {
      return Collections.unmodifiableSet(ridToTid.keySet());
    } else {
      return new HashSet<Integer>(); // return an empty list
    }
  }


  /////////////////////////////////
  // DEBUG
  /////////////////////////////////

  /**
   * Check whether the array are consistent
   */
  private boolean checkConsistency() {
    for(int dt : grids.keySet()) {
      int[] tids = grids.get(dt);
      for(int tid=0; tid < numOfTiles; tid++) {
        int rid = tids[tid];
        if (rid >= 0) {
          assert timeToRidToTid.get(dt) != null;
          assert timeToRidToTid.get(dt).get(rid) != null;
          if (!timeToRidToTid.get(dt).get(rid).contains(tid)) {
            throw new RuntimeException("ReservationArray::checkConsistency():" +
                                       "grids > timeToRidToTid");
          }
          assert ridToTimeToTid.get(rid) != null;
          assert ridToTimeToTid.get(rid).get(dt) != null;
          if (!ridToTimeToTid.get(rid).get(dt).contains(tid)) {
            throw new RuntimeException("ReservationArray::checkConsistency():" +
                                       "grids > ridToTimeToTid");
          }
        }
      }
    }

    for(int dt : timeToRidToTid.keySet()) {
      Map<Integer,Set<Integer>> ridToTid = timeToRidToTid.get(dt);
      for(int rid : ridToTid.keySet()) {
        for(int tid : ridToTid.get(rid)) {
          if (grids.get(dt)[tid] != rid) {
            throw new RuntimeException("ReservationArray::checkConsistency():" +
                                        "timeToRidToTid > grids");

          }
        }
      }
    }

    for(int rid : ridToTimeToTid.keySet()) {
      NavigableMap<Integer,Set<Integer>> timeToTid = ridToTimeToTid.get(rid);
      for(int dt : timeToTid.keySet()) {
        for(int tid : timeToTid.get(dt)) {
          if (grids.get(dt)[tid] != rid) {
            throw new RuntimeException("ReservationArray::checkConsistency():" +
                                        "ridToTimeToTid > grids");

          }
        }
      }
    }
    return true;
  }
}

