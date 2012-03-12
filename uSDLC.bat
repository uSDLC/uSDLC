@echo off
if not exist "uSDLC-full.jar" goto developerMode

# Stand-alone mode

	java -jar uSDLC-full.jar userId=anon port=80

goto end

:developerMode
if "%1"=="" goto usage

	java -cp web\lib\jars\ant-launcher.jar -Dant.home=web\lib\jars org.apache.tools.ant.launch.Launcher -nouserlib %1 %2

goto end

:usage
	echo Usage:
	echo     ./uSDLC build
	echo     ./uSDLC clean build
	echo     ./uSDLC run
	echo     ./uSDLC clean run
goto end

:end
