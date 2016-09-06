/**
 * 
 */
package com.sandeepkaul.imdbapi.utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import lombok.extern.slf4j.Slf4j;

/**
 * @author sandeep.kaul
 *
 */
@Slf4j
public class DBUtils {

	protected Connection getConnection() {
		try {
			DataSource dataSource = Constants.DATA_SOURCE;
			Connection connection = dataSource.getDbConnection();
			return connection;
		} catch (SQLException e) {
			log.error("SQLExceotion in getting connection from Data Source", e);
			throw new RuntimeException("SQLExceotion in getting connection from Data Source");
		}
	}

	protected void closeAll(final ResultSet rs, final PreparedStatement preparedStatement,
			final Connection connection) {
		try {
			if (rs != null && !rs.isClosed()) {
				rs.close();
			}
		} catch (Exception e) {
			log.error("Failed to close result set.");
		}
		try {
			if (preparedStatement != null && !preparedStatement.isClosed()) {
				preparedStatement.close();
			}
		} catch (Exception e) {
			log.error("Failed to close preparedStatement.");
		}
		try {
			if (connection != null && !connection.isClosed()) {
				connection.close();
			}
		} catch (Exception e) {
			log.error("Failed to close database connection.");
		}
	}
}
