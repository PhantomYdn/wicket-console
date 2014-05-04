@echo off
echo BUILDING
call mvn clean install
echo RUNNING Jetty
call mvn -f wicket-console-demo/pom.xml jetty:run
