package org.codehaus.mojo.fitnesse;

import static org.codehaus.mojo.fitnesse.FitnesseRunnerMojo.FITNESSE_RESULT_PREFIX;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

import org.apache.maven.doxia.siterenderer.Renderer;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.apache.maven.reporting.AbstractMavenReport;
import org.apache.maven.reporting.MavenReportException;

/**
 * Generates a <a href="http://fitnesse.org">FitNesse</a> report from a FitNesse web server. The generated report is an
 * external report generated FitNesse itself. If the project use Clover for code coverage and if FitNesse has clover
 * dependency (ie use the <i>ArtifactId-Version-clover.jar</i>), the code executed during the FitNesse execution (phase
 * integration-test) will be had to the unit-test code coverage. See the <a href="examples/multiproject.html">clover example</a>.
 * 
 * @goal fitnesse
 * @aggregator
 */
public class FitnesseReportMojo extends AbstractMavenReport
{

    /**
     * The Maven project instance for the executing project.
     * 
     * <p>
     * Note: This is passed by Maven and must not be configured by the user.
     * </p>
     * 
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    private MavenProject project;

    /**
     * Report output directory.
     * 
     * @parameter expression="${project.build.directory}/generated-site/xdoc/fitnesse"
     * @required
     */
    private File xmlOutputDirectory;

    /**
     * The directory where the Fitnesse report will be generated.
     * 
     * @parameter expression="${project.reporting.outputDirectory}/fitnesse"
     * @required
     */
    private File outputDirectory;

    /**
     * The directory where the Fitnesse report has be generated. It must be defined when it's not the default value
     * (${project.build.directory}/fitnesse. It's the case for exemple with the clover plugin (that use
     * ${project.build.directory}/clover/fitnesse).
     * 
     * @parameter
     */
    private File fitnesseOutputDirectory;

    /**
     * @parameter expression="${project.build.directory}"
     * @required
     */
    private File workingDir;

    /**
     * <p>
     * Note: This is passed by Maven and must not be configured by the user.
     * </p>
     * 
     * @component
     */
    private Renderer siteRenderer;

    /**
     * @see org.apache.maven.reporting.AbstractMavenReport#executeReport(java.util.Locale)
     */
    protected void executeReport( Locale pArg0 ) throws MavenReportException
    {
        // Ensure the output directory exists
        this.outputDirectory.mkdirs();
        this.xmlOutputDirectory.mkdirs();

        checkReport();

        createReport();
        createIndex();
        copyResource( "fitnesse.js" );
        copyResource( "fitnesse_base.css" );
        copyResource( "fitnesse_print.css" );
        // images
        new File( this.outputDirectory + "/images" ).mkdir();
        copyResource( "images/collapsableClosed.gif" );
        copyResource( "images/collapsableOpen.gif" );
        copyResource( "images/FitNesseLogo.gif" );
        copyResource( "images/FitNesseLogoMedium.jpg" );
        copyResource( "images/folder.gif" );
        copyResource( "images/importedPage.jpg" );
        copyResource( "images/virtualPage.jpg" );
        new File( this.outputDirectory + "/images/executionStatus" ).mkdir();
        copyResource( "images/executionStatus/error.gif" );
        copyResource( "images/executionStatus/ok.gif" );
        copyResource( "images/executionStatus/output.gif" );

        getLog().info( "Fitnesse report finished" );
    }

    private void copyResource( String pFileName ) throws MavenReportException
    {
        File tDest = new File( this.outputDirectory + "/" + pFileName );
        copyFile( getLog(), getClass().getClassLoader().getResourceAsStream( "fitnesse_resources/" + pFileName ), tDest );
    }

    private void createReport() throws MavenReportException
    {
        for ( File curFile : getFitnesseReportDir().listFiles() )
        {
            if ( !curFile.exists() )
            {
                throw new MavenReportException( "Unable to find Fitnesse report for server " + curFile );
            }
            File tDestFile = new File( outputDirectory + "/" + curFile.getName() );
            try
            {
                copyFile( getLog(), new FileInputStream( curFile ), tDestFile );
            }
            catch ( IOException e )
            {
                throw new MavenReportException( "Unable to create File [" + curFile.getAbsolutePath() + "].", e );
            }
        }
    }

    void checkReport() throws MavenReportException
    {
        if ( getFitnesseReportDir().listFiles().length == 0 )
        {
            getLog().error(
                            "Your should configure at least one Fitnesse server. "
                                            + "Check your Fitnesse plugin configuration." );
            throw new MavenReportException( "Your should configure at least one Fitnesse server. "
                            + "Check your Fitnesse plugin configuration." );
        }
    }

    static void copyFile( Log pLogger, InputStream pIn, File pDestFile ) throws MavenReportException
    {
        FileOutputStream tOut = null;
        try
        {
            if ( !pDestFile.exists() )
            {
                pDestFile.createNewFile();
            }
            tOut = new FileOutputStream( pDestFile );
            byte[] tBuff = new byte[100];
            int tRead = pIn.read( tBuff );
            while ( tRead >= 0 )
            {
                tOut.write( tBuff, 0, tRead );
                tRead = pIn.read( tBuff );
            }
            pLogger.debug( "Report copied to " + pDestFile );
            pLogger.debug( "Report exist " + pDestFile.exists() );
        }
        catch ( FileNotFoundException e )
        {
            throw new MavenReportException( "File doesn't exist", e );
        }
        catch ( IOException e )
        {
            throw new MavenReportException( "Unable to write into file...", e );
        }
        finally
        {
            try
            {
                if ( tOut != null )
                {
                    tOut.close();
                }
                if ( pIn != null )
                {
                    pIn.close();
                }
            }
            catch ( IOException e )
            {
                throw new MavenReportException( "Unable to close report file report...", e );
            }
        }
    }

