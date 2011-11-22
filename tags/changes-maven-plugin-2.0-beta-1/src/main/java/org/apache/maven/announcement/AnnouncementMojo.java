package org.apache.maven.announcement;

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
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Writer;
import java.io.IOException;
import java.util.*;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

import org.codehaus.plexus.velocity.VelocityComponent;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.context.Context;
import org.apache.velocity.exception.VelocityException;
import org.apache.velocity.exception.ResourceNotFoundException;

import org.apache.maven.changes.ChangesXML;
import org.apache.maven.changes.Release;

/**
 * @goal announcement-generate
 * @phase generate-sources
 * @description Goal which generate the template for announcement
 * @author aramirez@exist.com
 * @version $Id: AnnouncementMojo.java 422 2005-08-26 aqr $
 * 
 * @requiresDependencyResolution test
 */
public class AnnouncementMojo extends AbstractMojo 
{   
    /**
     * Directory where the template file will be generated
     * @parameter expression="${project.build.directory}/announcement"
     * @required
     */
    private String outputDirectory;
    
    /**
     * @parameter expression="${project.groupId}"
     * @readonly
     */
    private String groupId;
    
    /**
     * @parameter expression="${project.artifactId}"
     * @readonly
     */
    private String artifactId;
    
    /**
     * Version of the plugin
     * @parameter expression="${project.version}"
     * @readonly
     */
    private String version;
    
    /**
     * Distribution url of the plugin
     * @parameter expression="${project.url}"
     * @required
     */
    private String url;
    
    /**
     * Packaging structure based on the pom
     * @parameter expression="${project.packaging}"
     * @readonly
     */
    private String packaging;
    
    /**
     * @parameter expression="${project.build.finalName}.jar"
     * @required
     */
    private String finalName;
    
    /**
     * URL where the plugin can be downloaded
     * @parameter expression="${project.url}/${project.build.finalName}.jar"
     */
    private String urlDownload;
    
    /**
     * Directory which contains the changes.xml file
     * @parameter expression="${basedir}/src/main"
     * @required
     */
    private String xmlPath;
    
    /**
     * Name of the team that develops the project
     * @parameter default-value="${project.artifactId}-team"
     * @required
     */
    private String developmentTeam;
    
    /**
     * Short description or introduction of the released project.
     * @parameter expression="${project.description}"
     */
    private String introduction;
    
    /**
     * Velocity Component
     * @parameter expression="${component.org.codehaus.plexus.velocity.VelocityComponent}"
     * @readonly
     */
    private VelocityComponent velocity;
    
    /**
     * Name of the tempalte to be generated
     * @parameter default-value="announcement.vm"
     * @required
     */
    private String template;
    
    /**
     * Directory that contains the template
     * @parameter default-value="org/apache/maven/announcement"
     * @required
     */
    private String templateDirectory;
    
    /**
     * @parameter expression="true"
     * @required
     */
    private boolean useDefaultTemplate;
    
    private ChangesXML xml;
    
    private final String CHANGES_XML = "changes.xml";
    
    /**
     * Generate the template
     *
     * @throws MojoExecutionException
     */
    public void execute() throws MojoExecutionException 
    {
        String path = getXmlPath() + "/" + CHANGES_XML;
        
        setXml( new ChangesXML( path ) );
        
        doGenerate( getXml() );
    }
    
    /**
     * Add the parameters to velocity context
     *
     * @param xml parsed changes.xml
     * @throws MojoExecutionException
     */
    public void doGenerate(ChangesXML xml) throws MojoExecutionException 
    {
        try 
        {
            Context context = new VelocityContext();
            
            List releaseList = xml.getReleaseList();
            
            getLog().info( "Creating file..." );
            
            if( getIntroduction() == null || getIntroduction().equals( "" ) ) 
            {
                setIntroduction( getUrl() );
            }
            
            context.put( "releases"         , releaseList                       );
            
            context.put( "groupId"          , getGroupId()                      );
            
            context.put( "artifactId"       , getArtifactId()                   );
            
            context.put( "version"          , getVersion()                      );
            
            context.put( "packaging"        , getPackaging()                    );
            
            context.put( "url"              , getUrl()                          );
            
            context.put( "release"          , getLatestRelease( releaseList )   );
            
            context.put( "introduction"     , getIntroduction()                 );
            
            context.put( "developmentTeam"  , getDevelopmentTeam()              );
            
            context.put( "finalName"        , getFinalName()                    );
            
            context.put( "urlDownload"      , getUrlDownload()                  );
            
            processTemplate( context, getOutputDirectory(), template  );
        } 
        catch( ResourceNotFoundException rnfe ) 
        {
            throw new MojoExecutionException( "resource not found." );
        } 
        catch( VelocityException ve ) 
        {
            throw new MojoExecutionException( ve.toString() );
        }
        catch( IOException ioe ) 
        {
            throw new MojoExecutionException( ioe.toString() );
        }
    }
    
