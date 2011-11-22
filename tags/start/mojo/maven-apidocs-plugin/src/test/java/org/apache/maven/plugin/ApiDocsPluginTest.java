package org.apache.maven.plugin;

/*
 * LICENSE
 */

import junit.framework.TestCase;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class ApiDocsPluginTest
    extends TestCase
{
    public void testBasic()
        throws Exception
    {
        ApiDocsPlugin plugin = new ApiDocsPlugin();

        plugin.setSourceDirectory( "src/test/resources/test-source" );
        plugin.setOutputDirectory( "target/test-output" );

        plugin.execute();
    }
}
