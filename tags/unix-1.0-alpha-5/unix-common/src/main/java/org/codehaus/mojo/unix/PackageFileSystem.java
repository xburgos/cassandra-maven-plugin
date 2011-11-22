package org.codehaus.mojo.unix;

/*
 * The MIT License
 *
 * Copyright 2009 The Codehaus.
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

import static fj.Bottom.*;
import fj.*;
import fj.Function;
import static fj.Function.compose;
import static fj.Function.*;
import static fj.P.*;
import fj.data.*;
import static fj.data.Either.*;
import static fj.data.Option.*;
import static fj.data.Tree.*;
import static fj.data.TreeZipper.*;
import fj.pre.*;
import static fj.pre.Ord.*;
import org.codehaus.mojo.unix.UnixFsObject.*;
import org.codehaus.mojo.unix.util.*;
import static org.codehaus.mojo.unix.util.fj.FunctionF.*;
import org.codehaus.mojo.unix.util.fj.*;

/**
 * @author <a href="mailto:trygvis@codehaus.org">Trygve Laugst&oslash;l</a>
 */
public class PackageFileSystem<A>
{
    private final Fs fs = new Fs();

    private final TreeZipper<PackageFileSystemObject<A>> root;

    private final PackageFileSystemObject<A> defaultDirectory;

    public static <A> PackageFileSystem<A> create( PackageFileSystemObject<A> root,
                                                   PackageFileSystemObject<A> defaultDirectory )
    {
        return new UglyPackageFileSystem<A>( fromTree( leaf( root ) ), defaultDirectory );
    }

    private PackageFileSystem( TreeZipper<PackageFileSystemObject<A>> root, PackageFileSystemObject<A> defaultDirectory )
    {
        Validate.validateNotNull( root, defaultDirectory );
        this.root = root;
        this.defaultDirectory = defaultDirectory;
    }

    private static class UglyPackageFileSystem<A>
        extends PackageFileSystem<A>
    {
        public UglyPackageFileSystem( TreeZipper<PackageFileSystemObject<A>> root, PackageFileSystemObject<A> defaultDirectory )
        {
            super( root, defaultDirectory );
        }
    }

    private class PrettyPackageFileSystem
        extends PackageFileSystem<A>
    {
        public PrettyPackageFileSystem()
        {
            super( fromTree( prettyTree( root.toTree() ) ), defaultDirectory );
        }

        public PackageFileSystem<A> prettify()
        {
            return this;
        }
    }

    // -----------------------------------------------------------------------
    //
    // -----------------------------------------------------------------------

    public boolean hasPath( final RelativePath path )
    {
        List<String> paths = path.toList();

        return paths.isEmpty() || find( root, paths ).isRight();
    }

    public Option<PackageFileSystemObject<A>> getObject( final RelativePath path )
    {
        if ( path.isBase() )
        {
            return some( root.getLabel() );
        }

        return find( root, path.toList() ).right().
            map( TreeZipperF.<PackageFileSystemObject<A>>getLabel_() ).right().toOption();
    }

    public PackageFileSystem<A> addDirectory( PackageFileSystemObject<A> object )
    {
        List<String> names = object.getUnixFsObject().path.toList();
//        String name = names.isEmpty() ? root.getLabel().name : names.reverse().head();

//        PackageFileSystemObject<A> settings = p( (UnixFsObject) directory, a );
        Tree<PackageFileSystemObject<A>> newChild = leaf( object );

        Either<TreeZipper<PackageFileSystemObject<A>>, TreeZipper<PackageFileSystemObject<A>>> either = findAndCreateParentsFor( names );

        return either.
            either( compose( fs.navigateToRootAndCreatePackageFileSystem, curry( flip2( fs.addChild ), newChild ) ),
                    compose( fs.navigateToRootAndCreatePackageFileSystem, curry( fs.mutateExisting, object ) ) );
    }

    public PackageFileSystem<A> addFile( PackageFileSystemObject<A> file )
    {
        if ( file.getUnixFsObject().path.isBase() )
        {
            throw error( "addFile on base path." );
        }

        List<String> names = file.getUnixFsObject().path.toList();
        Tree<PackageFileSystemObject<A>> newChild = leaf( file );

        return findAndCreateParentsFor( names ).
            either( compose( fs.navigateToRootAndCreatePackageFileSystem, curry( flip2( fs.addChild ), newChild ) ),
                    compose( fs.navigateToRootAndCreatePackageFileSystem, curry( fs.mutateExisting, file ) ) );
    }

    public PackageFileSystem<A> addSymlink( PackageFileSystemObject<A> symlink )
    {
        if ( symlink.getUnixFsObject().path.isBase() )
        {
            throw error( "addSymlink on base path." );
        }

        List<String> names = symlink.getUnixFsObject().path.toList();
        Tree<PackageFileSystemObject<A>> newChild = leaf( symlink );

        return findAndCreateParentsFor( names ).
            either( compose( fs.navigateToRootAndCreatePackageFileSystem, curry( flip2( fs.addChild ), newChild ) ),
                compose( fs.navigateToRootAndCreatePackageFileSystem, curry( fs.mutateExisting, symlink ) ) );
    }

