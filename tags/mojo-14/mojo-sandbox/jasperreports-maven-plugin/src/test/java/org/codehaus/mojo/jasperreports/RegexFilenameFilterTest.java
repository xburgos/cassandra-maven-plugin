package org.codehaus.mojo.jasperreports;

import junit.framework.TestCase;

import java.io.File;
import java.io.IOException;
import java.util.regex.Pattern;

/**
 * @author gjoseph
 * @author $Author: $ (last edit)
 * @version $Revision: $
 */
public class RegexFilenameFilterTest
    extends TestCase
{
    private File dir;

    protected void tearDown()
        throws Exception
    {
        super.tearDown();
        if ( dir != null && dir.exists() )
        {
            assertTrue( "could not remove temp dir" + dir.getAbsolutePath(), dir.delete() );
        }
    }

    public void testDirectoriesShouldAlwaysBeAcceptedWhetherTheyMatchThePattern()
        throws IOException
    {
        RegexFilenameFilter filter = new RegexFilenameFilter( Pattern.compile( "(.*)\\.jrxml" ) );

        dir = new File( "foo.bar" );
        assertTrue( dir.mkdir() );
        assertTrue( filter.accept( dir ) );
    }

    public void testDirectoriesShouldAlwaysBeAcceptedWhetherTheyMatchThePatternOrNot()
        throws IOException
    {
        RegexFilenameFilter filter = new RegexFilenameFilter( Pattern.compile( "(.*)\\.jrxml" ) );

        dir = new File( "test.jrxml" );
        assertTrue( dir.mkdir() );
        assertTrue( filter.accept( dir ) );
    }

    public void testFilesShouldMatchRegexToBeAccepted()
    {
        RegexFilenameFilter filter = new RegexFilenameFilter( Pattern.compile( "(.*)\\.jrxml" ) );
        assertTrue( filter.accept( new File( "foo.jrxml" ) ) );
        assertFalse( filter.accept( new File( "foo.bar" ) ) );
    }
}
