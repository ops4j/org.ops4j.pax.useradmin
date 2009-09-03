#!/bin/sh
#
while getopts ":M:P:D:" o ; do
   case $o in
     M ) MAVEN_DIR="$OPTARG/bin/" ;;
     P ) PROFILE="-P$OPTARG" ;;
     D ) DEPLOY_DIR="-D$OPTARG" ;;
     esac
done
#
if [ ! -z ${MAVEN_DIR} ] ; then
  MAVEN_OPT=-M${MAVEN_DIR}
else
  MAVEN_OPT=
fi
#
DEPLOY_SCRIPT=`dirname $0`/deploy-site.sh
${MAVEN_DIR}mvn ${PROFILE} clean install && ${MAVEN_DIR}mvn ${PROFILE} site && ${DEPLOY_SCRIPT} ${MAVEN_OPT} ${PROFILE} ${DEPLOY_DIR}
