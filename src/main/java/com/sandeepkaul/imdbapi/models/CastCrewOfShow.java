/**
 * 
 */
package com.sandeepkaul.imdbapi.models;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

/**
 * @author sandeep.kaul
 *
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CastCrewOfShow {

	@NotNull
	private Integer ccMemberId;
	@NotNull
	private Integer ccTypeId;
}
