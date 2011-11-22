
Test Cases
===========

1) expat
---------
Demonstrates the "standard use case". 
Grabs a remote source tarball, the full autotools cycle, and construction of a RPM

2) log4c
---------
Demonstrates the "standard use case". 
Grabs a remote source tarball, the full autotools cycle, and construction of a RPM
Adds the application of many Patches. 
Adds a single "first level" dependency

3) test-myutil
---------------
(Eventually) Demonstrates an autotools build from local /src instead of a source tarball
(Currently) Grabs a remote source tarball.
Contains two dependencies. Adds a "second level" "chained" dependency

4) test-jrpm
-------------
Demonstrates the "jrpm lifecycle"
Grabs a remote source tarball, applies patches, and constructs a RPM. 
No actual "build" takes place.

5) decNumber
-------------
Demonstrates the an "explicit build".In other words, when we cannot use autotools.
Grabs a remote source tarball, does an explicit build, applies a patch, and constructs a RPM. 

6) test-harvest
----------------
Demonstrates the "harvest lifecycle"
Grabs a remote source tarball, applies patches, and constructs a RPM. 
No actual "build" takes place because it simply sets all of the autotools steps to "skip"





