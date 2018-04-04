@echo off
echo ---------------------------------------------------------------------
echo ---------------------------------------------------------------------
echo MAVEN WILL NOW CLEAN YOUR WORKING DIRECTORY AND THEN VERIFY, 
echo COMPILE AND BUILD THE RADAR, THIS MAY TAKE SOME TIME
echo ---------------------------------------------------------------------
echo ANY WARNINGS YOU SEE CAN BE SAFELY IGNORED IF THE BUILD IS SUCCESSFUL
echo ---------------------------------------------------------------------
echo YOU NEED TO INSTALL MAVEN AND JDK8 AND JAVA8 BEFORE RUNNING THIS
echo ---------------------------------------------------------------------

timeout 10

mvn -T 1C clean verify install

timeout 10
