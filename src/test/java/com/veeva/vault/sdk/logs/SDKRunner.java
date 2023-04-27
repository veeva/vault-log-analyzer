package com.veeva.vault.sdk.logs;

import com.veeva.vault.tools.sdk.SdkDebugLog;

import java.io.File;

public class SDKRunner {
	public static void main(String[] args) {
		String rootFolderPath = "";

		String logFolderPath = rootFolderPath + "logs/";
		File logDir = new File(logFolderPath);

		String outputFilePath = rootFolderPath + "output.csv";
		File outputFile = new File(outputFilePath);

		SdkDebugLog sdkDebugLogHelper = new SdkDebugLog();
		sdkDebugLogHelper.analyze(logDir, outputFile);
	}
}
