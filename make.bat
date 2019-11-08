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

set CLASSPATH=.;.\classesWrittenByOthers\jw-1130-jscroll\scrollabledesktop.jar;.\languages
javac parliaments\*.java
javac parliaments\uninominal\*.java
javac parliaments\plurinominal\*.java
javac indexes\*.java
javac ALEX4.java
javac sortOutput.java