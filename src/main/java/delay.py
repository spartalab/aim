#!/usr/local/bin/python3.1

import sys
import getopt
import pprint
from collections import defaultdict

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
    count = 0
    total_delay = 0
    for vin,delay in delay_time.items():
        total_delay += delay
        count += 1
    return total_delay / count

#===============================================================================
# Main
#===============================================================================

def usage():
    print(sys.argv[0], "[-b baseline.csv] datafile.csv")
    print(sys.argv[0], "[-h|--help]")

def main():
    global baseline_filename
    global base_time
    try:
        opts, args = getopt.getopt(sys.argv[1:], "hb:", ["help", "baseline="])
    except getopt.GetoptError as err:
        print(err)
        usage()
        exit(2)
    for o, a in opts:
        if o in ("-h", "--help"):
            usage()
            sys.exit()
        elif o in ("-b", "--baseline"):
            baseline_filename = a
        else:
            assert False, "unhandled option"
    if len(args) == 0:
        usage()
        exit(2)
    base_time = read_baseline(baseline_filename)
    delay_time = read_delay(args[0])
    # for vin,delay in delay_time.items():
    #     print(vin, ' => ', format(delay, '.8f'))

    print(format(avg_delay(delay_time), '.4f'))

if __name__ == "__main__":
    main()


