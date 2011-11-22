#!/bin/bash

#-------------------------------------------------------------------------------#

mvn="mvn -nsu"

expat=org/libexpat
log4c=net/sourceforge/log4c

remoteRepo=$HOME/mvn_p6
remoteExpat=$remoteRepo/$expat
remoteLog4c=$remoteRepo/$log4c

localRepo=$HOME/.m2/repository
localExpat=$localRepo/$expat
localLog4c=$localRepo/$log4c

function cleanLocalRepo
{
  rm -rf $localExpat $localLog4c
}

function cleanRemoteRepo
{
  rm -rf $remoteExpat $remoteLog4c
}

function cleanRemoteRepoRPMs
{
  rm -rf `find $remoteExpat -type f -name '*.rpm'`        
  rm -rf `find $remoteLog4c -type f -name '*.rpm'`        
}

function cleanOSRPMs
{
  sudo ./rpm-clean.sh        
}

function cleanParents()
{
  rm -rf $localRepo/com/myco/m2        
}

#-------------------------------------------------------------------------------#

# Start from scratch
cleanOSRPMs()
cleanParents()
cleanLocalRepo()
cleanRemoteRepo()

#-------------------------------------------------------------------------------#

# Install parents
( cd parents; mvn clean install )

#-------------------------------------------------------------------------------#

# dependency of test-myutil which is installed in the local repository and placed
# in the RPM database on the OS
( cd expat; $mvn clean deploy )

# dependency of test-myutil which is installed in the local repository and placed
# in the RPM database on the OS
( cd log4c; $mvn clean deploy )

# Now test test-myutil will use the RPMS that are installed locally
( cd test-myutil; $mvn clean deploy )

#-------------------------------------------------------------------------------#

# Clean up for next phase
(cd test-myutil && make distclean)
cleanOSRPMs()

#-------------------------------------------------------------------------------#

# Here we have removed the RPMs from the OS, but we have the RPM binaries
# in the local repository so they will be installed into the RPM database again
# before we start this build. 

( cd test-myutil; $mvn clean deploy -DbuildOnDemand.mode=binary-only )

#-------------------------------------------------------------------------------#

# Clean up for next phase
(cd test-myutil && make distclean)
cleanOSRPMs()
cleanLocalRepo()
cleanRemoteRepoRPMs()

#-------------------------------------------------------------------------------#

# Here we have removed the RPMs from the OS, and removed them from the local
# repository so the sources will be downloaded again, compiled, RPMs built,
# installed in the local repo and the OS.

( cd test-myutil; $mvn clean deploy )

#-------------------------------------------------------------------------------#

(cd test-myutil && make distclean)
cleanOSRPMs()
cleanLocalRepo()
cleanRemoteRepoRPMs()

#-------------------------------------------------------------------------------#

# We clean up again and try the same thing using the source-only flag.

( cd test-myutil; $mvn clean deploy -DbuildOnDemand.mode=source-only )
