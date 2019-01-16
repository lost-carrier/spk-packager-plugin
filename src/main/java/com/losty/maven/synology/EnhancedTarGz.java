package com.losty.maven.synology;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.apache.commons.compress.utils.Charsets;
import org.apache.commons.compress.utils.IOUtils;

public class EnhancedTarGz {

	private TarGz tarGz;
	
	public EnhancedTarGz(TarGz tarGz) {
		this.tarGz = tarGz;
	}
	
	public long addTextFile(String filename, String content) throws IOException {
		return addTextFile(filename, content, null);
	}

	@SuppressWarnings("deprecation") // ...cause whole Java 6 is deprecated!!!
	public long addTextFile(String filename, String content, Integer mode) throws IOException {
		byte[] bytes = content.getBytes(Charsets.UTF_8);
		return tarGz.putFile(new ByteArrayInputStream(bytes), filename, bytes.length, mode);
	}

	public void addFileDirect(String file, URL source) throws IOException {
		addFileDirect(file, source, null);
	}
	
	public void addFileDirect(String filename, URL source, Integer mode) throws IOException {
		InputStream is = null;
		try {
			is = source.openStream();
			byte[] content = IOUtils.toByteArray(is);
			tarGz.putFile(new ByteArrayInputStream(content), filename, content.length, mode);
		} finally {
			IOUtils.closeQuietly(is);
		}
	}
	
}
