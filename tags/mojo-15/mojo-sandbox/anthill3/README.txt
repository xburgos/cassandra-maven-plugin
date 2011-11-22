Overview
========

Provides support for Anthill3 (http://www.anthillpro.com), currently limited
to interaction with the Anthill3 Codestation repository.

Building
========

Since AH is a commercial product, there are no are no deployed artifacts in any
public Maven repositories.

You will need to have an installation archive of Anthill3 available and unpacked
and set the anthill.install property to that location for the build to pick up
and install artifacts required to build these plugins.

For example:

    gunzip anthill3-3.2.1.tar.gz | tar xf -
    mvn -Danthill3.install=`pwd`/anthill3-install
