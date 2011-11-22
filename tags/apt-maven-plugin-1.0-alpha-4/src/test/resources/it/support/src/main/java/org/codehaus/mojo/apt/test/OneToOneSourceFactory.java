package org.codehaus.mojo.apt.test;

/*
 * The MIT License
 *
 * Copyright 2006-2008 The Codehaus.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import com.sun.mirror.apt.AnnotationProcessor;
import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import com.sun.mirror.apt.AnnotationProcessorFactory;
import com.sun.mirror.apt.AnnotationProcessors;
import com.sun.mirror.apt.RoundCompleteEvent;
import com.sun.mirror.apt.RoundCompleteListener;
import com.sun.mirror.declaration.AnnotationTypeDeclaration;

/**
 * Simple apt factory for use by integration tests.
 * 
 * @author <a href="mailto:markhobson@gmail.com">Mark Hobson</a>
 * @version $Id$
 */
public class OneToOneSourceFactory implements AnnotationProcessorFactory, RoundCompleteListener
{
    // fields -----------------------------------------------------------------

    private static boolean complete;

    // AnnotationProcessorFactory methods -------------------------------------

    /**
     * {@inheritDoc}
     */
    public Collection<String> supportedOptions()
    {
        return Collections.emptySet();
    }

    /**
     * {@inheritDoc}
     */
    public Collection<String> supportedAnnotationTypes()
    {
        return Collections.singleton( "*" );
    }

    /**
     * {@inheritDoc}
     */
    public AnnotationProcessor getProcessorFor( Set<AnnotationTypeDeclaration> annotations,
                                                AnnotationProcessorEnvironment environment )
    {
        // listen to round complete event to prevent recursion
        environment.addListener( this );

        return complete ? AnnotationProcessors.NO_OP : new OneToOneSourceProcessor( environment );
    }

    // RoundCompleteListener methods ------------------------------------------

    /**
     * {@inheritDoc}
     */
    public void roundComplete( RoundCompleteEvent event )
    {
        complete = true;
    }
}
