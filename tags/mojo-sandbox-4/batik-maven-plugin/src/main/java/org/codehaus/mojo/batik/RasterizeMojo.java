package org.codehaus.mojo.batik;

/*
 * Copyright 2006 Mark Hobson.
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

import java.awt.Color;
import java.io.File;
import java.util.Map;
import java.util.Vector;

import org.apache.batik.apps.rasterizer.DestinationType;
import org.apache.batik.apps.rasterizer.SVGConverter;
import org.apache.batik.apps.rasterizer.SVGConverterController;
import org.apache.batik.apps.rasterizer.SVGConverterException;
import org.apache.batik.apps.rasterizer.SVGConverterSource;
import org.apache.batik.transcoder.Transcoder;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.plexus.util.DirectoryScanner;
import org.codehaus.plexus.util.StringUtils;

/**
 * Rasterize SVGs.
 * 
 * @author	Mark Hobson <markhobson@gmail.com>
 * @version	$Id$
 * @goal		rasterize
 * @phase	generate-resources
 */
public class RasterizeMojo
    extends AbstractMojo
    implements SVGConverterController
{
    // parameters -------------------------------------------------------------

    /**
     * The directory containing the SVG files.
     * 
     * @parameter expression="${project.build.sourceDirectory}"
     * @required
     */
    private File srcDir;

    /**
     * The file pattern for inclusion of SVG files.
     * 
     * @parameter default-value="**\/*.svg";
     * @required
     */
    private String srcIncludes;

    /**
     * The file pattern for exclusion of SVG files.
     * 
     * @parameter
     */
    private String srcExcludes;

    /**
     * The directory to write the rasterized SVG files.
     * 
     * @parameter expression="${project.build.outputDirectory}"
     * @required
     */
    private File destDir;

    /**
     * The type of file to convert to. Valid values are <code>png</code>,
     * <code>jpeg</code>, <code>tiff</code> or <code>pdf</code>.
     * 
     * @parameter default-value = "png"
     * @required
     */
    private String destType;
    
    /**
     * The output width, or -1 to not constrain the width.
     * 
     * @parameter default-value = "-1"
     */
    private float width;

    /**
     * The output height, or -1 to not constrain the height.
     * 
     * @parameter default-value = "-1"
     */
    private float height;

    /**
     * The maximum output width, or -1 to not constrain the maximum width.
     * 
     * @parameter default-value = "-1"
     */
    private float maxWidth;

    /**
     * The maximum output height, or -1 to not constrain the maximum height.
     * 
     * @parameter default-value = "-1"
     */
    private float maxHeight;

    /**
     * The PNG index value.  Only used when destination type is
     * <code>png</code>.  Valid values are 1, 2, 4 or 8 bits, or -1 to not
     * index.
     * 
     * @parameter default-value = "-1"
     */
    private int indexed;

    /**
     * The JPEG encoding quality.  Only used when destination type is
     * <code>jpeg</code>.  Valid values are between 0.0 and 0.99 inclusive, or
     * -1 to disable setting the quality.
     * 
     * @parameter default-value = "-1"
     */
    private float quality;

    /**
     * The background color.  Valid values are of the form <code>#rrggbb</code>.
     * 
     * @parameter
     */
    private String background;

    // Mojo methods -----------------------------------------------------------

    /*
     * @see org.apache.maven.plugin.Mojo#execute()
     */
    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        // get src files
        String[] src = getSrc( srcDir, srcIncludes, srcExcludes );
        if ( src.length == 0 )
        {
            getLog().info( "No SVG files found" );
            return;
        }

        // build converter
        SVGConverter converter = new SVGConverter( this );
        converter.setSources( src );
        converter.setDst( destDir );
        converter.setDestinationType( toDestinationType( destType ) );
        converter.setWidth( width );
        converter.setHeight( height );
        converter.setMaxWidth( maxWidth );
        converter.setMaxHeight( maxHeight );
        converter.setIndexed( indexed );
        converter.setQuality( quality );
        if ( background != null )
        {
            converter.setBackgroundColor( toColor( background ) );
        }

        // log message
        StringBuffer buffer = new StringBuffer();
        buffer.append( "Rasterizing " ).append( src.length ).append( " image" );
        if ( src.length > 1 )
            buffer.append( "s" );
        buffer.append( " from SVG to " );
        buffer.append( converter.getDestinationType().toString() );
        getLog().info( buffer.toString() );

        // convert svgs
        try
        {
            converter.execute();
        }
        catch ( SVGConverterException exception )
        {
            throw new MojoExecutionException( "Cannot rasterize SVGs", exception );
        }
    }

    // SVGConverterController methods -----------------------------------------

    /*
     * @see org.apache.batik.apps.rasterizer.SVGConverterController#proceedWithComputedTask(org.apache.batik.transcoder.Transcoder, java.util.Map, java.util.Vector, java.util.Vector)
     */
    public boolean proceedWithComputedTask( Transcoder transcoder, Map hints, Vector sources, Vector dest )
    {
        return true;
    }

    /*
     * @see org.apache.batik.apps.rasterizer.SVGConverterController#proceedWithSourceTranscoding(org.apache.batik.apps.rasterizer.SVGConverterSource, java.io.File)
     */
    public boolean proceedWithSourceTranscoding( SVGConverterSource source, File dest )
    {
        return true;
    }

    /*
     * @see org.apache.batik.apps.rasterizer.SVGConverterController#proceedOnSourceTranscodingFailure(org.apache.batik.apps.rasterizer.SVGConverterSource, java.io.File, java.lang.String)
     */
    public boolean proceedOnSourceTranscodingFailure( SVGConverterSource source, File dest, String errorCode )
    {
        // unfortunately SVGConverter always dumps the stack trace here without
        // allowing us to handle it more gracefully..
        
        getLog().warn( "Cannot rasterize SVG " + source.getName() + ": " + errorCode );

        return false;
    }

    /*
     * @see org.apache.batik.apps.rasterizer.SVGConverterController#onSourceTranscodingSuccess(org.apache.batik.apps.rasterizer.SVGConverterSource, java.io.File)
     */
    public void onSourceTranscodingSuccess( SVGConverterSource source, File dest )
    {
        getLog().debug( "Rasterized SVG " + source.getName() );
    }

    // private methods --------------------------------------------------------

    /**
     * Gets files within the specified directory according to the given
     * patterns.
     * 
     * @param directory the base directory that contains the files
     * @param includes the fileset pattern to match for inclusion 
     * @param excludes the fileset pattern to match for exclusion  
     * @return an array of matched filenames 
     */
    private String[] getSrc( File directory, String includes, String excludes )
    {
        DirectoryScanner scanner = new DirectoryScanner();
        scanner.setBasedir( directory );
        scanner.setIncludes( StringUtils.split( includes, "," ) );
        if ( excludes != null )
            scanner.setExcludes( StringUtils.split( excludes, "," ) );

        scanner.scan();

        String[] files = scanner.getIncludedFiles();
        for ( int i = 0; i < files.length; i++ )
            files[i] = new File( directory, files[i] ).toString();

        return files;
    }

    /**
     * Gets the <code>DestinationType</code> for the specified destination type
     * string.
     * 
     * @param destinationType the destination type string, either
     *        <code>png</code>, <code>jpeg</code>, <code>tiff</code> or
     *        <code>pdf</code>
     * @return the corresponding <code>DestinationType</code> instance
     * @throws MojoExecutionException if an invalid destination type string was
     *         specified
     */
    private DestinationType toDestinationType( String destinationType )
        throws MojoExecutionException
    {
        if ( "png".equals( destinationType ) )
            return DestinationType.PNG;
        if ( "jpeg".equals( destinationType ) )
            return DestinationType.JPEG;
        if ( "tiff".equals( destinationType ) )
            return DestinationType.TIFF;
        if ( "pdf".equals( destinationType ) )
            return DestinationType.PDF;
        throw new MojoExecutionException( "Invalid destination type: " + destinationType );
    }

    /**
     * Gets the <code>Color</code> for the specified color string.
     * 
     * @param color the color string, in the format <code>#rrggbb</code>
     * @return the corresponding <code>Color</code> instance
     * @throws MojoExecutionException if an invalid color string was specified
     */
    private Color toColor( String color )
        throws MojoExecutionException
    {
        if ( !color.startsWith( "#" ) || color.length() != 7 )
            throw new MojoExecutionException( "Color must be of the form #rrggbb: " + color );

        int r = Integer.parseInt( color.substring( 1, 3 ), 16 );
        int g = Integer.parseInt( color.substring( 3, 5 ), 16 );
        int b = Integer.parseInt( color.substring( 5 ), 16 );

        return new Color( r, g, b );
    }
}
