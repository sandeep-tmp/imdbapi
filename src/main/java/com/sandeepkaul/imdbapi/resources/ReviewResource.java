/**
 * 
 */
package com.sandeepkaul.imdbapi.resources;

import java.util.List;

import javax.inject.Singleton;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.codahale.metrics.annotation.ExceptionMetered;
import com.codahale.metrics.annotation.Metered;
import com.codahale.metrics.annotation.Timed;
import com.sandeepkaul.imdbapi.models.ReviewComment;
import com.sandeepkaul.imdbapi.models.ReviewDetails;
import com.sandeepkaul.imdbapi.models.ReviewRequest;
import com.sandeepkaul.imdbapi.models.VoteRequest;
import com.sandeepkaul.imdbapi.services.ReviewService;

import lombok.extern.slf4j.Slf4j;

/**
 * @author sandeep.kaul
 *
 */
@Path("/review")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Slf4j
@Singleton
public class ReviewResource {

	private ReviewService reviewService;

	public ReviewResource() {
		reviewService = new ReviewService();
	}

	@Metered
	@Timed(name = "getReviewsForShow.TimeMeter")
	@ExceptionMetered(name = "getReviewsForShow.ExceptionMeter", cause = Exception.class)
	@GET
	@Path("/{showId}")
	public List<ReviewDetails> getReviewsForShow(final @NotNull @PathParam("showId") Integer showId,
			@QueryParam("pageNum") @DefaultValue("1") Integer pageNum,
			@QueryParam("pageSize") @DefaultValue("20") Integer pageSize) {

		log.info("getReviewsForShow called with showId:{}, pageNum:{}, pageSize:{} ", showId, pageNum, pageSize);
		List<ReviewDetails> reviewDetails = reviewService.getReviewsForShow(showId, pageNum, pageSize);
		log.info("Returning Reviews for Show:{} with data:{},", showId, reviewDetails);
		return reviewDetails;
	}

	@Metered
	@Timed(name = "addReview.TimeMeter")
	@ExceptionMetered(name = "addReview.ExceptionMeter", cause = Exception.class)
	@POST
	@Path("/add_review/{showId}")
	public ReviewDetails addReview(final @NotNull @PathParam("showId") Integer showId,
			@NotNull ReviewRequest reviewRequest) {

		log.info("Add Review called for shoeID:{}, reviewRequest:{}", showId, reviewRequest);
		ReviewDetails reviewDetails = reviewService.addReview(showId, reviewRequest);
		log.info("Added Review:{} , for show ID:{}", reviewDetails, showId);
		return reviewDetails;
	}

	/**
	 * Rate limiting needs to be put in at a user level. Should be at infra
	 * level
	 * 
	 * @param reviewId
	 * @param voteRequest
	 * @return
	 */
	@Metered
	@Timed(name = "voteReview.TimeMeter")
	@ExceptionMetered(name = "voteReview.ExceptionMeter", cause = Exception.class)
	@POST
	@Path("/vote_review/{reviewId}")
	public Boolean voteReview(final @NotNull @PathParam("reviewId") Integer reviewId,
			@NotNull VoteRequest voteRequest) {

		log.info("Vote Review called for reviewId:{} and voteRequest:{}", reviewId, voteRequest);
		reviewService.voteReview(reviewId, voteRequest);
		log.info("Voted Review:{} ", reviewId);
		return true;
	}

	/**
	 * Rate limiting needs to be put in at a review/user level. Should be at
	 * infra level
	 * 
	 * @param reviewComment
	 * @return
	 */
	@Metered
	@Timed(name = "commentOnReview.TimeMeter")
	@ExceptionMetered(name = "commentOnReview.ExceptionMeter", cause = Exception.class)
	@POST
	@Path("/comment_on_review")
	public Boolean commentOnReview(final @NotNull ReviewComment reviewComment) {

		log.info("commentOnReview called for reviewComment Request:{}", reviewComment);
		reviewService.commentOnReview(reviewComment);
		log.info("Commented on Review:{} ", reviewComment.getReviewId());
		return true;
	}
}
