package org.apache.maven.diagrams.connectors.classes;

import java.io.InputStream;

import org.apache.maven.diagrams.connectors.classes.model.ClassModel;

/**
 * Interface for all datasources (sources of informations about single class). 
 * There could be many different implementation (reflection, asm, javassist) of such a datasource   
 * 
 * @author Piotr Tabor
 *
 */
public interface ClassDataSource
{

    /**
     * Gets the information from given class object
     * @param c - class to get information about it
     * @return
     * @throws ClassDataSourceException
     */
    @SuppressWarnings( "unchecked" )
    public abstract ClassModel translateToClassModel( Class c ) throws ClassDataSourceException;

    /**
     * Gets the information from given inputstream of class's bytecode. 
     * @param is
     * @return
     * @throws ClassDataSourceException
     */
    public abstract ClassModel translateToClassModel( InputStream is ) throws ClassDataSourceException;

    /**
     * Gets the information about given class name (full "dot" qualified name)  
     * @param className
     * @return
     * @throws ClassDataSourceException
     */
    public abstract ClassModel translateToClassModel( String className ) throws ClassDataSourceException;

    /**
     * Gets the information about given class name (full "dot" qualified name), using
     * given classloader
     *   
     * @param className
     * @return
     * @throws ClassDataSourceException
     */
    public abstract ClassModel translateToClassModel( ClassLoader classLoader,String className ) throws ClassDataSourceException;

}