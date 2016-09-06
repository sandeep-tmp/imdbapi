/**
 * 
 */
package com.sandeepkaul.imdbapi.resources;

import java.util.List;

import javax.inject.Singleton;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.codahale.metrics.annotation.ExceptionMetered;
import com.codahale.metrics.annotation.Metered;
import com.codahale.metrics.annotation.Timed;
import com.sandeepkaul.imdbapi.services.RecommendationService;

import lombok.extern.slf4j.Slf4j;

/**
 * @author sandeep.kaul
 *
 */
@Path("/recommendation")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Slf4j
@Singleton
public class RecommendationResource {

	private RecommendationService recommendationService;
	
	public RecommendationResource() {
		recommendationService = new RecommendationService();
	}
	
	@Metered
	@Timed(name = "getRecommendationForUser.TimeMeter")
	@ExceptionMetered(name = "getRecommendationForUser.ExceptionMeter", cause = Exception.class)
	@GET
	@Path("/{userId}")
	public List<Integer> getRecommendationForUser(final @NotNull @PathParam("userId") Integer userId) {

		log.info("getRecommendationForUser called with userId:{}", userId);
		List<Integer> recommendedShows = recommendationService.getRecommendationForUser(userId);
		log.info("Returning from getRecommendationForUser, userId:{}. showIds: {}", userId, recommendedShows);
		return recommendedShows;
	}
}
