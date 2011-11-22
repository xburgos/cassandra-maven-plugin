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
package org.codehaus.mojo.castor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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
import org.exolab.castor.builder.SourceGenerator;

/**
 * A mojo that uses Castor to generate a collection of javabeans from an XSD. Detailed explanations of many of these can
 * be found in the details for the Castor <a href="http://castor.codehaus.org/sourcegen.html">SourceGenerator</a>.
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

    private static final String DISABLE_MARSHALL_MSG =
        "Disabling generation of Marshalling framework methods (marshall, unmarshall, validate).";

    private static final String DEFAULT_PROPERTY_FILE_LOCATION = "src/main/castor/castorbuilder.properties";

    /**
     * The binding file to use for mapping xml to java.
     * 
     * @parameter expression="${basedir}/src/main/castor/bindings.xml"
     */
    private File bindingfile;

    /**
     * A schema file to process. If this is not set then all .xsd files in schemaDirectory will be processed.
     * 
     * @parameter
     */
    private File schema;

    /**
     * The source directory containing *.xsd files
     * 
     * @parameter expression="${basedir}/src/main/castor"
     */
    private File schemaDirectory;

    /**
     * The directory to output the generated sources to
     * 
     * @parameter expression="${project.build.directory}/generated-sources/castor"
     * @todo This would be better as outputDirectory but for backward compatibility I left it as dest
     */
    private File dest;

    /**
     * The directory to store the processed xsds. The timestamps of these xsds are used to determine if the source for
     * that xsd need to be regenerated
     * 
     * @parameter expression="${project.build.directory}/xsds"
     * @todo timestampDirectory would be a better name for this. Used this name for backward compatibility
     */
    private File tstamp;

    /**
     * The granularity in milliseconds of the last modification date for testing whether a source needs recompilation
     * 
     * @parameter expression="${lastModGranularityMs}" default-value="0"
     */
    private int staleMillis = 0;

    /**
     * Castor collection types. Allowable values are 'vector', 'arraylist', 'j2' or 'odmg' 'j2' and 'arraylist' are the
     * same.
     * 
     * @parameter default-value="arraylist";
     */
    private String types = "arraylist";

    /**
     * If true, generate descriptors
     * 
     * @parameter default-value="true"
     */
    private boolean descriptors = true;

    /**
     * Verbose output during generation
     * 
     * @parameter default-value="false"
     */
    private boolean verbose = false;

    /**
     * Enable warning messages
     * 
     * @parameter default-value="false"
     */
    private boolean warnings = false;

    /**
     * if false, don't generate the marshaller
     * 
     * @parameter default-value="true"
     */
    private boolean marshal = true;

    /**
     * The line separator to use in generated source. Can be either win, unix, or mac
     * 
     * @parameter
     */
    private String lineSeparator;

    /**
     * The castorbuilder.properties file to use
     * 
     * @parameter expression="${basedir}/src/main/castor/castorbuilder.properties"
     */
    private File properties;

    /**
     * The package for the generated source
     * 
     * @parameter
     */
    private String packaging;

    /**
     * Whether to generate Java classes from imported XML schemas or not.
     * 
     * @parameter default-value="false"
     */
    private boolean generateImportedSchemas = false;

    /**
     * Set to <tt>true</tt> to generate Castor XML class mappings for the Java classes generated by the XML code
     * generator from imported XML schemas.
     * 
     * @parameter default-value="false"
     */
    private boolean generateMappings = false;

    /**
     * The Maven project to act upon.
     * @parameter expression="${project}"
     * @required
     */
    private MavenProject project;

    /**
     * {@link SourceGenerator} instance used for code generation.
     * 
     * @see SourceGenerator#generateSource(org.xml.sax.InputSource, String)
     */
    private CastorSourceGenerator sgen;

    /**
     * Whether to generate JDO-specific descriptor classes or not.
     * 
     * @parameter default-value="false"
     */
    private boolean createJdoDescriptors = false;

    /**
     * {@inheritDoc}
     * 
     * @see org.apache.maven.plugin.AbstractMojo#execute()
     */
    public void execute()
        throws MojoExecutionException
    {

        if ( !dest.exists() )
        {
            FileUtils.mkdir( dest.getAbsolutePath() );
        }

        Set staleXSDs = computeStaleXSDs();

        if ( staleXSDs.isEmpty() )
        {
            getLog().info( "Nothing to process - all xsds are up to date" );
            project.addCompileSourceRoot( dest.getAbsolutePath() );
            return;
        }

        config();

        for ( Iterator i = staleXSDs.iterator(); i.hasNext(); )
        {
            File xsd = (File) i.next();

            try
            {

                processFile( xsd.getCanonicalPath() );

                // copy stale xsd to timestamp directory within the same relative path
                File timeStampDir = getTimeStampDirectory();
                // make sure this is after the actual processing,
                // otherwise it if fails the computeStaleXSDs will think it completed.
                FileUtils.copyFileToDirectory( xsd, timeStampDir );
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
            project.addCompileSourceRoot( dest.getAbsolutePath() );
            
            // TODO: add .castor.cdr files automatically
//            project.addTestResource( testResource );
        }
    }

    /**
     * Computes the collection of <i>stale</i> XML schemas for which 
     * sources need to be re-generated.
     * @return A set of XML schemas for which sources need to be re-generated.
     * @throws MojoExecutionException If there's a problem with scanning all potential
     *    XML schemas. 
     */
    private Set computeStaleXSDs()
        throws MojoExecutionException
    {
        Set staleSources = new HashSet();

        if ( schema != null && schema.exists() )
        {
            File sourceFile = schema;
            File targetFile = new File( getTimeStampDirectory(), sourceFile.getName() );
            if ( !targetFile.exists() || ( targetFile.lastModified() + staleMillis < sourceFile.lastModified() ) )
            {
                getLog().debug( "Finding XSDs - adding schema " + targetFile );
                staleSources.add( sourceFile );
            }
            else
            {
                getLog().debug( "Finding XSDs - adding schema " + targetFile );
            }
        }
        else
        {
            SourceMapping mapping = new SuffixMapping( ".xsd", ".xsd" );

            File tstampDir = getTimeStampDirectory();

            File schemaDir = schemaDirectory;

            SourceInclusionScanner scanner = getSourceInclusionScanner();

            scanner.addSourceMapping( mapping );

            try
            {
                getLog().debug(
                                "Finding XSDs - adding scanned XSDs from schemaDir " + schemaDir + " tstampDir "
                                    + tstampDir );
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

    /**
     * Returns a {@link SourceInclusionScanner} instance.
     * @return A {@link SourceInclusionScanner} instance.
     */
    private SourceInclusionScanner getSourceInclusionScanner()
    {
        return new StaleSourceScanner( staleMillis );
    }

    /**
     * Returns the location where timestamp information is recorded.
     * @return the location where timestamp information is recorded.
     */
    private File getTimeStampDirectory()
    {
        return tstamp;
    }

    /**
     * Entry point for configuring the underlying Castor XML code generator
     * based upon the plugin configuration.
     * @throws MojoExecutionException If there's a problem accessing resources as specified 
     * in the plugin configuration.
     */
    private void config()
        throws MojoExecutionException
    {
        sgen = CastorSourceGenerator.createSourceGenerator( types );

        sgen.setLog( getLog() );

        sgen.setLineSeparatorStyle( lineSeparator );

        sgen.setDestDir( dest.getAbsolutePath() );

        if ( bindingfile != null && bindingfile.exists() )
        {
            sgen.setBindingFile( bindingfile );
        }

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

        if ( properties != null && properties.exists() )
        {
            sgen.setBuilderProperties( properties );
        }
        else
        {
            File defaultPropertyFile = new File( getProject().getBasedir(), DEFAULT_PROPERTY_FILE_LOCATION );
            if ( properties != null && !properties.equals( defaultPropertyFile ) )
            {
                getLog().warn( "Cannot find custom builder property file " + properties.getAbsolutePath() );
                throw new MojoExecutionException( "Cannot find custom builder property file "
                    + properties.getAbsolutePath() );
            }
            else if ( properties != null )
            {
                getLog().info(
                               "There is no custom builder property file at " + "the default location at "
                                   + properties.getAbsolutePath() + ". Continuing code generation without." );
            }
        }

        if ( createJdoDescriptors == true )
        {
            callSetterMethodUsingReflection( "setJdoDescriptorCreation", boolean.class,
                                             new Boolean( createJdoDescriptors ) );
        }

        if ( isGenerateImportedSchemas() == true )
        {
            sgen.setGenerateImportedSchemas( true );
        }

        if ( generateMapping() == true )
        {
            sgen.setGenerateMappingFile( this.generateMappings );
            sgen.setDescriptorCreation( false );
        }

    }

    /**
     * Helper method to invoke a setter method on SourceGenerator that might not be available due to a version issue.
     * 
     * @param methodName Name of the method
     * @param parameterType Type of the method parameter.
     * @param parameterValue Actual parameter value to be used during method invocation.
     * @throws MojoExecutionException If the method cannot be invoked.
     */
    private void callSetterMethodUsingReflection( final String methodName, final Class parameterType,
                                                  final Object parameterValue )
        throws MojoExecutionException
    {
        try
        {
            Method method = sgen.getClass().getMethod( methodName, new Class[] { parameterType } );
            method.invoke( sgen, new Object[] { parameterValue } );
        }
        catch ( NoSuchMethodException e )
        {
            // unable to find method to configure JDO descriptor creation.
        }
        catch ( IllegalArgumentException e )
        {
            throw new MojoExecutionException( "Problem calling SourceGenerator.setJdoDescriptorCreation: ", e );
        }
        catch ( IllegalAccessException e )
        {
            throw new MojoExecutionException( "Problem calling SourceGenerator.setJdoDescriptorCreation: ", e );
        }
        catch ( InvocationTargetException e )
        {
            throw new MojoExecutionException( "Problem calling SourceGenerator.setJdoDescriptorCreation: ", e );
        }
    }

    /**
     * Run source generation on the {@link File} soecified.
     * @path filePath The XML schema file (path) to be processed.
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

    /**
     * Logs a message to the logger.
     * @param msg The message ot be logged.
     */
    private void log( final String msg )
    {
        getLog().info( msg );
    }

    /**
     * Returns the destination directory to used during code generation.
     * @return the destination directory to used during code generation.
     */
    public File getDest()
    {
        return dest;
    }

    /**
     * Sets the destination directory to used during code generation.
     * @param dest the destination directory to used during code generation.
     */
    public void setDest( final File dest )
    {
        this.dest = dest;
    }

    /**
     * Returns the directory to store time stamp information.
     * @return the directory to store time stamp information.
     */
    public File getTstamp()
    {
        return tstamp;
    }

    /**
     * Sets the directory to store time stamp information.
     * @param tstamp the directory to store time stamp information.
     */
    public void setTstamp( final File tstamp )
    {
        this.tstamp = tstamp;
    }

    /**
     * Returns the default package to be used during code generation.
     * @return the default package to be used during code generation.
     */
    public String getPackaging()
    {
        return packaging;
    }

    /**
     * Sets the default package to be used during code generation.
     * @param packaging the default package to be used during code generation.
     */
    public void setPackaging( final String packaging )
    {
        this.packaging = packaging;
    }

    /**
     * Returns the (single) XML schema file to be processed.
     * @return the (single) XML schema file to be processed.
     */
    public File getSchema()
    {
        return schema;
    }

    /**
     * Sets the (single) XML schema file to be processed.
     * @param schema the (single) XML schema file to be processed.
     */
    public void setSchema( final File schema )
    {
        this.schema = schema;
    }

    /**
     * Returns the collection types Castor XML is capable of working with.
     * @return the collection types Castor XML is capable of working with.
     */
    public String getTypes()
    {
        return types;
    }

    /**
     * Sets the collection types Castor XML is capable of working with.
     * @param types the collection types Castor XML is capable of working with.
     */
    public void setTypes( final String types )
    {
        this.types = types;
    }

    /**
     * Sets the Castor XML code generator binding file to be used during code generation.
     * @param bindingfile the Castor XML code generator binding file to be used during code generation.
     */
    public void setBindingfile( final File bindingfile )
    {
        this.bindingfile = bindingfile;
    }

    /**
     * Sets the (user-specific) <tt>castorbuilder.properties</tt> file to be used during code generation.
     * @param properties the (user-specific) <tt>castorbuilder.properties</tt> file to be used during code generation.
     */
    public void setProperties( final File properties )
    {
        this.properties = properties;
    }

    /**
     * Indicates whether #marshal() methods will be generated during the code generation.
     * @return True if #marshal() methods will be generated during the code generation.
     */
    public boolean getMarshal()
    {
        return marshal;
    }

    /**
     * Sets whether #marshal() methods will be generated during the code generation.
     * @param marshal True if #marshal() methods will be generated during the code generation.
     */
    public void setMarshal( final boolean marshal )
    {
        this.marshal = marshal;
    }

    /**
     * Indicates whether code should be generated for imported XML schemas as well. 
     * @return True if code should be generated for imported XML schemas as well.
     */
    public boolean isGenerateImportedSchemas()
    {
        return generateImportedSchemas;
    }

    /**
     * Sets whether code should be generated for imported XML schemas as well.
     * @param generateImportedSchemas True if code should be generated for imported XML schemas as well.
     */
    public void setGenerateImportedSchemas( final boolean generateImportedSchemas )
    {
        this.generateImportedSchemas = generateImportedSchemas;
    }

    /**
     * Returns the {@link MavenProject} instance for which code generation should be executed.
     * @return the {@link MavenProject} instance for which code generation should be executed.
     */
    public MavenProject getProject()
    {
        return project;
    }

    /**
     * Sets the {@link MavenProject} instance for which code generation should be executed.
     * @param project the {@link MavenProject} instance for which code generation should be executed.
     */
    public void setProject( MavenProject project )
    {
        this.project = project;
    }

    /**
     * Indicates whether JDO descriptors should be generated during code generation.
     * @return True if JDO descriptors should be generated during code generation.
     */
    public final boolean getCreateJdoDescriptors()
    {
        return createJdoDescriptors;
    }

    /**
     * Sets whether JDO descriptors should be generated during code generation.
     * @param newCreateJdoDescriptors True if JDO descriptors should be generated during code generation.
     */
    public final void setCreateJdoDescriptors( final boolean newCreateJdoDescriptors )
    {
        this.createJdoDescriptors = newCreateJdoDescriptors;
    }

    /**
     * Indicates whether mapping files should be created during code generation.
     * @return True if mapping files should be created during code generation
     */
    private boolean generateMapping()
    {
        return this.generateMappings;
    }

    /**
     * Sets whether mapping files should be created during code generation.
     * @param generateMappings True if mapping files should be created during code generation.
     */
    public final void setGenerateMappings( final boolean generateMappings )
    {
        this.generateMappings = generateMappings;
    }

}
