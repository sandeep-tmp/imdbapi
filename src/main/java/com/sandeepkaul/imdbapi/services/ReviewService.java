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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import com.sandeepkaul.imdbapi.models.ReviewComment;
import com.sandeepkaul.imdbapi.models.ReviewDetails;
import com.sandeepkaul.imdbapi.models.ReviewRequest;
import com.sandeepkaul.imdbapi.models.Show;
import com.sandeepkaul.imdbapi.models.User;
import com.sandeepkaul.imdbapi.models.VoteRequest;
import com.sandeepkaul.imdbapi.models.VotesOfReview;
import com.sandeepkaul.imdbapi.utils.DBUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * @author sandeep.kaul
 *
 */
@Slf4j
public class ReviewService extends DBUtils {

	private static final String SELECT_REVIEWS = "SELECT id,user_id,comment,created_on FROM reviews where show_id=? limit ? offset ?;";

	public static final String ADD_REVIEW = "INSERT INTO reviews(user_id,show_id,show_type_id,comment) VALUES (?,?,?,?);";

	public static final String GET_REVIEW_BY_ID = "SELECT * from reviews where id = ?;";

	public static final String ADD_VOTE = "INSERT INTO votes(user_id,action_user_id,action_id,action_type_id,is_upvote) VALUES(?,?,?,?,?);";

	public static final String GET_REVIEW_VOTES_BY_USER = "SELECT * from votes where user_id = ? and action_id = ? limit 1;";

	public static final String GET_COMMENT_COUNT_ON_REVIEW_BY_USER = "SELECT count(*) from review_comments where user_id = ? and review_id = ?;";

	public static final String COMMENT_ON_REVIEW = "INSERT INTO review_comments (user_id, review_id, comment) VALUES(?,?,?);";

	public static final String GET_VOTES_FOR_REVIEW = "SELECT is_upvote, action_id from votes where action_id in (reviewsIdsListHere);";

	public static final String GET_COMMENTS_FOR_REVIEW = "SELECT id,user_id, review_id,comment from review_comments where review_id in (reviewsIdsListHere);";

	private ShowService showService;

	private UserService userService;

	public ReviewService() {
		showService = new ShowService();
		userService = new UserService();
	}

	public void getUpvotesDownvotesForReview(int reviewId) {

	}

	/**
	 * 
	 * @param showId
	 * @param pageNum
	 * @param pageSize
	 * 
	 *            Validates if the Show id is correct, and the User ID is
	 *            correct, and then persists the comment.
	 */

	public List<ReviewDetails> getReviewsForShow(final Integer showId, final Integer pageNum, final Integer pageSize) {

		ResultSet resultSet = null;
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		List<ReviewDetails> reviews = new ArrayList<ReviewDetails>();

		try {
			connection = getConnection();

			List<Integer> reviewIds = new ArrayList<Integer>();

			// Skipping the step just to make it more efficient. Show ID
			// validation is note required, and incorrect iD would retuen null
			// reviews.
			// Show show = showService.getShow(showId);
			// if (show == null) {
			// throw new RuntimeException("Invalid Show ID");
			// }
			int limit = pageSize;
			int offset = (pageNum - 1) * pageSize;
			log.info("Querying for limit:{}, offsset:{}", limit, offset);
			preparedStatement = connection.prepareStatement(SELECT_REVIEWS);
			preparedStatement.setInt(1, showId);
			preparedStatement.setInt(2, limit);
			preparedStatement.setInt(3, offset);

			log.info("Query:{}",preparedStatement);
			
			resultSet = preparedStatement.executeQuery();
			while (resultSet.next()) {

				int reviewId = resultSet.getInt(1);
				reviewIds.add(reviewId);

				ReviewDetails review = new ReviewDetails();
				review.setReviewId(reviewId);
				review.setUserId(resultSet.getInt(2));
				review.setReviewText(resultSet.getString(3));
				review.setReviewDate(resultSet.getDate(4));
				reviews.add(review);
			}
			log.info("reviews:{}",reviews);
			Map<Integer, VotesOfReview> votesMap = getVotesForReview(reviewIds);
			Map<Integer, ArrayList<ReviewComment>> reviewComments = getCommentsForReviews(reviewIds);
			if (votesMap != null && reviewComments != null)
				for (ReviewDetails review : reviews) {

					Integer reviewId = review.getReviewId();

					// Setting Votes
					if (votesMap != null) {
						VotesOfReview votes = votesMap.get(reviewId);
						if (votes != null) {
							review.setDownvotes(votes.getDownvotes());
							review.setUpvotes(votes.getUpvotes());
						}
					}

					// Setting Comments
					if (reviewComments != null) {
						ArrayList<ReviewComment> comments = reviewComments.get(reviewId);
						review.setReviewComments(comments);
					}
				}

		} catch (SQLException e) {
			log.error("SQLExceotion in getting connection from Data Source", e);
			throw new RuntimeException("Something went wrong. getReviewsForShow failed.");
		} finally {
			closeAll(resultSet, preparedStatement, connection);
		}
		return reviews;
	}

