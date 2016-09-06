/**
 * 
 */
package com.sandeepkaul.imdbapi.services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;

import com.sandeepkaul.imdbapi.models.CastCrewOfShow;
import com.sandeepkaul.imdbapi.models.CastCrewOfShowResponse;
import com.sandeepkaul.imdbapi.models.Show;
import com.sandeepkaul.imdbapi.utils.DBUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * @author sandeep.kaul
 *
 */
@Slf4j
public class ShowService extends DBUtils {

	public static final String CREATE_SHOW_QUERY = "INSERT INTO shows (name, genre_id,release_date, language_id, show_type_id) VALUES (?,?,?,?,?);";

	public static final String UPDATE_SHOW_QUERY = "UPDATE shows SET name=?, genre_id= ?,release_date=?, language_id=?, show_type_id=? WHERE id=?;";

	public static final String GET_SHOW_QUERY = "SELECT * from shows where id =?";

	public static final String DELETE_CAST_CREW_OF_SHOW_QUERY = "DELETE from show_cast where show_id =?";

	public static final String INSERT_CAST_CREW_OF_SHOW_QUERY = "INSERT INTO show_cast(member_id, show_id, show_type_id, cast_crew_type_id) VALUES (?,?,?,?)";

	public static final String SELECT_CREW_FOR_SHOW = "SELECT member_id,cast_crew_type_id FROM show_cast where show_id = ?";
	
	public ShowService() {

	}

	public Show createShow(Show show) {

		ResultSet resultSet = null;
		Connection connection = null;
		PreparedStatement preparedStatement = null;

		try {
			connection = getConnection();

			preparedStatement = connection.prepareStatement(CREATE_SHOW_QUERY, Statement.RETURN_GENERATED_KEYS);
			preparedStatement.setString(1, show.getName());
			preparedStatement.setInt(2, show.getGenreId());
			preparedStatement.setDate(3, new java.sql.Date(show.getReleaseDate().getTime()));
			preparedStatement.setInt(4, show.getLanguageId());
			preparedStatement.setInt(5, show.getShowTypeId());

			int updatedCount = preparedStatement.executeUpdate();
			if (updatedCount != 1) {
				throw new RuntimeException("Something went wrong. Insert failed.");
			}
			resultSet = preparedStatement.getGeneratedKeys();
			if (resultSet.next()) {
				int lastInsertedId = resultSet.getInt(1);
				show.setId(lastInsertedId);
			}
		} catch (SQLException e) {
			log.error("SQLExceotion in getting connection from Data Source", e);
			throw new RuntimeException("Something went wrong. createShow failed.");
		} finally {
			closeAll(resultSet, preparedStatement, connection);
		}
		return show;
	}

	public void updateShow(int showId, Show show) {

		ResultSet resultSet = null;
		Connection connection = null;
		PreparedStatement preparedStatement = null;

		show.setId(showId);
		try {
			connection = getConnection();

			preparedStatement = connection.prepareStatement(UPDATE_SHOW_QUERY);
			preparedStatement.setString(1, show.getName());
			preparedStatement.setInt(2, show.getGenreId());
			preparedStatement.setDate(3, new java.sql.Date(show.getReleaseDate().getTime()));
			preparedStatement.setInt(4, show.getLanguageId());
			preparedStatement.setInt(5, show.getShowTypeId());
			preparedStatement.setInt(6, showId);

			int updatedCount = preparedStatement.executeUpdate();
			if (updatedCount != 1) {
				throw new RuntimeException("Something went wrong. Insert failed.");
			}
		} catch (SQLException e) {
			log.error("SQLExceotion in getting connection from Data Source", e);
			throw new RuntimeException("Something went wrong. updateShow failed.");
		} finally {
			closeAll(resultSet, preparedStatement, connection);
		}
		return;
	}

