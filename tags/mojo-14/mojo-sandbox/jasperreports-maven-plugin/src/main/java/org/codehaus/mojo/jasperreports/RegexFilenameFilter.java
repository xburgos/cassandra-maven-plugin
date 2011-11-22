package org.codehaus.mojo.jasperreports;

import java.io.File;
import java.io.FileFilter;
import java.util.regex.Pattern;

/**
 * @author gjoseph
 * @author $Author: $ (last edit)
 * @version $Revision: $
 */
class RegexFilenameFilter
    implements FileFilter
{
    private final Pattern pattern;

    RegexFilenameFilter( Pattern pattern )
    {
        this.pattern = pattern;
    }

    public boolean accept( File pathname )
    {
        if ( pathname.isDirectory() )
        {
            return true;
        }
        return pattern.matcher( pathname.getName() ).matches();
    }

}
