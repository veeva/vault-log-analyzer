package com.veeva.vault.api.logs;

import com.veeva.vault.tools.api.ApiUsageLog;

import java.io.File;

public class APIRunner {
	public static void main(String[] args) {
		String rootFolderPath = "/example/";

		String logFolderPath = rootFolderPath + "logs/";
		File logDir = new File(logFolderPath);

		String outputFilePath = rootFolderPath + "output.csv";
		File outputFile = new File(outputFilePath);

		ApiUsageLog apiUsageLogAnalyzer = new ApiUsageLog(outputFile);
		apiUsageLogAnalyzer.importLogFiles(logDir);
		apiUsageLogAnalyzer.analyze(outputFile);
	}
}
