#!/bin/sh
#
# Builds the dashboard and deploys the generated site
#
while getopts ":M:P:D:" o ; do
   case $o in
     M ) MAVEN_DIR="$OPTARG/bin/" ;;
     P ) PROFILE="-P $OPTARG" ;;
     D ) DEPLOY_DIR="-Dsite.deploy.dir=$OPTARG" ;;
     esac
done
#
${MAVEN_DIR}mvn ${PROFILE},repos.ops4j deploy && ${MAVEN_DIR}mvn ${PROFILE} dashboard:dashboard && ${MAVEN_DIR}mvn ${PROFILE} ${DEPLOY_DIR} site:deploy
