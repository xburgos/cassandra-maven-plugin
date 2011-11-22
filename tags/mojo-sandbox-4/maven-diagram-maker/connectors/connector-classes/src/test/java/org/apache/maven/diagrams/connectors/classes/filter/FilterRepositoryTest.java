package org.apache.maven.diagrams.connectors.classes.filter;

import java.util.LinkedList;
import java.util.List;

import junit.framework.TestCase;

public class FilterRepositoryTest extends TestCase
{

    public void testGetStatus1()
    {
        ClassNamesFilter classNamesFilter=new ClassNamesFilter();
        classNamesFilter.setIncludes( IncludePattern.createList( new String[]{"org\\.apache\\.maven\\.\\*","java\\.lang\\..*"} ));
 
        
        List<ExcludePattern> exclude=new LinkedList<ExcludePattern>();
        exclude.add( new ExcludePattern(".*\\.Object") );
        exclude.add( new ExcludePattern(".*\\.reflect\\..*",true) );
        classNamesFilter.setExcludes( exclude );
        
        FilterRepository filterRepository=new FilterRepository(classNamesFilter);
        assertEquals( ClassFilterStatus.EXCLUDED_WITHOUT_KEEP_EDGES, filterRepository.getStatus( "java.lang.Object" ));
        assertEquals( ClassFilterStatus.EXCLUDED_WITH_KEEP_EDGES, filterRepository.getStatus( "java.lang.reflect.Array"));
        assertEquals( ClassFilterStatus.NOT_INCLUDED, filterRepository.getStatus( "org.apache.all.All" ));
    }

}