    /**
     * Applies the <code>f</code> to all objects in this filesystem.
     * <p/>
     * TODO: Shouldn't it just return a new UnixFsObject?
     */
    public PackageFileSystem<A> apply( final F2<UnixFsObject, FileAttributes, FileAttributes> f )
    {
        TreeZipper<PackageFileSystemObject<A>> root = this.root.map( new F<PackageFileSystemObject<A>, PackageFileSystemObject<A>>()
        {
            public PackageFileSystemObject<A> f( PackageFileSystemObject<A> node )
            {
                final FileAttributes fileAttributes = f.f( node.getUnixFsObject(), node.getUnixFsObject().getFileAttributes() );

                // TODO: check if the attributes was modified
                return node.setFileAttributes( fileAttributes );
            }
        } );

        return new UglyPackageFileSystem<A>( root, defaultDirectory );
    }

    // -----------------------------------------------------------------------
    //
    // -----------------------------------------------------------------------

    // Applies the function to each path, and selects the last one that was some()

    F2<P2<RelativePath, FileAttributes>, F<RelativePath, Option<FileAttributes>>, P2<RelativePath, FileAttributes>>
        fileAttributeFolder =
        new F2<P2<RelativePath, FileAttributes>, F<RelativePath, Option<FileAttributes>>, P2<RelativePath, FileAttributes>>()
        {
            public P2<RelativePath, FileAttributes> f( final P2<RelativePath, FileAttributes> previous,
                                                       final F<RelativePath, Option<FileAttributes>> transformer )
            {
                return previous.map2( new F<FileAttributes, FileAttributes>()
                {
                    public FileAttributes f( FileAttributes fileAttributes )
                    {
                        return transformer.f( previous._1() ).orSome( previous._2() );
                    }
                } );
            }
        };

    public List<PackageFileSystemObject<A>> toList()
    {
        return root.toTree().flatten();
    }

    Tree<PackageFileSystemObject<A>> getTree()
    {
        return root.toTree();
    }

    public PackageFileSystem<A> prettify()
    {
        return new PrettyPackageFileSystem();
    }

    // -----------------------------------------------------------------------
    //
    // -----------------------------------------------------------------------

    /**
     * Returns right with the node if it was just created, or left with the parent and the remaining path
     */
    private Either<TreeZipper<PackageFileSystemObject<A>>, TreeZipper<PackageFileSystemObject<A>>> findAndCreateParentsFor( List<String> paths )
    {
        // If paths is empty, then we're looking for the root
        if ( paths.isEmpty() )
        {
            return right( root );
        }

        return find( root, paths ).left().
            map( fs.createParentsFor );
    }

    private TreeZipper<PackageFileSystemObject<A>> createParentsFor( P2<TreeZipper<PackageFileSystemObject<A>>, List<String>> p2 )
    {
        TreeZipper<PackageFileSystemObject<A>> zipper = p2._1();
        List<String> paths = p2._2();
        RelativePath path = zipper.getLabel().getUnixFsObject().path;

        // Create all but the last path element
        while ( paths.isNotEmpty() && paths.tail().isNotEmpty() )
        {
            path = path.add( paths.head() );
            zipper = addChild( zipper, leaf( defaultDirectory.setPath( path ) ) );
            paths = paths.tail();
        }

        return zipper;
    }

    /**
     * @param parent A zipper focused on the closest, existing parent
     * @param node   The node to insert
     */
    public TreeZipper<PackageFileSystemObject<A>> addChild( TreeZipper<PackageFileSystemObject<A>> parent, Tree<PackageFileSystemObject<A>> node )
    {
//        System.out.println( "Adding to " + parent.getLabel().object.path + ": " + node.root().object.path + " which is a " + node.root().object.getClass() );

        if ( parent.getLabel().getUnixFsObject() instanceof Directory )
        {
            return parent.insertDownFirst( node );
        }

        throw error( "Parent has to be a directory, parent: " + parent.getLabel().getUnixFsObject().path );
    }

    /**
     * Returns right with the node, or left with the closest parent and the remaining path.
     */
    private Either<P2<TreeZipper<PackageFileSystemObject<A>>, List<String>>, TreeZipper<PackageFileSystemObject<A>>> find(
        final TreeZipper<PackageFileSystemObject<A>> parent,
        final List<String> paths )
    {
        String head = paths.head();
        List<String> tail = paths.tail();

        Option<TreeZipper<PackageFileSystemObject<A>>> option = parent.firstChild();

        while ( option.isSome() )
        {
            TreeZipper<PackageFileSystemObject<A>> zipper = option.some();

            if ( zipper.getLabel().getUnixFsObject().path.name().equals( head ) )
            {
                if ( tail.isEmpty() )
                {
                    // We found the parent
                    // TODO: or did this actually find the child?
                    return right( zipper );
                }
                else
                {
                    return find( zipper, tail );
                }
            }

            option = zipper.right();
        }

        return left( p( parent, paths ) );
    }

