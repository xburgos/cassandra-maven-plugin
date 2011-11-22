package org.codehaus.mojo.tools.rpm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;

import org.apache.maven.artifact.versioning.InvalidVersionSpecificationException;

public class VersionRangeParserTest
    extends TestCase
{

    public void testMultipleRanges_ShouldFailWhenUpperOverlapsWithAnotherLower()
    {
        String range = "[1.0,2.0],[2.0,3.0]";

        try
        {
            new VersionRangeFormatter().getRpmVersionRestrictions( range );
            fail( "should fail when an upper bound overlaps with another lower bound." );
        }
        catch ( InvalidVersionSpecificationException e )
        {
            // expected
        }
    }

    public void testMultipleRanges_ShouldFailWhenUpperOverlapsWithAnotherLower_NonInclUpper()
    {
        String range = "[1.0,2.0),[2.0,3.0]";

        try
        {
            new VersionRangeFormatter().getRpmVersionRestrictions( range );
            fail( "should fail when an upper bound overlaps with another lower bound." );
        }
        catch ( InvalidVersionSpecificationException e )
        {
            // expected
        }
    }

    public void testMultipleRanges_ShouldNotFailWhenUpperAbutsAnotherLower_NoInclusiveOverlap()
        throws InvalidVersionSpecificationException
    {
        String range = "[1.0,2.0),(2.0,3.0]";
        
        Set expected = new HashSet();
        
        expected.add( VersionRangeFormatter.RPM_VERSION_EQUAL_OR_GREATER_THAN + "1.0" );
        expected.add( VersionRangeFormatter.RPM_VERSION_LESS_THAN + "2.0" );
        expected.add( VersionRangeFormatter.RPM_VERSION_GREATER_THAN + "2.0" );
        expected.add( VersionRangeFormatter.RPM_VERSION_EQUAL_OR_LESS_THAN + "3.0" );

        List restrictions = new VersionRangeFormatter().getRpmVersionRestrictions( range );
        
        assertTrue( expected.containsAll( restrictions ) );
        
        restrictions.removeAll( expected );
        assertTrue( restrictions.isEmpty() );
    }

    public void testSanityChecks_LowerBoundNonInclusive_UpperBoundaryInclusivenessNotSpecified()
    {
        String range = "(1.0,2.0";

        try
        {
            new VersionRangeFormatter().sanityCheckRange( range );
            fail( "must throw an error on invalid range syntax." );
        }
        catch ( InvalidVersionSpecificationException e )
        {
            // expected
        }
    }

    public void testSanityChecks_LowerBoundInclusivenessNotSpecified_UpperBoundNonInclusive()
    {
        String range = "1.0,2.0)";

        try
        {
            new VersionRangeFormatter().sanityCheckRange( range );
            fail( "must throw an error on invalid range syntax." );
        }
        catch ( InvalidVersionSpecificationException e )
        {
            // expected
        }
    }

    public void testSingleRange_LowerBoundInclusive_UpperBoundEmptyNonInclusive()
        throws InvalidVersionSpecificationException
    {
        String range = "[1.0,)";

        Set result = new HashSet();
        result.add( " >= 1.0" );

        assertParseSingleRange( range, result );
    }

    public void testSingleRange_LowerBoundNonInclusive_UpperBoundNonInclusive()
        throws InvalidVersionSpecificationException
    {
        String range = "(1.0,2.0)";

        Set result = new HashSet();
        result.add( " > 1.0" );
        result.add( " < 2.0" );

        assertParseSingleRange( range, result );
    }

    public void testSingleRange_LowerBoundNonInclusive_UpperBoundInclusive()
        throws InvalidVersionSpecificationException
    {
        String range = "(1.0,2.0]";

        Set result = new HashSet();
        result.add( " > 1.0" );
        result.add( " <= 2.0" );

        assertParseSingleRange( range, result );
    }

    public void testSingleRange_SingleVersionWithNoInclusivenessRules()
        throws InvalidVersionSpecificationException
    {
        String range = "1.0";

        List result = new ArrayList();

        new VersionRangeFormatter().parseRange( range, result, new HashMap(), new HashMap() );

        assertEquals( 0, result.size() );
    }

    public void testSingleRange_SingleCompletelyConstrainedVersion()
        throws InvalidVersionSpecificationException
    {
        String range = "[1.0]";

        Set result = new HashSet();
        result.add( " = 1.0" );

        assertParseSingleRange( range, result );
    }

    private void assertParseSingleRange( String versionRange, Set expected )
        throws InvalidVersionSpecificationException
    {
        List result = new ArrayList();

        new VersionRangeFormatter().parseRange( versionRange, result, new HashMap(), new HashMap() );

        assertTrue( expected.containsAll( result ) );
        
        result.removeAll( expected );
        assertTrue( result.isEmpty() );
    }

}
