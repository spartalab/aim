f=aim4.jar
g=target/AIM4-1.0-SNAPSHOT-jar-with-dependencies.jar

JAVA_OPTIONS= -ea -server -Xmx1000M

all: $(f)

jar: $(f)

$(f): $(g)
	cp $(g) $(f)

$(g): clean
	mvn -Dmaven.test.skip=true assembly:assembly

run:
	java $(JAVA_OPTIONS) -jar $(f) &

javadoc:
	mvn javadoc:javadoc

style:
	mvn checkstyle:checkstyle
	
clean:
	rm -f $(f)
	mvn clean


