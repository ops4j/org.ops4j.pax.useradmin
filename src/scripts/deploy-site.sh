#!/bin/sh
#
# Deploys the generated site
#
while getopts ":m:P:s:" o ; do
   case $o in
     m ) MAVEN_BIN_DIR="$OPTARG/bin/" ;;
     P ) PROFILE_OPT="-P $OPTARG" ;;
     s ) SITE_DIR_OPT="-Dsite.deploy.dir=$OPTARG" ;;
     esac
done
#
${MAVEN_BIN_DIR}mvn ${PROFILE_OPT} ${SITE_DIR_OPT} site:deploy
