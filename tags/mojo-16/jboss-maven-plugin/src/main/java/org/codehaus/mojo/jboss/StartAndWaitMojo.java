package org.codehaus.mojo.jboss;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.maven.plugin.MojoExecutionException;

/**
 * Start JBoss and wait until server is started
 * 
 * @author <a href="mailto:jc7442@yahoo.fr">J-C</a>
 * @goal startAndWait
 * @description Maven 2 JBoss plugin
 */
public class StartAndWaitMojo extends StartMojo {
    /**
     * Number maximum of retry to JBoss jmx MBean connection
     * 
     * @parameter expression="3"
     * @required
     */
    protected int retry;

    /**
     * Timeout in ms to start the application server (once jmx MBean connection as
     * been readched)
     * 
     * @parameter expression="20000"
     * @required
     */
    protected int timeout;

    /**
     * The port for the naming service
     * 
     * @parameter expression="1099"
     * @required
     */
    protected String namingPort;

    /**
     * The host jboss is running on
     * 
     * @parameter expression="localhost"
     * @required
     */
    protected String hostName;

    public void execute() throws MojoExecutionException {
        // Start JBoss
        super.execute();
        // Initialize the initial context
        InitialContext ctx = getInitialContext();
        // Try to get JBoss jmx MBean connection
        MBeanServerConnection s = null;
        int i = 0;
        while (true) {
            try {
                s = (MBeanServerConnection) ctx
                    .lookup("jmx/invoker/RMIAdaptor");
                break;
            } catch (NamingException e) {
                i++;
                if (i > retry) {
                    throw new MojoExecutionException(
                            "Unable to get JBoss jmx MBean connection: "
                            + e.getMessage(), e);
                }
                getLog().info("Retry to retrieve JBoss jmx MBean connection !");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        getLog().info("JBoss jmx MBean connection successful!");
        // Wait server is started
        boolean isStarted = false;
        long startTime = System.currentTimeMillis();
        while (!isStarted && System.currentTimeMillis() - startTime < timeout) {
            try {
                isStarted = isStarted(s);
            } catch (Exception e) {
                throw new MojoExecutionException("Unable to wait: "
                        + e.getMessage(), e);
            }
        }
        if (!isStarted) {
            throw new MojoExecutionException(
                    "JBoss AS is not stared before timeout has expired! ");
        }
        getLog().info("JBoss server started!");
    }

    protected boolean isStarted(MBeanServerConnection s) throws Exception {
        ObjectName serverMBeanName = new ObjectName("jboss.system:type=Server");
        return ((Boolean) s.getAttribute(serverMBeanName, "Started")).booleanValue();
    }

    protected InitialContext getInitialContext() throws MojoExecutionException {
        try {
            System.getProperties().put("java.naming.factory.initial",
                    "org.jnp.interfaces.NamingContextFactory");
            System.getProperties().put("java.naming.factory.url.pkgs",
                    "org.jboss.naming:org.jnp.interfaces");
            System.getProperties().put("java.naming.provider.url",
                    hostName + ":" + namingPort);
            return new InitialContext();
        } catch (NamingException e) {
            throw new MojoExecutionException(
                    "Unable to instantiate naming context: " + e.getMessage(), e);
        }
    }
}
