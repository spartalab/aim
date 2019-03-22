/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package expr.trb;

import aim4.config.Debug;
import aim4.config.Platoon;
import aim4.config.SimConfig;
import aim4.config.SimConfig.VEHICLE_TYPE;
import aim4.driver.pilot.V2IPilot;
import aim4.gui.Viewer;
import aim4.map.BasicMap;
import aim4.map.Road;
import aim4.map.SpawnPoint;
import aim4.map.lane.Lane;
import aim4.vehicle.VehicleSimView;
import aim4.vehicle.VinRegistry;
import java.util.List;
import aim4.sim.AutoDriverOnlySimulator;
import aim4.sim.Simulator;
import aim4.sim.setup.ApproxNPhasesTrafficSignalSimSetup;
import aim4.sim.setup.BasicSimSetup;
import aim4.util.Registry;
import aim4.util.Util;
import static expr.trb.TrafficSignalExpr.AVrejects;
import static expr.trb.TrafficSignalExpr.AVtotal;
import static expr.trb.TrafficSignalExpr.AVtotalTime;
import static expr.trb.TrafficSignalExpr.Hrejects;
import static expr.trb.TrafficSignalExpr.Htotal;
import static expr.trb.TrafficSignalExpr.HtotalTime;
import static expr.trb.TrafficSignalExpr.SAVrejects;
import static expr.trb.TrafficSignalExpr.SAVtotal;
import static expr.trb.TrafficSignalExpr.SAVtotalTime;
import static expr.trb.TrafficSignalExpr.dropMessageProb;
import static expr.trb.TrafficSignalExpr.droppedTimeToDetect;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Random;

/**
 *
 * @author guni
 */
public class DesignatedLanesExpr {

    public static boolean DESIGNATED_LANES = true;
    public static boolean STAY_IN_SAME_LANE = true;
    public static int NUMBER_OF_LANES = 3;
    public static int VEHICLES_LANE_HOUR = 300;
    public static double VEHICLES_ROAD_SECOND;
    public static double MAXIMAL_TIME_TO_FUTURE_RESERVATION = 6;
    public static int SEED = 2017;
    public static Registry<Lane> laneRegistry;
    public static int[][][] designated;
    public static double SPEED_LIMIT = 15;
    public static double SAFETY_BUFFER_SECONDS = 0.1;
    public static double SAFETY_BUFFER_METERS = 0.1;

    // T intesection parameters/////////
    public static boolean intersectionIsTypeT = false;

    public enum Policy_Type {

        STRICT, FLEXIBLE, LIBERAL
    };
    public static Policy_Type policyH_T = Policy_Type.STRICT;
    public static Policy_Type policyAV_T = Policy_Type.LIBERAL;
    ////////////////////////////////////////////

    //Type proportion
    public static double ratioAV = 0.8;
    public static double ratioCC = 0;
    public static double ratioACC = 0;
    public static double ratioH;

    //Turn proportion
    public static double ratioRIGHT = 0.3;
    public static double ratioSTRAIGHT = 0.5;
    public static double ratioLEFT;

    //Don't change these values//
    public static int segmentIndex = 0;
    public static int maxQueueLength = 0;
    public static double maxTravelTimeH = 0;
    public static double maxTravelTimeAV = 0;
    public static double maxTravelTimeCC = 0;
    public static double maxTravelTimeACC = 0;
    //////////////////////////////

    // Output near misses timing/////////
    public static boolean OBSERVE_NEAR_MISS = false;
    public static List<Double> nearMisses = new ArrayList<Double>();
    public static double MAX_MISS_MEASURE = 2.0;

    public static void main(String[] args) {

        //parseArray("");
        //args = testArgs();
        //System.out.println(args.length + " - " + ARGS.values().length);
        if (args.length != ARGS.values().length) {
            System.out.println("args.length observed = " + args.length);
            System.out.println("args.length required = " + ARGS.values().length);
            SimConfig.signalType = SimConfig.SIGNAL_TYPE.TRADITIONAL;
            RunVisual();
        } else {
            RunExperiment(args);
        }
    }

