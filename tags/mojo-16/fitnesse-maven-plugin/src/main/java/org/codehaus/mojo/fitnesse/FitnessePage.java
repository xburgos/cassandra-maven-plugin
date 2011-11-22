package org.codehaus.mojo.fitnesse;

import java.io.File;
import java.io.IOException;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.reporting.MavenReportException;

public class FitnessePage
{
    public static final String STATUS_OK = "OK";

    public static final String STATUS_FAIL = "Fail";

    public static final String STATUS_ERROR = "Error";

    private File mResultFile;

    private String mResultContent;

    public FitnessePage( File resultFile )
    {
        super();
        mResultFile = resultFile;
    }

    public FitnessePage( String resultContent )
    {
        super();
        mResultContent = resultContent;
    }

    public FitnessePage()
    {
    }

    public boolean isFitnessePageResult()
    {
        return mResultFile.getName().startsWith( FitnesseRunnerMojo.FITNESSE_RESULT_PREFIX )
                        && mResultFile.getName().endsWith( ".html" );
    }

    String getFitnessePageName() throws MavenReportException
    {
        String tPageName = mResultFile.getName();
        if ( tPageName.length() < ( FitnesseRunnerMojo.FITNESSE_RESULT_PREFIX.length() + 5 ) )
        {
            throw new MavenReportException( "Invalid report Name " + mResultFile.getName()
                            + ", the name should match [" + FitnesseRunnerMojo.FITNESSE_RESULT_PREFIX + "Xxx.xml]" );
        }
        tPageName =
            tPageName.substring( FitnesseRunnerMojo.FITNESSE_RESULT_PREFIX.length() + 1, tPageName.length() - 5 );
        return tPageName;
    }

    public String getName()
    {
        return mResultFile.getName();
    }

    public File getResultFile()
    {
        return mResultFile;
    }

    public void setFileName( String pFileName )
    {
        mResultFile = new File( pFileName );

    }

    public String getStatus() throws MojoExecutionException
    {
        try
        {
            if ( mResultContent == null )
            {
                mResultContent = FileUtil.getString( mResultFile );
            }
            if ( mResultContent.indexOf( "document.getElementById(\"test-summary\").className = \"pass" ) >= 0 )
            {
                return STATUS_OK;
            }
            else if ( mResultContent.indexOf( "document.getElementById(\"test-summary\").className = \"fail" ) >= 0 )
            {
                return STATUS_FAIL;
            }
            else if ( mResultContent.indexOf( "document.getElementById(\"test-summary\").className = \"error" ) >= 0 )
            {
                return STATUS_ERROR;
            }
            else
            {
                throw new MojoExecutionException( "This file isn't a FitNesse result page ["
                                + mResultFile.getAbsolutePath() + "]" );
            }

        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "Invalid file name [" + mResultFile.getAbsolutePath() + "]" );
        }
    }
}
