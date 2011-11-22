package org.codehaus.mojo.castor;

/*
 * Copyright 2005 The Codehaus.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.compiler.util.scan.InclusionScanException;
import org.codehaus.plexus.compiler.util.scan.SourceInclusionScanner;
import org.codehaus.plexus.compiler.util.scan.StaleSourceScanner;
import org.codehaus.plexus.compiler.util.scan.mapping.SourceMapping;
import org.codehaus.plexus.compiler.util.scan.mapping.SuffixMapping;
import org.codehaus.plexus.util.FileUtils;

/**
 * A mojo that uses Castor to generate a collection of javabeans from an XSD.  Detailed
 * explanations of many of these can be found in the details for the Castor 
 * <a href="http://castor.codehaus.org/sourcegen.html">SourceGenerator</a>.
 * 
 * @goal generate
 * @phase generate-sources
 * @description Castor plugin
 * @author brozow <brozow@opennms.org>
 * @author jesse <jesse.mcconnell@gmail.com>
 */
public class GenerateMojo
    extends AbstractMojo
{

    private static final String DISABLE_DESCRIPTORS_MSG = "Disabling generation of Class descriptors";

    private static final String DISABLE_MARSHALL_MSG = "Disabling generation of Marshalling framework methods (marshall, unmarshall, validate).";

    /**
     * The binding file to use for mapping xml to java.
     * 
     * @parameter expression="${basedir}/src/main/castor/bindings.xml"
     */
    private String bindingfile;

    /**
     * A schema file to process.  If this is not set then all .xsd files in
     * schemaDirectory will be processed.
     * 
     * @parameter
     */
    private String schema;

    /**
     * The source directory containing *.xsd files
     * 
     * @parameter expression="${basedir}/src/main/castor"
     */
    private String schemaDirectory;

    /**
     * The directory to output the generated sources to
     * 
     * @parameter expression="${project.build.directory}/generated-sources/castor"
     * @todo This would be better as outputDirectory but for backward compatibility I left it as dest
     */
    private String dest;

    /**
     * The directory to store the processed xsds.  The timestamps of these xsds
     * are used to determine if the source for that xsd need to be regenerated
     * 
     * @parameter expression="${basedir}/target/xsds"
     * @todo timestampDirectory would be a better name for this. Used this name for backward compatibility
     */
    private String tstamp;

    /**
     * The granularity in milliseconds of the last modification
     * date for testing whether a source needs recompilation
     * 
     * @parameter expression="${lastModGranularityMs}" default-value="0"
     */
    private int staleMillis = 0;

    /**
     * Castor collection types. Allowable values are 'vector', 'arraylist', 'j2' or 'odmg'
     * 'j2' and 'arraylist' are the same.
     * @parameter default-value="arraylist";
     */
    private String types = "arraylist";

    /**
     * If true, generate descriptors
     * @parameter default-value="true"
     */
    private boolean descriptors = true;

    /**
     * Verbose output during generation
     * @parameter default-value="false"
     */
    private boolean verbose = false;

    /**
     * Enable warning messages
     * @parameter default-value="false"
     */
    private boolean warnings = false;

    /**
     * if false, don't generate the marshaller
     * @parameter default-value="true"
     */
    private boolean marshal = true;

    /**
     * The line separator to use in generated source. Can be either win, unix, or mac
     * @parameter 
     */
    private String lineSeparator;

    /**
     * The castorbuilder.properties file to use
     * @parameter expression="${basedir}/src/main/castor/castorbuilder.properties"
     */
    private String properties;

    /**
     * The package for the generated source
     * @parameter
     */
    private String packaging;

    /**
     * @parameter expression="${project}"
     * @required
     */
    private MavenProject project;

    private CastorSourceGenerator sgen;

    public void execute()
        throws MojoExecutionException
    {

        if ( !FileUtils.fileExists( dest ) )
        {
            FileUtils.mkdir( dest );
        }

        Set staleXSDs = computeStaleXSDs();

        if ( staleXSDs.isEmpty() )
        {
            getLog().info( "Nothing to process - all xsds are up to date" );
            project.addCompileSourceRoot( dest );
            return;
        }

        config();

        for ( Iterator i = staleXSDs.iterator(); i.hasNext(); )
        {
            File xsd = (File) i.next();

            try
            {

                processFile( xsd.getCanonicalPath() );
                // make sure this is after the acutal processing, 
                //otherwise it if fails the computeStaleXSDs will think it completed.
                FileUtils.copyFileToDirectory( xsd, getTimeStampDirectory() );
            }
            catch ( Exception e )
            {
                throw new MojoExecutionException( "Castor execution failed", e );
            }
            catch ( Throwable t )
            {
                throw new MojoExecutionException( "Castor execution failed", t );
            }
        }

        if ( project != null )
        {
            project.addCompileSourceRoot( dest );
        }
    }

    private Set computeStaleXSDs()
        throws MojoExecutionException
    {
        Set staleSources = new HashSet();

        if ( schema != null )
        {
            File sourceFile = new File( schema );
            File targetFile = new File( getTimeStampDirectory(), sourceFile.getName() );
            if ( !targetFile.exists() || ( targetFile.lastModified() + staleMillis < sourceFile.lastModified() ) )
            {
                staleSources.add( sourceFile );
            }
        }
        else
        {
            SourceMapping mapping = new SuffixMapping( ".xsd", ".xsd" );

            File tstampDir = getTimeStampDirectory();

            File schemaDir = new File( schemaDirectory );

            SourceInclusionScanner scanner = getSourceInclusionScanner();

            scanner.addSourceMapping( mapping );

            try
            {
                staleSources.addAll( scanner.getIncludedSources( schemaDir, tstampDir ) );
            }
            catch ( InclusionScanException e )
            {
                throw new MojoExecutionException( "Error scanning source root: \'" + schemaDir
                    + "\' for stale xsds to reprocess.", e );
            }
        }

        return staleSources;
    }

    private SourceInclusionScanner getSourceInclusionScanner()
    {
        return new StaleSourceScanner( staleMillis );
    }

    private File getTimeStampDirectory()
    {
        return new File( tstamp );
    }

    private void config()
        throws MojoExecutionException
    {
        sgen = CastorSourceGenerator.createSourceGenerator( types );

        sgen.setLog( getLog() );

        sgen.setLineSeparatorStyle( lineSeparator );

        sgen.setDestDir( dest );

        sgen.setBindingFile( bindingfile );

        sgen.setVerbose( verbose );

        sgen.setSuppressNonFatalWarnings( !warnings );

        sgen.setDescriptorCreation( descriptors );
        if ( !descriptors )
        {
            log( DISABLE_DESCRIPTORS_MSG );
        }

        sgen.setCreateMarshalMethods( marshal );
        if ( !marshal )
        {
            log( DISABLE_MARSHALL_MSG );
        }

        sgen.setBuilderProperties( properties );
        
        
    }

    /**
     * Run source generation
     */
    private void processFile( String filePath )
        throws MojoExecutionException
    {
        log( "Processing " + filePath );
        try
        {
            sgen.generateSource( filePath, packaging );
        }
        catch ( FileNotFoundException e )
        {
            String message = "XML Schema file \"" + filePath + "\" not found.";
            log( message );
            throw new MojoExecutionException( message );
        }
        catch ( IOException iox )
        {
            throw new MojoExecutionException( "An IOException occurred processing " + filePath, iox );
        }
        catch ( Exception e )
        {
            throw new MojoExecutionException( "An Exception occurred processing " + filePath, e );
        }
    }

    private void log( String msg )
    {
        getLog().info( msg );
    }

    public String getDest()
    {
        return dest;
    }

    public void setDest( String dest )
    {
        this.dest = dest;
    }

    public String getTstamp()
    {
        return tstamp;
    }

    public void setTstamp( String tstamp )
    {
        this.tstamp = tstamp;
    }

    public String getPackaging()
    {
        return packaging;
    }

    public void setPackaging( String packaging )
    {
        this.packaging = packaging;
    }

    public String getSchema()
    {
        return schema;
    }

    public void setSchema( String schema )
    {
        this.schema = schema;
    }

    public String getTypes()
    {
        return types;
    }

    public void setTypes( String types )
    {
        this.types = types;
    }
    
    public void setProperties(String properties) {
        this.properties = properties;
    }

    public boolean getMarshal()
    {
        return marshal;
    }

    public void setMarshal( boolean marshal )
    {
        this.marshal = marshal;
    }

    public MavenProject getProject()
    {
        return project;
    }

    public void setProject( MavenProject project )
    {
        this.project = project;
    }

}
