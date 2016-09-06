/**
 * 
 */
package com.sandeepkaul.imdbapi.models;

import java.util.Date;
import java.util.List;

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
public class ReviewDetails {

	private int reviewId;
	private String reviewText;
	private int userId;
	private int showId;
	private Date reviewDate;
	private int upvotes;
	private int downvotes;
	private List<ReviewComment> reviewComments;
	private int showTypeId;
	
}
