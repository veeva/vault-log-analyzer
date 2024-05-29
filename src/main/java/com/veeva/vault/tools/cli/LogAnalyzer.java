package com.veeva.vault.tools.cli;

import com.veeva.vault.tools.api.ApiUsageLog;
import com.veeva.vault.tools.sdk.SdkDebugLog;
import com.veeva.vault.tools.sdk.SdkRuntimeLog;
import com.veeva.vault.vapil.api.client.VaultClient;
import shaded.org.slf4j.Logger;
import shaded.org.slf4j.LoggerFactory;

import java.io.File;

public class LogAnalyzer {
	private static Logger logger = LoggerFactory.getLogger(LogAnalyzer.class);

	public static void main(String[] args) {
		AnalyzerOptions analyzerOptions = AnalyzerOptions.loadFromCliArguments(args);

		if (analyzerOptions.getLogType() == null) {
			logger.error("logtype required");
		}
		else {
			switch (analyzerOptions.getLogType().toUpperCase()) {
				case "API":
					switch (analyzerOptions.getAction().toUpperCase()) {
						case "ANALYZE":
							File inputDatabaseFile = analyzerOptions.getInputFile("vault-log-analyzer.db");
							File outputApiUsageResults = analyzerOptions.getOutputFile(true);

							if (inputDatabaseFile == null || outputApiUsageResults == null) {
								logger.error("Unexpected error");
							}
							else if (!inputDatabaseFile.exists()) {
								logger.error("File does not exist [" + inputDatabaseFile.getAbsolutePath() + "]");
							} else if (!outputApiUsageResults.getParentFile().exists()) {
								logger.error("Directory does not exist [" + outputApiUsageResults.getParentFile().getAbsolutePath() + "]");
							}
							else {
								ApiUsageLog apiUsageLogAnalyzer = new ApiUsageLog(inputDatabaseFile);
								apiUsageLogAnalyzer.analyze(outputApiUsageResults);
							}
							break;
						case "IMPORT":
							File inputApiLogDirectory = analyzerOptions.getInputFile("./logs");
							File outputDatabaseFile = analyzerOptions.getOutputFile("vault-log-analyzer.db");

							if (inputApiLogDirectory == null || outputDatabaseFile == null) {
								logger.error("Unexpected error");
							}
							else if (!inputApiLogDirectory.exists()) {
								logger.error("Directory does not exist [" + inputApiLogDirectory.getAbsolutePath() + "]");
							} else if (!outputDatabaseFile.getParentFile().exists()) {
								logger.error("Directory does not exist [" + outputDatabaseFile.getParentFile().getAbsolutePath() + "]");
							}
							else {
								ApiUsageLog apiUsageLogImporter = new ApiUsageLog(outputDatabaseFile);
								apiUsageLogImporter.importLogFiles(inputApiLogDirectory, analyzerOptions);
							}

							break;
						case "DOWNLOAD":
							File outputApiLogDirectory = analyzerOptions.getOutputFile("./logs");
							if (outputApiLogDirectory == null) {
								logger.error("Unexpected error");
							}
							else if (!outputApiLogDirectory.exists()) {
								logger.error("Directory does not exist [" + outputApiLogDirectory.getAbsolutePath() + "]");
							} else {
								String clientId = "veeva-vault-devsupport-client-log-analyzer";
								VaultClient vaultClient = VaultClient
										.newClientBuilder(VaultClient.AuthenticationType.SESSION_ID)
										.withVaultClientId(clientId)
										.withVaultDNS(analyzerOptions.getVaultDNS())
										.withVaultUsername(analyzerOptions.getVaultUsername())
										.withVaultSessionId(analyzerOptions.getVaultSessionId())
										.build();

								ApiUsageLog apiUsageLogDownloader = new ApiUsageLog();
								apiUsageLogDownloader.download(
										vaultClient,
										analyzerOptions.getStartDate(),
										analyzerOptions.getEndDate(),
										outputApiLogDirectory,
										analyzerOptions.getAppend()
								);
							}
							break;
					}
					break;
				case "DEBUG":
					switch (analyzerOptions.getAction().toUpperCase()) {
							case "ANALYZE":
								File inputSdkDebugLog = analyzerOptions.getInputFile("./logs");
								File outputSdkDebugFile = analyzerOptions.getOutputFile(true);
								if (inputSdkDebugLog == null || outputSdkDebugFile == null) {
									logger.error("Unexpected error");
								}
								else if (!inputSdkDebugLog.exists()) {
									logger.error("Directory does not exist [" + inputSdkDebugLog.getAbsolutePath() + "]");
								} else if (!outputSdkDebugFile.getParentFile().exists()) {
									logger.error("Directory does not exist [" + outputSdkDebugFile.getParentFile().getAbsolutePath() + "]");
								}
								else {
									SdkDebugLog sdkDebugLogAnalyzer = new SdkDebugLog();
									sdkDebugLogAnalyzer.analyze(inputSdkDebugLog, outputSdkDebugFile);
								}
							break;
					}
					break;
				case "RUNTIME":
					switch (analyzerOptions.getAction().toUpperCase()) {
						case "DOWNLOAD":
							File outputApiLogDirectory = analyzerOptions.getOutputFile("./logs");
							if (outputApiLogDirectory == null) {
								logger.error("Unexpected error");
							}
							else if (!outputApiLogDirectory.exists()) {
								logger.error("Directory does not exist [" + outputApiLogDirectory.getAbsolutePath() + "]");
							} else {
								String vaultClientId = "verteobiotech-vault-quality-client-myintegration";
								// Instantiate the VAPIL VaultClient using user name and password authentication
								VaultClient vaultClient = VaultClient
										.newClientBuilder(VaultClient.AuthenticationType.BASIC)
										.withVaultClientId(vaultClientId)
										.withVaultDNS(analyzerOptions.getVaultDNS())
										.withVaultUsername(analyzerOptions.getVaultUsername())
										.withVaultSessionId(analyzerOptions.getVaultSessionId())
										.build();

								SdkRuntimeLog sdkRuntimeLogDownloader = new SdkRuntimeLog();
								sdkRuntimeLogDownloader.download(
										vaultClient,
										analyzerOptions.getStartDate(),
										analyzerOptions.getEndDate(),
										outputApiLogDirectory,
										analyzerOptions.getAppend()
								);
							}
							break;
					}
					break;
				default:
					logger.error("unknown logtype [" + analyzerOptions.getLogType().toUpperCase() + "]; expected values = [API, DEBUG, RUNTIME]");
			}
		}
	}
}
