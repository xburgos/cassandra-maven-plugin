package org.apache.maven.diagrams.connectors.classes;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.List;

import junit.framework.TestCase;

public class PackageUtilsTest extends TestCase
{
    public void test() throws MalformedURLException, URISyntaxException
    {
        List<String> s=PackageUtils.getClassNamesOnClassPathItem(new File("/home/ptab/gsoc/Maveny_dev/plugins/maven-assembly-plugin/target/classes").toURI().toURL());
        System.out.println(s);
        System.out.println(s.size());
    }
}
