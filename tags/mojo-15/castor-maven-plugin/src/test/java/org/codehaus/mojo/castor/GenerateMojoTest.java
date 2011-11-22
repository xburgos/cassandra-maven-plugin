package org.codehaus.mojo.castor;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileChannel;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.maven.model.Model;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.util.IOUtil;

public class GenerateMojoTest
    extends PlexusTestCase
{

    private static final String TIMESTAMP_DIR = getBasedir() + "/target/test/resources/timestamp";

    private static final String GENERATED_DIR = getBasedir() + "/target/test/generated";

    private static final String MAPPING_XSD = getBasedir() + "/src/test/resources/mapping.xml";

    GenerateMojo generateMojo;

    private File aClassFile;

    private File aDescriptorClassFile;

    public void setUp()
        throws IOException
    {
       // FileUtils.deleteDirectory( new File( GENERATED_DIR ) );
        FileUtils.deleteDirectory( new File( TIMESTAMP_DIR ) );

        aClassFile = new File( GENERATED_DIR, "org/codehaus/mojo/castor/A.java" );
        aDescriptorClassFile = new File( GENERATED_DIR, "org/codehaus/mojo/castor/descriptors/ADescriptor.java" );

        generateMojo = new GenerateMojo();
        generateMojo.setProject( new MavenProject( new Model() ) );
        generateMojo.setDest( GENERATED_DIR );
        generateMojo.setTstamp( TIMESTAMP_DIR );
    }

    public void tearDown()
        throws IOException
    {
        generateMojo = null;
        //FileUtils.deleteDirectory( new File( GENERATED_DIR ) );
        //FileUtils.deleteDirectory( new File( TIMESTAMP_DIR ) );
    }

    public void testExecute()
        throws MojoExecutionException
    {

        generateMojo.setPackaging( "org.codehaus.mojo.castor" );
        generateMojo.setSchema( MAPPING_XSD );
        generateMojo.execute();

        assertTrue( aClassFile.exists() );
        assertTrue( aDescriptorClassFile.exists() );

    }
    
    // MCASTOR-5 issue
    public void testForGetContent() throws Exception {
    	
        generateMojo.setSchema( getPathTo( "src/test/resources/availability_report.xsd" ) );
        generateMojo.setProperties( getPathTo( "src/test/resources/castorbuilder.properties" ) );
        generateMojo.setTypes("arraylist");
        generateMojo.execute();

        File generatedClass = new File( GENERATED_DIR + "/org/opennms/report/availability", "Created.java" );
		assertTrue( "Expected " + generatedClass + " to exist.", generatedClass.exists() );
		assertFileContains( generatedClass, "getContent" );
		
    }

    private void assertFileContains( File file, String string ) throws IOException {
    	
    	String contents = FileUtils.readFileToString( file, "ISO-8859-1" );
        boolean contains = (contents.indexOf(string) > -1);
    	assertTrue( "Expected " + file + " to contain string " + string, contains );
    	
	}
    

	public void testEmptyPackage()
        throws MojoExecutionException
    {

        generateMojo.setSchema( getPathTo( "src/test/resources/vacuumd-configuration.xsd" ) );
        generateMojo.setProperties( getPathTo( "src/test/resources/castorbuilder.properties" ) );
        generateMojo.setTypes("arraylist");
        generateMojo.execute();

        assertFalse( new File( GENERATED_DIR, "Actions.java" ).exists() );
    }

    private File getTimeStampFile()
    {
        return new File( TIMESTAMP_DIR, "mapping.xml" );
    }

    public void testCreateTimeStamp()
        throws MojoExecutionException
    {
        File timeStampFile = getTimeStampFile();

        generateMojo.setPackaging( "org.codehaus.mojo.castor" );
        generateMojo.setSchema( MAPPING_XSD );
        generateMojo.execute();
        
        assertTrue( aClassFile.exists() );
        assertTrue( aDescriptorClassFile.exists() );
        assertTrue( timeStampFile.exists() );

    }
       

    public void testCreateTimeStampFolder()
        throws MojoExecutionException
    {
        File timeStampFile = getTimeStampFile();

        generateMojo.setPackaging( "org.codehaus.mojo.castor" );
        generateMojo.setSchema( MAPPING_XSD );
        generateMojo.execute();

        assertTrue( aClassFile.exists() );
        assertTrue( aDescriptorClassFile.exists() );
        assertTrue( timeStampFile.exists() );

    }

    // timestamp exist but not updated
    public void testCreateTimeStampOld()
        throws MojoExecutionException, IOException
    {
        File timeStampFile = createTimeStampWithTime( timestampOf( MAPPING_XSD ) - 1 );

        generateMojo.setPackaging( "org.codehaus.mojo.castor" );
        generateMojo.setSchema( MAPPING_XSD );
        generateMojo.execute();

        assertTrue( aClassFile.exists() );
        assertTrue( aDescriptorClassFile.exists() );
        assertTrue( timeStampFile.exists() );

    }

    private File createTimeStampWithTime( long time )
        throws IOException
    {
        File timeStampFolder = new File( TIMESTAMP_DIR );
        File timeStampFile = getTimeStampFile();
        if ( !timeStampFolder.exists() )
        {
            timeStampFolder.mkdirs();
        }
        if ( !timeStampFile.exists() )
        {
            FileUtils.touch( timeStampFile );
            timeStampFile.setLastModified( time );
        }
        return timeStampFile;
    }

    public void testCreateTimeStampLatest()
        throws MojoExecutionException, IOException
    {
        File timeStampFile = createTimeStampWithTime( timestampOf( MAPPING_XSD ) + 1 );

        generateMojo.setPackaging( "org.codehaus.mojo.castor" );
        generateMojo.setSchema( MAPPING_XSD );
        generateMojo.execute();

        assertTrue( !aClassFile.exists() );
        assertTrue( !aDescriptorClassFile.exists() );
        assertTrue( timeStampFile.exists() );

    }

    private long timestampOf( String file )
    {
        File sourcefile = new File( file );
        long time = sourcefile.lastModified();
        return time;
    }

    public void testDestProperty()
    {
        generateMojo.setDest( "testString" );
        assertEquals( "testString", generateMojo.getDest() );
    }

    public void testTStampProperty()
    {
        generateMojo.setTstamp( "testString" );
        assertEquals( "testString", generateMojo.getTstamp() );
    }

    public void testSchemaProperty()
    {
        generateMojo.setSchema( "teststring" );
        assertEquals( "teststring", generateMojo.getSchema() );
    }

    public void testPackagingProperty()
    {
        generateMojo.setPackaging( "teststring" );
        assertEquals( "teststring", generateMojo.getPackaging() );
    }

    public void testTypesProperty()
    {
        generateMojo.setTypes( "teststring" );
        assertEquals( "teststring", generateMojo.getTypes() );
    }

    public void testMarshalProperty()
    {
        generateMojo.setMarshal( true );
        assertTrue( generateMojo.getMarshal() );
    }

    private String getPathTo( String relativePath )
    {
        return getBasedir() + '/' + relativePath;
    }

}
