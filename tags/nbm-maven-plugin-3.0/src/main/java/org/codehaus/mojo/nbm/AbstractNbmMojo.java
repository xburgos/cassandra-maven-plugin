/* ==========================================================================
 * Copyright 2003-2004 Mevenide Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 * =========================================================================
 */
package org.codehaus.mojo.nbm;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactCollector;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.artifact.resolver.filter.ArtifactFilter;
import org.apache.maven.artifact.resolver.filter.ScopeArtifactFilter;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.dependency.tree.DependencyNode;
import org.apache.maven.shared.dependency.tree.DependencyTreeBuilder;
import org.apache.maven.shared.dependency.tree.DependencyTreeBuilderException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Taskdef;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.codehaus.mojo.nbm.model.Dependency;
import org.codehaus.mojo.nbm.model.NetbeansModule;
import org.codehaus.mojo.nbm.model.io.xpp3.NetbeansModuleXpp3Reader;
import org.codehaus.plexus.util.IOUtil;

public abstract class AbstractNbmMojo
    extends AbstractMojo
{

    protected boolean hasJavaHelp = false;

    protected final Project registerNbmAntTasks()
    {
        Project antProject = new Project();
        antProject.init();

        Taskdef taskdef = (Taskdef) antProject.createTask( "taskdef" );
        taskdef.setClassname( "org.netbeans.nbbuild.MakeListOfNBM" );
        taskdef.setName( "genlist" );
        taskdef.execute();

        taskdef = (Taskdef) antProject.createTask( "taskdef" );
        taskdef.setClassname( "org.netbeans.nbbuild.MakeNBM" );
        taskdef.setName( "makenbm" );
        taskdef.execute();

        taskdef = (Taskdef) antProject.createTask( "taskdef" );
        taskdef.setClassname( "org.netbeans.nbbuild.MakeUpdateDesc" );
        taskdef.setName( "updatedist" );
        taskdef.execute();

        taskdef = (Taskdef) antProject.createTask( "taskdef" );
        taskdef.setClassname( "org.netbeans.nbbuild.CreateModuleXML" );
        taskdef.setName( "createmodulexml" );
        taskdef.execute();

        try
        {
            getClass().getClassLoader().loadClass(
                "javax.help.search.SearchEngine" );
            hasJavaHelp = true;
            taskdef = (Taskdef) antProject.createTask( "taskdef" );
            taskdef.setClassname( "org.netbeans.nbbuild.JHIndexer" );
            taskdef.setName( "jhindexer" );
            taskdef.execute();
        }
        catch ( ClassNotFoundException ex )
        {
            hasJavaHelp = false;
        }

        return antProject;
    }

    static final boolean matchesLibrary( Artifact artifact, List<String> libraries, ExamineManifest depExaminator,
        Log log )
    {
        String artId = artifact.getArtifactId();
        String grId = artifact.getGroupId();
        String id = grId + ":" + artId;
        boolean explicit = libraries.remove( id );
        if ( explicit )
        {
            log.debug(
                id + " included as module library, explicitly declared in module descriptor." );
            return explicit;
        }
        if ( Artifact.SCOPE_PROVIDED.equals( artifact.getScope() ) || Artifact.SCOPE_SYSTEM.equals(
            artifact.getScope() ) )
        {
            log.debug(
                id + " omitted as module library, has scope 'provided/system'" );
            return false;
        }
        if ( "nbm".equals( artifact.getType() ) )
        {
            return false;
        }
        if ( depExaminator.isNetbeansModule() )
        {
            return false;
        }
        log.debug(
            id + " included as module library, squeezed through all the filters." );
        return true;
    }

    static final Dependency resolveNetbeansDependency( Artifact artifact, List<Dependency> deps,
        ExamineManifest manifest, Log log )
    {
        String artId = artifact.getArtifactId();
        String grId = artifact.getGroupId();
        String id = grId + ":" + artId;
        for ( Dependency dep : deps )
        {
            if ( id.equals( dep.getId() ) )
            {
                if ( manifest.isNetbeansModule() )
                {
                    return dep;
                }
                else
                {
                    if ( dep.getExplicitValue() != null )
                    {
                        return dep;
                    }
                    log.warn(
                        id + " declared as module dependency in descriptor, but not a NetBeans module" );
                    return null;
                }
            }
        }
        if ( "nbm".equals( artifact.getType() ) )
        {
            Dependency dep = new Dependency();
            dep.setId( id );
            dep.setType( "spec" );
            log.debug( "Adding nbm module dependency - " + id );
            return dep;
        }
        if ( manifest.isNetbeansModule() )
        {
            Dependency dep = new Dependency();
            dep.setId( id );
            dep.setType( "spec" );
            log.debug( "Adding direct NetBeans module dependency - " + id );
            return dep;
        }
        return null;
    }

    protected final NetbeansModule readModuleDescriptor( File descriptor )
        throws MojoExecutionException
    {
        if ( descriptor == null )
        {
            throw new MojoExecutionException(
                "The module descriptor has to be configured." );
        }
        if ( !descriptor.exists() )
        {
            throw new MojoExecutionException(
                "The module descriptor is missing: '" + descriptor + "'." );
        }
        Reader r = null;
        try
        {
            r = new FileReader( descriptor );
            NetbeansModuleXpp3Reader reader = new NetbeansModuleXpp3Reader();
            NetbeansModule module = reader.read( r );
            return module;
        }
        catch ( IOException exc )
        {
            throw new MojoExecutionException(
                "Error while reading module descriptor '" + descriptor + "'.",
                exc );
        }
        catch ( XmlPullParserException xml )
        {
            throw new MojoExecutionException(
                "Error while reading module descriptor '" + descriptor + "'.",
                xml );
        }
        finally
        {
            IOUtil.close( r );
        }
    }

    protected final NetbeansModule createDefaultDescriptor( MavenProject project, boolean log )
    {

        if ( log )
        {
            getLog().info(
                "No Module Descriptor defined, trying to fallback to generated values:" );
        }
        NetbeansModule module = new NetbeansModule();
        module.setAuthor( "Nobody" );
        module.setCluster( "maven" );
        if ( log )
        {
            getLog().info( "   Cluster:" + module.getCluster() );
        }
        String codename = project.getGroupId() + "." + project.getArtifactId();
        codename = codename.replaceAll( "-", "." );
        module.setCodeNameBase( codename );
        if ( log )
        {
            getLog().info( "   Codenamebase:" + module.getCodeNameBase() );
        }
        module.setModuleType( "normal" );
        if ( log )
        {
            getLog().info( "   Type:" + module.getModuleType() );
        }
        module.setRequiresRestart( false );
        return module;
    }

    static List<Artifact> getLibraryArtifacts( DependencyNode treeRoot, NetbeansModule module,
        List<Artifact> runtimeArtifacts, Map<Artifact, ExamineManifest> examinerCache, Log log ) throws MojoExecutionException
    {
        List<Artifact> include = new ArrayList<Artifact>();
        if ( module != null )
        {
            List<String> librList = new ArrayList<String>();
            if ( module.getLibraries() != null )
            {
                librList.addAll( module.getLibraries() );
            }
            CollectLibrariesNodeVisitor visitor = new CollectLibrariesNodeVisitor( librList,
                runtimeArtifacts, examinerCache, log, treeRoot );
            treeRoot.accept( visitor );
            include.addAll( visitor.getArtifacts() );
        }
        return include;
    }

    static List<ModuleWrapper> getModuleDependencyArtifacts( DependencyNode treeRoot, NetbeansModule module,
        MavenProject project, Map<Artifact, ExamineManifest> examinerCache,
        List<Artifact> libraryArtifacts, Log log ) throws MojoExecutionException
    {
        List<ModuleWrapper> include = new ArrayList<ModuleWrapper>();
        if ( module != null )
        {
            List deps = module.getDependencies();
            List artifacts = project.getCompileArtifacts();
            for ( Iterator iter = artifacts.iterator(); iter.hasNext(); )
            {
                Artifact artifact = (Artifact) iter.next();
                if ( libraryArtifacts.contains( artifact ) )
                {
                    continue;
                }
                ExamineManifest depExaminator = examinerCache.get( artifact );
                if ( depExaminator == null )
                {
                    depExaminator = new ExamineManifest( log );
                    depExaminator.setJarFile( artifact.getFile() );
                    depExaminator.checkFile();
                    examinerCache.put( artifact, depExaminator );
                }
                Dependency dep = resolveNetbeansDependency( artifact, deps,
                    depExaminator, log );
                if ( dep != null )
                {
                    ModuleWrapper wr = new ModuleWrapper();
                    wr.dependency = dep;
                    wr.artifact = artifact;
                    wr.transitive = false;
                    //only direct deps matter to us..
                    if ( depExaminator.isNetbeansModule() && artifact.getDependencyTrail().size() > 2 )
                    {
                        log.debug(
                            artifact.getId() + " omitted as NetBeans module dependency, not a direct one. Declare it in the pom for inclusion." );
                        wr.transitive = true;

                    }
                    include.add( wr );
                }
            }
        }
        return include;
    }

    static class ModuleWrapper
    {

        Dependency dependency;

        Artifact artifact;

        boolean transitive = true;

    }

    //copied from dependency:tree mojo
    protected DependencyNode createDependencyTree( MavenProject project,
        DependencyTreeBuilder dependencyTreeBuilder, ArtifactRepository localRepository,
        ArtifactFactory artifactFactory, ArtifactMetadataSource artifactMetadataSource,
        ArtifactCollector artifactCollector,
        String scope ) throws MojoExecutionException
    {
        ArtifactFilter artifactFilter = createResolvingArtifactFilter( scope );

        try
        {
            // TODO: note that filter does not get applied due to MNG-3236
            return dependencyTreeBuilder.buildDependencyTree( project,
                localRepository, artifactFactory,
                artifactMetadataSource, artifactFilter, artifactCollector );
        }
        catch ( DependencyTreeBuilderException exception )
        {
            throw new MojoExecutionException( "Cannot build project dependency tree", exception );
        }

    }

    //copied from dependency:tree mojo
    /**
     * Gets the artifact filter to use when resolving the dependency tree.
     *
     * @return the artifact filter
     */
    private ArtifactFilter createResolvingArtifactFilter( String scope )
    {
        ArtifactFilter filter;

        // filter scope
        if ( scope != null )
        {
            getLog().debug( "+ Resolving dependency tree for scope '" + scope + "'" );

            filter = new ScopeArtifactFilter( scope );
        }
        else
        {
            filter = null;
        }

        return filter;
    }

    protected final Artifact turnJarToNbmFile( Artifact art, ArtifactFactory artifactFactory,
        ArtifactResolver artifactResolver, MavenProject project, ArtifactRepository localRepository ) throws MojoExecutionException
    {
        if ( "jar".equals( art.getType() ) || "nbm".equals( art.getType() ) )
        {
            //TODO, it would be nice to have a check to see if the
            // "to-be-created" module nbm artifact is actually already in the
            // list of dependencies (as "nbm-file") or not..
            // that would be a timesaver
            ExamineManifest mnf = new ExamineManifest( getLog() );
            mnf.setJarFile( art.getFile() );
            mnf.checkFile();
            if ( mnf.isNetbeansModule() )
            {
                Artifact nbmArt = artifactFactory.createDependencyArtifact(
                    art.getGroupId(),
                    art.getArtifactId(),
                    art.getVersionRange(),
                    "nbm-file",
                    art.getClassifier(),
                    art.getScope() );
                try
                {
                    artifactResolver.resolve( nbmArt, project.getRemoteArtifactRepositories(), localRepository );
                }
                catch ( ArtifactResolutionException ex )
                {
                    throw new MojoExecutionException( "Failed to retrieve the nbm file from repository", ex );
                }
                catch ( ArtifactNotFoundException ex )
                {
                    throw new MojoExecutionException( "Failed to retrieve the nbm file from repository", ex );
                }
                return nbmArt;
            }
        }
        return null;
    }
}