package org.codehaus.mojo.weblogic;

/*
 * Copyright 2006 The Apache Software Foundation.
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

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.mojo.weblogic.util.WeblogicMojoUtilities;

import weblogic.webservice.tools.clientgen.ClientGen;

import java.io.File;
import java.util.Set;

/**
 * Runs Client Gen on a given WSDL.
 * 
 * @author <a href="mailto:scott@theryansplace.com">Scott Ryan</a>
 * @version $Id: ListAppsMojo.java 2166 2006-07-18 21:32:16Z carlos $
 * @description This mojo will run client gen on a given WSDL.
 * @goal clientgen
 * @requiresDependencyResolution compile
 */
public class ClientGenMojo extends AbstractMojo
{

    /**
     * The wsdl to client gen from.
     * 
     * @parameter default-value="http://localhost:7001"
     */
    private String inputWSDL;

    /**
     * The directory to output the geneated code to.
     * 
     * @parameter default-value="${basedir}/src/main/java"
     */
    private String outputDir;

    /**
     * The package name of the output code.
     * 
     * @parameter default-value="com.test.webservice"
     */
    private String packageName;

    /**
     * The name of the service.
     * 
     * @parameter default-value="test"
     */
    private String serviceName;

    /**
     * This is the set of artifacts that are defined as part of this project's pom which are active for the compile
     * scope. You should not need to override this unless your pom file is incomplete.
     * 
     * @parameter expression="${project.artifacts}"
     * @required
     * @readonly
     */
    private Set artifacts;

    /**
     * This method will run client gen on the given WSDL.
     * 
     * @throws MojoExecutionException
     *             Thrown if we fail to obtain the WSDL.
     */
    public void execute() throws MojoExecutionException
    {

        if ( getLog().isInfoEnabled() )
        {
            getLog().info( "Weblogic client gen beginning " );
        }

        try
        {
            ClientGen clientGen = new ClientGen();
            clientGen.setWSDL( inputWSDL );
            clientGen.setClientJar( new File( this.outputDir ) );
            clientGen.setClientPackageName( this.packageName );
            clientGen.setServiceName( this.serviceName );
            // Set the classpath
            clientGen.setClasspath( WeblogicMojoUtilities.getDependencies( this.getArtifacts() ) );
            clientGen.generateClientJar();
        }
        catch ( Exception ex )
        {
            getLog().error( "Exception encountered during client gen ", ex );
            throw new MojoExecutionException( "Exception encountered during listapps", ex );
        }

        if ( getLog().isInfoEnabled() )
        {
            getLog().info( "Weblogic client gen successful " );
        }
    }

    /**
     * Getter for property input WSDL.
     * 
     * @return The value of input WSDL.
     */
    public String getInputWSDL()
    {
        return this.inputWSDL;
    }

    /**
     * Setter for the input WSDL.
     * 
     * @param inInputWSDL
     *            The value of input WSDL.
     */
    public void setInputWSDL( final String inInputWSDL )
    {
        this.inputWSDL = inInputWSDL;
    }

    /**
     * Getter for property output dir.
     * 
     * @return The value of output dir.
     */
    public String getOutputDir()
    {
        return this.outputDir;
    }

    /**
     * Setter for the output dir.
     * 
     * @param inOutputDir
     *            The value of output dir.
     */
    public void setOutputDir( final String inOutputDir )
    {
        this.outputDir = inOutputDir;
    }

    /**
     * Getter for property package name.
     * 
     * @return The value of package name.
     */
    public String getPackageName()
    {
        return this.packageName;
    }

    /**
     * Setter for the package name.
     * 
     * @param inPackageName
     *            The value of package name.
     */
    public void setPackageName( String inPackageName )
    {
        this.packageName = inPackageName;
    }

    /**
     * Getter for property service name.
     * 
     * @return The value of service name.
     */
    public String getServiceName()
    {
        return this.serviceName;
    }

    /**
     * Setter for the service name.
     * 
     * @param inServiceName
     *            The value of service name.
     */
    public void setServiceName( final String inServiceName )
    {
        this.serviceName = inServiceName;
    }

    /**
     * Getter for property artifacts.
     * 
     * @return The value of artifacts.
     */
    public Set getArtifacts()
    {
        return artifacts;
    }

    /**
     * Setter for the artifacts.
     * 
     * @param inArtifacts
     *            The value of artifacts.
     */
    public void setArtifacts( final Set inArtifacts )
    {
        this.artifacts = inArtifacts;
    }

    /**
     * toString methode: creates a String representation of the object
     * 
     * @return the String representation
     */
    public String toString()
    {
        StringBuffer buffer = new StringBuffer();
        buffer.append( "ClientGenMojo[" );
        buffer.append( "inputWSDL = " ).append( inputWSDL );
        buffer.append( ", outputDir = " ).append( outputDir );
        buffer.append( ", packageName = " ).append( packageName );
        buffer.append( ", serviceName = " ).append( serviceName );
        buffer.append( ", artifacts = " ).append( artifacts );
        buffer.append( "]" );
        return buffer.toString();
    }
}
