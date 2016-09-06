/**
 * 
 */
package com.sandeepkaul.imdbapi.models;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

/**
 * @author sandeep.kaul
 *
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class VoteRequest {

	@NotNull
	private int userId;
	// True for Updvotes, False for Downvotes
	@JsonProperty("is_upvote")
	@NotNull
	private boolean isUpvote;
}
