/**
 * 
 */
package com.sandeepkaul.imdbapi.models;

import java.util.Date;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

/**
 * @author sandeep.kaul
 *
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Show {

	private Integer id;
	@NotNull
	private String name;
	@NotNull
	private Integer genreId;
	private Date releaseDate;
	private Date createdOn;
	private Date updatedOn;
	private Integer languageId;
	private Integer showTypeId;
}
