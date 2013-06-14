#!/usr/local/bin/python3.1

import sys
import getopt
import pprint
from collections import defaultdict


#===============================================================================
# Utility functions
#===============================================================================

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
    total = 0.0
    for dcl, rate in eff_spawn_rate.items():
        total += rate
    return total / len(eff_spawn_rate)


#===============================================================================
# Main
#===============================================================================

def usage():
    print(sys.argv[0], "[-v] datafile.csv")
    print(sys.argv[0], "[-h|--help]")

def main():
    try:
        opts, args = getopt.getopt(sys.argv[1:], "hv", ["help", "verbose"])
    except getopt.GetoptError as err:
        print(err)
        usage()
        exit(2)
    isVerbose = False
    for o, a in opts:
        if o in ("-h", "--help"):
            usage()
            sys.exit()
        elif o in ("-v", "--verbose"):
            isVerbose = True
        else:
            assert False, "unhandled option"
    if len(args) == 0:
        usage()
        exit(2)
    eff_spawn_rate = read_eff_spawn_rate(args[0])
    avg_eff_spawn_rate = calc_avg_eff_spawn_rate(eff_spawn_rate)
    if isVerbose:
        for dcl, rate in eff_spawn_rate.items():
            print("Effective spawn rate at", dcl,"=",rate)
        print("Avg effective spawn rate =", avg_eff_spawn_rate)
    else:
        print(avg_eff_spawn_rate)

if __name__ == "__main__":
    main()

