package org.codehaus.mojo.bod.source.locator;

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
import java.util.Iterator;

import org.apache.maven.model.Scm;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.io.location.FileLocation;
import org.apache.maven.shared.io.location.Location;
import org.apache.maven.shared.io.location.LocatorStrategy;
import org.apache.maven.shared.io.logging.MessageHolder;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.command.checkout.CheckOutScmResult;
import org.apache.maven.scm.provider.ScmProvider;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.ScmProviderRepositoryWithHost;
import org.apache.maven.scm.provider.svn.repository.SvnScmProviderRepository;
import org.apache.maven.scm.repository.ScmRepository;
import org.apache.maven.scm.repository.ScmRepositoryException;
import org.apache.maven.scm.manager.NoSuchScmProviderException;
import org.apache.maven.scm.manager.ScmManager;
import org.apache.maven.settings.Server;
import org.apache.maven.settings.Settings;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.StringUtils;

public class ScmLocatorStrategy
    implements LocatorStrategy
{
    private MavenProject project;
    private ScmManager scmManager;
    private String username;
    private String password;
    private Settings settings;
    private File checkoutDir;
    
    public ScmLocatorStrategy( MavenProject project, ScmManager scmManager, 
                               String username, String password, Settings settings, 
                               File checkoutDir )
    {
        this.project = project;
        this.scmManager = scmManager;
        this.username = username;
        this.password = password;
        this.settings = settings;
        this.checkoutDir = checkoutDir;
    }

    public Location resolve( String locationSpecification, MessageHolder messageHolder )
    {
        try
        {
            try
            {
                FileUtils.deleteDirectory( checkoutDir );
            }
            catch ( IOException e )
            {
                messageHolder.addMessage( "Cannot remove " + checkoutDir + ". " +e.getMessage() );
                return null;
            }

            if ( !checkoutDir.mkdirs() )
            {
                messageHolder.addMessage( "Cannot create " + checkoutDir );
                return null;
            }

            ScmRepository repository = getScmRepository( locationSpecification, messageHolder );
            
            if ( repository == null )
            {
                return null;
            }
            
            ScmFileSet fileset = new ScmFileSet( checkoutDir );
            
            ScmProvider provider = null;
            try
            {
                provider = scmManager.getProviderByRepository( repository );
            }
            catch ( Exception e )
            {
                messageHolder.addMessage( "Cannot load SCM provider: ", e );
                return null;
            }
            
            CheckOutScmResult result = provider.checkOut( repository, fileset, null );
            
            if ( !result.isSuccess() )
            {
                messageHolder.addMessage( "Provider message:" );
                messageHolder.addMessage( result.getProviderMessage() == null ? "" : result.getProviderMessage() );
                messageHolder.addMessage( "Command output:" );
                messageHolder.addMessage( result.getCommandOutput() == null ? "" : result.getCommandOutput() );
                
                messageHolder.addMessage( "Command failed." + StringUtils.defaultString( result.getProviderMessage() ) );
                return null;
            }
        }
        catch ( ScmException e )
        {
            messageHolder.addMessage( "Cannot run checkout command : ", e );
            return null;
        }
        
        if( checkoutDir.exists() )
        {
            return new FileLocation( checkoutDir, locationSpecification);
        }
        
        return null;
    }
    
    private ScmRepository getScmRepository( String locationSpecification, MessageHolder messageHolder )
    {
        ScmRepository repository = null;
        
        String scmConnectionURL = null;
            
        if( StringUtils.isNotEmpty( locationSpecification ) )
        {
            scmConnectionURL = locationSpecification + '/' + 
                                      project.getGroupId().replace( '.', '/' ) + '/' + 
                                      project.getArtifactId();
            
            try
            {
                repository = scmManager.makeScmRepository( scmConnectionURL );
            }
            catch ( NoSuchScmProviderException e )
            {
                messageHolder.addMessage( "The specified workspace is not a valid SCM URL. " + e.getMessage() +
                                          ". \nWill try the scm URL derived from pom.");
            }
            catch ( ScmRepositoryException e )
            {
                messageHolder.addMessage( "The specified workspace is not a valid SCM URL. " + e.getMessage() +
                                          ". \nWill try the scm URL derived from pom.");
            }
        }
        
        try
        {
            Scm scm = project.getScm();
            
            if( repository == null )
            {
                if ( scm == null )
                {
                    messageHolder.addMessage( "Cannot load the SCM provider. No <scm> info declared in pom." );
                    return null;
                }
                
                repository = scmManager.makeScmRepository( scm.getConnection() );
                
                if ( !StringUtils.isEmpty( scm.getTag() ) && repository.getProvider().equals( "svn" ) )
                {
                    SvnScmProviderRepository svnRepo = (SvnScmProviderRepository) repository.getProviderRepository();
        
                    svnRepo.setTagBase( scm.getTag() );
                }
            }
    
            ScmProviderRepository providerRepo = repository.getProviderRepository();
    
            if ( !StringUtils.isEmpty( username ) )
            {
                providerRepo.setUser( username );
            }
    
            if ( !StringUtils.isEmpty( password ) )
            {
                providerRepo.setPassword( password );
            }
    
            if ( providerRepo instanceof ScmProviderRepositoryWithHost )
            {
                ScmProviderRepositoryWithHost repo = (ScmProviderRepositoryWithHost) repository.getProviderRepository();
    
                if ( StringUtils.isEmpty( username ) || StringUtils.isEmpty( password ) )
                {
                    String host = repo.getHost();
    
                    int port = repo.getPort();
    
                    if ( port > 0 )
                    {
                        host += ":" + port;
                    }
    
                    Server server = settings.getServer( host );
    
                    if ( server != null )
                    {
                        repo.setUser( server.getUsername() );
                        repo.setPassword( server.getPassword() );
                        repo.setPrivateKey( server.getPrivateKey() );
                        repo.setPassphrase( server.getPassphrase() );
                    }
                }
            }
        }
        catch ( ScmRepositoryException e )
        {
            if ( !e.getValidationMessages().isEmpty() )
            {
                for ( Iterator i = e.getValidationMessages().iterator(); i.hasNext(); )
                {
                    String message = (String) i.next();
                    messageHolder.addMessage( "Cannot load the SCM provider. " + message );
                }
            }
    
            messageHolder.addMessage( "Cannot load the SCM provider. " + e.getMessage() );
        }
        catch ( Exception e )
        {
            messageHolder.addMessage( "Cannot load the SCM provider. " + e.getMessage() );
        }
    
        return repository;
    }
}
