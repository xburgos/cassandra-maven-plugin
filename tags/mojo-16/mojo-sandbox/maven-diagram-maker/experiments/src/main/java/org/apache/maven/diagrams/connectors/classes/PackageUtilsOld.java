package org.apache.maven.diagrams.connectors.classes;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

public class PackageUtilsOld
{

    public static List<String> getClasseNamesInPackage( String jarName, String packageName )
        throws IOException
    {
        return getClasseNamesInPackage( new FileInputStream( jarName ), packageName );
    }

    public static List<String> getClasseNamesInPackage( InputStream is, String packageName ) throws IOException
    {
        ArrayList<String> classes = new ArrayList<String>();

        packageName = packageName.replaceAll( "\\.", "/" );

        JarInputStream jarFile = new JarInputStream( is );
        JarEntry jarEntry;

        while ( true )
        {
            jarEntry = jarFile.getNextJarEntry();
            if ( jarEntry == null )
            {
                break;
            }
            if ( ( jarEntry.getName().startsWith( packageName ) ) && ( jarEntry.getName().endsWith( ".class" ) ) )
            {
                classes.add( jarEntry.getName().replaceAll( "/", "\\." ) );
            }
        }
        return classes;
    }
};
