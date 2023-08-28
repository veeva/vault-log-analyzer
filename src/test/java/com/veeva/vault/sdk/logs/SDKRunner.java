package com.veeva.vault.sdk.logs;

import com.veeva.vault.extension.TestRunHelper;
import com.veeva.vault.tools.api.ApiUsageLog;
import com.veeva.vault.tools.cli.AnalyzerOptions;
import com.veeva.vault.tools.sdk.SdkDebugLog;
import org.junit.jupiter.api.*;

import java.io.File;
import java.io.IOException;

public class SDKRunner {

	@Test
	@DisplayName("Log Analyzer should successfully analyze SDK Debug logs")
	public void testSdkDebugLog() throws IOException {
		String[] testArgs = TestRunHelper.getTestArgs("src\\test\\resources\\input_files\\sdk_debug_analyze.txt");
		AnalyzerOptions analyzerOptions = AnalyzerOptions.loadFromCliArguments(testArgs);
		File inputLogDirectory = analyzerOptions.getInputFile("./logs");
		File outputFile = analyzerOptions.getOutputFile("output.csv");
		SdkDebugLog sdkDebugLogHelper = new SdkDebugLog();
		sdkDebugLogHelper.analyze(inputLogDirectory, outputFile);

		Assertions.assertTrue(outputFile.exists());
	}
}
