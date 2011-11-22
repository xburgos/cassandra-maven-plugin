$Id$

Building
========

Simply run Maven ;-)

    mvn install


Integration Testing
===================

To invoke integration tests using the SHITTY plugin you need to:

    mvn -Dshit


IDE Configuration
=================

To generate IDEA configuration w/sources & javadocs:

    mvn -Pidea

Or the basic configuration:

    mvn idea:idea


Site Generation
===============

You need to perform a full build first, before site generation will function
correctly:

    mvn install
    mvn site

To generate the full site locally for review:

    mvn site-deploy -Dstage.distributionUrl=file:`pwd`/dist

And then open up the main index in a browser, as in:

    open dist/site/index.html


