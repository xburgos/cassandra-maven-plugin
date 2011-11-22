package org.apache.maven.diagrams.gui.bindings;

import org.apache.maven.diagrams.gui.bindings.connectors.BindingsManager;

import junit.framework.TestCase;

public class BindingsManagerTest extends TestCase
{

    public void testLoadAndResolveBindings()
    {
        BindingsManager bm = new BindingsManager();
        bm.loadAndResolveBindings( TestCase.class.getResourceAsStream( "/diagrams-gui-bindings.xml" ) );
        assertEquals( 1, bm.getResolvedBindings().size());
    }

}
