package org.codehaus.mojo.versions.api;

/*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements.  See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership.  The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License.  You may obtain a copy of the License at
*
*  http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

import org.apache.maven.artifact.versioning.VersionRange;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.Profile;
import org.apache.maven.model.ReportPlugin;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.project.MavenProject;
import org.apache.maven.plugin.logging.Log;
import org.codehaus.mojo.versions.rewriting.ModifiedPomXMLEventReader;
import org.codehaus.mojo.versions.utils.RegexUtils;
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluationException;
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluator;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.File;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Stack;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Collection;
import java.util.regex.Pattern;

/**
 * Helper class for modifying pom files.
 *
 * @author <a href="mailto:stephen.alan.connolly@gmail.com">Stephen Connolly</a>
 * @since 1.0-alpha-3
 */
public class PomHelper
{
    /**
     * The encoding used for the pom file.
     *
     * @since 1.0-alpha-3
     */
    public static final String POM_ENCODING = "UTF-8";

    public static final String APACHE_MAVEN_PLUGINS_GROUPID = "org.apache.maven.plugins";

    /**
     * Gets the raw model before any interpolation what-so-ever.
     *
     * @param project The project to get the raw model for.
     * @return The raw model.
     * @throws IOException if the file is not found or if the file does not parse.
     */
    public static Model getRawModel( MavenProject project )
        throws IOException
    {
        FileReader fileReader = null;
        BufferedReader bufferedReader = null;
        try
        {
            fileReader = new FileReader( project.getFile() );
            bufferedReader = new BufferedReader( fileReader );
            MavenXpp3Reader reader = new MavenXpp3Reader();
            return reader.read( bufferedReader );
        }
        catch ( XmlPullParserException e )
        {
            IOException ioe = new IOException( e.getMessage() );
            ioe.initCause( e );
            throw ioe;
        }
        finally
        {
            if ( bufferedReader != null )
            {
                bufferedReader.close();
            }
            if ( fileReader != null )
            {
                fileReader.close();
            }
        }
    }

    /**
     * Searches the pom re-defining the specified property to the specified version.
     *
     * @param pom       The pom to modify.
     * @param profileId The profile in which to modify the property.
     * @param property  The property to modify.
     * @param value     The new value of the property.
     * @return <code>true</code> if a replacement was made.
     * @throws XMLStreamException if somethinh went wrong.
     */
    public static boolean setPropertyVersion( final ModifiedPomXMLEventReader pom, final String profileId,
                                              final String property, final String value )
        throws XMLStreamException
    {
        Stack stack = new Stack();
        String path = "";
        final Pattern propertyRegex;
        final Pattern matchScopeRegex;
        final Pattern projectProfileId;
        boolean inMatchScope = false;
        boolean madeReplacement = false;
        if ( profileId == null )
        {
            propertyRegex = Pattern.compile( "/project/properties/" + RegexUtils.quote( property ) );
            matchScopeRegex = Pattern.compile( "/project/properties" );
            projectProfileId = null;
        }
        else
        {
            propertyRegex = Pattern.compile( "/project/profiles/profile/properties/" + RegexUtils.quote( property ) );
            matchScopeRegex = Pattern.compile( "/project/profiles/profile" );
            projectProfileId = Pattern.compile( "/project/profiles/profile/id" );
        }

        pom.rewind();

        while ( pom.hasNext() )
        {
            XMLEvent event = pom.nextEvent();
            if ( event.isStartElement() )
            {
                stack.push( path );
                path = new StringBuffer().append( path ).append( "/" ).append(
                    event.asStartElement().getName().getLocalPart() ).toString();

                if ( propertyRegex.matcher( path ).matches() )
                {
                    pom.mark( 0 );
                }
                else if ( matchScopeRegex.matcher( path ).matches() )
                {
                    // we're in a new match scope
                    // reset any previous partial matches
                    inMatchScope = profileId == null;
                    pom.clearMark( 0 );
                    pom.clearMark( 1 );
                }
                else if ( profileId != null && projectProfileId.matcher( path ).matches() )
                {
                    String candidateId = pom.getElementText();
                    path = (String) stack.pop(); // since getElementText will be after the end element

                    inMatchScope = profileId.trim().equals( candidateId.trim() );
                }
            }
            if ( event.isEndElement() )
            {
                if ( propertyRegex.matcher( path ).matches() )
                {
                    pom.mark( 1 );
                }
                else if ( matchScopeRegex.matcher( path ).matches() )
                {
                    if ( inMatchScope && pom.hasMark( 0 ) && pom.hasMark( 1 ) )
                    {
                        pom.replaceBetween( 0, 1, value );
                        madeReplacement = true;
                    }
                    pom.clearMark( 0 );
                    pom.clearMark( 1 );
                    inMatchScope = false;
                }
                path = (String) stack.pop();
            }
        }
        return madeReplacement;
    }

