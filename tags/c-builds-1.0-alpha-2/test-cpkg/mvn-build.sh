#!/bin/sh

dir=`pwd`
mvn="mvn -Pcbuilds"

echo "AS ROOT: be sure to run rpm-clean.sh first"

( cd $dir/parents; $mvn clean install )

( cd $dir/expat; $mvn clean install )
 
( cd $dir/log4c; $mvn clean install )
 
( cd $dir/test-myutil; $mvn clean install )

( cd $dir/test-jrpm; $mvn clean install )

( cd $dir/decNumber; $mvn clean install )

( cd $dir/test-harvest; $mvn clean install )