	private Map<Integer, ArrayList<ReviewComment>> getCommentsForReviews(List<Integer> reviewIds) {

		if (CollectionUtils.isEmpty(reviewIds)) {
			return null;
		}

		ResultSet resultSet = null;
		Connection connection = null;
		PreparedStatement preparedStatement = null;

		Map<Integer, ArrayList<ReviewComment>> result = new HashMap<Integer, ArrayList<ReviewComment>>();
		try {
			connection = getConnection();

			// Validate Show ID against which the review is happening

			String query = GET_COMMENTS_FOR_REVIEW;
			query = query.replace("reviewsIdsListHere", StringUtils.join(reviewIds, ","));

			// Prepared statement not required here, nevermind.
			preparedStatement = connection.prepareStatement(query);

			resultSet = preparedStatement.executeQuery();
			while (resultSet.next()) {
				// user_id, review_id,comment
				Integer userId = resultSet.getInt("user_id");
				Integer reviewId = resultSet.getInt("review_id");
				String comment = resultSet.getString("comment");
				Integer reviewCommentId = resultSet.getInt("id");

				if (result.get(reviewId) == null) {
					result.put(reviewId, new ArrayList<ReviewComment>());
				}
				ArrayList<ReviewComment> reviewComments = result.get(reviewId);
				reviewComments.add(new ReviewComment(reviewCommentId, reviewId, userId, comment));
			}

		} catch (SQLException e) {
			log.error("SQLExceotion in getting connection from Data Source", e);
			throw new RuntimeException("Something went wrong. getCommentsForReviews failed.");
		} finally {
			closeAll(resultSet, preparedStatement, connection);
		}
		return result;

	}

	private Map<Integer, VotesOfReview> getVotesForReview(List<Integer> reviewIds) {

		if (CollectionUtils.isEmpty(reviewIds)) {
			return null;
		}

		ResultSet resultSet = null;
		Connection connection = null;
		PreparedStatement preparedStatement = null;

		Map<Integer, VotesOfReview> result = new HashMap<Integer, VotesOfReview>();
		try {
			connection = getConnection();

			// Validate Show ID against which the review is happening

			String query = GET_VOTES_FOR_REVIEW;
			query = query.replace("reviewsIdsListHere", StringUtils.join(reviewIds, ","));

			// Prepared statement not required here, nevermind.
			preparedStatement = connection.prepareStatement(query);

			resultSet = preparedStatement.executeQuery();
			while (resultSet.next()) {
				boolean isUpvote = resultSet.getBoolean("is_upvote");
				Integer reviewId = resultSet.getInt("action_id");
				if (result.get(reviewId) == null) {
					result.put(reviewId, new VotesOfReview());
				}
				VotesOfReview votesOfreview = result.get(reviewId);
				if (isUpvote) {
					votesOfreview.setUpvotes(votesOfreview.getUpvotes() + 1);
				} else {
					votesOfreview.setDownvotes(votesOfreview.getDownvotes() + 1);
				}
			}

		} catch (SQLException e) {
			log.error("SQLExceotion in getting connection from Data Source", e);
			throw new RuntimeException("Something went wrong. getVotesForReview failed.");
		} finally {
			closeAll(resultSet, preparedStatement, connection);
		}
		return result;
	}

