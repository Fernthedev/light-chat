@echo off
mkdir java
mkdir dart
set protopath=%cd%\src
set mainfolder=C:\Users\fernr\IdeaProjects\lighttest\java
set jout=%cd%\out
set curPath=%cd%

rmdir /S /Q java
rmdir /S /Q dart

mkdir java
mkdir dart

@echo Building 

for /R %protopath% %%i in (*.proto) do ( 
@echo Building %%i
protoc -I=%protopath%\ --java_out=java --dart_out=dart %%i

)


@echo Moving to java folder
REM protoc -I=%protopath%\ --java_out=java --dart_out=dart %protopath%\*
REM @echo %cd%\java eeeeeeeeeee %mainfolder%\universalChat\src\main\java\com\github\fernthedev\packets
xcopy /s /Y %cd%\java %mainfolder%\universalChat\src\main\java\ 
pause