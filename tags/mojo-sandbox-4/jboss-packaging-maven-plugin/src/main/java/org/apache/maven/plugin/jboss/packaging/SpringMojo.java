package org.apache.maven.plugin.jboss.packaging;

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

import org.apache.maven.plugin.MojoExecutionException;

/**
 * Build a deployable JBoss Spring Archive.
 * 
 * @goal spring
 * @phase package
 * @requiresDependencyResolution runtime
 */
public class SpringMojo 
  extends AbstractSpringPackagingMojo
{

  /**
   * Executes the SpringMojo on the current project.
   * 
   * @throws MojoExecutionException
   *           if an error occured while building the webapp
   */
  public void execute()
    throws MojoExecutionException
  {
	  	  	
    File archiveFile = new File(getOutputDirectory(), getArchiveName() + ".spring");

    try {
      performPackaging(archiveFile);
    }
    catch (Exception e) {
      throw new MojoExecutionException("Error assembling archive", e);
    }
  }

}