    File getFitnesseReportDir() throws MavenReportException
    {
        File tExecutionFile;
        if ( fitnesseOutputDirectory != null )
        {
            getLog().info( "Using the specified fitnesse outpout directory " + fitnesseOutputDirectory );
            tExecutionFile = fitnesseOutputDirectory;
        }
        else
        {
            getLog().debug( "Trying to find the fitnesse default dir..." );
            tExecutionFile = new File( workingDir + "/fitnesse" );
            if ( !tExecutionFile.exists() )
            {
                getLog().info( "Fitnesse default report not found, " + tExecutionFile );
                getLog().debug( "Trying to find the fitnesse with clover dir..." );
                tExecutionFile = new File( workingDir + "/clover/fitnesse" );
            }
        }

        if ( !tExecutionFile.exists() || !tExecutionFile.isDirectory() || tExecutionFile.list().length == 0 )
        {
            throw new MavenReportException( "The directory doesn't contain any Fitnesse report: "
                            + tExecutionFile.getAbsolutePath() );
        }
        else
        {
            return tExecutionFile;
        }
    }

    void createIndex() throws MavenReportException
    {
        if ( outputDirectory.listFiles().length > 1 )
        {
            File tIndex = new File( this.xmlOutputDirectory + "/index.xml" );
            FileWriter tWriter = null;
            try
            {
                tIndex.createNewFile();
                tWriter = new FileWriter( tIndex );
                tWriter.append( "<document>\n" );
                tWriter.append( " <properties>\n" );
                tWriter.append( "   <title>maven-fitnesse-plugin - execution report</title>\n" );
                tWriter.append( " </properties>\n" );
                tWriter.append( " <body>\n" );

                tWriter.append( "<section name=\"List of the Fitnesse Pages:\n\">" );
                tWriter.append( "<ul>\n" );
                for ( File curChil : outputDirectory.listFiles() )
                {
                    if ( !"index.html".equals( curChil.getName() ) && !curChil.getName().endsWith( ".css" )
                                    && !curChil.getName().endsWith( ".js" ) )
                    {
                        tWriter.append( "<li><a href=\"" + curChil.getName() + "\">" + getFitnessePageName( curChil )
                                        + ".html</a></li>\n" );
                    }
                }
                tWriter.append( "</ul>\n" );
                tWriter.append( "</section>\n" );
                tWriter.append( "</body>\n" );
                tWriter.append( "</document>\n" );
                tWriter.flush();
            }
            catch ( IOException e )
            {
                throw new MavenReportException( "Unable to create index file " + tIndex.getAbsolutePath(), e );
            }
            finally
            {
                if ( tWriter != null )
                {
                    try
                    {
                        tWriter.close();
                    }
                    catch ( IOException e )
                    {
                        throw new MavenReportException( "Unable to close index file " + tIndex.getAbsolutePath(), e );
                    }
                }
            }
        }
    }

    String getFitnessePageName( File curChil ) throws MavenReportException
    {
        String tPageName = curChil.getName();
        if ( tPageName.length() < ( FITNESSE_RESULT_PREFIX.length() + 4 ) )
        {
            throw new MavenReportException( "Invalid report Name " + curChil.getName() + ", the name should match ["
                            + FITNESSE_RESULT_PREFIX + "Xxx.xml]" );
        }
        tPageName = tPageName.substring( FITNESSE_RESULT_PREFIX.length(), tPageName.length() - 5 );
        return tPageName;
    }

    /**
     * @see org.apache.maven.reporting.AbstractMavenReport#getOutputDirectory()
     */
    protected String getOutputDirectory()
    {
        return this.outputDirectory.getAbsoluteFile().toString();
    }

    /**
     * @see org.apache.maven.reporting.AbstractMavenReport#getProject()
     */
    protected MavenProject getProject()
    {
        return project;
    }

    /**
     * @see org.apache.maven.reporting.AbstractMavenReport#getSiteRenderer()
     */
    protected Renderer getSiteRenderer()
    {
        return this.siteRenderer;
    }

    /**
     * @see org.apache.maven.reporting.MavenReport#getDescription(java.util.Locale)
     */
    public String getDescription( Locale locale )
    {
        return "Fitnesse report";
    }

    /**
     * @see org.apache.maven.reporting.MavenReport#getName(java.util.Locale)
     */
    public String getName( Locale locale )
    {
        return "Fitnesse report";
    }

    /**
     * @see org.apache.maven.reporting.MavenReport#getOutputName()
     */
    public String getOutputName()
    {
        File tDir;
        try
        {
            tDir = getFitnesseReportDir();
            if ( tDir.listFiles().length == 1 )
            {
                return "fitnesse" + FITNESSE_RESULT_PREFIX + "_" + getFitnessePageName( tDir.listFiles()[0] );
            }
            else
            {
                return "fitnesse/index";
            }
        }
        catch ( MavenReportException e )
        {
            throw new RuntimeException( e );
        }
    }

    /**
     * Always return true as we're using the report generated by Clover rather than creating our own report.
     * 
     * @return true
     */
    public boolean isExternalReport()
    {
        return true;
    }

    void setWorkingDir( File pWorkingDir )
    {
        workingDir = pWorkingDir;
    }

    void setFitnesseOutputDirectory( File pFitnesseOutputDirectory )
    {
        fitnesseOutputDirectory = pFitnesseOutputDirectory;
    }

    void setOutputDirectory( File pOutputDirectory )
    {
        outputDirectory = pOutputDirectory;
    }

    public void setXmlOutputDirectory( File xmlOutputDirectory )
    {
        this.xmlOutputDirectory = xmlOutputDirectory;
    }

}
