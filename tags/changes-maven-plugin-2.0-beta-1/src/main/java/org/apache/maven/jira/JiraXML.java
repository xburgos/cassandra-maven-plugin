package org.apache.maven.jira;

import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

public class JiraXML
    extends DefaultHandler
{
    private List issueList;
    String currentElement;
    String currentParent = "";
    JiraIssue issue;

    public JiraXML( String xmlPath )
    {
        SAXParserFactory factory = SAXParserFactory.newInstance(  );

        issueList = new ArrayList(  );

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
        if ( qName.equals( "item" ) )
        {
            issue = new JiraIssue(  );

            currentParent = "item";
        }
    }

    public void endElement( String namespaceURI, String sName, String qName )
                    throws SAXException
    {
        if ( qName.equals( "item" ) )
        {
            issueList.add( issue );

            currentParent = "";
        } else if ( qName.equals( "key" ) )
        {
            issue.setKey( currentElement );
        } else if ( qName.equals( "summary" ) )
        {
            issue.setSummary( currentElement );
        } else if ( qName.equals( "link" ) && currentParent.equals( "item" ) )
        {
            issue.setLink( currentElement );
        } else if ( qName.equals( "status" ) )
        {
            issue.setStatus( currentElement );
        } else if ( qName.equals( "resolution" ) )
        {
            issue.setResolution( currentElement );
        } else if ( qName.equals( "assignee" ) )
        {
            issue.setAssignee( currentElement );
        }

        currentElement = "";
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

    public List getIssueList(  )
    {
        return this.issueList;
    }
}