    /**
     * Searches the pom re-defining the project version to the specified version.
     *
     * @param pom   The pom to modify.
     * @param value The new value of the property.
     * @return <code>true</code> if a replacement was made.
     * @throws XMLStreamException if somethinh went wrong.
     */
    public static boolean setProjectVersion( final ModifiedPomXMLEventReader pom, final String value )
        throws XMLStreamException
    {
        Stack stack = new Stack();
        String path = "";
        final Pattern matchScopeRegex;
        boolean madeReplacement = false;
        matchScopeRegex = Pattern.compile( "/project/version" );

        pom.rewind();

        while ( pom.hasNext() )
        {
            XMLEvent event = pom.nextEvent();
            if ( event.isStartElement() )
            {
                stack.push( path );
                path = new StringBuffer().append( path ).append( "/" ).append(
                    event.asStartElement().getName().getLocalPart() ).toString();

                if ( matchScopeRegex.matcher( path ).matches() )
                {
                    pom.mark( 0 );
                }
            }
            if ( event.isEndElement() )
            {
                if ( matchScopeRegex.matcher( path ).matches() )
                {
                    pom.mark( 1 );
                    if ( pom.hasMark( 0 ) && pom.hasMark( 1 ) )
                    {
                        pom.replaceBetween( 0, 1, value );
                        madeReplacement = true;
                    }
                    pom.clearMark( 0 );
                    pom.clearMark( 1 );
                }
                path = (String) stack.pop();
            }
        }
        return madeReplacement;
    }

    /**
     * Retrieves the project version from the pom.
     *
     * @param pom The pom.
     * @return the project version or <code>null</code> if the project version is not defined (i.e. inherited from parent version).
     * @throws XMLStreamException if something went wrong.
     */
    public static String getProjectVersion( final ModifiedPomXMLEventReader pom )
        throws XMLStreamException
    {
        Stack stack = new Stack();
        String path = "";
        final Pattern matchScopeRegex = Pattern.compile( "/project/version" );

        pom.rewind();

        while ( pom.hasNext() )
        {
            XMLEvent event = pom.nextEvent();
            if ( event.isStartElement() )
            {
                stack.push( path );
                path = new StringBuffer().append( path ).append( "/" ).append(
                    event.asStartElement().getName().getLocalPart() ).toString();

                if ( matchScopeRegex.matcher( path ).matches() )
                {
                    pom.mark( 0 );
                }
            }
            if ( event.isEndElement() )
            {
                if ( matchScopeRegex.matcher( path ).matches() )
                {
                    pom.mark( 1 );
                    if ( pom.hasMark( 0 ) && pom.hasMark( 1 ) )
                    {
                        return pom.getBetween( 0, 1 ).trim();
                    }
                    pom.clearMark( 0 );
                    pom.clearMark( 1 );
                }
                path = (String) stack.pop();
            }
        }
        return null;
    }

