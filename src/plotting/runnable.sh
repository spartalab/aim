#!/bin/bash
# we need to check whether we need to add zero before it - traffic level
if [ $2 -lt 10 ]; then
  python delay_for_human.py $1 0.0$2
else
  python delay_for_human.py $1 0.$2
fi
