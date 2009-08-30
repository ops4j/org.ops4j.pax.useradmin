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
${MAVEN_DIR}mvn ${PROFILE} clean install site && ${DEPLOY_SCRIPT} ${PROFILE} ${DEPLOY_DIR}


#DEPLOY_SCRIPT=`dirname $0`/deploy-site.sh
#echo "running: /java/maven/bin/mvn clean install site && ${DEPLOY_SCRIPT} $1"
#/java/maven/bin/mvn clean install site && ${DEPLOY_SCRIPT} $1
