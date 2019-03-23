#!/usr/local/bin/python3.1

import sys
import getopt


#===============================================================================
# Core functions
#===============================================================================

def read_time_diff(infilename):
    vins = [];
    max_time = dict()
    min_time = dict()
    diff_time = dict()
    with open(infilename) as infile:
        isFirstLine = True
        for s in infile:
            if isFirstLine:
                isFirstLine = False
            else:
                d = s.strip().split(',')
                vin = d[0]
                t = float(d[1])
                if not vin in vins:
                    vins.append(vin)
                if vin in max_time:
                    if t > max_time[vin]:
                        max_time[vin] = t
                else:
                    max_time[vin] = t
                if vin in min_time:
                    if t < min_time[vin]:
                        min_time[vin] = t
                else:
                    min_time[vin] = t
    for vin in vins:
        diff_time[vin] = max_time[vin] - min_time[vin]
    return diff_time


def avg_time_diff(diff_time):
    count = 0.0
    total = 0.0
    for vin,time in diff_time.items():
        total += time
        count += 1
    return total / count

#===============================================================================
# Main
#===============================================================================

def usage():
    print("Usage:")
    print("   ", sys.argv[0], "datafile.csv")
    print("   ", sys.argv[0], "[-h|--help]")

def main():
    global baseline_filename
    global base_time
    try:
        opts, args = getopt.getopt(sys.argv[1:], "h:", ["help"])
    except getopt.GetoptError as err:
        print(err)
        usage()
        exit(2)
    for o, a in opts:
        if o in ("-h", "--help"):
            usage()
            exit()
        else:
            assert False, "unhandled option"
    if len(args) == 0:
        usage()
        exit(2)
    print(format(avg_time_diff(read_time_diff(args[0])), '.4f'))

if __name__ == "__main__":
    main()


