package org.codehaus.mojo.xmlbeans;

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
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

import org.apache.xmlbeans.impl.tool.SchemaCompiler;

import org.apache.maven.artifact.DependencyResolutionRequiredException;

import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.FileUtils;

/**
 * <p>A Maven 2 plugin which parses xsd files and produces a corresponding object
 * model based on the Apache XML Beans parser.</p>
 *
 * <p>The plugin produces two sets of output files referred to as generated sources
 * and generated classes. The former is then compiled to the build
 * <code>outputDirectory</code>. The latter is generated in this directory.</p>
 *
 * <p>Note that the descriptions for the goal's parameters have been blatently
 * copied from http://xmlbeans.apache.org/docs/2.0.0/guide/antXmlbean.html for
 * convenience.</p>
 *
 * @author <a href="mailto:brett@apache.org">Brett Porter</a>
 * @author <a href="mailto:kris.bravo@corridor-software.us">Kris Bravo</a>
 * @version $Id$
 * @goal xmlbeans-test
 * @phase generate-test-sources
 * @requiresDependencyResolution compile
 * @description Creates java beans which map to XML schemas.
 *
 */
public class TestXmlBeansMojo
   extends AbstractXmlBeansPlugin
{

	/**
    * The directory where .xsd files are to be found.
    *
    * @parameter default-value="${basedir}/src/test/xsd"
    * @required
    */
   protected File schemaDirectory;

   /**
    * List of artifacts whose xsds need to be compiled.
    * 
    * @parameter
    */
   private List testXsdJars;
   
   /**
    * The directory where .xsd's pulled from xsdJars will be stored.
    * 
    * @parameter default-value="${project.build.directory}/test-xmlbeans-xsds"
    * @parameter
    */
   private File generatedSchemaDirectory;

   /**
    * Set a location to generate CLASS files into.
    *
    * @parameter expression="${project.build.directory}/generated-classes/test-xmlbeans"
    * @required
    */
   protected File classGenerationDirectory;

   /**
    * Set a location to generate JAVA files into.
    *
    * @parameter expression="${project.build.directory}/generated-sources/test-xmlbeans"
    * @required
    */
   protected File sourceGenerationDirectory;
   
   /**
    * The location of the flag file used to determine if the output is stale.
    *
    * @parameter expression="${project.build.directory}/generated-sources/test-xmlbeans/.staleFlag"
    * @required
    */
   protected File staleFile;

   /**
    * Default xmlConfigs directory. If no xmlConfigs list is specified, this
    * one is checked automatically.
    *
    * @parameter expression="${basedir}/src/test/xsdconfig"
    */
   protected File defaultXmlConfigDir;

    /**
    * Empty constructor for the XML Beans plugin.
    */
   public TestXmlBeansMojo() 
   {
   }

   
   protected void updateProject(MavenProject project, SchemaCompiler.Parameters compilerParams) 
   throws DependencyResolutionRequiredException, XmlBeansException
   {
       project.addTestCompileSourceRoot(compilerParams.getSrcDir().getAbsolutePath());
       
       File outputDirectory = new File( project.getBuild().getTestOutputDirectory() );
       if ( !outputDirectory.exists() )
       {
           outputDirectory.mkdirs();
       }
       
       getLog().debug("Copying generated test classes into the output directory.");
       try {
            FileUtils.copyDirectoryStructure(compilerParams.getClassesDir(), outputDirectory);
       } catch (IOException ex) {
            throw new XmlBeansException(XmlBeansException.COPY_CLASSES, outputDirectory.getAbsolutePath(), ex);
       }
       
   }
   
   /**
    * Returns the directory where the schemas are located. Note that this is
    * the base directory of the schema compiler, not the maven project.
    *
    * @return The schema directory.
    */
   public File getBaseDir()
   {
      return getSchemaDirectory();
   }

   /**
    * Returns the class directory of the project.
    *
    * @return The project build classes directory.
    */
   public final File getGeneratedClassesDirectory()
   {
      return classGenerationDirectory;
   }

   /**
    * Returns the directory for saving generated source files.
    *
    * @return The generated=sources directory.
    */
   public final File getGeneratedSourceDirectory()
   {
      return sourceGenerationDirectory;
   }

   public File getStaleFile()
   {
	   return staleFile;
   }
   
   public File getDefaultXmlConfigDir()
   {
	   return defaultXmlConfigDir;
   }
   
   /**
    * Returns the directory where the schemas are located. Note that this is
    * the base directory of the schema compiler, not the maven project.
    *
    * @return The schema directory.
    */
   public File getSchemaDirectory()
   {
	   return schemaDirectory;
   }

   /**
    * Returns the list of xsd jars.
    */
	protected List getXsdJars() 
	{
		if (testXsdJars == null) 
		{
			testXsdJars = new ArrayList();
		}
		return testXsdJars;
	}

	protected File getGeneratedSchemaDirectory() 
	{
		return generatedSchemaDirectory;
	}
	
}