    public static void init() {

        VEHICLES_ROAD_SECOND = (double) (VEHICLES_LANE_HOUR * NUMBER_OF_LANES) / 3600;
        ratioH = 1 - ratioAV - ratioCC - ratioACC;
        ratioLEFT = 1 - ratioRIGHT - ratioSTRAIGHT;
        int[][] designatedAV = {TurnPolicies.AV_RIGHT_ALLOWED, TurnPolicies.AV_STRAIGHT_ALLOWED, TurnPolicies.AV_LEFT_ALLOWED};
        int[][] designatedH = {TurnPolicies.H_RIGHT_ALLOWED, TurnPolicies.H_STRAIGHT_ALLOWED, TurnPolicies.H_LEFT_ALLOWED};
        //designatedAV = designatedH;
        int[][] designatedCC = {TurnPolicies.CC_RIGHT_ALLOWED, TurnPolicies.CC_STRAIGHT_ALLOWED, TurnPolicies.CC_LEFT_ALLOWED};
        int[][] designatedACC = {TurnPolicies.ACC_RIGHT_ALLOWED, TurnPolicies.ACC_STRAIGHT_ALLOWED, TurnPolicies.ACC_LEFT_ALLOWED};
        int[][][] designatedT = {designatedAV, designatedH, designatedCC, designatedACC};
        designated = designatedT;
        SimConfig.MUST_STOP_BEFORE_INTERSECTION = false;
        Util.resetRand(SEED);
        maxQueueLength = 0;
        maxTravelTimeH = 0;
        maxTravelTimeAV = 0;
        maxTravelTimeCC = 0;
        maxTravelTimeACC = 0;

        SimConfig.FULLY_OBSERVING = true;
        Platoon.platooning = false;
        SimConfig.RED_PHASE_LENGTH = 0.0;
    }

    public static void spawnVehicles(double timeStep, AutoDriverOnlySimulator sim) {

        double humanProb = timeStep * VEHICLES_ROAD_SECOND * ratioH;
        double CCProb = timeStep * VEHICLES_ROAD_SECOND * ratioCC;
        double ACCProb = timeStep * VEHICLES_ROAD_SECOND * ratioCC;
        double AVProb = timeStep * VEHICLES_ROAD_SECOND * ratioAV;

        double[] typeProbs = {AVProb, humanProb, CCProb, ACCProb};
        //probs[VEHICLE_TYPE.ADAPTIVE_CRUISE.ordinal()] = 4;
        double[] turnProb = {ratioRIGHT, ratioSTRAIGHT, ratioLEFT};
        //For each road
        for (Road road : sim.getMap().getRoads()) {
            //While prob of spawning any kind of car is graeter than random
            if (intersectionIsTypeT) {
                if (road.getIndex() == 1) {
                    continue;
                }
                turnProb = getTurnProbForT(road);
            }
            while (sum(typeProbs) > Util.random.nextDouble()) {
                //Choose vehicle type according to the probability and reduce probability by 1. use proportionalPick(double[] possibilities)
                int type = proportionalPick(typeProbs);
                typeProbs[type] = Math.max(typeProbs[type] - 1, 0);
                //Choose heading
                int heading = proportionalPick(turnProb);
                //Assign appropriate lane
                Lane asignedLane = assignLane(road, type, heading, sim);

                if (asignedLane == null) {
                    continue;
                }
                if (asignedLane.getId() == 3) {
                    int j = -1;
                }
                //assign destination
                Road destination = assignDestination(road, sim.getMap(), heading);
                //Spawn vehicle
                spawnVehicle(VEHICLE_TYPE.values()[type], asignedLane, destination, timeStep, sim);
            }
        }
        for (SpawnPoint spawnPoint : sim.getMap().getSpawnPoints()) {
            spawnPoint.advance(timeStep);
        }
    }

    private static void spawnVehicle(VEHICLE_TYPE type, Lane asignedLane, Road destination, double timeStep, AutoDriverOnlySimulator sim) {

        SpawnPoint spawnPoint = asignedLane.getSpawnPoint();
        SpawnPoint.SpawnSpec spawnSpec = spawnPoint.act(timeStep, type, destination);

        VehicleSimView vehicle = sim.makeVehicle(spawnPoint, spawnSpec);
        VinRegistry.registerVehicle(vehicle); // Get vehicle a VIN number

        sim.vinToVehicles.put(vehicle.getVIN(), vehicle);
        spawnPoint.vehicleGenerated(); // so it knows a platooning vehicle is generated.

        sim.generatedVehicles++; // counter for vehicles generated
    }

    private static int proportionalPick(double[] possibilities) {
        double r = Util.random.nextDouble() * sum(possibilities);
        //if r is bigger than [0]/sum then 0 if r is bigger than [0]+[1]/sum then 1 and so on
        for (int i = 0; i < possibilities.length; i++) {
            if (r < sum(possibilities, 0, i)) {
                return i;
            }
        }
        return -1;
    }

