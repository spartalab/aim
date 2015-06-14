#!/usr/local/bin/python3.1

import os
import sys
import getopt
import pprint
from collections import defaultdict
import csv
import math

#===============================================================================
# Variables
#===============================================================================

baseline_filename = 'baseline.csv'
infilename = ''
base_time = dict()

pp = pprint.PrettyPrinter(indent=4).pprint

#===============================================================================
# Utility classes
#===============================================================================

class IncorrectParameter(Exception):
    def __init__(self, value):
        self.value = value
    def __str__(self):
        return "Incorrect command line parameters:"+repr(self.value)

#===============================================================================
# Core functions
#===============================================================================

def read_baseline(infilename):
    traversal_time = dict()
    with open(baseline_filename) as infile:
        isFirstLine = True
        vtime = dict()
        for s in infile:
            if isFirstLine:
                isFirstLine = False
            else:
                s2 = s.strip()
                d = s2.split(',')
                key = (d[3],d[4],d[5])
                if key in vtime:
                    t = abs(float(d[1])-float(vtime[key]))
                    traversal_time[key] = t
                else:
                    vtime[key] = d[1]
    return traversal_time

def read_delay(infilename):
    delay_time = dict()
    with open(infilename) as infile:
        isFirstLine = True
        vtime = dict()
        for s in infile:
            if isFirstLine:
                isFirstLine = False
            else:
                s2 = s.strip()
                d = s2.split(',')
                vin = d[0]
                t = d[1]
                key = (d[3],d[4],d[5])
                if vin in vtime:
                    traversal_time = abs(float(t)-float(vtime[vin]))
                    delay_time[vin] = traversal_time - base_time[key]
                else:
                    vtime[vin] = t
    return delay_time

def print_traversal_time(traversal_time):
    for (type,laneId,destRoad),t in traversal_time.items():
        print("[", type, ",", laneId, ",", destRoad, "] = ", t);

def avg_delay(delay_time):
	count_for_human = 0
	count_for_auto = 0
	delay_for_human = 0
	delay_for_auto = 0
	
	for vin,delay in delay_time.items():
		if (int(vin) >= 10000):
			count_for_human += 1
			delay_for_human += delay
		else:
			count_for_auto += 1
			delay_for_auto += delay

	if (count_for_human != 0):
	  avg_delay_human = delay_for_human / count_for_human
	else:
	  avg_delay_human = 0
	  
	if (count_for_auto != 0):
	  avg_delay_auto = delay_for_auto / count_for_auto
	else:
	  avg_delay_auto = 0

	if (count_for_human != 0 or count_for_auto != 0):
	  avg_delay = (delay_for_human + delay_for_auto) / (count_for_human + count_for_auto)
	else:
	  avg_delay = 0
	return [avg_delay, avg_delay_human, avg_delay_auto]

#===============================================================================
# Main
#===============================================================================

def operate(array, runningForOneData):
    global baseline_filename
    global base_time

    base_time = read_baseline(baseline_filename)

    writer = csv.writer(open("delay_result_for_human_" + '_'.join([str(n) for n in array]) + ".csv", "w"))
    command = 'java expr.trb.TrafficSignalExpr -d ' + ' '.join([str(n) for n in array])
    fileName = 'ts_dcl_' + '_'.join([str(n) for n in array]) + '.csv'

    data = []

    for timeForData in range(runningForOneData):
      # informative
      print command
      print 'for the ' + str(timeForData) + " time"
	  
      os.system(command)
      delay_time = read_delay(fileName)
      avg_delay_time = avg_delay(delay_time)
      print "delay time is " + str(avg_delay_time)

      writer.writerow(avg_delay_time)
    os.system("rm " + fileName)
