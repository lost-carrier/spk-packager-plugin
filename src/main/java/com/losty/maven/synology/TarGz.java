package com.losty.maven.synology;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.utils.IOUtils;

public class TarGz {

	private OutputStream out;
	private BufferedOutputStream bos;
	private GZIPOutputStream gos;
	private TarArchiveOutputStream tar;
	
	public TarGz(File file) throws IOException {
		
		out = new FileOutputStream(file);
		bos = new BufferedOutputStream(out);
		gos = new GZIPOutputStream(bos);
		tar = new TarArchiveOutputStream(gos);
		
	}
	
	public long putFile(File externalFile, String inTarGz) throws IOException {
		return putFile(externalFile, inTarGz, null);
	}

	public long putFile(File externalFile, String inTarGz, Integer mode) throws IOException {
		
        TarArchiveEntry entry = new TarArchiveEntry(inTarGz);
        entry.setSize(externalFile.length());
        tar.putArchiveEntry(entry);
        if (mode != null) {
        	entry.setMode(mode);
        }
        IOUtils.copy(new FileInputStream(externalFile), tar);
        tar.closeArchiveEntry();
        return entry.getSize();
        
	}
	
	public long putFile(InputStream externalFile, String inTarGz, long length) throws IOException {
		return putFile(externalFile, inTarGz, length, null);
	}
	
	public long putFile(InputStream externalFile, String inTarGz, long length, Integer mode) throws IOException {
		
        TarArchiveEntry entry = new TarArchiveEntry(inTarGz);
        entry.setSize(length);
        if (mode != null) {
        	entry.setMode(mode);
        }
        tar.putArchiveEntry(entry);
        IOUtils.copy(externalFile, tar);
        tar.closeArchiveEntry();
        return entry.getSize();
        
	}
	
	public void close() {
		
		if (tar != null) try {
			tar.finish();
			tar.close();
		} catch (IOException e) {}
		
		if (gos != null) try {
			gos.close();
		} catch (IOException e) {}

		if (bos != null) try {
			bos.close();
		} catch (IOException e) {}

		if (out != null) try {
			out.close();
		} catch (IOException e) {}

	}
	
}
