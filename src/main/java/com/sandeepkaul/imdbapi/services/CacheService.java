/**
 * 
 */
package com.sandeepkaul.imdbapi.services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.ConcurrentHashMap;


import com.sandeepkaul.imdbapi.utils.DBUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * @author sandeep.kaul
 *
 */
@Slf4j
public class CacheService extends DBUtils{

	private static ConcurrentHashMap<Integer, String> ccIdToNameMap = new ConcurrentHashMap<Integer, String>();
	private static ConcurrentHashMap<Integer, String> ccTypeIdToNameMap = new ConcurrentHashMap<Integer, String>();
	
	
	
	private static final String LOAD_CAST_CREW_CACHE = "SELECT id,name FROM cast_crew_members;";
	private static final String LOAD_CAST_CREW_TYPE_CACHE = "SELECT id,name FROM cast_crew_type;";
	

	public CacheService() {
		loadCastCrewCache();
		loadCastCrewTypeCache();
	}
	
	
	public static ConcurrentHashMap<Integer, String> getCastCrewMemberIdToNameMap() {
		return ccIdToNameMap;
	}
	
	public static ConcurrentHashMap<Integer, String> getCastCrewMemberTypeIdToTypeNameMap() {
		return ccTypeIdToNameMap;
	}
	
	
	private void loadCastCrewTypeCache() {
		ResultSet resultSet = null;
		Connection connection = null;
		PreparedStatement preparedStatement = null;

		try {
			connection = getConnection();

			preparedStatement = connection.prepareStatement(LOAD_CAST_CREW_TYPE_CACHE);

			resultSet = preparedStatement.executeQuery();
			while (resultSet.next()) {
				int memberId = resultSet.getInt(1);
				String name = resultSet.getString(2);
				ccTypeIdToNameMap.put(memberId,name);
			}
			log.info("CastCrewType Cache loaded:{}",ccTypeIdToNameMap);
		} catch (SQLException e) {
			log.error("SQLExceotion in getting connection from Data Source", e);
			throw new RuntimeException("Something went wrong. loadCastCrewTypeCache failed.");
		} finally {
			closeAll(resultSet, preparedStatement, connection);
		}
	}
	
	private void loadCastCrewCache() {
		
		ResultSet resultSet = null;
		Connection connection = null;
		PreparedStatement preparedStatement = null;

		try {
			connection = getConnection();

			preparedStatement = connection.prepareStatement(LOAD_CAST_CREW_CACHE);

			resultSet = preparedStatement.executeQuery();
			while (resultSet.next()) {
				int memberId = resultSet.getInt(1);
				String name = resultSet.getString(2);
				ccIdToNameMap.put(memberId,name);
			}
			log.info("CastCrewCache loaded: {}",ccIdToNameMap);
		} catch (SQLException e) {
			log.error("SQLExceotion in getting connection from Data Source", e);
			throw new RuntimeException("Something went wrong. loadCastCrewCache failed.");
		} finally {
			closeAll(resultSet, preparedStatement, connection);
		}
	}
}
