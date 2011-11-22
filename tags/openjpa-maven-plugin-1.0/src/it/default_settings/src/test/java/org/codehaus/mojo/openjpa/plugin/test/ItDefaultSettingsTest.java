package org.codehaus.mojo.openjpa.plugin.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;

import org.apache.openjpa.enhance.PersistenceCapable;

import junit.framework.TestCase;

public class ItDefaultSettingsTest extends TestCase {

    /** contains the directory where all generated results are placed */
    private final static String TARGET_DIR = "target";
    
    /** the file containing the generated SQL syntax */
    private final static String SQL_FILE = "database.sql";
    
    /** if the SQL generation has been successful, the following result should be in the SQL file */
    private final static String VALID_SQL = "CREATE TABLE TestEntity (xint1 INTEGER NOT NULL, string1 VARCHAR(255), PRIMARY KEY (xint1));";
    
    private final static String TEST_ENTITY_CLASS = "org.codehaus.mojo.openjpa.plugin.testentity.TestEntity";

    /** the file containing the generated schema XML */
    private final static String SCHEMA_FILE = "schema.xml";
    
    /** the name of the schema XML file which should be taken as test reference */
    private final static String REFERENCE_SCHEMA_XML = "reference_schema.xml";

    
    /**
     * check if the generated classes have been enhanced.
     * @throws Exception
     */
    public void testEnhancement() throws Exception
    {
        Class tec = Thread.currentThread().getContextClassLoader().loadClass( TEST_ENTITY_CLASS );
        
        boolean isPersistenceCapable = false;
        Class[] interfaces  = tec.getInterfaces();
        for ( int i = 0; i < interfaces.length; i++ )
        {
            if ( interfaces[ i ].getName().equals( PersistenceCapable.class.getName() ) )
            {
                isPersistenceCapable = true;
                break;
            }
        } 
        
        assertTrue( "the class " + TEST_ENTITY_CLASS + " does not implement PersistenceCapable!", isPersistenceCapable );
    }
    
    /**
     * check if the generated SQL script is correct.
     * @throws Exception
     */
    public void testSqlGeneration() throws Exception
    {
        File sqlFile = new File( TARGET_DIR, SQL_FILE );
        BufferedReader in = new BufferedReader( new FileReader( sqlFile ) );
        String sqlIn = in.readLine();
        assertEquals( VALID_SQL, sqlIn );
    }
    
    /**
     * check if the generated schema.xml is correct.
     * @throws Exception
     */
    public void testSchemaGeneration() throws Exception
    {
        File sqlFile = new File( TARGET_DIR, SCHEMA_FILE );
        BufferedReader schemaGen = new BufferedReader( new FileReader( sqlFile ) );
        
        InputStream schemaRefIs = Thread.currentThread().getContextClassLoader().getResourceAsStream( REFERENCE_SCHEMA_XML );
     
        BufferedReader schemaRef = new BufferedReader( new InputStreamReader( schemaRefIs ) );
        
        String refLine;
        while ( (refLine = schemaRef.readLine()) != null)
        {
            String genLine = schemaGen.readLine();
            assertEquals("generated schema.xml differs from expected one!", refLine, genLine );
        }
    }
}
