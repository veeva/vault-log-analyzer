package com.veeva.vault.tools.cli;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.veeva.vault.vapil.api.model.VaultModel;
import shaded.org.slf4j.Logger;
import shaded.org.slf4j.LoggerFactory;
import org.json.JSONObject;

import java.io.File;
import java.nio.file.FileSystems;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AnalyzerOptions extends VaultModel {
	private static Logger logger = LoggerFactory.getLogger(AnalyzerOptions.class);

	//------------------------------------------------------------------------------------------------
	// Log Type: Expected Values [API,DEBUG,RUNTIME]
	//------------------------------------------------------------------------------------------------

	@JsonProperty("logType")
	@JsonAlias("logtype")
	public String getLogType() { return this.getString("logType"); }
	public void setLogType(String logType) { this.set("logType", logType); }

	//------------------------------------------------------------------------------------------------
	// Action to perform: Expected Values [ANALYZE,DOWNLOAD,IMPORT]
	//------------------------------------------------------------------------------------------------

	@JsonProperty("action")
	public String getAction() {
		String action = this.getString("action");
		if (action == null || action.isEmpty()) {
			return "ANALYZE";
		}
		return action;
	}
	public void setAction(String action) { this.set("action", action); }

	//------------------------------------------------------------------------------------------------
	// Vault ID
	//------------------------------------------------------------------------------------------------

	@JsonProperty("vaultId")
	@JsonAlias("vaultid")
	public String getVaultId() {return this.getString("vaultId");}
	public void setVaultId(String vaultId) { this.set("vaultId", vaultId); }

	//------------------------------------------------------------------------------------------------
	// Vault API Details
	//------------------------------------------------------------------------------------------------
	@JsonProperty("vaultDNS")
	@JsonAlias({"vaultdns"})
	public String getVaultDNS() { return this.getString("vaultDNS"); }
	public void setVaultDNS(String vaultDNS) { this.set("vaultDNS", vaultDNS); }

	@JsonProperty("username")
	public String getVaultUsername() { return this.getString("username"); }
	public void setVaultUsername(String username) { this.set("username", username); }

	@JsonProperty("sessionId")
	@JsonAlias("sessionid")
	public String getVaultSessionId() { return this.getString("sessionId"); }
	public void setVaultSessionId(String sessionId) { this.set("sessionId", sessionId); }

	//------------------------------------------------------------------------------------------------
	// Input and Output
	//------------------------------------------------------------------------------------------------

	@JsonProperty("input")
	public String getInput() { return this.getString("input"); }
	public void setInput(String input) { this.set("input", input); }

	@JsonIgnore
	public File getInputFile(String defaultInput) {
		String input = getInput();
		if (input != null) {
			String inputFilePath = FileSystems.getDefault().getPath(input).normalize().toAbsolutePath().toString();
			return new File(inputFilePath);
		}
		else if (defaultInput != null) {
			String inputFilePath = FileSystems.getDefault().getPath(defaultInput).normalize().toAbsolutePath().toString();
			return new File(inputFilePath);
		}

		return null;
	}

	@JsonProperty("output")
	public String getOutput() { return this.getString("output"); }
	public void setOutput(String output) { this.set("output", output); }

	@JsonIgnore
	public File getOutputFile(String defaultOutput) {
		String output = getOutput();
		if (output != null) {
			String outputFilePath = FileSystems.getDefault().getPath(output).normalize().toAbsolutePath().toString();
			return new File(outputFilePath);
		}
		else if (defaultOutput != null) {
			String outputFilePath = FileSystems.getDefault().getPath(defaultOutput).normalize().toAbsolutePath().toString();
			return new File(outputFilePath);
		}
		return null;
	}

	@JsonIgnore
	public File getOutputFile(boolean useDefaultOutputFileName) {
		if (useDefaultOutputFileName) {
			StringBuilder outputFileNameBuilder = new StringBuilder("vault-log-analyzer");
			outputFileNameBuilder.append("-");
			outputFileNameBuilder.append(this.getLogType().toLowerCase());
			outputFileNameBuilder.append("-");
			DateTimeFormatter utcFormatter = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmssSSS");
			outputFileNameBuilder.append(LocalDateTime.now().format(utcFormatter));
			outputFileNameBuilder.append(".csv");

			return getOutputFile(outputFileNameBuilder.toString());

		}
		return null;
	}

	//------------------------------------------------------------------------------------------------
	// Append to File: not implemented
	//------------------------------------------------------------------------------------------------

	@JsonProperty("append")
	public Boolean getAppend() { return this.getBoolean("append"); }
	public void setAppend(String append) {
		if (append != null) {
			this.set("append", Boolean.valueOf(append));
		}
	}

	//------------------------------------------------------------------------------------------------
	// Unzip files after API Usage Download
	//------------------------------------------------------------------------------------------------

	@JsonProperty("unzip")
	public Boolean getUnzip() { return this.getBoolean("unzip"); }
	public void setUnzip(String unzip) {
		if (unzip != null) {
			this.set("unzip", Boolean.valueOf(unzip));
		}
		else {
			this.set("unzip", true);
		}
	}

	//------------------------------------------------------------------------------------------------
	// Start and End Date for API Usage Download
	//------------------------------------------------------------------------------------------------

	@JsonProperty("startDate")
	@JsonAlias("startdate")
	public LocalDate getStartDate() { return (LocalDate)this.get("startDate"); }
	public void setStartDate(String startDate) {
		if (startDate != null) {
			this.set("startDate", LocalDate.parse(startDate));
		}
	}

	@JsonProperty("endDate")
	@JsonAlias("enddate")
	public LocalDate getEndDate() { return (LocalDate)this.get("endDate"); }
	public void setEndDate(String endDate) {
		if (endDate != null) {
			this.set("endDate", LocalDate.parse(endDate));
		}
	}

	//------------------------------------------------------------------------------------------------
	public static AnalyzerOptions loadFromCliArguments(String[] cliArguments) {
		try {
			JSONObject jsonParams = new JSONObject();
			if (cliArguments != null) {
				String key = null;
				String value = null;
				for (int i = 0; i < cliArguments.length; i++) {
					String buffer = cliArguments[i];
					boolean isKey = buffer.startsWith("-");
					if (isKey) {
						key = buffer.substring(1).toLowerCase();
						value = null;
					} else {
						value = buffer;
					}

					if (key != null && value != null) {
						jsonParams.put(key, value);
					}
				}
			}

			ObjectMapper mapper = new ObjectMapper();
			mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
			AnalyzerOptions analyzerOptions = mapper.readValue(jsonParams.toString(), AnalyzerOptions.class);
			return analyzerOptions;
		}
		catch (Exception e) {
			logger.error(e.getMessage());
			e.printStackTrace();
			return null;
		}
	}
}
