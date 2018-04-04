@echo off
setlocal

echo THIS IS THE DEFAULT IF YOU RUN THIS FIRST YOU WILL BREAK STUFF
echo THIS IS THE DEFAULT IF YOU RUN THIS FIRST YOU WILL BREAK STUFF
echo THIS IS THE DEFAULT IF YOU RUN THIS FIRST YOU WILL BREAK STUFF
echo THIS IS THE DEFAULT IF YOU RUN THIS FIRST YOU WILL BREAK STUFF
echo THIS IS THE DEFAULT IF YOU RUN THIS FIRST YOU WILL BREAK STUFF

:PROMPT
SET /P AREYOUSURE=THIS IS THE DEFAULT IF YOU RUN THIS FIRST YOU WILL BREAK STUFF. ARE YOU SURE? (Y/[N])?
IF /I "%AREYOUSURE%" NEQ "Y" GOTO END


ren src\main\resources\maps\Erangel_Minimap.png Erangel4k.png
echo "Renamed Erangel_Minimap.png to Erangel4k.png"

echo "....."
timeout 1

ren src\main\resources\maps\Miramar_Minimap.png Miramar4k.png
echo "Renamed Miramar_Minimap.png to Miramar4k.png"

echo "....."
timeout 1

ren src\main\resources\maps\Erangel_8k.png Erangel_Minimap.png
echo "Renamed Erangel_8k.png to Erangel_Minimap.png"

echo "....."
timeout 1

ren src\main\resources\maps\Miramar_8k.png Miramar_Minimap.png
echo "Renamed Miramar_8k.png to Miramar_Minimap.png"

echo "....."
timeout 1

echo "YOU CAN NOW BUILD WITH THE 8K MAPS, DO NOT RUN THIS AGAIN WITHOUT RUNNING USE4kMAPS"
echo "YOU CAN NOW BUILD WITH THE 8K MAPS, DO NOT RUN THIS AGAIN WITHOUT RUNNING USE4kMAPS"
echo "YOU CAN NOW BUILD WITH THE 8K MAPS, DO NOT RUN THIS AGAIN WITHOUT RUNNING USE4kMAPS"
echo "YOU CAN NOW BUILD WITH THE 8K MAPS, DO NOT RUN THIS AGAIN WITHOUT RUNNING USE4kMAPS"

:END
pause