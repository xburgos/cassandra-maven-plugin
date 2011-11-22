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

import org.apache.xmlbeans.impl.tool.SchemaCompiler;
import org.apache.maven.artifact.DependencyResolutionRequiredException;

/**
 *
 * @author <a href="mailto:kris.bravo@corridor-software.us">Kris Bravo</a>
 * @version $Id$
 */
public final class ParameterAdapter
{

   /**
    * Creates a new instance of ParameterAdapter.
    */
   private ParameterAdapter()
   {
   }

   /**
    * Returns a parameter set appropriate for the SchemaCompiler.
    *
    * @param properties XML Bean Plugin properties.
    * @throws DependencyResolutionRequiredException Maven Dependencies were
    *         not resolved.
    * @return Parameters for the schema compiler
    */
   static SchemaCompiler.Parameters getCompilerParameters(final PluginProperties properties)
      throws DependencyResolutionRequiredException, XmlBeansException
   {
      SchemaCompiler.Parameters params = new SchemaCompiler.Parameters();

      params.setBaseDir(properties.getBaseDir());
      params.setXsdFiles(properties.getXsdFiles());
      params.setWsdlFiles(properties.getWsdlFiles());
      params.setJavaFiles(properties.getJavaFiles());
      params.setConfigFiles(properties.getConfigFiles());
      params.setClasspath(properties.getClasspath());
      params.setOutputJar(properties.getOutputJar());
      params.setName(properties.getName());
      params.setSrcDir(properties.getGeneratedSourceDirectory());
      params.setClassesDir(properties.getGeneratedClassesDirectory());
      params.setCompiler(properties.getCompiler());
      params.setMemoryInitialSize(properties.getMemoryInitialSize());
      params.setMemoryMaximumSize(properties.getMemoryMaximumSize());
      params.setNojavac(properties.isNoJavac());
      params.setQuiet(properties.isQuiet());
      params.setVerbose(properties.isVerbose());
      params.setDownload(properties.isDownload());
      params.setNoUpa(properties.isNoUpa());
      params.setNoPvr(properties.isNoPvr());
      params.setDebug(properties.isDebug());
      params.setErrorListener(properties.getErrorListeners());
      params.setRepackage(properties.getRepackage());
      params.setExtensions(properties.getExtensions());
      params.setMdefNamespaces(properties.getMdefNamespaces());
      return params;
   }
}
