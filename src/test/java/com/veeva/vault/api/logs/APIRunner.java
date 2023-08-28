package com.veeva.vault.api.logs;

import com.veeva.vault.extension.TestRunHelper;
import com.veeva.vault.tools.api.ApiUsageLog;
import com.veeva.vault.tools.cli.AnalyzerOptions;
import org.junit.jupiter.api.*;

import java.io.File;
import java.io.IOException;

public class APIRunner {

	@Nested
	@DisplayName("Log Analyzer should")
	class testApiUsageLog {

		@Test
		@DisplayName("successfully import API usage logs")
		public void testApiUsageLogImport() throws IOException {
			String[] testArgs = TestRunHelper.getTestArgs("src\\test\\resources\\input_files\\api_import.txt");
			AnalyzerOptions analyzerOptions = AnalyzerOptions.loadFromCliArguments(testArgs);
			File inputApiLogDirectory = analyzerOptions.getInputFile("./logs");
			File outputDatabaseFile = analyzerOptions.getOutputFile("vault-log-analyzer.db");
			ApiUsageLog apiUsageLogAnalyzer = new ApiUsageLog(outputDatabaseFile);
			apiUsageLogAnalyzer.importLogFiles(inputApiLogDirectory, analyzerOptions);

			Assertions.assertTrue(outputDatabaseFile.exists());
		}

		@Test
		@Disabled("ANALYZE action has an error when Sqlite.execute() method runs, but works using the compiled jar from the terminal")
		@DisplayName("successfully analyze generated .db file from logs import")
		public void testApiUsageLogAnalyze() throws IOException {
			String[] testArgs = TestRunHelper.getTestArgs("src\\test\\resources\\input_files\\api_analyze.txt");
			AnalyzerOptions analyzerOptions = AnalyzerOptions.loadFromCliArguments(testArgs);
			File databaseFile = analyzerOptions.getInputFile("vault-log-analyzer.db");
			File outputFile = analyzerOptions.getOutputFile("output.csv");
			ApiUsageLog apiUsageLogAnalyzer = new ApiUsageLog(outputFile);
			apiUsageLogAnalyzer.analyze(databaseFile);

			Assertions.assertTrue(outputFile.exists());
		}
	}
}
