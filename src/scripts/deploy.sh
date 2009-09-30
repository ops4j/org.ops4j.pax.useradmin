#!/bin/sh
#
# Deploys the generated artifacts
#
while getopts ":m:P:" o ; do
   case $o in
     m ) MAVEN_BIN_DIR="$OPTARG/bin/" ;;
     P ) PROFILE_OPT="-P $OPTARG," ;;
   esac
done
#
${MAVEN_BIN_DIR}mvn ${PROFILE_OPT} deploy
