package org.apache.maven.diagrams.gui.renderers.umlClass.renderer;

import java.util.EnumSet;

import org.apache.maven.diagrams.connectors.classes.model.ModifierModel;

public enum AccessModifier
{
    PUBLIC, PROTECTED, PRIVATE, PACKAGE;

    boolean hasSuchModifier( EnumSet<ModifierModel> modifier )
    {
        switch ( this )
        {
            case PRIVATE:
                return modifier.contains( ModifierModel.PRIVATE );
            case PUBLIC:
                return modifier.contains( ModifierModel.PUBLIC );
            case PROTECTED:
                return modifier.contains( ModifierModel.PROTECTED );
            case PACKAGE:
                return !( modifier.contains( ModifierModel.PRIVATE ) || modifier.contains( ModifierModel.PUBLIC ) || modifier.contains( ModifierModel.PROTECTED ) );
            default:
                assert ( false );
                return false;
        }
    }

    static boolean hasOneOfSuchModifiers( EnumSet<AccessModifier> accessModifiers, EnumSet<ModifierModel> modelModifier )
    {
        for ( AccessModifier m : accessModifiers )
        {
            if ( m.hasSuchModifier( modelModifier ) )
                return true;
        }
        return false;
    }
}
