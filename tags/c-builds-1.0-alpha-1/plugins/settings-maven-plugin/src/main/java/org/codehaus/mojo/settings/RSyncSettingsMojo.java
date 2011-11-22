package org.codehaus.mojo.settings;

/*
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
 *
 */

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.shared.io.download.DownloadFailedException;
import org.apache.maven.shared.io.download.DownloadManager;
import org.apache.maven.shared.io.logging.DefaultMessageHolder;
import org.apache.maven.shared.io.logging.MessageHolder;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.interpolation.MapBasedValueSource;

/**
 * Download a setting.xml document from some remote location (URL or repository-artifact), and 
 * replace the user's settings.xml with the downloaded copy, optionally creating a backup first.
 * 
 * @goal rsync
 * @requiresProject false
 * @aggregator
 * 
 * @author jdcasey
 */
public class RSyncSettingsMojo
    extends AbstractMojo
{
    
    public static final String SETTINGS_TYPE = ".settings";
    
    /**
     * @parameter expression="${settings.url}"
     */
    private String url;
    
    /**
     * @parameter expression="${settings.groupId}"
     */
    private String groupId;
    
    /**
     * @parameter expression="${settings.artifactId}"
     */
    private String artifactId;
    
    /**
     * @parameter expression="${settings.version}"
     */
    private String version;
    
    /**
     * @parameter expression="${settings.classifier}"
     */
    private String classifier;
    
    /**
     * @parameter expression="${sync.doBackup}" default-value="true"
     */
    private boolean doBackup;
    
    /**
     * @parameter default-value="${project.remoteArtifactRepositories}"
     * @required
     * @readonly
     */
    private List remoteRepositories;
    
    /**
     * @parameter default-value="${localRepository}"
     * @required
     * @readonly
     */
    private ArtifactRepository localRepository;
    
    /**
     * @component
     */
    private ArtifactResolver resolver;
    
    /**
     * @component
     */
    private ArtifactFactory factory;
    
    /**
     * @component
     */
    private DownloadManager downloadManager;

    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        File newSettings = null;
        
        newSettings = getSettingsFromUrl();
        
        if ( newSettings == null )
        {
            getLog().info( "Could not get settings template from URL. Attempting artifact resolution..." );
            
            newSettings = getSettingsFromRepository();
        }
        
        if ( newSettings == null )
        {
            throw new MojoFailureException( this, "Cannot synchronize settings.", "Failed to retrieve new settings file. Cannot synchronize." );
        }
        
        // TODO: Allow adjustments to the settings location...or, better yet, DETECT SETTINGS LOCATION.
        String userHome = System.getProperty( "user.home" );
        
        File settingsFile = new File( userHome, ".m2/settings.xml" );

        if( settingsFile.exists() )
        {
        
            getLog().info( "ATTENTION: The Maven settings at: " + settingsFile.getAbsolutePath() + " is about to be replaced. Pausing 3 seconds..." );

            try
            {
                Thread.sleep( 3000 );
            }
            catch ( InterruptedException e )
            {
                getLog().warn( "3 second pause was interrupted! Continuing settings synchronization..." );
            }

            if ( doBackup )
            {
                File backupSettingsFile = new File( settingsFile.getAbsolutePath() + ".sync-backup" );

                getLog().info( "Your original settings.xml will be backed up to: " + backupSettingsFile.getPath() );

                if ( backupSettingsFile.exists() )
                {
                    backupSettingsFile.delete();
                }

                if ( !settingsFile.renameTo( backupSettingsFile ) )
                {
                    throw new MojoFailureException( this, "Cannot backup old settings.", "Failed to backup old settings file. Cannot synchronize." );
                }
            }
        }
        
        try
        {
            PrompterValueSource pvs = new PrompterValueSource();
            MapBasedValueSource mbvs = new MapBasedValueSource( System.getProperties() );
            
            List segments = new ArrayList();
            segments.add( Collections.singletonList( mbvs ) );
            segments.add( Collections.singletonList( pvs ) );
            
            SplitterValueSource svs = new SplitterValueSource( ":", segments );
            
            Interpolator interpolator = new Interpolator( Collections.singletonList( svs ) );

            interpolator.interpolate( newSettings, settingsFile, "prompt" );
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "Failed to transfer new settings to user's settings file. Reason: " + e.getMessage(), e );
        }
    }

    private File getSettingsFromRepository()
    {
        File result = null;
        
        if ( checkForArtifactViability() )
        {
            Artifact artifact = null;
            
            if ( StringUtils.isNotEmpty( classifier ) )
            {
                artifact = factory.createArtifactWithClassifier( groupId, artifactId, version, SETTINGS_TYPE, classifier );
            }
            else
            {
                artifact = factory.createArtifact( groupId, artifactId, version, Artifact.SCOPE_RUNTIME, SETTINGS_TYPE );
            }
            
            try
            {
                getLog().info( "Attempting to retrieve settings template from artifact: " + artifact.getId() );
                
                resolver.resolve( artifact, remoteRepositories, localRepository );
                
                getLog().info( "Settings template artifact was resolved to: " + artifact.getFile() );
                
                result = artifact.getFile();
            }
            catch ( ArtifactResolutionException e )
            {
                getLog().error( "Failed to resolve settings from artifact: " + e.getArtifactId() );
                getLog().debug( "Failed to resolve settings from artifact: " + e.getArtifactId(), e );
            }
            catch ( ArtifactNotFoundException e )
            {
                getLog().error( "Failed to resolve settings from artifact: " + e.getArtifactId() );
                getLog().debug( "Failed to resolve settings from artifact: " + e.getArtifactId(), e );
            }
        }
        
        return result;
    }

    private File getSettingsFromUrl()
    {
        File result = null;
        
        if ( StringUtils.isNotEmpty( url ) )
        {
            try
            {
                getLog().info( "Attempting to retrieve settings template from: " + url );
                
                MessageHolder mh = new DefaultMessageHolder();
                
                result = downloadManager.download( url, mh );
                
                if ( !mh.isEmpty() )
                {
                    getLog().debug( "DownloadManager gave the following debug output:\n\n" + mh.render() );
                }
            }
            catch ( DownloadFailedException e )
            {
                getLog().error( "Failed to download settings from URL: " + url );
                getLog().debug( "Failed to download settings from URL: " + url, e );
            }
        }
        
        return result;
    }

    private boolean checkForArtifactViability()
    {
        boolean groupIdViable = StringUtils.isNotEmpty( groupId );
        boolean artifactIdViable = StringUtils.isNotEmpty( artifactId );
        boolean versionViable = StringUtils.isNotEmpty( version );
        
        return groupIdViable && artifactIdViable && versionViable;
    }

}
