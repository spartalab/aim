Install Apache Maven (version >= 2.2.1):

To compile with testing, type

  mvn assembly:assembly

To compile without testing, type

  mvn -Dmaven.test.skip=true assembly:assembly

To run the jar file, type

  java -jar target/AIM4-1.0-SNAPSHOT-jar-with-dependencies.jar

To execute a particular main function, type

  java -cp target/AIM4-1.0-SNAPSHOT-jar-with-dependencies.jar <YOUR_MAIN_FUNCTION>

To check the coding style, type

  mvn checkstyle:checkstyle
  view target/checkstyle-result.xml

To clean up, type

  mvn clean

=============================
Places need to look at:

aim4.sim.AutoDriverOnlySimulator
	spawnVehicles
		uses aim4.map.GridMapUtil.UniformSpawnSpecGenerator

aim4.im.v2i.processV2IMessage
	process request messages

AutoDriverOnlySimSetup
	batch mode on/off (interface in SimConfig)
GridMapUtil.setBatchManagers
GridMapUtil.setFCFSManagers
aim4.im.v2i.RequestHandler.BatchModeRequestHandler
	batch mode, etc.
	control highlight of vehicles in batch mode
aim4.im.v2i.RequestHandler.FCFSRequestHandler

aim4.map.GridMapUtil.setBatchManagers
	set batch managers to each intersection
aim4.im.v2i.batch.RoadBasedReordering
	used by batch manager
