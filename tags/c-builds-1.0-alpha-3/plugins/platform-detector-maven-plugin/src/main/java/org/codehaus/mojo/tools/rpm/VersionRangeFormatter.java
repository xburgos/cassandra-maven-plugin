package org.codehaus.mojo.tools.rpm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.maven.artifact.versioning.InvalidVersionSpecificationException;

/**
 * @plexus.component role="org.codehaus.mojo.tools.rpm.VersionRangeFormatter" role-hint="default"
 * @author jdcasey
 */
public class VersionRangeFormatter
{

    public static final String RPM_VERSION_IS_EXACTLY = " = ";

    public static final String RPM_VERSION_EQUAL_OR_GREATER_THAN = " >= ";

    public static final String RPM_VERSION_EQUAL_OR_LESS_THAN = " <= ";

    public static final String RPM_VERSION_GREATER_THAN = " > ";

    public static final String RPM_VERSION_LESS_THAN = " < ";

    public List getRpmVersionRestrictions( String version )
        throws InvalidVersionSpecificationException
    {
        sanityCheckRange( version );
        
        List rpmVersionRestrictions = new ArrayList();
        
        // used to look for overlaps.
        Map rawLowerBoundsWithInclusiveness = new HashMap();
        Map rawUpperBoundsWithInclusiveness = new HashMap();

        String process = version;

        while ( process.startsWith( "[" ) || process.startsWith( "(" ) )
        {
            int index1 = process.indexOf( ")" );
            int index2 = process.indexOf( "]" );

            int index = index2;
            if ( index2 < 0 || index1 < index2 )
            {
                if ( index1 >= 0 )
                {
                    index = index1;
                }
            }

            if ( index < 0 )
            {
                throw new InvalidVersionSpecificationException( "Unbounded range: " + version );
            }

            parseRange( process.substring( 0, index + 1 ), rpmVersionRestrictions, rawLowerBoundsWithInclusiveness, rawUpperBoundsWithInclusiveness );

            process = process.substring( index + 1 ).trim();

            if ( process.length() > 0 && process.startsWith( "," ) )
            {
                process = process.substring( 1 ).trim();
            }
        }

        checkForOverlaps( rawLowerBoundsWithInclusiveness, rawUpperBoundsWithInclusiveness, version );
        
        if ( process.length() > 0 && rpmVersionRestrictions.size() > 0 )
        {
            throw new InvalidVersionSpecificationException(
                                                            "Only fully-qualified sets allowed in multiple set scenario: "
                                                                            + version );
        }

        return rpmVersionRestrictions;
    }
    
    protected void sanityCheckRange( String version )
        throws InvalidVersionSpecificationException
    {
        // sanity checks:
        int startRangeCheckIdx = version.indexOf( '[' );
        if ( startRangeCheckIdx < 0 )
        {
            startRangeCheckIdx = version.indexOf( '(' );
        }
        
        int endRangeCheckIdx = version.indexOf( ']' );
        if ( endRangeCheckIdx < 0 )
        {
            endRangeCheckIdx = version.indexOf( ')' );
        }
        
        if ( startRangeCheckIdx > -1 && endRangeCheckIdx < 0 )
        {
            throw new InvalidVersionSpecificationException( "Version range has start but no end: " + version );
        }
        else if ( startRangeCheckIdx < 0 && endRangeCheckIdx > -1 )
        {
            throw new InvalidVersionSpecificationException( "Version range has end but no start: " + version );
        }
    }

