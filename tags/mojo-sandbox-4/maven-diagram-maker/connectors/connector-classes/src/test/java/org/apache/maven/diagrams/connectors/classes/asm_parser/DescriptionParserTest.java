package org.apache.maven.diagrams.connectors.classes.asm_parser;

import java.text.ParseException;
import java.util.List;

import org.apache.maven.diagrams.connector_api.DiagramConnector;
import org.apache.maven.diagrams.connectors.classes.ClassesConnector;
import org.codehaus.plexus.PlexusTestCase;

public class DescriptionParserTest extends PlexusTestCase
{
    public void testReadParamsList() throws ParseException
    {
        DescriptionParser dp = new DescriptionParser( "()" );
        assertEquals( 0, dp.readParamsList().size() );

        dp = new DescriptionParser( "(V[[I[[[Lorg/apache/maven/Test;F[J)" );
        List<String> paramsList = dp.readParamsList();
        assertEquals( 5, paramsList.size() );
        assertEquals( "void", paramsList.get( 0 ) );
        assertEquals( "int[][]", paramsList.get( 1 ) );
        assertEquals( "org.apache.maven.Test[][][]", paramsList.get( 2 ) );
        assertEquals( "float", paramsList.get( 3 ) );
        assertEquals( "long[]", paramsList.get( 4 ) );
    }

    public void testGetConnectorsDescriptor() throws Exception
    {
        ClassesConnector cc = (ClassesConnector) lookup( DiagramConnector.class, "connector-classes" );
        assertNotNull( cc.getConnectorDescriptor() );
    }
}
