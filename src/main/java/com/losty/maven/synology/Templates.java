package com.losty.maven.synology;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URL;

public class Templates {

	private final static ClassLoader classLoader = Templates.class.getClassLoader();
	
	private File baseDir;
	
	public Templates(File baseDir) {
		this.baseDir = baseDir;
	}
	
	public URL getTemplate(String file) {
		return classLoader.getResource("templates/"+file);
	}
	
	public URL getExternalOrTemplate(String filename, String externalFilename) throws MalformedURLException, FileNotFoundException {
		if (externalFilename != null) {
			File externalFile = new File(baseDir, externalFilename);
			if (externalFile.exists()) {
				return externalFile.toURI().toURL();
			} else {
				throw new FileNotFoundException(filename);
			}
		} else {
			return getTemplate(filename);
		}
	}
	
}