    /**
     * Searches the pom re-defining the project version to the specified version.
     *
     * @param pom   The pom to modify.
     * @param value The new value of the property.
     * @return <code>true</code> if a replacement was made.
     * @throws XMLStreamException if somethinh went wrong.
     */
    public static boolean setProjectParentVersion( final ModifiedPomXMLEventReader pom, final String value )
        throws XMLStreamException
    {
        Stack stack = new Stack();
        String path = "";
        final Pattern matchScopeRegex;
        boolean madeReplacement = false;
        matchScopeRegex = Pattern.compile( "/project/parent/version" );

        pom.rewind();

        while ( pom.hasNext() )
        {
            XMLEvent event = pom.nextEvent();
            if ( event.isStartElement() )
            {
                stack.push( path );
                path = new StringBuffer().append( path ).append( "/" ).append(
                    event.asStartElement().getName().getLocalPart() ).toString();

                if ( matchScopeRegex.matcher( path ).matches() )
                {
                    pom.mark( 0 );
                }
            }
            if ( event.isEndElement() )
            {
                if ( matchScopeRegex.matcher( path ).matches() )
                {
                    pom.mark( 1 );
                    if ( pom.hasMark( 0 ) && pom.hasMark( 1 ) )
                    {
                        pom.replaceBetween( 0, 1, value );
                        madeReplacement = true;
                    }
                    pom.clearMark( 0 );
                    pom.clearMark( 1 );
                }
                path = (String) stack.pop();
            }
        }
        return madeReplacement;
    }

    /**
     * Gets the parent artifact from the pom.
     *
     * @param pom    The pom.
     * @param helper The helper (used to create the artifact).
     * @return The parent artifact or <code>null</code> if no parent is specified.
     * @throws XMLStreamException if something went wrong.
     */
    public static Artifact getProjectParent( final ModifiedPomXMLEventReader pom, VersionsHelper helper )
        throws XMLStreamException
    {
        Stack stack = new Stack();
        String path = "";
        final Pattern matchScopeRegex = Pattern.compile( "/project/parent((/groupId)|(/artifactId)|(/version))" );
        String groupId = null;
        String artifactId = null;
        String version = null;

        pom.rewind();

        while ( pom.hasNext() )
        {
            XMLEvent event = pom.nextEvent();
            if ( event.isStartElement() )
            {
                stack.push( path );
                final String elementName = event.asStartElement().getName().getLocalPart();
                path = new StringBuffer().append( path ).append( "/" ).append( elementName ).toString();

                if ( matchScopeRegex.matcher( path ).matches() )
                {
                    if ( "groupId".equals( elementName ) )
                    {
                        groupId = pom.getElementText().trim();
                        path = (String) stack.pop();
                    }
                    else if ( "artifactId".equals( elementName ) )
                    {
                        artifactId = pom.getElementText().trim();
                        path = (String) stack.pop();
                    }
                    else if ( "version".equals( elementName ) )
                    {
                        version = pom.getElementText().trim();
                        path = (String) stack.pop();
                    }
                }
            }
            if ( event.isEndElement() )
            {
                path = (String) stack.pop();
            }
        }
        if ( groupId == null || artifactId == null || version == null )
        {
            return null;
        }
        return helper.createDependencyArtifact( groupId, artifactId, VersionRange.createFromVersion( version ), "pom",
                                                null, null, false );
    }

