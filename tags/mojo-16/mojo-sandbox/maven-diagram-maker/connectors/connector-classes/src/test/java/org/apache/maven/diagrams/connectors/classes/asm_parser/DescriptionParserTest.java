package org.apache.maven.diagrams.connectors.classes.asm_parser;

import java.text.ParseException;
import java.util.List;

import junit.framework.TestCase;

public class DescriptionParserTest extends TestCase
{
    public void testReadParamsList() throws ParseException
    {
        DescriptionParser dp=new DescriptionParser("()");
        assertEquals( 0, dp.readParamsList().size());
        
        dp=new DescriptionParser("(V[[I[[[Lorg/apache/maven/Test;F[J)");
        List<String> paramsList=dp.readParamsList();
        assertEquals( 5, paramsList.size());
        assertEquals( "void", paramsList.get( 0 ) );
        assertEquals( "int[][]", paramsList.get( 1 ) );
        assertEquals( "org.apache.maven.Test[][][]", paramsList.get( 2 )); 
        assertEquals( "float", paramsList.get( 3 ) );
        assertEquals( "long[]", paramsList.get( 4 ) );
    }
}
