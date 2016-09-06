/**
 * 
 */
package com.sandeepkaul.imdbapi.resources;

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
import com.sandeepkaul.imdbapi.models.User;
import com.sandeepkaul.imdbapi.services.UserService;

import lombok.extern.slf4j.Slf4j;

/**
 * @author sandeep.kaul
 *
 */
@Path("/user")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Slf4j
@Singleton
public class UserResource {
	
	private UserService userService;
	
	public UserResource() {
		userService = new UserService();
	}

	@Metered
	@Timed(name="getUserById.TimeMeter")
	@ExceptionMetered(name = "getUserById.ExceptionMeter", cause = Exception.class)
	@Path("/{id}")
	@GET
	public User getUserById(final @NotNull @PathParam("id") Integer userId) {
		log.info("getUserById called with userId:{}", userId);
		User user = userService.getUserById(userId);
		log.debug("getUserById returned with user:{}", user);
		return user;
	}

	
	@Metered
	@Timed(name="createUser.TimeMeter")
	@ExceptionMetered(name = "createUser.ExceptionMeter", cause = Exception.class)
	@POST
	public User createUser(@NotNull final User user) {

		log.info("Create User request comes with user:{}", user);
		userService.createUser(user);
		log.info("Created User ID:{} with user:{}", user.getId(), user);
		return user;
	}

	/**
	 * Only Phone NUmber and password can be updated biven the current setup.
	 * 
	 * Not including password update here. That needs to be taken care of in a planned way.
	 * 
	 * @param user
	 * @param showId
	 * @return
	 */
	@Metered
	@Timed(name="updateUser.TimeMeter")
	@ExceptionMetered(name = "updateUser.ExceptionMeter", cause = Exception.class)
	@PUT
	@Path("/{id}")
	public User updateUser(@NotNull final User user, final @NotNull @PathParam("id") Integer userId) {
		
		log.info("Update User request comes with userId:{}, user:{} ", userId, user);
		userService.updateUser(userId, user);
		log.info("Update User ID:{} with user:{}", userId, user);
		return user;
	}
}
