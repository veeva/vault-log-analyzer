package com.veeva.vault.tools.api;

import com.veeva.vault.tools.csv.CsvMetadataReader;
import com.veeva.vault.tools.csv.CsvMetadataWriter;
import com.veeva.vault.tools.sql.Sqlite;
import com.veeva.vault.tools.util.FileUtils;
import com.veeva.vault.vapil.api.client.VaultClient;
import com.veeva.vault.vapil.api.model.VaultModel;
import com.veeva.vault.vapil.api.model.response.VaultResponse;
import com.veeva.vault.vapil.api.request.LogRequest;
import edu.emory.mathcs.backport.java.util.Arrays;
import org.apache.log4j.Logger;

import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ApiUsageLog {
	private Logger logger = Logger.getLogger(ApiUsageLog.class);

	private final int BATCH_SIZE = 500;

	File dbFile = null;

	Sqlite sqlDb = null;

	public ApiUsageLog() {
	}

	public ApiUsageLog(File dbFile) {
		this.dbFile = dbFile;
		sqlDb = new Sqlite(dbFile);
	}

	public void analyze(File outputFile) {
		try {
			if (outputFile == null) {
				String defaultOutputFileName = new SimpleDateFormat("vault-log-analyzer-api-yyyyMMdd-HHmmssSSS")
						.format(ZonedDateTime.now())
						+ ".csv";
				String defaultOutputFilePath = FileSystems
						.getDefault()
						.getPath(defaultOutputFileName)
						.normalize()
						.toAbsolutePath()
						.toString();
				outputFile = new File(defaultOutputFilePath);
			}

			String sql = new String(FileUtils.getResourceContent("api/stats.sql"));
			sqlDb.execute(sql);

			CsvMetadataWriter csvMetadataWriter = new CsvMetadataWriter();
			List<VaultModel> statRows = new ArrayList<>();

			ResultSet resultSet = sqlDb.query("SELECT * FROM stats");
			if (resultSet != null) {
				List<String> fieldNames = new ArrayList<>();

				boolean addHeader = true;
				boolean append = false;
				int rowCount = 0;
				while (resultSet.next()) {
					rowCount++;
					VaultModel model = new VaultModel();
					statRows.add(model);

					if (rowCount == 1) {
						ResultSetMetaData metaData = resultSet.getMetaData();
						for (int i = 1; i <= metaData.getColumnCount(); i++) {
							String fieldName = metaData.getColumnName(i);
							fieldNames.add(fieldName);
							csvMetadataWriter.addColumn(fieldName);
						}
					}

					for (String fieldName : fieldNames) {
						model.set(fieldName, resultSet.getString(fieldName));
					}

					if (statRows.size() == BATCH_SIZE) {
						csvMetadataWriter.writeAllRows(addHeader, append, outputFile, statRows);
						statRows.clear();
						addHeader = false;
						append = true;
					}
				}

				if (statRows.size() > 0) {
					csvMetadataWriter.writeAllRows(addHeader, append, outputFile, statRows);
				}
			}

		}
		catch (Exception e) {
			logger.error(e.getMessage());
		}
	}

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
				VaultResponse response = vaultClient.newRequest(LogRequest.class).retrieveDailyAPIUsage(logDate);

				//build the file name since the API does not include the name in the response
				//this is the Vault default form:
				// 		{vaultId}=APIUsageLog-{YYYY-MM-DD}.zip			12345=APIUsageLog-2022-01-15.zip
				StringBuilder logFilePath = new StringBuilder(outputDirectory.getAbsolutePath() + "/");
				logFilePath.append(response.getHeaderVaultId());
				logFilePath.append("-APIUsageLog-");
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

	public void importLogFiles(File logDirectory) {
		try {
			List<File> logFiles = FileUtils.getFiles(logDirectory, ".csv");

			int fileCount = 0;
			for (File apiLogFile : logFiles) {
				fileCount++;

				long numLines = Files.lines(apiLogFile.toPath()).count();
				if (numLines > 1) {
					long totalBatches = (numLines + BATCH_SIZE - 1) / BATCH_SIZE;

					CsvMetadataReader apiLogReader = new CsvMetadataReader(apiLogFile, VaultModel.class);
					String sqlTableName = "vaultApi" + apiLogFile.getName().replace("-", "").replace(".csv", "");

					boolean createdTable = false;
					int batchCount = 0;

					Map<Long, Boolean> percentMap = new HashMap<>();
					while (apiLogReader.hasNext()) {
						batchCount++;

						long percent = 100 - ((batchCount * 100) / totalBatches);
						if (!percentMap.keySet().contains(percent)) {
							percentMap.put(percent, true);

							StringBuilder progressBuilder = new StringBuilder();
							progressBuilder.append(apiLogFile.getName());
							for (int i = 0; i < percent; i++) {
								progressBuilder.append("_");
							}
							logger.info(progressBuilder.toString());
						}

						List<VaultModel> apiLogEntries = apiLogReader.getRows(BATCH_SIZE);
						if (apiLogEntries != null && apiLogEntries.size() > 0) {
							transform(apiLogEntries);
							if (!createdTable) {
								sqlDb.createTable(sqlTableName, apiLogEntries.get(0).getFieldNames(), true);
								sqlDb.createTable("api", apiLogEntries.get(0).getFieldNames(), false);
								createdTable = true;
							}
							loadToSql(sqlDb, sqlTableName, apiLogEntries);
						}
					}
				}
			}

		}
		catch (Exception e) {
			logger.error(e.getMessage());
		}
	}

	private void loadToSql(Sqlite sqlDb, String tableName, List<VaultModel> apiLogEntries) {
		try {
			if (apiLogEntries != null && apiLogEntries.size() > 0) {
				for (VaultModel apiLogEntry : apiLogEntries) {
					sqlDb.createInsertStatement(tableName, apiLogEntry);
					sqlDb.createInsertStatement("api", apiLogEntry);
				}
				sqlDb.flush();
			}
		}
		catch (Exception e) {
			logger.error(e.getMessage());
		}
	}

	private void transform(List<VaultModel> apiLogEntries) {
		try {
			for (VaultModel apiLogEntry : apiLogEntries) {

				//preparing 23R1 features
				if (!apiLogEntry.getFieldNames().contains("connection")) {
					apiLogEntry.set("connection", "23R1-Feature");
				}
				if (!apiLogEntry.getFieldNames().contains("api_resource")) {
					apiLogEntry.set("api_resource", "23R1-Feature");
				}

				String apiEndpoint = apiLogEntry.getString("endpoint");
				if (apiEndpoint != null) {
					StringBuilder apiResourceBuilder = new StringBuilder();
					if (apiEndpoint.contains("/") && apiEndpoint.length() > 1) {
						List<String> parts = Arrays.asList(apiEndpoint.split("/"));

						int idCount = 0;
						String lastPart = null;
						for (String part : parts) {
							if (!part.isEmpty()) {
								String tempPart = part;
								if (lastPart != null && lastPart.equals("api") && part.startsWith("v")) {
									tempPart = "{version}";
								}
								else if (!part.contains("__") && part.matches(".*\\d.*")) {
									idCount++;
									tempPart = (idCount > 1) ? "{id" + idCount + "}" : "{id}";

								}
								apiResourceBuilder.append("/" + tempPart);
								lastPart = part;
							}
						}
						apiEndpoint = apiResourceBuilder.toString();
					}
				}
				apiLogEntry.set("api_endpoint", apiEndpoint);
			}
		}
		catch (Exception e) {
			logger.error(e.getMessage());
		}
	}
}
