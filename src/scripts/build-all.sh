#!/bin/sh
#
while getopts ":M:P:D:" o ; do
   case $o in
     M ) MAVEN_DIR=$OPTARG/bin/ ;;
     P ) PROFILE="-P$OPTARG" ;;
     D ) DEPLOY_DIR="-D$OPTARG" ;;
     esac
done
#
DEPLOY_SCRIPT=`dirname $0`/deploy-site.sh
${MAVEN_DIR}mvn ${PROFILE} clean install site && ${DEPLOY_SCRIPT} -M${MAVEN_DIR} ${PROFILE} ${DEPLOY_DIR}
