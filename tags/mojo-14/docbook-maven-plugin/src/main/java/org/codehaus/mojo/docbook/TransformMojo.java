/* 
 * maven-docbook-plugin - Copyright (C) 2005 OPEN input - http://www.openinput.com/
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *   
 * $Id$
 */
package org.codehaus.mojo.docbook;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.settings.Proxy;
import org.apache.maven.settings.Settings;
import org.codehaus.plexus.compiler.util.scan.InclusionScanException;

/**
 * Transforms a set of Docbook files into XHTML output. Currently there is only support for
 * XHTML output, though is planned to add all kind of outputs available in the standard 
 * stylesheets. 
 * 
 * @author jgonzalez
 * @goal transform
 * @description Transform Docbook files into XHTML output
 */
public class TransformMojo
    extends AbstractMojo
{
    private static String XERCES_PARSER_CONFIG = "org.apache.xerces.xni.parser.XMLParserConfiguration";

    private static String XERCES_XINCLUDE_PARSER = "org.apache.xerces.parsers.XIncludeParserConfiguration";

    /**
     * Directory where the source Docbook files are located.
     *  
     * @parameter expression="${basedir}/src/docbook"
     * @required
     */
    private File sourceDirectory;

    /**
     * Directory where the resource files are located.
     *  
     * @parameter expression="${basedir}/src/docbook/resources"
     * @required
     */
    private File resourceDirectory;

    /**
     * Work directory where the olink database will be generated.
     * 
     * @parameter expression="${project.build.directory}/docbook"
     * @required
     */
    private File databaseDirectory;

    /**
     * Target directory where the resulting files will be placed.
     * 
     * @parameter expression="${project.build.directory}/site/docbook"
     * @required
     */
    private File outputDirectory;

    /**
     * Specifies the output encoding.
     * 
     * @parameter expression="${outputEncoding}" default-value="UTF-8"
     */
    private String outputEncoding;

    /**
     * Specifies the stylesheet location, useful if you want to use a local copy or a 
     * specific version instead of the current release from the Internet.
     * 
     * @parameter expression="${stylesheetLocation}" default-value="http://docbook.sourceforge.net/release/xsl/current/"
     */
    private String stylesheetLocation;

    /**
     * @parameter expression="${settings}"
     * @required
     * @readonly
     */
    private Settings settings;

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.maven.plugin.Mojo#execute()
     */
    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        Log log = this.getLog();

        Proxy activeProxy = this.settings.getActiveProxy();
        String httpProxyHost = System.getProperty( "http.proxyHost" );
        String httpProxyPort = System.getProperty( "http.proxyPort" );
        String httpNonProxyHosts = System.getProperty( "http.nonProxyHosts" );
        if ( activeProxy != null )
        {
            System.setProperty( "http.proxyHost", activeProxy.getHost() );
            System.setProperty( "http.proxyPort", new Integer( activeProxy.getPort() ).toString() );
            System.setProperty( "http.nonProxyHosts", activeProxy.getNonProxyHosts() );
        }

        // Set XInclude Xerces parser so we're able to process master olink database file
        String xercesParser = System.getProperty( TransformMojo.XERCES_PARSER_CONFIG );
        System.setProperty( TransformMojo.XERCES_PARSER_CONFIG, TransformMojo.XERCES_XINCLUDE_PARSER );

        try
        {
            URI stylesheetLocationURI = new URI( this.stylesheetLocation );
            OLinkDBUpdater olinkDBUpdater = new OLinkDBUpdater( log, this.sourceDirectory, this.databaseDirectory,
                                                                stylesheetLocationURI );
            DocumentTransformer documentTransformer = new DocumentTransformer( log, this.sourceDirectory,
                                                                               this.resourceDirectory,
                                                                               this.databaseDirectory,
                                                                               this.outputDirectory,
                                                                               stylesheetLocationURI );
            olinkDBUpdater.update();
            documentTransformer.transform();
        }
        catch ( TransformerFactoryConfigurationError exc )
        {
            throw new MojoExecutionException( exc.getLocalizedMessage(), exc );
        }
        catch ( TransformerException exc )
        {
            throw new MojoFailureException( exc.getLocalizedMessage() );
        }
        catch ( URISyntaxException exc )
        {
            throw new MojoExecutionException( exc.getLocalizedMessage(), exc );
        }
        catch ( IOException exc )
        {
            throw new MojoExecutionException( exc.getLocalizedMessage(), exc );
        }
        catch ( InclusionScanException exc )
        {
            throw new MojoExecutionException( exc.getLocalizedMessage(), exc );
        }
        finally
        {
            // Reset XInclude Xerces parser to previous value
            if ( xercesParser != null )
            {
                System.setProperty( TransformMojo.XERCES_PARSER_CONFIG, xercesParser );
            }
            else
            {
                // In 1.4 there's no clear property method... is this correct?
                System.setProperty( TransformMojo.XERCES_PARSER_CONFIG, "" );
            }

            if ( httpProxyHost != null )
            {
                System.setProperty( "http.proxyHost", httpProxyHost );
                System.setProperty( "http.proxyPort", httpProxyPort );
                System.setProperty( "http.nonProxyHosts", httpNonProxyHosts );
            }
            else
            {
                System.setProperty( "http.proxyHost", "" );
                System.setProperty( "http.proxyPort", "" );
                System.setProperty( "http.nonProxyHosts", "" );
            }
        }
    }
}
