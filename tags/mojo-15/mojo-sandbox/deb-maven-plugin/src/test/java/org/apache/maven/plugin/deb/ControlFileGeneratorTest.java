package org.apache.maven.plugin.deb;

import org.codehaus.plexus.PlexusTestCase;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class ControlFileGeneratorTest
    extends PlexusTestCase
{
    public void testPackageName()
        throws Exception
    {
        ControlFileGenerator generator = (ControlFileGenerator) lookup( ControlFileGenerator.ROLE );

        generator.setGroupId( "myGroup" );
        generator.setArtifactId( "myArtifact" );

        assertEquals( "mygroup-myartifact", generator.getDebianPackageName() );
    }

    public void testDescription()
        throws Exception
    {
        ControlFileGenerator generator = (ControlFileGenerator) lookup( ControlFileGenerator.ROLE );

        // ----------------------------------------------------------------------
        // Description from POM.
        // ----------------------------------------------------------------------

        generator.setDescription( "Short description. Long description." );

        assertEquals( "Short description.\n" +
                      " Long description.", generator.getDebianDescription() );

        generator = (ControlFileGenerator) lookup( ControlFileGenerator.ROLE );

        // ----------------------------------------------------------------------
        // Short description is set.
        // ----------------------------------------------------------------------

        generator.setShortDescription( "My short description." );

        generator.setDescription( "Description." );

        assertEquals( "My short description.\n" +
                      " Description.", generator.getDebianDescription() );

        // ----------------------------------------------------------------------
        // A long description with blank lines.
        // ----------------------------------------------------------------------

        generator.setDescription(
            "Maven was originally started as an attempt to simplify the build \n" +
            "processes in the Jakarta Turbine project. There were several \n" +
            "projects each with their own Ant build files that were all \n" +
            "slightly different and JARs were checked into CVS. We wanted \n" +
            "a standard way to build the projects, a clear definition of \n" +
            "what the project consisted of, an easy way to publish \n" +
            "project information and a way to share JARs across several \n" +
            "projects.\n" +
            "\n" +
            "What resulted is a tool that can now be used for building and \n" +
            "managing any Java-based project. We hope that we have \n" +
            "created something that will make the day-to-day work of \n" +
            "Java developers easier and generally help with the \n" +
            "comprehension of any Java-based project." );

        assertEquals( "My short description.\n" +
            " Maven was originally started as an attempt to simplify the build\n" +
            " processes in the Jakarta Turbine project. There were several\n" +
            " projects each with their own Ant build files that were all\n" +
            " slightly different and JARs were checked into CVS. We wanted\n" +
            " a standard way to build the projects, a clear definition of\n" +
            " what the project consisted of, an easy way to publish\n" +
            " project information and a way to share JARs across several\n" +
            " projects.\n" +
            ".\n" +
            " What resulted is a tool that can now be used for building and\n" +
            " managing any Java-based project. We hope that we have\n" +
            " created something that will make the day-to-day work of\n" +
            " Java developers easier and generally help with the\n" +
            " comprehension of any Java-based project.",
                      generator.getDebianDescription() );
    }
}
