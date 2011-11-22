package org.apache.maven.diagrams.connectors.classes.asm_parser;

import java.text.ParseException;
import java.util.LinkedList;
import java.util.List;

/**
 * The class is a helper class for AsmClassDataSource. It helps to parse JVM Method/type signature into meaningful
 * pieces.
 * 
 * @author Piotr Tabor
 * 
 */
public class DescriptionParser
{
    /**
     * Full JVM method or field signature to be parsed
     */
    private String str;

    /**
     * Current position (how many chars we have alredy parsed)
     */
    private int position;

    /**
     * @param description -
     *            the descriptor (JVM method or field signature) to be parsed
     */
    public DescriptionParser( String description )
    {
        str = description;
        position = 0;
    }

    /**
     * Reads list of method parameters (type names) from signature. The first (not jet parsed) char should be '('.
     * 
     * The returned types are "normal java types" (not JVM signature types)
     * 
     * @return ordered list of types of method parameters
     * @throws ParseException
     */
    public List<String> readParamsList() throws ParseException
    {
        if ( str.charAt( position ) != '(' )
            throw new ParseException( "'(' expected", position );
        position++;

        List<String> result = new LinkedList<String>();
        while ( ( position < str.length() ) && ( str.charAt( position ) != ')' ) )
        {
            result.add( readType() );
        }

        if ( str.charAt( position ) != ')' )
            throw new ParseException( "')' expected", position );
        position++;

        return result;
    }

    /**
     * The methods reads a single type from the signature. The returned type is "normal java type" (not JVM signature
     * type)
     * 
     * @return
     * @throws ParseException
     */
    public String readType() throws ParseException
    {
        StringBuffer arrays = new StringBuffer();
        while ( str.charAt( position ) == '[' )
        {
            arrays.append( "[]" );
            position++;
        }

        switch ( str.charAt( position ) )
        {
            case 'C':
                position++;
                return "char" + arrays.toString();
            case 'B':
                position++;
                return "byte" + arrays.toString();
            case 'S':
                position++;
                return "short" + arrays.toString();
            case 'V':
                position++;
                return "void" + arrays.toString();
            case 'I':
                position++;
                return "int" + arrays.toString();
            case 'D':
                position++;
                return "double" + arrays.toString();
            case 'F':
                position++;
                return "float" + arrays.toString();
            case 'J':
                position++;
                return "long" + arrays.toString();
            case 'Z':
                position++;
                return "boolean" + arrays.toString();
            case 'L':
                return readClass() + arrays.toString();
            default:
                throw new ParseException( "Unkown char", position );
        }
    }

    /**
     * Reads single class name. The first unparsed char should be l
     * 
     * @return
     */
    private String readClass() throws ParseException
    {
        int start = position;
        if ( str.charAt( position ) == 'L' )
        {
            position++;
            while ( position < str.length() && str.charAt( position++ ) != ';' )
                ;
            return str.substring( start + 1, position - 1 ).replace( '/', '.' );
        }
        else
            throw new ParseException( "L expected as first char of class name signature", position );
    }

}
