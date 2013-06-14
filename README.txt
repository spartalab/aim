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


