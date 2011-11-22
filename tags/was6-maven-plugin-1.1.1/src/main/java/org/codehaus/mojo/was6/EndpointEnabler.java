package org.codehaus.mojo.was6;

import java.io.File;

import org.apache.maven.plugin.MojoExecutionException;
import org.dom4j.Document;

/**
 * Executes the endpoint enabler ant task on the EAR archive.
 * 
 * @see http://publib.boulder.ibm.com/infocenter/wasinfo/v6r1/index.jsp?topic=/com.ibm.websphere.javadoc.doc/public_html/api/com/ibm/websphere/ant/tasks/endptEnabler.html
 * @goal endpointEnabler
 * @author karltdav
 * @since 1.1.1
 *
 */
public class EndpointEnabler
    extends AbstractWas6Mojo
{
    
    /**
     * The earFile to process.
     * 
     * @parameter expression="${was6.earFile}" default-value="${project.artifact.file}"
     * @required
     */
    private File earFile;

    /**
     * {@inheritDoc}
     */
    protected void configureBuildScript( Document document )
        throws MojoExecutionException
    {
        configureTaskAttribute( document, "earFile", earFile );
    }

    /**
     * {@inheritDoc}
     */
    protected String getTaskName()
    {
        return "wsEndpointEnabler";
    }

}