    /**
     * Searches the pom re-defining the specified dependency to the specified version.
     *
     * @param pom        The pom to modify.
     * @param groupId    The groupId of the dependency.
     * @param artifactId The artifactId of the dependency.
     * @param oldVersion The old version of the dependency.
     * @param newVersion The new version of the dependency.
     * @return <code>true</code> if a replacement was made.
     * @throws XMLStreamException if somethinh went wrong.
     */
    public static boolean setDependencyVersion( final ModifiedPomXMLEventReader pom, final String groupId,
                                                final String artifactId, final String oldVersion,
                                                final String newVersion )
        throws XMLStreamException
    {
        Stack stack = new Stack();
        String path = "";
        final Pattern matchScopeRegex;
        final Pattern matchTargetRegex;
        boolean inMatchScope = false;
        boolean madeReplacement = false;
        boolean haveGroupId = false;
        boolean haveArtifactId = false;
        boolean haveOldVersion = false;

        matchScopeRegex = Pattern.compile( "/project" + "(/profiles/profile)?"
            + "((/dependencyManagement)|(/build(/pluginManagement)?/plugins/plugin))?" + "/dependencies/dependency" );

        matchTargetRegex = Pattern.compile( "/project" + "(/profiles/profile)?"
            + "((/dependencyManagement)|(/build(/pluginManagement)?/plugins/plugin))?" + "/dependencies/dependency"
            + "((/groupId)|(/artifactId)|(/version))" );

        pom.rewind();

        while ( pom.hasNext() )
        {
            XMLEvent event = pom.nextEvent();
            if ( event.isStartElement() )
            {
                stack.push( path );
                final String elementName = event.asStartElement().getName().getLocalPart();
                path = new StringBuffer().append( path ).append( "/" ).append( elementName ).toString();

                if ( matchScopeRegex.matcher( path ).matches() )
                {
                    // we're in a new match scope
                    // reset any previous partial matches
                    inMatchScope = true;
                    pom.clearMark( 0 );
                    pom.clearMark( 1 );

                    haveGroupId = false;
                    haveArtifactId = false;
                    haveOldVersion = false;
                }
                else if ( inMatchScope && matchTargetRegex.matcher( path ).matches() )
                {
                    if ( "groupId".equals( elementName ) )
                    {
                        haveGroupId = groupId.equals( pom.getElementText().trim() );
                        path = (String) stack.pop();
                    }
                    else if ( "artifactId".equals( elementName ) )
                    {
                        haveArtifactId = artifactId.equals( pom.getElementText().trim() );
                        path = (String) stack.pop();
                    }
                    else if ( "version".equals( elementName ) )
                    {
                        pom.mark( 0 );
                    }
                }
            }
            if ( event.isEndElement() )
            {
                if ( matchTargetRegex.matcher( path ).matches() && "version".equals(
                    event.asEndElement().getName().getLocalPart() ) )
                {
                    pom.mark( 1 );
                    String compressedPomVersion = StringUtils.deleteWhitespace( pom.getBetween( 0, 1 ).trim() );
                    String compressedOldVersion = StringUtils.deleteWhitespace( oldVersion );
                    haveOldVersion = compressedOldVersion.equals( compressedPomVersion );
                }
                else if ( matchScopeRegex.matcher( path ).matches() )
                {
                    if ( inMatchScope && pom.hasMark( 0 ) && pom.hasMark( 1 ) && haveGroupId && haveArtifactId
                        && haveOldVersion )
                    {
                        pom.replaceBetween( 0, 1, newVersion );
                        madeReplacement = true;
                    }
                    pom.clearMark( 0 );
                    pom.clearMark( 1 );
                    haveArtifactId = false;
                    haveGroupId = false;
                    haveOldVersion = false;
                    inMatchScope = false;
                }
                path = (String) stack.pop();
            }
        }
        return madeReplacement;
    }

