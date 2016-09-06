/**
 * 
 */
package com.sandeepkaul.imdbapi.services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.sandeepkaul.imdbapi.models.User;
import com.sandeepkaul.imdbapi.utils.DBUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * @author sandeep.kaul
 *
 */
@Slf4j
public class UserService extends DBUtils {

	//Skipping Password related fields.
	private static final String SELECT_USER_BY_ID = "SELECT id, user_name, email, phone_number, created_on,updated_on from users where id = ?;";
	
	private static final String CREATE_USER = "INSERT INTO users (user_name, email, phone_number) VALUES (?,?,?);";
	
	private static final String UPDATE_USER = "UPDATE users set phone_number=? where id=?;";
	
	public User createUser(User user) {
		
		ResultSet resultSet = null;
		Connection connection = null;
		PreparedStatement preparedStatement = null;

		try {
			connection = getConnection();

			preparedStatement = connection.prepareStatement(CREATE_USER, Statement.RETURN_GENERATED_KEYS);
			preparedStatement.setString(1, user.getUserName());
			preparedStatement.setString(2, user.getEmail());
			preparedStatement.setString(3, user.getPhoneNumber());

			int updatedCount = preparedStatement.executeUpdate();
			if (updatedCount != 1) {
				throw new RuntimeException("Something went wrong. Insert failed.");
			}
			resultSet = preparedStatement.getGeneratedKeys();
			if (resultSet.next()) {
				int lastInsertedId = resultSet.getInt(1);
				user.setId(lastInsertedId);
			}
		} catch (SQLException e) {
			log.error("SQLExceotion in getting connection from Data Source", e);
			throw new RuntimeException("Something went wrong. createUser failed.");
		} finally {
			closeAll(resultSet, preparedStatement, connection);
		}
		return user;
	}
	
	public User getUserById(Integer userId) {
		
		if(userId == null) {
			// No User present.
			return null;
		}
		ResultSet resultSet = null;
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		User user = null;
		try {
			connection = getConnection();

			preparedStatement = connection.prepareStatement(SELECT_USER_BY_ID);
			preparedStatement.setInt(1, userId);

			resultSet = preparedStatement.executeQuery();
			if (resultSet.next()) {
				user = new User();
				user.setId(resultSet.getInt(1));
				user.setUserName(resultSet.getString(2));
				user.setEmail(resultSet.getString(3));
				user.setPhoneNumber(resultSet.getString(4));
				user.setCreatedOn(resultSet.getDate(5));
				user.setUpdatedOn(resultSet.getDate(6));
			}

		} catch (SQLException e) {
			log.error("SQLExceotion in getting connection from Data Source", e);
			throw new RuntimeException("Something went wrong. getUserById failed.");
		} finally {
			closeAll(resultSet, preparedStatement, connection);
		}
		return user;
	}

	public User updateUser(Integer userId, User user) {
		if(userId == null || user == null) {
			throw new RuntimeException("Invalid User ID/User");
		}
		User existingUser = getUserById(userId);
		
		if(existingUser == null) {
			throw new RuntimeException("User with ID: "+userId+" does not exist");
		}
		
		ResultSet resultSet = null;
		Connection connection = null;
		PreparedStatement preparedStatement = null;

		try {
			connection = getConnection();

			preparedStatement = connection.prepareStatement(UPDATE_USER);
			preparedStatement.setString(1, user.getPhoneNumber());
			preparedStatement.setInt(2, userId);

			int updatedCount = preparedStatement.executeUpdate();
			if (updatedCount != 1) {
				throw new RuntimeException("Something went wrong. User Update failed.");
			}
		} catch (SQLException e) {
			log.error("SQLExceotion in getting connection from Data Source", e);
			throw new RuntimeException("Something went wrong. updateUser failed.");
		} finally {
			closeAll(resultSet, preparedStatement, connection);
		}
		return user;
		
	}
}
