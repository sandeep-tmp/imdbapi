/**
 * 
 */
package com.sandeepkaul.imdbapi.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author sandeep.kaul
 *
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReviewComment {

	private int reviewCommentId;
	private int reviewId;
	private int userId;
	private String comment;
}