    private static Road assignDestination(Road from, BasicMap layout, int heading) {
        List<Road> destinationRoads = layout.getDestinationRoads();

        if (heading == 1) {//if Straight
            return from;

        } else if (heading == 2) {//if Left
            if (from.getName().equals("1st Street E")) {
                return destinationRoads.get(2);
            } else if (from.getName().equals("1st Street W")) {
                return destinationRoads.get(3);
            } else if (from.getName().equals("1st Avenue N")) {
                return destinationRoads.get(1);
            } else if (from.getName().equals("1st Avenue S")) {
                return destinationRoads.get(0);
            } else {
                throw new RuntimeException("Error in Destination assignment");
            }

        } else if (heading == 0) {//if Right
            if (from.getName().equals("1st Street E")) {
                return destinationRoads.get(3);
            } else if (from.getName().equals("1st Street W")) {
                return destinationRoads.get(2);
            } else if (from.getName().equals("1st Avenue N")) {
                return destinationRoads.get(0);
            } else if (from.getName().equals("1st Avenue S")) {
                return destinationRoads.get(1);
            } else {
                throw new RuntimeException("Error in Destination assignment");
            }
        } else {
            throw new RuntimeException("Error in Destination assignment");
        }
    }

    private static Lane assignLane(Road road, int type, int heading, AutoDriverOnlySimulator sim) {
        List<Lane> lanesInRoad = road.getLanes();
        int[] allowedLanes = designated[type][heading];
        if (intersectionIsTypeT) {
            allowedLanes = TurnPolicies.getPolicy_T(type, heading, road);
        }
        Lane best = null;
        //Find the lane with the shortest queue 
        int bestCarCount = Integer.MAX_VALUE;
        for (int i = 0; i < allowedLanes.length; i++) {
            Lane l = lanesInRoad.get(allowedLanes[i]);
            if (sim.canSpawnVehicle(l.getSpawnPoint()) && l.getNumberOfCarsOnLane(0) < bestCarCount) {
                best = l;
                bestCarCount = l.getNumberOfCarsOnLane(0);
            }
        }
        return best;
    }

    private static double sum(double[] arr, int from, int to) {
        double ans = 0;
        for (int i = from; i <= to; i++) {
            ans += arr[i];
        }
        return ans;
    }

    private static double sum(double[] arr) {
        return sum(arr, 0, arr.length - 1);
    }

    private static void RunVisual() {

        init();
        if (intersectionIsTypeT) {
            TurnPolicies.initPolicyT();
        }
        final String dir = System.getProperty("user.dir") + "/src/main/java/6phases";
        String trafficSignalPhaseFileName = dir + "/Optimized-4way-120.csv";
        if (intersectionIsTypeT) {
            trafficSignalPhaseFileName = dir + "/AIM4Phases-Balanced-T.csv";
        }
        String trafficVolumeFileName = dir + "/AIM4Volumes.csv";
        BasicSimSetup basicSimSetup
                = new BasicSimSetup(1, // columns
                        1, // rows
                        4, // lane width
                        SPEED_LIMIT, // speed limit
                        NUMBER_OF_LANES, // lanes per road
                        1, // median size
                        150, // distance between
                        0, // traffic level
                        // (for now, it can be any number)
                        1.0 // stop distance before intersection
                );

        BasicSimSetup basicSimSetup2 = null;
        // ReservationGridManager.Config fcfsPolicyConfig = null;

        ApproxNPhasesTrafficSignalSimSetup approxNPhasesTrafficSignalSimSetup
                = new ApproxNPhasesTrafficSignalSimSetup(basicSimSetup,
                        trafficSignalPhaseFileName);
        approxNPhasesTrafficSignalSimSetup.setTrafficVolume(trafficVolumeFileName);
        approxNPhasesTrafficSignalSimSetup.setTrafficLevel(0);
        basicSimSetup2 = approxNPhasesTrafficSignalSimSetup;

        Debug.SHOW_VEHICLE_COLOR_BY_MSG_STATE = false;

        V2IPilot.DEFAULT_STOP_DISTANCE_BEFORE_INTERSECTION = 1.0;

        new Viewer(basicSimSetup2, true);

    }

    public enum ARGS {

