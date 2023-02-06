/*---------------------------------------------------------------------
 *	Copyright (c) 2020 Veeva Systems Inc.  All Rights Reserved.
 *	This code is based on pre-existing content developed and
 *	owned by Veeva Systems Inc. and may only be used in connection
 *	with the deliverable with which it was provided to Customer.
 *---------------------------------------------------------------------
 */
package com.veeva.vault.tools.csv;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.dataformat.csv.CsvGenerator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.veeva.vault.tools.util.FileUtils;
import org.apache.log4j.Logger;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

public class CsvMetadataWriter<T> {
	private static Logger logger = Logger.getLogger(CsvMetadataWriter.class);

	private CsvSchema.Builder outputSchemaBuilder;


	public CsvMetadataWriter() {
		outputSchemaBuilder = CsvSchema.builder();
	}

	public void addColumn(String fieldName) {
		outputSchemaBuilder.addColumn(fieldName);
	}

	public CsvSchema.Builder getOutputSchemaBuilder() {
		return outputSchemaBuilder;
	}

	public void writeHeader(boolean useHeader, boolean appendToFile, File outputFile, Object outputRows) {
		try {
			CsvMapper outputMapper = new CsvMapper();
			outputMapper.configure(JsonGenerator.Feature.IGNORE_UNKNOWN, true);
			outputMapper.configure(CsvGenerator.Feature.ALWAYS_QUOTE_STRINGS, true);

			outputSchemaBuilder.setUseHeader(useHeader);
			FileUtils.makeDirectories(outputFile.getParentFile());
			ObjectWriter resultsWriter = outputMapper.writer(outputSchemaBuilder.build());
			FileOutputStream tempFileOutputStream = new FileOutputStream(outputFile, appendToFile);
			BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(tempFileOutputStream, 1024);
			OutputStreamWriter writerOutputStream = new OutputStreamWriter(bufferedOutputStream, StandardCharsets.UTF_8);
			resultsWriter.writeValue(writerOutputStream, outputRows);
		}
		catch (Exception e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
	}

	public void writeAllRows(boolean useHeader, boolean appendToFile, File outputFile, Object outputRows) {
		try {
			CsvMapper outputMapper = new CsvMapper();
			outputMapper.configure(JsonGenerator.Feature.IGNORE_UNKNOWN, true);
			outputMapper.configure(CsvGenerator.Feature.ALWAYS_QUOTE_STRINGS, true);
			outputSchemaBuilder.setUseHeader(useHeader);
			outputSchemaBuilder.setNullValue("");
			FileUtils.makeDirectories(outputFile.getParentFile());
			ObjectWriter resultsWriter = outputMapper.writer(outputSchemaBuilder.build());
			FileOutputStream tempFileOutputStream = new FileOutputStream(outputFile, appendToFile);
			BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(tempFileOutputStream, 1024);
			OutputStreamWriter writerOutputStream = new OutputStreamWriter(bufferedOutputStream, StandardCharsets.UTF_8);
			resultsWriter.writeValue(writerOutputStream, outputRows);
		}
		catch (Exception e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
	}
}
