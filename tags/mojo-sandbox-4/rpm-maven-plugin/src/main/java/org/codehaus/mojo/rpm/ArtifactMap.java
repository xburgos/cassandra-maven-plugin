package org.codehaus.mojo.rpm;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.util.List;

/**
 * A description of which project artifacts to package.
 * @version $Id$
 */
public class ArtifactMap
{
    
    // // //  Properties
    
    /** The list of classifiers to package. */
    private List classifiers;
    
    // // //  Bean methods
    
    /**
     * Retrieve the list of classifiers to package.
     * @return The list of classifiers to package.
     */
    public List getClassifiers()
    {
        return classifiers;
    }
    
    /**
     * Set the list of classifiers to package.
     * @param clist The new list of classifiers to package.
     */
    public void setClassifiers( List clist ) 
    {
        classifiers = clist;
    }
    
    // // //  Public methods
    
    /** {@inheritDoc} */
    public String toString()
    {
        StringBuffer sb = new StringBuffer();
        sb.append( "[artifacts" );
        
        if ( classifiers != null )
        {
            sb.append( " w/classifiers " + classifiers );
        }
        
        sb.append( "]" );
        return sb.toString();
    }
}
