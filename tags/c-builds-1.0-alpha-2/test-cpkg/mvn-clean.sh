#!/bin/sh

dir=`pwd`
mvn="mvn -Pcbuilds"

( cd $dir/parents; $mvn clean )

( cd $dir/expat; $mvn clean )
 
( cd $dir/log4c; $mvn clean )
 
( cd $dir/test-myutil; $mvn clean )

( cd $dir/test-jrpm; $mvn clean )

( cd $dir/decNumber; $mvn clean )

( cd $dir/test-harvest; $mvn clean )

find . -name "*~" -exec rm {} \;




