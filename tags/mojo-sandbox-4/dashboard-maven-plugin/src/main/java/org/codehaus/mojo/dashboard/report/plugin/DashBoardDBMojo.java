package org.codehaus.mojo.dashboard.report.plugin;

/*
 * Copyright 2007 David Vicente
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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.mojo.dashboard.report.plugin.beans.AbstractReportBean;
import org.codehaus.mojo.dashboard.report.plugin.beans.DashBoardMavenProject;
import org.hibernate.Query;

/**
 * A Dashboard report which aggregates all other report results and stores all results in database.
 * 
 * @author <a href="dvicente72@gmail.com">David Vicente</a>
 * @goal persist
 * 
 */
public class DashBoardDBMojo extends AbstractDashBoardMojo
{


    private Date generatedDate;

    private boolean isPropHibernateSet = false;

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.maven.plugin.Mojo#execute()
     */
    public void execute() throws MojoExecutionException, MojoFailureException
    {

        boolean persistDB = canPersistDB();
        boolean recursive = isRecursive();
        if(recursive)
        {
            dashBoardUtils = DashBoardUtils.getInstance( getLog(), mavenProjectBuilder, localRepository, true );
            generatedDate = new Date( System.currentTimeMillis() );
            DashBoardMavenProject mavenProject =
                dashBoardUtils.getDashBoardMavenProject( project, dashboardDataFile, generatedDate );
            dashBoardUtils.saveXMLDashBoardReport( project, mavenProject, dashboardDataFile );
            if ( persistDB )
            {
                configureHibernateDriver();
    
                long start = System.currentTimeMillis();
    
                getLog().info( "DashBoardDBMojo project = " + project.getName() );
                getLog().info( "DashBoardDBMojo nb modules = " + project.getModules().size() );
                getLog().info( "DashBoardDBMojo is root = " + project.isExecutionRoot() );
                getLog().info( "DashBoardDBMojo base directory = " + project.getBasedir() );
                getLog().info( "DashBoardDBMojo output directory = " + outputDirectory );
                getLog().info(
                               "DashBoardDBMojo project language = "
                                               + project.getArtifact().getArtifactHandler().getLanguage() );
                refactorMavenProject( mavenProject );
    
                hibernateService.saveOrUpdate( mavenProject );
    
                long end = System.currentTimeMillis();
                SimpleDateFormat formatter = new SimpleDateFormat( "HH:mm:ss:SSS", Locale.getDefault() );
                getLog().info(
                               "DashBoardDBMojo save Dashboard elapsed time = "
                                               + formatter.format( new Date( end - start ) ) );
            }
        }
    }

    private boolean canPersistDB()
    {
        boolean persist = false;

        boolean recursive = isRecursive();
        boolean root = project.isExecutionRoot();

        isPropHibernateSet = isDBAvailable();
        
        if ( recursive && root && isPropHibernateSet )
        {
            persist = true;
        }
        else
        {
            if ( !root )
            {
                getLog().warn( "DashBoardDBMojo: Not root project - skipping persist goal." );
            }
            if ( !isPropHibernateSet )
            {
                getLog().warn( "DashBoardDBMojo: Hibernate properties not set - skipping persist goal." );
            }
        }

        return persist;
    }
    private boolean isRecursive()
    {
       boolean recursive = ( project.getCollectedProjects().size() < project.getModules().size() ) ? false : true;
        if ( !recursive )
        {
            getLog().warn( "DashBoardDBMojo: Not recursive into sub-projects - skipping XML generation." );
        }
        return recursive;
    }

    private void refactorMavenProject( DashBoardMavenProject mavenProject )
    {
        StringBuffer queryString = new StringBuffer();
        queryString.append( "select m.id from DashBoardMavenProject m where " );
        queryString.append( "m.artifactId = :artifactid " );
        queryString.append( "and m.groupId = :groupid " );
        queryString.append( "and m.version = :version " );

        Query query = hibernateService.getSession().getNamedQuery( "org.codehaus.mojo.dashboard.report.plugin.beans.DashBoardMavenProject.getDashBoardMavenProjectID" );
        query.setParameter( "artifactid", mavenProject.getArtifactId() );
        query.setParameter( "groupid", mavenProject.getGroupId() );
        query.setParameter( "version", mavenProject.getVersion() );
        List result = query.list();
        if ( result != null && !result.isEmpty() )
        {
            long id=((Long)(result.get( 0 ) )).longValue();
            mavenProject.setId( id );
        }
        
        Set reports = mavenProject.getReports();
        Iterator iter = reports.iterator();
        while ( iter.hasNext() )
        {
            AbstractReportBean report = (AbstractReportBean) iter.next();
            if( report != null )
            {
                report.setMavenProject( mavenProject );
            }
        }
        Set modules = mavenProject.getModules();
        Iterator iterModule = modules.iterator();
        while ( iterModule.hasNext() )
        {
            DashBoardMavenProject project = (DashBoardMavenProject) iterModule.next();
            refactorMavenProject( project );
        }
    }

}
