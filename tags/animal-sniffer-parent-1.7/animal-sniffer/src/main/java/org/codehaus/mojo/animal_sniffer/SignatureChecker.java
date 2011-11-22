package org.codehaus.mojo.animal_sniffer;

/*
 * The MIT License
 *
 * Copyright (c) 2008 Kohsuke Kawaguchi and codehaus.org.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 */

import org.codehaus.mojo.animal_sniffer.logging.Logger;
import org.codehaus.mojo.animal_sniffer.logging.PrintWriterLogger;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.commons.EmptyVisitor;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

/**
 * Checks the signature against classes in this list.
 *
 * @author Kohsuke Kawaguchi
 */
public class SignatureChecker
    extends ClassFileVisitor
{
    private final Map/*<String, Clazz>*/ classes = new HashMap();

    private final Logger logger;

    /**
     * Classes in this packages are considered to be resolved elsewhere and
     * thus not a subject of the error checking when referenced.
     */
    private final List ignoredPackageRules;

    private final Set ignoredPackages;

    private boolean hadError = false;

    public static void main( String[] args )
        throws Exception
    {
        Set ignoredPackages = new HashSet();
        ignoredPackages.add( "org.jvnet.animal_sniffer.*" );
        ignoredPackages.add( "org.codehaus.mojo.animal_sniffer.*" );
        ignoredPackages.add( "org.objectweb.*" );
        new SignatureChecker( new FileInputStream( "signature" ), ignoredPackages,
                              new PrintWriterLogger( System.out ) ).process( new File( "target/classes" ) );
    }

    public SignatureChecker( InputStream in, Set ignoredPackages, Logger logger )
        throws IOException
    {
        this.ignoredPackages = new HashSet();
        this.ignoredPackageRules = new LinkedList();
        Iterator i = ignoredPackages.iterator();
        while ( i.hasNext() )
        {
            String wildcard = (String) i.next();
            if ( wildcard.indexOf( '*' ) == -1 && wildcard.indexOf( '?' ) == -1 )
            {
                this.ignoredPackages.add( wildcard.replace( '.', '/' ) );
            }
            else
            {
                this.ignoredPackageRules.add( newMatchRule( wildcard.replace( '.', '/' ) ) );
            }
        }
        this.logger = logger;
        ObjectInputStream ois = null;
        try
        {
            ois = new ObjectInputStream( new GZIPInputStream( in ) );
            while ( true )
            {
                Clazz c = (Clazz) ois.readObject();
                if ( c == null )
                {
                    return; // finished
                }
                classes.put( c.getName(), c );
            }
        }
        catch ( ClassNotFoundException e )
        {
            throw new NoClassDefFoundError( e.getMessage() );
        }
        finally
        {
            if ( ois != null )
            {
                try
                {
                    ois.close();
                }
                catch ( IOException e )
                {
                    // ignore
                }
            }
        }
    }

    protected void process( final String name, InputStream image )
        throws IOException
    {
        ClassReader cr = new ClassReader( image );

        try
        {
            cr.accept( new CheckingVisitor( name ), 0 );
        }
        catch ( ArrayIndexOutOfBoundsException e )
        {
            logger.error( "Bad class file " + name );
            // MANIMALSNIFFER-9 it is a pity that ASM does not throw a nicer error on encountering a malformed
            // class file.
            IOException ioException = new IOException( "Bad class file " + name );
            ioException.initCause( e );
            throw ioException;
        }
    }

    private static interface MatchRule
    {
        boolean matches( String text );
    }

    private static class PrefixMatchRule
        implements SignatureChecker.MatchRule
    {
        private final String prefix;

        public PrefixMatchRule( String prefix )
        {
            this.prefix = prefix;
        }

        public boolean matches( String text )
        {
            return text.startsWith( prefix );
        }
    }

    private static class ExactMatchRule
        implements SignatureChecker.MatchRule
    {
        private final String match;

        public ExactMatchRule( String match )
        {
            this.match = match;
        }

        public boolean matches( String text )
        {
            return match.equals( text );
        }
    }

    private static class RegexMatchRule
        implements SignatureChecker.MatchRule
    {
        private final Pattern regex;

        public RegexMatchRule( Pattern regex )
        {
            this.regex = regex;
        }

        public boolean matches( String text )
        {
            return regex.matcher( text ).matches();
        }
    }

    private SignatureChecker.MatchRule newMatchRule( String matcher )
    {
        int i = matcher.indexOf( '*' );
        if ( i == -1 )
        {
            return new ExactMatchRule( matcher );
        }
        if ( i == matcher.length() - 1 )
        {
            return new PrefixMatchRule( matcher.substring( 0, i ) );
        }
        return new RegexMatchRule( RegexUtils.compileWildcard( matcher ) );
    }

    public boolean isSignatureBroken()
    {
        return hadError;
    }

    private class CheckingVisitor
        extends EmptyVisitor
    {
        private final Set ignoredPackageCache;

        private final Set warned;

        private final String name;

        public CheckingVisitor( String name )
        {
            this.ignoredPackageCache = new HashSet( 50 * ignoredPackageRules.size() );
            this.warned = new HashSet();
            this.name = name;
        }

        public MethodVisitor visitMethod( int access, String name, String desc, String signature, String[] exceptions )
        {
            return new EmptyVisitor()
            {
                /**
                 * True if @IgnoreJRERequirement is set.
                 */
                boolean ignoreError = false;

                public AnnotationVisitor visitAnnotation( String desc, boolean visible )
                {
                    if ( desc.equals( "Lorg/jvnet/animal_sniffer/IgnoreJRERequirement;" ) )
                    {
                        ignoreError = true;
                    }
                    if ( desc.equals( "Lorg/codehaus/mojo/animal_sniffer/IgnoreJRERequirement;" ) )
                    {
                        ignoreError = true;
                    }
                    return super.visitAnnotation( desc, visible );
                }

                public void visitMethodInsn( int opcode, String owner, String name, String desc )
                {
                    check( owner, name + desc );
                }

                public void visitTypeInsn( int opcode, String type )
                {
                    if ( shouldBeIgnored( type ) )
                    {
                        return;
                    }
                    if ( type.charAt( 0 ) == '[' )
                    {
                        return; // array
                    }
                    Clazz sigs = (Clazz) classes.get( type );
                    if ( sigs == null )
                    {
                        error( "Undefined reference: " + type );
                    }
                }

                public void visitFieldInsn( int opcode, String owner, String name, String desc )
                {
                    check( owner, name + '#' + desc );
                }

                private void check( String owner, String sig )
                {
                    if ( shouldBeIgnored( owner ) )
                    {
                        return;
                    }
                    if ( find( (Clazz) classes.get( owner ), sig ) )
                    {
                        return; // found it
                    }
                    error( "Undefined reference: " + owner + '.' + sig );
                }

                private boolean shouldBeIgnored( String type )
                {
                    if ( ignoreError )
                    {
                        return true;    // warning suppressed in this context
                    }
                    if ( type.charAt( 0 ) == '[' )
                    {
                        return true; // array
                    }

                    if ( ignoredPackages.contains( type ) || ignoredPackageCache.contains( type ) )
                    {
                        return true;
                    }
                    Iterator i = ignoredPackageRules.iterator();
                    while ( i.hasNext() )
                    {
                        MatchRule rule = (MatchRule) i.next();
                        if ( rule.matches( type ) )
                        {
                            ignoredPackageCache.add( type );
                            return true;
                        }
                    }
                    return false;
                }
            };
        }

        /**
         * If the given signature is found in the specified class, return true.
         */
        private boolean find( Clazz c, String sig )
        {
            if ( c == null )
            {
                return false;
            }
            if ( c.getSignatures().contains( sig ) )
            {
                return true;
            }

            if ( sig.startsWith( "<" ) )
            // constructor and static initializer shouldn't go up the inheritance hierarchy
            {
                return false;
            }

            if ( find( (Clazz) classes.get( c.getSuperClass() ), sig ) )
            {
                return true;
            }

            if ( c.getSuperInterfaces() != null )
            {
                for ( int i = 0; i < c.getSuperInterfaces().length; i++ )
                {
                    if ( find( (Clazz) classes.get( c.getSuperInterfaces()[i] ), sig ) )
                    {
                        return true;
                    }
                }
            }

            return false;
        }

        private void error( String msg )
        {
            hadError = true;
            if ( warned.add( msg ) )
            {
                logger.error( msg + " in " + name );
            }
        }
    }
}