    public static <A> Ordering compareTreeNodes( Tree<PackageFileSystemObject<A>> a, Tree<PackageFileSystemObject<A>> b )
    {
        return RelativePath.ord.compare( a.root().getUnixFsObject().path, b.root().getUnixFsObject().path );
    }

    private static <A> Tree<PackageFileSystemObject<A>> prettyTree( Tree<PackageFileSystemObject<A>> root )
    {
        List<Tree<PackageFileSystemObject<A>>> forest = root.subForest().
            sort( PackageFileSystem.<A>treeOrd() ).
            map( new F<Tree<PackageFileSystemObject<A>>, Tree<PackageFileSystemObject<A>>>()
            {
                public Tree<PackageFileSystemObject<A>> f( Tree<PackageFileSystemObject<A>> child )
                {
                    return prettyTree( child );
                }
            } );

        return Tree.node( root.root(), forest );
    }

    public PackageFileSystem<A> navigateToRootAndCreatePackageFileSystem( TreeZipper<PackageFileSystemObject<A>> zipper )
    {
        return new UglyPackageFileSystem<A>( zipper.root(), defaultDirectory );
    }

    private static <A> Ord<Tree<PackageFileSystemObject<A>>> treeOrd()
    {
        return ord( curry( PackageFileSystem.<A>compareTreeNodes() ) );
    }

    static <A> F2<Tree<PackageFileSystemObject<A>>, Tree<PackageFileSystemObject<A>>, Ordering> compareTreeNodes()
    {
        return new F2<Tree<PackageFileSystemObject<A>>, Tree<PackageFileSystemObject<A>>, Ordering>()
        {
            public Ordering f( Tree<PackageFileSystemObject<A>> a, Tree<PackageFileSystemObject<A>> b )
            {
                return compareTreeNodes( a, b );
            }
        };
    }

    /**
     * First-order versions
     */
    private class Fs
    {
        F2<String, String, String> last = new F2<String, String, String>()
        {
            public String f( String s, String s1 )
            {
                return s1;
            }
        };

        F<P2<TreeZipper<PackageFileSystemObject<A>>, List<String>>, TreeZipper<PackageFileSystemObject<A>>> createParentsFor =
            new F<P2<TreeZipper<PackageFileSystemObject<A>>, List<String>>, TreeZipper<PackageFileSystemObject<A>>>()
            {
                public TreeZipper<PackageFileSystemObject<A>> f( P2<TreeZipper<PackageFileSystemObject<A>>, List<String>> p2 )
                {
                    return createParentsFor( p2 );
                }
            };

        F<TreeZipper<PackageFileSystemObject<A>>, PackageFileSystem<A>> navigateToRootAndCreatePackageFileSystem =
            new F<TreeZipper<PackageFileSystemObject<A>>, PackageFileSystem<A>>()
            {
                public PackageFileSystem<A> f( TreeZipper<PackageFileSystemObject<A>> treeZipper )
                {
                    return navigateToRootAndCreatePackageFileSystem( treeZipper );
                }
            };

        F2<TreeZipper<PackageFileSystemObject<A>>, Tree<PackageFileSystemObject<A>>, TreeZipper<PackageFileSystemObject<A>>> addChild =
            new F2<TreeZipper<PackageFileSystemObject<A>>, Tree<PackageFileSystemObject<A>>, TreeZipper<PackageFileSystemObject<A>>>()
            {
                public TreeZipper<PackageFileSystemObject<A>> f( TreeZipper<PackageFileSystemObject<A>> parent, Tree<PackageFileSystemObject<A>> node )
                {
                    return addChild( parent, node );
                }
            };

//        public F2<PackageFileSystemObject<A>, PackageFileSystemObject<A>, PackageFileSystemObject<A>> apply_()
//        {
//            return new F2<PackageFileSystemObject<A>, PackageFileSystemObject<A>, PackageFileSystemObject<A>>()
//            {
//                public PackageFileSystemObject<A> f( PackageFileSystemObject<A> node, PackageFileSystemObject<A> p2 )
//                {
//                    return node.apply( p2 );
//                }
//            };
//        }

//        public F<PackageFileSystemObject<A>, PackageFileSystemObject<A>> toP2_()
//        {
//            return new F<PackageFileSystemObject<A>, PackageFileSystemObject<A>>()
//            {
//                public PackageFileSystemObject<A> f( PackageFileSystemObject<A> node )
//                {
//                    return node.toP2();
//                }
//            };
//        }

        F2<PackageFileSystemObject<A>, TreeZipper<PackageFileSystemObject<A>>, TreeZipper<PackageFileSystemObject<A>>> mutateExisting =
            new F2<PackageFileSystemObject<A>, TreeZipper<PackageFileSystemObject<A>>, TreeZipper<PackageFileSystemObject<A>>>()
            {
                public TreeZipper<PackageFileSystemObject<A>> f( PackageFileSystemObject<A> newSettings,
                                                                 TreeZipper<PackageFileSystemObject<A>> nodeTreeZipper )
                {
                    return nodeTreeZipper.modifyLabel(
                        Function.<PackageFileSystemObject<A>, PackageFileSystemObject<A>>constant( newSettings ) );
                }
            };
    }
}
