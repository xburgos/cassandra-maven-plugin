
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
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.model.Developer;
import org.codehaus.plexus.mailsender.simple.SimpleMailMessage;
import org.codehaus.plexus.util.IOUtil;


/**
 * Maven Announcement mailer goal.
 * @goal announcement-mail
 * @description Goal which sends the template thru email
 * @author aramirez@exist.com
 */

public class AnnouncementMailMojo extends AbstractMojo
{
    //=========================================
    // announcement-mail goal fields
    //=========================================

    /**
     * @parameter expression=${project}
     * @readonly
     */
    private MavenProject project;
    
    /**
     * Smtp Server
     * @parameter
     * @required
     */
    private String smtpHost;
    
    /**
     * Port
     * @parameter default-value="25";
     * @required
     */
    private int smtpPort;
    
    /**
     * Subject for the email
     * @parameter default-value="[ANNOUNCEMENT] - ${project.artifactId} ${project.version} release!"
     * @required
     */
    private String subject;
    
    /**
     * Recipient email address.
     * @parameter 
     * @required
     */
    private List toAddresses;
    
    /**
     * Sender
     * @parameter expression="${project.developers}"
     * @required
     */
    private List from;
    
    /**
     * Directory which contains the template for announcement email.
     * @parameter expression="${project.build.directory}/announcement/announcement.vm"
     * @required
     */
    private String file;
    
    private SimpleMailMessage simpleMailMessage;    
    
    
    public void execute() throws MojoExecutionException
    {              
        if( isTextFileExisting( getFile() ) )
        {  
            int i=0;

            while( i < getToAddresses().size() )
            {   
                String email = getToAddresses().get( i ).toString();

                sendMessage( email );

                i++;
            }
        }
        else
        {               
            throw new MojoExecutionException( "Announcement template not found..." );
        }
    }
    
    /**
     * Send the email 
     *
     * @param recipient receiver of the email
     * @throws MojoExecutionException
     */
    protected void sendMessage( String recipient ) throws MojoExecutionException
    {
        try
        {
            getLog().info( "Connecting to Host: " + getSmtpHost() + " : " + getSmtpPort() );

            getLog().info( getFile() + " found..." );

            simpleMailMessage = new SimpleMailMessage( getSmtpHost() , getSmtpPort() );
            
            simpleMailMessage.from( getFirstDevEmail( getFrom() ) );
            
            simpleMailMessage.to( recipient );

            simpleMailMessage.setSubject( getSubject() );

            simpleMailMessage.getPrintStream().print( IOUtil.toString( readAnnouncement( getFile() ) ) );

            getLog().info( "Sending mail... " + getToAddresses() );

            simpleMailMessage.sendAndClose();

            getLog().info("[INFO] Sent...");
        }
        catch( IOException ioe )
        {
            throw new MojoExecutionException( "Failed to send email.", ioe );
        }
    }
    
    protected boolean isTextFileExisting( String fileName )
    {
        boolean found = false;
        
        File f = new File( fileName );

        if( f.exists() )
        {
            found = true;
        }
        return found;
    }
    
    /**
     * Read the announcement generated file
     * @param  fileName         Accepts filename to be read.
     * @return  fileReader      Return the FileReader.
     */
    public FileReader readAnnouncement( String fileName ) throws MojoExecutionException
    {   
        FileReader fileReader = null;
        
        try
        {
            File file = new File( fileName );
            
            fileReader = new FileReader( file );
        }
        catch( FileNotFoundException fnfe )
        {
            throw new MojoExecutionException( "File not found. " + fileName );
        }
        return fileReader;
    }
    
    /**
     * Retrieve the 1st email address found in the list
     * @param fromNames         Accepts List of developers.
     * @return fromAddress      Returns the 1st email address found in the list.
     */
    public String getFirstDevEmail( List fromNames ) throws MojoExecutionException
    {
        String fromAddress = "";
        
        if( fromNames.size() > 0 )
        {
            Developer developer = ( Developer ) fromNames.get( 0 );

            fromAddress = developer.getEmail();

            getLog().info( "email retrieved. " + fromAddress );

            if( fromAddress == null  || fromAddress.equals( "" ) )
            {
                throw new MojoExecutionException( "Email address in <developers> section is required." );
            }
        }
        else
        {
            throw new MojoExecutionException( "Email address in <developers> section is required." );
        }
        return fromAddress;
    }
        
    //================================
    // announcement-mail accessors
    //================================
    
    public String getSmtpHost() 
    {
        return smtpHost;
    }

    public void setSmtpHost(String smtpHost) 
    {
        this.smtpHost = smtpHost;
    }

    public int getSmtpPort() 
    {
        return smtpPort;
    }

    public void setSmtpPort(int smtpPort) 
    {
        this.smtpPort = smtpPort;
    }

    public String getSubject() 
    {
        return subject;
    }

    public void setSubject(String subject) 
    {
        this.subject = subject;
    }

    public List getFrom() 
    {
        return from;
    }

    public void setFrom(List from) 
    {
        this.from = from;
    }

    public String getFile() 
    {
        return file;
    }

    public void setFile(String file) 
    {
        this.file = file;
    }

    public MavenProject getProject() 
    {
        return project;
    }

    public void setProject(MavenProject project) 
    {
        this.project = project;
    }

    public List getToAddresses() 
    {
        return toAddresses;
    }

    public void setToAddresses(List toAddresses) 
    {
        this.toAddresses = toAddresses;
    }
}
