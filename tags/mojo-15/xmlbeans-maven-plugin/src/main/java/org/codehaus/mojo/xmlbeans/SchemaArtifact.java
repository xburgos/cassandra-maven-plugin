package org.codehaus.mojo.xmlbeans;

import java.io.File;
import java.util.Iterator;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.ArrayList;
import java.util.jar.JarFile;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.handler.ArtifactHandler;
import org.apache.maven.artifact.versioning.VersionRange;
import org.apache.maven.plugin.logging.Log;

public class SchemaArtifact {

	private static String[] XSD_SUFFIXES = {"xsd", "XSD"};
	
	/**
	 * Assuming that the artifact has a file handle, returns a collection of strings
	 * pointing to each xsd file within the jar.
	 * 
	 * @return Collection of xsd file paths relative to the jar file.
	 */
	public static Collection getFilePaths(Artifact artifact, Log logger, File prefix)
	throws XmlBeansException {
		List xsds = new ArrayList();
		File artifactFile = artifact.getFile();
		List nextSet = null;
		if (artifactFile != null) {
			try {
				FilteredJarFile jarFile = new FilteredJarFile(artifactFile, logger);

				nextSet = jarFile.getEntryPathsAndExtract(XSD_SUFFIXES, prefix);
				
				for (Iterator i = nextSet.iterator(); i.hasNext(); ) {
					xsds.add(new File(prefix, (String)i.next()));
				}
				
			} catch (IOException ioe) {
				throw new XmlBeansException(XmlBeansException.XSD_ARTIFACT_JAR, ioe);
				
			}
		} else {
			throw new XmlBeansException(XmlBeansException.ARTIFACT_FILE_PATH, artifact.toString());
		}
		
		return xsds;
	}

}
