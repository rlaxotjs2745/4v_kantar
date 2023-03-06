package com.kantar.util;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Path;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import org.mozilla.universalchardet.UniversalDetector;

@Component
public class FileEncode {
	public static String findFileEncoding(String fileName) throws Exception {
		File file = new File(fileName);
		Path path = file.toPath();
		String encoding = UniversalDetector.detectCharset(path);
		if (encoding != null) {
			System.out.println("Detected encoding = " + encoding);
		} else {
			System.out.println("No encoding detected.");
		}
		return encoding;
	}

	public static String findFileEncoding(File file) throws Exception {
		String encoding = UniversalDetector.detectCharset(file);
		if (encoding != null) {
			System.out.println("Detected encoding = " + encoding);
		} else {
			System.out.println("No encoding detected.");
		}
		return encoding;
	}

	public static String findFileEncoding(MultipartFile file) throws Exception {
		InputStream fis = file.getInputStream();

		String encoding = UniversalDetector.detectCharset(fis);
		if (encoding != null) {
			System.out.println("Detected encoding = " + encoding);
		} else {
			System.out.println("No encoding detected.");
		}
		return encoding;
	}
}