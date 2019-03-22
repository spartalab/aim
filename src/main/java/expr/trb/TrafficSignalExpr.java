package expr.trb;

import aim4.config.Debug;
import aim4.config.Platoon;
import aim4.config.SimConfig;
import aim4.config.SimConfig.SIGNAL_TYPE;
import aim4.driver.pilot.V2IPilot;
import aim4.gui.Viewer;
import aim4.map.BasicMap;
import aim4.sim.Simulator;
import aim4.sim.setup.ApproxNPhasesTrafficSignalSimSetup;
import aim4.sim.setup.ApproxStopSignSimSetup;
import aim4.sim.setup.AutoDriverOnlySimSetup;
import aim4.sim.setup.BasicSimSetup;
import aim4.util.Util;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class TrafficSignalExpr {
    
    private enum SIM_TYPE {
        
        FCFS,
        APPROX_TRAFFIC_SIGNAL,
        APPROX_STOP_SIGN
    }
    
    private static boolean GENERATE_BASELINE = false;
    private static boolean SHOW_GUI = true;
    //aim4.map.destination -> TurnBasedDestinationSelector -> selectDestination

    public static int SAVrejects = 0;
    public static int SAVtotal = 0;
    public static double SAVtotalTime = 0;
    public static int AVrejects = 0;
    public static int AVtotal = 0;
    public static double AVtotalTime = 0;
    public static int Hrejects = 0;
    public static int Htotal = 0;
    public static double HtotalTime = 0;
    
    public static double dropMessageProb = 0;
    public static double droppedTimeToDetect = 0;

    /**
     * The main function of this experiment
     *
     * Arguments -> OPTIONS PARAMETERS OPTIONS -> [-d] [-r redPhaseLength] [-o]
     * [-h] [-p] [-nf] PARAMETERS -> trafficLeveL humanPercentage
     * simpleCruiseControlPercentage adaptiveCruiseControlPercentage
     */
    public static void main(String[] args) {

        /////////////////////////////////
        // Settings
        /////////////////////////////////
        Util.resetRand(2016);
        if (args.length < 4 || args[0].endsWith("-help")) {
            System.out.println("Arguments -> OPTIONS PARAMETERS\n"
                    + "OPTIONS -> [-d] [-r redPhaseLength] [-o] [-h] [-p] [-nf]\n"
                    + "-d generate data file instead of showing GUI\n"
                    + "-r set the red phase length manually\n"
                    + "-o one lane version\n"
                    + "-h the traffic signal phases would adapt to the human traffic - the lights are on for the lanes with most number of human drivers\n"
                    + "-p turn on platooning\n"
                    + "-nf not fully observable. The IM knows nothing with the position of human drivers - it knows autonomous and semi-autonomous ones only.\n"
                    + "PARAMETERS -> trafficLeveL humanPercentage simpleCruiseControlPercentage adaptiveCruiseControlPercentage");
            
            return;
        }
        
        System.out.println("YELLOW : fully autonomous vehicles.\n"
                + "GREEN  : simple cruise control vehicles.\n"
                + "BLUE   : adaptive cruise control vehicles.\n"
                + "MAGENTA: human-driven vehicles.");
        
        DesignatedLanesExpr.init();
        
        SimConfig.signalType = SIGNAL_TYPE.TRADITIONAL;
        
        String trafficLevelStr = args[args.length - 4];
        String humanPercentage = args[args.length - 3];
        String simpleCruiseControl = args[args.length - 2];
        String adaptiveCruiseControl = args[args.length - 1];
        String simulationTime = "1800"; // default simulation time

        boolean readRedPhase = false;
        
        for (int i = 0; i < args.length - 4; i++) {
            String flag = args[i];
            
            if (readRedPhase) {
                SimConfig.RED_PHASE_LENGTH = Double.parseDouble(flag);
                readRedPhase = false;
            } else if (flag.equals("-d")) {
                SHOW_GUI = false;
            } else if (flag.equals("-o")) {
                SimConfig.signalType = SIGNAL_TYPE.ONE_LANE_VERSION;
            } else if (flag.equals("-h")) {
                SimConfig.signalType = SIGNAL_TYPE.HUMAN_ADAPTIVE;
            } else if (flag.equals("-nf")) {
                SimConfig.FULLY_OBSERVING = false;
            } else if (flag.equals("-p")) {
                Platoon.platooning = true;
            } else if (flag.equals("-r")) {
                readRedPhase = true;
            }
        }
        
        args = new String[]{"FCFS-SIGNAL-TRADITIONAL", // FIXME need this field no more
            // for "FCFS-SIGNAL-DEBUG", config which to run in following part
            "6phases",
            ".25",
            "0.10",
            "0.25",
            trafficLevelStr, // 5) traffic level
            humanPercentage, // 6) human percentage
            simpleCruiseControl, // 7) constant human drivers percentage
            simulationTime, // 8) simulation time
            adaptiveCruiseControl // 9) adaptive human drivers percentage
    };
        
        if (args[0] == "BASELINE") {
            args[0] = "FCFS";
            GENERATE_BASELINE = true;
        }
        
        SIM_TYPE simType = SIM_TYPE.APPROX_TRAFFIC_SIGNAL;
        
        if (SimConfig.signalType != null && SimConfig.signalType != SimConfig.SIGNAL_TYPE.DEFAULT) {
            SimConfig.HUMAN_PERCENTAGE = Double.parseDouble(args[6]);
            SimConfig.CONSTANT_HUMAN_PERCENTAGE = Double.parseDouble(args[7]);
        } else {
            // in this case, this parameter is not called.
            // just make it safe.
            SimConfig.HUMAN_PERCENTAGE = 0;
        }

//    double staticBufferSize = 0.25;
//    double internalTileTimeBufferSize = 0.1;
//    double edgeTileTimeBufferSize = 0.25;
        double staticBufferSize = Double.parseDouble(args[2]);
        double internalTileTimeBufferSize = Double.parseDouble(args[3]);
        double edgeTileTimeBufferSize = Double.parseDouble(args[4]);
        double trafficLevel = Double.parseDouble(args[5]) * 0.00028;

        /**
         * for source files to read
         */
        final String dir = System.getProperty("user.dir") + "/src/main/java/6phases";
        
        String trafficSignalPhaseFileName = dir + "/AIM4Phases-Balanced.csv";
        
        String trafficVolumeFileName = "";
        if (SimConfig.DEDICATED_LANES == 0) {
            trafficVolumeFileName = dir + "/AIM4Volumes.csv";
        } else {
            trafficVolumeFileName = dir + "/AIM4BalancedVolumes.csv";
        }

        /*
         // when we're doing experiments...
         if (args[8] != "") {
         trafficSignalPhaseFileName = args[1] + "/AIM4Phases_" + args[8] + ".csv";
         }
    
         if (args[9] != "") {
         trafficVolumeFileName = args[1] + "/AIM4Volumes_" + args[9] + ".csv";
         }
         */
        // the number of (simulated) seconds the simulator should run
        SimConfig.TOTAL_SIMULATION_TIME = Double.parseDouble(args[8]); // seconds

        SimConfig.ADAPTIVE_HUMAN_PERCENTAGE = Double.parseDouble(args[9]);

        /*
         * For dedicated lanes exp
         SimConfig.DEDICATED_LANES = Integer.parseInt(args[9]);
         */
        BasicSimSetup basicSimSetup
                = new BasicSimSetup(1, // columns
                        1, // rows
                        4, // lane width
                        25.0, // speed limit
                        DesignatedLanesExpr.NUMBER_OF_LANES, // lanes per road
                        1, // median size
                        150, // distance between
                        0.28, // traffic level
                        // (for now, it can be any number)
                        1.0 // stop distance before intersection
                );
        
        BasicSimSetup basicSimSetup2 = null;
        // ReservationGridManager.Config fcfsPolicyConfig = null;

        switch (simType) {
            case FCFS:
                boolean isEdgeTileTimeBufferEnabled = true;
                double granularity = 1.0;
                
                AutoDriverOnlySimSetup autoDriverOnlySimSetup
                        = new AutoDriverOnlySimSetup(basicSimSetup);
                autoDriverOnlySimSetup.setIsBaseLineMode(GENERATE_BASELINE);
                autoDriverOnlySimSetup.setBuffers(staticBufferSize,
                        internalTileTimeBufferSize,
                        edgeTileTimeBufferSize,
                        isEdgeTileTimeBufferEnabled,
                        granularity);
                autoDriverOnlySimSetup.setTrafficVolume(trafficVolumeFileName);
                autoDriverOnlySimSetup.setTrafficLevel(trafficLevel);
                basicSimSetup2 = autoDriverOnlySimSetup;
                break;
            case APPROX_TRAFFIC_SIGNAL:
                ApproxNPhasesTrafficSignalSimSetup approxNPhasesTrafficSignalSimSetup
                        = new ApproxNPhasesTrafficSignalSimSetup(basicSimSetup,
                                trafficSignalPhaseFileName);
                approxNPhasesTrafficSignalSimSetup.setTrafficVolume(trafficVolumeFileName);
                approxNPhasesTrafficSignalSimSetup.setTrafficLevel(trafficLevel);
                basicSimSetup2 = approxNPhasesTrafficSignalSimSetup;
                
                Debug.SHOW_VEHICLE_COLOR_BY_MSG_STATE = false;
                break;
            case APPROX_STOP_SIGN:
                ApproxStopSignSimSetup approxStopSignSimSetup
                        = new ApproxStopSignSimSetup(basicSimSetup);
                //approxStopSignSimSetup.setTrafficVolume(trafficVolumeFileName);
                approxStopSignSimSetup.setTrafficLevel(trafficLevel);
                SimConfig.MUST_STOP_BEFORE_INTERSECTION = true;
                Debug.SHOW_VEHICLE_COLOR_BY_MSG_STATE = false;
                basicSimSetup2 = approxStopSignSimSetup;
                break;
        }
        
        V2IPilot.DEFAULT_STOP_DISTANCE_BEFORE_INTERSECTION = 1.0;

        /////////////////////////////////
        // Instantiate the simulator
        /////////////////////////////////
//    AutoDriverOnlySimSetup autoDriverOnlySimSetup =
//        new AutoDriverOnlySimSetup(basicSimSetup);
        /////////////////////////////////
        // Run the simulator
        /////////////////////////////////
        if (SHOW_GUI) {
            new Viewer(basicSimSetup2, true);
        } else {
            // get the simulator
            //System.out.println(System.getProperty("user.dir"));
            Simulator sim = basicSimSetup2.getSimulator();
            // run the simulator
            double currentTime = 0.0;
            while (currentTime <= SimConfig.TOTAL_SIMULATION_TIME) {
                Debug.clearShortTermDebugPoints();
                sim.step(SimConfig.TIME_STEP);
                currentTime += SimConfig.TIME_STEP;
            }

            /*
             // Print how many vehicles are inhibited
             System.out.println("Prohibited: " + sim.getProhibitedVehiclesNum());
             System.out.println("Generated: " + sim.getGeneratedVehiclesNum());
             System.out.println("ETL: " + 1.0 * sim.getGeneratedVehiclesNum() / 12 / SimConfig.TOTAL_SIMULATION_TIME);
             System.out.println("TTL: " 
             + 1.0 * (sim.getGeneratedVehiclesNum() + sim.getProhibitedVehiclesNum()) / 12 / SimConfig.TOTAL_SIMULATION_TIME);
             */
            /////////////////////////////////
            // Generate data files
            /////////////////////////////////
            BasicMap map = sim.getMap();

            // output the collected data of DCL
            String dclOutFileName
                    = "Output/ts_dcl_" + trafficLevel + "_" + humanPercentage + "_" + simpleCruiseControl + "_" + adaptiveCruiseControl + ".csv";
            map.printDataCollectionLinesData(dclOutFileName);
            
            System.out.printf("%s: done.\n", TrafficSignalExpr.class);
        }
        
    }
    
    public static void main2(String[] args) {
        
        String outfile = "results_SEMI.csv";
        String outfilePath = "/Users/guni/Desktop/exp-res/";//Mac laptop
        //String outfilePath = "C:\\Users\\user\\Desktop\\experiments\\Delta-tolling\\";//Dell leptop
        //String outfilePath = "/home/guni/Desktop/Experiments/Tolling/";//Dell desktop
        //String outfilePath = "/u/jphanna/aim_results/expr_test/";
        int timeLimit = 1800;
        int numOfLanes = 4;
        int instances = 5;
        SimConfig.TOTAL_SIMULATION_TIME = timeLimit;
        
        String header = "%Configuration,H,%CC,%ACC,%A,lanes,Cars/houre,Cars completed,AVG time,Time STD\n";
        System.out.print(header);

        //double[] H = {1, 0.95, 0.9, 0.8, 0.6, 0.35, 0.2, 0.1, 0.05, 0, 0, 0};
        //double[] H = {0,0,0,0,0,0,0,0,0,0,0,0};
        //double[] H = {1, 0.9, 0.8, 0.7, 0.6, 0.5, 0.4, 0.3, 0.2, 0.1, 0};
        //double[] H = {1, 0.9, 0.8, 0.7, 0.6, 0.5, 0.4, 0.3, 0.2, 0.1, 0,0,0,0,0,0,0,0,0,0,0};
        double[] H = {1, 0.9, 0.8, 0.7, 0.6, 0.5, 0.4, 0.3, 0.2, 0.1, 0};

        //double[] CC = {0, 0.05, 0.1, 0.2, 0.4, 0.6, 0.7, 0.7, 0.55, 0.4, 0.2, 0};
        //double[] CC = {0, 0.9, 0.8, 0.7, 0.6, 0.5, 0.4, 0.3, 0.2, 0.1, 0}; 
        //double[] CC = {0, 0.1,0.2,0.3,0.4,0.5,0.6,0.7,0.8,0.9,1};
        //double[] CC = {0, 0.1,0.2,0.3,0.4,0.5,0.6,0.7,0.8,0.9,1, 0.9, 0.8, 0.7, 0.6, 0.5, 0.4, 0.3, 0.2, 0.1, 0};
        double[] CC = {0, 0.1, 0.2, 0.3, 0.4, 0.5, 0.4, 0.3, 0.2, 0.1, 0};
        //double[] CC = {0,0,0,0,0,0,0,0,0,0,0,0};

        double[] ACC = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        //double[] ACC = {1, 0.9, 0.8, 0.7, 0.6, 0.5, 0.4, 0.3, 0.2, 0.1, 0};
        //double[] ACC = {0, 0.1,0.2,0.3,0.4,0.5,0.6,0.7,0.8,0.9,1};
        double[] numOfCars = {360};
        
        SHOW_GUI = false;
        SimConfig.signalType = SIGNAL_TYPE.TRADITIONAL;
        //SimConfig.signalType = SIGNAL_TYPE.HUMAN_ADAPTIVE;
        //SimConfig.signalType = SIGNAL_TYPE.TRADITIONAL;
        SimConfig.FULLY_OBSERVING = true;
        Platoon.platooning = false;
        SimConfig.RED_PHASE_LENGTH = 0.0;
        int j = -1;
        
        for (double noc : numOfCars) {
            System.out.println("Running with traffic = " + noc);
            for (int i = j; i < H.length; i++) {
                j = 0;
                System.out.println("Running configuration =" + i);
                
                Util.resetRand(2016);
                boolean readRedPhase = false;
                
                SIM_TYPE simType = SIM_TYPE.APPROX_TRAFFIC_SIGNAL;
                
                if (SimConfig.signalType != null && SimConfig.signalType != SimConfig.SIGNAL_TYPE.DEFAULT) {
                    if (i == -1) {
                        SimConfig.HUMAN_PERCENTAGE = 0;
                        SimConfig.CONSTANT_HUMAN_PERCENTAGE = 0;
                    } else {
                        SimConfig.HUMAN_PERCENTAGE = H[i];
                        SimConfig.CONSTANT_HUMAN_PERCENTAGE = CC[i];
                    }
                } else {
                    // in this case, this parameter is not called.
                    // just make it safe.
                    SimConfig.HUMAN_PERCENTAGE = 0;
                }

//    double staticBufferSize = 0.25;
//    double internalTileTimeBufferSize = 0.1;
//    double edgeTileTimeBufferSize = 0.25;
                for (int t = 0; t < instances; t++) {
                    double staticBufferSize = 0.25;
                    double internalTileTimeBufferSize = 0.1;
                    double edgeTileTimeBufferSize = 0.25;
                    double trafficLevel = noc * 0.00028;
                    if (i == -1) {
                        trafficLevel = 0.02;
                        t = instances;
                    }

                    /**
                     * for source files to read
                     */
                    final String dir = System.getProperty("user.dir") + "/src/main/java/6phases";
                    
                    String trafficSignalPhaseFileName = dir + "/AIM4Phases-Balanced.csv";
                    
                    String trafficVolumeFileName = "";
                    if (SimConfig.DEDICATED_LANES == 0) {
                        trafficVolumeFileName = dir + "/AIM4Volumes.csv";
                    } else {
                        trafficVolumeFileName = dir + "/AIM4BalancedVolumes.csv";
                    }

                    /*
                     // when we're doing experiments...
                     if (args[8] != "") {
                     trafficSignalPhaseFileName = args[1] + "/AIM4Phases_" + args[8] + ".csv";
                     }
    
                     if (args[9] != "") {
                     trafficVolumeFileName = args[1] + "/AIM4Volumes_" + args[9] + ".csv";
                     }
                     */
                    // the number of (simulated) seconds the simulator should run
                    SimConfig.TOTAL_SIMULATION_TIME = timeLimit; // seconds

                    if (i == -1) {
                        SimConfig.ADAPTIVE_HUMAN_PERCENTAGE = 0;
                    } else {
                        SimConfig.ADAPTIVE_HUMAN_PERCENTAGE = ACC[i];
                    }

                    /*
                     * For dedicated lanes exp
                     SimConfig.DEDICATED_LANES = Integer.parseInt(args[9]);
                     */
                    BasicSimSetup basicSimSetup
                            = new BasicSimSetup(1, // columns
                                    1, // rows
                                    numOfLanes, // lane width
                                    25.0, // speed limit
                                    3, // lanes per road
                                    1, // median size
                                    150, // distance between
                                    trafficLevel, // traffic level
                                    // (for now, it can be any number)
                                    1.0 // stop distance before intersection
                            );
                    
                    BasicSimSetup basicSimSetup2 = null;
                    // ReservationGridManager.Config fcfsPolicyConfig = null;

                    switch (simType) {
                        case FCFS:
                            boolean isEdgeTileTimeBufferEnabled = true;
                            double granularity = 1.0;
                            
                            AutoDriverOnlySimSetup autoDriverOnlySimSetup
                                    = new AutoDriverOnlySimSetup(basicSimSetup);
                            autoDriverOnlySimSetup.setIsBaseLineMode(GENERATE_BASELINE);
                            autoDriverOnlySimSetup.setBuffers(staticBufferSize,
                                    internalTileTimeBufferSize,
                                    edgeTileTimeBufferSize,
                                    isEdgeTileTimeBufferEnabled,
                                    granularity);
                            autoDriverOnlySimSetup.setTrafficVolume(trafficVolumeFileName);
                            autoDriverOnlySimSetup.setTrafficLevel(trafficLevel);
                            basicSimSetup2 = autoDriverOnlySimSetup;
                            break;
                        case APPROX_TRAFFIC_SIGNAL:
                            ApproxNPhasesTrafficSignalSimSetup approxNPhasesTrafficSignalSimSetup
                                    = new ApproxNPhasesTrafficSignalSimSetup(basicSimSetup,
                                            trafficSignalPhaseFileName);
                            approxNPhasesTrafficSignalSimSetup.setTrafficVolume(trafficVolumeFileName);
                            approxNPhasesTrafficSignalSimSetup.setTrafficLevel(trafficLevel);
                            basicSimSetup2 = approxNPhasesTrafficSignalSimSetup;
                            
                            Debug.SHOW_VEHICLE_COLOR_BY_MSG_STATE = false;
                            break;
                        case APPROX_STOP_SIGN:
                            ApproxStopSignSimSetup approxStopSignSimSetup
                                    = new ApproxStopSignSimSetup(basicSimSetup);
                            //approxStopSignSimSetup.setTrafficVolume(trafficVolumeFileName);
                            approxStopSignSimSetup.setTrafficLevel(trafficLevel);
                            SimConfig.MUST_STOP_BEFORE_INTERSECTION = true;
                            Debug.SHOW_VEHICLE_COLOR_BY_MSG_STATE = false;
                            basicSimSetup2 = approxStopSignSimSetup;
                            break;
                    }
                    
                    V2IPilot.DEFAULT_STOP_DISTANCE_BEFORE_INTERSECTION = 1.0;

                    /////////////////////////////////
                    // Instantiate the simulator
                    /////////////////////////////////
//    AutoDriverOnlySimSetup autoDriverOnlySimSetup =
//        new AutoDriverOnlySimSetup(basicSimSetup);
                    /////////////////////////////////
                    // Run the simulator
                    /////////////////////////////////
                    if (SHOW_GUI) {
                        new Viewer(basicSimSetup2, true);
                    } else {
                        // get the simulator
                        //System.out.println(System.getProperty("user.dir"));
                        Simulator sim = basicSimSetup2.getSimulator();
                        // run the simulator
                        double currentTime = 0.0;
                        while (currentTime <= SimConfig.TOTAL_SIMULATION_TIME) {
                            Debug.clearShortTermDebugPoints();
                            sim.step(SimConfig.TIME_STEP);
                            currentTime += SimConfig.TIME_STEP;
                        }

                        /*
                         // Print how many vehicles are inhibited
                         System.out.println("Prohibited: " + sim.getProhibitedVehiclesNum());
                         System.out.println("Generated: " + sim.getGeneratedVehiclesNum());
                         System.out.println("ETL: " + 1.0 * sim.getGeneratedVehiclesNum() / 12 / SimConfig.TOTAL_SIMULATION_TIME);
                         System.out.println("TTL: " 
                         + 1.0 * (sim.getGeneratedVehiclesNum() + sim.getProhibitedVehiclesNum()) / 12 / SimConfig.TOTAL_SIMULATION_TIME);
                         */
                        /////////////////////////////////
                        // Generate data files
                        /////////////////////////////////
                        BasicMap map = sim.getMap();

                        // output the collected data of DCL
//                        String dclOutFileName
//                                = "Output/ts_dcl_" + trafficLevel + "_" + H[i] + "_" + CC[i] + "_" + ACC[i] + ".csv";
//                        map.printDataCollectionLinesData(dclOutFileName);
                        System.out.printf("%s: done.\n", TrafficSignalExpr.class);
                        
                        double A;
                        double Hi;
                        double CCi;
                        double ACCi;
                        if (i == -1) {
                            A = 1;
                            Hi = 0;
                            CCi = 0;
                            ACCi = 0;
                        } else {
                            A = 1.0 - (H[i] + CC[i] + ACC[i]);
                            Hi = H[i];
                            CCi = CC[i];
                            ACCi = ACC[i];
                        }
//                  String header = "%H,%CC,%ACC,%A,lanes,Cars/houre,Cars completed,AVG time,Time STD\n";
                        String output = i + "," + Hi + "," + CCi
                                + "," + ACCi + "," + A
                                + "," + numOfLanes
                                + "," + noc + ","
                                + sim.getNumCompletedVehicles() + ","
                                + sim.getAvgTravelTime()
                                + "," + sim.getTimeSTD() + "\n";

                        //System.gc();
                        System.out.print(output);
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
                }
            }
            
        }
        System.out.println("All Done");
        
    }
    
    public static void main3(String[] args) {
        
        for (int i = 0; i < args.length; i++) {
            System.out.println("args[" + i + "] = " + args[i]);
        }
        
        double H = Double.parseDouble(args[0]);
        double CC = Double.parseDouble(args[1]);
        double ACC = Double.parseDouble(args[2]);
        double noc = Double.parseDouble(args[3]);
        DesignatedLanesExpr.NUMBER_OF_LANES = Integer.parseInt(args[4]);
        int i = Integer.parseInt(args[5]);
        if (i == -1) {
            noc = 60;
        }
        dropMessageProb = Double.parseDouble(args[6]);
        droppedTimeToDetect = Double.parseDouble(args[7]);
        Util.resetRand(Integer.parseInt(args[8]));
        //DesignatedLanesExpr.DESIGNATED_LANES = Integer.parseInt(args[9]);
        //DesignatedLanesExpr.ALLOW_STRIGHT_ON_RIGHT = args[10].equals("1");
        String outfile = args[11] + ".csv";
        //System.out.println("designated = " + DESIGNATED_LANES);
        
        //String outfilePath = "/Users/guni/Desktop/exp-res/";//Mac laptop
        //String outfilePath = "C:\\Users\\user\\Desktop\\experiments\\Delta-tolling\\";//Dell leptop
        String outfilePath = "/home/guni/Desktop/Experiments/Tolling/";//Dell desktop
        //String outfilePath = "/u/jphanna/aim_results/expr_test/";
        int timeLimit = 1800;
        SimConfig.TOTAL_SIMULATION_TIME = timeLimit;
        
        String header = "Configuration,Drop Prob, Drop Timeout,Lanes,%H,%CC,%ACC,%A,Cars/hour,Completed H,Completed SAV,Completed AV,AVG time H,AVG time SAV,AVG time AV, Rejections H, Rejections SAV, Rejections AV\n";
        //System.out.print(header);
        File f = new File(outfilePath + outfile);
        if (!f.exists()) {
            
            try {
                Files.write(Paths.get(outfilePath + outfile), header.getBytes(), StandardOpenOption.CREATE);
            } catch (IOException e2) {
                System.out.println("Could not create file " + outfile);
            }
        }
        
        SHOW_GUI = false;
        SimConfig.signalType = SIGNAL_TYPE.TRADITIONAL;
        //SimConfig.signalType = SIGNAL_TYPE.HUMAN_ADAPTIVE;
        //SimConfig.signalType = SIGNAL_TYPE.TRADITIONAL;
        SimConfig.FULLY_OBSERVING = true;
        Platoon.platooning = false;
        SimConfig.RED_PHASE_LENGTH = 0.0;

        //System.out.println("Running with traffic = " + noc);
        //System.out.println("Running configuration =" + i);
        boolean readRedPhase = false;
        
        SIM_TYPE simType = SIM_TYPE.APPROX_TRAFFIC_SIGNAL;
        
        if (SimConfig.signalType != null && SimConfig.signalType != SimConfig.SIGNAL_TYPE.DEFAULT) {
            
            SimConfig.HUMAN_PERCENTAGE = H;
            SimConfig.CONSTANT_HUMAN_PERCENTAGE = CC;
            
        } else {
            // in this case, this parameter is not called.
            // just make it safe.
            SimConfig.HUMAN_PERCENTAGE = 0;
        }

//    double staticBufferSize = 0.25;
//    double internalTileTimeBufferSize = 0.1;
//    double edgeTileTimeBufferSize = 0.25;
        double staticBufferSize = 0.25;
        double internalTileTimeBufferSize = 0.1;
        double edgeTileTimeBufferSize = 0.25;
        double trafficLevel = noc * 0.00028;

        /**
         * for source files to read
         */
        final String dir = System.getProperty("user.dir") + "/src/main/java/6phases";
        
        String trafficSignalPhaseFileName = dir + "/AIM4Phases-Balanced.csv";
        
        String trafficVolumeFileName = "";
        if (SimConfig.DEDICATED_LANES == 0) {
            trafficVolumeFileName = dir + "/AIM4Volumes.csv";
        } else {
            trafficVolumeFileName = dir + "/AIM4BalancedVolumes.csv";
        }

        /*
         // when we're doing experiments...
         if (args[8] != "") {
         trafficSignalPhaseFileName = args[1] + "/AIM4Phases_" + args[8] + ".csv";
         }
    
         if (args[9] != "") {
         trafficVolumeFileName = args[1] + "/AIM4Volumes_" + args[9] + ".csv";
         }
         */
        // the number of (simulated) seconds the simulator should run
        SimConfig.TOTAL_SIMULATION_TIME = timeLimit; // seconds

        SimConfig.ADAPTIVE_HUMAN_PERCENTAGE = ACC;


        /*
         * For dedicated lanes exp
         SimConfig.DEDICATED_LANES = Integer.parseInt(args[9]);
         */
        BasicSimSetup basicSimSetup
                = new BasicSimSetup(1, // columns
                        1, // rows
                        4, // lane width
                        25.0, // speed limit
                        DesignatedLanesExpr.NUMBER_OF_LANES, // lanes per road
                        1, // median size
                        150, // distance between
                        trafficLevel, // traffic level
                        // (for now, it can be any number)
                        1.0 // stop distance before intersection
                );
        
        BasicSimSetup basicSimSetup2 = null;
        // ReservationGridManager.Config fcfsPolicyConfig = null;

        switch (simType) {
            case FCFS:
                boolean isEdgeTileTimeBufferEnabled = true;
                double granularity = 1.0;
                
                AutoDriverOnlySimSetup autoDriverOnlySimSetup
                        = new AutoDriverOnlySimSetup(basicSimSetup);
                autoDriverOnlySimSetup.setIsBaseLineMode(GENERATE_BASELINE);
                autoDriverOnlySimSetup.setBuffers(staticBufferSize,
                        internalTileTimeBufferSize,
                        edgeTileTimeBufferSize,
                        isEdgeTileTimeBufferEnabled,
                        granularity);
                autoDriverOnlySimSetup.setTrafficVolume(trafficVolumeFileName);
                autoDriverOnlySimSetup.setTrafficLevel(trafficLevel);
                basicSimSetup2 = autoDriverOnlySimSetup;
                break;
            case APPROX_TRAFFIC_SIGNAL:
                ApproxNPhasesTrafficSignalSimSetup approxNPhasesTrafficSignalSimSetup
                        = new ApproxNPhasesTrafficSignalSimSetup(basicSimSetup,
                                trafficSignalPhaseFileName);
                approxNPhasesTrafficSignalSimSetup.setTrafficVolume(trafficVolumeFileName);
                approxNPhasesTrafficSignalSimSetup.setTrafficLevel(trafficLevel);
                basicSimSetup2 = approxNPhasesTrafficSignalSimSetup;
                
                Debug.SHOW_VEHICLE_COLOR_BY_MSG_STATE = false;
                break;
            case APPROX_STOP_SIGN:
                ApproxStopSignSimSetup approxStopSignSimSetup
                        = new ApproxStopSignSimSetup(basicSimSetup);
                //approxStopSignSimSetup.setTrafficVolume(trafficVolumeFileName);
                approxStopSignSimSetup.setTrafficLevel(trafficLevel);
                SimConfig.MUST_STOP_BEFORE_INTERSECTION = true;
                Debug.SHOW_VEHICLE_COLOR_BY_MSG_STATE = false;
                basicSimSetup2 = approxStopSignSimSetup;
                break;
        }
        
        V2IPilot.DEFAULT_STOP_DISTANCE_BEFORE_INTERSECTION = 1.0;

        /////////////////////////////////
        // Instantiate the simulator
        /////////////////////////////////
//    AutoDriverOnlySimSetup autoDriverOnlySimSetup =
//        new AutoDriverOnlySimSetup(basicSimSetup);
        /////////////////////////////////
        // Run the simulator
        /////////////////////////////////
        if (SHOW_GUI) {
            new Viewer(basicSimSetup2, true);
        } else {
            System.out.print("Running simulation");
            // get the simulator
            //System.out.println(System.getProperty("user.dir"));
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

            /*
             // Print how many vehicles are inhibited
             System.out.println("Prohibited: " + sim.getProhibitedVehiclesNum());
             System.out.println("Generated: " + sim.getGeneratedVehiclesNum());
             System.out.println("ETL: " + 1.0 * sim.getGeneratedVehiclesNum() / 12 / SimConfig.TOTAL_SIMULATION_TIME);
             System.out.println("TTL: " 
             + 1.0 * (sim.getGeneratedVehiclesNum() + sim.getProhibitedVehiclesNum()) / 12 / SimConfig.TOTAL_SIMULATION_TIME);
             */
            /////////////////////////////////
            // Generate data files
            /////////////////////////////////
            BasicMap map = sim.getMap();

            // output the collected data of DCL
//                        String dclOutFileName
//                                = "Output/ts_dcl_" + trafficLevel + "_" + H[i] + "_" + CC[i] + "_" + ACC[i] + ".csv";
//                        map.printDataCollectionLinesData(dclOutFileName);
            System.out.printf("%s: done.\n", TrafficSignalExpr.class);
            
            double A = 1.0 - (H + CC + ACC);
            
            double avgTH = -1;
            double avgTSAV = -1;
            double avgTAV = -1;
            double avgRH = -1;
            double avgRSAV = -1;
            double avgRAV = -1;
            
            if (Htotal > 0) {
                avgTH = HtotalTime / Htotal;
                avgRH = Hrejects / Htotal;
            }
            if (SAVtotal > 0) {
                avgTSAV = SAVtotalTime / SAVtotal;
                avgRSAV = SAVrejects / SAVtotal;
            }
            if (AVtotal > 0) {
                avgTAV = AVtotalTime / AVtotal;
                avgRAV = AVrejects / AVtotal;
            }

//                  String header = "Configuration,Lanes,%H,%CC,%ACC,%A,Cars/houre,Completed H,Completed SAV,Completed AV,AVG time H,AVG time SAV,AVG time AV, Rejections H, Rejections SAV, Rejections AV\n";
            String output = i + "," + dropMessageProb
                    + "," + droppedTimeToDetect + ","
                    + DesignatedLanesExpr.NUMBER_OF_LANES + "," + H + "," + CC
                    + "," + ACC + "," + A
                    + "," + noc + ","
                    + Htotal + ","
                    + SAVtotal + ","
                    + AVtotal + ","
                    + avgTH + ","
                    + avgTSAV + ","
                    + avgTAV + ","
                    + avgRH + ","
                    + avgRSAV + ","
                    + avgRAV + "\n";

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
        
    }
    
}