	public Show getShow(Integer showId) {

		if(showId == null) {
			// No Show present.
			return null;
		}
		ResultSet resultSet = null;
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		Show show = null;

		try {
			connection = getConnection();

			preparedStatement = connection.prepareStatement(GET_SHOW_QUERY);
			preparedStatement.setInt(1, showId);

			resultSet = preparedStatement.executeQuery();
			if (resultSet.next()) {
				show = new Show();
				show.setId(resultSet.getInt(1));
				show.setName(resultSet.getString(2));
				show.setGenreId(resultSet.getInt(3));
				show.setReleaseDate(resultSet.getDate(4));
				show.setCreatedOn(resultSet.getDate(5));
				show.setUpdatedOn(resultSet.getDate(6));
				show.setLanguageId(resultSet.getInt(7));
				show.setShowTypeId(resultSet.getInt(8));
			}

		} catch (SQLException e) {
			log.error("SQLExceotion in getting connection from Data Source", e);
			throw new RuntimeException("Something went wrong. getShow failed.");
		} finally {
			closeAll(resultSet, preparedStatement, connection);
		}
		return show;
	}

	public void updateCastCrewOfShow(Integer showId, List<CastCrewOfShow> castCrewOfShow) {

		if (CollectionUtils.isEmpty(castCrewOfShow)) {
			// Empty Collection came. Don't update. Case Crew cannot be empty
			return;
		}
		ResultSet resultSet = null;
		Connection connection = null;
		PreparedStatement preparedStatement = null;

		try {
			connection = getConnection();
			connection.setAutoCommit(false);

			preparedStatement = connection.prepareStatement(DELETE_CAST_CREW_OF_SHOW_QUERY);
			preparedStatement.setInt(1, showId);
			preparedStatement.executeUpdate();

			Show show = getShow(showId);
			if(show == null) {
				throw new RuntimeException("Invalid Show ID");
			}
			boolean transactionFailed = false;
			for (CastCrewOfShow castCrew : castCrewOfShow) {
				// member_id, show_id, show_type_id, cast_crew_type_id
				preparedStatement = connection.prepareStatement(INSERT_CAST_CREW_OF_SHOW_QUERY);
				preparedStatement.setInt(1, castCrew.getCcMemberId());
				preparedStatement.setInt(2, showId);
				preparedStatement.setInt(3, show.getShowTypeId());
				preparedStatement.setInt(4, castCrew.getCcTypeId());

				int updatedCount = preparedStatement.executeUpdate();
				if (updatedCount != 1) {
					transactionFailed = true;
					break;
				}
			}
			if (!transactionFailed) {
				connection.commit();
				connection.setAutoCommit(true);
			} else {
				connection.rollback();
				connection.setAutoCommit(true);
			}
		} catch (Exception e) {
			try {
				connection.rollback();
				connection.setAutoCommit(true);
			} catch (SQLException e1) {
				log.error("Transaction Rollback failed. SQLExceotion in getting connection from Data Source", e);
			}
			log.error("SQLExceotion in getting connection from Data Source", e);
			throw new RuntimeException("Something went wrong. updateShow failed.");
		} finally {
			closeAll(resultSet, preparedStatement, connection);
		}
		return;
	}

	
	
	
	

	public List<CastCrewOfShowResponse> getCastCrewOfShow(Integer showId) {
		
		List<CastCrewOfShowResponse> response = new ArrayList<CastCrewOfShowResponse>();
		ResultSet resultSet = null;
		Connection connection = null;
		PreparedStatement preparedStatement = null;

		try {
			connection = getConnection();

			preparedStatement = connection.prepareStatement(SELECT_CREW_FOR_SHOW);
			preparedStatement.setInt(1, showId);

			resultSet = preparedStatement.executeQuery();
			while (resultSet.next()) {
				int memberId = resultSet.getInt(1);
				int castCrewTypeId = resultSet.getInt(2);
				String castCrewMemberName = CacheService.getCastCrewMemberIdToNameMap().get(memberId);
				String castCrewType = CacheService.getCastCrewMemberTypeIdToTypeNameMap().get(castCrewTypeId);
				CastCrewOfShowResponse ccObject = new CastCrewOfShowResponse(memberId, castCrewTypeId, castCrewMemberName, castCrewType);
				response.add(ccObject);
			}

		} catch (SQLException e) {
			log.error("SQLExceotion in getting connection from Data Source", e);
			throw new RuntimeException("Something went wrong. getCastCrewOfShow failed.");
		} finally {
			closeAll(resultSet, preparedStatement, connection);
		}
		return response;
	}

	
}
