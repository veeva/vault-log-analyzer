package com.veeva.vault.tools.sdk;

import com.veeva.vault.tools.util.FileUtils;
import com.veeva.vault.vapil.api.client.VaultClient;
import com.veeva.vault.vapil.api.model.response.VaultResponse;
import com.veeva.vault.vapil.api.request.LogRequest;
import shaded.org.slf4j.Logger;
import shaded.org.slf4j.LoggerFactory;

import java.io.File;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class SdkRuntimeLog {
	private Logger logger = LoggerFactory.getLogger(SdkRuntimeLog.class);

	public void download(VaultClient vaultClient, LocalDate startDate, LocalDate endDate, File outputDirectory, Boolean unZipAfterDownload) {
		try {
			//default startdate to today when null
			if (startDate == null) {
				startDate = LocalDate.now(ZoneId.of("UTC"));
			}

			//default enddate to today when null
			if (endDate == null) {
				endDate = LocalDate.now(ZoneId.of("UTC"));
			}

			//default unzip to true when null
			if (unZipAfterDownload == null) {
				unZipAfterDownload = true;
			}

			LocalDate logDate = startDate;
			while (!logDate.isAfter(endDate)) {
				VaultResponse response = vaultClient.newRequest(LogRequest.class).downloadSdkRuntimeLog(logDate);

				//build the file name since the API does not include the name in the response
				//this is the Vault default form:
				// 		{vaultId}=APIUsageLog-{YYYY-MM-DD}.zip			12345=APIUsageLog-2022-01-15.zip
				StringBuilder logFilePath = new StringBuilder(outputDirectory.getAbsolutePath() + "/");
				logFilePath.append(response.getHeaderVaultId());
				logFilePath.append("-SDKLog-");
				logFilePath.append(DateTimeFormatter.ofPattern("yyyy-MM-dd").format(logDate));
				logFilePath.append(".zip");


				File logFile = new File(logFilePath.toString());
				FileUtils.makeDirectories(logFile.getParentFile());
				FileUtils.writeFileContent(logFile, response.getBinaryContent());

				if (unZipAfterDownload) {
					FileUtils.unzipFiles(logFile,logFile.getParentFile());
				}

				logDate = logDate.plusDays(1);
			}
		}
		catch (Exception e) {
			logger.error(e.getMessage());
		}
	}
}
