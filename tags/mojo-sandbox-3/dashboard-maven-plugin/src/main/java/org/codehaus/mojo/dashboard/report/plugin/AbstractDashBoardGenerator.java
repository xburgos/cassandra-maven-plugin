package org.codehaus.mojo.dashboard.report.plugin;

/*
 * Copyright 2006 David Vicente
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


import java.text.NumberFormat;

import org.codehaus.doxia.sink.Sink;

/**
 * 
 * @author <a href="dvicente72@gmail.com">David Vicente</a>
 * 
 */
public abstract class AbstractDashBoardGenerator
{
    private String imagesPath = "";
    
    private static final int SPACE_NUMBER = 5;
    
    private int nbExportedPackagesSummary = 10;
    
    private char[] forbiddenChar = new char[]{'\\','/',':','*','?','"','<','>',';'};

    protected void sinkHeader( Sink sink, String header )
    {
        sink.tableHeaderCell();
        sink.text( header );
        sink.tableHeaderCell_();
    }

    protected void sinkCell( Sink sink, String text )
    {
        sink.tableCell();
        sink.text( text );
        sink.tableCell_();
    }

    protected void sinkCellBold( Sink sink, String text )
    {
        sink.tableCell();
        sink.bold();
        sink.text( text );
        sink.bold_();
        sink.tableCell_();
    }
    
    protected void sinkCellWithLink( Sink sink, String text, String link )
    {
        sink.tableCell();
        sink.link( link );
        sink.text( text );
        sink.link_();
        sink.tableCell_();
    }

    protected void sinkCellBoldWithLink( Sink sink, String text, String link )
    {
        sink.tableCell();
        sink.bold();
        sink.link( link );
        sink.text( text );
        sink.link_();
        sink.bold_();
        sink.tableCell_();
    }

    protected void sinkCellTab( Sink sink, String text, int nbTabulation )
    {
        sink.tableCell();
        int loop = AbstractDashBoardGenerator.SPACE_NUMBER * nbTabulation;
        for ( int i = 0; i < loop; i++ )
        {
            sink.nonBreakingSpace();
        }
        sink.text( text );
        sink.tableCell_();
    }
    
    protected void sinkCellTabWithLink( Sink sink, String text, int nbTabulation, String link )
    {
        sink.tableCell();
        int loop = AbstractDashBoardGenerator.SPACE_NUMBER * nbTabulation;
        for ( int i = 0; i < loop; i++ )
        {
            sink.nonBreakingSpace();
        }
        sink.link( link );
        sink.text( text );
        sink.link_();
        sink.tableCell_();
    }

    protected void sinkCellTabBold( Sink sink, String text, int nbTabulation )
    {
        sink.tableCell();
        int loop = AbstractDashBoardGenerator.SPACE_NUMBER * nbTabulation;
        for ( int i = 0; i < loop; i++ )
        {
            sink.nonBreakingSpace();
        }
        sink.bold();
        sink.text( text );
        sink.bold_();
        sink.tableCell_();
    }
    
    protected void sinkCellTabBoldWithLink( Sink sink, String text, int nbTabulation, String link )
    {
        sink.tableCell();
        int loop = AbstractDashBoardGenerator.SPACE_NUMBER * nbTabulation;
        for ( int i = 0; i < loop; i++ )
        {
            sink.nonBreakingSpace();
        }
        sink.bold();
        sink.link( link );
        sink.text( text );
        sink.link_();
        sink.bold_();
        sink.tableCell_();
    }

    protected static String getPercentValue( double value )
    {
        String sValue;
        NumberFormat formatter;
        formatter = NumberFormat.getPercentInstance();
        if ( value == -1.0 )
        {
            sValue = "0%";
        }
        else
        {
            sValue = formatter.format( value );
        }
        return sValue;
    }

    protected void iconInfo( Sink sink )
    {
        sink.figure();
        sink.figureCaption();
        sink.text( "info" );
        sink.figureCaption_();
        sink.figureGraphics( "images/icon_info_sml.gif" );
        sink.figure_();
    }

    protected void iconWarning( Sink sink )
    {
        sink.figure();
        sink.figureCaption();
        sink.text( "warning" );
        sink.figureCaption_();
        sink.figureGraphics( "images/icon_warning_sml.gif" );
        sink.figure_();
    }

    protected void iconError( Sink sink )
    {
        sink.figure();
        sink.figureCaption();
        sink.text( "error" );
        sink.figureCaption_();
        sink.figureGraphics( "images/icon_error_sml.gif" );
        sink.figure_();
    }

    protected void linkToTopPage( Sink sink )
    {
        sink.bold();
        sink.text( "[" );
        sink.link( "#top" );
        sink.text( "Top" );
        sink.link_();
        sink.text( "]" );
        sink.bold_();
    }

    protected void setImagesPath( String path )
    {
        this.imagesPath = path;
    }
    
    protected String getImagesPath()
    {
        return this.imagesPath;
    }

    public void setNbExportedPackagesSummary( int nbExportedPackagesSummary )
    {
        this.nbExportedPackagesSummary = nbExportedPackagesSummary;
    }

    public int getNbExportedPackagesSummary()
    {
        return nbExportedPackagesSummary;
    }
    /**
     * replace all invalid characters as {'\\','/',':','*','?','"','<','>',';'}
     * by '-'.
     * MOJO-623 correction
     * @param value
     * @return
     */
    protected String replaceForbiddenChar(String value)
    {
		String replaced = value;
		for ( int i = 0; i < forbiddenChar.length; i++ ) 
		{
			char rep = forbiddenChar[i];
			replaced = replaced.replace( rep, '-' );
		}
		return replaced;
	}
}
