@echo off
echo ---------------------------------------------------------------------
echo FIXES ERROR: Settings missing from settings.json
echo ---------------------------------------------------------------------
echo THIS WILL FIX YOUR MISSING SETTINGS BY DELETING THEM
echo AND CREATING THEM AGAIN, YOU WILL LOSE YOUR SETTINGS
echo ARE YOU SURE YOU WANT TO CONTINUE?
echo ---------------------------------------------------------------------
echo ---------------------------------------------------------------------

timeout 10

del /f settings.json
OFF.bat

timeout 10
