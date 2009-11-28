#!/bin/sh
#
# Main build script.
#
# - read cmdline options
#
while getopts "dm:P:s:" o ; do
   case $o in
     d ) DO_DEPLOY="true" ;;     # the artifacts are deployed to the repositories specified in the profiles - default: false
     m ) MAVEN_DIR="$OPTARG" ;;  # the path to the Maven root dir - default none
     P ) PROFILES="$OPTARG" ;;   # the profiles to use - default: none
     s ) SITE_DIR="$OPTARG" ;;   # the directory to which the site will be deployed - default see pom.xml
   esac
done
#
# - shift to end and collect additional parameters
#
#shift $OPTIND-1
#RAW_MAVEN_OPTS="$@"
#
# - provide recognized options as variables/subtask-parameters
#
if [ ! -z ${MAVEN_DIR} ] ; then
  MAVEN_OPT="-m ${MAVEN_DIR}"
  MAVEN_BIN_DIR=${MAVEN_DIR}/bin/
fi
#
if [ ! -z ${PROFILES} ]; then
  PROFILE_OPT="-P ${PROFILES}"
fi
#
if [ ! -z ${SITE_DIR} ]; then
  SITE_DIR_OPT="-s ${SITE_DIR}"
fi
#
# - identify scripts
#
DEPLOY_SCRIPT=`dirname $0`/deploy.sh
DEPLOY_SITE_SCRIPT=`dirname $0`/deploy-site.sh
#
# - run subtasks
#
## TODO: if actual version is not a snapshot ... exit(code) / TODO: code = 0 or error needs to be defined 
#
echo "running: ${MAVEN_BIN_DIR}mvn ${PROFILE_OPT} ${RAW_MAVEN_OPTS} clean"
${MAVEN_BIN_DIR}mvn ${RAW_MAVEN_OPTS} clean
if [ 0 != $? ]; then
  echo "error building project"
  exit -1
fi
#
echo "running: ${MAVEN_BIN_DIR}mvn ${PROFILE_OPT} ${RAW_MAVEN_OPTS} -Dmaven.test.skip=true install"
${MAVEN_BIN_DIR}mvn ${PROFILE_OPT} ${RAW_MAVEN_OPTS} -Dmaven.test.skip=true install
if [ 0 != $? ]; then
  echo "error building project"
  exit -1
fi
#
echo "running: ${MAVEN_BIN_DIR}mvn ${PROFILE_OPT} ${RAW_MAVEN_OPTS} site"
${MAVEN_BIN_DIR}mvn ${PROFILE_OPT} ${RAW_MAVEN_OPTS} site
if [ 0 != $? ]; then
  echo "error building project"
  exit -1
fi
#
#echo "running: ${MAVEN_BIN_DIR}mvn ${PROFILE_OPT} ${RAW_MAVEN_OPTS} clean install site"
#${MAVEN_BIN_DIR}mvn ${PROFILE_OPT} clean install site
#if [ 0 != $? ]; then
#  echo "error building project"
#  exit -1
#fi
#
if [ ! -z ${DO_DEPLOY} ]; then
  echo "running: ${DEPLOY_SCRIPT} ${MAVEN_OPT} ${PROFILE_OPT}"
  ${DEPLOY_SCRIPT} ${MAVEN_OPT} ${PROFILE_OPT}
  if [ 0 != $? ]; then
    echo "error deploying artifacts"
    exit -1
  fi
fi
#
echo "running: ${DEPLOY_SITE_SCRIPT} ${MAVEN_OPT} ${PROFILE_OPT} ${SITE_DIR_OPT}"
${DEPLOY_SITE_SCRIPT} ${MAVEN_OPT} ${PROFILE_OPT} ${SITE_DIR_OPT}
if [ 0 != $? ]; then
  echo "error deploying site"
  exit -1
fi
