package org.codehaus.mojo.surefire;

import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;

import java.io.BufferedReader;

/*
 * Copyright 2001-2005 The Codehaus.
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
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

public class ReportTestSuite
    extends DefaultHandler
{
    private List testCases;
    private int numberOfErrors;
    private int numberOfFailures;
    private int numberOfTests;
    private String name;
    private String packageName;
    private float timeElapsed;
    String currentElement;
    String currentName;
    ReportTestCase testCase;

    public ReportTestSuite(  )
    {
    }

    public ReportTestSuite( String xmlPath )
    {
        SAXParserFactory factory = SAXParserFactory.newInstance(  );

        try
        {
            SAXParser saxParser = factory.newSAXParser(  );

            saxParser.parse( new File( xmlPath ),
                             this );
        } catch ( Throwable t )
        {
            t.printStackTrace(  );
        }
    }

    public void startElement( String namespaceURI, String sName, String qName, Attributes attrs )
                      throws SAXException
    {
        if ( qName.equals( "testsuite" ) )
        {
            numberOfErrors = Integer.parseInt( attrs.getValue( "errors" ) );

            numberOfFailures = Integer.parseInt( attrs.getValue( "failures" ) );

            numberOfTests = Integer.parseInt( attrs.getValue( "tests" ) );

            timeElapsed = Float.parseFloat( attrs.getValue( "time" ) );

            String fName = attrs.getValue( "name" );

            name = fName.substring( fName.lastIndexOf( "." ) + 1,
                                    fName.length(  ) );

            packageName = fName.substring( 0,
                                           fName.lastIndexOf( "." ) );
            testCases = new ArrayList(  );
        } else if ( qName.equals( "testcase" ) )
        {
            currentElement = "";

            testCase = new ReportTestCase(  );

            testCase.setName( attrs.getValue( "name" ) );

            testCase.setTime( Float.parseFloat( attrs.getValue( "time" ) ) );

            testCase.setFullName( packageName + "." + name + "." + testCase.getName(  ) );
        } else if ( qName.equals( "failure" ) )
        {
            HashMap failure = new HashMap(  );

            testCase.setFailure( failure );

            failure.put( "message",
                         attrs.getValue( "message" ) );

            failure.put( "type",
                         attrs.getValue( "type" ) );
        } else if ( qName.equals( "error" ) )
        {
            HashMap error = new HashMap(  );

            testCase.setFailure( error );

            error.put( "message",
                       attrs.getValue( "message" ) );

            error.put( "type",
                       attrs.getValue( "type" ) );
        }
    }

    public void endElement( String namespaceURI, String sName, String qName )
                    throws SAXException
    {
        if ( qName.equals( "testcase" ) )
        {
            testCases.add( testCase );
        } else if ( qName.equals( "failure" ) )
        {
            HashMap failure = testCase.getFailure(  );

            failure.put( "detail",
                         parseCause( currentElement ) );
        } else if ( qName.equals( "error" ) )
        {
            HashMap error = testCase.getFailure(  );

            error.put( "detail",
                       parseCause( currentElement ) );
        }
    }

    public void characters( char[] buf, int offset, int len )
                    throws SAXException
    {
        String s = new String( buf, offset, len );

        if ( ! s.trim(  ).equals( "" ) )
        {
            currentElement = currentElement + s.trim(  ) + "\n";
        }
    }

    public List getTestCases(  )
    {
        return this.testCases;
    }

    public void setTestCases( List TestCases )
    {
        this.testCases = TestCases;
    }

    public int getNumberOfErrors(  )
    {
        return numberOfErrors;
    }

    public void setNumberOfErrors( int numberOfErrors )
    {
        this.numberOfErrors = numberOfErrors;
    }

    public int getNumberOfFailures(  )
    {
        return numberOfFailures;
    }

    public void setNumberOfFailures( int numberOfFailures )
    {
        this.numberOfFailures = numberOfFailures;
    }

    public int getNumberOfTests(  )
    {
        return numberOfTests;
    }

    public void setNumberOfTests( int numberOfTests )
    {
        this.numberOfTests = numberOfTests;
    }

    public String getName(  )
    {
        return name;
    }

    public void setName( String name )
    {
        this.name = name;
    }

    public String getPackageName(  )
    {
        return packageName;
    }

    public void setPackageName( String packageName )
    {
        this.packageName = packageName;
    }

    public float getTimeElapsed(  )
    {
        return this.timeElapsed;
    }

    public void setTimeElapsed( float timeElapsed )
    {
        this.timeElapsed = timeElapsed;
    }

    private String parseCause( String detail )
    {
        String lineString = "";

        BufferedReader buffReader = new BufferedReader( new StringReader( detail ) );

        try
        {
            do
            {
                lineString = buffReader.readLine(  );

                if ( lineString == null )
                {
                    break;
                }
            } while ( ! ( lineString.trim(  ).startsWith( "at " + testCase.getFullName(  ) ) ) );
        } catch ( IOException e )
        {
            e.printStackTrace(  );
        }

        return lineString;
    }
}
