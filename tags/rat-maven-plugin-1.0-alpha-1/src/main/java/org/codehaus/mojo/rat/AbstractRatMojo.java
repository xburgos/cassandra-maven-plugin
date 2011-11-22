package org.codehaus.mojo.rat;

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.transform.TransformerConfigurationException;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.plexus.util.DirectoryScanner;

import rat.Defaults;
import rat.element.ArchiveElement;
import rat.element.BinaryElement;
import rat.element.ElementTypeEnum;
import rat.element.IElementConsumer;
import rat.element.IElementContainer;
import rat.element.NoteElement;
import rat.element.StandardContentElement;
import rat.license.ILicenseMatcher;
import rat.license.util.MatcherMultiplexer;


/**
 * Abstract base class for Mojos, which are running RAT.
 */
public abstract class AbstractRatMojo extends AbstractMojo
{
    /**
     * The Maven specific default excludes.
     */
    public static final String[] MAVEN_DEFAULT_EXCLUDES =
        new String[]{"target/**/*", "cobertura.ser"};

    /**
     * The Eclipse specific default excludes.
     */
    public static final String[] ECLIPSE_DEFAULT_EXCLUDES =
        new String[]{".classpath", ".project", ".settings/**/*"};

    /**
     * The base directory, in which to search for files.
     * 
     * @parameter expression="${rat.basedir}" default-value="${basedir}"
     * @required
     */
    protected File basedir;

    /**
     * The licenses we want to match on.
     * 
     * @parameter
     */
    private LicenseMatcherSpecification[] licenseMatchers;

    /**
     * Whether to add the default list of license matchers.
     * 
     * @parameter expression="${rat.addDefaultLicenseMatchers}" default-value="true"
     */
    private boolean addDefaultLicenseMatchers;

    /**
     * Specifies files, which are included in the report. By default, all files are included.
     * 
     * @parameter
     */
    private String[] includes;

    /**
     * Specifies files, which are excluded in the report. By default, no files are included.
     * 
     * @parameter
     */
    private String[] excludes;

    /**
     * Whether to use the default excludes when scanning for files.
     * 
     * @parameter expression="${rat.useDefaultExcludes}" default-value="true"
     */
    private boolean useDefaultExcludes;

    /**
     * Whether to use the Maven specific default excludes when
     * scanning for files. Maven specific default excludes are
     * given by the constant MAVEN_DEFAULT_EXCLUDES: The target
     * directory, the cobertura.ser file, and so on.
     *
     * @parameter expression="${rat.useMavenDefaultExcludes}" default-value="true"
     */
    private boolean useMavenDefaultExcludes;

    /**
     * Whether to use the Eclipse specific default excludes when
     * scanning for files. Eclipse specific default excludes are
     * given by the constant ECLIPSE_DEFAULT_EXCLUDES: The
     * .classpath and .project files, the .settings directory,
     * and so on.
     *
     * @parameter expression="${rat.useEclipseDefaultExcludes}" default-value="true"
     */
    private boolean useEclipseDefaultExcludes;

    /**
     * Returns the set of {@link ILicenseMatcher} to use.
     * @throws MojoFailureException An error in the plugin configuration was detected.
     * @throws MojoExecutionException An error occurred while calculating the result.
     * @return Array of license matchers to use
     */
    protected ILicenseMatcher[] getLicenseMatchers() throws MojoExecutionException, MojoFailureException
    {
        final List list = new ArrayList();

        if ( licenseMatchers != null )
        {
            for ( int i = 0; i < licenseMatchers.length; i++ )
            {
                final LicenseMatcherSpecification spec = licenseMatchers[i];
                final String className = spec.getClassName();
                final ILicenseMatcher licenseMatcher;
                try
                {
                    final ClassLoader cl = Thread.currentThread().getContextClassLoader();
                    licenseMatcher = (ILicenseMatcher) cl.loadClass( className ).newInstance();
                }
                catch ( InstantiationException e )
                {
                    throw new MojoExecutionException( "Failed to instantiate license matcher class " + className + ": "
                                    + e.getMessage(), e );
                }
                catch ( ClassCastException e )
                {
                    throw new MojoExecutionException(
                                                      "The license matcher class " + className
                                                                      + " is not implementing "
                                                                      + ILicenseMatcher.class.getName() + ": "
                                                                      + e.getMessage(), e );
                }
                catch ( IllegalAccessException e )
                {
                    throw new MojoExecutionException( "Illegal access to license matcher class " + className + ": "
                                    + e.getMessage(), e );
                }
                catch ( ClassNotFoundException e )
                {
                    throw new MojoExecutionException( "License matcher class " + className + " not found: "
                                    + e.getMessage(), e );
                }
                list.add( licenseMatcher );
            }
        }

        if ( addDefaultLicenseMatchers )
        {
            list.addAll( Arrays.asList( Defaults.DEFAULT_MATCHERS ) );
        }

        if ( list.size() == 0 )
        {
            throw new MojoFailureException( "No license matchers specified." );
        }

        return (ILicenseMatcher[]) list.toArray( new ILicenseMatcher[list.size()] );
    }