        NUMBER_OF_LANES(0),
        VEHICLES_LANE_HOUR(1),
        SEED_FOR_RANDOM(2),
        T_INTERSECTION(3),
        T_POLICY_H(4),
        T_POLICY_AV(5),
        H_RIGHT_ALLOWED(6),
        H_STRAIGHT_ALLOWED(7),
        H_LEFT_ALLOWED(8),
        AV_RIGHT_ALLOWED(9),
        AV_STRAIGHT_ALLOWED(10),
        AV_LEFT_ALLOWED(11),
        CC_RIGHT_ALLOWED(12),
        CC_STRAIGHT_ALLOWED(13),
        CC_LEFT_ALLOWED(14),
        ACC_RIGHT_ALLOWED(15),
        ACC_STRAIGHT_ALLOWED(16),
        ACC_LEFT_ALLOWED(17),
        RATIO_AV(18),
        RATIO_CC(19),
        RATIO_ACC(20),
        RATIO_RIGHT(21),
        RATIO_STRAIGHT(22),
        OUT_FILE_NAME(23),
        DROP_MESSAGE_PROB(24),
        DROPPED_MESSAGE_TIME_TO_DETECT(25),
        SCENARIO_INDEX(26),
        MACHINE_NAME(27),
        FREE_FLOW(28),
        ONE_LANE_GREEN(29),
        SAFETY_BUFFER_SECONDS(30),
        SPEED_LIMIT(31);

        private int index;

        //Constructor which will initialize the enum
        ARGS(int i) {
            index = i;
        }

        //method to return the index set by the user which initializing the enum
        public int toint() {
            return index;
        }
    }

    private static int[] parseArray(String str) {
        if (str.length() == 0) {
            return new int[0];
        }
        String[] parts = str.trim().split("\\s+");
        int[] ans = new int[parts.length];
        for (int i = 0; i < ans.length; i++) {
            ans[i] = Integer.parseInt(parts[i]);
        }
        return ans;
    }

