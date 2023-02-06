package com.veeva.vault.tools.sdk;

import com.veeva.vault.tools.csv.CsvMetadataWriter;
import com.veeva.vault.tools.util.FileUtils;
import org.apache.log4j.Logger;

import java.io.*;
import java.nio.file.FileSystems;
import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.util.*;

public class SdkDebugLog {
	private Logger logger = Logger.getLogger(SdkDebugLog.class);

	private final int BATCH_SIZE = 500;

	/**
	 * Accepts an input file (log file or directory of log files) to parse
	 * and creates a CSV of log analysis in output file
	 *
	 * @param inputFile log directory or single log file
	 * @param outputFile output csv
	 */
	public void analyze(File inputFile, File outputFile) {
		try {
			if (inputFile == null) {
				String defaultInputFilePath = FileSystems
						.getDefault()
						.getPath("/logs")
						.normalize()
						.toAbsolutePath()
						.toString();
				inputFile = new File(defaultInputFilePath);
			}

			if (outputFile == null) {
				String defaultOutputFileName = new SimpleDateFormat("vault-log-analyzer-debug-yyyyMMdd-HHmmssSSS")
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

			if (outputFile.exists()) {
				logger.warn("Deleting existing file [" + outputFile + "]");
				outputFile.delete();
			}



			if (inputFile.isDirectory()) {
				List<File> files = FileUtils.getFiles(inputFile, ".txt");
				int numFiles = 0;
				for (File logFile : files) {
					numFiles++;
					analyzeLogFile(logFile, outputFile);
				}
			}
			else {
				analyzeLogFile(inputFile, outputFile);
			}
		}
		catch (Exception e) {
			logger.error(e.getMessage());
		}
	}

	private void analyzeLogFile(File logFile, File outputFile) {
		try {
			logger.info("Parsing " + logFile.getAbsolutePath());

			CsvMetadataWriter csvMetadataWriter = new CsvMetadataWriter();
			csvMetadataWriter.addColumn("timestamp");
			csvMetadataWriter.addColumn("log_file");
			csvMetadataWriter.addColumn("type");
			csvMetadataWriter.addColumn("category");
			csvMetadataWriter.addColumn("class_name");
			csvMetadataWriter.addColumn("service_method");
			csvMetadataWriter.addColumn("service_name");
			csvMetadataWriter.addColumn("method_name");
			csvMetadataWriter.addColumn("elapsed_time");
			csvMetadataWriter.addColumn("elapsed_time_seconds");
			csvMetadataWriter.addColumn("cpu_time");
			csvMetadataWriter.addColumn("cpu_time_seconds");
			csvMetadataWriter.addColumn("memory");
			csvMetadataWriter.addColumn("memory_mb");
			csvMetadataWriter.addColumn("gross_memory");
			csvMetadataWriter.addColumn("gross_memory_mb");
			csvMetadataWriter.addColumn("invocation_count");
			csvMetadataWriter.addColumn("message");

			List<SdkDebugLogEntry> sdkDebugLogEntries = new ArrayList<>();
			BufferedReader reader = new BufferedReader(new FileReader(logFile));
			String lineBuffer = reader.readLine();
			if (lineBuffer != null) {
				while (lineBuffer != null) {
					SdkDebugLogEntry sdkDebugLogEntry = transformLine(lineBuffer, reader);
					sdkDebugLogEntry.setLogFile(logFile.getName());
					sdkDebugLogEntries.add(sdkDebugLogEntry);

					if (sdkDebugLogEntries.size() == BATCH_SIZE) {
						csvMetadataWriter.writeAllRows(!outputFile.exists(), outputFile.exists(), outputFile, sdkDebugLogEntries);
						sdkDebugLogEntries.clear();
					}

					lineBuffer = reader.readLine();
				}

				if (sdkDebugLogEntries.size() > 0) {
					csvMetadataWriter.writeAllRows(!outputFile.exists(), outputFile.exists(), outputFile, sdkDebugLogEntries);
				}
			}
		}
		catch (Exception e) {
			logger.error(e.getMessage());
		}
	}

	private SdkDebugLogEntry transformLine(String lineBuffer, BufferedReader reader) {
		try {
			SdkDebugLogEntry sdkDebugLogEntry = new SdkDebugLogEntry();

			sdkDebugLogEntry.setTimestamp(lineBuffer.substring(0, 23));

			String tempBuffer = lineBuffer.substring(24);
			sdkDebugLogEntry.setClassName(tempBuffer.substring(0, tempBuffer.indexOf(" ")));

			tempBuffer = tempBuffer.substring(tempBuffer.indexOf(" ") + 1);
			sdkDebugLogEntry.setType(tempBuffer.substring(0, tempBuffer.indexOf(" ")));

			tempBuffer = tempBuffer.substring(tempBuffer.indexOf(" ") + 1).trim();

			switch (sdkDebugLogEntry.getType()) {
				case "SYSERR":
					sdkDebugLogEntry.setCategory("EXCEPTION");
					StringBuilder exceptionBuilder = new StringBuilder();
					String errorBuffer = tempBuffer;
					while (errorBuffer != null) {
						exceptionBuilder.append(errorBuffer + "\n");
						errorBuffer = reader.readLine();
					}
					sdkDebugLogEntry.setMessage(exceptionBuilder.toString());
					break;
				case "SYSWRN":
					sdkDebugLogEntry.setCategory("ALERT");
					StringBuilder warningBuilder = new StringBuilder();
					String warningBuffer = tempBuffer;
					while (warningBuffer != null) {
						warningBuilder.append(warningBuffer + "\n");
						warningBuffer = reader.readLine();
					}
					sdkDebugLogEntry.setMessage(warningBuilder.toString());
					break;
				case "SYSINFO":
					if (tempBuffer.startsWith("*****Start Execution")) {
						sdkDebugLogEntry.setCategory("ENTRY_POINT_START");
						sdkDebugLogEntry.setClassName(lineBuffer.substring(lineBuffer.indexOf("[") + 1, lineBuffer.indexOf("]")));
					} else if (tempBuffer.startsWith("*****End Execution")) {
						sdkDebugLogEntry.setCategory("ENTRY_POINT_END");
						sdkDebugLogEntry.setClassName(lineBuffer.substring(lineBuffer.indexOf("[") + 1, lineBuffer.indexOf("]")));
					} else if (tempBuffer.startsWith("HttpRequest:") || tempBuffer.startsWith("HttpResponse:")) {
						sdkDebugLogEntry.setCategory("HTTPSERVICE");
						sdkDebugLogEntry.setMessage(tempBuffer);
					} else {
						sdkDebugLogEntry.setCategory("SERVICE");
						if (tempBuffer.startsWith("com.veeva.vault")) {
							sdkDebugLogEntry.setServiceMethod(tempBuffer.substring(0, tempBuffer.indexOf(" - [")));

							String tempPerf = tempBuffer.substring(tempBuffer.indexOf(" - [") + 4, tempBuffer.length() - 1);
							List<String> metrics = Arrays.asList(tempPerf.split(", "));
							for (String metric : metrics) {
								if (metric.startsWith("count")) {
									sdkDebugLogEntry.setInvocationCount(Long.valueOf(metric.substring(6)));
								} else if (metric.startsWith("elapsed")) {
									sdkDebugLogEntry.setElapsedTime(Long.valueOf(metric.substring(12)));
								} else if (metric.startsWith("CPU")) {
									sdkDebugLogEntry.setCpuTime(Long.valueOf(metric.substring(8)));
								} else if (metric.startsWith("memory")) {
									sdkDebugLogEntry.setMemory(Long.valueOf(metric.substring(10)));
								} else if (metric.startsWith("grossMemory")) {
									sdkDebugLogEntry.setGrossMemory(Long.valueOf(metric.substring(15)));
								}
							}
						}
						else {
							sdkDebugLogEntry.setMessage(tempBuffer);
						}
					}

					break;
				case "PERF":
					if (lineBuffer.contains("\"")) {
						sdkDebugLogEntry.setCategory("LOGSERVICE");
						sdkDebugLogEntry.setMessage(tempBuffer.substring(1, tempBuffer.indexOf("\":")));
						tempBuffer = tempBuffer.substring(tempBuffer.indexOf("\":") + 2);
					} else {
						sdkDebugLogEntry.setCategory("SYSPERF");
					}

					List<String> metrics = Arrays.asList(tempBuffer.split(" "));
					for (String metric : metrics) {
						if (metric.startsWith("elapsed")) {
							sdkDebugLogEntry.setElapsedTime(Long.valueOf(metric.substring(12)));
						} else if (metric.startsWith("CPU")) {
							sdkDebugLogEntry.setCpuTime(Long.valueOf(metric.substring(8)));
						} else if (metric.startsWith("memory")) {
							sdkDebugLogEntry.setMemory(Long.valueOf(metric.substring(10)));
						} else {
							//message = tempBuffer;
						}
					}

					break;
				case "DEBUG":
				case "ERROR":
				case "INFO":
				case "WARN":
					sdkDebugLogEntry.setCategory("LOGSERVICE");
					sdkDebugLogEntry.setMessage(tempBuffer);
					break;
			}

			return sdkDebugLogEntry;
		}
		catch (Exception e) {
			logger.error(e.getMessage());
			e.printStackTrace();
			return null;
		}
	}
}
