package org.codehaus.mojo.xmlbeans;

import java.util.List;
import java.io.File;
import java.io.InputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.jar.JarFile;
import java.util.jar.JarEntry;

import org.apache.maven.plugin.logging.Log;

public class FilteredJarFile extends JarFile 
{

	private Log logger;
	
	public FilteredJarFile(File arg0, Log log) throws IOException 
	{
		super(arg0);
		logger = log;
	}
	
	public List getEntryPathsAndExtract(String[] filter, File prefix) throws IOException 
	{
		final List entries = new ArrayList();
		
		JarEntry nextEntry = null;
		for (Enumeration e = entries(); e.hasMoreElements(); ) 
		{
			nextEntry = ((JarEntry) e.nextElement());
			if (!nextEntry.isDirectory() && !isFiltered(nextEntry.getName(), filter) ) 
			{
				logger.debug("adding and extracting " + nextEntry.getName());
				extractEntry(prefix, nextEntry);
				entries.add(nextEntry.getName());
			}
		}
		return entries;
	}
	
	private boolean isFiltered(final String name, final String[] filter) 
	{
		int size = filter.length;
		if (name != null) {
			for (int i = 0; i < size; i++) 
			{
				if (name.endsWith(filter[i])) 
				{
					logger.debug("Accepting " + name);
					return false;
				} 
			}
			logger.debug("Filtering " + name);
			return true;
		} else {
			logger.debug("Filtering out null.");
			return true;
		}
	}

	/**
	 * Unpack this entry into the given file location.
	 * 
	 * @param prefix The directory to unpack to.
	 * @param entry The entry to unpack.
	 */
	public void extractEntry(File prefix, JarEntry entry) throws IOException 
	{
		File output = new File(prefix, entry.getName());
		output.getParentFile().mkdirs();
		output.createNewFile();
		InputStream ios = null;
		FileOutputStream fos = null;
		try 
		{
			ios = getInputStream(entry);
			fos = new FileOutputStream(output);
			
	        byte[] buf = new byte[8192];
	        while (true) 
	        {
	            int length = ios.read(buf);
	            if (length < 0) break;
	            fos.write(buf, 0, length);
	        }
		}
		finally 
		{
			if (ios != null) 
			{
		        try 
		        {
		        	ios.close();
		        }
		        catch  (IOException ignore) 
		        {
		        }
			}
			if (fos != null) 
			{
		        try 
		        {
		        	fos.close();
		        }
		        catch  (IOException ignore) 
		        {
		        }
			}
		}
        
	}

}
