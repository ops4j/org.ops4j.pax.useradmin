#!/bin/sh
#
# Calls mvn:release.
#
# Synopsis:
#
# release.sh [-m <Maven root>] [-P <profiles>] <goal: prepare|perform>
#
while getopts "m:p:" o ; do
   case $o in
     m ) MAVEN_DIR="$OPTARG" ;;
     P ) PROFILES="$OPTARG" ;;
   esac
done
#
if [ ! -z ${MAVEN_DIR} ] ; then
  MAVEN_OPT=-m ${MAVEN_DIR}
  MAVEN_BIN_DIR=${MAVEN_DIR}/bin/
fi
#
if [ ! -z ${PROFILES} ]; then
  PROFILE_OPT="-P ${PROFILES}"
fi
#
shift $(($OPTIND - 1))
#
GOAL=$1
if [ "${GOAL}" = "prepare" ] || [ "${GOAL}" = "perform" ]; then
  RELEASE_GOAL="${GOAL}"
else
  echo stderr "Error: release goal must be 'prepare' or 'perform' not " ${GOAL}
  exit -1
fi
#
echo "running: ${MAVEN_BIN_DIR}mvn ${PROFILE_OPT} release:${RELEASE_GOAL}"
echo ${MAVEN_BIN_DIR}mvn ${PROFILE_OPT} release:${RELEASE_GOAL}
