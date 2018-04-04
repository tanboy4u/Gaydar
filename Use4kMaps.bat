@echo off
setlocal

echo ARE YOU SURE YOU WANT TO CHANGE TO THE 4K MAPS?
echo ARE YOU SURE YOU WANT TO CHANGE TO THE 4K MAPS?
echo ARE YOU SURE YOU WANT TO CHANGE TO THE 4K MAPS?
echo ARE YOU SURE YOU WANT TO CHANGE TO THE 4K MAPS?

:PROMPT
SET /P AREYOUSURE=ARE YOU SURE YOU WANT TO CHANGE TO THE 4K MAPS? (Y/[N])?
IF /I "%AREYOUSURE%" NEQ "Y" GOTO END


ren src\main\resources\maps\Erangel_Minimap.png Erangel_8k.png
echo "Renamed Erangel_Minimap.png to  Erangel_8k.png"

echo "....."
timeout 1

ren src\main\resources\maps\Miramar_Minimap.png Miramar_8k.png
echo "Renamed Miramar_Minimap.png to  Miramar_8k.png"

echo "....."
timeout 1

ren src\main\resources\maps\Erangel4k.png Erangel_Minimap.png
echo "Renamed Erangel4k.png to Erangel_Minimap.png"

echo "....."
timeout 1

ren src\main\resources\maps\Miramar4k.png Miramar_Minimap.png
echo "Renamed Miramar4k.png to Miramar_Minimap.png"

echo "....."
timeout 1

echo "Done! You can now build with the 4k Maps, DONT RUN THIS AGAIN BEFORE RUNNING USE8kMAPS."
echo "Done! You can now build with the 4k Maps, DONT RUN THIS AGAIN BEFORE RUNNING USE8kMAPS."
echo "Done! You can now build with the 4k Maps, DONT RUN THIS AGAIN BEFORE RUNNING USE8kMAPS."
echo "Done! You can now build with the 4k Maps, DONT RUN THIS AGAIN BEFORE RUNNING USE8kMAPS."
echo "Done! You can now build with the 4k Maps, DONT RUN THIS AGAIN BEFORE RUNNING USE8kMAPS."
echo "Done! You can now build with the 4k Maps, DONT RUN THIS AGAIN BEFORE RUNNING USE8kMAPS."
echo "Done! You can now build with the 4k Maps, DONT RUN THIS AGAIN BEFORE RUNNING USE8kMAPS."
echo "Done! You can now build with the 4k Maps, DONT RUN THIS AGAIN BEFORE RUNNING USE8kMAPS."


:END
pause