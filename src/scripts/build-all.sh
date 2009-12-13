#!/bin/sh
#
# Main build script.
#
# - read cmdline options
#
echo "Dynamic configuration:"
while getopts "dm:P:s:" o ; do
   case $o in
     d ) DO_DEPLOY="true";    echo "set DO_DEPLOY='true'" ;;  # the artifacts are deployed to the repositories specified in the profiles - default: false
     m ) MAVEN_DIR="$OPTARG"; echo "set MAVEN_DIR=$OPTARG";;  # the path to the Maven root dir - default none
     P ) PROFILES="$OPTARG";  echo "set PROFILES=$OPTARG" ;;  # a comma-separated list of profiles to use - default: none
     s ) SITE_DIR="$OPTARG";  echo "set SITE_DIR=$OPTARG" ;;  # the directory to which the site will be deployed - default see pom.xml
   esac
done
#
# - shift to end and collect additional parameters
#
#shift $OPTIND-1
#RAW_MAVEN_OPTS="$@"
#
# - digest recognized options and provide them as variables/subtask-parameters
#
if [ ! -z ${MAVEN_DIR} ] ; then
  MAVEN_BIN_DIR=${MAVEN_DIR}/bin/
  MAVEN_OPT="-m ${MAVEN_DIR}"
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
# - retrieve current version from pom.xml
#
#   Note: relies on 
#         1. the parent pom is specified at the top of the POM file
#         2. the project version following the parent section with
#            no other <version> tag in between
#
current_version=$(awk ' \
    BEGIN { parentRead=0; } \
    { \
     if ( $0 ~ /<\/parent>/ ) parentRead=1; \
     if (parentRead == 1 && $0 ~ /<version>/ ) { \
       split($0, arrayWithoutStartTag, ">"); \
       split(arrayWithoutStartTag[2], arrayWithoutEndTag, "<"); \
       print arrayWithoutEndTag[1]; \
       exit 0; \
     } \
    }' pom.xml)
#
# - check current version: if it's not a SNAPSHOT version then don't 
#   build it if the 'continuous-integration' profile is specified. 
#   Otherwise proceed as usual.
#
#   Used to avoid continuous builds while creating a release
#
if    [ ! -z `echo ${PROFILES} | grep "continuous-integration"` ] \
    & [ -z `echo ${current_version} | grep "SNAPSHOT"` ]; then
    echo "Refusing to build non-SNAPSHOT version: ${current_version}"
    exit -1 # TODO: or should we exit with 0?
fi
#
echo "Building version $current_version"
#
# - clean project
#
echo "running: ${MAVEN_BIN_DIR}mvn ${PROFILE_OPT} clean"
${MAVEN_BIN_DIR}mvn ${PROFILE_OPT} clean
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
${MAVEN_BIN_DIR}mvn ${PROFILE_OPT} jxr:jxr
if [ 0 != $? ]; then
  echo "error creating source xref"
  exit -1
fi
${MAVEN_BIN_DIR}mvn ${PROFILE_OPT} jxr:test-jxr
if [ 0 != $? ]; then
  echo "error creating test source xref"
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
  echo "running: ${DEPLOY_SCRIPT} ${MAVEN_OPT} ${PROFILE_OPT} "
  ${DEPLOY_SCRIPT} ${MAVEN_OPT} ${PROFILE_OPT}
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
