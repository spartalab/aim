#!/usr/local/bin/python3.1

import sys
import getopt
import math

#===============================================================================
# Core functions
#===============================================================================

def calcAvgAndStd(infilename):
    orderedKey = [];
    fieldNum = -1;
    countDict = dict();
    sumDict = dict();
    sumSqDict = dict();
    with open(infilename) as infile:
        condorIdFieldId = -1
        for s in infile:
            arr = s.strip().split(',')
            arr1 = arr[0].split('_')
            # Identify the condor id field index
            if condorIdFieldId < 0:
                i=0
                for field in arr1:
                    if field.lower().rfind("condor") >= 0:
                        condorIdFieldId = i
                        break
                    else:
                        i = i + 1
            assert condorIdFieldId >= 0
            fieldNum = len(arr1) - 1
            # record
            key = "_".join(arr1[:condorIdFieldId]+arr1[(condorIdFieldId+1):])
            value = float(arr[1])
            if key in countDict:
                countDict[key] += 1
                sumDict[key] += value
                sumSqDict[key] += value * value
            else:
                orderedKey.append(key)
                countDict[key] = 1
                sumDict[key] = value
                sumSqDict[key] = value * value
    return orderedKey,fieldNum,countDict,sumDict,sumSqDict

def printAvgAndStd(orderedKey,fieldNum,countDict,sumDict,sumSqDict):
    print((',' * fieldNum )+"sample_size,mean,sd,95% error,lower 95% limit, upper 95% limit")
    for key in orderedKey:
        print(','.join(key.split('_')), end="")
        n = countDict[key]
        print(',' + str(n), end="")
        # print(',' + str(sumDict[key]), end="")
        # print(',' + str(sumSqDict[key]), end="")
        mean = sumDict[key]/n
        print(',' + format(mean, '.2f'), end="")
        sd = math.sqrt(sumSqDict[key]/n - mean * mean)
        print(',' + format(sd, '.4f'), end="")
        stderr = 1.96 * sd / math.sqrt(n)
        print(',' + format(stderr, '.4f'), end="")
        print(',' + format(mean - stderr, '.4f'), end="")
        print(',' + format(mean + stderr, '.4f'), end="")
        print()

#===============================================================================
# Main
#===============================================================================

def usage():
    print(syste.argv[0], "-h|--help")

def main():
    try:
        opts, args = getopt.getopt(sys.argv[1:], "h", ["help"])
    except getopt.GetoptError as err:
        print(err)
        usage()
        exit(2)
    for o, a in opts:
        if o in ("-h", "--help"):
            usage()
            sys.exit()
        else:
            assert False, "unhandled option"
    if len(args) == 0:
        usage()
        exit(2)
    orderedKey,fieldNum,countDict,sumDict,sumSqDict = calcAvgAndStd(args[0])
    printAvgAndStd(orderedKey,fieldNum,countDict,sumDict,sumSqDict)

if __name__ == "__main__":
    main()

