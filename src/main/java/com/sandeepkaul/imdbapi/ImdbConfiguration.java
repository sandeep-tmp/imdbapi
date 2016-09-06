/**
 * 
 */
package com.sandeepkaul.imdbapi;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.sandeepkaul.imdbapi.models.DBConfig;

import io.dropwizard.Configuration;
import lombok.Data;

/**
 * @author sandeep.kaul
 *
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ImdbConfiguration extends Configuration {

	private DBConfig dbConfig;
}