    protected void checkForOverlaps( Map rawLowerBoundsWithInclusiveness, Map rawUpperBoundsWithInclusiveness, String originalVersion )
        throws InvalidVersionSpecificationException
    {
        for ( Iterator it = rawUpperBoundsWithInclusiveness.keySet().iterator(); it.hasNext(); )
        {
            String upperBound = (String) it.next();

            for ( Iterator itLower = rawLowerBoundsWithInclusiveness.keySet().iterator(); itLower.hasNext(); )
            {
                String lowerBound = (String) itLower.next();

                int comparison = lowerBound.compareTo( upperBound );
                
                if ( comparison > 0 )
                {
                    throw new InvalidVersionSpecificationException( "Ranges overlap: " + originalVersion );
                }
                else if ( comparison == 0 )
                {
                    Boolean upperInclusive = (Boolean) rawUpperBoundsWithInclusiveness.get( upperBound );
                    Boolean lowerInclusive = (Boolean) rawLowerBoundsWithInclusiveness.get( lowerBound );
                    
                    // if neither one is inclusive, there's no overlap.
                    if ( Boolean.FALSE.equals( upperInclusive ) && Boolean.FALSE.equals( lowerInclusive ) )
                    {
                        // this is fine.
                        continue;
                    }
                    else
                    {
                        throw new InvalidVersionSpecificationException( "Ranges overlap: " + originalVersion );
                    }
                }
            }
        }
    }

    protected void parseRange( String range, List rpmVersionRestrictions, Map rawLowerBoundsWithInclusiveness, Map rawUpperBoundsWithInclusiveness )
        throws InvalidVersionSpecificationException
    {
        boolean lowerBoundInclusive = range.startsWith( "[" );
        boolean upperBoundInclusive = range.endsWith( "]" );

        String process = range.substring( 1, range.length() - 1 ).trim();

        int index = process.indexOf( "," );

        if ( index < 0 )
        {
            if ( !lowerBoundInclusive && !upperBoundInclusive )
            {
                // this is a "normal" version, such as '1.0'...no RPM restrictions are placed from 
                // these.
                return;
            }
            else if ( !lowerBoundInclusive || !upperBoundInclusive )
            {
                throw new InvalidVersionSpecificationException( "Single version must be surrounded by []: " + range );
            }

            rpmVersionRestrictions.add( RPM_VERSION_IS_EXACTLY + process );
            rawLowerBoundsWithInclusiveness.put( process, Boolean.valueOf( lowerBoundInclusive ) );
            rawUpperBoundsWithInclusiveness.put( process, Boolean.valueOf( upperBoundInclusive ) );
        }
        else
        {
            String lowerBound = process.substring( 0, index ).trim();
            String upperBound = process.substring( index + 1 ).trim();

            if ( lowerBound.length() < 1 )
            {
                lowerBound = null;
            }

            if ( upperBound.length() < 1 )
            {
                upperBound = null;
            }

            if ( lowerBound != null && upperBound != null )
            {
                if ( lowerBound.equals( upperBound ) )
                {
                    throw new InvalidVersionSpecificationException( "Range cannot have identical boundaries: " + range );
                }

                if ( upperBound.compareTo( lowerBound ) < 0 )
                {
                    throw new InvalidVersionSpecificationException( "Range defies version ordering: " + range );
                }
            }

            if ( lowerBound != null )
            {
                if ( lowerBoundInclusive )
                {
                    rpmVersionRestrictions.add( RPM_VERSION_EQUAL_OR_GREATER_THAN + lowerBound );
                }
                else
                {
                    rpmVersionRestrictions.add( RPM_VERSION_GREATER_THAN + lowerBound );
                }

                rawLowerBoundsWithInclusiveness.put( lowerBound, Boolean.valueOf( lowerBoundInclusive ) );
            }

            if ( upperBound != null )
            {
                if ( upperBoundInclusive )
                {
                    rpmVersionRestrictions.add( RPM_VERSION_EQUAL_OR_LESS_THAN + upperBound );
                }
                else
                {
                    rpmVersionRestrictions.add( RPM_VERSION_LESS_THAN + upperBound );
                }

                rawUpperBoundsWithInclusiveness.put( upperBound, Boolean.valueOf( upperBoundInclusive ) );
            }
        }
    }
}
