package org.apache.maven.diagrams.gui.renderers.umlClass;

import org.apache.maven.diagrams.gui.renderers.AbstractRendererConfiguration;
import org.apache.maven.diagrams.gui.renderers.RendererConfigurationItemImpl;

public class UmlClassRendererConfiguration extends AbstractRendererConfiguration
{
    static public final String ATT_NAME = "name";

    static public final String ATT_PROPERTIES = "properties";

    static public final String ATT_PUBLIC_FIELDS = "public fields";

    static public final String ATT_PUBLIC_METHODS = "public methods";

    static public final String ATT_PROTECTED_FIELDS = "protected fields";

    static public final String ATT_PROTECTED_METHODS = "protected methods";

    static public final String ATT_PRIVATE_FIELDS = "private fields";

    static public final String ATT_PRIVATE_METHODS = "private methods";

    public UmlClassRendererConfiguration()
    {
        getRenderConfigurationItems().put( ATT_NAME, new UmlClassRendererConfigurationItem( ATT_NAME, true, true ) );
        getRenderConfigurationItems().put( ATT_PROPERTIES, new RendererConfigurationItemImpl( ATT_PROPERTIES, true ) );
        getRenderConfigurationItems().put( ATT_PUBLIC_FIELDS,
                                           new RendererConfigurationItemImpl( ATT_PUBLIC_FIELDS, true ) );
        getRenderConfigurationItems().put( ATT_PUBLIC_METHODS,
                                           new RendererConfigurationItemImpl( ATT_PUBLIC_METHODS, true ) );
        getRenderConfigurationItems().put( ATT_PROTECTED_FIELDS,
                                           new RendererConfigurationItemImpl( ATT_PROTECTED_FIELDS, false ) );
        getRenderConfigurationItems().put( ATT_PROTECTED_METHODS,
                                           new RendererConfigurationItemImpl( ATT_PROTECTED_METHODS, false ) );
        getRenderConfigurationItems().put( ATT_PRIVATE_FIELDS,
                                           new RendererConfigurationItemImpl( ATT_PRIVATE_FIELDS, false ) );
        getRenderConfigurationItems().put( ATT_PRIVATE_METHODS,
                                           new RendererConfigurationItemImpl( ATT_PRIVATE_METHODS, false ) );
    }
}
