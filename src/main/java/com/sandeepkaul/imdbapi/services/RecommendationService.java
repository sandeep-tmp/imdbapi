/**
 * 
 */
package com.sandeepkaul.imdbapi.services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import com.sandeepkaul.imdbapi.models.User;
import com.sandeepkaul.imdbapi.utils.DBUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * @author sandeep.kaul
 *
 */
@Slf4j
public class RecommendationService extends DBUtils {

	private UserService userService;
	private RatingService ratingService;

	private static final String GENRE_IDS_THAT_USER_LIKES = "select genre_id, AVG(rating) from rating where user_id=? group by genre_id HAVING AVG(rating)>4 order by 2 limit 3;";

	private static final String TOP_SHOWS_IN_GENRES = "SELECT show_id from show_rating_summary where genre_id in (replaceGenreIdsHere);";

	public RecommendationService() {
		userService = new UserService();
		ratingService = new RatingService();
	}

	/**
	 * Logic: If the users has done some ratings: get the shows that the user
	 * has rated 4/5 stars, then get their genres, then find the other shows in
	 * that genres ordered by their rating. return top 10.
	 * 
	 * If the user has not rated anything, return the top 10 rated shows .
	 */
	public List<Integer> getRecommendationForUser(Integer userId) {

		User user = userService.getUserById(userId);
		if (user == null) {
			throw new RuntimeException("User Id Invalid");
		}

		int showCountToReturn = 10;
		int minimumRating = 4;

		List<Integer> topRatedShows = null;
		boolean userHasRated = ratingService.userHasRatedAnyShow(userId);
		if (!userHasRated) {
			// User has not rated any shows, so return the top to rated shows
			// from the show_rating_summary table

			topRatedShows = ratingService.getTopRatedShows(minimumRating, showCountToReturn);
			return topRatedShows;
		} else {
			// Users have rated some shows, get their top genres, and then
			// return the best shows from that.

			List<Integer> genreIds = getGenreIdsThatUserLikes(userId);
			if (CollectionUtils.isEmpty(genreIds)) {
				// Maybe the user didnt gave any 4 star+ rating, or gave lot of
				// negative ratings.
				// Get the top rateed shows
				topRatedShows = ratingService.getTopRatedShows(minimumRating, showCountToReturn);
				return topRatedShows;
			}
			// Now coming to the case where there are some Genre Ids that the
			// user likes.
			topRatedShows = getTopShowsBasedForGenres(genreIds);
			if (topRatedShows.size() > 10) {
				// we have a good list to return to the user.
				return topRatedShows;
			} else {
				// Search By Crew as well. TODO complete here
//				getTopRecommendationFromCrew(userId);
				// TODO remove the below lines
				topRatedShows.addAll(ratingService.getTopRatedShows(minimumRating, showCountToReturn));
				return new ArrayList<Integer>(new HashSet<Integer>(topRatedShows));
			}

		}
	}

	/**
	 * FInds out the director, LEAD actors of the shows the user has given 4/5
	 * start to, then finds the shows by those directors and lead actors, orderd
	 * by their avg rating, and returns that.
	 * 
	 * @param userId
	 */
	private void getTopRecommendationFromCrew(Integer userId) {
		// TODO Auto-generated method stub

	}

	private List<Integer> getTopShowsBasedForGenres(List<Integer> genreIds) {

		List<Integer> topRatedShows = new ArrayList<Integer>();
		if (CollectionUtils.isEmpty(genreIds)) {
			return topRatedShows;
		}

		ResultSet resultSet = null;
		Connection connection = null;
		PreparedStatement preparedStatement = null;

		try {
			connection = getConnection();
			String query = TOP_SHOWS_IN_GENRES;
			query = query.replace("replaceGenreIdsHere", StringUtils.join(genreIds, ","));

			preparedStatement = connection.prepareStatement(query);

			resultSet = preparedStatement.executeQuery();
			while (resultSet.next()) {
				int showId = resultSet.getInt("show_id");
				topRatedShows.add(showId);
			}
		} catch (SQLException e) {
			log.error("SQLExceotion in getting connection from Data Source", e);
			throw new RuntimeException("Something went wrong. getTopShowsBasedForGenres failed.");
		} finally {
			closeAll(resultSet, preparedStatement, connection);
		}
		return topRatedShows;

	}

	private List<Integer> getGenreIdsThatUserLikes(Integer userId) {

		List<Integer> genreIdsThatUserLikes = new ArrayList<Integer>();
		if (userId == null) {
			throw new RuntimeException("Invalid User Id");
		}

		ResultSet resultSet = null;
		Connection connection = null;
		PreparedStatement preparedStatement = null;

		try {
			connection = getConnection();

			preparedStatement = connection.prepareStatement(GENRE_IDS_THAT_USER_LIKES);
			preparedStatement.setInt(1, userId);

			resultSet = preparedStatement.executeQuery();
			while (resultSet.next()) {
				int genreId = resultSet.getInt("genre_id");
				genreIdsThatUserLikes.add(genreId);
			}
		} catch (SQLException e) {
			log.error("SQLExceotion in getting connection from Data Source", e);
			throw new RuntimeException("Something went wrong. getGenreIdsThatUserLikes failed.");
		} finally {
			closeAll(resultSet, preparedStatement, connection);
		}
		return genreIdsThatUserLikes;
	}

}
