cls
del *.class
del globals\*.class
del gui\*.class
del actions\*.class
del votingObjects\*.class
del classesWrittenByOthers\*.class
del parliaments\*.class
del parliaments\uninominal\*.class
del parliaments\plurinominal\*.class
del graphs\*.class
del indexes\*.class

set CLASSPATH=.;.\classesWrittenByOthers\jw-1130-jscroll\scrollabledesktop.jar


javac -Xlint:unchecked parliaments\*.java
javac -Xlint:unchecked parliaments\uninominal\*.java
javac -Xlint:unchecked parliaments\plurinominal\*.java
javac -Xlint:unchecked indexes\*.java
javac -Xlint:unchecked ALEX4.java
javac -Xlint:unchecked sortOutput.java