	public ReviewDetails addReview(Integer showId, ReviewRequest reviewRequest) {

		ResultSet resultSet = null;
		Connection connection = null;
		PreparedStatement preparedStatement = null;

		ReviewDetails reviewDetails = null;
		if (reviewRequest == null) {
			return reviewDetails;
		}
		Integer userId = reviewRequest.getUserId();
		if (showId == null || userId == null) {
			throw new RuntimeException("Invalid Show ID/User ID");
		}

		try {
			connection = getConnection();

			// Validate Show ID against which the review is happening
			Show show = showService.getShow(showId);
			if (show == null) {
				throw new RuntimeException("Invalid Show ID");
			}

			// Validate the User ID who is reviewing the Show.
			User user = userService.getUserById(userId);
			if (user == null) {
				throw new RuntimeException("Invalid User ID");
			}

			preparedStatement = connection.prepareStatement(ADD_REVIEW, Statement.RETURN_GENERATED_KEYS);
			preparedStatement.setInt(1, userId);
			preparedStatement.setInt(2, showId);
			preparedStatement.setInt(3, show.getShowTypeId());
			preparedStatement.setString(4, reviewRequest.getReviewString());

			int updatedCount = preparedStatement.executeUpdate();
			if (updatedCount != 1) {
				throw new RuntimeException("Add Review Failed.");
			}

			resultSet = preparedStatement.getGeneratedKeys();
			if (resultSet.next()) {
				int lastInsertedId = resultSet.getInt(1);
				reviewDetails = new ReviewDetails(lastInsertedId, reviewRequest.getReviewString(), userId, showId,
						new Date(), 0, 0, null, show.getShowTypeId());
			}
		} catch (SQLException e) {
			log.error("SQLExceotion in getting connection from Data Source", e);
			throw new RuntimeException("Something went wrong. addReview failed.");
		} finally {
			closeAll(resultSet, preparedStatement, connection);
		}
		return reviewDetails;
	}

	public boolean voteReview(Integer reviewId, VoteRequest voteRequest) {

		if (reviewId == null || voteRequest == null) {
			throw new RuntimeException("Invalid Review ID or voteRequest");
		}

		int userId = voteRequest.getUserId();

		ReviewDetails review = getReviewById(reviewId);
		if (review == null) {
			throw new RuntimeException("Invalid Review ID");
		}

		User user = userService.getUserById(userId);
		if (user == null) {
			throw new RuntimeException("Invalid User ID");
		}

		if (userId == review.getUserId()) {
			throw new RuntimeException("Can't vote your own review.");
		}

		// Get Votes on this review by this user. Don't allow to re-vote the
		// same review.
		List<Boolean> reviewVotesByUser = getVotesOfReviewByUser(reviewId, userId);
		if (CollectionUtils.isNotEmpty(reviewVotesByUser)) {
			log.info("Same user cannot vote the same review twice. Breaking out");
			throw new RuntimeException("Same user cannot vote the same review twice.");
		}

		ResultSet resultSet = null;
		Connection connection = null;
		PreparedStatement preparedStatement = null;

		try {
			connection = getConnection();

			preparedStatement = connection.prepareStatement(ADD_VOTE);
			// user_id,action_user_id,action_id,action_type_id,is_upvote
			preparedStatement.setInt(1, userId);
			preparedStatement.setInt(2, review.getUserId());
			preparedStatement.setInt(3, review.getReviewId());
			// 2 is for Review. Hardcoding for now because voting of rating is
			// not allowed
			preparedStatement.setInt(4, 2);
			preparedStatement.setBoolean(5, voteRequest.isUpvote());

			int result = preparedStatement.executeUpdate();
			if (result != 1) {
				throw new RuntimeException("ADD_VOTE Insert failed");
			}

		} catch (SQLException e) {
			log.error("SQLExceotion in getting connection from Data Source", e);
			throw new RuntimeException("Something went wrong. voteReview failed.");
		} finally {
			closeAll(resultSet, preparedStatement, connection);
		}
		return true;
	}

	private List<Boolean> getVotesOfReviewByUser(Integer reviewId, int userId) {

		if (reviewId == null || userId == 0) {
			throw new RuntimeException("Invalid reviewId or userId");
		}

		ResultSet resultSet = null;
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		List<Boolean> result = null;

		try {
			connection = getConnection();

			preparedStatement = connection.prepareStatement(GET_REVIEW_VOTES_BY_USER);
			preparedStatement.setInt(1, userId);
			preparedStatement.setInt(2, reviewId);

			resultSet = preparedStatement.executeQuery();
			if (resultSet.next()) {
				// Since query is limiting on 1, therefor initializing here.
				result = new ArrayList<Boolean>();
				result.add(resultSet.getBoolean("is_upvote"));
			}

		} catch (SQLException e) {
			log.error("SQLExceotion in getting connection from Data Source", e);
			throw new RuntimeException("Something went wrong. getVotesOfReviewByUser failed.");
		} finally {
			closeAll(resultSet, preparedStatement, connection);
		}
		log.info("Exitting getVotesOfReviewByUser for revireId:{}, and userId:{}, with votesList:{}", reviewId, userId,
				result);
		return result;

	}

