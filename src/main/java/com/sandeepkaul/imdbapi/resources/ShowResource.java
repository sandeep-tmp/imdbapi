/**
 * 
 */
package com.sandeepkaul.imdbapi.resources;

import java.util.List;

import javax.inject.Singleton;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.codahale.metrics.annotation.ExceptionMetered;
import com.codahale.metrics.annotation.Metered;
import com.codahale.metrics.annotation.Timed;
import com.sandeepkaul.imdbapi.models.CastCrewOfShow;
import com.sandeepkaul.imdbapi.models.CastCrewOfShowResponse;
import com.sandeepkaul.imdbapi.models.Show;
import com.sandeepkaul.imdbapi.services.ShowService;

import lombok.extern.slf4j.Slf4j;

/**
 * @author sandeep.kaul
 *
 */
@Path("/show")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Slf4j
@Singleton
public class ShowResource {

	private ShowService showService;

	public ShowResource() {
		showService = new ShowService();
	}

	@Metered
	@Timed(name="getShowById.TimeMeter")
	@ExceptionMetered(name = "getShowById.ExceptionMeter", cause = Exception.class)
	@Path("/{id}")
	@GET
	public Show getShowById(final @NotNull @PathParam("id") Integer showId) {
		log.info("getShowById called with showId:{}", showId);
		Show show = showService.getShow(showId);
		log.debug("getShowById returned with show:{}", show);
		return show;
	}

	@Metered
	@Timed(name="createShow.TimeMeter")
	@ExceptionMetered(name = "createShow.ExceptionMeter", cause = Exception.class)
	@POST
	public Show createShow(@NotNull final Show show) {

		log.info("Create Show request comes with show:{}", show);
		showService.createShow(show);
		log.info("Created Show ID:{} with show:{}", show.getId(), show);
		return show;
	}

	@Metered
	@Timed(name="updateShow.TimeMeter")
	@ExceptionMetered(name = "updateShow.ExceptionMeter", cause = Exception.class)
	@PUT
	@Path("/{id}")
	public Show updateShow(@NotNull final Show show, final @NotNull @PathParam("id") Integer showId) {

		log.info("Update Show request comes with showId:{}, show:{} ", showId, show);
		showService.updateShow(showId, show);
		log.info("Update Show ID:{} with show:{}", show.getId(), show);
		return show;
	}

	@Metered
	@Timed(name="getCastCrewOfShow.TimeMeter")
	@ExceptionMetered(name = "getCastCrewOfShow.ExceptionMeter", cause = Exception.class)
	@GET
	@Path("/{id}/get_cast_crew")
	public List<CastCrewOfShowResponse> getCastCrewOfShow(final @NotNull @PathParam("id") Integer showId) {

		log.info("getCastCrewOfShow request comes with showId:{} ", showId);
		List<CastCrewOfShowResponse> castCrewOfShow = showService.getCastCrewOfShow(showId);
		log.debug("getCastCrewOfShow for show ID:{}, returned with response:{}", showId, castCrewOfShow);
		return castCrewOfShow;
	}

	@Metered
	@Timed(name="updateCastCrewOfShow.TimeMeter")
	@ExceptionMetered(name = "updateCastCrewOfShow.ExceptionMeter", cause = Exception.class)
	@PUT
	@Path("/{id}/update_cast_crew")
	public boolean updateCastCrewOfShow(@NotNull final List<CastCrewOfShow> castCrewOfShow,
			final @NotNull @PathParam("id") Integer showId) {

		log.info("updateCastCrewOfShow request comes with showId:{}, castCrewOfShow:{} ", showId, castCrewOfShow);
		showService.updateCastCrewOfShow(showId, castCrewOfShow);
		return true;
	}


}
