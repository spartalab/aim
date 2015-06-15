SemiAIM Codebase
==============

This is revised from the AIM codebase authored by Tsz-Chiu Au, available here:
http://www.cs.utexas.edu/~aim/

Run the following commands at the first level.

```
cd src/main/java
javac -d bin expr/trb/TrafficSignalExpr.java
cd bin
java expr.trb.TrafficSignalExpr
```

`expr.trb.TrafficSignalExpr` is the class for experiments. Running it without arguments
will print out the arguments to run experiments.

There are some python files in `aim5/src/main/java/bin` to parse the experiment
results and compute the delay time. For example, run
`delay_tech_penetration.py` to generate results in Figure 6 in our paper below.

http://www.cs.utexas.edu/~pstone/Papers/bib2html-links/Routledge15-Au.pdf
