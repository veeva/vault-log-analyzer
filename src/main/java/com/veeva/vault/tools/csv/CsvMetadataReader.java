/*---------------------------------------------------------------------
 *	Copyright (c) 2020 Veeva Systems Inc.  All Rights Reserved.
 *	This code is based on pre-existing content developed and
 *	owned by Veeva Systems Inc. and may only be used in connection
 *	with the deliverable with which it was provided to Customer.
 *---------------------------------------------------------------------
 */
package com.veeva.vault.tools.csv;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvParser;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import shaded.org.slf4j.Logger;
import shaded.org.slf4j.LoggerFactory;

import java.io.File;
import java.util.*;

public class CsvMetadataReader<T> {
	private static Logger logger = LoggerFactory.getLogger(CsvMetadataReader.class);

	Set<String> fieldNames = null;
	Map<String, String> headerRow;
	CsvSchema readerSchema = null;
	MappingIterator<?> rowIterator = null;
	File inputFile = null;
	Class<T> rowClass;

	public CsvMetadataReader(File inputFile, Class<T> rowClass) throws Exception {
		this.inputFile = inputFile;

		CsvMapper mapper = new CsvMapper();
		mapper.enable(CsvParser.Feature.IGNORE_TRAILING_UNMAPPABLE);

		CsvMapper sourceMapper = new CsvMapper();
		CsvSchema schema = CsvSchema.emptySchema().withHeader();
		MappingIterator<Map<String, String>> headerIterator = sourceMapper.readerFor(Map.class)
				.with(schema)
				.readValues(inputFile);

		headerRow = headerIterator.next();

		headerRow.put("vault_id", "");

		this.rowClass = rowClass;
		rowIterator = mapper.readerFor(rowClass)
				.with(getSchema())
				.readValues(inputFile);
	}

	public <T> List<T> getAllRows() {
		return getRows(null);
	}

	public <T> List<T> getRows(Integer batchLimit) {
		try {
			List<T> result = new ArrayList<>();
			while (rowIterator.hasNext()
					&& ((batchLimit == null)
						|| (batchLimit == 0)
						|| (result.size() < batchLimit))) {
				result.add((T)rowIterator.next());
			}

			return result;
		}
		catch (Exception e) {
			logger.error(e.getMessage());
			e.printStackTrace();
			return null;
		}
	}

	public CsvSchema getSchema() {
		if (readerSchema == null) {
			CsvSchema.Builder builder = new CsvSchema.Builder();
			builder.setUseHeader(true);

			fieldNames = new LinkedHashSet<>();
			for (String fieldname : headerRow.keySet()) {
				builder.addColumn(fieldname);
				fieldNames.add(fieldname);
			}
			readerSchema = builder.build();
		}

		return readerSchema;
	}

	public boolean hasNext() {
		if (rowIterator != null)
			return rowIterator.hasNext();
		else
			return false;
	}

	public Set<String> getFieldNames() {
		return fieldNames;
	}
}
