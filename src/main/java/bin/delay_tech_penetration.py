import sys
import delay_kernel

def main():
  runningForOneData = 10
  simulationTime = 1800
  trafficLevel = 0.15

  processId = int(sys.argv[1])

  vType = processId / 21
  percent = 0.05 * (processId % 21)

  fullAuto = simpleCruise = adaptiveCruise = 0
  if vType == 0:
    fullAuto = percent
  elif vType == 1:
    simpleCruise = percent
  elif vType == 2:
    adaptiveCruise = percent

  human = 1 - percent
  array = ["0.1", str(human), str(simpleCruise), str(adaptiveCruise)]

  delay_kernel.operate(array, runningForOneData)

if __name__ == "__main__":
  main()
