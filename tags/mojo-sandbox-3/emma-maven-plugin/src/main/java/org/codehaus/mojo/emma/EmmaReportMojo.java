package org.codehaus.mojo.emma;

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
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.reporting.MavenReport;
import org.apache.maven.reporting.MavenReportException;
import org.codehaus.doxia.sink.Sink;

/**
 * Makes EMMA Coverage tests.
 * 
 * @author <a href="anna.nieslony@sdm.de">Anna Nieslony</a>
 * @goal report
 * @phase site
 * @description emma coverage test plugin
 */
public class EmmaReportMojo extends EmmaMojo implements MavenReport
{

    private String commandName = "report";

    /**
     * The project whose project files to create.
     * 
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    private MavenProject project;

    private File outputDirectory;

    public void execute() throws MojoExecutionException
    {
        try
        {
            generate( null, null );
        }
        catch ( MavenReportException e )
        {
            throw new MojoExecutionException( "Report generation threw an exception", e );
        }
    }

    public void generate( Sink sink, Locale locale ) throws MavenReportException
    {
        getLog().info( "[emma-plugin:report]" );
        File buildDir = new File( project.getBuild().getDirectory() );
        File emmaClassesDir = new File( buildDir, "emma-classes" );
        File ec = new File( emmaClassesDir, "coverage.ec" );
        if ( ec.exists() )
        {
            generateReport();
        }
        else
        {
            generateEmptyReport();
        }
    }

    private void generateReport() throws MavenReportException
    {
        File buildDir = new File( project.getBuild().getDirectory() );
        File emmaClassesDir = new File( buildDir, "emma-classes" );
        List args = new ArrayList();

        // Produce all three supported report types
        args.add( "-report" );
        args.add( "txt,xml,html" );

        // Add all files with meta data and coverage information
        File ec = new File( emmaClassesDir, "coverage.ec" );
        if ( ec.exists() )
        {
            args.add( "-input" );
            args.add( ec.getAbsolutePath() );
        }
        else
        {
            return; // Without overage.ec, there is nothing to report
        }
        File em = new File( emmaClassesDir, "coverage.em" );
        if ( em.exists() )
        {
            args.add( "-input" );
            args.add( em.getAbsolutePath() );
        }

        // Add all source paths (needed for html report)
        // -sp: comma or OS specific seperated (repeatable)
        List sourceDirs = new ArrayList(); // List of File (directories)

        // Add source directories from own project
        File sourceDir = new File( project.getBuild().getSourceDirectory() );
        if ( sourceDir.isDirectory() )
        {
            sourceDirs.add( sourceDir );
        }
        Iterator sdIter = sourceDirs.iterator();
        while ( sdIter.hasNext() )
        {
            File d = (File) sdIter.next();
            args.add( "-sourcepath" );
            args.add( d.getAbsolutePath() );
        }

        // Write a property file and point emma to it.
        // For documentaion of properties see:
        // http://emma.sourceforge.net/reference_single/reference.html#prop-ref.lookup

        // Hm, this should be project.getReporting().getOutputDirectory(), but that
        // gives always the master site directory in a reactor build.
        File siteDir = new File( buildDir, "site" );
        File coverageDir = new File( siteDir, "coverage" );
        File propFile = new File( emmaClassesDir, "report.properties" );
        try
        {
            PrintWriter propWriter = new PrintWriter( new FileWriter( propFile ) );
            propWriter.println( "report.txt.out.file="
                            + propertyFormat( new File( coverageDir, "coverage.txt" ).getAbsolutePath() ) );
            propWriter.println( "report.html.out.file="
                            + propertyFormat( new File( coverageDir, "index.html" ).getAbsolutePath() ) );
            propWriter.println( "report.xml.out.file="
                            + propertyFormat( new File( coverageDir, "report.xml" ).getAbsolutePath() ) );
            propWriter.close();
            if ( propWriter.checkError() )
                throw new MavenReportException( "Can't write report.properties" );
        }
        catch ( IOException e )
        {
            throw new MavenReportException( "Can't write report.properties", e );
        }
        // -properties to select property file
        args.add( "-properties" );
        args.add( propFile.getAbsolutePath() );

        // Create directory for coverage
        getLog().info( "Creating coverage dir: " + coverageDir.getAbsolutePath() );
        if ( !coverageDir.exists() )
        {
            if ( !coverageDir.mkdirs() )
            {
                throw new MavenReportException( "Can't create dir: " + coverageDir.getAbsolutePath() );
            }
        }

        // Finally: Run it...
        getLog().debug( "Generating Emma Reports." );
        runEmma( commandName, args );
    }

    private void generateEmptyReport() throws MavenReportException
    {
        File buildDir = new File( project.getBuild().getDirectory() );
        File siteDir = new File( buildDir, "site" );
        File coverageDir = new File( siteDir, "coverage" );

        // Create directory for coverage
        getLog().info( "Creating coverage dir: " + coverageDir.getAbsolutePath() );
        if ( !coverageDir.exists() )
        {
            if ( !coverageDir.mkdirs() )
            {
                throw new MavenReportException( "Can't create dir: " + coverageDir.getAbsolutePath() );
            }
        }

        File propFile = new File( coverageDir, "index.html" );
        try
        {
            PrintWriter writer = new PrintWriter( new FileWriter( propFile ) );
            writer.println( "<HTML><HEAD><META CONTENT=\"text/html; charset=ISO-8859-1\" HTTP-EQUIV=\"Content-Type\"/>" );
            writer.println( "<TITLE>Empty Report</TITLE>" );
            writer.println( "<STYLE TYPE=\"text/css\">" );
            writer.println( "P,H1,H2,H3,TH {font-family:verdana,arial,sans-serif;font-size:10pt;}" );
            writer.println( "</STYLE>" );
            writer.println( "</HEAD>" );
            writer.println( "<BODY>" );
            writer.println( "<H2>No coverage information available</H2>" );
            writer.println( "</BODY>" );
            writer.println( "</HTML>" );

            writer.close();
            if ( writer.checkError() )
                throw new MavenReportException( "Can't write index.html" );
        }
        catch ( IOException e )
        {
            throw new MavenReportException( "Can't write index.html", e );
        }
    }

    public String getOutputName()
    {
        return "coverage/index"; // .html is added by maven
    }

    public String getName( Locale locale )
    {
        return "EMMA coverage report";
    }

    public String getCategoryName()
    {
        return CATEGORY_PROJECT_REPORTS;
    }

    public String getDescription( Locale locale )
    {
        return "Coverage report generated by EMMA";
    }

    public void setReportOutputDirectory( File outputDirectory )
    {
        this.outputDirectory = outputDirectory;
    }

    public File getReportOutputDirectory()
    {
        return outputDirectory;
    }

    public boolean isExternalReport()
    {
        return true;
    }

    public boolean canGenerateReport()
    {
        return true;
    }
}
