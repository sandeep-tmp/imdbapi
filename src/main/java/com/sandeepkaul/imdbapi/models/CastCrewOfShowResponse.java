/**
 * 
 */
package com.sandeepkaul.imdbapi.models;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author sandeep.kaul
 *
 */
@Data
@AllArgsConstructor
public class CastCrewOfShowResponse {

	private Integer ccMemberId;
	private Integer ccTypeId;
	private String ccName;
	private String ccType;
}
