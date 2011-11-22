#!/bin/bash

mvn="mvn -Pcbuilds -nsu"

# This script is meant to exercise the build-on-demand resolution features of 
# the c-builds plugin suite.


echo "Clearing environment before we start.\n\n"
##############################################################################
# Before we do anything, let's cleanup the environment.
##############################################################################
sudo ./rpm-clean.sh
rm -rf $LOCAL_REPO/$mvn_p6
rm -rf $LOCAL_REPO/org/libexpat $LOCAL_REPO/net/sourceforge




echo "Preparing remote repository.\n\n"
##############################################################################
# First, we have to deploy everything.
##############################################################################

# Deploy expat
$mvn -f expat/pom.xml clean deploy -e | tee expat/build.log

# Deploy log4c
$mvn -f log4c/pom.xml clean deploy -e | tee log4c/build.log

# Deploy test-myutil
$mvn -f test-myutil/pom.xml clean deploy -e | tee test-myutil/build.log



echo "Manually cleaning test-myutil project...this should be fixed...\n\n"
##############################################################################
# Next, we need to manually run `make distclean` on the test-myutil project.
# NOTE: This needs to be incorporated into the 'clean' phase of the Maven build.
##############################################################################

(cd test-myutil && make distclean)




echo "Building test-myutil project with binary-only resolution mode.\n\n"
##############################################################################
# Now, we'll clean out the RPM database, and test the binary-only resolution
# capabilities of build-on-demand.
##############################################################################

sudo ./rpm-clean.sh

$mvn -f test-myutil/pom.xml clean deploy -e -DbuildOnDemand.mode=binary-only | tee \
    test-myutil/build_binary-only.log





echo "Manually cleaning test-myutil project...this should be fixed...\n\n"
##############################################################################
# Again, we need to manually run `make distclean` on the test-myutil project.
# NOTE: This needs to be incorporated into the 'clean' phase of the Maven build.
##############################################################################

(cd test-myutil && make distclean)




echo "Building test-myutil project with build-on-demand (default) resolution mode.\n\n"
##############################################################################
# Now, we'll clean out the RPM database, local repository, and remote 
# repository, then test the build-on-demand (default) resolution mode.
##############################################################################

sudo ./rpm-clean.sh
rm -rf $LOCAL_REPO/org/libexpat $LOCAL_REPO/net/sourceforge
rm -rf `find $LOCAL_REPO/$mvn_p6 -type f -name '*.rpm'`

$mvn -f test-myutil/pom.xml clean deploy -e | tee test-myutil/build_build-on-demand.log




echo "Manually cleaning test-myutil project...this should be fixed...\n\n"
##############################################################################
# Once again, we need to manually run `make distclean` on the test-myutil project.
# NOTE: This needs to be incorporated into the 'clean' phase of the Maven build.
##############################################################################

(cd test-myutil && make distclean)




echo "Building test-myutil project with source-only resolution mode.\n\n"
##############################################################################
# Now, we'll clean out the RPM database, local repository, and remote 
# repository, then test the source-only resolution mode.
##############################################################################

sudo ./rpm-clean.sh
rm -rf $LOCAL_REPO/org/libexpat $LOCAL_REPO/net/sourceforge
rm -rf `find $LOCAL_REPO/$mvn_p6 -type f -name '*.rpm'`

$mvn -f test-myutil/pom.xml clean deploy -e -DbuildOnDemand.mode=source-only | tee \
    test-myutil/build_source-only.log

