#!/usr/local/bin/python3.1

import sys
import getopt
import pprint
from collections import defaultdict

#===============================================================================
# Utility functions
#===============================================================================

def read_arrival_rate(infilename):
    latest_time = 0.0
    count = 0
    with open(infilename) as infile:
        last_vins = list()
        isFirstLine = True
        for s in infile:
            if isFirstLine:
                isFirstLine = False
            else:
                s2 = s.strip()
                d = s2.split(',')
                t = float(d[0])
                vins = d[1:]
                for vin in vins:
                    if not vin in last_vins:
                        count += 1
                last_vins = vins
                latest_time = t
    return count / latest_time;


#===============================================================================
# Main
#===============================================================================

def usage():
    print(sys.argv[0], "[-l num_of_lanes] datafile.csv")
    print(sys.argv[0], "[-h|--help]")

def main():
    num_of_lanes = 4
    try:
        opts, args = getopt.getopt(sys.argv[1:], "hl:", ["help", "num_of_lanes="])
    except getopt.GetoptError as err:
        print(err)
        usage()
        exit(2)
    for o, a in opts:
        if o in ("-h", "--help"):
            usage()
            sys.exit()
        elif o in ("-l", "--num_of_lanes"):
            num_of_lanes = int(a)
        else:
            assert False, "unhandled option"
    if len(args) == 0:
        usage()
        exit(2)
    arrival_rate = read_arrival_rate(args[0])
    print(arrival_rate / num_of_lanes)

if __name__ == "__main__":
    main()

