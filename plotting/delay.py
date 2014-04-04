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

sampleSize = 30

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

def avg_delay(delay_time, delayFunc):
    count = 0
    total_delay = 0
    for vin,delay in delay_time.items():
		# delay may or may not be linear. consult delayFunc
        total_delay += delayFunc(delay)
        count += 1

    if count == 0:
      # no data
      return 0
    else:
      return total_delay / count

#===============================================================================
# Main
#===============================================================================

def delay_batch():
    global sampleSize

    data = []

    for sampleId in range(sampleSize):
      # get the file name of the csv
      filename = "out." + str(sampleId)
      data.append(delay(filename))
    
    print min(data)

def delay(filename):
	linearDelay = lambda x: x
	squaredDelay = lambda x: x ** 2
	cubeDelay = lambda x: x ** 3

	delay_time = read_delay(filename)
	avg_delay_time = avg_delay(delay_time, linearDelay)
	print "delay time is " + str(avg_delay_time)
	return avg_delay_time

if __name__ == "__main__":
	base_time = read_baseline(baseline_filename)

	#writer = csv.writer(open("result.csv", "w"))

	if len(sys.argv) < 2:
		delay_batch()
	else:
		delay(sys.argv[1])
