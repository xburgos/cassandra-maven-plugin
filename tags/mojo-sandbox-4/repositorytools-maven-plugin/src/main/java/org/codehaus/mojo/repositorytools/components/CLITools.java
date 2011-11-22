package org.codehaus.mojo.repositorytools.components;

import java.io.File;
import java.util.List;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;

public interface CLITools
{

	static final String ROLE = CLITools.class.getName();
	
	/**
	 * format id::layout::url,id::layout::url,...
	 * @param repositories
	 * @return
	 * @throws RepositoryToolsException
	 */
	List createRemoteRepositories(String repositories) throws RepositoryToolsException;

	/**
	 * format id::layout::url
	 * 
	 * @param repository
	 * @return
	 * @throws RepositoryToolsException
	 */
	ArtifactRepository createRemoteRepository(String repository) throws RepositoryToolsException;
	
	ArtifactRepository createLocalRepository(File location);

	/**
	 * 
	 * @param id
	 *            the artifact as groupId:artifactId:version
	 * @param type
	 * 				the type of the artifact ("pom", "jar", ...)
	 * @return
	 * @throws RepositoryToolsException
	 */
	Artifact createArtifact(String id, String type) throws RepositoryToolsException;

	
}
