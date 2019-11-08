Online distribution for JScrollableDesktopPane
==============================================

To compile your own programs against scrollabledesktop.jar:

javac -classpath scrollabledesktop.jar YourClassName.java

To execute your own programs against scrollabledesktop.jar:

java -classpath .;scrollabledesktop.jar YourClassName

Note that if scrollabledesktop.jar is added to the global classpath
via the CLASSPATH environment variable, the -classpath attribute 
is unnecessary


Directory layout:
-----------------

docs\index.html - JavaDOC
examples\ - examples
src\ - source code

compile.bat - compiles the source code
readme.txt - this file
scrollabledesktop.jar - scrollabledesktop package
