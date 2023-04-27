package com.veeva.vault.tools.sql;

import com.veeva.vault.vapil.api.model.VaultModel;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class Sqlite {

	private final Integer BATCH_SIZE = 10000;

	File dbFile = null;
	Connection conn = null;
	DatabaseMetaData dbMetadata = null;

	int numStatements = 0;
	StringBuilder sqlCachedStatements = new StringBuilder();
	Map<String, StringBuilder> tableToStatementBuilder = new HashMap<>();
	Map<String, AtomicInteger> tableToStatementCount = new HashMap<>();

	public Sqlite(File dbFile) {
		this.dbFile = dbFile;
		try {
			conn = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath());
			dbMetadata = conn.getMetaData();
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
	}

	public void createTable(String tableName, List<String> fieldNames, boolean drop) {
		StringBuilder sqlBuilder = new StringBuilder("");
		if (drop) {
			sqlBuilder.append("DROP TABLE IF EXISTS " + tableName + ";\n");
			sqlBuilder.append("CREATE TABLE " + tableName + " (\n");
		} else {
			sqlBuilder.append("CREATE TABLE IF NOT EXISTS " + tableName + " (\n");
		}

		//sqlBuilder.append("\tlogId INTEGER PRIMARY KEY AUTOINCREMENT,\n");
		int fieldCount = 0;
		for (String fieldName : fieldNames) {
			fieldCount++;
			sqlBuilder.append("\t" + fieldName + " TEXT");
			if (fieldCount < fieldNames.size()) {
				sqlBuilder.append(",");
			}
			sqlBuilder.append("\n");
		}
		sqlBuilder.append(");");

		execute(sqlBuilder.toString());
	}

	private void initializeTableMaps(String table) {
		if(!tableToStatementBuilder.containsKey(table)) {
			tableToStatementBuilder.put(table, new StringBuilder());
			tableToStatementCount.put(table, new AtomicInteger(-1));
		}
	}
	public String startInsertStatement(String tableName, VaultModel data) {
		initializeTableMaps(tableName);
		StringBuilder sqlBuilder = new StringBuilder("INSERT INTO " + tableName + " (");
		sqlBuilder.append(String.join(",", data.getFieldNames()));
		sqlBuilder.append(")\nVALUES ");
		addBuildingStatement(tableName, sqlBuilder.toString());
		tableToStatementCount.get(tableName).set(0);
		return sqlBuilder.toString();
	}
	public String addInsertValues(String table, VaultModel data) {
		StringBuilder sqlBuilder = new StringBuilder("(");
		int fieldCount = 0;
		for (String fieldName : data.getFieldNames()) {
			fieldCount++;
			sqlBuilder.append("\"" + data.get(fieldName) + "\"");
			if (fieldCount < data.getFieldNames().size()) {
				sqlBuilder.append(",");
			}
		}
		sqlBuilder.append(")\n");

		addBuildingStatement(table, sqlBuilder.toString());
		return sqlBuilder.toString();
	}

	public String createInsertStatement(String tableName, VaultModel data) {
		StringBuilder sqlBuilder = new StringBuilder("INSERT INTO " + tableName + " (");
		sqlBuilder.append(String.join(",", data.getFieldNames()));
		sqlBuilder.append(")\nSELECT ");
		int fieldCount = 0;
		for (String fieldName : data.getFieldNames()) {
			fieldCount++;
			sqlBuilder.append("\"" + data.get(fieldName) + "\"");
			if (fieldCount < data.getFieldNames().size()) {
				sqlBuilder.append(",");
			}
		}
		sqlBuilder.append(";\n");

		addStatement(sqlBuilder.toString());
		return sqlBuilder.toString();
	}
	public void addBuildingStatement(String table, String sql) {
		AtomicInteger localNumStatements = tableToStatementCount.get(table);
		StringBuilder localBuilder = tableToStatementBuilder.get(table);
		if (localNumStatements.get()>0) {
			localBuilder.append(",");
		}
		localBuilder.append(sql);
		localNumStatements.incrementAndGet();
	}

	public void addStatement(String sql) {
		sqlCachedStatements.append(sql);
		numStatements++;

		if (numStatements == BATCH_SIZE) {
			execute(sqlCachedStatements.toString());
			sqlCachedStatements.setLength(0);
			numStatements = 0;
		}
	}

	public void flushBuilders() {
		try {
			StringBuilder sb = new StringBuilder();
			sb.append("BEGIN TRANSACTION;\n");
			tableToStatementBuilder.values()
					.stream()
					.map(builder-> builder.append(";\n").toString())
					.forEach(statement-> sb.append(statement));
			sb.append("COMMIT;\n");
			if (sb != null) {
				execute(sb.toString());
				tableToStatementBuilder
						.values()
						.forEach(builder-> builder.setLength(0));
				tableToStatementCount
						.values()
						.forEach(counter-> counter.set(0));
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	public void flush() {
		try {
			if (sqlCachedStatements != null) {
				execute(sqlCachedStatements.toString());
				sqlCachedStatements.setLength(0);
				numStatements = 0;
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	public ResultSet query(String sql) {
		try {
			Statement stmt = conn.createStatement();
			return stmt.executeQuery(sql);
		} catch (SQLException e) {
			System.out.println(e.getMessage());
			return null;
		}
	}

	public void execute(String sql) {
		if (sql != null && !sql.isEmpty()) {
			if (!dbFile.exists()) {
				//createDatabase();
			}

			//System.out.println(dbFile.getAbsolutePath());
			try {
				Statement stmt = conn.createStatement();
				stmt.executeUpdate(sql);
				//stmt.close();
			} catch (SQLException e) {
				System.err.println(e.getMessage());
				e.printStackTrace(System.err);
				System.out.println(sql);
			}
		}
	}

	public List<String> getTableNames() {
		return getTableNames(null);
	}

	public List<String> getTableNames(String search) {
		try {

			StringBuilder sql = new StringBuilder();
			sql.append("SELECT name FROM sqlite_master s where s.type='table'");
			if (search != null && !search.isEmpty()) {
				sql.append(" AND name LIKE '%" + search + "%'");
			}

			List<String> tableNames = new ArrayList<>();
			Statement stmt = conn.createStatement();
			ResultSet resultSet = stmt.executeQuery(sql.toString());
			if (resultSet != null) {
				while (resultSet.next()) {
					String tableName = resultSet.getString("name");
					tableNames.add(tableName);
				}
			}
			return tableNames;
		} catch (SQLException e) {
			System.out.println(e.getMessage());
			return null;
		}
	}

	public VaultModel convertToModel(ResultSet resultSet) {
		try {

			VaultModel model = new VaultModel();
			ResultSetMetaData metaData = resultSet.getMetaData();
			for (int i = 0; i < metaData.getColumnCount(); i++) {
				String fieldName = metaData.getColumnName(i);
				model.set(fieldName, resultSet.getString(fieldName));
			}
			return model;
		} catch (SQLException e) {
			System.out.println(e.getMessage());
			return null;
		}
	}

	public void mergeTables(List<String> sourceTableNames, String targetTable) {
		try {
			StringBuilder sqlBuilder = new StringBuilder();
			sqlBuilder.append("DROP TABLE IF EXISTS " + targetTable + ";\n");
			sqlBuilder.append("CREATE TABLE " + targetTable + " AS\n");
			int tableCount = 0;
			for (String sourceTableName : sourceTableNames) {
				tableCount++;
				if (tableCount > 1) {
					sqlBuilder.append("UNION\n");
				}
				sqlBuilder.append("SELECT * FROM " + sourceTableName + "\n");

			}

			execute(sqlBuilder.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
