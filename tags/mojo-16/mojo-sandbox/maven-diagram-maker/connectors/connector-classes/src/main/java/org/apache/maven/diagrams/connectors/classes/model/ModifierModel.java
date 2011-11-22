package org.apache.maven.diagrams.connectors.classes.model;

import java.util.EnumSet;

import com.sun.org.apache.bcel.internal.Constants;

/**
 * The enum represents access modifier to the class/field/method 
 * @author Piotr Tabor
 *
 */
public enum ModifierModel
{
    STATIC,
    PUBLIC,
    PROTECTED,
    PRIVATE;
    
    /**
     * Transforms maps of com.sun.org.apache.bcel.internal.Constants.ACC_... modifiers
     * into the EnumSet. 
     * 
     * @param access - combination of modifier's constants
     * @return
     */
    public static EnumSet<ModifierModel> accessContantsToModifiers(int access)
    {
        EnumSet<ModifierModel> result=EnumSet.noneOf( ModifierModel.class );
        
        if((access & Constants.ACC_PRIVATE) == Constants.ACC_PRIVATE)
            result.add(PRIVATE);
        if((access & Constants.ACC_PUBLIC) == Constants.ACC_PUBLIC)
            result.add(PUBLIC);
        if((access & Constants.ACC_PROTECTED) == Constants.ACC_PROTECTED)
            result.add(PROTECTED);
        if((access & Constants.ACC_STATIC) == Constants.ACC_STATIC)
            result.add(STATIC);
        
        return result;
    }
}
