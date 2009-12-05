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
     P ) PROFILES="$OPTARG" ;;   # a comma-separated list of profiles to use - default: none
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
base_dir=$(dirname $0)
DEPLOY_SCRIPT=${base_dir}/deploy.sh
DEPLOY_SITE_SCRIPT=${base_dir}/deploy-site.sh
#
# - run subtasks
#
## TODO: if actual version is not a snapshot ... exit(code) / TODO: code = 0 or error needs to be defined 
#
# - clean project
#
echo "running: ${MAVEN_BIN_DIR}mvn ${PROFILE_OPT} clean"
${MAVEN_BIN_DIR}mvn clean
if [ 0 != $? ]; then
  echo "error building project"
  exit -1
fi
#
# - build project (w/o tests)
#
echo "running: ${MAVEN_BIN_DIR}mvn ${PROFILE_OPT} -Dmaven.test.skip=true install"
${MAVEN_BIN_DIR}mvn ${PROFILE_OPT} -Dmaven.test.skip=true install
if [ 0 != $? ]; then
  echo "error building project"
  exit -1
fi
#
# - create site (includes running tests and creating reports)
#
echo "running: ${MAVEN_BIN_DIR}mvn ${PROFILE_OPT} site"
${MAVEN_BIN_DIR}mvn ${PROFILE_OPT} site
if [ 0 != $? ]; then
  echo "error building project site"
  exit -1
fi
#
# - create the site dashboard (aggregating some of the reports)
#
echo "running ${MAVEN_BIN_DIR}mvn ${PROFILE_OPT} dashboard:dashboard"
${MAVEN_BIN_DIR}mvn ${PROFILE_OPT} dashboard:dashboard
if [ 0 != $? ]; then
  echo "error building project site dashboard"
  exit -1
fi
#
# - deploy artifacts to the configured remote repository (w/o running tests again)
#
if [ ! -z ${DO_DEPLOY} ]; then
  echo "running: ${DEPLOY_SCRIPT} ${MAVEN_OPT} ${PROFILE_OPT} -Dmaven.test.skip=true "
  ${DEPLOY_SCRIPT} ${MAVEN_OPT} ${PROFILE_OPT} -Dmaven.test.skip=true 
  if [ 0 != $? ]; then
    echo "error deploying artifacts"
    exit -1
  fi
fi
#
# - deploy the site (defaults to /tmp/sites/<projectname>
#
echo "running: ${DEPLOY_SITE_SCRIPT} ${MAVEN_OPT} ${PROFILE_OPT} ${SITE_DIR_OPT}"
${DEPLOY_SITE_SCRIPT} ${MAVEN_OPT} ${PROFILE_OPT} ${SITE_DIR_OPT}
if [ 0 != $? ]; then
  echo "error deploying site"
  exit -1
fi
