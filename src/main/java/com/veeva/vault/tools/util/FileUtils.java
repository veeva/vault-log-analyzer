package com.veeva.vault.tools.util;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class FileUtils {
	private static Logger logger = LoggerFactory.getLogger(FileUtils.class);

	public static boolean endsWith(File file, String fileExtension) {
		if (file != null && fileExtension != null) {
			fileExtension = fileExtension.replace("*.", ".");

			if (file.getName().toLowerCase().endsWith(fileExtension)) {
				return true;
			}
			else if (fileExtension.equals("*") || fileExtension.equals("*.*")) {
				return true;
			}
		}
		return false;
	}

	public static List<String> getFileNames(File sourceFile, String extension) {
		try (Stream<Path> walk = Files.walk(sourceFile.toPath())) {

			return walk.map(x -> x.toString())
					.filter(file -> (endsWith(new File(file), extension)))
					.collect(Collectors.toList());

		} catch (IOException e) {
			logger.error(e.getMessage());
			return null;
		}
	}

	public static List<File> getFiles(File source, String extension) {
		try {
			List<File> files = new ArrayList<>();
			if (source.isDirectory()) {
				List<String> fileNames = getFileNames(source, extension);
				Collections.sort(fileNames);
				for (String filePath : fileNames) {
					files.add(new File(filePath));
				}
			}
			else {
				files.add(source);
			}

			return files;
		} catch (Exception e) {
			logger.error(e.getMessage());
			return null;
		}
	}

	public static byte[] getResourceContent(String resourcePath) {
		byte[] resourceContent = null;
		ClassLoader classLoader = ClassLoader.getSystemClassLoader();
		if (classLoader != null) {
			URL resourceUrl = classLoader.getResource(resourcePath);
			if (resourceUrl != null) {
				logger.debug("URL: " + resourcePath);
				try {
					InputStream sourceStream = classLoader.getResourceAsStream(resourcePath);
					resourceContent = IOUtils.toByteArray(sourceStream);
				}
				catch (Exception e) {
					logger.error(e.getMessage());
					e.printStackTrace();
				}
			}
			else {
				logger.debug("NOURL: " + resourcePath);
			}
		}
		else {
			logger.debug("NOCLASSLOADER: " + resourcePath);
		}

		return resourceContent;
	}

	public static void makeDirectories(File directory) {
		try {
			if (directory != null && !directory.isFile() && !directory.exists()) {
				logger.debug("MKDIR: " + directory);
				directory.mkdirs();
			}
		}
		catch (Exception e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}

	}

	public static void unzipFiles(File zipFile, File outputDirectory) {
		try {
			byte[] buffer = new byte[1024];
			ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile.getAbsolutePath()));
			ZipEntry zipEntry = zis.getNextEntry();
			while (zipEntry != null) {
				File newFile = new File(outputDirectory, zipEntry.getName());
				String parentDirPath = newFile.getParent();
				File parentDir = new File(parentDirPath);
				makeDirectories(parentDir);

				if (!zipEntry.isDirectory()) {
					FileOutputStream fos = new FileOutputStream(newFile);
					int len;
					while ((len = zis.read(buffer)) > 0) {
						fos.write(buffer, 0, len);
					}
					fos.close();
				}
				logger.info("UNZIP: " + newFile.getAbsolutePath());
				zipEntry = zis.getNextEntry();
			}
			zis.closeEntry();
			zis.close();
		} catch (IOException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
	}

	public static void writeFileContent(File outputFile, byte[] fileContent) {
		try {
			if (outputFile != null && fileContent != null) {
				makeDirectories(outputFile.getParentFile());
				Files.write(outputFile.toPath(), fileContent);
			}
		}
		catch (Exception e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}

	}

	public static String toAbsolutePath(String maybeRelative) {
		Path path = Paths.get(maybeRelative);
		Path effectivePath = path;
		Path base = Paths.get("");
		effectivePath = base.resolve(path).toAbsolutePath();
		return effectivePath.normalize().toString();
	}

	public static boolean checkApiLogFileNameFormat(String fileName) {
		String format = "^[0-9]+-[a-zA-Z]+-\\d{4}-\\d{2}-\\d{2}$";

		return false;
	}
}
