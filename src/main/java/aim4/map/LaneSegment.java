/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aim4.map;

import expr.trb.DesignatedLanesExpr;

/**
 *
 * @author guni
 */
public class LaneSegment {

    private int numberOfCars;
    //private LaneSegment[] neighbours;
    private final int segmentIndex;

    public LaneSegment() {
        this.numberOfCars = 0;
        this.segmentIndex = DesignatedLanesExpr.segmentIndex++;
    }

//    public void setNeighbour(LaneSegment neighbour, DIRECTION dir){
//         neighbours[dir.toint()] = neighbour;
//    }
    
    public void exit() {
        if(numberOfCars > DesignatedLanesExpr.maxQueueLength){
            DesignatedLanesExpr.maxQueueLength = numberOfCars;
        }
        numberOfCars--;
        //neighbours[dir.toint()].enter();
    }

    public void enter() {
        numberOfCars++;
        //System.out.println("Lane " + segmentIndex + ", " + numberOfCars + " Vehicles.");
    }
    
        public int getNumberOfCars() {
        return numberOfCars;
    }

    public int getSegmentIndex() {
        return segmentIndex;
    }
        
        

    public enum DIRECTION {

        //You can initialize enums using enumname(value)

        LEFT(0),
        STRIGHT(1),
        RIGHT(2),
        OUT(3);

        private int direction;

        //Constructor which will initialize the enum

        DIRECTION(int dir) {
            direction = dir;
        }

        //method to return the direction set by the user which initializing the enum
        public int toint() {
            return direction;
        }
    }
}
