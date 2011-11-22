package org.codehaus.mojo.cis.model;

import java.io.File;

import org.apache.maven.model.Dependency;


/**
 * A bean for configuring a single CIS application.
 */
public class CisApplication
{
    private String name;
    private Dependency[] dependencies;
    private File xmlDir, appWebXml;
    private String[] startAppClasses;

    /**
     * Returns the CIS applications name.
     * @return The applications name
     */
    public String getName()
    {
        return name;
    }

    /**
     * Sets the CIS applications name.
     * @param name The applications name
     */
    public void setName( String name )
    {
        this.name = name;
    }

    /**
     * Returns the applications set of dependencies. These are being copied
     * to "appclasses/lib".
     * @return Dependency list.
     */
    public Dependency[] getDependencies()
    {
        return dependencies;
    }

    /**
     * Returns the applications set of dependencies. These are being copied
     * to "appclasses/lib".
     * @param dependencies Dependency list.
     */
    public void setDependencies( Dependency[] dependencies )
    {
        this.dependencies = dependencies;
    }

    /**
     * Returns the CIS applications web.xml, which is merged into the
     * web applications web.xml.
     * @return Location of the CIS applications "web.xml" file.
     */
    public File getAppWebXml()
    {
        return appWebXml;
    }

    /**
     * Sets the CIS applications web.xml, which is merged into the
     * web applications web.xml.
     * @param appWebXml Location of the CIS applications "web.xml" file.
     */
    public void setAppWebXml( File appWebXml )
    {
        this.appWebXml = appWebXml;
    }

    /**
     * Returns the applications xml directory. Defaults to "src/main/cis/<appname>/xml".
     * @return The applications xml directory.
     */
    public File getXmlDir()
    {
        return xmlDir;
    }

    /**
     * Sets the applications xml directory. Defaults to "src/main/cis/<appname>/xml".
     * @param xmlDir The applications xml directory.
     */
    public void setXmlDir( File pXmlDir )
    {
        xmlDir = pXmlDir;
    }

    /**
     * Returns the set of classes, which are being invoked when the application
     * starts.
     * @param The set of classes, which are being started with the application.
     */
    public String[] getStartAppClasses()
    {
        return startAppClasses;
    }

    /**
     * Sets the set of classes, which are being invoked when the application
     * starts.
     * @param The set of classes, which are being started with the application.
     */
    public void setStartAppClasses(String[] pClassNames)
    {
        startAppClasses = pClassNames;
    }
}
