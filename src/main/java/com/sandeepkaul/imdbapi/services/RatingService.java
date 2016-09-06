/**
 * 
 */
package com.sandeepkaul.imdbapi.services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.sandeepkaul.imdbapi.models.Show;
import com.sandeepkaul.imdbapi.utils.DBUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * @author sandeep.kaul
 *
 */
@Slf4j
public class RatingService extends DBUtils {

	public static final String GET_SHOW_RATING_SUMMARY = "SELECT rating_count,rating_sum from show_rating_summary WHERE show_id=? ;";

	public static final String SELECT_RATING_DATA = "SELECT rating from rating where user_id=? and show_id=?";
	
	public static final String GET_USERS_RATINGS = "SELECT rating, show_id, genre_id from rating where user_id=? and show_id=?";

	public static final String CHECK_IF_USER_HAS_RATED_ANY_SHOWS = "SELECT * from rating where user_id = ? limit 1;";
	
	public static final String INSERT_RATING_DATA = "INSERT INTO rating (user_id,show_id,show_type_id,genre_id,rating) VALUES(?,?,?,?,?);";

	public static final String INSERT_RATING_SUMMARY = "INSERT INTO show_rating_summary (show_id,rating_count,rating_sum,genre_id) VALUES(?,1,?,?);";

	public static final String UPDATE_RATING_SUMMARY = "UPDATE show_rating_summary set rating_count=rating_count+1, rating_sum=rating_sum+? WHERE show_id=?;";

	public static final String SELECT_TOP_N_SHOWS = "select * from show_rating_summary where rating_count>? and (rating_sum/rating_count)>? order by  (rating_sum/rating_count) desc limit ?";
	
	private ShowService showService;
	
	public RatingService() {
		showService = new ShowService();
	}
	public Double getRatingSummary(Integer showId) {

		if(showId == null) {
			throw new RuntimeException("Invalid Show Id");
		}
		Double rating = null;
		ResultSet resultSet = null;
		Connection connection = null;
		PreparedStatement preparedStatement = null;

		try {
			connection = getConnection();

			preparedStatement = connection.prepareStatement(GET_SHOW_RATING_SUMMARY);
			preparedStatement.setInt(1, showId);

			resultSet = preparedStatement.executeQuery();
			if (resultSet.next()) {
				int ratingCount = resultSet.getInt(1);
				int ratingSum = resultSet.getInt(2);
				rating = (double) ratingSum / (double) ratingCount;
			}

		} catch (SQLException e) {
			log.error("SQLExceotion in getting connection from Data Source", e);
			throw new RuntimeException("Something went wrong. getUsersShowRating failed.");
		} finally {
			closeAll(resultSet, preparedStatement, connection);
		}
		return rating;
	}


	public boolean userHasRatedAnyShow(Integer userId ) {
		
		if(userId == null) {
			throw new RuntimeException("Invalid User Id");
		}
		
		ResultSet resultSet = null;
		Connection connection = null;
		PreparedStatement preparedStatement = null;

		boolean userHasRated = false;
		try {
			connection = getConnection();

			preparedStatement = connection.prepareStatement(CHECK_IF_USER_HAS_RATED_ANY_SHOWS);
			preparedStatement.setInt(1, userId);

			resultSet = preparedStatement.executeQuery();
			if (resultSet.next()) {
				userHasRated = true;
			}

		} catch (SQLException e) {
			log.error("SQLExceotion in getting connection from Data Source", e);
			throw new RuntimeException("Something went wrong. userHasRatedAnyShow failed.");
		} finally {
			closeAll(resultSet, preparedStatement, connection);
		}
		
		return userHasRated;
	}
	/**
	 * Incomeplte method.
	 * @param userId
	 * @return
	 */
	public Map<Integer,Integer> getRatingsByUser(Integer userId) {

		if(userId == null) {
			throw new RuntimeException("Invalid User Id");
		}
		Integer rating = null;
		ResultSet resultSet = null;
		Connection connection = null;
		PreparedStatement preparedStatement = null;

		try {
			connection = getConnection();

			preparedStatement = connection.prepareStatement("");
			preparedStatement.setInt(1, userId);

			resultSet = preparedStatement.executeQuery();
			if (resultSet.next()) {
				rating = resultSet.getInt(1);
			}

		} catch (SQLException e) {
			log.error("SQLExceotion in getting connection from Data Source", e);
			throw new RuntimeException("Something went wrong. getUsersShowRating failed.");
		} finally {
			closeAll(resultSet, preparedStatement, connection);
		}
		return null;
	}
	
