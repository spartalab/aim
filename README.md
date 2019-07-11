# Autonomous Intersection Management

AIM4 is a traffic simulator for the study of innovative design of intersection control protocol.
This program is based on the source code of AIM3, which is written by Kurt Dresner.
AIM4 is currently maintained by Tsz-Chiu Au <chiu@cs.utexas.edu>.
For more information visit the [AIM website.](http://www.cs.utexas.edu/~aim/)

This is a fork for use by Dr. Stephen Boyles's lab based on the AIM4 patch for collision avoidance by Diana Toader (dianaelenatoader@gmail.com).


## Instructions

Install Apache Maven (version >= 2.2.1) and Java (version >=6):


### AIM4

To compile with testing, type
```mvn assembly:assembly```

To compile without testing, type
```mvn -Dmaven.test.skip=true assembly:assembly```

To run the jar file, type
```java -jar target/AIM4-1.0-SNAPSHOT-jar-with-dependencies.jar```

To execute a particular main function, type
```java -cp target/AIM4-1.0-SNAPSHOT-jar-with-dependencies.jar <YOUR_MAIN_FUNCTION>```

To check the coding style, type
```
mvn checkstyle:checkstyle
view target/checkstyle-result.xml
```

To clean up, type
```mvn clean```


### SemiAIM (AIM5)

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
