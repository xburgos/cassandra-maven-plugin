package org.codehaus.mojo.archetypeng.mojos.registry;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.mojo.archetypeng.ArchetypeArtifactManager;
import org.codehaus.mojo.archetypeng.ArchetypeConfiguration;
import org.codehaus.mojo.archetypeng.ArchetypeDefinition;
import org.codehaus.mojo.archetypeng.ArchetypeFactory;
import org.codehaus.mojo.archetypeng.ArchetypePropertiesManager;
import org.codehaus.mojo.archetypeng.ArchetypeRegistryManager;
import org.codehaus.mojo.archetypeng.Constants;
import org.codehaus.mojo.archetypeng.exception.ArchetypeGenerationConfigurationFailure;
import org.codehaus.mojo.archetypeng.exception.ArchetypeNotDefined;
import org.codehaus.mojo.archetypeng.exception.UnknownArchetype;
import org.codehaus.mojo.archetypeng.registry.ArchetypeRegistry;
import org.codehaus.plexus.util.StringUtils;

/**
 *
 * @author rafale

 * @requiresProject  false
 * @goal             remove-languages
 */
public class RemoveLanguagesMojo
extends AbstractMojo
{
    /**
     * @parameter  expression="${user.home}/.m2/archetype.xml"
     */
    private File archetypeRegistryFile;

    /**
     * @component
     */
    ArchetypeRegistryManager archetypeRegistryManager;
    
    /**
     * @parameter expression="${language}"
     */
    String  language;
    
    /**
     * @parameter expression="${languages}"
     */
    String languages;

    public void execute()
    throws MojoExecutionException, MojoFailureException
    {
        if (StringUtils.isEmpty(language)&&StringUtils.isEmpty(languages))
        {
            throw new MojoFailureException("-Dlanguage or -Dlanguages must be set");
        }
        else if (StringUtils.isNotEmpty(language)&& StringUtils.isNotEmpty(languages))
        {
            throw new MojoFailureException("Only one of -Dlanguage or -Dlanguages can be set");
        }
        
        
        try
        {
            List languagesToRemove = new ArrayList();
            if (StringUtils.isNotEmpty(language))
            {
                languagesToRemove.add(language);
            }
            else
            {
                languagesToRemove.addAll(Arrays.asList(StringUtils.split(languages, ",")));
            }
            
            ArchetypeRegistry registry = archetypeRegistryManager.readArchetypeRegistry(
                    archetypeRegistryFile
                );
            
            Iterator languagesToRemoveIterator = languagesToRemove.iterator();
            while(languagesToRemoveIterator.hasNext())
            {
                String languageToRemove = (String)languagesToRemoveIterator.next();
                if (registry.getLanguages().contains(languageToRemove))
                {
                    registry.removeLanguage(languageToRemove);
                    getLog().debug("Language "+ languageToRemove+" removed");
                }
                else
                {
                    getLog().debug("Language "+ languageToRemove+" doesn't exist");
                }
            }
            archetypeRegistryManager.writeArchetypeRegistry(archetypeRegistryFile, registry);
        }
        catch ( Exception ex )
        {
            throw new MojoExecutionException ( ex.getMessage (), ex );
        }
    }

}
