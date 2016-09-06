/**
 * 
 */
package com.sandeepkaul.imdbapi.models;

import lombok.Data;

/**
 * @author sandeep.kaul
 *
 */
@Data
public class ReviewRequest {

	private int userId;
	private String reviewString;
}