    /**
     * Get the latest release by matching the release in
     * changes.xml and in the version of the pom
     *
     * @param releases list of releases in changes.xml
     * @throws MojoExecutionException
     */
    public Release getLatestRelease( List releases ) throws MojoExecutionException 
    {
        boolean isFound = false;
        
        Release release = null;
        
        for( int i=0; i<releases.size(); i++ ) 
        {
            release = (Release) releases.get(i);
            
            if( release.getVersion().equals( getVersion() ) ) 
            {
                isFound = true;
                return release;
            }
        }
        
        if( isFound == false )
        {
            throw new MojoExecutionException( "Make sure that the latest release version of changes.xml matches the version of the POM." );
        }
        return release;
    }
    
    /**
     * Create the velocity template
     *
     * @param context velocity context that has the parameter values
     * @param outputDirectory directory where the file will be generated
     * @param template velocity template which will the context be merged
     * @throws ResourceNotFoundException, VelocityException, IOException
     */
    public void processTemplate( Context context, String outputDirectory, String template )
        throws ResourceNotFoundException, VelocityException, IOException, MojoExecutionException
    {
        File f;
        
        try
        {
            f = new File( outputDirectory, template );
            
            if ( !f.getParentFile().exists() ) 
            {
                f.getParentFile().mkdirs();
            } 
            
            Writer writer = new FileWriter( f );

            getVelocity().getEngine().mergeTemplate( templateDirectory + "/" + template, context, writer );
            
            writer.flush();
            
            writer.close();
            
            getLog().info( "File created..." );
        }
        
        catch( ResourceNotFoundException rnfe ) 
        {
            throw new ResourceNotFoundException( "Template not found. ( " + templateDirectory + "/" + template + " )" );
        } 
        catch( VelocityException ve) 
        {
            throw new VelocityException( ve.toString() );
        }
   
        catch( Exception e ) 
        {
            throw new MojoExecutionException( e.toString(), e.getCause() );
        }
    }

    /*
     * accessors
     */
    
    public String getXmlPath() 
    {
        return xmlPath;
    }
    
    public void setXmlPath(String xmlPath)
    {
        this.xmlPath = xmlPath;
    }
    
    public String getOutputDirectory()
    {
        return outputDirectory;
    }
    
    public void setOutputDirectory(String outputDirectory) 
    {
        this.outputDirectory = outputDirectory;
    }
    
    public String getGroupId() 
    {
        return groupId;
    }
    
    public void setGroupId(String groupId)
    {
        this.groupId = groupId;
    }
    
    public String getArtifactId() 
    {
        return artifactId;
    }
    
    public void setArtifactId(String artifactId)
    {
        this.artifactId = artifactId;
    }
    
    public String getVersion()
    {
        return version;
    }
    
    public void setVersion(String version)
    {
        this.version = version;
    }
    
    public String getUrl() 
    {
        return url;
    }
    
    public void setUrl(String url) 
    {
        this.url = url;
    }
    
    public ChangesXML getXml() 
    {
        return xml;
    }
    
    public void setXml(ChangesXML xml) 
    {
        this.xml = xml;
    }
    
    public String getPackaging() 
    {
        return packaging;
    }
    
    public void setPackaging(String packaging) 
    {
        this.packaging = packaging;
    }
    
    public String getDevelopmentTeam() 
    {
        return developmentTeam;
    }
    
    public void setDevelopmentTeam(String developmentTeam) 
    {
        this.developmentTeam = developmentTeam;
    }
    
    public String getIntroduction() 
    {
        return introduction;
    }
    
    public void setIntroduction(String introduction) 
    {
        this.introduction = introduction;
    }
    
    public VelocityComponent getVelocity()
    {
        return velocity;
    }
    
    public void setVelocity(VelocityComponent velocity) 
    {
        this.velocity = velocity;
    }
    
    public String getFinalName() 
    {
        return finalName;
    }
    
    public void setFinalName(String finalName) 
    {
        this.finalName = finalName;
    }
    
    public String getUrlDownload() 
    {
        return urlDownload;
    }
            
    public void setUrlDownload(String urlDownload) 
    {
        this.urlDownload = urlDownload;
    }
}