    private static void RunExperiment(String[] args) {
//args: 
//0 - int NUMBER_OF_LANES, 1 - int VEHICLES_LANE_HOUR, 2 - int rand seed,
//3 - boolean T_INTERSECTION, 4 - Policy_Type T_POLICY_H, 5 - Policy_Type T_POLICY_AV,
//6 - int[] H_RIGHT_ALLOWED 7 - int[] H_STRAIGHT_ALLOWED, 8 - int[] H_LEFT_ALLOWED
//9 - int[] AV_RIGHT_ALLOWED 10 - int[] AV_STRAIGHT_ALLOWED, 11 - int[] AV_LEFT_ALLOWED
//12 - int[] CC_RIGHT_ALLOWED 13 - int[] CC_STRAIGHT_ALLOWED, 14 - int[] CC_LEFT_ALLOWED
//15 - int[] ACC_RIGHT_ALLOWED 16 - int[] ACC_STRAIGHT_ALLOWED, 17 - int[] ACC_LEFT_ALLOWED
//18 - double ratioAV, 19 - double ratioCC, 20 - double ratioACC, 21 - double ratioRIGHT
//22 - double ratioSTRAIGHT, 23 - String outfile, 24 - double dropMessageProb, 
//25 - double droppedTimeToDetect, 26 - int scenario index, 27 - String machine name, 
//28 - double free flow time, 29 - boolean ONE_LANE_GREEN, 30 - double safety buffer in seconds, 31 - double speed limit

        for (int i = 0; i < args.length; i++) {
            System.out.println(ARGS.values()[i] + " = " + args[i]);
        }

        NUMBER_OF_LANES = Integer.parseInt(args[ARGS.NUMBER_OF_LANES.toint()]);
        VEHICLES_LANE_HOUR = Integer.parseInt(args[ARGS.VEHICLES_LANE_HOUR.toint()]);
        SEED = Integer.parseInt(args[ARGS.SEED_FOR_RANDOM.toint()]);

        TurnPolicies.H_RIGHT_ALLOWED = parseArray(args[ARGS.H_RIGHT_ALLOWED.toint()]);
        TurnPolicies.H_STRAIGHT_ALLOWED = parseArray(args[ARGS.H_STRAIGHT_ALLOWED.toint()]);
        TurnPolicies.H_LEFT_ALLOWED = parseArray(args[ARGS.H_LEFT_ALLOWED.toint()]);

        TurnPolicies.AV_RIGHT_ALLOWED = parseArray(args[ARGS.AV_RIGHT_ALLOWED.toint()]);
        TurnPolicies.AV_STRAIGHT_ALLOWED = parseArray(args[ARGS.AV_STRAIGHT_ALLOWED.toint()]);
        TurnPolicies.AV_LEFT_ALLOWED = parseArray(args[ARGS.AV_LEFT_ALLOWED.toint()]);

        TurnPolicies.CC_RIGHT_ALLOWED = parseArray(args[ARGS.CC_RIGHT_ALLOWED.toint()]);
        TurnPolicies.CC_STRAIGHT_ALLOWED = parseArray(args[ARGS.CC_STRAIGHT_ALLOWED.toint()]);
        TurnPolicies.CC_LEFT_ALLOWED = parseArray(args[ARGS.CC_LEFT_ALLOWED.toint()]);

        TurnPolicies.ACC_RIGHT_ALLOWED = parseArray(args[ARGS.CC_RIGHT_ALLOWED.toint()]);
        TurnPolicies.ACC_STRAIGHT_ALLOWED = parseArray(args[ARGS.CC_STRAIGHT_ALLOWED.toint()]);
        TurnPolicies.ACC_LEFT_ALLOWED = parseArray(args[ARGS.CC_LEFT_ALLOWED.toint()]);

        String isT = args[ARGS.T_INTERSECTION.toint()];
        assert isT.equals("TRUE") || isT.equals("FALSE") : "T_INTERSECTION argument must be \"TRUE\" or \"FALSE\"";
        intersectionIsTypeT = isT.equals("TRUE");

        String isOneLaneGreen = args[ARGS.ONE_LANE_GREEN.toint()];
        assert isOneLaneGreen.equals("TRUE") || isOneLaneGreen.equals("FALSE") : "ONE_LANE_GREEN argument must be \"TRUE\" or \"FALSE\"";
        if (isOneLaneGreen.equals("TRUE")) {
            SimConfig.signalType = SimConfig.SIGNAL_TYPE.ONE_LANE_VERSION;
        } else {
            SimConfig.signalType = SimConfig.SIGNAL_TYPE.TRADITIONAL;
        }

        policyH_T = getPolicyT(args[ARGS.T_POLICY_H.toint()]);
        policyAV_T = getPolicyT(args[ARGS.T_POLICY_AV.toint()]);

        ratioAV = Double.parseDouble(args[ARGS.RATIO_AV.toint()]);
        ratioCC = Double.parseDouble(args[ARGS.RATIO_CC.toint()]);
        ratioACC = Double.parseDouble(args[ARGS.RATIO_ACC.toint()]);

        ratioRIGHT = Double.parseDouble(args[ARGS.RATIO_RIGHT.toint()]);
        ratioSTRAIGHT = Double.parseDouble(args[ARGS.RATIO_STRAIGHT.toint()]);

        if (intersectionIsTypeT) {
            assert ratioRIGHT == 0 : "For T intersection specify ONLY \"RATIO_STRAIGHT\" (RATIO_RIGHT must equal zero)";
            TurnPolicies.initPolicyT();
        }

        String outfile = args[ARGS.OUT_FILE_NAME.toint()] + ".csv";

        dropMessageProb = Double.parseDouble(args[ARGS.DROP_MESSAGE_PROB.toint()]);
        droppedTimeToDetect = Double.parseDouble(args[ARGS.DROPPED_MESSAGE_TIME_TO_DETECT.toint()]);

        double freeFlow = Double.parseDouble(args[ARGS.FREE_FLOW.toint()]);

        int i = Integer.parseInt(args[ARGS.SCENARIO_INDEX.toint()]);
        if (i == -1) {
            VEHICLES_LANE_HOUR = 50;
            args[ARGS.VEHICLES_LANE_HOUR.toint()] = "50";
            ratioAV = 1;
            ratioCC = 0;
            ratioACC = 0;
        }

        SPEED_LIMIT = Double.parseDouble(args[ARGS.SPEED_LIMIT.toint()]);
        SAFETY_BUFFER_SECONDS = Double.parseDouble(args[ARGS.SAFETY_BUFFER_SECONDS.toint()]);

        init();
        //System.out.println("designated = " + DESIGNATED_LANES);

        //System.out.println("args machine name = " + args[ARGS.MACHINE_NAME.toint()]);
        String outfilePath = null;
        if (args[ARGS.MACHINE_NAME.toint()].equals("MAC")) {
            outfilePath = "/Users/guni/Desktop/exp-res/";//Mac laptop
        } else if (args[ARGS.MACHINE_NAME.toint()].equals("DELL")) {
            outfilePath = "/home/guni/Dropbox/results/";//Dell desktop
        } else {
            System.out.println("Unundetified machine name.");
            assert outfilePath != null : "Unundetified machine name.";
        }

        //String outfilePath = "/u/jphanna/aim_results/expr_test/";
        int timeLimit = 3600;
        SimConfig.TOTAL_SIMULATION_TIME = timeLimit;

        String header = "";
        for (int j = 0; j < args.length; j++) {
            header += (ARGS.values()[j] + ",");
        }
        header += "Completed H,Completed SAV,Completed AV,AVG time H,AVG time SAV,AVG time AV,AVG time all,AVG delay H,AVG delay SAV,AVG delay AV,AVG delay all, MAX time H, MAX time CC, MAX time ACC, MAX time AV, Rejections H, Rejections SAV, Rejections AV, Max queue\n";
        //System.out.print(header);
        File f = new File(outfilePath + outfile);
        if (!f.exists()) {

            try {
                Files.write(Paths.get(outfilePath + outfile), header.getBytes(), StandardOpenOption.CREATE);
            } catch (IOException e2) {
                System.out.println("Could not create file " + outfile);
            }
        }

        /**
         * for source files to read
         */
        final String dir = System.getProperty("user.dir") + "/src/main/java/6phases";

        String trafficSignalPhaseFileName = dir + "/Optimized-4way-120.csv";
        if (VEHICLES_LANE_HOUR <= 100) {
            trafficSignalPhaseFileName = dir + "/Optimized-4way-57.csv";
        }
        if (intersectionIsTypeT) {
            trafficSignalPhaseFileName = dir + "/Optimized-3way-120.csv";
            if (VEHICLES_LANE_HOUR <= 100) {
                trafficSignalPhaseFileName = dir + "/Optimized-3way-20.csv";
            }
        }

        String trafficVolumeFileName = "";
        if (SimConfig.DEDICATED_LANES == 0) {
            trafficVolumeFileName = dir + "/AIM4Volumes.csv";
        } else {
            trafficVolumeFileName = dir + "/AIM4BalancedVolumes.csv";
        }

        BasicSimSetup basicSimSetup
                = new BasicSimSetup(1, // columns
                        1, // rows
                        4, // lane width
                        SPEED_LIMIT, // speed limit
                        DesignatedLanesExpr.NUMBER_OF_LANES, // lanes per road
                        1, // median size
                        150, // distance between
                        0, // traffic level
                        // (for now, it can be any number)
                        1.0 // stop distance before intersection
                );

        BasicSimSetup basicSimSetup2 = null;

        ApproxNPhasesTrafficSignalSimSetup approxNPhasesTrafficSignalSimSetup
                = new ApproxNPhasesTrafficSignalSimSetup(basicSimSetup,
                        trafficSignalPhaseFileName);
        approxNPhasesTrafficSignalSimSetup.setTrafficVolume(trafficVolumeFileName);
        approxNPhasesTrafficSignalSimSetup.setTrafficLevel(0);
        basicSimSetup2 = approxNPhasesTrafficSignalSimSetup;

        Debug.SHOW_VEHICLE_COLOR_BY_MSG_STATE = false;

        V2IPilot.DEFAULT_STOP_DISTANCE_BEFORE_INTERSECTION = 1.0;

        /////////////////////////////////
        // Run the simulator
        /////////////////////////////////
        System.out.print("Running simulation");
        Simulator sim = basicSimSetup2.getSimulator();
        // run the simulator
        double nextProgressReport = 50;
        double currentTime = 0.0;
        while (currentTime <= SimConfig.TOTAL_SIMULATION_TIME) {
            Debug.clearShortTermDebugPoints();
            sim.step(SimConfig.TIME_STEP);
            currentTime += SimConfig.TIME_STEP;
            if (currentTime > nextProgressReport) {
                System.out.print('.');
                nextProgressReport += 50;
            }
        }
        /////////////////////////////////
        // Generate data files
        /////////////////////////////////
        System.out.printf("%s: done.\n", TrafficSignalExpr.class);

        double avgTH = -1;
        double avgTSAV = -1;
        double avgTAV = -1;
        double avgRH = -1;
        double avgRSAV = -1;
        double avgRAV = -1;
        double avgDH = -1;
        double avgDSAV = -1;
        double avgDAV = -1;

        if (Htotal > 0) {
            avgTH = HtotalTime / Htotal;
            avgRH = Hrejects / Htotal;
            avgDH = avgTH - freeFlow;
        }
        if (SAVtotal > 0) {
            avgTSAV = SAVtotalTime / SAVtotal;
            avgRSAV = SAVrejects / SAVtotal;
            avgDSAV = avgTSAV - freeFlow;
        }
        if (AVtotal > 0) {
            avgTAV = AVtotalTime / AVtotal;
            avgRAV = AVrejects / AVtotal;
            avgDAV = avgTAV - freeFlow;
        }
        double avgTall = (AVtotalTime + HtotalTime + SAVtotalTime) / (AVtotal + Htotal + SAVtotal);
        double avgDall = avgTall - freeFlow;

        String output = "";
        for (int j = 0; j < args.length; j++) {
            output += (args[j] + ",");
        }
// String header = "args...,Completed H,Completed SAV,Completed AV,AVG time H,
//AVG time SAV,AVG time AV,AVG time all,AVG delay H,AVG delay SAV,AVG delay AV,
//AVG delay all, MAX time H, MAX time CC, MAX time ACC, MAX time AV, Rejections H,
//Rejections SAV, Rejections AV, Max queue\n";
        output
                += Htotal + ","
                + SAVtotal + ","
                + AVtotal + ","
                + avgTH + ","
                + avgTSAV + ","
                + avgTAV + ","
                + avgTall + ","
                + avgDH + ","
                + avgDSAV + ","
                + avgDAV + ","
                + avgDall + ","
                + maxTravelTimeH + ","
                + maxTravelTimeCC + ","
                + maxTravelTimeACC + ","
                + maxTravelTimeAV + ","
                + avgRH + ","
                + avgRSAV + ","
                + avgRAV + ","
                + maxQueueLength + "\n";

        //System.gc();
        System.out.println();
        try {
            Files.write(Paths.get(outfilePath + outfile), output.getBytes(), StandardOpenOption.APPEND);
        } catch (IOException e1) {
            try {
                Files.write(Paths.get(outfilePath + outfile), header.getBytes(), StandardOpenOption.CREATE);
                Files.write(Paths.get(outfilePath + outfile), output.getBytes(), StandardOpenOption.APPEND);
            } catch (IOException e2) {
                System.out.println("Could not create file " + outfile);
            }
        }

    }

