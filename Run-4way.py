#!/usr/bin/env python
import numpy as np
import matplotlib.pyplot as plt

import subprocess
import random

#MACHINE_NAME = "MAC"
MACHINE_NAME = "DELL"

#Three lanes
threeLanesLeft0 = "0"
threeLanesStraight0 = "1"
threeLanesRight0 = "2"

threeLanesLeft1a = "0"
threeLanesStraight1a = "1 2"
threeLanesRight1a = "2"

threeLanesLeft1b = "0 1"
threeLanesStraight1b = "2"
threeLanesRight1b = "2"

threeLanesLeft2a = "0"
threeLanesStraight2a = "0 1 2"
threeLanesRight2a = "2"

threeLanesLeft2b = "0 1"
threeLanesStraight2b = "1 2"
threeLanesRight2b = "2"

threeLanesLeft2c = "0 1"
threeLanesStraight2c = "1"
threeLanesRight2c = "1 2"

threeLanesLeft3 = "0 1"
threeLanesStraight3 = "0 1 2"
threeLanesRight3 = "2"

threeLanesLeft4 = "0 1"
threeLanesStraight4 = "0 1 2"
threeLanesRight4 = "1 2"

#-----------------------------------
#Four lanes
fourLanesLeftNormal = "0 1"
fourLanesStraightNormal = "2 3"
fourLanesRightNormal = "3"

fourLanesLeftStrict = "0"
fourLanesStraightStrict = "1 2"
fourLanesRightStrict = "3"

fourLanesLeftLiberal = "0 1"
fourLanesStraightLiberal = "0 1 2 3"
fourLanesRightLiberal = "2 3"
#-----------------------------------

blockedLane = ""

#threeLanesLeftTurn = [threeLanesLeft0, threeLanesLeft1a, threeLanesLeft1b, threeLanesLeft2a, threeLanesLeft2b, threeLanesLeft2c, threeLanesLeft3, threeLanesLeft4, blockedLane]
#threeLanesStraightTurn = [threeLanesStraight0, threeLanesStraight1a, threeLanesStraight1b, threeLanesStraight2a, threeLanesStraight2b, threeLanesStraight2c, threeLanesStraight3, threeLanesStraight4, blockedLane]
#threeLanesRightTurn = [threeLanesRight0, threeLanesRight1a, threeLanesRight1b, threeLanesRight2a, threeLanesRight2b, threeLanesRight2c, threeLanesRight3, threeLanesRight4, blockedLane]

threeLanesLeftTurn = [threeLanesLeft0, threeLanesLeft2a, threeLanesLeft2c, threeLanesLeft4, blockedLane]
threeLanesStraightTurn = [threeLanesStraight0, threeLanesStraight2a, threeLanesStraight2c, threeLanesStraight4, blockedLane]
threeLanesRightTurn = [threeLanesRight0, threeLanesRight2a, threeLanesRight2c, threeLanesRight4, blockedLane]

threeLanes = [threeLanesLeftTurn,threeLanesStraightTurn,threeLanesRightTurn]

fourLanesLeftTurn = [fourLanesLeftNormal, fourLanesLeftStrict, fourLanesLeftLiberal, blockedLane]
fourLanesStraightTurn = [fourLanesStraightNormal, fourLanesStraightStrict, fourLanesStraightLiberal, blockedLane]
fourLanesRightTurn = [fourLanesRightNormal, fourLanesRightStrict, fourLanesRightLiberal, blockedLane]

fourLanes = [fourLanesLeftTurn,fourLanesStraightTurn,fourLanesRightTurn]

laneAssignment = [threeLanes,fourLanes] #laneAssignment[number of lanes][direction][policy]

# Number of lanes
threeLanesIndex = 0
fourLaneIndex = 1

# Direction
leftIndex = 0
straightIndex = 1
rightIndex = 2

# policy
#Index0 = 0
#Index1a = 1
#Index1b = 2
#Index2a = 3
#Index2b = 4
#Index2c = 5
#Index3 = 6
#Index4 = 7
#IndexBlocked = 8

Index0 = 0
Index2a = 1
Index2c = 2
Index4 = 3
IndexBlocked = 4

#FREE_FLOW_3_LANES_X = 14.08375369 #speed = 25, 20% right, 20 % left, 60% straight
#FREE_FLOW_2_LANES_T = 14.6225667 #speed = 25, 40% turn, 60% straight
FREE_FLOW_2_LANES_T = 23 #speed = 15, 40% turn, 60% straight
FREE_FLOW_3_LANES_X = 22.03 #speed = 15, 30% right, 20 % left, 50% straight

fileName = "100percent-4way"


def run(args):
    try:
        p = subprocess.Popen(['/usr/bin/java', '-classpath',path,'expr/trb/DesignatedLanesExpr',args[0], args[1], args[2],args[3], args[4], args[5],args[6], args[7], args[8],args[9], args[10], args[11],args[12], args[13], args[14],args[15], args[16], args[17],args[18], args[19], args[20],args[21], args[22], args[23], args[24], args[25],args[26], args[27], args[28], args[29], args[30], args[31]])
        p.wait()
    except subprocess.CalledProcessError as e:
        print "Error 1"

