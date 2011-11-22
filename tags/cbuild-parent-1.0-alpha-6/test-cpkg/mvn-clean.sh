#!/bin/sh

dir=`pwd`
mvn="mvn -Pcbuilds"

( cd $dir/parents; $mvn post-clean )

( cd $dir/expat; $mvn post-clean )
 
( cd $dir/log4c; $mvn post-clean )
 
( cd $dir/test-myutil; $mvn post-clean )

#commenting for a skeletal test...
#( cd $dir/test-jrpm; $mvn post-clean )

#( cd $dir/decNumber; $mvn post-clean )

#( cd $dir/test-harvest; $mvn post-clean )

find . -name "*~" -exec rm {} \;




