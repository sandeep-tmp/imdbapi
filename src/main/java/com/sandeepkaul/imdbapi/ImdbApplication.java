/**
 * 
 */
package com.sandeepkaul.imdbapi;

import java.util.EnumSet;

import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;

import org.eclipse.jetty.servlets.CrossOriginFilter;
import org.glassfish.jersey.server.ServerProperties;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.sandeepkaul.imdbapi.resources.RatingResource;
import com.sandeepkaul.imdbapi.resources.RecommendationResource;
import com.sandeepkaul.imdbapi.resources.ReviewResource;
import com.sandeepkaul.imdbapi.resources.ShowResource;
import com.sandeepkaul.imdbapi.resources.UserResource;
import com.sandeepkaul.imdbapi.services.CacheService;
import com.sandeepkaul.imdbapi.utils.Constants;
import com.sandeepkaul.imdbapi.utils.DataSource;

import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

/**
 * @author sandeep.kaul
 *
 */
public class ImdbApplication extends Application<ImdbConfiguration> {

	public void initialize(Bootstrap<ImdbConfiguration> bootstrap) {
		super.initialize(bootstrap);

	}

	/**
	 * main method.
	 */
	public static void main(String[] args) throws Exception {
		new ImdbApplication().run(args[0], args[1]);
	}

	@Override
	public void run(ImdbConfiguration configuration, Environment environment) throws Exception {

		environment.getObjectMapper()
				.setPropertyNamingStrategy(PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES);
		environment.getObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL);
		environment.getObjectMapper()
				.setPropertyNamingStrategy(PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES);

		FilterRegistration.Dynamic filter = environment.servlets().addFilter("CORS", CrossOriginFilter.class);
		filter.addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class), true, "/*");
		filter.setInitParameter(CrossOriginFilter.ALLOWED_METHODS_PARAM, "GET,PUT,POST,DELETE,OPTIONS");
		filter.setInitParameter(CrossOriginFilter.ALLOWED_ORIGINS_PARAM, "*");
		filter.setInitParameter(CrossOriginFilter.ACCESS_CONTROL_ALLOW_ORIGIN_HEADER, "*");
		filter.setInitParameter(CrossOriginFilter.ALLOWED_HEADERS_PARAM, "*");
		filter.setInitParameter(CrossOriginFilter.ALLOW_CREDENTIALS_PARAM, "true");

		environment.jersey().property(ServerProperties.OUTBOUND_CONTENT_LENGTH_BUFFER, 0);

		DataSource dataSource = new DataSource(configuration.getDbConfig());
		Constants.DATA_SOURCE = dataSource;

		// Resources
		environment.jersey().register(new ShowResource());
		environment.jersey().register(new UserResource());
		environment.jersey().register(new RatingResource());
		environment.jersey().register(new ReviewResource());
		environment.jersey().register(new RecommendationResource());
		
		
		//ExceptionMapper
		environment.jersey().register(new ImdbExceptionMapper());
		
		// For Loading Cahce.
		new CacheService();
	}

}
