@echo off
echo ---------------------------------------------------------------------
echo FIXES ERROR: FAILED TO CREATE PARENT DIRECTORIES FOR TRACKING [...] MOSHI-PARENT
echo ---------------------------------------------------------------------
echo MAVEN WILL NOW CLEAN YOUR WORKING DIRECTORY AND THEN  
echo DOWNLOAD DEPENDENCYS FOR THE RADAR AND PACKAGE THEM
echo ---------------------------------------------------------------------
echo ANY WARNINGS YOU SEE CAN BE SAFELY IGNORED IF THE BUILD IS SUCCESSFUL
echo ---------------------------------------------------------------------
echo YOU NEED TO INSTALL MAVEN AND JDK8 AND JAVA8 BEFORE RUNNING THIS
echo ---------------------------------------------------------------------

timeout 10

del /f C:\Users\%username%\.m2
mvn -T 1C clean dependency:copy-dependencies package

timeout 10