    //private static int i = 0;
    public static boolean turnAllowed(int laneIndex, int heading, Road road) { //heading: 0 - Right, 1 - Straight, 2 - Left
        if (intersectionIsTypeT == false) {
            if (heading == 0) {
                return contains(TurnPolicies.H_RIGHT_ALLOWED, laneIndex);
            }
            if (heading == 1) {
                return contains(TurnPolicies.H_STRAIGHT_ALLOWED, laneIndex);
            }
            if (heading == 2) {
                return contains(TurnPolicies.H_LEFT_ALLOWED, laneIndex);
            }
        } else {
//            System.out.println(i++);
//            if(i==115){
//                i++;
//            }
            return contains(TurnPolicies.getPolicy_T(SimConfig.VEHICLE_TYPE.HUMAN.ordinal(), heading, road), laneIndex);
        }
        return false;
    }

//    public static boolean leftAllowed(int index) {
//        return contains(H_LEFT_ALLOWED, index);
//    }
//
//    public static boolean straightAllowed(int index) {
//        return contains(H_STRAIGHT_ALLOWED, index);
//    }
    public static boolean contains(int[] arr, int val) {
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] == val) {
                return true;
            }
        }
        return false;
    }

    private static Road nextRoad(Road currentRoad, int heading) { //heading: 0 - Right, 1 - Straight, 2 - Left
        //checkIfValid();

        if (heading == 0) { //RIGHT
            switch (currentRoad.getIndex()) {
                case 0:
                    return getRoad(2);
                case 1:
                    return getRoad(3);
                case 2:
                    return getRoad(1);
                case 3:
                    return getRoad(0);
                default:
                    assert false : "Can only handle one intersection";
            }
        }
        if (heading == 1) { //STRAIGHT
            return currentRoad;
        }
        if (heading == 2) {
            switch (currentRoad.getIndex()) {
                case 0:
                    return getRoad(3);
                case 1:
                    return getRoad(2);
                case 2:
                    return getRoad(0);
                case 3:
                    return getRoad(1);
                default:
                    assert false : "Can only handle one intersection";
            }
        }
        return null;
    }

    public static List<Lane> possibleDestinationsH(Lane currentLane) {
        List<Lane> ans = new ArrayList<Lane>();
        Road currentRoad = Debug.currentMap.getRoad(currentLane);
        for (int heading = 0; heading < 3; heading++) {
            if (turnAllowed(currentLane.getIndexInRoad(), heading, currentRoad)) {
                Road r = nextRoad(currentRoad, heading);
                ans.add(r.getLanes().get(currentLane.getIndexInRoad()));
            }
        }
        return ans;
    }

    public static void checkIfValid() {
        for (int i = 0; i < Debug.currentMap.getRoads().size(); i++) {
            Road r = getRoad(i);
            assert r.getIndex() == i : "Wrong road index";
            for (int j = 0; j < r.getLanes().size(); j++) {
                Lane l = r.getLanes().get(j);
                assert l.getIndexInRoad() == j : "Wrong lane index in road";
            }
        }
    }

    private static double[] getTurnProbForT(Road road) {
        double[] ans = new double[3];
        switch (road.getIndex()) {
            case 0: //Northbound
                ans[0] = 0.5;
                ans[1] = 0;
                ans[2] = 0.5;
                break;

            case 2: //Eastbound
                ans[0] = 1 - ratioSTRAIGHT;
                ans[1] = ratioSTRAIGHT;
                ans[2] = 0;
                break;

            case 3: //Westbound
                ans[0] = 0;
                ans[1] = ratioSTRAIGHT;
                ans[2] = 1 - ratioSTRAIGHT;
                break;

            default: // Optional
                assert false : "Cannot support more than 3 incoming roads.";
        }

        return ans;
    }

    private static Policy_Type getPolicyT(String policy) {
        if (policy.equals("STRICT")) {
            return Policy_Type.STRICT;
        }
        if (policy.equals("FLEXIBLE")) {
            return Policy_Type.FLEXIBLE;
        }
        if (policy.equals("LIBERAL")) {
            return Policy_Type.LIBERAL;
        }
        if (policy.equals("NULL")) {
            return null;
        }
        assert false : "T intersection policy must be one of the following {STRICT, FLEXIBLE, LIBERAL} or NULL for X intersection";
        return null;
    }

    private static Road getRoad(int index) {
        for (Road r : Debug.currentMap.getRoads()) {
            if (r.getIndex() == index) {
                return r;
            }
        }
        return null;
    }

    private static String[] testArgs() {
        String[] args = new String[24];
        args[0] = "3";//                     #0 - int NUMBER_OF_LANES
        args[1] = "100";//                   #1 - int VEHICLES_LANE_HOUR
        args[2] = "2016";//         #2 - int rand seed

        args[3] = "2";//      #3 - int[] H_RIGHT_ALLOWED
        args[4] = "1";//    #4 - int[] H_STRAIGHT_ALLOWED
        args[5] = "0";//      #5 - int[] H_LEFT_ALLOWED

        args[6] = "1 2";//     #6 - int[] AV_RIGHT_ALLOWED
        args[7] = "0 1 2";//    #7 - int[] AV_STRAIGHT_ALLOWED
        args[8] = "0 1";//       #8 - int[] AV_LEFT_ALLOWED

        args[9] = "";//     #9 - int[] CC_RIGHT_ALLOWED
        args[10] = "";//  #10 - int[] CC_STRAIGHT_ALLOWED
        args[11] = "";//     #11 - int[] CC_LEFT_ALLOWED

        args[12] = "";//   #12 - int[] ACC_RIGHT_ALLOWED
        args[13] = "";// #13 - int[] ACC_STRAIGHT_ALLOWED
        args[14] = "";//    #14 - int[] ACC_LEFT_ALLOWED

        args[15] = "0.1";//                         #15 - double ratioAV
        args[16] = "0";//                         #16 - double ratioCC
        args[17] = "0";//                        #17 - double ratioACC
        args[18] = "0.2";//                      #18 - double ratioRIGHT
        args[19] = "0.6";//                    #19 - double ratioSTRAIGHT
        args[20] = "100-designated";//          #20 - String outfile
        args[21] = "0"; //                                 #21 - double dropMessageProb
        args[22] = "0"; //                                 #22 - double droppedTimeToDetect
        args[23] = "3";
        return args;
    }
}