    /**
     * Used to determine, whether a file has binary content.
     * @param pFile The file to check
     * @return True, if the file has binary content, otherwise false.
     */
    boolean isBinaryFile( File pFile )
    {
        boolean result = false;
        FileInputStream in = null;
        try
        {
            in = new FileInputStream( pFile );
            result = BinaryElement.isBinary( in );
            in.close();
            in = null;
        }
        catch ( IOException ex )
        {
            // Ignore me
        }
        finally
        {
            if ( in != null )
            {
                try
                {
                    in.close();
                }
                catch ( Throwable t )
                {
                    // Ignore me
                }
            }
        }
        return result;
    }

    /**
     * Adds the given string array to the list.
     * @param pList The list to which the array elements are being added.
     * @param pArray The strings to add to the list.
     */
    private void add( List pList, String[] pArray )
    {
        if ( pArray != null )
        {
            for ( int i = 0;  i < pArray.length;  i++ )
            {
                pList.add( pArray[i] );
            }
        }
    }

    /**
     * Creates an iterator over the files to check.
     * @return A container of files, which are being checked.
     */
    protected IElementContainer getResources()
    {
        DirectoryScanner ds = new DirectoryScanner();
        ds.setBasedir( basedir );
        final List excludeList = new ArrayList();
        if ( useDefaultExcludes )
        {
            add( excludeList, DirectoryScanner.DEFAULTEXCLUDES );
        }
        if ( useMavenDefaultExcludes )
        {
            add( excludeList, MAVEN_DEFAULT_EXCLUDES );
        }
        if ( useEclipseDefaultExcludes )
        {
            add( excludeList, ECLIPSE_DEFAULT_EXCLUDES );
        }
        add( excludeList, excludes );
        if ( !excludeList.isEmpty() )
        {
            String[] allExcludes = (String[])
                excludeList.toArray( new String[ excludeList.size() ] );
            ds.setExcludes( allExcludes );
        }
        if ( includes != null )
        {
            ds.setIncludes( includes );
        }
        ds.scan();
        final String[] files = ds.getIncludedFiles();

        return new IElementContainer()
        {
            public void consumeContents( IElementConsumer consumer )
            {
                for ( int i = 0; i < files.length; i++ )
                {
                    final File f = new File( basedir, files[i] );
                    final String name = f.getName();
                    ElementTypeEnum guess = ElementTypeEnum.guessType( name );

                    if ( guess == ElementTypeEnum.NOTE )
                    {
                        consumer.consume( new NoteElement( name ) );
                    }
                    else if ( guess == ElementTypeEnum.BINARY )
                    {
                        consumer.consume( new BinaryElement( name ) );
                    }
                    else if ( guess == ElementTypeEnum.ARCHIVE )
                    {
                        consumer.consume( new ArchiveElement( f ) );
                    }
                    else if ( guess == ElementTypeEnum.STANDARD_CONTENT )
                    {
                        consumer.consume( new StandardContentElement( f ) );
                    }
                    else
                    {
                        if ( isBinaryFile( f ) )
                        {
                            consumer.consume( new BinaryElement( name ) );
                        }
                        else
                        {
                            consumer.consume( new StandardContentElement( f ) );
                        }
                    }
                }
            }
        };
    }

    /**
     * Writes the report to the given stream.
     * @param out The target writer, to which the report is being written.
     * @throws MojoFailureException An error in the plugin configuration was detected.
     * @throws MojoExecutionException Another error occurred while creating the report.
     */
    protected void createReport( PrintWriter out )
            throws MojoExecutionException, MojoFailureException
    {
        MatcherMultiplexer m = new MatcherMultiplexer( getLicenseMatchers() );
        try
        {
            rat.Report.report( out, getResources(), Defaults.getDefaultStyleSheet(), m );
        }
        catch ( TransformerConfigurationException e )
        {
            throw new MojoExecutionException( e.getMessage(), e );
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( e.getMessage(), e );
        }
        catch ( InterruptedException e )
        {
            throw new MojoExecutionException( e.getMessage(), e );
        }
    }
}
