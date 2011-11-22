package org.codehaus.mojo.build;

/**
 * The MIT License
 *
 * Copyright (c) 2005 Learning Commons, University of Calgary
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
 * OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.plexus.PlexusTestCase;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.Locale;

public class TestBuildMojo
        extends PlexusTestCase
{

    protected void setUp()
        throws Exception
    {
        super.setUp();
    }


    public void testMessageFormat()
    {
        URL url = Thread.currentThread().getContextClassLoader().getResource("svnOutput-1.xml");
        File baseDir = new File(url.getPath()).getParentFile().getParentFile().getParentFile();

        assertTrue("Can't get the correct base dir: " + baseDir, new File(baseDir, "pom.xml").exists());

        BuildMojo mojo = new BuildMojo();
        mojo.setBasedir(baseDir);
        mojo.setFormat("At {1,time} on {1,date}, there was {2} on planet {0,number,integer}.");
        mojo.setItems(Arrays.asList(new Object[] {new Integer(7), "timestamp", "a disturbance in the Force"}));

        Locale currentLocale = Locale.getDefault();
        try
        {
            Locale.setDefault( Locale.US );

            mojo.execute();

            String rev = mojo.getRevision();

            System.out.println( "rev = " + rev );

            assertTrue( "Format didn't match.", rev.matches( "^At (\\d{1,2}:?){3} (AM|PM) on \\w{3} \\d{1,2}, \\d{4}, there was a disturbance in the Force on planet 7.") );

        } catch (MojoExecutionException e) {
            fail(e.toString());
        } catch (MojoFailureException e) {
            fail(e.toString());
        }
        finally
        {
            Locale.setDefault( currentLocale );
        }
    }

    public void testSequenceFormat()
    {
        URL url = Thread.currentThread().getContextClassLoader().getResource( "svnOutput-1.xml" );
        File baseDir = new File( url.getPath() ).getParentFile().getParentFile().getParentFile();

        assertTrue( "Can't get the correct base dir: " + baseDir, new File( baseDir, "pom.xml" ).exists() );

        BuildMojo mojo = new BuildMojo();
        mojo.setBasedir( baseDir );
        mojo.setFormat( "{0,number}.{1,number}.{2,number}" );
        mojo.setItems( Arrays.asList( new Object[]{"buildNumber0", "buildNumber1", "buildNumber2"} ) );

        try
        {
            mojo.execute();

            String rev = mojo.getRevision();

            System.out.println( "rev = " + rev );

            assertTrue( "Format didn't match.", rev.matches( "(\\d+\\.?){3}" ) );

            File file = new File(baseDir, "buildNumber.properties");
            assertTrue( file.exists() );

            // for tests, we don't want this hanging around
            file.delete();

        }
        catch ( MojoExecutionException e )
        {
            fail( e.toString() );
        }
        catch ( MojoFailureException e )
        {
            fail( e.toString() );
        }

    }

    public void testXMLParse() throws Exception {

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();

        InputStream testXML = Thread.currentThread().getContextClassLoader().getResourceAsStream("svnOutput-1.xml");

        Document document = builder.parse(testXML);

        Node entryNode = document.getDocumentElement().getElementsByTagName("entry").item(0);
        Node node = entryNode.getAttributes().getNamedItem("revision");

        assertEquals("78", node.getNodeValue());

    }


}
