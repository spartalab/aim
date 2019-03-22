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
package aim4.im.v2i.RequestHandler;

import aim4.config.Debug;
import aim4.config.Resources;
import aim4.config.SimConfig;
import aim4.config.TrafficSignal;
import aim4.config.SimConfig.SIGNAL_TYPE;
import aim4.driver.Driver;
import aim4.driver.coordinator.V2ICoordinator.State;

import java.util.List;

import aim4.im.v2i.policy.BasePolicy;
import aim4.im.v2i.policy.BasePolicyCallback;
import aim4.im.v2i.policy.BasePolicy.ProposalFilterResult;
import aim4.im.v2i.policy.BasePolicy.ReserveParam;
import aim4.map.GridMapUtil;
import aim4.map.Road;
import aim4.map.lane.Lane;
import aim4.msg.i2v.Reject;
import aim4.msg.v2i.Request;
import aim4.sim.StatCollector;
import aim4.util.Registry;
import aim4.vehicle.VehicleSimView;
import expr.trb.DesignatedLanesExpr;
import static expr.trb.DesignatedLanesExpr.designated;
import expr.trb.TrafficSignalExpr;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * The approximate N-Phases traffic signal request handler.
 */
public class ApproxNPhasesTrafficSignalRequestHandler implements
        TrafficSignalRequestHandler {

    /////////////////////////////////
    // NESTED CLASSES
    /////////////////////////////////
    /**
     * The interface of signal controllers.
     */
    public static interface SignalController {

        /**
         * Get the signal at the given time
         *
         * @param time the given time
         * @return the signal
         */
        TrafficSignal getSignal(double time);

        /**
         * Set the offset
         *
         * @param time
         */
        void setOffset(double time);
    }

    /**
     * The cyclic signal controller.
     */
    public static class CyclicSignalController implements SignalController {

        /**
         * The durations of the signals
         */
        private double[] durations;
        /**
         * The list of signals
         */
        private TrafficSignal[] signals;
        /**
         * The duration offset
         */
        private static double durationOffset;
        /**
         * The total duration
         */
        private static double totalDuration;

        public CyclicSignalController(double[] durations, TrafficSignal[] signals) {
            this(durations, signals, 0.0);
        }

        /**
         * Create a cyclic signal controller.
         *
         * @param durations the durations of the signals
         * @param signals the list of signals
         * @param durationOffset the duration offset
         */
        public CyclicSignalController(double[] durations, TrafficSignal[] signals,
                double durationOffset) {
            this.durations = durations.clone();
            this.signals = signals.clone();
            this.durationOffset = durationOffset;

            totalDuration = 0.0;
            for (double d : durations) {
                totalDuration += d;
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public TrafficSignal getSignal(double time) {
            time -= durationOffset;
            double d = time % totalDuration;
            assert 0.0 <= d && d < totalDuration;
            double maxd = 0.0;
            for (int i = 0; i < durations.length; i++) {
                maxd += durations[i];
                if (d < maxd) {
                    return signals[i];
                }
            }
            assert false : ("Error in CyclicLightController()");
            return null;
        }

        /**
         * Return whether the current time exceeds this total duration. If so,
         * it needs to re-calculate the red phase time.
         *
         * @param currentTime the current time
         * @return
         */
        public static boolean needRecalculate(double currentTime) {
            if (currentTime > durationOffset + totalDuration) {
                return true;
            } else {
                return false;
            }
        }

        /**
         * A new round would calculate from this end time - this is the offset
         * of next round
         *
         * @return the time of end of this round
         */
        public static double getEndTime() {
            return durationOffset + totalDuration;
        }

        @Override
        public void setOffset(double time) {
            durationOffset = time;
        }
    }

    /**
     * The class that green signal is on one lane by one. This is leaving enough
     * space for autonomous vehicles to pass thorugh the intersection because of
     * enough space left for them.
     *
     * @author menie
     *
     */
    public static class OneLaneSignalController implements SignalController {

        /**
         * information necessary for this controller
         */
        private double greenTime;
        private double redTime;
        private double totalTime;
        private int laneNum;
        private int id;

        public OneLaneSignalController(int id, double greenTime, double redTime) {
            this.id = id;
            this.greenTime = greenTime;
            this.redTime = redTime;
            this.totalTime = greenTime + redTime;

            this.laneNum = Resources.map.getLaneRegistry().getValues().size();
        }

        @Override
        public TrafficSignal getSignal(double time) {
            if (Math.ceil(time / totalTime) % laneNum == this.id) {
                if (time % this.totalTime < greenTime) {
                    return TrafficSignal.GREEN;
                } else {
                    return TrafficSignal.RED;
                }
            } else {
                return TrafficSignal.RED;
            }
        }

        @Override
        public void setOffset(double time) {
            // TODO Auto-generated method stub

        }

    }

    /**
     * For revised phase signal policy. 1. two opposing directions without
     * turning left ones 2. turning left ones
     *
     * @author menie
     *
     */
    public static class RevisedPhaseSignalController implements SignalController {

        /**
         * Some useful variables
         */
        private double greenDuration[];
        private double totalTime;

        public RevisedPhaseSignalController(double[] greenDuration, double totalTime) {
            this.greenDuration = greenDuration;
            this.totalTime = totalTime;
        }

        @Override
        public TrafficSignal getSignal(double time) {
            double localTime = time % totalTime;

            if (localTime >= greenDuration[0] && localTime < greenDuration[1]) {
                return TrafficSignal.GREEN;
            } else {
                return TrafficSignal.RED;
            }
        }

        @Override
        public void setOffset(double time) {
            // TODO Auto-generated method stub

        }

    }

    public static class AdaptiveSignalController implements SignalController {

        private ArrayList<ArrayList<Double>> greenPhaseDuration = new ArrayList<ArrayList<Double>>();

        @Override
        public TrafficSignal getSignal(double time) {
            for (ArrayList<Double> duration : greenPhaseDuration) {
                if (duration.get(0) < time && time <= duration.get(1)) {
                    return TrafficSignal.GREEN;
                }
            }

            // otherwise, it's not a green phase
            return TrafficSignal.RED;
        }

        public void prepareGreenPhase(double start, double end) {
            ArrayList<Double> duration = new ArrayList<Double>();
            duration.add(start);
            duration.add(end);

            greenPhaseDuration.add(duration);
        }

        @Override
        public void setOffset(double time) {
            // TODO Auto-generated method stub

        }

    }

    public static class DedicatedLanesSignalController implements SignalController {

        private double greenTime = 15;
        private double redIntervalTime = 2;
        private double redTime = 15;
        private double totalTime;
        private int rank; // No. ? to turn on green light
        private boolean forHuman;

        public DedicatedLanesSignalController(int lane) {
            int laneNum = Resources.map.getLaneRegistry().getValues().size();
            rank = lane / (laneNum / 4);
            forHuman = (lane % (laneNum / 4)) < SimConfig.DEDICATED_LANES;

            totalTime = greenTime * 8 + redIntervalTime * 3 + redTime;
        }

        @Override
        public TrafficSignal getSignal(double time) {
            double timeInPeriod = time % totalTime;

            if (SimConfig.DEDICATED_LANES > 0) {
                // check whether need to change signal time
                if (timeInPeriod < greenTime * 8 + redIntervalTime * 3) {
                    SimConfig.signalType = SIGNAL_TYPE.DEFAULT;
                } else {
                    SimConfig.signalType = SIGNAL_TYPE.TRADITIONAL;
                }
            }

            // after 4 green phases, or it's not a human lane, return red phase
            if (timeInPeriod > greenTime * 8 + redIntervalTime * 3) {
                return TrafficSignal.RED;
            } else {
                int round = (int) (timeInPeriod / (greenTime * 2 + redIntervalTime));
                double timeInRound = timeInPeriod % (greenTime * 2 + redIntervalTime);

                if (round == rank) {
                    if (timeInRound < greenTime && forHuman) {
                        return TrafficSignal.GREEN;
                    } else if (timeInRound > greenTime && timeInRound < greenTime * 2 && !forHuman) {
                        return TrafficSignal.GREEN;
                    } else {
                        return TrafficSignal.RED;
                    }
                } else {
                    return TrafficSignal.RED;
                }
            }
        }

        @Override
        public void setOffset(double time) {
            // TODO Auto-generated method stub

        }

    }
    /////////////////////////////////
    // PRIVATE FIELDS
    /////////////////////////////////

    /**
     * A mapping from lane ID to the traffic signal controllers on the lane.
     */
    private Map<Integer, SignalController> signalControllers;
    /**
     * The base policy
     */
    private BasePolicyCallback basePolicy;

    /////////////////////////////////
    // CONSTRUCTORS
    /////////////////////////////////
    /**
     * Create the approximate N-Phases traffic signal request handler.
     */
    public ApproxNPhasesTrafficSignalRequestHandler() {
        signalControllers = new HashMap<Integer, SignalController>();
        Resources.signalControllers = signalControllers;
    }

    /////////////////////////////////
    // PUBLIC METHODS
    /////////////////////////////////
    /**
     * {@inheritDoc}
     */
    @Override
    public void act(double timeStep) {
        // do nothing
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setBasePolicyCallback(BasePolicyCallback basePolicy) {
        this.basePolicy = basePolicy;
    }

    /**
     * Set the traffic signal controller of a lane
     *
     * @param laneId the lane ID
     * @param signalController the signal controller
     */
    public void setSignalControllers(int laneId,
            SignalController signalController) {
        signalControllers.put(laneId, signalController);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void processRequestMsg(Request msg) {
        int vin = msg.getVin();
        boolean insertingTraffic = false; // for FCFS-SIGNAL when the vehicle is in red lane but get reservation

        // If the vehicle has got a reservation already, reject it.
        if (basePolicy.hasReservation(vin)) {
            basePolicy.sendRejectMsg(vin, msg.getRequestId(),
                    Reject.Reason.CONFIRMED_ANOTHER_REQUEST);
            return;
        }

        // filter the proposals
        ProposalFilterResult filterResult
                = BasePolicy.generousFilter(msg.getProposals(),
                        basePolicy.getCurrentTime());
        if (filterResult.isNoProposalLeft()) {
            basePolicy.sendRejectMsg(vin,
                    msg.getRequestId(),
                    filterResult.getReason());
            return;
        }

        List<Request.Proposal> proposals = filterResult.getProposals();

        // double check
        if (proposals == null) {
            return;
        }
        if (!canEnterFromLane(proposals.get(0).getArrivalLaneID(),
                proposals.get(0).getDepartureLaneID())
                && proposals.get(0).getArrivalTime()
                > basePolicy.getCurrentTime()
                + DesignatedLanesExpr.MAXIMAL_TIME_TO_FUTURE_RESERVATION) {
            basePolicy.sendRejectMsg(vin, msg.getRequestId(),
                    Reject.Reason.NO_CLEAR_PATH);
            return;
        }
        if (Math.random() < TrafficSignalExpr.dropMessageProb && !Resources.vinToVehicles.get(vin).isHuman()) {
            if (!canEnterFromLaneAtTimepoint(proposals.get(0).getArrivalLaneID(),
                    proposals.get(0).getDepartureLaneID(),
                    proposals.get(0).getArrivalTime())) {

                basePolicy.sendRejectMsg(vin, msg.getRequestId(), Reject.Reason.DROPPED_MESSAGE);
                return;
            }
        } else if (SimConfig.signalType == SimConfig.SIGNAL_TYPE.DEFAULT) {
            // if this is SIGNAL and FCFS is not applied, check whether the light allows it to go across
            if (!canEnterFromLane(proposals.get(0).getArrivalLaneID(),
                    proposals.get(0).getDepartureLaneID())) {
                // If cannot enter from lane according to canEnterFromLane(), reject it.
                basePolicy.sendRejectMsg(vin, msg.getRequestId(),
                        Reject.Reason.NO_CLEAR_PATH);
                return;
            }
        } else {
            if (// to be frank, this vehicle cannot go through the intersection now,
                    // but we need to check more to confirm this, see the following part
                    !canEnterFromLaneAtTimepoint(proposals.get(0).getArrivalLaneID(),
                            proposals.get(0).getDepartureLaneID(),
                            proposals.get(0).getArrivalTime())) {

                // it's now red light. it's human => wait here!
                if (Resources.vinToVehicles.get(vin).isHuman()
                        && makingRightTurn(proposals.get(0).getArrivalLaneID(),
                                proposals.get(0).getDepartureLaneID()) == false) {
                    basePolicy.sendRejectMsg(vin, msg.getRequestId(),
                            Reject.Reason.NO_CLEAR_PATH);
                    return;
                }

                if (SimConfig.signalType == SimConfig.SIGNAL_TYPE.TRADITIONAL
                        || SimConfig.signalType == SimConfig.SIGNAL_TYPE.ONE_LANE_VERSION
                        || SimConfig.signalType == SimConfig.SIGNAL_TYPE.REVISED_PHASE
                        || SimConfig.signalType == SimConfig.SIGNAL_TYPE.RED_PHASE_ADAPTIVE) {

                    if (SimConfig.FULLY_OBSERVING) {
                        // in this case, just check whether human drivers appear
//                        if (vin == 1006) {
//                            DesignatedLanesExpr.segmentIndex++;
//                            System.out.println(DesignatedLanesExpr.segmentIndex);
//                            if (DesignatedLanesExpr.segmentIndex == 222) {
//                                System.out.println(DesignatedLanesExpr.segmentIndex);
//
//                            }
//                        }
//                        if(vin == 1044){
//                            System.out.println(DesignatedLanesExpr.SEED++); 
//                            if(DesignatedLanesExpr.SEED == 2030){
//                                int i = -1;
//                            }
//                        }
//                        if (!notHinderingHumanVehicles(proposals.get(0).getArrivalLaneID(),
//                                proposals.get(0).getDepartureLaneID(),
//                                proposals.get(0).getArrivalTime())
//                                || !notHinderingCAV(proposals.get(0).getArrivalLaneID(),
//                                        proposals.get(0).getDepartureLaneID(),
//                                        proposals.get(0).getArrivalTime())) {
                            if (!notHinderingHumanVehicles(proposals.get(0).getArrivalLaneID(),
                                proposals.get(0).getDepartureLaneID(),
                                proposals.get(0).getArrivalTime())) {
                            basePolicy.sendRejectMsg(vin, msg.getRequestId(),
                                    Reject.Reason.NO_CLEAR_PATH);
                            return;
                        }
                    } else {
                        // in this case, the intersection is not fully observable
                        // we need to assume there's always a human driven vehicle would appear.
                        if (!notHinderingPotentialHumanDrivers(proposals.get(0).getArrivalLaneID(),
                                proposals.get(0).getDepartureLaneID(),
                                proposals.get(0).getArrivalTime())) {
                            basePolicy.sendRejectMsg(vin, msg.getRequestId(),
                                    Reject.Reason.NO_CLEAR_PATH);
                            return;
                        }
                    }
                }

                // for informed human vehicles in fully observing policy
                if (Resources.vinToVehicles.get(vin).withCruiseControll()) {

                    if ( // if he's turning left.. Don't do it!
                            makingLeftTurn(proposals.get(0).getArrivalLaneID(),
                                    proposals.get(0).getDepartureLaneID())
                            || // if he has been told to stop,
                            // he can no longer enter the intersection in the same red phase.
                            Resources.vinToVehicles.get(vin).hasStopped()) {

                        // reject
                        basePolicy.sendRejectMsg(vin, msg.getRequestId(),
                                Reject.Reason.NO_CLEAR_PATH);
                        return;
                    }
                } else if (Resources.vinToVehicles.get(vin).withAdaptiveCruiseControll()) {

                    if ( // Make sure there is some vehicle in front of it for it to follow
                            !canFollowFrontVehicle(Resources.vinToVehicles.get(vin))
                            || // He cannot simply follow the vehicle in front of it in right lane,
                            // when the vehicle in front of it is a human-driven vehicle.
                            inRightLane(proposals.get(0).getArrivalLaneID(),
                                    proposals.get(0).getDepartureLaneID())
                            && Resources.vinToVehicles.get(vin).getFrontVehicle().isHuman()) {
                        // reject
                        basePolicy.sendRejectMsg(vin, msg.getRequestId(),
                                Reject.Reason.NO_CLEAR_PATH);
                        return;
                    }
                }

                // wow, a brave action! it's going into the intersection in red light!
                // mark this, and we need to check if it's also safe when it exit
                insertingTraffic = true;
            }
        }

        // try to see if reservation is possible for the remaining proposals.
        ReserveParam reserveParam = basePolicy.findReserveParam(msg, proposals);

        // now, check whether everything is fine when it exit
        // it would be sorry if this vehicle enters the intersection, and in the middle of his road,
        // the traffic light on a intersecting road becomes green.
        if (reserveParam != null && insertingTraffic) {
            double exitTime = reserveParam.getExitTime();

            if (SimConfig.signalType == SimConfig.SIGNAL_TYPE.TRADITIONAL
                    || SimConfig.signalType == SimConfig.SIGNAL_TYPE.ONE_LANE_VERSION
                    || SimConfig.signalType == SimConfig.SIGNAL_TYPE.REVISED_PHASE) {

                if (SimConfig.FULLY_OBSERVING) {
//                     if (!notHinderingHumanVehicles(proposals.get(0).getArrivalLaneID(),
//                            proposals.get(0).getDepartureLaneID(),
//                            exitTime)
//                            || !notHinderingCAV(proposals.get(0).getArrivalLaneID(),
//                                    proposals.get(0).getDepartureLaneID(),
//                                    proposals.get(0).getArrivalTime())) {
                    if (!notHinderingHumanVehicles(proposals.get(0).getArrivalLaneID(),
                            proposals.get(0).getDepartureLaneID(),
                            exitTime)) {
                        basePolicy.sendRejectMsg(vin, msg.getRequestId(),
                                Reject.Reason.NO_CLEAR_PATH);
                        return;
                    }
                } else {
                    if (!notHinderingPotentialHumanDrivers(proposals.get(0).getArrivalLaneID(),
                            proposals.get(0).getDepartureLaneID(),
                            exitTime)) {
                        basePolicy.sendRejectMsg(vin, msg.getRequestId(),
                                Reject.Reason.NO_CLEAR_PATH);
                        return;
                    }
                }
            }
        }

        if (reserveParam != null) {
            basePolicy.sendComfirmMsg(msg.getRequestId(), reserveParam);
            DesignatedLanesExpr.laneRegistry.get(proposals.get(0).getArrivalLaneID()).exit(0);
            DesignatedLanesExpr.laneRegistry.get(proposals.get(0).getDepartureLaneID()).enter(1);
        } else {
            basePolicy.sendRejectMsg(vin, msg.getRequestId(),
                    Reject.Reason.NO_CLEAR_PATH);
        }

    }

    private boolean canFollowFrontVehicle(VehicleSimView vehicle) {
        VehicleSimView frontVehicle = vehicle.getFrontVehicle();

        // if no such vehicle in front of it, he cannot follow.
        if (frontVehicle == null) {
            return false;
        }

        // if the vehicle is following the vehicle in front of it within a certain distance
        // it's okay to follow it.
        if (vehicle.getPosition().distance(frontVehicle.getPosition()) < SimConfig.FOLLOW_DISTANTCE) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TrafficSignal getSignal(int laneId) {
        return signalControllers.get(laneId).getSignal(
                basePolicy.getCurrentTime());
    }

    public TrafficSignal getSignalAtTimePoint(int laneId, double time) {
        return signalControllers.get(laneId).getSignal(time);
    }

    /////////////////////////////////
    // PRIVATE METHODS
    /////////////////////////////////
    /**
     * check whether it's in a phase where more than one direction light is on!
     *
     * @param time required time
     */
    public boolean isTwoDirectionPhase(double time) {
        int numOfLaneGreen = 0; // see how many lanes are green light now
        for (SignalController signalController : signalControllers.values()) {
            if (signalController.getSignal(time) == TrafficSignal.GREEN) {
                numOfLaneGreen++;
            }
        }

        if (numOfLaneGreen > DesignatedLanesExpr.NUMBER_OF_LANES) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Check whether the vehicle can enter the intersection from a lane at the
     * current time. This method is intended to be overridden by superclass.
     *
     * THIS FUNCTION IS ONLY FOR SIGNAL, NOT FCFS-SIGNAL
     *
     * @param arrivalLaneId the id of the lane from which the vehicle enters the
     * intersection.
     * @return whether the vehicle can enter the intersection
     */
    private boolean canEnterFromLane(int arrivalLaneId, int departureLaneId) {
        if (getSignal(arrivalLaneId) == TrafficSignal.GREEN) {
            return true;
        } else {
            // TODO: should not hard code it: need to make it more general
            if (arrivalLaneId == 2 && departureLaneId == 8) {
                return true;
            } else if (arrivalLaneId == 11 && departureLaneId == 2) {
                return true;
            } else if (arrivalLaneId == 5 && departureLaneId == 11) {
                return true;
            } else if (arrivalLaneId == 8 && departureLaneId == 5) {
                return true;
            } else {
                return false;
            }
        }
    }

    /////////////////////////////////
    // PRIVATE METHODS
    /////////////////////////////////
    /**
     * Check whether the vehicle can enter the intersection from a lane at the
     * ARRIVAL time. For human drivers, we need to check this.
     *
     * @param arrivalLaneId the id of the lane from which the vehicle enters the
     * intersection.
     * @return whether the vehicle can enter the intersection
     */
    private boolean canEnterFromLaneAtTimepoint(int arrivalLaneId,
            int departureLaneId,
            double arrivalTime) {
        if (!SimConfig.ALWAYS_ALLOW_TURNING_LEFT
                && isTwoDirectionPhase(arrivalTime)
                && makingLeftTurn(arrivalLaneId, departureLaneId)) {
            // forbid vehicles turning left in when green lights of two opposite directions are on.
            return false;
        } else if (signalControllers.get(arrivalLaneId).getSignal(arrivalTime) == TrafficSignal.GREEN) {
            return true;
        } else {
            return false;
        }
    }

    private boolean makingLeftTurn(int arrivalLaneId, int departureLaneId) {

        int arrivalRoad = Debug.currentMap.getRoad(arrivalLaneId).getIndex();
        int departureRoad = Debug.currentMap.getRoad(departureLaneId).getIndex();
        switch (arrivalRoad) {
            case 0:
                return departureRoad == 3;
            case 1:
                return departureRoad == 2;
            case 2:
                return departureRoad == 0;
            case 3:
                return departureRoad == 1;
            default:
                assert false : "Can only handle one intersection";
        }
        return false;
        // Check how roas indeces are applied. Fix this function as well as inRightLane()

//        return arrivalLaneId == 0
//                || arrivalLaneId == 6
//                || arrivalLaneId == 3
//                || arrivalLaneId == 9;
    }

    private boolean makingRightTurn(int arrivalLaneId, int departureLaneId) {

        int arrivalRoad = Debug.currentMap.getRoad(arrivalLaneId).getIndex();
        int departureRoad = Debug.currentMap.getRoad(departureLaneId).getIndex();
        switch (arrivalRoad) {
            case 0:
                return departureRoad == 2;
            case 1:
                return departureRoad == 3;
            case 2:
                return departureRoad == 1;
            case 3:
                return departureRoad == 0;
            default:
                assert false : "Can only handle one intersection";
        }
        return false;
        // Check how roas indeces are applied. Fix this function as well as inRightLane()

//        return arrivalLaneId == 0
//                || arrivalLaneId == 6
//                || arrivalLaneId == 3
//                || arrivalLaneId == 9;
    }

    private boolean inRightLane(int arrivalLaneId, int departureLaneId) {

        int arrivalRoad = Debug.currentMap.getRoad(arrivalLaneId).getIndex();
        int departureRoad = Debug.currentMap.getRoad(departureLaneId).getIndex();
        switch (arrivalRoad) {
            case 0:
                return departureRoad == 2;
            case 1:
                return departureRoad == 3;
            case 2:
                return departureRoad == 1;
            case 3:
                return departureRoad == 0;
            default:
                assert false : "Can only handle one intersection";
        }
        return false;

//        return arrivalLaneId == 8
//                || arrivalLaneId == 2
//                || arrivalLaneId == 11
//                || arrivalLaneId == 5;
    }

    /**
     * Check whether a certain autonomous vehicle would intersects ANY green
     * lane path. This is assuming the intersection has no idea of the coming of
     * human drivers.
     *
     * @param arrivalLaneID
     * @param departureLaneID
     * @param arrivalTime
     * @return
     */
    private boolean notHinderingPotentialHumanDrivers(int arrivalLaneID, int departureLaneID, double arrivalTime) {

        Registry<Lane> laneRegistry = Resources.map.getLaneRegistry();

        for (Lane lane : laneRegistry.getValues()) {
            // only consider green lanes
            if (signalControllers.get(lane.getId()).getSignal(arrivalTime) != TrafficSignal.GREEN) {
                continue;
            }

            List<Road> destinationRoad = Resources.destinationSelector.getPossibleDestination(lane);

            if (destinationRoad.size() == 0) {
                System.err.println("Possible destination empty in notHinderingHumanVehicles! This cannot be true.");
                System.err.println("Lane id: " + lane.getId());
            }

            for (Road road : destinationRoad) {
                for (Lane destinationLane : road.getLanes()) {
                    // Check whether it's going into the right lane.
                    // Actually, the vehicle must go into its corresponding lane the in destination road
                    if (lane.getIndexInRoad() == destinationLane.getIndexInRoad()
                            && canEnterFromLaneAtTimepoint(lane.getId(), destinationLane.getId(), arrivalTime)) {
                        if (GridMapUtil.laneIntersect(arrivalLaneID, departureLaneID,
                                lane.getId(), destinationLane.getId())) {
                            // There's a chance that this vehicle would collide into the human vehicle
                            return false;
                        }
                    }
                }
            }
        }

        return true;
    }

    private boolean canEnterFromLaneAtTimepoint(VehicleSimView vehicle, double arrivalTime) {
        if (vehicle == null || vehicle.getDriver().getState() == State.V2I_CLEARING || vehicle.getDriver().getState() == State.V2I_TERMINAL_STATE) {
            return true;
        }

        if (vehicle.getDriver().getState() == State.V2I_TRAVERSING) {
            return true;
        }

        if (!vehicle.isHuman()) {
            return canEnterFromLaneAtTimepoint(vehicle.getFrontVehicle(), arrivalTime);
        }

        Road currentRoad = Debug.currentMap.getRoad(vehicle.getDriver().getCurrentLane());
//        if(currentRoad.getIndex() == 1){
//            int i = -1;
//        }

        //int i = arrivalLane.getIndexInRoad();
        List<Lane> destinationLanes = DesignatedLanesExpr.possibleDestinationsH(vehicle.getDriver().getCurrentLane());

        // we only consider the vehicles that are going into the intersection
        for (Lane lane : destinationLanes) {
            if (vehicle.getDriver().getState() == State.V2I_TRAVERSING) {
                return canEnterFromLaneAtTimepoint(vehicle.getFrontVehicle(), arrivalTime);
            }
            if (canEnterFromLaneAtTimepoint(vehicle.getDriver().getCurrentLane().getId(), lane.getId(), arrivalTime)) {
                return canEnterFromLaneAtTimepoint(vehicle.getFrontVehicle(), arrivalTime);
            }

        }
        return false;
    }

    /**
     * Check whether a certain autonomous vehicle would collides into a
     * potential human driver
     *
     * @param arrivalLaneID of a certain auto vehicle
     * @param departureLaneID of a certain auto vehicle
     * @param arrivalTime of a certain auto vehicle
     * @return whether it would collides into a human driver
     */
    private boolean notHinderingHumanVehicles(int arrivalLaneID, int departureLaneID, double arrivalTime) {
        Map<Integer, VehicleSimView> vinToVehicles = Resources.vinToVehicles;

//         if (true) {
//            return true;
//        }
        for (VehicleSimView vehicle : vinToVehicles.values()) {
            if (!vehicle.isHuman()) {
                continue;
            }

            Driver driver = vehicle.getDriver();

            if (vehicle.getVIN() == 1009) {
                int i = -1;                                                          ///////////**********************///////////////**********GuniGuni 
            }
            // if this vehicle has already left the intersection, forget it.
            if (driver.getState() == State.V2I_CLEARING || driver.getState() == State.V2I_TERMINAL_STATE) {
                continue;
            }

            //If behind the AV in question
            if (driver.getCurrentLane().getId() == arrivalLaneID) {
                continue;
            }
            // if it's in green light, and it's human,
            // then the path of this vehicle must not intersect with the human
            if (canEnterFromLaneAtTimepoint(vehicle, arrivalTime)) {
                //  if (vehicle.isHuman() || vehicle.withCruiseControll() || vehicle.withAdaptiveCruiseControll()) { Guni: Changed here
                Lane humanArrivalLane = driver.getCurrentLane();

                // In this simulator, we DO know where the human vehicle is going.
                // This is not a realistic assumption -
                // we don't know the exact destination lane in the real world.
                // So, we can only determine by the current lane of human driver
                Road destinationRoad = driver.getDestination();

                for (Lane lane : destinationRoad.getLanes()) {
                    // Check whether it's going into the right lane.
                    // Actually, the vehicle must go into its corresponding lane the in destination road
                    if (lane.getIndexInRoad() == humanArrivalLane.getIndexInRoad()) {
                        if (GridMapUtil.laneIntersect(arrivalLaneID, departureLaneID,
                                humanArrivalLane.getId(), lane.getId())) {
                            // There's a chance that this vehicle would collide into the human vehicle
                            return false;
                        }
                    }
                }

                // }
            }
        }

        return true;
    }

//    /**
//     * Check whether a certain autonomous vehicle would collides into a CAV with
//     * green light
//     *
//     * @param arrivalLaneID of a certain auto vehicle
//     * @param departureLaneID of a certain auto vehicle
//     * @param arrivalTime of a certain auto vehicle
//     * @return whether it would collides into a human driver
//     */
//    private boolean notHinderingCAV(int arrivalLaneID, int departureLaneID, double arrivalTime) {
//
//        Map<Integer, VehicleSimView> vinToVehicles = Resources.vinToVehicles;
//
////             if (true) {
////            return true;
////        }
//        for (VehicleSimView vehicle : vinToVehicles.values()) {
//            if (vehicle.isHuman()) {
//                continue;
//            }
//            Driver driver = vehicle.getDriver();
//
//            // if this vehicle has already left the intersection, forget it.
//            if (driver.getState() == State.V2I_CLEARING) {
//                continue;
//            }
//
//            List<Lane> destinationLanes = driver.getDestination().getLanes();
//
//            // we only consider the vehicles that are going into the intersection
//            boolean canGetInto = false;
//            for (Lane lane : destinationLanes) {
//                if (canEnterFromLaneAtTimepoint(driver.getCurrentLane().getId(), lane.getId(), arrivalTime)) {
//                    canGetInto = true;
//                    break;
//                }
//            }
//
//            // if it's in green light, and it's CAV,
//            // then the path of this vehicle must not intersect with the CAV
//            if (canGetInto) {
//
//                // In this simulator, we DO know where the human vehicle is going.
//                // This is not a realistic assumption -
//                // we don't know the exact destination lane in the real world.
//                // So, we can only determine by the current lane of human driver
//                Lane destinationLane = driver.getDestination().getLanes().get(driver.getCurrentLane().getIndexInRoad());
//
//			  			// Check whether it's going into the right lane.
//                // Actually, the vehicle must go into its corresponding lane the in destination road
//                if (GridMapUtil.laneIntersect(arrivalLaneID, departureLaneID,
//                        driver.getCurrentLane().getId(), destinationLane.getId())) {
//                    // There's a chance that this vehicle would collide into the human vehicle
//                    return false;
//                }
//
//            }
//        }
//
//        return true;
//    }

//    /**
//     * Check whether a certain autonomous vehicle would collides into a
//     * potential human driver
//     *
//     * @param arrivalLaneID of a certain auto vehicle
//     * @param departureLaneID of a certain auto vehicle
//     * @param arrivalTime of a certain auto vehicle
//     * @return whether it would collides into a human driver
//     */
//    private boolean notHinderingHumanVehicles(int arrivalLaneID, int departureLaneID, double arrivalTime) {
//        Map<Integer, VehicleSimView> vinToVehicles = Resources.vinToVehicles;
//
////         if (SimConfig.FULLY_OBSERVING == true) {
////            return true;
////        }
//         
//        for (VehicleSimView vehicle : vinToVehicles.values()) {
//            Driver driver = vehicle.getDriver();
//
//            // if this vehicle has already left the intersection, forget it.
//            if (driver.getState() == State.V2I_CLEARING) {
//                continue;
//            }
//
//            List<Lane> destinationLanes = driver.getDestination().getLanes();
//
//            // we only consider the vehicles that are going into the intersection
//            boolean canGetInto = false;
//            for (Lane lane : destinationLanes) {
//                if (canEnterFromLaneAtTimepoint(driver.getCurrentLane().getId(), lane.getId(), arrivalTime)) {
//                    canGetInto = true;
//                    break;
//                }
//            }
//
//            // if it's in green light, and it's human,
//            // then the path of this vehicle must not intersect with the human
//            if (canGetInto) {
//                if (vehicle.isHuman() || vehicle.withCruiseControll() || vehicle.withAdaptiveCruiseControll()) {
//                    Lane humanArrivalLane = driver.getCurrentLane();
//
//                    // In this simulator, we DO know where the human vehicle is going.
//                    // This is not a realistic assumption -
//                    // we don't know the exact destination lane in the real world.
//                    // So, we can only determine by the current lane of human driver
//                    List<Road> destinationRoad = Resources.destinationSelector.getPossibleDestination(humanArrivalLane);
//
//                    if (destinationRoad.size() == 0) {
//                        System.err.println("Possible destination empty in notHinderingHumanVehicles! This cannot be true.");
//                    }
//
//                    for (Road road : destinationRoad) {
//                        for (Lane lane : road.getLanes()) {
//                            // Check whether it's going into the right lane.
//                            // Actually, the vehicle must go into its corresponding lane the in destination road
//                            if (lane.getIndexInRoad() == humanArrivalLane.getIndexInRoad()) {
//                                if (GridMapUtil.laneIntersect(arrivalLaneID, departureLaneID,
//                                        humanArrivalLane.getId(), lane.getId())) {
//                                    // There's a chance that this vehicle would collide into the human vehicle
//                                    return false;
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//        }
//
//        return true;
//    }
    /**
     * Check whether all the vehicles in the green lanes have get reservation.
     * If so, the vehicles in red lanes can feel free to send request
     *
     * THIS IS NOT A GOOD WAY TO DETERMINE WHETHER A AUTO VEHICLE CAN GO ACCROSS
     *
     * @return whether all get reservation or not
     *
     */
    private boolean allHumanVehicleInGreenLaneGetReservation(double arrivalTime) {
        Map<Integer, VehicleSimView> vinToVehicles = Resources.vinToVehicles;

        for (VehicleSimView vehicle : vinToVehicles.values()) {
            Driver driver = vehicle.getDriver();

            List<Lane> destinationLanes = driver.getDestination().getLanes();

            boolean canGetInto = false;
            for (Lane lane : destinationLanes) {
                if (canEnterFromLaneAtTimepoint(driver.getCurrentLane().getId(), lane.getId(), arrivalTime)) {
                    // in this case, this vehicle should go into intersection, if it's driven by human
                    canGetInto = true;
                    break;
                }
            }

            // if it's in green light, and it's human, then it must have reservation
            // of course, it cannot be a human driver
            if (canGetInto) {
                if (vehicle.isHuman()) {
                    int vin = vehicle.getVIN();
                    if (!basePolicy.hasReservation(vin)) {
                        // a human driver has not got a reservation here
                        return false;
                    }
                }
            }
        }

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public StatCollector<?> getStatCollector() {
        return null;
    }

}
