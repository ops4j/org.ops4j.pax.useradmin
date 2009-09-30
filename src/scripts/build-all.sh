#!/bin/sh
#
# Main build script.
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
if [ ! -z ${MAVEN_DIR} ] ; then
  MAVEN_OPT=-m ${MAVEN_DIR}
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
DEPLOY_SCRIPT=`dirname $0`/deploy.sh
DEPLOY_SITE_SCRIPT=`dirname $0`/deploy-site.sh
#
echo "running: ${MAVEN_BIN_DIR}mvn ${PROFILE_OPT} clean install site"
${MAVEN_BIN_DIR}mvn ${PROFILE_OPT} clean install site
## run this in a separate call???
### ${MAVEN_DIR}mvn ${PROFILE} site &&
if [ 0 != $? ]; then
  echo "error building project"
  exit -1
fi
if [ ! -z ${DO_DEPLOY} ]; then
  ${DEPLOY_SCRIPT} ${MAVEN_OPT} ${PROFILE_OPT}
fi
if [ 0 != $? ]; then
  echo "error deploying artifacts"
  exit -1
fi
#
${DEPLOY_SITE_SCRIPT} ${MAVEN_OPT} ${PROFILE_OPT} ${SITE_DIR_OPT}
if [ 0 != $? ]; then
  echo "error deploying site"
  exit -1
fi

