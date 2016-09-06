/**
 * 
 */
package com.sandeepkaul.imdbapi.resources;

import java.util.List;

import javax.inject.Singleton;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.codahale.metrics.annotation.ExceptionMetered;
import com.codahale.metrics.annotation.Metered;
import com.codahale.metrics.annotation.Timed;
import com.sandeepkaul.imdbapi.services.RatingService;

import lombok.extern.slf4j.Slf4j;

/**
 * @author sandeep.kaul
 *
 */
@Path("/rating")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Slf4j
@Singleton
public class RatingResource {

	private RatingService ratingService;

	public RatingResource() {
		ratingService = new RatingService();
	}

	@Metered
	@Timed(name = "getRatingSummaryForShow.TimeMeter")
	@ExceptionMetered(name = "getRatingSummaryForShow.ExceptionMeter", cause = Exception.class)
	@GET
	@Path("/{showId}/rating_summary")
	public Double getRatingSummaryForShow(final @NotNull @PathParam("showId") Integer showId) {
		return ratingService.getRatingSummary(showId);
	}

	@Metered
	@Timed(name = "rateShow.TimeMeter")
	@ExceptionMetered(name = "rateShow.ExceptionMeter", cause = Exception.class)
	@PUT
	@Path("/{showId}/{userId}/{rating}")
	public boolean rateShow(final @NotNull @PathParam("showId") Integer showId,
			final @NotNull @PathParam("userId") Integer userId, final @NotNull @PathParam("rating") Integer rating) {

		// Ideally the UserID here would come from session, but given that we
		// don't have the infra for it, taking it in the Path Param
		log.info("rateShow request comes with showId:{}, userId:{}, rating:{} ", showId, userId, rating);
		ratingService.rateShow(showId, userId, rating);
		return true;
	}

	@Metered
	@Timed(name = "getTopRatedShows.TimeMeter")
	@ExceptionMetered(name = "getTopRatedShows.ExceptionMeter", cause = Exception.class)
	@GET
	@Path("/get_top_rates_shows")
	public List<Integer> getTopRatedShows() {

		log.info("getTopRatedShows Called");
		List<Integer> topRatedShows = ratingService.getTopRatedShows(0,10);
		log.info("getTopRatedShows returned with: {}", topRatedShows);
		return topRatedShows;
	}

}
