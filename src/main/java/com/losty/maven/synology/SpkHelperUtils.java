package com.losty.maven.synology;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.compress.utils.IOUtils;

public class SpkHelperUtils {

	public static String md5(File file) throws FileNotFoundException, IOException, NoSuchAlgorithmException {
		MessageDigest md = MessageDigest.getInstance("MD5");
		InputStream is = null;
		DigestInputStream dis = null;
		try {
			is = new FileInputStream(file);
			dis = new DigestInputStream(is, md);
			byte[] buf = new byte[1024];
			while (dis.read(buf) > 0) {}
		} finally {
			IOUtils.closeQuietly(dis);
			IOUtils.closeQuietly(is);
		}
		byte[] digest = md.digest();
		return Hex.encodeHexString(digest);
	}
	
	
	public static String readToString(URL template) throws IOException {
		InputStream is = null;
		try {
			is = template.openStream();
			return new String(IOUtils.toByteArray(is));
		} finally {
			is.close();
		}
	}
	
}
