/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package expr.trb;

import aim4.config.SimConfig;
import aim4.map.Road;

/**
 *
 * @author guni
 */
public class TurnPolicies {

    //DESIGNATE Lanes/TURNS (0 is left most lane)
    //X intersection 3 lanes
    public static int[] H_RIGHT_ALLOWED = {2};
    public static int[] H_STRAIGHT_ALLOWED = {1};
    public static int[] H_LEFT_ALLOWED = {0};

    public static int[] AV_RIGHT_ALLOWED = {2, 1};
    public static int[] AV_STRAIGHT_ALLOWED = {1, 2, 0};
    public static int[] AV_LEFT_ALLOWED = {0, 1};

    public static int[] CC_RIGHT_ALLOWED = {2};
    public static int[] CC_STRAIGHT_ALLOWED = {1};
    public static int[] CC_LEFT_ALLOWED = {0};

    public static int[] ACC_RIGHT_ALLOWED = {2};
    public static int[] ACC_STRAIGHT_ALLOWED = {1};
    public static int[] ACC_LEFT_ALLOWED = {0};

    // T intersection policies 2 lanes
    //Strict
    public static int[] strictEastBoundLeft = {};
    public static int[] strictEastBoundStraight = {0};
    public static int[] strictEastBoundRight = {1};

    public static int[] strictNorthBoundLeft = {0};
    public static int[] strictNorthBoundStraight = {};
    public static int[] strictNorthBoundRight = {1};

    public static int[] strictWestBoundLeft = {0};
    public static int[] strictWestBoundStraight = {1};
    public static int[] strictWestBoundRight = {};
   

    //Flexible
    public static int[] flexibleEastBoundLeft = {};
    public static int[] flexibleEastBoundStraight = {0, 1};
    public static int[] flexibleEastBoundRight = {1};

    public static int[] flexibleNorthBoundLeft = {0, 1};
    public static int[] flexibleNorthBoundStraight = {};
    public static int[] flexibleNorthBoundRight = {1};

    public static int[] flexibleWestBoundLeft = {0, 1};
    public static int[] flexibleWestBoundStraight = {1};
    public static int[] flexibleWestBoundRight = {};

    //Liberal
    public static int[] liberalEastBoundLeft = {};
    public static int[] liberalEastBoundStraight = {0, 1};
    public static int[] liberalEastBoundRight = {0, 1};

    public static int[] liberalNorthBoundLeft = {0, 1};
    public static int[] liberalNorthBoundStraight = {};
    public static int[] liberalNorthBoundRight = {0, 1};

    public static int[] liberalWestBoundLeft = {0, 1};
    public static int[] liberalWestBoundStraight = {0, 1};
    public static int[] liberalWestBoundRight = {};
    
    private static int[][][][] policy_T;
    
    public static void initPolicyT(){
        int[][] strictEastBound = {strictEastBoundRight, strictEastBoundStraight, strictEastBoundLeft};
        int[][] strictNorthBound = {strictNorthBoundRight, strictNorthBoundStraight, strictNorthBoundLeft};
        int[][] strictWestBound = {strictWestBoundRight, strictWestBoundStraight, strictWestBoundLeft};
        
        int[][][] strict = {strictEastBound, strictNorthBound, strictWestBound};
        
        int[][] flexibleEastBound = {flexibleEastBoundRight, flexibleEastBoundStraight, flexibleEastBoundLeft};
        int[][] flexibleNorthBound = {flexibleNorthBoundRight, flexibleNorthBoundStraight, flexibleNorthBoundLeft};
        int[][] flexibleWestBound = {flexibleWestBoundRight, flexibleWestBoundStraight, flexibleWestBoundLeft};
        
        int[][][] flexible = {flexibleEastBound, flexibleNorthBound, flexibleWestBound};
        
        int[][] liberalEastBound = {liberalEastBoundRight, liberalEastBoundStraight, liberalEastBoundLeft};
        int[][] liberalNorthBound = {liberalNorthBoundRight, liberalNorthBoundStraight, liberalNorthBoundLeft};
        int[][] liberalWestBound = {liberalWestBoundRight, liberalWestBoundStraight, liberalWestBoundLeft};
        
        int[][][] liberal = {liberalEastBound, liberalNorthBound, liberalWestBound};
        
        int[][][][] policy_Ttemp = {strict, flexible, liberal};
        policy_T = policy_Ttemp;
    }

    public static int[] getPolicy_T(int type, int heading, Road road) {
        if (SimConfig.VEHICLE_TYPE.values()[type] == SimConfig.VEHICLE_TYPE.HUMAN) {
            return policy_T[DesignatedLanesExpr.policyH_T.ordinal()][roadToIndex(road.getIndex())][heading];
        } else { //Vehicle is Autonamous or semi-autonamous
            return policy_T[DesignatedLanesExpr.policyAV_T.ordinal()][roadToIndex(road.getIndex())][heading];
        }
    }
    
    private static int roadToIndex(int r){
        if(r == 0){
            return 1;
        }
        if(r == 2){
            return 0;
        }
        if(r == 3){
            return 2;
        }
        return -1;
    }
}
