#!/bin/bash
# Script that will run a uSDLC server. If you have downloaded the
# stand-alone jar, it will be run in the background and all output will go
# to hohup.out. If you have downloaded the source from
# then you have options to clean, build and run uSDLC.

if [ -e "uSDLC-full.jar" ]
then
	# Stand-alone mode, when uSDLC-full.jar was downloaded

	# Run it in the background. If first time,
	# extracts uSDLC into the current directory. Otherwise only extracts
	# changed file if uSDLC.jar is newer (downloaded update).

	# Remove comment if you want to save output from prior runs
	#cat nohup.out >> nohup.log
	rm nohup.out 2>/dev/null

	echo 'Running uSDLC in stand-alone mode...'
	nohup java -jar uSDLC-full.jar userId=Guest port=80&

	# Uncomment if you want to see the output. Remember that ^C will not
	# stop the uSDLC process, only displaying the output.
	#tail -f nohup.out
else
	# *** LOOK HERE IF YOU ARE A DEVELOPER ***
	# Developer mode - when source pulled from http://github.com/uSDLC/uSDLC/
	if [ -z "$1" ]
	then
		cat <<DONE
Usage:
    ./uSDLC build
    ./uSDLC clean build
    ./uSDLC run
    ./uSDLC clean run
DONE
	else
		# Run the ant task as defined in build.xml. Note that only a
		# Java JDK is required. Everything else is self-contained.
		# Look to build.xml for more developer enlightenment

		java -cp web/lib/jars/ant-launcher.jar -Dant.home=web/lib/jars org.apache.tools.ant.launch.Launcher -nouserlib $*
	fi
fi
