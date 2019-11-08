#!/bin/bash
# Proper header for a Bash script.

pwd

rm -f *.class
rm -f globals/*.class
rm -f gui/*.class
rm -f actions/*.class
rm -f votingObjects/*.class
rm -f classesWrittenByOthers/*.class
rm -f parliaments/*.class
rm -f parliaments/uninominal/*.class
rm -f parliaments/plurinominal/*.class
rm -f graphs/*.class
rm -f indexes/*.class

set CLASSPATH=.:./classesWrittenByOthers/jw-1130-jscroll/scrollabledesktop.jar

javac parliaments/*.java
javac parliaments/uninominal/*.java
javac parliaments/plurinominal/*.java
javac indexes/*.java
javac ALEX4.java
javac sortOutput.java

exit # The right and proper method of "exiting" from a script.
