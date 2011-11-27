package org.codehaus.mojo.unix.maven;

import org.codehaus.mojo.unix.MissingSettingException;
import org.codehaus.mojo.unix.UnixPackage;
import org.codehaus.mojo.unix.maven.rpm.RpmUnixPackage;
import org.codehaus.plexus.util.StringUtils;

/**
 * @author <a href="mailto:trygvis@codehaus.org">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
class RpmMojoHelper
    extends MojoHelper
{
    private RpmSpecificSettings rpm;

    public RpmMojoHelper( RpmSpecificSettings rpm )
    {
        this.rpm = rpm;
    }

    protected void validateMojoSettings()
        throws MissingSettingException
    {
        if ( rpm == null )
        {
            throw new MissingSettingException( "You need to specify the required properties when building rpm packages." );
        }

        if ( StringUtils.isEmpty( rpm.getSoftwareGroup() ) )
        {
            throw new MissingSettingException( "softwareGroup" );
        }
    }

    protected void applyFormatSpecificSettingsToPackage( UnixPackage unixPackage )
    {
        RpmUnixPackage.cast( unixPackage ).
            group( rpm.getSoftwareGroup() );
    }
}