    /**
     * Searches the pom re-defining the specified plugin to the specified version.
     *
     * @param pom        The pom to modify.
     * @param groupId    The groupId of the dependency.
     * @param artifactId The artifactId of the dependency.
     * @param oldVersion The old version of the dependency.
     * @param newVersion The new version of the dependency.
     * @return <code>true</code> if a replacement was made.
     * @throws XMLStreamException if somethinh went wrong.
     */
    public static boolean setPluginVersion( final ModifiedPomXMLEventReader pom, final String groupId,
                                            final String artifactId, final String oldVersion, final String newVersion )
        throws XMLStreamException
    {
        Stack stack = new Stack();
        String path = "";
        final Pattern matchScopeRegex;
        final Pattern matchTargetRegex;
        boolean inMatchScope = false;
        boolean madeReplacement = false;
        boolean haveGroupId = false;
        boolean needGroupId = groupId != null && !APACHE_MAVEN_PLUGINS_GROUPID.equals( groupId );
        boolean haveArtifactId = false;
        boolean haveOldVersion = false;

        matchScopeRegex = Pattern.compile(
            "/project" + "(/profiles/profile)?" + "((/build(/pluginManagement)?)|(/reporting))/plugins/plugin" );

        matchTargetRegex = Pattern.compile(
            "/project" + "(/profiles/profile)?" + "((/build(/pluginManagement)?)|(/reporting))/plugins/plugin"
                + "((/groupId)|(/artifactId)|(/version))" );

        pom.rewind();

        while ( pom.hasNext() )
        {
            XMLEvent event = pom.nextEvent();
            if ( event.isStartElement() )
            {
                stack.push( path );
                final String elementName = event.asStartElement().getName().getLocalPart();
                path = new StringBuffer().append( path ).append( "/" ).append( elementName ).toString();

                if ( matchScopeRegex.matcher( path ).matches() )
                {
                    // we're in a new match scope
                    // reset any previous partial matches
                    inMatchScope = true;
                    pom.clearMark( 0 );
                    pom.clearMark( 1 );

                    haveGroupId = false;
                    haveArtifactId = false;
                    haveOldVersion = false;
                }
                else if ( inMatchScope && matchTargetRegex.matcher( path ).matches() )
                {
                    if ( "groupId".equals( elementName ) )
                    {
                        haveGroupId = groupId.equals( pom.getElementText().trim() );
                        path = (String) stack.pop();
                    }
                    else if ( "artifactId".equals( elementName ) )
                    {
                        haveArtifactId = artifactId.equals( pom.getElementText().trim() );
                        path = (String) stack.pop();
                    }
                    else if ( "version".equals( elementName ) )
                    {
                        pom.mark( 0 );
                    }
                }
            }
            if ( event.isEndElement() )
            {
                if ( matchTargetRegex.matcher( path ).matches() && "version".equals(
                    event.asEndElement().getName().getLocalPart() ) )
                {
                    pom.mark( 1 );
                    haveOldVersion = oldVersion.equals( pom.getBetween( 0, 1 ).trim() );
                }
                else if ( matchScopeRegex.matcher( path ).matches() )
                {
                    if ( inMatchScope && pom.hasMark( 0 ) && pom.hasMark( 1 ) && ( haveGroupId || !needGroupId )
                        && haveArtifactId && haveOldVersion )
                    {
                        pom.replaceBetween( 0, 1, newVersion );
                        madeReplacement = true;
                        pom.clearMark( 0 );
                        pom.clearMark( 1 );
                        haveArtifactId = false;
                        haveGroupId = false;
                        haveOldVersion = false;
                    }
                    inMatchScope = false;
                }
                path = (String) stack.pop();
            }
        }
        return madeReplacement;
    }

