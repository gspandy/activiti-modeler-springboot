package com.activiti.conf;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.ibatis.session.SqlSessionFactory;

public class CreateCustomerTable {
	private SqlSessionFactory sqlSessionFactory;

	public CreateCustomerTable(SqlSessionFactory sqlSessionFactory) {
		this.sqlSessionFactory = sqlSessionFactory;
	}

	private boolean isExist(String tableName) throws SQLException {
		DatabaseMetaData metaData = sqlSessionFactory.openSession().getConnection().getMetaData();
		ResultSet tables = null;
		try {
			tables = metaData.getTables(null, null, tableName, new String[] { "TABLE" });
			return tables.next();
		} finally {
			tables.close();
		}
	}

	public void executeSchemaResource(String resourceName) throws IOException, SQLException {
		InputStream inputStream = null;
		if (resourceName == null) {
			inputStream = this.getClass().getClassLoader().getResourceAsStream("dbsql/create.sql");
		} else {
			inputStream = new FileInputStream(resourceName);
		}
		if (inputStream == null) {
			return;
		}
		Connection connection = sqlSessionFactory.openSession().getConnection();
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
		String line = readNextTrimmedLine(reader);
		String sqlStatement = null;
		while (line != null) {
			if (line.length() > 0) {
				if (line.endsWith(";")) {
					sqlStatement = addSqlStatementPiece(sqlStatement, line.substring(0, line.length() - 1));
					// 读取第一行时
					String tableName = sqlStatement.substring(12, sqlStatement.indexOf("(")).trim();
					if (isExist(tableName)) {
						line = readNextTrimmedLine(reader);
						sqlStatement = null;
						continue;
					}
					Statement jdbcStatement = connection.createStatement();
					jdbcStatement.execute(sqlStatement);
					jdbcStatement.close();
					sqlStatement = null;
				} else {
					sqlStatement = addSqlStatementPiece(sqlStatement, line);
				}
			}
			line = readNextTrimmedLine(reader);
		}

	}

	protected String addSqlStatementPiece(String sqlStatement, String line) {
		if (sqlStatement == null) {
			return line;
		}
		return sqlStatement + " \n" + line;
	}

	protected String readNextTrimmedLine(BufferedReader reader) throws IOException {
		String line = reader.readLine();
		if (line != null) {
			line = line.trim();
		}
		return line;
	}
}