def makeArgs(lanesPerRoad, vehicleVolume, isIntersectionT, turnPolicy_T_H, turnPolicy_T_AV ,turnPolicyH, turnPolicyAV, turnPolicyCC, turnPolicyACC, ratioAV, ratioCC,
             ratioACC, ratioRight, ratioStright, scenarioIndex, isOneLaneSignal, freeFlow, speed_limit, buffer_size_seconds):
    args = [0 for index in range(32)]
    args[0] = str(lanesPerRoad)                                             # 0 - int, NUMBER_OF_LANES
    args[1] = str(vehicleVolume)                                            # 1 - int, VEHICLES_LANE_HOUR
    args[2] = str(random.randint(0,999999))                                 # 2 - int, rand seed

    args[3] = isIntersectionT                                               # 3 - String (TRUE, FALSE), is T intersection
    args[4] = turnPolicy_T_H                                                # 4 - String (STRICT, FLEXIBLE, LIBERAL, NULL), HV policy for T intersection
    args[5] = turnPolicy_T_AV                                               # 5 - String (STRICT, FLEXIBLE, LIBERAL, NULL), CAV policy for T intersection

    if isIntersectionT == "FALSE":
        args[6] = laneAssignment[lanesPerRoad-3][rightIndex][turnPolicyH]       # 6 - int[], H_RIGHT_ALLOWED
        args[7] = laneAssignment[lanesPerRoad-3][straightIndex][turnPolicyH]    # 7 - int[], H_STRIGHT_ALLOWED
        args[8] = laneAssignment[lanesPerRoad-3][leftIndex][turnPolicyH]        # 8 - int[], H_LEFT_ALLOWED

        args[9] = laneAssignment[lanesPerRoad-3][rightIndex][turnPolicyAV]      # 9 - int[], AV_RIGHT_ALLOWED
        args[10] = laneAssignment[lanesPerRoad-3][straightIndex][turnPolicyAV]  # 10 - int[], AV_STRIGHT_ALLOWED
        args[11] = laneAssignment[lanesPerRoad-3][leftIndex][turnPolicyAV]      # 11 - int[], AV_LEFT_ALLOWED

        args[12] = laneAssignment[lanesPerRoad-3][rightIndex][turnPolicyCC]     # 12 - int[], CC_RIGHT_ALLOWED
        args[13] = laneAssignment[lanesPerRoad-3][straightIndex][turnPolicyCC]  # 13 - int[], CC_STRIGHT_ALLOWED
        args[14] = laneAssignment[lanesPerRoad-3][leftIndex][turnPolicyCC]      # 14 - int[], CC_LEFT_ALLOWED

        args[15] = laneAssignment[lanesPerRoad-3][rightIndex][turnPolicyACC]    # 15 - int[], ACC_RIGHT_ALLOWED
        args[16] = laneAssignment[lanesPerRoad-3][straightIndex][turnPolicyACC] # 16 - int[], ACC_STRIGHT_ALLOWED
        args[17] = laneAssignment[lanesPerRoad-3][leftIndex][turnPolicyACC]     # 17 - int[], ACC_LEFT_ALLOWED

    else:
        args[6] = ""       # 6 - int[], H_RIGHT_ALLOWED
        args[7] = ""    # 7 - int[], H_STRIGHT_ALLOWED
        args[8] = ""        # 8 - int[], H_LEFT_ALLOWED

        args[9] = ""      # 9 - int[], AV_RIGHT_ALLOWED
        args[10] = ""  # 10 - int[], AV_STRIGHT_ALLOWED
        args[11] = ""      # 11 - int[], AV_LEFT_ALLOWED

        args[12] = ""     # 12 - int[], CC_RIGHT_ALLOWED
        args[13] = ""  # 13 - int[], CC_STRIGHT_ALLOWED
        args[14] = ""      # 14 - int[], CC_LEFT_ALLOWED

        args[15] = ""    # 15 - int[], ACC_RIGHT_ALLOWED
        args[16] = "" # 16 - int[], ACC_STRIGHT_ALLOWED
        args[17] = ""     # 17 - int[], ACC_LEFT_ALLOWED

    args[18] = str(ratioAV)                                                 # 18 - double, ratioAV
    args[19] = str(ratioCC)                                                 # 19 - double, ratioCC
    args[20] = str(ratioACC)                                                # 20 - double, ratioACC
    args[21] = str(ratioRight)                                              # 21 - double, ratioRIGHT
    args[22] = str(ratioStright)                                            # 22 - double, ratioSTRIGHT
    args[23] = "%s" %(fileName)                                             # 23 - String, outfile
    args[24] = "0"                                                          # 24 - double, dropMessageProb
    args[25] = "0"                                                          # 25 - double, droppedTimeToDetect
    args[26] = str(scenarioIndex)                                           # 26 - int, scenario index
    args[27] = MACHINE_NAME                                                 # 27 - String, What machine are we running
    args[28] = str(freeFlow)                                                # 28 - int, free flow average time
    args[29] = isOneLaneSignal                                              # 29 - String (TRUE, FALSE), is one lane green policy
    args[30] = str(buffer_size_seconds)                                          # 30 - double, safety buffer in seconds
    args[31] = str(speed_limit)                                                 # 31 - double, speed limit
    return args





