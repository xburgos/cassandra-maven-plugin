package org.codehaus.plexus.shade;

import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.shade.relocation.SimpleRelocator;
import org.codehaus.plexus.shade.resource.ComponentsXmlResourceTransformer;

import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.io.File;


/** @author Jason van Zyl */
public class ShadeMojoTest
    extends PlexusTestCase
{
    public void testShader()
        throws Exception
    {
        Shader s = (Shader) lookup( Shader.ROLE );

        Set set = new HashSet();

        set.add( new File( getBasedir(), "src/test/jars/test-project-1.0-SNAPSHOT.jar") );

        set.add( new File( getBasedir(), "src/test/jars/plexus-utils-1.4.1.jar") );

        File jar = new File( "foo.jar" );

        List relocators = new ArrayList();

        relocators.add( new SimpleRelocator( "org/codehaus/plexus/util" ) );

        List resourceTransformers = new ArrayList();

        resourceTransformers.add( new ComponentsXmlResourceTransformer() );

        s.shade( set, jar, relocators, resourceTransformers );
    }
}