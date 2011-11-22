package org.apache.maven.diagrams.connectors.classes;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;

import org.apache.maven.diagrams.connectors.classes.asm_parser.AsmClassDataSource;
import org.apache.maven.diagrams.connectors.classes.model.ClassModel;

/**
 * The class represent something like classLoader. You can initialize the object with the classPath and then ask it for
 * {@link ClassModel}s of any class contained on the classpath.
 * 
 * @author Piotr Tabor
 * 
 */
public class ClassModelsRepository
{
    private ClassLoader classLoader;

    private Map<String, ClassModel> classModelsMap;

    private ClassDataSource classDataSource;

    /**
     * 
     * @param classpath
     */
    public ClassModelsRepository( URL[] classpath )
    {
        classLoader = new URLClassLoader( classpath );
        classModelsMap = new HashMap<String, ClassModel>();
        classDataSource = new AsmClassDataSource();
    }

    /**
     * Returns classModel for given fully-dot-qualified className
     * 
     * @param className
     * @return
     * @throws ClassDataSourceException
     */
    public ClassModel getClassModel( String className ) throws ClassDataSourceException
    {
        ClassModel res = classModelsMap.get( className );
        if ( res == null )
        {
            res = classDataSource.translateToClassModel( classLoader, className );
            classModelsMap.put( className, res );
        }
        return res;
    }
}
