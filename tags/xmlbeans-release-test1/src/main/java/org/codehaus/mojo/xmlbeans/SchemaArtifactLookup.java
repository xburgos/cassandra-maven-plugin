package org.codehaus.mojo.xmlbeans;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.StringTokenizer;
import org.apache.maven.plugin.logging.Log;


import org.apache.maven.artifact.Artifact;

public class SchemaArtifactLookup 
{
	
	private Log logger;
	
	private Set artifacts;
	
	public SchemaArtifactLookup(Set projectArtifacts, Log log) 
	{
		artifacts = projectArtifacts;
		logger = log;
		
	}

	/**
	 * Finds an artifact in the list of project artifacts and
	 * returns a casted version of it with extra helper methods.
	 * 
	 * @param string
	 * @return
	 */
	public Artifact find(String string) throws XmlBeansException 
	{
		Artifact result = null;
		ArtifactReference ref = new ArtifactReference(string);
		
		Artifact nextArtifact = null;
		
		for (Iterator i = artifacts.iterator(); i.hasNext(); ) 
		{
			nextArtifact = (Artifact)i.next();
			logger.debug("Looking at " + nextArtifact.getArtifactId());
			if (ref.equals(nextArtifact)) 
			{
				result = nextArtifact;
				logger.debug("Found one.");
				break;
			}
		}
		
		if (result == null) 
		{
			throw new XmlBeansException(XmlBeansException.INVALID_ARTIFACT_REFERENCE, string);
		}
		
		return result;
	}
	
	private class ArtifactReference 
	{
		String groupId;
		String artifactId;
		
		private ArtifactReference(String path) throws XmlBeansException 
		{
		   StringTokenizer tokens = new StringTokenizer(path, ":");
		   if (tokens.countTokens() == 2) 
		   {
			   groupId = tokens.nextToken();
			   artifactId = tokens.nextToken();
		   } else {
			   throw new XmlBeansException(XmlBeansException.INVALID_ARTIFACT_REFERENCE, path);
		   }
		}
		
		public boolean equals(Object candidate) 
		{
			boolean outcome = false;
			if (candidate instanceof Artifact) 
			{
				Artifact artifact = (Artifact) candidate;
				outcome = groupId.equals(artifact.getGroupId()) &&
				  artifactId.equals(artifact.getArtifactId());
			}
			return outcome;
		}
	}
}