    /**
     * Examines the project to find any properties which are associated with versions of artifacts in the project.
     *
     * @param helper              Our versions helper.
     * @param project             The project to examine.
     * @param expressionEvaluator The expression evaluator to use when examining properties.
     * @return An array of properties that are associated within the project.
     * @throws ExpressionEvaluationException if an expression cannot be evaluated.
     * @throws IOException                   if the project's pom file cannot be parsed.
     * @since 1.0-alpha-3
     */
    public static PropertyVersions[] getPropertyVersions( VersionsHelper helper, MavenProject project,
                                                          ExpressionEvaluator expressionEvaluator )
        throws ExpressionEvaluationException, IOException
    {
        Model model = getRawModel( project );
        Map/*<String,PropertyVersions>*/ result = new TreeMap();

        Set/*<String*/ activeProfiles = new TreeSet();
        for ( Iterator i = project.getActiveProfiles().iterator(); i.hasNext(); )
        {
            Profile profile = (Profile) i.next();
            activeProfiles.add( profile.getId() );
        }

        // add any properties from profiles first (as they override properties from the project
        for ( Iterator i = model.getProfiles().iterator(); i.hasNext(); )
        {
            Profile profile = (Profile) i.next();
            if ( !activeProfiles.contains( profile.getId() ) )
            {
                continue;
            }
            addProperties( helper, result, profile.getId(), profile.getProperties() );
            if ( profile.getDependencyManagement() != null )
            {
                addDependencyAssocations( helper, expressionEvaluator, result,
                                          profile.getDependencyManagement().getDependencies(), false );
            }
            addDependencyAssocations( helper, expressionEvaluator, result, profile.getDependencies(), false );
            if ( profile.getBuild() != null )
            {
                if ( profile.getBuild().getPluginManagement() != null )
                {
                    addPluginAssociations( helper, expressionEvaluator, result,
                                           profile.getBuild().getPluginManagement().getPlugins() );
                }
                addPluginAssociations( helper, expressionEvaluator, result, profile.getBuild().getPlugins() );
            }
            if ( profile.getReporting() != null )
            {
                addReportPluginAssociations( helper, expressionEvaluator, result, profile.getReporting().getPlugins() );
            }
        }

        // second, we add all the properties in the pom
        addProperties( helper, result, null, model.getProperties() );
        if ( model.getDependencyManagement() != null )
        {
            addDependencyAssocations( helper, expressionEvaluator, result,
                                      model.getDependencyManagement().getDependencies(), false );
        }
        addDependencyAssocations( helper, expressionEvaluator, result, model.getDependencies(), false );
        if ( model.getBuild() != null )
        {
            if ( model.getBuild().getPluginManagement() != null )
            {
                addPluginAssociations( helper, expressionEvaluator, result,
                                       model.getBuild().getPluginManagement().getPlugins() );
            }
            addPluginAssociations( helper, expressionEvaluator, result, model.getBuild().getPlugins() );
        }
        if ( model.getReporting() != null )
        {
            addReportPluginAssociations( helper, expressionEvaluator, result, model.getReporting().getPlugins() );
        }

        // finally, remove any properties without associations
        purgeProperties( result );

        return (PropertyVersions[]) result.values().toArray( new PropertyVersions[result.values().size()] );
    }

    /**
     * Takes a list of {@link org.apache.maven.model.Plugin} instances and adds associations to properties used to
     * define versions of the plugin artifact or any of the plugin dependencies specified in the pom.
     *
     * @param helper              Our helper.
     * @param expressionEvaluator Our expression evaluator.
     * @param result              The map of {@link org.codehaus.mojo.versions.api.PropertyVersions} keyed by property name.
     * @param plugins             The list of {@link org.apache.maven.model.Plugin}.
     * @throws ExpressionEvaluationException if an expression cannot be evaluated.
     */
    private static void addPluginAssociations( VersionsHelper helper, ExpressionEvaluator expressionEvaluator,
                                               Map result, List plugins )
        throws ExpressionEvaluationException
    {
        if ( plugins == null )
        {
            return;
        }
        for ( Iterator i = plugins.iterator(); i.hasNext(); )
        {
            Plugin plugin = (Plugin) i.next();
            String version = plugin.getVersion();
            if ( version != null && version.indexOf( "${" ) != -1 && version.indexOf( '}' ) != -1 )
            {
                for ( Iterator j = result.values().iterator(); j.hasNext(); )
                {
                    // any of these could be defined by a property
                    PropertyVersions property = (PropertyVersions) j.next();
                    if ( version.indexOf( "${" + property.getName() + "}" ) != -1 )
                    {
                        String groupId = plugin.getGroupId();
                        if ( groupId == null || groupId.trim().length() == 0 )
                        {
                            // group Id has a special default
                            groupId = APACHE_MAVEN_PLUGINS_GROUPID;
                        }
                        else
                        {
                            groupId = (String) expressionEvaluator.evaluate( groupId );
                        }
                        String artifactId = plugin.getArtifactId();
                        if ( artifactId == null || artifactId.trim().length() == 0 )
                        {
                            // malformed pom
                            continue;
                        }
                        else
                        {
                            artifactId = (String) expressionEvaluator.evaluate( artifactId );
                        }
                        // might as well capture the current value
                        VersionRange versionRange = VersionRange.createFromVersion(
                            (String) expressionEvaluator.evaluate( plugin.getVersion() ) );
                        property.addAssociation( helper.createPluginArtifact( groupId, artifactId, versionRange ),
                                                 true );
                    }
                }
            }
            addDependencyAssocations( helper, expressionEvaluator, result, plugin.getDependencies(), true );
        }
    }

