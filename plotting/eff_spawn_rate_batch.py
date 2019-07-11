#!/usr/local/bin/python3.1

import os
import csv
import sys
import getopt
import pprint
from collections import defaultdict


#===============================================================================
# Utility functions
#===============================================================================
laneNum = 3

def read_eff_spawn_rate(infilename):
    with open(infilename) as infile:
        latest_time = 0.0
        counts = dict()
        isFirstLine = True
        for s in infile:
            if isFirstLine:
                isFirstLine = False
            else:
                s2 = s.strip()
                d = s2.split(',')
                dcl = d[2]
                if dcl in counts:
                    counts[dcl] += 1
                else:
                    counts[dcl] = 1
                t = float(d[1])
                if latest_time < t:
                    latest_time = t
    eff_spawn_rate = dict()
    for dcl, n in counts.items():
        if dcl.find("Entrance") >= 0:
            eff_spawn_rate[dcl] = n / latest_time
    return eff_spawn_rate

def calc_avg_eff_spawn_rate(eff_spawn_rate):
    global laneNum
    total = 0.0
    for dcl, rate in eff_spawn_rate.items():
        total += rate
    if total == 0.0:
      return 0
    else:
      return total / len(eff_spawn_rate) / laneNum


#===============================================================================
# Main
#===============================================================================

def usage():
    print "python eff_spawn_rate_batch.py TYPE level"

def main():
    if len(sys.argv) < 3:
      usage()

    # useful variables
    level = float(sys.argv[2])
    runningForOneData = 30 
    runningTime = 1800
    type = sys.argv[1]

    data = []

    writer = csv.writer(open(type+"_eff_spawn_result_" + str(level) + ".csv", "w"))
  
    for timeForData in range(runningForOneData):
      print "Type:", type, "Level:", level, "runningTime:", timeForData
      os.system("java expr.trb.TrafficSignalExpr "+type+" 6phases .25 0.10 0.25 " + format(level, ".2f") + " " + str(runningTime))
      filename = "ts_dcl_"+type+"_6phases_.25_0.10_0.25_" + format(level, ".2f") + "_" + str(runningTime) + ".csv"
      eff_spawn_rate = read_eff_spawn_rate(filename)
      avg_eff_spawn_rate = calc_avg_eff_spawn_rate(eff_spawn_rate)
      data.append(avg_eff_spawn_rate)
      print eff_spawn_rate
     
    row = [level] + data
    print row
    print "================="
    writer.writerow(row)

if __name__ == "__main__":
    main()