path = '/Users/guni/Dropbox/Code/Semi-AIM/target/classes' #MAC
if MACHINE_NAME == "DELL":
    path = '/home/guni/Dropbox/Code/Semi-AIM/target/classes' #DELL

vehicleVolume = [100,300,500]
lanes = 3
AVratio = [0, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1]
instances = 20

oneLaneGreen = "FALSE"
isIntersectionT = "FALSE"
turnPoliciesStrictT = "STRICT"
turnPoliciesFlexibleT = "FLEXIBLE"
turnPoliciesLiberalT = "LIBERAL"
turnPoliciesNullT = "NULL"

ratio_CC = 0
ratio_ACC = 0

ratio_right_turn = 0.3
ratio_straight_turn = 0.5

speed_limit = 15
buffer_size = 0.5

# first run 50 cars per lane per hour only AV to get free flow travel times
#for j in range(0, instances):
#    args = makeArgs(lanes,50, isIntersectionT, turnPoliciesStrictT, turnPoliciesLiberalT,IndexBlocked,IndexBlocked,IndexBlocked,IndexBlocked, 1, 0, 0, 0, 0.6, j, oneLaneGreen, 0)
#    run(args)

if(isIntersectionT == "FALSE"):
    i = len(AVratio) * len(vehicleVolume) * instances * 4;
    for vehiclePerLanePerHour in vehicleVolume:
        for ratioAV in AVratio:
            for x in range(0, 3):

                turnPolicyH = x
                turnPolicyAV = x
                turnPolicyCC = IndexBlocked
                turnPolicyACC = IndexBlocked

                for j in range(0, instances):

                    print "********************"
                    print "%s instances left" %i
                    print "********************"
                    print ""
                    args = makeArgs(lanes,vehiclePerLanePerHour, isIntersectionT, turnPoliciesNullT, turnPoliciesNullT,turnPolicyH,turnPolicyAV,turnPolicyCC,turnPolicyACC, ratioAV, ratio_CC, ratio_ACC, ratio_right_turn, ratio_straight_turn, j, oneLaneGreen,FREE_FLOW_3_LANES_X, speed_limit, buffer_size)
                    run(args)
                    i -= 1

                if x == 0:
                    turnPolicyAV = Index4
                    for j in range(0, instances):

                        print "********************"
                        print "%s instances left" %i
                        print "********************"
                        print ""
                        args = makeArgs(lanes,vehiclePerLanePerHour, isIntersectionT, turnPoliciesNullT, turnPoliciesNullT,turnPolicyH,turnPolicyAV,turnPolicyCC,turnPolicyACC, ratioAV, ratio_CC, ratio_ACC, ratio_right_turn, ratio_straight_turn, j, oneLaneGreen,FREE_FLOW_3_LANES_X, speed_limit, buffer_size)
                        run(args)
                        i -= 1
else:
    i = len(AVratio) * len(vehicleVolume) * instances * 3;
    for vehiclePerLanePerHour in vehicleVolume:
        for ratioAV in AVratio:
            for j in range(0, instances):

                print "********************"
                print "%s instances left" %i
                print "********************"
                print ""
                args = makeArgs(lanes,vehiclePerLanePerHour, isIntersectionT, turnPoliciesStrictT, turnPoliciesStrictT,IndexBlocked,IndexBlocked,IndexBlocked,IndexBlocked, ratioAV, ratio_CC, ratio_ACC, ratio_right_turn, ratio_straight_turn, j, oneLaneGreen,FREE_FLOW_2_LANES_T, speed_limit, buffer_size)
                run(args)
                i -= 1

            for j in range(0, instances):

                print "********************"
                print "%s instances left" %i
                print "********************"
                print ""
                args = makeArgs(lanes,vehiclePerLanePerHour, isIntersectionT, turnPoliciesFlexibleT, turnPoliciesFlexibleT,IndexBlocked,IndexBlocked,IndexBlocked,IndexBlocked, ratioAV, ratio_CC, ratio_ACC, ratio_right_turn, ratio_straight_turn, j, oneLaneGreen,FREE_FLOW_2_LANES_T, speed_limit, buffer_size)
                run(args)
                i -= 1

            for j in range(0, instances):

                print "********************"
                print "%s instances left" %i
                print "********************"
                print ""
                args = makeArgs(lanes,vehiclePerLanePerHour, isIntersectionT, turnPoliciesStrictT, turnPoliciesLiberalT,IndexBlocked,IndexBlocked,IndexBlocked,IndexBlocked, ratioAV, ratio_CC, ratio_ACC, ratio_right_turn, ratio_straight_turn, j, oneLaneGreen,FREE_FLOW_2_LANES_T, speed_limit, buffer_size)
                run(args)
                i -= 1