    private static void addReportPluginAssociations( VersionsHelper helper, ExpressionEvaluator expressionEvaluator,
                                                     Map result, List reportPlugins )
        throws ExpressionEvaluationException
    {
        if ( reportPlugins == null )
        {
            return;
        }
        for ( Iterator i = reportPlugins.iterator(); i.hasNext(); )
        {
            ReportPlugin plugin = (ReportPlugin) i.next();
            String version = plugin.getVersion();
            if ( version != null && version.indexOf( "${" ) != -1 && version.indexOf( '}' ) != -1 )
            {
                for ( Iterator j = result.values().iterator(); j.hasNext(); )
                {
                    PropertyVersions property = (PropertyVersions) j.next();
                    if ( version.indexOf( "${" + property.getName() + "}" ) != -1 )
                    {
                        // any of these could be defined by a property
                        String groupId = plugin.getGroupId();
                        if ( groupId == null || groupId.trim().length() == 0 )
                        {
                            // group Id has a special default
                            groupId = APACHE_MAVEN_PLUGINS_GROUPID;
                        }
                        else
                        {
                            groupId = (String) expressionEvaluator.evaluate( groupId );
                        }
                        String artifactId = plugin.getArtifactId();
                        if ( artifactId == null || artifactId.trim().length() == 0 )
                        {
                            // malformed pom
                            continue;
                        }
                        else
                        {
                            artifactId = (String) expressionEvaluator.evaluate( artifactId );
                        }
                        // might as well capture the current value
                        VersionRange versionRange = VersionRange.createFromVersion(
                            (String) expressionEvaluator.evaluate( plugin.getVersion() ) );
                        property.addAssociation( helper.createPluginArtifact( groupId, artifactId, versionRange ),
                                                 true );
                    }
                }
            }
        }
    }

    private static void addDependencyAssocations( VersionsHelper helper, ExpressionEvaluator expressionEvaluator,
                                                  Map result, List dependencies, boolean usePluginRepositories )
        throws ExpressionEvaluationException
    {
        if ( dependencies == null )
        {
            return;
        }
        for ( Iterator i = dependencies.iterator(); i.hasNext(); )
        {
            Dependency dependency = (Dependency) i.next();
            String version = dependency.getVersion();
            if ( version != null && version.indexOf( "${" ) != -1 && version.indexOf( '}' ) != -1 )
            {
                for ( Iterator j = result.values().iterator(); j.hasNext(); )
                {
                    PropertyVersions property = (PropertyVersions) j.next();
                    if ( version.indexOf( "${" + property.getName() + "}" ) != -1 )
                    {
                        // Any of these could be defined by a property
                        String groupId = dependency.getGroupId();
                        if ( groupId == null || groupId.trim().length() == 0 )
                        {
                            // malformed pom
                            continue;
                        }
                        else
                        {
                            groupId = (String) expressionEvaluator.evaluate( groupId );
                        }
                        String artifactId = dependency.getArtifactId();
                        if ( artifactId == null || artifactId.trim().length() == 0 )
                        {
                            // malformed pom
                            continue;
                        }
                        else
                        {
                            artifactId = (String) expressionEvaluator.evaluate( artifactId );
                        }
                        // might as well capture the current value
                        VersionRange versionRange = VersionRange.createFromVersion(
                            (String) expressionEvaluator.evaluate( dependency.getVersion() ) );
                        property.addAssociation(
                            helper.createDependencyArtifact( groupId, artifactId, versionRange, dependency.getType(),
                                                             dependency.getClassifier(), dependency.getScope(),
                                                             dependency.isOptional() ), usePluginRepositories );
                    }
                }
            }
        }
    }

