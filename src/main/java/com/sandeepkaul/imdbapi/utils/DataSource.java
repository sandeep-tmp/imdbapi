/**
 * 
 */
package com.sandeepkaul.imdbapi.utils;

import java.sql.Connection;
import java.sql.SQLException;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import com.sandeepkaul.imdbapi.models.DBConfig;

import io.dropwizard.lifecycle.Managed;

/**
 * @author sandeep.kaul
 *
 */
public class DataSource implements Managed{

	private ComboPooledDataSource cpds;

	public DataSource(DBConfig dbConfig) throws Exception{
		cpds = new ComboPooledDataSource();
	    cpds.setDriverClass("com.mysql.jdbc.Driver");
	    cpds.setJdbcUrl(dbConfig.getDbUrl());
	    cpds.setUser(dbConfig.getDbUser());
	    cpds.setPassword(dbConfig.getDbPassword());
	    cpds.setTestConnectionOnCheckin(dbConfig.getTestConnectionOnCheckin());
	    cpds.setIdleConnectionTestPeriod(dbConfig.getIdleConnectionTestPeriod());
	    cpds.setTestConnectionOnCheckout(dbConfig.getTestConnectionOnCheckout());
	    cpds.setMinPoolSize(dbConfig.getMinPoolSize());
	    cpds.setMaxPoolSize(dbConfig.getMaxPoolSize());
	    cpds.setInitialPoolSize(dbConfig.getInitialPoolSize());
	    cpds.setMaxIdleTime(dbConfig.getMaxIdleTime());
	    cpds.setMaxIdleTimeExcessConnections(dbConfig.getMaxIdleTimeExcessConnections());
	    cpds.setUnreturnedConnectionTimeout(dbConfig.getUnreturnedConnectionTimeout());
	}
	
	public Connection getDbConnection() throws SQLException {
		return cpds.getConnection();
	}
	
	public void start() throws Exception {
	}

	public void stop() throws Exception {
		cpds.close();
	}
	
}
