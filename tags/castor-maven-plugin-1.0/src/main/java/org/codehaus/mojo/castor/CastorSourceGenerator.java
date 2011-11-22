package org.codehaus.mojo.castor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;
import java.util.Properties;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.exolab.castor.builder.CollectionInfo;
import org.exolab.castor.builder.FieldInfoFactory;
import org.exolab.castor.builder.SourceGenerator;
import org.exolab.castor.builder.binding.ExtendedBinding;
import org.exolab.castor.util.LocalConfiguration;
import org.exolab.castor.xml.XMLException;
import org.exolab.castor.xml.schema.Schema;
import org.exolab.castor.xml.schema.reader.Sax2ComponentReader;
import org.exolab.castor.xml.schema.reader.SchemaUnmarshaller;
import org.xml.sax.InputSource;
import org.xml.sax.Parser;
import org.xml.sax.SAXException;

/**
 * Override Castor's SourceGenerator to inject exception handling.
 * Code based on castor-0.9.5.3-xml.jar
 */
class CastorSourceGenerator
    extends SourceGenerator
{
    private Log log;

    // we need to save this in order to override its properties later
    // since SourceGenerator doesn't give us access to this and 
    // the properties are only read during the constructor call
    private FieldInfoFactory fieldInfoFactory;

    // base class does not provide access to this variable to we intercept
    // setting it and store its value here
    private boolean verbose;

    public CastorSourceGenerator()
    {
        this( new FieldInfoFactory() );
    }

    public CastorSourceGenerator( FieldInfoFactory fieldInfoFactory )
    {
        super( fieldInfoFactory );
        this.fieldInfoFactory = fieldInfoFactory;
    }

    public CastorSourceGenerator( FieldInfoFactory fieldInfoFactory, ExtendedBinding extendedBinding )
    {
        super( fieldInfoFactory, extendedBinding );
        this.fieldInfoFactory = fieldInfoFactory;
    }

    public void generateSource( InputSource source, String packageName )
        throws IOException
    {
        Parser parser = null;
        try
        {
            parser = LocalConfiguration.getInstance().getParser();
        }
        catch ( RuntimeException e )
        {
            throw new RuntimeException( "Unable to create SAX parser.", e );
        }
        if ( parser == null )
        {
            throw new RuntimeException( "Unable to create SAX parser." );
        }

        SchemaUnmarshaller schemaUnmarshaller = null;
        try
        {
            schemaUnmarshaller = new SchemaUnmarshaller();
        }
        catch ( XMLException e )
        {
            throw new RuntimeException( "Unable to create schema unmarshaller.", e );
        }

        Sax2ComponentReader handler = new Sax2ComponentReader( schemaUnmarshaller );
        parser.setDocumentHandler( handler );
        parser.setErrorHandler( handler );
        try
        {
            parser.parse( source );
        }
        catch ( IOException e )
        {
            throw new RuntimeException( "Can't read input file " + source.getSystemId() + ".\n" + e, e );
        }
        catch ( SAXException e )
        {
            throw new RuntimeException( "Can't parse input file " + source.getSystemId() + ".\n" + e, e );
        }
        Schema schema = schemaUnmarshaller.getSchema();
        if ( packageName == null && schema.getTargetNamespace() != null )
        {
            packageName = lookupPackageByNamespace( schema.getTargetNamespace() );
            if ( "".equals( packageName ) )
                packageName = null;
        }

        generateSource( schema, packageName );
    }

    public static CastorSourceGenerator createSourceGenerator( String types )
        throws MojoExecutionException
    {
        // Create Source Generator with appropriate type factory
        CastorSourceGenerator sgen;
        if ( types != null )
        {
            try
            {
                String typ = "j2".equals( types ) ? "arraylist" : types;
                FieldInfoFactory factory = new FieldInfoFactory( typ );
                sgen = new CastorSourceGenerator( factory );
            }
            catch ( Exception e )
            {
                try
                {
                    sgen = new CastorSourceGenerator( (FieldInfoFactory) Class.forName( types ).newInstance() );
                }
                catch ( Exception e2 )
                {
                    throw new MojoExecutionException( "Invalid types \"" + types + "\": " + e.getMessage() );
                }
            }
        }
        else
        {
            sgen = new CastorSourceGenerator(); // default
        }
        return sgen;
    }

    public void setLog( Log log )
    {
        this.log = log;
    }

    public Log getLog()
    {
        return log;
    }

    public void log( String msg )
    {
        getLog().info( msg );
    }

    public void verbose( String msg )
    {
        if ( verbose )
            getLog().info( msg );
    }

    public void setLineSeparatorStyle( String lineSeparator )
        throws MojoExecutionException
    {
        // Set Line Separator
        String lineSep = System.getProperty( "line.separator" );
        if ( lineSeparator != null )
        {
            if ( "win".equals( lineSeparator ) )
            {
                log( "Using Windows style line separation." );
                lineSep = "\r\n";
            }
            else if ( "unix".equals( lineSeparator ) )
            {
                log( "Using UNIX style line separation." );
                lineSep = "\n";
            }
            else if ( "mac".equals( lineSeparator ) )
            {
                log( "Using Macintosh style line separation." );
                lineSep = "\r";
            }
            else
            {
                throw new MojoExecutionException( "Invalid value for lineseparator, must be win, unix, or mac." );
            }
        }
        setLineSeparator( lineSep );
    }

    public void setVerbose( boolean verbose )
    {
        this.verbose = verbose;
        super.setVerbose( verbose );
    }

    public void setBindingFile( String bindingFile )
    {
        if ( bindingFile != null && new File( bindingFile ).exists() )
        {
            setBinding( bindingFile );
        }
    }

    public void setBuilderProperties( String properties )
        throws MojoExecutionException
    {
        // Set Builder Properties;
        if ( properties != null )
        {
            String filePath = new File( properties ).getAbsolutePath();
            Properties customProperties = new Properties();
            try
            {
                customProperties.load( new FileInputStream( filePath ) );
            }
            catch ( FileNotFoundException e )
            {
                throw new MojoExecutionException( "Properties file \"" + filePath + "\" not found" );
            }
            catch ( IOException e )
            {
                throw new MojoExecutionException( "Can't read properties file \"" + filePath + "\": " + e );
            }
            setDefaultProperties( customProperties );

            // these properties are read at contstruction time and copied into FieldInfoFactory 
            // se we set them directly in the fieldInfoFactory here.
            if ( generateExtraCollectionMethods() )
            {
                verbose( "Overriding default castorbuilder.properties and setting createExtraMethods to true" );
                fieldInfoFactory.setCreateExtraMethods( true );
            }

            String suffix = getProperty( CollectionInfo.REFERENCE_SUFFIX_PROPERTY, null );
            if ( suffix != null )
                verbose( "Overriding default castorbuilder.properties and setting referenceSuffixProperty to " + suffix );
            fieldInfoFactory.setReferenceMethodSuffix( suffix );

            if ( boundPropertiesEnabled() )
            {
                verbose( "Overriding default castorbuilder.properties and setting boundProperties to true" );
                fieldInfoFactory.setBoundProperties( true );
            }

        }
    }

}