	public Integer getUsersShowRating(Integer userId, Integer showId) {

		if(userId == null) {
			throw new RuntimeException("Invalid User Id");
		}
		if(showId == null) {
			throw new RuntimeException("Invalid Show Id");
		}
		Integer rating = null;
		ResultSet resultSet = null;
		Connection connection = null;
		PreparedStatement preparedStatement = null;

		try {
			connection = getConnection();

			preparedStatement = connection.prepareStatement(SELECT_RATING_DATA);
			preparedStatement.setInt(1, userId);
			preparedStatement.setInt(2, showId);

			resultSet = preparedStatement.executeQuery();
			if (resultSet.next()) {
				rating = resultSet.getInt(1);
			}

		} catch (SQLException e) {
			log.error("SQLExceotion in getting connection from Data Source", e);
			throw new RuntimeException("Something went wrong. getUsersShowRating failed.");
		} finally {
			closeAll(resultSet, preparedStatement, connection);
		}
		return rating;
	}

	public void rateShow(Integer showId, Integer userId, Integer rating) {

		if (rating > 5 || rating < 0) {
			throw new RuntimeException("Invalid rating value");
		}
		ResultSet resultSet = null;
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		PreparedStatement preparedStatement2 = null;

		try {
			connection = getConnection();

			Show show = showService.getShow(showId);
			if (show == null) {
				throw new RuntimeException("Invalid Show ID");
			}

			// If the user has already rated the show, then don't allow
			// re-rating.
			Integer currentRating = getUsersShowRating(userId, showId);
			if (currentRating != null) {
				throw new RuntimeException("User has already rated the movie, cannot change the rating once given.");
			}

			// Check if this is the first rating for the show, in that case
			// insert into rating summary else udpate.
			Double ratingSummary = getRatingSummary(showId);

			// user_id,show_id,show_type_id,genre_id,rating
			preparedStatement = connection.prepareStatement(INSERT_RATING_DATA);
			preparedStatement.setInt(1, userId);
			preparedStatement.setInt(2, showId);
			preparedStatement.setInt(3, show.getShowTypeId());
			preparedStatement.setInt(4, show.getGenreId());
			preparedStatement.setInt(5, rating);

			int updatedCount = preparedStatement.executeUpdate();
			if (updatedCount != 1) {
				throw new RuntimeException("Ratings insert failed.");
			}

			// Rating is inserted, now also insert in show_rating_summary for
			// quick retrieval.
			// If the second insert fails, and the first one is as it is, then
			// let it be, minor descripency is okay.
			if (ratingSummary == null) {
				// First rating for the show. Insert into Rating Summary
				preparedStatement2 = connection.prepareStatement(INSERT_RATING_SUMMARY);
				preparedStatement2.setInt(1, showId);
				preparedStatement2.setInt(2, rating);
				preparedStatement.setInt(3, show.getGenreId());

				updatedCount = preparedStatement2.executeUpdate();
				if (updatedCount != 1) {
					throw new RuntimeException("Ratings Summary insert failed.");
				}
			} else {
				// Update Rating Summary
				preparedStatement2 = connection.prepareStatement(UPDATE_RATING_SUMMARY);
				preparedStatement2.setInt(1, rating);
				preparedStatement2.setInt(2, showId);

				updatedCount = preparedStatement2.executeUpdate();
				if (updatedCount != 1) {
					throw new RuntimeException("Ratings Summary update failed.");
				}
			}

		} catch (Exception e) {
			log.error("SQLExceotion in getting connection from Data Source", e);
			throw new RuntimeException("Something went wrong. rateShow failed.");
		} finally {
			closeAll(resultSet, preparedStatement, connection);
			closeAll(null, preparedStatement2, null);
		}
		return;
	}
	
	/**
	 * Returns a list of top N shows based on their average rating
	 * @param showCountToReturn
	 * @return
	 */
	public List<Integer> getTopRatedShows(int minimumRatingValue, int showCountToReturn) {
		
		List<Integer> response = new ArrayList<Integer>();
		
		if(showCountToReturn == 0) {
			return response;
		}

		// The minimum number of ratings requied for a show to be shortlisted in top N shows globaly.
		// Should come from config again
		int minimumRatingCount = 0;

		ResultSet resultSet = null;
		Connection connection = null;
		PreparedStatement preparedStatement = null;

		try {
			connection = getConnection();

			preparedStatement = connection.prepareStatement(SELECT_TOP_N_SHOWS);
			preparedStatement.setInt(1, minimumRatingCount);
			preparedStatement.setInt(2, minimumRatingValue);
			preparedStatement.setInt(3, showCountToReturn);

			resultSet = preparedStatement.executeQuery();
			while (resultSet.next()) {
				int showId = resultSet.getInt("show_id");
				response.add(showId);
			}

		} catch (SQLException e) {
			log.error("SQLExceotion in getting connection from Data Source", e);
			throw new RuntimeException("Something went wrong. getTopRatedShows failed.");
		} finally {
			closeAll(resultSet, preparedStatement, connection);
		}
		return response;
	}
}
