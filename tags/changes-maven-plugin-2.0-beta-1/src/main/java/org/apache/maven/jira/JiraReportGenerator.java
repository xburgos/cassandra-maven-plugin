package org.apache.maven.jira;

import java.util.List;
import java.util.ResourceBundle;

import org.codehaus.doxia.sink.Sink;

public class JiraReportGenerator
{
    JiraXML jira;

    public JiraReportGenerator(  )
    {
     
    }
    
    public JiraReportGenerator( String xmlPath )
    {
        jira = new JiraXML( xmlPath );
    }

    public void doGenerateReport( ResourceBundle bundle, Sink sink )
    {
        List issueList = jira.getIssueList(  );

        sinkBeginReport( sink, bundle );

        constructHeaderRow( sink, issueList, bundle );

        constructDetailRows( sink, issueList );

        sinkEndReport( sink );
    }

    public void doGenerateEmptyReport( ResourceBundle bundle, Sink sink)
    {
        sinkBeginReport( sink, bundle );
    
        sinkEndReport( sink );
    }
    
    private void constructHeaderRow( Sink sink, List issueList, ResourceBundle bundle )
    {
        if ( issueList == null )
        {
            return;
        }

        sink.table(  );

        sink.tableRow(  );

        sinkHeader( sink,
                    bundle.getString( "report.jira.label.key" ) );

        sinkHeader( sink,
                    bundle.getString( "report.jira.label.summary" ) );

        sinkHeader( sink,
                    bundle.getString( "report.jira.label.status" ) );

        sinkHeader( sink,
                    bundle.getString( "report.jira.label.resolution" ) );

        sinkHeader( sink,
                    bundle.getString( "report.jira.label.by" ) );

        sink.tableRow_(  );
    }

    private void constructDetailRows( Sink sink, List issueList )
    {
        if ( issueList == null )
        {
            return;
        }

        for ( int idx = 0; idx < issueList.size(  ); idx++ )
        {
            JiraIssue issue = (JiraIssue) issueList.get( idx );

            sink.tableRow(  );

            sink.tableCell(  );

            sink.link( issue.getLink(  ) );

            sink.text( issue.getKey(  ) );

            sinkFigure( sink, "images/external.png" );

            sink.tableCell_(  );

            sinkCell( sink,
                      issue.getSummary(  ) );

            sinkCell( sink,
                      issue.getStatus(  ) );

            sinkCell( sink,
                      issue.getResolution(  ) );

            sinkCell( sink,
                      issue.getAssignee(  ) );

            sink.tableRow_(  );
        }

        sink.table_(  );
    }

    private void sinkBeginReport( Sink sink, ResourceBundle bundle )
    {
        sink.head(  );

        sink.text( bundle.getString( "report.jira.header" ) );

        sink.head_(  );

        sink.body(  );
        
        sinkSectionTitle1( sink,
                           bundle.getString( "report.jira.header" ) );

    }

    private void sinkEndReport( Sink sink )
    {
        sink.body_(  );

        sink.flush(  );

        sink.close(  );
    }

    private void sinkFigure( Sink sink, String image )
    {
        sink.figure(  );

        sink.figureGraphics( image );

        sink.figure_(  );
    }

    private void sinkHeader( Sink sink, String header )
    {
        sink.tableHeaderCell(  );

        sink.text( header );

        sink.tableHeaderCell_(  );
    }

    private void sinkCell( Sink sink, String text )
    {
        sink.tableCell(  );

        sink.rawText( text );

        sink.tableCell_(  );
    }

    private void sinkSectionTitle1( Sink sink, String text )
    {
        sink.sectionTitle1(  );

        sink.text( text );

        sink.sectionTitle1_(  );
    }
}
