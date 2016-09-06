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
public class User {

	private Integer id;
	@NotNull
	private String userName;
	@NotNull
	private String email;
	private String phoneNumber;
	private char[] passwordHash;
	private char[] passwordSalt;
	private Date createdOn;
	private Date updatedOn;
	
}
