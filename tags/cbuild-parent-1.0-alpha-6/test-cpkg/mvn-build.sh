#!/bin/sh

dir=`pwd`
mvn="mvn -Pcbuilds"

echo "AS ROOT: be sure to run rpm-clean.sh first"

( cd $dir/parents; $mvn clean install )

( cd $dir/expat; $mvn clean deploy )
 
( cd $dir/log4c; $mvn clean deploy )
 
( cd $dir/test-myutil; $mvn clean deploy )

#commenting for a skeletal test run...
#( cd $dir/test-jrpm; $mvn clean deploy )

#( cd $dir/decNumber; $mvn clean deploy )

#( cd $dir/test-harvest; $mvn clean deploy )