    private static void addProperties( VersionsHelper helper, Map result, String profileId, Properties properties )
    {
        if ( properties == null )
        {
            return;
        }
        for ( Enumeration j = properties.propertyNames(); j.hasMoreElements(); )
        {
            String propertyName = (String) j.nextElement();
            if ( !result.containsKey( propertyName ) )
            {
                result.put( propertyName, new PropertyVersions( profileId, propertyName, helper ) );
            }
        }
    }

    private static void purgeProperties( Map result )
    {
        for ( Iterator i = result.values().iterator(); i.hasNext(); )
        {
            PropertyVersions versions = (PropertyVersions) i.next();
            if ( versions.getAssociations().length == 0 )
            {
                i.remove();
            }
        }
    }

    /**
     * Returns a set of all child modules for a project, including any defined in profiles (ignoring profile
     * activation).
     *
     * @param project The project.
     * @param logger
     * @return the set of all child modules of the project.
     */
    public static Set getAllChildModules( MavenProject project, Log logger )
    {
        logger.debug( "Finding child modules..." );
        Set childModules = new TreeSet();
        childModules.addAll( project.getOriginalModel().getModules() );
        Iterator i = project.getOriginalModel().getProfiles().iterator();
        while ( i.hasNext() )
        {
            Profile profile = (Profile) i.next();
            childModules.addAll( profile.getModules() );
        }
        debugModules( logger, "Child modules:", childModules );
        return childModules;
    }

    /**
     * Outputs a debug message with a list of modules.
     *
     * @param logger  The logger to log to.
     * @param message The message to display.
     * @param modules The modules to append to the message.
     */
    public static void debugModules( Log logger, String message, Collection modules )
    {
        Iterator i;
        if ( logger.isDebugEnabled() )
        {
            logger.debug( message );
            if ( modules.isEmpty() )
            {
                logger.debug( "None." );
            }
            else
            {
                i = modules.iterator();
                while ( i.hasNext() )
                {
                    logger.debug( "  " + i.next() );
                }
            }

        }
    }

    /**
     * Modifies the collection of child modules removing those which cannot be found relative to the parent.
     *
     * @param logger       The logger to log to.
     * @param project      the project.
     * @param childModules the child modules.
     */
    public static void removeMissingChildModules( Log logger, MavenProject project, Collection childModules )
    {
        logger.debug( "Removing child modules which are missing..." );
        Iterator i = childModules.iterator();
        while ( i.hasNext() )
        {
            String modulePath = (String) i.next();
            File moduleFile = new File( project.getBasedir(), modulePath );

            if ( moduleFile.isDirectory() && new File( moduleFile, "pom.xml" ).isFile() )
            {
                // it's a directory that exists
                continue;
            }

            if ( moduleFile.isFile() )
            {
                // it's the pom.xml file directly referenced and it exists.
                continue;
            }

            logger.debug( "Removing missing child module " + modulePath );
            i.remove();
        }
        debugModules( logger, "After removing missing", childModules );
    }
}