	/**
	 * Returns basic details of a review like User ID, Review comment.
	 * 
	 * @param reviewId
	 * @return
	 */
	public ReviewDetails getReviewById(Integer reviewId) {

		ResultSet resultSet = null;
		Connection connection = null;
		PreparedStatement preparedStatement = null;

		ReviewDetails review = null;

		if (reviewId == null) {
			throw new RuntimeException("Invalid Review ID");
		}

		try {
			connection = getConnection();

			preparedStatement = connection.prepareStatement(GET_REVIEW_BY_ID);
			preparedStatement.setInt(1, reviewId);

			resultSet = preparedStatement.executeQuery();
			if (resultSet.next()) {
				review = new ReviewDetails();
				review.setReviewId(resultSet.getInt("id"));
				review.setUserId(resultSet.getInt("user_id"));
				review.setShowId(resultSet.getInt("show_id"));
				review.setShowTypeId(resultSet.getInt("show_type_id"));
				review.setReviewText(resultSet.getString("comment"));
				review.setReviewDate(resultSet.getDate("created_on"));
				// Upvotes, Downvotes and comments are missing.
			}

		} catch (SQLException e) {
			log.error("SQLExceotion in getting connection from Data Source", e);
			throw new RuntimeException("Something went wrong. getReviewById failed.");
		} finally {
			closeAll(resultSet, preparedStatement, connection);
		}
		return review;
	}

	public void commentOnReview(ReviewComment reviewComment) {

		if (reviewComment == null || StringUtils.isBlank(reviewComment.getComment()) || reviewComment.getReviewId() == 0
				|| reviewComment.getUserId() == 0) {

			throw new RuntimeException("Invalid input");
		}

		int reviewId = reviewComment.getReviewId();
		int userId = reviewComment.getUserId();
		String comment = reviewComment.getComment();

		// Blank already tested
		if (comment.length() > 1000) {
			throw new RuntimeException("Comment cannot be more than 1000 characters");
		}

		ReviewDetails review = getReviewById(reviewId);
		if (review == null) {
			throw new RuntimeException("Invalid Review ID");
		}

		User user = userService.getUserById(userId);
		if (user == null) {
			throw new RuntimeException("User Id is invalid");
		}

		int count = getCommentCountByUserOnReview(reviewId, userId);
		// 100 should be ideally a config driven number. Keeping is here just
		// because this is non prod code.
		if (count >= 100) {
			throw new RuntimeException("No More than 100 comments allowed per review");
		}

		ResultSet resultSet = null;
		Connection connection = null;
		PreparedStatement preparedStatement = null;

		try {
			connection = getConnection();

			preparedStatement = connection.prepareStatement(COMMENT_ON_REVIEW);
			// user_id, review_id, comment
			preparedStatement.setInt(1, userId);
			preparedStatement.setInt(2, reviewId);
			preparedStatement.setString(3, comment);

			int result = preparedStatement.executeUpdate();
			if (result != 1) {
				throw new RuntimeException("Something went wrong. Insert unsuccessful");
			}

		} catch (SQLException e) {
			log.error("SQLExceotion in getting connection from Data Source", e);
			throw new RuntimeException("Something went wrong. commentOnReview failed.");
		} finally {
			closeAll(resultSet, preparedStatement, connection);
		}

	}

	public int getCommentCountByUserOnReview(int reviewId, int userId) {

		if (reviewId == 0 || userId == 0) {
			throw new RuntimeException("Invalid Review ID/User Id");
		}

		ResultSet resultSet = null;
		Connection connection = null;
		PreparedStatement preparedStatement = null;

		int count = 0;
		try {
			connection = getConnection();

			preparedStatement = connection.prepareStatement(GET_COMMENT_COUNT_ON_REVIEW_BY_USER);
			preparedStatement.setInt(1, userId);
			preparedStatement.setInt(2, reviewId);

			resultSet = preparedStatement.executeQuery();
			if (resultSet.next()) {
				count = resultSet.getInt("count(*)");
			}

		} catch (SQLException e) {
			log.error("SQLExceotion in getting connection from Data Source", e);
			throw new RuntimeException("Something went wrong. getCommentCountByUserOnReview failed.");
		} finally {
			closeAll(resultSet, preparedStatement, connection);
		}
		return count;
	}

}
