package org.codehaus.mojo.bod;

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

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.maven.shared.invoker.DefaultInvocationRequest;

public class BuildConfiguration
    extends DefaultInvocationRequest
{

    public BuildConfiguration copy()
    {
        BuildConfiguration config = new BuildConfiguration();
        config.setBaseDirectory( getBaseDirectory() );
        config.setDebug( isDebug() );
        config.setErrorHandler( getErrorHandler( null ) );
        config.setFailureBehavior( getFailureBehavior() );
        config.setGlobalChecksumPolicy( getGlobalChecksumPolicy() );

        List goals = getGoals();
        if ( goals != null && !goals.isEmpty() )
        {
            config.setGoals( new ArrayList( getGoals() ) );
        }

        config.setInputStream( getInputStream( null ) );
        config.setInteractive( isInteractive() );
        config.setLocalRepositoryDirectory( getLocalRepositoryDirectory( null ) );
        config.setOffline( isOffline() );
        config.setOutputHandler( getOutputHandler( null ) );
        config.setPomFile( getPomFile() );
        config.setPomFileName( getPomFileName() );
        config.setProperties( new Properties( getProperties() ) );
        config.setShellEnvironmentInherited( isShellEnvironmentInherited() );
        config.setShowErrors( isShowErrors() );
        config.setUpdateSnapshots( isUpdateSnapshots() );
        config.setUserSettingsFile( getUserSettingsFile() );

        return config;
    }
    
}
